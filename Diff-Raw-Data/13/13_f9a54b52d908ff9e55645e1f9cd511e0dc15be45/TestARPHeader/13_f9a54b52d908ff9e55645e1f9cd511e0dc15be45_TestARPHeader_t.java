 
 package test.com.zanoccio.packetkit;
 
 import static com.zanoccio.packetkit.PacketUtilities.assertPacketEquals;
 
 import org.junit.Test;
 
 import com.zanoccio.packetkit.IP4Address;
 import com.zanoccio.packetkit.MACAddress;
 import com.zanoccio.packetkit.exceptions.PacketKitException;
 import com.zanoccio.packetkit.headers.ARPHardwareType;
 import com.zanoccio.packetkit.headers.ARPHeader;
 import com.zanoccio.packetkit.headers.ARPProtocolType;
 import com.zanoccio.packetkit.mock.MockNetworkInterface;
 
 public class TestARPHeader {
 
 	@Test
 	public void construct() throws PacketKitException {
 		ARPHeader arp = new ARPHeader();
 
 		arp.setHardwareType(ARPHardwareType.ETHERNET);
 		arp.setProtocolType(ARPProtocolType.IP4);
 		arp.setTargetMAC(MACAddress.BROADCAST);
 		arp.setTargetIP(IP4Address.EMPTY);
 
 		arp.associate(new MockNetworkInterface());
		assertPacketEquals("0000  00 01 08 00 06 04 00 01  de ad de ad be ef 12 34\n"
 		        + "0010  56 78 ff ff ff ff ff ff  00 00 00 00", arp);
 	}
}
