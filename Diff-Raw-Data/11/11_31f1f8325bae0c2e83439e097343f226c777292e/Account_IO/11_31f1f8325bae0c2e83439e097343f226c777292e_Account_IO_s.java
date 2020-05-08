 import java.util.*;
 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.text.DateFormat;
 
 /**
  * 
 * @author Mark Duncan, James Sanderlin, Avi Levi, Mudrekh Goderya
  * 
  * The Account_IO class handles input and output of the current state of the program to accounts.txt
  * 
  * @version 1.0
  *
  */
 public class Account_IO 
 {
 	//This method reads data stored from a text file and prints the file contents to the console (for testing)
 	public static void Account_IO_Debug(){
 		File accountFile = new File("accounts.txt");
 		
 		try
 		{
 			Scanner in = new Scanner(accountFile);
 			//Parse the accounts file
 			while(in.hasNext())
 			{
 				//Stores the type
 				String clientFirstName = in.nextLine();
 				String clientLastName = in.nextLine();
 
 				//Stores the unique account number of the account
 				int accountId = Integer.parseInt(in.nextLine());
 				//Stores the current balance of the account
 				double balance = Double.parseDouble(in.nextLine());
 				//Stores if there is a flag on the account or not
 				boolean flag = (in.nextInt() == 0) ? false : true;
 				//Indicates if the account is commercial or residential
 				boolean isCommercial = (in.nextInt() == 0) ? false : true; in.nextLine();
 				
 				//Sets up the formatting of a date throughout the file
 				DateFormat formatter = new SimpleDateFormat("MM/dd/yy h:mm a z");
 				
 				//Reads in the date as a string, to be converted into a date object
 				String dateString = in.nextLine();
 				Date deadline = new Date();
 				try {
 					deadline = (Date)formatter.parse(dateString);
 				} catch (ParseException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 					
 				//Gets the first line of the address and stores it in String l1
 				String l1 = in.nextLine();
 				//Holds the second line of the address and stores it in String l2
 				String l2 = in.nextLine();
 				//Holds the string for the city
 				String city = in.nextLine();
 				//Holds the string for the state
 				String state = in.nextLine();
 				//Holds the string for the zipcode
 				String zip = in.nextLine();
 				
 				//Creates address object and stores its info for storing into the account object
 				Address addressParam = new Address(l1,l2,city,state,zip);
 				
 				//Variables for storing data from file for the payment object
 				double amount = 0.0;
 				String paymentType = "";
 				String paymentDate ="";
 				ArrayList<Payment> paymentHist = new ArrayList<Payment>();
 				
 				//Advances the writer past the "Payments:" delimiter. (We may decide to remove this altogether)
 				@SuppressWarnings("unused")
 				String paymentsDelimiter = in.nextLine();
 				while(!in.hasNext("end"))
 				{
 					amount = Double.parseDouble(in.nextLine());
 					paymentType = in.nextLine();
 					paymentDate = in.nextLine();
 					
 					//Creates a date object and parses the string paymentDate from the file
 					Date payDate = new Date();
 					try {
 						payDate = (Date)formatter.parse(paymentDate);
 					} catch (ParseException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					//Creates the payment object to be stored in the account object's paymentHistory list
 					Payment newPayment = new Payment(amount,paymentType,payDate);
 					paymentHist.add(newPayment);
 				}
 				//Advances the writer past the end delimiter if necessary to get new info
 				if(in.hasNext()){
 					@SuppressWarnings("unused")
 					String delimiter = in.nextLine();
 				}
 				
 					
 				if(isCommercial)
 				{
 					CommercialAccount a = new CommercialAccount(clientFirstName, clientLastName, accountId, balance, flag, deadline, addressParam);
 									  a.paymentHistory = paymentHist;
 					System.out.println("COMMERCIAL ACCOUNT");
 					System.out.println(a.clientFirstName);
 					System.out.println(a.clientLastName);
 					System.out.println(a.accountID);
 					System.out.println(a.balance);
 					System.out.println(a.flag);
 					System.out.println(a.isCommercialtoString());
 					System.out.println(a.deadline);
 					System.out.println(a.billingAddress.getCity());
 					System.out.println(a.billingAddress.getLocation1());
 					System.out.println(a.billingAddress.getLocation2());
 					System.out.println(a.billingAddress.getState());
 					System.out.println(a.billingAddress.getZip());
 						
 					for (int i = 0; i<a.paymentHistory.size();i++){
 						
 						System.out.print(a.paymentHistory.get(i).toString());					
 					}
 					
 					System.out.println("END COMMERCIAL ACCOUNT");
 				}
 				else
 				{
 					//*TODO* We are need to redo the structure here for residential and commercial account names. Right now we read everything as a name but
 					//it also needs to delineate between a company name and a name. They aren't separated right now.
 					ResidentialAccount b = new ResidentialAccount(clientFirstName, clientLastName, accountId, balance, flag, deadline, addressParam);
 									   b.paymentHistory = paymentHist;
 					System.out.println("RESIDENTIAL ACCOUNT");
 					System.out.println(b.clientFirstName);
 					System.out.println(b.clientLastName);
 					System.out.println(b.accountID);
 					System.out.println(b.balance);
 					System.out.println(b.flag);
 					System.out.println(b.isCommercialtoString());
 					System.out.println(b.deadline);
 					System.out.println(b.billingAddress.getCity());
 					System.out.println(b.billingAddress.getLocation1());
 					System.out.println(b.billingAddress.getLocation2());
 					System.out.println(b.billingAddress.getState());
 					System.out.println(b.billingAddress.getZip());
 					
 					for (int i = 0; i<b.paymentHistory.size();i++){
 						
 						System.out.print(b.paymentHistory.get(i).toString());					
 					}
 					
 					System.out.println("END RESIDENTIAL ACCOUNT");
 				}
 			}
 		}
 		catch(FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	//This method writes the account information from the accounts file to a list of accounts by reference
 		public static ArrayList<Account> AccountIn(){
 			File accountFile = new File("accounts.txt");
 			ArrayList<Account> accountlist = new ArrayList<Account>();
 			try
 			{
 				Scanner in = new Scanner(accountFile);
 				//Parse the accounts file
 				while(in.hasNext())
 				{
 					//Stores the type
 					String clientFirstName = in.nextLine();
 					String clientLastName = in.nextLine();
 
 					//Stores the unique account number of the account
 					int accountId = Integer.parseInt(in.nextLine());
 					//Stores the current balance of the account
 					double balance = Double.parseDouble(in.nextLine());
 					//Stores if there is a flag on the account or not
 					boolean flag = (in.nextInt() == 0) ? false : true;
 					//Indicates if the account is commercial or residential
 					boolean isCommercial = (in.nextInt() == 0) ? false : true; in.nextLine();
 					
 					//Sets up the formatting of a date throughout the file
 					DateFormat formatter = new SimpleDateFormat("MM/dd/yy h:mm a z");
 					
 					//Reads in the date as a string, to be converted into a date object
 					String dateString = in.nextLine();
 					Date deadline = new Date();
 					try {
 						deadline = (Date)formatter.parse(dateString);
 					} catch (ParseException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 						
 					//Gets the first line of the address and stores it in String l1
 					String l1 = in.nextLine();
 					//Holds the second line of the address and stores it in String l2
 					String l2 = in.nextLine();
 					//Holds the string for the city
 					String city = in.nextLine();
 					//Holds the string for the state
 					String state = in.nextLine();
 					//Holds the string for the zipcode
 					String zip = in.nextLine();
 					
 					//Creates address object and stores its info for storing into the account object
 					Address addressParam = new Address(l1,l2,city,state,zip);
 					
 					//Variables for storing data from file for the payment object
 					double amount = 0.0;
 					String paymentType = "";
 					String paymentDate ="";
 					ArrayList<Payment> paymentHist = new ArrayList<Payment>();
 					
 					//Advances the writer past the "Payments:" delimiter. (We may decide to remove this altogether)
 					@SuppressWarnings("unused")
 					String paymentsDelimiter = in.nextLine();
 					
 					while(!in.hasNext("end"))
 					{
 						amount = Double.parseDouble(in.nextLine());
 						paymentType = in.nextLine();
 						paymentDate = in.nextLine();
 						
 						//Creates a date object and parses the string paymentDate from the file
 						Date payDate = new Date();
 						try {
 							payDate = (Date)formatter.parse(paymentDate);
 						} catch (ParseException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						//Creates the payment object to be stored in the account object's paymentHistory list
 						Payment newPayment = new Payment(amount,paymentType,payDate);
 						paymentHist.add(newPayment);
 					}
 					
 					//Advances the writer past the end delimiter if necessary to get new info
 					if(in.hasNext()){
 						@SuppressWarnings("unused")
 						String delimiter = in.nextLine();
 					}
 					
 					//Change this to writing to the accounts reference object, not the console
 					if(isCommercial)
 					{		
 						CommercialAccount a = new CommercialAccount(clientFirstName, clientLastName, accountId, balance, flag, deadline, addressParam);
 						  				  a.paymentHistory = paymentHist;
 						  				  accountlist.add(a);
 					}
 					else
 					{
 						//*TODO* We are need to redo the structure here for residential and commercial account names. Right now we read everything as a name but
 						//it also needs to delineate between a company name and a name. They aren't separated right now.
 						ResidentialAccount b = new ResidentialAccount(clientFirstName, clientLastName, accountId, balance, flag, deadline, addressParam);
 										   b.paymentHistory = paymentHist;
 										   accountlist.add(b);
 					}
 					
 				}
 			}
 			catch(FileNotFoundException e)
 			{
 				e.printStackTrace();
 			}
 			
 			return accountlist;
 		}
 		
 		//This method persists the account information from a list by reference to a file for reading in later
 		public static void AccountOut(Collection<Account> accountlist){
 			try{
 			
 				//TODO When comfortable with the writing format here, change the file below to be the same as
 				//the input file to link up the persisting function. This will overwrite the existing accountsfile though
 				//so make sure you have a backup somewhere if we are still testing.
 			FileWriter fstream = new FileWriter("out_accounts.txt");
 			BufferedWriter out = new BufferedWriter(fstream);
 
 				for(Account a : accountlist) 
 				{	
 					out.write(a.getClientFirstName() +"\n"+a.getClientLastName() 
 													 +"\n"+a.accountID
 													 +"\n"+a.balance
 													 +"\n"+a.flag 
 													 +"\n"+a.isCommercialtoString()
 													 +"\n"+a.deadline
 													 +"\n"+a.billingAddress.getLocation1()
 													 +"\n"+a.billingAddress.getLocation2()
 													 +"\n"+a.billingAddress.getCity()
 													 +"\n"+a.billingAddress.getState()
 													 +"\n"+a.billingAddress.getZip()
 													 +"\n"
 							);
 					for (int i = 0; i<a.paymentHistory.size();i++){
 						
 						out.write(a.paymentHistory.get(i).toString());					
 					}
 					out.write("end");
 				}
 					out.close();
 			}
 			
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}	
 }
