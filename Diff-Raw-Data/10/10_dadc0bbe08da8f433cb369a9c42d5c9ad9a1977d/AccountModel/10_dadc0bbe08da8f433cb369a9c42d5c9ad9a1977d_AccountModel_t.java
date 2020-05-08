 package act.model;
 
 import java.io.*;
 import java.lang.Exception;
 import java.util.ArrayList;
 import java.util.Scanner;
 import act.view.AccountView;
 import java.util.Collections;
 
 /**
  * Model class for the Account program handles all functionality of Application
  * @author Alex
  */
 public class AccountModel extends AbstractModel{
 	
 //	public PriorityQueue<Account> accounts = new PriorityQueue<Account>(100, new AccountComparator());
 	protected ArrayList<Account> accounts = new ArrayList<Account>();
 	public Account currentAccount;
 	public String inputFile;
 	public static final double EURO = 0.77;
 	public static final double YEN = 93.57;
 	public static final double USD = 1.0;
 	private double currentRate = USD;
 	public int stopDepositAgents = 0;
 	public int stopWithdrawAgents = 0;
 	public int depositOps = 0;
 	public int withdrawOps = 0;
 	public String state = "running";
 	
 	
 	/**
 	 * Initializes model to contain accounts read from input file
 	 * @param args
 	 */
 	public AccountModel(String [] args){
 		readAccounts(args[0]);
 	}
 	
 	public ArrayList<Account> getAccounts(){
 		return accounts;
 	}
 	
 	/**
 	 * this function sets the current currency conversion rate
 	 * @param selection
 	 */
 	public void setcurrentRate(String selection){
 		if(selection == AccountView.EUROS){
 			currentRate = EURO;
 		}else if(selection == AccountView.USD){
 			currentRate = USD;
 		}else if(selection == AccountView.YEN){
 			currentRate = YEN;
 		}
 	}
 	
 	/**
 	 * This function sets the current account tho the account selected
 	 * @param act
 	 */
 	public void setCurrentAccount(Account act){
 		this.currentAccount = act;
 	}
 	
 /**
  * returns the current account
  * @return
  */
 	public Account getCurrentAccount(){
 		return currentAccount;
 	}
 	
 	/**
 	 * If amount is valid, deposits ammount in customers balance based on current transfer rate
 	 * else throws exception
 	 * @param amt
 	 * @return 
 	 * @throws Exception
 	 */
 	public void deposit(String amt) throws Exception{
 		
 		if( !isValid(amt)){
 			throw new Exception("Input must be number");
 		}else{
 			double amount = Double.parseDouble(amt);
 			currentAccount.setBalance(currentAccount.getBalance() + amount*currentRate);
 			depositOps++;
 			ModelEvent e = new ModelEvent((Object)this, 1, "changed", currentAccount.getBalance(), state, depositOps);
 			notifyChanged(e);
 			notifyAll();
 		}
 	}
 	
 	public synchronized void threadDeposit(String amt) throws Exception{
 		deposit(amt);
 	}
 	
 	/**
 	 * Changes balance based on amount if balance would be negative then exeption is thrown and 
 	 * withdrawal is not performed.
 	 * @param amt
 	 * @throws Exception
 	 */
 	public void withdraw(String amt) throws Exception{
 		
 			double amount = Double.parseDouble(amt);
 			if( !isValid(amt) ){
 				throw new Exception("Input must be number");
 			}else if(insufficientFunds(amount)){
 				throw new Exception("Insufficient Funds: Amount to withdraw is " + -(currentAccount.balance - amount*currentRate) + " over the current balance of " + currentAccount.balance);
 			}else{
 				currentAccount.setBalance(currentAccount.getBalance() - amount*currentRate);
 				withdrawOps++;
 //				ModelEvent e = new ModelEvent((Object)this, 1, "changed", currentAccount.getBalance());
 				ModelEvent e = new ModelEvent((Object)this, 1, "changed", currentAccount.getBalance(), state, withdrawOps);
 				notifyChanged(e);
 			}
 	}
 	
 	public synchronized void threadWithdraw(String amt) throws Exception{
 		while(currentAccount.getBalance() - Integer.parseInt(amt) < 0) wait();
 		withdraw(amt);
 		notifyAll();
 	}
 	
 	/**
 	 * checks if amount passed will cause currentBalance to go below zero
 	 * @param amount
 	 * @return
 	 */
 	private boolean insufficientFunds(double amount){
 		
 		if( (currentAccount.balance - amount*currentRate <= 0)){
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks s to see if it is a valid string to be converted to a currency amount
 	 * @param s
 	 * @return
 	 */
 	private boolean isValid(String s){
 		char t = '0';
 		for (int i = 0; i<s.length(); i++){
 			t = s.charAt(i);
 			if( !Character.isDigit(t) && t != '.' ){
 				return false;
 			}
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * writes all account information to file specified by user.
 	 */
 	public void save(){
 		
 		try {
 			FileWriter fstream = new FileWriter(inputFile);
 			BufferedWriter out = new BufferedWriter(fstream);
 			
 			for (Account e : accounts){
 				out.write(e.getID() + ",");
 				out.write(e.getName() + ",");
 				out.write(String.format("%.2f",e.getBalance()) + '\n');
 			}
 			out.close();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * When program exits, save first
 	 */
 	public void exit(){
 		save();
 	}
 	
 	
 	/**
 	 * readAccounts is for the initialization of the JComboBox that holds the accounts.
 	 * 
 	 * @param args
 	 * @return String []
 	 */
 	private void readAccounts(String input){
 		inputFile = new String(input);
         // Location of file to read
         File file = new File(input);
         String [] temp;
         int ID;
         String name;
         double balance;
                 
         try {
             Scanner scanner = new Scanner(file);
  
             while (scanner.hasNextLine()) {
                 temp = ((String)scanner.nextLine()).split("[,\n]");
                 boolean repeat = false;
                 ID = Integer.parseInt(temp[0]);
                 name = temp[1];
                 balance = Double.parseDouble(temp[2]);
                 for (Account e : accounts){
                 	if(e.getID() == ID){
                 		repeat = true;
                 		System.out.println("found repeat");
                 	}
                 }
                 if(repeat == false){
                 	accounts.add(new Account(ID, name, balance));
                 }
                 repeat = false;
                 
             }
             
             scanner.close();
             Collections.sort(accounts);
             
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
 	}
 	
 //	private class AccountComparator implements Comparator<Account>{
 //		public int compare(Account a1, Account a2){
 //			if(a1.ID < a2.ID)
 //				return -1;
 //			else if (a1.ID > a2.ID)
 //				return 1;
 //			else 
 //				return 0;
 //		}
 //	}
 }
