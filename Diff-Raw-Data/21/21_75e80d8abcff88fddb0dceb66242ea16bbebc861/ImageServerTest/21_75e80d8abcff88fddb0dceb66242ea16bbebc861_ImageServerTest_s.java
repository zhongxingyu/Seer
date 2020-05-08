 package test;
 
 import static org.junit.Assert.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import bll.admin.ImageServer;
 
 /**
  * 
  * Integration tests to make sure that we can
  * fetch a JSON representation of the last images
  * as well as configuration values stored in our
  * database.
  * @author Stian Sandve
  *
  */
 
 public class ImageServerTest {
 	
 	//I am aware of the redundancy in these tests.
 	//I will do some refactoring later.
 	
 	ImageServer server;
 	
 	@Before
 	public void initServer() {
 		server = new ImageServer();
 		server.start();
 	}
 	
 	@After
 	public void shutdownServer() {
 		server.stop(0);
 	}
 
 	//requires that the database is running!
 	@Test
 	public void connectToServerFetchImagesAndCheckThatResponseIsNotEmptyTest() throws IOException {
 		URL url = new URL("http://localhost:8000/images");
 		URLConnection conn = url.openConnection();
 		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		String response = in.readLine();
		assertNotEquals("", response);
 	}
 	
 	@Test
	public void connectToServerAndFetchConfigTest() throws IOException {
 		URL url = new URL("http://localhost:8000/config");
 		URLConnection conn = url.openConnection();
 		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		assertEquals("config", in.readLine());
 	}
 }
