 package nl.sogyo.boilerplate.domain;
 
 import javax.persistence.Entity;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 
 @Entity
 public class SavingsAccount extends Account {
 
 	@Min(0)
 	@Max(100)
 	@NotNull
 	private Double interestRate;
 
 	public Double getInterestRate() {
 		return interestRate;
 	}
 
 	public void setInterestRate(Double interestRate) {
 		this.interestRate = interestRate;
 	}
 	
 	public void calculateInterest() {
 		double oldBalance = this.getBalance();
 		double interest = (oldBalance / 100) * this.getInterestRate();
 		this.setBalance(oldBalance + interest);
 	}
 }
