 import java.awt.event.KeyEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class Eingabe {
 
 	static String[] CtrlArray = new String[6]; /*
 												 * Feld zum speichern der
 												 * Steuerung
 												 */
 
 	/*
 	 * Diese Methode kriegt das KeyEvent bergeben das beim Tastendruck
 	 * ausgelst wurde bergeben. Es gleicht den KeyText mit den
 	 * eingespeicherten Steuerungs-Optionen ab und bergibt falls diese
 	 * bereinstimmen den Befehl frs Bewegen usw. an die Funktion weiter die
 	 * Bomberman auf der GUI bewegt weiter
 	 */
 
 	public static String Ctrl(KeyEvent e) { /* erhlt KeyEvent */
 
 		int code = e.getKeyCode(); /*
 									 * Umwandelung des KeyEvents in die passende
 									 * KeyCode
 									 */
 		String KeyId = KeyEvent.getKeyText(code); /*
 												 * Umwandelung des KeyCodes in
 												 * den KeyText
 												 */
 
 		if (KeyId.equals(CtrlArray[0])) { /*
 										 * Vergleichen der Steuerungs-Optionen
 										 * mit dem KeyText
 										 */
 			System.out.println("Es ist oben");
 		} else if (KeyId.equals(CtrlArray[1])) {
 			System.out.println("Es ist unten");
 		} else if (KeyId.equals(CtrlArray[2])) {
 			System.out.println("Es ist rechts");
 		} else if (KeyId.equals(CtrlArray[3])) {
 			System.out.println("Es ist links");
 		} else if (KeyId.equals(CtrlArray[4])) {
 			System.out.println("Bombelegen");
 		} else if (KeyId.equals(CtrlArray[5])) {
 			System.out.println("Ingame-Menue");
 		}
 
 		return KeyId;
 	}
 
 	/*
 	 * Diese Funktion liest die zuletzt gespeicherten Steuerungs-Optionen aus
 	 * der Datei Test.TXT aus. Zustzlich legt sie diese im Feld CtrlArray fest.
 	 */
 
 	public static void CtrlReader() { /* 0815 Reader */
 		String Ctrl = "";
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader("Test.TXT"));
 			Ctrl = in.readLine();
 		} catch (IOException e) {
 		}
 
 		System.out.println(Ctrl);
 
 		CtrlArray = Ctrl.split("_"); /*
 									 * Splitten des gelesenen Strings am "_" und
 									 * fllen den Feldes was zum vergleichen mit
 									 * der gedrckten Taste ntig ist
 									 */
 	}
 
 	/*
 	 * Diese Funktion schreibt die im Menue fest gelegten Steuerungs-Optionen in
 	 * die Datei Test.TXT.(Name noch ndern)
 	 */
 
 	public static void CtrlWrite() { /* 0815 Writer */
		String CtrlString = "Oben_Unten_Rechts_Links_Leertaste_ESC_";
 
		
 
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter("Test.TXT"));
 			out.write(CtrlString);
 			out.close();
 		} catch (IOException e) {
 		}
 
 	}
 
 }
