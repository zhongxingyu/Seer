 package ch.cern.atlas.apvs.ptu;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import ch.cern.atlas.apvs.domain.Message;
 import ch.cern.atlas.apvs.domain.Packet;
 import ch.cern.atlas.apvs.ptu.server.Humidity;
 import ch.cern.atlas.apvs.ptu.server.PtuJsonReader;
 import ch.cern.atlas.apvs.ptu.server.PtuJsonWriter;
 import ch.cern.atlas.apvs.ptu.server.PtuServerConstants;
 
 public class PtuJsonTest {
 
 	String json = "{"
 			+ "\"Sender\":\"PTU_88\",\"Receiver\":\"Broadcast\",\"FrameID\":\"0\",\"Acknowledge\":\"False\",\"Messages\":["
 			+ "{\"Type\":\"Measurement\",\"Sensor\":\"Humidity\",\"Time\":\"11/09/2012 10:02:25\",\"Method\":\"OneShoot\",\"Value\":\"33.19684099267707\",\"SamplingRate\":\"10000\",\"Unit\":\"ppm\",\"DownThreshold\":\"33.0\",\"UpThreshold\":\"35.7\"},"
 			+ "{\"Type\":\"Measurement\",\"Sensor\":\"Humidity\",\"Time\":\"11/09/2012 10:07:10\",\"Method\":\"OneShoot\",\"Value\":\"35.45927608218701\",\"SamplingRate\":\"15000\",\"Unit\":\"ppm\",\"DownThreshold\":\"33.0\",\"UpThreshold\":\"35.7\"}"
 			+ "]}";
 
 	String msg0 = "Measurement(PTU_88): name=Humidity value=33.19684099267707 unit=ppm sampling rate=10000 date: Tue Sep 11 10:02:25 CEST 2012";
 	String msg1 = "Measurement(PTU_88): name=Humidity value=35.45927608218701 unit=ppm sampling rate=15000 date: Tue Sep 11 10:07:10 CEST 2012";
 
 	String parsedJson = "{"
 			+ "\"Sender\":\"PTU_88\",\"Receiver\":\"Broadcast\",\"FrameID\":\"0\",\"Acknowledge\":\"False\",\"Messages\":["
 			+ "{\"Type\":\"Measurement\",\"Sensor\":\"Humidity\",\"Value\":\"33.19684099267707\",\"DownThreshold\":\"50.0\",\"UpThreshold\":\"130.0\",\"Unit\":\"ppm\",\"Date\":\"04/07/2013 15:42:53\",\"SamplingRate\":\"60000\",\"Connected\":\"True\"},"
 			+ "{\"Type\":\"Measurement\",\"Sensor\":\"Humidity\",\"Value\":\"35.45927608218701\",\"DownThreshold\":\"50.0\",\"UpThreshold\":\"130.0\",\"Unit\":\"ppm\",\"Date\":\"04/07/2013 21:16:13\",\"SamplingRate\":\"60000\",\"Connected\":\"True\"}"
 			+ "]}";
 
 	@Test
 	public void readerTest() throws IOException {
 
 		Packet packet = PtuJsonReader.jsonToJava(json);
 		// System.err.println(packet);
 
 		List<Message> list = packet.getMessages();
 		Assert.assertEquals(2, list.size());
 		// System.err.println(list.get(0).toString());
 		// System.err.println(list.get(1).toString());
 		Assert.assertEquals(msg0, list.get(0).toString());
 		Assert.assertEquals(msg1, list.get(1).toString());
 	}
 
 	@Test
 	public void streamReaderTest() throws IOException {
 		ByteArrayInputStream ba = new ByteArrayInputStream(
 				(json + json).getBytes("UTF-8"));
 		System.err.println("Len "+json.length());
 		PtuJsonReader jr = new PtuJsonReader(ba, true);
 		Packet packet1 = (Packet) jr.readObject();
 //		System.err.println(packet1);
 		Assert.assertEquals("PTU_88", packet1.getSender());
 		Packet packet2 = (Packet) jr.readObject();
 //		System.err.println(packet2);
		Assert.assertEquals("PTU_88", packet2.getSender());
 		ba.close();
 		jr.close();
 	}
 
 	@Test
 	public void writerTest() throws ParseException {
 		Packet packet = new Packet("PTU_88", "Broadcast", 0, false);
 		packet.addMessage(new Humidity("PTU_88", 33.19684099267707,
 				PtuServerConstants.dateFormat.parse("04/07/2013 15:42:53")));
 		packet.addMessage(new Humidity("PTU_88", 35.45927608218701,
 				PtuServerConstants.dateFormat.parse("04/07/2013 21:16:13")));
 
 		String output = PtuJsonWriter.toJson(packet);
 		// System.err.println(output);
 		Assert.assertEquals(parsedJson, output);
 	}
 }
