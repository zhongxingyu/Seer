 package controller;
 
 import org.junit.Test;
 import play.mvc.Result;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static play.test.Helpers.fakeRequest;
 import static play.test.Helpers.routeAndCall;
 
 public class ClientSettingsController {
 
     @Test
     public void testGetClientSetting() {
         Result result = routeAndCall(fakeRequest("GET", "/clients/1"));
        assertThat(result).isNotNull();                                                           Cli
     }
 }
