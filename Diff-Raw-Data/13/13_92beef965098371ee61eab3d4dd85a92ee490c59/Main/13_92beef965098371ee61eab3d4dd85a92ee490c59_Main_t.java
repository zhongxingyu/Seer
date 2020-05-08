 package org.suite;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.suite.doer.Formatter;
 import org.suite.doer.Generalizer;
 import org.suite.doer.Prover;
 import org.suite.doer.Station;
 import org.suite.doer.TermParser;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.kb.RuleSet;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 import org.util.IoUtil;
 import org.util.LogUtil;
 import org.util.Util;
 
 /**
  * Logic interpreter. Likes Prolog.
  * 
  * @author ywsing
  */
 public class Main {
 
 	public static void main(String args[]) {
 		LogUtil.initLog4j();
 
 		try {
 			int code = 0;
 
 			boolean isFilter = false;
 			boolean isFunctional = false;
 			boolean isLogical = false;
 			boolean isLazy = true;
 			List<String> inputs = Util.createList();
 
 			for (String arg : args)
 				if (arg.startsWith("-eager"))
 					isLazy = false;
 				else if (arg.startsWith("-filter"))
 					isFilter = true;
 				else if (arg.startsWith("-functional"))
 					isFunctional = true;
 				else if (arg.startsWith("-lazy"))
 					isLazy = true;
 				else if (arg.startsWith("-logical"))
 					isLogical = true;
 				else
 					inputs.add(arg);
 
 			if (isFilter)
 				code = new Main().runFilter(inputs, isLazy) ? 0 : 1;
 			else if (isFunctional)
 				code = new Main().runFunctional(inputs, isLazy) ? 0 : 1;
 			else if (isLogical)
 				code = new Main().runLogical(inputs) ? 0 : 1;
 			else
 				new Main().run(inputs);
 
 			System.exit(code);
 		} catch (Throwable ex) {
 			log.error(Main.class, ex);
 		}
 	}
 
 	public enum InputType {
 		FACT, QUERY, ELABORATE, EVALUATE
 	};
 
 	public void run(List<String> importFilenames) throws IOException {
 		RuleSet rs = new RuleSet();
 		SuiteUtil.importResource(rs, "auto.sl");
 
 		for (String importFilename : importFilenames)
 			SuiteUtil.importFile(rs, importFilename);
 
 		Prover prover = new Prover(rs);
 
 		InputStreamReader is = new InputStreamReader(System.in);
 		BufferedReader br = new BufferedReader(is);
 
 		quit: while (true)
 			try {
 				StringBuilder sb = new StringBuilder();
 				String line;
 
 				do {
 					System.out.print((sb.length() == 0) ? "=> " : "   ");
 
 					if ((line = br.readLine()) != null)
 						sb.append(line + "\n");
 					else
 						break quit;
 				} while (!line.isEmpty() && !line.endsWith("#"));
 
 				String input = sb.toString();
 				InputType type;
 
 				if (Util.isBlank(input))
 					continue;
 
 				if (input.startsWith("?")) {
 					type = InputType.QUERY;
 					input = input.substring(1);
 				} else if (input.startsWith("/")) {
 					type = InputType.ELABORATE;
 					input = input.substring(1);
 				} else if (input.startsWith("\\")) {
 					type = InputType.EVALUATE;
 					input = input.substring(1);
 				} else
 					type = InputType.FACT;
 
 				input = input.trim();
 				if (input.endsWith("#"))
 					input = input.substring(0, input.length() - 1);
 
 				final int count[] = { 0 };
 				Node node = new TermParser().parse(input.trim());
 
 				if (type == InputType.FACT)
 					rs.addRule(node);
 				else if (type == InputType.EVALUATE) {
 					Node result = SuiteUtil.evaluateFunctional(node, false);
 					System.out.println(Formatter.dump(result));
 				} else {
 					final Generalizer generalizer = new Generalizer();
 					node = generalizer.generalize(node);
 
 					if (type == InputType.QUERY) {
 						boolean result = prover.prove(node);
 						System.out.println(result ? "Yes\n" : "No\n");
 					} else if (type == InputType.ELABORATE) {
 						Node elab = new Station() {
 							public boolean run() {
 								String dump = generalizer.dumpVariables();
 								if (!dump.isEmpty())
 									System.out.println(dump);
 
 								count[0]++;
 								return false;
 							}
 						};
 
 						prover.prove(new Tree(TermOp.AND___, node, elab));
 
 						if (count[0] == 1)
 							System.out.println(count[0] + " solution\n");
 						else
 							System.out.println(count[0] + " solutions\n");
 					}
 				}
 			} catch (Throwable ex) {
 				LogUtil.error(Main.class, ex);
 			}
 	}
 
 	public boolean runLogical(List<String> files) throws IOException {
 		boolean result = true;
 
 		RuleSet rs = new RuleSet();
 		result &= SuiteUtil.importResource(rs, "auto.sl");
 
 		for (String file : files)
 			result &= SuiteUtil.importFile(rs, file);
 
 		return result;
 	}
 
 	public boolean runFilter(List<String> inputs, boolean isLazy)
 			throws IOException {
		StringBuilder sb = new StringBuilder();
		for (String input : inputs)
			sb.append(input + " ");

		Node result = SuiteUtil.evaluateFunctional(sb.toString(), isLazy);
		System.out.println(result);
		return true;
 	}
 
 	public boolean runFunctional(List<String> files, boolean isLazy)
 			throws IOException {
 		if (files.size() == 1) {
 			FileInputStream is = new FileInputStream(files.get(0));
 			String expression = IoUtil.readStream(is);
 			Node result = SuiteUtil.evaluateFunctional(expression, isLazy);
 			return result == Atom.create("true");
 		} else
 			throw new RuntimeException("Only one evaluation is allowed");
 	}
 
 	private static Log log = LogFactory.getLog(Util.currentClass());
 
 }
