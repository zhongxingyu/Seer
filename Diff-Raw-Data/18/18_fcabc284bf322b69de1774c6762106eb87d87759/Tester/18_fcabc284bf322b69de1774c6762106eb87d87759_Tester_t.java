 package testing;
 
 import java.util.Scanner;
 
 import network.Client;
 
 public class Tester {
 
 	public static void main(String[] args){
		Scanner s = new Scanner(System.in);
		System.out.println("what is your username?");
		String username = s.nextLine();
 		Client c = new Client();
 		c.init("192.168.1.107");
 		while(true){
			c.sendMessage(username+": "+s.nextLine());
 		
 		}
 	}
 }
