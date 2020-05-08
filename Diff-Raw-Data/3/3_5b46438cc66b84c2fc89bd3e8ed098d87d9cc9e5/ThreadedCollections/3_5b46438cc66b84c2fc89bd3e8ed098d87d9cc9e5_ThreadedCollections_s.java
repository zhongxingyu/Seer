 package fm.audiobox.core.test;
 
 
 import java.net.SocketException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.models.Albums;
 import fm.audiobox.core.models.Artists;
 import fm.audiobox.core.models.Genres;
 import fm.audiobox.core.models.Playlists;
import fm.audiobox.core.test.mocks.fixtures.StaticAudioBox;
 import fm.audiobox.core.test.mocks.fixtures.UserFixture;
 import fm.audiobox.core.test.mocks.models.User;
 
 public class ThreadedCollections extends junit.framework.TestCase {
 
 	StaticAudioBox abc;
     User user;
 
     @SuppressWarnings("deprecation")
     @Before
     public void setUp() throws Exception {
         System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
         System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl", "debug");
 
         System.setProperty("org.apache.commons.logging.simplelog.log.fm.audiobox.api", "debug");
 
         StaticAudioBox.initClass(StaticAudioBox.USER_KEY , User.class );
         //AudioBoxClient.setUserClass(User.class);
         abc = new StaticAudioBox();
         abc.setForceTrust(true);
         
         try {
             user = (User) abc.login( UserFixture.LOGIN , UserFixture.RIGHT_PASS );
         } catch (LoginException e) {
             e.printStackTrace();
         } catch (SocketException e) {
             e.printStackTrace();
         }
     }
 
     @Test
     public void testPreconditions() {
         assertNotNull( abc );
         assertNotNull( user );
     }
 
     @Test
     public void testTrhead()  {
         
         try {
             Playlists pls = user.getPlaylists(true);
             Artists ars = user.getArtists(true);
             Genres gr = user.getGenres(true);
             Albums alb = user.getAlbums(true);
             
             assertNotNull( pls );
             assertNotNull( ars );
             assertNotNull( gr );
             assertNotNull( alb );
             
         } catch (ModelException e) {
             e.printStackTrace();
         }
         
     }
 
 }
