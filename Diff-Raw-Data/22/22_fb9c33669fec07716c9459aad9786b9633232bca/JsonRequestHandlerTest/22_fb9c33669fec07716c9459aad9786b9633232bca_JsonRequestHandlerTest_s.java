 /**
  * 
  */
 package uk.co.pekim.nodejava.server;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
import uk.co.pekim.nodejava.NodeJavaException;

 /**
  * Test {@link JsonRequestHandler}.
  * 
  * @author Mike D Pilsbury
  */
 public class JsonRequestHandlerTest {
     private JsonRequestHandler jsonRequestHandler;
 
     @Before
     public void setup() {
         jsonRequestHandler = new JsonRequestHandler();
     }
 
    @Test(expected = NodeJavaException.class)
     public void testHandlerNotFound() {
        jsonRequestHandler.handle("bad", "request");
     }
 
    @Test(expected = NodeJavaException.class)
     public void testHandlerNotImplementNodeJavaHandler() {
        jsonRequestHandler.handle("java.lang.Object", "request");
     }
 
     @Test
     public void voidHandle() {
         String request = "{\"value\":3}";
         String response = jsonRequestHandler.handle(Handler.class.getName(), request);
 
         assertEquals("{\"value\":4}", response);
     }
 
     public static class Request implements NodeJavaRequest {
         private int value;
 
         public void setValue(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
     }
 
     public static class Response implements NodeJavaResponse {
         private int value;
 
         public void setValue(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
     }
 
     public static class Handler implements NodeJavaHandler {
         @Override
         public Class<? extends NodeJavaRequest> getRequestClass() {
             return Request.class;
         }
 
         @Override
         public NodeJavaResponse handle(NodeJavaRequest nodeJavaRequest) {
             Request request = (Request) nodeJavaRequest;
 
             Response response = new Response();
             response.setValue(request.getValue() + 1);
 
             return response;
         }
 
     }
 }
