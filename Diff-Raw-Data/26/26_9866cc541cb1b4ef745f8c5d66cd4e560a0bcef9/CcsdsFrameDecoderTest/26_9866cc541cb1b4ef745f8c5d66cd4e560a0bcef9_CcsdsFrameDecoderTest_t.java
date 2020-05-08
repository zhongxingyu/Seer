 package org.hbird.transport.protocols.ccsds.transferframe;
 
 import org.apache.camel.EndpointInject;
 import org.apache.camel.Message;
 import org.apache.camel.Produce;
 import org.apache.camel.ProducerTemplate;
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.junit4.CamelTestSupport;
import org.hbird.transport.protocols.ccsds.transferframe.data.CcsdsFramePayload;
import org.hbird.transport.protocols.ccsds.transferframe.data.FramePayload;
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

		encodedFrame = frameEncoder.encodeFrames(1023, 7, new byte[0]).get(0);

 		template.sendBody(encodedFrame);
 		
 		resultEndpoint.setExpectedMessageCount(1);
 		resultEndpoint.assertIsSatisfied();
 		
 		Message message = resultEndpoint.getExchanges().get(0).getIn();
 		
		assertIsInstanceOf(FramePayload.class, message.getBody());
		
		CcsdsFramePayload framePayload = message.getBody(CcsdsFramePayload.class);
		
		assertEquals(7, framePayload.virtualChannelId);
		assertEquals(1023, framePayload.spacecraftId);
 		
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
