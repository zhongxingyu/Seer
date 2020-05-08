 /*
  * Copyright (c) 2011, Karl Trygve Kalleberg <karltk near strategoxt dot org>
 *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.library.jline;
 
import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Test;
 import org.spoofax.interpreter.core.Interpreter;
 import org.spoofax.interpreter.core.InterpreterException;
 
 public class TestJLINE {
 
 	private Interpreter makeInterpreter() throws IOException, InterpreterException {
 		Interpreter intp = new Interpreter();
		JLINELibrary.attach(intp);
 		intp.load(System.getProperty("user.home") + "/.nix-profile/share/stratego-lib/libstratego-lib.ctree");
 		intp.load("jline-test.ctree");
 		intp.setCurrent(intp.getFactory().makeTuple());
 		return intp;
 	}
 
 	@Test
 	public void test_jline_printnl() throws IOException, InterpreterException {
 		Interpreter intp = makeInterpreter();
 		intp.setCurrent(intp.getFactory().makeTuple());
 		assertTrue("Strategy failed", intp.invoke("test_println_0_0"));
 	}
 
 	@Test
 	public void test_jline_make_console() throws IOException, InterpreterException {
 		Interpreter intp = makeInterpreter();
 		System.out.println(intp.current());
 		assertTrue("Strategy failed", intp.invoke("test_make_console_0_0"));
 	}
 
 	@Test
 	public void test_jline_repl() throws IOException, InterpreterException {
 		Interpreter intp = makeInterpreter();
 		intp.setCurrent(intp.getFactory().makeTuple());
 		assertTrue("Strategy failed", intp.invoke("test_repl_0_0"));
 	}
 }
