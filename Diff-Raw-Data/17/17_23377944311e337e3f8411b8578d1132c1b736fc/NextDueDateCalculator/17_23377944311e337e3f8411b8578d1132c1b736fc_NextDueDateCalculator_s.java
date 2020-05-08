 package org.gsoft.phoenix.buslogic.repayment;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.gsoft.phoenix.domain.loan.Loan;
 import org.gsoft.phoenix.domain.loan.LoanEvent;
 import org.gsoft.phoenix.domain.loan.LoanEventType;
 import org.gsoft.phoenix.domain.loan.LoanTypeProfile;
 import org.gsoft.phoenix.repositories.loan.LoanEventRepository;
 import org.gsoft.phoenix.repositories.loan.LoanTypeProfileRepository;
 import org.joda.time.DateTime;
 import org.springframework.stereotype.Component;
 
 @Component
 public class NextDueDateCalculator {
 	@Resource
 	private LoanEventRepository loanEventRepository;
 	@Resource
 	private LoanTypeProfileRepository loanTypeProfileRepository;
 	
 	public void updateNextDueDate(Loan loan){
 		loan.setNextDueDate(loan.getInitialDueDate());
 		LoanTypeProfile ltp = loanTypeProfileRepository.findOne(loan.getEffectiveLoanTypeProfileID());
 		List<LoanEvent> paymentsForLoan = loanEventRepository.findAllLoanEventsOfTypeForLoan(loan.getLoanID(), LoanEventType.PAYMENT_APPLIED);
 		Collections.reverse(paymentsForLoan);
 		BigDecimal accumulatedPaymentAmount = BigDecimal.ZERO;
 		for(LoanEvent paymentEvent:paymentsForLoan){
 			if(!paymentEvent.getEffectiveDate().before(new DateTime(loan.getNextDueDate()).minusDays(ltp.getPrepaymentDays()).toDate())){
 				accumulatedPaymentAmount = accumulatedPaymentAmount.add(paymentEvent.getLoanTransaction().getTransactionNetAmount().negate());
 				if(accumulatedPaymentAmount.compareTo(new BigDecimal(loan.getMinimumPaymentAmount())) >= 0){
					loan.setNextDueDate(new DateTime(loan.getInitialDueDate()).plusMonths(accumulatedPaymentAmount.divideToIntegralValue(new BigDecimal(loan.getMinimumPaymentAmount())).intValue()).toDate());
					accumulatedPaymentAmount = accumulatedPaymentAmount.remainder(new BigDecimal(loan.getMinimumPaymentAmount()));
 				}
 			}
 		}
 	}
 }
