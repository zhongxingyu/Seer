 package name.reerink.musicserver;
 
 import static org.junit.Assert.*;
 import name.reerink.musicserver.misc.ServiceLocator;
 
 import org.junit.Test;
 
 
 public class MusicServerTest {
 
 	@Test
 	public void testStart(){
 		MusicServer musicServer = (MusicServer) ServiceLocator.getService("MusicServer", "musicServer");
 		
 		assertNotNull(musicServer);
 		assertEquals(0, musicServer.getMusicService().getArtistCount());
 	}
 }
