 /*
  * 
  */
 package com.paymium.paytunia.PaytuniaAPI;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 
 import com.paymium.paytunia.PaytuniaAPI.account.Account;
 import com.paymium.paytunia.PaytuniaAPI.account.NewAccount;
 import com.paymium.paytunia.PaytuniaAPI.account.RegisterAccount;
 import com.paymium.paytunia.PaytuniaAPI.exceptions.ConnectionNotInitializedException;
 import com.paymium.paytunia.PaytuniaAPI.transaction.Invoice;
 import com.paymium.paytunia.PaytuniaAPI.transaction.ListTransaction;
import com.paymium.paytunia.PaytuniaAPI.transaction.Transaction;
 import com.paymium.paytunia.PaytuniaAPI.transfer.TransferAddressBitcoin;
 import com.paymium.paytunia.PaytuniaAPI.transfer.TransferMail;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Main.
  */
 public class Main 
 {
 	
 	/** The connection (variable essential). */
 	private static Connection connection;
 
 	/**
 	 * The main method.
 	 *
 	 * @param args the arguments
 	 */
 	public static void main(String[] args) 
 	{
 		
		connection = Connection.getInstance().initialize("https://paytunia.com", 
 															"trung.nguyen@paymium.com", 
 															"trung.nguyen");
 		try {
 
 			/* LOG IN TO AN ACCOUNT AND SHOW ACCOUNT DESCRIPTION */
 			
 			Account account = connection.getAccount();
 			System.out.println(account);
 			
 			if (account != null)
 			{
 			
 			
 				/* PRINT THE FIRST PAGE AND THE SECOND PAGE OF TRANSACTIONS */
 				
 				
 				//First page 
 				ListTransaction transactions = connection.getTransfers(0,0);
 
 
 				System.out.println("**************PAGE 1**************");
 				for (int i = 0; i < transactions.size(); i++) 
 				{
 					System.out.println("----------------------------");
 					System.out.println(transactions.get(i));
 				}
 				
 				System.out.println("**************PAGE 2**************");
 				
 				//Second page (20 transactions per page)
 				transactions = connection.getTransfers(2,20);
 
 				for (int i = 0; i < transactions.size(); i++) 
 				{
 					System.out.println("----------------------------");
 					System.out.println(transactions.get(i));
 				}
 				
 				
 				
 				
 				
 				/* MAKE A TRANSFER */
 				
 				
 				// By Email
 				
 				TransferMail transferMail = new TransferMail();
 				
 				transferMail.setEmail("nguyennhuquoctrung300890@gmail.com");
 				transferMail.setAmount(BigDecimal.valueOf(0.0623));
 				transferMail.setCurrency(Currency.BTC);
 				
				Transaction transaction = null;
				transaction = connection.postTransferMail(transferMail);
				
				System.out.println("++++++++++++++++++++++++++++++++++");
				System.out.println(transaction);
				System.out.println("++++++++++++++++++++++++++++++++++");
 				//System.out.println(connection.postTransferMail(transferMail));
 				
 				
 				// By Address Bitcoin
 				
 				TransferAddressBitcoin transferAddressBitcoin = new TransferAddressBitcoin();
 				
 				transferAddressBitcoin.setAddressBitcoin("18SSJzXCdVevkcJ7eLJdbFWha5oU86AGxY");
 				transferAddressBitcoin.setAmount(BigDecimal.valueOf(0.052));
 				transferAddressBitcoin.setCurrency(Currency.BTC);
 				
				//System.out.println(connection.postTransferAddressBitcoin(transferAddressBitcoin));
 				
 				
 				
 				/* SIGN UP */
 				
 				NewAccount newAccount = new NewAccount("nguyen.nhu.quoc.trung300890@gmail.com"
 														,"trung30890"
 														,"trung30890");
 				RegisterAccount register = new RegisterAccount();
 				
 				String registerToken = "nguyen nhu quoc trung";
 				System.out.println(register.Request(newAccount,registerToken));
 
 				
 				
 				/* REGISTER AND DELETE DEVICE TOKEN ( PUSH NOTIFICATION ) */
 				
 				//System.out.println(connection.registerDevice("APA91bH_N7p34tY4s7SsY-n2uN7TaI-8LyRcZvuURyQ_g9hn1m1JQ75SGAkPXu9Zl90h7MjIBDqNSjx-xpYtd7VRXA0Ux6LiHBoFS9up9Rrz8-M2gVP_Dkilme7GVtOOZBWEa-z9FJjwlMMO99E8CsZmnr9N5Dk-lQ"));
 				//connection.deleteDevice("APA91bH_N7p34tY4s7SsY-n2uN7TaI-8LyRcZvuURyQ_g9hn1m1JQ75SGAkPXu9Zl90h7MjIBDqNSjx-xpYtd7VRXA0Ux6LiHBoFS9up9Rrz8-M2gVP_Dkilme7GVtOOZBWEa-z9FJjwlMMO99E8CsZmnr9N5Dk-lQ");
 				
 				
 				
 				/* INVOICE (NUMBER OF PAGES, NUMBER OF TRANSACTIONS PER PAGE) */
 				
 				
 				// Invoice by default
 				Invoice a = connection.getInvoice(0,0);
 				
 				System.out.println(a.getOffset());
 				System.out.println(a.isFirst_page());
 				System.out.println(a.getTotal());
 				System.out.println(a.getPrevious_page());
 				System.out.println(a.getTotal_pages());
 				System.out.println(a.isLast_page());
 				System.out.println(a.getNext_page());
 				
 				
 				// Invoice modified
 				a = connection.getInvoice(3,20);
 				
 				System.out.println(a.getOffset());
 				System.out.println(a.isFirst_page());
 				System.out.println(a.getTotal());
 				System.out.println(a.getPrevious_page());
 				System.out.println(a.getTotal_pages());
 				System.out.println(a.isLast_page());
 				System.out.println(a.getNext_page());
 
 			}
 			
 			
 		} 
 		catch (IOException ioException) 
 		{
 			System.out.println(ioException.getMessage());
 		} 
 		catch (ConnectionNotInitializedException e) 
 		{
 			e.printStackTrace();
 		}
 
 	}
 }
