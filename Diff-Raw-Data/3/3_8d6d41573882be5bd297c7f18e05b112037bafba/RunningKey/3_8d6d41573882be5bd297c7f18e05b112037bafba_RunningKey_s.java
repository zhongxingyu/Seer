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
 
 import java.io.*;
 import java.util.*;
 
 import de.tubs.cs.iti.jcrypt.chiffre.*;
 
 /**
  * Dummy-Klasse für die Chiffre mit laufendem Schlüssel.
  * 
  * @author Martin Klußmann
  * @version 1.0 - Tue Mar 30 16:23:47 CEST 2010
  */
 public class RunningKey extends Cipher {
 
 	String keystreamFilename = "";
 
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
 	public void breakCipher(BufferedReader inbuf, BufferedWriter outbuf) {
 		try {
             BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 
 			String ciphertext;
 			{ // read ciphertext into a string
 				String tmp;
 				StringBuilder tmp2 = new StringBuilder();
 				while ((tmp = inbuf.readLine()) != null) {
 					tmp2.append(tmp);
 				}
 				ciphertext = tmp2.toString();
 			}
 
 			double[] weights = new double[3];
 			int start = 0;
 			int keyIndex = 0;
 			int len = 4;
 
			System.out
					.println("Please enter your space sperated weights g1 g2 g3:");
 			StringTokenizer stWeights = new StringTokenizer(
 					in.readLine(), " ");
 			for (int i = 0; i < 3; i++) {
 				if (stWeights.hasMoreTokens() == false) {
 					System.out.println("Error: You have to enter excatly 3 weights!");
 				}
 				weights[i] = Double.parseDouble(stWeights.nextToken());
 			}
 			System.out.println("start position:");
 			try {
 				start = Integer.parseInt(in.readLine());
 			} catch (NumberFormatException e1) {
 				start = 0;
 				e1.printStackTrace();
 			}
 
 			while (true) {
 				String cipherSubstring = ciphertext.substring(start, start + len); 
 				System.out.println(cipherSubstring);
 				
 				
 				switch (in.readLine().charAt(0)) {
 				case 'q':
 					return;
 				case 'p':
 					System.out.println("start position:");
 					try {
 						start = Integer.parseInt(in.readLine());
 						if (start >= ciphertext.length() | start < 0) {
 							start = 0;
 							System.out.println("Error: position has to be smaller than text length and bigger than 0");
 						}
 					} catch (NumberFormatException e1) {
 						start = 0;
 						e1.printStackTrace();
 					}
 					break;
 				case 'l':
 					System.out.println("new length:");
 					try {
 						len = Integer.parseInt(in.readLine());
 						if (len <= 0) {
 							len = 4;
 							System.out.println("Error: length has to be bigger than 0 (and even bigger to make sense as well)");
 						}
 					} catch (NumberFormatException e1) {
 						len = 4;
 						e1.printStackTrace();
 					}
 				case 'k':
 					System.out.println("new key (0..4), where 0 is the key with the highest probability:");
 					try {
 						keyIndex = Integer.parseInt(in.readLine());
 						if (keyIndex < 0 | keyIndex > 4) {
 							keyIndex = 0;
 							System.out.println("Error: key has to between 0 and 4");
 						}
 					} catch (NumberFormatException e1) {
 						len = 4;
 						e1.printStackTrace();
 					}
 				}
 			}
 		} catch (IOException e) {
 			System.out.println("Could not parse your freaking input.");
 			e.printStackTrace();
 		}
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
 			BufferedReader keystream = new BufferedReader(new FileReader(
 					this.keystreamFilename));
 			
 			int charCipher;
 			while ((charCipher = ciphertext.read()) != -1) {
 				charCipher = charMap.mapChar(charCipher);
 				if (charCipher != -1) { //skip invald characters
 					int charKey = charMap.mapChar(keystream.read());
 					while (charKey == -1)
 						charKey = charMap.mapChar(keystream.read());
 					int charClear = (modulus + charCipher - charKey) % modulus;
 					charClear = charMap.remapChar(charClear);
 					cleartext.write(charClear);
 				}
 			}
 			cleartext.close();
 			ciphertext.close();
 			keystream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Error: keystream file not found :-(");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Error: Could not read or write file :-(");
 			e.printStackTrace();
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
 			BufferedReader keystream = new BufferedReader(new FileReader(
 					this.keystreamFilename));
 			
 			int charClear;
 			while ((charClear = cleartext.read()) != -1) {
 				charClear = charMap.mapChar(charClear);
 				if (charClear != -1) { //skip invald characters
 					int charKey = charMap.mapChar(keystream.read());
 					while (charKey == -1)
 						charKey = charMap.mapChar(keystream.read());
 					int charCipher = (modulus + charClear + charKey) % modulus;
 					charCipher = charMap.remapChar(charCipher);
 					ciphertext.write(charCipher);
 				}
 			}
 			cleartext.close();
 			ciphertext.close();
 			keystream.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("Error: keystream file not found :-(");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Error: Could not read or write file :-(");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Erzeugt einen neuen Schlüssel.
 	 * 
 	 * @see #readKey readKey
 	 * @see #writeKey writeKey
 	 */
 	public void makeKey() {
 
 		System.out.println("Dummy für die Schlüsselerzeugung.");
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
 
 	}
 
 	public static void main(String[] args) throws IOException {
 		// read from input file, or stdin
 		BufferedReader input = args.length > 1 ? new BufferedReader(
 				new FileReader(args[1])) : new BufferedReader(
 				new InputStreamReader(System.in));
 		// use output file, if any, or stdout
 		BufferedWriter output = args.length > 2 ? new BufferedWriter(
 				new FileWriter(args[2])) : new BufferedWriter(new PrintWriter(
 				System.out));
 
 		RunningKey v = new RunningKey(26);
 		v.keystreamFilename = "keystream.txt";
 		v.charMap = new CharacterMapping(v.modulus);
 
 		if (args[0].equals("encipher")) {
 			v.encipher(input, output);
 			return;
 		}
 		else if (args[0].equals("decipher")) {
 			v.decipher(input, output);
 			return;
 		}
 		else if (args[0].equals("break")) {
 			v.breakCipher(input, output);
 			return;
 		}
 		else {
 				System.out.println("Usage: $0 encipher|decipher|break [infile [outfile]]");
 		}
 
 	}
 
         // took me a frickin' hour to write this one!
         static private class Combinator<T> implements Iterator<List<T>> {
             T[][] source;
             int step, max;
             boolean finished = false;
 
             Combinator(T[][] source) {
                 this.source = source;
                 this.max = source[0].length;
                 for(int i = 1; i < source.length; i++)
                     this.max *= source[i].length;
             }
             public boolean hasNext() {
                 return step < max;
             }
             public List<T> next() {
                 List<T> tmp = new ArrayList<T>(source.length);
                 int step = this.step;
                 for(int i = 0; i < source.length; i++) {
                     tmp.add(source[i][step % source[i].length]);
                     step = step / source[i].length;
                 }
                 this.step += 1;
                 return tmp;
             }
             public void remove() {
                 step += 1;
             }
         }
 
 
         public Iterator<List<AbstractMap.SimpleEntry<Integer, Integer>>> combinations(String cipher, int offset, int length) {
             AbstractMap.SimpleEntry<Integer, Integer>[][] possible_pieces = (AbstractMap.SimpleEntry<Integer, Integer>[][]) new AbstractMap.SimpleEntry[length][];
             for(int i = 0; i < length; i++) {
                 AbstractMap.SimpleEntry<Integer, Integer>[] prototype = (AbstractMap.SimpleEntry<Integer, Integer>[]) new AbstractMap.SimpleEntry[0];
                 possible_pieces[i] = sumpieces[charMap.mapChar(cipher.charAt(i+offset))].toArray(prototype);
             }
 
             return new Combinator<AbstractMap.SimpleEntry<Integer, Integer>>(possible_pieces);
         }
 
 //        public static void main(String[] args) {
 //            RunningKey k = new RunningKey(26);
 //            k.combinations("abcd", 0, 4);
 //        }
 
         public double score(String plain, String cipher, int offset, int length, double[] weights) {
             return singlescore(plain, offset, length, weights) * singlescore(cipher, offset, length, weights);
         }
 
 
         public double singlescore(String snippet, int offset, int length, double[] weights) {
 
             double score = 0.0d;
 
             for(int d = 0; d < nGrams.length; d++) {
                 double sub = 0.0d;
                 for(int i = 0; i < length-(d+1); i++) {
                     Double k = nGrams[d].get(snippet.substring(offset+i, offset+i+d+1));
                     if(k != null)
                         sub += k;
                 }
 
                 score += sub * weights[d];
             }
 
             return score;
         }
 
         HashSet<AbstractMap.SimpleEntry<Integer, Integer>>[] sumpieces;
         HashMap<String, Double>[] nGrams;
 
         RunningKey(int modulus) {
         	this.modulus = modulus;
             charMap = new CharacterMapping(modulus);
             generateSumPieces();
 
             nGrams = (HashMap<String, Double>[]) new HashMap[3];
             for(int d = 0; d < 3; d++) {
                 Iterator<NGram> it = FrequencyTables.getNGramsAsList(d+1, charMap).iterator();
                 nGrams[d] = new HashMap<String, Double>();
                 while (it.hasNext()) {
                     NGram n = it.next();
                     nGrams[d].put(n.getCharacters(), n.getFrequency());
                 }
             }
         }
 
 
         private void generateSumPieces() {
             sumpieces = (HashSet<AbstractMap.SimpleEntry<Integer, Integer>>[]) new HashSet[modulus];
             for(int i = 0; i < modulus; i++)
                 sumpieces[i] = new HashSet<AbstractMap.SimpleEntry<Integer, Integer>>();
             for(int i = 0; i < modulus; i++) {
                 for(int j = 0; j < modulus; j++) {
                     sumpieces[(i+j) % modulus].add(new AbstractMap.SimpleEntry<Integer, Integer>(i, j));
                 }
             }
         }
 
         public static void main_testpieces(String[] args) {
             RunningKey k = new RunningKey(26);
 
             Iterator<AbstractMap.SimpleEntry<Integer, Integer>> it = k.sumpieces[17].iterator();
             AbstractMap.SimpleEntry<Integer, Integer> x;
             while(it.hasNext()) {
                 x = it.next();
                 System.out.println(x);
             }
 
         }
 
         public static void main_combinator(String[] args) {
             Integer[][] x = new Integer[][] { { 1, 2, 3 }, {4, 5}, {5, 6} };
             Combinator<Integer> it = new Combinator(x);
             while(it.hasNext())
                 System.out.println(it.next());
         }
 
 }
