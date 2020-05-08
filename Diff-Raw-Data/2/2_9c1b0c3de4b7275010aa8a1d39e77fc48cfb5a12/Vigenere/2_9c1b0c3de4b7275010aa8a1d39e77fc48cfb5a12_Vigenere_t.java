 /*
  * jCrypt - Programmierumgebung fÃ¼r das Kryptologie-Praktikum
  * Studienarbeit am Institut fÃ¼r Theoretische Informatik der
  * Technischen UniversitÃ¤t Braunschweig
  * 
  * Datei:        Vigenere.java
  * Beschreibung: Dummy-Implementierung der VigenÃ¨re-Chiffre
  * Erstellt:     30. MÃ¤rz 2010
  * Autor:        Martin KluÃmann
  */
 
 package task1;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import de.tubs.cs.iti.jcrypt.chiffre.CharacterMapping;
 import de.tubs.cs.iti.jcrypt.chiffre.Cipher;
 
 import java.util.Vector;
 
 /**
  * Dummy-Klasse fÃ¼r die VigenÃ¨re-Chiffre.
  *
  * @author Martin KluÃmann
  * @version 1.0 - Tue Mar 30 15:53:38 CEST 2010
  */
 public class Vigenere extends Cipher {
 
   protected Vector<Integer> keys = new Vector<Integer>();
 
   /**
    * Analysiert den durch den Reader <code>ciphertext</code> gegebenen
    * Chiffretext, bricht die Chiffre bzw. unterstÃ¼tzt das Brechen der Chiffre
    * (ggf. interaktiv) und schreibt den Klartext mit dem Writer
    * <code>cleartext</code>.
    *
    * @param ciphertext
    * Der Reader, der den Chiffretext liefert.
    * @param cleartext
    * Der Writer, der den Klartext schreiben soll.
    */
   public void breakCipher(BufferedReader ciphertext, BufferedWriter cleartext) {
 
   }
 
   /**
    * EntschlÃ¼sselt den durch den Reader <code>ciphertext</code> gegebenen
    * Chiffretext und schreibt den Klartext mit dem Writer
    * <code>cleartext</code>.
    *
    * @param ciphertext
    * Der Reader, der den Chiffretext liefert.
    * @param cleartext
    * Der Writer, der den Klartext schreiben soll.
    */
   public void decipher(BufferedReader ciphertext, BufferedWriter cleartext) {
 
     try {
       int character, ord = 0;
       boolean characterSkipped = false;
       while ((character = ciphertext.read()) != -1) {
         character = charMap.mapChar(character);
         if (character != -1) {
           character = (character - keys.get(ord) + modulus) % modulus;
           character = charMap.remapChar(character);
           cleartext.write(character);
           ord = (ord+1) % keys.size();
         } else {
           // doing nothing with this (for now)
           characterSkipped = true;
         }
       }
       cleartext.close();
       ciphertext.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Zugriff auf Klar- oder "
           + "Chiffretextdatei.");
       e.printStackTrace();
       System.exit(1);
     }
 
   }
 
   /**
    * VerschlÃ¼sselt den durch den Reader <code>cleartext</code> gegebenen
    * Klartext und schreibt den Chiffretext mit dem Writer
    * <code>ciphertext</code>.
    * 
    * @param cleartext
    * Der Reader, der den Klartext liefert.
    * @param ciphertext
    * Der Writer, der den Chiffretext schreiben soll.
    */
   public void encipher(BufferedReader cleartext, BufferedWriter ciphertext) {
 
     try {
       int character, ord = 0;
       boolean characterSkipped = false;
       while ((character = cleartext.read()) != -1) {
         character = charMap.mapChar(character);
         if (character != -1) {
           character = (character + keys.get(ord)) % modulus;
           character = charMap.remapChar(character);
           ciphertext.write(character);
           ord = (ord+1) % keys.size();
         } else {
           // doing nothing with this (for now)
           characterSkipped = true;
         }
       }
       cleartext.close();
       ciphertext.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Zugriff auf Klar- oder "
           + "Chiffretextdatei.");
       e.printStackTrace();
       System.exit(1);
     }
 
   }
 
   /**
    * Erzeugt einen neuen SchlÃ¼ssel.
    * 
    * @see #readKey readKey
    * @see #writeKey writeKey
    */
   public void makeKey() {
 	  BufferedReader standardInput = launcher.openStandardInput();
 	    boolean accepted = false;
 	    String msg = "Geeignete Werte fr den Modulus werden in der Klasse "
 	        + "'CharacterMapping'\nfestgelegt. Probieren Sie ggf. einen Modulus "
 	        + "von 26, 27, 30 oder 31.\nDie Verschiebung mu grer oder gleich 0 "
 	        + "und kleiner als der gewhlte\nModulus sein.";
 	    System.out.println(msg);
 	    // Frage jeweils solange die Eingabe ab, bis diese akzeptiert werden kann.
 	    do {
 	      System.out.print("Geben Sie den Modulus ein: ");
 	      try {
 	        modulus = Integer.parseInt(standardInput.readLine());
 	        if (modulus < 1) {
 	          System.out.println("Ein Modulus < 1 wird nicht akzeptiert. Bitte "
 	              + "korrigieren Sie Ihre Eingabe.");
 	        } else {
 	          // Prfe, ob zum eingegebenen Modulus ein Default-Alphabet existiert.
 	          String defaultAlphabet = CharacterMapping.getDefaultAlphabet(modulus);
 	          if (!defaultAlphabet.equals("")) {
 	            msg = "Vordefiniertes Alphabet: '" + defaultAlphabet
 	                + "'\nDieses vordefinierte Alphabet kann durch Angabe einer "
 	                + "geeigneten Alphabet-Datei\nersetzt werden. Weitere "
 	                + "Informationen finden Sie im Javadoc der Klasse\n'Character"
 	                + "Mapping'.";
 	            System.out.println(msg);
 	            accepted = true;
 	          } else {
 	            msg = "Warnung: Dem eingegebenen Modulus kann kein Default-"
 	                + "Alphabet zugeordnet werden.\nErstellen Sie zustzlich zu "
 	                + "dieser Schlssel- eine passende Alphabet-Datei.\nWeitere "
 	                + "Informationen finden Sie im Javadoc der Klasse 'Character"
 	                + "Mapping'.";
 	            System.out.println(msg);
 	            accepted = true;
 	          }
 	        }
 	      } catch (NumberFormatException e) {
 	        System.out.println("Fehler beim Parsen des Modulus. Bitte korrigieren"
 	            + " Sie Ihre Eingabe.");
 	      } catch (IOException e) {
 	        System.err
 	            .println("Abbruch: Fehler beim Lesen von der Standardeingabe.");
 	        e.printStackTrace();
 	        System.exit(1);
 	      }
 	    } while (!accepted);
 	    accepted = true;
 	    do {
 	      try {
 		    keys.clear();
 	        System.out.print("Geben Sie die durch Leerzeichen getrennten Keys ein: ");
 	        StringTokenizer stKeys = new StringTokenizer(standardInput.readLine(), " ");
 	        while (stKeys.hasMoreElements()) {
 	        	int key = Integer.parseInt(stKeys.nextToken());
 	        	if (key >=0 && key < modulus) {
 	        		this.keys.add(key);
 	        	}
 	        	else {
 	  	          System.out.println("Error: " + key + "is an invald key (key must be >= 0 and < modulus)");
 		        	accepted = false;
 	        	}
 	        }
 	        if (keys.size() == 0) {
 	        	accepted = false;
 	        }
 	      } catch (NumberFormatException e) {
 	        System.out.println("Fehler beim Parsen der Verschiebung. Bitte "
 	            + "korrigieren Sie Ihre Eingabe.");
 	      } catch (IOException e) {
 	        System.err
 	            .println("Abbruch: Fehler beim Lesen von der Standardeingabe.");
 	        e.printStackTrace();
 	        System.exit(1);
 	      }
 	    } while (!accepted);
   }
 
   /**
    * Liest den SchlÃ¼ssel mit dem Reader <code>key</code>.
    * 
    * @param key
    * Der Reader, der aus der SchlÃ¼sseldatei liest.
    * @see #makeKey makeKey
    * @see #writeKey writeKey
    */
   public void readKey(BufferedReader key) {
 	    try {
 	        StringTokenizer st = new StringTokenizer(key.readLine(), " ");
 	        modulus = Integer.parseInt(st.nextToken());
 	        System.out.println("Modulus: " + modulus);
 	        while (st.hasMoreElements()) {
 	        	this.keys.add(Integer.parseInt(st.nextToken()));
 	        }
 	        key.close();
 	      } catch (IOException e) {
 	        System.err.println("Abbruch: Fehler beim Lesen oder SchlieÃen der "
 	            + "Schlsseldatei.");
 	        e.printStackTrace();
 	        System.exit(1);
 	      } catch (NumberFormatException e) {
 	        System.err.println("Abbruch: Fehler beim Parsen eines Wertes aus der "
 	            + "Schlsseldatei.");
 	        e.printStackTrace();
 	        System.exit(1);
 	      }
   }
 
   /**
    * Schreibt den SchlÃ¼ssel mit dem Writer <code>key</code>.
    * 
    * @param key
    * Der Writer, der in die SchlÃ¼sseldatei schreibt.
    * @see #makeKey makeKey
    * @see #readKey readKey
    */
   public void writeKey(BufferedWriter key) {
 	    try {
	        key.write(String.valueOf(modulus));
 	        for (int k: keys) {
 	        	key.write(" " + k);
 	        }
 	        key.newLine();
 	        key.close();
 	      } catch (IOException e) {
 	        System.out.println("Abbruch: Fehler beim Schreiben oder SchlieÃen der "
 	            + "SchlÃ¼sseldatei.");
 	        e.printStackTrace();
 	        System.exit(1);
 	      }
   }
 }
