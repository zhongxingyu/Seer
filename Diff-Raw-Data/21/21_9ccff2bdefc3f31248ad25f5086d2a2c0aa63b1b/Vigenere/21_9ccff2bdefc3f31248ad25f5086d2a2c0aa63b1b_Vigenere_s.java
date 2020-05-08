 /*
  * jCrypt - Programmierumgebung für das Kryptologie-Praktikum
  * Studienarbeit am Institut für Theoretische Informatik der
  * Technischen Universität Braunschweig
  * 
  * Datei:        Vigenere.java
  * Beschreibung: Dummy-Implementierung der Vigenère-Chiffre
  * Erstellt:     30. März 2010
  * Autor:        Martin Klußmann
  */
 
 package task1;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 import de.tubs.cs.iti.jcrypt.chiffre.CharacterMapping;
 import de.tubs.cs.iti.jcrypt.chiffre.Cipher;
 import de.tubs.cs.iti.jcrypt.chiffre.FrequencyTables;
 import de.tubs.cs.iti.jcrypt.chiffre.NGram;
 
 /**
  * Dummy-Klasse für die Vigenère-Chiffre.
  * 
  * @author Martin Klußmann
  * @version 1.0 - Tue Mar 30 15:53:38 CEST 2010
  */
 public class Vigenere extends Cipher {
 
   /**
    * keyword
    */
   private int[] keyword;
 
   /**
    * Analysiert den durch den Reader <code>ciphertext</code> gegebenen Chiffretext, bricht die
    * Chiffre bzw. unterstützt das Brechen der Chiffre (ggf. interaktiv) und schreibt den Klartext
    * mit dem Writer <code>cleartext</code>.
    * 
    * @param ciphertext
    *          Der Reader, der den Chiffretext liefert.
    * @param cleartext
    *          Der Writer, der den Klartext schreiben soll.
    */
   public void breakCipher(BufferedReader ciphertext, BufferedWriter cleartext) {
     // ciphertext als list mit integer representations der characters bauen
     LinkedList<Integer> ciphertextList = new LinkedList<Integer>();
     try {
       int character;
       while ((character = ciphertext.read()) != -1) {
         if (character != -1) {
           ciphertextList.add(character);
         } else {
           // Ein überlesenes Zeichen sollte bei korrekter Chiffretext-Datei
           // eigentlich nicht auftreten können.
         }
       }
       cleartext.close();
       ciphertext.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Zugriff auf Klar- oder " + "Chiffretextdatei.");
       e.printStackTrace();
       System.exit(1);
     }
 
     HashMap<Integer, Integer> quantities = getQuantities(ciphertextList);
 
 //    Logger("quantities: " + quantities.toString());
 
     int N = ciphertextList.size();
 
     double IC = IC(N, quantities);
     Logger("IC= " + IC);
 
     int d = d(N, IC);
     Logger("d= " + d);
 
     int[] key = new int[d];
 
     for (int i = 0; i < d; i++) {
       LinkedList<Integer> sublist = getSublist(ciphertextList, i, d);
//      Logger("" + sublist);
       HashMap<Integer, Integer> quantityHashMap = getQuantities(sublist);
//      Logger("" + quantityHashMap);
       key[i] = calculateShift(quantityHashMap);
     }
 
     String keyOutput = "";
     String keyOutputRemaped = "";
     for (int j = 0; j < key.length; j++) {
       // int:
       keyOutput += key[j] + " ";
       // ascii:
      int remapedChar = charMap.mapChar(key[j]);
       keyOutputRemaped += remapedChar + " ";
     }
 
     Logger("Key as Integers: " + keyOutput);
      Logger("Key as ASCII: " + keyOutputRemaped);
 
     Logger("ende");
   }
 
   /**
    * Generiere HashMap mit allen im Chiffretext vorkommenden Buchstaben und der jeweiligen Anzahl
    * 
    * @param ciphertextList
    * @return quantities
    */
   private HashMap<Integer, Integer> getQuantities(LinkedList<Integer> ciphertextList) {
     // häufigkeiten der einzelnen buchstaben als hashmap
     HashMap<Integer, Integer> quantities = new HashMap<Integer, Integer>();
 
     Iterator<Integer> iter = ciphertextList.iterator();
     int character;
     while (iter.hasNext()) {
       character = iter.next();
       // Erhöhe die Anzahl für dieses Zeichen bzw. lege einen neuen Eintrag
       // für dieses Zeichen an.
       if (quantities.containsKey(character)) {
         quantities.put(character, quantities.get(character) + 1);
       } else {
         quantities.put(character, 1);
       }
     }
     return quantities;
   }
 
   private LinkedList<Integer> getSublist(LinkedList<Integer> list, int start, int period) {
     LinkedList<Integer> subList = new LinkedList<Integer>();
 
     for (int i = start; i < list.size(); i = i + period) {
       subList.add(list.get(i));
     }
 
     return subList;
   }
 
   int calculateShift(HashMap<Integer, Integer> quantityHashMap) {
    int currKey = -1, currValue = -1, greatest = -1, mostFrequented = -1;
     ArrayList<NGram> nGrams = FrequencyTables.getNGramsAsList(1, charMap);
 
     Iterator<Integer> it = quantityHashMap.keySet().iterator();
     while (it.hasNext()) {
       currKey = it.next();
       currValue = quantityHashMap.get(currKey);
       if (currValue > greatest) {
         greatest = currValue;
         mostFrequented = currKey;
       }
     }

    int computedShift = mostFrequented - charMap.mapChar(Integer.parseInt(nGrams.get(0).getIntegers()));
     if (computedShift < 0) {
       computedShift += modulus;
     }
     int shift = computedShift;
 
     return shift;
 
   }
 
   private double IC(int N, HashMap<Integer, Integer> quantities) {
     int currentCharacter, F, sum = 0;
     double IC;
 
     Iterator<Integer> iter = quantities.keySet().iterator();
     while (iter.hasNext()) {
       currentCharacter = (int) iter.next();
       F = quantities.get(currentCharacter);
 
       sum += F * (F - 1);
     }
 
     IC = (double) sum / (N * (N - 1));
 
     return IC;
   }
 
   private int d(int N, double IC) {
 //    Logger("N, IC, modulus " + N + "," + IC + "," + modulus);
     // Summe der relativen Häufigkeiten eines beliebigen zufälligen chiffretextes:
     double sum = sumP();
 
 //    Logger("sum= " + sum);
 
     double enumerator = ((sum - (1 / (double) modulus)) * (double) N);
     double denominator = (((double) N - 1) * IC - (1 / (double) modulus) * (double) N + sum);
 
     int d = (int) Math.round(enumerator / denominator);
 
     return d;
   }
 
   /**
    * Berechne summe mit pi's für standard nGram frequency tabellen
    * 
    * @param modulus
    * @return
    */
   private double sumP() {
     // unigramm frequency tabelle
     ArrayList<NGram> nGrams = FrequencyTables.getNGramsAsList(1, charMap);
 
     NGram currentNGram;
     double p, sum = 0;
     Iterator<NGram> iter = nGrams.iterator();
     while (iter.hasNext()) {
       currentNGram = iter.next();
 
       // get frequency as percentage
       p = currentNGram.getFrequency() / 100;
       sum += p * p;
     }
 
     return sum;
   }
 
   /**
    * Entschlüsselt den durch den Reader <code>ciphertext</code> gegebenen Chiffretext und schreibt
    * den Klartext mit dem Writer <code>cleartext</code>.
    * 
    * @param ciphertext
    *          Der Reader, der den Chiffretext liefert.
    * @param cleartext
    *          Der Writer, der den Klartext schreiben soll.
    */
   public void decipher(BufferedReader ciphertext, BufferedWriter cleartext) {
     // Kommentierung analog 'encipher(cleartext, ciphertext)'.
     try {
 
       int character;
       int d = 0;
 
       while ((character = ciphertext.read()) != -1) {
         character = charMap.mapChar(character);
         if (character != -1) {
 
           int index = d++ % keyword.length;
 
           // int keyVal = keyword[index];
           // int keyVal = charMap.mapChar(keyword[index]);
 
           int val = character - keyword[index];
           character = ((val % modulus) + modulus) % modulus;
 
           character = charMap.remapChar(character);
           cleartext.write(character);
         } else {
           // Ein überlesenes Zeichen sollte bei korrekter Chiffretext-Datei
           // eigentlich nicht auftreten können.
         }
       }
       cleartext.close();
       ciphertext.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Zugriff auf Klar- oder " + "Chiffretextdatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   /**
    * Verschlüsselt den durch den Reader <code>cleartext</code> gegebenen Klartext und schreibt den
    * Chiffretext mit dem Writer <code>ciphertext</code>.
    * 
    * @param cleartext
    *          Der Reader, der den Klartext liefert.
    * @param ciphertext
    *          Der Writer, der den Chiffretext schreiben soll.
    */
   public void encipher(BufferedReader cleartext, BufferedWriter ciphertext) {
     // An dieser Stelle könnte man alle Zeichen, die aus der Klartextdatei
     // gelesen werden, in Klein- bzw. Großbuchstaben umwandeln lassen:
     // charMap.setConvertToLowerCase();
     // charMap.setConvertToUpperCase();
 
     int c = 0;
 
     try {
       int character;
       boolean characterSkipped = false;
       while ((character = cleartext.read()) != -1) {
         character = charMap.mapChar(character);
         if (character != -1) {
 
           // character = (character + shift) % modulus;
           int index = c % keyword.length;
           // int keywordMapped = charMap.mapChar();
           character = (character + keyword[index]) % modulus;
 
           character = charMap.remapChar(character);
           ciphertext.write(character);
           c++;
         } else {
           // Das gelesene Zeichen ist im benutzten Alphabet nicht enthalten.
           characterSkipped = true;
           c++;
         }
       }
       if (characterSkipped) {
         System.out.println("Warnung: Mindestens ein Zeichen aus der " + "Klartextdatei ist im Alphabet nicht\nenthalten und wurde " + "überlesen.");
       }
       cleartext.close();
       ciphertext.close();
     } catch (IOException e) {
       System.err.println("Abbruch: Fehler beim Zugriff auf Klar- oder " + "Chiffretextdatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   /**
    * Erzeugt einen neuen Schlüssel.
    * 
    * @see #readKey readKey
    * @see #writeKey writeKey
    */
   public void makeKey() {
     BufferedReader standardInput = launcher.openStandardInput();
     boolean accepted = false;
 
     // Frage jeweils solange die Eingabe ab, bis diese akzeptiert werden kann.
     do {
       Logger("Geben Sie den Modulus ein: ");
       try {
         modulus = Integer.parseInt(standardInput.readLine());
 
         String defaultAlphabet = CharacterMapping.getDefaultAlphabet(modulus);
         if (!defaultAlphabet.equals("")) {
           Logger("Vordefiniertes Alphabet: '" + defaultAlphabet + "'\nDieses vordefinierte Alphabet kann durch Angabe einer " + "geeigneten Alphabet-Datei\nersetzt werden. Weitere " + "Informationen finden Sie im Javadoc der Klasse\n'Character" + "Mapping'.");
           accepted = true;
         } else {
           Logger("Warnung: Dem eingegebenen Modulus kann kein Default-" + "Alphabet zugeordnet werden.\nErstellen Sie zusätzlich zu " + "dieser Schlüssel- eine passende Alphabet-Datei.\nWeitere " + "Informationen finden Sie im Javadoc der Klasse 'Character" + "Mapping'.");
           accepted = true;
         }
       } catch (NumberFormatException e) {
         System.out.println("Fehler beim Parsen des Modulus. Bitte korrigieren" + " Sie Ihre Eingabe.");
       } catch (IOException e) {
         System.err.println("Abbruch: Fehler beim Lesen von der Standardeingabe.");
         e.printStackTrace();
         System.exit(1);
       }
     } while (!accepted);
     // define mapping for testing later
     CharacterMapping mapping = new CharacterMapping(modulus);
 
     accepted = false;
     do {
       try {
         Logger("Schlüssel eingeben:");
         // von string nach character array konvertieren
         String keywordString = standardInput.readLine().toString();
         char keywordChar[] = keywordString.toCharArray();
 
         // länge vom keyword array initialiseren
         keyword = new int[keywordChar.length];
 
         // konvertieren in int array
         int character, mappedCharacter;
         for (int i = 0; i < keywordChar.length; i++) {
           // konvertiere in int
           character = (int) keywordChar[i];
           // map to our alphabet
           mappedCharacter = mapping.mapChar(character);
 
           if (mappedCharacter >= 0 && mappedCharacter < modulus) {
             accepted = true;
           } else {
             System.out.println("Ein Buchstabe im Schlüssel passt nicht zum Alphabet, das durch den Modulus definiert wurde. " + "korrigieren Sie Ihre Eingabe.");
             System.exit(1);
           }
 
           // character in dem array speichern
           keyword[i] = mappedCharacter;
         }
 
       } catch (IOException e) {
         System.err.println("Abbruch: Fehler beim Lesen von der Standardeingabe.");
         e.printStackTrace();
         System.exit(1);
       }
     } while (!accepted);
   }
 
   /**
    * Liest den Schlüssel mit dem Reader <code>key</code>.
    * 
    * @param key
    *          Der Reader, der aus der Schlüsseldatei liest.
    * @see #makeKey makeKey
    * @see #writeKey writeKey
    */
   public void readKey(BufferedReader key) {
 
     try {
       StringTokenizer st = new StringTokenizer(key.readLine(), " ");
 
       modulus = Integer.parseInt(st.nextToken());
       Logger("Modulus: " + modulus);
 
       keyword = new int[st.countTokens()];
 
       int c = 0;
       while (st.hasMoreTokens()) {
         keyword[c++] = Integer.parseInt(st.nextToken());
       }
 
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
    *          Der Writer, der in die Schlüsseldatei schreibt.
    * @see #makeKey makeKey
    * @see #readKey readKey
    */
   public void writeKey(BufferedWriter key) {
     try {
 
       String keyString = "";
 
       for (int i = 0; i < keyword.length; i++) {
         keyString = keyString + " " + keyword[i];
       }
 
       key.write(modulus + keyString);
 
       key.newLine();
       key.close();
     } catch (IOException e) {
       System.out.println("Abbruch: Fehler beim Schreiben oder Schließen der " + "Schlüsseldatei.");
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   private static void Logger(String event) {
     System.out.println(event);
   }
 }
