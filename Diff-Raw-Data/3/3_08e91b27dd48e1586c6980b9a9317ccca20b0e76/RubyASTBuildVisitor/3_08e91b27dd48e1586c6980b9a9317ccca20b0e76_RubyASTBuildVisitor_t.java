 package org.eclipse.dltk.ruby.internal.parsers.jruby;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.Modifiers;
 import org.eclipse.dltk.ast.declarations.Argument;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Assignment;
 import org.eclipse.dltk.ast.expressions.BigNumericLiteral;
 import org.eclipse.dltk.ast.expressions.BinaryExpression;
 import org.eclipse.dltk.ast.expressions.BooleanLiteral;
 import org.eclipse.dltk.ast.expressions.CallArgumentsList;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.expressions.ExpressionList;
 import org.eclipse.dltk.ast.expressions.FloatNumericLiteral;
 import org.eclipse.dltk.ast.expressions.NilLiteral;
 import org.eclipse.dltk.ast.expressions.NumericLiteral;
 import org.eclipse.dltk.ast.expressions.StringLiteral;
 import org.eclipse.dltk.ast.expressions.UnaryNotExpression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.SimpleReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Block;
 import org.eclipse.dltk.ast.statements.ForStatement;
 import org.eclipse.dltk.ast.statements.IfStatement;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.ast.statements.UntilStatement;
 import org.eclipse.dltk.ast.statements.WhileStatement;
 import org.eclipse.dltk.ruby.ast.AliasExpression;
 import org.eclipse.dltk.ruby.ast.BacktickStringLiteral;
 import org.eclipse.dltk.ruby.ast.ColonExpression;
 import org.eclipse.dltk.ruby.ast.ConstantDeclaration;
 import org.eclipse.dltk.ruby.ast.DynamicBackquoteStringExpression;
 import org.eclipse.dltk.ruby.ast.DynamicStringExpression;
 import org.eclipse.dltk.ruby.ast.EvaluatableStringExpression;
 import org.eclipse.dltk.ruby.ast.HashExpression;
 import org.eclipse.dltk.ruby.ast.HashPairExpression;
 import org.eclipse.dltk.ruby.ast.IterableBlock;
 import org.eclipse.dltk.ruby.ast.RegexpExpression;
 import org.eclipse.dltk.ruby.ast.RescueBodyStatement;
 import org.eclipse.dltk.ruby.ast.RescueStatement;
 import org.eclipse.dltk.ruby.ast.RubyArrayExpression;
 import org.eclipse.dltk.ruby.ast.RubyBeginExpression;
 import org.eclipse.dltk.ruby.ast.RubyBlockPassExpression;
 import org.eclipse.dltk.ruby.ast.RubyBreakExpression;
 import org.eclipse.dltk.ruby.ast.RubyCaseStatement;
 import org.eclipse.dltk.ruby.ast.RubyDAssgnExpression;
 import org.eclipse.dltk.ruby.ast.RubyDRegexpExpression;
 import org.eclipse.dltk.ruby.ast.RubyDSymbolExpression;
 import org.eclipse.dltk.ruby.ast.RubyDVarExpression;
 import org.eclipse.dltk.ruby.ast.RubyDefinedExpression;
 import org.eclipse.dltk.ruby.ast.RubyDotExpression;
 import org.eclipse.dltk.ruby.ast.RubyEnsureExpression;
 import org.eclipse.dltk.ruby.ast.RubyMatch2Expression;
 import org.eclipse.dltk.ruby.ast.RubyMatch3Expression;
 import org.eclipse.dltk.ruby.ast.RubyMatchExpression;
 import org.eclipse.dltk.ruby.ast.RubyMethodArgument;
 import org.eclipse.dltk.ruby.ast.RubyReturnStatement;
 import org.eclipse.dltk.ruby.ast.RubySingletonClassDeclaration;
 import org.eclipse.dltk.ruby.ast.RubySingletonMethodDeclaration;
 import org.eclipse.dltk.ruby.ast.RubySuperExpression;
 import org.eclipse.dltk.ruby.ast.RubyVariableKind;
 import org.eclipse.dltk.ruby.ast.RubyWhenStatement;
 import org.eclipse.dltk.ruby.ast.SelfReference;
 import org.eclipse.dltk.ruby.ast.SymbolReference;
 import org.eclipse.dltk.ruby.core.RubyPlugin;
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 import org.eclipse.dltk.ruby.internal.core.RubyClassDeclaration;
 import org.eclipse.dltk.ruby.internal.parser.JRubySourceParser;
 import org.jruby.ast.AliasNode;
 import org.jruby.ast.AndNode;
 import org.jruby.ast.ArgsCatNode;
 import org.jruby.ast.ArgsNode;
 import org.jruby.ast.ArgsPushNode;
 import org.jruby.ast.ArgumentNode;
 import org.jruby.ast.ArrayNode;
 import org.jruby.ast.AttrAssignNode;
 import org.jruby.ast.BackRefNode;
 import org.jruby.ast.BeginNode;
 import org.jruby.ast.BignumNode;
 import org.jruby.ast.BlockArgNode;
 import org.jruby.ast.BlockNode;
 import org.jruby.ast.BlockPassNode;
 import org.jruby.ast.BreakNode;
 import org.jruby.ast.CallNode;
 import org.jruby.ast.CaseNode;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.ClassVarAsgnNode;
 import org.jruby.ast.ClassVarDeclNode;
 import org.jruby.ast.ClassVarNode;
 import org.jruby.ast.Colon2Node;
 import org.jruby.ast.Colon3Node;
 import org.jruby.ast.ConstDeclNode;
 import org.jruby.ast.ConstNode;
 import org.jruby.ast.DAsgnNode;
 import org.jruby.ast.DRegexpNode;
 import org.jruby.ast.DStrNode;
 import org.jruby.ast.DSymbolNode;
 import org.jruby.ast.DVarNode;
 import org.jruby.ast.DXStrNode;
 import org.jruby.ast.DefinedNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.DotNode;
 import org.jruby.ast.EnsureNode;
 import org.jruby.ast.EvStrNode;
 import org.jruby.ast.FCallNode;
 import org.jruby.ast.FalseNode;
 import org.jruby.ast.FixnumNode;
 import org.jruby.ast.FlipNode;
 import org.jruby.ast.FloatNode;
 import org.jruby.ast.ForNode;
 import org.jruby.ast.GlobalAsgnNode;
 import org.jruby.ast.GlobalVarNode;
 import org.jruby.ast.HashNode;
 import org.jruby.ast.IfNode;
 import org.jruby.ast.InstAsgnNode;
 import org.jruby.ast.InstVarNode;
 import org.jruby.ast.IterNode;
 import org.jruby.ast.ListNode;
 import org.jruby.ast.LocalAsgnNode;
 import org.jruby.ast.LocalVarNode;
 import org.jruby.ast.Match2Node;
 import org.jruby.ast.Match3Node;
 import org.jruby.ast.MatchNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.MultipleAsgnNode;
 import org.jruby.ast.NewlineNode;
 import org.jruby.ast.NextNode;
 import org.jruby.ast.NilNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.NotNode;
 import org.jruby.ast.NthRefNode;
 import org.jruby.ast.OpAsgnAndNode;
 import org.jruby.ast.OpAsgnNode;
 import org.jruby.ast.OpAsgnOrNode;
 import org.jruby.ast.OpElementAsgnNode;
 import org.jruby.ast.OptNNode;
 import org.jruby.ast.OrNode;
 import org.jruby.ast.PostExeNode;
 import org.jruby.ast.RedoNode;
 import org.jruby.ast.RegexpNode;
 import org.jruby.ast.RescueBodyNode;
 import org.jruby.ast.RescueNode;
 import org.jruby.ast.RetryNode;
 import org.jruby.ast.ReturnNode;
 import org.jruby.ast.RootNode;
 import org.jruby.ast.SClassNode;
 import org.jruby.ast.SValueNode;
 import org.jruby.ast.SelfNode;
 import org.jruby.ast.SplatNode;
 import org.jruby.ast.StrNode;
 import org.jruby.ast.SuperNode;
 import org.jruby.ast.SymbolNode;
 import org.jruby.ast.ToAryNode;
 import org.jruby.ast.TrueNode;
 import org.jruby.ast.UndefNode;
 import org.jruby.ast.UntilNode;
 import org.jruby.ast.VAliasNode;
 import org.jruby.ast.VCallNode;
 import org.jruby.ast.WhenNode;
 import org.jruby.ast.WhileNode;
 import org.jruby.ast.XStrNode;
 import org.jruby.ast.YieldNode;
 import org.jruby.ast.ZArrayNode;
 import org.jruby.ast.ZSuperNode;
 import org.jruby.ast.visitor.NodeVisitor;
 import org.jruby.evaluator.Instruction;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.lexer.yacc.SourcePosition;
 import org.jruby.runtime.Arity;
 import org.jruby.runtime.Visibility;
 
 /**
  * RubyASTBuildVisitor performs trasformation from JRuby's AST to DLTK AST.
  */
 public class RubyASTBuildVisitor implements NodeVisitor {
 
 	protected static final boolean TRACE_RECOVERING = Boolean
 			.valueOf(
 					Platform
 							.getDebugOption("org.eclipse.dltk.ruby.core/parsing/traceRecoveryWhenInterpretingAST"))
 			.booleanValue();
 
 	private ModuleDeclaration module;
 
 	private final char[] content;
 
 	protected static interface IState {
 		public void add(Statement statement);
 	}
 
 	protected static class CollectingState implements IState {
 		private final ArrayList list;
 
 		public CollectingState() {
 			list = new ArrayList();
 		}
 
 		public void add(Statement s) {
 			list.add(s);
 		}
 
 		public ArrayList getList() {
 			return list;
 		}
 
 		public void reset() {
 			list.clear();
 		}
 
 	}
 
 	protected static class ArgumentsState implements IState {
 		private final CallArgumentsList list;
 
 		public ArgumentsState(CallArgumentsList list) {
 			this.list = list;
 		}
 
 		public void add(Statement s) {
 			if (s instanceof Expression)
 				list.addExpression((Expression) s);
 			else {
 				if (!JRubySourceParser.isSilentState())
 					throw new RuntimeException(
 							"Adding statement into argument state is impossible");
 			}
 		}
 
 	}
 
 	protected static class TopLevelState implements IState {
 		private final ModuleDeclaration module;
 
 		public TopLevelState(ModuleDeclaration module) {
 			this.module = module;
 		}
 
 		public void add(Statement statement) {
 			module.getStatements().add(statement);
 		}
 	}
 
 	protected static abstract class ClassLikeState implements IState {
 		public int visibility;
 		public final TypeDeclaration type;
 
 		public ClassLikeState(TypeDeclaration type) {
 			this.type = type;
 			visibility = Modifiers.AccPublic;
 		}
 
 		public void add(Statement statement) {
 			type.getStatements().add(statement);
 		}
 
 	}
 
 	protected static class ClassState extends ClassLikeState {
 
 		public ClassState(TypeDeclaration type) {
 			super(type);
 		}
 
 	}
 
 	protected static class ModuleState extends ClassLikeState {
 
 		public ModuleState(TypeDeclaration type) {
 			super(type);
 		}
 
 	}
 
 	protected static class MethodState implements IState {
 
 		private final MethodDeclaration method;
 
 		public MethodState(MethodDeclaration method) {
 			this.method = method;
 		}
 
 		public void add(Statement statement) {
 			method.getStatements().add(statement);
 		}
 
 	}
 
 	protected static class BlockState implements IState {
 
 		public final Block block;
 
 		public BlockState(Block block) {
 			this.block = block;
 		}
 
 		public void add(Statement statement) {
 			block.getStatements().add(statement);
 		}
 
 	}
 	
 	private static class StateManager
 	{
 		private LinkedList states = new LinkedList();
 		
 		public IState peek() {			
 			return (IState)states.getLast();
 		}
 		
 		public void pop() {
 			states.removeLast();				
 		}
 		
 		public void push(IState state) {
 			states.add(state);
 		}	
 	}
 	
 	private StateManager states = new StateManager();
 
 
 	protected Statement collectSingleStatementSafe(Node pathNode) {
 		return collectSingleStatementSafe(pathNode, false);
 	}
 
 	/**
 	 * Safe wrapper for <code>collectSingleStatement0</code>. It checks
 	 * SILENT_MODE flag, and it is true, just performs
 	 * <code>node.accept(this);</code> without throwing an exception.
 	 * 
 	 * @param node
 	 * @param allowZero
 	 * @return
 	 */
 	protected Statement collectSingleStatementSafe(Node node, boolean allowZero) {
 		Statement res = null;
 		try {
 			res = collectSingleStatement0(node, allowZero);
 		} catch (Throwable t) {
 			if (JRubySourceParser.isSilentState()) {
 				node.accept(this);
 			} else {
 				throw new RuntimeException(t);
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Tries to convert single JRuby's node to single DLTK AST node. If
 	 * convertion fails, and for ex., more than one or zero nodes were fetched
 	 * as result, then RuntimeException will be thrown. Optoin
 	 * <code>allowZero</code> allows to fetch no dltk nodes and just return
 	 * null without throwing an exception.
 	 * 
 	 * @param node
 	 * @param allowZero
 	 * @return
 	 */
 	protected Statement collectSingleStatement0(Node node, boolean allowZero) {
 		if (node == null)
 			return null;
 		CollectingState state = new CollectingState();
 		states.push(state);
 		node.accept(this);
 		states.pop();
 
 		ArrayList list = state.getList();
 		if (list.size() == 1)
 			return (Statement) list.iterator().next();
 
 		if (node instanceof NewlineNode) {
 			NewlineNode newlineNode = (NewlineNode) node;
 			node = newlineNode.getNextNode();
 		}
 		if (list.size() > 1) {
 			throw new RuntimeException(
 					"DLTKASTBuildVisitor.collectSingleStatement(): JRuby node "
 							+ node.getClass().getName()
 							+ " hasn't been converted into any DLTK AST node");
 		}
 		if (allowZero)
 			return null;
 		throw new RuntimeException(
 				"DLTKASTBuildVisitor.collectSingleStatement(): JRuby node "
 						+ node.getClass().getName()
 						+ " hasn't been converted into any DLTK AST node");
 	}
 
 	protected char[] getContent() {
 		return content;
 	}
 
 	public RubyASTBuildVisitor(ModuleDeclaration module, char[] content) {
 		this.module = module;
 		this.content = content;
 		states.push(new TopLevelState(this.module));
 	}
 
 	public Instruction visitAliasNode(AliasNode iVisited) { // done
 		String oldName = iVisited.getOldName();
 		String newName = iVisited.getNewName();
 		ISourcePosition pos = iVisited.getPosition();
 		AliasExpression expr = new AliasExpression(pos.getStartOffset(), pos
 				.getEndOffset(), oldName, newName);
 		states.peek().add(expr);
 		return null;
 	}
 
 	public Instruction visitAndNode(AndNode iVisited) { // done
 		Expression left = (Expression) collectSingleStatementSafe(iVisited
 				.getFirstNode());
 		Expression right = (Expression) collectSingleStatementSafe(iVisited
 				.getSecondNode());
 		BinaryExpression b = new BinaryExpression(left, Expression.E_BAND,
 				right);
 		states.peek().add(b);
 		return null;
 	}
 
 	// should never get here
 	public Instruction visitArgsNode(ArgsNode iVisited) {
 		if (iVisited.getOptArgs() != null) {
 			iVisited.getOptArgs().accept(this);
 		}
 		return null;
 	}
 
 	// should never get here
 	public Instruction visitArgsCatNode(ArgsCatNode iVisited) {
 		if (iVisited.getFirstNode() != null) {
 			iVisited.getFirstNode().accept(this);
 		}
 		if (iVisited.getSecondNode() != null) {
 			iVisited.getSecondNode().accept(this);
 		}
 		return null;
 	}
 
 	private List processListNode(ListNode node) { // done
 		CollectingState coll = new CollectingState();
 
 		states.push(coll);
 		Iterator iterator = node.iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		states.pop();
 
 		return coll.getList();
 	}
 
 	public Instruction visitArrayNode(ArrayNode iVisited) { // done
 		List exprs = processListNode(iVisited);
 
 		ISourcePosition position = iVisited.getPosition();
 		RubyArrayExpression arr = new RubyArrayExpression();
 		arr.setEnd(position.getEndOffset());
 		arr.setStart(position.getStartOffset());
 		arr.setExpresssions(exprs); // XXX cast exception cause
 		states.peek().add(arr);
 
 		return null;
 	}
 
 	public Instruction visitBackRefNode(BackRefNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		VariableReference ref = new VariableReference(pos.getStartOffset(), pos
 				.getEndOffset(), "$" + iVisited.getType());
 		states.peek().add(ref);
 		return null;
 	}
 
 	public Instruction visitBeginNode(BeginNode iVisited) { // done
 		Statement body = collectSingleStatementSafe(iVisited.getBodyNode());
 		ISourcePosition pos = iVisited.getPosition();
 		RubyBeginExpression e = new RubyBeginExpression(pos.getStartOffset(),
 				pos.getEndOffset(), body);
 		states.peek().add(e);
 		return null;
 	}
 
 	// should never get here
 	public Instruction visitBlockArgNode(BlockArgNode iVisited) {
 		return null;
 	}
 
 	public Instruction visitBlockNode(BlockNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		Block block = new Block(pos.getStartOffset(), pos.getEndOffset());
 		states.push(new BlockState(block));
 		Iterator iterator = iVisited.iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		states.pop();
 		states.peek().add(block);
 		return null;
 	}
 
 	public Instruction visitBlockPassNode(BlockPassNode iVisited) { // done
 		Statement args = collectSingleStatementSafe(iVisited.getArgsNode());
 		Statement body = collectSingleStatementSafe(iVisited.getBodyNode());
 		ISourcePosition pos = iVisited.getPosition();
 		RubyBlockPassExpression e = new RubyBlockPassExpression(pos
 				.getStartOffset(), pos.getEndOffset(), args, body);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitBreakNode(BreakNode iVisited) { // done
 		Statement value = collectSingleStatementSafe(iVisited.getValueNode());
 		ISourcePosition pos = iVisited.getPosition();
 		RubyBreakExpression e = new RubyBreakExpression(pos.getStartOffset(),
 				pos.getEndOffset(), value);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitConstDeclNode(ConstDeclNode iVisited) {
 		Node pathNode = iVisited.getConstNode();
 		Expression pathResult = null;
 		if (pathNode != null)
 			pathResult = (Expression) collectSingleStatementSafe(pathNode);
 		Statement value = collectSingleStatementSafe(iVisited.getValueNode());
 		ISourcePosition position = iVisited.getPosition();
 		int start = position.getStartOffset();
 		int end = start;
 		while (RubySyntaxUtils.isWhitespace(content[end])) {
 			end++;
 		}
 		while (RubySyntaxUtils.isNameChar(content[end])) {
 			end++;
 		}
 		SimpleReference name = new SimpleReference(start, end, iVisited
 				.getName());
 		ConstantDeclaration node = new ConstantDeclaration(pathResult, name,
 				value);
 		states.peek().add(node);
 		return null;
 	}
 
 	public Instruction visitClassVarAsgnNode(ClassVarAsgnNode iVisited) {
 		processVariableAssignment(iVisited, iVisited.getName(),
 				RubyVariableKind.CLASS, iVisited.getValueNode());
 		return null;
 	}
 
 	public Instruction visitClassVarDeclNode(ClassVarDeclNode iVisited) {
 		processVariableAssignment(iVisited, iVisited.getName(),
 				RubyVariableKind.CLASS, iVisited.getValueNode());
 		return null;
 	}
 
 	public Instruction visitClassVarNode(ClassVarNode iVisited) {
 		processVariableReference(iVisited, iVisited.getName(),
 				RubyVariableKind.CLASS);
 		return null;
 	}
 
 	private void fixCallOffsets(CallExpression callNode, String nameNode,
 			int possibleDotPosition, int firstArgStart, int lastArgEnd) {
 		int dotPosition = RubySyntaxUtils.skipWhitespaceForward(content,
 				possibleDotPosition);
 		if (dotPosition >= 0 && dotPosition < content.length
 				&& content[dotPosition] == '.') {
 			fixFunctionCallOffsets(callNode, nameNode, dotPosition + 1,
 					firstArgStart, lastArgEnd);
 			return;
 		}
 		String methodName = nameNode;
 		if (methodName == RubySyntaxUtils.ARRAY_GET_METHOD) {
 			// TODO
 		} else if (methodName == RubySyntaxUtils.ARRAY_PUT_METHOD) {
 			// TODO
 		} else {
 			// WTF?
 			if (TRACE_RECOVERING)
 				RubyPlugin
 						.log("Ruby AST: non-dot-call not recognized, non-dot found at "
 								+ dotPosition + ", function name " + methodName);
 		}
 	}
 
 	private void fixFunctionCallOffsets(CallExpression callNode,
 			String nameNode, int possibleNameStart, int firstArgStart,
 			int lastArgEnd) {
 		String methodName = nameNode;
 		int nameStart = RubySyntaxUtils.skipWhitespaceForward(content,
 				possibleNameStart);
 		int nameEnd = nameStart + methodName.length();
 
 		// Assert.isLegal(nameSequence.toString().equals(methodName)); //XXX
 		// nameNode.setStart(nameStart);
 		// nameNode.setEnd(nameEnd);
 
 		if (firstArgStart < 0) {
 			int lParenOffset = RubySyntaxUtils.skipWhitespaceForward(content,
 					nameEnd);
 			if (lParenOffset >= 0 && content[lParenOffset] == '(') {
 				int rParenOffset = RubySyntaxUtils.skipWhitespaceForward(
 						content, lParenOffset + 1);
 				if (rParenOffset >= 0 && content[rParenOffset] == ')')
 					callNode.setEnd(rParenOffset + 1);
 				else {
 					if (TRACE_RECOVERING)
 						RubyPlugin
 								.log("Ruby AST: function call, empty args, no closing paren; "
 										+ "opening paren at "
 										+ lParenOffset
 										+ ", function name " + methodName);
 					callNode.setEnd(lParenOffset - 1); // don't include these
 					// parens
 				}
 			}
 		} else {
 			if (nameEnd > firstArgStart) {
 				if (TRACE_RECOVERING)
 					RubyPlugin
 							.log("DLTKASTBuildVisitor.fixFunctionCallOffsets("
 									+ methodName + "): nameEnd > firstArgStart");
 				return; // /XXX: it's a kind of magic, please, FIXME!!!
 			}
 			int lParenOffset = RubySyntaxUtils.skipWhitespaceForward(content,
 					nameEnd, firstArgStart);
 			if (lParenOffset >= 0 && content[lParenOffset] == '(') {
 				if (lastArgEnd <= lParenOffset) {
 					if (TRACE_RECOVERING)
 						RubyPlugin
 								.log("DLTKASTBuildVisitor.fixFunctionCallOffsets("
 										+ methodName
 										+ "): lastArgEnd <= lParenOffset");
 					return;
 				}
 				int rParenOffset = RubySyntaxUtils.skipWhitespaceForward(
 						content, lastArgEnd);
 				if (rParenOffset >= 0 && content[rParenOffset] == ')')
 					callNode.setEnd(rParenOffset + 1);
 				else {
 					if (TRACE_RECOVERING)
 						RubyPlugin
 								.log("Ruby AST: function call, non-empty args, no closing paren; "
 										+ "opening paren at "
 										+ lParenOffset
 										+ ", "
 										+ "last argument ending at "
 										+ lastArgEnd
 										+ ", function name "
 										+ methodName);
 					callNode.setEnd(lastArgEnd); // probably no closing paren
 				}
 			}
 		}
 
 		if (lastArgEnd >= 0 && callNode.sourceEnd() < lastArgEnd)
 			callNode.setEnd(lastArgEnd);
 	}
 
 	/**
 	 * @fixme iteration not correctly defined
 	 */
 	public Instruction visitCallNode(CallNode iVisited) {
 		String methodName = iVisited.getName();
 		CollectingState collector = new CollectingState();
 
 		Assert.isTrue(iVisited.getReceiverNode() != null);
 		states.push(collector);
 		iVisited.getReceiverNode().accept(this);
 		states.pop();
 		// TODO: uncomment when visitor is done
 		if (collector.getList().size() > 1) {
 			if (TRACE_RECOVERING)
 				RubyPlugin.log("DLTKASTBuildVisitor.visitCallNode("
 						+ methodName + "): receiver "
 						+ iVisited.getReceiverNode().getClass().getName()
 						+ " turned into multiple nodes");
 		}
 		Statement recv;
 		if (collector.getList().size() < 1) {
 			recv = new NumericLiteral(new DLTKToken(0, "")); // FIXME
 			recv.setStart(iVisited.getPosition().getStartOffset());
 			recv.setEnd(iVisited.getPosition().getEndOffset() + 1);
 		} else
 			recv = (Statement) collector.getList().get(0);
 
 		collector.reset();
 
 		int argsStart = -1, argsEnd = -1;
 		CallArgumentsList argList = new CallArgumentsList();
 		Node argsNode = iVisited.getArgsNode();
 		if (argsNode != null) {
 			states.push(new ArgumentsState(argList));
 			if (argsNode instanceof ListNode) {
 				ListNode arrayNode = (ListNode) argsNode;
 				List list = arrayNode.childNodes();
 				for (Iterator iter = list.iterator(); iter.hasNext();) {
 					Node node = (Node) iter.next();
 					node.accept(this);
 				}
 			} else {
 				if (TRACE_RECOVERING)
 					RubyPlugin.log("DLTKASTBuildVisitor.visitCallNode("
 							+ methodName + ") - unknown args node type: "
 							+ argsNode.getClass().getName());
 				argsNode.accept(this);
 			}
 			states.pop();
 			List children = argsNode.childNodes();
 			if (children.size() > 0) {
 				argsStart = ((Node) children.get(0)).getPosition()
 						.getStartOffset();
 				argsEnd = ((Node) children.get(children.size() - 1))
 						.getPosition().getEndOffset() + 1;
 				// correction for nodes with incorrect positions
 				List argListExprs = argList.getExpressions();
 				if (!argListExprs.isEmpty())
 					argsEnd = Math.max(argsEnd, ((ASTNode) argListExprs
 							.get(argListExprs.size() - 1)).sourceEnd());
 			}
 		}
 		if (iVisited.getIterNode() != null) {
 			Statement s = collectSingleStatementSafe(iVisited.getIterNode());
 			if (s instanceof Expression)
 				argList.addExpression((Expression) s);
 			else
 				System.err.println("Failed to get block argument");
 		}
 
 		CallExpression c = new CallExpression(recv, methodName, argList);
 		int receiverStart = recv.sourceStart();
 		int receiverEnd = recv.sourceEnd();
 
 		// FIXME handle whitespace and special functions like []= (which are
 		// called without dot)
 		int funcNameStart = receiverEnd + 1 /* skip dot */;
 		int funcNameEnd = funcNameStart + methodName.length();
 		// Assert.isTrue(iVisited.getPosition().getStartOffset() ==
 		// receiverStart);
 
 		c.setStart(receiverStart);
 		c.setEnd(argsEnd >= 0 ? argsEnd : funcNameEnd); // just in case, should
 		// be overriden
 		fixCallOffsets(c, methodName, receiverEnd, argsStart, argsEnd);
 
 		this.states.peek().add(c);
 
 		return null;
 	}
 
 	public Instruction visitCaseNode(CaseNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		RubyCaseStatement statement = new RubyCaseStatement(pos
 				.getStartOffset(), pos.getEndOffset());
 		Statement caseTarget = collectSingleStatementSafe(iVisited
 				.getCaseNode());
 		statement.setTarget(caseTarget);
 		Node caseBody = iVisited.getFirstWhenNode();
 		Statement caseSt = collectSingleStatementSafe(caseBody);
 		List whens = new ArrayList(1);
 		while (caseBody instanceof WhenNode) {
 			WhenNode whenNode = (WhenNode) caseBody;
 			whens.add(caseSt);
 			caseBody = whenNode.getNextCase();
 			caseSt = collectSingleStatementSafe(caseBody);
 		}
 		statement.setWhens(whens);
 		if (caseSt != null) {
 			statement.setElseWhen(caseSt);
 		}
 		states.peek().add(statement);
 		return null;
 	}
 
 	private static String colons2Name(Node cpathNode) {
 		String name = "";
 		while (cpathNode instanceof Colon2Node) {
 			Colon2Node colon2Node = (Colon2Node) cpathNode;
 			if (name.length() > 0)
 				name = "::" + name;
 			name = colon2Node.getName() + name;
 			cpathNode = colon2Node.getLeftNode();
 		}
 		if (cpathNode instanceof Colon3Node) {
 			Colon3Node colon3Node = (Colon3Node) cpathNode;
 			if (name.length() > 0)
 				name = "::" + name;
 			name = "::" + colon3Node.getName() + name;
 		} else if (cpathNode instanceof ConstNode) {
 			ConstNode constNode = (ConstNode) cpathNode;
 			if (name.length() > 0)
 				name = "::" + name;
 			name = constNode.getName() + name;
 		}
 		return name;
 	}
 
 	private ISourcePosition fixNamePosition(ISourcePosition pos) {
 		int start = pos.getStartOffset();
 		int end = pos.getEndOffset();
 
 		while (end - 1 >= 0 && (end - 1) > start
 				&& !RubySyntaxUtils.isNameChar(content[end - 1])) {
 			end--;
 		}
 		if (end >= 0) {
 			while (end < content.length
 					&& RubySyntaxUtils.isNameChar(content[end]))
 				end++;
 		}
 		return new SourcePosition(pos.getFile(), pos.getStartLine(), pos
 				.getEndLine(), start, end);
 	}
 
 	private ISourcePosition fixBorders(ISourcePosition pos) {
 		int start = pos.getStartOffset();
 		int end = pos.getEndOffset();
 		while (end - 1 >= 0 && !RubySyntaxUtils.isNameChar(content[end - 1])) {
 			end--;
 		}
 		if (end >= 0) {
 			while (end < content.length
 					&& RubySyntaxUtils.isNameChar(content[end]))
 				end++;
 		}
 		return new SourcePosition(pos.getFile(), pos.getStartLine(), pos
 				.getEndLine(), start, end);
 	}
 
 	public Instruction visitClassNode(ClassNode iVisited) {
 		Node cpathNode = iVisited.getCPath();
 		Node superClassNode = iVisited.getSuperNode();
 		Statement cpath = collectSingleStatementSafe(cpathNode);
 		Statement supernode = collectSingleStatementSafe(superClassNode);
 		ISourcePosition pos = iVisited.getCPath().getPosition();
 		ISourcePosition cPos = iVisited.getPosition();
 		cPos = fixNamePosition(cPos);
 		pos = fixNamePosition(pos);
 
 		RubyClassDeclaration type = new RubyClassDeclaration((Expression)supernode, 
 				cpath, null, cPos.getStartOffset(), cPos.getEndOffset());
 		
 		String name = String.copyValueOf(content, pos.getStartOffset(), pos.getEndOffset() - pos.getStartOffset());
 		type.setName(name);
 		
 		states.peek().add(type);
 		states.push(new ClassState(type));
 		// body
 		if (iVisited.getBodyNode() != null) {
 			pos = iVisited.getBodyNode().getPosition();
 			Node bodyNode = iVisited.getBodyNode();
 			int end = -1;
 			while (bodyNode instanceof NewlineNode)
 				bodyNode = ((NewlineNode) bodyNode).getNextNode();
 			if (bodyNode instanceof BlockNode) {
 				BlockNode blockNode = (BlockNode) bodyNode;
 				end = blockNode.getLast().getPosition().getEndOffset() + 1; // /XXX!!!!
 			} else {
 				if (TRACE_RECOVERING)
 					RubyPlugin.log("DLTKASTBuildVisitor.visitClassNode(" 
 							+ "): unknown body type "
 							+ bodyNode.getClass().getName());
 			}
 			pos = fixBorders(pos);
 			Block bl = new Block(pos.getStartOffset(), (end == -1) ? pos
 					.getEndOffset() + 1 : end);
 			type.setBody(bl);
 			iVisited.getBodyNode().accept(this);
 		}
 		
 		states.pop();
 		return null;
 	}
 
 	public Instruction visitColon2Node(Colon2Node iVisited) {
 
 		CollectingState collector = new CollectingState();
 		states.push(collector);
 		if (iVisited.getLeftNode() != null) {
 			iVisited.getLeftNode().accept(this);
 		}
 		states.pop();
 
 		int start = iVisited.getPosition().getStartOffset();
 		int end = iVisited.getPosition().getEndOffset();
 
 		Expression left = null;
 		if (collector.list.size() == 1) {
 			if (collector.list.get(0) instanceof Expression) {
 				left = (Expression) collector.list.get(0);
 			}
 		}
 
 		String right = iVisited.getName();
 
 		if (left != null) {
 			ColonExpression colon = new ColonExpression(right, left);
 			colon.setStart(start);
 			colon.setEnd(end);
 			states.peek().add(colon);
  		} else {
  			ConstantReference ref = new ConstantReference(start, end, right);
  			states.peek().add(ref);
  		}
 
 		return null;
 	}
 
 	public Instruction visitColon3Node(Colon3Node iVisited) {
 		ISourcePosition position = iVisited.getPosition();
 		ColonExpression colon = new ColonExpression(iVisited.getName(), null);
 		colon.setStart(position.getStartOffset());
 		colon.setEnd(position.getEndOffset());
 		states.peek().add(colon);
 		return null;
 	}
 
 	public Instruction visitConstNode(ConstNode iVisited) {
 		String name = iVisited.getName();
 		ISourcePosition pos = iVisited.getPosition();
 		pos = fixBorders(pos);
 		this.states.peek().add(
 				new ConstantReference(pos.getStartOffset(), pos.getEndOffset(),
 						name));
 		return null;
 	}
 
 	public Instruction visitDAsgnNode(DAsgnNode iVisited) { // FIXME, just a
 															// stub
 		ISourcePosition pos = iVisited.getPosition();
 		RubyDAssgnExpression e = new RubyDAssgnExpression(pos.getStartOffset(),
 				pos.getEndOffset());
 		states.peek().add(e);
 		// Iterator iterator = iVisited.childNodes().iterator();
 		// while (iterator.hasNext()) {
 		// ((Node) iterator.next()).accept(this);
 		// }
 		return null;
 	}
 
 	public Instruction visitDRegxNode(DRegexpNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		List list = processListNode(iVisited);
 		RubyDRegexpExpression ex = new RubyDRegexpExpression(pos
 				.getStartOffset(), pos.getEndOffset());
 		ex.setExpresssions(list);
 		states.peek().add(ex);
 		return null;
 	}
 
 	public Instruction visitDStrNode(DStrNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		List list = processListNode(iVisited);
 		DynamicStringExpression ex = new DynamicStringExpression(pos
 				.getStartOffset(), pos.getEndOffset());
 		ex.setExpresssions(list);
 		states.peek().add(ex);
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitDSymbolNode(DSymbolNode)
 	 */
 	public Instruction visitDSymbolNode(DSymbolNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		List list = processListNode(iVisited);
 		RubyDSymbolExpression ex = new RubyDSymbolExpression(pos
 				.getStartOffset(), pos.getEndOffset());
 		ex.setExpresssions(list);
 		states.peek().add(ex);
 		return null;
 	}
 
 	public Instruction visitDVarNode(DVarNode iVisited) { // done (?)
 		String name = iVisited.getName();
 		ISourcePosition pos = iVisited.getPosition();
 		RubyDVarExpression e = new RubyDVarExpression(pos.getStartOffset(), pos
 				.getEndOffset(), name);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitDXStrNode(DXStrNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		List list = processListNode(iVisited);
 		DynamicBackquoteStringExpression ex = new DynamicBackquoteStringExpression(
 				pos.getStartOffset(), pos.getEndOffset());
 		ex.setExpresssions(list);
 		states.peek().add(ex);
 		return null;
 	}
 
 	public Instruction visitDefinedNode(DefinedNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		Statement value = collectSingleStatementSafe(iVisited
 				.getExpressionNode());
 		RubyDefinedExpression e = new RubyDefinedExpression(pos
 				.getStartOffset(), pos.getEndOffset(), value);
 		states.peek().add(e);
 		return null;
 	}
 
 	private List processMethodArguments(ArgsNode args) {
 		List arguments = new ArrayList();
 		Arity arity = args.getArity();
 		int endPos = args.getPosition().getStartOffset() - 1;
 		if (arity.getValue() != 0) { // BIG XXX, PLEASE CHECK IT
 			ListNode argsList = args.getArgs();
 			if (argsList != null) {
 				Iterator i = argsList.iterator();
 				while (i.hasNext()) {
 					Node nde = (Node) i.next();
 					if (nde instanceof ArgumentNode) {
 						ArgumentNode a = (ArgumentNode) nde;
 						Argument aa = new RubyMethodArgument();
 						ISourcePosition argPos = fixNamePosition(a
 								.getPosition());
 
 						if (argPos.getEndOffset() > endPos)
 							endPos = argPos.getEndOffset();
 
 						aa.set(new SimpleReference(argPos.getStartOffset(),
 								argPos.getEndOffset(), a.getName()), null);
 						aa.setModifier(RubyMethodArgument.SIMPLE);
 						arguments.add(aa);
 					}
 				}
 			}
 			ListNode optArgs = args.getOptArgs();
 			if (optArgs != null) {
 				Iterator iterator = optArgs.iterator();
 				while (iterator.hasNext()) {
 					Object obj = iterator.next();
 					if (obj instanceof LocalAsgnNode) {
 						LocalAsgnNode a = (LocalAsgnNode) obj;
 						Argument aa = new RubyMethodArgument();
 						ISourcePosition argPos = a.getPosition();
 
 						if (argPos.getEndOffset() > endPos)
 							endPos = argPos.getEndOffset();
 
 						CollectingState coll = new CollectingState();
 						states.push(coll);
 						a.getValueNode().accept(this);
 						states.pop();
 
 						Expression defaultVal = null;
 
 						if (coll.list.size() == 1) {
 							Object object = coll.list.get(0);
 							if (object instanceof Expression)
 								defaultVal = (Expression) object;
 						}
 
 						aa
 								.set(new SimpleReference(argPos
 										.getStartOffset(), argPos
 										.getEndOffset(), a.getName()),
 										defaultVal);
 						aa.setModifier(RubyMethodArgument.SIMPLE);
 						arguments.add(aa);
 					} else {
 						System.err.println("unknown argument type!");
 					}
 				}
 			}
 		}
 		if (args.getRestArg() >= 0) {
 
 			// restore vararg name and position
 			int vaStart = 0, vaEnd = 0;
 			IState s = states.peek();
 			if (s instanceof MethodState) {
 				MethodState ms = (MethodState) s;
 				// int bodyStart = ms.method.getBody().sourceStart();
 				int bodyStart = args.getPosition().getEndOffset();
 				if (endPos >= 0 && endPos < bodyStart) { // this assumes that
 					// sourceStart is always set and always less than
 					// contents.length()
 					while (endPos < bodyStart && content[endPos] != '*')
 						endPos++;
 					endPos++;
 					if (endPos < content.length) {
 						vaStart = endPos - 1;
 						while (RubySyntaxUtils
 								.isIdentifierCharacter(content[endPos]))
 							endPos++;
 						vaEnd = endPos;
 					}
 
 				}
 			}
 
 			Argument aa = new RubyMethodArgument();
 			aa.set(new SimpleReference(vaStart + 1, vaEnd, String.copyValueOf(content, 
 					vaStart, vaEnd - vaStart )), null);
 			aa.setModifier(RubyMethodArgument.VARARG);
 			arguments.add(aa);
 		}
 		BlockArgNode blockArgNode = args.getBlockArgNode();
 		if (blockArgNode != null) {
 			ISourcePosition position = fixNamePosition(blockArgNode
 					.getPosition());
 			String baName = String.copyValueOf( content, position.getStartOffset() - 1,
 					position.getEndOffset() - (position.getStartOffset() - 1));
 			Argument aa = new RubyMethodArgument();
 			aa.set(new SimpleReference(position.getStartOffset(), position
 					.getEndOffset(), baName), null); // XXX:
 			aa.setModifier(RubyMethodArgument.BLOCK);
 			arguments.add(aa);
 		}
 		return arguments;
 	}
 
 	private void setMethodVisibility(MethodDeclaration method,
 			Visibility visibility) {
 		if (visibility.isPrivate())
 			ASTUtils.setVisibility(method, Modifiers.AccPrivate);
 
 		if (visibility.isPublic())
 			ASTUtils.setVisibility(method, Modifiers.AccPublic);
 
 		if (visibility.isProtected())
 			ASTUtils.setVisibility(method, Modifiers.AccProtected);
 	}
 
 	// method
 	public Instruction visitDefnNode(DefnNode iVisited) {
 		ArgumentNode nameNode = iVisited.getNameNode();
 
 		ISourcePosition pos = fixNamePosition(nameNode.getPosition());
 		ISourcePosition cPos = fixNamePosition(iVisited.getPosition());
 		MethodDeclaration method = new MethodDeclaration(iVisited.getName(),
 				pos.getStartOffset(), pos.getEndOffset(),
 				cPos.getStartOffset(), cPos.getEndOffset());
 
 		setMethodVisibility(method, iVisited.getVisibility());
 		if (states.peek() instanceof ClassLikeState) {
 			ClassLikeState classState = (ClassLikeState) states.peek();
 			ASTUtils.setVisibility(method, classState.visibility);
 		}
 
 		states.peek().add(method);
 		states.push(new MethodState(method));
 		Node bodyNode = iVisited.getBodyNode();
 		if (bodyNode != null) {
 			ISourcePosition bodyPos = bodyNode.getPosition();
 			method.getBody().setStart(bodyPos.getStartOffset());
 			method.getBody().setEnd(bodyPos.getEndOffset());
 			bodyNode.accept(this);
 		}
 		ArgsNode args = (ArgsNode) iVisited.getArgsNode();
 		if (args != null) {
 			List arguments = processMethodArguments(args);
 			method.acceptArguments(arguments);
 		}
 		states.pop();
 		return null;
 	}
 
 	private ISourcePosition restoreMethodNamePosition(DefsNode node, int recvEnd) {
 		ISourcePosition recvPos = node.getReceiverNode().getPosition();
 		int pos = recvEnd;
 		if (pos >= 0) {
 			while (pos < content.length && content[pos] != '.'
 					&& content[pos] != ':')
 				pos++;
 			if (content[pos] == ':')
 				pos++;
 		}
 		if (pos >= content.length || pos < 0)
 			return recvPos;
 		int nameStart = RubySyntaxUtils.skipWhitespaceForward(content, pos + 1);
 		int nameEnd = nameStart;
 		while (RubySyntaxUtils.isIdentifierCharacter(content[nameEnd]))
 			nameEnd++;
 		return new SourcePosition(recvPos.getFile(), recvPos.getStartLine(),
 				recvPos.getEndLine(), nameStart, nameEnd);
 	}
 
 	// singleton method
 	public Instruction visitDefsNode(DefsNode iVisited) {
 		Expression receiverExpression = null;
 		Node receiverNode = iVisited.getReceiverNode();
 		CollectingState collectingState = new CollectingState();
 
 		states.push(collectingState);
 		receiverNode.accept(this);
 		states.pop();
 		if (collectingState.list.size() == 1) {
 			Object obj = collectingState.list.get(0);
 			if (obj instanceof Expression) {
 				receiverExpression = (Expression) obj;
 			}
 		}
 
 		ISourcePosition cPos = iVisited.getPosition();
 		// if (receiverExpression == null)
 		// System.out.println();
 		ISourcePosition namePos = restoreMethodNamePosition(iVisited,
 				receiverExpression.sourceEnd());
 		String name = iVisited.getName();
 //		if (receiverNode instanceof SelfNode) {
 //			name = "self." + name;
 //		} else if (receiverNode instanceof ConstNode) {
 //			name = ((ConstNode) receiverNode).getName() + "." + name;
 //		}
 		RubySingletonMethodDeclaration method = new RubySingletonMethodDeclaration(
 				name, namePos.getStartOffset(), namePos.getEndOffset(), cPos
 						.getStartOffset(), cPos.getEndOffset(),
 				receiverExpression);
 		method.setModifier(Modifiers.AccStatic);
 		ASTUtils.setVisibility(method, Modifiers.AccPublic);
 		if (states.peek() instanceof ClassLikeState) {
 			ClassLikeState classState = (ClassLikeState) states.peek();
 			ASTUtils.setVisibility(method, classState.visibility);
 		}
 		states.peek().add(method);
 		states.push(new MethodState(method));
 		ArgsNode args = (ArgsNode) iVisited.getArgsNode();
 		if (args != null) {
 			List list = processMethodArguments(args);
 			method.acceptArguments(list);
 		}
 		if (iVisited.getBodyNode() != null)
 			iVisited.getBodyNode().accept(this);
 		states.pop();
 		return null;
 	}
 
 	public Instruction visitDotNode(DotNode iVisited) { // done
 		Statement begin = collectSingleStatementSafe(iVisited.getBeginNode());
 		Statement end = collectSingleStatementSafe(iVisited.getEndNode());
 		ISourcePosition pos = iVisited.getPosition();
 		RubyDotExpression e = new RubyDotExpression(pos.getStartOffset(), pos
 				.getEndOffset(), begin, end);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitEnsureNode(EnsureNode iVisited) { // done
 		Statement body = collectSingleStatementSafe(iVisited.getBodyNode());
 		Statement ensure = collectSingleStatementSafe(iVisited.getEnsureNode());
 		ISourcePosition pos = iVisited.getPosition();
 		RubyEnsureExpression e = new RubyEnsureExpression(pos.getStartOffset(),
 				pos.getEndOffset(), ensure, body);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitEvStrNode(EvStrNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		Statement body = collectSingleStatementSafe(iVisited.getBody());
 		EvaluatableStringExpression e = new EvaluatableStringExpression(pos
 				.getStartOffset(), pos.getEndOffset(), body);
 		states.peek().add(e);
 		return null;
 	}
 
 	/** @fixme iteration not correctly defined */
 	public Instruction visitFCallNode(FCallNode iVisited) {
 		// System.out.println("DLTKASTBuildVisitor.visitFCallNode(" +
 		// iVisited.getName() + ")");
 		String methodName = iVisited.getName();
 
 		// System.out.println("== (AST) Method name: " + methodName);
 
 		IState state = states.peek();
 		if (state instanceof ClassLikeState) {
 			if (methodName.equals("private"))
 				handleVisibilitySetter(iVisited, Modifiers.AccPrivate);
 			else if (methodName.equals("protected"))
 				handleVisibilitySetter(iVisited, Modifiers.AccProtected);
 			else if (methodName.equals("public"))
 				handleVisibilitySetter(iVisited, Modifiers.AccPublic);
 		}
 
 		int argsStart = -1, argsEnd = -1;
 		CallArgumentsList argList = new CallArgumentsList();
 		Node argsNode = iVisited.getArgsNode();
 		if (argsNode != null) {
 			states.push(new ArgumentsState(argList));
 			if (argsNode instanceof ListNode) {
 				ListNode arrayNode = (ListNode) argsNode;
 				List list = arrayNode.childNodes();
 				for (Iterator iter = list.iterator(); iter.hasNext();) {
 					Node node = (Node) iter.next();
 					node.accept(this);
 				}
 			} else {
 				if (TRACE_RECOVERING)
 					RubyPlugin.log("DLTKASTBuildVisitor.visitFCallNode("
 							+ methodName + ") - unknown args node type: "
 							+ argsNode.getClass().getName());
 				argsNode.accept(this);
 			}
 			states.pop();
 			List children = argsNode.childNodes();
 			if (children.size() > 0) {
 				argsStart = ((Node) children.get(0)).getPosition()
 						.getStartOffset();
 				argsEnd = ((Node) children.get(children.size() - 1))
 						.getPosition().getEndOffset() + 1;
 			}
 		}
 		
 		if (iVisited.getIterNode() != null) {
 			Statement s = collectSingleStatementSafe(iVisited.getIterNode());
 			if (s instanceof Expression)
 				argList.addExpression((Expression) s);
 			else
 				System.err.println("Failed to get block argument");
 		}
 
 		CallExpression c = new CallExpression(null, methodName, argList);
 
 		int funcNameStart = iVisited.getPosition().getStartOffset();
 		c.setStart(funcNameStart);
 		fixFunctionCallOffsets(c, methodName, funcNameStart, argsStart, argsEnd);
 
 		states.peek().add(c);
 
 		return null;
 	}
 
 	private void handleVisibilitySetter(FCallNode node, int newVisibility) {
 		IState state = states.peek();
 		if (state instanceof ClassLikeState) {
 			ClassLikeState classState = (ClassLikeState) state;
 			Node argsNode = node.getArgsNode();
 			if (argsNode instanceof ArrayNode) {
 				ArrayNode argsArrayNode = (ArrayNode) argsNode;
 				List args = argsArrayNode.childNodes();
 				for (Iterator iter = args.iterator(); iter.hasNext();) {
 					Node arg = (Node) iter.next();
 					if (arg instanceof SymbolNode) {
 						SymbolNode symbolNode = (SymbolNode) arg;
 						String xmethodName = symbolNode.getName();
 						List statements = classState.type.getStatements();
 						for (Iterator statIter = statements.iterator(); statIter
 								.hasNext();) {
 							Statement statement = (Statement) statIter.next();
 							if (statement instanceof MethodDeclaration) {
 								MethodDeclaration methodDeclaration = (MethodDeclaration) statement;
 								if (methodDeclaration.getName().equals(
 										xmethodName))
 									ASTUtils.setVisibility(methodDeclaration,
 											newVisibility);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public Instruction visitFalseNode(FalseNode iVisited) { // done
 		ISourcePosition position = iVisited.getPosition();
 		states.peek().add(
 				new BooleanLiteral(position.getStartOffset(), position
 						.getEndOffset(), false));
 		return null;
 	}
 
 	public Instruction visitFlipNode(FlipNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitForNode(ForNode iVisited) { // done
 		Statement varNode = collectSingleStatementSafe(iVisited.getVarNode());
 		Statement listSt = collectSingleStatementSafe(iVisited.getIterNode());
 		Expression listNode;
 		if (!(listSt instanceof ExpressionList)) {
 			ExpressionList list = new ExpressionList();
 			if (listSt instanceof Expression)
 				list.addExpression((Expression) listSt);
 			listNode = list;
 		} else
 			listNode = (Expression) listSt;
 		Statement bodyNode = collectSingleStatementSafe(iVisited.getBodyNode());
 		ISourcePosition pos = iVisited.getPosition();
 		ForStatement statement = new ForStatement(pos.getStartOffset(), pos
 				.getEndOffset(), varNode, (ExpressionList) listNode, bodyNode);
 		states.peek().add(statement);
 		return null;
 	}
 
 	public Instruction visitGlobalAsgnNode(GlobalAsgnNode iVisited) {
 
 		Node valueNode = iVisited.getValueNode();
 		if (valueNode != null) {
 			valueNode.accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitGlobalVarNode(GlobalVarNode iVisited) {
 		processVariableReference(iVisited, iVisited.getName(),
 				RubyVariableKind.GLOBAL);
 		return null;
 	}
 
 	public Instruction visitHashNode(HashNode iVisited) { // done
 		List exprs = processListNode(iVisited.getListNode());
 
 		ISourcePosition position = iVisited.getPosition();
 		HashExpression arr = new HashExpression();
 		arr.setEnd(position.getEndOffset());
 		arr.setStart(position.getStartOffset());
 
 		List hashPairs = new ArrayList();
 
 		if (exprs.size() % 2 == 0) {
 			Iterator i = exprs.iterator();
 			while (i.hasNext()) {
 				Statement key = (Statement) i.next();
 				Statement value = (Statement) i.next();
 				HashPairExpression e = new HashPairExpression(0, 0, key, value);
 				hashPairs.add(e);
 			}
 		} else {
 			if (!JRubySourceParser.isSilentState()) {
 				throw new RuntimeException("visitHashNode(): Unpaired hash!");
 			}
 		}
 
 		arr.setExpresssions(hashPairs);
 		states.peek().add(arr);
 		return null;
 	}
 
 	public Instruction visitInstAsgnNode(InstAsgnNode iVisited) {
 		processVariableAssignment(iVisited, iVisited.getName(),
 				RubyVariableKind.INSTANCE, iVisited.getValueNode());
 		return null;
 	}
 
 	public Instruction visitInstVarNode(InstVarNode iVisited) {
 		processVariableReference(iVisited, iVisited.getName(),
 				RubyVariableKind.INSTANCE);
 		return null;
 	}
 
 	public Instruction visitIfNode(IfNode iVisited) { // done
 		Statement condition = collectSingleStatementSafe(iVisited
 				.getCondition());
 		Statement thenPart = collectSingleStatementSafe(iVisited.getThenBody());
 		Statement elsePart = collectSingleStatementSafe(iVisited.getElseBody());
 		IfStatement res = new IfStatement(condition, thenPart, elsePart);
 		res.setStart(iVisited.getPosition().getStartOffset());
 		res.setEnd(iVisited.getPosition().getEndOffset() + 1);
 		states.peek().add(res);
 		return null;
 	}
 
 	public Instruction visitIterNode(IterNode iVisited) { // done
 		Statement bodyNode = collectSingleStatementSafe(iVisited.getBodyNode());
 		Statement varNode = collectSingleStatementSafe(iVisited.getVarNode());
 		ISourcePosition pos = iVisited.getPosition();
 		IterableBlock block = new IterableBlock(pos.getStartOffset(), pos
 				.getEndOffset(), varNode, bodyNode);
 		states.peek().add(block);
 		return null;
 	}
 
 	public Instruction visitLocalAsgnNode(LocalAsgnNode iVisited) {
 		processVariableAssignment(iVisited, iVisited.getName(),
 				RubyVariableKind.LOCAL, iVisited.getValueNode());
 		return null;
 	}
 
 	private void processVariableAssignment(Node iVisited, String name,
 			RubyVariableKind varKind, Node valueNode) {
 		ISourcePosition pos = iVisited.getPosition();
 		Expression left = new VariableReference(pos.getStartOffset(), pos
 				.getStartOffset()
 				+ name.length(), name, varKind);
 		Statement right = collectSingleStatementSafe(valueNode);
 		if (right == null) {
 			if (TRACE_RECOVERING)
 				RubyPlugin.log("DLTKASTBuildVisitor.processVariableAssignment("
 						+ name + "): cannot parse rhs, skipped");
 			return;
 		}
 		Assignment assgn = new Assignment(left, right);
 		copyOffsets(assgn, iVisited);
 		states.peek().add(assgn);
 	}
 
 	public Instruction visitLocalVarNode(LocalVarNode iVisited) {
 		ISourcePosition pos = fixNamePosition(iVisited.getPosition());
 		String varName = String.copyValueOf(content, pos.getStartOffset(),
 				pos.getEndOffset() - pos.getStartOffset() );
 		processVariableReference(iVisited, varName, RubyVariableKind.LOCAL);
 		return null;
 	}
 
 	private void processVariableReference(Node iVisited, String varName,
 			RubyVariableKind varKind) {
 		ISourcePosition pos2 = fixNamePosition(iVisited.getPosition());
 		if (varName.endsWith("\r"))
 			varName = varName.substring(0, varName.length() - 1);
 		VariableReference node = new VariableReference(pos2.getStartOffset(),
 				pos2.getEndOffset(), varName, varKind);
 		states.peek().add(node);
 	}
 
 	private void copyOffsets(ASTNode target, Node source) {
 		ISourcePosition pos = source.getPosition();
 		target.setStart(pos.getStartOffset());
 		target.setEnd(pos.getEndOffset());
 	}
 
 	public Instruction visitMultipleAsgnNode(MultipleAsgnNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitMatch2Node(Match2Node iVisited) {// done
 		ISourcePosition pos = iVisited.getPosition();
 		Expression receiverNode = (Expression) collectSingleStatementSafe(iVisited
 				.getReceiverNode());
 		Expression valueNode = (Expression) collectSingleStatementSafe(iVisited
 				.getValueNode());
 		RubyMatch2Expression e = new RubyMatch2Expression(pos.getStartOffset(),
 				pos.getEndOffset(), receiverNode, valueNode);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitMatch3Node(Match3Node iVisited) {// done
 		ISourcePosition pos = iVisited.getPosition();
 		Expression receiverNode = (Expression) collectSingleStatementSafe(iVisited
 				.getReceiverNode());
 		Expression valueNode = (Expression) collectSingleStatementSafe(iVisited
 				.getValueNode());
 		RubyMatch3Expression e = new RubyMatch3Expression(pos.getStartOffset(),
 				pos.getEndOffset(), receiverNode, valueNode);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitMatchNode(MatchNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		Expression regexp = (Expression) collectSingleStatementSafe(iVisited
 				.getRegexpNode());
 		RubyMatchExpression e = new RubyMatchExpression(pos.getStartOffset(),
 				pos.getEndOffset(), regexp);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitModuleNode(ModuleNode iVisited) {
 		String name = "";
 		Node cpathNode = iVisited.getCPath();
 		if (cpathNode instanceof Colon2Node || cpathNode instanceof ConstNode) {
 			name = colons2Name(cpathNode);
 		}
 		ISourcePosition pos = iVisited.getCPath().getPosition();
 		ISourcePosition cPos = iVisited.getPosition();
 		cPos = fixNamePosition(cPos);
 		pos = fixNamePosition(pos);
 		TypeDeclaration type = new TypeDeclaration(name, pos.getStartOffset(),
 				pos.getEndOffset(), cPos.getStartOffset(), cPos.getEndOffset());
 		type.setModifier(Modifiers.AccModule);
 		states.peek().add(type);
 		states.push(new ModuleState(type));
 		// body
 		Node bodyNode = iVisited.getBodyNode();
 		if (bodyNode != null) {
 			pos = bodyNode.getPosition();
 			int end = -1;
 			while (bodyNode instanceof NewlineNode)
 				bodyNode = ((NewlineNode) bodyNode).getNextNode();
 			if (bodyNode instanceof BlockNode) {
 				BlockNode blockNode = (BlockNode) bodyNode;
 				end = blockNode.getLast().getPosition().getEndOffset(); // /XXX!!!!
 			} else {
 				if (TRACE_RECOVERING)
 					RubyPlugin.log("DLTKASTBuildVisitor.visitModuleNode("
 							+ name + "): unknown body type "
 							+ bodyNode.getClass().getName());
 			}
 			pos = fixBorders(pos);
 			Block bl = new Block(pos.getStartOffset(), (end == -1) ? pos
 					.getEndOffset() : end);
 			type.setBody(bl);
 			bodyNode.accept(this);
 		}
 		states.pop();
 		return null;
 	}
 
 	public Instruction visitNewlineNode(NewlineNode iVisited) { // done
 		iVisited.getNextNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitNextNode(NextNode iVisited) {
 		return null;
 	}
 
 	public Instruction visitNilNode(NilNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		states.peek().add(
 				new NilLiteral(pos.getStartOffset(), pos.getEndOffset()));
 		return null;
 	}
 
 	public Instruction visitNotNode(NotNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		Expression expr = (Expression) collectSingleStatementSafe(iVisited
 				.getConditionNode());
 		UnaryNotExpression e = new UnaryNotExpression(pos.getStartOffset(), pos
 				.getEndOffset(), expr);
 		states.peek().add(e);
 		return null;
 	}
 
 	public Instruction visitNthRefNode(NthRefNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOpElementAsgnNode(OpElementAsgnNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOpAsgnNode(OpAsgnNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOpAsgnAndNode(OpAsgnAndNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOpAsgnOrNode(OpAsgnOrNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitOptNNode(OptNNode iVisited) {
 		// System.out.println("DLTKASTBuildVisitor.visitOptNNode()");
 		iVisited.getBodyNode().accept(this);
 		return null;
 	}
 
 	public Instruction visitOrNode(OrNode iVisited) { // done
 		Statement leftSt = collectSingleStatementSafe(iVisited.getFirstNode());
 		Expression left;
 		if (leftSt instanceof Expression) {
 			left = (Expression) leftSt;
 		} else {
 			left = null;
 		}
 		Statement rightSt = collectSingleStatementSafe(iVisited.getFirstNode());
 		Expression right;
 		if (rightSt instanceof Expression) {
 			right = (Expression) rightSt;
 		} else {
 			right = null;
 		}
 		BinaryExpression b = new BinaryExpression(left, Expression.E_BOR, right);
 		states.peek().add(b);
 		return null;
 	}
 
 	public Instruction visitPostExeNode(PostExeNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitRedoNode(RedoNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitRescueBodyNode(RescueBodyNode iVisited) { // done
 		Statement bodyNode = collectSingleStatementSafe(iVisited.getBodyNode());
 		Statement exceptionNodes = collectSingleStatementSafe(iVisited
 				.getExceptionNodes()); // in fact it would be an expression
 		// list
 		// TODO: exception nodes contains only names of exceptions, not their
 		// names itself
 		// so we need to parse them by hands
 		RescueBodyStatement optRescueNode = (RescueBodyStatement) collectSingleStatementSafe(iVisited
 				.getOptRescueNode());
 
 		ISourcePosition pos = iVisited.getPosition();
 
 		RescueBodyStatement rescueStatement = new RescueBodyStatement(pos
 				.getStartOffset(), pos.getEndOffset(), bodyNode,
 				exceptionNodes, optRescueNode);
 
 		states.peek().add(rescueStatement);
 
 		return null;
 	}
 
 	public Instruction visitRescueNode(RescueNode iVisited) { // done
 
 		Statement bodyNode = collectSingleStatementSafe(iVisited.getBodyNode());
 		Statement elseNode = collectSingleStatementSafe(iVisited.getElseNode());
 
 		RescueBodyStatement rescueNode = (RescueBodyStatement) collectSingleStatementSafe(iVisited
 				.getRescueNode());
 
 		ISourcePosition pos = iVisited.getPosition();
 
 		RescueStatement rescueStatement = new RescueStatement(pos
 				.getStartOffset(), pos.getEndOffset(), bodyNode, elseNode,
 				rescueNode);
 
 		states.peek().add(rescueStatement);
 
 		return null;
 	}
 
 	public Instruction visitRetryNode(RetryNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitReturnNode(ReturnNode iVisited) {
 		ISourcePosition position = iVisited.getPosition();
 		ASTNode value = null;
 		if (iVisited.getValueNode() != null) {
 			CollectingState state = new CollectingState();
 			states.push(state);
 			iVisited.getValueNode().accept(this);
 			states.pop();
 			if (state.list.size() == 1 && state.list.get(0) instanceof ASTNode) {
 				value = (ASTNode) state.list.get(0);
 			}
 		}
 		states.peek().add(
 				new RubyReturnStatement(value, position.getStartOffset(),
 						position.getEndOffset()));
 		return null;
 	}
 
 	public Instruction visitSClassNode(SClassNode iVisited) {
 		String name = "";
 		Node receiver = iVisited.getReceiverNode();
 		if (receiver instanceof ConstNode) {
 			name = "<< " + ((ConstNode) iVisited.getReceiverNode()).getName();
 		} else if (receiver instanceof SelfNode) {
 			name = "<< self";
 		} else {
 			int startOffset = receiver.getPosition().getStartOffset();
 			int endOffset = receiver.getPosition().getEndOffset();
 			name = "<< " + new String(String.copyValueOf(content, startOffset, endOffset - startOffset)).trim();			
 		}
 		ISourcePosition pos = iVisited.getReceiverNode().getPosition();
 		ISourcePosition cPos = iVisited.getPosition();
 		RubySingletonClassDeclaration type = new RubySingletonClassDeclaration(
 				name, pos.getStartOffset(), pos.getEndOffset() + 1, cPos
 						.getStartOffset(), cPos.getEndOffset() + 1);
 		states.peek().add(type);
 
 		CollectingState coll = new CollectingState();
 		states.push(coll);
 		receiver.accept(this);
 		states.pop();
 		if (coll.list.size() == 1 && coll.list.get(0) instanceof Expression) {
 			Object obj = coll.list.get(0);
 			type.setReceiver((Expression) obj);
 			if (obj instanceof SimpleReference) {
 				SimpleReference reference = (SimpleReference) obj;
 				type.setName("<< " + reference.getName());
 			}
 		}
 		states.push(new ClassState(type));
 		Node bodyNode = iVisited.getBodyNode();
 		if (bodyNode != null) {
 			pos = bodyNode.getPosition();
 			Block bl = new Block(pos.getStartOffset(), pos.getEndOffset() + 1);
 			type.setBody(bl);
 			bodyNode.accept(this);
 		}
 		states.pop();
 		return null;
 	}
 
 	public Instruction visitSelfNode(SelfNode iVisited) {
 		ISourcePosition position = fixNamePosition(iVisited.getPosition());
 		states.peek().add(
 				new SelfReference(position.getStartOffset(), position
 						.getEndOffset()));
 		return null;
 	}
 
 	public Instruction visitSplatNode(SplatNode iVisited) {
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 
 		return null;
 	}
 
 	public Instruction visitStrNode(StrNode iVisited) {
 		String value = iVisited.getValue().toString();
 		ISourcePosition position = iVisited.getPosition();
		if (value.length() == 0) {
			value = String.copyValueOf(content, position.getStartOffset(), position.getEndOffset() - position.getStartOffset());
		}
 		states.peek().add(
 				new StringLiteral(position.getStartOffset(), position
 						.getEndOffset(), value));
 		return null;
 	}
 
 	public Instruction visitSValueNode(SValueNode iVisited) {
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		return null;
 	}
 
 	private CallArgumentsList processCallArguments(Node argsNode) {
 		CallArgumentsList argList = new CallArgumentsList();
 		states.push(new ArgumentsState(argList));
 
 		if (argsNode instanceof ListNode) {
 			ListNode arrayNode = (ListNode) argsNode;
 			List list = arrayNode.childNodes();
 			for (Iterator iter = list.iterator(); iter.hasNext();) {
 				Node node = (Node) iter.next();
 				node.accept(this);
 			}
 		} else if (argsNode instanceof ArgsCatNode) {
 			ArgsCatNode argsCatNode = (ArgsCatNode) argsNode;
 			CallArgumentsList first = processCallArguments(argsCatNode
 					.getFirstNode());
 			CallArgumentsList second = processCallArguments(argsCatNode
 					.getSecondNode());
 			for (Iterator iterator = first.getExpressions().iterator(); iterator
 					.hasNext();) {
 				Expression e = (Expression) iterator.next();
 				argList.addExpression(e);
 			}
 			for (Iterator iterator = second.getExpressions().iterator(); iterator
 					.hasNext();) {
 				Expression e = (Expression) iterator.next();
 				argList.addExpression(e);
 			}
 		} else {
 			argsNode.accept(this);
 		}
 
 		states.pop();
 
 		return argList;
 	}
 
 	public Instruction visitSuperNode(SuperNode iVisited) { // done
 		Node argsNode = iVisited.getArgsNode();
 		CallArgumentsList callArguments = processCallArguments(argsNode);
 
 		Node iterNode = iVisited.getIterNode();
 		Statement block = (Statement) collectSingleStatementSafe(iterNode);
 
 		ISourcePosition pos = iVisited.getPosition();
 
 		RubySuperExpression expr = new RubySuperExpression(
 				pos.getStartOffset(), pos.getEndOffset(), callArguments, block);
 
 		states.peek().add( expr);
 
 		return null;
 	}
 
 	public Instruction visitToAryNode(ToAryNode iVisited) {
 		Iterator iterator = iVisited.childNodes().iterator();
 		while (iterator.hasNext()) {
 			((Node) iterator.next()).accept(this);
 		}
 		return null;
 	}
 
 	public Instruction visitTrueNode(TrueNode iVisited) { // done
 		ISourcePosition position = iVisited.getPosition();
 		states.peek().add(
 				new BooleanLiteral(position.getStartOffset(), position
 						.getEndOffset(), true));
 		return null;
 	}
 
 	public Instruction visitUndefNode(UndefNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitUntilNode(UntilNode iVisited) { // done
 		Statement condition = collectSingleStatementSafe(iVisited
 				.getConditionNode());
 		Statement body = collectSingleStatementSafe(iVisited.getBodyNode());
 		ISourcePosition pos = iVisited.getPosition();
 		UntilStatement st = new UntilStatement(condition, body);
 		st.setStart(pos.getStartOffset());
 		st.setEnd(pos.getEndOffset());
 		states.peek().add(st);
 		return null;
 	}
 
 	public Instruction visitVAliasNode(VAliasNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitVCallNode(VCallNode iVisited) {
 		String methodName = iVisited.getName();
 		// System.out.println("DLTKASTBuildVisitor.visitVCallNode(" + methodName
 		// + ")");
 
 		IState state = states.peek();
 		if (state instanceof ClassLikeState) {
 			ClassLikeState classState = (ClassLikeState) state;
 			if (methodName.equals("private"))
 				classState.visibility = Modifiers.AccPrivate;
 			else if (methodName.equals("protected"))
 				classState.visibility = Modifiers.AccProtected;
 			else if (methodName.equals("public"))
 				classState.visibility = Modifiers.AccPublic;
 		}
 
 		ISourcePosition pos = iVisited.getPosition();
 		int funcNameStart = pos.getStartOffset();
 		int funcNameEnd = pos.getEndOffset() + 1;
 		// Assert.isTrue(funcNameStart + methodName.length() == funcNameEnd);
 		CallExpression c = new CallExpression(null, methodName,
 				CallArgumentsList.EMPTY);
 		c.setStart(funcNameStart);
 		c.setEnd(funcNameEnd);
 		this.states.peek().add(c);
 
 		return null;
 	}
 
 	public Instruction visitWhenNode(WhenNode iVisited) { // done
 		ISourcePosition position = iVisited.getPosition();
 		RubyWhenStatement statement = new RubyWhenStatement(position
 				.getStartOffset(), position.getEndOffset());
 		Statement bodyStatement = collectSingleStatementSafe(iVisited
 				.getBodyNode());
 		Statement expressionsStatement = collectSingleStatementSafe(iVisited
 				.getExpressionNodes());
 
 		statement.setBody(bodyStatement);
 		if (expressionsStatement instanceof ExpressionList) {
 			ExpressionList list = (ExpressionList) expressionsStatement;
 			statement.setExpressions(list.getExpressions());
 		} else if (expressionsStatement instanceof Expression) {
 			List list = new ArrayList(1);
 			list.add(expressionsStatement);
 			statement.setExpressions(list);
 		} else {
 			throw new RuntimeException("Unsupported expression node:"
 					+ expressionsStatement + " fixme!");
 		}
 
 		states.peek().add(statement);
 		return null;
 	}
 
 	public Instruction visitWhileNode(WhileNode iVisited) { // done
 		Statement condition = collectSingleStatementSafe(iVisited
 				.getConditionNode());
 		Statement body = collectSingleStatementSafe(iVisited.getBodyNode());
 		ISourcePosition pos = iVisited.getPosition();
 		WhileStatement st = new WhileStatement(condition, body);
 		st.setStart(pos.getStartOffset());
 		st.setEnd(pos.getEndOffset());
 		states.peek().add(st);
 		return null;
 	}
 
 	public Instruction visitXStrNode(XStrNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		String value = iVisited.getValue().toString();
 		BacktickStringLiteral s = new BacktickStringLiteral(pos
 				.getStartOffset(), pos.getEndOffset(), value);
 		states.peek().add(s);
 		return null;
 	}
 
 	public Instruction visitYieldNode(YieldNode iVisited) {
 
 		return null;
 	}
 
 	public Instruction visitZArrayNode(ZArrayNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		RubyArrayExpression arr = new RubyArrayExpression();
 		arr.setStart(pos.getStartOffset());
 		arr.setEnd(pos.getEndOffset());
 		states.peek().add(arr);
 		return null;
 	}
 
 	public Instruction visitZSuperNode(ZSuperNode iVisited) { // done
 
 		CallArgumentsList callArguments = new CallArgumentsList(); // no
 		// arguments
 
 		Node iterNode = iVisited.getIterNode();
 		Statement block = collectSingleStatementSafe(iterNode);
 
 		ISourcePosition pos = iVisited.getPosition();
 
 		RubySuperExpression expr = new RubySuperExpression(
 				pos.getStartOffset(), pos.getEndOffset(), callArguments, block);
 
 		states.peek().add(expr);
 
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitBignumNode(BignumNode)
 	 */
 	public Instruction visitBignumNode(BignumNode iVisited) {// done
 		ISourcePosition pos = iVisited.getPosition();
 		BigInteger value = iVisited.getValue();
 		BigNumericLiteral literal = new BigNumericLiteral(pos.getStartOffset(),
 				pos.getEndOffset(), value);
 		states.peek().add(literal);
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitFixnumNode(FixnumNode)
 	 */
 	public Instruction visitFixnumNode(FixnumNode iVisited) {
 		ISourcePosition pos = iVisited.getPosition();
 		NumericLiteral node = new NumericLiteral(pos.getStartOffset(), pos.getEndOffset(), iVisited.getValue());		
 		states.peek().add(node);
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitFloatNode(FloatNode)
 	 */
 	public Instruction visitFloatNode(FloatNode iVisited) { // done
 		ISourcePosition pos = iVisited.getPosition();
 		double value = iVisited.getValue();
 		FloatNumericLiteral num = new FloatNumericLiteral(pos.getStartOffset(),
 				pos.getEndOffset(), value);
 		states.peek().add(num);
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitRegexpNode(RegexpNode)
 	 */
 	public Instruction visitRegexpNode(RegexpNode iVisited) { // done
 		Pattern pattern = iVisited.getPattern();
 		ISourcePosition position = iVisited.getPosition();
 		String value = iVisited.getValue().toString();
 		RegexpExpression e = new RegexpExpression(position.getStartOffset(),
 				position.getEndOffset(), value);
 		e.setPattern(pattern);
 		states.peek().add(e);
 		return null;
 	}
 
 	/**
 	 * @see NodeVisitor#visitSymbolNode(SymbolNode)
 	 */
 	public Instruction visitSymbolNode(SymbolNode iVisited) { // done
 		ISourcePosition position = iVisited.getPosition();
 
 		SymbolReference sr = new SymbolReference(position.getStartOffset(),
 				position.getEndOffset(), iVisited.getName());
 		states.peek().add(sr);
 
 		return null;
 	}
 
 	public Instruction visitArgsPushNode(ArgsPushNode arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Instruction visitAttrAssignNode(AttrAssignNode arg0) { // done
 		Statement receiver = collectSingleStatementSafe(arg0.getReceiverNode());
 		CallArgumentsList list = processCallArguments(arg0.getArgsNode());
 		CallExpression expr = new CallExpression(receiver, arg0.getName(), list);
 		copyOffsets(expr, arg0);
 		states.peek().add(expr);
 		return null;
 	}
 
 	public Instruction visitRootNode(RootNode arg0) { // done
 		Node bodyNode = arg0.getBodyNode();
 		if (bodyNode instanceof BlockNode) {
 			BlockNode blockNode = (BlockNode) bodyNode;
 			Iterator iterator = blockNode.iterator();
 			while (iterator.hasNext()) {
 				((Node) iterator.next()).accept(this);
 			}
 		} else if (bodyNode != null)
 			bodyNode.accept(this);
 		return null;
 	}
 }
