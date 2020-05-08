 import java.net.*;
 import java.util.*; //Bib für Eingabe über Konsole & Vector
 
 //import java.util.Vector;
 import java.io.*;
 
 import com.google.api.detect.Detect;
 import com.google.api.translate.Language;
 import com.google.api.translate.Translate;
 
 public class client implements Runnable
 {
 	public static int port = 8765;
 	public static String ip;
 	protected Socket socket;
 	protected Scanner in;
 	private volatile Thread connect;
 	private String consoleinput;
 	public String username;
 
 	
 	/**
 	 * Initialisierung
 	 * Verbindung aufbauen
 	 */
 	public client()
 	{
 		in = new Scanner(System.in);
		if (ip.isEmpty()) {
			System.out.println("Bitte geben Sie eine IP-Adresse ein:");
			ip = in.nextLine();
			// TODO Die Variable input muss auf Richtigkeit überprüft werden!
			// TODO vielleicht Abfrage weg lassen und nur mit Startoptionen arbeiten?
		}
 		System.out.println("Bitte geben Sie Ihren Nickname ein:");
 		do {
 			if (username != null && username.contains(" "))
 				System.out.println("In Ihrem Benutzernamen darf kein Leerzeichen enthalten sein." +
 						           " Bitte geben Sie erneut ein:");
 			username = in.nextLine();
 		} while( username.isEmpty() || username.contains(" ") );
 		
 		try
 		{
 			socket = new Socket( ip, port);
 		} catch (IOException e)
 		{
 			System.err.println("Verbindung fehlgeschlagen:"+ e);
 			System.exit(1);
 		}
 		
 		connect = new Thread(this);
 		connect.start();
 	}
 	
 	/**
 	 * Beenden
 	 */
 	public void done()
 	{
 		// Zeugs um Thread zu stoppen
 		Thread moribund = connect;
 		connect = null;
 		moribund.interrupt();
 	}
 	
 	/**
 	 * Wartet auf Konsoleneingabe
 	 */
 	public void run()
 	{		
 		connection_client c = new connection_client(socket, this, username);
 		
 		// Zeugs um Thread zu stoppen
 		// siehe auch http://download.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
 		Thread thisThread = Thread.currentThread();
 		while (connect == thisThread) {
 			//Abfrage der Konsoleneingabe
 			consoleinput = in.nextLine();
 						
 			c.out.println( consoleinput );
 			
 			if(consoleinput.matches("/quit")) {
 				//alle Threads beenden
 				c.done();
 				this.done();
 			}
 		}
 		System.out.println("bye bye ...");
 	}
 	
 	/**
 	 * Überprüfung der Startparameter
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		if (args.length > 0) {
 		for ( int i = 0; i < args.length - 1; i++) { // -1 da zwei argumente vorhanden sein müssen: flag und option
 			if( args[i].equals("--port")) {
 				try {
 					port = Integer.parseInt(args[i+1]);
 				} catch (NumberFormatException e) {
 					System.err.println("Portnummer ist kein Integer");
 					System.exit(1);
 				}
 				if (port < 1 || port > 65535) {
 					System.err.println("Portnummer ist ungültig");
 					System.exit(1);
 				}
 			}
			if( args[i].equals("--ip")) {
				ip = args[i+1];
			}
 		}
 	}
 		new client();
 	}
 	
 	/**
 	 * 
 	 * @param text der zu übersetzende Text
 	 * @return übersetzer Text, oder leerer String, wenn nicht erfolgreich
 	 */
 	private String translate(String text)
 	{
 		Translate.setHttpReferrer("moep.de"); // wozu?
 		Language lang = null;
 		String translatedText = new String();
 		
 		// Spracherkennung
 		try {
 			lang = Detect.execute(text).getLanguage();
 		} catch (Exception e) {
 			System.err.println("Fehler bei Spracherkennung: " + e);
 			return translatedText;
 		}
 		
 		//Übersetze Text
 		try {
 			translatedText = Translate.execute(text, lang, Language.ENGLISH);
 			// FIXME Alles ins Englische übersetzen? bäää...
 		} catch (Exception e) {
 			System.err.println("Fehler bei Überstzung: " + e);
 		}
 		
 		return translatedText;
 		
 	}
 	
 	/**
 	 * Zeige String in der Konsole an
 	 * @param line
 	 */
 	public void display(String line)
 	{
 		if ( !line.startsWith(username) ) {
 			// String newtext = translate(line);
 			//if ( !newtext.isEmpty() ) {
 			//	System.out.println(newtext);
 			//} else {
 				System.out.println(line);
 			//}
 		}
 	}
 }
