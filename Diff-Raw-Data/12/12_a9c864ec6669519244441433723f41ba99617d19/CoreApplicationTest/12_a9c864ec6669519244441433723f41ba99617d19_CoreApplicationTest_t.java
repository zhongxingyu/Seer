 package controllers.origo.core;
 
 import org.junit.Test;
 import play.mvc.Http;
 import play.test.FunctionalTest;
 
 public class CoreApplicationTest extends FunctionalTest {
 
     @Test
     public void indexPageWorks() {
         Http.Response response = GET("/");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
     }
 
     @Test
     public void redirectedToPageNotFound() {
         Http.Response response = GET("/page-that-does-not-exist");
         assertStatus(302, response);
     }
 
     @Test
     public void fourthPageWorks() {
         Http.Response response = GET("/fourth");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
        assertContentMatch("Stan bam Mrs. Crabapple", response);
     }
 
     @Test
     public void programaticallyAddedContent() {
         Http.Response response = GET("/fourth");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
         assertContentMatch("Bla bla", response);
     }
 
 
 }
