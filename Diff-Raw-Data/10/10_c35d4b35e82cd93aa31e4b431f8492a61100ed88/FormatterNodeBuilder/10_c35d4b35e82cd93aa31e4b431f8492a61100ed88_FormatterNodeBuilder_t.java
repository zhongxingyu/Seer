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
 import org.eclipse.dltk.formatter.FormatterEmptyNode;
 import org.eclipse.dltk.formatter.FormatterIndentedBlockNode;
 import org.eclipse.dltk.formatter.FormatterUtils;
 import org.eclipse.dltk.formatter.IFormatterContainerNode;
 import org.eclipse.dltk.formatter.IFormatterDocument;
 import org.eclipse.dltk.formatter.IFormatterNode;
 import org.eclipse.dltk.formatter.IFormatterTextNode;
 import org.eclipse.dltk.javascript.ast.ASTVisitor;
 import org.eclipse.dltk.javascript.ast.Argument;
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
 import org.eclipse.dltk.javascript.ast.ConstStatement;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultClause;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DeleteStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.EmptyStatement;
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
 import org.eclipse.dltk.javascript.ast.IVariableStatement;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.JSNode;
 import org.eclipse.dltk.javascript.ast.Keyword;
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
 import org.eclipse.dltk.javascript.ast.SimpleType;
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
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.ast.VoidOperator;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.WithStatement;
 import org.eclipse.dltk.javascript.ast.XmlAttributeIdentifier;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.YieldOperator;
 import org.eclipse.dltk.javascript.formatter.JavaScriptFormatterConstants;
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
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterFinallyClauseNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterForInStatementNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterForStatementNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterFunctionNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterLabelledStatementNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterNewExpressionNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterObjectInitializerNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterRootNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterScriptNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterStringNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterSwitchNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterThrowNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.FormatterTypeofNode;
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
 import org.eclipse.dltk.javascript.formatter.internal.nodes.MultiLineObjectInitializerBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.OperationOrPunctuationNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ParensNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.PropertyExpressionPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.PropertyInitializerPunctuationConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SemicolonNode;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SingleLineObjectInitializerBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SpaceAfterKeyword;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.StatementBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SwitchBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.SwitchConditionParensConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.ThenBlockBracesConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.TryBodyConfiguration;
 import org.eclipse.dltk.javascript.formatter.internal.nodes.TypePunctuationConfiguration;
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
 		if (isMultiLineObjectInitializerComponent(node)) {
 			return true;
 		}
 		if (node instanceof SwitchComponent) {
 			return true;
 		}
 		if (!(node instanceof Statement)) {
 			return false;
 		}
 		final JSNode statement = (JSNode) node;
 		final ASTNode parent = statement.getParent();
 		if (parent instanceof ForStatement || parent instanceof IfStatement
 				|| parent instanceof WhileStatement) {
 			return false;
 		}
 		return true;
 	}
 
 	private boolean isMultiLineObjectInitializerComponent(ASTNode node) {
 		if (node instanceof PropertyInitializer) {
 			final PropertyInitializer initializer = (PropertyInitializer) node;
 			if (initializer.getValue() instanceof FunctionStatement) {
 				return true;
 			}
 		}
 		if (node instanceof Method) {
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isMultiLineObjectInitializer(ObjectInitializer initializer) {
 		for (ASTNode component : initializer.getInitializers()) {
 			if (isMultiLineObjectInitializerComponent(component)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private final Map<ASTNode, Boolean> processed = new IdentityHashMap<ASTNode, Boolean>();
 
 	@Override
 	protected void push(IFormatterContainerNode node) {
 		if (document
 				.getBoolean(JavaScriptFormatterConstants.STATEMENT_NEW_LINE)
 				&& isBlock(node)) {
 			final ASTNode astNode = nodes.peek();
 			if (isStatement(astNode)
 					&& processed.put(astNode, Boolean.TRUE) == null) {
 				super.push(new LineBreakFormatterNode(node));
 				return;
 			}
 		}
 		super.push(node);
 	}
 
 	public IFormatterContainerNode build(Script astRoot) {
 
 		final IFormatterContainerNode root = new FormatterRootNode(document);
 		start(root);
 
 		astRoot.visitAll(new ASTVisitor<IFormatterNode>() {
 
 			@Override
 			public IFormatterNode visit(ASTNode node) {
 				if (node instanceof Keyword) {
 					// for(each) *in*
 					// catch *if*
 					return addChild(createTextNode(document, node));
 				} else if (node instanceof PropertyInitializer) {
 					nodes.push(node);
 					final IFormatterNode result = formatPropertyInitializer((PropertyInitializer) node);
 					nodes.pop();
 					return result;
 				} else if (node instanceof GetMethod) {
 					nodes.push(node);
 					final IFormatterNode result = formatGetMethod((GetMethod) node);
 					nodes.pop();
 					return result;
 				} else if (node instanceof SetMethod) {
 					nodes.push(node);
 					final IFormatterNode result = formatSetMethod((SetMethod) node);
 					nodes.pop();
 					return result;
 				} else {
 					nodes.push(node);
 					final IFormatterNode result = super.visit(node);
 					nodes.pop();
 					return result;
 				}
 			}
 
 			@Override
 			public IFormatterNode visitArrayInitializer(ArrayInitializer node) {
 
 				IBracketsConfiguration configuration;
 				if (node.getItems().size() > 0)
 					configuration = new ArrayBracketsConfiguration(document,
 							node);
 				else
 					configuration = new EmptyArrayBracketsConfiguration(
 							document, node);
 
 				return processBrackets(node.getLB(), node.getRB(), node
 						.getItems(), node.getCommas(), configuration);
 			}
 
 			@Override
 			public IFormatterNode visitBinaryOperation(BinaryOperation node) {
 
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
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitBooleanLiteral(BooleanLiteral node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitBreakStatement(BreakStatement node) {
 
 				FormatterBreakNode formatterNode = new FormatterBreakNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getBreakKeyword()));
 
 				push(formatterNode);
 
 				if (node.getLabel() != null) {
 					addChild(new FormatterStringNode(document, node.getLabel()));
 				}
 
 				return processOptionalSemicolon(formatterNode, node);
 			}
 
 			@Override
 			public IFormatterNode visitCallExpression(CallExpression node) {
 
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
 
 				return formatterNode;
 			}
 
 			private IFormatterNode processSwitchComponent(
 					FormatterCaseNode caseNode, SwitchComponent node) {
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
 				return caseNode;
 			}
 
 			private IFormatterNode visitCombinedNodeList(
 					List<? extends ASTNode> nodes, List<Integer> punctuations,
 					List<IPunctuationConfiguration> configurations) {
 
 				if (nodes.isEmpty())
 					return null;
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, nodes.get(
 						0).sourceStart()));
 
 				push(formatterNode);
 
 				for (int i = 0; i < nodes.size(); i++) {
 					visit(nodes.get(i));
 					if (i < punctuations.size() && i + 1 < nodes.size()) {
 						int position = punctuations.get(i).intValue();
 						skipSpacesOnly(formatterNode, position);
 						processPunctuation(position, 1, configurations.get(i));
 						skipSpacesOnly(formatterNode, nodes.get(i + 1)
 								.sourceStart());
 					}
 				}
 				checkedPop(formatterNode, nodes.get(nodes.size() - 1)
 						.sourceEnd());
 				return formatterNode;
 			}
 
 			private IFormatterNode visitCombinedNodeList(
 					List<? extends ASTNode> nodes, List<Integer> punctuations,
 					IPunctuationConfiguration configuration) {
 				return visitCombinedNodeList(nodes, punctuations, Collections
 						.nCopies(punctuations.size(), configuration));
 			}
 
 			@Override
 			public IFormatterNode visitCommaExpression(CommaExpression node) {
 				return visitCombinedNodeList(node.getItems(), node.getCommas(),
 						new CommaPunctuationConfiguration());
 			}
 
 			@Override
 			public IFormatterNode visitConditionalOperator(
 					ConditionalOperator node) {
 
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
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitConstDeclaration(ConstStatement node) {
 
 				FormatterConstDeclarationNode formatterNode = new FormatterConstDeclarationNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getConstKeyword()));
 
 				push(formatterNode);
 
 				processVariableDeclarations(node);
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitContinueStatement(ContinueStatement node) {
 
 				FormatterContinueNode formatterNode = new FormatterContinueNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getContinueKeyword()));
 
 				push(formatterNode);
 
 				if (node.getLabel() != null) {
 					addChild(new FormatterStringNode(document, node.getLabel()));
 				}
 
 				return processOptionalSemicolon(formatterNode, node);
 			}
 
 			private IFormatterNode processOptionalSemicolon(
 					IFormatterContainerNode formatterNode,
 					ISemicolonStatement node) {
 				int semicolonPosition = node.getSemicolonPosition();
 				if (semicolonPosition > -1) {
 					checkedPop(formatterNode, semicolonPosition /*- 1*/);
 					if (semicolonPosition >= formatterNode.getEndOffset()) {
 						addChild(createSemicolonNode(document,
 								semicolonPosition));
 					}
 				} else {
 					checkedPop(formatterNode, node.sourceEnd());
 				}
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitDecimalLiteral(DecimalLiteral node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitDeleteStatement(DeleteStatement node) {
 				FormatterDeleteStatementNode formatterNode = new FormatterDeleteStatementNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getDeleteKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.getExpression().sourceEnd());
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitDoWhileStatement(DoWhileStatement node) {
 
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
 
 				return processOptionalSemicolon(formatterNode, node);
 			}
 
 			@Override
 			public IFormatterNode visitEmptyExpression(EmptyExpression node) {
 				// nothing
 				return null;
 			}
 
 			@Override
 			public IFormatterNode visitForEachInStatement(
 					ForEachInStatement node) {
 
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
 
 				checkedPop(formatterNode, node.sourceEnd());
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitForInStatement(ForInStatement node) {
 
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
 
 				checkedPop(formatterNode, node.sourceEnd());
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitForStatement(ForStatement node) {
 
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
 
 				checkedPop(formatterNode, node.sourceEnd());
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitFunctionStatement(FunctionStatement node) {
 
 				FormatterFunctionNode formatterNode = new FormatterFunctionNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getFunctionKeyword()));
 
 				push(formatterNode);
 
 				if (node.getName() != null)
 					visit(node.getName());
 
 				final IParensConfiguration parensConf;
 				if (node.getArguments().isEmpty()) {
 					parensConf = new FunctionNoArgumentsParensConfiguration(
 							document);
 				} else {
 					parensConf = new FunctionArgumentsParensConfiguration(
 							document);
 				}
 				final ParensNode parens = new ParensNode(document, parensConf);
 				parens.setBegin(createCharNode(document, node.getLP()));
 				push(parens);
 				if (!node.getArguments().isEmpty()) {
 					final Argument arg0 = node.getArguments().get(0);
 					skipSpaces(parens, arg0.sourceStart());
 				}
 				for (Argument argument : node.getArguments()) {
 					visit(argument.getIdentifier());
 					if (argument.getType() != null) {
 						skipSpaces(parens, argument.getColonPosition());
 						processPunctuation(argument.getColonPosition(), 1,
 								new TypePunctuationConfiguration());
 						visit(argument.getType());
 					}
 					if (argument.getCommaPosition() != -1) {
 						int position = argument.getCommaPosition();
 						skipSpacesOnly(parens, position);
 						processPunctuation(position, 1,
 								new FunctionArgumentsPunctuationConfiguration());
 					}
 				}
 				checkedPop(parens, node.getRP());
 				parens.setEnd(createCharNode(document, node.getRP()));
 
 				if (node.getReturnType() != null) {
 					skipSpaces(formatterNode, node.getColonPosition());
 					processPunctuation(node.getColonPosition(), 1,
 							new TypePunctuationConfiguration());
 					visit(node.getReturnType());
 				}
 
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
 
 				return formatterNode;
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
 
 			@Override
 			public IFormatterNode visitGetArrayItemExpression(
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
 
 				return formatterNode;
 			}
 
 			private IFormatterNode formatGetMethod(GetMethod node) {
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
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
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitIdentifier(Identifier node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitSimpleType(SimpleType node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			private IFormatterNode processParens(int leftParen, int rightParen,
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
 				return parens;
 			}
 
 			/**
 			 * process function declaration parameters and call arguments
 			 */
 			private void processParens(int leftParen, int rightParen,
 					List<? extends ASTNode> expressions,
 					IParensConfiguration configuration,
 					List<Integer> punctuations,
 					IPunctuationConfiguration punctuationConfiguration) {
 				ParensNode parens = new ParensNode(document, configuration);
 				parens.setBegin(createCharNode(document, leftParen));
 				push(parens);
 				if (!expressions.isEmpty()) {
 					final ASTNode expression0 = expressions.get(0);
 					skipSpaces(parens, expression0.sourceStart());
 				}
 				visitCombinedNodeList(expressions, punctuations,
 						punctuationConfiguration);
 				checkedPop(parens, rightParen);
 				parens.setEnd(createCharNode(document, rightParen));
 			}
 
 			private void processParens(int leftParen, int rightParen,
 					List<ASTNode> expressions,
 					IParensConfiguration configuration,
 					List<Integer> punctuations,
 					List<IPunctuationConfiguration> punctuationConfigurations) {
 				ParensNode parens = new ParensNode(document, configuration);
 				parens.setBegin(createCharNode(document, leftParen));
 				push(parens);
 				if (!expressions.isEmpty()) {
 					final ASTNode expression0 = expressions.get(0);
 					skipSpaces(parens, expression0.sourceStart());
 				}
 				visitCombinedNodeList(expressions, punctuations,
 						punctuationConfigurations);
 				checkedPop(parens, rightParen);
 				parens.setEnd(createCharNode(document, rightParen));
 			}
 
 			private void processParens(int leftParen, int rightParen,
 					List<ASTNode> expressions,
 					IParensConfiguration configuration) {
 				ParensNode parens = new ParensNode(document, configuration);
 				parens.setBegin(createCharNode(document, leftParen));
 				push(parens);
 				if (!expressions.isEmpty()) {
 					final ASTNode expression0 = expressions.get(0);
 					skipSpaces(parens, expression0.sourceStart());
 				}
 				visitNodeList(expressions);
 				checkedPop(parens, rightParen);
 				parens.setEnd(createCharNode(document, rightParen));
 			}
 
 			private void skipSpacesOnly(IFormatterContainerNode formatterNode,
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
 
 			private void skipSpaces(IFormatterContainerNode formatterNode,
 					int end) {
 				final int prev = formatterNode.getEndOffset();
 				int pos = prev;
 				while (pos < end
 						&& Character.isWhitespace(document.charAt(pos))) {
 					++pos;
 				}
 				if (pos > prev) {
 					formatterNode.addChild(createEmptyTextNode(document, pos));
 				}
 			}
 
 			private void processPunctuation(int position, int length,
 					IPunctuationConfiguration configuration) {
 				addChild(new OperationOrPunctuationNode(createTextNode(
 						document, position, position + length), configuration));
 			}
 
 			private IFormatterNode processBraces(ASTNode node,
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
 						return braces;
 					} else {
 						final FormatterBlockNode formatter = new FormatterIndentedBlockNode(
 								document, configuration.isIndenting());
 						formatter.addChild(createEmptyTextNode(document, node
 								.sourceStart()));
 						push(formatter);
 						visitNodeList(block.getStatements());
 						checkedPop(formatter, node.sourceEnd());
 						return formatter;
 
 					}
 				} else {
 					final FormatterBlockNode block = new FormatterIndentedBlockNode(
 							document, configuration.isIndenting());
 					block.addChild(createEmptyTextNode(document, node
 							.sourceStart()));
 					push(block);
 					visit(node);
 					checkedPop(block, node.sourceEnd());
 					return block;
 				}
 			}
 
 			/**
 			 * process array initialization
 			 */
 			private IFormatterNode processBrackets(int leftBracket,
 					int rightBracket, List<ASTNode> nodes,
 					List<Integer> commas, IBracketsConfiguration configuration) {
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
 				return brackets;
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
 
 			@Override
 			public IFormatterNode visitIfStatement(IfStatement node) {
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
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitLabelledStatement(LabelledStatement node) {
 
 				FormatterLabelledStatementNode formatterNode = new FormatterLabelledStatementNode(
 						document);
 
 				formatterNode
 						.setBegin(createTextNode(document, node.getLabel()));
 				formatterNode.addChild(createCharNode(document, node
 						.getColonPosition()));
 
 				push(formatterNode);
 
 				// TODO introduce options for it
 				processBraces(node.getStatement(), new CaseBracesConfiguration(
 						document));
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitNewExpression(NewExpression node) {
 
 				FormatterNewExpressionNode formatterNode = new FormatterNewExpressionNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getNewKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getObjectClass());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitNullExpression(NullExpression node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitObjectInitializer(ObjectInitializer node) {
 
 				final IBracesConfiguration configuration;
 
 				if (node.isMultiline()
 						|| document
 								.getBoolean(JavaScriptFormatterConstants.STATEMENT_NEW_LINE)
 						&& isMultiLineObjectInitializer(node))
 					configuration = new MultiLineObjectInitializerBracesConfiguration(
 							document, node);
 				else
 					configuration = new SingleLineObjectInitializerBracesConfiguration(
 							document);
 
 				FormatterObjectInitializerNode formatterNode = new FormatterObjectInitializerNode(
 						document, configuration);
 
 				formatterNode.setBegin(createCharNode(document, node.getLC()));
 
 				push(formatterNode);
 
 				visitCombinedNodeList(node.getInitializers(), node.getCommas(),
 						new PropertyInitializerPunctuationConfiguration());
 
 				checkedPop(formatterNode, node.sourceEnd() - 1);
 
 				formatterNode.setEnd(createCharNode(document, node.getRC()));
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitParenthesizedExpression(
 					ParenthesizedExpression node) {
 
 				return processParens(node.getLP(), node.getRP(), node
 						.getExpression(), new ExpressionParensConfiguration(
 						document));
 			}
 
 			@Override
 			public IFormatterNode visitPropertyExpression(
 					PropertyExpression node) {
 
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
 
 				return formatterNode;
 			}
 
 			private IFormatterNode formatPropertyInitializer(
 					PropertyInitializer node) {
 
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
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitRegExpLiteral(RegExpLiteral node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitReturnStatement(ReturnStatement node) {
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				final IFormatterTextNode keyword = createTextNode(document,
 						node.getReturnKeyword());
 				formatterNode
 						.addChild(node.getValue() != null ? new SpaceAfterKeyword(
 								keyword)
 								: keyword);
 
 				push(formatterNode);
 
 				if (node.getValue() != null)
 					visit(node.getValue());
 
 				return processOptionalSemicolon(formatterNode, node);
 			}
 
 			@Override
 			public IFormatterNode visitEmptyStatement(EmptyStatement node) {
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 				push(formatterNode);
 				return processOptionalSemicolon(formatterNode, node);
 			}
 
 			@Override
 			public IFormatterNode visitScript(Script node) {
 				FormatterScriptNode scriptNode = new FormatterScriptNode(
 						document);
 
 				push(scriptNode);
 
 				visitNodeList(node.getStatements());
 
 				checkedPop(scriptNode, node.sourceEnd());
 
 				return scriptNode;
 			}
 
 			private IFormatterNode formatSetMethod(SetMethod node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
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
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitStatementBlock(StatementBlock node) {
 				return processBraces(node,
 						new StatementBlockBracesConfiguration(document));
 			}
 
 			@Override
 			public IFormatterNode visitStringLiteral(StringLiteral node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitSwitchStatement(SwitchStatement node) {
 
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
 					nodes.push(component);
 					if (component instanceof CaseClause) {
 						final CaseClause caseClause = (CaseClause) component;
 						FormatterCaseNode caseNode = new FormatterCaseNode(
 								document);
 						caseNode.setBegin(new SpaceAfterKeyword(createTextNode(
 								document, caseClause.getKeyword())));
 						push(caseNode);
 						visit(caseClause.getCondition());
 						caseNode.addChild(new ColonNodeWrapper(createCharNode(
 								document, caseClause.getColonPosition())));
 						processSwitchComponent(caseNode, caseClause);
 					} else {
 						final DefaultClause defaultClause = (DefaultClause) component;
 						FormatterCaseNode defaultNode = new FormatterCaseNode(
 								document);
 						defaultNode.setBegin(createTextNode(document,
 								defaultClause.getKeyword()));
 						push(defaultNode);
 						defaultNode.addChild(new ColonNodeWrapper(
 								createCharNode(document, defaultClause
 										.getColonPosition())));
 						processSwitchComponent(defaultNode, defaultClause);
 					}
 					nodes.pop();
 				}
 				checkedPop(braces, node.getRC());
 				braces.setEnd(createCharNode(document, node.getRC()));
 
 				checkedPop(switchNode, node.sourceEnd());
 
 				return switchNode;
 			}
 
 			@Override
 			public IFormatterNode visitThisExpression(ThisExpression node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitThrowStatement(ThrowStatement node) {
 
 				FormatterThrowNode formatterNode = new FormatterThrowNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getThrowKeyword()));
 
 				push(formatterNode);
 
 				if (node.getException() != null)
 					visit(node.getException());
 
 				return processOptionalSemicolon(formatterNode, node);
 			}
 
 			@Override
 			public IFormatterNode visitTryStatement(TryStatement node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getTryKeyword()));
 
 				push(formatterNode);
 
 				processBraces(node.getBody(),
 						new TryBodyConfiguration(document));
 
 				for (CatchClause catchClause : node.getCatches()) {
 					processCatch(catchClause);
 				}
 				if (node.getFinally() != null) {
 					processFinally(node.getFinally());
 				}
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			private void processCatch(CatchClause catchClause) {
 				FormatterCatchClauseNode formatterNode = new FormatterCatchClauseNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, catchClause
 						.getCatchKeyword()));
 
 				push(formatterNode);
 
 				List<ASTNode> exceptionNodes = new ArrayList<ASTNode>();
 				exceptionNodes.add(catchClause.getException());
				if (catchClause.getIfKeyword() != null) {
					exceptionNodes.add(catchClause.getIfKeyword());
				}
				if (catchClause.getFilterExpression() != null) {
					exceptionNodes.add(catchClause.getFilterExpression());
 				}
 
 				processParens(catchClause.getLP(), catchClause.getRP(),
 						exceptionNodes, new CatchParensConfiguration(document));
 
 				processBraces(catchClause.getStatement(),
 						new CatchBracesConfiguration(document));
 
 				checkedPop(formatterNode, catchClause.sourceEnd());
 			}
 
 			private void processFinally(FinallyClause node) {
 				FormatterFinallyClauseNode formatterNode = new FormatterFinallyClauseNode(
 						document);
 
 				formatterNode.addChild(createTextNode(document, node
 						.getFinallyKeyword()));
 
 				push(formatterNode);
 
 				processBraces(node.getStatement(),
 						new FinallyBracesConfiguration(document));
 
 				checkedPop(formatterNode, node.sourceEnd());
 			}
 
 			@Override
 			public IFormatterNode visitTypeOfExpression(TypeOfExpression node) {
 				FormatterTypeofNode formatterNode = new FormatterTypeofNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getTypeOfKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitUnaryOperation(UnaryOperation node) {
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
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
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitVariableStatment(VariableStatement node) {
 				FormatterVariableDeclarationNode formatterNode = new FormatterVariableDeclarationNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getVarKeyword()));
 
 				push(formatterNode);
 
 				processVariableDeclarations(node);
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			private void processVariableDeclarations(IVariableStatement node) {
 				final List<VariableDeclaration> vars = node.getVariables();
 				if (vars.isEmpty()) {
 					return;
 				}
 				final FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 				formatterNode.addChild(createEmptyTextNode(document, vars
 						.get(0).sourceStart()));
 				push(formatterNode);
 				for (VariableDeclaration var : vars) {
 					visit(var.getIdentifier());
 					if (var.getType() != null) {
 						int position = var.getColonPosition();
 						skipSpaces(formatterNode, position);
 						processPunctuation(position, 1,
 								new TypePunctuationConfiguration());
 						visit(var.getType());
 					}
 					if (var.getInitializer() != null) {
 						int position = var.getAssignPosition();
 						skipSpaces(formatterNode, position);
 						processPunctuation(position, 1,
 								new BinaryOperationPinctuationConfiguration());
 						visit(var.getInitializer());
 					}
 					if (var.getCommaPosition() != -1) {
 						int position = var.getCommaPosition();
 						skipSpacesOnly(formatterNode, position);
 						processPunctuation(position, 1,
 								new CommaPunctuationConfiguration());
 					}
 				}
 				checkedPop(formatterNode, vars.get(vars.size() - 1).sourceEnd());
 			}
 
 			@Override
 			public IFormatterNode visitVoidExpression(VoidExpression node) {
 				FormatterVoidExpressionNode formatterNode = new FormatterVoidExpressionNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				return processOptionalSemicolon(formatterNode, node);
 			}
 
 			@Override
 			public IFormatterNode visitWhileStatement(WhileStatement node) {
 
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
 
 				checkedPop(formatterNode, node.sourceEnd());
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitWithStatement(WithStatement node) {
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
 
 				return formatterNode;
 			}
 
 			private void visitNodeList(List<? extends ASTNode> nodes) {
 				for (int i = 0; i < nodes.size(); i++) {
 					visit(nodes.get(i));
 				}
 			}
 
 			@Override
 			public IFormatterNode visitVoidOperator(VoidOperator node) {
 				FormatterVoidOperatorNode formatterNode = new FormatterVoidOperatorNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getVoidKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitYieldOperator(YieldOperator node) {
 				FormatterYieldOperatorNode formatterNode = new FormatterYieldOperatorNode(
 						document);
 
 				formatterNode.setBegin(createTextNode(document, node
 						.getYieldKeyword()));
 
 				push(formatterNode);
 
 				visit(node.getExpression());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitXmlLiteral(XmlLiteral node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitDefaultXmlNamespace(
 					DefaultXmlNamespaceStatement node) {
 
 				FormatterBlockNode formatter = new FormatterBlockNode(document);
 
 				formatter.addChild(createTextNode(document, node
 						.getDefaultKeyword()));
 
 				push(formatter);
 
 				addChild(createTextNode(document, node.getXmlKeyword()));
 				addChild(createTextNode(document, node.getNamespaceKeyword()));
 				visit(node.getValue());
 
 				checkedPop(formatter, node.sourceEnd());
 
 				return formatter;
 			}
 
 			@Override
 			public IFormatterNode visitXmlPropertyIdentifier(
 					XmlAttributeIdentifier node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitAsteriskExpression(
 					AsteriskExpression node) {
 				return addChild(new FormatterStringNode(document, node));
 			}
 
 			@Override
 			public IFormatterNode visitGetLocalNameExpression(
 					GetLocalNameExpression node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getNamespace());
 				visit(node.getLocalName());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
 			}
 
 			@Override
 			public IFormatterNode visitGetAllChildrenExpression(
 					GetAllChildrenExpression node) {
 
 				FormatterBlockNode formatterNode = new FormatterBlockNode(
 						document);
 
 				formatterNode.addChild(createEmptyTextNode(document, node
 						.sourceStart()));
 
 				push(formatterNode);
 
 				visit(node.getObject());
 				visit(node.getProperty());
 
 				checkedPop(formatterNode, node.sourceEnd());
 
 				return formatterNode;
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
 		return new FormatterEmptyNode(document, pos);
 	}
 
 	private IFormatterTextNode createSemicolonNode(IFormatterDocument document,
 			int offset) {
 		return new SemicolonNode(document, offset);
 	}
 
 }
