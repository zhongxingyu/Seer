 package fr.fcamblor.demos.sbjd.web;
 
 import fr.fcamblor.demos.sbjd.test.rules.RequiresDefaultRestAssuredConfiguration;
 import fr.fcamblor.demos.sbjd.test.rules.RequiresRunningEmbeddedTomcat;
 import org.junit.Rule;
 import org.junit.Test;
import org.springframework.http.HttpStatus;
 
 import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
 
 /**
  * @author fcamblor
  */
 public class AuthenticationControllerTest {
 
    private static final int VALIDATION_ERROR_HTTP_STATUS_CODE = HttpStatus.PRECONDITION_FAILED.value();
    private static final int VALIDATION_OK_HTTP_STATUS_CODE = HttpStatus.OK.value();
 
     @Rule
     // Ensuring a tomcat server will be up and running during test execution !
     public RequiresRunningEmbeddedTomcat tomcat = new RequiresRunningEmbeddedTomcat();
 
     @Rule
     public RequiresDefaultRestAssuredConfiguration raConfig = new RequiresDefaultRestAssuredConfiguration();
 
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
                param("login", "foo@bar.com").
                 param("password", "bar").
         expect().
                statusCode(not(equalTo(VALIDATION_ERROR_HTTP_STATUS_CODE))).
         when().
                 post("/auth/authenticate");
     }
 }
