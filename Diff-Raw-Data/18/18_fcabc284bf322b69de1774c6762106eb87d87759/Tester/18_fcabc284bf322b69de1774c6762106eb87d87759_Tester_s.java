 package testing;
 
 import java.util.Scanner;
 
 import network.Client;
 
 public class Tester {
 
 	public static void main(String[] args){
 		Client c = new Client();
 		c.init("192.168.1.107");
		Scanner s = new Scanner(System.in);
 		while(true){
			c.sendMessage(s.nextLine());
 		
 		}
 	}
 }
