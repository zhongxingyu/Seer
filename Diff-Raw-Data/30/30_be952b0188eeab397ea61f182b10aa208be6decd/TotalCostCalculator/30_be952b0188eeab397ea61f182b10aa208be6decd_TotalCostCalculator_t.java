 /**
  * 
  */
 package com.qut.spc.calculations;
 
 import com.qut.spc.api.TotalCostCalculationAPI;
 
 /**
  * @author Zuojun Chen
  *
  */
 public class TotalCostCalculator implements TotalCostCalculationAPI {
 
 	/* (non-Javadoc)
 	 * @see com.qut.spc.api.TotalCostCalculationAPI#getSystemTotalCost(double, int, double, int, double)
 	 */
 	@Override
 	public double getSystemTotalCost(
 			double panelPrice, 
 			int panelsQuantity,
 			double batterPrice, 
 			int batteriesQuantity, 
			double inverterPrice)  
 					throws IllegalArgumentException{
 		
		restrictInput(panelPrice,panelsQuantity,batterPrice, batteriesQuantity,inverterPrice);
		return (panelPrice*panelsQuantity) + (batterPrice*batteriesQuantity)+inverterPrice;
 	}
 
 	private void restrictInput(double panelPrice, int panelsQuantity,
 			double batterPrice, int batteriesQuantity, double intverterPrice) throws IllegalArgumentException{
 		if (panelPrice < 0) {
 			throw new IllegalArgumentException("Invaild input, " +
 					"the panelPrice parameter shoulbe be more than zero. ");
 		}  else if (panelsQuantity < 0) {
 			throw new IllegalArgumentException("Invaild input, " +
 					"the panelsQuantity parameter shoulbe be more than zero. ");
 		} else if (batterPrice < 0) {
 			throw new IllegalArgumentException("Invaild input," +
 					"the batterPrice parameter shoulbe be more than zero. ");
 		} else if (batteriesQuantity < 0) {
 			throw new IllegalArgumentException("Invaild input, " +
 					"the batteriesQuantity parameter shoulbe be more than zero. ");
 		} else if (intverterPrice < 0) {
 			throw new IllegalArgumentException("Invaild input, " +
 					"the intverterPrice parameter should be more than zero.");
 		} 
 		
 	}
 
 }
