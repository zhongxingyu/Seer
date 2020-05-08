 package com.chinarewards.qqgbvpn.main.protocol.handler;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.service.IoService;
 import org.apache.mina.core.session.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.ConfigKey;
 import com.chinarewards.qqgbvpn.main.Session;
 import com.chinarewards.qqgbvpn.main.SessionStore;
 import com.chinarewards.qqgbvpn.main.exception.PackageException;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceDispatcher;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceMapping;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceSession;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.Message;
 import com.chinarewards.qqgbvpn.main.protocol.filter.LoginFilter;
import com.chinarewards.qqgbvpn.main.protocol.filter.SessionKeyMessageFilter;
 import com.chinarewards.qqgbvpn.main.protocol.impl.ServiceDispatcherException;
 import com.chinarewards.qqgbvpn.main.protocol.impl.ServiceRequestImpl;
 import com.chinarewards.qqgbvpn.main.protocol.impl.ServiceResponseImpl;
 import com.chinarewards.qqgbvpn.main.protocol.impl.mina.MinaSession;
import com.chinarewards.qqgbvpn.main.session.v1.V1SessionKey;
 import com.chinarewards.qqgbvpn.main.util.MinaUtil;
 
 /**
  * Server handler.
  * <p>
  * 
  * Message reaching this class shoudl be ready for dispatching to the
  * corresponding <code>ServiceHandler</code> class.
  * 
  * @author huangwei
  * @author cyril
  * @since 0.1.0
  */
 public class ServerSessionHandler extends IoHandlerAdapter {
 	
 	private static class SessionWrapper implements ServiceSession {
 		
 		private Session session;
 		
 		public SessionWrapper(Session session) {
 			this.session = session;
 		}
 
 		/* (non-Javadoc)
 		 * @see com.chinarewards.qqgbvpn.main.protocol.ServiceSession#getAttribute(java.lang.String)
 		 */
 		@Override
 		public Object getAttribute(String key) {
 			return session.getAttribute(key);
 		}
 
 		/* (non-Javadoc)
 		 * @see com.chinarewards.qqgbvpn.main.protocol.ServiceSession#setAttribute(java.lang.String, java.lang.Object)
 		 */
 		@Override
 		public void setAttribute(String key, Object value) {
 			session.setAttribute(key, value);
 		}
 		
 	}
 	
 	/**
 	 * Default service handler thread pool size.
 	 */
 	public static final int DEFAULT_SERVICE_HANDLER_THREAD_POOL_SIZE = 100;
 
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	protected final ServiceDispatcher serviceDispatcher;
 
 	protected final ServiceMapping serviceMapping;
 
 	protected final ExecutorService exec;
 	
 	protected final Configuration configuration;
 	
 	protected final SessionStore sessionStore;
 
 	public ServerSessionHandler(ServiceDispatcher serviceDispatcher,
 			ServiceMapping serviceMapping, Configuration configuration, 
 			SessionStore sessionStore) {
 		
 		this.serviceDispatcher = serviceDispatcher;
 		this.serviceMapping = serviceMapping;
 		this.configuration = configuration;
 		this.sessionStore = sessionStore;
 
 		int poolSize = configuration.getInt(
 				ConfigKey.SERVER_SERVICEHANDLER_THREADPOOLSIZE,
 				DEFAULT_SERVICE_HANDLER_THREAD_POOL_SIZE);
 		
 		exec = Executors.newFixedThreadPool(poolSize);
 	}
 
 	@Override
 	public void exceptionCaught(IoSession session, Throwable cause)
 			throws Exception {
 		// XXX is this OK to handle this exception in this way?
 		log.error("An exception is caught in "
 				+ this.getClass().getSimpleName(), cause);
 	}
 
 	@Override
 	public void messageReceived(IoSession session, Object message)
 			throws Exception {
 
 		log.trace("messageReceived() started");
 
 		debugMessageReceived(session, message);
 
 		// do the actual dispatch
 		doDispatch(session, message);
 
 		log.trace("messageReceived() done");
 	}
 
 	/**
 	 * Print the debug information of any message received event. If logging is
 	 * not configured at DEBUG level or lower, no task will be executed.
 	 * 
 	 * @param session
 	 * @param message
 	 */
 	protected void debugMessageReceived(IoSession session, Object message) {
 
 		if (!log.isDebugEnabled())
 			return;
 
 		// debug print the remote address (IP, port and POS ID)
 		if (log.isDebugEnabled()) {
 			Session sStore = sessionStore.getSession(MinaUtil.getServerSessionId(session));
 			log.debug(
 					"A message is sent from client at "
 							+ MinaUtil.buildCommonClientAddressText(session, sStore)
 							+ ", message: {}", message);
 		}
 
 	}
 
 	/**
 	 * Returns the logged in POS ID, muting all exception.
 	 * 
 	 * @param session
 	 * @return
 	 */
 	protected String getLoggedInPosId(IoSession session) {
 		try {
 			Session sStore = sessionStore.getSession(MinaUtil.getServerSessionId(session));
 			String posId = (String) sStore.getAttribute(LoginFilter.POS_ID);
 			return posId;
 		} catch (Throwable t) {
 			log.error(
 					"Internal program error: POS ID in Mina session cannot be retrieved",
 					t);
 		}
 		return null;
 	}
 
 	/**
 	 * Do the actual dispatch job, including reading the request, prepare the
 	 * request and response environment, send the request to
 	 * <code>ServiceHandler</code>, and write back the response.
 	 * <p>
 	 * 
 	 * The request and response
 	 * 
 	 * @param session
 	 * @param message
 	 * @throws Exception
 	 */
 	protected void doDispatch(final IoSession session, final Object message)
 			throws Exception {
 
 		Callable<Void> callable = new Callable<Void>() {
 
 			@Override
 			public Void call() throws Exception {
 				// get the message
 				Message msg = (Message) message;
 
 				long cmdId = msg.getBodyMessage().getCmdId();
 
 				// FIXME throw PackageException if no handler found for command ID
 
 				// build a request (for dispatcher)
 				Session sStore = sessionStore.getSession(MinaUtil.getServerSessionId(session));
 				
 				SessionWrapper serviceSession = new SessionWrapper(sStore);
 				ServiceRequestImpl request = new ServiceRequestImpl(
 						msg.getBodyMessage(), serviceSession);
 				// build a response (for dispatcher)
 				ServiceResponseImpl response = new ServiceResponseImpl();
 
 				// dispatch the command to the corresponding service handler.
 				try {
 					serviceDispatcher.dispatch(serviceMapping, request,
 							response);
 				} catch (ServiceDispatcherException e) {
 					throw new PackageException(
 							"No mapping found for command ID " + cmdId, e);
 				}
 
 				// grep the response, and write back to the channel.
 				ICommand responseMsgBody = (ICommand) response.getResponse();
 				msg.getHeadMessage().setFlags(0);
				
				if (session
						.containsAttribute(SessionKeyMessageFilter.RETURN_SESSION_ID_TO_CLIENT)
						&& session
								.containsAttribute(SessionKeyMessageFilter.SESSION_ID)
						&& session
								.containsAttribute(SessionKeyMessageFilter.CLIENT_SUPPORT_SESSION_ID)) {
					log.debug("setting session key in message header");
					V1SessionKey key = new V1SessionKey(MinaUtil.getServerSessionId(session));
					msg.getHeadMessage().setSessionKey(key);
					msg.getHeadMessage().setFlags(msg.getHeadMessage().getFlags() | 0x8000);
				}
				
 				msg.setBodyMessage(responseMsgBody);
 				session.write(msg);
 
 				return null;
 			}
 
 		};
 
 		exec.submit(callable);
 
 	}
 
 	/**
 	 * Prints the number of managed session.
 	 * 
 	 * @param service
 	 */
 	protected void printManagedSessions(IoService service) {
 
 		log.info("Number of managed sessions: {}"
 				+ service.getManagedSessionCount());
 
 	}
 
 	/**
 	 * Format the remote address and port in the format of &lt;ip&gt;:&lt;port&gt;.
 	 * 
 	 * @param session
 	 * @return
 	 */
 	protected String buildAddressPortString(IoSession session) {
 		SocketAddress addr = session.getRemoteAddress();
 		if (addr == null || !(addr instanceof InetSocketAddress)) {
 			return null;
 		}
 
 		InetSocketAddress sAddr = (InetSocketAddress) addr;
 		return sAddr.getAddress().getHostAddress() + ":" + sAddr.getPort();
 	}
 
 }
