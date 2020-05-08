 package se.z_app.stb.api.zenterio.test;
 
 import android.util.Log;
import se.z_app.stb.Channel;
 import se.z_app.stb.EPG;
 import se.z_app.stb.api.zenterio.StandardCommand;
 import junit.framework.TestCase;
 
 public class StandardCommandTest extends TestCase {
 	public void testGetCurrentChannel(){
		StandardCommand cmd = new StandardCommand("130.236.248.226");
 		assertTrue(cmd.getCurrentChannel().getName() != null);
 		assertTrue(cmd.getCurrentChannel().getUrl() != null);
 	}
 	public void testGetEPG(){
 		
		
		StandardCommand cmd = new StandardCommand("130.236.248.226");
		
		long time = System.currentTimeMillis();
 		EPG epg = cmd.getEPG();
		Log.i("ZmoteTestLog", "Total Featching and parsing EPG: " + (System.currentTimeMillis() - time) + "ms");
		
 		assertTrue(epg != null);
 		assertTrue(epg.iterator().hasNext());
 		assertTrue(epg.iterator().next().getName() != null);
 		assertTrue(epg.iterator().next().iterator().hasNext());
		assertTrue(epg.iterator().next().iterator().next().getStart() != null);
		
		Channel channel = epg.getChannel(4);
		assertTrue(channel.getNr() == 4);
		
		channel = epg.getChannel(10);
		assertTrue(channel.getNr() == 10);
 		
 		
 	}
 }
