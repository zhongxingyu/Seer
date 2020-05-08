 /*
  * Copyright (c) 2011, Karl Trygve Kalleberg <karltk at strategoxt dot org>
  *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.ui;
 
 import java.io.IOException;
 
 import org.spoofax.interpreter.ConcreteInterpreter;
 import org.spoofax.interpreter.core.InterpreterErrorExit;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.InterpreterExit;
 import org.spoofax.interpreter.core.UndefinedStrategyException;
 import org.spoofax.interpreter.library.eclipse.EFILibrary;
 import org.spoofax.interpreter.library.java.JFFLibrary;
 import org.spoofax.interpreter.ui.SpoofaxConsole.ConsoleIOAgent;
 import org.spoofax.jsglr.client.ParseException;
 import org.spoofax.jsglr.shared.BadTokenException;
 import org.spoofax.jsglr.shared.SGLRException;
 import org.spoofax.jsglr.shared.TokenExpectedException;
 
 public class SpoofaxInterpreter {
 
 	private final ConcreteInterpreter interpreter;
 	private final ConsoleIOAgent ioAgent;
 
 	public SpoofaxInterpreter(ConsoleIOAgent ioAgent) {
 		this.ioAgent = ioAgent;
 		interpreter = new ConcreteInterpreter(); //new ECJFactory());
 		try {
 			JFFLibrary.attach(interpreter);
 			EFILibrary.attach(interpreter);
 			interpreter.setIOAgent(ioAgent);
 		} catch(IOException e) {
 			throw new RuntimeException(e);
 		} catch(InterpreterException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	public boolean eval(String line) {
 		try {
 			if(!interpreter.parseAndInvoke(line)) {
 				ioAgent.err.println("command failed");
 			} else {
 				ioAgent.out.println(interpreter.current().toString());
 			}
 		} catch (TokenExpectedException e) {
 			ioAgent.err.println(e.getMessage());
 		} catch (InterpreterErrorExit e) {
			logError(e); // FIXME might want to report this sensibly
 		} catch (BadTokenException e) {
 			ioAgent.err.println(e.getMessage());
 		} catch (ParseException e) {
			logError(e); // FIXME might want to report this sensibly
 		} catch (InterpreterExit e) {
			logError(e); // FIXME might want to report this sensibly
 		} catch (UndefinedStrategyException e) {
 			ioAgent.err.println(e.getMessage());
 		} catch (SGLRException e) {
			logError(e); // FIXME might want to report this sensibly
 		} catch (InterpreterException e) {
 			if(e.getCause() != null)
 				ioAgent.err.println(e.getCause().getMessage());
 			else
 				ioAgent.err.println(e.getMessage());
 		} finally {
 			ioAgent.out.flush();
 			ioAgent.err.flush();
 		}
 		return true;
 	}
 
	private void logError(Exception e) {
		InterpreterPlugin.logError("Interpreter failure", e);
	}

 
 }
