 package org.tomac.protocol.fix.fix42nordic;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.tomac.protocol.fix.FixGarbledException;
 import org.tomac.protocol.fix.FixSessionException;
 import org.tomac.protocol.fix.FixUtils;
 import org.tomac.protocol.fix.messaging.FixAcceptedCancel;
 import org.tomac.protocol.fix.messaging.FixAcceptedCancelReplace;
 import org.tomac.protocol.fix.messaging.FixApplicationReject;
 import org.tomac.protocol.fix.messaging.FixBreakNotification;
 import org.tomac.protocol.fix.messaging.FixBusinessReject;
 import org.tomac.protocol.fix.messaging.FixCancelNotification;
 import org.tomac.protocol.fix.messaging.FixEntryNotificationtoAllegedFirm;
 import org.tomac.protocol.fix.messaging.FixExecutionReportFill;
 import org.tomac.protocol.fix.messaging.FixExecutionRestatement;
 import org.tomac.protocol.fix.messaging.FixHeartbeat;
 import org.tomac.protocol.fix.messaging.FixLockedinNotification;
 import org.tomac.protocol.fix.messaging.FixLockedinTradeBreak;
 import org.tomac.protocol.fix.messaging.FixLogon;
 import org.tomac.protocol.fix.messaging.FixLogout;
 import org.tomac.protocol.fix.messaging.FixMessage;
 import org.tomac.protocol.fix.messaging.FixMessageListener;
 import org.tomac.protocol.fix.messaging.FixMessageParser;
 import org.tomac.protocol.fix.messaging.FixOrderAcknowledgement;
 import org.tomac.protocol.fix.messaging.FixOrderCancelReject;
 import org.tomac.protocol.fix.messaging.FixOrderCancelReplaceRequest;
 import org.tomac.protocol.fix.messaging.FixOrderCancelRequest;
 import org.tomac.protocol.fix.messaging.FixOrderReject;
