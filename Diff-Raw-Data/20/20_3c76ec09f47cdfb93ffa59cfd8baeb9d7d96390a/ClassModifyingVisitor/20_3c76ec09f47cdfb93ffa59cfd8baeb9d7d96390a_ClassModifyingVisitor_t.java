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
 * $Id: ClassModifyingVisitor.java,v 1.20 2005-11-17 15:27:30 klose Exp $
  */
 
 package org.caesarj.mixer.intern;
 
 import java.util.Collection;
 import java.util.Vector;
 
 import org.aspectj.apache.bcel.Constants;
 import org.aspectj.apache.bcel.classfile.Attribute;
 import org.aspectj.apache.bcel.classfile.Code;
 import org.aspectj.apache.bcel.classfile.Constant;
 import org.aspectj.apache.bcel.classfile.ConstantClass;
 import org.aspectj.apache.bcel.classfile.ConstantFieldref;
 import org.aspectj.apache.bcel.classfile.ConstantMethodref;
 import org.aspectj.apache.bcel.classfile.ConstantNameAndType;
 import org.aspectj.apache.bcel.classfile.ConstantPool;
 import org.aspectj.apache.bcel.classfile.ConstantUtf8;
 import org.aspectj.apache.bcel.classfile.DescendingVisitor;
 import org.aspectj.apache.bcel.classfile.EmptyVisitor;
 import org.aspectj.apache.bcel.classfile.Field;
 import org.aspectj.apache.bcel.classfile.InnerClass;
 import org.aspectj.apache.bcel.classfile.InnerClasses;
 import org.aspectj.apache.bcel.classfile.JavaClass;
 import org.aspectj.apache.bcel.classfile.LocalVariable;
 import org.aspectj.apache.bcel.classfile.Method;
 import org.aspectj.apache.bcel.generic.ClassGen;
 import org.aspectj.apache.bcel.generic.ObjectType;
 import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.verifier.VerificationResult;
