 package be.artesis.timelog.controller;
 
 import be.artesis.timelog.model.Connection;
 import be.artesis.timelog.model.WebserviceException;
 import be.artesis.timelog.view.*;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import org.json.*;
 
 public class Inserter {
 	
 	public static void CreateUser(String naam, String voornaam, String gebruikersnaam, String password, String email) throws MalformedURLException, IOException, WebserviceException {
         Connection.execute("gebruiker/create/naam/voornaam/gebruikersnaam/passwoord/email");
 	}
 	
 	public static void CreateUserExtern(String naam, String voornaam, String gebruikersnaam, String email) throws MalformedURLException, IOException, WebserviceException {
        Connection.execute("gebruiker/createExtern/naam/voornaam/gebruikersnaam/email");
 	}
 
 	public static void inputTijdSpanne(String sessionKey, Tijdspanne tijdSpanne, int taakID) throws MalformedURLException, IOException, WebserviceException {
             Connection.execute("tijdspanne/create/" + sessionKey + "/" + tijdSpanne.getBeginTijd() + "/" + tijdSpanne.getEindTijd() + "/" + tijdSpanne.isPauze() + "/" + taakID);
 	}
 
 	public static int inputProject(String sessionKey, Project project, int opdrachtGeverID) throws MalformedURLException, IOException, WebserviceException, JSONException {
             JSONObject key = Connection.getObject("project/create/" + sessionKey + "/" + project.getNaam() + "/" + project.getBegindatum() + "/" + project.getEinddatum() + "/" + opdrachtGeverID);
             return key.getInt("GENERATED_KEY");
     }
 
 	public static int inputOpdrachtgever(String sessionKey, Opdrachtgever opdrachtgever) throws JSONException, MalformedURLException, IOException, WebserviceException {
             // nog korter maken, maar is voor leesbaarheid
             JSONObject key = Connection.getObject("opdrachtgever/create/" + sessionKey + "/" + opdrachtgever.getBedrijfsnaam() + "/" + opdrachtgever.getNaam() + "/" + opdrachtgever.getVoornaam() + "/" + opdrachtgever.getEmail() + "/" + opdrachtgever.getTelefoonnummer());
             return key.getInt("GENERATED_KEY");
 	}
 
 	public static int inputTaak(String sessionKey, Taak taak, int projectID) throws MalformedURLException, IOException, WebserviceException, JSONException {
             JSONObject key = Connection.getObject("task/create/" + sessionKey + "/" + taak.getNaam() + "/" + taak.getBegindatum() + "/" + taak.getGeschatteEinddatum() + "/" + taak.getCommentaar() + "/" + projectID + "/" + taak.getCompleted());
             return key.getInt("GENERATED_KEY");
     }
 }
