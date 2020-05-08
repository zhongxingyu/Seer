 package edu.ncsu.csc216.solitaire.model;
 
 public class Message {
 	
 	private int[] message;
 	
 	public Message(String messageString) {
 		//Replace all non-alphabetic characters with '' and spaces with '[' (The ASCII value of 'Z' + 1)
 		char[] characterArray = messageString.toUpperCase().replaceAll("[^A-Z ]+","").replaceAll("[ ]","[").toCharArray();
 		message = new int[characterArray.length];
 		for (int i = 0; i < characterArray.length; i++) {
 			message[i] = characterArray[i] - 'A' + 1;
 		}
 	}
 
 	public String encrypt(Deck deck) {
 		System.out.println("Encrypting...");
 		char[] characterArray = new char[message.length];
 		for (int i = 0; i < message.length; i++) {
 			message[i] = (message[i] + deck.getKeySteamValue()) % ('[' - 'A');
 			characterArray[i] = (char)(message[i] + 'A' - 1);
 		}
		return String.copyValueOf(characterArray).replaceAll("\\[", " ");
 	}
 
 	public String decrypt(Deck deck) {
 		System.out.println("Decrypting...");
 		char[] characterArray = new char[message.length];
 		for (int i = 0; i < message.length; i++) {
 			message[i] = (message[i] - deck.getKeySteamValue());
 			if (message[i] <= 0) {
 				message[i] += '[' - 'A';
 			}
 			characterArray[i] = (char)(message[i] + 'A' - 1);
 		}
 		return String.copyValueOf(characterArray).replaceAll("[[]", " ");
 	}
 
 }
