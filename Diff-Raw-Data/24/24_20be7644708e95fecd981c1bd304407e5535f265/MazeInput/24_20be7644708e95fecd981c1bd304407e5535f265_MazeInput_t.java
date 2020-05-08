 package general_utilities;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 /* Utilities to receive player input */
 public class MazeInput {

 	public static char getChar() throws IOException {
 		Scanner in = new Scanner(System.in);
		char c;
 
 		String s = in.nextLine();
		if(!s.isEmpty())
			c = s.charAt(0);
		else
			c = ' ';
 
 		//in.close();
 		return c;
 	}
 
 }
