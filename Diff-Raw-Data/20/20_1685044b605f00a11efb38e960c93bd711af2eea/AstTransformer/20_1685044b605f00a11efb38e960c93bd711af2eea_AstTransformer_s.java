 /*
  * Copyright (c) 2009-2010, IETR/INSA of Rennes
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice,
  *     this list of conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice,
  *     this list of conditions and the following disclaimer in the documentation
  *     and/or other materials provided with the distribution.
  *   * Neither the name of the IETR/INSA of Rennes nor the names of its
  *     contributors may be used to endorse or promote products derived from this
  *     software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 package net.sf.orcc.frontend;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import net.sf.orcc.cal.cal.AstExpression;
 import net.sf.orcc.cal.cal.AstExpressionBinary;
 import net.sf.orcc.cal.cal.AstExpressionBoolean;
 import net.sf.orcc.cal.cal.AstExpressionCall;
 import net.sf.orcc.cal.cal.AstExpressionIf;
 import net.sf.orcc.cal.cal.AstExpressionIndex;
 import net.sf.orcc.cal.cal.AstExpressionInteger;
 import net.sf.orcc.cal.cal.AstExpressionList;
 import net.sf.orcc.cal.cal.AstExpressionString;
 import net.sf.orcc.cal.cal.AstExpressionUnary;
 import net.sf.orcc.cal.cal.AstExpressionVariable;
 import net.sf.orcc.cal.cal.AstFunction;
 import net.sf.orcc.cal.cal.AstGenerator;
 import net.sf.orcc.cal.cal.AstProcedure;
 import net.sf.orcc.cal.cal.AstStatement;
 import net.sf.orcc.cal.cal.AstStatementAssign;
 import net.sf.orcc.cal.cal.AstStatementCall;
 import net.sf.orcc.cal.cal.AstStatementForeach;
 import net.sf.orcc.cal.cal.AstStatementIf;
 import net.sf.orcc.cal.cal.AstStatementWhile;
 import net.sf.orcc.cal.cal.AstVariable;
 import net.sf.orcc.cal.cal.util.CalSwitch;
 import net.sf.orcc.cal.expression.AstExpressionEvaluator;
 import net.sf.orcc.cal.type.TypeChecker;
 import net.sf.orcc.ir.CFGNode;
 import net.sf.orcc.ir.Expression;
 import net.sf.orcc.ir.Instruction;
 import net.sf.orcc.ir.IrFactory;
 import net.sf.orcc.ir.LocalVariable;
 import net.sf.orcc.ir.Location;
 import net.sf.orcc.ir.Procedure;
 import net.sf.orcc.ir.Type;
 import net.sf.orcc.ir.Use;
 import net.sf.orcc.ir.Variable;
 import net.sf.orcc.ir.expr.BinaryExpr;
 import net.sf.orcc.ir.expr.BinaryOp;
 import net.sf.orcc.ir.expr.BoolExpr;
 import net.sf.orcc.ir.expr.IntExpr;
 import net.sf.orcc.ir.expr.StringExpr;
 import net.sf.orcc.ir.expr.UnaryExpr;
 import net.sf.orcc.ir.expr.UnaryOp;
 import net.sf.orcc.ir.expr.VarExpr;
 import net.sf.orcc.ir.instructions.Assign;
 import net.sf.orcc.ir.instructions.Call;
 import net.sf.orcc.ir.instructions.Load;
 import net.sf.orcc.ir.instructions.Return;
 import net.sf.orcc.ir.instructions.Store;
 import net.sf.orcc.ir.nodes.BlockNode;
 import net.sf.orcc.ir.nodes.IfNode;
 import net.sf.orcc.ir.nodes.WhileNode;
 import net.sf.orcc.util.OrderedMap;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.naming.IQualifiedNameProvider;
 
 import com.google.inject.Inject;
 
 /**
  * This class transforms an AST actor to its IR equivalent.
  * 
  * @author Matthieu Wipliez
  * 
  */
 public class AstTransformer {
 
 	/**
 	 * This class transforms an AST statement into one or more IR instructions
 	 * and/or nodes.
 	 * 
 	 */
 	private class ExpressionTransformer extends CalSwitch<Expression> {
 
 		private List<Expression> indexes;
 
 		private Variable target;
 
 		public ExpressionTransformer() {
 			indexes = new ArrayList<Expression>();
 		}
 
 		@Override
 		public Expression caseAstExpressionBinary(AstExpressionBinary expression) {
 			BinaryOp op = BinaryOp.getOperator(expression.getOperator());
 			Expression e1 = doSwitch(expression.getLeft());
 			Expression e2 = doSwitch(expression.getRight());
 
 			return new BinaryExpr(Util.getLocation(expression), e1, op, e2,
 					expression.getIrType());
 		}
 
 		@Override
 		public Expression caseAstExpressionBoolean(
 				AstExpressionBoolean expression) {
 			Location location = Util.getLocation(expression);
 			boolean value = expression.isValue();
 			return new BoolExpr(location, value);
 		}
 
 		@Override
 		public Expression caseAstExpressionCall(AstExpressionCall astCall) {
 			Location location = Util.getLocation(astCall);
 			Procedure procedure = context.getProcedure();
 
 			// retrieve IR procedure
 			AstFunction astFunction = astCall.getFunction();
 
 			// special case if the function is a built-in function
 			if (astFunction.eContainer() == null) {
 				return transformBuiltinFunction(astCall);
 			}
 
 			if (!mapFunctions.containsKey(astFunction)) {
 				transformFunction(astFunction);
 			}
 			Procedure calledProcedure = mapFunctions.get(astFunction);
 
 			// generates a new target
 			LocalVariable target = procedure.newTempLocalVariable(file,
 					calledProcedure.getReturnType(),
 					"call_" + calledProcedure.getName());
 
 			// creates call with spilling code around it
 			createCall(location, target, calledProcedure,
 					astCall.getParameters());
 
 			// return local variable
 			Use use = new Use(target);
 			Expression varExpr = new VarExpr(location, use);
 			return varExpr;
 		}
 
 		@Override
 		public Expression caseAstExpressionIf(AstExpressionIf expression) {
 			Location location = Util.getLocation(expression);
 
 			Expression condition = transformExpression(expression
 					.getCondition());
 
 			Variable currentTarget = target;
 			List<Expression> currentIndexes = indexes;
 
 			Type type = expression.getIrType();
 			if (target == null || !type.isList()) {
 				target = context.getProcedure().newTempLocalVariable(file,
 						expression.getIrType(), "_tmp_if");
 				indexes = new ArrayList<Expression>(0);
 			}
 
 			// transforms "then" statements and "else" statements
 			List<CFGNode> thenNodes = getNodes(expression.getThen());
 			List<CFGNode> elseNodes = getNodes(expression.getElse());
 
 			IfNode node = new IfNode(location, context.getProcedure(),
 					condition, thenNodes, elseNodes, new BlockNode(
 							context.getProcedure()));
 			context.getProcedure().getNodes().add(node);
 
 			Use use = new Use(target);
 			Expression varExpr = new VarExpr(location, use);
 
 			// restores target and indexes
 			target = currentTarget;
 			indexes = currentIndexes;
 
 			// return the expression
 			return varExpr;
 		}
 
 		@Override
 		public Expression caseAstExpressionIndex(AstExpressionIndex expression) {
 			// we always load in this case
 
 			Location location = Util.getLocation(expression);
 			AstVariable astVariable = expression.getSource().getVariable();
 			Variable variable = context.getVariable(astVariable);
 
 			List<Expression> indexes = transformExpressions(expression
 					.getIndexes());
 
 			LocalVariable target = context.getProcedure()
 					.newTempLocalVariable(file, expression.getIrType(),
 							"local_" + variable.getName());
 
 			Load load = new Load(location, target, new Use(variable), indexes);
 			addInstruction(load);
 
 			Use use = new Use(target);
 			Expression varExpr = new VarExpr(location, use);
 			return varExpr;
 		}
 
 		@Override
 		public Expression caseAstExpressionInteger(
 				AstExpressionInteger expression) {
 			Location location = Util.getLocation(expression);
 			int value = expression.getValue();
 			return new IntExpr(location, value);
 		}
 
 		@Override
 		public Expression caseAstExpressionList(AstExpressionList astExpression) {
 			Variable currentTarget = target;
 			List<Expression> currentIndexes = indexes;
 
 			List<AstExpression> expressions = astExpression.getExpressions();
 			List<AstGenerator> generators = astExpression.getGenerators();
 
 			checkTarget(expressions, generators);
 
 			if (generators.isEmpty()) {
 				transformListSimple(expressions);
 			} else {
 				transformListGenerators(expressions, generators);
 			}
 
 			Expression expression = new VarExpr(new Use(target));
 
 			// restores target and indexes
 			target = currentTarget;
 			indexes = currentIndexes;
 
 			// return the expression
 			return expression;
 		}
 
 		@Override
 		public Expression caseAstExpressionString(AstExpressionString expression) {
 			return new StringExpr(expression.getValue());
 		}
 
 		@Override
 		public Expression caseAstExpressionUnary(AstExpressionUnary expression) {
 			UnaryOp op = UnaryOp.getOperator(expression.getUnaryOperator());
 			Expression expr = doSwitch(expression.getExpression());
 
 			return new UnaryExpr(Util.getLocation(expression), op, expr,
 					expression.getIrType());
 		}
 
 		@Override
 		public Expression caseAstExpressionVariable(
 				AstExpressionVariable expression) {
 			AstVariable astVariable = expression.getValue().getVariable();
 			Location location = Util.getLocation(expression);
 
 			Variable variable = context.getVariable(astVariable);
 			if (variable.getType().isList()) {
 				Use use = new Use(variable);
 				Expression varExpr = new VarExpr(location, use);
 				return varExpr;
 			} else {
 				LocalVariable local = getLocalVariable(variable, false);
 				Use use = new Use(local);
 				Expression varExpr = new VarExpr(location, use);
 				return varExpr;
 			}
 		}
 
 		/**
 		 * Checks that the current target is not <code>null</code>. If it is,
 		 * this method initializes it according to the expressions and
 		 * generators.
 		 * 
 		 * @param expressions
 		 *            a list of expressions
 		 * @param generators
 		 *            a list of generators
 		 */
 		private void checkTarget(List<AstExpression> expressions,
 				List<AstGenerator> generators) {
 			if (target != null) {
 				return;
 			}
 
 			int size = 1;
 
 			// size of generators
 			for (AstGenerator generator : generators) {
 				AstExpression astValue = generator.getLower();
 				int lower = new AstExpressionEvaluator()
 						.evaluateAsInteger(astValue);
 
 				astValue = generator.getHigher();
 				int higher = new AstExpressionEvaluator()
 						.evaluateAsInteger(astValue);
 				size *= (higher - lower) + 1;
 			}
 
 			// size of expressions
 			TypeChecker checker = new TypeChecker();
 			Type type = checker.getType(expressions);
 			size *= expressions.size();
 
 			Procedure procedure = context.getProcedure();
 			Type listType = IrFactory.eINSTANCE.createTypeList(size, type);
 			target = procedure.newTempLocalVariable(file, listType, "_list");
 		}
 
 		/**
 		 * Returns a list of CFG nodes from the given expression. This method
 		 * creates a new block node to hold the statements created when
 		 * translating the expression, transforms the expression, and transfers
 		 * the nodes created to a new list that is the result.
 		 * 
 		 * @param astExpression
 		 *            an AST expression
 		 * @return a list of CFG nodes
 		 */
 		private List<CFGNode> getNodes(AstExpression astExpression) {
 			Location location = Util.getLocation(astExpression);
 			List<CFGNode> nodes = context.getProcedure().getNodes();
 
 			int first = nodes.size();
 			nodes.add(new BlockNode(context.getProcedure()));
 
 			Expression value = transformExpression(astExpression);
 			createAssignOrStore(location, target, indexes, value);
 
 			int last = nodes.size();
 
 			// moves selected CFG nodes from "nodes" list to resultNodes
 			List<CFGNode> subList = nodes.subList(first, last);
 			List<CFGNode> resultNodes = new ArrayList<CFGNode>(subList);
 			subList.clear();
 
 			return resultNodes;
 		}
 
 		/**
 		 * Returns a list of CFG nodes from the given list of statements. This
 		 * method creates a new block node to hold the statements, transforms
 		 * the statements, and transfers the nodes created to a new list that is
 		 * the result.
 		 * 
 		 * @param statements
 		 *            a list of statements
 		 * @return a list of CFG nodes
 		 */
 		private List<CFGNode> getNodes(List<AstExpression> astExpressions) {
 			List<CFGNode> nodes = context.getProcedure().getNodes();
 
 			int first = nodes.size();
 			nodes.add(new BlockNode(context.getProcedure()));
 
 			for (AstExpression astExpression : astExpressions) {
 				Location location = Util.getLocation(astExpression);
 				Expression value = transformExpression(astExpression);
 				createAssignOrStore(location, target, indexes, value);
 			}
 
 			int last = nodes.size();
 
 			// moves selected CFG nodes from "nodes" list to resultNodes
 			List<CFGNode> subList = nodes.subList(first, last);
 			List<CFGNode> resultNodes = new ArrayList<CFGNode>(subList);
 			subList.clear();
 
 			return resultNodes;
 		}
 
 		/**
 		 * Sets the target variable that is assigned a list expression with the
 		 * given indexes.
 		 * 
 		 * @param target
 		 *            a variable
 		 * @param indexes
 		 *            a list of indexes
 		 */
 		public void setTarget(Variable target, List<Expression> indexes) {
 			this.target = target;
 			this.indexes = indexes;
 		}
 
 		/**
 		 * Transforms the given function call to an expression. This method is
 		 * only called when the function is an intrinsic/built-in function (like
 		 * bitand, lshift, etc.)
 		 * 
 		 * @param astCall
 		 *            a call
 		 * @return an IR expression
 		 */
 		private Expression transformBuiltinFunction(AstExpressionCall astCall) {
 			Location location = Util.getLocation(astCall);
 			String name = astCall.getFunction().getName();
 			if ("bitnot".equals(name)) {
 				Expression expr = transformExpression(astCall.getParameters()
 						.get(0));
 				return new UnaryExpr(location, UnaryOp.BITNOT, expr,
 						expr.getType());
 			}
 
 			BinaryOp op = BinaryOp.getOperator(name);
 			if (op == null) {
 				return null;
 			}
 
 			Expression e1 = transformExpression(astCall.getParameters().get(0));
 			Expression e2 = transformExpression(astCall.getParameters().get(1));
 			return new BinaryExpr(location, e1, op, e2,
 					new TypeChecker().getLub(e1.getType(), e2.getType()));
 		}
 
 		/**
 		 * Transforms the given expressions and generators.
 		 * 
 		 * @param expressions
 		 *            a list of expressions
 		 * @param generators
 		 *            a list of generators
 		 */
 		private void transformListGenerators(List<AstExpression> expressions,
 				List<AstGenerator> generators) {
 			Variable currentTarget = target;
 			List<Expression> currentIndexes = indexes;
 
 			indexes = new ArrayList<Expression>(currentIndexes);
 
 			Procedure procedure = context.getProcedure();
 
 			// first add local variables
 			for (AstGenerator generator : generators) {
 				AstVariable astVariable = generator.getVariable();
 				LocalVariable loopVar = transformLocalVariable(astVariable);
 				procedure.getLocals().put(file, loopVar.getLocation(),
 						loopVar.getName(), loopVar);
 
 				indexes.add(new VarExpr(new Use(loopVar)));
 			}
 
 			// translates the expression (this will form the innermost nodes)
 			List<CFGNode> nodes = getNodes(expressions);
 
 			// build the loops from the inside out
 			ListIterator<AstGenerator> it = generators.listIterator(generators
 					.size());
 			while (it.hasPrevious()) {
 				AstGenerator generator = it.previous();
 				Location location = Util.getLocation(generator);
 
 				// assigns the loop variable its initial value
 				AstVariable astVariable = generator.getVariable();
 				LocalVariable loopVar = (LocalVariable) context
 						.getVariable(astVariable);
 				AstExpression astLower = generator.getLower();
 				Expression lower = transformExpression(astLower);
 				Assign assign = new Assign(location, loopVar, lower);
 				addInstruction(assign);
 
 				// condition
 				AstExpression astHigher = generator.getHigher();
 				Expression higher = transformExpression(astHigher);
 				Expression condition = new BinaryExpr(higher.getLocation(),
 						new VarExpr(new Use(loopVar)), BinaryOp.LE, higher,
 						IrFactory.eINSTANCE.createTypeBool());
 
 				// body
 				BlockNode block = BlockNode.getLast(procedure, nodes);
 				assign = new Assign(location, loopVar, new BinaryExpr(
 						new VarExpr(new Use(loopVar)), BinaryOp.PLUS,
 						new IntExpr(1), loopVar.getType()));
 				block.add(assign);
 
 				// create while
 				WhileNode whileNode = new WhileNode(location, procedure,
 						condition, nodes, new BlockNode(procedure));
 				nodes = new ArrayList<CFGNode>(1);
 				nodes.add(whileNode);
 			}
 
 			// add the outer while node
 			procedure.getNodes().addAll(nodes);
 
 			target = currentTarget;
 			indexes = currentIndexes;
 		}
 
 		/**
 		 * Transforms the list of expressions of an AstExpressionList (without
 		 * generators).
 		 * 
 		 * @param expressions
 		 *            a list of AST expressions
 		 */
 		private void transformListSimple(List<AstExpression> expressions) {
 			int i = 0;
 			for (AstExpression expression : expressions) {
 				Location location = Util.getLocation(expression);
 				Expression value = transformExpression(expression);
 
 				List<Expression> currentIndexes = new ArrayList<Expression>(
 						indexes);
 				currentIndexes.add(new IntExpr(i));
 
 				createAssignOrStore(location, target, currentIndexes, value);
 			}
 		}
 
 	}
 
 	/**
 	 * This class transforms an AST statement into one or more IR instructions
 	 * and/or nodes. It returns null because it appends the instructions/nodes
 	 * directly to the {@link #nodes} field.
 	 * 
 	 */
 	private class StatementTransformer extends CalSwitch<Void> {
 
 		@Override
 		public Void caseAstStatementAssign(AstStatementAssign astAssign) {
 			Location location = Util.getLocation(astAssign);
 
 			// get target
 			AstVariable astTarget = astAssign.getTarget().getVariable();
 			Variable target = context.getVariable(astTarget);
 
 			// transform indexes and value
 			List<Expression> indexes = transformExpressions(astAssign
 					.getIndexes());
 
 			exprTransformer.setTarget(target, indexes);
 			Expression value = transformExpression(astAssign.getValue());
 			createAssignOrStore(location, target, indexes, value);
 
 			return null;
 		}
 
 		@Override
 		public Void caseAstStatementCall(AstStatementCall astCall) {
 			Location location = Util.getLocation(astCall);
 
 			// retrieve IR procedure
 			AstProcedure astProcedure = astCall.getProcedure();
 			if (!mapProcedures.containsKey(astProcedure)) {
 				transformProcedure(astProcedure);
 			}
 			Procedure procedure = mapProcedures.get(astProcedure);
 
 			// creates call with spilling code around it
 			createCall(location, null, procedure, astCall.getParameters());
 
 			return null;
 		}
 
 		@Override
 		public Void caseAstStatementForeach(AstStatementForeach foreach) {
 			Location location = Util.getLocation(foreach);
 			Procedure procedure = context.getProcedure();
 
 			// creates loop variable and assigns it
 			AstVariable astVariable = foreach.getVariable();
 			LocalVariable loopVar = transformLocalVariable(astVariable);
 			procedure.getLocals().put(file, loopVar.getLocation(),
 					loopVar.getName(), loopVar);
 
 			AstExpression astLower = foreach.getLower();
 			Expression lower = transformExpression(astLower);
 			Assign assign = new Assign(location, loopVar, lower);
 			addInstruction(assign);
 
 			// condition
 			AstExpression astHigher = foreach.getHigher();
 			Expression higher = transformExpression(astHigher);
 			Expression condition = new BinaryExpr(higher.getLocation(),
 					new VarExpr(new Use(loopVar)), BinaryOp.LE, higher,
 					IrFactory.eINSTANCE.createTypeBool());
 
 			// body
 			List<CFGNode> nodes = getNodes(foreach.getStatements());
 			BlockNode block = BlockNode.getLast(procedure, nodes);
 			assign = new Assign(location, loopVar, new BinaryExpr(new VarExpr(
 					new Use(loopVar)), BinaryOp.PLUS, new IntExpr(1),
 					loopVar.getType()));
 			block.add(assign);
 
 			// create while
 			WhileNode whileNode = new WhileNode(location, procedure, condition,
 					nodes, new BlockNode(procedure));
 			procedure.getNodes().add(whileNode);
 
 			return null;
 		}
 
 		@Override
 		public Void caseAstStatementIf(AstStatementIf stmtIf) {
 			Location location = Util.getLocation(stmtIf);
 			Procedure procedure = context.getProcedure();
 
 			Expression condition = transformExpression(stmtIf.getCondition());
 
 			// transforms "then" statements and "else" statements
 			List<CFGNode> thenNodes = getNodes(stmtIf.getThen());
 			List<CFGNode> elseNodes = getNodes(stmtIf.getElse());
 
 			IfNode node = new IfNode(location, procedure, condition, thenNodes,
 					elseNodes, new BlockNode(procedure));
 			procedure.getNodes().add(node);
 
 			return null;
 		}
 
 		@Override
 		public Void caseAstStatementWhile(AstStatementWhile stmtWhile) {
 			Location location = Util.getLocation(stmtWhile);
 			Procedure procedure = context.getProcedure();
 
 			Expression condition = transformExpression(stmtWhile.getCondition());
 
 			List<CFGNode> nodes = getNodes(stmtWhile.getStatements());
 
 			WhileNode whileNode = new WhileNode(location, procedure, condition,
 					nodes, new BlockNode(procedure));
 			procedure.getNodes().add(whileNode);
 
 			return null;
 		}
 
 		/**
 		 * Returns a list of CFG nodes from the given list of statements. This
 		 * method creates a new block node to hold the statements, transforms
 		 * the statements, and transfers the nodes created to a new list that is
 		 * the result.
 		 * 
 		 * @param statements
 		 *            a list of statements
 		 * @return a list of CFG nodes
 		 */
 		private List<CFGNode> getNodes(List<AstStatement> statements) {
 			List<CFGNode> nodes = context.getProcedure().getNodes();
 
 			int first = nodes.size();
 			nodes.add(new BlockNode(context.getProcedure()));
 			transformStatements(statements);
 			int last = nodes.size();
 
 			// moves selected CFG nodes from "nodes" list to resultNodes
 			List<CFGNode> subList = nodes.subList(first, last);
 			List<CFGNode> resultNodes = new ArrayList<CFGNode>(subList);
 			subList.clear();
 
 			return resultNodes;
 		}
 
 	}
 
 	private Context context;
 
 	private final java.util.regex.Pattern dotPattern = java.util.regex.Pattern
 			.compile("\\.");
 
 	/**
 	 * expression transformer.
 	 */
 	final private ExpressionTransformer exprTransformer;
 
 	/**
 	 * The file in which the actor is defined.
 	 */
 	private String file;
 
 	/**
 	 * A map from AST functions to IR procedures.
 	 */
 	final private Map<AstFunction, Procedure> mapFunctions;
 
 	/**
 	 * A map from AST procedures to IR procedures.
 	 */
 	final private Map<AstProcedure, Procedure> mapProcedures;
 
 	@Inject
 	private IQualifiedNameProvider nameProvider;
 
 	/**
 	 * the list of procedures of the IR actor.
 	 */
 	private OrderedMap<String, Procedure> procedures;
 
 	/**
 	 * statement transformer.
 	 */
 	final private StatementTransformer stmtTransformer;
 
 	/**
 	 * Creates a new AST to IR transformation.
 	 */
 	public AstTransformer() {
 		mapFunctions = new HashMap<AstFunction, Procedure>();
 		mapProcedures = new HashMap<AstProcedure, Procedure>();
 
 		exprTransformer = new ExpressionTransformer();
 		stmtTransformer = new StatementTransformer();
 
 		context = new Context(null, null);
 
 		procedures = new OrderedMap<String, Procedure>();
 	}
 
 	/**
 	 * Adds the given instruction to the last block of the current procedure.
 	 * 
 	 * @param instruction
 	 *            an instruction
 	 */
 	private void addInstruction(Instruction instruction) {
 		BlockNode block = BlockNode.getLast(context.getProcedure());
 		block.add(instruction);
 	}
 
 	/**
 	 * Clears up context and functions/proceudres maps.
 	 */
 	public void clear() {
 		mapFunctions.clear();
 		mapProcedures.clear();
 
 		context = new Context(null, null);
 
 		procedures = new OrderedMap<String, Procedure>();
 	}
 
 	private void createAssignOrStore(Location location, Variable target,
 			List<Expression> indexes, Expression value) {
 		// special case for list expressions
 		if (value.isVarExpr()) {
 			Use use = ((VarExpr) value).getVar();
 			if (use.getVariable().getType().isList()) {
 				use.remove();
 				return;
 			}
 		}
 
 		Instruction instruction;
 		if (indexes == null || indexes.isEmpty()) {
 			LocalVariable local = getLocalVariable(target, true);
 			instruction = new Assign(location, local, value);
 		} else {
 			instruction = new Store(location, target, indexes, value);
 		}
 		addInstruction(instruction);
 	}
 
 	/**
 	 * Creates a call with the given arguments, and adds spilling code.
 	 * 
 	 * @param location
 	 *            location of the call
 	 * @param target
 	 *            target (or <code>null</code>)
 	 * @param procedure
 	 *            procedure being called
 	 * @param expressions
 	 *            arguments
 	 */
 	private void createCall(Location location, LocalVariable target,
 			Procedure procedure, List<AstExpression> expressions) {
 		// transform parameters
 		List<Expression> parameters = transformExpressions(expressions);
 
 		// stores variables that have been loaded and modified, and are loaded
 		// by the procedure
 		for (Variable global : procedure.getLoadedVariables()) {
 			LocalVariable local = context.getMapGlobals().get(global);
 			if (local != null
 					&& context.getSetGlobalsToStore().contains(global)) {
 				// variable already loaded somewhere and modified
 				VarExpr value = new VarExpr(new Use(local));
 
 				List<Expression> indexes = new ArrayList<Expression>(0);
 				Store store = new Store(global, indexes, value);
 				addInstruction(store);
 			}
 		}
 
 		// add call
 		Call call = new Call(location, target, procedure, parameters);
 		addInstruction(call);
 
 		// loads variables that are stored by the procedure
 		for (Variable global : procedure.getStoredVariables()) {
 			LocalVariable local = context.getMapGlobals().get(global);
 			if (local == null) {
 				// creates a new local variable
 				local = context.getProcedure().newTempLocalVariable(file,
 						global.getType(), "local_" + global.getName());
 				context.getMapGlobals().put(global, local);
 			}
 
 			List<Expression> indexes = new ArrayList<Expression>(0);
 			Load load = new Load(local, new Use(global), indexes);
 			addInstruction(load);
 		}
 	}
 
 	/**
 	 * Returns the current context of this AST transformer.
 	 * 
 	 * @return the current context of this AST transformer
 	 */
 	public Context getContext() {
 		return context;
 	}
 
 	/**
 	 * Returns the IR mapping of the given variable. If the variable is a
 	 * global, returns a local associated with it. The variable is added to the
 	 * set of globals to load, and if <code>isStored</code> is <code>true</code>
 	 * , the variable is added to the set of variables to store, too.
 	 * 
 	 * @param variable
 	 *            an IR variable, possibly global
 	 * @param isStored
 	 *            <code>true</code> if the variable is stored,
 	 *            <code>false</code> otherwise
 	 * @return a local IR variable
 	 */
 	private LocalVariable getLocalVariable(Variable variable, boolean isStored) {
 		if (variable.isGlobal()) {
 			LocalVariable local = context.getMapGlobals().get(variable);
 			if (local == null) {
 				local = context.getProcedure().newTempLocalVariable(file,
 						variable.getType(), "local_" + variable.getName());
 				context.getMapGlobals().put(variable, local);
 			}
 
 			context.getSetGlobalsToLoad().add(variable);
 			if (isStored) {
 				context.getSetGlobalsToStore().add(variable);
 			}
 
 			return local;
 		}
 
 		return (LocalVariable) variable;
 	}
 
 	/**
 	 * Returns the procedure map.
 	 * 
 	 * @return the procedure map
 	 */
 	public OrderedMap<String, Procedure> getProcedures() {
 		return procedures;
 	}
 
 	/**
 	 * Returns the qualified name for the given object.
 	 * 
 	 * @param obj
 	 *            an object
 	 * @return the qualified name for the given object
 	 */
 	public String getQualifiedName(EObject obj) {
 		return nameProvider.getQualifiedName(obj);
 	}
 
 	/**
 	 * Loads the globals that need to be loaded.
 	 */
 	private void loadGlobals() {
 		int i = 0;
 		for (Variable global : context.getSetGlobalsToLoad()) {
 			LocalVariable local = context.getMapGlobals().get(global);
 
 			List<Expression> indexes = new ArrayList<Expression>(0);
 			Load load = new Load(local, new Use(global), indexes);
 			BlockNode block = BlockNode.getFirst(context.getProcedure());
 			block.add(i, load);
 			i++;
 		}
 
 		context.getSetGlobalsToLoad().clear();
 	}
 
 	/**
 	 * Returns the current context, and creates a new one.
 	 * 
 	 * @return the current context
 	 */
 	public Context newContext(Procedure procedure) {
 		Context oldContext = context;
 		context = new Context(context, procedure);
 		return oldContext;
 	}
 
 	/**
 	 * Loads globals at the beginning of the current procedure, stores them at
 	 * the end, add a return instruction, and restores the con
 	 * 
 	 * @param context
 	 *            the context returned by {@link #newContext()}
 	 * @param value
 	 *            an expression to return, possibly <code>null</code> for
 	 *            imperative procedures
 	 */
 	public void restoreContext(Context context, Expression value) {
 		loadGlobals();
 		storeGlobals();
 
 		Return returnInstr = new Return(value);
 		addInstruction(returnInstr);
 
 		this.context = context;
 	}
 
 	/**
 	 * Sets the field "file" with the given value.
 	 * 
 	 * @param file
 	 *            a file name
 	 */
 	public void setFile(String file) {
 		this.file = file;
 	}
 
 	/**
 	 * Writes back the globals that need to be stored.
 	 */
 	private void storeGlobals() {
 		for (Variable global : context.getSetGlobalsToStore()) {
 			LocalVariable local = context.getMapGlobals().get(global);
 			VarExpr value = new VarExpr(new Use(local));
 
 			List<Expression> indexes = new ArrayList<Expression>(0);
 			Store store = new Store(global, indexes, value);
 			addInstruction(store);
 		}
 
 		context.getSetGlobalsToStore().clear();
 	}
 
 	/**
 	 * Transforms the given AST expression to an IR expression. In the process
 	 * nodes may be created and added to the current {@link #procedure}, since
 	 * many RVC-CAL expressions are expressed with IR statements.
 	 * 
 	 * @param expression
 	 *            an AST expression
 	 * @return an IR expression
 	 */
 	public Expression transformExpression(AstExpression expression) {
 		return exprTransformer.doSwitch(expression);
 	}
 
 	/**
 	 * Transforms the given AST expressions to a list of IR expressions. In the
 	 * process nodes may be created and added to the current {@link #procedure},
 	 * since many RVC-CAL expressions are expressed with IR statements.
 	 * 
 	 * @param expressions
 	 *            a list of AST expressions
 	 * @return a list of IR expressions
 	 */
 	public List<Expression> transformExpressions(List<AstExpression> expressions) {
 		int length = expressions.size();
 		List<Expression> irExpressions = new ArrayList<Expression>(length);
 		for (AstExpression expression : expressions) {
 			irExpressions.add(transformExpression(expression));
 		}
 		return irExpressions;
 	}
 
 	/**
 	 * Transforms the given AST function to an IR procedure, and adds it to the
 	 * IR procedure list {@link #procedures} and to the map
 	 * {@link #mapFunctions}.
 	 * 
 	 * @param astFunction
 	 *            an AST function
 	 */
 	public void transformFunction(AstFunction astFunction) {
 		String name = astFunction.getName();
 		Location location = Util.getLocation(astFunction);
 		Type type = astFunction.getIrType();
 
 		Procedure procedure = new Procedure(name, location, type);
 		Context oldContext = newContext(procedure);
 
 		transformParameters(astFunction.getParameters());
 		transformLocalVariables(astFunction.getVariables());
 		Expression value = transformExpression(astFunction.getExpression());
 
 		restoreContext(oldContext, value);
 
 		if (astFunction.eContainer() == null) {
 			procedure.setExternal(true);
 		}
 
 		procedures.put(file, location, name, procedure);
 		mapFunctions.put(astFunction, procedure);
 	}
 
 	/**
 	 * Transforms the given AST variable to an IR variable that has the name and
 	 * type of <code>astVariable</code>. A binding is added to the
 	 * {@link #mapVariables} between astVariable and the created local variable.
 	 * 
 	 * @param astVariable
 	 *            an AST variable
 	 * @return the IR local variable created
 	 */
 	public LocalVariable transformLocalVariable(AstVariable astVariable) {
 		Location location = Util.getLocation(astVariable);
 
 		String name = nameProvider.getQualifiedName(astVariable);
 		name = dotPattern.matcher(name).replaceAll("_");
 
 		boolean assignable = !astVariable.isConstant();
 		Type type = astVariable.getIrType();
 
 		// create local variable with the given name
 		LocalVariable local = new LocalVariable(assignable, 0, location, name,
 				type);
 
 		AstExpression value = astVariable.getValue();
 		if (value != null) {
 			Expression expression = transformExpression(value);
			Assign assign = new Assign(location, local, expression);
			addInstruction(assign);
 		}
 
 		context.putVariable(astVariable, local);
 		return local;
 	}
 
 	/**
 	 * Transforms the given list of AST variables to IR variables, and adds them
 	 * to the current {@link #procedure}'s local variables list.
 	 * 
 	 * @param variables
 	 *            a list of AST variables
 	 */
 	public void transformLocalVariables(List<AstVariable> variables) {
 		for (AstVariable astVariable : variables) {
 			LocalVariable local = transformLocalVariable(astVariable);
 			context.getProcedure().getLocals()
 					.put(file, local.getLocation(), local.getName(), local);
 		}
 	}
 
 	/**
 	 * Transforms the given list of AST parameters to IR variables, and adds
 	 * them to the current {@link #procedure}'s parameters list.
 	 * 
 	 * @param parameters
 	 *            a list of AST parameters
 	 */
 	private void transformParameters(List<AstVariable> parameters) {
 		for (AstVariable astParameter : parameters) {
 			LocalVariable local = transformLocalVariable(astParameter);
 			context.getProcedure().getParameters()
 					.put(file, local.getLocation(), local.getName(), local);
 		}
 	}
 
 	/**
 	 * Transforms the given AST procedure to an IR procedure, and adds it to the
 	 * IR procedure list {@link #procedures} and to the map
 	 * {@link #mapProcedures}.
 	 * 
 	 * @param astProcedure
 	 *            an AST procedure
 	 */
 	public void transformProcedure(AstProcedure astProcedure) {
 		String name = astProcedure.getName();
 		Location location = Util.getLocation(astProcedure);
 
 		Procedure procedure = new Procedure(name, location,
 				IrFactory.eINSTANCE.createTypeVoid());
 		Context oldContext = newContext(procedure);
 
 		transformParameters(astProcedure.getParameters());
 		transformLocalVariables(astProcedure.getVariables());
 		transformStatements(astProcedure.getStatements());
 
 		restoreContext(oldContext, null);
 
 		if (astProcedure.eContainer() == null) {
 			procedure.setExternal(true);
 		}
 
 		procedures.put(file, location, name, procedure);
 		mapProcedures.put(astProcedure, procedure);
 	}
 
 	/**
 	 * Transforms the given AST statement to one or more IR instructions and/or
 	 * nodes that are added directly to the current {@link #procedure}.
 	 * 
 	 * @param statement
 	 *            an AST statement
 	 */
 	private void transformStatement(AstStatement statement) {
 		stmtTransformer.doSwitch(statement);
 	}
 
 	/**
 	 * Transforms the given AST statements to IR instructions and/or nodes that
 	 * are added directly to the current {@link #procedure}.
 	 * 
 	 * @param statements
 	 *            a list of AST statements
 	 */
 	public void transformStatements(List<AstStatement> statements) {
 		for (AstStatement statement : statements) {
 			transformStatement(statement);
 		}
 	}
 
 }
