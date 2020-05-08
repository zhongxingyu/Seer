 package org.gsoft.openserv.buslogic.amortization;
 
 import java.math.BigDecimal;
import java.math.MathContext;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import org.gsoft.openserv.domain.amortization.AmortizationLoanPayment;
 import org.gsoft.openserv.domain.loan.Loan;
 import org.gsoft.openserv.util.Constants;
 
 public class PaymentAmountCalculator {
 	
 	public static Integer calculatePaymentAmount(Integer principalBalance, BigDecimal annualInterestRate, Integer remainingTerm){
         BigDecimal periodicRate = annualInterestRate.divide(new BigDecimal(Constants.MONTHS_IN_YEAR), Constants.INTEREST_ROUNDING_SCALE_35, Constants.INTEREST_ROUNDING_MODE);
		Integer paymentAmount = periodicRate.multiply(BigDecimal.valueOf(principalBalance)).divide(BigDecimal.ONE.subtract(periodicRate.add(BigDecimal.valueOf(1)).pow(remainingTerm * ( -1 ), MathContext.DECIMAL128)), Constants.INTEREST_ROUNDING_SCALE_35, Constants.INTEREST_ROUNDING_MODE).intValue();
 		if(paymentAmount <= 0){
 			if(remainingTerm > 0){
 				paymentAmount = principalBalance/remainingTerm;
 			}
 			else{
 				paymentAmount = principalBalance;
 			}
 		}
 		return paymentAmount;
 	}
 	
 	public static Integer findPaymentAmountForDate(Loan loan, Date asOfDate){
 		int minPaymentAmount = 0;
 		if(loan.getCurrentAmortizationSchedule() != null){
 			List<AmortizationLoanPayment> payments = loan.getCurrentAmortizationSchedule().getAmortizationLoanPayments();
 			Collections.sort(payments, new Comparator<AmortizationLoanPayment>(){
 				@Override
 				public int compare(AmortizationLoanPayment p1, AmortizationLoanPayment p2){
 					return p1.getPaymentOrder() - p2.getPaymentOrder();
 				}
 			});
 			int remainingTerm = loan.getRemainingLoanTermAsOf(asOfDate);
 			int usedTerm = payments.size() - remainingTerm;
 			int index = 0;
 			int paymentCount = 0;
 			while(paymentCount < usedTerm){
 				paymentCount += payments.get(index).getPaymentCount();
 				index++;
 			}
 			minPaymentAmount = payments.get(index).getPaymentAmount(); 
 		}
 		return minPaymentAmount; 
 	}
 }
