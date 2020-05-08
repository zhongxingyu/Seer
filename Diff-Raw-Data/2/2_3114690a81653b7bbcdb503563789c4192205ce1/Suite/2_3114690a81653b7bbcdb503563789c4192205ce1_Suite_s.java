 package suite;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import suite.fp.FunCompilerConfig;
 import suite.instructionexecutor.IndexedReader;
 import suite.lp.ImportUtil;
 import suite.lp.doer.Generalizer;
 import suite.lp.doer.Prover;
 import suite.lp.doer.ProverConfig;
 import suite.lp.doer.ProverConfig.TraceLevel;
 import suite.lp.kb.Prototype;
 import suite.lp.kb.Rule;
 import suite.lp.kb.RuleSet;
 import suite.lp.search.ProverBuilder.Builder;
 import suite.node.Atom;
 import suite.node.Data;
 import suite.node.Int;
 import suite.node.Node;
 import suite.node.Reference;
 import suite.node.Tree;
 import suite.node.io.TermParser.TermOp;
 
 public class Suite {
 
 	// Compilation defaults
 	public static final boolean isTrace = false;
 	public static final boolean isDumpCode = false;
 	public static final List<String> libraries = Arrays.asList("STANDARD");
 	public static final TraceLevel traceLevel = TraceLevel.SIMPLE;
 
 	private static CompileUtil compileUtil = new CompileUtil();
 	private static EvaluateUtil evaluateUtil = new EvaluateUtil();
 	private static ImportUtil importUtil = new ImportUtil();
 	private static ParseUtil parseUtil = new ParseUtil();
 
 	public static void addRule(RuleSet rs, String rule) {
 		addRule(rs, Suite.parse(rule));
 	}
 
 	public static void addRule(RuleSet rs, Node node) {
 		rs.addRule(Rule.formRule(node));
 	}
 
 	public static Node applyDo(Node node, Atom returnType) {
 		return Suite.substitute("type (do-of .1 => number => .1) (no-type-check id) {.0} {0}", node, returnType);
 	}
 
 	public static Node applyReader(Reader reader, Node func) {
 		Data<IndexedReader> data = new Data<IndexedReader>(new IndexedReader(reader));
		return Suite.substitute("source {atom:`.0`} | .1", data, func);
 	}
 
 	public static FunCompilerConfig fcc(Node fp) {
 		return fcc(fp, false);
 	}
 
 	public static FunCompilerConfig fcc(Node fp, boolean isLazy) {
 		FunCompilerConfig fcc = new FunCompilerConfig();
 		fcc.setNode(fp);
 		fcc.setLazy(isLazy);
 		return fcc;
 	}
 
 	public static Node ruleSetToNode(RuleSet rs) {
 		return getRuleList(rs, null);
 	}
 
 	public static RuleSet nodeToRuleSet(Node node) {
 		RuleSet rs = createRuleSet();
 		importUtil.importFrom(rs, node);
 		return rs;
 	}
 
 	/**
 	 * Convert rules in a rule set back into to #-separated format.
 	 * 
 	 * May specify a prototype to limit the rules listed.
 	 */
 	public static Node getRuleList(RuleSet rs, Prototype proto) {
 		List<Node> nodes = new ArrayList<>();
 
 		for (Rule rule : rs.getRules()) {
 			Prototype p1 = Prototype.get(rule);
 			if (proto == null || proto.equals(p1)) {
 				Node clause = Rule.formClause(rule);
 				nodes.add(clause);
 			}
 		}
 
 		Node node = Atom.NIL;
 		for (int i = nodes.size() - 1; i >= 0; i--)
 			node = Tree.create(TermOp.NEXT__, nodes.get(i), node);
 		return node;
 	}
 
 	/**
 	 * Forms a string using ASCII codes in a list of number.
 	 */
 	public static String stringize(Node node) {
 		StringBuilder sb = new StringBuilder();
 
 		for (Node elem : Node.iter(node)) {
 			Int i = (Int) elem;
 			sb.append((char) i.getNumber());
 		}
 
 		return sb.toString();
 	}
 
 	public static Node substitute(String s, Node... nodes) {
 		Node result = parse(s);
 		Generalizer generalizer = new Generalizer();
 		result = generalizer.generalize(result);
 		int i = 0;
 
 		for (Node node : nodes) {
 			Node variable = generalizer.getVariable(Atom.create("." + i++));
 			((Reference) variable).bound(node);
 		}
 
 		return result;
 	}
 
 	// --------------------------------
 	// Compilation utilities
 
 	public static RuleSet logicCompilerRuleSet() {
 		return compileUtil.logicCompilerRuleSet();
 	}
 
 	public static RuleSet funCompilerRuleSet() {
 		return compileUtil.funCompilerRuleSet();
 	}
 
 	public static RuleSet funCompilerRuleSet(boolean isLazy) {
 		return compileUtil.funCompilerRuleSet(isLazy);
 	}
 
 	public static boolean precompile(String libraryName, ProverConfig pc) {
 		return compileUtil.precompile(libraryName, pc);
 	}
 
 	// --------------------------------
 	// Evaluation utilities
 
 	public static boolean proveLogic(String lps) {
 		return evaluateUtil.proveLogic(Suite.parse(lps));
 	}
 
 	public static boolean proveLogic(RuleSet rs, String lps) {
 		return evaluateUtil.proveLogic(rs, Suite.parse(lps));
 	}
 
 	public static boolean proveLogic(Builder builder, RuleSet rs, String lps) {
 		return evaluateUtil.proveLogic(builder, rs, Suite.parse(lps));
 	}
 
 	public static boolean proveLogic(Builder builder, RuleSet rs, Node lp) {
 		return evaluateUtil.proveLogic(builder, rs, lp);
 	}
 
 	public static List<Node> evaluateLogic(Builder builder, RuleSet rs, String lps) {
 		return evaluateUtil.evaluateLogic(builder, rs, Suite.parse(lps));
 	}
 
 	public static String evaluateFilterFun(String program, boolean isLazy, String in) {
 		try (Reader reader = new StringReader(in); Writer writer = new StringWriter()) {
 			evaluateFilterFun(program, isLazy, reader, writer);
 			return writer.toString();
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public static void evaluateFilterFun(String program, boolean isLazy, Reader reader, Writer writer) {
 		try {
 			Node node = Suite.applyReader(reader, Suite.parse(program));
 			FunCompilerConfig fcc = Suite.fcc(node, isLazy);
 			evaluateUtil.evaluateFunToWriter(fcc, writer);
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public static Node evaluateFun(String fp, boolean isLazy) {
 		return evaluateUtil.evaluateFun(Suite.fcc(Suite.parse(fp), isLazy));
 	}
 
 	public static Node evaluateFun(FunCompilerConfig fcc) {
 		return evaluateUtil.evaluateFun(fcc);
 	}
 
 	public static void evaluateFunToWriter(FunCompilerConfig fcc, Writer writer) throws IOException {
 		evaluateUtil.evaluateFunToWriter(fcc, writer);
 	}
 
 	public static Node evaluateFunType(String fps) {
 		return evaluateUtil.evaluateFunType(Suite.fcc(Suite.parse(fps)));
 	}
 
 	public static Node evaluateFunType(FunCompilerConfig fcc) {
 		return evaluateUtil.evaluateFunType(fcc);
 	}
 
 	// --------------------------------
 	// Import utilities
 
 	public static boolean importFrom(RuleSet rs, String name) throws IOException {
 		return importUtil.importFrom(rs, name);
 	}
 
 	public static boolean importFile(RuleSet rs, String filename) throws IOException {
 		return importUtil.importFile(rs, filename);
 	}
 
 	public static boolean importResource(RuleSet rs, String classpath) throws IOException {
 		return importUtil.importResource(rs, classpath);
 	}
 
 	public static boolean importFrom(RuleSet ruleSet, Node node) {
 		return importUtil.importFrom(ruleSet, node);
 	}
 
 	public static Prover createProver(List<String> toImports) {
 		return importUtil.createProver(toImports);
 	}
 
 	public static RuleSet createRuleSet(List<String> toImports) {
 		return importUtil.createRuleSet(toImports);
 	}
 
 	public static RuleSet createRuleSet() {
 		return importUtil.createRuleSet();
 	}
 
 	// --------------------------------
 	// Parse utilities
 
 	public static Node parse(String s) {
 		return parseUtil.parse(s);
 	}
 
 	public static Node parse(InputStream is) throws IOException {
 		return parseUtil.parse(is);
 	}
 
 }
