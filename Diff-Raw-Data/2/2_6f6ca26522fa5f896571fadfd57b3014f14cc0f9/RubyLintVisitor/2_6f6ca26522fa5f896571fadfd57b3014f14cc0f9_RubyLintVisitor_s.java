 package org.rubypeople.rdt.internal.core.parser;
 
 import java.util.HashSet;
 import java.util.Set;
 import org.jruby.ast.BlockNode;
 import org.jruby.ast.CallNode;
 import org.jruby.ast.ConstDeclNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.FCallNode;
 import org.jruby.ast.FalseNode;
 import org.jruby.ast.IfNode;
 import org.jruby.ast.IterNode;
 import org.jruby.ast.NilNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.ScopeNode;
 import org.jruby.ast.TrueNode;
 import org.jruby.ast.WhenNode;
 import org.jruby.evaluator.Instruction;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.rubypeople.rdt.core.IProblemRequestor;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.parser.IProblem;
 
 public class RubyLintVisitor extends InOrderVisitor {
 
 	private IProblemRequestor problemRequestor;
 	private Set assignedConstants;
 	private Set methodsCalled;
 	private String contents;
 
 	public RubyLintVisitor(String contents, IProblemRequestor problemRequestor) {
 		this.problemRequestor = problemRequestor;
 		assignedConstants = new HashSet();
 		methodsCalled = new HashSet();
 		this.contents = contents;
 	}
 
 	public Instruction visitFCallNode(FCallNode iVisited) {
 		methodsCalled.add(iVisited.getName());
 		return super.visitFCallNode(iVisited);
 	}
 
 	public Instruction visitCallNode(CallNode iVisited) {
 		methodsCalled.add(iVisited.getName());
 		return super.visitCallNode(iVisited);
 	}
 
 	public Instruction visitIfNode(IfNode iVisited) {
 		Node condition = iVisited.getCondition();
 		if (condition instanceof TrueNode) {
 			problemRequestor.acceptProblem(new Warning(iVisited.getPosition(), "Condition is always true"));
 		} else if ((condition instanceof FalseNode)	|| (condition instanceof NilNode)) {
 			problemRequestor.acceptProblem(new Warning(iVisited.getPosition(), "Condition is always false"));
 		}
 
 		String source = NodeUtil.getSource(contents, iVisited);
		if (iVisited.getThenBody() == null && !source.contains("unless")) {
 			IProblem problem = createProblem(
 			RubyCore.COMPILER_PB_EMPTY_STATEMENT, iVisited.getPosition(), "Empty Conditional Body");
 
 			if (problem != null)
 				problemRequestor.acceptProblem(problem);
 		}
 		return super.visitIfNode(iVisited);
 	}
 
 	public Instruction visitWhenNode(WhenNode iVisited) {
 		if (iVisited.getBodyNode() == null) {
 			IProblem problem = createProblem(RubyCore.COMPILER_PB_EMPTY_STATEMENT, iVisited.getPosition(), "Empty When Body");
 			if (problem != null)
 				problemRequestor.acceptProblem(problem);
 		}
 		return super.visitWhenNode(iVisited);
 	}
 
 	public Instruction visitBlockNode(BlockNode iVisited) {
 		return super.visitBlockNode(iVisited);
 	}
 
 	public Instruction visitIterNode(IterNode iVisited) {
 		if (iVisited.getBodyNode() == null) {
 			IProblem problem = createProblem(RubyCore.COMPILER_PB_EMPTY_STATEMENT, iVisited.getPosition(), "Empty Block");
 
 			if (problem != null)
 				problemRequestor.acceptProblem(problem);
 		}
 		return super.visitIterNode(iVisited);
 	}
 
 	public Instruction visitDefnNode(DefnNode iVisited) {
 		// TODO Analyze method visibility. Create warning for uncalled private
 		// methods
 		ScopeNode scope = iVisited.getBodyNode();
 		if (scope.getBodyNode() == null) {
 			IProblem problem = createProblem(RubyCore.COMPILER_PB_EMPTY_STATEMENT, iVisited.getPosition(), "Empty Method Definition");
 			if (problem != null)
 				problemRequestor.acceptProblem(problem);
 		}
 		return super.visitDefnNode(iVisited);
 	}
 
 	public Instruction visitDefsNode(DefsNode iVisited) {
 		ScopeNode scope = iVisited.getBodyNode();
 		if (scope.getBodyNode() == null) {
 			IProblem problem = createProblem(RubyCore.COMPILER_PB_EMPTY_STATEMENT, iVisited.getPosition(), "Empty Method Definition");
 
 			if (problem != null)
 				problemRequestor.acceptProblem(problem);
 		}
 		return super.visitDefsNode(iVisited);
 	}
 
 	protected Instruction handleNode(Node visited) {
 		// System.out.println(visited.toString() + ", position -> "
 		// + visited.getPosition());
 		return super.handleNode(visited);
 	}
 
 	public Instruction visitConstDeclNode(ConstDeclNode iVisited) {
 		String name = iVisited.getName();
 
 		if (assignedConstants.contains(name)) {
 			problemRequestor.acceptProblem(new Warning(iVisited.getPosition(), "Reassignment of a constant"));
 		} else
 			assignedConstants.add(name);
 		return super.visitConstDeclNode(iVisited);
 	}
 
 	private IProblem createProblem(String compilerOption, ISourcePosition position, String message) {
 		String value = RubyCore.getOption(compilerOption);
 		if (value == null)
 			return new Error(position, message);
 		if (value.equals(RubyCore.WARNING))
 			return new Warning(position, message);
 		if (value.equals(RubyCore.ERROR))
 			return new Error(position, message);
 		return null;
 	}
 
 }
