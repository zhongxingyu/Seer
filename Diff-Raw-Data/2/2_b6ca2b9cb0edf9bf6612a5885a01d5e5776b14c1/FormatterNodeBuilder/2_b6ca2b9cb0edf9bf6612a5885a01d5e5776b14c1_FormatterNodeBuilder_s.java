 package org.eclipse.dltk.javascript.formatter.internal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.formatter.AbstractFormatterNodeBuilder;
 import org.eclipse.dltk.formatter.FormatterBlockNode;
 import org.eclipse.dltk.formatter.FormatterIndentedBlockNode;
 import org.eclipse.dltk.formatter.FormatterUtils;
 import org.eclipse.dltk.formatter.IFormatterContainerNode;
 import org.eclipse.dltk.formatter.IFormatterDocument;
 import org.eclipse.dltk.formatter.IFormatterTextNode;
 import org.eclipse.dltk.javascript.ast.ASTVisitor;
 import org.eclipse.dltk.javascript.ast.ArrayInitializer;
 import org.eclipse.dltk.javascript.ast.AsteriskExpression;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.BooleanLiteral;
 import org.eclipse.dltk.javascript.ast.BreakStatement;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.CaseClause;
 import org.eclipse.dltk.javascript.ast.CatchClause;
 import org.eclipse.dltk.javascript.ast.CommaExpression;
 import org.eclipse.dltk.javascript.ast.ConditionalOperator;
 import org.eclipse.dltk.javascript.ast.ConstDeclaration;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultClause;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DeleteStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.ExceptionFilter;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FinallyClause;
 import org.eclipse.dltk.javascript.ast.ForEachInStatement;
 import org.eclipse.dltk.javascript.ast.ForInStatement;
 import org.eclipse.dltk.javascript.ast.ForStatement;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetAllChildrenExpression;
 import org.eclipse.dltk.javascript.ast.GetArrayItemExpression;
 import org.eclipse.dltk.javascript.ast.GetLocalNameExpression;
 import org.eclipse.dltk.javascript.ast.GetMethod;
 import org.eclipse.dltk.javascript.ast.ISemicolonStatement;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.Keyword;
 import org.eclipse.dltk.javascript.ast.Label;
 import org.eclipse.dltk.javascript.ast.LabelledStatement;
 import org.eclipse.dltk.javascript.ast.Method;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.NullExpression;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ParenthesizedExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.RegExpLiteral;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.SetMethod;
 import org.eclipse.dltk.javascript.ast.Statement;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.ast.SwitchComponent;
 import org.eclipse.dltk.javascript.ast.SwitchStatement;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.TryStatement;
 import org.eclipse.dltk.javascript.ast.TypeOfExpression;
 import org.eclipse.dltk.javascript.ast.UnaryOperation;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.ast.VoidOperator;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.WithStatement;
 import org.eclipse.dltk.javascript.ast.XmlAttributeIdentifier;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.YieldOperator;
 import org.eclipse.dltk.javascript.formatter.JavaScriptFormatterConstants;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.AbstractParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ArrayBracketsConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.BinaryOperationPinctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.BlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.BracesNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.BracketsNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.CallExpressionPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.CallParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.CaseBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.CatchBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.CatchParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ColonNodeWrapper;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.CommaPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ConditionalOperatorPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.DoLoopWhileWrapper;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.DoWhileBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ElseBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ElseIfBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ElseIfElseBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.EmptyArrayBracketsConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ExpressionParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FinallyBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ForEmptySemicolonPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ForParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ForSemicolonPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterBinaryOperationNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterBreakNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterCaseNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterCatchClauseNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterConstDeclarationNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterContinueNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterDeleteStatementNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterElseIfNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterElseKeywordNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterElseNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterExceptionFilterNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterFinallyClauseNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterForInStatementNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterForStatementNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterFunctionNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterGetMethodNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterLabelledStatementNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterNewExpressionNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterObjectInitializerNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterReturnStatementNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterRootNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterScriptNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterSetMethodNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterStringNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterSwitchNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterThrowNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterTypeofNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterUnaryOperationNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterVariableDeclarationNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterVoidExpressionNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterVoidOperatorNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterYieldOperatorNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FunctionArgumentsParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FunctionArgumentsPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FunctionBodyBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FunctionExpressionBodyBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FunctionNoArgumentsParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.GetItemArrayBracketsConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.IBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.IBracketsConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.IParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.IPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.IfConditionParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.LineBreakFormatterNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.MethodInitializerPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.MultiLineObjectInitializerBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.OperationOrPunctuationNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ParensNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.PropertyExpressionPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.PropertyInitializerPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SemicolonNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SingleLineObjectInitializerBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.StatementBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SwitchBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SwitchConditionParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ThenBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.TrailingColonNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.TryBodyConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.WhileBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.WhileConditionParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.WithBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.WithConditionParensConfiguration;
 
 public class FormatterNodeBuilder extends AbstractFormatterNodeBuilder {
 
 	protected final IFormatterDocument document;
 
 	public FormatterNodeBuilder(IFormatterDocument document) {
 		this.document = document;
 	}
 
 	private Stack<ASTNode> nodes = new Stack<ASTNode>();
 
 	@Override
 	protected void start(IFormatterContainerNode root) {
 		super.start(root);
 		nodes.clear();
 		processed.clear();
 	}
 
 	private boolean isBlock(IFormatterContainerNode node) {
 		return node instanceof FormatterBlockNode
 				&& !(node instanceof BracesNode)
 				&& !(node instanceof ParensNode);
 	}
 
 	private boolean isStatement(ASTNode node) {
 		if (!(node instanceof Statement)) {
 			return false;
 		}
 		final Statement statement = (Statement) node;
 		final ASTNode parent = statement.getParent();
 		if (parent instanceof ForStatement || parent instanceof IfStatement
 				|| parent instanceof WhileStatement) {
 			return false;
 		}
 		return true;
 	}
 
 	private final Map<ASTNode, Boolean> processed = new IdentityHashMap<ASTNode, Boolean>();
 
 	@Override
 	protected void push(IFormatterContainerNode node) {
 		if (document
 				.getBoolean(JavaScriptFormatterConstants.STATEMENT_NEW_LINE)
 				&& isBlock(node)
 				&& isStatement(nodes.peek())
 				&& processed.put(nodes.peek(), Boolean.TRUE) == null) {
 			super.push(new LineBreakFormatterNode(node));
 		} else {
 			super.push(node);
 		}
 	}
 
 	public IFormatterContainerNode build(Script astRoot) {
 
 		final IFormatterContainerNode root = new FormatterRootNode(document);
 		start(root);
 
 		astRoot.visitAll(new ASTVisitor() {
 
 			@Override
 			public boolean visit(ASTNode node) {
 				nodes.push(node);
 				final boolean result = super.visit(node);
 				nodes.pop();
 				return result;
 			}
 
 			public boolean visitArrayInitializer(ArrayInitializer node) {
 
 				IBracketsConfiguration configuration;
 				if (node.getItems().size() > 0)
 					configuration = new ArrayBracketsConfiguration(document);
 				else
 					configuration = new EmptyArrayBracketsConfiguration(
 							document);
 
 				processBrackets(node.getLB(), node.getRB(), node.getItems(),
 						node.getCommas(), configuration);
 
 				return true;
 			}
 
 			public boolean visitBinaryOperation(BinaryOperation node) {
 
 				FormatterBinaryOperationNode formatterNode = new FormatterBinaryOperationNode(
 						document);
 
 				formatterNode.setBegin(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getLeftExpression());
 
 				skipSpaces(formatterNode, node.getOperationPosition());
 
 				processPunctuation(node.getOperationPosition(), node
 						.getOperationText().length(),
 						new BinaryOperationPinctuationConfiguration());
 
 				skipSpaces(formatterNode, node.getRightExpression()
 						.sourceStart());
 
 				visit(node.getRightExpression());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitBooleanLiteral(BooleanLiteral node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return false;
 			}
 
 			public boolean visitBreakStatement(BreakStatement node) {
 
 				FormatterBreakNode formatterNode = new FormatterBreakNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getBreakKeyword()));
 
 				push(formatterNode);
 
 				if (node.getLabel() != null)
 					visit(node.getLabel());
 
 				processOptionalSemicolon(formatterNode, node);
 				return true;
 			}
 
 			public boolean visitCallExpression(CallExpression node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				processParens(node.getLP(), node.getRP(), node.getArguments(),
 						new CallParensConfiguration(document),
 						node.getCommas(),
 						new CallExpressionPunctuationConfiguration());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			private void processTrailingColon(int colon, ASTNode keywordNode,
 					ASTNode valueNode) {
 				TrailingColonNode formatterNode = new TrailingColonNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, keywordNode));
 
 				push(formatterNode);
 
 				if (valueNode != null)
 					visit(valueNode);
 
 				formatterNode.addChild(createCharNode(document, colon));
 
 				checkedPop(formatterNode, colon);
 			}
 
 			public boolean visitCaseClause(CaseClause node) {
 				FormatterCaseNode caseNode = new FormatterCaseNode(document);
 				caseNode.setBegin(createTextNode(document, node.getKeyword()));
 				push(caseNode);
 				visit(node.getCondition());
 				caseNode.addChild(new ColonNodeWrapper(createCharNode(document,
 						node.getColonPosition())));
 
 				return processSwitchComponent(caseNode, node);
 			}
 
 			public boolean visitDefaultClause(DefaultClause node) {
 				FormatterCaseNode defaultNode = new FormatterCaseNode(document);
 				defaultNode
 						.setBegin(createTextNode(document, node.getKeyword()));
 				push(defaultNode);
 				defaultNode.addChild(new ColonNodeWrapper(createCharNode(
 						document, node.getColonPosition())));
 
 				return processSwitchComponent(defaultNode, node);
 			}
 
 			private boolean processSwitchComponent(FormatterCaseNode caseNode,
 					SwitchComponent node) {
 				if (node.getStatements().size() == 1
 						&& node.getStatements().get(0) instanceof StatementBlock) {
 					CaseBracesConfiguration configuration = new CaseBracesConfiguration(
 							document);
 					caseNode.setIndenting(false);
 					processBraces(node.getStatements().get(0), configuration);
 				} else {
 					visit(node.getStatements());
 				}
 				checkedPop(caseNode, node.sourceEnd());
 				return true;
 			}
 
 			public boolean visitCatchClause(CatchClause node) {
 
 				FormatterCatchClauseNode formatterNode = new FormatterCatchClauseNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getCatchKeyword()));
 
 				push(formatterNode);
 
 				List<ASTNode> exceptionNodes = new ArrayList<ASTNode>();
 				exceptionNodes.add(node.getException());
 				if (node.getExceptionFilter() != null) {
 					exceptionNodes.add(node.getExceptionFilter());
 				}
 
 				processParens(node.getLP(), node.getRP(), exceptionNodes,
 						new CatchParensConfiguration(document));
 
 				processBraces(node.getStatement(),
 						new CatchBracesConfiguration(document));
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			private void visitCombinedNodeList(List<ASTNode> nodes,
 					List<Integer> punctuations,
 					List<IPunctuationConfiguration> configurations) {
 
 				if (nodes.isEmpty())
 					return;
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, nodes.get(
 						0).sourceStart()));
 
 				push(formatterNode);
 
 				for (int i = 0; i < nodes.size(); i++) {
 					visit(nodes.get(i));
 					if (i < punctuations.size() && i + 1 < nodes.size()) {
 						int position = punctuations.get(i).intValue();
 						skipSpaces(formatterNode, position);
 						processPunctuation(position, 1, configurations.get(i));
 						skipSpaces(formatterNode, nodes.get(i + 1)
 								.sourceStart());
 					}
 				}
 				checkedPop(formatterNode, nodes.get(nodes.size() - 1)
 						.sourceEnd());
 			}
 
 			private void visitCombinedNodeList(List nodes, List punctuations,
 					IPunctuationConfiguration configuration) {
 				visitCombinedNodeList(nodes, punctuations, Collections.nCopies(
 						punctuations.size(), configuration));
 			}
 
 			public boolean visitCommaExpression(CommaExpression node) {
 
 				visitCombinedNodeList(node.getItems(), node.getCommas(),
 						new CommaPunctuationConfiguration());
 
 				return true;
 			}
 
 			public boolean visitConditionalOperator(ConditionalOperator node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getCondition());
 
 				skipSpaces(formatterNode, node.getQuestionPosition());
 				processPunctuation(node.getQuestionPosition(), 1,
 						new ConditionalOperatorPunctuationConfiguration());
 				skipSpaces(formatterNode, node.getTrueValue().sourceStart());
 
 				visit(node.getTrueValue());
 
 				skipSpaces(formatterNode, node.getColonPosition());
 				processPunctuation(node.getColonPosition(), 1,
 						new ConditionalOperatorPunctuationConfiguration());
 				skipSpaces(formatterNode, node.getFalseValue().sourceStart());
 
 				visit(node.getFalseValue());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitConstDeclaration(ConstDeclaration node) {
 
 				FormatterConstDeclarationNode formatterNode = new FormatterConstDeclarationNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getConstKeyword()));
 
 				push(formatterNode);
 
 				visitCombinedNodeList(node.getConsts(), node.getCommas(),
 						new CommaPunctuationConfiguration());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitContinueStatement(ContinueStatement node) {
 
 				FormatterContinueNode formatterNode = new FormatterContinueNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getContinueKeyword()));
 
 				push(formatterNode);
 
 				if (node.getLabel() != null)
 					visit(node.getLabel());
 
 				processOptionalSemicolon(formatterNode, node);
 				return true;
 			}
 
 			private void processOptionalSemicolon(
 					IFormatterContainerNode formatterNode,
 					ISemicolonStatement node) {
 				int semicolonPosition = node.getSemicolonPosition();
 				if (semicolonPosition > -1) {
 					checkedPop(formatterNode, semicolonPosition /*- 1*/);
					if (semicolonPosition > formatterNode.getEndOffset()) {
 						addChild(createSemicolonNode(document,
 								semicolonPosition));
 					}
 				} else {
 					checkedPop(formatterNode, node.sourceEnd());
 				}
 			}
 
 			public boolean visitDecimalLiteral(DecimalLiteral node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return false;
 			}
 
 			public boolean visitDeleteStatement(DeleteStatement node) {
 				FormatterDeleteStatementNode formatterNode = new FormatterDeleteStatementNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getDeleteKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.getExpression().sourceEnd());
 				return true;
 			}
 
 			public boolean visitDoWhileStatement(DoWhileStatement node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getDoKeyword()));
 
 				push(formatterNode);
 
 				processBraces(node.getBody(),
 						new DoWhileBlockBracesConfiguration(document));
 
 				formatterNode.addChild(new DoLoopWhileWrapper(createTextNode(
 						document, node.getWhileKeyword())));
 
 				processParens(node.getLP(), node.getRP(), node.getCondition(),
 						new WhileConditionParensConfiguration(document));
 
 				processOptionalSemicolon(formatterNode, node);
 				return true;
 			}
 
 			public boolean visitEmptyExpression(EmptyExpression node) {
 				// nothing
 				return true;
 			}
 
 			public boolean visitExceptionFilter(ExceptionFilter node) {
 				FormatterExceptionFilterNode formatterNode = new FormatterExceptionFilterNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getIfKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitFinallyClause(FinallyClause node) {
 				FormatterFinallyClauseNode formatterNode = new FormatterFinallyClauseNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getFinallyKeyword()));
 
 				push(formatterNode);
 
 				processBraces(node.getStatement(),
 						new FinallyBracesConfiguration(document));
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitForEachInStatement(ForEachInStatement node) {
 
 				FormatterForInStatementNode formatterNode = new FormatterForInStatementNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getForKeyword()));
 
 				push(formatterNode);
 
 				List<ASTNode> nodes = new ArrayList<ASTNode>();
 
 				nodes.add(node.getItem());
 				nodes.add(node.getInKeyword());
 				nodes.add(node.getIterator());
 
 				processParens(node.getLP(), node.getRP(), nodes,
 						new ForParensConfiguration(document));
 
 				if (node.getBody() != null)
 					processBraces(node.getBody(), new BlockBracesConfiguration(
 							document));
 
 				processOptionalSemicolon(formatterNode, node);
 				return true;
 			}
 
 			public boolean visitForInStatement(ForInStatement node) {
 
 				FormatterForInStatementNode formatterNode = new FormatterForInStatementNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getForKeyword()));
 
 				push(formatterNode);
 
 				List<ASTNode> nodes = new ArrayList<ASTNode>();
 
 				nodes.add(node.getItem());
 				nodes.add(node.getInKeyword());
 				nodes.add(node.getIterator());
 
 				processParens(node.getLP(), node.getRP(), nodes,
 						new ForParensConfiguration(document));
 
 				if (node.getBody() != null)
 					processBraces(node.getBody(), new BlockBracesConfiguration(
 							document));
 
 				processOptionalSemicolon(formatterNode, node);
 				return true;
 			}
 
 			public boolean visitForStatement(ForStatement node) {
 
 				FormatterForStatementNode formatterNode = new FormatterForStatementNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getForKeyword()));
 
 				push(formatterNode);
 
 				List<ASTNode> nodes = new ArrayList<ASTNode>();
 
 				nodes.add(node.getInitial());
 				nodes.add(node.getCondition());
 				nodes.add(node.getStep());
 
 				List<Integer> semicolons = new ArrayList<Integer>();
 				semicolons.add(new Integer(node.getInitialSemicolonPosition()));
 				semicolons.add(new Integer(node
 						.getConditionalSemicolonPosition()));
 
 				List<IPunctuationConfiguration> semicolonConfigurations = new ArrayList<IPunctuationConfiguration>();
 
 				if (node.getCondition() instanceof EmptyExpression)
 					semicolonConfigurations
 							.add(new ForEmptySemicolonPunctuationConfiguration());
 				else
 					semicolonConfigurations
 							.add(new ForSemicolonPunctuationConfiguration());
 
 				if (node.getStep() instanceof EmptyExpression)
 					semicolonConfigurations
 							.add(new ForEmptySemicolonPunctuationConfiguration());
 				else
 					semicolonConfigurations
 							.add(new ForSemicolonPunctuationConfiguration());
 
 				processParens(node.getLP(), node.getRP(), nodes,
 						new ForParensConfiguration(document), semicolons,
 						semicolonConfigurations);
 
 				if (node.getBody() != null)
 					processBraces(node.getBody(), new BlockBracesConfiguration(
 							document));
 
 				processOptionalSemicolon(formatterNode, node);
 				return true;
 			}
 
 			public boolean visitFunctionStatement(FunctionStatement node) {
 
 				FormatterFunctionNode formatterNode = new FormatterFunctionNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getFunctionKeyword()));
 
 				push(formatterNode);
 
 				if (node.getName() != null)
 					visit(node.getName());
 
 				AbstractParensConfiguration argsConfiguration;
 
 				if (node.getArguments().size() == 0) {
 					argsConfiguration = new FunctionNoArgumentsParensConfiguration(
 							document);
 				} else {
 					argsConfiguration = new FunctionArgumentsParensConfiguration(
 							document);
 				}
 
 				processParens(node.getLP(), node.getRP(), node.getArguments(),
 						argsConfiguration, node.getArgumentCommas(),
 						new FunctionArgumentsPunctuationConfiguration());
 
 				boolean emptyBody = node.getBody() == null
 						|| isEmptyBody(node.getBody());
 				IBracesConfiguration bodyConfiguration;
 
 				if (node.getName() != null)
 					bodyConfiguration = new FunctionBodyBracesConfiguration(
 							document, emptyBody);
 				else
 					bodyConfiguration = new FunctionExpressionBodyBracesConfiguration(
 							document, emptyBody);
 
 				processBraces(node.getBody(), bodyConfiguration);
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return false;
 			}
 
 			private boolean isEmptyBody(StatementBlock block) {
 				if (block.getStatements().isEmpty()) {
 					for (int i = block.getLC() + 1; i < block.getRC(); ++i) {
 						if (!Character.isWhitespace(document.charAt(i))) {
 							return false;
 						}
 					}
 					return true;
 				} else {
 					return false;
 				}
 			}
 
 			public boolean visitGetArrayItemExpression(
 					GetArrayItemExpression node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getArray());
 
 				processBrackets(node.getLB(), node.getRB(), Collections
 						.<ASTNode> singletonList(node.getIndex()), Collections
 						.<Integer> emptyList(),
 						new GetItemArrayBracketsConfiguration(document));
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitGetMethod(GetMethod node) {
 				FormatterGetMethodNode formatterNode = new FormatterGetMethodNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getGetKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getName());
 
 				processParens(node.getLP(), node.getRP(), (ASTNode) null,
 						new FunctionNoArgumentsParensConfiguration(document));
 
 				boolean emptyBody = node.getBody() == null
 						|| isEmptyBody(node.getBody());
 
 				processBraces(
 						node.getBody(),
 						new FunctionBodyBracesConfiguration(document, emptyBody));
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return false;
 			}
 
 			public boolean visitIdentifier(Identifier node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return true;
 			}
 
 			private void processParens(int leftParen, int rightParen,
 					ASTNode expression, IParensConfiguration configuration) {
 				ParensNode parens = new ParensNode(document, configuration);
 				parens.setBegin(createCharNode(document, leftParen));
 				push(parens);
 				if (expression != null) {
 					skipSpaces(parens, expression.sourceStart());
 					visit(expression);
 				}
 				checkedPop(parens, rightParen);
 				parens.setEnd(createCharNode(document, rightParen));
 			}
 
 			/**
 			 * process function declaration parameters and call arguments
 			 */
 			private void processParens(int leftParen, int rightParen,
 					List expressions, IParensConfiguration configuration,
 					List punctuations,
 					IPunctuationConfiguration punctuationConfiguration) {
 				ParensNode parens = new ParensNode(document, configuration);
 				parens.setBegin(createCharNode(document, leftParen));
 				push(parens);
 				if (!expressions.isEmpty()) {
 					final ASTNode expression0 = (ASTNode) expressions.get(0);
 					skipSpaces(parens, expression0.sourceStart());
 				}
 				visitCombinedNodeList(expressions, punctuations,
 						punctuationConfiguration);
 				checkedPop(parens, rightParen);
 				parens.setEnd(createCharNode(document, rightParen));
 			}
 
 			private void processParens(int leftParen, int rightParen,
 					List expressions, IParensConfiguration configuration,
 					List punctuations, List punctuationConfigurations) {
 				ParensNode parens = new ParensNode(document, configuration);
 				parens.setBegin(createCharNode(document, leftParen));
 				push(parens);
 				if (!expressions.isEmpty()) {
 					final ASTNode expression0 = (ASTNode) expressions.get(0);
 					skipSpaces(parens, expression0.sourceStart());
 				}
 				visitCombinedNodeList(expressions, punctuations,
 						punctuationConfigurations);
 				checkedPop(parens, rightParen);
 				parens.setEnd(createCharNode(document, rightParen));
 			}
 
 			private void processParens(int leftParen, int rightParen,
 					List expressions, IParensConfiguration configuration) {
 				ParensNode parens = new ParensNode(document, configuration);
 				parens.setBegin(createCharNode(document, leftParen));
 				push(parens);
 				if (!expressions.isEmpty()) {
 					final ASTNode expression0 = (ASTNode) expressions.get(0);
 					skipSpaces(parens, expression0.sourceStart());
 				}
 				visitNodeList(expressions);
 				checkedPop(parens, rightParen);
 				parens.setEnd(createCharNode(document, rightParen));
 			}
 
 			private void skipSpaces(IFormatterContainerNode formatterNode,
 					int end) {
 				final int prev = formatterNode.getEndOffset();
 				int pos = prev;
 				while (pos < end
 						&& FormatterUtils.isSpace(document.charAt(pos))) {
 					++pos;
 				}
 				if (pos > prev) {
 					formatterNode.addChild(createEmptyTextNode(document, pos));
 				}
 			}
 
 			private void processPunctuation(int position, int length,
 					IPunctuationConfiguration configuration) {
 				final OperationOrPunctuationNode block = new OperationOrPunctuationNode(
 						document, configuration);
 				block.addChild(createTextNode(document, position, position
 						+ length));
 				push(block);
 				// visit(node);
 				checkedPop(block, position + length);
 			}
 
 			private void processBraces(ASTNode node,
 					IBracesConfiguration configuration) {
 				if (node instanceof StatementBlock) {
 					StatementBlock block = (StatementBlock) node;
 
 					if (block.getLC() > -1 && block.getRC() > -1) {
 
 						BracesNode braces = new BracesNode(document,
 								configuration);
 
 						braces
 								.setBegin(createCharNode(document, block
 										.getLC()));
 						push(braces);
 						visitNodeList(block.getStatements());
 						checkedPop(braces, block.getRC());
 						braces.setEnd(createCharNode(document, block.getRC()));
 					} else {
 						final FormatterBlockNode formatter = new FormatterIndentedBlockNode(
 								document, configuration.isIndenting());
 						formatter.addChild(createEmptyTextNode(document, node
 								.sourceStart()));
 						push(formatter);
 						visitNodeList(block.getStatements());
 						checkedPop(formatter, node.sourceEnd());
 
 					}
 				} else {
 					final FormatterBlockNode block = new FormatterIndentedBlockNode(
 							document, configuration.isIndenting());
 					block.addChild(createEmptyTextNode(document, node
 							.sourceStart()));
 					push(block);
 					visit(node);
 					checkedPop(block, node.sourceEnd());
 				}
 			}
 
 			/**
 			 * process array initialization
 			 */
 			private void processBrackets(int leftBracket, int rightBracket,
 					List<ASTNode> nodes, List<Integer> commas,
 					IBracketsConfiguration configuration) {
 				BracketsNode brackets = new BracketsNode(document,
 						configuration);
 
 				brackets.setBegin(createCharNode(document, leftBracket));
 				push(brackets);
 				if (!nodes.isEmpty()) {
 					// TODO introduce option for: spaces after opening bracket
 					skipSpaces(brackets, nodes.get(0).sourceStart());
 				}
 				if (!commas.isEmpty()) {
 					// TODO introduce option for spaces between omitted values
 					visitCombinedNodeList(nodes, commas, Collections.nCopies(
 							commas.size(),
 							(IPunctuationConfiguration) configuration));
 				} else {
 					visitCombinedNodeList(nodes, commas, Collections
 							.<IPunctuationConfiguration> emptyList());
 				}
 				if (!nodes.isEmpty()) {
 					// TODO introduce option for: spaces before closing bracket
 					skipSpaces(brackets, rightBracket);
 				}
 				checkedPop(brackets, rightBracket);
 				brackets.setEnd(createCharNode(document, rightBracket));
 			}
 
 			private void processElseIf(ASTNode node,
 					IBracesConfiguration configuration) {
 				BracesNode braces = new BracesNode(document, configuration);
 				braces.setBegin(createEmptyTextNode(document, node
 						.sourceStart()));
 				push(braces);
 				visit(node);
 				checkedPop(braces, node.sourceEnd());
 			}
 
 			public boolean visitIfStatement(IfStatement node) {
 				final FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getIfKeyword()));
 				push(formatterNode);
 
 				processParens(node.getLP(), node.getRP(), node.getCondition(),
 						new IfConditionParensConfiguration(document));
 
 				if (node.getThenStatement() != null) {
 					final IBracesConfiguration thenConf;
 					if (node.getElseStatement() != null)
 						thenConf = new ThenBlockBracesConfiguration(document);
 					else
 						thenConf = new BlockBracesConfiguration(document);
 					processBraces(node.getThenStatement(), thenConf);
 					checkedPop(formatterNode, node.getThenStatement()
 							.sourceEnd());
 				} else {
 					checkedPop(formatterNode, node.sourceEnd());
 				}
 
 				if (node.getElseStatement() != null) {
 
 					boolean lineBreakBeforeElse = node.getThenStatement() == null
 							|| !(node.getThenStatement() instanceof StatementBlock);
 
 					IBracesConfiguration elseConfiguration;
 					FormatterElseNode elseNode = null;
 
 					if (node.getElseStatement() instanceof IfStatement) {
 						IfStatement elseStatement = (IfStatement) node
 								.getElseStatement();
 
 						if (elseStatement.getElseStatement() == null) {
 							elseConfiguration = new ElseIfBlockBracesConfiguration(
 									document);
 						} else {
 							elseConfiguration = new ElseIfElseBlockBracesConfiguration(
 									document);
 						}
 						elseNode = new FormatterElseIfNode(document,
 								lineBreakBeforeElse);
 					} else {
 						elseConfiguration = new ElseBlockBracesConfiguration(
 								document);
 						elseNode = new FormatterElseNode(document,
 								lineBreakBeforeElse);
 					}
 
 					elseNode.addChild(new FormatterElseKeywordNode(document,
 							node.getElseKeyword().sourceStart(), node
 									.getElseKeyword().sourceEnd()));
 
 					push(elseNode);
 
 					if (node.getElseStatement() instanceof IfStatement)
 						processElseIf(node.getElseStatement(),
 								elseConfiguration);
 					else
 						processBraces(node.getElseStatement(),
 								elseConfiguration);
 
 					checkedPop(elseNode, node.getElseStatement().sourceEnd());
 				}
 				return true;
 			}
 
 			public boolean visitKeyword(Keyword node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return false;
 			}
 
 			public boolean visitLabel(Label node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return true;
 			}
 
 			public boolean visitLabelledStatement(LabelledStatement node) {
 
 				FormatterLabelledStatementNode formatterNode = new FormatterLabelledStatementNode(
 						document);
 
 				formatterNode.setBegin(createEmptyTextNode(document, node
 						.getLabel().sourceStart()));
 
 				push(formatterNode);
 
 				processTrailingColon(node.getColonPosition(), node.getLabel(),
 						null);
 
 				processBraces(node.getStatement(), new CaseBracesConfiguration(
 						document));
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitNewExpression(NewExpression node) {
 
 				FormatterNewExpressionNode formatterNode = new FormatterNewExpressionNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getNewKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getObjectClass());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return false;
 			}
 
 			public boolean visitNullExpression(NullExpression node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return false;
 			}
 
 			public boolean visitObjectInitializer(ObjectInitializer node) {
 
 				IBracesConfiguration configuration;
 
 				if (node.isMultiline())
 					configuration = new MultiLineObjectInitializerBracesConfiguration(
 							document);
 				else
 					configuration = new SingleLineObjectInitializerBracesConfiguration(
 							document);
 
 				FormatterObjectInitializerNode formatterNode = new FormatterObjectInitializerNode(
 						document, configuration);
 
 				formatterNode.setBegin(createTextNode(document, node.getLC(),
 						node.getLC() + 1));
 
 				push(formatterNode);
 
 				List initializers = node.getInitializers();
 				List<IPunctuationConfiguration> commaConfigurations = new ArrayList<IPunctuationConfiguration>();
 
 				for (int i = 1; i < initializers.size(); i++) {
 					Expression item = (Expression) initializers.get(i);
 
 					if (item instanceof Method)
 						commaConfigurations
 								.add(new MethodInitializerPunctuationConfiguration());
 					else
 						commaConfigurations
 								.add(new PropertyInitializerPunctuationConfiguration());
 
 				}
 
 				visitCombinedNodeList(node.getInitializers(), node.getCommas(),
 						commaConfigurations);
 
 				checkedPop(formatterNode, node.sourceEnd() - 1);
 
 				formatterNode.setEnd(createTextNode(document, node.getRC(),
 						node.getRC() + 1));
 
 				return true;
 			}
 
 			public boolean visitParenthesizedExpression(
 					ParenthesizedExpression node) {
 
 				processParens(node.getLP(), node.getRP(), node.getExpression(),
 						new ExpressionParensConfiguration(document));
 
 				return true;
 			}
 
 			public boolean visitPropertyExpression(PropertyExpression node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getObject());
 
 				skipSpaces(formatterNode, node.getDotPosition());
 
 				processPunctuation(node.getDotPosition(), 1,
 						new PropertyExpressionPunctuationConfiguration());
 
 				skipSpaces(formatterNode, node.getProperty().sourceStart());
 
 				visit(node.getProperty());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return false;
 			}
 
 			public boolean visitPropertyInitializer(PropertyInitializer node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getName());
 
 				skipSpaces(formatterNode, node.getColon());
 
 				processPunctuation(node.getColon(), 1,
 						new PropertyInitializerPunctuationConfiguration());
 
 				skipSpaces(formatterNode, node.getValue().sourceStart());
 
 				visit(node.getValue());
 
 				checkedPop(formatterNode, node.getValue().sourceStart());
 
 				return true;
 			}
 
 			public boolean visitRegExpLiteral(RegExpLiteral node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return false;
 			}
 
 			public boolean visitReturnStatement(ReturnStatement node) {
 				FormatterReturnStatementNode formatterNode = new FormatterReturnStatementNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getReturnKeyword()));
 
 				push(formatterNode);
 
 				if (node.getValue() != null)
 					visit(node.getValue());
 
 				processOptionalSemicolon(formatterNode, node);
 				return false;
 			}
 
 			public boolean visitScript(Script node) {
 				FormatterScriptNode scriptNode = new FormatterScriptNode(
 						document);
 
 				push(scriptNode);
 
 				visitNodeList(node.getStatements());
 
 				checkedPop(scriptNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitSetMethod(SetMethod node) {
 
 				FormatterSetMethodNode formatterNode = new FormatterSetMethodNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getSetKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getName());
 
 				processParens(node.getLP(), node.getRP(), node.getArgument(),
 						new FunctionArgumentsParensConfiguration(document));
 
 				boolean emptyBody = node.getBody() == null
 						|| isEmptyBody(node.getBody());
 
 				processBraces(
 						node.getBody(),
 						new FunctionBodyBracesConfiguration(document, emptyBody));
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return false;
 			}
 
 			public boolean visitStatementBlock(StatementBlock node) {
 
 				processBraces(node, new StatementBlockBracesConfiguration(
 						document));
 
 				return true;
 			}
 
 			public boolean visitStringLiteral(StringLiteral node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return false;
 			}
 
 			public boolean visitSwitchStatement(SwitchStatement node) {
 
 				FormatterSwitchNode switchNode = new FormatterSwitchNode(
 						document);
 
 				switchNode.setBegin(createTextNode(document, node
 						.getSwitchKeyword()));
 
 				push(switchNode);
 
 				processParens(node.getLP(), node.getRP(), node.getCondition(),
 						new SwitchConditionParensConfiguration(document));
 				BracesNode braces = new BracesNode(document,
 						new SwitchBracesConfiguration(document));
 
 				braces.setBegin(createCharNode(document, node.getLC()));
 				push(braces);
 				for (SwitchComponent component : node.getCaseClauses()) {
 					visit(component);
 				}
 				checkedPop(braces, node.getRC());
 				braces.setEnd(createCharNode(document, node.getRC()));
 
 				checkedPop(switchNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitThisExpression(ThisExpression node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return false;
 			}
 
 			public boolean visitThrowStatement(ThrowStatement node) {
 
 				FormatterThrowNode formatterNode = new FormatterThrowNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getThrowKeyword()));
 
 				push(formatterNode);
 
 				if (node.getException() != null)
 					visit(node.getException());
 
 				processOptionalSemicolon(formatterNode, node);
 				return true;
 			}
 
 			public boolean visitTryStatement(TryStatement node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getTryKeyword()));
 
 				push(formatterNode);
 
 				processBraces(node.getBody(),
 						new TryBodyConfiguration(document));
 
 				List<Statement> nodes = new ArrayList<Statement>();
 				nodes.addAll(node.getCatches());
 				if (node.getFinally() != null) {
 					nodes.add(node.getFinally());
 				}
 				visitNodeList(nodes);
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitTypeOfExpression(TypeOfExpression node) {
 				FormatterTypeofNode formatterNode = new FormatterTypeofNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getTypeOfKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitUnaryOperation(UnaryOperation node) {
 				FormatterUnaryOperationNode formatterNode = new FormatterUnaryOperationNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				if (!node.isPostfix()) {
 					addChild(createTextNode(document, node
 							.getOperationPosition(), node
 							.getOperationPosition()
 							+ node.getOperationText().length()));
 					skipSpaces(formatterNode, node.getExpression()
 							.sourceStart());
 				}
 
 				visit(node.getExpression());
 
 				if (node.isPostfix()) {
 					skipSpaces(formatterNode, node.getOperationPosition());
 					addChild(createTextNode(document, node
 							.getOperationPosition(), node
 							.getOperationPosition()
 							+ node.getOperationText().length()));
 				}
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitVariableDeclaration(VariableDeclaration node) {
 				FormatterVariableDeclarationNode formatterNode = new FormatterVariableDeclarationNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getVarKeyword()));
 
 				push(formatterNode);
 
 				visitCombinedNodeList(node.getVariables(), node.getCommas(),
 						new CommaPunctuationConfiguration());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitVoidExpression(VoidExpression node) {
 				FormatterVoidExpressionNode formatterNode = new FormatterVoidExpressionNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				processOptionalSemicolon(formatterNode, node);
 				return false;
 			}
 
 			public boolean visitWhileStatement(WhileStatement node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getWhileKeyword()));
 
 				push(formatterNode);
 
 				processParens(node.getLP(), node.getRP(), node.getCondition(),
 						new WhileConditionParensConfiguration(document));
 
 				if (node.getBody() != null) {
 					processBraces(node.getBody(),
 							new WhileBlockBracesConfiguration(document));
 				}
 
 				processOptionalSemicolon(formatterNode, node);
 				return true;
 			}
 
 			public boolean visitWithStatement(WithStatement node) {
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getWithKeyword()));
 
 				push(formatterNode);
 
 				processParens(node.getLP(), node.getRP(), node.getExpression(),
 						new WithConditionParensConfiguration(document));
 
 				processBraces(node.getStatement(),
 						new WithBlockBracesConfiguration(document));
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			private void visitNodeList(List<? extends ASTNode> nodes) {
 				for (int i = 0; i < nodes.size(); i++) {
 					visit(nodes.get(i));
 				}
 			}
 
 			public boolean visitVoidOperator(VoidOperator node) {
 				FormatterVoidOperatorNode formatterNode = new FormatterVoidOperatorNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getVoidKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitYieldOperator(YieldOperator node) {
 				FormatterYieldOperatorNode formatterNode = new FormatterYieldOperatorNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getVoidKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitXmlLiteral(XmlLiteral node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return true;
 			}
 
 			public boolean visitDefaultXmlNamespace(
 					DefaultXmlNamespaceStatement node) {
 
 				FormatterBlockNode formatter = new FormatterBlockNode(document);
 
 				formatter.addChild(createTextNode(document, node
 						.getDefaultKeyword()));
 
 				push(formatter);
 
 				visit(node.getXmlKeyword());
 				visit(node.getNamespaceKeyword());
 				visit(node.getValue());
 
 				checkedPop(formatter, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitXmlPropertyIdentifier(
 					XmlAttributeIdentifier node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return true;
 			}
 
 			public boolean visitAsteriskExpression(AsteriskExpression node) {
 				FormatterStringNode strNode = new FormatterStringNode(document,
 						node);
 				addChild(strNode);
 				return true;
 			}
 
 			public boolean visitGetLocalNameExpression(
 					GetLocalNameExpression node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getNamespace());
 				visit(node.getLocalName());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitGetAllChildrenExpression(
 					GetAllChildrenExpression node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getObject());
 				visit(node.getProperty());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return true;
 			}
 
 			public boolean visitUnknownNode(ASTNode node) {
 				return false;
 			}
 
 		});
 
 		checkedPop(root, document.getLength());
 		return root;
 
 	}
 
 	private IFormatterTextNode createTextNode(IFormatterDocument document,
 			ASTNode node) {
 		return createTextNode(document, node.sourceStart(), node.sourceEnd());
 	}
 
 	private IFormatterTextNode createCharNode(IFormatterDocument document,
 			int startPos) {
 		return createTextNode(document, startPos, startPos + 1);
 	}
 
 	private IFormatterTextNode createEmptyTextNode(IFormatterDocument document,
 			int pos) {
 		return createTextNode(document, pos, pos);
 	}
 
 	private IFormatterTextNode createSemicolonNode(IFormatterDocument document,
 			int offset) {
 		return new SemicolonNode(document, offset);
 	}
 
 }
