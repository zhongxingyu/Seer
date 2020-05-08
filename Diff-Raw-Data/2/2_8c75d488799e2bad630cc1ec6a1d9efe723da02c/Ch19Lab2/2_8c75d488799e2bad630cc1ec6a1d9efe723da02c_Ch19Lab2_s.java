 /**
  * Program: Ch19Lab2.java
  * Programmer: Andrew Buskov
  * Class: CIS 249
  * Date: Jan 19, 2013
  * Purpose: Data input and output in binary form
  */
 
 package Ch19.Lab2;
 
 import java.io.*;
 
 public class Ch19Lab2 {
 	
 	public static void main(String[] args){
 		
 		// declare data input and output streams
 		DataInputStream dis = null;
 		DataOutputStream output = null;
 		
 		int count = 0;
 		
 		try {
 			
 			// creating folder if necessary for package clarity
 			File folder = new File("./archives");
 			folder.mkdir();
 			
 			// create data input stream
 			
 			dis = new DataInputStream(new FileInputStream("./archives/Ch19Lab2.dat"));
 			
 			int total = 0;
 			while (dis.available() > 0){
 				int temp = dis.readInt();
 				total += temp;
 				count++;
 				System.out.println(temp + " ");
 			}
 			
 			System.out.println("\nCount is " + count);
			System.out.println("\nTotal is "= total);
 		} 
 	}
 
 }
