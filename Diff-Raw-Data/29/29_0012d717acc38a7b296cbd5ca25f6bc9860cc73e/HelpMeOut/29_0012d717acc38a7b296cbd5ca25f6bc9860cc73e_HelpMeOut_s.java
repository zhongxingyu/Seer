 /**
  *  Some utility methods to help with stuff
  *    user input method from HeadFirst Java...
  * Author:     Jason Angus
  * File:       HelpMeOut.java
  * Last Edit:  09/10/2010
  */
 
 import java.io.*;
 
 public class HelpMeOut {
    public String getUserInput(String prompt) {
       String inputLine = null;
       System.out.println(prompt + " ");
       try {
          BufferedReader is = new BufferedReader(
          new InputStreamReader(System.in));
          inputLine = is.readLine();
         if (inputLine.length() == 0) return null;
      } Catch(IOException exception) {
          System.out.println("IOException " + exception);
       }
       return inputLine;
    } // close getUserInput method
 } // close HelpMeOut class
