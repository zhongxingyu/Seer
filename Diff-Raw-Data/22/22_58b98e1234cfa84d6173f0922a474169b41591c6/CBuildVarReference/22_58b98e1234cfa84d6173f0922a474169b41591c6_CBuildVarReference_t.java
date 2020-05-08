 /* 
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.caltoopia.codegen.printer;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.caltoopia.ast2ir.Stream;
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.cli.ActorDirectory;
 import org.caltoopia.cli.DirectoryException;
 import org.caltoopia.codegen.CEnvironment;
 import org.caltoopia.codegen.CodegenError;
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.transformer.IrTransformer;
 import org.caltoopia.codegen.transformer.TransUtil;
 import org.caltoopia.codegen.transformer.analysis.IrTypeStructureAnnotation.TypeMember;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarAccess;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarLocalAccess;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarType;
 import org.caltoopia.codegen.transformer.analysis.IrVariablePlacementAnnotation.VarPlacement;
 import org.caltoopia.ir.BinaryExpression;
 import org.caltoopia.ir.BooleanLiteral;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.FloatLiteral;
 import org.caltoopia.ir.ForwardDeclaration;
 import org.caltoopia.ir.FunctionCall;
 import org.caltoopia.ir.IfExpression;
 import org.caltoopia.ir.IntegerLiteral;
 import org.caltoopia.ir.ListExpression;
 import org.caltoopia.ir.Member;
 import org.caltoopia.ir.Namespace;
 import org.caltoopia.ir.StringLiteral;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeConstructorCall;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeUndef;
 import org.caltoopia.ir.UnaryExpression;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableExpression;
 import org.caltoopia.ir.VariableExternal;
 import org.caltoopia.ir.VariableImport;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.util.IrSwitch;
 import org.eclipse.emf.ecore.EObject;
 
 public class CBuildVarReference extends IrSwitch<Boolean> {
     String refStr="";
     String flagsStr = "";
     String ref2Str="";
     String lastVarStr = "";
     List<Expression> indexArray;
     List<Expression> sizeArray;
     VariableReference reference = null;
     boolean rangechk = false; //TODO be able to control this
     int level = 0;
     boolean asRef = false;
     boolean sepIndex = false;
     boolean lastMember = false;
     CEnvironment cenv = null;
     public CBuildVarReference(VariableReference reference, CEnvironment cenv, boolean asRef, boolean sepIndex) {
         refStr="";
         ref2Str="";
         this.flagsStr = "";
         indexArray = new ArrayList<Expression>();
         sizeArray = new ArrayList<Expression>();
         this.reference = reference;
         this.cenv = cenv;
         this.asRef = asRef;
         this.sepIndex = sepIndex;
     }
     
     public CBuildVarReference(VariableReference reference, CEnvironment cenv) {
         this(reference,cenv,false,false);
     }
     
     public String toStr() {
         Boolean res = doSwitch(reference);
         return ref2Str + refStr;
     }
 
     public String flagsStr() {
         return flagsStr + ".flags";
     }
 
     public String indexStr() {
         String indexStr = "(__arrayArg) {";
         indexStr += indexArray.size() + ",{";
         for(Iterator<Expression> i = indexArray.iterator();i.hasNext();) {
             Expression e = i.next();
             indexStr += new CBuildExpression(e, cenv).toStr();
             if(i.hasNext()) indexStr += ", ";
         }
         indexStr += "}}";
         return indexStr;
     }
 
     public String sizeStr() {
         String indexStr = "(__arrayArg) {";
         indexStr += sizeArray.size() + ",{";
         for(int i = 0; i<sizeArray.size();i++) {
             Expression e = sizeArray.get(i);
             if(e!=null) {
                 indexStr += new CBuildExpression(e, cenv).toStr();
             } else {
                 indexStr += "/*dynamic*/";
                 indexStr += lastVarStr +".sz[" + i + "]";
             }
             if(i<(sizeArray.size()-1)) indexStr += ", ";
         }
         indexStr += "}}";
         return indexStr;
     }
     
     public int indexLen() {
         return indexArray.size();
     }
 
     public int sizeLen() {
         return sizeArray.size();
     }
 
     private void enter(EObject obj) {level++;}
     private void leave() {level--;}
     
     //---------------------util------------------------------------
     //Prints indexStr, but also returns boolean true when the resulting type is not a list
     protected Boolean indexPStr(List<Expression> index, Type varType, String varStr, boolean sep) {
         List<Expression> szExpr = new ArrayList<Expression>();
         List<Expression> indExpr = new ArrayList<Expression>();
         lastVarStr = varStr;
         String indexStr ="";
         if(index!=null && !index.isEmpty() && varType instanceof TypeList) {
             Type list = varType;
             while(list instanceof TypeList) {
                 szExpr.add(((TypeList)list).getSize());
                 list = ((TypeList)list).getType();
             }
             list = varType;
             indexStr += ("[");
             for (int i = 0; i<index.size();i++) {
                 indexStr += ("(");
             }
             for (int i = 0; i<index.size();i++) {
                 Expression e = index.get(i);
                 indExpr.add(e);
                 if(i>0) {
                     indexStr += ")*";
                     if(szExpr.get(i)==null) {
                         indexStr += varStr + ".sz["+ (i-1) +"]+";
                     } else {
                         indexStr += new CBuildExpression(szExpr.get(i),cenv).toStr() +"+";
                     }
                 }
                 if(rangechk) {
                     indexStr += ("RANGECHK(");
                 }
                 indexStr += new CBuildExpression(e, cenv).toStr();
                 if(rangechk) {
                     indexStr += (",");
                     indexStr += varStr + ".sz["+ i +"]";
                     indexStr += (")");
                 }
                 list = ((TypeList)list).getType();
             }
             indexStr += (")");
             //Any more dimensions size to multiply with?
             if(list instanceof TypeList) {
                 int i = index.size();
                 Type t = list;
                 while(t instanceof TypeList) {
                     indexStr += "*";
                     if(szExpr.get(i)==null) {
                         indexStr += varStr + ".sz["+ (i) +"]";
                     } else {
                         indexStr += new CBuildExpression(szExpr.get(i),cenv).toStr();
                     }
                     i++;
                     t = ((TypeList)t).getType();
                 }
             }
             indexStr += ("]");
             if(sep) {
                 indexArray.addAll(indExpr);
                 sizeArray.addAll(szExpr);
             } else {
                 refStr += indexStr;
             }
             return !(list instanceof TypeList);
         }
         Type list = varType;
         while(list instanceof TypeList) {
            sizeArray.add(((TypeList)list).getSize());
             list = ((TypeList)list).getType();
         }
         return false;
     }
     
     
     private boolean directMember(Member member) {
         TypeMember typeMember = TypeMember.valueOf(TransUtil.getAnnotationArg(member, IrTransformer.TYPE_ANNOTATION, "TypeStructure"));
         boolean direct = true;
 /*      FIXME Not used anyway
         switch(typeMember) {
         case unknown:
             CodegenError.err("Var ref builder", "unknown placement of member " + member.getName());
         case builtin:
         case byListSome: //Used when list of decided size and inlined  but have deeper members that are not
         case byListFull: //Used when list of decided size and inlined and all deeper members also (including lists of builtins)
         case inlineSome: //Used when user type that is inlined but have deeper members that are not
         case inlineFull: //Used when user type is inlined and all deeper members also
             direct = true;
             break;
         case byRef: //Used when either type (or list of non-decided size?)
             direct = false;
             break;
         default:
         }*/
         return direct;
     }
     
     private boolean directVar(VariableReference var) {
         VarPlacement varPlacement = VarPlacement.valueOf(TransUtil.getAnnotationArg(var, IrTransformer.VARIABLE_ANNOTATION, "VarPlacement"));
         boolean direct = true;
         switch(varPlacement) {
         case unknown:
             CodegenError.err("var ref builder", "unknown placement of var expr " + var.getDeclaration().getName());
         case constant:
         case actor: //Placed in constructor
         case auto:
         case heap:
         case code:
         case autoDeepHeap:
         case autoListDeepHeap:
         case autoListHeap:
             direct = true;
             break;
         case ref: //used when passing as parameter as ref
         case fifo:
         case fifoDeepFifo:
         case autoListDeepFifo:
         case autoListFifo:
             direct = false;
             break;
         default:
         }
         return direct;
     }
 
     //---------------------caseX-----------------------------------
 
     @Override
     public Boolean caseVariableReference(VariableReference var) {
         enter(var);
         VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(var, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
 
         switch(varType) {
         case actorVar:
             refStr += ("thisActor->");
             break;
         case importConstVar:
         case constVar:
             refStr += TransUtil.getNamespaceAnnotation(var) + "__";
             break;
         default:
         }
         String varStr = (CPrintUtil.validCName(var.getDeclaration().getName()));
         refStr += varStr;
         VarLocalAccess vla = VarLocalAccess.valueOf(TransUtil.getAnnotationArg(var, IrTransformer.VARIABLE_ANNOTATION, "VarLocalAccess"));
         boolean sep = false;
         switch(vla) {
         case listMemberScalar:
         case listMemberList:
         case listMemberListMulti:
         case listMemberScalarUserType:
         case listMemberListUserType:
         case listMemberListMultiUserType:
         case listMemberListSingle:
         case listMemberListMultiSingle:
         case listMemberListUserTypeSingle:
         case listMemberListMultiUserTypeSingle:
         case listMemberListMultiList:
         case listMemberListMultiUserTypeList:
             refStr += ".p";
             flagsStr = refStr;
             break;
         case list:
         case listUserType:
         case listMulti:
         case listMultiUserType:
         case listSingle:
         case listMultiSingle:
         case listUserTypeSingle:
         case listMultiUserTypeSingle:
         case listMultiList:
         case listMultiUserTypeList:
             sep = sepIndex;
             flagsStr = refStr;
             if(!sepIndex) {
                 refStr += ".p";
             }
         default:
         }
 
         boolean hasIndex = indexPStr(var.getIndex(),var.getDeclaration().getType(), varStr, sep);
         boolean direct = directVar(var);
         int nbrMembers = var.getMember().size();
         boolean inSep = sepIndex;
         sepIndex = false;
         for (Member m : var.getMember()) {
             nbrMembers--;
             if(nbrMembers==0){
                 sepIndex = inSep;
                 lastMember = true;
             }
             refStr += ((direct||hasIndex)?".":"->");
             hasIndex = caseMember(m);
             direct = directMember(m);
         }
 
         if(asRef && ((var.getMember().isEmpty()  && !var.getIndex().isEmpty()) || sep || sepIndex) && level == 1) {
             ref2Str += "&";
         }
 
         leave();
         return true;
     }
     
     @Override
     public Boolean caseMember(Member member) {
         enter(member);
         String varStr = (CPrintUtil.validCName(member.getName()));
         refStr += "members." + varStr;
         if(UtilIR.isList(member.getType()) && !sepIndex) {
             if(lastMember) {
                 flagsStr = refStr;
             }
             refStr += ".p";
         }
         if(asRef && !member.getIndex().isEmpty()) {
             ref2Str = "&";
         } else {
             //When deeper members overwrite any &
             ref2Str = "";
         }
 
         boolean hasIndex = indexPStr(member.getIndex(),member.getType(), varStr, sepIndex);
         leave();
         return hasIndex;
     }
 }
