 package edu.ncsu.csc216.solitaire.model;
 
 import junit.framework.TestCase;
 
 
 /**, 28
  * Tests the Message class
  * @author William Blazer, Andrew Kofink
  */
 public class MessageTest extends TestCase {
 
 	private String startingMessage = "GO PACK BEAT CAROLINA AS WELL AS THOSE OTHER KIND OF UGLY SCHOOLS THIS " +
 			"RATHER LONG STRING IS A TEST OF XYLOPHONES AND LONGNESS TO CREATE RANDOM LISTS";
 
 	/**
 	 * Tests the encrypt method
 	 */
 	public void testEncrypt() {
 		for (int i = 0; i < DeckTest.testDecks.length; i++) {
 			Deck d1 = new Deck(DeckTest.testDecks[i]);
 			Message md = new Message(startingMessage);
 			String encryptedMessage = md.encrypt(d1);
 			
 			Deck d2 = new Deck(DeckTest.testDecks[i]);
 			Message me = new Message(encryptedMessage);
 			String decryptedMessage = me.decrypt(d2);
			//System.out.println(encryptedMessage + " | " + decryptedMessage);
 			assertEquals(decryptedMessage, startingMessage);
 		}
 	}
 
 	/**
 	 * Tests the decrypt method
 	 */
 	public void testDecrypt() {
 		testEncrypt();
 	}
 
 }
