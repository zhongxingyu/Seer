 package transaction;
 
 import Composestar.Java.FLIRT.Env.ReifiedMessage;
 
 public class TransactionManagement
 	{   
 	    public TransactionManagement()
 		{
 			
 		}
 	
 		public void startTransaction(ReifiedMessage message)
 		{
			System.out.println(message.getSelector());
 		}
 }
