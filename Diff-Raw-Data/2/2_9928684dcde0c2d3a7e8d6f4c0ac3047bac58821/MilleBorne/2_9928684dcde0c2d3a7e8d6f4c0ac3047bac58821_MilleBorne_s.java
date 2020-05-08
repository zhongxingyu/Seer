 import java.awt.EventQueue;
 import java.util.Scanner;
 
 import fr.lo02.controller.Controller;
 import fr.lo02.controller.ControllerGameGUI;
 import fr.lo02.controller.gui.ConsoleHandGUI;
 import fr.lo02.controller.gui.ConsoleMatchGUI;
 import fr.lo02.controller.gui.GameGUI;
 import fr.lo02.model.Game;
 
 
 public class MilleBorne {
 	
 	
 	public MilleBorne() {
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Scanner scan = new Scanner(System.in);
 
		System.out.println("Comment lancez le jeu ?");
 		System.out.println("1 - Console");
 		System.out.println("2 - Interface Graphique");
 
 		String choice = scan.nextLine();
 		// ---- Se defausser ----
 		if (choice.equals("2")) {
 			Game model = new Game();
 			GameGUI view = new GameGUI(model);
 			view.setVisible(true);
 			ControllerGameGUI controller = new ControllerGameGUI(model, view);
 		}
 		else if (choice.equals("1")) {
 			Game model = new Game();
 			ConsoleHandGUI view1 = new ConsoleHandGUI();
 			ConsoleMatchGUI view2 = new ConsoleMatchGUI();
 			Controller controller = new Controller(model, view1, view2);
 		}
 	}
 }
