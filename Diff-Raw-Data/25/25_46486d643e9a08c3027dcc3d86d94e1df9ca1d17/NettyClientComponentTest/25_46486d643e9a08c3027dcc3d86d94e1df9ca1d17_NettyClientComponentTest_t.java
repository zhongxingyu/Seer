 package org.hbird.camel.nettyclient;
 
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.apache.camel.builder.RouteBuilder;
 import org.apache.camel.component.mock.MockEndpoint;
 import org.apache.camel.test.junit4.CamelTestSupport;
 import org.junit.Test;
 
 public class NettyClientComponentTest extends CamelTestSupport {
 
 	private ServerSocket serverSocket;
	private int testPort = 0;
 
 	@Override
 	public void doPreSetup() throws Exception {
		serverSocket = new ServerSocket(testPort);
		testPort = serverSocket.getLocalPort();
		System.out.println("client local port: " + testPort);
 	}
 
 	@Test
 	public void testNettyClient() throws Exception {
 		final Socket clientSocket = serverSocket.accept();
 
 		final byte[] rawOut = DataGenerator.createRawTestData();
 		clientSocket.getOutputStream().write(rawOut, 0, 9);
 
 		final MockEndpoint mock = getMockEndpoint("mock:result");
 		mock.expectedMinimumMessageCount(1);
 		mock.expectedBodiesReceived(rawOut);
 		assertMockEndpointsSatisfied();
 
 		// Extra asserts - really checking!
 		final Object msgBody = mock.getExchanges().get(0).getIn().getBody();
 		final byte[] receivedBytes = (byte[]) msgBody;
 
 		assertEquals(rawOut.length, receivedBytes.length);
 
 		assertEquals(DataGenerator.BYTE, receivedBytes[0]);
 
 		//@formatter:off
 		final int oneReceived = receivedBytes[1] & DataGenerator.BYTE_INT_MASK +
 				(receivedBytes[2] & DataGenerator.BYTE_INT_MASK << 8) +
 				(receivedBytes[3] & DataGenerator.BYTE_INT_MASK << 16) +
 				(receivedBytes[4] & DataGenerator.BYTE_INT_MASK << 24);
 		assertEquals(DataGenerator.INT_ONE, oneReceived);
 
 		final int twoReceived = receivedBytes[5] & DataGenerator.BYTE_INT_MASK +
 				(receivedBytes[6] & DataGenerator.BYTE_INT_MASK << 8) +
 				(receivedBytes[7] & DataGenerator.BYTE_INT_MASK << 16) +
 				(receivedBytes[8] & DataGenerator.BYTE_INT_MASK << 24);
 		assertEquals(DataGenerator.INT_TWO, twoReceived);
 		//@formatter:on
 
 	}
 
 	@Override
 	protected RouteBuilder createRouteBuilder() throws Exception {
 		return new RouteBuilder() {
 			@Override
 			public void configure() {
				from("nettyclient:tcp://localhost:" + testPort).to("mock:result");
 			}
 		};
 	}
 }
