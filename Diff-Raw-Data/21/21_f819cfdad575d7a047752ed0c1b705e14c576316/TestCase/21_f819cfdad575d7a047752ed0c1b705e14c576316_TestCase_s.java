 package org.jackie.test.java5.annotation;
 
 import org.jackie.compiler.extension.ExtensionManager;
 import org.jackie.compiler.filemanager.FileManager;
 import org.jackie.compiler.typeregistry.TypeRegistry;
import org.jackie.compiler_impl.ExtensionManagerImpl;
 import org.jackie.compiler_impl.filemanager.ClassPathFileManager;
 import org.jackie.compiler_impl.typeregistry.JClassRegistry;
 import org.jackie.compiler_impl.typeregistry.MultiRegistry;
 import org.jackie.compiler_impl.typeregistry.PrimitiveTypeRegistry;
 import static org.jackie.context.ContextManager.*;
 
 /**
  * @author Patrik Beno
  */
 public abstract class TestCase {
 
 	protected FileManager filemanager;
 	protected TypeRegistry typeregistry;
 	protected ExtensionManager extmanager;
 
 	{
		filemanager = new ClassPathFileManager();
		typeregistry = new MultiRegistry(
				new PrimitiveTypeRegistry(),
				new JClassRegistry(filemanager)
		);
		extmanager = new ExtensionManagerImpl();
 	}
 
 	protected void run(Runnable r) {
 		newContext();
 		try {
 			context().set(TypeRegistry.class, typeregistry);
 			context().set(ExtensionManager.class, extmanager);
 			r.run();
 		} finally {
 			closeContext();
 		}
 
 	}
 
 }
