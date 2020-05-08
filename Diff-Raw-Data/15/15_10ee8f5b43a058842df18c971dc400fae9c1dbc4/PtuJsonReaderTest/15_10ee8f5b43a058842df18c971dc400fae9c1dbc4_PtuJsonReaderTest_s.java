 package ch.cern.atlas.apvs.ptu;
 
import java.util.Iterator;
 import java.util.List;
 
 import org.junit.Test;
 
 import ch.cern.atlas.apvs.domain.Message;
 import ch.cern.atlas.apvs.ptu.server.PtuJsonReader;
 
 public class PtuJsonReaderTest {
 
 	String json = "{"+
 			"\"Sender\":\"PTU_88\",\"Receiver\":\"Broadcast\",\"FrameID\":\"0\",\"Acknowledge\":\"false\",\"Messages\":["+
 			"{\"Type\":\"measurement\",\"Sensor\":\"Humidity\",\"Time\":\"11/9/2012 10:02:25\",\"Method\":\"OneShoot\",\"Value\":\"33.19684099267707\",\"Samplerate\":\"\",\"Unit\":\"ppm\"},"+
 			"{\"Type\":\"measurement\",\"Sensor\":\"Humidity\",\"Time\":\"11/9/2012 10:07:10\",\"Method\":\"OneShoot\",\"Value\":\"35.45927608218701\",\"Samplerate\":\"\",\"Unit\":\"ppm\"}"+
 			"]}";
 	
 	@Test
 	public void test() {
 		List<Message> list = (List<Message>)PtuJsonReader.toJava(json);
		for (Iterator<Message> i = list.iterator(); i.hasNext(); ) {
			System.err.println(i.next());
		}
 	}
 
 }
