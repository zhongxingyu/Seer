 package se.knifhen.javarebel.plugin;
 
 import org.zeroturnaround.bundled.javassist.CannotCompileException;
 import org.zeroturnaround.bundled.javassist.ClassPool;
 import org.zeroturnaround.bundled.javassist.CtClass;
 import org.zeroturnaround.bundled.javassist.CtMethod;
 import org.zeroturnaround.bundled.javassist.NotFoundException;
 import org.zeroturnaround.javarebel.integration.support.JavassistClassBytecodeProcessor;
 
 /**
  * This processor alters the way the <code>UrlBindingFactory</code> looks up the urlBinding for each <code>ActionBean</code>.
  * The original would first look in a cache for any previous urlBindings for an <code>ActionBean</code>. This however
  * prevented ActionBeans from rebinding to another urlBinding.
  *    
  * @author Andreas Knifh
  */
 public class UrlBindingFactoryBytecodeProcessor extends JavassistClassBytecodeProcessor {
 
 	/**
 	 * Applies changes to <code>UrlBindingFactory</code> to make Stripes always extract the correct urlBinding from each <code>ActionBean</code>. 
 	 */
 	public void process(ClassPool cp, ClassLoader cl, CtClass urlBindingFactory) throws Exception {
 		getBindingPrototype(cp, urlBindingFactory);
 	}
 
 	/**
 	 * Overrides the implementation of <code>UrlBindingFactory.getBindingPrototype(Class)</code> to make sure that only the 
 	 * correct urlBinding is used for each <code>ActionBean</code>. 
 	 */
 	private void getBindingPrototype(ClassPool cp, CtClass urlBindingFactory) throws NotFoundException, CannotCompileException {
 		CtMethod getBindingPrototype = urlBindingFactory.getDeclaredMethod("getBindingPrototype", new CtClass[] {cp.get("java.lang.Class")});
 		getBindingPrototype.setBody("" +
 			"{" +
 			"	net.sourceforge.stripes.controller.UrlBinding binding = parseUrlBinding($1);" +
 			"	if (binding != null)" +
 			"		addBinding($1, binding);" +
 			"	return binding;" +
 			"}");
 	}
 
 }
