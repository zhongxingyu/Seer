 /**
  *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  * 
  * File: org.bgp4j.netty.protocol.BGPv4CodecTest.java 
  */
 package org.bgp4j.netty.protocol;
 
 import java.net.Inet4Address;
 
 import junit.framework.Assert;
 
 import org.bgp4j.netty.BGPv4Constants.AddressFamily;
 import org.bgp4j.netty.BGPv4Constants.SubsequentAddressFamily;
 import org.bgp4j.netty.NetworkLayerReachabilityInformation;
 import org.bgp4j.netty.protocol.open.AutonomousSystem4Capability;
 import org.bgp4j.netty.protocol.open.Capability;
 import org.bgp4j.netty.protocol.open.MultiProtocolCapability;
 import org.bgp4j.netty.protocol.open.OpenPacket;
 import org.bgp4j.netty.protocol.open.RouteRefreshCapability;
 import org.bgp4j.netty.protocol.open.UnknownCapability;
 import org.bgp4j.netty.protocol.update.ASPathAttribute;
 import org.bgp4j.netty.protocol.update.LocalPrefPathAttribute;
 import org.bgp4j.netty.protocol.update.MultiExitDiscPathAttribute;
 import org.bgp4j.netty.protocol.update.MultiProtocolReachableNLRI;
 import org.bgp4j.netty.protocol.update.MultiProtocolUnreachableNLRI;
 import org.bgp4j.netty.protocol.update.NextHopPathAttribute;
 import org.bgp4j.netty.protocol.update.OriginPathAttribute;
 import org.bgp4j.netty.protocol.update.OriginPathAttribute.Origin;
 import org.bgp4j.netty.protocol.update.UpdatePacket;
 import org.jboss.netty.channel.ChannelHandler;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.Channels;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Rainer Bieniek (Rainer.Bieniek@web.de)
  *
  */
 public class BGPv4CodecTest extends ProtocolPacketTestBase {
 	@Before
 	public void before() {
 		codecOnlyHandler = obtainInstance(MockChannelHandler.class);
 		codecOnlySink = obtainInstance(MockChannelSink.class);;
 		codecOnlyPipeline = Channels.pipeline(new ChannelHandler[] { obtainInstance(BGPv4Codec.class), codecOnlyHandler });
 		codecOnlyChannel = new MockChannel(codecOnlyPipeline, codecOnlySink);		
 
 		completeHandler = obtainInstance(MockChannelHandler.class);
 		completeSink = obtainInstance(MockChannelSink.class);
 		completePipeline = Channels.pipeline(new ChannelHandler[] { 
 				obtainInstance(BGPv4Reframer.class), 
 				obtainInstance(BGPv4Codec.class), 
 				completeHandler });
 		completeChannel = new MockChannel(completePipeline, completeSink);		
 		
 		Assert.assertNotSame(codecOnlyHandler, completeHandler);
 		Assert.assertNotSame(codecOnlySink, completeSink);
 }
 	
 	@After
 	public void after() {
 		codecOnlyHandler = null;
 		codecOnlySink = null;
 		codecOnlyPipeline = null;
 		codecOnlyChannel = null;
 	}
 
 	// channel setup with only the codec in the chain
 	private MockChannelHandler codecOnlyHandler;
 	private MockChannelSink codecOnlySink;
 	private ChannelPipeline codecOnlyPipeline;
 	private MockChannel codecOnlyChannel;
 	
 	// channel setup with reframer and codec in the chain to test complete BGP protocol packets (header + payload)
 	private MockChannelHandler completeHandler;
 	private MockChannelSink completeSink;
 	private ChannelPipeline completePipeline;
 	private MockChannel completeChannel;
 
 	@Test
 	public void testStrippedBasicOpenPacket() throws Exception {
 		codecOnlyPipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(codecOnlyChannel, new byte[] {
 				(byte)0x01, // type code OPEN
 				(byte)0x04, // BGP version 4 
 				(byte)0xfc, (byte)0x00, // Autonomous system 64512 
 				(byte)0x00, (byte)0xb4, // hold time 180 seconds
 				(byte)0xc0, (byte)0xa8, (byte)0x09, (byte)0x01, /// BGP identifier 192.168.9.1 
 				(byte)0x0, // optional parameter length 0 
 		}));
 
 		Assert.assertEquals(0, codecOnlySink.getWaitingEventNumber());
 		Assert.assertEquals(1, codecOnlyHandler.getWaitingEventNumber());
 	
 		OpenPacket open = safeDowncast(safeExtractChannelEvent(codecOnlyHandler.nextEvent()), OpenPacket.class);
 		
 		Assert.assertEquals(4, open.getProtocolVersion());
 		Assert.assertEquals(64512, open.getAutonomousSystem());
 		Assert.assertEquals(180, open.getHoldTime());
 		Assert.assertEquals(0, open.getCapabilities().size());
 		Assert.assertEquals(((192<<24) | (168 << 16) | (9 << 8) | 1), open.getBgpIdentifier());
 	}
 
 	@Test
 	public void testCompleteBasicOpenPacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1d, // length 29 octets
 				(byte)0x01, // type code OPEN
 				(byte)0x04, // BGP version 4 
 				(byte)0xfc, (byte)0x00, // Autonomous system 64512 
 				(byte)0x00, (byte)0xb4, // hold time 180 seconds
 				(byte)0xc0, (byte)0xa8, (byte)0x09, (byte)0x01, /// BGP identifier 192.168.9.1 
 				(byte)0x0, // optional parameter length 0 
 		}));
 
 		Assert.assertEquals(0, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(1, completeHandler.getWaitingEventNumber());
 	
 		OpenPacket open = safeDowncast(safeExtractChannelEvent(completeHandler.nextEvent()), OpenPacket.class);
 		
 		Assert.assertEquals(4, open.getProtocolVersion());
 		Assert.assertEquals(64512, open.getAutonomousSystem());
 		Assert.assertEquals(180, open.getHoldTime());
 		Assert.assertEquals(0, open.getCapabilities().size());
 		Assert.assertEquals(((192<<24) | (168 << 16) | (9 << 8) | 1), open.getBgpIdentifier());
 	}
 
 	@Test
 	public void testStrippedFullOpenPacket() throws Exception {
 		Capability cap;
 		
 		codecOnlyPipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(codecOnlyChannel, new byte[] {
 				(byte)0x01, // type code OPEN
 				(byte)0x04, // BGP version 4 
 				(byte)0xfc, (byte)0x00, // Autonomous system 64512 
 				(byte)0x00, (byte)0xb4, // hold time 180 seconds
 				(byte)0xc0, (byte)0xa8, (byte)0x09, (byte)0x01, /// BGP identifier 192.168.9.1 
 				(byte)0x18, // optional parameter length 
 				(byte)0x02, (byte)0x06, // parameter type 2 (capability), length 6 octets 
 				(byte)0x01, (byte)0x04, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x01, // Multi-Protocol capability (type 1), IPv4, Unicast 
 				(byte)0x02, (byte)0x02, // parameter type 2 (capability), length 2 octets 
 				(byte)0x80, (byte)0x00, // Route-Refresh capability according to Wireshark, length 0 octets
 				(byte)0x02, (byte)0x02, // parameter type 2 (capability), length 2 octets
 				(byte)0x02, (byte)0x00, // Route-Refresh capability, length 0 octets
 				(byte)0x02, (byte)0x06, // parameter type 2 (capability), length 6 octets
 				(byte)0x41,	(byte)0x04, (byte)0x00, (byte)0x00, (byte)0xfc, (byte)0x00 // 4 octet AS capability, AS 64512
 		}));
 
 		Assert.assertEquals(0, codecOnlySink.getWaitingEventNumber());
 		Assert.assertEquals(1, codecOnlyHandler.getWaitingEventNumber());
 	
 		OpenPacket open = safeDowncast(safeExtractChannelEvent(codecOnlyHandler.nextEvent()), OpenPacket.class);
 		
 		Assert.assertEquals(4, open.getProtocolVersion());
 		Assert.assertEquals(64512, open.getAutonomousSystem());
 		Assert.assertEquals(180, open.getHoldTime());
 		Assert.assertEquals(((192<<24) | (168 << 16) | (9 << 8) | 1), open.getBgpIdentifier());
 		Assert.assertEquals(4, open.getCapabilities().size());
 
 		cap = open.getCapabilities().remove(0);
 		Assert.assertEquals(MultiProtocolCapability.class, cap.getClass());
 		Assert.assertEquals(AddressFamily.IPv4, ((MultiProtocolCapability)cap).getAfi());
 		Assert.assertEquals(SubsequentAddressFamily.NLRI_UNICAST_FORWARDING, ((MultiProtocolCapability)cap).getSafi());
 
 		cap = open.getCapabilities().remove(0);
 		Assert.assertEquals(UnknownCapability.class, cap.getClass());
 		Assert.assertEquals(128, ((UnknownCapability)cap).getCapabilityType());
 		
 		cap = open.getCapabilities().remove(0);
 		Assert.assertEquals(RouteRefreshCapability.class, cap.getClass());
 
 		cap = open.getCapabilities().remove(0);
 		Assert.assertEquals(AutonomousSystem4Capability.class, cap.getClass());
 		Assert.assertEquals(64512, ((AutonomousSystem4Capability)cap).getAutonomousSystem());
 	}
 
 	@Test
 	public void testCompleteFullOpenPacket() throws Exception {
 		Capability cap;
 		
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x35, // length 53 octets
 				(byte)0x01, // type code OPEN
 				(byte)0x04, // BGP version 4 
 				(byte)0xfc, (byte)0x00, // Autonomous system 64512 
 				(byte)0x00, (byte)0xb4, // hold time 180 seconds
 				(byte)0xc0, (byte)0xa8, (byte)0x09, (byte)0x01, /// BGP identifier 192.168.9.1 
 				(byte)0x18, // optional parameter length 
 				(byte)0x02, (byte)0x06, // parameter type 2 (capability), length 6 octets 
 				(byte)0x01, (byte)0x04, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x01, // Multi-Protocol capability (type 1), IPv4, Unicast 
 				(byte)0x02, (byte)0x02, // parameter type 2 (capability), length 2 octets 
 				(byte)0x80, (byte)0x00, // Route-Refresh capability according to Wireshark, length 0 octets
 				(byte)0x02, (byte)0x02, // parameter type 2 (capability), length 2 octets
 				(byte)0x02, (byte)0x00, // Route-Refresh capability, length 0 octets
 				(byte)0x02, (byte)0x06, // parameter type 2 (capability), length 6 octets
 				(byte)0x41,	(byte)0x04, (byte)0x00, (byte)0x00, (byte)0xfc, (byte)0x00 // 4 octet AS capability, AS 64512
 		}));
 
 		Assert.assertEquals(0, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(1, completeHandler.getWaitingEventNumber());
 	
 		OpenPacket open = safeDowncast(safeExtractChannelEvent(completeHandler.nextEvent()), OpenPacket.class);
 		
 		Assert.assertEquals(4, open.getProtocolVersion());
 		Assert.assertEquals(64512, open.getAutonomousSystem());
 		Assert.assertEquals(180, open.getHoldTime());
 		Assert.assertEquals(((192<<24) | (168 << 16) | (9 << 8) | 1), open.getBgpIdentifier());
 		Assert.assertEquals(4, open.getCapabilities().size());
 
 		cap = open.getCapabilities().remove(0);
 		Assert.assertEquals(MultiProtocolCapability.class, cap.getClass());
 		Assert.assertEquals(AddressFamily.IPv4, ((MultiProtocolCapability)cap).getAfi());
 		Assert.assertEquals(SubsequentAddressFamily.NLRI_UNICAST_FORWARDING, ((MultiProtocolCapability)cap).getSafi());
 
 		cap = open.getCapabilities().remove(0);
 		Assert.assertEquals(UnknownCapability.class, cap.getClass());
 		Assert.assertEquals(128, ((UnknownCapability)cap).getCapabilityType());
 		
 		cap = open.getCapabilities().remove(0);
 		Assert.assertEquals(RouteRefreshCapability.class, cap.getClass());
 
 		cap = open.getCapabilities().remove(0);
 		Assert.assertEquals(AutonomousSystem4Capability.class, cap.getClass());
 		Assert.assertEquals(64512, ((AutonomousSystem4Capability)cap).getAutonomousSystem());
 	}
 	
 	@Test
 	public void testStrippedBadBgpVersionOpenPacket() throws Exception {
 		codecOnlyPipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(codecOnlyChannel, new byte[] {
 				(byte)0x01, // type code OPEN
 				(byte)0x05, // BGP version 5 
 				(byte)0xfc, (byte)0x00, // Autonomous system 64512 
 				(byte)0x00, (byte)0xb4, // hold time 180 seconds
 				(byte)0xc0, (byte)0xa8, (byte)0x09, (byte)0x01, /// BGP identifier 192.168.9.1 
 				(byte)0x0, // optional parameter length 0 
 		}));
 
 		Assert.assertEquals(1, codecOnlySink.getWaitingEventNumber());
 		Assert.assertEquals(0, codecOnlyHandler.getWaitingEventNumber());
 	
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x17, // length 23 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x2, // OPEN error message
 				(byte)01, // Unsupported Version Number
 				(byte)0x00, (byte)0x04, // BGP version 4 
 		}, codecOnlySink.nextEvent());		
 	}
 	
 	@Test
 	public void testCompleteBadBgpVersionOpenPacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1d, // length 29 octets
 				(byte)0x01, // type code OPEN
 				(byte)0x05, // BGP version 5 
 				(byte)0xfc, (byte)0x00, // Autonomous system 64512 
 				(byte)0x00, (byte)0xb4, // hold time 180 seconds
 				(byte)0xc0, (byte)0xa8, (byte)0x09, (byte)0x01, /// BGP identifier 192.168.9.1 
 				(byte)0x0, // optional parameter length 0 
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x17, // length 23 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x2, // OPEN error message
 				(byte)01, // Unsupported Version Number
 				(byte)0x00, (byte)0x04, // BGP version 4 
 		}, completeSink.nextEvent());		
 	}
 	
 	@Test
 	public void testStrippedBadBgpIdentifierOpenPacket() throws Exception {
 		codecOnlyPipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(codecOnlyChannel, new byte[] {
 				(byte)0x01, // type code OPEN
 				(byte)0x04, // BGP version 4 
 				(byte)0xfc, (byte)0x00, // Autonomous system 64512 
 				(byte)0x00, (byte)0xb4, // hold time 180 seconds
 				(byte)0xe0, (byte)0x0, (byte)0x0, (byte)0x01, /// BGP identifier 224.0.0.1 (Multicast IP) 
 				(byte)0x0, // optional parameter length 0 
 		}));
 
 		Assert.assertEquals(1, codecOnlySink.getWaitingEventNumber());
 		Assert.assertEquals(0, codecOnlyHandler.getWaitingEventNumber());
 	
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x15, // length 21 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x2, // OPEN error message
 				(byte)0x3, // Bad BGP Identifier
 		}, codecOnlySink.nextEvent());		
 	}
 
 	@Test
 	public void testStrippedUnsupportedOptionalParameterOpenPacket() throws Exception {
 		codecOnlyPipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(codecOnlyChannel, new byte[] {
 				(byte)0x01, // type code OPEN
 				(byte)0x04, // BGP version 4 
 				(byte)0xfc, (byte)0x00, // Autonomous system 64512 
 				(byte)0x00, (byte)0xb4, // hold time 180 seconds
 				(byte)0xc0, (byte)0xa8, (byte)0x09, (byte)0x01, /// BGP identifier 192.168.9.1 
 				(byte)0x2, // optional parameter length 0
 				(byte)0x03, (byte)0x00 // bogus optional parameter type code 3
 		}));
 
 		Assert.assertEquals(1, codecOnlySink.getWaitingEventNumber());
 		Assert.assertEquals(0, codecOnlyHandler.getWaitingEventNumber());
 	
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x15, // length 21 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x2, // OPEN error message
 				(byte)0x4, // Unsupported Optional Parameter
 		}, codecOnlySink.nextEvent());		
 	}
 
 	@Test
 	public void testCompleteUpdatePacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x35, // length 53 octets
 				(byte)0x02, // type code 2 (UPDATE) 
 				(byte)0x00, (byte)0x00, // withdrawn routes length (0 octets)
 				(byte)0x00, (byte)0x1d, // path attributes length (29 octets)
 				(byte)0x40, (byte)0x01, (byte)0x01, (byte)0x02, // Path attribute: ORIGIN INCOMPLETE  
 				(byte)0x50, (byte)0x02, (byte)0x00, (byte)0x00, // Path attribute: AS_PATH emtpy 
 				(byte)0x40, (byte)0x03, (byte)0x04, (byte)0xc0, (byte)0xa8, (byte)0x04, (byte)0x02, // Path attribute: NEXT_HOP 192.168.4.2
 				(byte)0x80, (byte)0x04, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x08, (byte)0x00, // Path attribute: MULT_EXIT_DISC 2048
 				(byte)0x40, (byte)0x05, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x64, // Path attribute: LOCAL_PREF 100
 				(byte)0x00 // NLRI: 0.0.0.0/0	
 		}));
 
 		Assert.assertEquals(0, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(1, completeHandler.getWaitingEventNumber());
 	
 		UpdatePacket packet = safeDowncast(safeExtractChannelEvent(completeHandler.nextEvent()), UpdatePacket.class);
 		
 		Assert.assertEquals(0, packet.getWithdrawnRoutes().size());
 		Assert.assertEquals(5, packet.getPathAttributes().size());
 		Assert.assertEquals(1, packet.getNlris().size());		
 		
 		OriginPathAttribute origin = (OriginPathAttribute)packet.getPathAttributes().remove(0);
 		ASPathAttribute asPath = (ASPathAttribute)packet.getPathAttributes().remove(0);
 		NextHopPathAttribute nextHop = (NextHopPathAttribute)packet.getPathAttributes().remove(0);
 		MultiExitDiscPathAttribute multiExitDisc = (MultiExitDiscPathAttribute)packet.getPathAttributes().remove(0);
 		LocalPrefPathAttribute localPref = (LocalPrefPathAttribute)packet.getPathAttributes().remove(0);
 		NetworkLayerReachabilityInformation nlri = packet.getNlris().remove(0);
 		
 		Assert.assertEquals(Origin.INCOMPLETE, origin.getOrigin());
 		Assert.assertEquals(ASType.AS_NUMBER_2OCTETS, asPath.getAsType());
 		Assert.assertEquals(0, asPath.getPathSegments().size());
 		Assert.assertEquals(Inet4Address.getByAddress(new byte[] {(byte)0xc0, (byte)0xa8, (byte)0x04, (byte)0x02 }), nextHop.getNextHop());
 		Assert.assertEquals(2048, multiExitDisc.getDiscriminator());
 		Assert.assertEquals(100, localPref.getLocalPreference());
 		
 		Assert.assertEquals(0, nlri.getPrefixLength());
 		Assert.assertNull(nlri.getPrefix());
 	}
 	
 	@Test
 	public void testMalformedWithdrawnRoutesNopathAttributeListUpdatePacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x15, // length 21 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x00, // bad withdrawn routes length (2 octets), points to end of packet 
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x15, // length 21 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x01, // Message header error
 				(byte)0x01, // Connection not synchronized
 		}, completeSink.nextEvent());		
 	}
 
 	@Test
 	public void testDecodeMalformedWithdrawnRoutesLengthOnPacketEndUpdatePacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x17, // length 23 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x02, // bad withdrawn routes length (2 octets), points to end of packet 
 				0x00, 0x00, // Total path attributes length  (0 octets)
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x15, // length 21 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x01, // Malformed attribute list
 		}, completeSink.nextEvent());		
 	}
 		
 	@Test
 	public void testDecodeMalformedWithdrawnRoutesLengthOverEndUpdatePacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x17, // length 23 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x04, // bad withdrawn routes length (4 octets), points beyond end of packet 
 				0x00, 0x00, // Total path attributes length  (0 octets)
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x15, // length 21 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x01, // Malformed attribute list
 		}, completeSink.nextEvent());		
 	}
 	
 	@Test
 	public void testDecodeAttributeListTooLongUpdatePacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x17, // length 23 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x00, // withdrawn routes length (0 octets) 
 				0x00, 0x02, // Total path attributes length  (2 octets), points to end of packet
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x15, // length 21 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x01, // Malformed attribute list
 		}, completeSink.nextEvent());		
 	}
 
 	
 	@Test
 	public void testDecodeOriginInvalidPacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1b, // length 27 octets
 				(byte)0x02, // type code UPDATE
 				(byte)0x00, (byte)0x00, // withdrawn routes length (0 octets)
 				(byte)0x00, (byte)0x04, // path attributes length (29 octets)
 				(byte)0x40, (byte)0x01, (byte)0x01, (byte)0x04, // Path attribute: ORIGIN INCOMPLETE  
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x19, // length 25 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x06, // Invalid origin
 				(byte)0x40, (byte)0x01, (byte)0x01, (byte)0x04, // Path attribute: ORIGIN INCOMPLETE  
 		}, completeSink.nextEvent());		
 	}
 
 	@Test
 	public void testDecodeOriginShortPacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1a, // length 26 octets
 				(byte)0x02, // type code UPDATE
 				(byte)0x00, (byte)0x00, // withdrawn routes length (0 octets)
 				(byte)0x00, (byte)0x03, // path attributes length (29 octets)
 				(byte)0x40, (byte)0x01, (byte)0x00, // Path attribute:   
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x18, // length 24 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x05, // Attribute length
 				(byte)0x40, (byte)0x01, (byte)0x00, // Path attribute: ORIGIN INCOMPLETE  
 		}, completeSink.nextEvent());		
 	}
 
 	@Test
 	public void testDecodeASPath4BadPathTypeOneASNumberPacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x21, // length 33 octets
 				(byte)0x02, // type code UPDATE
 				(byte)0x00, (byte)0x00, // withdrawn routes length (0 octets)
 				(byte)0x00, (byte)0x0a, // path attributes length (10 octets)
 				(byte)0x50, (byte)0x11, (byte)0x00, (byte)0x06, // Path attribute: 6 octets AS_PATH  
				0x05, 0x01, 0x00, 0x00, 0x12, 0x34, // Invalid 0x1234 
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x1f, // length 31 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x0b, // Malformed AS Path
				(byte)0x50, (byte)0x11, (byte)0x00, (byte)0x06,  0x05, 0x01, 0x00, 0x00, 0x12, 0x34, 
 		}, completeSink.nextEvent());
 	}
 
 	@Test
 	public void testDecodeASPath4ASSequenceTwoASNumberOneMissingPacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x21, // length 33 octets
 				(byte)0x02, // type code UPDATE
 				(byte)0x00, (byte)0x00, // withdrawn routes length (0 octets)
 				(byte)0x00, (byte)0x0a, // path attributes length (8 octets)
 				(byte)0x50, (byte)0x11, (byte)0x00, (byte)0x06, // Path attribute: 4 octets AS_PATH  
 				0x02, 0x02, 0x00, 0x00, 0x12, 0x34, // Invalid 0x1234 
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x1f, // length 31 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x0b, // Malformed AS Path
 				(byte)0x50, (byte)0x11, (byte)0x00, (byte)0x06,  0x02, 0x02, 0x00, 0x00, 0x12, 0x34, 
 		}, completeSink.nextEvent());		
 	}
 	
 	@Test
 	public void testDecodeASPath2BadPathTypeOneASNumberPacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1f, // length 31 octets
 				(byte)0x02, // type code UPDATE
 				(byte)0x00, (byte)0x00, // withdrawn routes length (0 octets)
 				(byte)0x00, (byte)0x08, // path attributes length (8 octets)
 				(byte)0x50, (byte)0x02, (byte)0x00, (byte)0x04, // Path attribute: 4 octets AS_PATH  
				0x05, 0x01, 0x12, 0x34, // Invalid 0x1234 
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x1d, // length 29 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x0b, // Malformed AS Path
				(byte)0x50, (byte)0x02, (byte)0x00, (byte)0x04, 0x05, 0x01, 0x12, 0x34, 
 		}, completeSink.nextEvent());		
 	}
 
 	@Test
 	public void testDecodeASPath2ASSequenceTwoASNumberOneMissingPacket() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1f, // length 31 octets
 				(byte)0x02, // type code UPDATE
 				(byte)0x00, (byte)0x00, // withdrawn routes length (0 octets)
 				(byte)0x00, (byte)0x08, // path attributes length (8 octets)
 				(byte)0x50, (byte)0x02, (byte)0x00, (byte)0x04, // Path attribute: 4 octets AS_PATH  
 				0x02, 0x02, 0x12, 0x34, // Invalid 0x1234 
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x1d, // length 29 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x0b, // Malformed AS Path
 				(byte)0x50, (byte)0x02, (byte)0x00, (byte)0x04, 0x02, 0x02, 0x12, 0x34, 
 		}, completeSink.nextEvent());		
 	}
 
 	@Test
 	public void testNextHopPacketIpMulticastNextHop() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1e, // length 30 octets
 				(byte)0x02, // type code UPDATE
 				(byte)0x00, (byte)0x00, // withdrawn routes length (0 octets)
 				(byte)0x00, (byte)0x07, // path attributes length (7 octets)
 				(byte)0x40, (byte)0x03, (byte)0x04, (byte)0xe0, (byte)0x00, (byte)0x00, (byte)0x01, // Path attribute: NEXT_HOP 224.0.0.1
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x1c, // length 28 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x08, // Invalid next hop
 				(byte)0x40, (byte)0x03, (byte)0x04, (byte)0xe0, (byte)0x00, (byte)0x00, (byte)0x01,
 		}, completeSink.nextEvent());		
 	}
 	
 	@Test
 	public void testDecodeOneNlri() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1a, // length 26 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x00, // withdrawn routes length (0 octets)
 				0x00, 0x00, // Total path attributes length  (0 octets)
 				0x10, (byte)0xac, 0x10, // NLRI 172.16/16 
 		}));
 
 		Assert.assertEquals(0, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(1, completeHandler.getWaitingEventNumber());
 
 		UpdatePacket packet = safeDowncast(safeExtractChannelEvent(completeHandler.nextEvent()), UpdatePacket.class);
 		NetworkLayerReachabilityInformation nlri;
 		
 		Assert.assertEquals(0, packet.getWithdrawnRoutes().size());
 		Assert.assertEquals(0, packet.getPathAttributes().size());
 		Assert.assertEquals(1, packet.getNlris().size());
 		
 		nlri = packet.getNlris().remove(0);
 
 		Assert.assertEquals(16, nlri.getPrefixLength());
 		assertArraysEquals(new byte[] { (byte)0xac, 0x10} , nlri.getPrefix());
 	}
 	
 	@Test
 	public void testDecodeTwoNlri() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1f, // length 31 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x00, // withdrawn routes length (0 octets)
 				0x00, 0x00, // Total path attributes length  (0 octets)
 				0x10, (byte)0xac, 0x10, // NLRI 172.16/16
 				0x1c, (byte)0xc0, (byte)0xa8, 0x20, 0, // NLRI 192.168.32.0/28
 		}));
 
 		Assert.assertEquals(0, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(1, completeHandler.getWaitingEventNumber());
 
 		UpdatePacket packet = safeDowncast(safeExtractChannelEvent(completeHandler.nextEvent()), UpdatePacket.class);
 		NetworkLayerReachabilityInformation nlri;
 		
 		Assert.assertEquals(0, packet.getWithdrawnRoutes().size());
 		Assert.assertEquals(0, packet.getPathAttributes().size());
 		Assert.assertEquals(2, packet.getNlris().size());
 		
 		nlri = packet.getNlris().remove(0);
 
 		Assert.assertEquals(16, nlri.getPrefixLength());
 		assertArraysEquals(new byte[] { (byte)0xac, 0x10} , nlri.getPrefix());
 
 		nlri = packet.getNlris().remove(0);
 
 		Assert.assertEquals(28, nlri.getPrefixLength());
 		assertArraysEquals(new byte[] { (byte)0xc0, (byte)0xa8, 0x20, 0} , nlri.getPrefix());
 	}
 
 	@Test
 	public void testDecodeBogusNlri() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1e, // length 30 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x00, // withdrawn routes length (0 octets)
 				0x00, 0x00, // Total path attributes length  (0 octets)
 				0x10, (byte)0xac, 0x10, // NLRI 172.16/16
 				0x1c, (byte)0xc0, (byte)0xa8, 0x20,  // NLRI 192.168.32/28 bogus one octet missing
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x15, // length 21 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x0a, // Invalid network field
 		}, completeSink.nextEvent());		
 	}
 	
 	@Test
 	public void testDecodeValidMpReachNlriFourByteNextHopTwoNlri() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x2a, // length 42 octets
 				(byte)0x02, // type code 2 (UPDATE) 
 				0x00, 0x00, // withdrawn routes length (0 octets)
 				0x00, 0x13, // Total path attributes length  (19 octets)
 				(byte)0x80, 0x0e, 0x10, // Path Attribute MP_REACH_NLRI
 				0x00, 0x01, 0x01, // AFI(IPv4) SAFI(UNICAT_ROUTING) 
 				0x04, (byte)0xc0, (byte)0xa8, 0x04, 0x02, // NEXT_HOP 4 octets 192.168.4.2, 
 				0x00, // reserved 
 				0x0c, (byte)0xab, 0x10, //  NLRI 172.16.0.0/12
 				0x14, (byte)0xc0, (byte)0xa8, (byte)0xf0, //  NLRI 192.168.255.0/20
 		}));
 
 		Assert.assertEquals(0, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(1, completeHandler.getWaitingEventNumber());
 	
 		UpdatePacket packet = safeDowncast(safeExtractChannelEvent(completeHandler.nextEvent()), UpdatePacket.class);
 		
 		Assert.assertEquals(0, packet.getWithdrawnRoutes().size());
 		Assert.assertEquals(1, packet.getPathAttributes().size());
 		Assert.assertEquals(0, packet.getNlris().size());		
 		
 		MultiProtocolReachableNLRI mp = (MultiProtocolReachableNLRI)packet.getPathAttributes().remove(0);
 		
 		Assert.assertEquals(AddressFamily.IPv4, mp.getAddressFamily());
 		Assert.assertEquals(SubsequentAddressFamily.NLRI_UNICAST_FORWARDING, mp.getSubsequentAddressFamily());
 		assertArraysEquals(new byte[] { (byte)0xc0, (byte)0xa8, 0x04, 0x02, }, mp.getNextHopAddress());
 		Assert.assertEquals(2, mp.getNlris().size());
 
 		NetworkLayerReachabilityInformation nlri = mp.getNlris().remove(0);
 		
 		Assert.assertEquals(12, nlri.getPrefixLength());
 		assertArraysEquals(new byte[] { (byte)0xab, 0x10, } , nlri.getPrefix());
 		
 		nlri = mp.getNlris().remove(0);
 		Assert.assertEquals(20, nlri.getPrefixLength());
 		assertArraysEquals(new byte[] { (byte)0xc0, (byte)0xa8, (byte)0xf0, } , nlri.getPrefix());
 	}
 
 	@Test
 	public void testDecodeBogusSafiMpReachNlri() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1f, // length 31 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x00, // withdrawn routes length (0 octets)
 				0x00, 0x08, // Total path attributes length  (8 octets)
 				(byte)0x80, 0x0e, 0x05, // Path Attribute MP_REACH_NLRI
 				0x00, (byte)0x01, // AFI(IPv4) 
 				0x04, // Bogus SAFI 
 				0x00, // NEXT_HOP length 0
 				0x00, // reserved
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x1d, // length 29 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x09, // Invalid Optional Attribute Error
 				(byte)0x80, 0x0e, 0x05, // Path Attribute MP_REACH_NLRI
 				0x00, (byte)0x01, // AFI(IPv4) 
 				0x04, // Bogus SAFI 
 				0x00, // NEXT_HOP length 0
 				0x00, // reserved
 		}, completeSink.nextEvent());		
 	}
 	
 	@Test
 	public void testDecodeValidMpUnreachNlriTwoNlri() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x24, // length 36 octets
 				(byte)0x02, // type code 2 (UPDATE) 
 				0x00, 0x00, // withdrawn routes length (0 octets)
 				0x00, 0x0d, // Total path attributes length  (13 octets)
 				(byte)0x80, 0x0f, 0x0a, // Path Attribute MP_REACH_NLRI
 				0x00, 0x01, 0x01, // AFI(IPv4) SAFI(UNICAT_ROUTING) 
 				0x0c, (byte)0xab, 0x10, //  NLRI 172.16.0.0/12
 				0x14, (byte)0xc0, (byte)0xa8, (byte)0xf0, //  NLRI 192.168.255.0/20
 		}));
 
 		Assert.assertEquals(0, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(1, completeHandler.getWaitingEventNumber());
 	
 		UpdatePacket packet = safeDowncast(safeExtractChannelEvent(completeHandler.nextEvent()), UpdatePacket.class);
 		
 		Assert.assertEquals(0, packet.getWithdrawnRoutes().size());
 		Assert.assertEquals(1, packet.getPathAttributes().size());
 		Assert.assertEquals(0, packet.getNlris().size());		
 		
 		MultiProtocolUnreachableNLRI mp = (MultiProtocolUnreachableNLRI)packet.getPathAttributes().remove(0);
 		
 		Assert.assertEquals(AddressFamily.IPv4, mp.getAddressFamily());
 		Assert.assertEquals(SubsequentAddressFamily.NLRI_UNICAST_FORWARDING, mp.getSubsequentAddressFamily());
 		Assert.assertEquals(2, mp.getNlris().size());
 
 		NetworkLayerReachabilityInformation nlri = mp.getNlris().remove(0);
 		
 		Assert.assertEquals(12, nlri.getPrefixLength());
 		assertArraysEquals(new byte[] { (byte)0xab, 0x10, } , nlri.getPrefix());
 		
 		nlri = mp.getNlris().remove(0);
 		Assert.assertEquals(20, nlri.getPrefixLength());
 		assertArraysEquals(new byte[] { (byte)0xc0, (byte)0xa8, (byte)0xf0, } , nlri.getPrefix());
 	}
 
 	@Test
 	public void testDecodeBogusSafiMpUnreachNlri() throws Exception {
 		completePipeline.sendUpstream(buildProtocolPacketUpstreamMessageEvent(completeChannel, new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker 
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				0x00, 0x1d, // length 29 octets
 				(byte)0x02, // type code UPDATE
 				0x00, 0x00, // withdrawn routes length (0 octets)
 				0x00, 0x06, // Total path attributes length  (6 octets)
 				(byte)0x80, 0x0f, 0x03, // Path Attribute MP_REACH_NLRI
 				0x00, (byte)0x01, // AFI(IPv4) 
 				0x04, // Bogus SAFI 
 		}));
 
 		Assert.assertEquals(1, completeSink.getWaitingEventNumber());
 		Assert.assertEquals(0, completeHandler.getWaitingEventNumber());
 
 		assertChannelEventContents(new byte[] {
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, // marker
 				(byte)0x00, (byte)0x1b, // length 27 octets
 				(byte)0x03, // type code NOTIFICATION
 				(byte)0x03, // Update message error
 				(byte)0x09, // Invalid Optional Attribute Error
 				(byte)0x80, 0x0f, 0x03, // Path Attribute MP_REACH_NLRI
 				0x00, (byte)0x01, // AFI(IPv4) 
 				0x04, // Bogus SAFI 
 		}, completeSink.nextEvent());		
 	}
 	
 }
