 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import javax.swing.JOptionPane;
 
 /**
  * Beinhaltet Methoden zum Starten & Warten des Servers
  * @author Kolja Salewski
  */
 public class Server extends Thread {
 	Socket clientSocket = null;
 	ServerSocket serverSocket = null;
 	PrintWriter out;
 	BufferedReader in;
 	int x, y, level, meldungen_zaehler;
 	String in_string, out_string, antwort = "leer", schwierigkeitsgrad = "";
 	boolean verbunden = false, anfrage_erhalten = false;
 	
 	/* METHODEN: */
 	
 	// starten-Methode:
 	/**
 	 * Startet den Server, erstellt Sockets sowie Writer fuer Aus- und Reader
 	 * fuer Eingabe. Falls sich ein Client verbunden hat, wird das Spiel neu-
 	 * gestartet und begonnen auf Client-Nachrichten zu warten.
 	 */
 	void starten() {
 		System.out.println("Server gestartet");	// Test
 		System.out.println();					// Test
 		
 		try {
 			// Sockets erstellen:
 			serverSocket = new ServerSocket(4711);
 			
 			// Ausgabe:
 			Menue.meldungen[0].setText("Ihre IP-Adresse(n):");
 			//System.out.println("Ihre IP-Adresse(n):");					// Test
 	        String localHost = InetAddress.getLocalHost().getHostName();
 	        meldungen_zaehler = 1;
 	        for (InetAddress ia : InetAddress.getAllByName(localHost)) {
 	        	if (ia.getHostAddress().contains(".")) {
 	        		if (meldungen_zaehler == 4) {
 	        			meldungen_zaehler = 1;
 	        		}
 	        		
 	        		//System.out.println(ia.getHostAddress());			// Test
         			Menue.meldungen[meldungen_zaehler].setText(ia.getHostAddress());
         			meldungen_zaehler++;	        		 
 	        	}
 	    	}
 	        Menue.meldungen[4].setText("Es wird auf einen Client gewartet...");
 //			System.out.println("Es wird auf einen Client gewartet...");	// Test
 //			System.out.println();										// Test
 			
 			clientSocket = serverSocket.accept();
 			
 			// Verbindungsstatus aktualisieren:
 			verbunden = true;
 			Menue.twoPlayer = true;
			Menue.lan = true;
			Menue.hotSeat = false;
 			for (int nr = 0; nr < 5; nr++) {
 				Menue.meldungen[nr].setText("");
 			}
 //			Menue.meldungen[4].setText("Verbindung mit Client aufgebaut");
 //			System.out.println("Verbindung mit Client aufgebaut");	// Test
 //			System.out.println();									// Test
 
 			// Writer fuer Aus- & Read fuer Eingabe erstellen
 			out = new PrintWriter(clientSocket.getOutputStream(), true);
 		    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 		    
 		    // Client die aktuelle Levelnr. mitteilen:
 		    out.println("level");
 		    out.println(MapLoader.get_level());
 		    
 			// Spiel neustarten
 			Menue.spiel_neustarten();
 			
 			// Beginnen auf Client-Nachrichten zu warten
 			warte();
 		}
 		
 		catch (IOException e) {
 			for (int nr = 0; nr < 5; nr++) {
 				Menue.meldungen[nr].setText("");
 			}
 		}
 
 	}
 	
 	// warte-Methode:
 	/**
 	 * Wartet dauerhaft auf Nachrichten vom Client, nimmt diese entgegen und
 	 * fuehrt die entsprechenden Aktionen mit der zweiten Spielfigur durch
 	 */
 	void warte() {
 		try {
 			while (System.in.available()==0 && !isInterrupted())
 			{ 
 				// Nehme Client-Nachrichten entgegen
 				in_string = in.readLine();
 				
 				/*
 				 * Fuehre je nach Nachrichteninhalt verschiedene Aktionen durch:
 				 */
 
 				// Mit 2. Spielfigur Bombe legen:
 				if (in_string.equals("bomb")) {
 					Menue.spieler2_bombe();
 				}
 				
 				// Frage ausgeben:
 				else if (in_string.contains("?")) {
 					
 					int frage = JOptionPane.showConfirmDialog(null,
 							in_string,
 							"Frage des Clients", JOptionPane.YES_NO_OPTION);
 					
 					switch (frage) {
 						case 0:
 							if (in_string.contains("Spieler")) {
 								System.out.println("yes geschickt");
 								out.println("yes");	
 							}
 							
 							else {
 								out.println("Spieler 1 moechte das Spiel neustarten. Soll das Spiel neugestartet werden?");
 								//Menue.antwort_erhalten = true;
 								antwort = "rueckfrage";
 //								Menue.createAndShowGui(
 //								"Spieler 2 wurde eine Anfrage zum Neustart des Spiels geschickt. Warte ",
 //								" auf Antwort...", 60, 600, 100, 0, "", "neustart");
 							}
 
 							//Menue.createAndShowGui("Das Spiel wird in ", " neugestartet...", 5, 300, 100, 0); // BITTE AUSKOMMENTIERT LASSEN & NICHT LOESCHEN
 							break;
 						case 1:
 							out.println("no");
 							//Menue.createAndShowGui("Das Spiel wird in ", " fortgesetzt...", 5, 300, 100, 0); // BITTE AUSKOMMENTIERT LASSEN & NICHT LOESCHEN
 							break;
 					}
 					
 				}
 				
 				// Antwort speichern:
 				else if (in_string.equals("yes") || in_string.equals("no")) {
 					System.out.println("antwort erhalten");
 					antwort = in_string;
 				}
 				
 //				// Abfrage zum Neustart des Spiels ausgeben:
 //				else if (in_string.equals("abfrage_neustarten")) {
 //					anfrage_erhalten = true;
 //					Menue.abfrage_neustarten();
 //					anfrage_erhalten = false;
 //				}
 				
 				// Spiel neustarten:
 				else if (in_string.equals("neustart")) {
 					antwort = "leer";
 					Menue.antwort_erhalten = true;
 					
 					//Menue.createAndShowGui("Das Spiel wird in ", " neugestartet...", 5, 300, 100, 0);
 					Menue.spiel_neustarten();
 				}
 				
 				// Level wechseln:
 				else if (in_string.equals("level")) {
 					level = Integer.parseInt(in.readLine());
 					antwort = "leer";
 					
 					//Menue.createAndShowGui("Es wird in ", " zu Level " + level + " gewechselt...", 5, 400, 100, level); // BITTE AUSKOMMENTIERT LASSEN
 					MapLoader.set_level(level);
 					Menue.spiel_neustarten();
 				}
 				
 				// Schwierigkeitsgrad wechseln:
 				else if (in_string.equals("schwierigkeitsgrad")) {
 					schwierigkeitsgrad = in.readLine();
 					antwort = "leer";
 					Menue.antwort_erhalten = true;
 					
 					Menue.schwierigkeitsgrad_aendern(schwierigkeitsgrad);
 					Menue.spiel_neustarten();
 				}
 				
 				// 2. Spielfigur bewegen:
 				else {														// ...sonst...
 					System.out.println("x = " + in_string);	// Test
 					System.out.println();					// Test
 					
 					x = Integer.parseInt(in_string);						// ...interpretiere
 																			// den int-Wert der
 																			// Nachricht als x-
 																			// Bewegung,...
 					
 					System.out.println("x-Bewegung von Spieler 2: " + x); 	// Test
 					System.out.println();									// Test
 					
 					in_string = in.readLine();								// ...lese die
 																			// naechste
 																			// Nachricht,...
 					System.out.println("y = " + in_string);	// Test
 					System.out.println();					// Test
 					
 					y = Integer.parseInt(in_string);						// ...interpretiere
 																			// den int-Wert der
 																			// zweiten Nachricht
 																			// als y-Bewegung und...
 					
 					System.out.println("y-Bewegung von Spieler 2: " + y); 	// Test
 					System.out.println();									// Test
 	
 					Menue.spieler2_aktionen(x, y);							// ...fuehre die Bewegungen
 																			// mit der 2. Spielfigur
 																			// durch
 				}
 				
 			}
 			
 		}
 		
 		catch (IOException e) {
 			JOptionPane.showMessageDialog(null, "Verbindung zum Client getrennt");
 			if (Menue.hotSeat == false) {
 				Menue.singleplayer_starten();	
 			}
 			
 		}
 		
 		catch (NullPointerException ex) {
 			JOptionPane.showMessageDialog(null, "Verbindung zum Client getrennt");
 			if (Menue.hotSeat == false) {
 				Menue.singleplayer_starten();	
 			}
 			
 		}
 		
 	}
 	
 	// run-Methode:
 	/**
 	 * Laesst den Server starten
 	 */
 	@Override
 	public void run() {
 		starten();
 	}
 	
 }
