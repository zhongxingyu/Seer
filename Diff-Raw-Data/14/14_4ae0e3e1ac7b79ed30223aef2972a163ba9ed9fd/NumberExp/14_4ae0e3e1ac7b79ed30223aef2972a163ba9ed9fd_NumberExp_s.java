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
		return "NumberExp(" + expTag.toString() + ")" + super.toString();
 	}
 
 	public String toDotString() {
		return "NumberExp(" + expTag.toString() + ")";
 	}
 
 	public String toJava() {

		return left.toJava() + " " + expTag + " " + right.toJava();
 	}
 }
