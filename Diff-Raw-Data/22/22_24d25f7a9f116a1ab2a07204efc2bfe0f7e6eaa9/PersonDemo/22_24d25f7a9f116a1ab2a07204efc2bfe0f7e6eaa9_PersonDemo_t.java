 package de.htwg.seapal.person.app;
 
 import java.io.BufferedReader;
import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
import play.api.Application;
import play.api.DefaultApplication;
import play.api.Mode;
import play.api.Play;

 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import de.htwg.seapal.person.controllers.IPersonController;
 import de.htwg.seapal.person.views.tui.PersonTUI;
 
 public final class PersonDemo {
 
 	private PersonDemo() {
 	}
 	
 	/**
 	 * @param args
 	 * @throws ParseException 
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		
		Application play = new DefaultApplication(new File("."),
				PersonDemo.class.getClassLoader(), null, Mode.Dev());
		Play.start(play);
		
 		// Set up Google Guice DI
 		Injector injector = Guice.createInjector(new PersonDemoImplModule());
 		
 		// Build up the application, resolving dependencies automatically by Guice
 		IPersonController controller = injector.getInstance(IPersonController.class);
 
 		PersonTUI tui = new PersonTUI(controller);
 		tui.printTUI();
 		
 		// continue to read user input on the tui until the user decides to quit
 		boolean continu = true;
 		InputStreamReader isr = new InputStreamReader(System.in, "UTF-8");
 		BufferedReader br = new BufferedReader(isr);
 		
 		while (continu) {
 			continu = tui.processInputLine(br.readLine());
 		}
		
		Play.stop();
 	}
 
 }
