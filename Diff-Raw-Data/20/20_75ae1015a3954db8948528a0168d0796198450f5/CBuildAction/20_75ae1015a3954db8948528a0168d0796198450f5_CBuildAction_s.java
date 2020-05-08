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
 
 import java.util.Iterator;
 
 import org.caltoopia.ast2ir.Stream;
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.ast2ir.Stream.Indent;
 import org.caltoopia.codegen.CEnvironment;
 import org.caltoopia.codegen.CodegenError;
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.printer.CBuildVarDeclaration.varCB;
 import org.caltoopia.codegen.printer.CPrintUtil.dummyCB;
 import org.caltoopia.codegen.transformer.IrTransformer;
 import org.caltoopia.codegen.transformer.TransUtil;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarAccess;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarType;
 import org.caltoopia.ir.Action;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.Expression;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.PortRead;
 import org.caltoopia.ir.PortWrite;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.ReturnValue;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeActor;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.util.IrSwitch;
 import org.eclipse.emf.ecore.EObject;
 
 public class CBuildAction extends IrSwitch<Boolean> {
     String bodyStr="";
     String thisStr="";
     int idNbr=0;
     Action action;
     boolean header = false;
     CEnvironment cenv = null;
     private IndentStr ind = null;
 
    public CBuildAction(Action action, CEnvironment cenv, String thisStr, int idNbr) {
         bodyStr="";
         this.thisStr = thisStr;
         this.idNbr = idNbr;
         this.action = action;
         this.cenv = cenv;
         this.ind = new IndentStr();
     }
     
     public String toStr() {
         Boolean res = doSwitch(action);
         if(!res) {
             CodegenError.err("Action builder", bodyStr);
         }
         return bodyStr;
     }
     
     private void enter(EObject obj) {}
     private void leave() {}
 
     @Override
     public Boolean caseAction(Action action) {
         enter(action);
         String actionId = thisStr + CPrintUtil.getNamespace(action.getTag());
         bodyStr += "ART_ACTION(" + action.getId() + ", " + "ActorInstance_" + thisStr + ")" + ind.nl();
         bodyStr += ind.ind() + ("{") + ind.nl();
         ind.inc();
         bodyStr += ind.ind() + "void * __tempVoidPointer;" +ind.nl();
         bodyStr += ind.ind() + "__array4void __tempArray;" +ind.nl();
         for (Declaration d : action.getDeclarations()) {
             VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(d, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
             switch(varType) {
             case constVar:
             case actorConstVar:
                 bodyStr += ind.ind() + (new CBuildConstDeclaration((Variable) d, cenv,false).toStr()) + ";" + ind.nl();
                 break;
             /*case blockConstVar:
                 bodyStr += ind.ind() + (new CBuildConstDeclaration((Variable) d,cenv, false,true).toStr()) + ";" + ind.nl();
                 break;*/
             //case actorVar:
             //case blockVar:
             //case procVar:
             //case funcVar:
             case actionVar:
             case actionInitInDepVar:
             case outPortVar:
             case outPortInitInDepVar:
                 if(((Variable)d).getInitValue() != null) {
                     //TODO should have separate class for var declaration with initialization
                     bodyStr += ind.ind() + (new CBuildConstDeclaration((Variable) d, cenv, false).toStr()) + ";" + ind.nl();
                 } else {
                     bodyStr += ind.ind() + (new CBuildVarDeclaration((Variable) d,cenv, false).toStr()) + ";" + ind.nl();
                 }
                 break;
             case inPortVar:
             case inPortPeekVar:
                 bodyStr += ind.ind() + (new CBuildVarDeclaration((Variable) d,cenv, false).toStr()) + ";" + ind.nl();
                 break;
             case inOutPortVar:
             case inOutPortPeekVar:
                 bodyStr += ind.ind() + (new CBuildVarDeclaration((Variable) d,cenv, false).toStr()) + ";" + ind.nl();
                 break;
             default:
                 VarAccess varAccess = VarAccess.valueOf(TransUtil.getAnnotationArg(d, IrTransformer.VARIABLE_ANNOTATION, "VarAccess"));
                 String typeUsage = TransUtil.getAnnotationArg(d, IrTransformer.TYPE_ANNOTATION, "TypeUsage");
                 String varStr =(varType.name() +", " +
                         varAccess.name() +", " +
                         typeUsage);
                 bodyStr += ind.ind() + ("/*TODO BAD "+d.getName() + ", " + varStr + " */") +ind.nl();
             } 
         }
 
         bodyStr += ind.ind() + "ART_ACTION_ENTER(" + action.getId() + ", " + idNbr + ");" +ind.nl();
         for (PortRead read: action.getInputs()){
             String portNbr = TransUtil.getAnnotationArg(read, "Port", "index");
             String portStr = "IN" + portNbr+ "_" + TransUtil.getAnnotationArg(read, "Port", "name");
             if(read.getRepeat()==null) {
                 for(VariableReference readVar: read.getVariables()) {
                     VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(readVar, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
                     //Don't save result if never used
                     if(!varType.equals(VarType.peekVar) && !varType.equals(VarType.syncVar)) {
                         bodyStr += ind.ind() + new CBuildVarReference(readVar, cenv).toStr() + " = ";
                     } else {
                         bodyStr += ind.ind();
                     }
                     bodyStr += "pinRead_" + new CBuildTypeName(read.getPort().getType(), new CPrintUtil.dummyCB(), false).toStr();
                     bodyStr += "(" + portStr + ");"+ind.nl();
                 }
             } else {
                 bodyStr += ind.ind() + "{" + ind.nl();
                 ind.inc();
                 String repStr = "__temp" + CPrintUtil.validCName(portStr);
                 bodyStr += ind.ind() + "int " + repStr + "Count, " + repStr + " = " + 
                         new CBuildExpression(read.getRepeat(),cenv).toStr() + ";" + ind.nl();
                 bodyStr += ind.ind() + "for(" + repStr + "Count = 0;" + repStr + "Count<" + repStr + "; "+repStr+"Count++) {" + ind.nl(); 
                 ind.inc();
                 for(VariableReference readVar: read.getVariables()) {
                     VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(readVar, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
                     //Don't save result if never used
                     if(!varType.equals(VarType.peekVar) && !varType.equals(VarType.syncVar)) {
                         bodyStr += ind.ind() + new CBuildVarReference(readVar, cenv, false, true).toStr() + ".p["+ repStr +"Count] = ";
                     } else {
                         bodyStr += ind.ind();
                     }
                     bodyStr += "pinRead_" + new CBuildTypeName(read.getPort().getType(), new CPrintUtil.dummyCB(), false).toStr();
                     bodyStr += "(" + portStr + ");" + ind.nl();
                 }
                 ind.dec();
                 bodyStr += ind.ind() + "}" + ind.nl();
                 ind.dec();
                 bodyStr += ind.ind() + "}" + ind.nl();
             }
         }
         for (Statement s : action.getStatements()) {
             bodyStr += new CBuildStatement(s, cenv, ind,true,action).toStr();
         }
         for (PortWrite write: action.getOutputs()){
             String portNbr = TransUtil.getAnnotationArg(write, "Port", "index");
             String portStr = "OUT" + portNbr+ "_" + TransUtil.getAnnotationArg(write, "Port", "name");
             if(write.getRepeat()==null) {
                 for(Expression writeExpr: write.getExpressions()) {
                     bodyStr += ind.ind() + "pinWrite_" + new CBuildTypeName(write.getPort().getType(), new CPrintUtil.dummyCB(), false).toStr();
                     bodyStr += "(" + portStr + ", " + new CBuildExpression(writeExpr, cenv).toStr() + ");"+ind.nl();
                 }
             } else {
                 bodyStr += ind.ind() + "{" + ind.nl();
                 ind.inc();
                 String repStr = "__temp" + CPrintUtil.validCName(portStr);
                 bodyStr += ind.ind() + "int " + repStr + "Count, " + repStr + " = " + 
                         new CBuildExpression(write.getRepeat(),cenv).toStr() + ";" + ind.nl();
                 bodyStr += ind.ind() + "for(" + repStr + "Count = 0;" + repStr + "Count<" + repStr + "; "+repStr+"Count++) {" + ind.nl(); 
                 ind.inc();
                 for(Expression writeExpr: write.getExpressions()) {
                     bodyStr += ind.ind() + "pinWrite_" + new CBuildTypeName(write.getPort().getType(), new CPrintUtil.dummyCB(), false).toStr();
                     bodyStr += "(" + portStr + ", " + new CBuildExpression(writeExpr, cenv,false,true,false).toStr() + ".p["+ repStr + "Count]);"+ind.nl();
                 }
                 ind.dec();
                 bodyStr += ind.ind() + "}" + ind.nl();
                 ind.dec();
                 bodyStr += ind.ind() + "}" + ind.nl();
             }
         }
         for(Declaration d:action.getDeclarations()) {
             VarType varType = VarType.valueOf(TransUtil.getAnnotationArg(d, IrTransformer.VARIABLE_ANNOTATION, "VarType"));
             if(!varType.equals(VarType.peekVar) && !varType.equals(VarType.syncVar)) {
                 boolean retValue = TransUtil.getAnnotationArg(d, IrTransformer.VARIABLE_ANNOTATION, "VarAssign").equals(IrVariableAnnotation.VarAssign.movedRetAssigned.name());
                 if(!retValue && (d instanceof Variable) && UtilIR.isList(((Variable)d).getType())) {
                     VariableReference varRef = UtilIR.createVarRef((Variable) d);
                     TransUtil.copySelectedAnnotations(varRef, d, new TransUtil.AnnotationsFilter(IrTransformer.VARIABLE_ANNOTATION, new String[]{"VarPlacement"}));
                     CBuildVarReference cVarRefF = new CBuildVarReference(varRef , cenv, false, true);
                     String varStrF = cVarRefF.toStr();
                     bodyStr += ind.ind() + "free" + new CBuildTypeName(((Variable)d).getType(), new CPrintUtil.dummyCB(), false).toStr() + "(&" + varStrF + ", TRUE);" + ind.nl();
                 } else if(!retValue && (d instanceof Variable) && UtilIR.isRecord(((Variable)d).getType())) {
                     VariableReference varRef = UtilIR.createVarRef((Variable) d);
                     TransUtil.copySelectedAnnotations(varRef, d, new TransUtil.AnnotationsFilter(IrTransformer.VARIABLE_ANNOTATION, new String[]{"VarPlacement"}));
                     CBuildVarReference cVarRefF = new CBuildVarReference(varRef , cenv, false, true);
                     String varStrF = cVarRefF.toStr();
                     bodyStr += ind.ind() + "freeStruct" + new CBuildTypeName(((Variable)d).getType(), new CPrintUtil.dummyCB(), false).toStr() + "(&" + varStrF + ", TRUE);" + ind.nl();
                 }
             }
         }
         bodyStr += ind.ind() + "ART_ACTION_EXIT(" + action.getId() + ", " + idNbr + ");" +ind.nl();
         ind.dec();
         bodyStr += ind.ind() + ("}") + ind.nl();
         leave();
         return true;
     }
 
 }
