 
 public class KivaLoans {
 	KivaHeader header;
 	Loan [] loans;
 	
 	class Loan {
 		  String type="loan";
		  String id;      
 		  String name;     
		  Description description;   
 		  String status;    
 		  String funded_amount;   
 		  String basket_amount;
 		  String paid_amount;     
 		  String activity;
 		  String sector;
 		  String use;
 		  String delinquent;
 		  Location location;
 		  String partner_id;
 		  String posted_date;
 		  String planned_expiration_date;
 		  String loan_amount;
 		  String currency_exchange_loss_amount;
 		  Borrower[] borrowers;
 		  Terms terms;
 		  Payment[] payments;
 		  
 		
 	}
 	class Location {
 		  String country_code;
 		  String country;
 		  String town;
 	
 	}
 	class Borrower {
 		  String first_name;
 		  String last_name;
 		  String picture;
 	
 	}
 
 	class Terms {
 		  String disbursal_date;
 		  String disbursal_currency;
 		  String disbursal_amount;
 		  String loan_amount;
 		  LocalPayment[] local_payments;
 		  ScheduledPayment[] schedule_payments;
 		  LossLiability loss_liability;
 		  
 		  class LocalPayment{
 			  	String due_date;
 			  	String amount;
 		  }	
 		  class ScheduledPayment{
 			  	String due_date;
 			  	String amount;
 		  }	
 		  class LossLiability{
 			  	String nonpayment;
 			  	String currency_exchange;
 			  	String currency_exchange_coverage_rate;
 		  }
 	}
 	class Payment{
 		String amount;
 		String local_amount;
 		String processed_date;
 		String settlement_date;
 		String rounded_local_amount;
 		String currency_exchange_loss_amount;
 		String payment_id;
 		String comment;
 		
 		
 	}
	class Description {
 		  String disbursal_date;
 		  String disbursal_currency;
 		  String disbursal_amount;
 		  String loan_amount;
 		  LocalPayment[] local_payments;
 		  ScheduledPayment[] schedule_payments;
 		  LossLiability loss_liability;
 		  
 		  class LocalPayment{
 			  	String due_date;
 			  	String amount;
 		  }	
 		  class ScheduledPayment{
 			  	String due_date;
 			  	String amount;
 		  }	
 		  class LossLiability{
 			  	String nonpayment;
 			  	String currency_exchange;
 			  	String currency_exchange_coverage_rate;
 		  }
 	}
 	
 }
