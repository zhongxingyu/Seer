 package ar.edu.itba.pdc.duta.proxy.operation;
 
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.itba.pdc.duta.http.MessageFactory;
 import ar.edu.itba.pdc.duta.http.model.Connection;
 import ar.edu.itba.pdc.duta.http.model.Message;
 import ar.edu.itba.pdc.duta.http.model.RequestHeader;
 import ar.edu.itba.pdc.duta.http.model.ResponseHeader;
 import ar.edu.itba.pdc.duta.net.Server;
 import ar.edu.itba.pdc.duta.net.buffer.DataBuffer;
 import ar.edu.itba.pdc.duta.proxy.ClientHandler;
 import ar.edu.itba.pdc.duta.proxy.ServerHandler;
 import ar.edu.itba.pdc.duta.proxy.filter.Filter;
 import ar.edu.itba.pdc.duta.proxy.filter.FilterPart;
 import ar.edu.itba.pdc.duta.proxy.filter.http.HttpFilter;
 
 public class Operation {
 
 	private static final Logger logger = Logger.getLogger(Operation.class);
 
 	private ClientHandler clientHandler;
 
 	private List<Filter> filters;
 
 	private boolean closed = false;
 
 	private MessageHandler clientMessageHandler;
 
 	private MessageHandler serverMessageHandler;
 
 	private boolean closeServer = false;
 
 	private boolean closeClient = false;
 
 	private ChannelProxy serverProxy;
 
 	private boolean isHead;
 
 	public Operation(ClientHandler requestChannelHandler) {
 		clientHandler = requestChannelHandler;
 	}
 
 	public DataBuffer setClientHeader(RequestHeader header) {
 
 		closeClient = Connection.checkStatus(header) == Connection.CLOSE;
 
 		buildFilterList(header);
 
 		List<OperationFilter> requestFilters = new ArrayList<OperationFilter>();
 
 		for (Filter filter : filters) {
 			FilterPart requestPart = filter.getRequestPart();
 			if (requestPart != null) {
 				requestFilters.add(new OperationFilter(requestPart, requestPart.checkInterest(header)));
 			}
 		}
 
 		InetSocketAddress address = extractAddress(header);
 		if (address == null) {
 			writeMessage(MessageFactory.build500());
 			return null;
 		}
 
 		logger.debug("Destination address: " + address);
 		
 		isHead = "HEAD".equalsIgnoreCase(header.getMethod());
 
 		serverProxy = new ChannelProxy(address, this);
 		clientMessageHandler = new MessageHandler(header, requestFilters, serverProxy);
 
 		Message res = clientMessageHandler.processHeader(this);
 		if (res != null) {
 			writeMessage(res);
 			return null;
 		}
 
 		return clientMessageHandler.getBuffer();
 	}
 
 	private void writeMessage(Message res) {
 		
 		if (serverMessageHandler != null && serverMessageHandler.wroteBody()) {
 			logger.warn("Wont write filter message since headers were written already");
			close();
 			return;
 		}
 
 		logger.debug("Got a response message from a filter. Headers are: " + res.getHeader());
 
 		try {
 			DataBuffer buffer = new DataBuffer(res.getHeader().toString().getBytes("ascii"));
 			clientHandler.queueOutput(buffer);
 			buffer.release();
 		} catch (UnsupportedEncodingException e) {
 			// If this happens, the world is screwed
 			logger.error("Failed to encode header", e);
 
 			abort();
 			return;
 		}
 
 		clientHandler.queueOutput(res.getBody());
 		close();
 	}
 
 	private InetSocketAddress extractAddress(RequestHeader header) {
 
 		String host = header.getField("Host");
 
 		if (host.contains(":")) {
 			String[] split = host.split(":");
 
 			try {
 				return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
 			} catch (NumberFormatException e) {
 				// Force close!
 				logger.error("Got an invalid port number in the Host header", e);
 				return null;
 			}
 		} else {
 			return new InetSocketAddress(host, 80);
 		}
 	}
 
 	private void buildFilterList(RequestHeader header) {
 		// FIXME: Get this from somewhere else
 		filters = new ArrayList<Filter>();
 		filters.add(new HttpFilter());
 		//filters.add(new L33tFilter());
 		//filters.add(new BlockFilter());
 		//filters.add(new MediaTypeFilter("image/jpeg"));
 		//filters.add(new IPFilter("69.171.247.53"));
 	}
 
 	public void abort() {
 
 		if (serverProxy != null && serverProxy.getChannel() != null) {
 			ServerHandler handler = serverProxy.getChannel();
 
 			handler.setCurrentOperation(null);
 			handler.close();
 		}
 
 		if (clientHandler != null) {
 			clientHandler.operationComplete();
 			clientHandler.close();
 		}
 
 		
 		if (clientMessageHandler != null) {
 			clientMessageHandler.collect();
			clientMessageHandler = null;
 		}
 		
 		if (serverMessageHandler != null) {
 			serverMessageHandler.collect();
			serverMessageHandler = null;
 		}
 	}
 
 	public void close() {
 
 		if (serverProxy != null && serverProxy.getChannel() != null) {
 			
 			serverProxy.getChannel().operationComplete();
 			
 			if (closeServer) {
 				serverProxy.getChannel().close();
 			} else {
 				Server.getConnectionPool().registerConnection(serverProxy.getChannel());
 			}
 		}
 		serverProxy = null;
 
 		if (serverMessageHandler != null && !serverMessageHandler.isMessageComplete()) {
 			Message res = serverMessageHandler.forceCompletion(this);
 			if (res != null) {
 				writeMessage(res);
 			}
 		}
 		serverMessageHandler = null;
 
 		if (clientHandler != null) {
 
 			clientHandler.operationComplete();
 			if (closeClient) {
 				clientHandler.close();
 			}
 
 		}
 		clientHandler = null;
 
 		closed = true;
 	}
 
 	public void addClientBody() {
 		Message res = clientMessageHandler.append(this);
 		if (res != null) {
 			writeMessage(res);
 		}
 	}
 
 	public boolean isClientMessageComplete() {
 		return closed || clientMessageHandler.isMessageComplete();
 	}
 
 	public DataBuffer setServerHeader(ResponseHeader header) {
 		closeServer = Connection.checkStatus(header) == Connection.CLOSE;
 
 		List<OperationFilter> responseFilters = new ArrayList<OperationFilter>();
 
 		for (Filter filter : filters) {
 			FilterPart responsePart = filter.getResponsePart();
 			if (responsePart != null) {
 				responseFilters.add(new OperationFilter(responsePart, responsePart.checkInterest(header)));
 			}
 		}
 
 		serverMessageHandler = new MessageHandler(header, responseFilters, clientHandler, !isHead);
 		Message res = serverMessageHandler.processHeader(this);
 		if (res != null) {
 			writeMessage(res);
 			return null;
 		}
 
 		return serverMessageHandler.getBuffer();
 	}
 
 	public void addServerBody() {
 
 		Message res = serverMessageHandler.append(this);
 		if (res != null) {
 			writeMessage(res);
 		} else if (serverMessageHandler.isMessageComplete()) {
 			close();
 		}
 	}
 
 }
