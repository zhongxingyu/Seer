 package sk.stuba.fiit.perconik.eclipse.jdt.core.dom;
 
 import java.util.Set;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.BodyDeclaration;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.Statement;
 import sk.stuba.fiit.perconik.utilities.IntegralConstant;
 import sk.stuba.fiit.perconik.utilities.IntegralConstantSupport;
 import sk.stuba.fiit.perconik.utilities.TypeConstant;
 import sk.stuba.fiit.perconik.utilities.TypeConstantSupport;
 
 public enum AstParserConstructKind implements IntegralConstant, TypeConstant<ASTNode>
 {
 	/**
 	 * @see org.eclipse.jdt.core.dom.ASTParser#K_COMPILATION_UNIT
 	 */
 	COMPILATION_UNIT(ASTParser.K_COMPILATION_UNIT, CompilationUnit.class),
 	
 	/**
 	 * @see org.eclipse.jdt.core.dom.ASTParser#K_CLASS_BODY_DECLARATIONS
 	 */
 	CLASS_BODY_DECLARATIONS(ASTParser.K_CLASS_BODY_DECLARATIONS, BodyDeclaration.class),
 	
 	/**
 	 * @see org.eclipse.jdt.core.dom.ASTParser#K_EXPRESSION
 	 */
 	EXPRESSION(ASTParser.K_EXPRESSION, Expression.class),
 	
 	/**
	 * @see org.eclipse.jdt.core.dom.ASTParser#K_STATEMENTS
 	 */
 	STATEMENTS(ASTParser.K_STATEMENTS, Statement.class);
 
 	private static final IntegralConstantSupport<AstParserConstructKind> integers = IntegralConstantSupport.of(AstParserConstructKind.class);
 	
 	private static final TypeConstantSupport<AstParserConstructKind, ASTNode> types = TypeConstantSupport.of(AstParserConstructKind.class);
 
 	private final int value;
 	
 	private final Class<? extends ASTNode> type;
 	
 	private AstParserConstructKind(final int value, final Class<? extends ASTNode> type)
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
 	
 	public static final AstParserConstructKind valueOf(final int value)
 	{
 		return integers.getConstant(value);
 	}
 
 	public static final AstParserConstructKind valueOf(final Class<? extends ASTNode> type)
 	{
 		return types.getConstant(type);
 	}
 
 	public static final AstParserConstructKind valueOf(final ASTNode element)
 	{
 		return valueOf(element.getClass());
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
