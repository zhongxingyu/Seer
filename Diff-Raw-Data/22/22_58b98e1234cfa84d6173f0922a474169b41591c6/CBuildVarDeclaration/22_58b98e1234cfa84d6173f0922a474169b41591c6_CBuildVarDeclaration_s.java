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
 
 import java.io.File;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.caltoopia.codegen.CEnvironment;
 import org.caltoopia.codegen.CodegenError;
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.printer.CPrintUtil.listStarCB;
 import org.caltoopia.codegen.transformer.IrTransformer;
 import org.caltoopia.codegen.transformer.TransUtil;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarAccess;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarLocalAccess;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarType;
 import org.caltoopia.ir.IntegerLiteral;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeBool;
 import org.caltoopia.ir.TypeDeclaration;
 import org.caltoopia.ir.TypeExternal;
 import org.caltoopia.ir.TypeFloat;
 import org.caltoopia.ir.TypeInt;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.TypeRecord;
 import org.caltoopia.ir.TypeString;
 import org.caltoopia.ir.TypeUint;
 import org.caltoopia.ir.TypeUndef;
 import org.caltoopia.ir.TypeUser;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.util.IrSwitch;
 import org.eclipse.emf.ecore.EObject;
 
 public class CBuildVarDeclaration extends IrSwitch<Boolean> {
     String vtypeStr="";
     String varStr="";
     String dimStr="";
     String sizeStr = "";
     int maxDim = 0;
     Variable variable;
     boolean onlyVar = false;
     boolean allFixedSize = true;
     CEnvironment cenv = null;
     
     public CBuildVarDeclaration(Variable variable, CEnvironment cenv, boolean onlyVar) {
         vtypeStr="";
         varStr="";
         dimStr="";
         maxDim = 0;
         sizeStr = "";
         allFixedSize = true;
         this.variable = variable;
         this.onlyVar = onlyVar;
         this.cenv = cenv;
     }
     
     public String toStr() {
         Boolean res = doSwitch(variable);
         if(!res) {
             CodegenError.err("Var declaration builder", vtypeStr + " " + varStr);
         }
         return (onlyVar?"":vtypeStr + " ") + varStr + dimStr;
     }
     
     //-----------Util--------------------------------------------------------
     
     private void enter(EObject obj) {}
     private void leave() {}
     
     //---------------Type callbacks -----------------------------------------
     protected class varCB implements ITypeCallbacks {
         public String preTypeFn(Type type) {
             // TODO Auto-generated method stub
             return "";
         }
 
         public String postTypeFn(Type type) {
             /*VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(type, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
             VarAccess varAccess = VarAccess.valueOf(TransUtil.getAnnotationArg(type, IrTransformer.VARIABLE_ANNOTATION, "VarAccess"));
             String typeUsage = TransUtil.getAnnotationArg(type, IrTransformer.TYPE_ANNOTATION, "TypeUsage");*/
 //            varStr +=("/* " +
 //                    varType.name() +", " +
 //                    varAccess.name() +", " +
 //                    typeUsage +" */");
             return "";
         }
 
         public String listTypeFn(TypeList type,int dim) {
             if(!onlyVar) {
                 if(dim>1) {
                    dimStr += ", ";
                 }
                 if(type.getSize()!=null) {
                     String sz = (new CBuildExpression(type.getSize(),cenv).toStr());
                    dimStr += sz;
                     sizeStr += "*" + sz;
                 } else {
                     allFixedSize = false;
                    dimStr += "0";
                 }
             }
             if(dim>maxDim) {
                 maxDim = dim;
             }
             return "";
         }
 
         public String userTypeFn(TypeUser type) {
             // TODO Auto-generated method stub
             return "";
         }
     }
 
     //------------------------VARIABLES, FUNC, PROC, ETC DECLARATIONS---------------------------------------
     //------------------util------
     private void buildVarDeclaration(Variable variable, boolean initialize) {
         CBuildTypeName tn = new CBuildTypeName(variable.getType(),new varCB(),true);
         vtypeStr = tn.toStr();
         varStr = CPrintUtil.validCName(variable.getName())+varStr;
         if(!dimStr.equals("") && initialize) {
             String tmpStr = " = {";
             if(allFixedSize) {
                 tmpStr += "malloc(sizeof(" + tn.toFinalTypeStr() +")" + sizeStr + "), ";
                 tmpStr += (TransUtil.getAnnotationArg(variable, "Variable", "VarLocalAccess").equals(VarLocalAccess.temp.name()))?"0xf":"0x7";
                 tmpStr += ", ";
             } else {
                 tmpStr += "NULL, ";
                 tmpStr += (TransUtil.getAnnotationArg(variable, "Variable", "VarLocalAccess").equals(VarLocalAccess.temp.name()))?"0x8":"0x0";
                 tmpStr += ", ";
             }
             dimStr = tmpStr + maxDim + ", {" + dimStr + "}}";
         }
     }
     
     private void buildParamDeclaration(Variable variable) {
         vtypeStr = new CBuildTypeName(variable.getType(),new CPrintUtil.dummyCB(),true).toStr();
         varStr = CPrintUtil.validCName(variable.getName())+varStr;
     }
 
     @Override
     public Boolean caseVariable(Variable variable) {
         enter(variable);
         VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(variable, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
         boolean initialize = true;
         switch(varType) {
         case memberDeclType:
             initialize = false;
             buildVarDeclaration(variable, initialize);
             dimStr = "";
             break;
         case actorVar:
         case funcVar:
         case procVar:
         case generatorVar:
         case blockVar:
             buildVarDeclaration(variable, initialize);
             break;
         case funcInParamVar:
         case procInParamVar:
             buildParamDeclaration(variable);
             break;
         default:
             varStr += ("/*TODO BVD "+variable.getName() + " of varType " + varType.name() + " */");
         }
         leave();
         return true;
     }
 
 }
