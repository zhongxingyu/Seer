 package com.cahoots.eclipse.test.integration.client;
 
 import java.util.Arrays;
 import java.util.concurrent.Future;
 
 import org.eclipse.jetty.websocket.WebSocketClientFactory;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.cahoots.connection.CahootsConnection;
 import com.cahoots.connection.http.tools.CahootsHttpClient;
 import com.cahoots.connection.websocket.CahootsSocket;
 import com.cahoots.eclipse.indigo.widget.TextEditorTools;
 import com.cahoots.eclipse.op.OpSynchronizedClock;
import com.cahoots.eclipse.op.OpTransformation;
 import com.cahoots.events.OpDeleteEventListener;
 import com.cahoots.events.OpInsertEventListener;
 import com.cahoots.events.OpReplaceEventListener;
 import com.cahoots.events.ShareDocumentEventListener;
 import com.cahoots.json.receive.OpDeleteMessage;
 import com.cahoots.json.receive.OpInsertMessage;
 import com.cahoots.json.receive.OpReplaceMessage;
 import com.cahoots.json.receive.ShareDocumentMessage;
 import com.cahoots.json.send.SendOpDeleteMessage;
 import com.cahoots.json.send.SendOpInsertMessage;
 import com.cahoots.json.send.SendOpReplaceMessage;
 import com.cahoots.json.send.SendShareDocumentMessage;
 
 public class TestOpService {
 
 	private static CahootsSocket socket;
 	private static CahootsConnection cahootsConnection;
 
 	@BeforeClass
 	public static void classSetUp() throws Exception {
 		cahootsConnection = new CahootsConnection();
 		socket = new CahootsSocket(cahootsConnection,
 				new WebSocketClientFactory(), new TextEditorTools());
 		socket.connect("admin", "admin", "127.0.0.1:9000");
 	}
 
 	@Test
 	public void testShareDocument() throws Exception {
 		shareDocument();
 	}
 
 	private ShareDocumentMessage shareDocument() {
 		final SendShareDocumentMessage shareDocumentMessage = new SendShareDocumentMessage(
 				"admin", "cahoots-eclipse/README.txt", Arrays.asList("admin"));
 		return socket.sendAndWaitForResponse(shareDocumentMessage,
 				ShareDocumentMessage.class, ShareDocumentEventListener.class);
 	}
 
 	@Test
 	public void testOpInsert() {
 		final ShareDocumentMessage document = shareDocument();
 
 		final SendOpInsertMessage op = new SendOpInsertMessage();
 		op.setOpId(document.getOpId());
 		op.setDocumentId(document.getDocumentId());
 		op.setStart(0);
 		op.setTickStamp(0L);
 		op.setContent("Testing insert 123");
 
 		socket.sendAndWaitForResponse(op, OpInsertMessage.class,
 				OpInsertEventListener.class);
 	}
 
 	@Test
 	public void testOpReplace() {
 		final ShareDocumentMessage document = shareDocument();
 
 		final String content = "Testing replace 123";
 
 		final SendOpReplaceMessage op = new SendOpReplaceMessage();
 		op.setOpId(document.getOpId());
 		op.setStart(0);
 		op.setEnd(content.length());
 		op.setTickStamp(0L);
 		op.setContent(content);
 
 		socket.sendAndWaitForResponse(op, OpReplaceMessage.class,
 				OpReplaceEventListener.class);
 	}
 
 	@Test
 	public void testOpDelete() {
 		final ShareDocumentMessage document = shareDocument();
 
 		final SendOpDeleteMessage op = new SendOpDeleteMessage();
 		op.setOpId(document.getOpId());
 		op.setStart(0);
 		op.setEnd(1);
 		op.setTickStamp(0L);
 
 		socket.sendAndWaitForResponse(op, OpDeleteMessage.class,
 				OpDeleteEventListener.class);
 	}
 
 	@Test
 	public void testSynchronizedClock() throws Exception {
 		final ShareDocumentMessage document = shareDocument();
 		
 		final Future<OpSynchronizedClock> clockFuture = OpSynchronizedClock.fromConnection(new CahootsHttpClient(), cahootsConnection);
 		final OpSynchronizedClock clock = clockFuture.get();
 		
 		System.out.println(clock);
 	}
 }
