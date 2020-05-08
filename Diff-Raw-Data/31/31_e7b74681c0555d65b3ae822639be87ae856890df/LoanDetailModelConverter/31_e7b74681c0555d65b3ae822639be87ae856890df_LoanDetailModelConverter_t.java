 package org.gsoft.phoenix.web.controller.accountsummary.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.gsoft.phoenix.domain.loan.Loan;
 import org.gsoft.phoenix.domain.loan.LoanEvent;
 import org.gsoft.phoenix.service.AccountSummaryService;
 import org.springframework.stereotype.Component;
 
 @Component
 public class LoanDetailModelConverter {
 	@Resource
 	private AccountSummaryService accountSummaryService;
 	@Resource
 	private LoanEventModelConverter loanEventModelConverter;
 	
 	public LoanDetailModel convertToModel(Loan loan){
 		LoanDetailModel model = new LoanDetailModel();
 		LoanFinancialDataModel finModel = new LoanFinancialDataModel();
 		finModel.setLoanID(loan.getLoanID());
 		finModel.setLoanType(loan.getLoanType());
 		finModel.setCurrentPrincipal(loan.getCurrentPrincipal());
 		finModel.setCurrentInterest(loan.getCurrentInterest());
 		finModel.setCurrentFees(loan.getCurrentFees());
 		finModel.setMinimumPaymentAmount(loan.getMinimumPaymentAmount());
 		finModel.setNextDueDate(loan.getNextDueDate());
 		finModel.setLastPaidDate(loan.getLastPaidDate());
 		finModel.setRepaymentStartDate(loan.getRepaymentStartDate());
		finModel.setFirstDueDate(loan.getFirstDueDate());
		finModel.setInitialDueDate(loan.getInitialDueDate());
 		List<LoanEvent> loanEvents = accountSummaryService.getAllLoanEventsForLoan(loan.getLoanID());
 		ArrayList<LoanEventModel> loanHistory = new ArrayList<LoanEventModel>();
 		for(LoanEvent loanEvent:loanEvents){
 			loanHistory.add(loanEventModelConverter.convertToModel(loanEvent));
 		}
 		model.setLoanFinancialData(finModel);
 		model.setLoanHistory(loanHistory);
 		return model;
 	}
 }
