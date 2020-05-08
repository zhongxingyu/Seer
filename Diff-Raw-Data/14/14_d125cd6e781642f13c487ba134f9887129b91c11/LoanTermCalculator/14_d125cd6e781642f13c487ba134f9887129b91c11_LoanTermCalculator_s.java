 package org.gsoft.openserv.buslogic.amortization;
 
 import java.util.Date;
 
 import org.gsoft.openserv.domain.loan.Loan;
 import org.joda.time.DateTime;
 import org.joda.time.Months;
 
 public class LoanTermCalculator {
 	
 	public static int calculateRemainingLoanTermAsOf(Loan loan, Date asOfDate){
		int remainingTerm = 0;
		if(loan.getCurrentAmortizationSchedule() != null){
			int term = loan.getCurrentAmortizationSchedule().getTotalNumberOfPayment();
			int used = Months.monthsBetween(new DateTime(loan.getCurrentAmortizationSchedule().getAmortizationSchedule().getEffectiveDate()),new DateTime(asOfDate)).getMonths(); 
			remainingTerm = term - used;
 		}
		else{
			remainingTerm = loan.getEffectiveLoanTypeProfile().getMaximumLoanTerm(); 
		}
		return remainingTerm;
 	}
 }
