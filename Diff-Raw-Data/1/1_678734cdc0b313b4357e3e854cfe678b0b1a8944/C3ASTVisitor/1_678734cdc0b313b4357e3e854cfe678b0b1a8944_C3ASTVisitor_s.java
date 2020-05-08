 package jp.ac.osaka_u.ist.sdl.mpanalyzer.ast;
 
 import java.util.List;
 
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Token;
 import jp.ac.osaka_u.ist.sdl.mpanalyzer.data.Token.TokenType;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
 import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
 import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
 import org.eclipse.jdt.core.dom.ArrayAccess;
 import org.eclipse.jdt.core.dom.ArrayCreation;
 import org.eclipse.jdt.core.dom.ArrayInitializer;
 import org.eclipse.jdt.core.dom.ArrayType;
 import org.eclipse.jdt.core.dom.AssertStatement;
 import org.eclipse.jdt.core.dom.Assignment;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.BlockComment;
 import org.eclipse.jdt.core.dom.BooleanLiteral;
 import org.eclipse.jdt.core.dom.BreakStatement;
 import org.eclipse.jdt.core.dom.CastExpression;
 import org.eclipse.jdt.core.dom.CatchClause;
 import org.eclipse.jdt.core.dom.CharacterLiteral;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.ConditionalExpression;
 import org.eclipse.jdt.core.dom.ConstructorInvocation;
 import org.eclipse.jdt.core.dom.ContinueStatement;
 import org.eclipse.jdt.core.dom.DoStatement;
 import org.eclipse.jdt.core.dom.EmptyStatement;
 import org.eclipse.jdt.core.dom.EnhancedForStatement;
 import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
 import org.eclipse.jdt.core.dom.EnumDeclaration;
 import org.eclipse.jdt.core.dom.ExpressionStatement;
 import org.eclipse.jdt.core.dom.FieldAccess;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.ForStatement;
 import org.eclipse.jdt.core.dom.IfStatement;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.InfixExpression;
 import org.eclipse.jdt.core.dom.Initializer;
 import org.eclipse.jdt.core.dom.InstanceofExpression;
 import org.eclipse.jdt.core.dom.Javadoc;
 import org.eclipse.jdt.core.dom.LabeledStatement;
 import org.eclipse.jdt.core.dom.LineComment;
 import org.eclipse.jdt.core.dom.MarkerAnnotation;
 import org.eclipse.jdt.core.dom.MemberRef;
 import org.eclipse.jdt.core.dom.MemberValuePair;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.MethodRef;
 import org.eclipse.jdt.core.dom.MethodRefParameter;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.NormalAnnotation;
 import org.eclipse.jdt.core.dom.NullLiteral;
 import org.eclipse.jdt.core.dom.NumberLiteral;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.ParameterizedType;
 import org.eclipse.jdt.core.dom.ParenthesizedExpression;
 import org.eclipse.jdt.core.dom.PostfixExpression;
 import org.eclipse.jdt.core.dom.PrefixExpression;
 import org.eclipse.jdt.core.dom.PrimitiveType;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.QualifiedType;
 import org.eclipse.jdt.core.dom.ReturnStatement;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
 import org.eclipse.jdt.core.dom.SuperFieldAccess;
 import org.eclipse.jdt.core.dom.SuperMethodInvocation;
 import org.eclipse.jdt.core.dom.SwitchCase;
 import org.eclipse.jdt.core.dom.SwitchStatement;
 import org.eclipse.jdt.core.dom.SynchronizedStatement;
 import org.eclipse.jdt.core.dom.TagElement;
 import org.eclipse.jdt.core.dom.TextElement;
 import org.eclipse.jdt.core.dom.ThisExpression;
 import org.eclipse.jdt.core.dom.ThrowStatement;
 import org.eclipse.jdt.core.dom.TryStatement;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
 import org.eclipse.jdt.core.dom.TypeLiteral;
 import org.eclipse.jdt.core.dom.TypeParameter;
 import org.eclipse.jdt.core.dom.UnionType;
 import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 import org.eclipse.jdt.core.dom.WhileStatement;
 import org.eclipse.jdt.core.dom.WildcardType;
 import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;
 
 public class C3ASTVisitor extends NaiveASTFlattener {
 
 	final private List<Token> tokens;
 	final private CompilationUnit unit;
 
 	public C3ASTVisitor(final List<Token> tokens, final CompilationUnit unit) {
 		this.tokens = tokens;
 		this.unit = unit;
 	}
 
 	private int getStartLineNumber(final ASTNode node) {
 		return this.unit.getLineNumber(node.getStartPosition());
 	}
 
 	private int getEndLineNumber(final ASTNode node) {
 		return this.unit.getLineNumber(node.getStartPosition()
 				+ node.getLength());
 	}
 
 	@Override
 	public boolean visit(final AnnotationTypeDeclaration node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final AnnotationTypeMemberDeclaration node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(AnonymousClassDeclaration node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final ArrayAccess node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getArray().accept(this);
 		this.tokens.add(new Token("[", TokenType.BRACKET, line));
 		node.getIndex().accept(this);
 		this.tokens.add(new Token("]", TokenType.BRACKET, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ArrayCreation node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("new ", TokenType.PRESERVED, line));
 		node.getType().getElementType().accept(this);
 		if (0 == node.dimensions().size()) {
 			for (int i = 0; i < node.getType().getDimensions(); i++) {
 				this.tokens.add(new Token("[", TokenType.BRACKET, line));
 				this.tokens.add(new Token("]", TokenType.BRACKET, line));
 			}
 		} else {
 			for (final Object element : node.dimensions()) {
 				this.tokens.add(new Token("[", TokenType.BRACKET, line));
 				((ASTNode) element).accept(this);
 				this.tokens.add(new Token("]", TokenType.BRACKET, line));
 			}
 		}
 
 		if (null != node.getInitializer()) {
 			node.getInitializer().accept(this);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ArrayInitializer node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("{", TokenType.BRACE, line));
 		for (final Object expression : node.expressions()) {
 			((ASTNode) expression).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 		if (0 < node.expressions().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 		this.tokens.add(new Token("}", TokenType.BRACE, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ArrayType node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getElementType().accept(this);
 		for (int i = 0; i < node.getDimensions(); i++) {
 			this.tokens.add(new Token("[", TokenType.BRACKET, line));
 			this.tokens.add(new Token("]", TokenType.BRACKET, line));
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final AssertStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("assert", TokenType.PRESERVED, line));
 		node.getExpression().accept(this);
 		if (null != node.getMessage()) {
 			this.tokens.add(new Token(":", TokenType.COLON, line));
 			node.getMessage().accept(this);
 		}
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final Assignment node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getLeftHandSide().accept(this);
 		this.tokens.add(new Token("=", TokenType.OPERATOR, line));
 		node.getRightHandSide().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final Block node) {
 
 		final int startLine = this.getStartLineNumber(node);
 		final int endLine = this.getEndLineNumber(node);
 
 		this.tokens.add(new Token("{", TokenType.BRACE, startLine));
 		for (final Object statement : node.statements()) {
 			((ASTNode) statement).accept(this);
 		}
 		this.tokens.add(new Token("}", TokenType.BRACE, endLine));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final BlockComment node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final BooleanLiteral node) {
 
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(node.toString(), TokenType.LITERAL, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final BreakStatement node) {
 
 		final int startLine = this.getStartLineNumber(node);
 		final int endLine = this.getEndLineNumber(node);
 
 		this.tokens.add(new Token("break", TokenType.PRESERVED, startLine));
 		if (null != node.getLabel()) {
 			node.getLabel().accept(this);
 		}
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, endLine));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final CastExpression node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		node.getType().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		node.getExpression().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final CatchClause node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("catch", TokenType.PRESERVED, line));
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		node.getException().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		node.getBody().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final CharacterLiteral node) {
 
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(node.toString(), TokenType.LITERAL, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ClassInstanceCreation node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("new ", TokenType.PRESERVED, line));
 
 		node.getType().accept(this);
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		for (final Object argument : node.arguments()) {
 			((ASTNode) argument).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 		if (0 < node.arguments().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		if (null != node.getExpression()) {
 			this.tokens.add(new Token(".", TokenType.PIRIOD, line));
 			node.getExpression().accept(this);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final CompilationUnit node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final ConditionalExpression node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getExpression().accept(this);
 		this.tokens.add(new Token("?", TokenType.HATENA, line));
 		node.getThenExpression().accept(this);
 		this.tokens.add(new Token(":", TokenType.COLON, line));
 		node.getElseExpression().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ConstructorInvocation node) {
 
 		final int line = this.getStartLineNumber(node);
 		
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		for (final Object argument : node.arguments()) {
 			((ASTNode) argument).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 		if (0 < node.arguments().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ContinueStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("continue", TokenType.PRESERVED, line));
 		if (null != node.getLabel()) {
 			node.getLabel().accept(this);
 		}
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final DoStatement node) {
 
 		final int startLine = this.getStartLineNumber(node);
 		final int endLine = this.getEndLineNumber(node);
 
 		this.tokens.add(new Token("do", TokenType.PRESERVED, startLine));
 		node.getBody().accept(this);
 		this.tokens.add(new Token("while", TokenType.PRESERVED, endLine));
 		this.tokens.add(new Token("(", TokenType.PAREN, endLine));
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, endLine));
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, endLine));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final EmptyStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final EnhancedForStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("for", TokenType.PRESERVED, line));
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		node.getParameter().accept(this);
 		this.tokens.add(new Token(":", TokenType.COLON, line));
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		node.getBody().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final EnumConstantDeclaration node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final EnumDeclaration node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ExpressionStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final FieldAccess node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(".", TokenType.PIRIOD, line));
 		node.getName().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final FieldDeclaration node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		for (final Object modifier : node.modifiers()) {
 			((ASTNode) modifier).accept(this);
 		}
 		node.getType().accept(this);
 		for (final Object variable : node.fragments()) {
 			((ASTNode) variable).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 		this.tokens.remove(this.tokens.size() - 1);
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ForStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("for", TokenType.PRESERVED, line));
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		for (final Object initializer : node.initializers()) {
 			((ASTNode) initializer).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 
 		if (0 < node.initializers().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		if (null != node.getExpression()) {
 			node.getExpression().accept(this);
 		}
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		for (final Object initializer : node.updaters()) {
 			((ASTNode) initializer).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 		if (0 < node.updaters().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		node.getBody().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final IfStatement node) {
 
 		final int ifLine = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("if", TokenType.PRESERVED, ifLine));
 		this.tokens.add(new Token("(", TokenType.PAREN, ifLine));
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, ifLine));
 		if (null != node.getThenStatement()) {
 			node.getThenStatement().accept(this);
 		}
 		if (null != node.getElseStatement()) {
 			final int elseLine = this.getStartLineNumber(node
 					.getElseStatement());
 			this.tokens.add(new Token("else", TokenType.PRESERVED, elseLine));
 			node.getElseStatement().accept(this);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ImportDeclaration node) {
 
 		// final int line = this.getStartLineNumber(node);
 		//
 		// this.tokens.add(new Token("import", line));
 		// node.getName().accept(this);
 		// if (node.isOnDemand()) {
 		// this.tokens.add(new Token(".", line));
 		// this.tokens.add(new Token("*", line));
 		// }
 		// this.tokens.add(new Token(";", line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final InfixExpression node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getLeftOperand().accept(this);
 		this.tokens.add(new Token(node.getOperator().toString(),
 				TokenType.OPERATOR, line));
 		node.getRightOperand().accept(this);
 
 		for (final Object operand : node.extendedOperands()) {
 			this.tokens.add(new Token(node.getOperator().toString(),
 					TokenType.OPERATOR, line));
 			((ASTNode) operand).accept(this);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final Initializer node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final InstanceofExpression node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getLeftOperand().accept(this);
 		this.tokens.add(new Token("instanceof", TokenType.PRESERVED, line));
 		node.getRightOperand().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final Javadoc node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final LabeledStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getLabel().accept(this);
 		this.tokens.add(new Token(":", TokenType.COLON, line));
 		node.getBody().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final LineComment node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final MethodInvocation node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		if (null != node.getExpression()) {
 			node.getExpression().accept(this);
 			this.tokens.add(new Token(".", TokenType.PIRIOD, line));
 		}
 		node.getName().accept(this);
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 
 		for (final Object argument : node.arguments()) {
 			((ASTNode) argument).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 		if (0 < node.arguments().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final MarkerAnnotation node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final MemberRef node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final MemberValuePair node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final MethodDeclaration node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		// for (final Object modififer : node.modifiers()) {
 		// ((ASTNode) modififer).accept(this);
 		// }
 		// if (null != node.getReturnType2()) {
 		// node.getReturnType2().accept(this);
 		// } else {
 		// this.tokens.add(new Token("void", TokenType.PRESERVED, line));
 		// }
 		// node.getName().accept(this);
 		// this.tokens.add(new Token("(", TokenType.PAREN, line));
 		// for (final Object parameter : node.parameters()) {
 		// ((ASTNode) parameter).accept(this);
 		// this.tokens.add(new Token(",", TokenType.COMMA, line));
 		// }
 		// if (0 < node.parameters().size()) {
 		// this.tokens.remove(this.tokens.size() - 1);
 		// }
 		// this.tokens.add(new Token(")", TokenType.PAREN, line));
 		// if (0 < node.thrownExceptions().size()) {
 		// this.tokens.add(new Token("throws", TokenType.PRESERVED, line));
 		// for (final Object exception : node.thrownExceptions()) {
 		// ((ASTNode) exception).accept(this);
 		// this.tokens.add(new Token(",", TokenType.COMMA, line));
 		// }
 		// this.tokens.remove(this.tokens.size() - 1);
 		// }
 		if (null != node.getBody()) {
 			node.getBody().accept(this);
 		} else {
 			// this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final MethodRef node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final MethodRefParameter node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final Modifier node) {
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(node.toString(), TokenType.PRESERVED, line));
 		return false;
 	}
 
 	@Override
 	public boolean visit(final NormalAnnotation node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final NullLiteral node) {
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(node.toString(), TokenType.PRESERVED, line));
 		return false;
 	}
 
 	@Override
 	public boolean visit(final NumberLiteral node) {
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(node.toString(), TokenType.LITERAL, line));
 		return false;
 	}
 
 	@Override
 	public boolean visit(final PackageDeclaration node) {
 
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token("package", TokenType.PRESERVED, line));
 		node.getName().accept(this);
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ParameterizedType node) {
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ParenthesizedExpression node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final PostfixExpression node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getOperand().accept(this);
 		this.tokens.add(new Token(node.getOperator().toString(),
 				TokenType.OPERATOR, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final PrefixExpression node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token(node.getOperator().toString(),
 				TokenType.OPERATOR, line));
 		node.getOperand().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final PrimitiveType node) {
 
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(node.toString(), TokenType.PRESERVED, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final QualifiedName node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getQualifier().accept(this);
 		this.tokens.add(new Token(".", TokenType.PIRIOD, line));
 		node.getName().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final QualifiedType node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final ReturnStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("return", TokenType.PRESERVED, line));
 		if (null != node.getExpression()) {
 			node.getExpression().accept(this);
 		}
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final SimpleName node) {
 
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(node.getIdentifier(), TokenType.IDENTIFIER,
 				line));
 		return false;
 	}
 
 	@Override
 	public boolean visit(final SimpleType node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final SingleMemberAnnotation node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final SingleVariableDeclaration node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final SuperConstructorInvocation node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("super", TokenType.PRESERVED, line));
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		for (final Object argument : node.arguments()) {
 			((ASTNode) argument).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 		if (0 < node.arguments().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final SuperFieldAccess node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("super", TokenType.PRESERVED, line));
 		this.tokens.add(new Token(".", TokenType.PIRIOD, line));
 		node.getName().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final SuperMethodInvocation node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("super", TokenType.PRESERVED, line));
 		this.tokens.add(new Token(".", TokenType.PIRIOD, line));
 		node.getName().accept(this);
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		for (final Object argument : node.arguments()) {
 			((ASTNode) argument).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, line));
 		}
 		if (0 < node.arguments().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final StringLiteral node) {
 
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token(node.toString(), TokenType.LITERAL, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final SwitchCase node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		if (null != node.getExpression()) {
 			this.tokens.add(new Token("case", TokenType.PRESERVED, line));
 			node.getExpression().accept(this);
 		} else {
 			this.tokens.add(new Token("default", TokenType.PRESERVED, line));
 		}
 		this.tokens.add(new Token(":", TokenType.COLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final SwitchStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("switch", TokenType.PRESERVED, line));
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		for (final Object statement : node.statements()) {
 			((ASTNode) statement).accept(this);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final SynchronizedStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("synchronized", TokenType.PRESERVED, line));
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		node.getBody().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final TagElement node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final TextElement node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final ThisExpression node) {
 		final int line = this.getStartLineNumber(node);
 		this.tokens.add(new Token("this", TokenType.PRESERVED, line));
 		return false;
 	}
 
 	@Override
 	public boolean visit(final ThrowStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("throw", TokenType.PRESERVED, line));
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final TryStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("try", TokenType.PRESERVED, line));
 		node.getBody().accept(this);
 		for (final Object catchClause : node.catchClauses()) {
 			((ASTNode) catchClause).accept(this);
 		}
 		if (null != node.getFinally()) {
 			node.getFinally().accept(this);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final TypeDeclaration node) {
 
 		final int startLine = this.getStartLineNumber(node);
 		final int endLine = this.getEndLineNumber(node);
 
 		for (final Object modifier : node.modifiers()) {
 			((ASTNode) modifier).accept(this);
 		}
 		if (node.isInterface()) {
 			this.tokens.add(new Token("interface", TokenType.PRESERVED,
 					startLine));
 		} else {
 			this.tokens.add(new Token("class", TokenType.PRESERVED, startLine));
 		}
 		node.getName().accept(this);
 		if (null != node.getSuperclassType()) {
 			this.tokens
 					.add(new Token("extends", TokenType.PRESERVED, startLine));
 			node.getSuperclassType().accept(this);
 		}
 		if (0 < node.superInterfaceTypes().size()) {
 			this.tokens.add(new Token("implements", TokenType.PRESERVED,
 					startLine));
 		}
 		for (final Object implementation : node.superInterfaceTypes()) {
 			((ASTNode) implementation).accept(this);
 			this.tokens.add(new Token(",", TokenType.COMMA, startLine));
 		}
 		if (0 < node.superInterfaceTypes().size()) {
 			this.tokens.remove(this.tokens.size() - 1);
 		}
 		this.tokens.add(new Token("{", TokenType.BRACE, startLine));
 		for (final Object declaration : node.bodyDeclarations()) {
 			((ASTNode) declaration).accept(this);
 		}
 		this.tokens.add(new Token("}", TokenType.BRACE, endLine));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final TypeDeclarationStatement node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final TypeLiteral node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final TypeParameter node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final UnionType node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final VariableDeclarationExpression node) {
 		return super.visit(node);
 	}
 
 	@Override
 	public boolean visit(final VariableDeclarationFragment node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		node.getName().accept(this);
 		if (null != node.getInitializer()) {
 			this.tokens.add(new Token("=", TokenType.OPERATOR, line));
 			node.getInitializer().accept(this);
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final VariableDeclarationStatement node) {
 
 		final int line = this.getEndLineNumber(node);
 
 		for (final Object modifier : node.modifiers()) {
 			((ASTNode) modifier).accept(this);
 		}
 		node.getType().accept(this);
 		for (final Object fragment : node.fragments()) {
 			((ASTNode) fragment).accept(this);
 		}
 		this.tokens.add(new Token(";", TokenType.SEMICOLON, line));
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final WhileStatement node) {
 
 		final int line = this.getStartLineNumber(node);
 
 		this.tokens.add(new Token("while", TokenType.PRESERVED, line));
 		this.tokens.add(new Token("(", TokenType.PAREN, line));
 		node.getExpression().accept(this);
 		this.tokens.add(new Token(")", TokenType.PAREN, line));
 		node.getBody().accept(this);
 
 		return false;
 	}
 
 	@Override
 	public boolean visit(final WildcardType node) {
 		return super.visit(node);
 	}
 
 }
