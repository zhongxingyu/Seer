 import java.util.Scanner;
 
 import org.apache.log4j.PropertyConfigurator;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import de.htwg.mastermind.controller.IMastermindController;
 import de.htwg.mastermind.model.IField;
 import de.htwg.mastermind.model.implementierung.Field;
 import de.htwg.mastermind.view.gui.MastermindFrame;
 import de.htwg.mastermind.view.tui.TUI;
 
 public abstract class Main { 
 
 
 	private static Scanner scanner;
 		
 	
 	public static void main(String[] args) {
 		// Set up logging through log4j
 		PropertyConfigurator.configure("log4j.properties");
 		
 		// Set up Google Guice Dependency Injector
 				Injector injector = Guice.createInjector(new MastermindModel());
 				
 		
 		/*model*/
		IField gamefield = injector.getInstance(Field.class);// new Field(FOUR,SIX); 
 		
 		/*controller*/
		IMastermindController controller = injector.getInstance(IMastermindController.class);//new MastermindController(gamefield);
		
 		MastermindFrame gui = new MastermindFrame(controller);
		
 		/*view*/
 		TUI tui = new TUI(controller);
 		
 		/*main menue*/
 		controller.createField();	
 		
 		scanner = new Scanner(System.in);
 
 		boolean continu = true;
 		while(continu) {
 			continu = tui.input(scanner.next());
 		}
 	}
 }
