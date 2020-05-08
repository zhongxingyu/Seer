 package org.oobium.client.websockets;
 
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.utils.json.JsonUtils.toJson;
 
 import java.net.URI;
 import java.util.Map;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
 
 public class Websocket {
 
 	public enum State { Created, Connecting, Open, Closing, Closed }
 	
 	private final WebsocketHandler handler;
 	private final URI uri;
 
 	State state;
 	
 	private String id;
 	private String group;
 	
 	Websocket(WebsocketHandler handler, URI uri) {
 		this.handler = handler;
 		this.uri = uri;
 		this.state = State.Created;
 	}
 
 	public void addListener(WebsocketListener listener) {
 		if(!handler.listeners.contains(listener)) {
 			handler.listeners.add(listener);
 		}
 	}
 	
 	public ChannelFuture connect() {
 		return handler.connect();
 	}
 
 	public ChannelFuture disconnect() {
 		return handler.disconnect();
 	}
 	
 	public boolean getAutoReconnect() {
 		return handler.autoReconnect;
 	}
 	
 	public String getFullPath() {
     	String path = uri.getPath();
     	if(path.length() == 0) {
     		path = "/";
     	}
     	else if(path.charAt(0) != '/') {
     		path = "/" + path;
     	}
         if (uri.getQuery() != null && uri.getQuery().length() > 0) {
             path = uri.getPath() + "?" + uri.getQuery();
         }
         return path;
 	}
 	
 	public String getGroup() {
 		return group;
 	}
 
 	public String getHost() {
 		return uri.getHost();
 	}
 	
 	public String getId() {
 		return id;
 	}
 	
 	public String getPath() {
 		return uri.getPath();
 	}
 	
 	public int getPort() {
		int port = uri.getPort();
		return (port == -1) ? 80 : port;
 	}
 
 	public State getState() {
 		return state;
 	}
 	
 	public String getURL() {
 		return uri.toString();
 	}
 	
 	public ChannelFuture register(Map<?,?> map) {
 		if(map.containsKey("id")) {
 			this.id = coerce(map.get("id"), String.class);
 		}
 		if(map.containsKey("group")) {
 			this.group = coerce(map.get("group"), String.class);
 		}
 		String json = "registration:" + toJson(map);
 		WebSocketFrame frame = new DefaultWebSocketFrame(json);
 		return send(frame);
 	}
 	
 	public ChannelFuture register(String id) {
 		this.id = id;
 		String json = "registration:{id:'" + id + "'}";
 		WebSocketFrame frame = new DefaultWebSocketFrame(json);
 		return send(frame);
 	}
 	
 	public ChannelFuture register(String id, String group) {
 		this.id = id;
 		this.group = group;
 		String json = "registration:{id:'" + id + "',group:'" + group + "'}";
 		WebSocketFrame frame = new DefaultWebSocketFrame(json);
 		return send(frame);
 	}
 	
 	public ChannelFuture registerGroup(String group) {
 		this.group = group;
 		String json = "registration:{group:'" + group + "'}";
 		WebSocketFrame frame = new DefaultWebSocketFrame(json);
 		return send(frame);
 	}
 	
 	public boolean removeListener(WebsocketListener listener) {
 		return handler.listeners.remove(listener);
 	}
 	
 	public ChannelFuture send(int type, byte[] binaryData) {
 		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(binaryData);
 		return send(new DefaultWebSocketFrame(type, buffer));
 	}
 	
 	public ChannelFuture send(int type, ChannelBuffer binaryData) {
 		return send(new DefaultWebSocketFrame(type, binaryData));
 	}
 	
 	public ChannelFuture send(String textData) {
 		return send(new DefaultWebSocketFrame(textData));
 	}
 	
 	public ChannelFuture send(WebSocketFrame frame) {
 		return handler.channel.write(frame);
 	}
 	
 	public void setAutoReconnect(boolean autoReconnect) {
 		handler.autoReconnect = autoReconnect;
 		if(autoReconnect && state != State.Open) {
 			connect();
 		}
 	}
 	
 	@Override
 	public String toString() {
 		if(id == null && group == null) {
 			return getClass().getSimpleName();
 		}
 		if(id != null) {
 			return getClass().getSimpleName() + "{id:'" + id + "'}";
 		}
 		if(group != null) {
 			return getClass().getSimpleName() + "{group:'" + group + "'}";
 		}
 		return getClass().getSimpleName() + "{id:'" + id + "',group:'" + group + "'}";
 	}
 
 }
