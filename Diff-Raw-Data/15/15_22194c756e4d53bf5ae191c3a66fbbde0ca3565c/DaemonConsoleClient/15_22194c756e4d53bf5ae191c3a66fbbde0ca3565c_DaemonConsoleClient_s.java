 package com.zzvc.mmps.daemon.console;
 
 import static com.zzvc.mmps.daemon.DaemonConstants.DEFAULT_GROUP;
 
 import java.net.InetSocketAddress;
 import java.nio.charset.Charset;
 import java.util.ResourceBundle;
 
 import javax.annotation.Resource;
 
 import org.apache.mina.core.future.ConnectFuture;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
 import org.apache.mina.transport.socket.nio.NioSocketConnector;
 
 import com.zzvc.mmps.console.ConsoleMessageSupport;
 import com.zzvc.mmps.daemon.DaemonMessage;
 import com.zzvc.mmps.gui.util.PatternUtil;
 
 public class DaemonConsoleClient extends ConsoleMessageSupport {
 	@Resource
 	private DaemonConsoleClientHandler handler;
 	
 	private NioSocketConnector connector;
 	private IoSession session;
 	private boolean closed;
 	
     private String group = DEFAULT_GROUP;
     private String host;
     private int port;
     
     private String localhost="localhost";
     
     private PatternUtil patternUtil;
 
 	public void init() {
 		ResourceBundle bundle = ResourceBundle.getBundle("daemon");
 		try {
 			group = bundle.getString("console.daemon.group");
 		} catch (Exception e) {
 		}
 		try {
 			host = bundle.getString("console.daemon.host");
 			port = Integer.parseInt(bundle.getString("console.daemon.port"));
 		} catch (Exception e) {
 		}
 		
 		connector = new NioSocketConnector();
 		connector.setHandler(handler);
 		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
 		
 		try {
 			patternUtil = new PatternUtil(findText("console.daemon.log.message.pattern"));
 		} catch (Exception e) {
 			patternUtil = new PatternUtil();
 		}
 	}
 
 	public boolean isClosed() {
 		return closed;
 	}
 	
 	public void setClosed(boolean closed) {
 		this.closed = closed;
 	}
 	
 	public boolean needConnect() {
 		return !(isClosed() || isConnected());
 	}
 	
 	public boolean isConnected() {
 		return session != null && session.isConnected();
 	}
 
 	public boolean connect() {
 		infoLocal("console.daemon.connect", host);
         ConnectFuture future = connector.connect(new InetSocketAddress(host, port));
         future.awaitUninterruptibly();
         if (!future.isConnected()) {
         	warnLocal("console.daemon.error.connectfailed", host, port);
             return false;
         }
         session = future.getSession();
         join();
         
         return true;
 	}
 	
 	public void join() {
 		session.write(new DaemonMessage(DaemonMessage.CMD_JOIN, group).createMessage());
 	}
 	
 	public void quit() {
 		if (isConnected()) {
 			session.write(new DaemonMessage(DaemonMessage.CMD_QUIT, "").createMessage());
 			session.close(true);
 		}
 		setClosed(true);
 	}
 	
 	public void approved(String daemonAppTitle) {
 		setConsoleTitle(findText("console.daemon.log.format", host, daemonAppTitle));
 		infoLocal("console.daemon.join.approved", host, daemonAppTitle);
 	}
 	
 	public void denied() {
 		errorLocal("console.daemon.join.denied", host);
 		session.close(true);
 		setClosed(true);
 	}
 	
 	public void broken() {
 		warnLocal("console.daemon.broken", host);
 		statusLocal("console.daemon.broken", host);
 	}
 	
 	public void closed() {
 		warnLocal("console.daemon.closed", host);
 		statusLocal("console.daemon.closed", host);
 	}
 	
 	public void infoRemote(long timestamp, String text) {
 		infoMessage("console.daemon.log.format", host, patternUtil.format(timestamp, "", text));
 	}
 	
 	public void warnRemote(long timestamp, String text) {
 		warnMessage("console.daemon.log.format", host, patternUtil.format(timestamp, "", text));
 	}
 	
 	public void errorRemote(long timestamp, String text) {
 		errorMessage("console.daemon.log.format", host, patternUtil.format(timestamp, "", text));
 	}
 	
 	public void statusRemote(String text) {
 		statusMessage("console.daemon.log.format", host, text);
 	}
 	
 	public void infoLocal(String key, Object... args) {
 		infoMessage("console.daemon.log.format", localhost, findText(key, args));
 	}
 	
 	public void warnLocal(String key, Object... args) {
 		warnMessage("console.daemon.log.format", localhost, findText(key, args));
 	}
 	
 	public void errorLocal(String key, Object... args) {
 		errorMessage("console.daemon.log.format", localhost, findText(key, args));
 	}
 	
 	public void statusLocal(String key, Object... args) {
 		statusMessage("console.daemon.log.format", localhost, findText(key, args));
 	}
 
 	@Override
 	protected String getConsolePrefix() {
 		return "";
 	}
 }
