 package temmental2;
 
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static org.junit.Assert.*;
 
 public class MessageTest extends AbstractTestElement {
 
 	@Test
 	public void testMessage() throws TemplateException, IOException {
 		Message message = message(identifier("'message", "-:l1:c1"), list());
 		
 		populateProperty("message", "hello mister");
 		
 		assertEquals("hello mister", message.writeObject(transforms, model, messages));
 	}
 	
 	@Test
 	public void testMessagePropertyNotPresent() throws TemplateException, IOException {
 		Message message = message(identifier("'message", "-:l1:c1"), list());
 		assertWriteObjectThrowsAnException("Key 'message' is not present in the property map to render message ''message[]' at position '-:l1:c1'.", message);
 		
 	}
 	
 	private void assertWriteObjectThrowsAnException(String expected, Message message) throws IOException {
 		try {
 			message.writeObject(transforms, model, messages);
 			fail("An exception must be raised.");
 		} catch (TemplateException e) {
 			assertEquals(expected, e.getMessage());
 		}
 	}
 
 	@Test
 	public void testMessageKeyOptionalPresent() throws TemplateException, IOException {
 		Message message = message(identifier("$message?", "-:l1:c1"), 
 				list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));
 		
 		populateProperty("themessage", "hello {0} {1}");
 		
 		populateModel("message", "themessage");
 		populateModel("firstname", "John");
 		populateModel("lastname", "Doe");
 		
 		assertEquals("hello John Doe", message.writeObject(null, model, messages));
 	}
 	
 	@Test
 	public void testMessageKeyOptionalNotPresent() throws TemplateException, IOException {
 		Message message = message(identifier("$message?", "-:l1:c1"), 
 				list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));
 		
 		populateProperty("themessage", "hello {0} {1}");
 		
 		populateModel("firstname", "John");
 		populateModel("lastname", "Doe");
 		
 		assertNull(message.writeObject(null, model, messages));
 	}
 	
 	@Test
 	public void testMessageKeyOptionalPropertyNotPresent() throws TemplateException, IOException {
 		Message message = message(identifier("$message?", "-:l1:c1"), 
 				list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));
 		
 		populateModel("message", "themessage");
 		populateModel("firstname", "John");
 		populateModel("lastname", "Doe");
 		
 		assertWriteObjectThrowsAnException("Key 'themessage' is not present in the property map to render message '$message?[\u2026]' at position '-:l1:c1'.", message);
 	}
 
 	@Test
 	public void testMessageWithParameter() throws TemplateException, IOException {
 		Message message = message(identifier("'message", "-:l1:c1"), 
 				list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));
 		
 		populateProperty("message", "hello {0} {1}");
 		
 		populateModel("firstname", "John");
 		populateModel("lastname", "Doe");
 		
 		assertEquals("hello John Doe", message.writeObject(null, model, messages));
 	}
 	
 	@Test
 	public void testMessageWithParameterOptionalPresent() throws TemplateException, IOException {
 		Message message = message(identifier("'message", "-:l1:c1"), 
 				list(identifier("$firstname?", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));
 		
 		populateProperty("message", "hello {0} {1}");
 		
 		populateModel("firstname", "John");
 		populateModel("lastname", "Doe");
 		
 		assertEquals("hello John Doe", message.writeObject(null, model, messages));
 	}
 	
 	@Test
 	public void testMessageWithParameterOptionalNotPresent() throws TemplateException, IOException {
 		Message message = message(identifier("'message", "-:l1:c1"), 
 				list(identifier("$firstname?", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));
 		
 		populateProperty("message", "hello {0} {1}");
 		
 		populateModel("lastname", "Doe");
 		
 		assertNull(message.writeObject(null, model, messages));
 	}
 
     @Test
     public void testMessageWithRequiredParameterNotPresent() throws TemplateException, IOException {
         Message message = message(identifier("'message", "-:l1:c1"),
                 list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));
 
         populateProperty("message", "hello {0} {1}");
 
         populateModel("firstname", "John");
 
         assertWriteObjectThrowsAnException("Key 'lastname' is not present or has null value in the model map at position '-:l1:c3'.", message);
     }
 
     @Test
     public void testMessageWithRequiredParameterNullValue() throws TemplateException, IOException {
         Message message = message(identifier("'message", "-:l1:c1"),
                 list(identifier("$firstname", "-:l1:c2"), identifier("$lastname", "-:l1:c3")));
 
         populateProperty("message", "hello {0} {1}");
 
         populateModel("firstname", "John");
         populateModel("lastname", null);
 
         assertWriteObjectThrowsAnException("Key 'lastname' is not present or has null value in the model map at position '-:l1:c3'.", message);
     }
 
     @Test
     public void testMessageWithRequiredParameterNullValueAfterProcess1() throws TemplateException, IOException {
         Message message = message(identifier("'message", "-:l1:c1"),
                 list(identifier("$firstname", "-:l1:c2"),
                         function(identifier("'z", "-:l1:c4"),
                                 identifier("$lastname", "-:l1:c3"))
                         ));
 
         populateProperty("message", "hello {0} {1}");
 
         Transform<Integer,Integer> toNull = new Transform<Integer, Integer>() {
             public Integer apply(Integer value) throws TemplateException {
                 return null;
             }
         };
         populateTransform("z", toNull);
 
         populateModel("firstname", "John");
         populateModel("lastname", null);
 
         assertWriteObjectThrowsAnException("Key 'lastname' is not present or has null value in the model map at position '-:l1:c3'.", message);
     }
 
     @Test
     public void testMessageWithRequiredParameterNullValueAfterProcess2() throws TemplateException, IOException {
         Message message = message(identifier("'message", "-:l1:c1"),
                 list(identifier("$firstname", "-:l1:c2"),
                         function(identifier("'z", p(1,11)),
                                 identifier("$lastname", "-:l1:c3"))
                 ));
 
         populateProperty("message", "hello {0} {1}");
 
         Transform<String,String> toNull = new Transform<String, String>() {
             public String apply(String value) throws TemplateException {
                 return null;
             }
         };
         populateTransform("z", toNull);
 
         populateModel("firstname", "John");
         populateModel("lastname", "Doe");
 
        assertWriteObjectThrowsAnException("Unable to render ''message[\u2026]' at position '-:l1:c1'. Parameter '#1' is null!", message);
     }
 
 }
