 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.teag.EstatePlan;
 
 import com.teag.sc.utils.LineObject;
 import com.teag.estate.*;
 
 /**
  *
  * @author Paul Stay
  * Specific Split Dollar tool for Marc Sheridan
  * Set up the cash flow for the tool!
  * We have the specifics here, instead in the tool.....
  *
  * The process is that 1) we liquidate $4m in muni bonds
  * We add a gift, and then loan money at the apr interest rate of 3.4%
  * until death. We purchase a $20M life insurance policy, at death
  * the loan is paid back to the estate, and the family gets the rest of the life insurance
  *
  * Premiums
 *    Year 1  $1.426M
 *    Year 2  $1.426M
 *    Year 3  $ .800M
  *
  * Gift
  *    Year 1  $400K
  */
 public class SplitDollarCashFlow extends EstatePlanSqlBean {
 
     double cashFlow[] = new double[61];
     double disbursement[] = new double[61];
     double networth[] = new double[61];
     double toFamily[] = new double[61];
 
 
     SplitDollarTool splitDollarTool;
     double afrMidTerm = .034;    // Oct 09 AFR mid term rate
     double[] loanAmount = new double[60];
    double[] premiumPayments = {1426000, 1426000, 1075000};
     double gift = 400000;
     double deathBenefit = 20000000;
 
     public void processSplitDollar() {
         loanAmount[0] = premiumPayments[0];
         for (int i = 1; i < 60; i++) {
             double addAmount = 0;
             if (i == 1) {
                 addAmount = premiumPayments[1];
             }
             if (i == 2) {
                 addAmount = premiumPayments[2];
             }
             loanAmount[i] = loanAmount[i - 1] * (1 + afrMidTerm) + addAmount;
         }
 
         genCashFlow();
         genDisburse();
         genNetWorth();
         genToFamily();
     }
 
     public void genCashFlow() {
 
         double balance = 4000000;
         balance = balance - (premiumPayments[0] + gift);
 
         cashFlow[0] = balance * .05;
         balance = balance - premiumPayments[1];
         cashFlow[1] = balance * .05;
         cashFlow[2] = 0;
     }
 
     public void genDisburse() {
        disbursement[0] = premiumPayments[0];
        disbursement[1] = premiumPayments[1];
        disbursement[2] = premiumPayments[2];
     }
 
     public void genNetWorth() {
         for(int i=0; i < 60; i++){
             networth[i] = loanAmount[i];
         }
     }
 
     public void genToFamily() {
         for(int i=0; i < 60; i++){
             toFamily[i] = deathBenefit - loanAmount[i];
         }
     }
 
     public static void main(String args[]) {
         SplitDollarCashFlow sdc = new SplitDollarCashFlow();
         sdc.processSplitDollar();
         java.text.DecimalFormat df = new java.text.DecimalFormat("###,###,###.##");
         for (int i = 0; i < 50; i++) {
             System.out.println("I: " + i + "   " + df.format(sdc.loanAmount[i]));
         }
     }
 
     public double getAfrMidTerm() {
         return afrMidTerm;
     }
 
     public void setAfrMidTerm(double afrMidTerm) {
         this.afrMidTerm = afrMidTerm;
     }
 
     public double[] getCashFlow() {
         return cashFlow;
     }
 
     public void setCashFlow(double[] cashFlow) {
         this.cashFlow = cashFlow;
     }
 
     public double getDeathBenefit() {
         return deathBenefit;
     }
 
     public void setDeathBenefit(double deathBenefit) {
         this.deathBenefit = deathBenefit;
     }
 
     public double[] getDisbursement() {
         return disbursement;
     }
 
     public void setDisbursement(double[] disbursement) {
         this.disbursement = disbursement;
     }
 
     public double getGift() {
         return gift;
     }
 
     public void setGift(double gift) {
         this.gift = gift;
     }
 
     public double[] getLoanAmount() {
         return loanAmount;
     }
 
     public void setLoanAmount(double[] loanAmount) {
         this.loanAmount = loanAmount;
     }
 
     public double[] getNetworth() {
         return networth;
     }
 
     public void setNetworth(double[] networth) {
         this.networth = networth;
     }
 
     public double[] getPremiumPayments() {
         return premiumPayments;
     }
 
     public void setPremiumPayments(double[] premiumPayments) {
         this.premiumPayments = premiumPayments;
     }
 
     public double[] getToFamily() {
         return toFamily;
     }
 
     public void setToFamily(double[] toFamily) {
         this.toFamily = toFamily;
     }
 
     
     
 }
