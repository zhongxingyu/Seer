 package de.piratenpartei.id.test;
 
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import de.piratenpartei.id.*;
 
 import org.junit.Test;
 
 public class TestMessenger {
 
 	@Test
	public void test() {
 		PrivateAccount pa;
 		try {
 			pa = new PrivateAccount();
 			Messenger m = new Messenger(pa,"test_files/messengerTest");
 			m.sendMessageToUser("troll", "Hallo, ich kenn dich nicht");
 			System.out.println("successful");
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println(e.getMessage());
 			throw new RuntimeException();
 		}
 	}
 
 }
