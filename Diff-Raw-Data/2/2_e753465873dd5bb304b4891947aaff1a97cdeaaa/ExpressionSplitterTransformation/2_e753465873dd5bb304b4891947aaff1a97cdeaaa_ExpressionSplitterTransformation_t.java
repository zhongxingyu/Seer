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
 package net.sf.orcc.backends.transformations.threeAddressCodeTransformation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 import net.sf.orcc.OrccException;
 import net.sf.orcc.OrccRuntimeException;
 import net.sf.orcc.ir.Actor;
 import net.sf.orcc.ir.CFGNode;
 import net.sf.orcc.ir.Expression;
 import net.sf.orcc.ir.Instruction;
 import net.sf.orcc.ir.LocalVariable;
 import net.sf.orcc.ir.Procedure;
 import net.sf.orcc.ir.Type;
 import net.sf.orcc.ir.Use;
 import net.sf.orcc.ir.expr.AbstractExpressionInterpreter;
 import net.sf.orcc.ir.expr.BinaryExpr;
 import net.sf.orcc.ir.expr.BinaryOp;
 import net.sf.orcc.ir.expr.BoolExpr;
 import net.sf.orcc.ir.expr.IntExpr;
 import net.sf.orcc.ir.expr.UnaryExpr;
 import net.sf.orcc.ir.expr.UnaryOp;
 import net.sf.orcc.ir.expr.VarExpr;
 import net.sf.orcc.ir.instructions.Assign;
 import net.sf.orcc.ir.instructions.Call;
 import net.sf.orcc.ir.instructions.Load;
 import net.sf.orcc.ir.instructions.Return;
 import net.sf.orcc.ir.instructions.Store;
 import net.sf.orcc.ir.nodes.AbstractNode;
 import net.sf.orcc.ir.nodes.BlockNode;
 import net.sf.orcc.ir.nodes.IfNode;
 import net.sf.orcc.ir.nodes.WhileNode;
 import net.sf.orcc.ir.transforms.AbstractActorTransformation;
 
 /**
  * Split expression and effective node that contains more than one fundamental
  * operation into a series of simple expressions.
  * 
  * @author Jerome GORIN
  * @author Matthieu Wipliez
  * 
  */
 public class ExpressionSplitterTransformation extends
 		AbstractActorTransformation {
 	private class ExpressionSplitter extends AbstractExpressionInterpreter {
 
 		@Override
 		public Object interpret(BinaryExpr expr, Object... args) {
 			Type type = expr.getType();
 			BinaryOp op = expr.getOp();
 			Expression e1 = (Expression) expr.getE1().accept(this, args);
 			Expression e2 = (Expression) expr.getE2().accept(this, args);
 
 			expr.setE1(e1);
 			expr.setE2(e2);
 
 			// Create a new binary expression
 			Expression binExpr = new BinaryExpr(e1, op, e2, type);
 
 			// Make a new assignment to the binary expression
 			LocalVariable target = procedure.newTempLocalVariable(file, type,
 					procedure.getName() + "_" + "expr");
 			Assign assign = new Assign(target, binExpr);
 
 			// Add assignment to instruction's list
 			instrs.add(assign);
 
 			return new VarExpr(new Use(target));
 		}
 
 		@Override
 		public Object interpret(UnaryExpr expr, Object... args) {
 			Expression varExpr = expr.getExpr();
 			Type type = expr.getType();
 
 			varExpr = (Expression) varExpr.accept(this, args);
 
 			BinaryExpr binaryExpr = transformUnaryExpr(expr.getOp(), varExpr);
 
 			// Make a new assignment to the binary expression
 			LocalVariable target = procedure.newTempLocalVariable(file, type,
 					procedure.getName() + "_" + "expr");
 			Assign assign = new Assign(target, binaryExpr);
 
 			// Add assignment to instruction's list
 			instrs.add(assign);
 
 			return new VarExpr(new Use(target));
 		}
 
 		public BinaryExpr transformUnaryExpr(UnaryOp op, Expression expr) {
 			Expression constExpr;
 			Type type = expr.getType();
 
 			switch (op) {
 			case MINUS:
 				constExpr = new IntExpr(0);
 				return new BinaryExpr(constExpr, BinaryOp.MINUS, expr, type);
 			case LOGIC_NOT:
 				constExpr = new BoolExpr(false);
 				return new BinaryExpr(expr, BinaryOp.EQ, constExpr, type);
 			case BITNOT:
 				return new BinaryExpr(expr, BinaryOp.BITXOR, expr, type);
 			default:
 				throw new OrccRuntimeException("unsupported operator");
 			}
 		}
 
 	}
 
 	ExpressionSplitter expressionSplitter;
 	private String file;
 
 	private List<Instruction> instrs;
 
 	public ExpressionSplitterTransformation() {
 		expressionSplitter = new ExpressionSplitter();
 		instrs = new ArrayList<Instruction>();
 	}
 
 	/**
 	 * Returns an iterator over the last instruction of the previous block. A
 	 * new block is created if there is no previous one.
 	 * 
 	 * @param it
 	 * @return
 	 */
 	private ListIterator<Instruction> getItr(ListIterator<CFGNode> it) {
 		BlockNode block;
 
 		it.previous();
 		if (it.hasPrevious()) {
 			// get previous and restore iterator's position
 			CFGNode previous = it.previous();
 			it.next();
 
 			if (previous.isBlockNode()) {
 				block = ((BlockNode) previous);
 			} else if (previous.isIfNode()) {
 				block = ((IfNode) previous).getJoinNode();
 			} else {
 				block = ((WhileNode) previous).getJoinNode();
 			}
 		} else {
 			// no previous block, create and add a new one
 			block = new BlockNode(procedure);
 			it.add(block);
 		}
 		it.next();
 
 		return block.lastListIterator();
 	}
 
 	@Override
 	public void transform(Actor actor) throws OrccException {
 		this.file = actor.getFile();
 		super.transform(actor);
 	}
 
 	@Override
 	public void visit(Assign assign) {
 		Expression value = assign.getValue();
 
 		if (value.isBinaryExpr()) {
 			BinaryExpr binExpr = (BinaryExpr) value;
 			Expression e1 = binExpr.getE1();
 			Expression e2 = binExpr.getE2();
 
 			if (e1.isBinaryExpr() || e1.isUnaryExpr()) {
 				// Split expression e1
 				instructionIterator.previous();
 				binExpr.setE1(visitExpression(e1, instructionIterator));
 				instructionIterator.next();
 			}
 
 			if (e2.isBinaryExpr() || e2.isUnaryExpr()) {
 				// Split expression e2
 				instructionIterator.previous();
 				binExpr.setE2(visitExpression(e2, instructionIterator));
 				instructionIterator.next();
 			}
 			
 			Use.addUses(assign, value);
 		} else if (value.isUnaryExpr()) {
 			UnaryExpr unaryExpr = (UnaryExpr) value;
 			instructionIterator.previous();
 
 			// Transform unary expression into binary expression
 			Expression newExpr = visitExpression(unaryExpr.getExpr(),
 					instructionIterator);
 			assign.setValue(expressionSplitter.transformUnaryExpr(
 					unaryExpr.getOp(), newExpr));
 
 			instructionIterator.next();
 		}
 	}
 
 	@Override
 	public void visit(Call call) {
 		List<Expression> parameters = call.getParameters();
 		for (Expression parameter : parameters) {
 			if (parameter.isBinaryExpr() || parameter.isUnaryExpr()) {
 				instructionIterator.previous();
 				Expression newParameter = visitExpression(parameter,
 						instructionIterator);
 				parameters.set(parameters.indexOf(parameter), newParameter);
 				instructionIterator.next();
 			}
 		}
 	}
 
 	@Override
 	public void visit(IfNode ifNode) {
 		Expression value = ifNode.getValue();
 		if ((value.isBinaryExpr()) || (value.isUnaryExpr())) {
 			ifNode.setValue(visitExpression(ifNode.getValue(),
 					getItr(nodeIterator)));
 		}
 		super.visit(ifNode);
 	}
 
 	@Override
 	public void visit(Load load) {
 		instructionIterator.previous();
 		visitIndexes(load.getIndexes(), instructionIterator);
 		instructionIterator.next();
 	}
 
 	@Override
 	public void visit(Return returnInstr) {
 		if (returnInstr.getValue() != null) {
 			instructionIterator.previous();
 			returnInstr.setValue(visitExpression(returnInstr.getValue(),
 					instructionIterator));
 			instructionIterator.next();
 		}
 	}
 
 	@Override
 	public void visit(Store store) {
 		Expression value = store.getValue();
 
 		instructionIterator.previous();
 
 		visitIndexes(store.getIndexes(), instructionIterator);
 
 		if ((value.isBinaryExpr()) || (value.isUnaryExpr())) {
 			Expression newValue = visitExpression(value, instructionIterator);
 			store.setValue(newValue);
 		}
 
 		instructionIterator.next();
 	}
 
 	@Override
 	public void visit(WhileNode whileNode) {
 		ListIterator<Instruction> it = whileNode.getJoinNode().listIterator();
 
 		// Go to the end of joinNode
 		while (it.hasNext()) {
 			it.next();
 		}
 
 		Expression value = whileNode.getValue();
 		if ((value.isBinaryExpr()) || (value.isUnaryExpr())) {
 			Expression expr = visitExpression(whileNode.getValue(), it);
 			whileNode.setValue(expr);
 		}
 
 		super.visit(whileNode);
 	}
 
 	private Expression visitExpression(Expression value,
 			ListIterator<Instruction> it) {
 		instrs.clear();
 		Expression expr = (Expression) value.accept(expressionSplitter);
 
 		for (Instruction instr : instrs) {
 			it.add(instr);
 		}
 
 		return expr;
 	}
 
 	private void visitIndexes(List<Expression> indexes,
 			ListIterator<Instruction> it) {
 		for (Expression value : indexes) {
 			if ((value.isBinaryExpr()) || (value.isUnaryExpr())) {
 				Expression newValue = visitExpression(value, it);
 				indexes.set(indexes.indexOf(value), newValue);
 			}
 		}
 	}
 
 	@Override
 	public void visitProcedure(Procedure procedure) {
 		// set the label counter to prevent new nodes from having the same label
 		// as existing nodes
 		List<CFGNode> nodes = procedure.getNodes();
 		if (nodes.size() > 0) {
 			CFGNode lastNode = nodes.get(nodes.size() - 1);
			AbstractNode.setLabelCount(lastNode.getLabel() + 2);
 		}
 		super.visitProcedure(procedure);
 	}
 }
