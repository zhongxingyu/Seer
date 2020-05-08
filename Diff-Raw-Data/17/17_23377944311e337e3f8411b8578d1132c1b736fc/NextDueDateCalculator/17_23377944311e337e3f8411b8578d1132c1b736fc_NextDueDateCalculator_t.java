 package org.gsoft.phoenix.buslogic.repayment;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Resource;
 
import org.gsoft.phoenix.buslogic.system.SystemSettingsLogic;
 import org.gsoft.phoenix.domain.loan.Loan;
 import org.gsoft.phoenix.domain.loan.LoanEvent;
 import org.gsoft.phoenix.domain.loan.LoanEventType;
 import org.gsoft.phoenix.domain.loan.LoanTypeProfile;
 import org.gsoft.phoenix.repositories.loan.LoanEventRepository;
 import org.gsoft.phoenix.repositories.loan.LoanTypeProfileRepository;
 import org.joda.time.DateTime;
import org.joda.time.Months;
 import org.springframework.stereotype.Component;
 
 @Component
 public class NextDueDateCalculator {
 	@Resource
 	private LoanEventRepository loanEventRepository;
 	@Resource
 	private LoanTypeProfileRepository loanTypeProfileRepository;
	@Resource
	private SystemSettingsLogic systemSettings;
 	
 	public void updateNextDueDate(Loan loan){
 		loan.setNextDueDate(loan.getInitialDueDate());
 		LoanTypeProfile ltp = loanTypeProfileRepository.findOne(loan.getEffectiveLoanTypeProfileID());
 		List<LoanEvent> paymentsForLoan = loanEventRepository.findAllLoanEventsOfTypeForLoan(loan.getLoanID(), LoanEventType.PAYMENT_APPLIED);
 		Collections.reverse(paymentsForLoan);
 		BigDecimal accumulatedPaymentAmount = BigDecimal.ZERO;
 		for(LoanEvent paymentEvent:paymentsForLoan){
			if(!new DateTime(loan.getNextDueDate()).minusDays(ltp.getPrepaymentDays()).toDate().before(systemSettings.getCurrentSystemDate()))
				break;
 			if(!paymentEvent.getEffectiveDate().before(new DateTime(loan.getNextDueDate()).minusDays(ltp.getPrepaymentDays()).toDate())){
 				accumulatedPaymentAmount = accumulatedPaymentAmount.add(paymentEvent.getLoanTransaction().getTransactionNetAmount().negate());
 				if(accumulatedPaymentAmount.compareTo(new BigDecimal(loan.getMinimumPaymentAmount())) >= 0){
					int monthsToAdvance = accumulatedPaymentAmount.divideToIntegralValue(new BigDecimal(loan.getMinimumPaymentAmount())).intValue();
					int monthsToCurrent = Months.monthsBetween(new DateTime(loan.getNextDueDate()), new DateTime(systemSettings.getCurrentSystemDate()).plusDays(ltp.getPrepaymentDays())).getMonths();
					monthsToAdvance = (monthsToAdvance <= monthsToCurrent)?monthsToAdvance:monthsToCurrent;
					loan.setNextDueDate(new DateTime(loan.getInitialDueDate()).plusMonths(monthsToAdvance).toDate());
					accumulatedPaymentAmount = accumulatedPaymentAmount.subtract(new BigDecimal(monthsToAdvance*loan.getMinimumPaymentAmount()));
 				}
 			}
 		}
 	}
 }
