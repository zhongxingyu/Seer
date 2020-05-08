 /************************************************************
 
 Program:      Stock-Tracker
 
 Files:        Client.java; Account.java; Stock.java;
 
 Package:	  au.com.bytecode.opencsv
 
 Function:     Keep track of stock accounts with fictional money
 
 Author:       Mike Melchione (mlm378)
 
 Environment:  JDK 7
 
 Revisions:    1.00  3/20/2013 (mlm378) First release
               1.01  3/21/2013 (mlm378) Implemented error handling & admin menu
               1.02	3/25/2013 (mlm378) Implemented MD5 secure passwords
 
 ************************************************************/
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.*;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class Client {
 	
 	public static void adminView(Account account) throws IOException{
 		DataInputStream r = new DataInputStream(System.in);
 		int choice = 0;
 		account.updateAccount();
 		System.out.println("\nWELCOME " + account.getUsername());
 		System.out.println("\nWhat can I help you with today?");
 		while(choice != 8){
 			try{
 				System.out.println("\n\t~~~~MENU~~~~");
 				System.out.println("\t1. Check stock");
 				System.out.println("\t2. View balance");
 				System.out.println("\t3. Buy stock");
 				System.out.println("\t4. Sell stock");
 				System.out.println("\t5. Show owned stocks");
 				System.out.println("\t6. Update all accounts");
 				System.out.println("\t7. Clear account data");
 				System.out.println("\t8. Quit");
 				System.out.print("\tEnter choice: ");
 				choice = Integer.parseInt(r.readLine());
 				
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
 					account.updateAllAccounts();
 					System.out.println("All accounts successfully updated");
 				}
 				else if(choice == 7){
 					System.out.print("Enter password to continue (This cannot be undone!): ");
 					if(r.readLine().equals("admin")){
 						account.clearAccountData();
 					}
 				}
 			}
 				catch(Exception e){
 					System.out.println("Error! Invalid input!");
 				}
 			}
 			
 		
 		
 	}
 	
 	public static void sellStock(Account account) throws IOException{
 		DataInputStream r = new DataInputStream(System.in);
 		try{
 			System.out.print("\nEnter the ticker of the stock you wish to sell: ");
 			String ticker = r.readLine();
 			System.out.print("\nEnter the number of shares you wish to sell: ");
 			int shares = Integer.parseInt(r.readLine());
 			
 			account.sellStock(ticker, shares);
 		}
 		catch(Exception e){
 			System.out.println("Error! Invalid input!");
 		}
 
 	}
 	
 	public static void buyStock(Account account) throws IOException{
 		DataInputStream r = new DataInputStream(System.in);
 		try{
 			System.out.print("\nEnter the ticker of the stock you want to purchase: ");
 			String ticker = r.readLine();
 			double price = checkStock(ticker);
 			
 			System.out.print("\nEnter the number of shares you want to purchase: ");
 			int shares = Integer.parseInt(r.readLine());
 			
 			while(shares <= 0){
 				System.out.print("\nERROR! Cannot buy " + shares + "\nEnter the number of shares you want to purchase: ");
 				shares = Integer.parseInt(r.readLine());
 			}
 			
 			if(account.getBalance() >= (shares * price)){
 				account.buyStock(ticker, price, shares);
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
 			if(ticker.equals("sp500") || ticker.equals("SP500")){
 				fileRead("sp500.txt");
 			}
 			else if(ticker.equals("amex") || ticker.equals("AMEX")){
 				fileRead("amex.txt");
 			}
 			else if(ticker.equals("nasdaq") || ticker.equals("NASDAQ")){
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
 		
 		DataInputStream r = new DataInputStream(System.in);
 		Reader changed;
 		try{
 			System.out.print("Enter the ticker symbol to recieve info: ");
 			String ticker = (r.readLine());
 			
 			if(ticker.equals("sp500") || ticker.equals("SP500")){
 				fileRead("sp500.txt");
 			}
 			else if(ticker.equals("amex") || ticker.equals("AMEX")){
 				fileRead("amex.txt");
 			}
 			else if(ticker.equals("nasdaq") || ticker.equals("NASDAQ")){
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
 		DataInputStream r = new DataInputStream(System.in);
 		int choice = 0;
 		account.updateAccount();
 		System.out.println("WELCOME " + account.getUsername());
 		System.out.println("\nWhat can I help you with today?");
 		
 			while(choice != 6){
 				try{
 					System.out.println("\n\t~~~~MENU~~~~");
 					System.out.println("\t1. Check stock");
 					System.out.println("\t2. View balance");
 					System.out.println("\t3. Buy stock");
 					System.out.println("\t4. Sell stock");
 					System.out.println("\t5. Show owned stocks");
 					System.out.println("\t6. Quit");
 					System.out.print("\tEnter choice: ");
 					choice = Integer.parseInt(r.readLine());
 					
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
 				}
 				catch(Exception e){
 					System.out.println("Error! Invalid input!");
 				}
 		}
 
 	}
 	public static void signIn() throws IOException, InterruptedException, NoSuchAlgorithmException{
 		DataInputStream r = new DataInputStream(System.in);
 		
 		System.out.print("Enter your username OR 1 to register: ");
 		String user = r.readLine();
 		
 		if(user.equals("1")){
 			System.out.print("Enter desired username: ");
 			String newuser = r.readLine();
 			System.out.print("Enter desired password: ");
 			String newpass = r.readLine();
 			
 			Account a = new Account();
 			
			if(a.doesUserExist(newuser)){
				System.out.println("This username is already taken. Please try again later.");
 				System.exit(0);
 			}
 			else{
 				System.out.println("Thank you for registering! You have been given $100000. Invest it wisely!");
 				a = new Account(newuser, newpass);
 				accountPage(a);
 			}
 		}
 		else{
 			System.out.print("Enter your password: ");
 			String pass = r.readLine();
 
 			System.out.println("Checking login details...");
 			Thread.sleep(1000);
 			Account a = new Account();
 
 			if(a.accountLookup(user, pass)){			
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
 		signIn();
 	}
 
 }
