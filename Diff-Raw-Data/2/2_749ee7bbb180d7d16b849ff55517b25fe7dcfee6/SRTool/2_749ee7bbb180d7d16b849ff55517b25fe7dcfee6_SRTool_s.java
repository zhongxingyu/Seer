 package srt.tool;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.Tree;
 
 import srt.ast.Node;
 import srt.ast.Program;
 import srt.ast.visitor.impl.Checker;
 import srt.ast.visitor.impl.MakeBlockVisitor;
 import srt.ast.visitor.impl.PrinterVisitor;
 import srt.exec.SMTQuery;
 import srt.parser.SimpleCParserUtil;
 import srt.tool.exception.CheckerExpception;
 import srt.tool.exception.SMTSolverTimeoutException;
 import srt.tool.exception.UnknownResultException;
 
 
 public class SRTool {
 	private String inputFile;
 	private CLArgs clArgs;
 
 	public SRTool(String inputFile, CLArgs clArgs) {
 		super();
 		this.inputFile = inputFile;
 		this.clArgs = clArgs;
 	}
 
 	public List<AssertionFailure> go() throws IOException,
 			RecognitionException, CheckerExpception, InterruptedException,
 			SMTSolverTimeoutException, UnknownResultException {
 		// We will return a list of assertions that can fail.
 		List<AssertionFailure> result = new ArrayList<AssertionFailure>();
 
 		// Parse input Simple C file to AST.
 		Program p = SimpleCParserUtil.createAST(inputFile);
 
 		// Add blocks to make things simpler.
 		// E.g. if(c) stmt; becomes if(c) {stmt;} else {}
 		p = (Program) new MakeBlockVisitor().visit(p);
 
 		// Do basic checks.
 		// E.g. Variables declared before use,
 		// no duplicate local variables.
 		Checker checker = new Checker();
 		boolean success = checker.check(p);
 		if (!success) {
 			throw new CheckerExpception(checker.getCheckerError());
 		}
 
 		// TODO: Transform program using Visitors here.
 		if (clArgs.abstractLoops) {
 			p = (Program) new LoopAbstractionVisitor().visit(p);
 		} else {
 			p = (Program) new LoopUnwinderVisitor(clArgs.unwindingAssertions,
 					clArgs.unwindDepth).visit(p);
 		}
		p = (Program) new PredicationVisitor().visit(p);
 		p = (Program) new SSAVisitor().visit(p);
 
 		// Output the program as text after being transformed (for debugging).
 		String programText = new PrinterVisitor().visit(p);
 		System.out.println(programText);
 
 		// Collect the constraint expressions and variable names.
 		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
 		ccv.visit(p);
 
 		// Stop here if there are no assertions (properties) to check.
 		if (ccv.propertyExprs.size() == 0) {
 			System.out.println("No asserts! Stopping.");
 			return result;
 		}
 
 		// TODO: Section 4: Convert constraints to SMTLIB String.
 		SMTLIBConverter converter = new SMTLIBConverter(ccv.variableNames,
 				ccv.transitionExprs, ccv.propertyExprs);
 		String smtQuery = converter.getQuery();
 
 		// Submit query to SMT solver.
 		SMTQuery query = new SMTQuery(smtQuery, clArgs.timeout * 1000);
 		String queryResult = query.go();
 		if (queryResult == null) {
 			throw new SMTSolverTimeoutException("Timeout!");
 		}
 		System.out.println(queryResult);
 
 		// Return the assertions that can be violated.
 		if (queryResult.startsWith("sat")) {
 			List<Integer> indexesFailed = converter
 					.getPropertiesThatFailed(queryResult);
 
 			// TODO: Use "indexesFailed" after implementing
 			// "getPropertiesThatFailed".
 			// For now:
 			for (Integer i : indexesFailed) {
 				Node exp = ccv.propertyNodes.get(i);
 				if (exp != null && exp.getTokenInfo() != null) {
 					result.add(new AssertionFailure(exp.getTokenInfo()));
 				}
 			}
 			
 			if (indexesFailed.isEmpty()) result.add(new AssertionFailure(null));
 
 		} else if (!queryResult.startsWith("unsat")) {
 			throw new UnknownResultException();
 		}
 		
 		return result;
 	}
 }
