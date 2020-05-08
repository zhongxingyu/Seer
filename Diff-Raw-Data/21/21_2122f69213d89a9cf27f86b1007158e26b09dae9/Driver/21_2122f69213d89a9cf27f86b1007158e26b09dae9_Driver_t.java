 package Access;
 import java.io.BufferedReader;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.Status;
 
 
 public class Driver {
 
 
 	public static void main(String args[]) throws Exception {
 
 		Tweet dave = new Tweet();
 		Scanner sc = new Scanner(System.in);
 		System.out.println("Have you already authorized this app? (y/n): ");
 		String dec = sc.nextLine();
 
 
 		if(dec.equals("n")){
 			dave.start();
 		}else{
 
 			dave.getCreds();	
 			dave.runF();
 		}
 
 
 
    }
 
 
 }
