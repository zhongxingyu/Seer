 package de.htwg.seapal.maps.app;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import play.api.Application;
 import play.api.DefaultApplication;
 import play.api.Mode;
 import play.api.Play;
 
 import com.avaje.ebean.Ebean;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import de.htwg.seapal.boat.app.AppMockModule;
 import de.htwg.seapal.maps.models.ebean.Maps;
 import de.htwg.seapal.maps.views.tui.MapsTUI;
 import de.htwg.seapal.person.app.PersonDemoImplModule;
 import de.htwg.seapal.person.app.PersonDemoMockModule;
 
 /**
  * The maps startup class.
  */ class MapsMain {
 
 	/**
 	 * The programs main.
 	 * 
 	 * @param args	The default program arguments.
 	 * @throws IOException 
 	 */
 	 
 	public static void main(String[] args) throws IOException {
 		// Initialize Play Application to use the play environment functions...
 		Application play = new DefaultApplication(new File("."),
 				MapsMain.class.getClassLoader(), null, Mode.Dev());
 		Play.start(play);
 
 		Maps map = new Maps();
 		Ebean.save(map);
 		
 		// Set up Google Guice Dependency Injector
 		Injector injector = MapsGlobal.createInjector();
 		
 		tui.printTUI();
 
 		// continue to read user input on the tui until the user decides to quit	
 		InputStreamReader isr = new InputStreamReader(System.in, "UTF-8");
 		BufferedReader br = new BufferedReader(isr);
 
 		while (tui.processInputLine(br.readLine()));
 		
 		Play.stop();
 	}
 
 }
