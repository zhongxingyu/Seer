 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 
 public class Assignment3 {
 
 	public static void main(String args[]){
		System.out.println("Welcome to the Hotel Database");
		System.out.println("Menu: (A) Add Guest /");
 		 
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
