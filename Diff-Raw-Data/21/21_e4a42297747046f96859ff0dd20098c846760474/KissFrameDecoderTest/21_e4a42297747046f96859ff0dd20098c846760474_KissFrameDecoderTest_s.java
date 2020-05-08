 package org.hbird.transport.protocols.kiss;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
 import org.apache.mina.filter.codec.ProtocolDecoderOutput;
 import org.junit.Before;
import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class KissFrameDecoderTest {
 
 	private final CumulativeProtocolDecoder decoder = new KissFrameDecoder();
 
 	@Mock
 	private ProtocolDecoderOutput mockOut;
 
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void testKissFrameDecode() throws Exception {
 		byte[] testInput = new byte[] { (byte) 0xC0, 0x08, 0x1F, (byte) 0xC0 };
 
		decoder.decode(null, IoBuffer.wrap(testInput), mockOut);
 	}
 
 }
