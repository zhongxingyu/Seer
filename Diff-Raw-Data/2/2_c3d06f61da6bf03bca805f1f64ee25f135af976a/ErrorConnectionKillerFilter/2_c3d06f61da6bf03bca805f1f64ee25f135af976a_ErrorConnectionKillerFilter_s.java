 package com.chinarewards.qqgbvpn.main.protocol.filter;
 
 import org.apache.mina.core.session.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.SessionStore;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ErrorBodyMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.ICommand;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.Message;
 import com.chinarewards.qqgbvpn.main.util.MinaUtil;
 import com.google.inject.Inject;
 
 /**
  * Kills connections if it has too many error message received.
  * <p>
  * This filter should be placed after an <code>ICommand</code> has been decoded.
  * 
  * @author cyril
  * @since 0.1.0
  */
 public class ErrorConnectionKillerFilter extends AbstractFilter {
 
 	Logger log = LoggerFactory.getLogger(getClass());
 
 	/**
 	 * TODO make this configurable.
 	 */
 	private int errorCountThreshold = 5;
 	
 	final SessionStore sessionStore;
 	
 	@Inject
 	public ErrorConnectionKillerFilter(SessionStore sessionStore) {
 		this.sessionStore = sessionStore;
 	}
 	
 
 	/**
 	 * Returns the error count threshold. When a Mina session has a consecutive
 	 * number of error message receive count equals to or large than this value
 	 * , the connection will be closed.
 	 * 
 	 * @return the errorCountThreshold
 	 */
 	public int getErrorCountThreshold() {
 		return errorCountThreshold;
 	}
 
 	/**
 	 * Sets the error count threshold. When a Mina session has a consecutive
 	 * number of error message receive count equals to or large than this value
 	 * , the connection will be closed.
 	 * <p>
 	 * If this value is set to 0, connection will never be closed regardless of
 	 * how many error messages are received.
 	 * 
 	 * @param errorCountThreshold
 	 *            the errorCountThreshold to set
 	 * @throws IllegalArgumentException
 	 *             if errorCountThreshold is set to a value less than zero.
 	 */
 	public void setErrorCountThreshold(int errorCountThreshold) {
 		if (errorCountThreshold < 0) throw new IllegalArgumentException();
 		this.errorCountThreshold = errorCountThreshold;
 	}
 
 	@Override
 	public void messageReceived(NextFilter nextFilter, IoSession session,
 			Object message) throws Exception {
 
 		// this filter only take cares of our message.
 		if (!(message instanceof Message)) {
 			nextFilter.messageReceived(session, message);
 			return;
 		}
 
 		ICommand msg = ((Message) message).getBodyMessage();
 		if (msg instanceof ErrorBodyMessage) {
 			
 			// increment error message counter and see if it reaches the 
 			// threshold. If so, close the session.
 			
 			int errorCount = incrementErrorCount(session);
 			if (errorCount >= getErrorCountThreshold()) {
 				if (log.isInfoEnabled()) {
 					log.info(
 							"Too many error message received on connection (addr={}, "
 									+ "Mina session ID {}, POS ID={}), closing connection.",
 							new Object[] {
 									MinaUtil.buildAddressPortString(session),
 									session.getId(),
 									MinaUtil.getPosIdFromSession(getServerSession(session, sessionStore)) });
 				}
 				
 				// close and return.
 				session.close(true);
 				return;
 			} else {
 				// continue processing, let the client has a chance to correct
 				// itself by sending correct messages.
 				nextFilter.messageReceived(session, message);
 			}
 			
 		} else {	// not error message, reset the stat if needed.
 
 			resetErrorCount(session);
 			
 			// continue the filter chain
 			nextFilter.messageReceived(session, message);
 		}
 
 		log.trace("messageReceived() done");
 	}
 
 	protected void resetErrorCount(IoSession session) {
 		
 		// reset only if needed.
 		int oldCount = getErrorCount(session);
 		if (oldCount > 0) {
 			if (log.isTraceEnabled()) {
 				log.trace("Reset error message counter to zero for connection  (addr={}, "
 									+ "Mina session ID {}, POS ID={}), closing connection.",
 							new Object[] {
 									MinaUtil.buildAddressPortString(session),
 									session.getId(),
 									MinaUtil.getPosIdFromSession(getServerSession(session, sessionStore)) });
 			}
 		}
		getServerSession(session, sessionStore).setAttribute(getSessionKey(), 0);
 		
 	}
 
 	protected int getErrorCount(IoSession session) {
 		Integer count = (Integer)session.getAttribute(getSessionKey());
 		return (count == null ? 0 : count);
 	}
 
 	protected int incrementErrorCount(IoSession session) {
 		String key = getSessionKey();
 		Integer value = (Integer) session.getAttribute(key);
 
 		if (value == null) {
 			// first time.
 			session.setAttribute(key, 1);
 		} else {
 			// increment error counter by 1.
 			session.setAttribute(key, value + 1);
 		}
 
 		return (Integer) session.getAttribute(key);
 	}
 
 	/**
 	 * Returns the session key which stores the error message counter.
 	 * 
 	 * @return
 	 */
 	protected String getSessionKey() {
 		return "_" + getClass().getName() + ".errorCount";
 	}
 
 }
