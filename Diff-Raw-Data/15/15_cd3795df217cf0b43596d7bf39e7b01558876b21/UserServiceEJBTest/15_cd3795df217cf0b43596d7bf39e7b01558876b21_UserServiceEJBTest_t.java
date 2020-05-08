 package au.com.stocksoftware.tide.server.service.tide;
 
 import au.com.stocksoftware.tide.server.service.core.UserService;
 import au.com.stocksoftware.tide.server.test.util.AbstractServerTest;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 public class UserServiceEJBTest
   extends AbstractServerTest
 {
   @Test
   public void testGetUsers()
   {
    factory().createUser( "a", "foo", "bar", "a@a" );
    factory().createUser( "b", "baz", "bob", "b@b" );
     assertEquals( service().getUsers().size(), 2 );
   }
 
   protected UserService service()
   {
     return s( UserService.class );
   }
 }
