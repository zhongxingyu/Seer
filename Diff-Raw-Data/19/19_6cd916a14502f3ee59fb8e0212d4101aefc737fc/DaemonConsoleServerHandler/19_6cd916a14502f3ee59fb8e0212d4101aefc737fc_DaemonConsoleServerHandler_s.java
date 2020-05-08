 package com.zzvc.mmps.daemon.console;
 
 import static com.zzvc.mmps.daemon.DaemonConstants.DEFAULT_GROUP;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 
 import sun.misc.Signal;
 
 import com.zzvc.mmps.console.ConsoleException;
 import com.zzvc.mmps.daemon.DaemonMessage;
 
 public class DaemonConsoleServerHandler extends IoHandlerAdapter {
 	private static final int DEFAULT_LOG_BUFFER_SIZE = 20;
     
 	private NioSocketAcceptor acceptor;
 	
     private final Set<IoSession> certifiedSessions = Collections.synchronizedSet(new HashSet<IoSession>());
     private final Map<IoSession, Date> receivedSessions = Collections.synchronizedMap(new HashMap<IoSession, Date>());
     
     private final List<DaemonMessage> cachedLogs = Collections.synchronizedList(new ArrayList<DaemonMessage>());
     private DaemonMessage cachedStatus = null;
 	
     private String consoleTitle;
     
     private int logBufferSize = DEFAULT_LOG_BUFFER_SIZE;
     
     private String group = DEFAULT_GROUP;
     private int port = 0;
 
 	public void init() {
 		ResourceBundle bundle = ResourceBundle.getBundle("daemon");
 		try {
 			group = bundle.getString("console.daemon.group");
 		} catch (Exception e) {
 		}
 		try {
 			port = Integer.parseInt(bundle.getString("console.daemon.port"));
 		} catch (Exception e) {
 			throw new ConsoleException("Console port not set or cannot be read", e);
 		}
 		
 		acceptor = new NioSocketAcceptor();
 		acceptor.setHandler(this);
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
 		acceptor.getSessionConfig().setReuseAddress(true);
 	}
 
 	protected void setConsoleTitle(String consoleTitle) {
 		this.consoleTitle = consoleTitle;
 	}
 
 	public void bind() {
 		try {
 			acceptor.bind(new InetSocketAddress(port));
 		} catch (IOException e) {
 			throw new ConsoleException("Cannot bind console port " + port, e);
 		}
 	}
 	
 	public void close() {
 		broadcastMessage(new DaemonMessage(DaemonMessage.CMD_CLOSE, ""));
 		acceptor.dispose(false);
 	}
 
 	@Override
 	public void sessionCreated(IoSession session) throws Exception {
 		receivedSessions.put(session, new Date());
 	}
 
 	@Override
 	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
 		invalidateSession(session);
 	}
 
 	@Override
 	public void messageReceived(IoSession session, Object message) throws Exception {
 		DaemonMessage consoleMessage = new DaemonMessage(message);
 		String messageCommand = consoleMessage.getCommand();
 		String messageContent = consoleMessage.getContent();
 		
 		if (DaemonMessage.CMD_JOIN.equals(messageCommand)) {
 			certifySession(session, messageContent);
 		} else if (DaemonMessage.CMD_QUIT.equals(messageCommand)) {
 			invalidateSession(session);
 		} else if (DaemonMessage.CMD_TERM.equals(messageCommand)) {
 			terminalApp(session, messageContent);
 		}
 	}
 	
 	public void broadcastLog(DaemonMessage consoleMessage) {
 		broadcastMessage(consoleMessage);
 		cacheLog(consoleMessage);
 	}
 	
 	public void broadcastStatus(DaemonMessage consoleMessage) {
 		broadcastMessage(consoleMessage);
 		cacheStatus(consoleMessage);
 	}
 	
 	private void broadcastMessage(DaemonMessage consoleMessage) {
 		for (IoSession session : certifiedSessions) {
 			session.write(consoleMessage.createMessage());
 		}
 	}
 	
 	private void cacheLog(DaemonMessage consoleMessage) {
 		if (cachedLogs.size() >= logBufferSize) {
 			cachedLogs.remove(0);
 		}
 		cachedLogs.add(consoleMessage);
 	}
 	
 	private void cacheStatus(DaemonMessage consoleMessage) {
 		cachedStatus = consoleMessage;
 	}
 	
 	private void certifySession(IoSession session, String joinGroup) {
 		receivedSessions.remove(session);
 
 		if (group.equals(joinGroup)) {
 			approveSession(session);
 		} else {
 			denySession(session);
 		}
 	}
 	
 	private void invalidateSession(IoSession session) {
 		session.close(true);
 		
 		receivedSessions.remove(session);
 		certifiedSessions.remove(session);
 	}
 	
 	private void terminalApp(IoSession session, String joinGroup) {
 		InetSocketAddress address = (InetSocketAddress) session.getRemoteAddress();
 		if ("127.0.0.1".equals(address.getHostName()) && group.equals(joinGroup)) {
 			certifiedSessions.add(session);
 			Signal.raise(new Signal("TERM"));
 		}
 	}
 	
 	private void approveSession(IoSession session) {
 		certifiedSessions.add(session);
 		session.write(new DaemonMessage(DaemonMessage.CMD_APRV, consoleTitle).createMessage());
 		
 		for (DaemonMessage cachedLog : cachedLogs) {
 			session.write(cachedLog.createMessage());
 		}
 		
 		if (cachedStatus != null) {
 			session.write(cachedStatus.createMessage());
 		}
 	}
 	
 	private void denySession(IoSession session) {
 		invalidateSession(session);
 		session.write(new DaemonMessage(DaemonMessage.CMD_DENY, "").createMessage());
 	}
 
 }
