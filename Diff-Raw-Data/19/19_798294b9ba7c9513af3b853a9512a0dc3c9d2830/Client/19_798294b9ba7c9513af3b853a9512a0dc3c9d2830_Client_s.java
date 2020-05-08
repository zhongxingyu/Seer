 /************************************************************
 
 Program:      Stock-Tracker
 
 Files:        Client.java; Account.java; Stock.java;
 
 Package:	  au.com.bytecode.opencsv
 
 Function:     Keep track of stock accounts with fictional money
 
 Author:       Mike Melchione (mikemelch)
 
 GitHub:		  https://github.com/mikemelch/Stock-Tracker.git
 
 ************************************************************/
 
 import java.io.BufferedReader;
import java.io.Console;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.*;
 
 import account.Account;
 import account.AccountService;
 import account.CSVAccountRepository;
 import au.com.bytecode.opencsv.CSVReader;
 import stock.StockService;
 
 public class Client {
     private static BufferedReader consoleReader;
 
     private static AccountService accountService;
     private static StockService stockService;
 	
 	public static void adminView(Account account) throws IOException{
 		int choice = 0;
 
         accountService.updateAccount(account);
 
 		System.out.println("\nWELCOME " + account.getUsername());
 		System.out.println("\nWhat can I help you with today?");
 		while(choice != 9){
 			try{
 				System.out.println("\n\t~~~~MENU~~~~");
 				System.out.println("\t1. Check stock");
 				System.out.println("\t2. View balance");
 				System.out.println("\t3. Buy stock");
 				System.out.println("\t4. Sell stock");
 				System.out.println("\t5. Show owned stocks");
 				System.out.println("\t6. Show today's account performance");
 				System.out.println("\t7. Update all accounts");
 				System.out.println("\t8. Clear account data");
 				System.out.println("\t9. Quit");
 				System.out.print("\tEnter choice: ");
 				choice = Integer.parseInt(consoleReader.readLine());
 				
 				if(choice == 1){
 					checkStock();
 				}
 				else if(choice == 2){
 					System.out.println("Your current balance is: $" + account.getBalance());
 				}
 				else if(choice == 3){
 					buyStock(account);
 				}
 				else if(choice == 4){
 					account.showStocks();
 					sellStock(account);
 				}
 				else if(choice == 5){
 					account.showStocks();
 				}
 				else if(choice == 6){
 					account.showAccountPerformance();
 				}
 				else if(choice == 7){
 					accountService.updateAllAccounts();
 					System.out.println("All accounts successfully updated");
 				}
 				else if(choice == 8){
 					System.out.print("Enter password to continue (This cannot be undone!): ");
 					if(consoleReader.readLine().equals("admin")){
 						accountService.clearAccountData();
 					}
 				}
 			}catch(Exception e){
 				System.out.println("Error! Invalid input!");
                 e.printStackTrace();
             }
 		}	
 	}
 	
 	public static void sellStock(Account account) throws IOException{
 		try{
 			System.out.print("\nEnter the ticker of the stock you wish to sell: ");
 			String ticker = consoleReader.readLine();
 			System.out.print("\nEnter the number of shares you wish to sell: ");
 			int shares = Integer.parseInt(consoleReader.readLine());
 
             stockService.sellStock(account, ticker, shares);
 		}
 		catch(Exception e){
 			System.out.println("Error! Invalid input!");
 		}
 
 	}
 	
 	public static void buyStock(Account account) throws IOException{
 		try{
 			System.out.print("\nEnter the ticker of the stock you want to purchase: ");
 			String ticker = consoleReader.readLine();
 			double price = checkStock(ticker);
 			
 			System.out.print("\nEnter the number of shares you want to purchase: ");
 			int shares = Integer.parseInt(consoleReader.readLine());
 			
 			while(shares <= 0){
 				System.out.print("\nERROR! Cannot buy " + shares + "\nEnter the number of shares you want to purchase: ");
 				shares = Integer.parseInt(consoleReader.readLine());
 			}
 			
 			if(account.getBalance() >= (shares * price)){
                 stockService.buyStock(account, ticker, price, shares);
 			}
 			else{
 				System.out.println("You have insufficient funds for this transaction!");
 			}
 		}
 		catch(Exception e){
 			System.out.println("Error! Invalid input!");
 		}
 		
 		
 	}
 	public static double checkStock(String ticker) throws MalformedURLException, UnsupportedEncodingException, IOException{
 		
 		Reader changed;
 		try{
 			if(ticker.equalsIgnoreCase("sp500")){
 				fileRead("sp500.txt");
 			}
 			else if(ticker.equalsIgnoreCase("amex")){
 				fileRead("amex.txt");
 			}
 			else if(ticker.equalsIgnoreCase("nasdaq")){
 				fileRead("nasdaq.txt");
 			}
 			else{
 				InputStream input = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + ticker.toUpperCase() + "&f=nsl1ocghjkr").openStream();
 				changed = new InputStreamReader(input, "UTF-8");		
 			
 			    CSVReader reader = new CSVReader(changed);
 			    String [] nextLine;
 			    double price = 0;
 			    while ((nextLine = reader.readNext()) != null) {
 			        System.out.println("\nCompany: " + nextLine[0] + "(" + nextLine[1] + ")\n");
 			        System.out.println("\tPrice: " + nextLine[2]);
 			        price = Double.parseDouble((nextLine[2]));
 			        System.out.println("\tOpen: " + nextLine[3]);
 			        System.out.println("\tChange: " + nextLine[4]);
 			        System.out.println("\tDay's Low: " + nextLine[5]);
 			        System.out.println("\tDay's High: " + nextLine[6]);
 			        System.out.println("\t52 Week Low: " + nextLine[7]);
 			        System.out.println("\t52 Week High: " + nextLine[8]);
 			        System.out.println("\tP/E Ratio: " + nextLine[9]);
 			    }
 			    reader.close();
 			    return price;
 			}
 		}
 		catch(Exception e){
 			System.out.println("Error! Invalid input!");
 			return 0;
 		}
 		
 		return 0;
 		
 
 	}
 	public static void checkStock() throws MalformedURLException, UnsupportedEncodingException, IOException{
 		
 		Reader changed;
 		try{
 			System.out.print("Enter the ticker symbol to recieve info: ");
 			String ticker = (consoleReader.readLine());
 			
 			if(ticker.equalsIgnoreCase("sp500")){
 				fileRead("sp500.txt");
 			}
 			else if(ticker.equalsIgnoreCase("amex")){
 				fileRead("amex.txt");
 			}
 			else if(ticker.equalsIgnoreCase("nasdaq")){
 				fileRead("nasdaq.txt");
 			}
 			else{
 				InputStream input = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + ticker.toUpperCase() + "&f=nsl1ocghjkr").openStream();
 				changed = new InputStreamReader(input, "UTF-8");		
 			
 			    CSVReader reader = new CSVReader(changed);
 			    String [] nextLine;
 			    
 			    while ((nextLine = reader.readNext()) != null) {
 			        System.out.println("\nCompany: " + nextLine[0] + "(" + nextLine[1] + ")\n");
 			        System.out.println("\tPrice: " + nextLine[2]);
 			        System.out.println("\tOpen: " + nextLine[3]);
 			        System.out.println("\tChange: " + nextLine[4]);
 			        System.out.println("\tDay's Low: " + nextLine[5]);
 			        System.out.println("\tDay's High: " + nextLine[6]);
 			        System.out.println("\t52 Week Low: " + nextLine[7]);
 			        System.out.println("\t52 Week High: " + nextLine[8]);
 			        System.out.println("\tP/E Ratio: " + nextLine[9]);
 			    }
 			    reader.close();
 			}
 		}
 		catch(Exception e){
 			System.out.println("Error! Invalid input!");
 		}
 		
 
 	}
 	public static void accountPage(Account account) throws IOException{
 		int choice = 0;
 
         accountService.updateAccount(account);
 
 		System.out.println("WELCOME " + account.getUsername());
 		System.out.println("\nWhat can I help you with today?");
 		
 			while(choice != 7){
 				try{
 					System.out.println("\n\t~~~~MENU~~~~");
 					System.out.println("\t1. Check stock");
 					System.out.println("\t2. View balance");
 					System.out.println("\t3. Buy stock");
 					System.out.println("\t4. Sell stock");
 					System.out.println("\t5. Show owned stocks");
 					System.out.println("\t6. Show today's account performance");
 					System.out.println("\t7. Quit");
 					System.out.print("\tEnter choice: ");
 					choice = Integer.parseInt(consoleReader.readLine());
 					
 					if(choice == 1){
 						checkStock();
 					}
 					else if(choice == 2){
 						System.out.println("Your current balance is:  $" + account.getBalance());
 					}
 					else if(choice == 3){
 						buyStock(account);
 					}
 					else if(choice == 4){
 						account.showStocks();
 						sellStock(account);
 					}
 					else if(choice == 5){
 						account.showStocks();
 					}
 					else if(choice == 6) {
 						account.showAccountPerformance();
 					}		
 				}
 				catch(Exception e){
 					System.out.println("Error! Invalid input!");
 				}
 		}
 
 	}
 	public static void signIn() throws IOException, InterruptedException, NoSuchAlgorithmException{
		Console consReader = System.console();
		char[] userCred = null;

 		System.out.print("Enter your username OR 1 to register: ");
 		String user = consoleReader.readLine();
 		
 		if(user.equals("1")){
 			System.out.print("Enter desired username: ");
 			String newuser = consoleReader.readLine();
 			
 			System.out.print("Enter desired password: ");
			
			// Read input and do not echo
			userCred = consReader.readPassword();
			String newpass = new String(userCred);
			userCred = null;
 
 			if(accountService.accountExists(newuser)){
 				System.out.println("This username is already taken. Please try again later.");
 				System.exit(0);
 			}
 			else {
 			System.out.println("Thank you for registering! You have been given $100000. Invest it wisely!");
                 		Account account = Account.create(newuser, newpass);
                 		accountService.addAccount(account);
 				accountPage(account);
 			}
 		}
 		else{
 			System.out.print("Enter your password: ");
	
			// Read input and do not echo
			userCred = consReader.readPassword();
			String pass = new String(userCred);		
			userCred = null;		
 	
 			System.out.println("Checking login details...");
 			Thread.sleep(1000);
 
             Account a = accountService.getAccount(user, pass);
 			if(null != a){
 				if(a.getUsername().equals("admin"))
 					adminView(a);
 				else{
 					System.out.println("Access granted.\n");
 					accountPage(a);
 				}
 				
 			}
 			else{
 				System.out.println("Access denied.");
 				System.exit(0);
 			}
 		}
 	}
 	public static void fileRead(String name) throws MalformedURLException, UnsupportedEncodingException, IOException{
 		Reader changed;
 		BufferedReader inFile = new BufferedReader(new FileReader(name));
 		String url = "http://finance.yahoo.com/d/quotes.csv?s=";
 		int i = 0;
 
 		while(inFile.ready()){
 			
 			url += (inFile.readLine() + "+"); 			
 			i++;
 			if(i == 200){
 				InputStream input = new URL(url.substring(0, url.length()) + "&f=nsl1ocghjkr").openStream();
 				changed = new InputStreamReader(input, "UTF-8");
 			
 				CSVReader reader = new CSVReader(changed);
 			    String [] nextLine;
 			    
 			    while ((nextLine = reader.readNext()) != null) {
 			        System.out.println("\nCompany: " + nextLine[0] + "(" + nextLine[1] + ")\n");
 			        System.out.println("\tPrice: " + nextLine[2]);
 			        System.out.println("\tOpen: " + nextLine[3]);
 			        System.out.println("\tChange: " + nextLine[4]);
 			        System.out.println("\tDay's Low: " + nextLine[5]);
 			        System.out.println("\tDay's High: " + nextLine[6]);
 			        System.out.println("\t52 Week Low: " + nextLine[7]);
 			        System.out.println("\t52 Week High: " + nextLine[8]);
 			        System.out.println("\tP/E Ratio: " + nextLine[9]);
 			    }
 			    url = "http://finance.yahoo.com/d/quotes.csv?s=";
 			    i = 0;
 			    reader.close();
 			}
 		}
 		
 		inFile.close();
 		
 		InputStream input = new URL(url.substring(0, url.length()) + "&f=nsl1ocghjkr").openStream();
 		changed = new InputStreamReader(input, "UTF-8");
 	
 		CSVReader reader = new CSVReader(changed);
 	    String [] nextLine;
 	    
 	    while ((nextLine = reader.readNext()) != null) {
 	        System.out.println("\nCompany: " + nextLine[0] + "(" + nextLine[1] + ")\n");
 	        System.out.println("\tPrice: " + nextLine[2]);
 	        System.out.println("\tOpen: " + nextLine[3]);
 	        System.out.println("\tChange: " + nextLine[4]);
 	        System.out.println("\tDay's Low: " + nextLine[5]);
 	        System.out.println("\tDay's High: " + nextLine[6]);
 	        System.out.println("\t52 Week Low: " + nextLine[7]);
 	        System.out.println("\t52 Week High: " + nextLine[8]);
 	        System.out.println("\tP/E Ratio: " + nextLine[9]);
 	    }
 	    reader.close();
 	}
 	
 	public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
         accountService = new AccountService(new CSVAccountRepository("accounts.csv"));
         stockService = new StockService(accountService);
 		consoleReader = new BufferedReader(new InputStreamReader(System.in));
 		signIn();
 	}
 
 }
