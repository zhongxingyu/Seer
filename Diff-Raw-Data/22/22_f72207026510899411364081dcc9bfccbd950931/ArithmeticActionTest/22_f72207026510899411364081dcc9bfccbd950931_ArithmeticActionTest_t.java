 package com.ninja_squad.jb.codestory.action;
 
 import com.ninja_squad.jb.codestory.HttpRequest;
 import com.ninja_squad.jb.codestory.HttpResponse;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 
 import static org.fest.assertions.Assertions.*;
 
 /**
  * Tests for ArithmeticAction
  * @author JB
  */
 public class ArithmeticActionTest {
 
     @Test
     public void shouldReturnGoodResult() throws IOException {
         HttpRequest request = HttpRequest.get("/?q=1+2*3/4-5");
         assertThat(new ArithmeticAction().execute(request).getBodyAsString(StandardCharsets.US_ASCII))
             .isEqualTo("-2,5");
         request = HttpRequest.get("/?q=1+1");
         assertThat(new ArithmeticAction().execute(request).getBodyAsString(StandardCharsets.US_ASCII))
             .isEqualTo("2");
         request = HttpRequest.get("/?q=1,5+1");
         assertThat(new ArithmeticAction().execute(request).getBodyAsString(StandardCharsets.US_ASCII))
             .isEqualTo("2,5");

        request = HttpRequest.get("/?q=((1,1+2)+3,14+4+(5+6+7)+(8+9+10)*4267387833344334647677634)/2*553344300034334349999000");
        System.out.println(new ArithmeticAction().execute(request).getBodyAsString(StandardCharsets.US_ASCII));
     }
 
     @Test
     public void shouldReturnInvalidExpression() throws IOException {
         HttpRequest request = HttpRequest.get("/?q=1+2*3/4-5+");
         HttpResponse result = new ArithmeticAction().execute(request);
         assertThat(result.getBodyAsString(StandardCharsets.US_ASCII)).isEqualTo("Invalid expression: 1+2*3/4-5+");
         assertThat(result.getStatus()).isEqualTo(HttpResponse.Status._400_BAD_REQUEST);
     }
 }
