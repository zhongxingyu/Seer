 package com.textadventure.game;
 
 import java.util.Scanner;
 
 public class Listener {
 	private static String string;
 	public static String fetch()
 	{
		
 		try
 		{
			Scanner scan = new Scanner(System.in);
 			string = scan.nextLine();
 			scan.close();
 			return string;
 		}
 		catch(java.util.InputMismatchException e)
 		{
 			Error.report(1);
 			fetch();
 		}
 		Error.report(0);
 		System.exit(0);
 		return string;
 	}
 	
 }
