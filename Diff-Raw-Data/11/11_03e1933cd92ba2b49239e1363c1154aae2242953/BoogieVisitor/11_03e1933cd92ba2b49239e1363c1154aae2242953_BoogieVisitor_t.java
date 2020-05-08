 package org.jmlspecs.jml4.boogie;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Stack;
 
 import org.eclipse.jdt.internal.compiler.ASTVisitor;
 import org.eclipse.jdt.internal.compiler.ast.*;
 import org.eclipse.jdt.internal.compiler.ast.Statement;
 import org.eclipse.jdt.internal.compiler.lookup.*;
 import org.jmlspecs.jml4.ast.*;
 import org.jmlspecs.jml4.boogie.ast.*;
 import org.jmlspecs.jml4.boogie.ast.AssertStatement;
 import org.jmlspecs.jml4.boogie.ast.Assignment;
 import org.jmlspecs.jml4.boogie.ast.BinaryExpression;
 import org.jmlspecs.jml4.boogie.ast.BreakStatement;
 import org.jmlspecs.jml4.boogie.ast.EmptyStatement;
 import org.jmlspecs.jml4.boogie.ast.Expression;
 import org.jmlspecs.jml4.boogie.ast.IfStatement;
 import org.jmlspecs.jml4.boogie.ast.ReturnStatement;
 import org.jmlspecs.jml4.boogie.ast.Scope;
 import org.jmlspecs.jml4.boogie.ast.TypeDeclaration;
 import org.jmlspecs.jml4.boogie.ast.TypeReference;
 import org.jmlspecs.jml4.boogie.ast.IntLiteral;
 import org.jmlspecs.jml4.boogie.ast.UnaryExpression;
 import org.jmlspecs.jml4.boogie.ast.WhileStatement;
 
 public class BoogieVisitor extends ASTVisitor {
 	private static final boolean DEBUG = true;
 	
 	private Scope boogieScope = null;
 	private BoogieNode result = null;
 	private Stack statementLists = new Stack();
 	private ArrayList postStatements = new ArrayList();
 
 	private BlockScope methodScope;
 	private VariableReference methodCallAssignmentVar = null;
 
 	private int stringPoolValue = 0;
 	private Hashtable stringPool = new Hashtable();
 
 	public Program visit(CompilationUnitDeclaration unit, Program program) {
 		boogieScope = program;
 		unit.traverse(this, unit.scope);
 		return program;
 	}
 	
 	public static Program visit(CompilationUnitDeclaration unit) {
 		Program program = new Program();
 		return new BoogieVisitor().visit(unit, program);
 	}
 
 	public static BoogieSource visitBuffer(CompilationUnitDeclaration unit) {
 		BoogieSource out = new BoogieSource();
 		visit(unit).toBuffer(out);
 		return out;
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
 
 	private BoogieNode result() {
 		return result;
 	}
 	
 	private Expression resultExpr() {
 		return (Expression)result;
 	}
 	
 	private ArrayList getStatementList() {
 		return (ArrayList)statementLists.peek();
 	}
 	
 	private void pushStatementList(ArrayList list) {
 		statementLists.push(list);
 	}
 	
 	private void popStatementList() {
 		statementLists.pop();
 	}
 	
 	private void traverseStatements(ArrayList stmtList, ASTNode[] statements, BlockScope scope) {
 		if (statements == null) return;
 		if (stmtList != null) pushStatementList(stmtList);
 		for (int i = 0; i < statements.length; i++) {
 			statements[i].traverse(this, scope);
 			getStatementList().addAll(postStatements);
 			postStatements.clear();
 		}
 		if (stmtList != null) popStatementList();
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
 	
 	private String getTypeName(TypeBinding typeBinding) {
 		if (typeBinding == TypeBinding.BOOLEAN) {
 			return "bool"; //$NON-NLS-1$
 		}
 		if (typeBinding.isArrayType()) {
 			ArrayBinding arrayBinding = (ArrayBinding)typeBinding;
 			return getTypeName(arrayBinding.leafComponentType());
 		}
 		
 		return new String(typeBinding.readableName());
 	}
 	
 	private TypeReference declareType(String type) {
 		return declareType(type, null);
 	}
 	
 	private TypeReference declareType(String type, String superType) {
 		Program program = boogieScope.getProgramScope();
 		TypeReference typeRef = new TypeReference(type, null, program);
 		TypeReference superTypeRef = superType == null ? null : new TypeReference(superType, null, program);
 		program.addType(new TypeDeclaration(typeRef, superTypeRef, program));
 		return typeRef;
 	}
 
 	private VariableReference declareString(String key) {
 		Program program = boogieScope.getProgramScope();
 		TypeReference typeRef = declareType("java.lang.String"); //$NON-NLS-1$
 		VariableReference ref = (VariableReference)stringPool.get(key);
 		if (ref == null) {
 			String name = "$string_lit_" + stringPoolValue++; //$NON-NLS-1$
 			ref = new VariableReference(name, null, program);
 			VariableDeclaration decl = new VariableDeclaration(ref, typeRef, true, program);
 			decl.setUnique(true);
 			stringPool.put(key, ref);
 			program.addVariable(decl);
 		}
 		return ref;
 	}
 	
 	private void variableInitialization(LocalDeclaration term, BlockScope scope) {
 		TypeBinding tb = term.type.resolvedType;
 		org.eclipse.jdt.internal.compiler.ast.Expression init = term.initialization;
 		if (init == null) {
 			if (tb == TypeBinding.INT || tb == TypeBinding.LONG) {
 					init = new org.eclipse.jdt.internal.compiler.ast.IntLiteral(new char[]{'0'}, 
 							term.sourceStart, term.sourceEnd, 0);
 			}
 			else if (tb == TypeBinding.BOOLEAN) {
 				init = new FalseLiteral(term.sourceStart, term.sourceEnd);
 			}
 			else {
 				init = new NullLiteral(term.sourceStart, term.sourceEnd);
 			}
 		}
 		
 		SingleNameReference sn = new SingleNameReference(term.name, term.sourceStart);
 		org.eclipse.jdt.internal.compiler.ast.Assignment a = 
 			new org.eclipse.jdt.internal.compiler.ast.Assignment(sn, init, term.sourceEnd);
 		sn.binding = term.binding;
 		sn.actualReceiverType = term.type.resolvedType;
 		sn.resolvedType = tb;
 		a.resolvedType = tb;
 		a.statementEnd = 0;
 		a.traverse(this, scope);
 	}
 
 	// priority=2 group=expr
 	public boolean visit(AllocationExpression term, BlockScope scope) {
 		debug(term, scope);
 		
 		String name = new String(term.binding.declaringClass.readableName()) + 
 			"." + new String(term.type.getLastToken()); //$NON-NLS-1$ 
 		CallStatement call = new CallStatement(name, new Expression[] { 
 				methodCallAssignmentVar }, null, term, boogieScope);
 		if (term.arguments != null) {
 			for (int i = 0; i < term.arguments.length; i++) {
 				term.arguments[i].traverse(this, scope);
 				call.getArguments().add(resultExpr());
 			}
 		}
 		
 		result = call;
 		getStatementList().add(result());
 		
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(AND_AND_Expression term, BlockScope scope) {
 		debug(term, scope);
 		term.left.traverse(this, scope);
 		Expression left = resultExpr();
 		term.right.traverse(this, scope);
 		Expression right = resultExpr();
		result = new BinaryExpression(left, "&&", right, term, boogieScope); //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(Argument term, BlockScope scope) {
 		debug(term, scope);
 		String name = new String(term.name);
 		VariableReference var = new VariableReference(name, term, boogieScope);
 		term.type.traverse(this, scope);
 		result = new VariableDeclaration(var, (TypeReference)result, boogieScope);
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(Argument term, ClassScope scope) {
 		debug(term, scope);
 		String name = new String(term.name);
 		VariableReference var = new VariableReference(name, term, boogieScope);
 		term.type.traverse(this, scope);
 		result = new VariableDeclaration(var, (TypeReference)result, boogieScope);
 		return false;
 	}
 	
 	private void defineArrayLength(org.eclipse.jdt.internal.compiler.ast.Expression lhs, BlockScope scope, int size) {
 		if (lhs instanceof JmlSingleNameReference && ((JmlSingleNameReference)lhs).binding instanceof FieldBinding) return; //FIXME
 		lhs.traverse(this, scope);
 		VariableReference ref = (VariableReference)result();
 		ref = new VariableLengthReference(ref.getName(), ref.getJavaNode(), ref.getScope()); 
 		result = new Assignment(ref, new IntLiteral(size, null, boogieScope), null, boogieScope.getProcedureScope());
 		getStatementList().add(result);
 	}
 	
 	private void initializeArray(org.eclipse.jdt.internal.compiler.ast.Expression lhs, org.eclipse.jdt.internal.compiler.ast.Expression[] expressions, BlockScope scope) {
 		for (int i = 0; i < expressions.length; i++) {
 			org.eclipse.jdt.internal.compiler.ast.Assignment asn = 
 				new org.eclipse.jdt.internal.compiler.ast.Assignment(
 					new ArrayReference(lhs, 
 						new org.eclipse.jdt.internal.compiler.ast.IntLiteral(
 								new Integer(i).toString().toCharArray(), 0, 0, i)),
 					expressions[i],
 					lhs.sourceEnd);
 			asn.traverse(this, scope);
 		}
 		defineArrayLength(lhs, scope, expressions.length);		
 	}
 
 	// priority=1 group=expr
 	public boolean visit(ArrayAllocationExpression term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(ArrayInitializer term, BlockScope scope) {
 		debug(term, scope);
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
 		VariableReference receiver = (VariableReference)result();
 		term.position.traverse(this, scope);
 		Expression pos = resultExpr();
 		if (receiver instanceof MapVariableReference) {
 			((MapVariableReference)receiver).getMapKeys().add(pos);
 			result = receiver;
 		}
 		else {
 			result = new MapVariableReference(receiver.getName(), new Expression[] { pos }, term, boogieScope);
 		}
 		
 		return false;
 	}
 
 	private MapTypeReference getArrayReference(ArrayTypeReference term) {
 		MapTypeReference ref = new MapTypeReference(getTypeName(term.resolvedType), null, term, boogieScope); 
 		for (int i = 0; i < term.dimensions; i++) {
 			ref.getMapTypes().add(new TypeReference("int", term, boogieScope)); //$NON-NLS-1$
 		}
 
 		return ref;
 	}
 	
 	// priority=2 group=array
 	public boolean visit(ArrayTypeReference term, BlockScope scope) {
 		debug(term, scope);
 		result = getArrayReference(term);
 		return false;
 	}
 
 	// priority=2 group=array
 	public boolean visit(ArrayTypeReference term, ClassScope scope) {
 		debug(term, scope);
 		result = getArrayReference(term);
 		return false;
 	}
 
 	// priority=3 group=misc
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.AssertStatement term, BlockScope scope) {
 		debug(term, scope);
 		JmlAssertStatement stmt = new JmlAssertStatement("assert", term.assertExpression, term.sourceStart); //$NON-NLS-1$
 		stmt.traverse(this, scope);
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.Assignment term, BlockScope scope) {
 		debug(term, scope);
 		
 		if (term.expression instanceof MessageSend || 
 				term.expression instanceof AllocationExpression) {
 			// method calling has its own assignment mechanism
 			// so just use that
 			VariableReference oldAssign = methodCallAssignmentVar;
 			term.lhs.traverse(this, scope);
 			methodCallAssignmentVar = (VariableReference)result();
 			term.expression.traverse(this, scope);
 			methodCallAssignmentVar = oldAssign;
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
 			else if (alloc.dimensions[0] instanceof org.eclipse.jdt.internal.compiler.ast.IntLiteral) {
 				int size = ((org.eclipse.jdt.internal.compiler.ast.IntLiteral)alloc.dimensions[0]).value;
 				org.eclipse.jdt.internal.compiler.ast.Expression[] exprs = 
 					new org.eclipse.jdt.internal.compiler.ast.Expression[size];
 				if (alloc.type.resolvedType == TypeBinding.INT || alloc.type.resolvedType == TypeBinding.LONG) {
 					for (int i = 0; i < size; i++) {
 						exprs[i] = new org.eclipse.jdt.internal.compiler.ast.IntLiteral(new char[]{'0'}, 0, 0, 0);
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
 
 		term.lhs.traverse(this, scope);
 		VariableReference ref = (VariableReference)result();
 		term.expression.traverse(this, scope);
 		Expression expr = resultExpr();
 	
 		Assignment assign = new Assignment(ref, expr, term, boogieScope.getProcedureScope());
 		if (term.statementEnd != -1) { // assignment is statement
 			result = assign;
 			getStatementList().add(result);
 		}
 		else { // assignment is expression, add assignment statement and return variable
 			getStatementList().add(assign);
 			result = ref;
 		}
 		
 		return false;
 	}
 
 	// priority=2 group=expr
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.BinaryExpression term, BlockScope scope) {
 		debug(term, scope);
 		String out = ""; //$NON-NLS-1$
 		Expression left, right;
 		
 		term.left.traverse(this, scope);
 		left = resultExpr();
 		term.right.traverse(this, scope);
 		right = resultExpr();
 
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
 		
 		result = new BinaryExpression(left, out, right, term, boogieScope);
 		return false;
 	}
 	
 	/**
 	 * Adds a RemoveLocal statement (which should be factored out in the future)
 	 * to remove the local when the block ends during buffer output phase. This
 	 * allows multiple block local variables to be used in one Boogie procedure,
 	 * which does not allow for block locals.
 	 */
 	private void removeLocals(BlockScope scope) {
 		for (int i = 0; i < scope.locals.length; i++) {
 			String name = new String(scope.locals[i].name);
 			VariableReference ref = new VariableReference(name, null, boogieScope);
 			getStatementList().add(new RemoveLocal(ref, boogieScope.getProcedureScope()));
 		}
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(Block term, BlockScope scope) {
 		debug(term, scope);
 		traverseStatements(null, term.statements, scope);
 		removeLocals(term.scope); // FIXME this should be factored out!
 		result = null;
 		return false;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.BreakStatement term, BlockScope scope) {
 		debug(term, scope);
 		result = new BreakStatement(term.label != null ? new String(term.label) : null, term, boogieScope); 
 		getStatementList().add(result);
 		return false;
 	}
 
 	// TODO priority=0 group=stmt
 	public boolean visit(CaseStatement term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(CastExpression term, BlockScope scope) {
 		debug(term, scope);
 		term.expression.traverse(this, scope);
 		return false;
 	}
 
 	// priority=2 group=lit
 	public boolean visit(CharLiteral term, BlockScope scope) {
 		debug(term, scope);
 		result = new IntLiteral(term.constant.intValue(), term, boogieScope);
 		return false;
 	}
 
 	// priority=3 group=decl
 	public boolean visit(CompilationUnitDeclaration term, CompilationUnitScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=2 group=expr
 	public boolean visit(CompoundAssignment term, BlockScope scope) {
 		debug(term, scope);
 		extractCompoundAssignment(term, scope);
 		return false;
 	}
 	
 	private VariableReference extractConditionalExpression(ConditionalExpression term) {
 		// add variable declaration to scope
 		String varName = "$call_" + boogieScope.getProcedureScope().getLocals().size(); //$NON-NLS-1$
 		TypeReference type = new TypeReference(getTypeName(term.resolvedType), term, boogieScope);
 		VariableReference ref = new VariableReference(varName, term, boogieScope);
 		VariableDeclaration decl = new VariableDeclaration(ref, type, boogieScope);
 		boogieScope.addVariable(decl);
 		return ref;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(ConditionalExpression term, BlockScope scope) {
 		debug(term, scope);
 		// TODO implement for program scope
 		if (!(boogieScope instanceof Procedure)) return false;
 		Procedure proc = boogieScope.getProcedureScope();
 		VariableReference ref = extractConditionalExpression(term);
 		Expression condition, trueExpr, falseExpr;
 		term.condition.traverse(this, scope);
 		condition = resultExpr();
 		term.valueIfTrue.traverse(this, scope);
 		trueExpr = resultExpr();
 		term.valueIfFalse.traverse(this, scope);
 		falseExpr = resultExpr();
 
 		IfStatement ifs = new IfStatement(condition, term, boogieScope);
 		Assignment trueAssign = new Assignment(ref, trueExpr, term, proc);
 		Assignment falseAssign = new Assignment(ref, falseExpr, term, proc);
 		ifs.getThenStatements().add(trueAssign);
 		ifs.getElseStatements().add(falseAssign);
 
 		getStatementList().add(ifs);
 		result = ref;
 		
 		
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
 		if (term.action != null) {
 			Block block = toBlock(term.action, scope);	
 			traverseStatements(null, block.statements, scope);
 		}
 		
 		if (term instanceof JmlDoStatement){
 			JmlDoStatement jmlDo = (JmlDoStatement)term;
 			JmlWhileStatement jmlwhl = new JmlWhileStatement(jmlDo.annotations, term.condition, term.action, term.sourceStart, term.sourceEnd);
 			jmlwhl.traverse(this, scope);
 		} else {
 			org.eclipse.jdt.internal.compiler.ast.WhileStatement whl = 
 				new org.eclipse.jdt.internal.compiler.ast.WhileStatement(
 						term.condition, term.action, term.sourceStart, term.sourceEnd);  
 			whl.traverse(this, scope); 
 		}
 		
 		return false;
 	}
 
 	// TODO priority=3 group=lit
 	public boolean visit(DoubleLiteral term, BlockScope scope) {
 		debug(term, scope);
 		result = new IntLiteral(0, term, boogieScope); // FIXME
 		return false;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.EmptyStatement term, BlockScope scope) {
 		debug(term, scope);
 		result = new EmptyStatement();
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(EqualExpression term, BlockScope scope) {
 		debug(term, scope);
 		Expression left, right;
 		term.left.traverse(this, scope);
 		left = resultExpr();
 		term.right.traverse(this, scope);
 		right = resultExpr();
 
 		String op = ""; //$NON-NLS-1$
 		switch ((term.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
 			case OperatorIds.EQUAL_EQUAL:
 				op = "=="; //$NON-NLS-1$
 				break;
 			case OperatorIds.NOT_EQUAL:
 				op = "!="; //$NON-NLS-1$
 				break;
 			case OperatorIds.JML_EQUIV:
 				op = "<=>"; //$NON-NLS-1$
 				break;
 		}
 		
 		result = new BinaryExpression(left, op, right, term, boogieScope);
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
 		result = new BooleanLiteral(false, term, boogieScope);
 		return false;
 	}
 
 	// priority=3 group=field
 	public boolean visit(FieldDeclaration term, MethodScope scope) {
 		debug(term, scope);
 		
 		String clsName = new String(term.binding.declaringClass.readableName());
 		String name = clsName + "." + new String(term.name);  //$NON-NLS-1$
 		term.type.traverse(this, scope);
 		TypeReference type = (TypeReference)result();
 		if (!term.isStatic()) {
 			TypeReference clsRef = new TypeReference(clsName, term, boogieScope);
 			if (type instanceof MapTypeReference) {
 				((MapTypeReference)type).getMapTypes().add(0, clsRef);
 			}
 			else {
 				type = new MapTypeReference(type.getTypeName(), 
 						new TypeReference[] { clsRef }, term, boogieScope);
 			}
 		}
 
 		result = new VariableDeclaration(new VariableReference(name, term, boogieScope), type, boogieScope);
 		boogieScope.getProgramScope().addVariable((VariableDeclaration)result);
 		return false;
 	}
 
 	// TODO priority=? group=field
 	public boolean visit(FieldReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=? group=field
 	public boolean visit(FieldReference term, ClassScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// TODO priority=3 group=lit
 	public boolean visit(FloatLiteral term, BlockScope scope) {
 		debug(term, scope);
 		result = new IntLiteral(0, term, boogieScope); // FIXME
 		return false;
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
 				if (!b.isEmptyBlock()) {
 					len = b.statements.length;
 				}
 			}
 			else { 
 				len = 1;
 			}
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
 	public boolean visit(ForStatement term, BlockScope scope) {
 		debug(term, scope);
 		for (int i = 0; i < term.initializations.length ; i++) {
 			term.initializations[i].traverse(this, scope);
 		}
 		
 		Block blk = makeBlockForLoop(term);
 		org.eclipse.jdt.internal.compiler.ast.WhileStatement w = 
 			new org.eclipse.jdt.internal.compiler.ast.WhileStatement(term.condition, 
 				blk, term.sourceStart, term.sourceEnd);
 		w.traverse(this, scope);
 		
 		return false;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.IfStatement term, BlockScope scope) {
 		debug(term, scope);
 		term.condition.traverse(this, scope);
 		Expression condition = resultExpr();
 		IfStatement ifs = new IfStatement(condition, term, boogieScope);
 		if (term.thenStatement != null) {
 			Block thenBlock = toBlock(term.thenStatement, scope);
 			traverseStatements(ifs.getThenStatements(), thenBlock.statements, scope);
 		}
 		if (term.elseStatement != null) {
 			Block elseBlock = toBlock(term.elseStatement, scope);
 			traverseStatements(ifs.getElseStatements(), elseBlock.statements, scope);
 		}
 		result = ifs;
 		getStatementList().add(result);
 		return false;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(InstanceOfExpression term, BlockScope scope) {
 		debug(term, scope);
 		term.expression.traverse(this, scope);
 		FunctionCall fn = new FunctionCall("$dtype", new Expression[]{ resultExpr() }, term.expression, boogieScope); //$NON-NLS-1$
 		TypeReference ref = new TypeReference(getTypeName(term.type.resolvedType), term, boogieScope);
 		result = new BinaryExpression(fn, "<:", new TokenLiteral(ref.getTypeName()), false, term, boogieScope); //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=3 group=lit
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.IntLiteral term, BlockScope scope) {
 		debug(term, scope);
 		result = new IntLiteral(term.value, term, boogieScope);
 		return false;
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlAssertStatement term, BlockScope scope) {
 		debug(term, scope);
 		term.assertExpression.traverse(this, scope);
 		result = new AssertStatement(resultExpr(), term.assertExpression, boogieScope.getProcedureScope());
 		getStatementList().add(result);
 		return false;
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlAssumeStatement term, BlockScope scope) {
 		debug(term, scope);
 		term.assertExpression.traverse(this, scope);
 		result = new AssumeStatement(resultExpr(), term.assertExpression, boogieScope.getProcedureScope());
 		getStatementList().add(result);
 		return false;
 	}
 
 	// priority=2 group=decl
 	public boolean visit(JmlConstructorDeclaration term, ClassScope scope) {
 		debug(term, scope);
 		
 		if (term.isDefaultConstructor() && (term.statements == null || term.statements.length == 0)) {
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
 
 	// priority=3 group=stmt
 	public boolean visit(JmlDoStatement term, BlockScope scope) {
 		debug(term, scope);	
 		visit((DoStatement)term, scope);
 		return false;
 	}
 	
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
 
 	// priority=0 group=jml
 	public boolean visit(JmlLoopAnnotations term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlLoopInvariant term, BlockScope scope) {
 		debug(term, scope);
 		term.expr.traverse(this, scope);
 		return false;
 	}
 
 	// TODO priority=3 group=jml
 	public boolean visit(JmlLoopVariant term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=decl
 	public boolean visit(JmlMethodDeclaration term, ClassScope scope) {
 		debug(term, scope);
 
 		methodScope = term.scope;
 		String cls = new String(term.binding.declaringClass.readableName());
 		String procName = cls + "." + new String(term.selector); //$NON-NLS-1$
 		
 		TypeReference ref = null;
 		if (term.binding.methodDeclaration instanceof JmlConstructorDeclaration) {
 			// do nothing
 		}
 		else if (term.returnType.resolvedType != TypeBinding.VOID) {
 			term.returnType.traverse(this, scope);
 			ref = (TypeReference)result();
 		}
 		
 		Procedure proc = new Procedure(procName, ref, term, (Program)boogieScope);
 		boogieScope.getProgramScope().getProcedures().add(proc);
 		boogieScope = proc;
 		
 		if (!term.isStatic()) {
 			VariableReference varRef = new VariableReference("this", term, boogieScope); //$NON-NLS-1$
 			TypeReference typeRef = new TypeReference(cls, term, boogieScope);
 			VariableDeclaration decl = new VariableDeclaration(varRef, typeRef, boogieScope);
 			proc.getArguments().add(decl);
 		}
 		if (term.arguments != null) {
 			for (int i = 0; i < term.arguments.length; i++) {
 				term.arguments[i].traverse(this, scope);
 				proc.getArguments().add(result());
 				proc.registerVariable((VariableDeclaration)result());
 			}
 		}
 		
 		// ensures & requires clause
 		if (term.getSpecification() != null) {
 			visit(term.getSpecification(), scope);
 		}
 
 		traverseStatements(proc.getStatements(), term.statements, term.scope);
 
 		boogieScope = proc.getProgramScope();
 		
 		return false;
 	}
 
 	private void getMethodModifies(JmlSpecCase specCase, ClassScope scope) {
 		if (specCase.body.rest != null) {
 			JmlSpecCaseRestAsClauseSeq rest = (JmlSpecCaseRestAsClauseSeq)specCase.body.rest;
 			for (int j = 0; j < rest.clauses.length; j++) {
 				if (rest.clauses[j] instanceof JmlAssignableClause) {
 					JmlAssignableClause modifies = (JmlAssignableClause)rest.clauses[j];
 					if (modifies.expr instanceof JmlStoreRefListExpression) {
 						JmlStoreRefListExpression stores = (JmlStoreRefListExpression)modifies.expr;
 						for (int x = 0; x < stores.exprList.length; x++) {
 							stores.exprList[x].traverse(this, scope);
 							boogieScope.getProcedureScope().getModifies().add(result());
 						}
 					}
 					else if (modifies.expr instanceof JmlKeywordExpression) {
 						// TODO support JmlKeywordExpression
 						// JmlKeywordExpression expr = (JmlKeywordExpression)modifies.expr;
 					}
 				}
 			}
 		}
 	}
 
 	private void getMethodContracts(List exprs, List contracts) {
 		for (int j = 0; j < exprs.size(); j++) {
 			org.eclipse.jdt.internal.compiler.ast.Expression expr = 
 				(org.eclipse.jdt.internal.compiler.ast.Expression)exprs.get(j);
 			expr.traverse(this, methodScope);
 			contracts.add(result()); 
 		}
 	}
 
 	// priority=3 group=jml
 	public boolean visit(JmlMethodSpecification term, ClassScope scope) {
 		debug(term, scope);
 		Procedure proc = boogieScope.getProcedureScope();
 
 		for (int i = 0; i < term.getSpecCases().length; i++) {
 			JmlSpecCase specCase = term.getSpecCases()[i];
 			getMethodModifies(specCase, scope);
 			getMethodContracts(specCase.getRequiresExpressions(), proc.getRequires());
 			getMethodContracts(specCase.getEnsuresExpressions(), proc.getEnsures());
 		}
 
 		return false;
 	}
 
 	// priority=2 group=jml
 	public boolean visit(JmlOldExpression term, BlockScope scope) {
 		debug(term, scope);
 		term.expression.traverse(this, scope);
 		result = new FunctionCall("old", new Expression[] { resultExpr() }, term, boogieScope); //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=1 group=jml
 	public boolean visit(JmlResultReference term, BlockScope scope) {
 		debug(term, scope);
 		result = new ResultReference(boogieScope.getProcedureScope());
 		return false;
 	}
 
 	public boolean visit(JmlWhileStatement term, BlockScope scope) {
 		debug(term, scope);
 		visit((org.eclipse.jdt.internal.compiler.ast.WhileStatement)term, scope);
 		WhileStatement whl = (WhileStatement)result();
 		if (term.annotations != null) {
 			// invariants
 			for (int i = 0; i < term.annotations.invariants.length; i++) {
 				term.annotations.invariants[i].traverse(this, scope);
 				whl.getInvariants().add(resultExpr());
 			}
 			
 			// TODO variants
 		}
 		result = whl;
 		return false;
 	}
 
 	// priority=1 group=stmt
 	public boolean visit(LabeledStatement term, BlockScope scope) {
 		debug(term, scope);
 		result = new LabelStatement(new String(term.label), term, boogieScope);
 		getStatementList().add(result());
 		
 		term.statement.traverse(this, scope);
 		
 		return false;
 	}
 
 	// priority=3 group=decl
 	public boolean visit(LocalDeclaration term, BlockScope scope) {
 		debug(term, scope);
 		
 		// add variable declaration to scope
 		term.type.traverse(this, scope);
 		TypeReference type = (TypeReference)result();
 		VariableReference ref = new VariableReference(new String(term.name), term, boogieScope);
 		VariableDeclaration decl = new VariableDeclaration(ref, type, boogieScope);
 		boogieScope.addVariable(decl);
 		
 		if (term.type instanceof ArrayTypeReference) {
 			TypeReference intType = new TypeReference("int", term.type, boogieScope); //$NON-NLS-1$
 			VariableReference lenRef = new VariableLengthReference(ref.getName(), term, boogieScope); 
 			VariableDeclaration lenDecl = new VariableDeclaration(lenRef, intType, boogieScope);
 			boogieScope.addVariable(lenDecl);
 			lenDecl.setShortName(null);
 		}
 
 		// initialization assignment
 		variableInitialization(term, scope);
 		result = result();
 		
 		return false;
 	}
 
 	// priority=3 group=lit
 	public boolean visit(LongLiteral term, BlockScope scope) {
 		debug(term, scope);
 		result = new IntLiteral(Integer.parseInt(term.toString()), term, boogieScope);
 		return false;
 	}
 	
	private VariableReference extractMethodExpression(MessageSend term, String procName, Expression[] args, BlockScope scope) {
 		// add variable declaration to scope
 		String varName = "$call_" + boogieScope.getProcedureScope().getLocals().size(); //$NON-NLS-1$
		TypeReference type = new TypeReference(getTypeName(term.binding.returnType), term, boogieScope);
 		VariableReference ref = new VariableReference(varName, term, boogieScope);
 		VariableDeclaration decl = new VariableDeclaration(ref, type, boogieScope);
 		boogieScope.addVariable(decl);
 		
 		// add the statement
 		CallStatement stmt = new CallStatement(procName, args, ref, term, boogieScope);
 		getStatementList().add(stmt);
 		
 		return ref;
 		
 	}
 
 	// priority=2 group=expr
 	public boolean visit(MessageSend term, BlockScope scope) {
 		debug(term, scope);
 		
 		Expression[] args = null;
 		String procName = new String(term.binding.declaringClass.readableName()) + "." + new String(term.selector); //$NON-NLS-1$
 		int staticOffset = term.binding.isStatic() ? 0 : 1;
 		if (term.arguments != null) {
 			args = new Expression[term.arguments.length + staticOffset];
 			term.receiver.traverse(this, scope);
 			args[0] = resultExpr();
 			for (int i = 0; i < term.arguments.length; i++) {
 				term.arguments[i].traverse(this, scope);
 				args[i + staticOffset] = resultExpr();
 			}
 		}
 		else if (staticOffset == 1) {
 			term.receiver.traverse(this, scope);
 			args = new Expression[] { resultExpr() };
 		}
 		
 		
 		if (term.statementEnd != -1 || methodCallAssignmentVar != null) {
 			result = new CallStatement(procName, args, methodCallAssignmentVar, term, boogieScope); 
 			getStatementList().add(result);
 		}
 		else { 
 			// method is an expression, pull it out to a statement
 			// and return the variable name as the expression
			result = extractMethodExpression(term, procName, args, scope);
 		}
 
 		return false;
 	}
 
 	// priority=3 group=lit
 	public boolean visit(NullLiteral term, BlockScope scope) {
 		debug(term, scope);
 		result = new TokenLiteral("null"); //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(OR_OR_Expression term, BlockScope scope) {
 		debug(term, scope);
 		term.left.traverse(this, scope);
 		Expression left = resultExpr();
 		term.right.traverse(this, scope);
 		Expression right = resultExpr();
 		result = new BinaryExpression(left, "||", right, term, boogieScope); //$NON-NLS-1$
 		return false;
 	}
 
 	private void extractCompoundAssignment(CompoundAssignment term, BlockScope scope) {
 		org.eclipse.jdt.internal.compiler.ast.BinaryExpression expr = 
 			new org.eclipse.jdt.internal.compiler.ast.BinaryExpression(term.lhs, term.expression, term.operator);
 		org.eclipse.jdt.internal.compiler.ast.Assignment a = new 
 			org.eclipse.jdt.internal.compiler.ast.Assignment(term.lhs, expr, term.sourceStart);
 		a.traverse(this, scope);
 	}
 
 	// priority=2 group=expr
 	public boolean visit(PostfixExpression term, BlockScope scope) {
 		debug(term, scope);
 		term.lhs.traverse(this, scope);
 		VariableReference lhs = (VariableReference)result();
 		term.expression.traverse(this, scope);
 		org.eclipse.jdt.internal.compiler.ast.BinaryExpression expr = 
 			new org.eclipse.jdt.internal.compiler.ast.BinaryExpression(term.lhs, term.expression, term.operator);
 		expr.traverse(this, scope);
 		postStatements.add(new Assignment(lhs, resultExpr(), term, boogieScope.getProcedureScope()));
 		result = lhs;
 		return false;
 	}
 
 	// priority=2 group=expr
 	public boolean visit(PrefixExpression term, BlockScope scope) {
 		debug(term, scope);
 		extractCompoundAssignment(term, scope);
 		return false;
 	}
 
 	// TODO priority=0 group=expr
 	public boolean visit(QualifiedAllocationExpression term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 	
 	private VariableReference getQualifiedVariableReference(QualifiedNameReference term, ClassScope scope) {
 		// Look for field or resolve type fully
 		TypeBinding classBinding = scope.referenceType().binding;
 		FieldBinding fieldBind = scope.findField(classBinding, term.tokens[1], null, true);
 		String termName = new String(term.tokens[1]);
 
 		if (fieldBind != null) {
 			String name = new String(classBinding.readableName()) + "." + termName; //$NON-NLS-1$
 			if (!fieldBind.isStatic()) {
 				return new MapVariableReference(name, new Expression[] { 
 						new VariableReference(new String(term.tokens[0]), term, boogieScope) }, term, boogieScope);
 			}
 			else {
 				return new VariableReference(name, term, boogieScope);
 				
 			}
 		}
 		else {
 			String name = new String(term.binding.readableName());
 			VariableDeclaration decl = boogieScope.lookupVariable(name);
 			if (decl != null) {
 				name = decl.getShortName() != null ? decl.getShortName() : decl.getName().getName();
 			}
 			name += "." + new String(scope.getType(term.tokens[1]).readableName()); //$NON-NLS-1$
 			return new VariableReference(name, term, boogieScope);
 		}
 	}
 
 	// priority=1 group=expr
 	public boolean visit(QualifiedNameReference term, BlockScope scope) {
 		debug(term, scope);
 		result = getQualifiedVariableReference(term, scope.classScope());
 		return false;
 	}
 
 	// priority=1 group=expr
 	public boolean visit(QualifiedNameReference term, ClassScope scope) {
 		debug(term, scope);
 		result = getQualifiedVariableReference(term, scope.classScope());
 		return false;
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
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.ReturnStatement term, BlockScope scope) {
 		debug(term, scope);
 		Procedure proc = boogieScope.getProcedureScope();
 		term.expression.traverse(this, scope);
 		getStatementList().add(new Assignment(new ResultReference(proc), resultExpr(), term.expression, proc));
 		result = new ReturnStatement(term.expression, boogieScope);
 		postStatements.add(result);
 		return false;
 	}
 	
 	private VariableReference getVariableReference(SingleNameReference term, ClassScope scope) {
 		// Look for field or resolve type fully
 		TypeBinding classBinding = scope.referenceType().binding;
 		FieldBinding fieldBind = scope.findField(classBinding, term.token, null, true);
 		String termName = new String(term.token);
 
 		if (fieldBind != null) {
 			String name = new String(classBinding.readableName()) + "." + termName; //$NON-NLS-1$
 			if (!fieldBind.isStatic()) {
 				return new MapVariableReference(name, new Expression[] { 
 						new VariableReference("this", term, boogieScope) }, term, boogieScope); //$NON-NLS-1$
 			}
 			else {
 				return new VariableReference(name, term, boogieScope);
 				
 			}
 		}
 		else {
 			return new VariableReference(new String(term.binding.readableName()), term, boogieScope);
 		}
 		
 	}
 
 	// priority=3 group=expr
 	public boolean visit(SingleNameReference term, BlockScope scope) {
 		debug(term, scope);
 		result = getVariableReference(term, scope.classScope());
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(SingleNameReference term, ClassScope scope) {
 		debug(term, scope);
 		result = getVariableReference(term, scope);
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(SingleTypeReference term, BlockScope scope) {
 		debug(term, scope);
 		result = new TypeReference(getTypeName(term.resolvedType), term, boogieScope);
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(SingleTypeReference term, ClassScope scope) {
 		debug(term, scope);
 		result = new TypeReference(getTypeName(term.resolvedType), term, boogieScope);
 		return false;
 	}
 
 	// priority=2 group=lit
 	public boolean visit(StringLiteral term, BlockScope scope) {
 		debug(term, scope);
 		result = declareString(term.toString());
 		return false;
 	}
 
 	// TODO priority=2 group=expr
 	public boolean visit(SuperReference term, BlockScope scope) {
 		debug(term, scope);
 		return true;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(ThisReference term, BlockScope scope) {
 		debug(term, scope);
 		result = new VariableReference("this", term, boogieScope); //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=3 group=expr
 	public boolean visit(ThisReference term, ClassScope scope) {
 		debug(term, scope);
 		result = new VariableReference("this", term, boogieScope); //$NON-NLS-1$
 		return false;
 	}
 
 	// priority=3 group=lit
 	public boolean visit(TrueLiteral term, BlockScope scope) {
 		debug(term, scope);
 		result = new BooleanLiteral(true, term, boogieScope);
 		return false;
 	}
 
 	// priority=2 group=decl
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration term, BlockScope scope) {
 		debug(term, scope);
 		if (term.superclass != null) {
 			declareType(new String(term.binding.readableName()), new String(term.superclass.resolvedType.readableName()));
 		}
 		else {
 			declareType(new String(term.binding.readableName()));
 		}
 		return true;
 	}
 
 	// priority=2 group=decl
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration term, ClassScope scope) {
 		debug(term, scope);
 		if (term.superclass != null) {
 			declareType(new String(term.binding.readableName()), new String(term.superclass.resolvedType.readableName()));
 		}
 		else {
 			declareType(new String(term.binding.readableName()));
 		}
 		return true;
 	}
 
 	// priority=2 group=decl
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration term, CompilationUnitScope scope) {
 		debug(term, scope);
 		if (term.superclass != null) {
 			declareType(new String(term.binding.readableName()), new String(term.superclass.resolvedType.readableName()));
 		}
 		else {
 			declareType(new String(term.binding.readableName()));
 		}
 		return true;
 	}
 
 	// TODO priority=3 group=expr
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.UnaryExpression term, BlockScope scope) {
 		debug(term, scope);
 		String operator = null;
 		switch ((term.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) {
 		case OperatorIds.PLUS:
 			break;
 		default:
 			operator = term.operatorToString();
 			break;
 		}
 
 		term.expression.traverse(this, scope);
 		if (operator != null) {
 			result = new UnaryExpression(resultExpr(), operator, term, boogieScope);
 		}
 		return false;
 	}
 
 	// priority=3 group=stmt
 	public boolean visit(org.eclipse.jdt.internal.compiler.ast.WhileStatement term, BlockScope scope) {
 		debug(term, scope);
 		term.condition.traverse(this, scope);
 		WhileStatement whl = new WhileStatement(resultExpr(), term, boogieScope);
 		if (term.action != null) {
 			Block stmts = toBlock(term.action, scope);
 			traverseStatements(whl.getStatements(), stmts.statements, scope);
 		}
 		result = whl;
 		getStatementList().add(result);
 		return false;
 	}
 }
