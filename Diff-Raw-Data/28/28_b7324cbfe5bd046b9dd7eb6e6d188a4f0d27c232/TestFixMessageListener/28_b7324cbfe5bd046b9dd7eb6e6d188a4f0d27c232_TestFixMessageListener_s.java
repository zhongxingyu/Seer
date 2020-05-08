 /**
  * Copyright (c) 2011 Sebastian Tomac (tomac.org)
  * Licensed under LGPL licenses.
  * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
  **/
 package org.tomac.protocol.fix.fix42nordic;
 
 import java.nio.ByteBuffer;
 
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
 
 
 /**
  * @author seto
  *
  */
 public class TestFixMessageListener implements FixMessageListener {
 
 	@Override
 	public void onUnknownMessageType(FixMessage msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixAcceptedCancelReplace(FixAcceptedCancelReplace msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixAcceptedCancel(FixAcceptedCancel msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixApplicationReject(FixApplicationReject msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixBreakNotification(FixBreakNotification msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixBusinessReject(FixBusinessReject msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixCancelNotification(FixCancelNotification msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixEntryNotificationtoAllegedFirm(
 			FixEntryNotificationtoAllegedFirm msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixExecutionReportFill(FixExecutionReportFill msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixExecutionRestatement(FixExecutionRestatement msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixLockedinNotification(FixLockedinNotification msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixLockedinTradeBreak(FixLockedinTradeBreak msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixOrderAcknowledgement(FixOrderAcknowledgement msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixOrderReject(FixOrderReject msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixPendingCancel(FixPendingCancel msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixTradeEntryNotificationtoEnteringFirm(
 			FixTradeEntryNotificationtoEnteringFirm msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixTradeReportCancel(FixTradeReportCancel msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixTradeReportEntry(FixTradeReportEntry msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixHeartbeat(FixHeartbeat msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixLogon(FixLogon msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixLogout(FixLogout msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
	public void onFixOrderSingle(FixOrderSingle msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixOrderCancelReject(FixOrderCancelReject msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixRejectedCancelReplace(FixRejectedCancelReplace msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixOrderCancelReplaceRequest(FixOrderCancelReplaceRequest msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixOrderCancelRequest(FixOrderCancelRequest msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixReject(FixReject msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixResendRequest(FixResendRequest msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixSequenceReset(FixSequenceReset msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onFixTestRequest(FixTestRequest msg) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
