 package de.fhb.kryptografie;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Observable;
 
 import de.fhb.kryptografie.exceptions.WrongNumberFormatException;
 
 public class KgModel extends Observable {
 
 	private String plainText = new String("");
 	private String cipherText = new String("");
 	private String key;
 
 	private FileReader fileReader;
 	private BufferedReader bufferedReader;
 	
 	public String getKey() {
 		return key;
 	}
 
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	public String getPlainText() {
 		return plainText;
 	}
 
 	public void setPlainText(String plainText) {
 		this.plainText = plainText;
 	}
 
 	public String getCypherText() {
 		return cipherText;
 	}
 
 	public void setCypherText(String cypherText) {
 		this.cipherText = cypherText;
 	}
 
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
 
 	public String calculateKey(String text) throws WrongNumberFormatException {
 		StringBuilder key = new StringBuilder();
 
 		for (int i = 0; i < text.length(); i++) {
 			if (text.charAt(i) >= 65 && text.charAt(i) <= 90) {
 				key.append((char) (text.charAt(i) - 64));
 			} else if ((text.charAt(i) >= 97 && text.charAt(i) <= 122)) {
				key.append((char) (text.charAt(i) - 97));
 			} else {
 				throw new WrongNumberFormatException();
 			}
 		}
 		this.key = text.toUpperCase();
 		return key.toString();
 	}
 
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
