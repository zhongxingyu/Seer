 package de.htwg.seapal.person.app;
 
 import java.text.ParseException;
 import java.util.Scanner;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import de.htwg.seapal.person.views.tui.PersonTUI;
 import de.htwg.seapal.person.controllers.IPersonController;
 
public final class PersonDemo {
 
 	private PersonDemo() {
 	}
 	
 	/**
 	 * @param args
 	 * @throws ParseException 
 	 */
 	public static void main(String[] args) throws ParseException {
 		
 		// Set up Google Guice DI
 		Injector injector = Guice.createInjector(new PersonDemoImplModule());
 		
 		// Build up the application, resolving dependencies automatically by Guice
 		IPersonController controller = injector.getInstance(IPersonController.class);
 
 		PersonTUI tui = new PersonTUI(controller);
 		tui.printTUI();
 		
 		// continue to read user input on the tui until the user decides to quit
 		boolean continu = true;
 		Scanner scanner = new Scanner(System.in);
 		
 		while (continu) {
 			continu = tui.processInputLine(scanner.next());
 		}
 		
 		scanner.close();
 	}
 
 }
