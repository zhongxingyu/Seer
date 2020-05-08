 package com.infinitiessoft.btrs.custom;
 
 import java.util.Map;
 
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.log.Log;
 
 import com.infinitiessoft.btrs.enums.ExpenseTypeEnum;
 import com.infinitiessoft.btrs.enums.HighSpeedRailEnum;
 import com.infinitiessoft.btrs.enums.ParameterEnum;
 import com.infinitiessoft.btrs.model.ApplicationVariable;
 import com.infinitiessoft.btrs.model.Expense;
 import com.infinitiessoft.btrs.model.ParameterValue;
 
 @Name("expenseAmountCalculator")
 public class ExpenseAmountCalculator {
 	
 	@Logger Log log;
 	
 	@In
 	Map<String, ApplicationVariable> allVariables;
 	
 	public void calculateTaxAndAmount(Expense expense) {
 		Integer amount = expense.getTotalAmount();
 		
 		if (ExpenseTypeEnum.TRANSP_HSR_RECEIPT.equals(expense.getExpenseType().getValue())) {
 			String sourceValue = expense.getParameterValue(ParameterEnum.SOURCE).getValue();
 			String destinationValue = expense.getParameterValue(ParameterEnum.DESTINATION).getValue();
 			
 			String ticketsValue = expense.getParameterValue(ParameterEnum.TICKETS).getValue();
 			Integer ticketsValueSafe = (ticketsValue == null || ticketsValue.isEmpty()) ? 1 : Integer.valueOf(ticketsValue);
 			
 			HighSpeedRailEnum source = HighSpeedRailEnum.valueOf(sourceValue);
 			HighSpeedRailEnum destination = HighSpeedRailEnum.valueOf(destinationValue);
 			Integer price = Integer.valueOf(allVariables.get(source + "_" + destination).getValue());
 			
 			amount = price * ticketsValueSafe;
 			
 		} else if (ExpenseTypeEnum.TRANSP_CAR.equals(expense.getExpenseType().getValue())) {
 			// only when created, not when edited
 			if (expense.getTaxAmount() == null) {
 				ParameterValue freewayToll = expense.getParameterValue(ParameterEnum.FREEWAY_TOLL);
 				try {
 					amount += Integer.valueOf(freewayToll.getValue());
 				} catch (NumberFormatException e) {
 					log.info("Error while converting Freeway Toll to number", e);
 				}
 			}
 		}
 		
		Integer taxPercent = expense.getExpenseType().getTaxPercent();
 		Integer taxAmount = calculateTax(amount, taxPercent);
 		
 		expense.setTotalAmount(amount);
 		expense.setTaxAmount(taxAmount);
 	}
 	
	public Integer calculateTax(Integer amount, Integer taxPercent) {
 		if (taxPercent > 0) {
			double taxCoefficient = 100 / (100.0 + taxPercent);
 			Double amountWithoutTax = amount * taxCoefficient;
 			Integer amountWithoutTaxRoundedDown = amountWithoutTax.intValue();
 			return amount - amountWithoutTaxRoundedDown;
 		} else {
 			return 0;
 		}
 	}
 
 }
