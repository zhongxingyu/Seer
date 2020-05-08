 /*
  * jCrypt - Programmierumgebung für das Kryptologie-Praktikum
  * Studienarbeit am Institut für Theoretische Informatik der
  * Technischen Universität Braunschweig
  * 
  * Datei:        IDEA.java
  * Beschreibung: Dummy-Implementierung des International Data Encryption
  *               Algorithm (IDEA)
  * Erstellt:     30. März 2010
  * Autor:        Martin Klußmann
  */
 
 package task3;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Random;
 import java.util.StringTokenizer;
 
 import de.tubs.cs.iti.jcrypt.chiffre.BlockCipher;
 
 /**
  * Dummy-Klasse für den International Data Encryption Algorithm (IDEA).
  *
  * @author Martin Klußmann
  * @version 1.1 - Sat Apr 03 21:57:35 CEST 2010
  */
 public final class IDEA extends BlockCipher {
   
   String keyString;
   
   
   /**
    * Entschlüsselt den durch den FileInputStream <code>ciphertext</code>
    * gegebenen Chiffretext und schreibt den Klartext in den FileOutputStream
    * <code>cleartext</code>.
    *
    * @param ciphertext
    * Der FileInputStream, der den Chiffretext liefert.
    * @param cleartext
    * Der FileOutputStream, in den der Klartext geschrieben werden soll.
    */
   public void decipher(FileInputStream ciphertext, FileOutputStream cleartext) {
 
   }
 
   /**
    * Verschlüsselt den durch den FileInputStream <code>cleartext</code>
    * gegebenen Klartext und schreibt den Chiffretext in den FileOutputStream
    * <code>ciphertext</code>.
    * 
    * @param cleartext
    * Der FileInputStream, der den Klartext liefert.
    * @param ciphertext
    * Der FileOutputStream, in den der Chiffretext geschrieben werden soll.
    */
   public void encipher(FileInputStream cleartext, FileOutputStream ciphertext) {
 
   }
 
   /**
    * Erzeugt einen neuen Schlüssel.
    * 
    * @see #readKey readKey
    * @see #writeKey writeKey
    */
   public void makeKey() {
     BufferedReader standardInput = launcher.openStandardInput();
     keyString = new String();
     char keyCharArray[] = new char[16];
     
     // Auswahl eingeben oder generieren:
     int choice = 0;
     try {
       Logger("[0] Möchtest du einen eigenen Schlüssel eingeben\n[1] oder einen Schlüssel zufällig generieren?");
       choice = Integer.parseInt(standardInput.readLine());
     } catch (NumberFormatException e) {
       e.printStackTrace();
     } catch (IOException e) {
       Logger("Problem beim Einlesen");
       e.printStackTrace();
     }
     
     if (choice == 0) { // eingeben
       
       try {
         Logger("Bitte gib einen 16 Zeichen langen Schlüssel ein:");
         keyString = standardInput.readLine();
       } catch (NumberFormatException e) {
         e.printStackTrace();
       } catch (IOException e) {
         Logger("Problem beim Einlesen");
         e.printStackTrace();
       }
       
       if (keyString.length() == 16) {
         keyCharArray = keyString.toCharArray();
         for (int i = 0; i < keyCharArray.length; i++) {
           if (keyCharArray[i] > 128) { // > 2^8-1
             Logger("Du hast ein Sonderzeichen verwendet, das nicht im ASCII-Zeichensatz verfügbar ist.");
             System.exit(0);
           }
         }
       }
       else {
         Logger("Der Schlüssel muss 16 Zeichen lang sein!");
       }
       
     }
     else if (choice == 1) { //zufällig generieren
       Random rand = new Random();
       
       for (int i = 0; i < keyCharArray.length; i++) {
         keyCharArray[i] = (char) rand.nextInt(128); // zufällig von 0...127
         keyString += "" + keyCharArray[i];
       }
       
       // print info
       String integerValues = new String();
       for (int i = 0; i < keyCharArray.length; i++) {
        keyCharArray[i] = (char) rand.nextInt(128); // zufällig von 0...127
         integerValues += "" + (int) keyCharArray[i] + ", ";
       }
       
       Logger("Zufällige Werte: "+integerValues);
       Logger("Der Schlüssel wurde zufällig generiert!");
     }
     else {
       Logger("Falsche Eingabe!");
     }
     
     
   }
 
   /**
    * Liest den Schlüssel mit dem Reader <code>key</code>.
    * 
    * @param key
    * Der Reader, der aus der Schlüsseldatei liest.
    * @see #makeKey makeKey
    * @see #writeKey writeKey
    */
   public void readKey(BufferedReader key) {
     try {
       StringTokenizer st = new StringTokenizer(key.readLine(), " ");
       
       keyString = new String();
       keyString = st.nextToken();
 
       Logger("Reading Information: ");
       Logger("+--KeyString: " + keyString);
 
       key.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Lesen oder Schließen der " + "Schlüsseldatei.");
       e.printStackTrace();
       System.exit(1);
     } catch (NumberFormatException e) {
       System.err.println("Abbruch: Fehler beim Parsen eines Wertes aus der " + "Schlüsseldatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   /**
    * Schreibt den Schlüssel mit dem Writer <code>key</code>.
    * 
    * @param key
    * Der Writer, der in die Schlüsseldatei schreibt.
    * @see #makeKey makeKey
    * @see #readKey readKey
    */
   public void writeKey(BufferedWriter key) {
     try {
       key.write(keyString);
       key.newLine();
 
       Logger("Writing Information: ");
       Logger("+--Key: " + keyString);
 
       key.close();
     } catch (IOException e) {
       System.out.println("Abbruch: Fehler beim Schreiben oder Schließen der " + "Schlüsseldatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
   
   private static void Logger(String event) {
     System.out.println("IDEA$  " + event);
   }
 }
