 package server;
 
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 import common.IllegalMoneyException;
 import common.Money;
 import common.BankAccount;
 
 public class Server {
 
 	public static void main(String[] args) {
 		
 		String registryHost = null;
 		String registryPort = null;
 		int serverPort = 0;
 		
 		try {
 			// Read command line arguments for the lookup service's machine and port.
 			if(args.length != 3) {
 				throw new IllegalArgumentException();
 			}
 			registryHost = args[0];
 			registryPort = args[1];
 			serverPort = Integer.parseInt(args[2]);
 			
 			// Create remote BankAccount instances.
 			BankAccount acc1 = new BankAccountServant(
 					new Money(), "Brent, D.", "67832189", new Money(1500, 00));
 			BankAccount acc2 = new BankAccountServant(
 					new Money(), "Tinsley, D.", "69826344", new Money(100, 00));
 			BankAccount acc3 = new BankAccountServant(
 					new Money(), "Keenan, G.", "61198701", new Money(250, 00));
 			
 			// Register BankAccounts with the lookup service.
 			// === YOUR CODE HERE ===
 			
 			System.out.println("Bank account objects exported.");
 		} catch(RemoteException e) {
 			e.printStackTrace();
 		} catch(IllegalMoneyException e) {
 			e.printStackTrace();
		} catch(MalformedURLException e) {
			e.printStackTrace();
 		}
 	}
 }
