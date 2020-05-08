 package com.ninja_squad.jb.codestory.action;
 
 import com.ninja_squad.jb.codestory.HttpRequest;
 import com.ninja_squad.jb.codestory.HttpResponse;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 
 import static org.fest.assertions.Assertions.*;
 
 /**
  * Tests for CodeStoryActionFactory
  * @author JB
  */
 public class CodeStoryActionFactoryTest {
     private CodeStoryActionFactory actionFactory;
 
     @Before
     public void setUp() {
         actionFactory = new CodeStoryActionFactory();
     }
 
     @Test
     public void getActionShouldReturnNotFoundForUnknownPath() throws IOException {
         HttpRequest request = HttpRequest.get("/foo?q=Quelle+est+ton+adresse+email");
         assertThat(actionFactory.getAction(request).execute(request).getStatus()).isEqualTo(HttpResponse.Status._404_NOT_FOUND);
     }
 
     @Test
     public void getActionShouldReturnBadRequestForUnknownQuestion() throws IOException {
         HttpRequest request = HttpRequest.get("/?q=Comment+ca+va");
         assertThat(actionFactory.getAction(request).execute(request).getStatus()).isEqualTo(HttpResponse.Status._400_BAD_REQUEST);
     }
 
     @Test
     public void getActionShouldReturnAddressEmailActionForStep1Question() throws IOException {
         HttpRequest request = HttpRequest.get("/?q=Quelle+est+ton+adresse+email");
         assertThat(actionFactory.getAction(request).execute(request).getBodyAsString(StandardCharsets.US_ASCII))
             .isEqualTo("jb+codestory@ninja-squad.com");
     }
 
     @Test
     public void getActionShouldReturnYesForStep2Question() throws IOException {
        HttpRequest request = HttpRequest.get("/?q=Es+tu+abonne+a+la+mailing+list");
         assertThat(actionFactory.getAction(request).execute(request).getBodyAsString(StandardCharsets.US_ASCII))
             .isEqualTo("OUI");
     }
 }
