 package org.jackie.jclassfile;
 
 import org.jackie.context.ContextObject;
 import static org.jackie.context.ContextManager.context;
 import org.jackie.jclassfile.model.ClassFile;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.impl.Factory;
 
 /**
  * @author Patrik Beno
  */
 public class ClassFileContext implements ContextObject {
 
 	static public ClassFileContext classFileContext() {
 		return context(ClassFileContext.class);
 	}
 
 	ClassFile classFile;
 
 	public ClassFileContext(ClassFile classFile) {
 		this.classFile = classFile;
 	}
 
 	public ClassFile classFile() {
 		return classFile;
 	}
 
 	public ConstantPool constantPool() {
		return classFile().pool();
 	}
 
 	public Factory constantFactory() {
		return constantPool().factory();
 	}
 }
