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
 
 import java.io.*;
 
 import java.util.*;
 import de.tubs.cs.iti.jcrypt.chiffre.*;
 
 /**
  * Dummy-Klasse für die Vigenère-Chiffre.
  * 
  * @author Martin Klußmann
  * @version 1.0 - Tue Mar 30 15:53:38 CEST 2010
  */
 public class Vigenere extends Cipher {
 
 	private Vector<Integer> keys = new Vector<Integer>();
 
 	/**
 	 * Analysiert den durch den Reader <code>ciphertext</code> gegebenen
 	 * Chiffretext, bricht die Chiffre bzw. unterstützt das Brechen der Chiffre
 	 * (ggf. interaktiv) und schreibt den Klartext mit dem Writer
 	 * <code>cleartext</code>.
 	 * 
 	 * @param ciphertext
 	 *            Der Reader, der den Chiffretext liefert.
 	 * @param cleartext
 	 *            Der Writer, der den Klartext schreiben soll.
 	 */
 	public void breakCipher(BufferedReader inbuf, BufferedWriter cleartext) {
 
             try {
                 String ciphertext;
                 { // read ciphertext into a string
                     String tmp;
                     StringBuilder tmp2 = new StringBuilder();
                     while( (tmp = inbuf.readLine()) != null) {
                         tmp2.append(tmp);
                     }
                     ciphertext = tmp2.toString();
                 }
                 // expected period
                 int d = (int) Math.round(approxPeriodLength(ciphertext));
                 // keys
                 int[][] candidates = new int[d][modulus];
                 for (int i = 0; i < d; i++)
                     candidates[i] = breakCaesar(ciphertext.substring(i), d);
 
                 int[] current = new int[d];
                 int cursor = 0;
                 Arrays.fill(current, 0);
 
                 BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 
                 while(true) {
 
                     System.out.println();
                     for (int i = 0; i < d; i++) {
                         if(cursor == i)
                             System.out.print("[" + current[i] + "]");
                         else
                             System.out.print(current[i]);
                     }
                     System.out.println();
 
                     for (int i = 0; i < d; i++) {
                         if(cursor == i)
                             System.out.print("[" + candidates[i][current[i]] + "]");
                         else
                             System.out.print(candidates[i][current[i]]);
                     }
                     System.out.println();
 
                     for (int i = 0; i < d; i++) {
                         if(cursor == i)
                             System.out.print("[" + (char) charMap.remapChar(candidates[i][current[i]]) + "]");
                         else
                             System.out.print((char) charMap.remapChar(candidates[i][current[i]]));
                     }
                     System.out.println();
 
                     switch(in.readLine().charAt(0)) {
                         case 'q':
                             return;
                         case 'h':
                             System.out.println("q: quit\nh: help\no:output\nwasd: move cursor");
                             break;
                         case 'p':
                             for (int i = 0; i < d; i++) {
                                 System.out.print(i + ": ");
                                 for(int j = 0; j < modulus; j++)
                                     System.out.print((char) charMap.remapChar(candidates[i][j]));
                                 System.out.println();
                             }
                             break;
                         case 'o':
                             Vector<Integer> possibleKey = new Vector<Integer>();
                             for(int i = 0; i < d; i++) {
                                 possibleKey.add(candidates[i][current[i]]);
                             }
                             this.keys = possibleKey;
                             StringWriter sw = new StringWriter();
                             this.decipher(new BufferedReader(new StringReader(ciphertext)), new BufferedWriter(sw));
                             System.out.println(sw.getBuffer()); //Debug output
                             break;
 
                         case 'a':
                             cursor -= 1;
                             if(cursor < 0)
                                 cursor = 0;
                             break;
                         case 'd':
                             cursor += 1;
                             if(cursor >= d)
                                 cursor = d-1;
                             break;
                         case 'w':
                             current[cursor] += 1;
                             if(current[cursor] >= modulus)
                                 current[cursor] = modulus-1;
                             break;
                         case 's':
                             current[cursor] -= 1;
                             if(current[cursor] < 0)
                                 current[cursor] = 0;
                             break;
                     }
 
                 }
 
                 
             } catch(IOException e) {
                 e.printStackTrace();
                 return;
             }
 
         }
 
         public static void main(String[] args) throws IOException {
             // read from input file, or stdin
             BufferedReader input = args.length > 1 ? new BufferedReader(new FileReader(args[1])) : new BufferedReader(new InputStreamReader(System.in));
             // use output file, if any, or stdout
             BufferedWriter output = args.length > 2 ? new BufferedWriter(new FileWriter(args[2])) : new BufferedWriter(new PrintWriter(System.out));
 
             Vigenere v = new Vigenere();
             v.readKey(new BufferedReader(new StringReader("26 1 2 3\n")));
             v.charMap = new CharacterMapping(v.modulus);
 
             switch(args[0]) {
                 case "encipher":
                     v.encipher(input, output);
                     return;
 
                 case "decipher":
                     v.decipher(input, output);
                     return;
 
                 case "break":
                     v.breakCipher(input, output);
                     return;
 
                 default:
                     System.out.println("Usage: $0 encipher|decipher|break [infile [outfile]]");
             }
 
         }
 
         public int[] breakCaesar(String ciphertext, int period) {
             int shift = 0;
 
             Integer[] shifts = new Integer[modulus];
             { // find all character frequencies
                 final int[] freqs = new int[modulus];
                 for (int i = 0; i < ciphertext.length(); i += period) {
                     freqs[charMap.mapChar(ciphertext.charAt(i))] += 1;
                 }
 
                 double N = ciphertext.length();
                 // for (int i = 0; i < freqs.length; i++) {
                     // freqs[i] /= (N/period);
                 // }
 
                 for(int i = 0; i < modulus; i++)
                     shifts[i] = i;
                 Arrays.sort(shifts, new Comparator<Integer>() {
                     public int compare(Integer a, Integer b) {
                         return freqs[b] -freqs[a];
                     }
                 });
             }
 
             int[] ret = new int[modulus];
             { // correlate shifts by frequencies
                 ArrayList<NGram> nGrams = FrequencyTables.getNGramsAsList(1, charMap);
                 for(int i = 0; i < modulus; i++)
                    ret[i] = (shifts[i] +modulus -charMap.mapChar(Integer.parseInt(nGrams.get(i).getIntegers()))) % modulus;
             }
 
             return ret;
         }
 
         /**
          * Approximate period length of an input text
          */
         public double approxPeriodLength(String ciphertext) throws IOException {
 
             double randDist = randomDistribution();
             double IC = friedmann(ciphertext);
             int N = ciphertext.length();
 
             return ( (randDist - 1.0/modulus) * N ) / ( (N-1)*IC - (1.0/modulus*N) + randDist);
             
         }
 
         /**
          * Friedmann test
          *
          * http://en.wikipedia.org/wiki/Vigen%C3%A8re_cipher#Friedman_test
          */
         public double friedmann(String ciphertext) {
 
             int[] freqs = new int[modulus];
             double N = ciphertext.length();
             { // find all character frequencies
                 for (int i = 0; i < ciphertext.length(); i++) {
                     freqs[charMap.mapChar(ciphertext.charAt(i))] += 1;
                 }
             }
 
             int sum = 0;
             { // sum up frequencies
                 for(int i = 0; i < freqs.length; i++)
                     sum += freqs[i]*(freqs[i]-1);
             }
 
             return sum / (N*(N-1.0d));
         }
 
 	public double randomDistribution() {
 		double[] ft = FrequencyTables.getNGramsAsArray(1, charMap);
 		double sum = 0;
 
 		for (int i = 0; i < modulus; i++) {
 			sum += ft[i]/100 * ft[i]/100;
 		}
 		return sum;
 	}
 
 	/**
 	 * Entschlüsselt den durch den Reader <code>ciphertext</code> gegebenen
 	 * Chiffretext und schreibt den Klartext mit dem Writer
 	 * <code>cleartext</code>.
 	 * 
 	 * @param ciphertext
 	 *            Der Reader, der den Chiffretext liefert.
 	 * @param cleartext
 	 *            Der Writer, der den Klartext schreiben soll.
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
 					ord = (ord + 1) % keys.size();
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
 	 * Verschlüsselt den durch den Reader <code>cleartext</code> gegebenen
 	 * Klartext und schreibt den Chiffretext mit dem Writer
 	 * <code>ciphertext</code>.
 	 * 
 	 * @param cleartext
 	 *            Der Reader, der den Klartext liefert.
 	 * @param ciphertext
 	 *            Der Writer, der den Chiffretext schreiben soll.
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
 					ord = (ord + 1) % keys.size();
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
 	 * Erzeugt einen neuen Schlüssel.
 	 * 
 	 * @see #readKey readKey
 	 * @see #writeKey writeKey
 	 */
 	public void makeKey() {
 		BufferedReader standardInput = launcher.openStandardInput();
 		boolean accepted = false;
 		String msg = "Geeignete Werte für den Modulus werden in der Klasse "
 				+ "'CharacterMapping'\nfestgelegt. Probieren Sie ggf. einen Modulus "
 				+ "von 26, 27, 30 oder 31.\nDie Verschiebung muß größer oder gleich 0 "
 				+ "und kleiner als der gewählte\nModulus sein.";
 		System.out.println(msg);
 		// Frage jeweils solange die Eingabe ab, bis diese akzeptiert werden
 		// kann.
 		do {
 			System.out.print("Geben Sie den Modulus ein: ");
 			try {
 				modulus = Integer.parseInt(standardInput.readLine());
 				if (modulus < 1) {
 					System.out
 							.println("Ein Modulus < 1 wird nicht akzeptiert. Bitte "
 									+ "korrigieren Sie Ihre Eingabe.");
 				} else {
 					// Prüfe, ob zum eingegebenen Modulus ein Default-Alphabet
 					// existiert.
 					String defaultAlphabet = CharacterMapping
 							.getDefaultAlphabet(modulus);
 					if (!defaultAlphabet.equals("")) {
 						msg = "Vordefiniertes Alphabet: '"
 								+ defaultAlphabet
 								+ "'\nDieses vordefinierte Alphabet kann durch Angabe einer "
 								+ "geeigneten Alphabet-Datei\nersetzt werden. Weitere "
 								+ "Informationen finden Sie im Javadoc der Klasse\n'Character"
 								+ "Mapping'.";
 						System.out.println(msg);
 						accepted = true;
 					} else {
 						msg = "Warnung: Dem eingegebenen Modulus kann kein Default-"
 								+ "Alphabet zugeordnet werden.\nErstellen Sie zusätzlich zu "
 								+ "dieser Schlüssel- eine passende Alphabet-Datei.\nWeitere "
 								+ "Informationen finden Sie im Javadoc der Klasse 'Character"
 								+ "Mapping'.";
 						System.out.println(msg);
 						accepted = true;
 					}
 				}
 			} catch (NumberFormatException e) {
 				System.out
 						.println("Fehler beim Parsen des Modulus. Bitte korrigieren"
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
 				System.out
 						.print("Geben Sie die durch Leerzeichen getrennten Keys ein: ");
 				StringTokenizer stKeys = new StringTokenizer(
 						standardInput.readLine(), " ");
 				while (stKeys.hasMoreElements()) {
 					int key = Integer.parseInt(stKeys.nextToken());
 					if (key >= 0 && key < modulus) {
 						this.keys.add(key);
 					} else {
 						System.out
 								.println("Error: "
 										+ key
 										+ "is an invald key (key must be >= 0 and < modulus)");
 						accepted = false;
 					}
 				}
 				if (keys.size() == 0) {
 					accepted = false;
 				}
 			} catch (NumberFormatException e) {
 				System.out
 						.println("Fehler beim Parsen der Verschiebung. Bitte "
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
 	 * Liest den Schlüssel mit dem Reader <code>key</code>.
 	 * 
 	 * @param key
 	 *            Der Reader, der aus der Schlüsseldatei liest.
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
 			System.err
 					.println("Abbruch: Fehler beim Lesen oder SchlieÃen der "
 							+ "Schlüsseldatei.");
 			e.printStackTrace();
 			System.exit(1);
 		} catch (NumberFormatException e) {
 			System.err
 					.println("Abbruch: Fehler beim Parsen eines Wertes aus der "
 							+ "Schlüsseldatei.");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * Schreibt den Schlüssel mit dem Writer <code>key</code>.
 	 * 
 	 * @param key
 	 *            Der Writer, der in die Schlüsseldatei schreibt.
 	 * @see #makeKey makeKey
 	 * @see #readKey readKey
 	 */
 	public void writeKey(BufferedWriter key) {
 		try {
 			key.write(String.valueOf(modulus));
 			for (int k : keys) {
 				key.write(" " + k);
 			}
 			key.newLine();
 			key.close();
 		} catch (IOException e) {
 			System.out
 					.println("Abbruch: Fehler beim Schreiben oder Schließen der "
 							+ "Schlüsseldatei.");
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 }
