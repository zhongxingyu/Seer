 package com.sk.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Basic;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name="credit_card_profile")
 public class CreditCardProfile extends BaseEntity implements Comparable<CreditCardProfile> {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Basic
 	private String vendor;
 	
 	@OneToMany(cascade={CascadeType.ALL,CascadeType.MERGE})
 	private List<InstallmentPlan> installmentPlans = new ArrayList<InstallmentPlan>();
 	
 	@Basic
 	private String binDigits;
 
 	public String getBinDigits() {
 		return binDigits;
 	}
 
 	public void setBinDigits(String binDigits) {
 		this.binDigits = binDigits;
 	}
 
 	public String getVendor() {
 		return vendor;
 	}
 
 	public void setVendor(String vendor) {
 		this.vendor = vendor;
 	}
 
 	public List<InstallmentPlan> getInstallmentPlans() {
 		return installmentPlans;
 	}
 
 	public void setInstallmentPlans(List<InstallmentPlan> installmentPlans) {
 		this.installmentPlans = installmentPlans;
 	}
 	
 	public void addInstallmentPlan(InstallmentPlan installmentPlan) {
 		installmentPlans.add(installmentPlan);
 	}
 	
 	public void deleteInstallmentPlan(InstallmentPlan installmentPlan) {
 		installmentPlans.remove(installmentPlan);	
 	}
 
 	public double monthlyPaymentOf(double amount, int months) {
 		InstallmentPlan planToUse = findInstallmentPlanFor(months);
 		
 		if (planToUse == null) {
 			throw new IllegalArgumentException("Missing installment plan for" + months);
 		} 
 		return planToUse.paymentFor(amount);
 	}
 
 	private InstallmentPlan findInstallmentPlanFor(int months) {
 		for (InstallmentPlan eachPlan : installmentPlans) {
 			if (eachPlan.getMonths() == months) {
 				return eachPlan;
 			}
 		}
 		return null;
 	}
 
 	public boolean issuerOf(String creditCardNo) {
 		return creditCardNo.startsWith(binDigits);
 	}
 
 	@Override
 	public int compareTo(CreditCardProfile toCompare) {
 		return vendor.compareTo(toCompare.vendor);
 	}
 	
 
 }
