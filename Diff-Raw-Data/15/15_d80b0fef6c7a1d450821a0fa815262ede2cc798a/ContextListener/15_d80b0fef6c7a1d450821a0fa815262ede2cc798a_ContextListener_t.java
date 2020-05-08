 package com.appspot.AccentNijkerk.listeners;
 
import java.text.SimpleDateFormat;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.mortbay.log.Log;
 
 import com.appspot.AccentNijkerk.model.Competentie;
 import com.appspot.AccentNijkerk.model.CompetentieDao;
 import com.appspot.AccentNijkerk.model.CompetentieDaoOfyImpl;
 import com.appspot.AccentNijkerk.model.CompetentieLijst;
 import com.appspot.AccentNijkerk.model.Gebruiker;
 import com.appspot.AccentNijkerk.model.GebruikerDao;
 import com.appspot.AccentNijkerk.model.GebruikerDaoOfyImpl;
 import com.appspot.AccentNijkerk.model.Leerling;
 import com.appspot.AccentNijkerk.model.Medewerker;
import com.appspot.AccentNijkerk.model.Stage;
 import com.appspot.AccentNijkerk.model.StageBedrijf;
 import com.appspot.AccentNijkerk.model.Vraag;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 
 public class ContextListener implements ServletContextListener {
 	private static final Logger log = Logger.getLogger(ContextListener.class.getName());
 	Objectify ofy = ObjectifyService.begin();
 	
 	public void contextInitialized(ServletContextEvent sce) {
 		initObjectifyClasses();
 	
 		//Testgebruikers aanmaken
 		Gebruiker g1 = (Gebruiker) new Leerling("TestLeerling", "test", "Jason Koolman", "jason.koolman@hotmail.com", "5390531");
 		Gebruiker g2 = (Gebruiker) new StageBedrijf("TestBedrijf", "test", "Bedrijfsnaam", "Industrielaan 32", "info@bedrijf.com", "030-8890566");
 		Gebruiker g3 = (Gebruiker) new Medewerker("TestMedewerker", "test", "Medewerker", "Slingstraat 24", "info@medewerker.com");
 		
 		GebruikerDao gebruikerDao = new GebruikerDaoOfyImpl();
 		gebruikerDao.voegGebruikerToe(g1);
 		gebruikerDao.voegGebruikerToe(g2);
 		gebruikerDao.voegGebruikerToe(g3);
 		
 		//Testcompetenties aanmaken
 		Competentie c1 = new Competentie("Sociale vaardigheden");
 		Competentie c2 = new Competentie("Communicatie");
 		Competentie c3 = new Competentie("Motivatie");
 		
 		CompetentieDao competentieDao = new CompetentieDaoOfyImpl();
 		competentieDao.voegCompetentieToe(c1);
 		competentieDao.voegCompetentieToe(c2);
 		competentieDao.voegCompetentieToe(c3);
		
		Stage s1 = new Stage();
 	}
 
 	public void contextDestroyed(ServletContextEvent sce) {
 		//ContextDestroyed
 	}
 	
 	private void initObjectifyClasses() {
 		//Objectify classes initialiseren
 		ObjectifyService.register(StageBedrijf.class);
 		ObjectifyService.register(Medewerker.class);
 		ObjectifyService.register(Leerling.class);
 		ObjectifyService.register(Gebruiker.class);
 		ObjectifyService.register(Competentie.class);
 		ObjectifyService.register(CompetentieLijst.class);
 		ObjectifyService.register(Vraag.class);
		ObjectifyService.register(Stage.class);
 	}
 }
