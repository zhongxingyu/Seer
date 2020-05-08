 package com.cahoots.eclipse.test.integration.client;
 
 import java.util.Arrays;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.cahoots.eclipse.Activator;
 import com.cahoots.events.ShareDocumentEventListener;
 import com.cahoots.json.receive.OpInsertMessage;
 import com.cahoots.json.receive.ShareDocumentMessage;
 import com.cahoots.json.send.SendOpInsertMessage;
 import com.cahoots.json.send.SendOpReplaceMessage;
 import com.cahoots.json.send.SendShareDocumentMessage;
 import com.cahoots.websocket.CahootsSocket;
 
 public class TestOpService {
 
 	private static CahootsSocket socket;
 	@BeforeClass
 	public static void classSetUp() throws Exception {
 		Activator.initializeCahootsSocket();
 		Activator.connect("admin", "admin", "127.0.0.1:9000");
 		socket = CahootsSocket.getInstance();
 	}
 
 	@Test
 	public void testShareDocument() throws Exception {
 		shareDocument();
 	}
 
 	private ShareDocumentMessage shareDocument() {
 		SendShareDocumentMessage shareDocumentMessage = new SendShareDocumentMessage(
 				"admin", "cahoots-eclipse/README.txt", Arrays.asList("admin"));
 		return socket.sendAndWaitForResponse(shareDocumentMessage,
 				ShareDocumentMessage.class, ShareDocumentEventListener.class);
 	}
 
 	@Test
 	public void testOpInsert() {
 		ShareDocumentMessage document = shareDocument();
 		
 		SendOpInsertMessage op = new SendOpInsertMessage();
 		op.setOpId(Integer.parseInt(document.getOpId()));
 		op.setDocumentId(document.getDocumentId());
 		op.setStart(0);
 		op.setTickStamp(0);
 		op.setContents("Testing insert 123");
 
 		socket.send(op);
 	}
 
 	@Test
 	public void testOpReplace() {
 		ShareDocumentMessage document = shareDocument();
 		
 		SendOpReplaceMessage op = new SendOpReplaceMessage();
		op.setOpId(Integer.parseInt(document.getOpId()));
		op.setDocumentId(document.getDocumentId());
		op.setStart(0);
		op.setTickStamp(0);
		op.setContents("Testing insert 123");
 
 		socket.send(op);
 	}
 
 	@Test
 	public void testOpDelete() {
 		OpInsertMessage message = new OpInsertMessage();
		message.setOpId();
 
 		socket.send(message);
 	}
 
 }
