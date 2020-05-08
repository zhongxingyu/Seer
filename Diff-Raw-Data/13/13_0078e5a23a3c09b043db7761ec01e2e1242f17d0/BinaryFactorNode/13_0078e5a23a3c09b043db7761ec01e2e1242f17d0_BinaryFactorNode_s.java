 package ast;
 
/**
 * For this class, an interface would not make much sense any more. ;)
 */
public abstract class FactorNode extends BinaryTermNode {
 	ExponentNode lhs;
 	FactorNode rhs;
 
 	public FactorNode(ExponentNode lhs, FactorNode rhs){
 		this.lhs=lhs;
 		this.rhs=rhs;
 	}	
 }
