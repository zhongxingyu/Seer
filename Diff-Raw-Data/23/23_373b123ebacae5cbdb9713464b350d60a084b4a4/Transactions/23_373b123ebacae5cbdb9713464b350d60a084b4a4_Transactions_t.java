 public class Transactions
 {
 	public static void main (String[] args)
 	{
 	Account acct1 = new Account ("Don Strawsburg", 99999, 100.00);
 	Account acct2 = new Account ("Petr Ruben", 30000, 80.00);
 	Account acct3 = new Account ("Jessica Strawsburg", 40000, 200.00);
 	
 	acct1.deposit (3000.00);
 	
	double donBalance = acct1.deposit (1000.00);
 	System.out.println ("Don's balance after deposit: " + donBalance);
 	
 	System.out.println ("Don balance after withdrawal: " +acct1.withdraw (600.00, 2.50));
 	
 	acct1.addInterest();
 	acct2.addInterest();
 	acct3.addInterest();
 	
 	System.out.println();
 	System.out.println(acct1);
 	System.out.println(acct2);
 	System.out.println(acct3);
 	}
 }
