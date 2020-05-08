 package core.scientrix.syntax;
 
 import core.scientrix.math.Real;
 
 /*
  * @author Mohamed Adam Chaieb
  * 
  * This class represents binary expressions (expressions with an operator and two operands).
  * */
 public class BinaryOperation implements Operation 
 {
 	public Operation o1;
 	public Operation o2;
 	public BinaryOperator operator;
 	
 	public BinaryOperation(Operation o1, BinaryOperator operator, Operation o2)
 	{
 		this.o1 = o1;
 		this.o2 = o2;
 		this.operator = operator;
 	}
 	
 	@Override
 	public Value evaluate() 
 	{
 		if((this.o1 instanceof Real) && (this.o2 instanceof Real))
 		{
 			Real real1 = (Real)this.o1;
 			Real real2 = (Real)this.o2;
 			if(this.operator.equals(BinaryOperator.PLUS))
 				return real1.add(real2);
 			if(this.operator.equals(BinaryOperator.MINUS))
				return real1.add(real2);
 			if(this.operator.equals(BinaryOperator.MULTIPLY))
 				return real1.multiply(real2);
 			if(this.operator.equals(BinaryOperator.DIVIDE))
 				return real1.divide(real2, 20);
 			if(this.operator.equals(BinaryOperator.MOD))
 				return real1.mod(real2);
 			if(this.operator.equals(BinaryOperator.POW))
 				return real1.pow(real2);
 		}
 		return (new BinaryOperation((Operation)this.o1.evaluate(), this.operator, (Operation)this.o2.evaluate() )).evaluate();
 	}
 
 	@Override
 	public Operation substitute(Variable x, Real number) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String toString()
 	{
 		return "("+this.o1.toString()+") "+this.operator.toString()+" ("+this.o2.toString()+")";
 	}
 }
