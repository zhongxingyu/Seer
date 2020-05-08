 /*
  * Copyright (C) 2011 - Jingle Nodes - Yuilop - Neppo
  *
  *   This file is part of Switji (http://jinglenodes.org)
  *
  *   Switji is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   Switji is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with MjSip; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *   Author(s):
  *   Benhur Langoni (bhlangonijr@gmail.com)
  *   Thiago Camargo (barata7@gmail.com)
  */
 
 package org.jinglenodes.sip.processor;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.jingle.Info;
 import org.jinglenodes.jingle.Jingle;
 import org.jinglenodes.jingle.Reason;
 import org.jinglenodes.jingle.content.Content;
 import org.jinglenodes.jingle.description.Description;
 import org.jinglenodes.jingle.description.Payload;
 import org.jinglenodes.jingle.processor.JingleException;
 import org.jinglenodes.jingle.processor.JingleProcessor;
 import org.jinglenodes.jingle.processor.JingleSipException;
 import org.jinglenodes.jingle.transport.Candidate;
 import org.jinglenodes.jingle.transport.RawUdpTransport;
 import org.jinglenodes.prepare.CallPreparation;
 import org.jinglenodes.prepare.PrepareStatesManager;
 import org.jinglenodes.session.CallSession;
 import org.jinglenodes.session.CallSessionMapper;
 import org.jinglenodes.sip.GatewayRouter;
 import org.jinglenodes.sip.SipPacketProcessor;
 import org.jinglenodes.sip.SipToJingleBind;
 import org.xmpp.packet.JID;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.address.NameAddress;
 import org.zoolu.sip.address.SipURL;
 import org.zoolu.sip.header.CSeqHeader;
 import org.zoolu.sip.header.ContactHeader;
 import org.zoolu.sip.header.Header;
 import org.zoolu.sip.header.ToHeader;
 import org.zoolu.sip.message.*;
 import org.zoolu.sip.provider.SipProviderInfoInterface;
 
 import javax.sdp.*;
 import javax.sdp.fields.AttributeField;
 import java.net.SocketAddress;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class SipProcessor implements SipPacketProcessor, PrepareStatesManager {
     private static final Logger log = Logger.getLogger(SipProcessor.class);
     public static final String emptyAddress = "0.0.0.0";
     private SipProviderInfoInterface sipProviderInfo;
     private GatewayRouter gatewayRouter;
     private CallSessionMapper callSessions;
     private JingleProcessor jingleProcessor;
     private SipToJingleBind sipToJingleBind;
     private boolean forceMapper = false;
 
     private List<CallPreparation> preparations = new ArrayList<CallPreparation>();
 
     public void processSip(final org.zoolu.sip.message.Message msg, final SipChannel sipChannel) {
         try {
             final CallSession callSession = msg.isRequest() ? callSessions.addReceivedRequest(msg) : callSessions.addReceivedResponse(msg);
 
             if (msg.isInvite() && msg.isRequest()) {
                 if (!isReInvite(msg)) {
                     for (final CallPreparation p : preparations) {
                         callSession.addCallPreparation(p);
                     }
                     prepareCall(msg, callSession, sipChannel);
                     return;
                 }
             }
             proceedCall(msg, callSession, sipChannel);
         } catch (JingleException e) {
             log.error("Could not Process Packet", e);
         }
     }
 
     @Override
     public void prepareCall(final Message msg, CallSession session, final SipChannel sipChannel) {
 
         for (CallPreparation preparation = session.popCallPreparation(); preparation != null; preparation = session.popCallPreparation()) {
             session.addCallProceed(preparation);
             if (!preparation.prepareInitiate(msg, session, sipChannel)) return;
         }
 
        proceedCall(msg, session, sipChannel);
     }
 
     @Override
     public void proceedCall(Message msg, CallSession session, final SipChannel sipChannel) {
         try {
 
             final CSeqHeader ch = msg.getCSeqHeader();
             if (msg.isRegister() || (ch != null && ch.getMethod().equals(SipMethods.REGISTER))) {
                 return;
             }
             final int statusLineCode = msg.getStatusLine() != null ? msg.getStatusLine().getCode() : -1;
 
             // 200 OK
             if (statusLineCode >= 200 && statusLineCode < 300 && ch != null && ch.getMethod() != null) {
                 if (ch.getMethod().equals(SipMethods.INVITE)) {
                     process2xxSip(msg);
                 }
             }
             // Bye Request
             else if (msg.isBye()) {
                 processByeSip(msg, sipChannel);
             }
             // CANCEL Request
             else if (msg.isCancel()) {
                 processCancelSip(msg, sipChannel);
             }
             // Invite Request
             else if (msg.isInvite() && msg.isRequest()) {
                 if (isReInvite(msg)) {
                     processReInviteSip(msg, sipChannel);
                 } else {
                     processInviteSip(msg, sipChannel);
                 }
             }
             // Ringing
             else if (msg.isRinging()) {
                 processRingingSip(msg);
             }
             // Ack
             else if (msg.isAck()) {
                 processAckSip(msg);
             }
             // 486 BUSY, 408 TIMEOUT, 483 MANY HOPS
             else if (statusLineCode > -1) {
                 processFailSip(msg, sipChannel);
             } else if (msg.isOption()) {
                 processSipOption(msg);
             }
         } catch (JingleException e) {
             log.error("Could not Parse Packet", e);
         } catch (Throwable e) {
             log.error("Severe Error Processing SIP Packet: " + msg, e);
         }
 
     }
 
     @Override
     public void prepareCall(JingleIQ iq, CallSession session) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void proceedCall(JingleIQ iq, CallSession session) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public SipProviderInfoInterface getSipProviderInfo() {
         return sipProviderInfo;
     }
 
     public void setSipProviderInfo(final SipProviderInfoInterface sipProviderInfo) {
         this.sipProviderInfo = sipProviderInfo;
     }
 
     public GatewayRouter getGatewayRouter() {
         return gatewayRouter;
     }
 
     public void setGatewayRouter(GatewayRouter gatewayRouter) {
         this.gatewayRouter = gatewayRouter;
     }
 
     public boolean isForceMapper() {
         return forceMapper;
     }
 
     public void setForceMapper(boolean forceMapper) {
         this.forceMapper = forceMapper;
     }
 
     public CallSessionMapper getCallSessions() {
         return callSessions;
     }
 
     public void setCallSessions(CallSessionMapper callSessions) {
         this.callSessions = callSessions;
     }
 
     public JingleProcessor getJingleProcessor() {
         return jingleProcessor;
     }
 
     public void setJingleProcessor(JingleProcessor jingleProcessor) {
         this.jingleProcessor = jingleProcessor;
     }
 
     public boolean isReInvite(final org.zoolu.sip.message.Message msg) throws JingleException {
 
         final CallSession callSession = callSessions.getSession(msg);
 
         if (callSession == null) {
             return false;
         }
 
         final org.zoolu.sip.message.Message lastResponse = callSession.getLastReceivedResponse();
         if (lastResponse == null) {
             return false;
         }
 
         final int statusLineCode = lastResponse.getStatusLine() != null ? lastResponse.getStatusLine().getCode() : -1;
         final CSeqHeader ch = msg.getCSeqHeader();
 
         return statusLineCode >= 200 && statusLineCode < 300 && ch != null && ch.getMethod() != null;
 
     }
 
     protected void process2xxSip(final org.zoolu.sip.message.Message msg) throws JingleException {
         sendJingleAccepted(msg);
         sendSipAck(msg);
 
     }
 
     protected void processByeSip(final org.zoolu.sip.message.Message msg, final SipChannel sipChannel) throws JingleException {
         sendJingleTerminate(msg, sipChannel);
         sendSipOk(msg);
         callSessions.removeSession(callSessions.getSession(msg));
 
     }
 
     protected void processCancelSip(final org.zoolu.sip.message.Message msg, final SipChannel sipChannel) throws JingleException {
         sendJingleTerminate(msg, sipChannel);
         sendSipOk(msg);
         callSessions.removeSession(callSessions.getSession(msg));
 
     }
 
     protected void processInviteSip(final org.zoolu.sip.message.Message msg, final SipChannel sipChannel) throws JingleException {
         sendSipTrying(msg);
         sendJingleInitialization(msg, sipChannel);
 
     }
 
     protected void processReInviteSip(final org.zoolu.sip.message.Message msg, final SipChannel sipChannel) throws JingleException {
         final CallSession callSession = callSessions.getSession(msg);
         JingleIQ iq = callSession.getLastSentJingle();
         jingleProcessor.sendSipInviteOk(iq);
     }
 
     protected void processRingingSip(final org.zoolu.sip.message.Message msg) throws JingleException {
         final int statusLineCode = msg.getStatusLine() != null ? msg.getStatusLine().getCode() : -1;
         sendJingleRinging(msg);
         if (statusLineCode == 183) {
             sendJingleEarlyMedia(msg);
         }
     }
 
     protected void processAckSip(final org.zoolu.sip.message.Message msg) throws JingleException {
         // Fix Contact Address Update
         if (msg.getContactHeader() != null) {
             Participants p = null;
             try {
                 p = msg.getParticipants();
             } catch (SipParsingException e) {
                 log.error("Error Processing ACK.", e);
             }
             final CallSession t = callSessions.getSession(msg);
             if (p != null && p.getInitiator() != null) {
                 t.addContact(p.getInitiator().toBareJID(), msg.getContactHeader());
             }
         }
     }
 
     protected void process100Sip(final org.zoolu.sip.message.Message msg) throws JingleException {
     }
 
     protected void processFailSip(final org.zoolu.sip.message.Message msg, final SipChannel sipChannel) throws JingleException {
         final int statusLineCode = msg.getStatusLine() != null ? msg.getStatusLine().getCode() : -1;
         boolean matches = false;
 
         for (final int c : SipResponses.ackRequiredCodes) {
             if (statusLineCode == c) {
                 matches = true;
                 break;
             }
         }
         if (matches) {
             if (msg.isInvite() || (msg.getCSeqHeader() != null && SipMethods.INVITE.equals(msg.getCSeqHeader().getMethod()))) {
                 sendJingleTerminate(msg, sipChannel);
             }
             sendSipAck(msg);
             callSessions.removeSession(callSessions.getSession(msg));
         }
 
     }
 
     protected void processSipOption(final org.zoolu.sip.message.Message msg) throws JingleException {
         sendSipOk(msg);
     }
 
     public final void sendSipTrying(final Message msg) {
         try {
             final Message ringing = createSipTrying(msg, null);
             callSessions.addSentResponse(ringing);
             ringing.setSendTo(msg.getSendTo());
             ringing.setArrivedAt(msg.getArrivedAt());
             gatewayRouter.routeSIP(ringing, null);
         } catch (JingleSipException e) {
             log.error("Error Sending Trying", e);
         } catch (JingleException e) {
             log.error("Error Sending Trying", e);
         }
     }
 
     public final void sendJingleRinging(final Message msg) {
 
 
         final Participants participants;
 
         try {
             participants = msg.getParticipants();
 
             final JID initiator = participants.getInitiator();
             final JID responder = participants.getResponder();
             JID to = initiator;
 
             final CallSession callSession = callSessions.getSession(msg);
             if (callSession != null) {
                 if (sipToJingleBind != null) {
                     to = sipToJingleBind.getXmppTo(initiator, callSession.getLastReceivedJingle());
                 }
 
                 for (final JID usr : callSession.getUsers()) {
                     if (to.toBareJID().equals(usr.toBareJID())) {
                         to = usr;
                     }
                 }
             }
 
             final JingleIQ iq = JingleProcessor.createJingleSessionInfo(initiator, responder, to.toString(), msg.getCallIdHeader().getCallId(), Info.Type.ringing);
 
             callSessions.addSentJingle(iq);
             gatewayRouter.send(iq);
         } catch (JingleException e) {
             log.error("Error Creating Ring Packet", e);
         } catch (SipParsingException e) {
             log.error("Error Sending Trying", e);
         } catch (JingleSipException e) {
             log.error("Error Creating Ring Packet", e);
         }
     }
 
     public final void sendJingleAccepted(final Message msg) {
 
         final Participants participants;
         try {
 
             try {
                 participants = msg.getParticipants();
             } catch (SipParsingException e) {
                 log.error("Error Processing 200OK.", e);
                 return;
             }
 
             final JID initiator = participants.getInitiator();
             JID responder = participants.getResponder();
             JID to = initiator;
 
             final CallSession callSession = callSessions.getSession(msg);
             if (callSession != null) {
                 if (sipToJingleBind != null) {
                     to = sipToJingleBind.getXmppTo(initiator, callSession.getLastReceivedJingle());
                 }
                 for (final JID usr : callSession.getUsers()) {
                     if (to.toBareJID().equals(usr.toBareJID())) {
                         to = usr;
                     }
                 }
                 if (callSession.getLastReceivedJingle() != null) {
                     //TODO Dynamic Configure XMPP Route
                     responder = new JID(callSession.getLastReceivedJingle().getJingle().getResponder().split("@")[0] + "@sip.yuilop.tv");
                 }
             }
 
             final Content content = getContent(msg.getBody());
             JingleIQ iq = JingleProcessor.createJingleAccept(initiator, responder, to.toString(), content, msg.getCallIdHeader().getCallId());
 
             if (callSession != null) {
                 log.debug("Trying to Update Transport...");
                 if (callSession.getRelayIQ() != null) {
                     iq = JingleProcessor.updateJingleTransport(iq, callSession.getRelayIQ());
                 } else {
                     log.debug("Trying to Update Transport... Failed. No RelayIQ");
                 }
             } else {
                 log.debug("Trying to Update Transport... Failed. No Session Found!");
             }
 
             callSessions.addSentJingle(iq);
             gatewayRouter.send(iq);
         } catch (JingleSipException e) {
             log.error("Error creating Session-accept packet", e);
         } catch (JingleException e) {
             log.error("Error Sending Trying", e);
         }
     }
 
     public final void sendJingleTerminate(final Message msg, final SipChannel sipChannel) {
 
         try {
 
             final CallSession callSession = callSessions.getSession(msg);
             Participants mainParticipants;
 
             try {
                 mainParticipants = msg.getParticipants();
             } catch (SipParsingException e) {
                 log.debug("Error Processing BYE.", e);
                 return;
             }
 
             if (callSession == null) {
                 log.debug("CallSession not found for packet: " + msg.toString());
                 return;
             }
 
             JID initiator;
             JID responder;
             JID to;
 
             if (callSession.getLastSentJingle() != null) {
                 initiator = new JID(callSession.getLastSentJingle().getJingle().getInitiator());
                 responder = new JID(callSession.getLastSentJingle().getJingle().getResponder());
             } else if (callSession.getLastReceivedJingle() != null) {
                 initiator = new JID(callSession.getLastReceivedJingle().getJingle().getInitiator());
                 responder = new JID(callSession.getLastReceivedJingle().getJingle().getResponder());
             } else {
                 log.info("Invalid CallSession to Terminate.");
                 return;
             }
 
             if (msg.isRequest()) {
                 to = mainParticipants.getResponder();
             } else {
                 to = mainParticipants.getInitiator();
             }
 
             if (initiator.getResource() == null) {
                 if (initiator.toBareJID().equals(mainParticipants.getInitiator().toBareJID())) {
                     initiator = mainParticipants.getInitiator();
                 } else {
                     initiator = mainParticipants.getResponder();
                 }
             } else if (responder.getResource() == null) {
                 if (responder.toBareJID().equals(mainParticipants.getResponder().toBareJID())) {
                     responder = mainParticipants.getResponder();
                 } else {
                     responder = mainParticipants.getInitiator();
                 }
             }
 
             // Try last Message as a last resort.
 
             final Message lastMsg = callSession.getLastMessage();
             if (lastMsg != null) {
                 mainParticipants = lastMsg.getParticipants();
             }
 
             if (mainParticipants == null) {
                 log.info("Invalid Participants on Message: " + msg);
                 return;
             }
 
             if (initiator.getResource() == null) {
                 if (initiator.toBareJID().equals(mainParticipants.getInitiator().toBareJID())) {
                     initiator = mainParticipants.getInitiator();
                 } else {
                     initiator = mainParticipants.getResponder();
                 }
             } else if (responder.getResource() == null) {
                 if (responder.toBareJID().equals(mainParticipants.getResponder().toBareJID())) {
                     responder = mainParticipants.getResponder();
                 } else {
                     responder = mainParticipants.getInitiator();
                 }
             }
 
             if (sipChannel != null && sipChannel.getId() != null) {
                 to = JIDFactory.getInstance().getJID(sipChannel.getId());
             } else {
                 if (sipToJingleBind != null) {
                     to = sipToJingleBind.getXmppTo(to, callSession.getLastReceivedJingle());
                 }
             }
 
             for (final JID usr : callSession.getUsers()) {
                 if (to.toBareJID().equals(usr.toBareJID())) {
                     to = usr;
                 }
             }
 
             if (to.getResource() == null) {
                 if (to.toBareJID().equals(mainParticipants.getInitiator().toBareJID())) {
                     to = mainParticipants.getInitiator();
                 } else if (to.toBareJID().equals(mainParticipants.getResponder().toBareJID())) {
                     to = mainParticipants.getResponder();
                 }
             }
 
             final int code = getCode(msg);
 
             final Reason reason = getReason(msg, code);
 
             final JingleIQ terminate = JingleProcessor.createJingleTermination(initiator, responder, to.toString(), reason, msg.getCallIdHeader().getCallId());
 
             callSessions.addSentJingle(terminate);
             gatewayRouter.send(terminate);
 
         } catch (JingleException e) {
             log.debug("Error Processing BYE.", e);
         } catch (SipParsingException e) {
             log.debug("Error Processing BYE.", e);
         }
 
     }
 
     public static int getCode(final Message msg) {
         return msg.getStatusLine() != null ? msg.getStatusLine().getCode() : -1;
     }
 
     public static Reason getReason(final Message msg) {
         final int code = getCode(msg);
         return getReason(msg, code);
     }
 
     public static Reason getReason(final Message msg, final int code) {
         final Reason reason;
         switch (code) {
             case 602:
                 reason = new Reason(code + " - " + msg.getStatusLine().getReason(), Reason.Type.decline);
                 break;
             case 402:
                 reason = new Reason((code) + " - " + msg.getStatusLine().getReason(), Reason.Type.general_error);
                 break;
             case 483:
             case 404:
             case 420:
             case 405:
             case 414:
             case 484:
             case 485:
                 reason = new Reason((code) + " - " + msg.getStatusLine().getReason(), Reason.Type.connectivity_error);
                 break;
             case 486:
             case 480:
                 reason = new Reason((code) + " - " + msg.getStatusLine().getReason(), Reason.Type.busy);
                 break;
             case 415:
                 reason = new Reason((code) + " - " + msg.getStatusLine().getReason(), Reason.Type.media_error);
                 break;
             case 403:
                 reason = new Reason((code) + " - " + msg.getStatusLine().getReason(), Reason.Type.security_error);
                 break;
             case 487:
             case -1:
                 reason = new Reason(Reason.Type.no_error);
                 break;
             default:
                 reason = new Reason(code > 0 ? (code) + " - " + msg.getStatusLine().getReason() : String.valueOf(code), Reason.Type.no_error);
         }
         return reason;
     }
 
     public final void sendJingleInitialization(final Message msg, final SipChannel sipChannel) {
 
         try {
 
             //TODO Query and Inject Relay
 
             final Participants participants = msg.getParticipants();
 
             if (participants == null) {
                 log.info("Invalid Participants on Message: " + msg);
                 return;
             }
 
 
             JID initiator = participants.getInitiator();
             final JID responder = participants.getResponder();
             JID to = responder;
 
             if (sipChannel != null && sipChannel.getId() != null && !isForceMapper()) {
                 to = JIDFactory.getInstance().getJID(sipChannel.getId());
             } else {
                 if (sipToJingleBind != null) {
                     to = sipToJingleBind.getXmppTo(responder, null);
                 }
             }
 
             final Content content = getContent(msg.getBody());
 
             // Added to Support Display Name Translation
             final String display = msg.getFromHeader().getNameAddress().getDisplayName();
 
             if (display != null && !display.trim().equals("")) {
                 content.setName(display.trim());
             }
 
             final JingleIQ initialization = JingleProcessor.createJingleInitialization(initiator, responder, to.toString(), content, msg.getCallIdHeader().getCallId());
 
             initialization.setTo(to);
             final CallSession callSession = callSessions.addSentJingle(initialization);
             callSession.setInitiateIQ(initialization);
             callSession.addContact(initiator.toBareJID(), msg.getContactHeader());
 
             gatewayRouter.send(initialization);
 
         } catch (JingleSipException e) {
             log.error("Error Processing INVITE.", e);
         } catch (SipParsingException e) {
             log.debug("Error Processing INVITE.", e);
         } catch (Throwable e) {
             log.debug("SEVERE - Error Processing INVITE.", e);
         }
     }
 
     public final void sendJingleEarlyMedia(final Message msg) {
 
 
         try {
             final Participants participants;
 
             participants = msg.getParticipants();
 
             final JID initiator = participants.getInitiator();
             final JID responder = participants.getResponder();
             JID to = initiator;
 
             final CallSession callSession = callSessions.getSession(msg);
             if (callSession != null) {
                 if (sipToJingleBind != null) {
                     to = sipToJingleBind.getXmppTo(initiator, callSession.getLastReceivedJingle());
                 }
                 for (final JID usr : callSession.getUsers()) {
                     if (to.toBareJID().equals(usr.toBareJID())) {
                         to = usr;
                     }
                 }
             }
 
             final Content content = getContent(msg.getBody());
 
             final JingleIQ iq = JingleProcessor.createJingleEarlyMedia(initiator, responder, to.toString(), content, msg.getCallIdHeader().getCallId());
 
             callSessions.addSentJingle(iq);
             gatewayRouter.send(iq);
         } catch (JingleSipException e) {
             log.error("Error Creating Ring Packet", e);
         } catch (JingleException e) {
             log.error("Error Sending Trying", e);
         } catch (SipParsingException e) {
             log.error("Error Sending Trying", e);
         }
 
 
     }
 
     public final void sendSipOk(final Message msg) {
         try {
             final Message response = createSipOk(msg, sipProviderInfo);
             response.setSendTo(msg.getSendTo());
             response.setArrivedAt(msg.getArrivedAt());
             gatewayRouter.routeSIP(response, null);
         } catch (JingleSipException e) {
             log.error("Error Creating SIP OK", e);
         }
     }
 
     public final void sendSipAck(final Message msg) {
         final Message ack = createSipAck(msg, sipProviderInfo);
         ack.setSendTo(msg.getSendTo());
         ack.setArrivedAt(msg.getArrivedAt());
         gatewayRouter.routeSIP(ack, null);
     }
 
     public final void sendSipNon2xxAck(final Message msg) {
         final Message ack = createSipNon2xxAck(msg, sipProviderInfo);
         ack.setSendTo(msg.getSendTo());
         ack.setArrivedAt(msg.getArrivedAt());
         gatewayRouter.routeSIP(ack, null);
     }
 
     public void setSipToJingleBind(SipToJingleBind sipToJingleBind) {
         this.sipToJingleBind = sipToJingleBind;
     }
 
     public void processSipPacket(ByteBuffer byteBuffer, SocketAddress address, SipChannel channel) {
         try {
             byteBuffer.rewind();
             final byte[] bytes = new byte[byteBuffer.remaining()];
             byteBuffer.get(bytes);
             if (bytes.length < 40) {
                 return;
             }
 
             final Message message = new Message(bytes, 0, bytes.length);
             message.setArrivedAt(channel);
             message.setSendTo(address);
             message.setArrivedAt(channel);
             processSip(message, channel);
         } catch (Throwable e) {
             log.error("Severe Error Parsing SIP Message.", e);
         }
     }
 
     public static enum MediaDirection {
         both,
         sendonly,
         recvonly,
         none
     }
 
     public static Message createSipInvite(final JID initiator, final JID responder, final String sid, final SipProviderInfoInterface sipProvider, final Description rtpDescription, final RawUdpTransport transport) throws SdpException {
         final String contact = getContact(initiator.getNode(), sipProvider);
         final SessionDescription description = createSipSDP(rtpDescription, transport, sipProvider);
         final String to = responder.toBareJID();
         final String from = initiator.toBareJID();
         return MessageFactory.createInviteRequest(sipProvider, new SipURL(to), new NameAddress(to.split("@")[0], new SipURL(to)), new NameAddress(from, new SipURL(from)), new NameAddress(new SipURL(contact)), description.toString(), sid, initiator.getResource());
     }
 
     public static Message createSipOnHold(final JID initiator, final JID responder, final String sid, final SipProviderInfoInterface sipProvider, final Description rtpDescription, final RawUdpTransport transport) throws SdpException {
         final String contact = getContact(initiator.getNode(), sipProvider);
         final SessionDescription description = createSipSDP(rtpDescription, transport, sipProvider);
         description.setAttribute("c", emptyAddress);
         description.setAttribute("s", "");
         final String to = responder.toBareJID();
         final String from = initiator.toBareJID();
         return MessageFactory.createInviteRequest(sipProvider, new SipURL(to), new NameAddress(to.split("@")[0], new SipURL(to)), new NameAddress(from, new SipURL(from)), new NameAddress(new SipURL(contact)), description.toString(), sid, initiator.getResource());
     }
 
     public static String getContact(final String node, final SipProviderInfoInterface sipProvider) {
         return node + "@" + sipProvider.getIP() + ":" + sipProvider.getPort() + ";transport=udp";
     }
 
     public static SessionDescription createSipSDP(final Description rtpDescription, final RawUdpTransport transport, final SipProviderInfoInterface sipProvider) throws SdpException {
         return createSipSDP(rtpDescription, transport, sipProvider, MediaDirection.both);
     }
 
     public static SessionDescription createSipSDP(final Description rtpDescription, final RawUdpTransport transport, final SipProviderInfoInterface sipProvider, final MediaDirection mediaDirection) throws SdpException {
 
         final SessionDescription description = SdpFactory.getInstance().createSessionDescription(sipProvider);
 
         final int[] ids = new int[rtpDescription.getPayloads().size()];
         int i = 0;
         final List<String> names = new ArrayList<String>();
         final List<String> values = new ArrayList<String>();
 
         for (final Payload payload : rtpDescription.getPayloads()) {
             ids[i++] = Integer.parseInt(payload.getId());
             names.add("rtpmap");
             values.add(payload.getId() + " " + payload.getName() + (payload.getClockrate() > -1 ? "/" + payload.getClockrate() : "") + (payload.getChannels() > -1 ? "/" + payload.getChannels() : ""));
             // Fix for G729 prevent VAD support
 
             if (payload.equals(Payload.G729)) {
                 names.add("fmtp");
                 values.add(String.valueOf(payload.getId()) + " annexb=no");
             }
         }
 
         if (transport.getCandidates().size() < 1) {
             throw new SdpException("No Transports Found in Jingle Packet.");
         }
 
         final MediaDescription md = SdpFactory.getInstance().createMediaDescription(rtpDescription.getMedia(), Integer.parseInt(transport.getCandidates().get(0).getPort()), 1, "RTP/AVP", ids);
         md.addDynamicPayloads(names, values);
 
         final AttributeField af = new AttributeField();
         if (!MediaDirection.both.equals(mediaDirection)) {
             af.setValueAllowNull(mediaDirection.toString());
             md.addAttribute(af);
         } else {
             af.setValueAllowNull("sendrecv");
             md.addAttribute(af);
         }
 
         final List<MediaDescription> mv = new ArrayList<MediaDescription>();
         mv.add(md);
         description.setMediaDescriptions(mv);
 
         String ip = transport.getCandidates().get(0).getIp();
         ip = ip == null || ip.trim().length() < 7 ? sipProvider.getIP() : ip;
 
         final Origin origin = SdpFactory.getInstance().createOrigin("J2S", ip);
         origin.setSessionId(3);
         origin.setSessionVersion(1);
         description.setOrigin(origin);
         description.setConnection(SdpFactory.getInstance().createConnection(ip));
 
         return description;
 
     }
 
     public static Message createSipAck(final Message response, final SipProviderInfoInterface sipProvider) {
         return MessageFactory.create2xxAckRequest(sipProvider, response, null);
     }
 
     public static Message createSipNon2xxAck(final Message response, final SipProviderInfoInterface sipProvider) {
         SipURL requestUri = response.getContactHeader() != null ? response.getContactHeader().getNameAddress().getAddress() : response.getFromHeader().getNameAddress().getAddress();
         return MessageFactory.createNon2xxAckRequest(sipProvider, response, requestUri);
     }
 
     public static Message createSipOk(final Message request, final String fromTag, final SipProviderInfoInterface sipProvider) throws JingleSipException {
         final String contact = getContact(request.getToHeader().getNameAddress().getAddress().getUserName(), sipProvider);
         return MessageFactory.createResponse(request, 200, SipResponses.reasonOf(200), fromTag, new NameAddress(contact), "audio", "");
     }
 
     public static Message createSipOk(final Message request, final SipProviderInfoInterface sipProvider) throws JingleSipException {
         final ToHeader toHeader = request.getToHeader();
         if (toHeader != null) {
             final NameAddress nameAddress = toHeader.getNameAddress();
             if (nameAddress != null) {
                 final SipURL address = nameAddress.getAddress();
                 if (address != null) {
                     final String contact = getContact(address.getUserName(), sipProvider);
                     return MessageFactory.createResponse(request, 200, SipResponses.reasonOf(200), toHeader.getTag(), new NameAddress(contact), "audio", "");
                 }
                 throw new JingleSipException("Could NOT get Address for: " + request);
             }
             throw new JingleSipException("Could NOT get NameAddress for: " + request);
         }
         throw new JingleSipException("Could NOT get ToHeader for: " + request);
     }
 
     public static Message createSipBye(final JID initiator, final JID responder, final String sid, final SipProviderInfoInterface sipProvider, final Message lastMessage, final NameAddress requestURI) {
         return MessageFactory.createByeRequest(sipProvider, new NameAddress(new SipURL(responder.toBareJID())), new NameAddress(new SipURL(initiator.toBareJID())), sid, initiator.getResource(), responder.getResource(), lastMessage, requestURI);
     }
 
     public static Message createSipCancel(final Message lastMessage) throws JingleSipException {
         if (lastMessage == null) {
             throw new JingleSipException("Failed to create CANCEL Request. No arguments.");
         }
         final Message cancel = MessageFactory.createCancelRequest(lastMessage);
         if (cancel == null) {
             throw new JingleSipException("Failed to create CANCEL Request. Null return.");
         }
         return cancel;
     }
 
     public static Message createSipInvite(final Jingle iq, final SipProviderInfoInterface sipProvider) throws JingleSipException, SdpException {
 
         // Checks to verify if the conversion is supported
         if (!iq.getAction().equals(Jingle.SESSION_INITIATE)) {
             throw new JingleSipException("The IQ MUST have a session-initiate action.");
         }
         if (iq.getContent() == null) {
             throw new JingleSipException("Session Description Required.");
         }
 
         final Content content = iq.getContent();
 
         // Checks to verify if the conversion is supported
         if (!(content.getDescription() instanceof Description)) {
             throw new JingleSipException("Only RTP Session Description Supported.");
         }
 
         final Description description = content.getDescription();
 
         // Checks to verify if the conversion is supported
         if (!(content.getTransport() instanceof RawUdpTransport)) {
             throw new JingleSipException("Only RAW Transport Supported.");
         }
 
         final RawUdpTransport transport = content.getTransport();
 
         return createSipInvite(JIDFactory.getInstance().getJID(iq.getInitiator()), JIDFactory.getInstance().getJID(iq.getResponder()), iq.getSid(), sipProvider, description, transport);
 
     }
 
     public static Message createSipBye(final JingleIQ iq, final SipProviderInfoInterface sipProvider, final Message lastResponse, final CallSession callSession) throws JingleSipException, SipParsingException {
 
         // Checks to verify if the conversion is supported
         if (!iq.getJingle().getAction().equals(Jingle.SESSION_TERMINATE)) {
             throw new JingleSipException("The IQ MUST have a session-terminate action.");
         }
 
         if (lastResponse == null) {
             throw new JingleSipException("No related Message Found.");
         }
 
         final Participants p;
 
         p = Participants.getParticipants(lastResponse);
 
         final JID from;
         final JID to;
 
         if (iq.getFrom().toBareJID().equals(p.getResponder().toBareJID())) {
             from = p.getResponder();
             to = p.getInitiator();
         } else {
             from = p.getInitiator();
             to = p.getResponder();
         }
 
         final ContactHeader contactHeader = callSession.getContactHeader(to.toBareJID());
         NameAddress requestURI = null;
         if (contactHeader != null) {
             requestURI = contactHeader.getNameAddress();
         }
 
         final Message bye = createSipBye(from, to, iq.getJingle().getSid(), sipProvider, lastResponse, requestURI);
         if (bye == null) {
             throw new JingleSipException("Failed to create BYE Request.");
         }
         return bye;
 
     }
 
     public static Message createSipRinging(final Message req, final JID from, final String fromTag, final SipProviderInfoInterface sipProvider) throws JingleSipException {
         final NameAddress contact = new NameAddress(getContact(from.getNode(), sipProvider));
         return MessageFactory.createResponse(req, 180, BaseSipResponses.reasonOf(180), fromTag, contact, "audio", "");
     }
 
     public static Message createSipTrying(final Message req, final String fromTag) throws JingleSipException {
         return MessageFactory.createResponse(req, 100, BaseSipResponses.reasonOf(100), fromTag, null, "audio", "");
     }
 
     public static Content getContent(final String sdpDescription) throws JingleSipException {
 
         final Description rtpDescription;
         final RawUdpTransport rawTransport;
 
         if (sdpDescription == null) {
             throw new JingleSipException("SDP Parsing Error. SDP Missing.");
         }
 
         try {
             final SessionDescription sdp = SdpFactory.getInstance().createSessionDescription(sdpDescription);
             final List m = sdp.getMediaDescriptions(true);
             final List<Payload> sdpPayloads = new ArrayList<Payload>();
 
             // Ignore Extra Contents
             final MediaDescription md = (MediaDescription) m.get(0);
 
             final HashMap<Integer, Payload> payloads = new HashMap<Integer, Payload>();
 
             final List atbs = md.getAttributes(false);
             for (final Object atb : atbs) {
                 if (atb instanceof AttributeField) {
                     final AttributeField codec = (AttributeField) atb;
                     final String key = codec.getName();
                     final String value = codec.getValue();
 
                     if ("rtpmap".equals(key)) {
                         try {
                             String c[] = value.split(" ");
                             final int id = Integer.valueOf(c[0]);
                             c = c[1].split("/");
                             final String name = c[0];
                             int rate = -1;
                             int channels = 1;
 
                             if (c.length > 1) {
                                 rate = Integer.valueOf(c[1]);
                                 if (c.length > 2) {
                                     channels = Integer.valueOf(c[2]);
                                 }
                             }
 
                             final Payload p = new Payload(c[0], name, rate, channels);
                             payloads.put(id, p);
 
                         } catch (Exception e) {
                             // Ignore Payload
                         }
                     }
                 }
             }
 
             final List p = md.getMedia().getMediaFormats(false);
 
             if (p == null) {
                 throw new JingleSipException("SDP Parsing Error.");
             }
 
             // Calculate Payloads that Matches with Supported Payloads
             for (final Object aP : p) {
                 final String mm = (String) aP;
                 final int id = Integer.valueOf(mm);
                 Payload payload = Payload.getPayload(id);
                 if (payload != null)
                     sdpPayloads.add(payload);
             }
 
             final String type = md.getMedia().getMediaType();
 
             rtpDescription = new Description(type);
             rtpDescription.addPayload(sdpPayloads);
             rawTransport = new RawUdpTransport(new Candidate(sdp.getConnection().getAddress(), String.valueOf(md.getMedia().getMediaPort()), "0"));
             return new Content("initiator", sdp.getOrigin().getUsername(), "both", rtpDescription, rawTransport);
 
         } catch (SdpParseException e) {
             throw new JingleSipException("SDP Parsing Error.");
         } catch (SdpException e) {
             throw new JingleSipException("SDP Parsing Error.");
         } catch (Throwable e) {
             throw new JingleSipException("Critical SDP Parsing Error:" + sdpDescription);
         }
     }
 
     public void setPreparations(List<CallPreparation> preparations) {
         this.preparations = preparations;
     }
 }
