 /*
  * jCrypt - Programmierumgebung für das Kryptologie-Praktikum
  * Studienarbeit am Institut für Theoretische Informatik der
  * Technischen Universität Braunschweig
  * 
  * Datei:        RunningKey.java
  * Beschreibung: Dummy-Implementierung der Chiffre mit laufendem Schlüssel
  * Erstellt:     30. März 2010
  * Autor:        Martin Klußmann
  */
 
 package task2;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
 import java.util.StringTokenizer;
 import java.util.TreeMap;
 
 import javax.management.openmbean.KeyAlreadyExistsException;
 
 import com.sun.org.apache.bcel.internal.generic.NEW;
 
 import de.tubs.cs.iti.jcrypt.chiffre.CharacterMapping;
 import de.tubs.cs.iti.jcrypt.chiffre.Cipher;
 import de.tubs.cs.iti.jcrypt.chiffre.FrequencyTables;
 import de.tubs.cs.iti.jcrypt.chiffre.NGram;
 
 public class RunningKey extends Cipher {
 
   File keyFile;
   int position = 42;
   BufferedReader keyBuffer;
 
   HashMap<String, Double> unigramHashMap = new HashMap<String, Double>();
   HashMap<String, Double> digramHashMap = new HashMap<String, Double>();
   HashMap<String, Double> trigramHashMap = new HashMap<String, Double>();
 
   public void makeKey() {
 
     BufferedReader standardInput = launcher.openStandardInput();
     boolean accepted = false;
 
     do {
       Logger("Geben Sie den Modulus ein: ");
       try {
         modulus = Integer.parseInt(standardInput.readLine());
         if (modulus < 1) {
           Logger("Ein Modulus < 1 wird nicht akzeptiert. Bitte " + "korrigieren Sie Ihre Eingabe.");
         } else {
           String defaultAlphabet = CharacterMapping.getDefaultAlphabet(modulus);
           if (!defaultAlphabet.equals("")) {
             Logger("Vordefiniertes Alphabet: '" + defaultAlphabet + "'\nDieses vordefinierte Alphabet kann durch Angabe einer " + "geeigneten Alphabet-Datei\nersetzt werden. Weitere " + "Informationen finden Sie im Javadoc der Klasse\n'Character" + "Mapping'.");
             accepted = true;
           } else {
             Logger("Warnung: Dem eingegebenen Modulus kann kein Default-" + "Alphabet zugeordnet werden.\nErstellen Sie zusätzlich zu " + "dieser Schlüssel- eine passende Alphabet-Datei.\nWeitere " + "Informationen finden Sie im Javadoc der Klasse 'Character" + "Mapping'.");
             accepted = true;
           }
         }
       } catch (NumberFormatException e) {
         Logger("Fehler beim Parsen des Modulus. Bitte korrigieren" + " Sie Ihre Eingabe.");
       } catch (IOException e) {
         Logger("Abbruch: Fehler beim Lesen von der Standardeingabe.");
         e.printStackTrace();
         System.exit(1);
       }
     } while (!accepted);
     accepted = false;
 
     Logger("Folgende Dateien stehen zur Auswahl:");
     File directory = new File("../text/");
     String[] list = directory.list();
     for (int i = 0; i < list.length; i++) {
       Logger("[" + i + "] " + list[i]);
     }
     int choice = 0;
     try {
       Logger("Welche Datei soll zum Verschluesseln benutzt werden: ");
       choice = Integer.parseInt(standardInput.readLine());
     } catch (NumberFormatException e) {
       e.printStackTrace();
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     keyFile = new File("../text/" + list[choice]);
 
     FileReader fr = null;
     try {
       fr = new FileReader(keyFile);
     } catch (FileNotFoundException e) {
       e.printStackTrace();
     }
     keyBuffer = new BufferedReader(fr);
 
     Logger("Using File: " + keyFile.getName() + " with Modulus " + modulus);
 
   }
 
   public void writeKey(BufferedWriter key) {
     try {
       key.write(modulus + " " + keyFile.getName());
       key.newLine();
 
       Logger("Writing Information: ");
       Logger("+--Modulus: " + modulus);
       Logger("+--File: " + keyFile.getName());
 
       key.close();
     } catch (IOException e) {
       System.out.println("Abbruch: Fehler beim Schreiben oder Schließen der " + "Schlüsseldatei.");
       e.printStackTrace();
       System.exit(1);
     }
 
   }
 
   public void readKey(BufferedReader key) {
     try {
       StringTokenizer st = new StringTokenizer(key.readLine(), " ");
 
       modulus = Integer.parseInt(st.nextToken());
       keyFile = new File("../text/" + st.nextToken());
 
       FileReader fr = null;
       try {
         fr = new FileReader(keyFile);
       } catch (FileNotFoundException e) {
         e.printStackTrace();
       }
       keyBuffer = new BufferedReader(fr);
 
       Logger("Reading Information: ");
       Logger("+--Modulus: " + modulus);
       Logger("+--File: " + keyFile.getName());
 
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
 
   public void breakCipher(BufferedReader ciphertext, BufferedWriter cleartext) {
 
     generateNGramHashMaps();
 
     String[] cipherArray = getTextAsStringArray(ciphertext, 4);
 
     String cipherPart = "";
     String keyPart = "";
     String clearPart = "";
 
     /***** Interaction START */
     BufferedReader cin = launcher.openStandardInput();
 
     Logger("Welche Stelle des Ciphertextes soll betrachtet werden?");
     Logger("Waehle: 0-" + cipherArray.length);
 
     int textPosition = 0;
     try {
       textPosition = Integer.parseInt(cin.readLine());
       cipherPart = cipherArray[textPosition];
     } catch (Exception e) {
       Logger("Falsche Eingabe, 0. Stelle wird ausgewaehlt!");
       cipherPart = cipherArray[textPosition];
     }
 
     Logger("Wie soll die Gewichtung aussehen fuer Uni/Di/Tri Grams?");
 
     int uni = 1, di = 1, tri = 1;
     try {
       Logger("Gewichtung Unigram: 1-1000: ");
       uni = Integer.parseInt(cin.readLine());
 
       Logger("Gewichtung Digram: 1-1000: ");
       di = Integer.parseInt(cin.readLine());
 
       Logger("Gewichtung Trigram: 1-1000: ");
       tri = Integer.parseInt(cin.readLine());
     } catch (Exception e) {
       Logger("Falsche Eingabe, 1 wird fuer die restlichen ausgewaehlt");
     }
     /***** Interaction END */
 
     Logger("Berechne moegliche Kombination!");
     LinkedList<int[]> combinationsList = getCombination(cipherPart);
 
     Logger("Anzahl von sinnvollen Kombinationen: " + combinationsList.size());
 
     Logger("Berechne beste Kombination! Kann dauern...");
     TreeMap<Double, int[]> calculationMap = new TreeMap<Double, int[]>();
 
     Iterator<int[]> listIterator = combinationsList.iterator();
     while (listIterator.hasNext()) {
       int[] currentCombination = listIterator.next();
       double calculation = bewertung(currentCombination, 1, 1, 1);
       calculationMap.put(calculation, currentCombination);
     }
 
     Logger("Folgende Mappings erziehlten das beste Ergebnis fuer" + cipherPart);
     Iterator<Double> mapIterator = calculationMap.keySet().iterator();
     HashMap<Integer, String> userInputMap = new HashMap<Integer, String>();

     for (int i = 0; i < 10 & mapIterator.hasNext(); i++) {

      double calculationResult = mapIterator.next();
      int[] currentArray = calculationMap.get(calculationResult);
 
       clearPart = "" + ((char) charMap.remapChar(currentArray[0])) + ((char) charMap.remapChar(currentArray[1])) + ((char) charMap.remapChar(currentArray[2])) + ((char) charMap.remapChar(currentArray[3]));
       keyPart = "" + ((char) charMap.remapChar(currentArray[4])) + ((char) charMap.remapChar(currentArray[5])) + ((char) charMap.remapChar(currentArray[6])) + ((char) charMap.remapChar(currentArray[7]));

      Logger("Calculation Result: " + calculationResult + " CIPHER: " + cipherPart + " => CLEAR: " + clearPart + " KEY: " + keyPart);
       userInputMap.put(i, clearPart);
     }
 
     Logger("Welches Mapping soll ausgewaehlt werden?");
     int choice = 0;
     try {
       choice = Integer.parseInt(cin.readLine());
     } catch (Exception e) {
       Logger("Falsche Eingabe, 0. Stelle wird ausgewaehlt!");
     }
 
     Logger(cipherPart + " wird in " + clearPart + " gemapped!");
 
     // Suppress framework exception
     System.exit(0);
   }
 
   private String getTextAsString(BufferedReader br) {
     String s = "", story = "";
     int character = 0;
     try {
       while ((character = br.read()) != -1) {
         char c = (char) character;
         story = story + c;
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     Logger("Read the whole story! Phew~");
 
     return story;
   }
 
   private String[] getTextAsStringArray(BufferedReader br, int tokenSize) {
 
     String text = getTextAsString(br);
 
     String[] story = new String[(text.length() / tokenSize) + 1];
 
     for (int i = 0; i < story.length; i++) {
       if (text.length() > tokenSize) {
         String subString = text.substring(0, tokenSize);
         text = text.substring(tokenSize);
         story[i] = subString;
       } else {
         story[i] = text;
       }
     }
 
     return story;
   }
 
   /**
    * 
    * @param cipherToken
    *          Token of the length 4 from the cipher text.
    * @return List with suggestions. Format of int[]: int[0-3] = cleartext suggestions int[4-6] =
    *         keytext suggestions
    */
   private LinkedList<int[]> getCombination(String cipherToken) {
 
     int counter = 0;
 
     char[] cipherTokenArray = cipherToken.toCharArray();
 
     int firstCharMapped = charMap.mapChar(cipherTokenArray[0]);
     int secondCharMapped = charMap.mapChar(cipherTokenArray[1]);
     int thirdCharMapped = charMap.mapChar(cipherTokenArray[2]);
     int fourthCharMapped = charMap.mapChar(cipherTokenArray[3]);
 
     HashMap<Integer, LinkedList<int[]>> map = cipherMapping();
 
     LinkedList<int[]> la = map.get(firstCharMapped);
     LinkedList<int[]> lb = map.get(secondCharMapped);
     LinkedList<int[]> lc = map.get(thirdCharMapped);
     LinkedList<int[]> ld = map.get(fourthCharMapped);
 
     LinkedList<int[]> list = new LinkedList<int[]>();
 
     for (int a = 0; a < la.size(); a++) {
       int[] clearCharsMapped = new int[4];
       int[] keyCharsMapped = new int[4];
 
       clearCharsMapped[0] = la.get(a)[0];
       keyCharsMapped[0] = la.get(a)[1];
 
       for (int b = 0; b < lb.size(); b++) {
         clearCharsMapped[1] = lb.get(b)[0];
         keyCharsMapped[1] = lb.get(b)[1];
 
         for (int c = 0; c < lc.size(); c++) {
           clearCharsMapped[2] = lc.get(c)[0];
           keyCharsMapped[2] = lc.get(c)[1];
 
           for (int d = 0; d < ld.size(); d++) {
             clearCharsMapped[3] = ld.get(d)[0];
             keyCharsMapped[3] = ld.get(d)[1];
 
             int[] mixed = { clearCharsMapped[0], clearCharsMapped[1], clearCharsMapped[2], clearCharsMapped[3], keyCharsMapped[0], keyCharsMapped[1], keyCharsMapped[2], keyCharsMapped[3] };
             counter++;
 
             if (isCorrectCombination(mixed)) {
               list.add(mixed);
             } else {
               // Nothing to do, yay!
             }
 
           }
         }
       }
     }
 
     Logger("Maximale Anzahl von Kombinationen: " + counter);
 
     return list;
   }
 
   private boolean isCorrectCombination(int[] combination) {
 
     char clearChar0 = (char) charMap.remapChar(combination[0]);
     char clearChar1 = (char) charMap.remapChar(combination[1]);
     char clearChar2 = (char) charMap.remapChar(combination[2]);
     char clearChar3 = (char) charMap.remapChar(combination[3]);
     char keyChar0 = (char) charMap.remapChar(combination[4]);
     char keyChar1 = (char) charMap.remapChar(combination[5]);
     char keyChar2 = (char) charMap.remapChar(combination[6]);
     char keyChar3 = (char) charMap.remapChar(combination[7]);
 
     char[] clearArray = { clearChar0, clearChar1, clearChar2, clearChar3 };
     char[] keyArray = { keyChar0, keyChar1, keyChar2, keyChar3 };
 
     String clearString = String.valueOf(clearArray);
     String keyString = String.valueOf(keyArray);
 
     boolean digram = false;
     boolean trigram = false;
 
     for (int i = 0; i < 3; i++) {
       String digramString = "";
       char c1, c2;
 
       c1 = (char) clearArray[i];
       c2 = (char) clearArray[i + 1];
       char[] charArray = { c1, c2 };
       digramString = String.valueOf(charArray);
       if (digramHashMap.get(digramString) != null) {
         digram |= true;
       } else {
         digram |= false;
       }
 
       c1 = (char) keyArray[i];
       c2 = (char) keyArray[i + 1];
       char[] charArray2 = { c1, c2 };
       digramString = String.valueOf(charArray2);
       if (digramHashMap.get(digramString) != null) {
         digram |= true;
       } else {
         digram |= false;
       }
 
     }
     for (int i = 0; i < 2; i++) {
       String trigramString = "";
       char c1, c2, c3;
 
       c1 = (char) clearArray[i];
       c2 = (char) clearArray[i + 1];
       c3 = (char) clearArray[i + 2];
       char[] charArray = { c1, c2, c3 };
       trigramString = String.valueOf(charArray);
       if (trigramHashMap.get(trigramString) != null) {
         trigram |= true;
       } else {
         trigram |= false;
       }
 
       c1 = (char) keyArray[i];
       c2 = (char) keyArray[i + 1];
       c3 = (char) keyArray[i + 2];
       char[] charArray2 = { c1, c2, c3 };
       trigramString = String.valueOf(charArray2);
       if (trigramHashMap.get(trigramString) != null) {
         trigram |= true;
       } else {
         trigram |= false;
       }
 
     }
 
     // Logger("isCorrectCombination: " + clearString + " " + keyString + " " + (digram || trigram));
 
     if (digram || trigram) {
       return true;
     } else {
       return false;
     }
 
   }
 
   private HashMap<Integer, LinkedList<int[]>> cipherMapping() {
 
     HashMap<Integer, LinkedList<int[]>> map = new HashMap<Integer, LinkedList<int[]>>();
 
     for (int i = 0; i < modulus; i++) {
       for (int j = 0; j < modulus; j++) {
 
         int result = (i + j) % modulus;
 
         int[] mapping = { i, j };
 
         if (map.containsKey(result)) {
           LinkedList<int[]> list = map.get(result);
           list.add(mapping);
         } else {
           LinkedList<int[]> list = new LinkedList<int[]>();
           list.add(mapping);
           map.put(result, list);
         }
       }
     }
 
     return map;
 
   }
 
   private void generateNGramHashMaps() {
     ArrayList<NGram> unigram = FrequencyTables.getNGramsAsList(1, charMap);
     ArrayList<NGram> digram = FrequencyTables.getNGramsAsList(2, charMap);
     ArrayList<NGram> trigram = FrequencyTables.getNGramsAsList(3, charMap);
 
     unigramHashMap = nGramToHashMap(unigram);
     digramHashMap = nGramToHashMap(digram);
     trigramHashMap = nGramToHashMap(trigram);
   }
 
   private double bewertung(int[] combination, double g1, double g2, double g3) {
 
     int[] clearArray = { combination[0], combination[1], combination[2], combination[3] };
     int[] keyArray = { combination[4], combination[5], combination[6], combination[7] };
 
     double result = 0;
     double k1 = 0, k2 = 0, k3 = 0;
     double s1 = 0, s2 = 0, s3 = 0;
 
     for (int i = 0; i < 4; i++) {
       char s1Char = (char) charMap.remapChar(clearArray[i]);
       char k1Char = (char) charMap.remapChar(keyArray[i]);
 
       double d1 = unigramHashMap.get(s1Char + "");
       double d2 = unigramHashMap.get(k1Char + "");
 
       s1 += unigramHashMap.get(s1Char + "");
       k1 += unigramHashMap.get(k1Char + "");
     }
     for (int i = 0; i < 3; i++) {
 
       char s2Char1 = (char) charMap.remapChar(clearArray[i]);
       char s2Char2 = (char) charMap.remapChar(clearArray[i + 1]);
       char k2Char1 = (char) charMap.remapChar(keyArray[i]);
       char k2Char2 = (char) charMap.remapChar(keyArray[i + 1]);
 
       // try {
       // s2 += digramHashMap.get(s2Char1 + s2Char2 + "");
       // } else {
       // s2 += 0;
       // }
 
       if ((digramHashMap.get(s2Char1 + s2Char2 + "")) != null) {
         s2 += digramHashMap.get(s2Char1 + s2Char2 + "");
       } else {
         s2 += 0;
       }
 
       if ((digramHashMap.get(k2Char1 + k2Char2 + "")) != null) {
         k2 += digramHashMap.get(k2Char1 + k2Char2 + "");
       } else {
         k2 += 0;
       }
 
     }
     for (int i = 0; i < 2; i++) {
 
       char s3Char1 = (char) charMap.remapChar(clearArray[i]);
       char s3Char2 = (char) charMap.remapChar(clearArray[i + 1]);
       char s3Char3 = (char) charMap.remapChar(clearArray[i + 2]);
       char k3Char1 = (char) charMap.remapChar(keyArray[i]);
       char k3Char2 = (char) charMap.remapChar(keyArray[i + 1]);
       char k3Char3 = (char) charMap.remapChar(keyArray[i + 2]);
 
       if ((trigramHashMap.get(s3Char1 + s3Char2 + s3Char3 + "")) != null) {
         s3 += trigramHashMap.get(s3Char1 + s3Char2 + s3Char3 + "");
       } else {
         s3 += 0;
       }
 
       if ((trigramHashMap.get(k3Char1 + k3Char2 + k3Char3 + "")) != null) {
         k3 += trigramHashMap.get(k3Char1 + k3Char2 + k3Char3 + "");
       } else {
         k3 += 0;
       }
 
     }
 
     result = (g1 * k1 + g2 * k2 + g3 * k3) * (g1 * s1 + g2 * s2 + g3 * s3);
 
     // Logger("Result: " + result);
     return result;
   }
 
   private HashMap<String, Double> nGramToHashMap(ArrayList<NGram> nGram) {
     HashMap<String, Double> nGramHashMap = new HashMap<String, Double>();
 
     Iterator<NGram> it = nGram.iterator();
 
     while (it.hasNext()) {
       NGram n = it.next();
       nGramHashMap.put(n.getCharacters(), n.getFrequency());
     }
 
     return nGramHashMap;
   }
 
   private static void Logger(String event) {
     System.out.println("    RunningCipher$ " + event);
   }
 
   public void encipher(BufferedReader cleartext, BufferedWriter ciphertext) {
     int cipherChar = -1, cipherCharMapped = -1, clearChar = -1, clearCharMapped = -1, keyChar = -1, keyCharMapped = -1;
 
     try {
       while (((clearChar = cleartext.read()) != -1) && ((keyChar = keyBuffer.read()) != -1)) {
         clearCharMapped = charMap.mapChar(clearChar);
         keyCharMapped = charMap.mapChar(keyChar);
 
         boolean endOfFile = false;
 
         while (clearCharMapped == -1) {
           if ((clearChar = cleartext.read()) == -1) {
             Logger("End Of File: Cleartext");
             endOfFile = true;
             break;
           }
           clearCharMapped = charMap.mapChar(clearChar);
         }
 
         if (endOfFile) {
           break;
         }
 
         while (keyCharMapped == -1) {
           if ((keyChar = keyBuffer.read()) == -1) {
             Logger("End Of File: Key");
             endOfFile = true;
             break;
           }
           keyCharMapped = charMap.mapChar(keyChar);
         }
 
         if (endOfFile) {
           break;
         }
 
         cipherCharMapped = (clearCharMapped + keyCharMapped) % modulus;
         cipherChar = charMap.remapChar(cipherCharMapped);
 
         ciphertext.write(cipherChar);
 
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     // Close files
     try {
       cleartext.close();
       ciphertext.close();
       keyBuffer.close();
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   public void decipher(BufferedReader ciphertext, BufferedWriter cleartext) {
     int cipherChar = -1, cipherCharMapped = -1, clearChar = -1, clearCharMapped = -1, keyChar = -1, keyCharMapped = -1;
 
     try {
       while (((cipherChar = ciphertext.read()) != -1) && ((keyChar = keyBuffer.read()) != -1)) {
         cipherCharMapped = charMap.mapChar(cipherChar);
         keyCharMapped = charMap.mapChar(keyChar);
 
         boolean endOfFile = false;
 
         while (cipherCharMapped == -1) {
           if ((cipherChar = ciphertext.read()) == -1) {
             Logger("End of File: Ciphertext");
             endOfFile = true;
             break;
           }
           cipherCharMapped = charMap.mapChar(cipherChar);
         }
 
         if (endOfFile) {
           break;
         }
 
         while (keyCharMapped == -1) {
           if ((keyChar = keyBuffer.read()) == -1) {
             Logger("End of File: Key");
             endOfFile = true;
             break;
           }
           keyCharMapped = charMap.mapChar(keyChar);
         }
 
         if (endOfFile) {
           break;
         }
 
         clearCharMapped = (cipherCharMapped - keyCharMapped + modulus) % modulus;
         clearChar = charMap.remapChar(clearCharMapped);
         cleartext.write(clearChar);
 
       }
     } catch (IOException e1) {
       e1.printStackTrace();
     }
 
     // Close files
     try {
       cleartext.close();
       ciphertext.close();
       keyBuffer.close();
     } catch (IOException e) {
       e.printStackTrace();
     }
 
   }
 
 }
