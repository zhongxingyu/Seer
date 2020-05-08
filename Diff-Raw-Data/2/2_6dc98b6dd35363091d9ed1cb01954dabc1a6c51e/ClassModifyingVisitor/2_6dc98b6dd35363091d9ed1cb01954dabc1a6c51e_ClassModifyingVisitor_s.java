 /*
  * Created on 19.04.2004
  *
  */
 package org.caesarj.mixer2.intern;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Stack;
 import java.util.Vector;
 
 import org.apache.bcel.Constants;
 import org.apache.bcel.Repository;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.generic.ClassGen;
 import org.apache.bcel.generic.ConstantPoolGen;
 import org.apache.bcel.generic.ObjectType;
 import org.apache.bcel.generic.ReferenceType;
 import org.apache.bcel.generic.Type;
 import org.apache.bcel.verifier.GraphicalVerifier;
 import org.caesarj.classfile.NameAndTypeConstant;
 import org.caesarj.compiler.cclass.JavaQualifiedName;
 import org.caesarj.mixer2.ClassGenerator;
 import org.caesarj.mixer2.MixerException;
 
 
 /**
  * Modify a <code>JavaClass</code> to have a different class and superclass name.
  * Performs the following changes:
  * 1. Change className
  * 2. Change superclassName
  * 3. Change type of local this-variable
  * 4. Modify inner class references
  
  * @author Karl Klose
  */
 public class ClassModifyingVisitor extends EmptyVisitor {
 
 	protected String[]	outerClasses;// = new String[]{"example/Aprime"};
 	
 	protected static String	outputDirectory = "bin/";
 	
 	public static void setOutputDirectory( String outputDirectory ){
 		ClassModifyingVisitor.outputDirectory = outputDirectory;
 	}
 	
 	
 	protected String 	oldClassName, 
 						newClassName, 
 						newSuperclassName,
 						newOuterClassName,
 						oldOuterClassName;
 
 	public static	void modify( 
 			String className, 
 			String newClassName, 
 			String newSuperclassName, 
 			String outerClassName,
 			String [] outers ) throws MixerException{
 		
 		ClassModifyingVisitor	visitor = new ClassModifyingVisitor( 
 												className, 
 												newClassName, 
 												newSuperclassName, 
 												outerClassName, 
 												outers );
 		
 		JavaClass	clazz = Repository.lookupClass(className);
 		
 		if (clazz == null) {
 			throw new MixerException("Class not found "+ className);
 		}
 		
 		visitor.run(clazz);
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
 			String []outers ) {
 		this.oldClassName = oldClassName;
 		this.newClassName = newClassName;
 		this.newSuperclassName = newSuperclassName;
 		this.newOuterClassName = outerClassName;
 		outerClasses = outers;
 	
 	}
 	
 	
 	protected void run(JavaClass clazz) throws MixerException {
 		oldOuterClassName =  Tools.getOuterClass(clazz,oldClassName);
 		// create a copy as work base
 		JavaClass newClass = clazz.copy();
 	
 		
 
 		Attribute	[] attributes = newClass.getAttributes();
 		for (int i = 0; i < attributes.length; i++) {
 			Attribute attribute = attributes[i];
 			if (attribute.getTag() != Constants.ATTR_INNER_CLASSES) continue;
 		}
 		
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
 		
 ///		System.out.println( newClass.getConstantPool() );
 		
 		// Delete all inner class references 
 		Attribute[] atts = newClass.getAttributes();
 		Vector	v = new Vector();
 		for (int i = 0; i < atts.length; i++) {
 			Attribute attribute = atts[i];
 			if (attribute.getTag() == org.apache.bcel.Constants.ATTR_INNER_CLASSES){
 				InnerClasses ic = (InnerClasses)attribute;
 				ic.setInnerClasses(new InnerClass[0]);
 				ic.setLength(2);
 				
 			}
 			v.add( attribute );
 		}
 		atts = (Attribute[]) v.toArray(new Attribute[0]);
 		newClass.setAttributes(atts);
 		
 		// take a look at all methodrefs
 		modifyMethodRefs(newClass);
 /*		KK
 		// Add reference to the outer-class-file 
 		if (!oldOuterClassName.equals(newOuterClassName)){
 			JavaClass outer = Repository.lookupClass(newOuterClassName);
 			if (outer == null)	{
 				System.err.println("Waring: Referenced outer class "+newOuterClassName+" doesn't exist.");
 			}
 			else {
 				
 				InnerClass c = Tools.createInnerClass( 
 										outer, 
 										newOuterClassName, 
 										newClassName );
 				InnerClasses a = Tools.getInnerClassesAttribute(outer);
 				a.setInnerClasses( Tools.add(c, a.getInnerClasses() ) );
 				
 				
 				System.out.println(outer);
 				writeClass(outer);
 			}
 		}
 */
 		// at last, write the classfile
 		writeClass( newClass );
 	
 	}
 
 	void modifyMethodRefs( JavaClass clazz ){
 		ConstantPool cp = clazz.getConstantPool();
 		for (int i=1; i<cp.getLength(); i++){
 			Constant c = cp.getConstant(i);
 			if (c.getTag() == Constants.CONSTANT_Methodref){
 				ConstantMethodref mr = (ConstantMethodref) c;
 				String targetClassName = mr.getClass(cp);
 
 				if (Tools.isPrefix(targetClassName, oldClassName)){
 					String newTargetClass = Tools.getNewOuterName(
 												oldClassName,
 												targetClassName,
 												outerClasses);
 					int classIndex = mr.getClassIndex();
 					ConstantClass cc = (ConstantClass)cp.getConstant(classIndex);
 					int nameIndex = cc.getNameIndex();
 					cp.setConstant(nameIndex, new ConstantUtf8(newTargetClass));
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Write the class to file system 
 	 * @param clazz
 	 * @throws MixerException
 	 */
 	protected void writeClass( JavaClass clazz ) throws MixerException{
 		try {
 			clazz.dump(outputDirectory+newClassName+".class");
 		} catch (IOException e) {
 			throw new MixerException( "Unable to write classfile:" + e);
 		}
 	}
 	
 	
 	public void visitLocalVariable(LocalVariable variable) {
 		// Change the type of the local variable this
 		if (variable.getName().equals("this") ){
 			int index = variable.getSignatureIndex();
 			variable.getConstantPool().setConstant(index, 
 					new ConstantUtf8( 
 							new ObjectType(newClassName).getSignature()));
 		} 
 		super.visitLocalVariable(variable);
 	}
 
 	public void visitField(Field field) {
 		// and of outer this
 		if (field.getName().startsWith("this$")){
 			int index = field.getSignatureIndex();
 			field.getConstantPool().setConstant(index,
 					new ConstantUtf8( 
 							new ObjectType(newOuterClassName).getSignature()));
 		}
 		super.visitField(field);
 	}
 	
 	
 	public void visitMethod(Method obj) {
 		// we search for outer-class-access functions, which
 		// are static, have exactly one argument of this class' type and
 		// return an instance of the outer class' type
 		if (obj.getName().startsWith("access$")){
 			if (!obj.isStatic() ) return;
 			
 			String returnType = Type.getReturnType(obj.getSignature()).toString(); 
 			
 			if (!Tools.sameClass(returnType,oldOuterClassName)) return;
 			Type[]	argTypes = Type.getArgumentTypes(obj.getSignature());
 			if (argTypes.length != 1) return;
 			
 			// construct the new signature & use it to overwrite the old one
 			String newSignature = "(L"+newClassName+";)L"+newOuterClassName+";";// + " Just a little test";
 			
 			int index = obj.getSignatureIndex();
 			
 			obj.getConstantPool().setConstant(index, new ConstantUtf8(newSignature));
 		}
 		// and we check for constructors 
 		else if (obj.getName().equals("<init>")){
 			Type[]	argTypes = Type.getArgumentTypes(obj.getSignature());
 			if (argTypes.length != 1) return;
 			
 			if (Tools.sameClass(argTypes[0].toString(),oldOuterClassName)){
 				// construct the new signature & use it to overwrite the old one
				String newSignature = "(L"+newClassName+";)V";
 				
 				int index = obj.getSignatureIndex();
 				
 				obj.getConstantPool().setConstant(index, new ConstantUtf8(newSignature));
 			}
 		}
 	}
 
 }
