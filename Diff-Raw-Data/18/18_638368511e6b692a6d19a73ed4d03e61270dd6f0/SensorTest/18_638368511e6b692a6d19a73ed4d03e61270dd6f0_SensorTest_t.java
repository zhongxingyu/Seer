 package dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor;
 
 import static org.junit.Assert.assertEquals;
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.exceptions.NodeInitializationException;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor.sender.UdpPortSender;
 
 public class SensorTest {
 	
 	@Test
 	public void testPropertiesSensor11() {
 		
 		Sensor sensor11 = null;
 		try {
 			sensor11 = SensorUtility.getSensorInstance("sensor11-test.properties", true);
 		} catch (NodeInitializationException e) {
 			Assert.fail();
 		}
 		
 		assertEquals("11", sensor11.getId());
 		assertEquals(10000, sensor11.getSensorComponent().getPeriod());
 		assertEquals(40, sensor11.getSensorComponent().getThreshold());
 		
 //		assertEquals(9911, ((AbstractUdpPortListener) sensor11.getTransceiverComponent().getLeftPortListener()).getServerSocket().getPort());
 //		assertEquals(9912, ((AbstractUdpPortListener) sensor11.getTransceiverComponent().getRightPortListener()).getServerSocket().getPort());
 		
 		assertEquals("0", (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getLeftChannels()[0].getId()));
 		
		assertEquals("127.0.0.1", (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getLeftChannels()[0].getIpAddress()));
 		
 		assertEquals(9901, (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getLeftChannels()[0].getPortNumber()));
 		
 		assertEquals("21", (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[0].getId()));
 		assertEquals("22", (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[1].getId()));
 		assertEquals("23", (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[2].getId()));
 		
		assertEquals("127.0.0.1", (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[0].getIpAddress()));
		assertEquals("127.0.0.1", (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[1].getIpAddress()));
		assertEquals("127.0.0.1", (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[2].getIpAddress()));
 		
 		assertEquals(9921, (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[0].getPortNumber()));
 		assertEquals(9922, (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[1].getPortNumber()));
 		assertEquals(9923, (((UdpPortSender) sensor11.getTransceiverComponent().getPortSender()).getRightChannels()[2].getPortNumber()));
 
 	}
 	
 //	@Test
 //	public void testPropertiesSensor21() {
 //		
 //		Sensor sensor21 = SensorUtility.getSensorInstance("sensor11.properties");
 //		
 //		assertEquals(sensor21.id, 21);
 //		assertEquals(sensor21.period, 10);
 //		assertEquals(sensor21.threshold, 40);
 //		
 //		assertEquals(sensor21.leftPort, 20121);
 //		assertEquals(sensor21.rightPort, 21121);
 //		
 //		assertEquals(sensor21.leftChannelIDs[0], 11);
 //		assertEquals(sensor21.leftChannelIDs[1], 12);
 //		assertEquals(sensor21.leftChannelIDs[2], 13);
 //		
 //		assertEquals(sensor21.leftChannelIPs[0], "192.168.1.111");
 //		assertEquals(sensor21.leftChannelIPs[1], "192.168.1.112");
 //		assertEquals(sensor21.leftChannelIPs[2], "192.168.1.113");
 //		
 //		assertEquals(sensor21.leftChannelPorts[0], 21111);
 //		assertEquals(sensor21.leftChannelPorts[1], 21112);
 //		assertEquals(sensor21.leftChannelPorts[2], 21113);
 //		
 //		assertEquals(sensor21.rightChannelIDs[0], -1);
 //
 //		assertEquals(sensor21.rightChannelIPs[0], "");
 //		
 //		assertEquals(sensor21.rightChannelPorts[0], -1);
 //
 //	}
 
 }
