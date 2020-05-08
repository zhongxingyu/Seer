 package com.paymium.paytunia.PaytuniaAPI;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 
 import com.paymium.paytunia.PaytuniaAPI.exceptions.ConnectionNotInitializedException;
 
 public class Main 
 {
 
 	private static Connection connection;
 
 	public static void main(String[] args) 
 	{
 		connection = Connection.getInstance().initialize("https://bitcoin-central.net", 
 															"nguyennhuquoctrung300890@gmail.com", 
 															"trung.nguyen");
 		try {
 
 			Account account = connection.getAccount();
 			
 			if (account != null)
 			{
 				//First page , which is equivalent to ArrayList<Transfer> transfers = connection.getTransfers(0,0);
 				ArrayList<Transfer> transfers = connection.getTransfers();
 
 				//System.out.println(account);
 
 				for (int i = 0; i < transfers.size(); i++) 
 				{
 					System.out.println("----------------------------");
 					System.out.println(transfers.get(i).getAmount());
 				}
 				
 				System.out.println("****************************");
 				
 				//Second page
 				/*transfers = connection.getTransfers(2,20);
 
 				for (int i = 0; i < transfers.size(); i++) 
 				{
 					System.out.println("----------------------------");
 					System.out.println(transfers.get(i).getAmount());
 				}*/
 				
 				
 				Transfer transfer = new Transfer();
				transfer.setAmount(BigDecimal.valueOf(0.052));
 				
 				// Mauvaise Adresse
 				//transfer.setEmail("nguyennhuquoctrung300890@gmail.com");
 				
 				// Bon adresse
 				transfer.setEmail("trung.nguyen@paymium.com");
 				
 				transfer.setCurrency(Currency.BTC);
 				
 				System.out.println(connection.postTransfer(transfer));
 				
 				//connection.registerDevice("some-device-token");
 				
 				
 				/*NewAccount newAccount = new NewAccount("mathsboy300890@yahoo.com.bb"
 														,"trung30890"
 														,"trung30890");*/
 				//RegisterAccount register = new RegisterAccount();
 				//System.out.println(register.Request(newAccount));
 
 				//System.out.println(connection.registerDevice("APA91bH_N7p34tY4s7SsY-n2uN7TaI-8LyRcZvuURyQ_g9hn1m1JQ75SGAkPXu9Zl90h7MjIBDqNSjx-xpYtd7VRXA0Ux6LiHBoFS9up9Rrz8-M2gVP_Dkilme7GVtOOZBWEa-z9FJjwlMMO99E8CsZmnr9N5Dk-lQ"));
 				//connection.deleteDevice("APA91bH_N7p34tY4s7SsY-n2uN7TaI-8LyRcZvuURyQ_g9hn1m1JQ75SGAkPXu9Zl90h7MjIBDqNSjx-xpYtd7VRXA0Ux6LiHBoFS9up9Rrz8-M2gVP_Dkilme7GVtOOZBWEa-z9FJjwlMMO99E8CsZmnr9N5Dk-lQ");
 				
 				// Invoice by default
 				/*Invoice a = connection.getInvoice(0,0);
 				
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
 				System.out.println(a.getNext_page());*/
 				
 				//System.out.println(a.getTotal());
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
