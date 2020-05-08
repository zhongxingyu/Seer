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
 
 package org.jinglenodes.jingle.processor;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.jingle.Info;
 import org.jinglenodes.jingle.Jingle;
 import org.jinglenodes.jingle.Reason;
 import org.jinglenodes.jingle.content.Content;
 import org.jinglenodes.jingle.description.Description;
 import org.jinglenodes.jingle.transport.Candidate;
 import org.jinglenodes.jingle.transport.RawUdpTransport;
 import org.jinglenodes.prepare.CallPreparation;
 import org.jinglenodes.prepare.PrepareStatesManager;
 import org.jinglenodes.relay.RelayIQ;
 import org.jinglenodes.session.CallSession;
 import org.jinglenodes.session.CallSessionMapper;
 import org.jinglenodes.sip.GatewayRouter;
 import org.jinglenodes.sip.SipToJingleBind;
 import org.jinglenodes.sip.processor.SipProcessor;
 import org.xmpp.component.NamespaceProcessor;
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.JID;
 import org.xmpp.packet.Packet;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.address.SipURL;
 import org.zoolu.sip.header.ContactHeader;
 import org.zoolu.sip.header.StatusLine;
 import org.zoolu.sip.message.*;
 import org.zoolu.sip.provider.SipProviderInfoInterface;
 
 import javax.sdp.SdpException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class JingleProcessor implements NamespaceProcessor, PrepareStatesManager {
 
     final static Logger log = Logger.getLogger(JingleProcessor.class);
     protected CallSessionMapper callSessionMapper;
     private SipToJingleBind sipToJingleBind;
     private GatewayRouter gatewayRouter;
     private SipProviderInfoInterface sipProviderInfo;
 
     private List<CallPreparation> preparations = new ArrayList<CallPreparation>();
 
     public void init() {
     }
 
     public IQ processIQ(final IQ xmppIQ) {
 
         JingleIQ iq = null;
 
         try {
 
             iq = JingleIQ.fromXml(xmppIQ);
             processJingle(iq);
 
         } catch (JingleException e) {
             if (iq != null) {
                 if (Jingle.SESSION_INITIATE.equals(iq.getJingle().getAction())) {
                     cancelCall(iq, "Not Allowed.", Reason.Type.decline);
                 }
             }
             log.error("Error Processing Jingle", e);
         } catch (Throwable e) {
             log.error("Severe Error Processing Jingle: " + xmppIQ, e);
         }
 
         return null;//IQ.createResultIQ(iq);
 
     }
 
     public void processJingle(final JingleIQ iq) throws JingleException {
 
         final CallSession session = callSessionMapper.addReceivedJingle(iq);
         final String action = iq.getJingle().getAction();
 
         if (action.equals(Jingle.SESSION_INITIATE)) {
             for (final CallPreparation p : preparations) {
                 session.addCallPreparation(p);
             }
             prepareCall(iq, session);
             return;
         }
 
         proceedCall(iq, session);
     }
 
     public void prepareCall(final JingleIQ iq, CallSession session) {
         if (session == null) {
             session = callSessionMapper.getSession(iq);
         }
         if (session != null) {
             final String action = iq.getJingle().getAction();
             if (action.equals(Jingle.SESSION_INITIATE)) {
                 for (CallPreparation preparation = session.popCallPreparation(); preparation != null; preparation = session.popCallPreparation()) {
                     session.addCallProceed(preparation);
                     if (!preparation.prepareInitiate(iq, session)) return;
                 }
             }
             proceedCall(iq, session);
         }
     }
 
     public void proceedCall(final JingleIQ iq, final CallSession session) {
 
         final String action = iq.getJingle().getAction();
 
         if (action.equals(Jingle.SESSION_INITIATE)) {
             executeInitiateProceeds(iq, session);
             sendSipInvite(iq);
         } else if (action.equals(Jingle.SESSION_TERMINATE)) {
             executeTerminateProceeds(iq, session);
             sendSipTermination(iq);
         } else if (action.equals(Jingle.SESSION_INFO)) {
             sendSipRinging(iq);
         } else if (action.equals(Jingle.SESSION_ACCEPT)) {
             executeAcceptProceeds(iq, session);
             sendSipInviteOk(iq);
         }
 
     }
 
     @Override
     public void prepareCall(final Message msg, final CallSession session, final SipChannel channel) {
         // Do Nothing
     }
 
     @Override
     public void proceedCall(final Message msg, final CallSession session, final SipChannel channel) {
         // Do Nothing
     }
 
     private void executeAcceptProceeds(final JingleIQ iq, final CallSession session) {
         for (CallPreparation proceeds : session.getProceeds()) {
             if (!proceeds.proceedAccept(iq, session)) return;
         }
     }
 
     private void executeInitiateProceeds(final JingleIQ iq, final CallSession session) {
         for (CallPreparation proceeds : session.getProceeds()) {
             if (!proceeds.proceedInitiate(iq, session)) return;
         }
     }
 
     private void executeTerminateProceeds(final JingleIQ iq, final CallSession session) {
         for (CallPreparation proceeds : session.getProceeds()) {
             if (!proceeds.proceedTerminate(iq, session)) return;
         }
     }
 
     public GatewayRouter getGatewayRouter() {
         return gatewayRouter;
     }
 
     public SipProviderInfoInterface getSipProviderInfo() {
         return sipProviderInfo;
     }
 
     public final void sendSipInvite(final JingleIQ iq) {
         try {
 
             final Message invite = SipProcessor.createSipInvite(iq.getJingle(), sipProviderInfo);
             invite.setArrivedAt(gatewayRouter.getSipChannel(iq.getFrom().toBareJID()));
 
             final CallSession callSession = callSessionMapper.addSentRequest(invite);
             callSession.setInitiateIQ(iq);
             callSession.addContact(JIDFactory.getInstance().getJID(iq.getJingle().getInitiator()).toBareJID(), invite.getContactHeader());
 
             gatewayRouter.routeSIP(invite, iq.getFrom());
 
         } catch (JingleSipException e) {
             log.error("Jingle/SIP Conversion Error", e);
         } catch (SdpException e) {
             log.error("SDP Parsing Error", e);
         } catch (JingleException e) {
             log.error("Jingle/SIP Conversion Error", e);
         }
     }
 
     public final void sendSipInviteOk(JingleIQ iq) {
         try {
 
             final CallSession callSession = callSessionMapper.getSession(iq);
 
             if (callSession == null) {
                 throw new JingleSipException("No CallSession Found.");
             }
 
             if (callSession.getRelayIQ() != null) {
                 iq = updateJingleTransport(iq, callSession.getRelayIQ());
             }
 
             callSession.setUser(iq.getFrom());
 
             final Message request = callSession.getLastReceivedRequest();
 
             if (request == null) {
                 throw new JingleSipException("No Request Found.");
             }
 
             final Message ok = SipProcessor.createSipOk(request, JIDFactory.getInstance().getJID(iq.getJingle().getResponder()).getResource(), sipProviderInfo);
 
             if (iq.getJingle().getContent() != null) {
                 try {
                     final Content content = iq.getJingle().getContent();
                     final String body = SipProcessor.createSipSDP((Description) content.getDescription(), (RawUdpTransport) content.getTransport(), sipProviderInfo).toString();
                     ok.setBody(body);
                 } catch (SdpException e) {
                     log.error("SDP Parsing Error", e);
                 }
             }
 
             ok.setSendTo(request.getSendTo());
             ok.setArrivedAt(gatewayRouter.getSipChannel(iq.getFrom().toBareJID()));
             callSession.addSentResponse(ok);
             gatewayRouter.routeSIP(ok, callSession.getUser());
         } catch (JingleSipException e) {
             log.warn("Error Sending 200 OK.", e);
         }
     }
 
     public void cancelCall(final JingleIQ iq, final String reasonText) {
         cancelCall(iq, reasonText, Reason.Type.general_error);
     }
 
     public void cancelCall(final JingleIQ iq, final String reasonText, final Reason.Type type) {
         if (iq != null) {
             IQ reply = createJingleTermination(iq, new Reason(reasonText, type));
             reply.setTo(iq.getFrom());
             gatewayRouter.send(reply);
         }
     }
 
     public final void sendSipTermination(final JingleIQ iq) {
         final CallSession callSession = callSessionMapper.getSession(iq);
         if (callSession == null) {
             return;
         }
         sendSipTermination(iq, callSession);
     }
 
     public final void sendSipTermination(final JingleIQ iq, final CallSession callSession) {
         try {
 
             JID from;
             Message lastResponse = callSession.getLastSentResponse();
 
             if (lastResponse != null && lastResponse.getContactHeader() != null) {
 
                 from = Participants.getFromJidForResponse(lastResponse);
 
                 if (sipToJingleBind != null) {
                     from = sipToJingleBind.getXmppTo(from, callSession.getLastReceivedJingle());
                 }
 
                 if (!from.toBareJID().equals(iq.getFrom().toBareJID())) {
                     lastResponse = null;
                 }
 
                 if (sipToJingleBind != null) {
                     iq.setFrom(sipToJingleBind.getSipFrom(iq.getFrom()));
                 }
 
             }
 
             if (lastResponse != null && (lastResponse.isRinging() || lastResponse.isTrying())) {
 
                 if (iq.getJingle().getReason() != null && iq.getJingle().getReason().getType().equals(Reason.Type.media_error)) {
                     lastResponse.setStatusLine(new StatusLine(415, "Unsupported Media Type"));
                 } else {
                     lastResponse.setStatusLine(new StatusLine(480, "Unavailable"));
                 }
 
                 gatewayRouter.routeSIP(lastResponse, callSession.getUser());
 
             } else {
 
                 lastResponse = callSession.getLastReceivedResponse();
 
                 final Message lastSentRequest = callSession.getLastSentRequest();
 
                 if (lastResponse == null || (lastResponse.isRinging() || lastResponse.isTrying())) {
 
                     Message message = callSession.getLastSentRequest();
 
                     if (message == null) {
                         message = callSession.getLastMessage();
                     }
 
                     final Message cancel = SipProcessor.createSipCancel(message);
                     cancel.setSendTo(lastSentRequest.getSendTo());
                     cancel.setArrivedAt(lastSentRequest.getArrivedAt());
                     callSession.addSentRequest(cancel);
                     callSession.setRetries(2);
                     gatewayRouter.routeSIP(cancel, callSession.getUser());
 
                 } else {
 
                     final Message bye = SipProcessor.createSipBye(iq, sipProviderInfo, lastResponse, callSession);
 
                     Message msg = lastSentRequest;
                     if (msg == null) {
                         msg = callSession.getLastReceivedRequest();
                         if (msg == null) {
                             log.debug("Invalid State CallSession to BYE.");
                         }
                     }
 
                     if (msg == null) {
                         log.warn("Could NOT Create BYE Request.");
                         return;
                     }
 
                     bye.setSendTo(msg.getSendTo());
                     bye.setArrivedAt(msg.getArrivedAt());
                     gatewayRouter.routeSIP(bye, callSession.getUser());
 
                     // Send BYE also to contact address
                     final ContactHeader contact = msg.getContactHeader();
                     if (contact != null) {
                         final SipURL url = contact.getNameAddress().getAddress();
                         if (url.getHost() != null && url.getPort() > 0 && url.getPort() < 70000) {
                             //final SocketAddress address = new InetSocketAddress(url.getHost(), url.getPort());
                             gatewayRouter.routeSIP(bye, callSession.getUser());
                             // Burst BYE packet
                             gatewayRouter.routeSIP(bye, callSession.getUser());
                             //bye.setSendTo(address);
                         }
                     }
                 }
             }
         } catch (JingleSipException e) {
             log.warn("Call Termination Error", e);
         } catch (SipParsingException e) {
             log.warn("Call Termination Error", e);
         }
     }
 
     public final void sendSipRinging(final JingleIQ iq) {
         try {
 
             final CallSession callSession = callSessionMapper.getSession(iq);
 
             if (callSession == null) {
                 log.debug("CallSession not found for packet: " + iq.toXML());
                 return;
             }
 
             callSession.setUser(iq.getFrom());
 
             final Message request;
 
             if (callSession.getLastReceivedRequest() != null) {
                 request = callSession.getLastReceivedRequest();
             } else if (callSession.getLastSentRequest() != null) {
                 request = callSession.getLastSentRequest();
             } else {
                 log.info("Original Request not found for packet: " + iq.toXML());
                 return;
             }
 
             final Message ringing = SipProcessor.createSipRinging(request, JIDFactory.getInstance().getJID(iq.getJingle().getResponder()), JIDFactory.getInstance().getJID(iq.getJingle().getResponder()).getResource(), sipProviderInfo);
             ringing.setSendTo(callSession.getLastReceivedRequest().getSendTo());
             ringing.setArrivedAt(callSession.getLastReceivedRequest().getArrivedAt());
             callSessionMapper.getSession(iq).addSentResponse(ringing);
             gatewayRouter.routeSIP(ringing, callSession.getUser());
         } catch (JingleSipException e) {
             log.error("Error sending SIP Ringing.", e);
         }
     }
 
     public final void sendSipOnHold(final JingleIQ iq) {
         try {
 
             if (sipToJingleBind != null) {
                 iq.getJingle().setInitiator(sipToJingleBind.getSipFrom(JIDFactory.getInstance().getJID(iq.getJingle().getInitiator())).toString());
             }
 
             final JingleIQ initiateIq = callSessionMapper.getSession(iq).getInitiateIQ();
             final JID initiator = JIDFactory.getInstance().getJID(iq.getJingle().getInitiator());
             final JID responder = JIDFactory.getInstance().getJID(iq.getJingle().getResponder());
 
             final Description rtpDescription = initiateIq.getJingle().getContent().getDescription();
             final RawUdpTransport transport = initiateIq.getJingle().getContent().getTransport();
 
             final Message invite = SipProcessor.createSipOnHold(initiator, responder,
                     iq.getJingle().getSid(), sipProviderInfo, rtpDescription, transport);
             invite.setArrivedAt(gatewayRouter.getSipChannel(iq.getFrom().toBareJID()));
 
             final CallSession callSession = callSessionMapper.addSentRequest(invite);
             callSession.addContact(initiator.toBareJID(), invite.getContactHeader());
 
             gatewayRouter.routeSIP(invite, iq.getFrom());
 
         } catch (SdpException e) {
             log.error("SDP Parsing Error", e);
         } catch (JingleException e) {
             log.error("Jingle/SIP Conversion Error", e);
         }
     }
 
     public void setSipToJingleBind(SipToJingleBind sipToJingleBind) {
         this.sipToJingleBind = sipToJingleBind;
     }
 
     public CallSessionMapper getCallSessionMapper() {
         return callSessionMapper;
     }
 
     public void setCallSessionMapper(CallSessionMapper callSessionMapper) {
         this.callSessionMapper = callSessionMapper;
     }
 
     public void setGatewayRouter(GatewayRouter gatewayRouter) {
         this.gatewayRouter = gatewayRouter;
     }
 
     public void setSipProviderInfo(SipProviderInfoInterface sipProviderInfo) {
         this.sipProviderInfo = sipProviderInfo;
     }
 
     @Override
     public IQ processIQGet(IQ iq) {
         return null;
     }
 
     @Override
     public IQ processIQSet(IQ iq) {
         return processIQ(iq);
     }
 
     @Override
     public void processIQError(IQ iq) {
 
     }
 
     @Override
     public void processIQResult(IQ iq) {
 
     }
 
     @Override
     public String getNamespace() {
         return Jingle.XMLNS;
     }
 
     public List<CallPreparation> getPreparations() {
         return preparations;
     }
 
     public void setPreparations(List<CallPreparation> preparations) {
         this.preparations = preparations;
     }
 
     public static JingleIQ updateJingleTransport(final JingleIQ iq, final RelayIQ relayIQ) {
         final Candidate c = iq.getJingle().getContent().getTransport().getCandidates().get(0);
         log.debug("Updating Transport: " + iq.toXML() + " with: " + relayIQ.toXML());
         if (c != null) {
             if (!Candidate.RELAY.equals(c.getType())) {
                 c.setIp(relayIQ.getHost());
 
                 String port;
                 if (Jingle.SESSION_INITIATE.equals(iq.getJingle().getAction()) || Jingle.CONTENT_ADD.equals(iq.getJingle().getAction())) {
                     port = relayIQ.getLocalport();
                 } else {
                     port = relayIQ.getRemoteport();
                 }
 
                 c.setPort(port);
                 log.debug("Updated Transport: " + iq.toXML() + " with: " + relayIQ.toXML());
                 return JingleIQ.clone(iq);
             }
         }
         return iq;
     }
 
     public static JingleIQ createJingleInitialization(final JID initiator, final JID responder, final String to, final Content content, final String sid) {
         final Jingle jingle = new Jingle(sid, initiator.toString(), responder.toString(), Jingle.SESSION_INITIATE);
         jingle.setContent(content);
         final JingleIQ iq = new JingleIQ(jingle);
         iq.setTo(to);
         iq.setFrom(initiator);
         return iq;
     }
 
     public static JingleIQ createJingleEarlyMedia(final JID initiator, final JID responder, final String to, final Content content, final String sid) {
         final Jingle jingle = new Jingle(sid, initiator.toString(), responder.toString(), Jingle.CONTENT_ADD);
         jingle.setContent(content);
         final JingleIQ iq = new JingleIQ(jingle);
         iq.setTo(to);
         iq.setFrom(responder);
         return iq;
     }
 
     public static JingleIQ createJingleAccept(final JID initiator, final JID responder, final String to, final Content content, final String sid) {
         final Jingle jingle = new Jingle(sid, initiator.toString(), responder.toString(), Jingle.SESSION_ACCEPT);
         jingle.setContent(content);
         final JingleIQ iq = new JingleIQ(jingle);
         iq.setTo(to);
         iq.setFrom(responder);
         return iq;
     }
 
     public static JingleIQ createJingleTermination(final JID initiator, final JID responder, final String to, final Reason reason, final String sid) {
         final Jingle jingle = new Jingle(sid, initiator.toString(), responder.toString(), Jingle.SESSION_TERMINATE);
         jingle.setReason(reason);
         final JingleIQ iq = new JingleIQ(jingle);
         iq.setTo(to);
         iq.setFrom(responder);
         iq.setType(IQ.Type.set);
         return iq;
     }
 
     public static JingleIQ createJingleTermination(final JingleIQ initiate, final Reason reason) {
         final Jingle jingle = new Jingle(initiate.getJingle().getSid(), initiate.getJingle().getInitiator(), initiate.getJingle().getResponder(), Jingle.SESSION_TERMINATE);
         jingle.setReason(reason);
         final JingleIQ iq = new JingleIQ(jingle);
         iq.setTo(initiate.getFrom());
         iq.setFrom(initiate.getTo());
         return iq;
     }
 
     public static JingleIQ createJingleSessionInfo(final JID initiator, final JID responder, final String to, final String sid, final Info.Type type) throws JingleSipException {
         final Jingle jingle = new Jingle(sid, initiator.toString(), responder.toString(), Jingle.SESSION_INFO);
         jingle.setInfo(new Info());
         final JingleIQ iq = new JingleIQ(jingle);
         iq.setTo(to);
         iq.setFrom(responder);
         return iq;
     }
 
     public void sendJingleTermination(JingleIQ initiateIQ, CallSession session) {
         final JingleIQ terminate = createJingleTermination(initiateIQ, new Reason(Reason.Type.timeout));
         gatewayRouter.send(terminate);
     }
 
     public void send(final Packet packet) {
         gatewayRouter.send(packet);
     }
 
 }
