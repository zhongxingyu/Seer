 package banjo.parser.ast;
 
 import java.util.List;
 
 import banjo.parser.util.FileRange;
 
 /**
  * Sequencing operation.  Any "let" expressions are visible in later steps.  The
  * steps should operate as if they were run in order from first to last.  The value
  * of the expression is the evaluated result of the last expression.
  */
 public class Steps extends Expr {
 	private final List<Expr> steps;
 	
 	public Steps(FileRange range, List<Expr> steps) {
 		super(range);
 		this.steps = steps;
 	}
 
 	@Override
 	public void toSource(StringBuffer sb) {
		boolean first = true;
 		for(Expr step : steps) {
 			if(first) first = false;
 			else sb.append(", ");
 			step.toSource(sb, Precedence.COMMA);
 		}
 	}
 
 	@Override
 	public Precedence getPrecedence() {
 		return Precedence.COMMA;
 	}
 
 	public List<Expr> getSteps() {
 		return steps;
 	}
 
 }
