 package edu.um.arq.umflix.catalogservice.impl;
 
 import edu.umflix.authenticationhandler.AuthenticationHandler;
 import edu.umflix.authenticationhandler.exceptions.InvalidTokenException;
 import edu.umflix.authenticationhandler.impl.AuthenticationHandlerImpl;
 import edu.umflix.persistence.MovieDao;
 import edu.umflix.persistence.impl.MovieDaoImpl;
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import org.mockito.Mockito;
 
 import java.util.ResourceBundle;
 
 /**
  *
  * This class tests the implementation of CatalogServiceImpl methods defined in CatalogService interface using the Mocking Framework.
  *
  */
 public class CatalogServiceImplTest {
 
     /*
      *
      * This class mocks CatalogServiceImpl so it uses mocked classes for the CatalogService's methods implementation
      *
      */
     class CatalogServiceMock extends CatalogServiceImpl {
 
         public CatalogServiceMock(){
           authHandler = Mockito.mock(AuthenticationHandlerImpl.class);
           movieDao = Mockito.mock(MovieDaoImpl.class);
         }
 
     }
 
     /*
      *
      * This test verifies that CatalogServiceImpl delegates token validation to AuthenticationHandler
      * and when the token is valid and the String parameter is null, it delegates getMovieList to MovieDao.
      *
      */
     @Test
     public void testSearchAllMoviesWithValidToken() {
         CatalogServiceMock testedMock = new CatalogServiceMock();
         String key = null;
         String token ="Valid token";
         Mockito.when(testedMock.authHandler.validateToken(token)).thenReturn(true);  //Valid token; validate returns true
         try {
             testedMock.search(key,token);
         } catch (InvalidTokenException e) {
             fail("El token fue inválido");
         }
         // Verifies the method delegation is correctly done.
         Mockito.verify(testedMock.authHandler).validateToken(token);
         Mockito.verify(testedMock.movieDao).getMovieList();
     }
 
     /*
      *
      * This test verifies that CatalogServiceImpl delegates token validation to AuthenticationHandler
      * and if the token is invalid and the String parameter is null it throws an InvalidTokenException and has no interactions with MovieDao.
      *
     */
     @Test
     public void testSearchAllMoviesWithInvalidToken() {
         CatalogServiceMock testedMock = new CatalogServiceMock();
         String key = null;
         String token = "Invalid token";
         Mockito.when(testedMock.authHandler.validateToken(token)).thenReturn(false); // Invalid token; validate returns false
         try {
             testedMock.search(key,token);
         } catch (Exception e) {
             assertTrue(e instanceof InvalidTokenException);
         }
         // Verifies the method delegation is correctly done and there are no interactions with MovieDaoImpl.
         Mockito.verify(testedMock.authHandler).validateToken(token);
         Mockito.verifyZeroInteractions(testedMock.movieDao);
     }
 
     /*
      *
      * This test verifies that CatalogServiceImpl delegates token validation to AuthenticationHandler
      * and if the token is valid and the String parameter is not null, it delegates getMovieListByKey to MovieDao.
      *
     */
     @Test
     public void testSearchMoviesByKeyWithValidToken() {
         CatalogServiceMock testedMock = new CatalogServiceMock();
         String key = "Not Null Key";
         String token = "Valid token";
         Mockito.when(testedMock.authHandler.validateToken(token)).thenReturn(true);  //Valid token; validate returns true
         try {
             testedMock.search(key,token);
         } catch (InvalidTokenException e) {
             fail("El token fue inválido");
         }
         // Verifies the method delegation is correctly done.
         Mockito.verify(testedMock.authHandler).validateToken(token);
         Mockito.verify(testedMock.movieDao).getMovieListByKey(key);
     }
 
     /*
      *
      * This test verifies that CatalogServiceImpl delegates token validation to AuthenticationHandler
      * and if the token is invalid and the String parameter is not null it throws an InvalidTokenException and has no interactions with MovieDao.
      *
     */
     @Test
     public void testSearchMoviesByKeyWithInvalidToken() {
         CatalogServiceMock testedMock = new CatalogServiceMock();
         String key = "Not Null Key";
         String token = "Invalid token";
         Mockito.when(testedMock.authHandler.validateToken(token)).thenReturn(false); // Invalid token; validate returns false
         try {
             testedMock.search(key,token);
         } catch (Exception e) {
             assertTrue(e instanceof InvalidTokenException);
         }
         // Verifies the method delegation is correctly done and there are no interactions with MovieDaoImpl.
         Mockito.verify(testedMock.authHandler).validateToken(token);
         Mockito.verifyZeroInteractions(testedMock.movieDao);
     }
 
 }
