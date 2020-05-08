 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.catalina.websocket.MessageInbound;
 import org.apache.catalina.websocket.StreamInbound;
 import org.apache.catalina.websocket.WebSocketServlet;
 import org.apache.catalina.websocket.WsOutbound;
 
 public class WSserver extends WebSocketServlet
 {
 	 	private final static Set<EchoMessageInbound> connections = new CopyOnWriteArraySet<EchoMessageInbound>();
	 	//private static EchoMessageInbound currentConnect = new EchoMessageInbound();
 	 	
 	    @Override
 	    protected StreamInbound createWebSocketInbound(String subProtocol,
 	            HttpServletRequest request) {
 	        return new EchoMessageInbound();
 	    }
 
 	    private static final class EchoMessageInbound extends MessageInbound {
	    	 
	    	 public StreamInbound currentConnect;
	    	
 	    	 @Override
 	         protected void onOpen(WsOutbound outbound) {
 	             connections.add(this);
 	             currentConnect = this;
 	         }
 
 	         @Override
 	         protected void onClose(int status) {
 	             connections.remove(this);
 	         }
 	        @Override
 	        protected void onBinaryMessage(ByteBuffer message) throws IOException {
 	        	
 	            getWsOutbound().writeBinaryMessage(message);
 	            
 	        }
 
 	        @Override
 	        protected void onTextMessage(CharBuffer message) throws IOException {
 	        	
 	            //getWsOutbound().writeTextMessage(message);
 	            broadcast(message.toString());
 	        }
 	        
 	        private void broadcast(String messageAll) {
 	            for (EchoMessageInbound connection : connections) {
 	                try {
 	                    CharBuffer buffer = CharBuffer.wrap(messageAll);
 	                    
	                    if(connection != currentConnect ) //отсылаем всем кроме самого себя
 	                    {
 	                    	connection.getWsOutbound().writeTextMessage(buffer);
 	                    }
 	                    
 	                } catch (IOException ex) {
 	                    
 	                }
 	            }
 	        }
 	    }
 	}
