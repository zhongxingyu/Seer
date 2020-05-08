 package edu.gatech.cs2340_sp13.teamrocket.findmythings;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 
 
 public class Login {
 	
 	public Login() {
 		
 	}
 	public boolean verifyUser(Member m) {
		Scanner scan = new Scanner(nopasswordsinhere.txt);
 		boolean found = false;
 				while(scan.hasNext() && !found) {
 					String[] str = scan.nextLine().split(":");
					if(m.getUser.equals(str))	
						found=true;
 				}
 	}
 	public void register(String name, String password) {
 		try {
 		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("nopasswordsinhere.txt", true)));  //store usernames and passwords in super secure text file
 		    out.println(name + ":" + password);
 		    out.close();
 		} catch (IOException e) {
 		    System.out.println("This can't be life");
 		}
 	}
 
 }
