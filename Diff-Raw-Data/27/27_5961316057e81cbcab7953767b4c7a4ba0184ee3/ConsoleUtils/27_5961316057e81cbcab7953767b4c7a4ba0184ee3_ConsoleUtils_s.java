 package ecologylab.generic;
 
 /**
  * Little group of utilities for making console debugging a little easier.
  * 
  * @author blake
  */
 public class ConsoleUtils extends Debug
 {
 	public static void obtrusiveConsoleOutput(String outputMessage)
 	{
		System.out.println("****************************************************");
		System.out.println("----------------------------------------------------");
		System.out.println(outputMessage);
		System.out.println("----------------------------------------------------");
		System.out.println("****************************************************");
 	}
 }
