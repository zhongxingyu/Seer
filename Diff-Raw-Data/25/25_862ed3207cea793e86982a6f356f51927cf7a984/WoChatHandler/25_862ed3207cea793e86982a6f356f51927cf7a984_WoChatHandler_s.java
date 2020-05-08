 package it.uniroma1.dis.wsngroup.wochat.core;
 
 import java.net.InetSocketAddress;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 
 import io.netty.buffer.ByteBuf;
 import io.netty.buffer.Unpooled;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.SimpleChannelInboundHandler;
 import io.netty.channel.group.ChannelGroup;
 import io.netty.channel.group.ChannelMatchers;
 import io.netty.channel.group.DefaultChannelGroup;
 import io.netty.handler.codec.http.DefaultFullHttpResponse;
 import io.netty.handler.codec.http.FullHttpRequest;
 import io.netty.handler.codec.http.FullHttpResponse;
 import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
 import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
 import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
 import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
 import io.netty.handler.codec.http.websocketx.WebSocketFrame;
 import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
 import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
 import io.netty.util.CharsetUtil;
 import io.netty.util.concurrent.GlobalEventExecutor;
 import it.uniroma1.dis.wsngroup.wochat.dbfly.DataOnTheFly;
 import it.uniroma1.dis.wsngroup.wochat.dbfly.User;
 import it.uniroma1.dis.wsngroup.wochat.json.MultiUsersResponse;
 import it.uniroma1.dis.wsngroup.wochat.json.SingleUserRequest;
 import it.uniroma1.dis.wsngroup.wochat.json.SingleUserResponse;
 import it.uniroma1.dis.wsngroup.wochat.logging.LogConnection;
 import it.uniroma1.dis.wsngroup.wochat.logging.LogInteraction;
 import it.uniroma1.dis.wsngroup.wochat.logging.LogMessage;
 import it.uniroma1.dis.wsngroup.wochat.logging.LogUsersList;
 import it.uniroma1.dis.wsngroup.wochat.utils.Constants;
 import it.uniroma1.dis.wsngroup.wochat.utils.Functions;
 
 import static io.netty.handler.codec.http.HttpHeaders.Names.*;
 import static io.netty.handler.codec.http.HttpHeaders.*;
 import static io.netty.handler.codec.http.HttpMethod.*;
 import static io.netty.handler.codec.http.HttpResponseStatus.*;
 import static io.netty.handler.codec.http.HttpVersion.*;
 
 public class WoChatHandler extends SimpleChannelInboundHandler<Object> {
 	private static final Logger logger = Logger.getLogger(WoChatHandler.class.getName());
 	private static final Pattern chatUrlPattern = Pattern.compile("/chat");
 
 	private WebSocketServerHandshaker handshaker;
 	private Gson gson;
 	
 	private DataOnTheFly data;
 	private Map<String, String> usersMap_IpId;
 	private Map<String, String> usersMap_IdIp;
 	private Set<String> usernamesSet;
 	private Map<String, String> usersMap_IdUsername;
 	private Map<String, ChannelGroup> channelsMap_IpChannelGroup;
 	private ChannelGroup broadcastChannelGroup;
 	private long msgCounter;
 	
 	public WoChatHandler(DataOnTheFly data) {
 		gson = new Gson();
 		this.data = data;
 		this.usersMap_IpId = this.data.get_usersMap_IpId();
 		this.usersMap_IdIp = this.data.get_usersMap_IdIp();
 		this.usernamesSet = this.data.get_usernamesSet();
 		this.usersMap_IdUsername = this.data.get_usersMap_IdUsername();
 		this.channelsMap_IpChannelGroup = this.data.get_channelsMap_IpChannelGroup();
 		this.broadcastChannelGroup = this.data.get_broadcastChannelGroup();
 	}
 	
 	public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
 		if (msg instanceof FullHttpRequest) {
 			handleHttpRequest(ctx, (FullHttpRequest) msg);
 		} else if (msg instanceof WebSocketFrame) {
 			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
 		}
 	}
 
 	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
 		ctx.flush();
 	}
 
 	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
 		
 		/** A bad request */
 		if (!req.getDecoderResult().isSuccess()) {
 			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
 			return;
 		}
 
 		/** Allow only GET and POST methods */
 		if (req.getMethod() != GET && req.getMethod() != POST) {
 			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
 			return;
 		}
 		
 		logger.debug("Requested URI: " + req.getUri());
 
 		/** Homepage */
 		if ("/".equals(req.getUri())) {			
 			ByteBuf content = Unpooled.copiedBuffer(Functions.readFile("./web/index.html", CharsetUtil.UTF_8), CharsetUtil.UTF_8);
 			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
 
 			res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
 			setContentLength(res, content.readableBytes());
 
 			sendHttpResponse(ctx, req, res);
 			return;
 		}
 		
 		/** Website content */
 		if(req.getUri().matches(".*\\.(css|js|map)$")) {
 			ByteBuf content = Unpooled.copiedBuffer(Functions.readFile("./web/" + req.getUri(), CharsetUtil.UTF_8), CharsetUtil.UTF_8);
 			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
 			
 			if(req.getUri().matches(".*\\.css$")) {
 				res.headers().set(CONTENT_TYPE, "text/css; charset=UTF-8");
 			} else
 			if(req.getUri().matches(".*\\.js$")) {
 				res.headers().set(CONTENT_TYPE, "text/javascript; charset=UTF-8");
 			}
 			
 			setContentLength(res, content.readableBytes());
 
 			sendHttpResponse(ctx, req, res);
 			return;
 		}
 		
 		if ("/favicon.ico".equals(req.getUri())) {
 			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
 			sendHttpResponse(ctx, req, res);
 			return;
 		}
 		
 		/** New user's connection */
 		if("/newuser".equals(req.getUri())) {
 			ByteBuf receivedContent = req.content();
 			if(receivedContent.isReadable()) {
 				String receivedMsg = receivedContent.toString(CharsetUtil.UTF_8);
 				logger.debug("Request Content: " + receivedMsg);
 				String splitter[] = receivedMsg.split("=");
 				
 				if(splitter.length > 1) {	
 					String username = splitter[1];
 					SingleUserResponse responseJson = new SingleUserResponse();
 					
 					if(!usernamesSet.contains(username)) {
 						responseJson.setResponse(Constants.SUCCESS_CONN);
 						responseJson.setData(new User().setUsername(username));
 						usernamesSet.add(username);
 					} else {
 						responseJson.setResponse(Constants.FAIL_CONN);
 					}
 					
 					ByteBuf sendingContent = Unpooled.copiedBuffer(gson.toJson(responseJson), CharsetUtil.UTF_8);
 					FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, sendingContent);
 					
 					res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
 					setContentLength(res, sendingContent.readableBytes());
 		
 					sendHttpResponse(ctx, req, res);
 					return;
 				}
 			}
 			
 			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
 			return;
 		}
 		
 		
 		/** Web Socket does not match to chat path */
 		Matcher m = chatUrlPattern.matcher(req.getUri());
 		if (!m.matches()) {
 			logger.error("WebSocket does not match to chat path: " + req.getUri());
 			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
 			return;
 		}
 		
 		/** Web socket handshake */
 		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
 		handshaker = wsFactory.newHandshaker(req);
 		if (handshaker == null) {
 			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
 		} else {
 			handshaker.handshake(ctx.channel(), req);
 			logger.debug(ctx.channel().toString());
 		}
 	}
 
 	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
 
 		/** Check for closing frame */
 		if (frame instanceof CloseWebSocketFrame) {
 			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
 			logger.info(ctx.channel().toString() + " was closed.");
 			
 			/** A closed channel is automatically removed from any group*/
 			logger.debug("Number of channels in the broadcast group: " + broadcastChannelGroup.size());
 			manageDisconnections(ctx.channel());
 			return;
 		}
 		
 		if (frame instanceof PingWebSocketFrame) {
 			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
 			return;
 		}
 		
 		if (!(frame instanceof TextWebSocketFrame)) {
 			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
 		}
 		
 		String jsonReq = ((TextWebSocketFrame) frame).text();
 		logger.debug(String.format("%s received %s", ctx.channel(), jsonReq));
 		manageMessages(ctx.channel(), jsonReq);
 	}
 
 	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
 		/** Generate an error page if response getStatus code is not OK (200) */
 		if (res.getStatus().code() != 200) {
 			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
 			res.content().writeBytes(buf);
 			buf.release();
 			setContentLength(res, res.content().readableBytes());
 		}
 
 		/** Send the response and close the connection if necessary */
 		ChannelFuture f = ctx.channel().writeAndFlush(res);
 		if (!isKeepAlive(req) || res.getStatus().code() != 200) {
 			f.addListener(ChannelFutureListener.CLOSE);
 		}
 	}
 
 	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
 		cause.printStackTrace();
 		ctx.close();
 	}
 
 	private static String getWebSocketLocation(FullHttpRequest req) {
 		return "ws://" + req.headers().get(HOST) + Constants.WEBSOCKET_PATH;
 	}
 
 	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
 		messageReceived(ctx, msg);
 	}
 	
 	private void manageMessages(Channel channel, String jsonReq) {
 		logger.debug("Request:" + jsonReq);
 		SingleUserRequest userReq = gson.fromJson(jsonReq, SingleUserRequest.class);
 		
 		/** Message request on the connection status */
 		if(userReq.getRequest().equals(Constants.GET_CONN_STATUS)) {
 			manageConnectionStatus(channel);
 		}
 		
 		else
 		
 		/** Message request to join the chat */
 		if(userReq.getRequest().equals(Constants.JOIN_CHAT)) {
 			User user = userReq.getData();
 			logger.debug("Request to join the chat from: " + user.getUsername());
 			
 			/** Manage users */
 			manageUsers(channel, user.getUsername());
 		}
 		
 		else
 		
 		/** Messages among users */
 		if(userReq.getRequest().equals(Constants.DELIVER_MSG)) {
 			deliverMsg(userReq, channel);
 		}
 	}
 	
 	private void manageConnectionStatus(Channel channel) {
 		String remoteHost = getRemoteHost(channel);
 		SingleUserResponse userResp = new SingleUserResponse();
 		
 		logger.debug("Request of connection status by: " + remoteHost);
 		
 		/** If the user's IP address is already registered, then send user info ... */
 		if(usersMap_IpId.containsKey(remoteHost)) {
 			String id = usersMap_IpId.get(remoteHost);
 			String username = usersMap_IdUsername.get(id);
 			User user = new User();
 			user.setId(id).setUsername(username);
 			userResp.setResponse(Constants.REG_USER_STATUS);
 			userResp.setData(user);
 			logger.debug("Sending response to already registered user: " + id + " - " + username);
 		}
 		
 		/** ... otherwise send new_user_status string */
 		else {
 			userResp.setResponse(Constants.NEW_USER_STATUS);
 			logger.debug("Sending response to the new user");
 		}
 		
 		channel.writeAndFlush(new TextWebSocketFrame(gson.toJson(userResp)));
 	}
 	
 	private void manageUsers(Channel channel, String username) {
 		String remoteHost = getRemoteHost(channel);
 		logger.debug("Remote Host: " + remoteHost);
 		
 		/** Add the channel to the ChannelGroup; it will not add duplicate, like a set */
 		broadcastChannelGroup.add(channel);
 		logger.debug("Number of channels in the broadcast group: " + broadcastChannelGroup.size());
 		
 		/** Adding the current channel to the group of the corresponding IP (e.g., multiple connections from multiple browsers) */
 		if(channelsMap_IpChannelGroup.containsKey(remoteHost)) {
 			channelsMap_IpChannelGroup.get(remoteHost).add(channel);
 		} else {
 			ChannelGroup userChannelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
 			userChannelGroup.add(channel);
 			channelsMap_IpChannelGroup.put(remoteHost, userChannelGroup);
 		}
 		
 		
 		/** Managing the user... */
 		String id = "";
 		
 		/** IP address already registered */
 		if(usersMap_IpId.containsKey(remoteHost)) {
 			logger.debug("IP address already registered");
 			id = usersMap_IpId.get(remoteHost);
 			
 			LogConnection.logConnection("[RE-CONNECTION]\t(" + id + ", " + username + ") connected. #Channels: " + channelsMap_IpChannelGroup.get(remoteHost).size());
 			
 			/** The user reloaded the page or he disconnected and connected again using just one browser. */
 			if(channelsMap_IpChannelGroup.get(remoteHost).size() == 1) {
 				
 				/** Broadcast new user to everyone */
 				broadcastUserStatus(id, username, channel, Constants.USERS_ADD);
 			}
 		}
 		
 		/** New User */
 		else {
 			logger.debug("New IP address");
 			id = String.valueOf(usersMap_IpId.size() + Constants.MIN_USERS_ID_RANGE);
 			usersMap_IpId.put(remoteHost, id);
 			usersMap_IdIp.put(id, remoteHost);
 			usersMap_IdUsername.put(id, username);
 			
 			LogUsersList.logUsersList(remoteHost + Constants.CVS_DELIMITER + id + Constants.CVS_DELIMITER + username);
 			LogConnection.logConnection("[NEW CONNECTION]\t(" + id + ", " + username + ") connected. #Channels: 1");
 			
 			/** Broadcast new user to everyone */
 			broadcastUserStatus(id, username, channel, Constants.USERS_ADD);
 		}
 		
 		/** Creating a JSON object including all users connected */
 		MultiUsersResponse connectedUsersResp = new MultiUsersResponse();
 		List<User> connectedUsersList = new LinkedList<User>();
 		Set<Map.Entry<String, String>> usersSet = usersMap_IdUsername.entrySet();
 		for (Map.Entry<String, String> userIt : usersSet) {
 			User user = new User().setId(userIt.getKey()).setUsername(userIt.getValue());
 			connectedUsersList.add(user);
 	    }
 		connectedUsersResp.setResponse(Constants.USERS_ADD);
 		connectedUsersResp.setData(connectedUsersList);
 		
 		/** Send all users list to the connected user (new or already registered) */
 		channel.writeAndFlush(new TextWebSocketFrame(gson.toJson(connectedUsersResp)));
 	}
 	
 	private void deliverMsg(SingleUserRequest userReq, Channel channel) {
 		String remoteHost = getRemoteHost(channel);
 		
 		User userFrom = userReq.getData();
 		String userIdFrom = userFrom.getId();
 		String userNickFrom = userFrom.getUsername();
 		User userTo = userFrom.getMsg().getReceiver();
 		String userIdTo = userTo.getId();
 		String msgBody = userFrom.getMsg().getBody();
 		
 		/** Check the consistency of connections in DB */
 		checkConsistencyConnectionsInDB(channel, remoteHost, userIdFrom, userNickFrom); //TODO Try cases
 		
 		long currentTimestamp = System.currentTimeMillis() / 1000L;
 		msgCounter = data.getMsgCounter();
 		data.setMsgCounter(msgCounter + 1);
 		String seqHex = "0x" + String.format("%08x", msgCounter & 0xFFFFFFFF);
 		String interaction = "C t=" + currentTimestamp + " ip=0x00000000" + " id=" + userIdFrom + " boot_count=0" + " seq=" + seqHex + "[" + userIdTo + "(0)" + " #1]";
 		LogInteraction.logInteraction(interaction);
 		
 		String message = "(" + currentTimestamp + ") " + "[" + userIdFrom + "," + userIdTo + "] " + "{" + msgBody + "}";
 		LogMessage.logMsg(message);
 		
 		//TODO Try deliver message
 		String receiverIp = usersMap_IdIp.get(userIdTo);
 		ChannelGroup channelsReceiver = channelsMap_IpChannelGroup.get(receiverIp);
 		
 		/** Receiver is disconnected while forwarding a message to him */
 		if(channelsReceiver.size() == 0) {
 			logger.error("Message not delivered. ChannelGroup for IP " + receiverIp + " is empty");
 			ChannelGroup channelsSender = channelsMap_IpChannelGroup.get(remoteHost);
 			SingleUserResponse userResp = new SingleUserResponse();
 			userResp.setResponse(Constants.FAIL_DELIVERING);
 			channelsSender.writeAndFlush(new TextWebSocketFrame(gson.toJson(userResp)));
 			return;
 		}
 		
 		SingleUserResponse userResp = new SingleUserResponse();
 		userResp.setResponse(Constants.DELIVER_MSG);
 		userReq.setData(userFrom);
 		channelsReceiver.writeAndFlush(new TextWebSocketFrame(gson.toJson(userResp)));
 	}
 	
 	private void manageDisconnections(Channel channel) {
 		String remoteHost = getRemoteHost(channel);
 		if(usersMap_IpId.containsKey(remoteHost) && channelsMap_IpChannelGroup.containsKey(remoteHost)) {
 			String id = usersMap_IpId.get(remoteHost);
 			String username = usersMap_IdUsername.get(id);
 			
 			/** If all user's connections are closed then send an update message to delete user from the list */
 			if(channelsMap_IpChannelGroup.get(remoteHost).size() == 0) {
 				
 				LogConnection.logConnection("[DISCONNECTION]\t(" + id + ", " + username + ") disconnected. #Channels: 0");
 				
 				/** Update the users' list deleting the disconnected user */
 				broadcastUserStatus(id, username, channel, Constants.USERS_REM);
 			}
 		}
 	}
 	
 	private void broadcastUserStatus(String id, String username, Channel channel, String status) {
 		/** Creating a JSON object including just the corresponding new user */
 		MultiUsersResponse userResp = new MultiUsersResponse();
 		List<User> userList = new LinkedList<User>();
 		userList.add(new User().setId(id).setUsername(username));
 		userResp.setResponse(status);
 		userResp.setData(userList);
 		
 		/** Broadcast new user to everyone (excepting the new user who will processed later) */
 		broadcastChannelGroup.writeAndFlush(new TextWebSocketFrame(gson.toJson(userResp)), ChannelMatchers.isNot(channel));
 	}
 	
 	private void checkConsistencyConnectionsInDB(Channel channel, String remoteHost, String userIdFrom, String userNickFrom) {		
 		/** IP is changed and not in DB */
 		if(!usersMap_IpId.containsKey(remoteHost)) {
 			logger.warn("IP for user " + userIdFrom + " is changed. Updating...");
 			
 			usersMap_IpId.put(remoteHost, userIdFrom);
 			usersMap_IdIp.put(userIdFrom, remoteHost);
 			ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
 			channelGroup.add(channel);
 			channelsMap_IpChannelGroup.put(remoteHost, channelGroup);
 			
 			LogUsersList.logUsersList(remoteHost + Constants.CVS_DELIMITER + userIdFrom + Constants.CVS_DELIMITER + userNickFrom + Constants.CVS_DELIMITER + "*");
 			LogConnection.logConnection("[REALLOCATION]\t(" + userIdFrom + ", " + userNickFrom + ") connected. #Channels: 1");
 		}
 		
 		else
 		
 		/** The received ID is not equal to the corresponding one registered with that IP */
 		if(!userIdFrom.equals(usersMap_IpId.get(remoteHost))) {
 			logger.warn("IP related to ID " + userIdFrom +  " is changed. Updating...");
 			
 			String oldReferenceId = usersMap_IpId.get(remoteHost);
 			String oldReferenceIp = usersMap_IdIp.get(userIdFrom);
 			usersMap_IdIp.remove(oldReferenceId);
 			usersMap_IpId.remove(oldReferenceIp);
 			channelsMap_IpChannelGroup.remove(oldReferenceIp);
 			
 			usersMap_IdIp.put(userIdFrom, remoteHost);
 			usersMap_IpId.put(remoteHost, userIdFrom);
 			ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
 			channelGroup.add(channel);
 			channelsMap_IpChannelGroup.put(remoteHost, channelGroup);
 			
 			LogUsersList.logUsersList(remoteHost + Constants.CVS_DELIMITER + userIdFrom + Constants.CVS_DELIMITER + userNickFrom + Constants.CVS_DELIMITER + "*");
 			LogConnection.logConnection("[REALLOCATION]\t(" + userIdFrom + ", " + userNickFrom + ") connected. #Channels: 1");
 		}
 	}
 	
 	private String getRemoteHost(Channel channel) {
 		InetSocketAddress sockAddress = (InetSocketAddress) channel.remoteAddress();
 		return sockAddress.getHostString();
 	}
 	
 }
