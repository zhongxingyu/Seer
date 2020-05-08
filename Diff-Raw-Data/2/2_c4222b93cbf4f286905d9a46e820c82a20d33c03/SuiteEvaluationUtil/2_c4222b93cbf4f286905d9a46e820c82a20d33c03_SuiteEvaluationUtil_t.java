 package org.suite;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.instructionexecutor.FunInstructionExecutor;
 import org.instructionexecutor.LogicInstructionExecutor;
 import org.suite.doer.Cloner;
 import org.suite.doer.Generalizer;
 import org.suite.doer.Prover;
 import org.suite.doer.ProverConfig;
 import org.suite.kb.RuleSet;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.search.InterpretedProveBuilder;
 import org.suite.search.ProveSearch.Builder;
 import org.suite.search.ProveSearch.Finder;
 import org.util.FunUtil;
 import org.util.FunUtil.Sink;
 import org.util.FunUtil.Source;
 import org.util.Util;
 
 public class SuiteEvaluationUtil {
 
 	private class Collector implements Sink<Node> {
 		private List<Node> nodes = new ArrayList<>();
 
 		public void apply(Node node) {
 			nodes.add(new Cloner().clone(node));
 		}
 
 		public Node getNode() {
 			return nodes.size() == 1 ? nodes.get(0) : null;
 		}
 
 		public List<Node> getNodes() {
 			return nodes;
 		}
 	}
 
 	public boolean proveThis(RuleSet rs, String s) {
 		Prover prover = new Prover(rs);
 		Generalizer generalizer = new Generalizer();
 		return prover.prove(generalizer.generalize(SuiteUtil.parse(s)));
 	}
 
 	public boolean evaluateLogical(String lps) {
 		return evaluateLogical(SuiteUtil.parse(lps));
 	}
 
 	public boolean evaluateLogical(Node lp) {
 		ProverConfig pc = new ProverConfig();
 		return !evaluateLogical(lp, Atom.NIL, pc, false).isEmpty();
 	}
 
 	public List<Node> evaluateLogical(Node lp //
 			, Node eval //
 			, ProverConfig pc //
 			, boolean isDumpCode) {
 		Source<Node> source = FunUtil.nullSource();
 		Collector sink = new Collector();
 		evaluateLogical(lp, eval, pc, isDumpCode, source, sink);
 		return sink.getNodes();
 	}
 
 	private void evaluateLogical(Node lp //
 			, Node eval //
 			, ProverConfig pc //
 			, boolean isDumpCode //
 			, Source<Node> source //
 			, Sink<Node> sink) {
 		RuleSet rs = SuiteUtil.logicalRuleSet();
 
 		String goal = "compile-logic (.0, sink .1) .2"
 				+ (isDumpCode ? ", pretty.print .2" : "");
 		Node node = SuiteUtil.substitute(goal, Builder.in, eval, Builder.out);
 
 		Finder finder = new InterpretedProveBuilder(pc).build(rs, node);
 		Node code = singleResult(finder, lp);
 
 		if (code != null) {
			Prover p = new Prover(pc);
 			new LogicInstructionExecutor(code, p, source, sink).execute();
 		} else
 			throw new RuntimeException("Logic compilation error");
 	}
 
 	public Node evaluateEagerFun(String fp) {
 		return evaluateFun(fp, false);
 	}
 
 	public Node evaluateLazyFun(String fp) {
 		return evaluateFun(fp, true);
 	}
 
 	private Node evaluateFun(String fp, boolean isLazy) {
 		return evaluateFun(SuiteUtil.fcc(fp, isLazy));
 	}
 
 	public Node evaluateFun(FunCompilerConfig fcc) {
 		RuleSet rs = fcc.isLazy() ? SuiteUtil.lazyFunRuleSet() : SuiteUtil
 				.eagerFunRuleSet();
 		ProverConfig pc = fcc.getProverConfig();
 		String program = appendLibraries(fcc, ".1");
 
 		String s = "compile-function .0 (" + program + ") .2"
 				+ (fcc.isDumpCode() ? ", pretty.print .2" : "");
 		Node node = SuiteUtil.substitute(s //
 				, Atom.create(fcc.isLazy() ? "LAZY" : "EAGER") //
 				, Builder.in //
 				, Builder.out);
 
 		Finder finder = new InterpretedProveBuilder(pc).build(rs, node);
 		Node code = singleResult(finder, fcc.getNode());
 
 		if (code != null) {
 			FunInstructionExecutor e = new FunInstructionExecutor(code);
 			e.setIn(fcc.getIn());
 			e.setOut(fcc.getOut());
 			e.setProver(new Prover(new ProverConfig(rs, pc)));
 
 			Node result = e.execute();
 			if (fcc.isLazy())
 				result = e.unwrap(result);
 			return result;
 		} else
 			throw new RuntimeException("Function compilation error");
 	}
 
 	public Node evaluateFunType(String fps) {
 		return evaluateFunType(SuiteUtil.fcc(SuiteUtil.parse(fps)));
 	}
 
 	public Node evaluateFunType(FunCompilerConfig fcc) {
 		RuleSet rs = SuiteUtil.eagerFunRuleSet();
 		ProverConfig pc = fcc.getProverConfig();
 
 		Node node = SuiteUtil.substitute("" //
 				+ "fc-parse (" + appendLibraries(fcc, ".0") + ") .p" //
 				+ ", infer-type-rule .p ()/()/() .tr/() .t" //
 				+ ", resolve-types .tr" //
 				+ ", fc-parse-type .1 .t" //
 		, Builder.in, Builder.out);
 
 		Finder finder = new InterpretedProveBuilder(pc).build(rs, node);
 		Node type = singleResult(finder, fcc.getNode());
 
 		if (type != null)
 			return type.finalNode();
 		else
 			throw new RuntimeException("Type inference error");
 	}
 
 	private String appendLibraries(FunCompilerConfig fcc, String variable) {
 		StringBuilder sb = new StringBuilder();
 		for (String library : fcc.getLibraries())
 			if (!Util.isBlank(library))
 				sb.append("using " + library + " >> ");
 		sb.append("(" + variable + ")");
 		return sb.toString();
 	}
 
 	private Node singleResult(Finder finder, Node in) {
 		Collector sink = new Collector();
 		finder.find(in, sink);
 		return sink.getNode();
 	}
 
 }
