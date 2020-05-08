 import java.io.BufferedReader;
import java.io.Console;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 
 public class Assignment3 {
 
 	public static void main(String args[]){
		System.out.println("Enter something here : ");
 		 
 		try{
 		    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
 		    String s = bufferRead.readLine();
 	 
 		    System.out.println(s);
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 }
