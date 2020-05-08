 package org.hbird.transport.protocols.kiss;
 
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.service.TransportMetadata;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
 import org.apache.mina.filter.codec.ProtocolDecoderOutput;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class KissFrameDecoderTest {
 
 	private final CumulativeProtocolDecoder decoder = new KissFrameDecoder();
 
 	@Mock
 	private ProtocolDecoderOutput mockOut;
 
 	@Mock
 	private IoSession mockSession;
 
 	@Mock
 	private TransportMetadata mockTransportMetadata;
 
 	@Before
 	public void setUp() throws Exception {
		when(mockSession.getTransportMetadata()).thenReturn(mockTransportMetadata);
		when(mockTransportMetadata.hasFragmentation()).thenReturn(false);
 	}
 
 	@Test
 	public void testKissFrameDecode() throws Exception {
		byte[] testInput = new byte[] { (byte) 0xC0, 0x00, 0x08, 0x1F, (byte) 0xC0 };
		byte[] expectedOutput = new byte[] { 0x08, 0x1F };
 
 		decoder.decode(mockSession, IoBuffer.wrap(testInput), mockOut);
 
		verify(mockOut).write(expectedOutput);
	}
 }
