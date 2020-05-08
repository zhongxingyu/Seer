 package de.tr0llhoehle.buschtrommel.test;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 import de.tr0llhoehle.buschtrommel.models.ByeMessage;
import de.tr0llhoehle.buschtrommel.models.LocalShare;
 import de.tr0llhoehle.buschtrommel.models.RemoteShare;
 import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
 import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
 import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
 import de.tr0llhoehle.buschtrommel.models.Message;
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage.DiscoveryMessageType;
 import de.tr0llhoehle.buschtrommel.network.MessageDeserializer;
 
 public class TestMessageDeserializer {
 
 	@Test
 	public void testDeserializeByeMessage() {
 		assertEquals(new ByeMessage(), MessageDeserializer.Deserialize(new ByeMessage().Serialize()));
 	}
 	
 	@Test
 	public void testDeserializeGetFileListMessage() {
 		assertEquals(new GetFilelistMessage(), MessageDeserializer.Deserialize(new GetFilelistMessage().Serialize()));
 	}
 	
 	@Test
 	public void testDeserializeGetFileMessage() {
 		assertEquals(new GetFileMessage("ABC", 0, 100), MessageDeserializer.Deserialize(new GetFileMessage("ABC", 0, 100).Serialize()));
 		assertEquals(new GetFileMessage("ABC", 12, 101), MessageDeserializer.Deserialize(new GetFileMessage("ABC", 12, 101).Serialize()));
 	}
 	
 	@Test
 	public void testDeserializeGetFileMessage_brokenHash() {
 		assertEquals(new GetFileMessage("0AC", 0, 100), MessageDeserializer.Deserialize(new GetFileMessage("0aC", 0, 100).Serialize()));
 		assertEquals(new GetFileMessage("ABC", 12, 101), MessageDeserializer.Deserialize(new GetFileMessage("0xabc", 12, 101).Serialize()));
 	}
 
 	@Test
 	public void testDeserializeHiMessage() {
 		assertEquals(new PeerDiscoveryMessage(DiscoveryMessageType.HI, "fred", 1232), MessageDeserializer.Deserialize(new PeerDiscoveryMessage(DiscoveryMessageType.HI, "fred", 1232).Serialize()));
 		assertEquals(new PeerDiscoveryMessage(DiscoveryMessageType.HI, "", 1232), MessageDeserializer.Deserialize(new PeerDiscoveryMessage(DiscoveryMessageType.HI, "", 1232).Serialize()));
 		assertEquals(new PeerDiscoveryMessage(DiscoveryMessageType.HI, "易事", 1232), MessageDeserializer.Deserialize(new PeerDiscoveryMessage(DiscoveryMessageType.HI, "易事", 1232).Serialize()));
 	}
 	
 	@Test
 	public void testDeserializeInvalidHiMessage() {
 		assertNull(MessageDeserializer.Deserialize("HI" + Message.FIELD_SEPERATOR + "-123" + Message.FIELD_SEPERATOR + "entensuppe" + Message.MESSAGE_SPERATOR));
 		assertNull(MessageDeserializer.Deserialize("HI" + Message.FIELD_SEPERATOR + "-123" + "entensuppe" + Message.MESSAGE_SPERATOR));
 		assertNull(MessageDeserializer.Deserialize("HI"));
 	}
 	
 	@Test
 	public void testDeserializeYoMessage() {
 		assertEquals(new PeerDiscoveryMessage(DiscoveryMessageType.YO, "fred", 1232), MessageDeserializer.Deserialize(new PeerDiscoveryMessage(DiscoveryMessageType.YO, "fred", 1232).Serialize()));
 		assertEquals(new PeerDiscoveryMessage(DiscoveryMessageType.YO, "", 1232), MessageDeserializer.Deserialize(new PeerDiscoveryMessage(DiscoveryMessageType.YO, "", 1232).Serialize()));
 		assertEquals(new PeerDiscoveryMessage(DiscoveryMessageType.YO, "易事", 1232), MessageDeserializer.Deserialize(new PeerDiscoveryMessage(DiscoveryMessageType.YO, "易事", 1232).Serialize()));
 	}
 	
 	@Test
 	public void testDeserializeInvalidYoMessage() {
 		assertNull(MessageDeserializer.Deserialize("YO" + Message.FIELD_SEPERATOR + "-123" + Message.FIELD_SEPERATOR + "entensuppe" + Message.MESSAGE_SPERATOR));
 		assertNull(MessageDeserializer.Deserialize("YO" + Message.FIELD_SEPERATOR + "-123" + "entensuppe" + Message.MESSAGE_SPERATOR));
 		assertNull(MessageDeserializer.Deserialize("YO"));
 	}
 
 	@Test
 	public void testDeserializeFileMessage() {
		assertEquals(new FileAnnouncementMessage(new LocalShare("ABC", 1024, 2, "dsp", "meta", "")), MessageDeserializer.Deserialize(new FileAnnouncementMessage(new LocalShare("ABC", 1024, 2, "dsp", "meta", "")).Serialize()));
		assertEquals(new FileAnnouncementMessage(new LocalShare("ABC", 1024, 2, "", "", "")), MessageDeserializer.Deserialize(new FileAnnouncementMessage(new LocalShare("ABC", 1024, 2, "", "", "")).Serialize()));
		assertEquals(new FileAnnouncementMessage(new LocalShare("ABC", 1024, RemoteShare.TTL_INFINITY, "foo", "", "")), MessageDeserializer.Deserialize(new FileAnnouncementMessage(new LocalShare("ABC", 1024, RemoteShare.TTL_INFINITY, "foo", "", "")).Serialize()));
		assertEquals(new FileAnnouncementMessage(new LocalShare("ABC", 1111111111111l, 2, "易事", "易事易事易事", "")), MessageDeserializer.Deserialize(new FileAnnouncementMessage(new LocalShare("ABC", 1111111111111l, 2, "易事", "易事易事易事", "")).Serialize()));
 	}
 	
 	@Test
 	public void testDeserializeFileMessage_brokenHashes() {
		assertEquals(new FileAnnouncementMessage(new LocalShare("ABC", 1024, 2, "dsp", "meta", "")), MessageDeserializer.Deserialize(new FileAnnouncementMessage(new LocalShare("Abc", 1024, 2, "dsp", "meta", "")).Serialize()));
		assertEquals(new FileAnnouncementMessage(new LocalShare("ABC", 1024, 2, "dsp", "meta", "")), MessageDeserializer.Deserialize(new FileAnnouncementMessage(new LocalShare("0xabc", 1024, 2, "dsp", "meta", "")).Serialize()));
 	}
 	
 	@Test
 	public void testDeserializeInvalidFileMessage() {
 		assertNull(MessageDeserializer.Deserialize("FILE" + Message.FIELD_SEPERATOR + "HASH" + Message.FIELD_SEPERATOR + "-2" + Message.FIELD_SEPERATOR + "62" + Message.FIELD_SEPERATOR + "dsp" + Message.FIELD_SEPERATOR + "meta" + Message.MESSAGE_SPERATOR));
 		assertNull(MessageDeserializer.Deserialize("FILE" + Message.FIELD_SEPERATOR + "HASH" + Message.FIELD_SEPERATOR + "1" + Message.FIELD_SEPERATOR + "-1" + Message.FIELD_SEPERATOR + "dsp" + Message.FIELD_SEPERATOR + "meta" + Message.MESSAGE_SPERATOR));
 		assertNull(MessageDeserializer.Deserialize("FILE" + Message.FIELD_SEPERATOR + "HASH" + Message.FIELD_SEPERATOR + "1" + Message.FIELD_SEPERATOR + "1" + Message.FIELD_SEPERATOR + "meta" + Message.MESSAGE_SPERATOR));
 		assertNull(MessageDeserializer.Deserialize("FILE" + Message.FIELD_SEPERATOR + "HASH" + Message.FIELD_SEPERATOR + "1" + Message.FIELD_SEPERATOR + "1" + Message.FIELD_SEPERATOR));
 	}
 }
