 package com.cs.fwk.core.config.calculators;
 
import org.apache.log4j.Logger;

 import com.cs.fwk.api.Element;
 import com.cs.fwk.api.util.AttributeCalcInt;
 
 
 /**
  * Calculate oper value for L4IP by or-in together the values for P, Q, and Z
  * 
  * @author pjf
  *
  */
 public class L4IPOperCalc extends BaseAttributeCalculator implements AttributeCalcInt {
	private final static Logger logger = Logger.getLogger(L4IPOperCalc.class);
 
 	@Override
 	public void calculate(String attrName, Element el, String arg)
 			throws Exception {
 		initialise(attrName,el);
 		int p = getRawVal("P");
 		int q = getRawVal("Q");
 		int z = getRawVal("Z");
		int always = 0;
		try {
			always = getRawVal("Type");
		} catch (Exception e) {
			logger.info("Using legacy calc");
			always = 0;
		}
 		int oper = always | p | q | z;
 		setValueHex(oper);
 	}
 }
