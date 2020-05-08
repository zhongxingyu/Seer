 package edu.ncsu.csc216.solitaire.model;
 
 /**
  * The message class
  * @author William Blazer, Andrew Kofink
  */
 public class Message {
 	
 	private static final int NUM_STEPS = 5;
 	
 	/**
 	 * the message array
 	 */
 	private int[] message;
 	
 	/**
 	 * The message encryption method
 	 * @param messageString the message to be encrypted
 	 */
 	public Message(String messageString) {
 		//Replace all non-alphabetic characters with '' and spaces with '[' (The ASCII value of 'Z' + 1)
 		char[] characterArray = messageString.toUpperCase().replaceAll("[^A-Z ]+" , "").replaceAll("[ ]" , "[").toCharArray();
 		message = new int[characterArray.length];
 		for (int i = 0; i < characterArray.length; i++) {
 			message[i] = characterArray[i] - 'A' + 1;
 		}
 	}
 	// the @ symbol is value 64, right before 'A' value of 65, check boundary down here
 	// the characters for ' ' spaces are one more than they should be. I.E , a 'X' is being returned when it should be a 'W' for spaces
 	
 	/**
 	 * The encryption method
 	 * @param deck the deck to be processed
 	 * @return the encrypted character
 	 */
 	public String encrypt(Deck deck) {
 		char[] characterArray = new char[message.length];
 		for (int i = 0; i < message.length; i++) {
 			int keystream = deck.getKeystreamValue();
 			if (keystream + message[i] > Deck.DECK_SIZE - 1) {
 				keystream -= (Deck.DECK_SIZE - 1);
 			}
 			message[i] = message[i] + keystream;
 			characterArray[i] = (char)(message[i] + 'A' - 1);
 		}
 		return String.copyValueOf(characterArray).replaceAll("\\[", " ");
 	}
 	
 	public char nextLetter(Deck d) {
 		for (int i = 0; i < NUM_STEPS; i++) {
 			d.nextStep();
 		}
 		return translate(d.nextStep());
 	}
 
 	/**
 	 * The decryption Method
 	 * @param deck the deck to be processed 
 	 * @return a decrypted character
 	 */
 	public String decrypt(Deck deck) {
 		char[] characterArray = new char[message.length];
 		for (int i = 0; i < message.length; i++) {
 			message[i] = (message[i] - deck.getKeystreamValue());
 			if (message[i] <= 0) {
 				message[i] += Deck.DECK_SIZE - 1;
 			}
 			characterArray[i] = (char)(message[i] + 'A' - 1);
 		}
 		return String.copyValueOf(characterArray).replaceAll("\\[", " ");
 	}
 	
 	public String getMessage() {
 		return String.copyValueOf(translate(message));
 	}
 	
 	private char[] translate(int[] intArr) {
 		char[] characterArray = new char[intArr.length];
 		for (int i = 0; i < intArr.length; i++) {
 			if (intArr[i] == -1) {
 				characterArray[i] = '/';
 			} else {
 				characterArray[i] = (char)(message[i] + 'A' - 1);
 			}
 		}
 		return characterArray;
 	}
 	
 	private char translate(int input) {
 		int[] intArr = {input};
 		char[] charArr = translate(intArr);
 		return charArr[0];
 	}
 }
