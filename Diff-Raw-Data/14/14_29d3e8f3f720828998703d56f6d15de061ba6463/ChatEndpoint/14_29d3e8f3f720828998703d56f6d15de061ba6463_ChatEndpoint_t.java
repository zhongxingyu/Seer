 package iw7i.messages.chat;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.websocket.EncodeException;
 import javax.websocket.OnClose;
import javax.websocket.OnError;
 import javax.websocket.OnMessage;
 import javax.websocket.OnOpen;
 import javax.websocket.Session;
import javax.websocket.server.PathParam;
 import javax.websocket.server.ServerEndpoint;
 
 @ServerEndpoint(value = "/chat/{room}", encoders = ChatMessageEncoder.class, decoders = ChatMessageDecoder.class)
 public class ChatEndpoint {
 	private final Logger log = Logger.getLogger(getClass().getName());
	@OnError
	public void error(final Session session, final Throwable err) {
		log.log(Level.SEVERE, "fallo crítico", err);
	}

	
 	@OnOpen
 	public void open(final Session session) {
 	}
 
 	@OnClose
 	public void onClose(final Session session) {
		log.info("close");
 	}
 
 	@OnMessage
 	public void onMessage(final Session session, final ChatMessage chatMessage) {
		log.info("message "+chatMessage);
 		try {
 				session.getBasicRemote().sendObject(chatMessage);
 		} catch (IOException | EncodeException e) {
 			log.log(Level.WARNING, "onMessage failed", e);
 		}
 	}
 }
