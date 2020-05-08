 package csheets.ext.share.core;
 
 /**
  * Class that will validate the various imput errors in extension program
  * 
 * @author Andre
  * 
  */
 public class Validate {
 
     /**
      * Check if the introduced IP was valid
      * 
      * @param IP
      *            IP to be checked
      * @return true if IP is valid or if equal to localhost
      */
     public static boolean checkIFIPIsCorrect(String IP) {
 	return IP
 		.matches("^((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})$")
 		|| IP.equalsIgnoreCase("localhost");
     }
 
     /**
      * Check the port if is on allowed communication ports
      * 
      * @param port
      *            port to be tested
      * @return the result of the test
      */
     public static boolean checkPort(int port) {
 	return ((port >= 49152) && (port <= 65535));
     }
 
     /**
      * Check if the port passed is an number
      * 
      * @param port
      *            port to be tested
      * @return true if the port is a number
      */
     public static boolean checkIfANumber(String port) {
 	return port.matches("^[0-9]+$");
     }
 }
