 package org.tomac.protocol.fix.fix50sp2;
 
 import org.tomac.protocol.fix.messaging.*;
 
 public class FixMessageListenerTest implements FixMessageListener {
 	FixMessage outMsg;
 
 	@Override
 	public void onUnknownMessageType(FixMessage msg) {
 		outMsg = null;
 	}
 
 	@Override
 	public void onFixHeartbeat(FixHeartbeat msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
	public void onFixTestRequest(FixTestRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixResendRequest(FixResendRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixReject(FixReject msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSequenceReset(FixSequenceReset msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixLogout(FixLogout msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixIOI(FixIOI msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixAdvertisement(FixAdvertisement msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixExecutionReport(FixExecutionReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderCancelReject(FixOrderCancelReject msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
	public void onFixLogon(FixLogon msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixNews(FixNews msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixEmail(FixEmail msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixNewOrderSingle(FixNewOrderSingle msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixNewOrderList(FixNewOrderList msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderCancelRequest(FixOrderCancelRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderCancelReplaceRequest(
 			FixOrderCancelReplaceRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderStatusRequest(FixOrderStatusRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixAllocationInstruction(FixAllocationInstruction msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixListCancelRequest(FixListCancelRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixListExecute(FixListExecute msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixListStatusRequest(FixListStatusRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixListStatus(FixListStatus msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixAllocationInstructionAck(
 			FixAllocationInstructionAck msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixDontKnowTradeDK(FixDontKnowTradeDK msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixQuoteRequest(FixQuoteRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixQuote(FixQuote msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSettlementInstructions(FixSettlementInstructions msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMarketDataRequest(FixMarketDataRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMarketDataSnapshotFullRefresh(
 			FixMarketDataSnapshotFullRefresh msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMarketDataIncrementalRefresh(
 			FixMarketDataIncrementalRefresh msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMarketDataRequestReject(FixMarketDataRequestReject msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixQuoteCancel(FixQuoteCancel msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixQuoteStatusRequest(FixQuoteStatusRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMassQuoteAcknowledgement(
 			FixMassQuoteAcknowledgement msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityDefinitionRequest(
 			FixSecurityDefinitionRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityDefinition(FixSecurityDefinition msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityStatusRequest(FixSecurityStatusRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityStatus(FixSecurityStatus msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradingSessionStatusRequest(
 			FixTradingSessionStatusRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradingSessionStatus(FixTradingSessionStatus msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMassQuote(FixMassQuote msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixBusinessMessageReject(FixBusinessMessageReject msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixBidRequest(FixBidRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixBidResponse(FixBidResponse msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixListStrikePrice(FixListStrikePrice msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixRegistrationInstructions(
 			FixRegistrationInstructions msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixRegistrationInstructionsResponse(
 			FixRegistrationInstructionsResponse msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderMassCancelRequest(FixOrderMassCancelRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderMassCancelReport(FixOrderMassCancelReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixNewOrderCross(FixNewOrderCross msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixCrossOrderCancelReplaceRequest(
 			FixCrossOrderCancelReplaceRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixCrossOrderCancelRequest(FixCrossOrderCancelRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityTypeRequest(FixSecurityTypeRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityTypes(FixSecurityTypes msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityListRequest(FixSecurityListRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityList(FixSecurityList msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixDerivativeSecurityListRequest(
 			FixDerivativeSecurityListRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixDerivativeSecurityList(FixDerivativeSecurityList msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixNewOrderMultileg(FixNewOrderMultileg msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMultilegOrderCancelReplace(
 			FixMultilegOrderCancelReplace msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradeCaptureReportRequest(
 			FixTradeCaptureReportRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradeCaptureReport(FixTradeCaptureReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderMassStatusRequest(FixOrderMassStatusRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixQuoteRequestReject(FixQuoteRequestReject msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixRFQRequest(FixRFQRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixQuoteStatusReport(FixQuoteStatusReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixQuoteResponse(FixQuoteResponse msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixConfirmation(FixConfirmation msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixPositionMaintenanceRequest(
 			FixPositionMaintenanceRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixPositionMaintenanceReport(
 			FixPositionMaintenanceReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixRequestForPositions(FixRequestForPositions msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixRequestForPositionsAck(FixRequestForPositionsAck msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixPositionReport(FixPositionReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradeCaptureReportRequestAck(
 			FixTradeCaptureReportRequestAck msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradeCaptureReportAck(FixTradeCaptureReportAck msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixAllocationReport(FixAllocationReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixAllocationReportAck(FixAllocationReportAck msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixConfirmation_Ack(FixConfirmation_Ack msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSettlementInstructionRequest(
 			FixSettlementInstructionRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixAssignmentReport(FixAssignmentReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixCollateralRequest(FixCollateralRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixCollateralAssignment(FixCollateralAssignment msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixCollateralResponse(FixCollateralResponse msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixCollateralReport(FixCollateralReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixCollateralInquiry(FixCollateralInquiry msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixNetworkCounterpartySystemStatusRequest(
 			FixNetworkCounterpartySystemStatusRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixNetworkCounterpartySystemStatusResponse(
 			FixNetworkCounterpartySystemStatusResponse msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixUserRequest(FixUserRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixUserResponse(FixUserResponse msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixCollateralInquiryAck(FixCollateralInquiryAck msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixConfirmationRequest(FixConfirmationRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixContraryIntentionReport(FixContraryIntentionReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityDefinitionUpdateReport(
 			FixSecurityDefinitionUpdateReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSecurityListUpdateReport(
 			FixSecurityListUpdateReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixAdjustedPositionReport(FixAdjustedPositionReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixAllocationInstructionAlert(
 			FixAllocationInstructionAlert msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixExecutionAcknowledgement(
 			FixExecutionAcknowledgement msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradingSessionList(FixTradingSessionList msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradingSessionListRequest(
 			FixTradingSessionListRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixSettlementObligationReport(
 			FixSettlementObligationReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixDerivativeSecurityListUpdateReport(
 			FixDerivativeSecurityListUpdateReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixTradingSessionListUpdateReport(
 			FixTradingSessionListUpdateReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMarketDefinitionRequest(FixMarketDefinitionRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMarketDefinition(FixMarketDefinition msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixMarketDefinitionUpdateReport(
 			FixMarketDefinitionUpdateReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixUserNotification(FixUserNotification msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderMassActionReport(FixOrderMassActionReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixOrderMassActionRequest(FixOrderMassActionRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixApplicationMessageRequest(
 			FixApplicationMessageRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixApplicationMessageRequestAck(
 			FixApplicationMessageRequestAck msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixApplicationMessageReport(
 			FixApplicationMessageReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixStreamAssignmentRequest(FixStreamAssignmentRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixStreamAssignmentReport(FixStreamAssignmentReport msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixStreamAssignmentReportACK(
 			FixStreamAssignmentReportACK msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixPartyDetailsListRequest(FixPartyDetailsListRequest msg) {
 		outMsg = msg;
 		
 	}
 
 	@Override
 	public void onFixPartyDetailsListReport(FixPartyDetailsListReport msg) {
 		outMsg = msg;
 		
 	}
 
 	public FixMessage getOutMsg() {
 		return outMsg;
 	}
 }
