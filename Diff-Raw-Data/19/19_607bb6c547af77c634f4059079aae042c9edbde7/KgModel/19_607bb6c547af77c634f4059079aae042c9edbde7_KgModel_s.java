 /** @author:
  * Eric Dobritius 	 20110009
  * Gordan Just		 20091313 
  * Sebastian Kindt 	 20120023
  * 
  * @version: 1.0
  */
 package de.fhb.kryptografie;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Observable;
 
 import de.fhb.kryptografie.exceptions.WrongNumberFormatException;
 
 public class KgModel extends Observable {
 
 	/**
 	 * Deklaration der Variablen
 	 */
 	private String plainText = new String("");
 	private String cipherText = new String("");
 	private String key;
 
 	/**
 	 * IO-Elemente zum arbeiten mit Files
 	 */
 	private FileReader fileReader;
 	private BufferedReader bufferedReader;
 
 	/**
 	 * Getter fr Key, PlainText und CypherText
 	 */
 	public String getKey() {
 		return key;
 	}
 
 	public String getPlainText() {
 		return plainText;
 	}
 
 	public String getCypherText() {
 		return cipherText;
 	}
 
 	/**
 	 * Setter fr Key, PlainText und CypherText
 	 */
 	public void setCypherText(String cypherText) {
 		this.cipherText = cypherText;
 	}
 
 	public void setPlainText(String plainText) {
 		this.plainText = plainText;
 	}
 
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	/**
 	 * Die Methode transfrom(String) bearbeitet einen String so, dass nach
 	 * Bearbeitung alle Sonderzeichen, Leerzeichen und Zahlen entfernt werden.
 	 * Sowie     in ae oe ue ss umgewandelt: Es werden nur noch klein und
 	 * Grobuchstaben in den String geschrieben.
 	 * 
 	 * @param text
 	 * @return myText
 	 */
 	public String transform(String text) {
 
 		StringBuilder myText = new StringBuilder("");
 
 		for (int i = 0; i < text.length(); i++) {
 			if (text.charAt(i) > 96 && text.charAt(i) < 123) {
 				myText.append(text.charAt(i));
 			}
 			if (text.charAt(i) > 64 && text.charAt(i) < 91) {
				myText.append(text.charAt(i));
 			}
 			if (text.charAt(i) == '' || text.charAt(i) == '') {
 				myText.append("ae");
 			}
 			if (text.charAt(i) == '' || text.charAt(i) == '') {
 				myText.append("oe");
 			}
 			if (text.charAt(i) == '' || text.charAt(i) == '') {
 				myText.append("ue");
 			}
 			if (text.charAt(i) == '') {
 				myText.append("ss");
 			}
 		}
 
 		return myText.toString();
 	}
 
 	/**
 	 * Die Methode calcutateKey bearbeitet den eingegebenen Key. Es werden nur
 	 * Eingaben von Buchstaben akzeptiert. Der String wird bei angenommenen
 	 * Eingaben in Chars zerlegt und um einen Wert verringert der dem Abstand
 	 * von 0 bis zum a bzw A in der Asci-Tabelle entspricht. Um so an spterer
 	 * Stelle besser mit dem String arbeiten zu knnen.
 	 * 
 	 * @param text
 	 * @return key
 	 * @throws WrongNumberFormatException
 	 */
 	public String calculateKey(String text) throws WrongNumberFormatException {
		StringBuilder key = new StringBuilder();
 
 		for (int i = 0; i < text.length(); i++) {
 			if (text.charAt(i) >= 65 && text.charAt(i) <= 90) {
 				key.append((char) (text.charAt(i) - 64));
 			} else if ((text.charAt(i) >= 97 && text.charAt(i) <= 122)) {
 				key.append((char) (text.charAt(i) - 96));
 			} else {
 				throw new WrongNumberFormatException();
 			}
 		}
 		this.key = text.toUpperCase();
 		return key.toString();
 	}
 
 	/**
 	 * Die Methode openFile ffnet wie der Name schon sagt eine Datei. Je nach
 	 * dem mit welchem File-chooser diese Methode aufgerufen wird, wird auch der
 	 * Inhalt der Datei in dem entsprechenden Feld bergeben.
 	 * 
 	 * @param selectedFile
 	 * @param plain
 	 * @throws IOException
 	 */
 	public void openFile(File selectedFile, boolean plain) throws IOException {
 		fileReader = new FileReader(selectedFile);
 		bufferedReader = new BufferedReader(fileReader);
 		StringBuilder myString = new StringBuilder();
 
 		do {
 			myString.append(bufferedReader.readLine());
 		} while (bufferedReader.readLine() != null);
 
 		bufferedReader.close();
 
 		if (plain == true) {
 			plainText = myString.toString();
 			cipherText = "";
 		} else {
 			plainText = "";
 			cipherText = myString.toString();
 		}
 		setChanged();
 		notifyObservers();
 	}
 
 	/**
 	 * Die Methode enciher chiffriert einen String mit einem gegebenen Key auf
 	 * der Basis des Vigenre Verfahrens.
 	 * 
 	 * @param transformed
 	 * @param key
 	 */
 	public void encipher(String transformed, String key) {
 		StringBuilder myText = new StringBuilder("");
 		int j = 0;
 
 		for (int i = 0; i < transformed.length(); i++, j++) {
 			if (j == key.length()) {
 				j = 0;
 			}
 			if ((transformed.charAt(i) + key.charAt(j)) <= 122) {
 				myText.append((char) (transformed.charAt(i) + key.charAt(j)));
 			} else if (((char) transformed.charAt(i) + key.charAt(j)) >= 122) {
 				myText.append((char) (transformed.charAt(i) + key.charAt(j) - 26));
 			} else {
 				myText.append((char) (transformed.charAt(i) + key.charAt(j) + 26));
 			}
 		}
 
 		cipherText = myText.toString().toUpperCase();
 		plainText = transformed;
 
 		setChanged();
 		notifyObservers();
 	}
 
 	/**
 	 * Die Methode decipher dechiffriert einen String mit einem gegebenen Key
 	 * auf der Basis des Vigenre Verfahrens.
 	 * 
 	 * @param transformed
 	 * @param key
 	 */
 	public void decipher(String transformed, String key) {
 		StringBuilder myText = new StringBuilder("");
 		int j = 0;
 
 		for (int i = 0; i < transformed.length(); i++, j++) {
 			if (j == key.length()) {
 				j = 0;
 			}
			if ((transformed.charAt(i) - key.charAt(j)) >= 65) {
 				myText.append((char) (transformed.charAt(i) - key.charAt(j)));
			} else if (((char) transformed.charAt(i) - key.charAt(j)) <= 65) {
				myText.append((char) (transformed.charAt(i) - key.charAt(j) + 26));
			} else {
				myText.append((char) (transformed.charAt(i) - key.charAt(j) - 26));
 			}
 		}
 
 		plainText = myText.toString().toLowerCase();
		cipherText = transformed;
 
 		setChanged();
 		notifyObservers();
 	}
 }
