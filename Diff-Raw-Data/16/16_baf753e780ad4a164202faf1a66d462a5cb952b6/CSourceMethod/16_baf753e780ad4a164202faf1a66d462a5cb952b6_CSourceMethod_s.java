 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: CSourceMethod.java,v 1.8 2005-05-31 09:03:28 meffert Exp $
  */
 
 package org.caesarj.compiler.export;
 
 import java.util.ArrayList;
 
 import org.caesarj.classfile.ClassFileFormatException;
 import org.caesarj.classfile.CodeEnv;
 import org.caesarj.classfile.CodeInfo;
 import org.caesarj.classfile.LocalVariableScope;
 import org.caesarj.classfile.MethodInfo;
 import org.caesarj.compiler.ast.phylum.statement.JBlock;
 import org.caesarj.compiler.ast.phylum.variable.JFormalParameter;
 import org.caesarj.compiler.codegen.CodeSequence;
 import org.caesarj.compiler.context.GenerationContext;
 import org.caesarj.compiler.optimize.BytecodeOptimizer;
 import org.caesarj.compiler.types.CReferenceType;
 import org.caesarj.compiler.types.CType;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.util.SimpleStringBuffer;
 
 /**
  * This class represents an exported member of a class (fields)
  */
 public class CSourceMethod extends CMethod {
 
   // ----------------------------------------------------------------------
   // CONSTRUCTORS
   // ----------------------------------------------------------------------
 
   /**
    * Constructs a method export.
    *
    * @param	owner		the owner of this method
    * @param	modifiers	the modifiers on this method
    * @param	ident		the ident of this method
    * @param	returnType	the return type of this method
    * @param	paramTypes	the parameter types of this method
    * @param	exceptions	a list of all exceptions in the throws list
    * @param	deprecated	is this method deprecated
    * @param	body		the source code
    */
 //  public CSourceMethod(CClass owner,
 //		       int modifiers,
 //		       String ident,
 //		       CType returnType,
 //		       CType[] paramTypes,
 //		       CReferenceType[] exceptions,
 //		       boolean deprecated,
 //		       boolean synthetic,
 //		       JBlock body)
 //  {
 //    super(owner, modifiers, ident, returnType, paramTypes, exceptions, deprecated, synthetic);
 //    this.body = body;
 //    this.fparameters = new JFormalParameter[0];
 //  }
   
   /**
    * Constructs a method export.
    *
    * @param	owner		the owner of this method
    * @param	modifiers	the modifiers on this method
    * @param	ident		the ident of this method
    * @param	returnType	the return type of this method
    * @param params		the full parameter information (necessary for local variable table)
    * @param	paramTypes	the parameter types of this method
    * @param	exceptions	a list of all exceptions in the throws list
    * @param	deprecated	is this method deprecated
    * @param	body		the source code
    */
   public CSourceMethod(CClass owner,
 	       int modifiers,
 	       String ident,
 	       CType returnType,
 		   JFormalParameter[] params,
 	       CType[] paramTypes,
 	       CReferenceType[] exceptions,
 	       boolean deprecated,
 	       boolean synthetic,
 	       JBlock body)
 	{
   		super(owner, modifiers, ident, returnType, paramTypes, exceptions, deprecated, synthetic);
   		this.body = body;
   		if(params != null){
   			this.fparameters = params;
   		}else{
   			this.fparameters = new JFormalParameter[0];
   		}
 	}
 
   // ----------------------------------------------------------------------
   // ACCESSORS
   // ----------------------------------------------------------------------
 
   public boolean isUsed() {
     return used || !isPrivate() || isSynthetic(); //getIdent().indexOf("$") >= 0; // $$$
   }
 
   public void setUsed() {
     used = true;
   }
 
   public void setBody(JBlock body) {
     this.body = body;
   }
 
   public void setSynthetic(boolean syn) {
     synthetic = syn;
   }
 
   public void addSuperMethod(CMethod superMethod) {
     verify(superMethod != null);
     if(superMethods == null) {
       superMethods = new ArrayList();
     }
     superMethods.add(superMethod);
   }
   public CMethod[] getSuperMethods() {
     if (superMethods == null) {
       return CMethod.EMPTY;
     } else{
       return (CMethod[])superMethods.toArray(new CMethod[superMethods.size()]); 
     }
   }
   
   /**
    * @return the formal parameters for this method
    */
   public JFormalParameter[] getFormalParameters(){
   	return this.fparameters;
   }
   
   /**
    * @param params the formal parameters for this method
    */
   protected void setFormalParameters(JFormalParameter[] params){
   	this.fparameters = params;
   }
  
   /**
    * Find out that pre/postcondition of supermethod is already checked by 
    * pre/postcondition of another method.
    */
   public boolean includesSuperMethod(CMethod method) {
     if (superMethods == null) {
       return false;
     } else{
       int         size = superMethods.size();
       
       for (int i=0; i < size; i++) {
         if ((superMethods.get(i) == method) || (((CMethod)superMethods.get(i)).includesSuperMethod(method))){
           return true;
         }
       }
       return false;
     }
   }
   // ----------------------------------------------------------------------
   // GENERATE CLASSFILE INFO
   // ----------------------------------------------------------------------
 
   /**
    * Generate the code in a class file
    *
    * @param	optimizer	the bytecode optimizer to use
    */
   public MethodInfo genMethodInfo(BytecodeOptimizer optimizer, TypeFactory factory) throws ClassFileFormatException {
     CReferenceType[]	excs = getThrowables();
     String[]		exceptions = new String[excs.length];
 
     for (int i = 0; i < excs.length; i++) {
       exceptions[i] = excs[i].getQualifiedName();
     }
 
     MethodInfo methodInfo =  new MethodInfo((short)getModifiers(),
                                             getIdent(),
                                             getSignature(),
                                             getGenericSignature(),
                                             exceptions,
                                             body != null ? genCode(factory): null, 
                                             isDeprecated(),
                                             isSynthetic());
     methodInfo = optimizer.run(methodInfo);
     
     return methodInfo;
   }
 
   /**
    * @return the type signature (JVM) of this method
    */
   public String getSignature() {
     CType[]     parameters = getParameters();
 
     if (getOwner().isNested() && isConstructor()) {
       parameters = ((CSourceClass)getOwner()).genConstructorArray(parameters);
     }
 
     SimpleStringBuffer	buffer;
     String		result;
 
     buffer = SimpleStringBuffer.request();
     buffer.append('(');
     for (int i = 0; i < parameters.length; i++) {
       parameters[i].appendSignature(buffer);
     }
     buffer.append(')');
     returnType.appendSignature(buffer);
 
     result = buffer.toString();
     SimpleStringBuffer.release(buffer);
     return result;
   }
 
   /**
    * @return the generic type siganture (attribute) of this method
    * @see #getSignature()
    */
   public String getGenericSignature() {
     CType[]             parameters = getParameters();
     CType               returntype = getReturnType();
     CReferenceType[]        exceptions = getThrowables();
     SimpleStringBuffer	buffer;
     String		result;
 
     buffer = SimpleStringBuffer.request();
     
     // parameter declaration
     buffer.append('(');
     for (int i = 0; i < parameters.length; i++) {
       parameters[i].appendSignature(buffer);
     }
     buffer.append(')');
     // return type
     returntype.appendSignature(buffer);
     // throws clause
     if ((exceptions != null) && (exceptions.length > 0)) {
       buffer.append('^');
       for (int i = 0; i < exceptions.length; i++) {
         exceptions[i].appendSignature(buffer);
       }
     }
     result = buffer.toString();
     SimpleStringBuffer.release(buffer);
     return result;
   }
 
 
   private CodeInfo genByteCode(TypeFactory factory) {
     CodeSequence	code = CodeSequence.getCodeSequence();
 
     GenerationContext   context = new GenerationContext(factory, code);
     
     LocalVariableScope scope = new LocalVariableScope();
     code.pushLocalVariableScope(scope);
     
    // TODO [mef] : add "this" to local variable info
     JFormalParameter[] params = this.getFormalParameters();
 	for(int i = 0; i < params.length; i++){
 		code.addLocalVarInfo(null, params[i]);
 	}
     
     						 
 	body.genCode(context);
     // TODO [mef] : add linenumber attribute for return instruction
     if (getReturnType().getTypeID() == TID_VOID) {
       code.plantNoArgInstruction(opc_return);
     }
 
     code.popLocalVariableScope(scope);
     
     CodeInfo            info = new CodeInfo(code.getInstructionArray(),
                                             code.getHandlers(),
                                             code.getLineNumbers(),
                                             code.getLocalVariableInfos());
 
     code.release();
     body = null;
     return info;
   }
 
   /**
    * Generates JVM bytecode for this method.
    */
   public CodeInfo genCode(TypeFactory factory) throws ClassFileFormatException {
     CodeInfo    info;
     CType[]	parameters = getParameters();
     int		paramCount = 0;
 
     // generate byte code
     info = genByteCode(factory);
 
     // set necessary additional information
     for (int i = 0; i < parameters.length; i++) {
       paramCount += parameters[i].getSize();
     }
     paramCount += getReturnType().getSize();
     paramCount += isStatic() ? 0 : 1;
 
     info.setParameterCount(paramCount);
     // set maxStack, maxLocals, ...
     CodeEnv.check(info);
     return info;
   }
 
   // ----------------------------------------------------------------------
   // DATA MEMBERS
   // ----------------------------------------------------------------------
 //Walter start
 //  private JBlock                body;
   protected JBlock                body;
 //Walter end
 
   private boolean               used;
   private ArrayList             superMethods;
   
   private JFormalParameter[]	fparameters;
 }
