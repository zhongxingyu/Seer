 package ast;
 
public class FactorNode extends BinaryTermNode {
 	ExponentNode lhs;
 	FactorNode rhs;
 
 	public FactorNode(ExponentNode lhs, FactorNode rhs){
 		this.lhs=lhs;
 		this.rhs=rhs;
 	}	
 }
