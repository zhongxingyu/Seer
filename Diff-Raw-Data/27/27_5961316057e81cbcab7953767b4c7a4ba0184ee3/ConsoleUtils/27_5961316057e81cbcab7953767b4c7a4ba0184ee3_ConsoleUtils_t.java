 package ecologylab.generic;
 
 /**
  * Little group of utilities for making console debugging a little easier.
  * 
  * @author blake
  */
 public class ConsoleUtils extends Debug
 {
	public static void obtrusiveConsoleOutput(String outputMessage, boolean stdErr)
	{
		if (stdErr)
		{
			System.err.println("****************************************************");
			System.err.println("----------------------------------------------------");
			System.err.println(outputMessage);
			System.err.println("----------------------------------------------------");
			System.err.println("****************************************************");
		}
		else
		{
			System.out.println("****************************************************");
			System.out.println("----------------------------------------------------");
			System.out.println(outputMessage);
			System.out.println("----------------------------------------------------");
			System.out.println("****************************************************");
		}
	}
	
 	public static void obtrusiveConsoleOutput(String outputMessage)
 	{
		obtrusiveConsoleOutput(outputMessage, false);	
 	}
 }
