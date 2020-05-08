 package org.suite;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.instructionexecutor.FunctionInstructionExecutor;
 import org.instructionexecutor.LogicInstructionExecutor;
 import org.suite.doer.Generalizer;
 import org.suite.doer.Prover;
 import org.suite.doer.TermParser;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.kb.RuleSet;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.node.Tree;
 import org.util.IoUtil;
 import org.util.Util;
 
 public class SuiteUtil {
 
 	private static TermParser parser = new TermParser();
 	private static Prover logicalCompiler;
 	private static Prover eagerFunctionalCompiler;
 	private static Prover lazyFunctionalCompiler;
 
 	// The directory of the file we are now importing
 	private static boolean isImportFromClasspath = false;
 	private static String importerRoot = "";
 
 	public static void addRule(RuleSet rs, String rule) {
 		rs.addRule(parser.parse(rule));
 	}
 
 	public static synchronized boolean importFrom(RuleSet rs, String name)
 			throws IOException {
 		if (isImportFromClasspath)
 			return SuiteUtil.importResource(rs, name);
 		else
 			return SuiteUtil.importFile(rs, name);
 	}
 
 	public static synchronized boolean importFile(RuleSet rs, String filename)
 			throws IOException {
 		FileInputStream is = null;
 
 		boolean wasFromClasspath = isImportFromClasspath;
 		String oldRoot = importerRoot;
 		filename = setImporterRoot(false, filename, oldRoot);
 
 		try {
 			is = new FileInputStream(filename);
 			return rs.importFrom(SuiteUtil.parse(is));
 		} finally {
 			Util.closeQuietly(is);
 			isImportFromClasspath = wasFromClasspath;
 			importerRoot = oldRoot;
 		}
 	}
 
 	public static synchronized boolean importResource(RuleSet rs,
 			String classpath) throws IOException {
 		ClassLoader cl = SuiteUtil.class.getClassLoader();
 		InputStream is = null;
 
 		boolean wasFromClasspath = isImportFromClasspath;
 		String oldRoot = importerRoot;
 		classpath = setImporterRoot(true, classpath, oldRoot);
 
 		try {
 			is = cl.getResourceAsStream(classpath);
 			if (is != null)
 				return rs.importFrom(SuiteUtil.parse(is));
 			else
 				throw new RuntimeException("Cannot find resource " + classpath);
 		} finally {
 			Util.closeQuietly(is);
 			isImportFromClasspath = wasFromClasspath;
 			importerRoot = oldRoot;
 		}
 	}
 
 	private static String setImporterRoot(boolean isFromClasspath, String name,
 			String oldRoot) {
 		isImportFromClasspath = isFromClasspath;
 
 		if (!name.startsWith(File.separator))
 			name = oldRoot + name;
 
 		int pos = name.lastIndexOf(File.separator);
 		importerRoot = pos >= 0 ? name.substring(0, pos + 1) : "";
 		return name;
 	}
 
 	public static boolean proveThis(RuleSet rs, String s) {
 		Node node = parse(s);
 		node = new Generalizer().generalize(node);
 		Prover prover = new Prover(rs);
 		return prover.prove(node);
 	}
 
 	public static boolean evaluateLogical(String program) {
 		return evaluateLogical(parse(program));
 	}
 
 	public static boolean evaluateLogical(Node program) {
 		Prover lc = getLogicalCompiler();
 		Node node = SuiteUtil.parse("compile-logic .program .code");
 		// + ", pretty.print .code"
 
 		Generalizer generalizer = new Generalizer();
 		node = generalizer.generalize(node);
 		Node variable = generalizer.getVariable(Atom.create(".program"));
 		Node ics = generalizer.getVariable(Atom.create(".code"));
 
 		((Reference) variable).bound(program);
 		if (lc.prove(node)) {
 			Node result = new LogicInstructionExecutor(lc, ics).execute();
 			return result == Atom.TRUE;
 		} else
 			throw new RuntimeException("Logic compilation error");
 	}
 
 	public static class FunCompilerConfig {
 		private Node node;
 		private boolean isLazy;
 		private List<String> libraries = new ArrayList<>();
 		private boolean isTrace = false;
 		private boolean isDumpCode = false;
 		private Reader in = new InputStreamReader(System.in, IoUtil.charset);
 		private Writer out = new OutputStreamWriter(System.out, IoUtil.charset);
 
 		public FunCompilerConfig() {
 			addLibrary("STANDARD");
 		}
 
 		public void addLibrary(String library) {
 			libraries.add(library);
 		}
 
 		public void addLibraries(List<String> libs) {
 			libraries.addAll(libs);
 		}
 
 		public void setNode(Node node) {
 			this.node = node;
 		}
 
 		public void setLazy(boolean isLazy) {
 			this.isLazy = isLazy;
 		}
 
 		public void setLibraries(List<String> libraries) {
 			this.libraries = libraries;
 		}
 
 		public void setTrace(boolean isTrace) {
 			this.isTrace = isTrace;
 		}
 
 		public void setDumpCode(boolean isDumpCode) {
 			this.isDumpCode = isDumpCode;
 		}
 
 		public void setIn(Reader in) {
 			this.in = in;
 		}
 
 		public void setOut(Writer out) {
 			this.out = out;
 		}
 	}
 
 	public static FunCompilerConfig fcc(Node node) {
 		return fcc(node, false);
 	}
 
 	public static FunCompilerConfig fcc(String program, boolean isLazy) {
 		return fcc(parse(program), isLazy);
 	}
 
 	public static FunCompilerConfig fcc(Node node, boolean isLazy) {
 		FunCompilerConfig c = new FunCompilerConfig();
 		c.setNode(node);
 		c.setLazy(isLazy);
 		return c;
 	}
 
 	public static Node evaluateEagerFunctional(String program) {
 		return evaluateFunctional(program, false);
 	}
 
 	public static Node evaluateLazyFunctional(String program) {
 		return evaluateFunctional(program, true);
 	}
 
 	public static Node evaluateFunctional(String program, boolean isLazy) {
 		return evaluateFunctional(fcc(program, isLazy));
 	}
 
 	public static Node evaluateFunctional(FunCompilerConfig config) {
 		Prover compiler = config.isLazy ? getLazyFunCompiler()
 				: getEagerFunCompiler();
 		compiler = config.isTrace ? enableTrace(compiler) : compiler;
 
 		String program = appendLibraries(config);
 		String s = "compile-function .mode (" + program + ") .code"
 				+ (config.isDumpCode ? ", pretty.print .code" : "");
 		Node node = SuiteUtil.parse(s);
 
 		Generalizer generalizer = new Generalizer();
 		node = generalizer.generalize(node);
 		Node modeRef = generalizer.getVariable(Atom.create(".mode"));
 		Node progRef = generalizer.getVariable(Atom.create(".program"));
 		Node ics = generalizer.getVariable(Atom.create(".code"));
 
 		Atom mode = Atom.create(config.isLazy ? "LAZY" : "EAGER");
 
 		((Reference) modeRef).bound(mode);
 		((Reference) progRef).bound(config.node);
 		if (compiler.prove(node)) {
 			FunctionInstructionExecutor e = new FunctionInstructionExecutor(ics);
 			e.setIn(config.in);
 			e.setOut(config.out);
 			e.setProver(compiler);
 
 			Node result = e.execute();
 			if (config.isLazy)
 				result = e.unwrap(result);
 			return result;
 		} else
 			throw new RuntimeException("Function compilation error");
 	}
 
 	public static Node evaluateFunctionalType(String program) {
 		return evaluateFunctionalType(fcc(SuiteUtil.parse(program)));
 	}
 
 	public static Node evaluateFunctionalType(FunCompilerConfig config) {
 		Prover compiler = lazyFunctionalCompiler;
 		compiler = compiler != null ? compiler : getEagerFunCompiler();
 		compiler = config.isTrace ? enableTrace(compiler) : compiler;
 
		Node node = SuiteUtil.parse("" //
				+ "fc-parse (" + appendLibraries(config) + ") .p" //
				+ ", infer-type-rule .p ()/()/()/() .tr .t" //
				+ ", resolve-types .tr" //
				+ ", fc-parse-type .type .t");
 
 		Generalizer generalizer = new Generalizer();
 		node = generalizer.generalize(node);
 		Node variable = generalizer.getVariable(Atom.create(".program"));
 		Node type = generalizer.getVariable(Atom.create(".type"));
 
 		((Reference) variable).bound(config.node);
 
 		if (compiler.prove(node))
 			return type.finalNode();
 		else
 			throw new RuntimeException("Type inference error");
 	}
 
 	private static Prover enableTrace(Prover compiler) {
 		compiler = new Prover(compiler);
 		compiler.setEnableTrace(true);
 		return compiler;
 	}
 
 	private static String appendLibraries(FunCompilerConfig config) {
 		StringBuilder sb = new StringBuilder();
 		for (String library : config.libraries)
 			sb.append("using " + library + " >> ");
 		sb.append("(.program)");
 		return sb.toString();
 	}
 
 	public static synchronized Prover getLogicalCompiler() {
 		if (logicalCompiler == null)
 			logicalCompiler = getProver(new String[] { "auto.sl", "lc.sl" });
 		return logicalCompiler;
 	}
 
 	public static synchronized Prover getEagerFunCompiler() {
 		if (eagerFunctionalCompiler == null) {
 			String imports[] = { "auto.sl", "fc.sl", "fc-eager-evaluation.sl" };
 			eagerFunctionalCompiler = getProver(imports);
 		}
 		return eagerFunctionalCompiler;
 	}
 
 	public static synchronized Prover getLazyFunCompiler() {
 		if (lazyFunctionalCompiler == null) {
 			String imports[] = { "auto.sl", "fc.sl", "fc-lazy-evaluation.sl" };
 			lazyFunctionalCompiler = getProver(imports);
 		}
 		return lazyFunctionalCompiler;
 	}
 
 	public static Prover getProver(String toImports[]) {
 		RuleSet rs = new RuleSet();
 
 		try {
 			for (String toImport : toImports)
 				SuiteUtil.importResource(rs, toImport);
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 
 		return new Prover(rs);
 	}
 
 	public static Node parse(String s) {
 		return parser.parse(s);
 	}
 
 	public static Node parse(InputStream is) throws IOException {
 		return parser.parse(is);
 	}
 
 	/**
 	 * Forms a string using ASCII codes in a list of number.
 	 */
 	public static String stringize(Node node) {
 		StringBuilder sb = new StringBuilder();
 		Tree tree;
 
 		while ((tree = Tree.decompose(node, TermOp.AND___)) != null) {
 			Int i = (Int) tree.getLeft();
 			sb.append((char) i.getNumber());
 			node = tree.getRight();
 		}
 
 		return sb.toString();
 	}
 
 }
