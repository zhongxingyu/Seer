 package org.spoofax.compiler;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.spoofax.interpreter.Interpreter;
 import org.spoofax.interpreter.InterpreterException;
 import org.spoofax.interpreter.adapter.aterm.WrappedATermFactory;
 import org.spoofax.interpreter.library.jsglr.JSGLRLibrary;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.jsglr.InvalidParseTableException;
 
 public class Compiler {
 	private Interpreter compiler;
 	private String strategoBase;
 	private String spoofaxBase;
 	
     public Compiler() throws IOException, InterpreterException, InvalidParseTableException {
     	this(strategoPath(), spoofaxPath(), new WrappedATermFactory());
     }
     
 	Compiler(String strcBasepath, String spoofaxBase, WrappedATermFactory factory) throws IOException, InterpreterException, InvalidParseTableException {
 		init(factory);
     }
     
     private void init(WrappedATermFactory factory) throws IOException, InterpreterException, InvalidParseTableException
     {
 		compiler = new Interpreter(factory);
 		compiler.addOperatorRegistry("JSGLR", new JSGLRLibrary(factory));
 		compiler.load(strategoBase + "/share/stratego-lib/libstratego-lib.ctree");
 		compiler.load(strategoBase + "/share/libstratego-sglr.ctree");
 		compiler.load(strategoBase + "/share/libstrc.ctree");
 		compiler.load(spoofaxBase + "/share/jstrc.ctree");
 	}
 	
 	IStrategoTerm compile(String file, String[] path, boolean lib) throws InterpreterException
 	{
 		Collection<IStrategoTerm> terms = new LinkedList<IStrategoTerm>();
 		for (String p : path) {
 			terms.add(compiler.getFactory().makeString(p));
 		}
 		IStrategoTerm tp = compiler.getFactory().makeList(terms);
 		IStrategoTerm[] tuple = {
 		  compiler.getFactory().makeString(file),
 		  tp,
 		  compiler.getFactory().makeInt(lib?1:0)
 		};
 		compiler.setCurrent(
 		  compiler.getFactory().makeTuple(tuple)
 		);
 		if (!compiler.invoke("main_0_0"))
 			return null;
 		return compiler.current();
 	}
 
 	public static String strategoPath() {
 		String path = System.getProperty("stratego.dir");
 		return path == null ? System.getProperty("user.dir") + "/.nix-profile" : path;
 	}
 
 	public static String spoofaxPath() {
 		String path = System.getProperty("spoofax.dir");
 		return path == null ? "." : path; //System.getProperty("user.dir") + "/.nix-profile" : path;
 	}
 }
