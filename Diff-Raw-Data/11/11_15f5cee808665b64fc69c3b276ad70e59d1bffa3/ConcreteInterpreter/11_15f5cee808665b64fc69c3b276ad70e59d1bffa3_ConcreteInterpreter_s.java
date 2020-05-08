 /*
  * Copyright (c) 2005-2011, Karl Trygve Kalleberg <karltk at strategoxt dot org>
  *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.spoofax.interpreter.core.Interpreter;
 import org.spoofax.interpreter.core.InterpreterErrorExit;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.core.InterpreterExit;
 import org.spoofax.interpreter.core.UndefinedStrategyException;
 import org.spoofax.interpreter.stratego.SDefT;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.jsglr.client.InvalidParseTableException;
 import org.spoofax.jsglr.client.ParseException;
 import org.spoofax.jsglr.client.ParseTable;
 import org.spoofax.jsglr.client.SGLR;
 import org.spoofax.jsglr.client.imploder.TreeBuilder;
 import org.spoofax.jsglr.io.ParseTableManager;
 import org.spoofax.jsglr.shared.BadTokenException;
 import org.spoofax.jsglr.shared.SGLRException;
 import org.spoofax.jsglr.shared.TokenExpectedException;
 import org.spoofax.terms.ParseError;
 import org.spoofax.terms.TermFactory;
 
 public class ConcreteInterpreter extends Interpreter {
 
 	private final ParseTable sugarTable;
 	private final SGLR sugarParser;
 
 	public ConcreteInterpreter() {
 		this(new TermFactory());
 	}
 
 	public ConcreteInterpreter(ITermFactory termFactory) {
 		super(termFactory, new TermFactory());
 		try {
 			load(findLibrary("stratego-lib/libstratego-lib.ctree"));
 			load(findLibrary("libstrc.ctree"));
			load(findLocalLibrary("share/frontend.ctree"));
 			// load(findLibrary("libstratego-aterm.ctree"));
 			// load(findLibrary("libstratego-gpp.ctree"));
 			// load(findLibrary("libstratego-rtg.ctree"));
 			// load(findLibrary("libstratego-sdf.ctree"));
 			// load(findLibrary("libstratego-sglr.ctree"));
 			// load(findLibrary("libstratego-tool-doc.ctree"));
 
 			ParseTableManager ptm = new ParseTableManager();
 			sugarTable = ptm
					.loadFromStream(findLocalLibrary("/share/Stratego-Shell.tbl"));
 			sugarParser = new SGLR(new TreeBuilder(), sugarTable);
 			sugarParser.setUseStructureRecovery(false);
 			setCurrent(getFactory().makeList());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (InterpreterException e) {
 			throw new RuntimeException(e);
 		} catch (ParseError e) {
 			throw new RuntimeException(e);
 		} catch (InvalidParseTableException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
	private InputStream findLocalLibrary(String path) throws IOException {
 		InputStream ins = ConcreteInterpreter.class.getClassLoader().getResourceAsStream(path);
 		if(ins == null)
			throw new IOException("Failed to load internal library " + path);
 		return ins;
 	}
 
 	protected InputStream findLibrary(String libraryPath) throws IOException {
 		String shareDir = System.getProperty("user.home")
 				+ "/.nix-profile/share/";
 		File file = new File(shareDir + "/" + libraryPath);
 		if (file.exists()) {
 			try {
 				return new FileInputStream(file);
 			} catch (FileNotFoundException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		throw new IOException("Failed to find Stratego library " + file.getAbsolutePath());
 	}
 
 	public void loadConcrete(String file, String[] path, boolean lib)
 			throws IOException, InterpreterException {
 	}
 
 	private IStrategoAppl parseAndCompile(String codeAsString, String frontendStrategy, String startSymbol) throws TokenExpectedException, BadTokenException, ParseException, SGLRException, InterpreterErrorExit, InterpreterExit, UndefinedStrategyException, InterpreterException {
 		IStrategoTerm tree = (IStrategoTerm) sugarParser.parse(codeAsString, "stdin", startSymbol);
 		IStrategoTerm old = current();
 		setCurrent(tree);
 		invoke(frontendStrategy);
 		IStrategoAppl ret = (IStrategoAppl) current();
 		setCurrent(old);
 		return ret;
 	}
 
 	public boolean parseAndInvoke(String codeAsString) throws TokenExpectedException, InterpreterErrorExit, BadTokenException, ParseException, InterpreterExit, UndefinedStrategyException, SGLRException, InterpreterException {
 		IStrategoAppl program = parseAndCompile(codeAsString, "spx_shell_frontend_0_0", "Toplevel");
 		if(program == null) {
 			throw new InterpreterException("Failed to compile fragment");
 		} else if(program.getName().equals("SDefT")) {
 			SDefT def = loader.parseSDefT(program);
 			context.addSVar(def.getName(), def);
 			return true;
 		} else {
 			return evaluate(program);
 		}
 	}
 }
