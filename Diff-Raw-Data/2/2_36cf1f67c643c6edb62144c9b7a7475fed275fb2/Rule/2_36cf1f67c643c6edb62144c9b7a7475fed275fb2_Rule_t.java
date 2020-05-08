 import java.util.ArrayList;
 import java.util.List;
 
/**
  * A Rule consists of a variable and a right side which consists of RuleElements.
  */
 public class Rule {
 	private Variable leftSide;
 	private List<RuleElement> rightSide;
 	
 	/**
 	 * Construct a new Rule with a variable for the left side and a list of RuleElements for the right side.
 	 * @param leftSide Variable on the left side
 	 * @param tailRuleRightSide List of RuleElements for the right side
 	 */
 	public Rule(Variable leftSide, List<RuleElement> tailRuleRightSide) {
 		this.leftSide = leftSide;
 		this.rightSide = tailRuleRightSide;
 	}
 	
 	/**
 	 * Construct a new Rule with a variable for the left side and a single RuleElement for the right side.
 	 * @param leftSide Variable on the left side
 	 * @param rightSide RuleElemt for the right side
 	 */
 	public Rule(Variable leftSide, RuleElement rightSide) {
 		this.leftSide = leftSide;
 		this.rightSide = new ArrayList<RuleElement>();
 		this.rightSide.add(rightSide);
 	}
 
 	/**
 	 * Detect whether or not this Rule has left recursion.
 	 * @return Whether the Rule has left recursion
 	 */
 	public boolean hasLeftRecursion() {
 		return leftSide == rightSide.get(0);
 	}
 
 	/**
 	 * Return the left side of the Rule.
 	 * @return Left side of the Rule
 	 */
 	public Variable getLeftSide() {
 		return leftSide;
 	}
 
 	/**
 	 * Return the right side of the Rule.
 	 * @return Right side of the Rule
 	 */
 	public List<RuleElement> getRightSide() {
 		return rightSide;
 	}
 	
 	/**
 	 * Add a Variable to the right side of the Rule.
 	 * @param ruleElement Variable to add
 	 */
 	public void addToRightSide(Variable ruleElement) {
 		rightSide.add(ruleElement);
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(leftSide + " : ");
 		
 		for (RuleElement re : rightSide) {
 			sb.append(re + " ");
 		}
 		
 		return sb.toString();
 	}
 }
