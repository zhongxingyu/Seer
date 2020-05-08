 package de.inger.booleanalgebra.antlr3.treenodes;
 
 public abstract class UnaryOperator extends Operator {
 
	private Operand operand;
 
	public UnaryOperator(Operand operand) {
 		operand.setParent(this);
 		setOperand(operand);
 	}
 
	public Operand getOperand() {
 		return this.operand;
 	}
 
	public void setOperand(Operand operand) {
 		this.operand = operand;
 	}
 
 }
