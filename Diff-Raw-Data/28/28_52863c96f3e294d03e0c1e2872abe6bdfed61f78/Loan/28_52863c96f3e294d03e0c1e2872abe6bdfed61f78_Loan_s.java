 package Helper;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Sean
  * Date: 3/11/13
  * Time: 12:11 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Loan {
     /*
         4 vars
         Gets/Sets
      */
     int loanAmount;
     int interest;
     int interestAmount;
    int firstYearPayment;
     int monthlyPayment;
 
    public Loan(int loanAmt, int interestPercent, int firstYear, int monthlyPayment){
         this.loanAmount = loanAmt;
         this.interest = interestPercent;
        this.firstYearPayment = firstYear;
         this.monthlyPayment = monthlyPayment;
         calculateInterest(interestPercent, loanAmt);
     }
 
     public int getLoanAmount() {
         return loanAmount;
     }
 
     public void setLoanAmount(int loanAmount) {
         this.loanAmount = loanAmount;
     }
 
     public int getInterest() {
         return interest;
     }
 
     public void setInterest(int interest) {
         this.interest = interest;
     }
 
    public int getFirstYearPayment() {
        return firstYearPayment;
    }

    public void setFirstYearPayment(int firstYearPayment) {
        this.firstYearPayment = firstYearPayment;
    }

     public int getMonthlyPayment() {
         return monthlyPayment;
     }
 
     public void setMonthlyPayment(int monthlyPayment) {
         this.monthlyPayment = monthlyPayment;
     }
 
     private void calculateInterest(int interestPercentage, int amount){
         double percentage = interestPercentage / 100;
         double temp =  amount * percentage;
         interestAmount = (int) temp;
     }
 }
