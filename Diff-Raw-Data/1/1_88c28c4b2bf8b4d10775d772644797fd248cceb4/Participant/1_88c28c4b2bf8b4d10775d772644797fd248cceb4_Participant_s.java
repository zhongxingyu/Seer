 package billsplit.engine;
 
 public class Participant {
 	private Account associatedAccount;
 	private double balance; //always initialized to 0
 	private String name;
 	
 	Participant(){
 		associatedAccount = null;
 		balance = 0;
 		name = "Anon";
 	}
 	
 	Participant(Account associatedAccount){
 		this.associatedAccount =associatedAccount;
 		balance = 0;
 		/* dacashman - make sure this account API exists
 		 * and verify that this is the desired functionality.
 		 * Same for other constructors.
 		 */
 		name = associatedAccount.getName();
 	}
 	
 	/*
 	 * This version is for quick 'n' dirty participant with 
 	 * no associated account. 
 	 */
 	Participant(String name){
 		associatedAccount = null;
 		balance = 0;
 		this.name = name;
 	}
 	
 	/*
 	 * This version is called if user wants to define his/her
 	 * own "nickname" to user that isn't the account name
 	 */
 	Participant(Account associatedAccount, String name){
 		this.associatedAccount = associatedAccount;
		this.balance  = balance;
 		this.name = name;
 	}
 	
 	public Account getAccount(){
 		return associatedAccount;
 	}
 	
 	public void setAccount(Account associatedAccount){
 		/* dacashman - should we check to see if an account is
 		 * already associated with the participant?
 		 */
 		this.associatedAccount = associatedAccount;
 	}
 	
 	public String getName(){
 		return name;
 	}
 	
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	public double getBalance(){
 		return balance;
 	}
 	
 	/*
 	 * Adds the indicated amount (positive or negative) to the 
 	 * participant balance and returns the new result.
 	 */
 	public double addBalance(double addAmount){
 		balance +=addAmount;
 		return balance;
 	}
 	
 	
 
 }
