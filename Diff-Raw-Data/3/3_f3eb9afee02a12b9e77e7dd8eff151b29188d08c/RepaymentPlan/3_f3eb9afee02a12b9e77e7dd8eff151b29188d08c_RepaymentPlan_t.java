 package de.puzzles.core.domain;
 
 import java.io.Serializable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hm
  * Date: 08.03.13
  * Time: 15:29
  * To change this template use File | Settings | File Templates.
  */
 public class RepaymentPlan implements Serializable {
 
     private int duration;
     private double amount;
     private double interest;
     private double rate;
     private int creditRequestId;
 
     private double[] repaymentRates;
     private double[] interestPayments;
 
     public double getAmount() {
         return amount;
     }
 
     public void setAmount(double amount) {
         this.amount = amount;
     }
 
     public int getCreditRequestId() {
         return creditRequestId;
     }
 
     public void setCreditRequestId(int creditRequestId) {
         this.creditRequestId = creditRequestId;
     }
 
     public int getDuration() {
         return duration;
     }
 
     public void setDuration(int duration) {
         this.duration = duration;
     }
 
     public double getInterest() {
         return interest;
     }
 
     public void setInterest(double interest) {
         this.interest = interest;
     }
 
     public double[] getInterestPayments() {
         return interestPayments;
     }
 
     public void setInterestPayments(double[] interestPayments) {
         this.interestPayments = interestPayments;
     }
 
     public double getRate() {
         return rate;
     }
 
     public void setRate(double rate) {
         this.rate = rate;
     }
 
     public double[] getRepaymentRates() {
         return repaymentRates;
     }
 
     public void setRepaymentRates(double[] repaymentRates) {
         this.repaymentRates = repaymentRates;
     }
 
     public int returnDuration() {
         return this.duration;
     }
 
     public double[] getRestDebtAmount() {
         return restDebtAmount;
     }
 
     public void setRestDebtAmount(double[] restDebtAmount) {
         this.restDebtAmount = restDebtAmount;
     }
 
     private double[] restDebtAmount;
 
     private double calculateRate() {
         double rate = amount * ((interest * Math.pow(interest + 1, duration)) / (Math.pow(1 + interest, duration) - 1));
         return rate;
     }
 
     public int calculateDuration() {
         interest = interest / 100;
         double tempDuration = (-(Math.log(1 - (interest * amount) / rate) / Math.log(1 + interest)));
         int duration = 0;
         if (tempDuration > 0) {
            duration = (int) tempDuration;
            //duration = (int) tempDuration + 1;
         }
         return duration;
     }
 
     private double[] calculateRestDebtAmount() {
         double[] restDebtAmount = new double[duration+1];
         restDebtAmount[0]= amount;
         for (int i = 0; i < duration; i++){
             restDebtAmount[i] = amount*((Math.pow(1+interest,duration)-Math.pow(1+interest,i))/(Math.pow(1+interest,duration)-1));
         }
         return restDebtAmount;
     }
 
     private double[] calculateInterestPayments(){
         double[] interestPayments = new double[duration+1];
         interestPayments[0]=0.0;
         double[] restDebtAmount = calculateRestDebtAmount();
         for (int i = 0; i < duration; i++){
             interestPayments[i]=restDebtAmount[i]*interest;
         }
         return interestPayments;
 
     }
 
     private double[] calculateRepaymentRates(){
         for (int i = 0; i< duration; i++){
             repaymentRates[i]= rate - interestPayments[i];
         }
         return repaymentRates;
     }
 
     public void generateRepaymentPlan(){
         interest = interest /100;
         repaymentRates = new double[duration];
         interestPayments = calculateInterestPayments();
         restDebtAmount = calculateRestDebtAmount();
         rate = calculateRate();
         calculateRepaymentRates();
 
     }
 
 }
 
