 package de.inger.booleanalgebra.antlr3.treenodes;
 
 public abstract class UnaryOperator extends Operator {
 
	private TreeNode operand;
 
	public UnaryOperator(TreeNode operand) {
 		operand.setParent(this);
 		setOperand(operand);
 	}
 
	public TreeNode getOperand() {
 		return this.operand;
 	}
 
	public void setOperand(TreeNode operand) {
 		this.operand = operand;
 	}
 
 }
