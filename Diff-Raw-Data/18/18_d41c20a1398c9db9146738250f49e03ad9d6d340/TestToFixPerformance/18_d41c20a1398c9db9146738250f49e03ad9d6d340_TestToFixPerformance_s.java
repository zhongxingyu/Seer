 /**
  * Copyright (c) 2011 Sebastian Tomac (tomac.org)
  * Licensed under LGPL licenses.
  * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
  **/
 package org.tomac.tools.fix50sp2;
 
 import java.nio.ByteBuffer;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.tomac.protocol.fix.messaging.*;
 
 
 /**
  * @author seto
  *
  */
 public class TestToFixPerformance {
 	final int ITERATIONS = 100000;
 	final int DO_SAMPLE_DATA = 100;
 	// discard data > 7000 due to grabage collection distortions and jvm warmup
 	final long DISCARD_LEVEL = 7000L;
 	FixMessageParser parser;
 	FixMessageListener listener;
 	FixMarketDataSnapshotFullRefresh message;
 	ByteBuffer out;
 	ByteBuffer buf;
 	byte[] tmp;
 	
 	@Before
 	public void setUp() {
 		tmp = new byte[1024];
 		out = ByteBuffer.allocate(1024);
 		buf = ByteBuffer.allocate(1024);
 		parser = new FixMessageParser();
 		listener = new FixMessageListener() {
     		@Override
     		public void onFixNewOrderSingle(FixNewOrderSingle msg) {
     		}
     		@Override
     		public void onFixMarketDataSnapshotFullRefresh(FixMarketDataSnapshotFullRefresh msg) {
     			message = msg;
     		}
 			@Override
 			public void onFixHeartbeat(FixHeartbeat msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTestRequest(FixTestRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixResendRequest(FixResendRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixReject(FixReject msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSequenceReset(FixSequenceReset msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixLogout(FixLogout msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixIOI(FixIOI msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixAdvertisement(FixAdvertisement msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixExecutionReport(FixExecutionReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderCancelReject(FixOrderCancelReject msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixLogon(FixLogon msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixNews(FixNews msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixEmail(FixEmail msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixNewOrderList(FixNewOrderList msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderCancelRequest(FixOrderCancelRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderCancelReplaceRequest(
 					FixOrderCancelReplaceRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderStatusRequest(FixOrderStatusRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixAllocationInstruction(FixAllocationInstruction msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixListCancelRequest(FixListCancelRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixListExecute(FixListExecute msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixListStatusRequest(FixListStatusRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixListStatus(FixListStatus msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixAllocationInstructionAck(
 					FixAllocationInstructionAck msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixDontKnowTradeDK(FixDontKnowTradeDK msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixQuoteRequest(FixQuoteRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixQuote(FixQuote msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSettlementInstructions(
 					FixSettlementInstructions msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMarketDataRequest(FixMarketDataRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMarketDataIncrementalRefresh(
 					FixMarketDataIncrementalRefresh msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMarketDataRequestReject(
 					FixMarketDataRequestReject msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixQuoteCancel(FixQuoteCancel msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixQuoteStatusRequest(FixQuoteStatusRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMassQuoteAcknowledgement(
 					FixMassQuoteAcknowledgement msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityDefinitionRequest(
 					FixSecurityDefinitionRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityDefinition(FixSecurityDefinition msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityStatusRequest(FixSecurityStatusRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityStatus(FixSecurityStatus msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradingSessionStatusRequest(
 					FixTradingSessionStatusRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradingSessionStatus(FixTradingSessionStatus msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMassQuote(FixMassQuote msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixBusinessMessageReject(FixBusinessMessageReject msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixBidRequest(FixBidRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixBidResponse(FixBidResponse msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixListStrikePrice(FixListStrikePrice msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixRegistrationInstructions(
 					FixRegistrationInstructions msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixRegistrationInstructionsResponse(
 					FixRegistrationInstructionsResponse msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderMassCancelRequest(
 					FixOrderMassCancelRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderMassCancelReport(FixOrderMassCancelReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixNewOrderCross(FixNewOrderCross msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixCrossOrderCancelReplaceRequest(
 					FixCrossOrderCancelReplaceRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixCrossOrderCancelRequest(
 					FixCrossOrderCancelRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityTypeRequest(FixSecurityTypeRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityTypes(FixSecurityTypes msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityListRequest(FixSecurityListRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityList(FixSecurityList msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixDerivativeSecurityListRequest(
 					FixDerivativeSecurityListRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixDerivativeSecurityList(
 					FixDerivativeSecurityList msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixNewOrderMultileg(FixNewOrderMultileg msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMultilegOrderCancelReplace(
 					FixMultilegOrderCancelReplace msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradeCaptureReportRequest(
 					FixTradeCaptureReportRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradeCaptureReport(FixTradeCaptureReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderMassStatusRequest(
 					FixOrderMassStatusRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixQuoteRequestReject(FixQuoteRequestReject msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixRFQRequest(FixRFQRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixQuoteStatusReport(FixQuoteStatusReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixQuoteResponse(FixQuoteResponse msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixConfirmation(FixConfirmation msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixPositionMaintenanceRequest(
 					FixPositionMaintenanceRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixPositionMaintenanceReport(
 					FixPositionMaintenanceReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixRequestForPositions(FixRequestForPositions msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixRequestForPositionsAck(
 					FixRequestForPositionsAck msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixPositionReport(FixPositionReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradeCaptureReportRequestAck(
 					FixTradeCaptureReportRequestAck msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradeCaptureReportAck(FixTradeCaptureReportAck msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixAllocationReport(FixAllocationReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixAllocationReportAck(FixAllocationReportAck msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixConfirmation_Ack(FixConfirmation_Ack msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSettlementInstructionRequest(
 					FixSettlementInstructionRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixAssignmentReport(FixAssignmentReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixCollateralRequest(FixCollateralRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixCollateralAssignment(FixCollateralAssignment msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixCollateralResponse(FixCollateralResponse msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixCollateralReport(FixCollateralReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixCollateralInquiry(FixCollateralInquiry msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixNetworkCounterpartySystemStatusRequest(
 					FixNetworkCounterpartySystemStatusRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixNetworkCounterpartySystemStatusResponse(
 					FixNetworkCounterpartySystemStatusResponse msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixUserRequest(FixUserRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixUserResponse(FixUserResponse msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixCollateralInquiryAck(FixCollateralInquiryAck msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixConfirmationRequest(FixConfirmationRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixContraryIntentionReport(
 					FixContraryIntentionReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityDefinitionUpdateReport(
 					FixSecurityDefinitionUpdateReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSecurityListUpdateReport(
 					FixSecurityListUpdateReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixAdjustedPositionReport(
 					FixAdjustedPositionReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixAllocationInstructionAlert(
 					FixAllocationInstructionAlert msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixExecutionAcknowledgement(
 					FixExecutionAcknowledgement msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradingSessionList(FixTradingSessionList msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradingSessionListRequest(
 					FixTradingSessionListRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixSettlementObligationReport(
 					FixSettlementObligationReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixDerivativeSecurityListUpdateReport(
 					FixDerivativeSecurityListUpdateReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixTradingSessionListUpdateReport(
 					FixTradingSessionListUpdateReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMarketDefinitionRequest(
 					FixMarketDefinitionRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMarketDefinition(FixMarketDefinition msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixMarketDefinitionUpdateReport(
 					FixMarketDefinitionUpdateReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixUserNotification(FixUserNotification msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderMassActionReport(FixOrderMassActionReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixOrderMassActionRequest(
 					FixOrderMassActionRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixApplicationMessageRequest(
 					FixApplicationMessageRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixApplicationMessageRequestAck(
 					FixApplicationMessageRequestAck msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixApplicationMessageReport(
 					FixApplicationMessageReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixStreamAssignmentRequest(
 					FixStreamAssignmentRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixStreamAssignmentReport(
 					FixStreamAssignmentReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixStreamAssignmentReportACK(
 					FixStreamAssignmentReportACK msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixPartyDetailsListRequest(
 					FixPartyDetailsListRequest msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
 			public void onFixPartyDetailsListReport(
 					FixPartyDetailsListReport msg) {
 				// TODO Auto-generated method stub
 				
 			}
 			@Override
			public void onUnknownMessageType(
					org.tomac.protocol.fix.messaging.fix50sp2.FixMessage msg) {
 				// TODO Auto-generated method stub
 				
 			}
     	};
 	}
 	
 	@Test
 	public void testInBoundLatency() throws Exception {
 		ByteBuffer buf = ByteBuffer.wrap("8=FIXT.1.1\u00019=241\u000135=D\u000149=SenderCompId\u000156=TargetCompId\u000134=37\u000152=20070223-22:28:33\u000111=1833\u000138=1\u000140=2\u000144=12\u000154=2\u000155=BHP\u000148=BHP\u000159=1\u000160=20060223-22:38:33\u0001526=3620\u000178=2\u000179=AllocACC180=1010.1\u000179=AllocACC2\u000180=2020.2\u0001453=2\u0001448=8\u0001447=D\u0001452=4\u0001448=AAA35354\u0001447=D\u0001452=3\u000110=089\u0001".getBytes());
 
         int count = 0;
         long cumTime = 0L;
         long cumTimeIntervall = 0L;
         int sampleCount = 0;
         long sampleTime = 0L;
 
         System.out.println("toFIX testInBoundLatency");
         while (count < ITERATIONS) {
         	long t0 = System.nanoTime();
         	parser.parse(buf, listener);
         	long t1 = System.nanoTime();
         	cumTime += t1 - t0;
         	cumTimeIntervall += t1 - t0;
         	++count;
         	if (count % DO_SAMPLE_DATA == 0) {
         		if (cumTimeIntervall / DO_SAMPLE_DATA < DISCARD_LEVEL) {
         			sampleCount++;
         			sampleTime += cumTimeIntervall / DO_SAMPLE_DATA;
         		}
         		//System.out.println(cumTime / count + " " + cumTimeIntervall / DO_SAMPLE_DATA);
         		cumTimeIntervall = 0L;
         	}
         	buf.flip();
         }
 		System.out.println("ns/msg\t#count\ttotns/msg\t#totCount");
 		System.out.println(sampleTime / sampleCount + "\t" + 
 				sampleCount * DO_SAMPLE_DATA + "\t" + cumTime / ITERATIONS + "\t" + ITERATIONS);
 	}
 
 	
     @Test
     public void testOutBoundLatency() throws Exception {
     	ByteBuffer buf = ByteBuffer.wrap("8=FIXT.1.1\u00019=155\u000135=W\u000134=2\u000149=ABFX\u000152=20080722-16:37:11.234\u000156=X2RV1\u00155=EUR/USD\u0001262=CAP00000112\u0001268=2\u0001269=1\u0001270=1.5786\u0001271=500000\u0001272=20080724\u0001269=1\u0001271=500000\u0001272=20080724\u000110=218\u0001".getBytes());
         
     	parser.parse(buf, listener);
         ByteBuffer out = ByteBuffer.allocate(1024);
 
         int count = 0;
         long cumTime = 0L;
         long cumTimeIntervall = 0L;
         int sampleCount = 0;
         long sampleTime = 0L;
         
         System.out.println("toFIX testOutBoundLatency");
         while (count < ITERATIONS) {
         	long t0 = System.nanoTime();
         	message.encode(out);
         	long t1 = System.nanoTime();
         	out.clear();
         	cumTime += t1 -t0;
         	cumTimeIntervall += t1 -t0;
         	message.clear();
         	++count;
         	if (count % DO_SAMPLE_DATA == 0) {
         		if (cumTimeIntervall / DO_SAMPLE_DATA < DISCARD_LEVEL) {
         			sampleCount++;
         			sampleTime += cumTimeIntervall / DO_SAMPLE_DATA;
         		}
         		//System.out.println(cumTime / count + " " + cumTimeIntervall / DO_SAMPLE_DATA);
         		cumTimeIntervall = 0L;
         	}
         }
 		System.out.println("ns/msg\t#count\ttotns/msg\t#totCount");
 		System.out.println(sampleTime / sampleCount + "\t" + 
 				sampleCount * DO_SAMPLE_DATA + "\t" + cumTime / ITERATIONS + "\t" + ITERATIONS);
     }
 	
 	
 }
