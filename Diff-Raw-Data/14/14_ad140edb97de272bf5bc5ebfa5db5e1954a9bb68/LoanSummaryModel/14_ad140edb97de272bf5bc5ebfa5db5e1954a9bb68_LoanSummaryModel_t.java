 package org.gsoft.openserv.web.models;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 
 import org.gsoft.openserv.domain.loan.LoanProgram;
 import org.gsoft.openserv.web.formatter.currency.CurrencyInPenniesFormat;
 
 public class LoanSummaryModel implements Serializable{
 	private static final long serialVersionUID = -6256763851983742148L;
 	private Long loanID;
	private String accountNumber;
 	private LoanProgram loanProgram;
 	private Integer currentPrincipal;
 	private BigDecimal currentInterest;
 	private Integer currentFees;
 	private boolean selected;
 	
 	public Long getLoanID(){
 		return loanID;
 	}
 	public void setLoanID(Long loanID){
 		this.loanID = loanID;
 	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
 	public LoanProgram getLoanProgram() {
 		return loanProgram;
 	}
 	public void setLoanProgram(LoanProgram loanProgram) {
 		this.loanProgram = loanProgram;
 	}
 	@CurrencyInPenniesFormat
 	public Integer getCurrentPrincipal() {
 		return currentPrincipal;
 	}
 	public void setCurrentPrincipal(Integer currentPrincipal) {
 		this.currentPrincipal = currentPrincipal;
 	}
 	@CurrencyInPenniesFormat
 	public BigDecimal getCurrentInterest() {
 		return currentInterest;
 	}
 	public void setCurrentInterest(BigDecimal currentInterest) {
 		this.currentInterest = currentInterest;
 	}
 	@CurrencyInPenniesFormat
 	public Integer getCurrentFees() {
 		return currentFees;
 	}
 	public void setCurrentFees(Integer currentFees) {
 		this.currentFees = currentFees;
 	}
 	public boolean isSelected() {
 		return selected;
 	}
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 }
