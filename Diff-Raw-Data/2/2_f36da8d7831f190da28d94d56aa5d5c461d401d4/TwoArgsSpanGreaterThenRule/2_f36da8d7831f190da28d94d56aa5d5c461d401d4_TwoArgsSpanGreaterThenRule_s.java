 package com.acclivyx.rules.sequenceintegerrules;
 
 import java.math.BigInteger;
 import java.util.Iterator;
 
 import com.acclivyx.rules.RuleResult;
 import com.acclivyx.rules.SequenceIntegerRule;
 import com.acclivyx.rules.args.RuleIntegerArgs;
 
 /**
  * If any two sequential integers in the sequence add up to 1000 or greater,
  * the rule is "passed". If not, the rule is "failed".
  */
 public class TwoArgsSpanGreaterThenRule extends SequenceIntegerRule{
 	private final BigInteger MIN = BigInteger.valueOf(1000);
 
 	public TwoArgsSpanGreaterThenRule() {
 		super("RuleA");
 	}
 
 	/**
 	 * @param args of specific type
 	 * @return results
 	 */
 	public RuleResult process(RuleIntegerArgs args) {
 		Integer first = null;
 		Integer second = null;
 		
 		for (Iterator<Integer> iter = args.getArgs().iterator(); iter.hasNext();) {
 			if (first == null) {
 				first = second = iter.next();
 				continue;
 			}
 			first = second;
 			second = iter.next();
 			BigInteger result = BigInteger.valueOf(first);
			result = result.add(result);
 			
 			if (result.compareTo(MIN) >= 0) {
 				return new RuleResult(true,this.name);
 			}
 		}
 		return new RuleResult(false,this.name);
 	}
 
 }
