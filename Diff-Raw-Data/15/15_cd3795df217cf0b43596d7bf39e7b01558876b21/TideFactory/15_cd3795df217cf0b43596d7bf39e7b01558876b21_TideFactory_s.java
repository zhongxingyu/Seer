 package au.com.stocksoftware.tide.server.test.util;
 
 import au.com.stocksoftware.tide.server.entity.core.User;
 import au.com.stocksoftware.tide.server.entity.core.dao.UserDAO;
 import com.google.inject.Injector;
 import java.util.UUID;
 
 public class TideFactory
 {
   private final Injector _injector;
 
   public TideFactory( final Injector injector )
   {
     _injector = injector;
   }
 
   protected final <T> T s( final Class<T> type )
   {
     return _injector.getInstance( type );
   }
 
  public User createUser( final String name, final String password )
   {
     final User user = new User();
     user.setName( name );
     user.setPassword( password );
     s( UserDAO.class ).persist( user );
     return user;
   }
 
   public static String createRandomID()
   {
     final String randomUUID = UUID.randomUUID().toString();
     return randomUUID.substring( 0, Math.min( 50, randomUUID.length() ) );
   }
 }
