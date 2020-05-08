 package suite;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import suite.fp.FunCompilerConfig;
 import suite.lp.doer.Generalizer;
 import suite.lp.doer.Prover;
 import suite.lp.doer.ProverConfig;
 import suite.lp.kb.RuleSet;
 import suite.lp.search.CompiledProverBuilder;
 import suite.lp.search.InterpretedProverBuilder;
 import suite.lp.search.ProverBuilder.Builder;
 import suite.node.Atom;
 import suite.node.Data;
 import suite.node.Node;
 import suite.node.Tree;
 import suite.node.io.Formatter;
 import suite.node.io.PrettyPrinter;
 import suite.node.io.TermParser;
 import suite.node.io.TermParser.TermOp;
 import suite.util.FileUtil;
 import suite.util.FunUtil.Source;
 import suite.util.LogUtil;
 import suite.util.ParserUtil;
 import suite.util.Util;
 
 /**
  * Logic interpreter and functional interpreter. Likes Prolog and Haskell.
  * 
  * @author ywsing
  */
 public class Main implements AutoCloseable {
 
 	private FunCompilerConfig fcc = new FunCompilerConfig();
 	private ProverConfig proverConfig = fcc.getProverConfig();
 
 	private boolean isQuiet = false;
 	private boolean isFilter = false;
 	private boolean isFunctional = false;
 	private boolean isLogical = false;
 
 	private Reader reader = new InputStreamReader(System.in, FileUtil.charset);
 	private Writer writer = new OutputStreamWriter(System.out, FileUtil.charset);
 
 	private enum InputType {
 		EVALUATE("\\"), //
 		EVALUATEDO("\\d"), //
 		EVALUATEDOSTR("\\ds"), //
 		EVALUATESTR("\\s"), //
 		EVALUATETYPE("\\t"), //
 		FACT(""), //
 		OPTION("-"), //
 		PRETTYPRINT("\\p"), //
 		QUERY("?"), //
 		QUERYCOMPILED("/l"), //
 		QUERYCOMPILED2("/ll"), //
 		QUERYELABORATE("/"), //
 		;
 
 		private String prefix;
 
 		private InputType(String prefix) {
 			this.prefix = prefix;
 		}
 	}
 
 	public static void main(String args[]) {
 		int code;
 
 		try (Main main = new Main()) {
 			try {
 				code = main.run(args);
 			} catch (Throwable ex) {
 				LogUtil.error(ex);
 				code = 1;
 			}
 		}
 
 		System.exit(code);
 	}
 
 	private int run(String args[]) throws IOException {
 		boolean result = true;
 		List<String> inputs = new ArrayList<>();
 		Iterator<String> iter = Arrays.asList(args).iterator();
 
 		while (iter.hasNext()) {
 			String arg = iter.next();
 
 			if (arg.startsWith("-"))
 				result &= processOption(arg, iter);
 			else
 				inputs.add(arg);
 		}
 
 		if (result)
 			if (isFilter)
 				result &= runFilter(inputs); // Inputs as program
 			else if (isFunctional)
 				result &= runFunctional(inputs); // Inputs as files
 			else if (isLogical)
 				result &= runLogical(inputs); // Inputs as files
 			else
 				result &= run(inputs); // Inputs as files
 
 		return result ? 0 : 1;
 	}
 
 	private boolean run(List<String> importFilenames) throws IOException {
 		Builder builderL2 = null;
 		RuleSet ruleSet = proverConfig.ruleSet();
 		Suite.importResource(ruleSet, "auto.sl");
 
 		for (String importFilename : importFilenames)
 			Suite.importFile(ruleSet, importFilename);
 
 		BufferedReader br = new BufferedReader(reader);
 		boolean code = true;
 
 		prompt().println("READY");
 
 		while (true)
 			try {
 				StringBuilder sb = new StringBuilder();
 				String line;
 
 				do {
 					prompt().print(sb.length() == 0 ? "=> " : "   ");
 
 					if ((line = br.readLine()) != null)
 						sb.append(line + "\n");
 					else
 						return code;
 				} while (!isQuiet //
						&& (!ParserUtil.isParseable(sb.toString()) || !line.isEmpty() && !line.endsWith("#")));
 
 				String input = sb.toString();
 
 				if (Util.isBlank(input))
 					continue;
 
 				InputType type = null;
 
 				commandFound: for (int i = Math.min(3, input.length()); i >= 0; i--) {
 					String starts = input.substring(0, i);
 
 					for (InputType inputType : InputType.values())
 						if (Util.equals(starts, inputType.prefix)) {
 							type = inputType;
 							input = input.substring(i);
 							break commandFound;
 						}
 				}
 
 				input = input.trim();
 				if (input.endsWith("#"))
 					input = Util.substr(input, 0, -1);
 
 				final int count[] = { 0 };
 				Node node = new TermParser().parse(input.trim());
 
 				switch (type) {
 				case EVALUATE:
 					System.out.println(Formatter.dump(evaluateFunctional(node)));
 					break;
 				case EVALUATEDO:
 					node = Suite.applyDo(node, Atom.create("any"));
 					System.out.println(Formatter.dump(evaluateFunctional(node)));
 					break;
 				case EVALUATEDOSTR:
 					node = Suite.applyDo(node, Atom.create("string"));
 					printEvaluatedString(node);
 					break;
 				case EVALUATESTR:
 					node = Suite.substitute(".0 as string", node);
 					printEvaluatedString(node);
 					break;
 				case EVALUATETYPE:
 					fcc.setNode(node);
 					System.out.println(Formatter.dump(Suite.evaluateFunType(fcc)));
 					break;
 				case FACT:
 					Suite.addRule(ruleSet, node);
 					break;
 				case OPTION:
 					List<String> args = Arrays.asList(("-" + input).split(" "));
 					Iterator<String> iter = args.iterator();
 					while (iter.hasNext())
 						processOption(iter.next(), iter);
 					break;
 				case PRETTYPRINT:
 					System.out.println(new PrettyPrinter().prettyPrint(node));
 					break;
 				case QUERY:
 					code = query(new InterpretedProverBuilder(proverConfig), ruleSet, node);
 					break;
 				case QUERYELABORATE:
 					final Generalizer generalizer = new Generalizer();
 					node = generalizer.generalize(node);
 					Prover prover = new Prover(proverConfig);
 
 					Node elab = new Data<Source<Boolean>>(new Source<Boolean>() {
 						public Boolean source() {
 							String dump = generalizer.dumpVariables();
 							if (!dump.isEmpty())
 								prompt().println(dump);
 
 							count[0]++;
 							return Boolean.FALSE;
 						}
 					});
 
 					prover.prove(Tree.create(TermOp.AND___, node, elab));
 
 					if (count[0] == 1)
 						prompt().println(count[0] + " solution\n");
 					else
 						prompt().println(count[0] + " solutions\n");
 
 					break;
 				case QUERYCOMPILED:
 					code = query(CompiledProverBuilder.level1(proverConfig, fcc.isDumpCode()), ruleSet, node);
 					break;
 				case QUERYCOMPILED2:
 					if (builderL2 == null)
 						builderL2 = CompiledProverBuilder.level2(proverConfig, fcc.isDumpCode());
 					code = query(builderL2, ruleSet, node);
 				}
 			} catch (Throwable ex) {
 				LogUtil.error(ex);
 			}
 	}
 
 	private boolean processOption(String arg, Iterator<String> iter) {
 		return processOption(arg, iter, true);
 	}
 
 	private boolean processOption(String arg, Iterator<String> iter, boolean on) {
 		boolean result = true;
 
 		if (arg.equals("-dump-code"))
 			fcc.setDumpCode(on);
 		else if (arg.equals("-eager"))
 			fcc.setLazy(!on);
 		else if (arg.equals("-filter"))
 			isFilter = on;
 		else if (arg.equals("-functional"))
 			isFunctional = on;
 		else if (arg.equals("-lazy"))
 			fcc.setLazy(on);
 		else if (arg.equals("-libraries") && iter.hasNext())
 			fcc.setLibraries(Arrays.asList(iter.next().split(",")));
 		else if (arg.equals("-logical"))
 			isLogical = on;
 		else if (arg.startsWith("-no-"))
 			result &= processOption("-" + arg.substring(4), iter, false);
 		else if (arg.equals("-precompile") && iter.hasNext())
 			for (String lib : iter.next().split(","))
 				result &= Suite.precompile(lib, proverConfig);
 		else if (arg.equals("-quiet"))
 			isQuiet = on;
 		else if (arg.equals("-trace"))
 			proverConfig.setTrace(on);
 		else
 			throw new RuntimeException("Unknown option " + arg);
 
 		return result;
 	}
 
 	private void printEvaluatedString(Node node) throws IOException {
 		evaluateFunctionalToWriter(node, writer);
 		writer.flush();
 	}
 
 	private boolean query(Builder builder, RuleSet ruleSet, Node node) {
 		boolean result = Suite.proveLogic(builder, ruleSet, node);
 		prompt().println(yesNo(result));
 		return result;
 	}
 
 	private boolean runLogical(List<String> files) throws IOException {
 		boolean result = true;
 
 		RuleSet ruleSet = Suite.createRuleSet();
 		result &= Suite.importResource(ruleSet, "auto.sl");
 
 		for (String file : files)
 			result &= Suite.importFile(ruleSet, file);
 
 		return result;
 	}
 
 	private boolean runFilter(List<String> inputs) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		for (String input : inputs)
 			sb.append(input + " ");
 
 		Node node = Suite.applyReader(reader, Suite.parse(sb.toString()));
 		evaluateFunctionalToWriter(node, writer);
 		return true;
 	}
 
 	private boolean runFunctional(List<String> files) throws IOException {
 		if (files.size() == 1)
 			try (FileInputStream is = new FileInputStream(files.get(0))) {
 				Node node = Suite.parse(is);
 				return evaluateFunctional(node) == Atom.TRUE;
 			}
 		else
 			throw new RuntimeException("Only one evaluation is allowed");
 	}
 
 	private Node evaluateFunctional(Node node) {
 		fcc.setNode(node);
 		return Suite.evaluateFun(fcc);
 	}
 
 	private void evaluateFunctionalToWriter(Node node, Writer writer) throws IOException {
 		fcc.setNode(node);
 		Suite.evaluateFunToWriter(fcc, writer);
 	}
 
 	private String yesNo(boolean q) {
 		return q ? "Yes\n" : "No\n";
 	}
 
 	private PrintStream prompt() {
 		try (OutputStream os = new OutputStream() {
 			public void write(int c) {
 			}
 		}) {
 			return !isQuiet ? System.out : new PrintStream(os);
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	@Override
 	public void close() {
 		Util.closeQuietly(reader);
 		Util.closeQuietly(writer);
 	}
 
 }
