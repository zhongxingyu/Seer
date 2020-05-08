 package org.gsoft.openserv.buslogic.amortization;
 
 import java.util.Date;
 
 import org.gsoft.openserv.domain.loan.Loan;
 import org.joda.time.DateTime;
 import org.joda.time.Months;
 
 public class LoanTermCalculator {
 	
 	public static int calculateRemainingLoanTermAsOf(Loan loan, Date asOfDate){
		int used = 0;
		if(loan.getRepaymentStartDate() != null){
			used = Months.monthsBetween(new DateTime(loan.getRepaymentStartDate()),new DateTime(asOfDate)).getMonths(); 
 		}
		return loan.getEffectiveLoanTypeProfile().getMaximumLoanTerm() - used;
 	}
 }
