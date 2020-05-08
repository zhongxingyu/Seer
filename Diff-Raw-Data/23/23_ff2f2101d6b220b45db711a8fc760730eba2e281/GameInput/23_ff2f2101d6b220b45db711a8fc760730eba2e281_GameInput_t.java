 package game.ui;
 
 import java.util.Scanner;
 
 import game.ui.GameOutput;
 
 public class GameInput {
 
 	public static boolean receiveMazeOptions(int size[]) { //Receives user input for maze size. If user wants predefined maze, return false, true otherwise
 
 		Scanner in = new Scanner(System.in);
		char c = 'a';
 
 
 		do {
 			GameOutput.printOptions(0);
 			String s = in.nextLine();
			if(!s.isEmpty())
				c = s.charAt(0);
 		}
 		while(c != 'Y' && c != 'y' && c != 'N' && c != 'n');
 
 		if(c == 'Y' || c == 'y') {
 			GameOutput.printOptions(1);
 			size[0] = in.nextInt();
 
 			GameOutput.printOptions(2);
 			size[1] = in.nextInt();
 
 			return true;
 		}

 		return false;
 
 	}
 
 }
