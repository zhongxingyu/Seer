 package fm.audiobox.core.test;
 
 
 import java.net.SocketException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Before;
 import org.junit.Test;
 
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.Album;
 import fm.audiobox.core.models.Playlist;
 import fm.audiobox.core.models.Playlists;
 import fm.audiobox.core.models.Track;
 import fm.audiobox.core.models.Tracks;
 import fm.audiobox.core.test.mocks.fixtures.Fixtures;
 import fm.audiobox.core.test.mocks.models.User;
 
 public class ThreadedTest extends junit.framework.TestCase {
 
 	StaticAudioBox abc;
     User user;
     Fixtures fx = new Fixtures();
     
     @SuppressWarnings("deprecation")
     @Before
     public void setUp() throws Exception {
         System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
         System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
         System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.impl", "debug");
 
         System.setProperty("org.apache.commons.logging.simplelog.log.fm.audiobox.api", "debug");
 
         StaticAudioBox.setModelClassFor(StaticAudioBox.USER_KEY , User.class );
 
         abc = new StaticAudioBox();
         abc.setForceTrust(true);
         
         try {
             user = (User) abc.login( fx.get( Fixtures.LOGIN ), fx.get( Fixtures.RIGHT_PASS ) );
         } catch (LoginException e) {
             e.printStackTrace();
         } catch (SocketException e) {
             e.printStackTrace();
         }
     }
 
     @Test
     public void testTrhead() throws InterruptedException {
         
         assertNotNull( abc );
         assertNotNull( user );
         
         final Log logger = LogFactory.getLog(ThreadedTest.class);
         final Handle h1 = new Handle();
         final Handle h2 = new Handle();
         
         Thread t1 = new Thread(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     
                     logger.debug("Started thread #1");
                     
                     String[] tracks = user.getUploadedTracks();
                     assertNotNull(tracks);
                     assertTrue(tracks.length > 0);
                     for (int i = 0; i < tracks.length; i++)
                         assertTrue( tracks[i].length() == 32 );
                     h1.setDone(true);
                     
                     logger.debug("Ended thread #1");
                     
                 } catch (LoginException e) {
                     e.printStackTrace();
                } catch (ServiceException e) {
                    e.printStackTrace();
                 }
             }
 
         });
 
         Thread t2 = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     logger.debug("Started thread #2");
                     
                     Playlists pls = (Playlists) user.getPlaylists(false);
                     assertNotNull(pls.getCollection());
                     
                     Playlist pl = pls.get(0);
                     
                     assertNotNull(pl);
                     
                     Tracks tracks = (Tracks) pl.getTracks();
                     
                     assertNotNull(tracks);
                     
                     Track tr = tracks.get(0);
                     
                     assertNotNull(tr);
                     assertNotNull( tr.getName() );
                     assertNotNull( tr.getToken() );
 
                     assertNotNull( tr.getArtist() );
                     assertNotNull( tr.getArtist().getToken());
 
                     assertNotNull( tr.getAlbum() );
                     assertNotNull( tr.getAlbum().getToken() );
 
                     assertNotNull( ((Album) tr.getAlbum()).getArtist() );
                     assertNotNull( ((Album) tr.getAlbum()).getArtist().getToken() );
                     
                     h2.setDone(true);
                     
                     logger.debug("Ended thread #2");
                     
                 } catch (ModelException e) {
                     e.printStackTrace();
                 }
             }
         });
 
         long _start = System.currentTimeMillis();
         t1.start();
         t2.start();
         
         while ( t1.isAlive() || t2.isAlive() ) {}
 
         logger.debug( "End: " +  (  System.currentTimeMillis() - _start  ) );
         
         assertTrue( h1.getDone() && h2.getDone() );
     }
 
     private class Handle {
         
         private boolean done = false;
         
         public void setDone(boolean done) {
             this.done = done;
         }
         
         public boolean getDone() {
             return this.done;
         }
     }
 }
