 package cerb;
 
 /* Simulation einer Kerberos-Session mit Zugriff auf einen Fileserver
 /* Server-Klasse
  */
 
 import java.util.*;
 import java.io.*;
 
 public class Server extends Object {
 
 	private final long fiveMinutesInMillis = 300000; // 5 Minuten in Millisekunden
 
     private String myName;
     private KDC myKDC;
     private long myKey;
 
     // Konstruktor
     public Server(String name) {
     	  myName = name;
     }
 
 	public String getName() {
 		return myName;
 	}
 
     public void setupService(KDC kdc) {
 		// Anmeldung des Servers beim KDC
 		myKDC = kdc;
 		myKey = myKDC.serverRegistration(myName);
 		System.out.println("Server " + myName + " erfolgreich registriert bei KDC "
 							+ myKDC.getName() + " mit ServerKey " + myKey);
 	}
 
 	public boolean requestService(Ticket srvTicket, Auth srvAuth, String command, String parameter) {
 		/*Anforderung eines Service-Request bearbeiten*/
 		String clientName = null;
 		boolean result = false;
 		//ServerTicket + Authentifikation prfen
 		//Ticket
 		srvTicket.decrypt(myKey);
 		clientName = srvTicket.getClientName();
 		srvAuth.decrypt(srvTicket.getSessionKey());
 		if(srvTicket.getServerName() == myName){
 			//richtiger Server
			if(timeValid(srvTicket.getStartTime(), srvTicket.getEndTime()) && srvAuth.getClientName() == clientName){
 				//noch nicht abgelaufen, Name ok
 				//Befehl ausfhren (showFile)
 				if(command == "showFile"){
 					result = showFile(parameter);
 				}
 			}
 		}
 		return result;
 	}
 
 	/* *********** Services **************************** */
 
 	private boolean showFile(String filePath) {
 		// Rckgabe: Status der Operation
 		String lineBuf = null;
 		File myFile = new File(filePath);
 
 		if (!myFile.exists()) {
 			System.out.println("Datei " + filePath + " existiert nicht!");
 			return false;
 		}
 		try {
 			BufferedReader inFile = new BufferedReader(new InputStreamReader(
 							new FileInputStream(myFile)));
 			lineBuf = inFile.readLine();
 			while (lineBuf != null) {
 		    	System.out.println(lineBuf);
 		    	lineBuf = inFile.readLine();
 			}
 			inFile.close();
 		} catch (IOException ex) {
 					System.out.println("Fehler beim Lesen der Datei " + filePath + ex);
 					return false;
     	}
 		return true;
 	}
 
 	/* *********** Hilfsmethoden **************************** */
 
 	private boolean timeValid (long lowerBound, long upperBound) {
 			long currentTime = (new Date()).getTime();  // Anzahl mSek. seit 1.1.1970
 			if (currentTime >= lowerBound &&
 			    currentTime <= upperBound) {
 				return true;
 			} else {
 				System.out.println("-------- Time not valid: " + currentTime + " not in (" +
 						lowerBound + "," + upperBound + ")!");
 				return false;
 			}
 	}
 
 	boolean timeFresh (long testTime) {
 			// Wenn die bergebene Zeit nicht mehr als 5 Minuten von der aktuellen Zeit abweicht,
 			// wird true zurckgegeben
 			long currentTime = (new Date()).getTime();  // Anzahl mSek. seit 1.1.1970
 				if (Math.abs(currentTime - testTime) < fiveMinutesInMillis) {
 					return true;
 				} else {
 					System.out.println("-------- Time not fresh: " + currentTime + " is current, " +
 						 testTime + " is old!");
 					return false;
 				}
 	}
 }
 
