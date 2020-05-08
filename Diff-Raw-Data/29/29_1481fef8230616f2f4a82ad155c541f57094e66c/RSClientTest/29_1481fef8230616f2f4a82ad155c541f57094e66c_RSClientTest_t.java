 package br.usp.ime.choreos.vv;
 
 import static junit.framework.Assert.assertEquals;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class RSClientTest {
 	
 	private static RSClient client;
 	
 	@BeforeClass
 	public static void setup() throws IllegalArgumentException, SQLException, ClassNotFoundException, IOException{
		client = new RSClient("http://choreos.ime.usp.br", "/rest/bookstore", 53111);
 	}
 	
 	@Test
 	public void shouldGetCorrectBook(){
 		String retrievedBook = client.get("/book/0");
 		String expectedBook = "Book [title=O Hobbit, author=J. R. R. Tolkien]";
 		
 		assertEquals(retrievedBook, expectedBook);
 	}
 	
 	@Test
 	public void shouldDeleteFirstBook(){
 		String deletedBook = client.delete("/book/0");
 		String expectedBook = "Book [title=O Hobbit, author=J. R. R. Tolkien]";
 		
 		assertEquals(deletedBook, expectedBook);
 		
		assertEquals("Not found!", client.get("/book/0"));
 	}
 	
 	@Test
 	public void shouldCreateABook() {
 
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("title", "New Moon");
 		parameters.put("author", "Stephanie Mayer");
 		
 		String id = client.post("/addBook", parameters);
 		
 		String retrievedBook = client.get("/book/" + id);
 		String expectedBook = "Book [title=New Moon, author=Stephanie Mayer]";
 		
 		assertEquals(expectedBook, retrievedBook);
 		
 		client.delete("/book/" + id);
 	}
 	
 
 }
