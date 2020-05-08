 package stein.net;
 
 import static org.mockito.Mockito.times;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 
 public class ReaderThreadTest {
 
 	@Test
 	public void testReadAndAppend() {
 		// mocking, trick readerThread to believe it has socket and chatGUI
 		Socket socket = Mockito.mock(Socket.class);
 		ChatGUI chatGUI = Mockito.mock(ChatGUI.class);
 
 		ByteArrayInputStream in = new ByteArrayInputStream(
 				"JOIN stein\nJOIN karman\n".getBytes());
 		try {
 			Mockito.when(socket.getInputStream()).thenReturn(in);
 
 			ReaderThread thread = new ReaderThread(socket, chatGUI);
 
 			thread.run();
 
 			// check if certain method called
 			//if just want to check that a string was returned, DO Mockito.anyString() instead of "Stein has"...
			//Mockito.verify(chatGUI, times(2)).getChatMessage(
					//Mockito.anyString());
 		} catch (IOException io) {
 			io.printStackTrace();
 		}
 	}
 }