import org.aspectj.apache.bcel.verifier.Verifier;
import org.aspectj.apache.bcel.verifier.VerifierFactory;
 import org.caesarj.compiler.constants.CaesarConstants;
 import org.caesarj.mixer.MixerException;
 
 /**
  * Modify a <code>JavaClass</code> to have a different class, superclass and outerclass.
  * Performs the following changes:
  * 1. Change className
  * 2. Change superclassName
  * 3. Change type of local this-variable
  * 4. Change super-constructor calls
  * 5. Change cosntructor signatures
  *
  * It is asserted that the modified class is not used as an argument, returntype or field type 
  * by itself. 
  *  
  * @author Karl Klose
  */
 public class ClassModifyingVisitor extends EmptyVisitor  {
 
 	protected String[]	outerClasses;
 	
 	protected String 	oldClassName; 
 	protected String 	newClassName; 
 	protected String 	newSuperclassName;
 	protected String 	newOuterClassName;
 	protected String 	oldOuterClassName;
 	protected String 	oldSuperclassName;
 	protected String 	outerOfOldSuper; 
 	protected String 	outerOfNewSuper;
 
     private Collection<String> anonymousInners = new Vector<String>();
 		
     static class ClassStructure{
         public final String 
             className,
             outerOfClassName,
             superName,
             outerOfSuper;
             
         public ClassStructure(final String className,
                 final String outerOfClassName, final String superName,
                 final String outerOfSuper) {
             super();
             this.className = className;
             this.outerOfClassName = outerOfClassName;
             this.superName = superName;
             this.outerOfSuper = outerOfSuper;
         }
     }
     
 	/**
 	 * Create a visitor to modify a class file
 	 * @param oldClassName	The original class name
 	 * @param newClassName	The new class name
 	 * @param newSuperclassName	The new name of super class
 	 * @param outerClassName	name of the outerclass
 	 */
 	protected ClassModifyingVisitor( 
 			String oldClassName, 
 			String newClassName, 
 			String newSuperclassName,
 			String outerClassName,
 			String outerOfOldSuper,
 			String outerOfNewSuper,
 			String []outers ) {
 		this.oldClassName = oldClassName;
 		this.newClassName = newClassName;
 		this.newSuperclassName = newSuperclassName;
 		this.newOuterClassName = outerClassName;
 		this.outerOfOldSuper = outerOfOldSuper;
 		this.outerOfNewSuper = outerOfNewSuper;
 		outerClasses = outers;
 	}
 		
 	protected JavaClass transform(JavaClass clazz) throws MixerException {
         // empty collection of anonymous inner classes
         anonymousInners = new Vector<String>();
 
         oldOuterClassName = Tools.getOuterClass(clazz,oldClassName);
 		oldSuperclassName = clazz.getSuperclassName();
 		
 		// create a copy as work base
 		JavaClass newClass = clazz.copy();
 		
 		// find indices of class and super class name
 		int classNameIndex = newClass.getClassNameIndex(),
 		superclassNameIndex = newClass.getSuperclassNameIndex();
 		ConstantClass 	cc = (ConstantClass)newClass.getConstantPool().getConstant(classNameIndex),
 						csc = (ConstantClass)newClass.getConstantPool().getConstant(superclassNameIndex);
 		classNameIndex = cc.getNameIndex();
 		superclassNameIndex = csc.getNameIndex();
 		
 		// Set new class & superclass name
 		newClass.getConstantPool().setConstant(superclassNameIndex, new ConstantUtf8(newSuperclassName));
 		newClass.getConstantPool().setConstant(classNameIndex, new ConstantUtf8(newClassName));
 		
 		
 		// visit fields, methods and local variables to replace type references
 		new DescendingVisitor(newClass, this).visit();
 		
 		// Delete all inner class references 
 		Attribute[] atts = newClass.getAttributes();
 		Vector<Attribute>	v = new Vector<Attribute>();
 		for (int i = 0; i < atts.length; i++) {
 			Attribute attribute = atts[i];
 			if (attribute.getTag() == Constants.ATTR_INNER_CLASSES){
 				InnerClasses ic = (InnerClasses)attribute;
 				ic.setInnerClasses(new InnerClass[0]);
 				ic.setLength(2);
 				
 			}
 			v.add( attribute );
 		}
 		atts = v.toArray(new Attribute[v.size()]);
 		newClass.setAttributes(atts);
 		
 
 		newClass = removeFactoryMethods(newClass);
 		
 		// Mixin classes must be abstract, because they do not implement factory methods,
         // (if the class is final => it was a anonymous inner class)
         if (!newClass.isFinal()){
             newClass.setAccessFlags(newClass.getAccessFlags() | Constants.ACC_ABSTRACT);
         }
 
 		// take a look at all methodrefs
 		modifyMethodAndFieldRefs(newClass);
 
        
 		// return generated class
 		return newClass;	
 	}
 
 	/**
 	 * Remove all methods from <code>clazz</code> whose names start
 	 * with '$new'.
 	 * @param clazz	The class to modify	
 	 * @return	The class with removed methods
 	 */
 	protected JavaClass removeFactoryMethods( JavaClass clazz ){
 		ClassGen gen = new ClassGen(clazz);
 		
 		Method[] methods = gen.getMethods();
 		for (int i = 0; i < methods.length; i++) {
 			Method method = methods[i];
 			if (method.getName().startsWith(CaesarConstants.FACTORY_METHOD_PREFIX)){
 				gen.removeMethod(method);
 			}
 		}
 		
 		return gen.getJavaClass();
 	}
     
 	/**
 	 * Checks method references to super-constructors and outerclass methods
 	 * and modifies them to refer to the correct new outer classes. 
 	 */
 	protected  void modifyMethodAndFieldRefs( JavaClass clazz ){
    
 		ConstantPool cp = clazz.getConstantPool();
 		for (int i=1; i<cp.getLength(); i++){
 			Constant c = cp.getConstant(i);
 			
 			if(c == null) continue;
             
             if (c.getTag() == Constants.CONSTANT_Fieldref){
                 ConstantFieldref fieldRef = (ConstantFieldref) c;
                 
                 String targetClass = fieldRef.getClass(cp);
                 if(Tools.sameClass(targetClass, oldOuterClassName)){
                     System.out.println("test");
                     int classIndex = fieldRef.getClassIndex();
                     ConstantClass cc = (ConstantClass)cp.getConstant(classIndex);
                     int nameIndex = cc.getNameIndex();
                     cp.setConstant(nameIndex, new ConstantUtf8(newOuterClassName));
                 }
                                                 
                 
             }else if (c.getTag() == Constants.CONSTANT_Methodref){
 				ConstantMethodref mr = (ConstantMethodref) c;
 				String targetClassName = mr.getClass(cp);
 
 				int nameAndTypeIndex = mr.getNameAndTypeIndex();
 				ConstantNameAndType	nat = ((ConstantNameAndType) cp.getConstant(nameAndTypeIndex));
                 String methodName = nat.getName(cp);
                 
                 // check for innerclass construction
                 if( Tools.isPrefix(oldClassName, targetClassName) ){
                     String innerIdent = targetClassName.substring( oldClassName.length() +1 );
                     String newInnerName = newClassName + "$" + innerIdent;
                     
                     try{
                         Integer.parseInt(innerIdent);
                     } catch( Exception e ){
                         // not an anonymous inner class
                         continue;
                     }
                     
                     // Change target class to new inner class
                     int targetClassIndex = mr.getClassIndex();
                     int targetNameIndex = ((ConstantClass)cp.getConstant(targetClassIndex)).getNameIndex();
                     cp.setConstant(targetNameIndex, new ConstantUtf8(newInnerName));
                     
                     // Change argument class to new class
                     Type[] args = Type.getArgumentTypes(nat.getSignature(cp));
                     for (int j = 0; j < args.length; j++) {
                         Type arg = args[j];
                         String signature = arg.getSignature();
                         String argumentType = arg.toString();
                         if (Tools.sameClass(argumentType, oldClassName)){
                             args[j] = Type.getType("L" + newClassName + ";");
                         }
                     }
                     String signature = Type.getMethodSignature( 
                                 Type.getReturnType(nat.getSignature(cp)), 
                                 args);    
                     int signatureIndex = nat.getSignatureIndex();
                     cp.setConstant(signatureIndex, new ConstantUtf8(signature));
                 }
                 
 				// Check for superconstructor calls with otuer class parameter
 				if (Tools.sameClass(targetClassName, newSuperclassName)){
 					if (methodName.equals("<init>")){		
 						Type[] args = Type.getArgumentTypes(nat.getSignature(cp));
 						if (args.length == 1){
 							String argumentType = args[0].toString();
 							// if parameter is of old super-outer-class type, set new signature
 							if (Tools.sameClass(argumentType, outerOfOldSuper)){
 								cp.setConstant( 
 										nat.getSignatureIndex(),
 										new ConstantUtf8("(L"+outerOfNewSuper+";)V")
 									);
 							}
 						}
 					}
 				}
 
 				// check whether its a call to our old outer class
 				if (Tools.isPrefix(targetClassName, oldOuterClassName)){
//					String newTargetClass = Tools.getNewOuterName(
//												oldClassName,
//												targetClassName,
//												outerClasses);
 					int classIndex = mr.getClassIndex();
 					ConstantClass cc = (ConstantClass)cp.getConstant(classIndex);
 					int nameIndex = cc.getNameIndex();
					cp.setConstant(nameIndex, new ConstantUtf8(newOuterClassName));
 				}
 			}
 		}
 	}
 	
 	public void visitLocalVariable(LocalVariable variable) {
 		// Change the type of the local variable this
 		if (variable.getName().equals("this") ){
 			int index = variable.getSignatureIndex();
             ConstantPool cp = variable.getConstantPool();
             Constant newSignature = new ConstantUtf8(
                     new ObjectType(newClassName).getSignature()); 
 			cp.setConstant(index, newSignature );
 		} 
 		super.visitLocalVariable(variable);
 	}
 
 	public void visitField(Field field) {
 		// and of outer this
 		if (field.getName().startsWith("this$")){
 			int index = field.getSignatureIndex();
             ConstantPool cp = field.getConstantPool();
             Constant newSignature = new ConstantUtf8( 
                     new ObjectType(newOuterClassName).getSignature()); 
             cp.setConstant(index, newSignature );
 		}
 
 		super.visitField(field);
 	}
 	
 	public void visitMethod(Method method) {
         final ConstantPool cp = method.getConstantPool();
 		// we search for outer-class-access functions, which
 		// are static, have exactly one argument of this class' type and
 		// return an instance of the outer class' type
 		if (method.isStatic() && method.getName().startsWith("access$")){
 			String returnType = Type.getReturnType(method.getSignature()).toString(); 
 			
 			if (!Tools.sameClass(returnType,oldOuterClassName)) return;
 			Type[]	argTypes = Type.getArgumentTypes(method.getSignature());
 			if (argTypes.length != 1) return;
 			
 			// construct the new signature & use it to overwrite the old one
 			String   newSignature = "(L"+newClassName+";)L"+newOuterClassName+";";
 			int      index = method.getSignatureIndex();
 			cp.setConstant(index, new ConstantUtf8(newSignature));
 		}
 		// and we check for constructors 
 		else if (method.getName().equals("<init>")){
 			Type[]	argTypes = Type.getArgumentTypes(method.getSignature());
 			for(int argIdx=0; argIdx<argTypes.length; argIdx++){
     			// modify the signature if neccessary
     			if (Tools.sameClass(argTypes[argIdx].toString(),oldOuterClassName)){
     				// construct the new signature and use it to overwrite the old one
                     argTypes[argIdx] = Type.getType( "L" + newOuterClassName + ";");
     			}
             }
 			// construct new signature
             String signature = Type.getMethodSignature( method.getReturnType(), argTypes );
             int signatureIndex = method.getSignatureIndex();
             cp.setConstant(signatureIndex, new ConstantUtf8(signature));
 		}
 	}
 	public void visitCode(Code obj) {
         // TODO Auto-generated method stub
         super.visitCode(obj);
     }
     
     public void visitInnerClasses(InnerClasses obj) {
         ConstantPool cp = obj.getConstantPool();
         InnerClass[] innerClasses = obj.getInnerClasses();
         for (int i = 0; i < innerClasses.length; i++) {
             InnerClass inner = innerClasses[i];
             String  innerName = Tools.loadName(inner.getInnerNameIndex(), cp),
                     className = Tools.loadClassName(inner.getInnerClassIndex(), cp);
             
             if (innerName.equals("")){
                 String shortName = className.split("\\$")[1];
                 try{
                     Integer.valueOf(shortName);
                     anonymousInners .add(className);
                 } catch(NumberFormatException e){                  
                 }
             }
         }
        // super.visitInnerClasses(obj);
    }
 
     
     /**
      * Returns the anonymous inner classes that where found while  
      * transforming the last class.
      */
     public Collection<String> getAnonymousInnerClasses() {
         return anonymousInners;
     }
 }
