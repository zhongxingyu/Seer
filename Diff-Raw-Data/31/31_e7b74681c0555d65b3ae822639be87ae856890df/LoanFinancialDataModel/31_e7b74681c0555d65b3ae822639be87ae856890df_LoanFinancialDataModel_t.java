 package org.gsoft.phoenix.web.controller.accountsummary.model;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.Date;
 
 import org.gsoft.phoenix.domain.loan.LoanType;
 import org.gsoft.phoenix.util.formatter.CurrencyInPenniesFormat;
 import org.springframework.format.annotation.DateTimeFormat;
 
 public class LoanFinancialDataModel implements Serializable{
 	private static final long serialVersionUID = 2799991603651710288L;
 
 	private Long loanID;
 	private LoanType loanType;
 	private Integer currentPrincipal;
 	private BigDecimal currentInterest;
 	private Integer currentFees;
 	private Integer minimumPaymentAmount;
 	private Date nextDueDate;
 	private Date lastPaidDate;
 	private Date repaymentStartDate;
	private Date firstDueDate;
	private Date initialDueDate;
 	private boolean selected;
 	
 	public Long getLoanID(){
 		return loanID;
 	}
 	public void setLoanID(Long loanID){
 		this.loanID = loanID;
 	}
 	public LoanType getLoanType() {
 		return loanType;
 	}
 	public void setLoanType(LoanType loanType) {
 		this.loanType = loanType;
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
 	@CurrencyInPenniesFormat
 	public Integer getMinimumPaymentAmount() {
 		return minimumPaymentAmount;
 	}
 	public void setMinimumPaymentAmount(Integer minimumPaymentAmount) {
 		this.minimumPaymentAmount = minimumPaymentAmount;
 	}
 	@DateTimeFormat(pattern="yyyy-MM-dd")
 	public Date getNextDueDate() {
 		return nextDueDate;
 	}
 	public void setNextDueDate(Date nextDueDate) {
 		this.nextDueDate = nextDueDate;
 	}
 	@DateTimeFormat(pattern="yyyy-MM-dd")
 	public Date getLastPaidDate() {
 		return lastPaidDate;
 	}
 	public void setLastPaidDate(Date lastPaidDate) {
 		this.lastPaidDate = lastPaidDate;
 	}
 	@DateTimeFormat(pattern="yyyy-MM-dd")
 	public Date getRepaymentStartDate() {
 		return repaymentStartDate;
 	}
 	public void setRepaymentStartDate(Date repaymentStartDate) {
 		this.repaymentStartDate = repaymentStartDate;
 	}
	@DateTimeFormat(pattern="yyyy-MM-dd")
	public Date getFirstDueDate() {
		return firstDueDate;
	}
	public void setFirstDueDate(Date firstDueDate) {
		this.firstDueDate = firstDueDate;
	}
	@DateTimeFormat(pattern="yyyy-MM-dd")
	public Date getInitialDueDate() {
		return initialDueDate;
	}
	public void setInitialDueDate(Date initialDueDate) {
		this.initialDueDate = initialDueDate;
	}
 	public boolean isSelected() {
 		return selected;
 	}
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 }
