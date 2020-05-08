 package org.hbird.transport.protocols.ccsds.transferframe;
 
 import org.apache.camel.EndpointInject;
 import org.apache.camel.Message;
 import org.apache.camel.Produce;
 import org.apache.camel.ProducerTemplate;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.junit4.CamelTestSupport;
import org.hbird.transport.protocols.ccsds.transferframe.exceptions.InvalidSpacecraftIdException;
import org.hbird.transport.protocols.ccsds.transferframe.exceptions.InvalidVirtualChannelIdException;
 import org.junit.Test;
 
 public class CcsdsFrameDecoderTest extends CamelTestSupport {
 	
 	CcsdsFrameEncoder frameEncoder = new CcsdsFrameEncoder(20);
 	
 	CcsdsFrameDecoder frameDecoder = new CcsdsFrameDecoder(20, false, false);
 
 	@Produce(uri = "direct:start")
 	protected ProducerTemplate template;
 
 	@EndpointInject(uri = "mock:result")
 	protected MockEndpoint resultEndpoint;
 	
 
 	@Test
 	public void camelMessageProcessing() throws InterruptedException, Exception {
 		
 		assertNotNull(template);
 		assertNotNull(resultEndpoint);
 		byte[] encodedFrame = new byte[20];
		try {
			encodedFrame = frameEncoder.encodeFrames(1023, 7, new byte[0]).get(0);
		} catch (InvalidVirtualChannelIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidSpacecraftIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		template.sendBody(encodedFrame);
 		
 		resultEndpoint.setExpectedMessageCount(1);
 		resultEndpoint.assertIsSatisfied();
 		
 		Message message = resultEndpoint.getExchanges().get(0).getIn();
 		
		assertEquals(1023, message.getHeader("SpacecraftId"));
		assertEquals(7, message.getHeader("VirtualChannelId"));
 		
 	}
 
 	@Override
 	protected RouteBuilder createRouteBuilder() throws Exception {
 		return new RouteBuilder() {
 			@Override
 			public void configure() throws Exception {
 				from("direct:start").bean(frameDecoder).to("mock:result");
 			}
 		};
 	}
 
 }
