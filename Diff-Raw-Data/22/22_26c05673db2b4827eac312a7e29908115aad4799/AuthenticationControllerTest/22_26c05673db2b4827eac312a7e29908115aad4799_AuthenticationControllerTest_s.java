 package fr.fcamblor.demos.sbjd.web;
 
 import fr.fcamblor.demos.sbjd.test.rules.RequiresDefaultRestAssuredConfiguration;
 import fr.fcamblor.demos.sbjd.test.rules.RequiresRunningEmbeddedTomcat;
import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 
 import static com.jayway.restassured.RestAssured.given;
 
 /**
  * @author fcamblor
  */
 public class AuthenticationControllerTest {
 
    private static final int VALIDATION_ERROR_HTTP_STATUS_CODE = 500;
    private static final int VALIDATION_OK_HTTP_STATUS_CODE = 200;
 
     @Rule
     // Ensuring a tomcat server will be up and running during test execution !
     public RequiresRunningEmbeddedTomcat tomcat = new RequiresRunningEmbeddedTomcat();
 
     @Rule
     public RequiresDefaultRestAssuredConfiguration raConfig = new RequiresDefaultRestAssuredConfiguration();
 
    @Before
    public void setup(){

    }

     @Test
     public void nullCredentialsShouldntBeAccepted(){
         given().
                 param("login").
                 param("password").
         expect().
                 statusCode(VALIDATION_ERROR_HTTP_STATUS_CODE).
         when().
                 post("/auth/authenticate");
     }
 
     @Test
     public void filledCredentialsShouldReturnOk(){
         given().
                param("login", "foo").
                 param("password", "bar").
         expect().
                statusCode(VALIDATION_OK_HTTP_STATUS_CODE).
         when().
                 post("/auth/authenticate");
     }
 }
