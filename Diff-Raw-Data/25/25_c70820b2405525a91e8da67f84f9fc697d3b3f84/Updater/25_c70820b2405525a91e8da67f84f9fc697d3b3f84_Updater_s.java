 package be.artesis.timelog.controller;
 
 
 import be.artesis.timelog.model.Connection;
 import be.artesis.timelog.model.WebserviceException;
 import be.artesis.timelog.view.Gebruiker;
 import be.artesis.timelog.view.Opdrachtgever;
 import be.artesis.timelog.view.Project;
 import be.artesis.timelog.view.Taak;
 import be.artesis.timelog.view.Tijdspanne;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 public class Updater {
 
 	public static void updateGebruiker(String sessionKey, Gebruiker gebruiker) throws MalformedURLException, IOException, WebserviceException{
 		Connection.execute("gebruiker/update/" + sessionKey + "/" + gebruiker.getNaam() + "/" + gebruiker.getVoornaam() + "/" + gebruiker.getGebruikersnaam() + "/" +gebruiker.getEmail());
 	}
         
 	public static void updateProject(String sessionKey, Project project) throws MalformedURLException, IOException, WebserviceException {
 		Connection.execute("project/update/" + sessionKey + "/" + project.getId() + "/" + project.getNaam() + "/" + project.getBegindatum() + "/" + project.getEinddatum());
 	}
 
 	public static void updateOpdrachtgever(String sessionKey, Opdrachtgever opdrachtgever) throws MalformedURLException, IOException, WebserviceException{
 		Connection.execute("opdrachtgever/update/" + sessionKey + "/" + opdrachtgever.getID() + "/" + opdrachtgever.getBedrijfsnaam() + "/" + opdrachtgever.getNaam() + "/" + opdrachtgever.getVoornaam() + "/" + opdrachtgever.getEmail() + "/" + opdrachtgever.getTelefoonnummer());
 	}
 	// FIXME Indien dit niet lukt, effe checken hoe het enkele commits terug was en aanpassen
 	public static void updateTaak(String sessionKey, Taak taak) throws MalformedURLException, IOException, WebserviceException {
		System.out.println("task/update/" + sessionKey + "/" + taak.getNaam() + "/" + taak.getBegindatum()+ "/" + taak.getGeschatteEinddatum() + "/" + taak.getCommentaar() + "/" + taak.getID() + "/" + taak.getCompleted());
 		Connection.execute("task/update/" + sessionKey + "/" + taak.getNaam() + "/" + taak.getBegindatum()+ "/" + taak.getGeschatteEinddatum() + "/" + taak.getCommentaar() + "/" + taak.getID() + "/" + taak.getCompleted());
     }
 	public static void updateTijdspanne(String sessionKey, Tijdspanne tijdspanne) throws IOException, WebserviceException{		 
 		Connection.execute("tijdspanne/update/" +sessionKey + "/" + tijdspanne.getEindTijd()+ "/" +tijdspanne.getEindTijd() + "/" + tijdspanne.isPauze() + "/" + tijdspanne.getId());
 	}
 }
