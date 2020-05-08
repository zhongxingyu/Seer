 package org.spoofax.interpreter.test;
 
 import java.io.IOException;
 
 import junit.framework.TestCase;
 
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.interpreter.compiler.ConcreteInterpreter;
 
 public abstract class AbstractCompilerTest extends TestCase {
 	private String path;
 	private ConcreteInterpreter intp;
 	
 	public void setUp(String string) throws Exception {
 		super.setUp();
 		path = string;
 		intp = new ConcreteInterpreter();
 	}
 	
 	protected void exec(String file) throws IOException, InterpreterException {
 		String[] strpath = { path };
 		intp.loadConcrete(path + "/" + file, strpath);
		intp.invoke("main");
 	}
 }
