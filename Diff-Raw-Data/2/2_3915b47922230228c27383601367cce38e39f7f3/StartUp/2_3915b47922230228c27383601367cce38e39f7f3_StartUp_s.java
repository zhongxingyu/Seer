 package de.uni_hamburg.informatik.sep.zuul;
 
 import java.net.MalformedURLException;
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 
 import de.uni_hamburg.informatik.sep.zuul.client.StartConsole;
 import de.uni_hamburg.informatik.sep.zuul.client.oberflaeche.gui.StartFenster;
 
 public class StartUp
 {
 	public static void main(String args[]) throws RemoteException,
 			AlreadyBoundException, NumberFormatException,
 			MalformedURLException, NotBoundException
 	{
		if(args.equals("console"))
 		{
 			new StartConsole();
 		}
 		else
 		{
 			new StartFenster();
 		}
 	}
 }
