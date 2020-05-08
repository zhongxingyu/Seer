 /*
  * Copyright (c) 2005-2011, Karl Trygve Kalleberg <karltk near strategoxt dot org>
  * 
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.cli;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jline.ANSIBuffer;
 import jline.ConsoleReader;
 
 import org.spoofax.NotImplementedException;
 import org.spoofax.interpreter.ConcreteInterpreter;
 import org.spoofax.interpreter.core.InterpreterErrorExit;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.InterpreterExit;
 import org.spoofax.interpreter.core.UndefinedStrategyException;
 import org.spoofax.interpreter.core.VarScope;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoInt;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoReal;
 import org.spoofax.interpreter.terms.IStrategoString;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.IStrategoTuple;
 import org.spoofax.jsglr.client.ParseException;
 import org.spoofax.jsglr.shared.BadTokenException;
 import org.spoofax.jsglr.shared.SGLRException;
 import org.spoofax.jsglr.shared.TokenExpectedException;
 
 public class Main {
 
 	public static void main(String[] args) throws IOException {
 
 		ConcreteInterpreter intp = new ConcreteInterpreter();
 
 		List<String> includes = new LinkedList<String>();
 		List<String> toCompile = new LinkedList<String>();
 		String outFile = null;
 		int skip = 0;
 
 		for (int i = 0; i < args.length; i++) {
 			if (skip > 0) {
 				skip--;
 				continue;
 			}
 			final String arg = args[i];
 			if (arg.equals("-I")) {
 				skip++;
 				includes.add(args[i + 1]);
 			} else if (arg.equals("-i")) {
 				toCompile.add(args[i + 1]);
 				skip++;
 			} else if (args.equals("-o")) {
 				outFile = args[i + 1];
 				skip++;
 			} else if (arg.startsWith("-")) {
 				System.err.println("Unknown option " + arg);
 				System.exit(-12);
 			} else {
 				throw new NotImplementedException();
 			}
 		}
 
 		if (toCompile.size() == 0) {
 			PrintWriter out = new PrintWriter(System.out);
 			ConsoleReader consoleReader = new ConsoleReader();
 			
 			File historyFile = new File(System.getProperty("user.home") + "/.stratego-shell-history");
 			if(historyFile.canRead() && historyFile.canWrite())
 				consoleReader.getHistory().setHistoryFile(historyFile);
 			
 			consoleReader.setBellEnabled(false);
 			consoleReader
 					.addCompletor(new StrategyCompletor(intp.getContext()));
 			String line;
 			int promptCount = 0;
 			boolean lastWasGood = true;
 			while ((line = consoleReader.readLine(makePrompt(lastWasGood,
 					promptCount))) != null) {
 				final String code = line.trim();
 				if (":quit".equals(code))
 					break;
 				else if (":clear".equals(code)) {
 					consoleReader.clearScreen();
 				} else if (":vars".equals(code)) {
 					VarScope vs = intp.getContext().getVarScope();
 					for(String v : vs.getVars()) {
 						IStrategoTerm t = vs.lookup(v);
 						ANSIBuffer ab = new ANSIBuffer();
 						out.println(" " + v + " = " + colorize(ab, t).toString());
 					}
 				} else if (":strategies".equals(code)) {
 					SortedSet<String> sorted = new TreeSet<String>();
 					for (String strategy : intp.getContext().getStrategyNames()) {
 						sorted.add(StrategyCompletor.uncify(strategy));
 					}
 					consoleReader.printColumns(sorted);
 				} else if(code.startsWith(":forget ")) {
 					String[] els = code.split(" ");
 					VarScope vs = intp.getContext().getVarScope();
 					for(int i = 1 ; i < els.length; i++) {
 						if("_".equals(els[i])) {
 							vs.removeAllVars();
 							break;
 						} else if (els[i].indexOf("/") == -1){
 							vs.removeVar(els[i]);
 						} else {
 							String name = StrategyCompletor.cify(els[i]);
 							if(!vs.removeSVar(name))
 								out.print(new ANSIBuffer().red("Strategy '" + name + "' unknown"));
 						}
 					}
 				} else if(code.startsWith(":arity ")) {
 					String[] els = code.split(" ");
 					Collection<String> interesting = new LinkedList<String>();
 					for(int i = 0; i < els.length; i++) {
 						for(String s : intp.getContext().getStrategyNames()) {
 							String uncifyName = StrategyCompletor.uncify(s);
 							if(els[i].equals(uncifyName)) {
 									interesting.add(s);
 							}
 						}
 					}
 					for(String s : interesting) {
 						out.println(StrategyCompletor.uncifyComplete(s));
 					}
 				} else if(code.startsWith(":")) {
 					usage(out);
 				}
 				if (code.equals("") || code.startsWith(":")) {
 					out.flush();
 					continue;
 				}
 				promptCount++;
 				IStrategoTerm old = intp.current();
				lastWasGood = false;
 				try {
 					if (intp.parseAndInvoke(code)) {
 						lastWasGood = true;
 						ANSIBuffer ab = new ANSIBuffer();
 						out.println(colorize(ab, intp.current()));
						old = intp.current();
 					}
 				} catch (UndefinedStrategyException e) {
 					handleUndefinedStrategyException(out, e);
 				} catch (TokenExpectedException e) {
 					e.printStackTrace(out);
 					out.println(e.getClass());
 				} catch (BadTokenException e) {
 					out.println(new ANSIBuffer().red(e.getMessage()).toString());
 				} catch (ParseException e) {
 					e.printStackTrace(out);
 					out.println(e.getClass());
 				} catch (SGLRException e) {
 					e.printStackTrace(out);
 					out.println(e.getClass());
 				} catch (InterpreterErrorExit e) {
 					e.printStackTrace(out);
 					out.println(e.getClass());
 				} catch (InterpreterExit e) {
 					e.printStackTrace(out);
 					out.println(e.getClass());
 				} catch (InterpreterException e) {
 					if (e.getCause() instanceof UndefinedStrategyException) {
 						handleUndefinedStrategyException(out,
 								(UndefinedStrategyException) e.getCause());
 					}
				} finally {
					intp.setCurrent(old);
 				}
 				out.flush();
 			}
 		}
 	}
 
 	private static ANSIBuffer colorize(ANSIBuffer ab, IStrategoTerm current) {
 		if (current instanceof IStrategoInt || current instanceof IStrategoReal) {
 			ab.yellow(current.toString());
 		} else if (current instanceof IStrategoTuple) {
 			printSequence(ab, "(", ")", current);
 		} else if (current instanceof IStrategoString) {
 			ab.green(current.toString());
 		} else if (current instanceof IStrategoList) {
 			printSequence(ab, "[", "]", current);
 		} else if (current instanceof IStrategoAppl) {
 			ab.append(((IStrategoAppl) current).getName());
 			printSequence(ab, "(", ")", current);
 		}
 		return ab;
 	}
 
 	private static void printSequence(ANSIBuffer ab, String open, String close,
 			IStrategoTerm current) {
 		ab.bold(open);
 		IStrategoTerm[] ts = current.getAllSubterms();
 		if (ts.length > 1)
 			colorize(ab, ts[0]);
 		for (int i = 1; i < ts.length; i++) {
 			ab.bold(",");
 			colorize(ab, ts[i]);
 		}
 		ab.bold(close);
 	}
 
 	private static void usage(PrintWriter out) {
 		out.println(new ANSIBuffer().yellow(" :help                       ").append("-- print this page"));
 		out.println(new ANSIBuffer().yellow(" :forget ").append("var1 var2 ... varN  -- forget specific global variables"));
 		out.println(new ANSIBuffer().yellow(" :forget ").append("strat/(n,m)         -- forget strategy with arity (n, m), e.g :forget zip/(1,0)"));
 		out.println(new ANSIBuffer().yellow(" :forget ").append("_                   -- forget all global variables"));
 		out.println(new ANSIBuffer().yellow(" :arity ").append("strategy             -- show available arities for a strategy"));
 		out.println(new ANSIBuffer().yellow(" :strategies                 ").append("-- show all global strategies"));
 		out.println(new ANSIBuffer().yellow(" :vars                       ").append("-- show all global variables"));
 	}
 	
 	
 	private static String makePrompt(boolean success, int promptCount) {
 		ANSIBuffer ab = new ANSIBuffer();
 		ab.bold(promptCount + "");
 		ab.append("/");
 		if (success)
 			ab.green("ok");
 		else
 			ab.red("fail");
 		ab.blue(">");
 		ab.append(" ");
 		return ab.toString();
 	}
 
 	private static void handleUndefinedStrategyException(PrintWriter out,
 			UndefinedStrategyException e) {
		out.println(new ANSIBuffer().red("Undefined strategy "
				+ StrategyCompletor.uncify(e.getStrategyName())));
 	}
 }
