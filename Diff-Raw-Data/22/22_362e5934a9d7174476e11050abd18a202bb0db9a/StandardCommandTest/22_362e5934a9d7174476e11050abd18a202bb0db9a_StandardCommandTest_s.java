 package se.z_app.stb.api.zenterio.test;
 
 import android.util.Log;
 import se.z_app.stb.EPG;
 import se.z_app.stb.api.zenterio.StandardCommand;
 import junit.framework.TestCase;
 
 public class StandardCommandTest extends TestCase {
 	public void testGetCurrentChannel(){
		StandardCommand cmd = new StandardCommand("130.236.248.227");
 		assertTrue(cmd.getCurrentChannel().getName() != null);
 		assertTrue(cmd.getCurrentChannel().getUrl() != null);
 	}
 	public void testGetEPG(){
		StandardCommand cmd = new StandardCommand("130.236.248.227");
 		
 		EPG epg = cmd.getEPG();
 		assertTrue(epg != null);
 		assertTrue(epg.iterator().hasNext());
 		assertTrue(epg.iterator().next().getName() != null);
 		assertTrue(epg.iterator().next().iterator().hasNext());
 		
 		
 	}
 }
