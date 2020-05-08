 package sk.stuba.fiit.perconik.eclipse.jdt.core.dom;
 
 import java.util.Set;
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
 import sk.stuba.fiit.perconik.utilities.constant.IntegralConstant;
 import sk.stuba.fiit.perconik.utilities.constant.IntegralConstantSupport;
 import sk.stuba.fiit.perconik.utilities.constant.TypeConstant;
 import sk.stuba.fiit.perconik.utilities.constant.TypeConstantSupport;
 
 /**
  * AST node types.
  * 
  * @see ASTNode
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public enum AstNodeType implements IntegralConstant, TypeConstant<ASTNode>
 {
 	/**
 	 * @see ASTNode#ANONYMOUS_CLASS_DECLARATION
 	 */
 	ANONYMOUS_CLASS_DECLARATION(ASTNode.ANONYMOUS_CLASS_DECLARATION, AnonymousClassDeclaration.class),
 	
 	/**
 	 * @see ASTNode#ARRAY_ACCESS
 	 */
 	ARRAY_ACCESS(ASTNode.ARRAY_ACCESS, ArrayAccess.class),
 	
 	/**
 	 * @see ASTNode#ARRAY_CREATION
 	 */
 	ARRAY_CREATION(ASTNode.ARRAY_CREATION, ArrayCreation.class),
 	
 	/**
 	 * @see ASTNode#ARRAY_INITIALIZER
 	 */
 	ARRAY_INITIALIZER(ASTNode.ARRAY_INITIALIZER, ArrayInitializer.class),
 	
 	/**
 	 * @see ASTNode#ARRAY_TYPE
 	 */
 	ARRAY_TYPE(ASTNode.ARRAY_TYPE, ArrayType.class),
 	
 	/**
 	 * @see ASTNode#ASSERT_STATEMENT
 	 */
 	ASSERT_STATEMENT(ASTNode.ASSERT_STATEMENT, AssertStatement.class),
 	
 	/**
 	 * @see ASTNode#ASSIGNMENT
 	 */
 	ASSIGNMENT(ASTNode.ASSIGNMENT, Assignment.class),
 	
 	/**
 	 * @see ASTNode#BLOCK
 	 */
 	BLOCK(ASTNode.BLOCK, Block.class),
 	
 	/**
 	 * @see ASTNode#BOOLEAN_LITERAL
 	 */
 	BOOLEAN_LITERAL(ASTNode.BOOLEAN_LITERAL, BooleanLiteral.class),
 	
 	/**
 	 * @see ASTNode#BREAK_STATEMENT
 	 */
 	BREAK_STATEMENT(ASTNode.BREAK_STATEMENT, BreakStatement.class),
 	
 	/**
 	 * @see ASTNode#CAST_EXPRESSION
 	 */
 	CAST_EXPRESSION(ASTNode.CAST_EXPRESSION, CastExpression.class),
 	
 	/**
 	 * @see ASTNode#CATCH_CLAUSE
 	 */
 	CATCH_CLAUSE(ASTNode.CATCH_CLAUSE, CatchClause.class),
 	
 	/**
 	 * @see ASTNode#CHARACTER_LITERAL
 	 */
 	CHARACTER_LITERAL(ASTNode.CHARACTER_LITERAL, CharacterLiteral.class),
 	
 	/**
 	 * @see ASTNode#CLASS_INSTANCE_CREATION
 	 */
 	CLASS_INSTANCE_CREATION(ASTNode.CLASS_INSTANCE_CREATION, ClassInstanceCreation.class),
 	
 	/**
 	 * @see ASTNode#COMPILATION_UNIT
 	 */
 	COMPILATION_UNIT(ASTNode.COMPILATION_UNIT, CompilationUnit.class),
 	
 	/**
 	 * @see ASTNode#CONDITIONAL_EXPRESSION
 	 */
 	CONDITIONAL_EXPRESSION(ASTNode.CONDITIONAL_EXPRESSION, ConditionalExpression.class),
 	
 	/**
 	 * @see ASTNode#CONSTRUCTOR_INVOCATION
 	 */
 	CONSTRUCTOR_INVOCATION(ASTNode.CONSTRUCTOR_INVOCATION, ConstructorInvocation.class),
 	
 	/**
 	 * @see ASTNode#CONTINUE_STATEMENT
 	 */
 	CONTINUE_STATEMENT(ASTNode.CONTINUE_STATEMENT, ContinueStatement.class),
 	
 	/**
 	 * @see ASTNode#DO_STATEMENT
 	 */
 	DO_STATEMENT(ASTNode.DO_STATEMENT, DoStatement.class),
 	
 	/**
 	 * @see ASTNode#EMPTY_STATEMENT
 	 */
 	EMPTY_STATEMENT(ASTNode.EMPTY_STATEMENT, EmptyStatement.class),
 	
 	/**
 	 * @see ASTNode#EXPRESSION_STATEMENT
 	 */
 	EXPRESSION_STATEMENT(ASTNode.EXPRESSION_STATEMENT, ExpressionStatement.class),
 	
 	/**
 	 * @see ASTNode#FIELD_ACCESS
 	 */
 	FIELD_ACCESS(ASTNode.FIELD_ACCESS, FieldAccess.class),
 	
 	/**
 	 * @see ASTNode#FIELD_DECLARATION
 	 */
 	FIELD_DECLARATION(ASTNode.FIELD_DECLARATION, FieldDeclaration.class),
 	
 	/**
 	 * @see ASTNode#FOR_STATEMENT
 	 */
 	FOR_STATEMENT(ASTNode.FOR_STATEMENT, ForStatement.class),
 	
 	/**
 	 * @see ASTNode#IF_STATEMENT
 	 */
 	IF_STATEMENT(ASTNode.IF_STATEMENT, IfStatement.class),
 	
 	/**
 	 * @see ASTNode#IMPORT_DECLARATION
 	 */
 	IMPORT_DECLARATION(ASTNode.IMPORT_DECLARATION, ImportDeclaration.class),
 	
 	/**
 	 * @see ASTNode#INFIX_EXPRESSION
 	 */
 	INFIX_EXPRESSION(ASTNode.INFIX_EXPRESSION, InfixExpression.class),
 	
 	/**
 	 * @see ASTNode#INITIALIZER
 	 */
 	INITIALIZER(ASTNode.INITIALIZER, Initializer.class),
 	
 	/**
 	 * @see ASTNode#JAVADOC
 	 */
 	JAVADOC(ASTNode.JAVADOC, Javadoc.class),
 	
 	/**
 	 * @see ASTNode#LABELED_STATEMENT
 	 */
 	LABELED_STATEMENT(ASTNode.LABELED_STATEMENT, LabeledStatement.class),
 	
 	/**
 	 * @see ASTNode#METHOD_DECLARATION
 	 */
 	METHOD_DECLARATION(ASTNode.METHOD_DECLARATION, MethodDeclaration.class),
 	
 	/**
 	 * @see ASTNode#METHOD_INVOCATION
 	 */
 	METHOD_INVOCATION(ASTNode.METHOD_INVOCATION, MethodInvocation.class),
 	
 	/**
 	 * @see ASTNode#NULL_LITERAL
 	 */
 	NULL_LITERAL(ASTNode.NULL_LITERAL, NullLiteral.class),
 	
 	/**
 	 * @see ASTNode#NUMBER_LITERAL
 	 */
 	NUMBER_LITERAL(ASTNode.NUMBER_LITERAL, NumberLiteral.class),
 	
 	/**
 	 * @see ASTNode#PACKAGE_DECLARATION
 	 */
 	PACKAGE_DECLARATION(ASTNode.PACKAGE_DECLARATION, PackageDeclaration.class),
 	
 	/**
 	 * @see ASTNode#PARENTHESIZED_EXPRESSION
 	 */
 	PARENTHESIZED_EXPRESSION(ASTNode.PARENTHESIZED_EXPRESSION, ParenthesizedExpression.class),
 	
 	/**
 	 * @see ASTNode#POSTFIX_EXPRESSION
 	 */
 	POSTFIX_EXPRESSION(ASTNode.POSTFIX_EXPRESSION, PostfixExpression.class),
 	
 	/**
 	 * @see ASTNode#PREFIX_EXPRESSION
 	 */
 	PREFIX_EXPRESSION(ASTNode.PREFIX_EXPRESSION, PrefixExpression.class),
 	
 	/**
 	 * @see ASTNode#PRIMITIVE_TYPE
 	 */
 	PRIMITIVE_TYPE(ASTNode.PRIMITIVE_TYPE, PrimitiveType.class),
 	
 	/**
 	 * @see ASTNode#QUALIFIED_NAME
 	 */
 	QUALIFIED_NAME(ASTNode.QUALIFIED_NAME, QualifiedName.class),
 	
 	/**
 	 * @see ASTNode#RETURN_STATEMENT
 	 */
 	RETURN_STATEMENT(ASTNode.RETURN_STATEMENT, ReturnStatement.class),
 	
 	/**
 	 * @see ASTNode#SIMPLE_NAME
 	 */
 	SIMPLE_NAME(ASTNode.SIMPLE_NAME, SimpleName.class),
 	
 	/**
 	 * @see ASTNode#SIMPLE_TYPE
 	 */
 	SIMPLE_TYPE(ASTNode.SIMPLE_TYPE, SimpleType.class),
 	
 	/**
 	 * @see ASTNode#SINGLE_VARIABLE_DECLARATION
 	 */
 	SINGLE_VARIABLE_DECLARATION(ASTNode.SINGLE_VARIABLE_DECLARATION, SingleVariableDeclaration.class),
 	
 	/**
 	 * @see ASTNode#STRING_LITERAL
 	 */
 	STRING_LITERAL(ASTNode.STRING_LITERAL, StringLiteral.class),
 	
 	/**
 	 * @see ASTNode#SUPER_CONSTRUCTOR_INVOCATION
 	 */
 	SUPER_CONSTRUCTOR_INVOCATION(ASTNode.SUPER_CONSTRUCTOR_INVOCATION, SuperConstructorInvocation.class),
 	
 	/**
 	 * @see ASTNode#SUPER_FIELD_ACCESS
 	 */
 	SUPER_FIELD_ACCESS(ASTNode.SUPER_FIELD_ACCESS, SuperFieldAccess.class),
 	
 	/**
 	 * @see ASTNode#SUPER_METHOD_INVOCATION
 	 */
 	SUPER_METHOD_INVOCATION(ASTNode.SUPER_METHOD_INVOCATION, SuperMethodInvocation.class),
 	
 	/**
 	 * @see ASTNode#SWITCH_CASE
 	 */
 	SWITCH_CASE(ASTNode.SWITCH_CASE, SwitchCase.class),
 	
 	/**
 	 * @see ASTNode#SWITCH_STATEMENT
 	 */
 	SWITCH_STATEMENT(ASTNode.SWITCH_STATEMENT, SwitchStatement.class),
 	
 	/**
 	 * @see ASTNode#SYNCHRONIZED_STATEMENT
 	 */
 	SYNCHRONIZED_STATEMENT(ASTNode.SYNCHRONIZED_STATEMENT, SynchronizedStatement.class),
 	
 	/**
 	 * @see ASTNode#THIS_EXPRESSION
 	 */
 	THIS_EXPRESSION(ASTNode.THIS_EXPRESSION, ThisExpression.class),
 	
 	/**
 	 * @see ASTNode#THROW_STATEMENT
 	 */
 	THROW_STATEMENT(ASTNode.THROW_STATEMENT, ThrowStatement.class),
 	
 	/**
 	 * @see ASTNode#TRY_STATEMENT
 	 */
 	TRY_STATEMENT(ASTNode.TRY_STATEMENT, TryStatement.class),
 	
 	/**
 	 * @see ASTNode#TYPE_DECLARATION
 	 */
 	TYPE_DECLARATION(ASTNode.TYPE_DECLARATION, TypeDeclaration.class),
 	
 	/**
 	 * @see ASTNode#TYPE_DECLARATION_STATEMENT
 	 */
 	TYPE_DECLARATION_STATEMENT(ASTNode.TYPE_DECLARATION_STATEMENT, TypeDeclarationStatement.class),
 	
 	/**
 	 * @see ASTNode#TYPE_LITERAL
 	 */
 	TYPE_LITERAL(ASTNode.TYPE_LITERAL, TypeLiteral.class),
 	
 	/**
 	 * @see ASTNode#VARIABLE_DECLARATION_EXPRESSION
 	 */
 	VARIABLE_DECLARATION_EXPRESSION(ASTNode.VARIABLE_DECLARATION_EXPRESSION, VariableDeclarationExpression.class),
 	
 	/**
 	 * @see ASTNode#VARIABLE_DECLARATION_FRAGMENT
 	 */
 	VARIABLE_DECLARATION_FRAGMENT(ASTNode.VARIABLE_DECLARATION_FRAGMENT, VariableDeclarationFragment.class),
 	
 	/**
 	 * @see ASTNode#VARIABLE_DECLARATION_STATEMENT
 	 */
 	VARIABLE_DECLARATION_STATEMENT(ASTNode.VARIABLE_DECLARATION_STATEMENT, VariableDeclarationStatement.class),
 	
 	/**
 	 * @see ASTNode#WHILE_STATEMENT
 	 */
 	WHILE_STATEMENT(ASTNode.WHILE_STATEMENT, WhileStatement.class),
 	
 	/**
 	 * @see ASTNode#INSTANCEOF_EXPRESSION
 	 */
 	INSTANCEOF_EXPRESSION(ASTNode.INSTANCEOF_EXPRESSION, InstanceofExpression.class),
 	
 	/**
 	 * @see ASTNode#LINE_COMMENT
 	 */
 	LINE_COMMENT(ASTNode.LINE_COMMENT, LineComment.class),
 	
 	/**
 	 * @see ASTNode#BLOCK_COMMENT
 	 */
 	BLOCK_COMMENT(ASTNode.BLOCK_COMMENT, BlockComment.class),
 	
 	/**
 	 * @see ASTNode#TAG_ELEMENT
 	 */
 	TAG_ELEMENT(ASTNode.TAG_ELEMENT, TagElement.class),
 	
 	/**
 	 * @see ASTNode#TEXT_ELEMENT
 	 */
 	TEXT_ELEMENT(ASTNode.TEXT_ELEMENT, TextElement.class),
 	
 	/**
 	 * @see ASTNode#MEMBER_REF
 	 */
 	MEMBER_REF(ASTNode.MEMBER_REF, MemberRef.class),
 	
 	/**
 	 * @see ASTNode#METHOD_REF
 	 */
 	METHOD_REF(ASTNode.METHOD_REF, MethodRef.class),
 	
 	/**
 	 * @see ASTNode#METHOD_REF_PARAMETER
 	 */
 	METHOD_REF_PARAMETER(ASTNode.METHOD_REF_PARAMETER, MethodRefParameter.class),
 	
 	/**
 	 * @see ASTNode#ENHANCED_FOR_STATEMENT
 	 */
 	ENHANCED_FOR_STATEMENT(ASTNode.ENHANCED_FOR_STATEMENT, EnhancedForStatement.class),
 	
 	/**
 	 * @see ASTNode#ENUM_DECLARATION
 	 */
 	ENUM_DECLARATION(ASTNode.ENUM_DECLARATION, EnumDeclaration.class),
 	
 	/**
 	 * @see ASTNode#ENUM_CONSTANT_DECLARATION
 	 */
 	ENUM_CONSTANT_DECLARATION(ASTNode.ENUM_CONSTANT_DECLARATION, EnumConstantDeclaration.class),
 	
 	/**
 	 * @see ASTNode#TYPE_PARAMETER
 	 */
 	TYPE_PARAMETER(ASTNode.TYPE_PARAMETER, TypeParameter.class),
 	
 	/**
 	 * @see ASTNode#PARAMETERIZED_TYPE
 	 */
 	PARAMETERIZED_TYPE(ASTNode.PARAMETERIZED_TYPE, ParameterizedType.class),
 	
 	/**
 	 * @see ASTNode#QUALIFIED_TYPE
 	 */
 	QUALIFIED_TYPE(ASTNode.QUALIFIED_TYPE, QualifiedType.class),
 	
 	/**
 	 * @see ASTNode#WILDCARD_TYPE
 	 */
 	WILDCARD_TYPE(ASTNode.WILDCARD_TYPE, WildcardType.class),
 	
 	/**
 	 * @see ASTNode#NORMAL_ANNOTATION
 	 */
 	NORMAL_ANNOTATION(ASTNode.NORMAL_ANNOTATION, NormalAnnotation.class),
 	
 	/**
 	 * @see ASTNode#MARKER_ANNOTATION
 	 */
 	MARKER_ANNOTATION(ASTNode.MARKER_ANNOTATION, MarkerAnnotation.class),
 	
 	/**
 	 * @see ASTNode#SINGLE_MEMBER_ANNOTATION
 	 */
 	SINGLE_MEMBER_ANNOTATION(ASTNode.SINGLE_MEMBER_ANNOTATION, SingleMemberAnnotation.class),
 	
 	/**
 	 * @see ASTNode#MEMBER_VALUE_PAIR
 	 */
 	MEMBER_VALUE_PAIR(ASTNode.MEMBER_VALUE_PAIR, MemberValuePair.class),
 	
 	/**
 	 * @see ASTNode#ANNOTATION_TYPE_DECLARATION
 	 */
 	ANNOTATION_TYPE_DECLARATION(ASTNode.ANNOTATION_TYPE_DECLARATION, AnnotationTypeDeclaration.class),
 	
 	/**
 	 * @see ASTNode#ANNOTATION_TYPE_MEMBER_DECLARATION
 	 */
 	ANNOTATION_TYPE_MEMBER_DECLARATION(ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION, AnnotationTypeMemberDeclaration.class),
 	
 	/**
 	 * @see ASTNode#MODIFIER
 	 */
 	MODIFIER(ASTNode.MODIFIER, Modifier.class),
 	
 	/**
 	 * @see ASTNode#UNION_TYPE
 	 */
 	UNION_TYPE(ASTNode.UNION_TYPE, UnionType.class);
 
 	private static final IntegralConstantSupport<AstNodeType> integers = IntegralConstantSupport.of(AstNodeType.class);
 
 	private static final TypeConstantSupport<AstNodeType, ASTNode> types = TypeConstantSupport.of(AstNodeType.class);
 
 	private final int value;
 	
 	private final Class<? extends ASTNode> type;
 	
 	private AstNodeType(final int value, final Class<? extends ASTNode> type)
 	{
 		assert type != null;
 		
 		this.value = value;
 		this.type  = type;
 	}
 
 	public static final Set<Integer> valuesAsIntegers()
 	{
 		return integers.getIntegers();
 	}
 	
 	public static final Set<Class<? extends ASTNode>> valuesAsTypes()
 	{
 		return types.getTypes();
 	}
 
 	public static final AstNodeType valueOf(final int value)
 	{
 		return integers.getConstant(value);
 	}
 
 	public static final AstNodeType valueOf(final Class<? extends ASTNode> type)
 	{
 		return types.getConstant(type);
 	}
 
 	public static final AstNodeType valueOf(final ASTNode node)
 	{
		return valueOf(node.getNodeType());
 	}
 	
 	public final boolean isInstance(ASTNode node)
 	{
 		return this.type.isInstance(node);
 	}
 
 	public final int getValue()
 	{
 		return this.value;
 	}
 
 	public final Class<? extends ASTNode> getType()
 	{
 		return this.type;
 	}
 }
