 package com.alibaba.fastut.condition;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
 import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.DoStatement;
 import org.eclipse.jdt.core.dom.EnhancedForStatement;
 import org.eclipse.jdt.core.dom.EnumDeclaration;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.ExpressionStatement;
 import org.eclipse.jdt.core.dom.ForStatement;
 import org.eclipse.jdt.core.dom.IfStatement;
 import org.eclipse.jdt.core.dom.InfixExpression;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.ReturnStatement;
 import org.eclipse.jdt.core.dom.Statement;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import org.eclipse.jdt.core.dom.SwitchCase;
 import org.eclipse.jdt.core.dom.SwitchStatement;
 import org.eclipse.jdt.core.dom.WhileStatement;
 
 public class FlowPathBuilder extends ASTVisitor {
 	private List<FlowPath> flowPaths;
 	private List<PartialFlowPath> currentPath = new ArrayList<>();
 	
 	
 	private int point = 0;
 	
 //	private ExitPathManager exitPathManager;
 //	private ExceptionPathManager exceptionPathManager;
 //	private FinallyPathManager finallyPathManager;
 	
 	public List<FlowPath> computePaths(MethodDeclaration  node) {
 		FlowPathBuilder pathBuilder = new FlowPathBuilder();
 	
 		pathBuilder.computePathsInNode(node,true);
 		return pathBuilder.flowPaths;
 	}
 	
 	private void computePathsInNode(ASTNode node, boolean forceReturn) {
 	    this.flowPaths = new ArrayList<>();
 //	    this.exitPathManager = new ExitPathManager();
 //	    this.exceptionPathManager = new ExceptionPathManager();
 //	    this.finallyPathManager = new FinallyPathManager();
 	    this.currentPath = computePartialPaths(createEmptyPaths(), node);
 	    if (forceReturn) {
 	    	for(PartialFlowPath flowPath : currentPath){
 	    		flowPath.forceReturn();
 	    	}
 	    }
 	    savePaths();
 	    if ((forceReturn) && (this.flowPaths.isEmpty())) {
 	    	this.flowPaths.add(new FlowPath(new DecisionPoint[] { ReturnDecisionPoint.mustReturn(null) }));
 	    }
 
 	    this.currentPath = null;
 //	    this.finallyPathManager = null;
 //	    this.exceptionPathManager = null;
 //	    this.exitPathManager = null;
 	}
 	
 	private void savePaths() {
 		savePathsAndReplaceWith(createEmptyPaths());
 	}
 
 	private void savePathsAndReplaceWith(List<PartialFlowPath> newCurrentPaths) {
 		savePaths(this.currentPath);
 		this.currentPath = newCurrentPaths;
 	}
 
 	private void savePaths(List<PartialFlowPath> paths) {
 		
 		for (PartialFlowPath path : paths) {
 			if (!path.isEmpty()) {
 				this.flowPaths.add(path.toFlowPath());
 			}
 		}
 //		if (this.finallyPathManager.isInTryStatement()) {
 //			this.finallyPathManager.addExitPaths(paths);
 //		} else {
 //			for (PartialFlowPath path : paths) {
 //				if (!path.isEmpty()) {
 //					this.flowPaths.add(path.toFlowPath());
 //				}
 //			}
 //		}
 	}
 	
 	public boolean visit(AnnotationTypeDeclaration node) {
 		return false;
 	}
 
 	public boolean visit(AnonymousClassDeclaration node) {
 		return false;
 	}
     
 	@Override
 	public boolean visit(EnumDeclaration node) {
 		return false;
 	}
 	
 	public boolean visit(IfStatement node) {
 		point++;
 		TrueAndFalseBuilder builder = buildTrueAndFalsePaths(this.currentPath,node.getExpression());
 		this.currentPath = builder.getTruePaths(String.valueOf(point),"if");
 		ExpressionStatement pointStatement = getAddPointStat(node.getAST(),String.valueOf(point));
 		Statement st = node.getThenStatement();
 	  	if(st instanceof Block){
 	  		Block block = (Block)st;
 	  		List<Statement> stats = block.statements();
 	  		stats.add(0, pointStatement);
 	  	}else{
 	  		Block block = node.getAST().newBlock();
 	  		node.setThenStatement(block);
 	  		block.statements().add(pointStatement);
 	  		block.statements().add(st);
 	  		
 	  	}
 		node.getThenStatement().accept(this);
 		Statement elseStatement = node.getElseStatement();
 		if (elseStatement != null) {
 			point++;
 			List<PartialFlowPath> savedPaths = this.currentPath;
 			this.currentPath = builder.getFalsePaths(String.valueOf(point),"else");
 			pointStatement = getAddPointStat(node.getAST(),String.valueOf(point));
 			if(elseStatement instanceof Block){
 		  		Block block = (Block)elseStatement;
 		  		List<Statement> stats = block.statements();
 		  		stats.add(0, pointStatement);
 		  	}else{
 		  		Block block = node.getAST().newBlock();
 		  		node.setElseStatement(block);
 		  		block.statements().add(pointStatement);
 		  		block.statements().add(elseStatement);
 		  		
 		  	}
 			
 			elseStatement.accept(this);
 			addNonEmptyPaths(this.currentPath, savedPaths);
 		} else {
 			addNonEmptyPaths(this.currentPath, builder.getFalsePaths(null,null));
 		}
 		// validatePathCount();
 
 		return false;
 	}
 	
 	public boolean visit(DoStatement node){
 		AST ast = node.getAST();
 		node.getBody().accept(this);
 		TrueAndFalseBuilder builder = buildTrueAndFalsePaths(this.currentPath, node.getExpression());
 		point++;
 		Expression leftExp = node.getExpression();
 		InfixExpression infixExpression = ast.newInfixExpression();
 		node.setExpression(infixExpression);
 		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
 		MethodInvocation methodInvocation = getPointInvocation(ast,String.valueOf(point));
 	    infixExpression.setLeftOperand(leftExp);
 		infixExpression.setRightOperand(methodInvocation);
 		
 		this.currentPath = builder.getTruePaths(String.valueOf(point),"do");
 	    addNonEmptyPaths(this.currentPath, builder.getFalsePaths(null,null));
 	    
 		return false;
 	}
 	
 	public boolean visit(WhileStatement node){
 		point++;
 		TrueAndFalseBuilder builder = buildTrueAndFalsePaths(this.currentPath, node.getExpression());
 	    this.currentPath = builder.getTruePaths(String.valueOf(point),"while");
 	  	ExpressionStatement pointStatement = getAddPointStat(node.getAST(),String.valueOf(point));
 	  	Statement st = node.getBody();
 	  	if(st instanceof Block){
 	  		Block block = (Block)st;
 	  		List<Statement> stats = block.statements();
 	  		stats.add(0, pointStatement);
 	  	}else{
 	  		Block block = node.getAST().newBlock();
 	  		node.setBody(block);
 	  		block.statements().add(pointStatement);
 	  		block.statements().add(st);
 	  		
 	  	}
 	    node.getBody().accept(this);
 	    addNonEmptyPaths(this.currentPath, builder.getFalsePaths(null,null));
 		return false;
 	}
 	
 	public boolean visit(ForStatement node){
 		point++;
 		TrueAndFalseBuilder builder = buildTrueAndFalsePaths(this.currentPath, node.getExpression());
 	    this.currentPath = builder.getTruePaths(String.valueOf(point),"for");
 	  	ExpressionStatement pointStatement = getAddPointStat(node.getAST(),String.valueOf(point));
 	  	Statement st = node.getBody();
 	  	if(st instanceof Block){
 	  		Block block = (Block)st;
 	  		List<Statement> stats = block.statements();
 	  		stats.add(0, pointStatement);
 	  	}else{
 	  		Block block = node.getAST().newBlock();
 	  		node.setBody(block);
 	  		block.statements().add(pointStatement);
 	  		block.statements().add(st);
 	  		
 	  	}
 	    node.getBody().accept(this);
 	    addNonEmptyPaths(this.currentPath, builder.getFalsePaths(null,null));
 		return false;
 	}
 	
 	public boolean visit(EnhancedForStatement node){
 		point++;
 		TrueAndFalseBuilder builder = buildTrueAndFalsePaths(this.currentPath, node.getExpression());
 	    this.currentPath = builder.getTruePaths(String.valueOf(point),"foreach");
 	    ExpressionStatement pointStatement = getAddPointStat(node.getAST(),String.valueOf(point));
 	  	Statement st = node.getBody();
 	  	if(st instanceof Block){
 	  		Block block = (Block)st;
 	  		List<Statement> stats = block.statements();
 	  		stats.add(0, pointStatement);
 	  	}else{
 	  		Block block = node.getAST().newBlock();
 	  		node.setBody(block);
 	  		block.statements().add(pointStatement);
 	  		block.statements().add(st);
 	  		
 	  	}
 	    node.getBody().accept(this);
 	    addNonEmptyPaths(this.currentPath, builder.getFalsePaths(null,null));
 		return false;
 	}
 	
 	
 	public boolean visit(SwitchStatement node){
 		Expression switchExpression = node.getExpression();
 		List<PartialFlowPath> originalPaths = this.currentPath;
 	    this.currentPath = null;
 	    List<Statement> statements = node.statements();
 	    for(Statement statement : statements){
	    	//statement.accept(this);
 	    	if(statement instanceof SwitchCase){
 	    		point++;
 	    		List<PartialFlowPath> savedPaths = this.currentPath;
 	    	    this.currentPath = copyPaths(originalPaths);
 	    	    Expression caseExpression = ((SwitchCase)statement).getExpression();
 	    	    DecisionPoint dp = ValueDecisionPoint.mustBeEqual(switchExpression, caseExpression);
 	    	    dp.setMark(String.valueOf(point));
 	    	    dp.setType("switch");
 	    	    addDecisionPoint(this.currentPath, dp);
     	        if (savedPaths != null){
     	        	addNonEmptyPaths(this.currentPath, savedPaths);
     	        }
     		  	
     		  	ExpressionStatement pointStatement = getAddPointStat(node.getAST(),String.valueOf(point));
     		  	int index = statements.indexOf(statement);
     		  	statements.add(index+1,pointStatement);
 	    	}
 	    }
 		return false;
 	}
 	
 	public boolean visit(ReturnStatement node) {
 	    Expression expression = node.getExpression();
 	    addDecisionPoint(this.currentPath, ReturnDecisionPoint.mustReturn(expression));
 	    
 	    savePaths();
 	    //validatePathCount();
 	    return false;
 	  }
 
 
 	private static void addNonEmptyPaths(List<PartialFlowPath> targetList, List<PartialFlowPath> sourceList){
 		Iterator<PartialFlowPath> paths = targetList.iterator();
 		while (paths.hasNext()) {
 			PartialFlowPath path = (PartialFlowPath) paths.next();
 			if (path.isEmpty()) {
 				paths.remove();
 			}
 		}
 		paths = sourceList.iterator();
 		while (paths.hasNext()) {
 			PartialFlowPath path = (PartialFlowPath) paths.next();
 			if (!path.isEmpty())
 				targetList.add(path);
 		}
 	}
 	
 	private static List<PartialFlowPath> createEmptyPaths() {
 		List<PartialFlowPath> paths = new ArrayList<>();
 		paths.add(new PartialFlowPath());
 		return paths;
 	}
 	
 	private List<PartialFlowPath> computePartialPaths(List<PartialFlowPath> initialPaths, ASTNode node) {
 		List<PartialFlowPath> savedPaths = this.currentPath;
 		try {
 			this.currentPath = initialPaths;
 			node.accept(this);
 			return this.currentPath;
 		} finally {
 			this.currentPath = savedPaths;
 		}
 	}
 	private static void addDecisionPoint(List<PartialFlowPath> paths, DecisionPoint point) {
 		for(PartialFlowPath path: paths){
 			path.addDecisionPoint(point);
 		}
 	}
 	private static List<PartialFlowPath> copyPaths(List<PartialFlowPath> originalPaths) {
 		
 		List<PartialFlowPath> savedPaths = new ArrayList<PartialFlowPath>();
 		for(PartialFlowPath path: originalPaths){
 			savedPaths.add(path.copy());
 		}
 		
 		return savedPaths;
 	}
 	
 	public TrueAndFalseBuilder buildTrueAndFalsePaths(List<PartialFlowPath> originalPaths, Expression expression) {
 		TrueAndFalseBuilder builder = new TrueAndFalseBuilder(originalPaths, expression);
 		//builder.buildPaths(expression);
 		return builder;
 	}
 
 	  
 	private class TrueAndFalseBuilder {
 		private List<PartialFlowPath> truePaths;
 		private List<PartialFlowPath> falsePaths;
 		private Expression expression;
 
 		public TrueAndFalseBuilder(List<PartialFlowPath> originalPaths,Expression expression) {
 			this.truePaths = FlowPathBuilder.copyPaths(originalPaths);
 			this.falsePaths = FlowPathBuilder.copyPaths(originalPaths);
 			this.expression = expression;
 		}
 		
 //		public void buildPaths(Expression expression) {
 //				//expression.accept(this);
 //			
 //				FlowPathBuilder.addDecisionPoint(this.truePaths,BooleanDecisionPoint.mustBeTrue(expression));
 //				FlowPathBuilder.addDecisionPoint(this.falsePaths,BooleanDecisionPoint.mustBeFalse(expression));
 //		
 //		}
 //		
 
 		
 		public List<PartialFlowPath> getTruePaths(String mark,String type){
 			DecisionPoint dp = BooleanDecisionPoint.mustBeTrue(expression);
 			dp.setMark(mark);
 			dp.setType(type);
 			FlowPathBuilder.addDecisionPoint(this.truePaths,dp);
 			return truePaths;
 		}
 		
 		public List<PartialFlowPath> getFalsePaths(String mark,String type){
 			DecisionPoint dp = BooleanDecisionPoint.mustBeFalse(expression);
 			dp.setMark(mark);
 			dp.setType(type);
 			FlowPathBuilder.addDecisionPoint(this.falsePaths,dp);
 			return falsePaths;
 		}
 		
 	}
 	
 	private ExpressionStatement getAddPointStat(AST ast ,String point){
 		MethodInvocation methodInvocation = getPointInvocation(ast,point);
 	    return ast.newExpressionStatement(methodInvocation);
 	}
 	
 	private MethodInvocation getPointInvocation(AST ast ,String point){
 		MethodInvocation methodInvocation = ast.newMethodInvocation();
 	    methodInvocation.setName(ast.newSimpleName("setPointMark")); 
 	    StringLiteral literal = ast.newStringLiteral();
 	    literal.setLiteralValue(String.valueOf(point));
 	    methodInvocation.arguments().add(literal);
 	    return methodInvocation;
 	}
 
 }
