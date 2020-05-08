 package org.suite;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.suite.doer.Formatter;
 import org.suite.doer.Generalizer;
 import org.suite.doer.PrettyPrinter;
 import org.suite.doer.Prover;
 import org.suite.doer.ProverConfig;
 import org.suite.doer.Station;
 import org.suite.doer.TermParser;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.kb.RuleSet;
 import org.suite.kb.RuleSetUtil;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 import org.suite.search.CompiledProverBuilder.CompiledProverBuilderLevel1;
 import org.suite.search.ProverBuilder.Builder;
 import org.util.IoUtil;
 import org.util.LogUtil;
 import org.util.Util;
 
 /**
  * Logic interpreter and functional interpreter. Likes Prolog and Haskell.
  * 
  * @author ywsing
  */
 public class Main {
 
 	private FunCompilerConfig fcc = new FunCompilerConfig();
	private ProverConfig proverConfig = fcc.getProverConfig();
 
 	private boolean isFilter = false;
 	private boolean isFunctional = false;
 	private boolean isLogical = false;
 
 	private enum InputType {
 		EVALUATE("\\"), //
 		EVALUATESTR("\\s"), //
 		EVALUATETYPE("\\t"), //
 		FACT(""), //
 		OPTION("-"), //
 		PRETTYPRINT("\\p"), //
 		QUERY("?"), //
 		QUERYCOMPILED("\\l"), //
 		QUERYELABORATE("/"), //
 		;
 
 		String prefix;
 
 		private InputType(String prefix) {
 			this.prefix = prefix;
 		}
 	}
 
 	public static void main(String args[]) {
 		try {
 			new Main().run(args);
 		} catch (Throwable ex) {
 			LogUtil.error(ex);
 		}
 	}
 
 	private void run(String args[]) throws IOException {
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
 				result &= runFilter(inputs);
 			else if (isFunctional)
 				result &= runFunctional(inputs);
 			else if (isLogical)
 				result &= runLogical(inputs);
 			else
 				result &= run(inputs);
 
 		System.exit(result ? 0 : 1);
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
 		else if (arg.equals("-trace"))
 			proverConfig.setTrace(on);
 		else
 			throw new RuntimeException("Unknown option " + arg);
 
 		return result;
 	}
 
 	private boolean run(List<String> importFilenames) throws IOException {
 		RuleSet ruleSet = proverConfig.ruleSet();
 		Suite.importResource(ruleSet, "auto.sl");
 
 		for (String importFilename : importFilenames)
 			Suite.importFile(ruleSet, importFilename);
 
 		InputStreamReader is = new InputStreamReader(System.in, IoUtil.charset);
 		BufferedReader br = new BufferedReader(is);
 
 		System.out.println("READY");
 
 		while (true)
 			try {
 				StringBuilder sb = new StringBuilder();
 				String line;
 
 				do {
 					System.out.print(sb.length() == 0 ? "=> " : "   ");
 
 					if ((line = br.readLine()) != null)
 						sb.append(line + "\n");
 					else
 						return true;
 				} while (!line.isEmpty() && !line.endsWith("#"));
 
 				String input = sb.toString();
 
 				if (Util.isBlank(input))
 					continue;
 
 				InputType type = null;
 
 				commandFound: for (int i = Math.min(2, input.length()); i >= 0; i--) {
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
 				case EVALUATESTR:
 					System.out.println(Suite.stringize(evaluateFunctional(node)));
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
 				case QUERYELABORATE:
 					final Generalizer generalizer = new Generalizer();
 					node = generalizer.generalize(node);
 					Prover prover = new Prover(proverConfig);
 
 					if (type == InputType.QUERY)
 						System.out.println(yesNo(prover.prove(node)));
 					else if (type == InputType.QUERYELABORATE) {
 						Node elab = new Station() {
 							public boolean run() {
 								String dump = generalizer.dumpVariables();
 								if (!dump.isEmpty())
 									System.out.println(dump);
 
 								count[0]++;
 								return false;
 							}
 						};
 
 						prover.prove(Tree.create(TermOp.AND___, node, elab));
 
 						if (count[0] == 1)
 							System.out.println(count[0] + " solution\n");
 						else
 							System.out.println(count[0] + " solutions\n");
 					}
 					break;
 				case QUERYCOMPILED:
 					node = Suite.substitute(".0, sink ()", node);
 					Builder builder = new CompiledProverBuilderLevel1(proverConfig, fcc.isDumpCode());
 					List<Node> nodes = Suite.evaluateLogical(builder, ruleSet, node);
 					System.out.println(yesNo(!nodes.isEmpty()));
 				}
 			} catch (Throwable ex) {
 				LogUtil.error(ex);
 			}
 	}
 
 	private boolean runLogical(List<String> files) throws IOException {
 		boolean result = true;
 
 		RuleSet ruleSet = RuleSetUtil.create();
 		result &= Suite.importResource(ruleSet, "auto.sl");
 
 		for (String file : files)
 			result &= Suite.importFile(ruleSet, file);
 
 		return result;
 	}
 
 	private boolean runFilter(List<String> inputs) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		for (String input : inputs)
 			sb.append(input + " ");
 
 		Node node = Suite.applyFilter(Suite.parse(sb.toString()));
 		Reader reader = new InputStreamReader(System.in, IoUtil.charset);
 		Writer writer = new OutputStreamWriter(System.out, IoUtil.charset);
 		evaluateFunctional(node, reader, writer);
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
 
 	private void evaluateFunctional(Node node, Reader reader, Writer writer) throws IOException {
 		fcc.setNode(node);
		Suite.evaluateFunIo(fcc, reader, writer);
 	}
 
 	private String yesNo(boolean q) {
 		return q ? "Yes\n" : "No\n";
 	}
 
 }
