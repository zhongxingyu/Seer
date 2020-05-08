 package org.jmlspecs.jml4.boogie;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.eclipse.jdt.internal.compiler.ASTVisitor;
 import org.eclipse.jdt.internal.compiler.ast.AND_AND_Expression;
 import org.eclipse.jdt.internal.compiler.ast.ASTNode;
 import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
 import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
 import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
 import org.eclipse.jdt.internal.compiler.ast.Argument;
 import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
 import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
 import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
 import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
 import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
 import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
 import org.eclipse.jdt.internal.compiler.ast.Assignment;
 import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
 import org.eclipse.jdt.internal.compiler.ast.Block;
 import org.eclipse.jdt.internal.compiler.ast.BreakStatement;
 import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
 import org.eclipse.jdt.internal.compiler.ast.CastExpression;
 import org.eclipse.jdt.internal.compiler.ast.CharLiteral;
 import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
 import org.eclipse.jdt.internal.compiler.ast.Clinit;
 import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
 import org.eclipse.jdt.internal.compiler.ast.CompoundAssignment;
 import org.eclipse.jdt.internal.compiler.ast.ConditionalExpression;
 import org.eclipse.jdt.internal.compiler.ast.ContinueStatement;
 import org.eclipse.jdt.internal.compiler.ast.DoStatement;
 import org.eclipse.jdt.internal.compiler.ast.DoubleLiteral;
 import org.eclipse.jdt.internal.compiler.ast.EmptyStatement;
 import org.eclipse.jdt.internal.compiler.ast.EqualExpression;
 import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
 import org.eclipse.jdt.internal.compiler.ast.Expression;
 import org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral;
 import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
 import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
 import org.eclipse.jdt.internal.compiler.ast.FieldReference;
 import org.eclipse.jdt.internal.compiler.ast.FloatLiteral;
 import org.eclipse.jdt.internal.compiler.ast.ForStatement;
 import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
 import org.eclipse.jdt.internal.compiler.ast.IfStatement;
 import org.eclipse.jdt.internal.compiler.ast.ImportReference;
 import org.eclipse.jdt.internal.compiler.ast.Initializer;
 import org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression;
 import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
 import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
 import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
 import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
 import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
 import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
 import org.eclipse.jdt.internal.compiler.ast.MessageSend;
 import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
 import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
 import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
 import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
 import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
 import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
 import org.eclipse.jdt.internal.compiler.ast.PostfixExpression;
 import org.eclipse.jdt.internal.compiler.ast.PrefixExpression;
 import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
 import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
 import org.eclipse.jdt.internal.compiler.ast.QualifiedSuperReference;
 import org.eclipse.jdt.internal.compiler.ast.QualifiedThisReference;
 import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
 import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
 import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
 import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
 import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
 import org.eclipse.jdt.internal.compiler.ast.Statement;
 import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
 import org.eclipse.jdt.internal.compiler.ast.StringLiteralConcatenation;
 import org.eclipse.jdt.internal.compiler.ast.SuperReference;
 import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
 import org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement;
 import org.eclipse.jdt.internal.compiler.ast.ThisReference;
 import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
 import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;
 import org.eclipse.jdt.internal.compiler.ast.TryStatement;
 import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
 import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
 import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
 import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
 import org.eclipse.jdt.internal.compiler.ast.Wildcard;
 import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
 import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
 import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
 import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
 import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
 import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
 import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
 import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
 import org.jmlspecs.jml4.ast.JmlAssertStatement;
 import org.jmlspecs.jml4.ast.JmlAssignableClause;
 import org.jmlspecs.jml4.ast.JmlAssumeStatement;
 import org.jmlspecs.jml4.ast.JmlCastExpressionWithoutType;
 import org.jmlspecs.jml4.ast.JmlConstructorDeclaration;
 import org.jmlspecs.jml4.ast.JmlDoStatement;
 import org.jmlspecs.jml4.ast.JmlEnsuresClause;
 import org.jmlspecs.jml4.ast.JmlForStatement;
 import org.jmlspecs.jml4.ast.JmlKeywordExpression;
 import org.jmlspecs.jml4.ast.JmlLoopAnnotations;
 import org.jmlspecs.jml4.ast.JmlLoopInvariant;
 import org.jmlspecs.jml4.ast.JmlLoopVariant;
 import org.jmlspecs.jml4.ast.JmlMethodDeclaration;
 import org.jmlspecs.jml4.ast.JmlMethodSpecification;
 import org.jmlspecs.jml4.ast.JmlOldExpression;
 import org.jmlspecs.jml4.ast.JmlRequiresClause;
 import org.jmlspecs.jml4.ast.JmlResultReference;
 import org.jmlspecs.jml4.ast.JmlSingleNameReference;
 import org.jmlspecs.jml4.ast.JmlSpecCase;
 import org.jmlspecs.jml4.ast.JmlSpecCaseRestAsClauseSeq;
 import org.jmlspecs.jml4.ast.JmlStoreRefListExpression;
 import org.jmlspecs.jml4.ast.JmlWhileStatement;
 
 public class BoogieVisitor extends ASTVisitor {
 	private static final boolean DEBUG = true;
 	private BlockScope methodScope;
 	private BoogieSource output;
 	private Hashtable typeList = new Hashtable();
 
 	private int stringPoolValue = 0;
 	private Hashtable stringPool = new Hashtable();
 
 	//private int objectPoolValue = 0;
 	
 	private static final String BLOCK_OPEN = "{"; //$NON-NLS-1$
 	private static final String BLOCK_CLOSE = "}"; //$NON-NLS-1$
 	private static final String PAREN_OPEN = "("; //$NON-NLS-1$
 	private static final String PAREN_CLOSE = ")"; //$NON-NLS-1$
 	private static final String STMT_END = ";"; //$NON-NLS-1$
 	private static final String SPACE = " "; //$NON-NLS-1$
 	private static final String RESULT = "$r"; //$NON-NLS-1$
 	private static final String REF = "$Ref"; //$NON-NLS-1$
 	
 	private BoogieSymbolTable symbolTable;
 	
 	public BoogieVisitor(BoogieSource output) {
 		this.output = output;
 	}
 	
 	public static BoogieSource visit(CompilationUnitDeclaration unit) {
 		return visit(unit, new BoogieSource());
 	}
 
 	public static BoogieSource visit(CompilationUnitDeclaration unit, BoogieSource output) {
 		BoogieVisitor visitor = new BoogieVisitor(output);
 		unit.traverse(visitor, unit.scope);
 		return output;
 	}
 
 	public void appendLine(Object o) { output.appendLine(o); }
 	
 	public void append(Object o) { output.append(o); }
 	public void append(char data[]) { output.append(new String(data)); }
 	
 	public void prepend(String o) {	output.prepend(o);	}
 
 	public void append(Object o, ASTNode linePointTerm) {
 		output.append(o, linePointTerm);
 	}
 
 	private void debug(ASTNode term, Object scope) {
 		if (!DEBUG)
 			return;
 		System.out.println("Visiting " //$NON-NLS-1$
 				+ term.getClass() + " on line " //$NON-NLS-1$
 				+ term.sourceStart + (scope != null ? (" from scope " //$NON-NLS-1$
 				+ scope.getClass().getSimpleName())
 						: " from class scope")); //$NON-NLS-1$
 	}
 
 	/**
 	 * Converts a statement to a block to traverse it with braces
 	 * @param term the statement to convert into a block
 	 * @return the original block if term is a Block, 
 	 * 	otherwise a new Block with one statement term. 
 	 */
 	private Block toBlock(Statement term, BlockScope scope) {
 		if (term instanceof Block) { 
 			((Block)term).scope = scope;
 			return (Block)term;
 		}
 		Block blk = new Block(0);
 		blk.statements = new Statement[]{term};
 		blk.scope = scope; 
 		return blk;
 	}
 	
 	/**
 	 * Traverses a block created by {@link #toBlock(Statement, BlockScope)}.
 	 * You must use this method if you want the block to have indentation and
 	 * surrounding braces (because Boogie does not allow anonymous blocks, 
 	 * automatically handling this in the Block visit will not work).
 	 * 
 	 * @param blk the block to traverse
 	 * @param scope the scope that would otherwise be passed to the traverse method.
 	 */
 	private void traverseBlock(Block blk, BlockScope scope) {
 		appendLine(BLOCK_OPEN);
 		output.increaseIndent();
 		blk.traverse(this, scope);
 		output.decreaseIndent();
 		appendLine(BLOCK_CLOSE);
 	}
 
 	private void variableInitialization(AbstractVariableDeclaration term, BlockScope scope) {
 		Expression init = term.initialization;
 		if (init == null) {
 			if (term.type.resolvedType == TypeBinding.INT) {
 					init = new IntLiteral(new char[]{'0'}, 
 							term.sourceStart, term.sourceEnd);
 			}
 			else if (term.type.resolvedType == TypeBinding.LONG) {
 				init = new LongLiteral(new char[]{'0'}, 
 						term.sourceStart, term.sourceEnd);
 			}
 			else if (term.type.resolvedType == TypeBinding.BOOLEAN) {
 				init = new FalseLiteral(term.sourceStart, term.sourceEnd);
 			}
 			else {
 				init = new NullLiteral(term.sourceStart, term.sourceEnd);
 			}
 		}
 		
 		Assignment a = 
 			new Assignment(new SingleNameReference(term.name, term.sourceStart), 
 					init, term.sourceEnd);
 		a.traverse(this, scope);
 	}
 	
 	private void declareType(String type, String superType) {
 		if (type.equals("int")) return; //$NON-NLS-1$
 		typeList.put(type, superType);
 	}
 	
 	private void declareType(String type) {
 		declareType(type, "java.lang.Object"); //$NON-NLS-1$
 	}
 	
 
 	private String declareString(String key) {
 		declareType("java.lang.String"); //$NON-NLS-1$
 		String value = (String)stringPool.get(key);
 		if (value == null) {
 			value = "$string_lit_" + stringPoolValue++; //$NON-NLS-1$
 			prepend("const " + value + " : $Ref;\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			prepend("axiom " + value + " <: java.lang.String;\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return value;
 	}
 	
 	/*
 	private String declareObject() {
 		String objName = "$obj_" + objectPoolValue++; //$NON-NLS-1$
 		prepend("const unique " + objName + ": $Ref;");  //$NON-NLS-1$//$NON-NLS-2$
 		return objName;
 	}
 	*/
 	
 	private void emitTypes() {
 		StringBuffer outBuf = new StringBuffer();
 		Enumeration e = typeList.keys();
 		while (e.hasMoreElements()) {
 			String key = (String)e.nextElement();
 			outBuf.append("const " + key + ": $TName;\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			outBuf.append("axiom " + key + " <: " + typeList.get(key) + ";\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		prepend(outBuf.toString());
 	}
 	
 	// priority=2 group=expr
 	public boolean visit(AllocationExpression term, BlockScope scope) {
 		debug(term, scope);
 		// implemented in Assignment
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(AND_AND_Expression term, BlockScope scope) {
 		debug(term, scope);
 		term.left.traverse(this, scope);
 		append(" && "); //$NON-NLS-1$
 		term.right.traverse(this, scope);
 		return false;
 	}
 
 	// priority=0 group=decl
 	public boolean visit(AnnotationMethodDeclaration term, ClassScope classScope) {
 		debug(term, classScope);
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(Argument term, BlockScope scope) {
 		debug(term, scope);
 
 		String sym = symbolTable.addSymbol(new String(term.name));
 		append(sym + ": "); //$NON-NLS-1$
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(Argument term, ClassScope scope) {
 		debug(term, scope);
 
 		String sym = symbolTable.addSymbol(new String(term.name));
 		append(sym + ": "); //$NON-NLS-1$
 		return true;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(ArrayAllocationExpression term, BlockScope scope) {
 		debug(term, scope);
 		// implemented in Assignment
 		return true;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(ArrayInitializer term, BlockScope scope) {
 		debug(term, scope);
 		// implemented in Assignment
 		return true;
 	}
 
 	// TODO priority=0 group=array
 	public boolean visit(ArrayQualifiedTypeReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=array
 	public boolean visit(ArrayQualifiedTypeReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=2 group=array
 	public boolean visit(ArrayReference term, BlockScope scope) {
 		debug(term, scope);
 		term.receiver.traverse(this, scope);
 		append("["); //$NON-NLS-1$
 		term.position.traverse(this, scope);
 		append("]"); //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=2 group=array
 	public boolean visit(ArrayTypeReference term, BlockScope scope) {
 		debug(term, scope);
 		append("[int] "); //$NON-NLS-1$
 		
 		if (term.resolvedType == TypeBinding.BOOLEAN || term.token.equals(TypeConstants.BOOLEAN)) {
 			append("bool"); //$NON-NLS-1$
 			return true;
 		}
 		
 		if (term.resolvedType != null && !term.resolvedType.leafComponentType().isBaseType()) {
 			String name = new String(term.resolvedType.leafComponentType().readableName());
 			declareType(name);
 			append(REF);
 		}
 		else if (term.resolvedType != null) {
 			append(term.resolvedType.leafComponentType().readableName());
 		}
 		else {
 			String name = new String(term.token);
 			if (!name.equals("int")) { //$NON-NLS-1$
 				declareType(name);
 				append(REF);
 			}
 			else {
 				append(name);
 			}
 		}
 		
 		return true;
 	}
 
 	// priority=2 group=array
 	public boolean visit(ArrayTypeReference term, ClassScope scope) {
 		debug(term, scope);
 		append("[int] "); //$NON-NLS-1$
 		if (term.resolvedType == TypeBinding.BOOLEAN || term.token.equals(TypeConstants.BOOLEAN)) {
 			append("bool"); //$NON-NLS-1$
 			return true;
 		}
 		
 		if (term.resolvedType != null && !term.resolvedType.leafComponentType().isBaseType()) {
 			String name = new String(term.resolvedType.leafComponentType().readableName());
 			declareType(name);
 			append(REF);
 		}
 		else if (term.resolvedType != null) {
 			append(term.resolvedType.leafComponentType().readableName());
 		}
 		else {
 			String name = new String(term.token);
 			declareType(name);
 			append(REF);
 		}
 		return true;
 	}
 
 	// priority=3 group=misc
 	public boolean visit(AssertStatement term, BlockScope scope) {
 		debug(term, scope);
 		JmlAssertStatement stmt = new JmlAssertStatement("assert", term.assertExpression, term.sourceStart); //$NON-NLS-1$
 		stmt.traverse(this, scope);
 		return false;
 	}
 	
 	private void defineArrayLength(Expression lhs, BlockScope scope, int size) {
 		if (lhs instanceof JmlSingleNameReference && ((JmlSingleNameReference)lhs).binding instanceof FieldBinding) return; //FIXME
 		lhs.traverse(this, scope);
 		appendLine(".length := " + size + STMT_END); //$NON-NLS-1$
 	}
 	
 	private void initializeArray(Expression lhs, Expression[] expressions, BlockScope scope) {
 		for (int i = 0; i < expressions.length; i++) {
 			Assignment asn = new Assignment(
 					new ArrayReference(lhs, 
 						new IntLiteral(new Integer(i).toString().toCharArray(), 0, 0, i)),
 					expressions[i],
 					lhs.sourceEnd);
 			asn.traverse(this, scope);
 		}
 		defineArrayLength(lhs, scope, expressions.length);		
 	}
 
 	// priority=3 group=expr
 	public boolean visit(Assignment term, BlockScope scope) {
 		debug(term, scope);
 		if (term.expression instanceof AllocationExpression) {
 			AllocationExpression expr = (AllocationExpression)term.expression;
 			//term.lhs.traverse(this, scope);
 			//appendLine(" := " + declareObject() + STMT_END); //$NON-NLS-1$
 			append("call ", expr); //$NON-NLS-1$
 			append(expr.binding.declaringClass.readableName());
 			append("." + new String(expr.type.getLastToken())); //$NON-NLS-1$
 			append(PAREN_OPEN);
 			term.lhs.traverse(this, scope);
 			
 			if (expr.arguments != null) {
 				append(", "); //$NON-NLS-1$
 				for (int i = 0; i < expr.arguments.length; i++) {
 					expr.arguments[i].traverse(this, scope);
 					if (i < expr.arguments.length - 1) append(", "); //$NON-NLS-1$
 				}
 			}
 			append(PAREN_CLOSE);
 			appendLine(STMT_END);
 			return false;
 		}
 		
 		if (term.expression instanceof ArrayInitializer) {
 			// treat this as a list of regular assignments
 			initializeArray(term.lhs, ((ArrayInitializer)term.expression).expressions, scope);
 			return false;
 		}
 		else if (term.expression instanceof ArrayAllocationExpression) {
 			ArrayAllocationExpression alloc = (ArrayAllocationExpression)term.expression;
 			if (alloc.initializer != null) {
 				initializeArray(term.lhs, alloc.initializer.expressions, scope);
 			}
 			else if (alloc.dimensions[0] instanceof IntLiteral) {
 				int size = ((IntLiteral)alloc.dimensions[0]).value;
 				Expression[] exprs = new Expression[size];
 				if (alloc.type.resolvedType == TypeBinding.INT || alloc.type.resolvedType == TypeBinding.LONG) {
 					for (int i = 0; i < size; i++) {
 						exprs[i] = new IntLiteral(new char[]{'0'}, 0, 0, 0);
 					}
 				}
 				else if (alloc.type.resolvedType == TypeBinding.BOOLEAN) {
 					for (int i = 0; i < size; i++) {
 						exprs[i] = new FalseLiteral(0, 0);
 					}
 				}
 				else {
 					for (int i = 0; i < size; i++) {
 						exprs[i] = new NullLiteral(0, 0);
 					}
 				}
 				initializeArray(term.lhs, exprs, scope);
 			}
 			return false;
 		}
 		
 		if (term.expression instanceof MessageSend) {
 			append("call ", term.expression); //$NON-NLS-1$
 		}
 		
 		if (term.expression instanceof PostfixExpression) {
 			term.lhs.traverse(this, scope);
 			append(" := ", term); //$NON-NLS-1$
 			((Assignment)term.expression).lhs.traverse(this, scope);
 		}
 		else if (term.expression instanceof Assignment) {
 			term.expression.traverse(this, scope);
 			term.lhs.traverse(this, scope);
 			append(" := ", term); //$NON-NLS-1$
 			((Assignment)term.expression).lhs.traverse(this, scope);
 		}
 		else {
 			term.lhs.traverse(this, scope);
 			append(" := ", term); //$NON-NLS-1$
 			term.expression.traverse(this, scope);
 		}
 		
 		return false;
 	}
 	
 	public void endVisit(Assignment term, BlockScope scope) {
 		if (term.expression instanceof AllocationExpression) {
 			return;
 		}
 		if (term.expression instanceof ArrayInitializer || term.expression instanceof ArrayAllocationExpression) {
 			return;	// don't need stmt_end here
 		}
 		
 		appendLine(STMT_END);		
 
 		if (term.expression instanceof PostfixExpression) {
 			term.expression.traverse(this, scope);
 		}
 	}
 
 	// priority=2 group=expr
 	public boolean visit(BinaryExpression term, BlockScope scope) {
 		debug(term, scope);
 		append("(");  //$NON-NLS-1$
 		term.left.traverse(this, scope);
 
 		String out = ""; //$NON-NLS-1$
 		switch ((term.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
 			case OperatorIds.PLUS :
 				out = "+"; //$NON-NLS-1$
 				break;
 			case OperatorIds.MINUS :
 				out = "-"; //$NON-NLS-1$
 				break;
 			case OperatorIds.MULTIPLY :
 				out = "*"; //$NON-NLS-1$
 				break;
 			case OperatorIds.DIVIDE :
 				out = "/"; //$NON-NLS-1$
 				break;
 			case OperatorIds.REMAINDER :
 				out = "%"; //$NON-NLS-1$
 				break;
 			case OperatorIds.AND :
 				out = "&"; //$NON-NLS-1$
 				break;
 			case OperatorIds.OR :
 				out = "|"; //$NON-NLS-1$
 				break;
 			case OperatorIds.XOR :
 				break;
 			case OperatorIds.LEFT_SHIFT :
 				break;
 			case OperatorIds.RIGHT_SHIFT :
 				break;
 			case OperatorIds.UNSIGNED_RIGHT_SHIFT :
 				break;
 			case OperatorIds.GREATER :
 				out = ">"; //$NON-NLS-1$
 				break;
 			case OperatorIds.GREATER_EQUAL :
 				out = ">="; //$NON-NLS-1$
 				break;
 			case OperatorIds.LESS :
 				out = "<"; //$NON-NLS-1$
 				break;
 			case OperatorIds.LESS_EQUAL :
 				out = "<="; //$NON-NLS-1$
 				break;
 			case OperatorIds.JML_IMPLIES:
 				out = "⇒"; //$NON-NLS-1$ 
 				break;
 			case OperatorIds.JML_REV_IMPLIES:
 				out = "=<"; //$NON-NLS-1$
 				break;
 		}
 		append(SPACE + out + SPACE);
 		term.right.traverse(this, scope);
 		append(")");  //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(Block term, BlockScope scope) {
 		debug(term, scope);
 		if (symbolTable != null) 
 			symbolTable.enterScope(term);
 		return true;
 	}
 
 	public void endVisit(Block term, BlockScope scope) {
 		if (symbolTable != null) 
 			symbolTable.exitScope();
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(BreakStatement term, BlockScope scope) {
 		debug(term, scope);
 		if (term.label == null)  
 			appendLine("break" + STMT_END); //$NON-NLS-1$
 		else
 			appendLine("break " + new String(term.label) + STMT_END);  //$NON-NLS-1$
 			
 		return true;
 	}
 
 	// TODO priority=0 group=stmt
 	public boolean visit(CaseStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=1 group=expr
 	public boolean visit(CastExpression term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=2 group=lit
 	public boolean visit(CharLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append(new Integer(term.source()[1]));
 		return true;
 	}
 
 	// TODO priority=3 group=lit
 	public boolean visit(ClassLiteralAccess term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(Clinit term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=decl
 	public boolean visit(CompilationUnitDeclaration term, CompilationUnitScope scope) {
 		debug(term, scope);
 		// implemented
 		appendLine("/*!BOOGIESTART!*/"); //$NON-NLS-1$
 		return true;
 	}
 	
 	public void endVisit(CompilationUnitDeclaration term, CompilationUnitScope scope) {
 		emitTypes();
 	}
 
 	// TODO priority=2 group=expr
 	public boolean visit(CompoundAssignment term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=3 group=expr
 	public boolean visit(ConditionalExpression term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=2 group=decl
 	public boolean visit(JmlConstructorDeclaration term, ClassScope scope) {
 		debug(term, scope);
 		
 		if (term.isDefaultConstructor() && 
 				(term.statements == null || term.statements.length == 0)) {
 			return false;
 		}
 
 		JmlMethodDeclaration decl = new JmlMethodDeclaration(term.compilationResult);
 		decl.annotations = term.annotations;
 		decl.arguments = term.arguments;
 		decl.binding = term.binding;
 		decl.bits = term.bits;
 		decl.bodyEnd = term.bodyEnd;
 		decl.bodyStart = term.bodyStart;
 		decl.sourceStart = term.sourceStart;
 		decl.sourceEnd = term.sourceEnd;
 		decl.selector = term.selector;
 		decl.statements = term.statements;
 		decl.specification = term.specification;
 		decl.scope = term.scope;
 		decl.traverse(this, scope);
 		return false;
 	}
 
 	// TODO priority=3 group=stmt
 	public boolean visit(ContinueStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(DoStatement term, BlockScope scope) {
 		debug(term, scope);		
 		if (term.action instanceof Block) {
 			Block block = (Block) term.action;
 			if (block.statements != null) {
 				for (int i = 0; i < block.statements.length; i++) {
 					block.statements[i].traverse(this, scope);
 				}
 			}				
 		} else {
 			appendLine(term.action);
 		}
 		
 		if (term instanceof JmlDoStatement){
 			JmlDoStatement jmlDo = (JmlDoStatement)term;
 			JmlWhileStatement jmlwhl = new JmlWhileStatement(jmlDo.annotations, term.condition, term.action, term.sourceStart, term.sourceEnd);
 			jmlwhl.traverse(this, scope);
 		} else {
 			WhileStatement whl = new WhileStatement(term.condition, term.action, term.sourceStart, term.sourceEnd);  
 			whl.traverse(this, scope); 
 		}
 		
 		return false;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(JmlDoStatement term, BlockScope scope) {
 		debug(term, scope);	
 		visit ((DoStatement)term, scope);
 		return false;
 	}
 	
 	// TODO priority=3 group=lit
 	public boolean visit(DoubleLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append("0"); //FIXME! //$NON-NLS-1$
 		return true;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(EmptyStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(EqualExpression term, BlockScope scope) {
 		debug(term, scope);
 
 		append("("); //$NON-NLS-1$
 		term.left.traverse(this, scope);
 		
 		String out = ""; //$NON-NLS-1$
 		switch ((term.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
 			case OperatorIds.EQUAL_EQUAL:
 				out = "=="; //$NON-NLS-1$
 				break;
 			case OperatorIds.NOT_EQUAL:
 				out = "!="; //$NON-NLS-1$
 				break;
 			case OperatorIds.JML_EQUIV:
 				out = "<=>"; //$NON-NLS-1$
 				break;
 		}
 		append(SPACE + out + SPACE);
 		term.right.traverse(this, scope);
 		append(")"); //$NON-NLS-1$
 
 		return false;
 	}
 
 	// TODO priority=1 group=expr
 	public boolean visit(ExplicitConstructorCall term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=1 group=lit
 	public boolean visit(ExtendedStringLiteral term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=lit
 	public boolean visit(FalseLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append("false"); //$NON-NLS-1$
 		return true;
 	}
 
 	// priority=3 group=field
 	public boolean visit(FieldDeclaration term, MethodScope scope) {
 		debug(term, scope);
 		
 		append("var "); //$NON-NLS-1$
 		append(new String(term.binding.declaringClass.readableName()) + "."); //$NON-NLS-1$		
 		append(new String(term.name) + " : "); //$NON-NLS-1$
 		if (!term.isStatic())
 			append("[" + REF + "] "); //$NON-NLS-1$ //$NON-NLS-2$
 		term.type.traverse(this, scope);
 		appendLine(STMT_END);
 		// FIXME this will not work, Boogie requires that all assignments are done in a procedure
 		//if (term.isStatic())
 		//	variableInitialization(term, scope);
 		return false;
 	}
 
 	// TODO priority=3 group=field
 	public boolean visit(FieldReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=3 group=field
 	public boolean visit(FieldReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=3 group=lit
 	public boolean visit(FloatLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append("0"); //FIXME! //$NON-NLS-1$
 		return true;
 	}
 
 	// TODO priority=3 group=stmt
 	public boolean visit(ForeachStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	private Block makeBlockForLoop(ForStatement term) {
 		Block blk = new Block(0);
 		int len = 0;
 		if (term.action != null ) {
 			if (term.action instanceof Block) {
 				Block b = ((Block)term.action); 
 				if (!b.isEmptyBlock())
 					len = b.statements.length;
 			}else 
 				len = 1;
 			}
 			
 		
 		blk.statements = new Statement[len + term.increments.length];
 		for (int i = 0; i < len; i++) {
 			if (term.action instanceof Block && ((Block)term.action).statements != null ) {
 				blk.statements[i] = ((Block)term.action).statements[i];
 			}
 			else {
 				blk.statements[i] = term.action;
 			}
 		}
 		for (int i = 0; i < term.increments.length; i++) {
 			blk.statements[i+len] = term.increments[i];
 		}
 		blk.scope = term.scope;
 		return blk;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(IfStatement term, BlockScope scope) {
 		debug(term, scope);
 
 		append("if ("); //$NON-NLS-1$
 		term.condition.traverse(this, scope);
 		append(") "); //$NON-NLS-1$
 		
 		if (term.thenStatement != null) {
 			traverseBlock(toBlock(term.thenStatement, scope), scope);
 		}
 		if (term.elseStatement != null) {
 			append("else ");  //$NON-NLS-1$
 			traverseBlock(toBlock(term.elseStatement, scope), scope);
 		}
 
 		return false;
 	}
 
 	// priority=0 group=misc
 	public boolean visit(ImportReference term, CompilationUnitScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=1 group=misc 
 	public boolean visit(Initializer term, MethodScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=1 group=expr
 	public boolean visit(InstanceOfExpression term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=lit
 	public boolean visit(IntLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append(new String(term.source()));
 		return true;
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlAssertStatement term, BlockScope scope) {
 		debug(term, scope);
 		append("assert ", term.assertExpression); //$NON-NLS-1$
 		term.assertExpression.traverse(this, scope);
 		appendLine(STMT_END);
 		return false;
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlAssumeStatement term, BlockScope scope) {
 		debug(term, scope);
 		append("assume ", term.assertExpression); //$NON-NLS-1$
 		term.assertExpression.traverse(this, scope);
 		appendLine(STMT_END);
 		return false;
 	}
 	
 	// TODO priority=0 group=jml
 	public boolean visit(JmlCastExpressionWithoutType term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlEnsuresClause term, BlockScope scope) {
 		debug(term, scope);
 		// implemented in JmlMethodSpecification
 		return true;
 	}
 
 	// priority=0 group=jml
 	public boolean visit(JmlLoopAnnotations term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 	
 	// priority=3 group=jml
 	public boolean visit(JmlLoopInvariant term, BlockScope scope) {
 		debug(term, scope);
		append("invariant ", term.expr); //$NON-NLS-1$
 		return true;
 	}
 
 	// TODO priority=3 group=jml
 	public boolean visit(JmlLoopVariant term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlMethodSpecification term, ClassScope scope) {
 		debug(term, scope);
 
 		for (int i = 0; i < term.getSpecCases().length; i++) {
 			JmlSpecCase specCase = term.getSpecCases()[i];
 			
 			if (specCase.body.rest != null) {
 				JmlSpecCaseRestAsClauseSeq rest = (JmlSpecCaseRestAsClauseSeq)specCase.body.rest;
 				for (int j = 0; j < rest.clauses.length; j++) {
 					if (rest.clauses[j] instanceof JmlAssignableClause) {
 						JmlAssignableClause modifies = (JmlAssignableClause)rest.clauses[j];
 						append(SPACE);
 						append("modifies ", term); //$NON-NLS-1$
 						if (modifies.expr instanceof JmlStoreRefListExpression) {
 							JmlStoreRefListExpression stores = (JmlStoreRefListExpression)modifies.expr;
 							for (int x = 0; x < stores.exprList.length; x++) {
 								//stores.exprList[x].traverse(this, methodScope);
 								if (stores.exprList[x] instanceof SingleNameReference) {
 									append(scope.classScope().referenceContext.binding.readableName());
 									append("."); //$NON-NLS-1$
 									append(((SingleNameReference)stores.exprList[x]).binding.readableName());
 								}
 								if (x < stores.exprList.length - 1) { append(", "); } //$NON-NLS-1$
 							}
 						}
 						else if (modifies.expr instanceof JmlKeywordExpression) {
 							// TODO support JmlKeywordExpression
 							JmlKeywordExpression expr = (JmlKeywordExpression)modifies.expr;
 							append(expr.toString());
 						}
 						append(STMT_END);
 					}
 				}
 			}
 			if (specCase.getRequiresExpressions().size() > 0) {
 				List exprs = specCase.getRequiresExpressions();
 				for (int j = 0; j < exprs.size(); j++) {
 					append(SPACE);
 					append("requires ", (Expression)exprs.get(j)); //$NON-NLS-1$
 					Expression expr = (Expression)exprs.get(j);
 					expr.traverse(this, methodScope);
 					append(STMT_END); 
 				}
 			}
 			if (specCase.getEnsuresExpressions().size() > 0) {
 				List exprs = specCase.getEnsuresExpressions();
 				for (int j = 0; j < exprs.size(); j++) {
 					append(SPACE);
 					append("ensures ", (Expression)exprs.get(j)); //$NON-NLS-1$
 					Expression expr = (Expression)exprs.get(j);
 					expr.traverse(this, methodScope);
 					append(STMT_END);
 				}
 			}
 		}
 
 		return true;
 	}
 
 	// priority=2 group=jml
 	public boolean visit(JmlOldExpression term, BlockScope scope) {
 		debug(term, scope);
 		append("old("); //$NON-NLS-1$
 		term.expression.traverse(this, scope);
 		append(")"); //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlRequiresClause term, BlockScope scope) {
 		debug(term, scope);
 		// implemented in JmlMethodSpecification
 		return true;
 	}
 
 	// priority=1 group=jml
 	public boolean visit(JmlResultReference term, BlockScope scope) {
 		debug(term, scope);
 		append(RESULT);
 		return true;
 	}
 	
 	// priority=3 group=jml
 	public boolean visit (JmlWhileStatement term, BlockScope scope) {
 		debug(term, scope);
 		visit((WhileStatement)term, scope);
 		return false;
 		
 	}	
 	// priority=3 group=stmt
 	public boolean visit(WhileStatement term, BlockScope scope) {
 		debug(term, scope);
 		append("while ("); //$NON-NLS-1$
 		term.condition.traverse(this, scope);
 		append(") "); //$NON-NLS-1$
 		
 		if (term instanceof JmlWhileStatement ) {
 			JmlWhileStatement jmlWhile = (JmlWhileStatement) term;
 			jmlWhile.annotations.traverse(this, scope);
 			append(STMT_END + SPACE);
 		}
 		
 		traverseBlock(toBlock(term.action, scope), scope);
 		return false;
 	}
 	
 	// priority=3 group=jml
 	public boolean visit (JmlForStatement term, BlockScope scope) {
 		debug(term, scope);
 		for (int i = 0; i< term.initializations.length ; i++) {
 			term.initializations[i].traverse(this, scope);
 		}
 		
 		Block blk = makeBlockForLoop(term);
 		JmlWhileStatement w = new JmlWhileStatement(term.annotations, term.condition, blk, term.sourceStart, term.sourceEnd);
 		w.traverse(this, scope);
 
 		return false;		
 
 	}
 	
 	// priority=3 group=stmt
 	public boolean visit(ForStatement term, BlockScope scope) {
 		debug(term, scope);
 		for (int i = 0; i< term.initializations.length ; i++) {
 			term.initializations[i].traverse(this, scope);
 		}
 		
 		Block blk = makeBlockForLoop(term);
 		WhileStatement w = new WhileStatement(term.condition, 
 				blk, term.sourceStart, term.sourceEnd);
 		w.traverse(this, scope);
 		
 		return false;
 	}
 	
 	// priority=1 group=stmt
 	public boolean visit(LabeledStatement term, BlockScope scope) {
 		debug(term, scope);
 		appendLine(new String (term.label) + ":"); //$NON-NLS-1$
 		return true;
 	}
 
 	// priority=3 group=decl
 	public boolean visit(LocalDeclaration term, BlockScope scope) {
 		debug(term, scope);
 		variableInitialization(term, scope);
 		return false;
 	}
 	
 	// priority=3 group=lit
 	public boolean visit(LongLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append(new String(term.source()));
 		return true;
 	}
 
 	// priority=0 group=decl
 	public boolean visit(MarkerAnnotation term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=1 group=expr
 	public boolean visit(MemberValuePair term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=2 group=expr
 	public boolean visit(MessageSend term, BlockScope scope) {
 		debug(term, scope);
 		
 		SingleNameReference name = null;
 		TypeBinding binding = null;
 		if (term.receiver instanceof SingleNameReference) {
 			name = (SingleNameReference)term.receiver;
 			binding = scope.getType(name.token);
 		}
 
 		if (term.statementEnd != -1) {
 			append("call ", term); //$NON-NLS-1$
 		}
 		
 		append(term.binding.declaringClass.readableName());
 		append("." + new String(term.selector)); //$NON-NLS-1$
 		append(PAREN_OPEN);
 		
 		if (term.receiver instanceof ThisReference) {
 			append("this");  //$NON-NLS-1$
 		}
 		else if (term.receiver instanceof SingleNameReference) {
 			if (binding instanceof SourceTypeBinding) {
 				SourceTypeBinding sBinding = (SourceTypeBinding)binding;
 				if (!sBinding.isStatic()) {
 					term.receiver.traverse(this, scope);
 				}
 			}
 			else {
 				term.receiver.traverse(this, scope);
 			}
 		}
 
 		if (term.arguments != null) {
 			append(", "); //$NON-NLS-1$
 			for (int i = 0; i < term.arguments.length; i++) {
 				term.arguments[i].traverse(this, scope);
 				if (i < term.arguments.length - 1) append(", "); //$NON-NLS-1$
 			}
 		}
 		append(PAREN_CLOSE);
 		return false;
 	}
 	
 	public void endVisit(MessageSend term, BlockScope scope) {
 		if (term.statementEnd != -1) appendLine(STMT_END);
 	}
 
 	/**
 	 * Appends the proper boogie source and also finds all declarations using the {@link BoogieVariableDeclFinderVisitor}
 	 * to generate a list of local declarations to then visit using the {@link #addLocalDeclaration(LocalDeclaration, BlockScope, Block)}
 	 * method.
 	 */
 	// priority=3 group=decl
 	public boolean visit(JmlMethodDeclaration term, ClassScope scope) {
 		methodScope = term.scope; // used by #visit(JmlMethodSpecification, ClassScope)
 		
 		debug(term, scope);
 		
 		symbolTable = new BoogieSymbolTable();
 
 		String cls = new String(term.binding.declaringClass.readableName());
 		append("procedure "); //$NON-NLS-1$
 		append(cls + "."); //$NON-NLS-1$
 		append(new String(term.selector));
 		append(PAREN_OPEN);
 		if (!term.isStatic()) {
 			append("this: " + REF); //$NON-NLS-1$
 		}
 		if (term.arguments != null) {
 			if (!term.isStatic()) append(", ");  //$NON-NLS-1$
 			for (int i = 0; i < term.arguments.length; i++) {
 				term.arguments[i].traverse(this, scope);
 				if (i < term.arguments.length - 1) {
 					append(", "); //$NON-NLS-1$
 				}
 			}
 		}
 		append(PAREN_CLOSE);
 		if (term.binding.methodDeclaration instanceof JmlConstructorDeclaration) {
 			// do nothing
 		}
 		else if (term.returnType.resolveType(scope) != TypeBinding.VOID) {
 			append(" returns (" + RESULT + " : "); //$NON-NLS-1$ //$NON-NLS-2$
 			term.returnType.traverse(this, scope);
 			append(PAREN_CLOSE);
 		}
 		
 		// ensures & requires clause
 		if (term.getSpecification() != null) {
 			visit(term.getSpecification(), scope);
 		}
 		
 		appendLine(SPACE + BLOCK_OPEN);
 		output.increaseIndent();
 
 		BoogieVariableDeclFinderVisitor varDeclFinder = new BoogieVariableDeclFinderVisitor(symbolTable);
 		varDeclFinder.visit(term, scope);
 		ArrayList locals = varDeclFinder.getDecls(); 
 		if (locals != null) {
 			for (int i = 0; i < locals.size(); i++) {
 				Object[] data = (Object[])locals.get(i);
 				LocalDeclaration loc = (LocalDeclaration)data[0];
 				Block blk = (Block)data[1];
 				addLocalDeclaration(loc, term.scope, blk);				
 			}
 		}
 		
 		if (term.statements != null) {
 			for (int i = 0; i < term.statements.length; i++) {
 				term.statements[i].traverse(this, term.scope);
 			}
 		}
 
 		output.decreaseIndent();
 		append(BLOCK_CLOSE, term);
 		appendLine(""); //$NON-NLS-1$
 		
 		symbolTable = null;
 
 		return false;
 	}
 
 	/**
 	 * Used by the {@link #visit(JmlMethodDeclaration, ClassScope)} to add
 	 * variable declarations to the top of the procedure 
 	 */
 	private void addLocalDeclaration(LocalDeclaration term, BlockScope scope, Block block) {
 		String name = symbolTable.lookup(new String(term.name), block);
 		append("var " + name + " : "); //$NON-NLS-1$//$NON-NLS-2$
 		term.type.traverse(this, scope);
 		appendLine(STMT_END);
 		if (term.type instanceof ArrayTypeReference) {
 			appendLine("var " + name + ".length : int;"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	// priority=0 group=decl
 	public boolean visit(NormalAnnotation term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=lit
 	public boolean visit(NullLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append("null"); //$NON-NLS-1$
 		return true;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(OR_OR_Expression term, BlockScope scope) {
 		debug(term, scope);
 		append("("); //$NON-NLS-1$
 		term.left.traverse(this, scope);
 		append(" || "); //$NON-NLS-1$
 		term.right.traverse(this, scope);
 		append(")"); //$NON-NLS-1$
 		return false;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(ParameterizedQualifiedTypeReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(ParameterizedQualifiedTypeReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(ParameterizedSingleTypeReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(ParameterizedSingleTypeReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=2 group=expr
 	public boolean visit(PostfixExpression term, BlockScope scope) {
 		debug(term, scope);	
 		return false;
 	}
 	
 	// priority=2 group=expr
 	public void endVisit(PostfixExpression term, BlockScope scope) {
 		endVistiPrePostFixExpression(term, scope);
 	}
 	
 	// priority=2 group=expr
 	public void endVistiPrePostFixExpression(CompoundAssignment term, BlockScope scope) {
 		debug(term, scope);
 		IntLiteral i = new IntLiteral(new char[] { '1' }, term.sourceStart, term.sourceEnd, 1);
 		BinaryExpression expr = new BinaryExpression(term.lhs, i, term.operator);
 		Assignment a = new Assignment(term.lhs, expr, term.sourceStart);
 		a.traverse(this, scope);
 	}
 	
 	// priority=2 group=expr
 	public boolean visit(PrefixExpression term, BlockScope scope) {
 		debug(term, scope);
 		endVistiPrePostFixExpression(term, scope);
 		return false;
 	}
 	
 	// TODO priority=0 group=expr
 	public boolean visit(QualifiedAllocationExpression term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(QualifiedNameReference term, BlockScope scope) {
 		debug(term, scope);
 		String termName = new String(term.tokens[0]);
 		
 		// Look for field or resolve type fully
 		TypeBinding classBinding = scope.classScope().referenceType().binding;
 		FieldBinding fieldBind = scope.classScope().findField(classBinding, term.tokens[1], null, true);
 
 		if (fieldBind != null) {
 			append(new String(classBinding.readableName()) + "." + new String(term.tokens[1])); //$NON-NLS-1$ 
 			if (!fieldBind.isStatic()) { 
 				if (symbolTable != null) {
 					String symName = symbolTable.lookup(termName);
 					if (symName != null) {
 						append("[" + symName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 				}
 				else {
 					// TODO implement non local qualified references
 				}
 			}
 		}
 		else {
 			if (symbolTable != null) {
 				String symName = symbolTable.lookup(termName);
 				if (symName != null) {
 					append(symName);
 				}
 			}
 			else {
 				append(term.binding.readableName());
 			}
 			append("." + new String(scope.getType(term.tokens[1]).readableName())); //$NON-NLS-1$
 		}
 		return true;
 	}
 
 	// TODO priority=1 group=expr
 	public boolean visit(QualifiedNameReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(QualifiedSuperReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(QualifiedSuperReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(QualifiedThisReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(QualifiedThisReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(QualifiedTypeReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(QualifiedTypeReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(ReturnStatement term, BlockScope scope) {
 		debug(term, scope);
 		if (term.expression != null) {
 			char result[] = RESULT.toCharArray(); 
 			Assignment m = new Assignment(
 					new SingleTypeReference(result,  term.sourceStart),
 						term.expression, term.sourceEnd);
 			m.traverse(this, scope);
 		}
 		append("return", term.expression); //$NON-NLS-1$
 		appendLine(STMT_END); 
 		return false;
 	}
 
 	// priority=0 group=expr
 	public boolean visit(SingleMemberAnnotation term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(SingleNameReference term, BlockScope scope) {
 		debug(term, scope);
 		String termName = new String(term.token);
 		
 		// First look in symbol table if there is one (local vars)
 		if (symbolTable != null) {
 			String symName = symbolTable.lookup(termName);
 			if (symName != null) {
 				append(symName);
 				return true;
 			} 
 		}
 
 		// Look for field or resolve type fully
 		TypeBinding classBinding = scope.classScope().referenceType().binding;
 		FieldBinding fieldBind = scope.classScope().findField(classBinding, term.token, null, true);
 
 		if (fieldBind != null) {
 			append(new String(classBinding.readableName()) + "." + termName); //$NON-NLS-1$ 
 			if (!fieldBind.isStatic()) append("[this]"); //$NON-NLS-1$ 
 		}
 		else {
 			append(new String(scope.getType(term.token).readableName()));
 		}
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(SingleNameReference term, ClassScope scope) {
 		debug(term, scope);
 		append(symbolTable.lookup(new String(term.token)));
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(SingleTypeReference term, BlockScope scope) {
 		debug(term, scope);
 		
 		if (term.resolvedType == TypeBinding.BOOLEAN || term.token.equals(TypeConstants.BOOLEAN)) {
 			append("bool"); //$NON-NLS-1$
 			return true;
 		}
 		
 		if (term.resolvedType != null && !term.resolvedType.isBaseType()) {
 			declareType(new String(term.resolvedType.readableName()));
 			append(REF);
 		}
 		else {
 			append(new String(term.token));
 		}
 		
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(SingleTypeReference term, ClassScope scope) {
 		debug(term, scope);
 		
 		TypeBinding binding = term.resolveType(scope);
 		if (binding == TypeBinding.BOOLEAN) {
 			append("bool"); //$NON-NLS-1$
 		}
 		else {
 			if (term.resolvedType != null && !term.resolvedType.isBaseType()) {
 				declareType(new String(term.resolvedType.readableName()));
 				append(REF);
 			}
 			else {
 				append(new String(term.token));
 			}
 		}
 		
 		return true;
 	}
 
 	// priority=2 group=lit
 	public boolean visit(StringLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append(declareString(term.toString()));
 		return true;
 	}
 
 	// TODO priority=1 group=lit
 	public boolean visit(StringLiteralConcatenation term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=2 group=expr
 	public boolean visit(SuperReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=stmt
 	public boolean visit(SwitchStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=0 group=stmt
 	public boolean visit(SynchronizedStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=3 group=expr
 	public boolean visit(ThisReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=3 group=expr
 	public boolean visit(ThisReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=0 group=stmt
 	public boolean visit(ThrowStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=lit
 	public boolean visit(TrueLiteral term, BlockScope scope) {
 		debug(term, scope);
 		append("true"); //$NON-NLS-1$
 		return true;
 	}
 
 	// priority=0 group=stmt
 	public boolean visit(TryStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=2 group=decl
 	public boolean visit(TypeDeclaration term, BlockScope scope) {
 		declareType(new String(term.superclass.resolvedType.readableName()));
 		declareType(new String(term.binding.readableName()), new String(term.superclass.resolvedType.readableName()));
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=2 group=decl
 	public boolean visit(TypeDeclaration term, ClassScope scope) {
 		debug(term, scope);
 		declareType(new String(term.binding.readableName()));
 		return true;
 	}
 
 	// priority=2 group=decl
 	public boolean visit(TypeDeclaration term, CompilationUnitScope scope) {
 		debug(term, scope);
 		declareType(new String(term.binding.readableName()));
 		return true;
 	}
 
 	// TODO priority=2 group=expr
 	public boolean visit(TypeParameter term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=2 group=expr
 	public boolean visit(TypeParameter term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=3 group=expr
 	public boolean visit(UnaryExpression term, BlockScope scope) {
 		debug(term, scope);
 		
 		switch ((term.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
 			case OperatorIds.PLUS:
 				break;
 			default:
 				append (term.operatorToString());
 				break;
 		}
 		return true;
 	}
 
 	// priority=0 group=misc
 	public boolean visit(Wildcard term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=0 group=misc
 	public boolean visit(Wildcard term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 }
