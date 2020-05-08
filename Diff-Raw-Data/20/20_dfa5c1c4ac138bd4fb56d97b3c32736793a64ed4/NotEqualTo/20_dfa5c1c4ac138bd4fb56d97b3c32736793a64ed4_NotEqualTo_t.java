 package asteroids.model.programs.expressions;
 
 public class NotEqualTo extends EqualityExpression {
 
 	public NotEqualTo(int line, int column, Expression left, Expression right)
 			throws IllegalArgumentException {
 		super(line, column, left, right);
 	}
 
 	@Override
 	public Boolean getValue() {
		if(this.getLeftPart().getValue() == null && this.getRightPart().getValue() == null)
			return false;
		if(this.getLeftPart().getValue() == null)
			return true;
 		return !(this.getLeftPart().getValue()).equals(this.getRightPart().getValue());
 	}
 
 	@Override
 	public String getSymbol() {
 		return "!=";
 	}
 }
