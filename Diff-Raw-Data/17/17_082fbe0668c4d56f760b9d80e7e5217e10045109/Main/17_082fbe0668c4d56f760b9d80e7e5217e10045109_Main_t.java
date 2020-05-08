 import java.util.Scanner;
 
 import de.htwg.mastermind.controller.MastermindController;
 import de.htwg.mastermind.model.*;
 import de.htwg.mastermind.view.Tui;
 
 
 
 public class Main {
 
 
 	static Scanner scanner;
 	public static void main(String[] args) {
 		
 		
 		/*model*/
 		Field gamefield = new Field(4);
 		
 		//System.out.println(gamefield);
 
 		/*controller*/
 		MastermindController controller = new MastermindController(gamefield);
 				
 		/*view*/
 		Tui tui = new Tui(controller);
 		
		System.out.println(gamefield.toString());
 		/*main menue*/
 		tui.showTUI();
 		
 		
 		scanner = new Scanner(System.in);
 		
 		
 		
 		boolean continu = true;
 		while(continu) {
 			continu = tui.input(scanner.next());
 		}
 		
 		
 
 	}
 
 }
