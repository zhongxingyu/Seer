 package lea.syntax;
 
 public class NumberExp extends Expression {
 	Expression left, right;
 
 	protected NumberExp() {
 
 	}
 
 	public NumberExp(Expression a1, Expression a2, EnumTagExp tag) {
 		super(a1, a2);
 		expTag = tag;
 		left = a1;
 		right = a2;
 	}
 
 	public String toString() {
		return "NumberExp(" + expTag + ")" + super.toString();
 	}
 
 	public String toDotString() {
		return "NumberExp(" + expTag + ")";
 	}
 
 	public String toJava() {
		if (right == null)
			// unary operators, like MINUS_U
			return expTag + " " + left.toJava();
		else
			// binary operators
			return left.toJava() + " " + expTag + " " + right.toJava();
 	}
 }
