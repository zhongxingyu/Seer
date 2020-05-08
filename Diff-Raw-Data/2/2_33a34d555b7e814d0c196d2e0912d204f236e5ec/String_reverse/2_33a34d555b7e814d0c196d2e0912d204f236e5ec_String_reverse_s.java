 /* Jonnie Simpson
  * Humberview S.S.
  * ICS 3U0, Created on 2010-12-07
  * String_revers.java
  * --------------------------------------------
  * Enter a string: apple
  * in reverse the string is: elppa
  * --------------------------------------------
  */
 
 // Imports
 import java.awt.*;
 import hsa.Console;
 
public class String_revers {
 	static Console c; // The output console
 
 	public static void main (String[] args)
     {
         c = new Console (25, 80, 14, "String Reverser");
         		
         		c.print("Enter a string: ");
         		String text = c.readLine();
         		
         		char [] text2;
         		text2 = text.toCharArray();
         		
         		for (int i = text.length(); i < 0; i--) {
 					
         			c.print(text2[i]);
         			
 				}
         		c.println("In the reverse the string is: " + text2);
 		
         } // main method
 } // test class
