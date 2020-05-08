 package org.gsoft.phoenix.web.controller.accountsummary.model;
 
 import java.math.BigDecimal;
 import java.util.Date;
 
 import org.gsoft.phoenix.util.formatter.CurrencyInPenniesFormat;
 
 public class LoanEventModel {
 	private Long loanID;
 	private String loanEventType;
 	private Date loanEventEffectiveDate;
 	private Date loanEventPostDate;
 	private BigDecimal interestAccrued;
 	private BigDecimal interestPaid;
 	private Integer principalChange;
 	private BigDecimal interestChange;
 	private Integer feesChange;
 	private Integer endingPrincipal;
 	private BigDecimal endingInterest;
 	private Integer endingFees;
 	
 	public Long getLoanID() {
 		return loanID;
 	}
 	public void setLoanID(Long loanID) {
 		this.loanID = loanID;
 	}
 	public String getLoanEventType() {
 		return loanEventType;
 	}
 	public void setLoanEventType(String loanEventType) {
 		this.loanEventType = loanEventType;
 	}
 	public Date getLoanEventEffectiveDate() {
 		return loanEventEffectiveDate;
 	}
 	public void setLoanEventEffectiveDate(Date loanEventEffectiveDate) {
 		this.loanEventEffectiveDate = loanEventEffectiveDate;
 	}
 	public Date getLoanEventPostDate() {
 		return loanEventPostDate;
 	}
 	public void setLoanEventPostDate(Date loanEventPostDate) {
 		this.loanEventPostDate = loanEventPostDate;
 	}
	@CurrencyInPenniesFormat(subPennyPrecision=4)
 	public BigDecimal getInterestAccrued() {
 		return interestAccrued;
 	}
 	public void setInterestAccrued(BigDecimal interestAccrued) {
 		this.interestAccrued = interestAccrued;
 	}
 	@CurrencyInPenniesFormat
 	public BigDecimal getInterestPaid() {
 		return interestPaid;
 	}
 	public void setInterestPaid(BigDecimal interestPaid) {
 		this.interestPaid = interestPaid;
 	}
 	@CurrencyInPenniesFormat
 	public Integer getPrincipalChange() {
 		return principalChange;
 	}
 	public void setPrincipalChange(Integer principalChange) {
 		this.principalChange = principalChange;
 	}
	@CurrencyInPenniesFormat(subPennyPrecision=4)
 	public BigDecimal getInterestChange() {
 		return interestChange;
 	}
 	public void setInterestChange(BigDecimal interestChange) {
 		this.interestChange = interestChange;
 	}
 	@CurrencyInPenniesFormat
 	public Integer getFeesChange() {
 		return feesChange;
 	}
 	public void setFeesChange(Integer feesChange) {
 		this.feesChange = feesChange;
 	}
 	@CurrencyInPenniesFormat
 	public Integer getEndingPrincipal() {
 		return endingPrincipal;
 	}
 	public void setEndingPrincipal(Integer endingPrincipal) {
 		this.endingPrincipal = endingPrincipal;
 	}
	@CurrencyInPenniesFormat(subPennyPrecision=4)
 	public BigDecimal getEndingInterest() {
 		return endingInterest;
 	}
 	public void setEndingInterest(BigDecimal endingInterest) {
 		this.endingInterest = endingInterest;
 	}
 	@CurrencyInPenniesFormat
 	public Integer getEndingFees() {
 		return endingFees;
 	}
 	public void setEndingFees(Integer endingFees) {
 		this.endingFees = endingFees;
 	}
 }
