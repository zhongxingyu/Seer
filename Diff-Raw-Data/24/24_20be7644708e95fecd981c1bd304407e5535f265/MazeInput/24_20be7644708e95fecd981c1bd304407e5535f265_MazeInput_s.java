 package general_utilities;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 /* Utilities to receive player input */
 public class MazeInput {
	
 	public static char getChar() throws IOException {
 		Scanner in = new Scanner(System.in);
 
 		String s = in.nextLine();
		char c = s.charAt(0);
 
 		//in.close();
 		return c;
 	}
 
 }
