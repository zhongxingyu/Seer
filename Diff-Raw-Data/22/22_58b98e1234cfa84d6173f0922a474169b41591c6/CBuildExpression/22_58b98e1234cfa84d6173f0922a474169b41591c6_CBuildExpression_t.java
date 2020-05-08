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
 
 public class CBuildExpression extends IrSwitch<Boolean> {
     String exprStr="";
     String refStr ="";
     String castStr ="";
     String lastVarStr="";
     List<Expression> indexArray;
     List<Expression> sizeArray;
     Expression expression = null;
     boolean sepIndex = false;
     CEnvironment cenv = null;
     boolean rangechk = false; //TODO be able to control this
     int dim = 0;
     boolean asRef = false;
     int level = 0;
     public CBuildExpression(Expression expression, CEnvironment cenv, boolean asRef, boolean sepIndex) {
         exprStr="";
         refStr="";
         indexArray = new ArrayList<Expression>();
         sizeArray = new ArrayList<Expression>();
         this.expression = expression;
         this.cenv = cenv;
         this.asRef = asRef;
         this.sepIndex = sepIndex;
         level = 0;
         dim = 0;
     }
     
     public CBuildExpression(Expression expression, CEnvironment cenv) {
         this(expression, cenv,false,false);
     }
 
     public String toStr() {
         Boolean res = doSwitch(expression);
         return refStr + castStr + exprStr;
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
 
     public String sizeStr(boolean partial) {
         int n = partial?indexArray.size():0;
         String indexStr = "(__arrayArg) {";
         indexStr += sizeArray.size()-n + ",{";
         for(int i = n; i<sizeArray.size();i++) {
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
 
     public int sizeLen(boolean partial) {
         return sizeArray.size()-(partial?indexArray.size():0);
     }
 
     private void enter(EObject obj) {level++;}
     private void leave() {level--;}
     
     //---------------------util------------------------------------
     //Prints indexStr, but also returns boolean true when the resulting type is not a list
     protected Boolean indexPStr(List<Expression> index, Type varType, String varStr, boolean sep) {
         List<Expression> szExpr = new ArrayList<Expression>();
         List<Expression> indExpr = new ArrayList<Expression>();
         String indexStr ="";
         lastVarStr = varStr;
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
             indexStr += ")";
             //Any more dimensions to multiply sizes?
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
                 exprStr += indexStr;
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
 /* FIXME not used anyway
         switch(typeMember) {
         case unknown:
             CodegenError.err("Expression builder", "unknown placement of member " + member.getName());
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
     
     private boolean directVar(VariableExpression var) {
         VarPlacement varPlacement = VarPlacement.valueOf(TransUtil.getAnnotationArg(var, IrTransformer.VARIABLE_ANNOTATION, "VarPlacement"));
         boolean direct = true;
         switch(varPlacement) {
         case unknown:
             CodegenError.err("Expression builder", "unknown placement of var expr " + var.getVariable().getName());
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
     
     private void printOperator(String operator) {
         if (operator.equals("=")) {
             exprStr += ("==");
         } else if (operator.equals(":=")) {
             exprStr += ("=");
         } else if (operator.equals("and")) {
             exprStr += ("&&");
         } else if (operator.equals("or")) {
             exprStr += ("||");
         } else if (operator.equals("bitand")) {
             exprStr += ("&");
         } else if (operator.equals("bitor")) {
             exprStr += ("|");
         } else if (operator.equals("not")) {
             exprStr += ("!");
         } else if (operator.equals("mod")) {
             exprStr += ("%");
         } else if (operator.equals(">>>")) {
             exprStr += (">>");
         } else {
             exprStr += (operator);
         }           
         
     }
 
     //---------------------caseX-----------------------------------
 
     @Override
     public Boolean caseIntegerLiteral(IntegerLiteral lit) {
         enter(lit);
         exprStr += (Long.toString(lit.getValue()));
         leave();
         return true;
     }
 
     @Override
     public Boolean caseFloatLiteral(FloatLiteral lit) {
         enter(lit);
         exprStr += (Double.toString(lit.getValue()));
         leave();
         return true;
     }
 
     @Override
     public Boolean caseBooleanLiteral(BooleanLiteral lit) {
         enter(lit);
         exprStr += (lit.isValue()?"1":"0");
         leave();
         return true;
     }
 
     @Override
     public Boolean caseStringLiteral(StringLiteral lit) {
         enter(lit);
         exprStr += ("\"" + lit.getValue() + "\"");
         leave();
         return true;
     }
 
     @Override
     public Boolean caseListExpression(ListExpression lit) {
         enter(lit);
         if(dim==0) 
             exprStr += ("{");
         dim++;
         for (Iterator<Expression> i = lit.getExpressions().iterator(); i.hasNext();) {
             Expression l = i.next();            
             doSwitch(l);
             if (i.hasNext()) exprStr += ", ";
         }
         if(!lit.getGenerators().isEmpty()) {
             exprStr += "/* Don't know what to do with a generator yet "+ lit.toString() +"*/";
             CodegenError.err("Expression builder", "Don't know what to do with a generator yet "+ lit.toString() + ", check output!");
         }
         dim--;
         if(dim==0) {
             exprStr += ("}");
             if(lit.getExpressions().get(0).getType() != null)
                 castStr = "(" + new CBuildTypeName(lit.getExpressions().get(0).getType(), new CPrintUtil.dummyCB(),false).toStr() +"[])";
         }
         leave();
         return true;
     }
 
     @Override
     public Boolean caseVariableExpression(VariableExpression var) {
         enter(var);
         VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(var, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
 
         switch(varType) {
         case actorVar:
             exprStr += ("thisActor->");
             break;
         case importConstVar:
         case constVar:
             exprStr += TransUtil.getNamespaceAnnotation(var) + "__";
             break;
         default:
         }
         String varStr = (CPrintUtil.validCName(var.getVariable().getName()));
         exprStr += varStr;
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
             exprStr += ".p";
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
             if(!sepIndex) {
                 exprStr += ".p";
             }
         default:
         }
         boolean hasIndex = indexPStr(var.getIndex(),((Variable)var.getVariable()).getType(),varStr,sep);
         boolean direct = directVar(var);
         int nbrMembers = var.getMember().size();
         boolean inSep = sepIndex;
         sepIndex = false;
         for (Member m : var.getMember()) {
             nbrMembers--;
             if(nbrMembers==0){
                 sepIndex = inSep;
             }
             exprStr += ((direct||hasIndex)?".":"->");
             hasIndex = caseMember(m);
             direct = directMember(m);
         }
         if(asRef && ((var.getMember().isEmpty()  && !var.getIndex().isEmpty()) || sep || sepIndex) && level == 1) {
             refStr = "&";
         }
         leave();
         return true;
     }
     
     @Override
     public Boolean caseMember(Member member) {
         enter(member);
         String varStr = (CPrintUtil.validCName(member.getName()));
         exprStr += "members." + varStr;
         if(UtilIR.isList(member.getType()) && !sepIndex) {
             exprStr += ".p";
         }
         if(asRef && !member.getIndex().isEmpty()) {
             refStr = "&";
         } else {
             //When deeper members overwrite any &
             refStr = "";
         }
         boolean hasIndex = indexPStr(member.getIndex(),member.getType(), varStr, sepIndex);
         leave();
         return hasIndex;
     }
 
     @Override
     public Boolean caseBinaryExpression(BinaryExpression expr) {
         // Ugly hack starts
         // This is a solution JUST to get the test examples compiling
         // The '..' operator shall be not be around at this stage, 
         // but for that we need some dead code removal to clean it up.
         // So for, now we just print 'null'
         /*
         if (expr.getOperator().equals("..")) {
             exprStr += ("0");
             return true;
         }
         */
         // Ugly ends
         
         enter(expr);
         exprStr += "(";
         doSwitch(expr.getOperand1());
         exprStr += " ";
         printOperator(expr.getOperator());
         exprStr += " ";
         doSwitch(expr.getOperand2());
         exprStr += ")";
         leave();
         return true;
     }
 
     @Override
     public Boolean caseUnaryExpression(UnaryExpression expr) {
         enter(expr);
         exprStr += "(";
         String operator = expr.getOperator();
         if (operator.equals("not")) {
             exprStr += ("!");
         } else if (operator.equals("#")) {
             exprStr += ("sizeof");
         } else if (operator.equals("old")) {
             exprStr += ("/* Usage of unary operator old is not supported! */");
             CodegenError.err("Expression builder", "Usage of unary operator old is not supported, check output!");
         } else {
             exprStr += (operator);
         }           
         exprStr += "(";
         doSwitch(expr.getOperand());
         exprStr += "))";
         
         leave();
         return true;
     }
 
     @Override
     public Boolean caseFunctionCall(FunctionCall expr) {
         enter(expr);
         VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(expr.getFunction(), IrTransformer.VARIABLE_ANNOTATION, "VarType"));
         VarAccess varAccess = VarAccess.valueOf(TransUtil.getAnnotationArg(expr.getFunction(), IrTransformer.VARIABLE_ANNOTATION, "VarAccess"));
         String typeUsage = TransUtil.getAnnotationArg(expr.getFunction(), IrTransformer.TYPE_ANNOTATION, "TypeUsage");
         String thisStr = "";
         String extraParamStr = "";
         String nameStr="";
         Declaration f = null;
         boolean print = false;
         switch(varType) {
         case func: //functions declared inside same namespace-level and likely called from a function or an initValue
             thisStr = TransUtil.getNamespaceAnnotation(expr.getFunction()) + "__";
             f = ((VariableExpression) expr.getFunction()).getVariable();
             Variable funcFunc = (Variable) ((f instanceof ForwardDeclaration)?((ForwardDeclaration)f).getDeclaration():f);
             nameStr = CPrintUtil.validCName(funcFunc.getName());
             print = true;
             break;
         case importFunc: //calling an imported function from any namespace including ours from inside an actor
             thisStr = CPrintUtil.getNamespace(((VariableImport)(((VariableExpression)expr.getFunction()).getVariable())).getNamespace()) + "__";
             VariableImport funcImport = (VariableImport) ((VariableExpression) expr.getFunction()).getVariable();
             nameStr = CPrintUtil.validCName(funcImport.getName());
             print = true;
             break;
         case actorFunc: //calling a function declared in the actor scope
             extraParamStr = ("thisActor");
             if(!expr.getParameters().isEmpty())
                 extraParamStr += (", ");
             f = ((VariableExpression) expr.getFunction()).getVariable();
             Variable funcActor = (Variable) ((f instanceof ForwardDeclaration)?((ForwardDeclaration)f).getDeclaration():f);
             thisStr ="__";
             nameStr = CPrintUtil.validCName(funcActor.getName());
             print = true;
             break;
         case externFunc: //calling a c-function
             Declaration decl = ((VariableExpression) expr.getFunction()).getVariable();
             VariableExternal e=null;
             List<String> namespaceStr = null;
             if(decl instanceof VariableImport) {
                 VariableImport funcExtern = (VariableImport) decl;
                 f = null;
                 try {
                     f = ActorDirectory.findVariable(funcExtern);
                 } catch (DirectoryException ee) {
                     System.err.println("[CBuildExpression] Could not find imported extern function " + funcExtern.getName());
                 }
                 e = (VariableExternal) f;
                 namespaceStr = funcExtern.getNamespace();
             } else if(decl instanceof VariableExternal) {
                 e = (VariableExternal) decl;
                 namespaceStr = TransUtil.getNamespaceAnnotationList(e);
             }
             Namespace ns = null;
             if(e!=null) {
                 try {
                     ns = ActorDirectory.findNamespace(namespaceStr);
                 } catch (DirectoryException ee) {}
                 Map<String,String> annotations = CPrintUtil.getExternAnnotations(CPrintUtil.collectAnnotations(e,ns));
                 nameStr = (CPrintUtil.externalCName(annotations,e));
                 CPrintUtil.toEnvEnv(annotations,cenv);
                 print = true;
             }
             break;
         default:
             exprStr +=("/* TODO FC " +
                     varType.name() +", " +
                     varAccess.name() +", " +
                     typeUsage + " */");
         }
         if(print) {
             exprStr += thisStr + nameStr + "(" + extraParamStr;
             for (int i = 0; i<expr.getParameters().size(); i++) {
                 Expression p = expr.getParameters().get(i);
                 exprStr += new CBuildExpression(p, cenv).toStr();
                 if (i<expr.getParameters().size()-1) exprStr += ", ";
             }
             exprStr += ")";
         }
         leave();
         return true;
     }
 
     @Override
     public Boolean caseTypeConstructorCall(TypeConstructorCall expr) {
         enter(expr);
         VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(expr, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
         VarAccess varAccess = VarAccess.valueOf(TransUtil.getAnnotationArg(expr, IrTransformer.VARIABLE_ANNOTATION, "VarAccess"));
         String typeUsage = TransUtil.getAnnotationArg(expr, IrTransformer.TYPE_ANNOTATION, "TypeUsage");
         exprStr +=("/* TC " +
                 varType.name() +", " +
                 varAccess.name() +", " +
                 typeUsage +" */");
 
         if(UtilIR.isLiteralExpression(expr)) {
             exprStr += "((" + CPrintUtil.validCName(expr.getName()) + "_t) ";
             exprStr += ("{");
             for(Iterator<Expression> i= ((TypeConstructorCall) expr).getParameters().iterator();i.hasNext();) {
                 Expression e = i.next();
                 doSwitch(e);
                 if(i.hasNext()) exprStr += ", ";
             }
             exprStr += ("})");               
         } else {
             exprStr += (CPrintUtil.validCName(expr.getName()));
             exprStr += ("(NULL, ");
             for (Iterator<Expression> i = expr.getParameters().iterator(); i.hasNext();) {
                 Expression p = i.next();
                 doSwitch(p);
                 if (i.hasNext()) exprStr += ", ";
             }
             exprStr += ")";
         }
         leave();
         return true;
     }
 
     @Override
     public Boolean caseIfExpression(IfExpression expr) {
         enter(expr);
         exprStr += ("((");
         doSwitch(expr.getCondition());
         exprStr += (") ? (");
         exprStr += new CBuildExpression(expr.getThenExpression(), cenv, asRef,false).toStr();
         exprStr += ("):(");
         exprStr += new CBuildExpression(expr.getElseExpression(), cenv, asRef,false).toStr();
         exprStr += ("))");
         leave();
         return true;
     }
 
     @Override
     public Boolean caseExpression(Expression object) {
         enter(object);
         //Generic Expression is used as a special case to print the type of the expression or NULL
         //This is used for example when using sizeof() or any c-function that takes a pointer but we have nothing to supply
         if(object.getType() instanceof TypeUndef)
             exprStr += ("NULL");
         else {
             int i=0;
             Type type = object.getType();
             while(type instanceof TypeList) {
                 type = ((TypeList) type).getType();
                 i++;
             }
             exprStr += new CBuildTypeName(type,null, true).toStr();
             for(;i>0;i--)
                 exprStr += ("*");
         }
         leave();
         return true;
     }
 
 }
