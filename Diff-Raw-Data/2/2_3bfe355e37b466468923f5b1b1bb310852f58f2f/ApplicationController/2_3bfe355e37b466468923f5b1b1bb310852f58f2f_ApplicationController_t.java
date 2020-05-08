 /**
  * @author Patryk Boczon
  * @author Oemer Sahin
  * @author Manuel Güntzel
  * @author Anatoli Brill
  */
 
 package database.application;
 
 /**
  * Verwaltet alle Datenbankzugriffe auf Bewerbungs-bezogene Daten.
  */
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Vector;
 
 import database.DatabaseController;
 import database.account.Account;
 import database.offer.OfferController;
 
 public class ApplicationController {
 
 	/**
 	 * Diese Methode prueft ob ein ApplicationController-Objekt existiert. Falls
 	 * nicht wird eine neue ApplicationOffer-Instanz angelegt, zurueckgegeben
 	 * und in dem Klassenattribut "appcontr" abgespeichert. Dies dient zur
 	 * Gewaehrleistung, dass nur eine Instanz von ApplicationController
 	 * existiert.
 	 * 
 	 * @return Es wird die Instanz zurueckgegeben.
 	 */
 	public static ApplicationController getInstance() {
 		if (appcontr == null)
 			appcontr = new ApplicationController();
 		return appcontr;
 
 	}
 
 	/**
 	 * Privater Konstruktor, da die Klasse selbst ein Singleton ist.
 	 */
 	private ApplicationController() {
 		dbc = DatabaseController.getInstance();
 		logger.Log.getInstance().write("ApplicationController",
 				"Instance created.");
 	}
 
 	/**
 	 * Klassenattribut "appcontr" beinhaltet eine ApplicationController-Instanz,
 	 * falls keine vorhanden war und mit der Methode getInstance angelegt wird.
 	 * Dies dient zur Gewaehrleistung, dass nur eine Instanz von
 	 * ApplicationController existiert.
 	 */
 	private static ApplicationController appcontr;
 
 	/**
 	 * Diese Instanz dient zum Zugang in die Datenbank.
 	 */
 	public DatabaseController dbc;
 	
 	final static String tableName = "bewerbungen";//tabellenname
 
 	/**
 	 * Diese Methode erstellt eine Bewerbung in der Datenbank. Mit ubergebenem
 	 * Bewerbungsobjekt.
 	 * 
 	 * @param application
 	 *            Parameter "application" ist ein Application-Objekt mit allen
 	 *            dazugehoerigen Attributen.
 	 */
 	public void createApplication(Application application) { //checked
 
 		Object[] values = { application.getUsername(), application.getAid(),
 				application.isFinished(), application.getClerk(),
 				application.isChosen() };
 
 		dbc.insert(tableName, values);
 
 	}
 
 	/**
 	 * Diese Methode loescht eine Bewerbung aus der Datenbank. Mit ubergebenem
 	 * Bewerbungsobjekt.
 	 * 
 	 * @param application
 	 *            Parameter "application" ist ein Application-Objekt mit allen
 	 *            dazugehoerigen Attributen.
 	 */
 
 	public void deleteApplication(Application application) {
 		
		String where = "AID = "+application.getAid()+"AND benutzername = '"+application.getUsername()+"'";
 		dbc.delete(tableName, where);
 
 	}
 
 	/**
 	 * Diese Methode aendert die Attribute einer Bewerbung bzw. aktualisiert
 	 * diese in der Datenbank.
 	 * 
 	 * @param application
 	 *            Parameter "application" ist ein Application-Objekt mit allen
 	 *            dazugehoerigen Attributen.
 	 */
 	public void updateApplication(Application application) {
 
 		String[] columns = { "benutzername", "status", "sachbearbeiter",
 				"ausgewaehlt" };
 		Object[] values = { application.getUsername(),
 				application.isFinished(), application.getClerk(),
 				application.isChosen() };
 		String where = "AID = " + application.getAid();
 
 		dbc.update(tableName, columns, values, where);
 
 	}
 
 	/**
 	 * Diese Methode sammelt alle Bewerbungen aus der Datenbank und speichert
 	 * diese in einem Vector.
 	 * 
 	 * @return Es wird ein Vector mit allen vorhanden Bewerbungen in der
 	 *         Datenbank zurueckgegeben.
 	 */
 	public Vector<Application> getAllApplications() {
 
 		Vector<Application> applicationvec = new Vector<Application>(50, 10);
 
 		String[] select = { "*" };
 		String[] from = { tableName };
 
 		ResultSet rs = dbc.select(select, from, null);
 		try {
 			while (rs.next()) {
 				Application currentapp;
 				currentapp = new Application(rs.getString(1), rs.getInt(2),
 						rs.getBoolean(3), rs.getString(4), rs.getBoolean(5));
 
 				applicationvec.add(currentapp);
 			}
 
 			rs.close();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return applicationvec;
 
 	}
 
 	/**
 	 * Diese Methode sammelt alle Bewerbungen eines bestimmten Bewerbers aus der
 	 * Datenbank und speichert diese in einem Vector.
 	 * 
 	 * @param username
 	 *            Parameter "username" gibt den Namen dese Bewerbers an, dessen
 	 *            Bewerbungen angezeigt werden sollen.
 	 * @return Alle Bewerbungen zu einem Bewerber aus der Datenbank in Form
 	 *         eines Vectors werden zurueckgegeben.
 	 */
 	public Vector<Application> getApplicationsByApplicant(String username) {
 
 		Vector<Application> applicationvec = new Vector<Application>(3, 2);
 
 		String[] select = { "*" };
 		String[] from = { tableName };
 		String where = "benutzername = '" + username + "'";
 
 
 		ResultSet rs = dbc.select(select, from, where);
 		try {
 			while (rs.next()) {
 				Application currentapp;
 				currentapp = new Application(rs.getString(1), rs.getInt(2),
 						rs.getBoolean(3), rs.getString(4), rs.getBoolean(5));
 
 				applicationvec.add(currentapp);
 			}
 
 			rs.close();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return applicationvec;
 
 	}
 
 	/**
 	 * Diese Methode sammelt alle Bewerbungen zu einem bestimmten Jobangebot aus
 	 * der Datenbank und speichert diese in einem Vector.
 	 * 
 	 * @param aid
 	 *            Parameter "aid" (Angebots-Id) ist die Id des Jobangebots.
 	 * @return Es wird ein Vector mit allen Bewerbungen zu einem bestimmten
 	 *         Jobangebot aus der Datenbank zurueckgegeben.
 	 */
 	public Vector<Application> getApplicationsByOffer(int aid) {
 
 
 		Vector<Application> applicationvec = new Vector<Application>(10, 5);
 
 		String[] select = { "*" };
 		String[] from = { tableName };
 		String where = "AID = " + aid;
 
 		ResultSet rs = dbc.select(select, from, where);
 		try {
 			while (rs.next()) {
 				Application currentapp;
 				currentapp = new Application(rs.getString(1), rs.getInt(2),
 						rs.getBoolean(3), rs.getString(4), rs.getBoolean(5));
 
 				applicationvec.add(currentapp);
 			}
 
 			rs.close();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return applicationvec;
 
 	}
 
 	/**
 	 * Gibt alle Bewerbungen die von einem Verwalter abgeschlossen wurden
 	 * zurueck.
 	 * 
 	 * @param clerkname
 	 *            Name des Verwalters.
 	 * @return Es wird ein Vektor mit Bewerbungen zurueckgegeben.
 	 */
 	public Vector<Application> getApprovedApplicationsByClerk(String clerkname) {
 
 
 		Vector<Application> applicationvec = new Vector<Application>(50, 10);
 
 		String[] select = { "*" };
 		String[] from = { tableName };
 		String where = "sachbearbeiter = '" + clerkname + "'";
 
 
 		ResultSet rs = dbc.select(select, from, where);
 		try {
 			while (rs.next()) {
 				Application currentapp;
 				currentapp = new Application(rs.getString(1), rs.getInt(2),
 						rs.getBoolean(3), rs.getString(4), rs.getBoolean(5));
 
 				applicationvec.add(currentapp);
 			}
 
 			rs.close();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return applicationvec;
 	}
 
 	/**
 	 * Gibt eine Bestimmte Bewerbung zurueck. Die Bewerbung wird eindeutig durch die ID bestimmt.
 	 * 
 	 * @param AId
 	 *            Id einer Bewerbung 
 	 * @return Es wird die gesuchte Bewerbung zurueck gegeben.
 	 */
 	public Application getApplicationById(int AId) throws SQLException {
 		String[] select = { "AID" };
 		String[] from = { tableName };
 		String where = null;
 
 		ResultSet rs = dbc.select(select, from, where);
 		Application app = new Application(rs.getString(1), rs.getInt(2),
 				rs.getBoolean(3), rs.getString(4), rs.getBoolean(5));
 		return app;
 	}
 
 }
