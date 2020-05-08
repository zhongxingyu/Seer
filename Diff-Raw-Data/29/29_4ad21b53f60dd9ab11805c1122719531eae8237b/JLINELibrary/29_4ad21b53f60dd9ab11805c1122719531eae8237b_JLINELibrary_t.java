 /*
  * Copyright (c) 2011, Karl Trygve Kalleberg <karltk near strategoxt dot org>
 *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.library.jline;
 
 import java.io.IOException;
 
 import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;
 
 public class JLINELibrary extends AbstractStrategoOperatorRegistry {
 
 	public static final String REGISTRY_NAME = "JLINE";
 
	private JLINELibrary() {
 		add(new JLINE_make_console());
 		add(new JLINE_clear_screen());
 		add(new JLINE_repl());
 		add(new JLINE_println());
 		add(new JLINE_format());
 	}

 	@Override
 	public String getOperatorRegistryName() {
 		return REGISTRY_NAME;
 	}
 
 	public static boolean reportException(IContext env, IOException e) {
 		return false;
 	}
 
    public static void attach(Interpreter intp) throws IOException, InterpreterException {
    	attach(intp, new JLINELibrary(), "/shared/jline-library.ctree");
    }

 }
