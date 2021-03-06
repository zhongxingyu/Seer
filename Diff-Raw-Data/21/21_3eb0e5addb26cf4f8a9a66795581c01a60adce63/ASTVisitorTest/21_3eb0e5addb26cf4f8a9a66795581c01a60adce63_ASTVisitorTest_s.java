 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.aspectj.tools.ajc;
 
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 
 import org.aspectj.org.eclipse.jdt.core.dom.AST;
 import org.aspectj.org.eclipse.jdt.core.dom.ASTParser;
 import org.aspectj.org.eclipse.jdt.core.dom.AfterAdviceDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.AfterReturningAdviceDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.AfterThrowingAdviceDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.AjASTVisitor;
 import org.aspectj.org.eclipse.jdt.core.dom.AjTypeDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.AroundAdviceDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.Assignment;
 import org.aspectj.org.eclipse.jdt.core.dom.BeforeAdviceDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.Block;
 import org.aspectj.org.eclipse.jdt.core.dom.BlockComment;
 import org.aspectj.org.eclipse.jdt.core.dom.BodyDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.CompilationUnit;
 import org.aspectj.org.eclipse.jdt.core.dom.DeclareDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.ExpressionStatement;
 import org.aspectj.org.eclipse.jdt.core.dom.FieldAccess;
 import org.aspectj.org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.InfixExpression;
 import org.aspectj.org.eclipse.jdt.core.dom.Initializer;
 import org.aspectj.org.eclipse.jdt.core.dom.InterTypeFieldDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.InterTypeMethodDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.MethodInvocation;
 import org.aspectj.org.eclipse.jdt.core.dom.NumberLiteral;
 import org.aspectj.org.eclipse.jdt.core.dom.PointcutDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.PrimitiveType;
 import org.aspectj.org.eclipse.jdt.core.dom.QualifiedName;
 import org.aspectj.org.eclipse.jdt.core.dom.SimpleName;
 import org.aspectj.org.eclipse.jdt.core.dom.StringLiteral;
 import org.aspectj.org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.VariableDeclaration;
 import org.aspectj.org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 import org.aspectj.weaver.patterns.DeclareAnnotation;
 import org.aspectj.weaver.patterns.DeclareErrorOrWarning;
 import org.aspectj.weaver.patterns.DeclareParents;
 import org.aspectj.weaver.patterns.DeclarePrecedence;
 import org.aspectj.weaver.patterns.DeclareSoft;
 
 public class ASTVisitorTest extends TestCase {
 	
     // from bug 110465 - will currently break because of casts
 	public void testAspectWithITD() {
 		check("aspect A{ public void B.x(){} }",
 			  "(compilationUnit(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block))))");
 	}
 	
 	public void testAspectWithCommentThenITD() {
 		check("aspect A{ /** */ public void B.x(){} }",
 			  "(compilationUnit(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block))))");
 	}
 	
 	public void testAspectWithCommentThenPointcut() {
 		check("aspect A{ /** */ pointcut x(); }","(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 				
 	// original tests
 	public void testAnInterface() {
 		check("interface AnInterface{}","(compilationUnit(interface(simpleName)))");
 	}
 	public void testAnAspect() {
 		check("aspect AnAspect{}","(compilationUnit(aspect(simpleName)))");
 	}
 	public void testPointcutInClass() {
 		check("class A {pointcut a();}",
 			"(compilationUnit(class(simpleName)(pointcut(simpleName))))");
 	}
 	public void testPointcutInAspect() {
 		check("aspect A {pointcut a();}","(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testAroundAdvice() {
 		check("aspect A {pointcut a();void around():a(){}}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(aroundAdvice(primitiveType)(simpleName)(block))))");
 	}
 	public void testAroundAdviceWithProceed() {
 		// ajh02: currently proceed calls are just normal method calls
 		// could add a special AST node for them if anyone would like
 		check("aspect A {pointcut a();void around():a(){proceed();}}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(aroundAdvice(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName)))))))");
 	}
 	public void testBeforeAdvice() {
 		check("aspect A {pointcut a();before():a(){}}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(beforeAdvice(simpleName)(block))))");
 	}
 	public void testAfterAdvice() {
 		check("aspect A {pointcut a();after():a(){}}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(afterAdvice(simpleName)(block))))");
 	}
 	public void testAfterThrowingAdvice() {
 		check("aspect A {pointcut a();after()throwing(Exception e):a(){} }",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(afterThrowingAdvice(simpleName)(simpleName)(simpleName)(block))))");
 	}
 	public void testAfterReturningAdvice() {
 		check("aspect A {pointcut a();after()returning(Object o):a(){}}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(afterReturningAdvice(simpleName)(simpleName)(simpleName)(block))))");
 	}
 	public void testMethodWithStatements() {
 		check("class A {void a(){System.out.println(\"a\");}}",
 				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(qualifiedName(simpleName)(simpleName))(simpleName)(stringLiteral)))))))");
 	}
 	public void testAdviceWithStatements() {
 		check("aspect A {pointcut a();before():a(){System.out.println(\"a\");}}",
 		"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(beforeAdvice(simpleName)(block(expressionStatement(methodInvocation(qualifiedName(simpleName)(simpleName))(simpleName)(stringLiteral)))))))");
 	}
 	public void testPointcutInAPointcut() {
 		check("aspect A {pointcut a();pointcut b();pointcut c(): a() && b();}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(pointcut(simpleName))(pointcut(simpleName)(simpleName)(simpleName))))");
 	}
 	
 	public void testCallPointcut(){
 		check("aspect A {pointcut a(): call(* *.*(..));}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testExecutionPointcut(){
 		check("aspect A {pointcut a(): execution(* *.*(..));}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testGetPointcut(){
 		check("aspect A {pointcut a(): get(* *.*);}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testSetPointcut(){
 		check("aspect A {pointcut a(): set(* *.*);}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testHandlerPointcut(){
 		check("aspect A {pointcut a(): handler(Exception+);}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testStaticInitializationPointcut(){
 		check("aspect A {pointcut a(): staticinitialization(Object+);}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testInitializationPointcut(){
 		check("aspect A {pointcut a(): initialization(public Object+.new());}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testPreInitializationPointcut(){
 		check("aspect A {pointcut a(): preinitialization(public Object+.new());}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	public void testAdviceExecutionPointcut(){
 		check("aspect A {pointcut a(): adviceexecution();}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))))");
 	}
 	
 	public void testFieldITD(){
 		check("class A {}aspect B {int A.a;}",
 				"(compilationUnit(class(simpleName))(aspect(simpleName)(fieldITD(primitiveType)(simpleName))))");
 	}
 	public void testMethodITD(){
 		check("class A {}aspect B {void A.a(){}}",
 				"(compilationUnit(class(simpleName))(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block))))");
 	}
 	public void testConstructorITD(){
 		check("class A {}aspect B {A.new(){}}",
 				"(compilationUnit(class(simpleName))(aspect(simpleName)(constructorITD(primitiveType)(simpleName)(block))))");
 	}
 	
 	public void testInitializedField(){
 		check("class A{int a = 1;}",
 				"(compilationUnit(class(simpleName)(field(primitiveType)(simpleName)(numberLiteral))))");
 	}
 	public void testMethodITDWithStatements(){
 		check("class A {}aspect B {void A.a(){System.out.println(\"a\");}}",
 				"(compilationUnit(class(simpleName))(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(qualifiedName(simpleName)(simpleName))(simpleName)(stringLiteral)))))))");
 	}
 	public void testConstructorITDWithStatements(){
 		check("class A {}aspect B {A.new(){System.out.println(\"a\");}}",
 				"(compilationUnit(class(simpleName))(aspect(simpleName)(constructorITD(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(qualifiedName(simpleName)(simpleName))(simpleName)(stringLiteral)))))))");
 	}
 	public void testInitializedFieldITD(){
 		check("class A {}aspect B {int A.a = 1;}",
 				"(compilationUnit(class(simpleName))(aspect(simpleName)(fieldITD(primitiveType)(simpleName)(numberLiteral))))");
 	}
 	
 	public void testMethodBeingCalled(){
 		check("class A {void a(){}void b(){a();}}",
 				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block))(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName)))))))");
 	}
 	public void testFieldBeingCalled(){
 		check("class A {int a;void b(){int c = a;a = c;}}",
 				"(compilationUnit(class(simpleName)(field(primitiveType)(simpleName))(method(primitiveType)(simpleName)(block(variableDeclarationStatement(primitiveType)(simpleName)(simpleName))(expressionStatement(assignment(simpleName)(simpleName)))))))");
 	}
 	public void testConstructorBeingCalled(){
 		check("class A {A(){}void b(){A();}}",
 				"(compilationUnit(class(simpleName)(constructor(primitiveType)(simpleName)(block))(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName)))))))");
 	}
 	public void testMethodITDBeingCalled(){
 		check("class A {void b(){a();}}aspect B {void A.a(){}}",
 				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName))))))(aspect(simpleName)(methodITD(primitiveType)(simpleName)(block))))");
 	}
 	public void testFieldITDBeingCalled(){
 		check("class A {void b(){int c = a;a = c;}}aspect B {int A.a;}",
 				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block(variableDeclarationStatement(primitiveType)(simpleName)(simpleName))(expressionStatement(assignment(simpleName)(simpleName))))))(aspect(simpleName)(fieldITD(primitiveType)(simpleName))))");
 	}
 	public void testConstructorITDBeingCalled(){
 		check("class A {void b(){A();}}aspect B {A.new(){}}",
 				"(compilationUnit(class(simpleName)(method(primitiveType)(simpleName)(block(expressionStatement(methodInvocation(simpleName))))))(aspect(simpleName)(constructorITD(primitiveType)(simpleName)(block))))");
 	}
 	
 	public void testDeclareParents(){
 		check("class A{}class B{}aspect C {declare parents : A extends B;}",
 				"(compilationUnit(class(simpleName))(class(simpleName))(aspect(simpleName)(declareParents)))");
 	}
 	public void testDeclareWarning(){
 		check("aspect A {pointcut a();declare warning: a(): \"warning\";}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(declareWarning)))");
 	}
 	public void testDeclareError(){
 		check("aspect A {pointcut a();declare error: a(): \"error\";}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(declareError)))");
 	}
 	public void testDeclareSoft(){
 		check("aspect A {pointcut a();declare soft: Exception+: a();}",
 				"(compilationUnit(aspect(simpleName)(pointcut(simpleName))(declareSoft)))");
 	}
 	public void testDeclarePrecedence(){
 		check("aspect A{}aspect B{declare precedence: B,A;}",
 				"(compilationUnit(aspect(simpleName))(aspect(simpleName)(declarePrecedence)))");
 	}
 	
 	public void testPerThis(){
 		check("aspect A perthis(a()) {pointcut a();}",
 				"(compilationUnit(aspect(simpleName)(simpleName)(pointcut(simpleName))))");
 	}
 	public void testPerTarget(){
 		check("aspect A pertarget(a()) {pointcut a();}",
 				"(compilationUnit(aspect(simpleName)(simpleName)(pointcut(simpleName))))");
 	}
 	public void testPerCFlow(){
 		check("aspect A percflow(a()) {pointcut a();}",
 				"(compilationUnit(aspect(simpleName)(simpleName)(pointcut(simpleName))))");
 	}
 	public void testPerCFlowBelow(){
 		check("aspect A percflowbelow(a()) {pointcut a();}",
 				"(compilationUnit(aspect(simpleName)(simpleName)(pointcut(simpleName))))");
 	}
 	
 	private void check(String source, String expectedOutput){
 		ASTParser parser = ASTParser.newParser(AST.JLS2); // ajh02: need to use 2 for returnType - in 3 it has "returnType2"
 		parser.setCompilerOptions(new HashMap());//JavaCore.getOptions());
 		parser.setSource(source.toCharArray());
 		CompilationUnit cu2 = (CompilationUnit) parser.createAST(null);
 		TestVisitor visitor = new TestVisitor();
 		cu2.accept(visitor);
 		String result = visitor.toString();
 		System.err.println("actual:\n" + result);
 		assertTrue("Expected:\n"+ expectedOutput + "====Actual:\n" + result,
 				expectedOutput.equals(result));
 	}
 	
 	/** @deprecated using deprecated code */
 	private static final int AST_INTERNAL_JLS2 = AST.JLS2;
 	
 	
 	/**
 	 * @deprecated (not really - just suppressing the warnings
 	 * that come from testing Javadoc.getComment())
 	 *
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	protected CompilationUnit createAST(char[] source) {
 		ASTParser parser= ASTParser.newParser(AST_INTERNAL_JLS2);
 		parser.setSource(source);
 		parser.setResolveBindings(false);
 		return (CompilationUnit) parser.createAST(null);
 	}
 }
 
 class TestVisitor extends AjASTVisitor {
 	
 	StringBuffer b = new StringBuffer();
 	boolean visitTheKids = true;
 	
 	boolean visitDocTags;
 	
 	TestVisitor() {
 		this(false);
 	}
 	
 	public String toString(){
 		return b.toString();
 	}
 	
 	TestVisitor(boolean visitDocTags) {
 		super(visitDocTags);
 		this.visitDocTags = visitDocTags;
 	}
 	
 	public boolean isVisitingChildren() {
 		return visitTheKids;
 	}
 
 	public void setVisitingChildren(boolean visitChildren) {
 		visitTheKids = visitChildren;
 	}
 	
 	public boolean visit(TypeDeclaration node) {
 		if (((AjTypeDeclaration)node).isAspect()) {
 			b.append("(aspect"); //$NON-NLS-1$
 			//if (((AspectDeclaration)node).getPerClause() != null){
 			//	b.append("{" + ((AspectDeclaration)node).getPerClause() + "}");
 			//}
 		} else if (node.isInterface()){
 			b.append("(interface"); // $NON-NLS-1$
 		} else {
 			b.append("(class"); //$NON-NLS-1$
 		}
 		return isVisitingChildren();
 	}
 	public void endVisit(TypeDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(PointcutDeclaration node) {
 		b.append("(pointcut"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}	
 	public void endVisit(PointcutDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(BeforeAdviceDeclaration node) {
 		b.append("(beforeAdvice"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public boolean visit(AroundAdviceDeclaration node) {
 		b.append("(aroundAdvice"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public boolean visit(AfterAdviceDeclaration node) {
 		b.append("(afterAdvice"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public boolean visit(AfterThrowingAdviceDeclaration node) {
 		b.append("(afterThrowingAdvice"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public boolean visit(AfterReturningAdviceDeclaration node) {
 		b.append("(afterReturningAdvice"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	
 	public void endVisit(BeforeAdviceDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public void endVisit(AroundAdviceDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public void endVisit(AfterAdviceDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public void endVisit(AfterThrowingAdviceDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public void endVisit(AfterReturningAdviceDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 		
 	public boolean visit(MethodDeclaration node) {
 		if (node instanceof InterTypeMethodDeclaration) return visit((InterTypeMethodDeclaration)node);
 		if (node.isConstructor()){
 			b.append("(constructor");
 		} else {
 			b.append("(method"); //$NON-NLS-1$
 		}
 		return isVisitingChildren();
 	}
 	public void endVisit(MethodDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(InterTypeFieldDeclaration node) {
 		b.append("(fieldITD"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(InterTypeFieldDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(InterTypeMethodDeclaration node) {
 		if (node.isConstructor()){
 			b.append("(constructorITD");
 		} else {
 			b.append("(methodITD"); //$NON-NLS-1$
 		}
 		return isVisitingChildren();
 	}
 	public void endVisit(InterTypeMethodDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(MethodInvocation node) {
 		b.append("(methodInvocation"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(MethodInvocation node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(BodyDeclaration node) {
 		b.append("(methodInvocation"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(BodyDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(FieldDeclaration node) {
 		b.append("(field"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(FieldDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(FieldAccess node) {
 		b.append("(fieldAccess"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(FieldAccess node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(Assignment node) {
 		b.append("(assignment"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(Assignment node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(Block node) {
 		b.append("(block"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(Block node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(CompilationUnit node) {
 		b.append("(compilationUnit"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(CompilationUnit node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(ExpressionStatement node) {
 		b.append("(expressionStatement"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(ExpressionStatement node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(InfixExpression node) {
 		b.append("(infixExpression"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(InfixExpression node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(Initializer node) {
 		b.append("(initializer"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(Initializer node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(NumberLiteral node) {
 		b.append("(numberLiteral"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(NumberLiteral node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(PrimitiveType node) {
 		b.append("(primitiveType"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(PrimitiveType node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(QualifiedName node) {
 		b.append("(qualifiedName"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(QualifiedName node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(SimpleName node) {
 		b.append("(simpleName"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(SimpleName node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(StringLiteral node) {
 		b.append("(stringLiteral"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(StringLiteral node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(VariableDeclaration node) {
 		b.append("(variableDeclaration"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public boolean visit(BlockComment bc) {
 		b.append("(blockcomment");
 		return isVisitingChildren();
 	}
 	public void endVisit(BlockComment bc) {
 		b.append(")");
 	}
 	public void endVisit(VariableDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(VariableDeclarationStatement node) {
 		b.append("(variableDeclarationStatement"); //$NON-NLS-1$
 		return isVisitingChildren();
 	}
 	public void endVisit(VariableDeclarationStatement node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 	public boolean visit(DeclareDeclaration node) {
 		System.err.println("visiting a DeclareDeclaration");
 		if (node.declareDecl instanceof DeclareAnnotation){
 			b.append("(declareAnnotation");
 		} else if (node.declareDecl instanceof DeclareErrorOrWarning){
 			if (((DeclareErrorOrWarning)node.declareDecl).isError()){
 				b.append("(declareError");
 			} else {
 				b.append("(declareWarning");
 			}
 		} else if (node.declareDecl instanceof DeclareParents){
 			b.append("(declareParents");
 		} else if (node.declareDecl instanceof DeclarePrecedence){
 			b.append("(declarePrecedence");
 		} else if (node.declareDecl instanceof DeclareSoft){
 			b.append("(declareSoft");
 		} else {
 			// node.declareDecl is null... weird
 			b.append("(declareErrorOrWarning");
 		}
 		return isVisitingChildren();
 	}
 	public void endVisit(DeclareDeclaration node) {
 		b.append(")"); //$NON-NLS-1$
 	}
 }