import org.tomac.protocol.fix.messaging.FixOrderSingle;
 import org.tomac.protocol.fix.messaging.FixPendingCancel;
 import org.tomac.protocol.fix.messaging.FixReject;
 import org.tomac.protocol.fix.messaging.FixRejectedCancelReplace;
 import org.tomac.protocol.fix.messaging.FixResendRequest;
 import org.tomac.protocol.fix.messaging.FixSequenceReset;
 import org.tomac.protocol.fix.messaging.FixTestRequest;
 import org.tomac.protocol.fix.messaging.FixTradeEntryNotificationtoEnteringFirm;
 import org.tomac.protocol.fix.messaging.FixTradeReportCancel;
 import org.tomac.protocol.fix.messaging.FixTradeReportEntry;
 
 public class TestParseFixStream {
 	/**
 	 * to rerun a "realworld" raw FIX log through the parser; 1. decoder 2.
 	 * encode 3. validate
 	 */
 	String RAW_FIX_LOG = "sample-raw-fix-log-fix42nordic.fix";
 	int FIX_MAX_MESSAGE_LENGTH = 1024;
 
 	byte[] flipByteArray;
 	byte[] readByteArray;
 	ByteBuffer buf;
 	FixMessageParser parser;
 	FixMessageListener listener;
 
 	@Before
 	public void setUp() throws Exception {
 		flipByteArray = new byte[FIX_MAX_MESSAGE_LENGTH];
 		readByteArray = new byte[FIX_MAX_MESSAGE_LENGTH];
 		buf = ByteBuffer.allocate(FIX_MAX_MESSAGE_LENGTH + FIX_MAX_MESSAGE_LENGTH);
 		parser = new FixMessageParser();
 		listener = new FixMessageListenerImpl();
 		System.setProperty("fix.raw", "true");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	//@Ignore
 	@Test
 	public void testParesFixStream() {
 		System.setProperty("useNasdaq", "true");
 		
 		boolean b = Boolean.getBoolean("useNasdaq");
 		
 		File f = new File(RAW_FIX_LOG);
 		FixMessage.IGNORE_CHECKSUM = true;
 		
 		assertTrue(f.isFile() && f.canRead());
 
 		try {
 			int read = 0;
 			int totRead = 0;
 
 			InputStream is = new FileInputStream(f);
 
 			while ((read = is.read(readByteArray)) > 0) {
 
 				buf.put(readByteArray); 
 				buf.flip();
 
 				totRead += read;
 				
 				//System.out.println("Read data: " + new String(buf.array()));
 				//System.out.println("readByteArray: " + new String(readByteArray));
 
 				while (buf.hasRemaining()) {
 
 					try {
 						
 						parser.parse(buf, listener);
 
 						if (buf.remaining() < 1024 && read == readByteArray.length) { 	
 							buf.compact();
 							break;
 						}
 
 					} catch (FixGarbledException e) {
 						fail(e.getMessage());
 					} catch (FixSessionException e) {
 						fail(e.getMessage());
 					}
 					
 				}
 
 			}
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 
 	}
 
 	private int findLastCehcksum(byte[] buf, int offset, int len) {
 		int sohPos = -1;
 			
 		for (int i = buf.length - 1; i >= 0; i --) {
 			
 			if (buf[i]== FixUtils.SOH) sohPos = i;
 			
 			if (sohPos != -1 && buf[i] == (byte)FixUtils.EQL) {
 				if (i > 3  && buf[i - 1] == (byte)'0' && buf[i-2] == (byte)'1' && buf[i-3] == FixUtils.SOH) 
 					return sohPos + 1;
 			}
 		}
 		return -1;
 	}
 
 	private class FixMessageListenerImpl implements FixMessageListener {
 
 		private void printIt(FixMessage msg) {
 			/*try {
 				msg.getAll();
 
 				if (Boolean.getBoolean("fix.raw")) {
 					ByteBuffer out = ByteBuffer.allocate(1024);
 					msg.encode(out);
 					byte[] outArray = new byte[out.limit()];
 					//msg.buf.position(0);
 					//msg.buf.get(outArray, 0, msg.buf.limit() - 1);
 					//System.out.println(new String(outArray));
 					out.get(outArray);
 					System.out.println(new String(outArray));
 				} else {
 					System.out.println(msg.toString());
 				}
 				msg.clear();
 			} catch (FixSessionException e) {
 				e.printStackTrace();
 			} catch (FixGarbledException e) {
 				e.printStackTrace();
 			}*/
 		}
 
 		/**
 		 * FixMessageListenre methods
 		 */
 
 		@Override
 		public void onUnknownMessageType(FixMessage msg) {
 		}
 
 		@Override
 		public void onFixAcceptedCancelReplace(FixAcceptedCancelReplace msg) {
 			printIt(msg);
 		}
 
 		@Override
 		public void onFixAcceptedCancel(FixAcceptedCancel msg) {
 			printIt(msg);
 		}
 
 		@Override
 		public void onFixApplicationReject(FixApplicationReject msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixBreakNotification(FixBreakNotification msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixBusinessReject(FixBusinessReject msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixCancelNotification(FixCancelNotification msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixEntryNotificationtoAllegedFirm(FixEntryNotificationtoAllegedFirm msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixExecutionReportFill(FixExecutionReportFill msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixExecutionRestatement(FixExecutionRestatement msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixLockedinNotification(FixLockedinNotification msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixLockedinTradeBreak(FixLockedinTradeBreak msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixOrderAcknowledgement(FixOrderAcknowledgement msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixOrderReject(FixOrderReject msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixPendingCancel(FixPendingCancel msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixTradeEntryNotificationtoEnteringFirm(FixTradeEntryNotificationtoEnteringFirm msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixTradeReportCancel(FixTradeReportCancel msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixTradeReportEntry(FixTradeReportEntry msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixHeartbeat(FixHeartbeat msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixLogon(FixLogon msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixLogout(FixLogout msg) {
 			printIt(msg);
 
 		}
 
 		@Override
		public void onFixOrderSingle(FixOrderSingle msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixOrderCancelReject(FixOrderCancelReject msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixRejectedCancelReplace(FixRejectedCancelReplace msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixOrderCancelReplaceRequest(FixOrderCancelReplaceRequest msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixOrderCancelRequest(FixOrderCancelRequest msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixReject(FixReject msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixResendRequest(FixResendRequest msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixSequenceReset(FixSequenceReset msg) {
 			printIt(msg);
 
 		}
 
 		@Override
 		public void onFixTestRequest(FixTestRequest msg) {
 			printIt(msg);
 
 		}
 
 	}
 }
