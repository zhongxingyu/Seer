 package test;
 
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.Arrays;
 
 import hub.PasswordStore;
 
 
 public class PasswordStoreTest {
 	
 	public static void main(String[] argv) {
 		PasswordStore pws = new PasswordStore();
 		pws.addEntry("10user1", "pass1".toCharArray());
 		pws.addEntry("user2", "pass2".toCharArray());
 		pws.addEntry("10user3", "pass2".toCharArray());
 		
 		System.out.println("All should be true");
 		try {
 			System.out.println(pws.containsEntry("10user1") == true);
 			System.out.println(pws.containsEntry("user2") == true);
 			System.out.println(!pws.containsEntry("godzilla"));
 			System.out.println(pws.authenticate("10user1", "pass1".toCharArray()));
 			System.out.println(!pws.authenticate("user2", "pass3".toCharArray()));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
