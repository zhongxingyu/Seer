 import com.sun.net.httpserver.HttpExchange;
 import cukepresentation.HelloWorldHandler;
 import org.easymock.EasyMock;
 import org.easymock.IArgumentMatcher;
 import org.junit.Test;
 
 import java.io.OutputStream;
 import java.util.Arrays;
 
 /**
  * @author: Stephen Abrams
  */
 public class HelloWorldHandlerTest {
     @Test
     public void testHandle() throws Exception {
 
        String expectedResponse = "Hello, World!";
 
         HelloWorldHandler handlerUnderTest = new HelloWorldHandler();
         HttpExchange mockExchange = EasyMock.createMock(HttpExchange.class);
 
         // output stream written to
         OutputStream mockOutputStream = EasyMock.createMock(OutputStream.class);
 
         // expect to send "OK" response with content length
         mockExchange.sendResponseHeaders(200, expectedResponse.length());
         EasyMock.expectLastCall();
 
         EasyMock.expect(mockExchange.getResponseBody()).andReturn(mockOutputStream);
 
         // expect "Hello World!" to be written to the output stream
         mockOutputStream.write(ByteArrayMatcher.eq(expectedResponse.getBytes()));
         EasyMock.expectLastCall();
 
         EasyMock.replay(mockExchange, mockOutputStream);
 
         handlerUnderTest.handle(mockExchange);
 
         EasyMock.verify(mockExchange, mockOutputStream);
     }
 
     public static class ByteArrayMatcher implements IArgumentMatcher {
 
         byte[] expected;
 
         public ByteArrayMatcher(byte[] expected) {
             this.expected = expected;
         }
 
         public static byte[] eq(byte[] byteArr) {
             EasyMock.reportMatcher(new ByteArrayMatcher(byteArr));
             return null;
         }
 
         @Override
         public boolean matches(Object argument) {
             byte[] actual = (byte[]) argument;
             return Arrays.equals(actual, this.expected);
         }
 
         @Override
         public void appendTo(StringBuffer buffer) {
             buffer.append("eq(").append(this.expected).append(')');
         }
     }
 }
