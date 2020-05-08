 package com.chinarewards.qqgbvpn.main.protocol.filter;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.filterchain.IoFilterAdapter;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.core.write.WriteRequest;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec.CodecUtil;
 import com.chinarewards.qqgbvpn.main.util.MinaUtil;
 
 /**
  * Provides better business-oriented logging.
  * <p>
  * Current this filter can log the client IP, client port and any identified POS
  * ID (if POS machine has logged in to this server before).
  * <p>
  * The best position to place this filter is after the message has been decoded,
  * before any business related filter.
  * 
  * @author cyril
  * @since 0.1.0
  */
 public class LoggingFilter extends IoFilterAdapter {
 
 	Logger log = LoggerFactory.getLogger(getClass());
 
 	private int maxHexDumpLength = 96;
 
 	/**
 	 * Returns the maximum length which data will be dumped for incoming and
 	 * outgoing message.
 	 * <p>
 	 * 
 	 * Default value is 128.
 	 * 
 	 * @return the maxHexDumpLength
 	 */
 	public int getMaxHexDumpLength() {
 		return maxHexDumpLength;
 	}
 
 	/**
 	 * Sets the maximum length which data will be dumped for incoming and
 	 * outgoing message. If zero is specified, no hex dump will be performed.
 	 * <p>
 	 * 
 	 * Default value is 128.
 	 * 
 	 * @return the maxHexDumpLength
 	 * @param maxHexDumpLength
 	 *            the maxHexDumpLength to set ＠throws IllegalArgumentException
 	 *            if the value is less than 0.
 	 */
 	public void setMaxHexDumpLength(int maxHexDumpLength) {
 		if (maxHexDumpLength < 0)
 			throw new IllegalArgumentException(
 					"Maximum length should not be less than zero.");
 		this.maxHexDumpLength = maxHexDumpLength;
 	}
 
 	@Override
 	public void exceptionCaught(NextFilter nextFilter, IoSession session,
 			Throwable cause) throws Exception {
 
 		logError(session, cause);
 
		exceptionCaught(nextFilter, session, cause);
 	}
 
 	/**
 	 * Log the following information to report that a command cannot be
 	 * processed.
 	 * <p>
 	 * Note that one should not assume the following information must exists.
 	 * 
 	 * <ol>
 	 * <li>Source IP</li>
 	 * <li>Source port</li>
 	 * <li>POS ID</li>
 	 * <li>TODO: Command ID</li>
 	 * <li>TODO: Command content</li>
 	 * </ol>
 	 */
 	protected void logError(IoSession session, Throwable cause) {
 
 		// return immediately if insufficient level is given.
 		if (!log.isDebugEnabled())
 			return;
 
 		// prepare the logging data
 		String addr = buildAddressPortString(session);
 		String posId = getPosIdFromSession(session);
 
 		if (log.isDebugEnabled()) {
 			log.debug(
 					"An exception is caught when handling command. Detailed information: "
 							+ " client address={}:{}, Mina session ID={}, POS ID={}", new Object[] {
 							addr, session.getId(), posId });
 		}
 
 	}
 
 	/**
 	 * Current implementation prints the raw message
 	 */
 	public void messageReceived(NextFilter nextFilter, IoSession session,
 			Object message) throws Exception {
 
 		printMessageReceivedFrom(session);
 		doHexDump(message, getMaxHexDumpLength());
 
 		nextFilter.messageReceived(session, message);
 
 	}
 	
 	protected void printMessageReceivedFrom(IoSession session) {
 		String addr = buildAddressPortString(session);
 		String posId = getPosIdFromSession(session);
 		if (log.isTraceEnabled()) {
 			log.trace(
 					"Message received from address {}:{}, Mina session ID={}, POS ID={}",
 					new Object[] { addr, session.getId(), posId });
 		}
 	}
 
 	protected void doHexDump(Object message, int maxLength) {
 		// do nothing if not IoBuffer
 		if (!(message instanceof IoBuffer))
 			return;
 		// do nothing if no need to print.
 		if (maxLength <= 0)
 			return;
 
 		// print the buffer
 		IoBuffer buffer = (IoBuffer) message;
 		if (!buffer.hasRemaining())
 			return;
 
 		// remember the position
 		int position = buffer.position();
 		// number of available bytes to read.
 		int remaining = buffer.remaining();
 		// the actual length to read.
 		int partLength = remaining < maxHexDumpLength ? remaining
 				: maxHexDumpLength;
 
 		try {
 			// copy the target bytes to print
 			byte[] part = new byte[partLength];
 			buffer.get(part);
 			int omitted = buffer.remaining();
 			
 			String hexDump = CodecUtil.hexDumpAsString(part);
 
 			// use hex dump to output
 			if (log.isTraceEnabled()) {
 				log.trace("Received raw bytes: ({} of {} bytes, {} omitted)",
 						new Object[] { partLength, remaining, omitted });
 				log.trace("[{}]", hexDump);
 			}
 
 		} finally {
 			// must reset the position after reading from IoBuffer!
 			buffer.position(position);
 		}
 
 	}
 
 	public void messageSent(NextFilter nextFilter, IoSession session,
 			WriteRequest writeRequest) throws Exception {
 		nextFilter.messageSent(session, writeRequest);
 	}
 
 	/**
 	 * Returns the POS ID stored in the Mina session.
 	 * 
 	 * @param session
 	 * @return
 	 */
 	protected String getPosIdFromSession(IoSession session) {
 		return MinaUtil.getPosIdFromSession(session);
 	}
 
 	protected String buildAddressPortString(IoSession session) {
 		return MinaUtil.buildAddressPortString(session);
 	}
 
 }
