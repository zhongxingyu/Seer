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
 
 package org.jinglenodes.credit;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.callkiller.CallKiller;
 import org.jinglenodes.jingle.Reason;
 import org.jinglenodes.prepare.CallPreparation;
 import org.jinglenodes.prepare.PrepareStatesManager;
 import org.jinglenodes.session.CallSession;
 import org.xmpp.component.IqRequest;
 import org.xmpp.component.ResultReceiver;
 import org.xmpp.component.ServiceException;
 import org.xmpp.packet.JID;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.message.JIDFactory;
 import org.zoolu.sip.message.Message;
 import org.zoolu.sip.message.SipChannel;
 
 /**
  * Created by IntelliJ IDEA.
  * User: thiago
  * Date: 3/26/12
  * Time: 1:52 PM
  */
 
 public class ChargePreparation extends CallPreparation implements ResultReceiver {
 
     final Logger log = Logger.getLogger(ChargePreparation.class);
     private ChargeServiceProcessor chargeServiceProcessor;
 
     @Override
     public boolean prepareInitiate(JingleIQ iq, CallSession session) {
         return true;
     }
 
     @Override
     public boolean proceedInitiate(JingleIQ iq, CallSession session) {
         return true;
     }
 
     @Override
     public boolean proceedTerminate(JingleIQ iq, CallSession session) {
         log.debug("Credit Proceed Terminate: " + iq.toXML() + " - " + session.getId());
         setSessionFinishTime(session, System.currentTimeMillis());
         chargeCall(iq, session);
         return true;
     }
 
     private void chargeCall(JingleIQ iq, CallSession session) {

        if(session.getSessionCredit() == null){
            final SessionCredit sessionCredit = new SessionCredit(SessionCredit.RouteType.pstn);
            session.setSessionCredit(sessionCredit);
        }

        if (!session.getSessionCredit().isCharged()) {
             JID initiator = JIDFactory.getInstance().getJID(iq.getJingle().getInitiator());
             JID responder = JIDFactory.getInstance().getJID(iq.getJingle().getResponder());
             if (initiator != null && responder != null) {
                 if (chargeServiceProcessor != null) {
                     try {
                         chargeServiceProcessor.queryService(iq, initiator.getNode(), responder.getNode(), this);
                     } catch (ServiceException e) {
                         log.error("Could NOT Query Charge Service.", e);
                     }
                 } else {
                     log.error("Charge Error: Charge Service Processor is null");
                 }
             } else {
                 log.error("Charge Error: Could NOT Retrieve Call Info");
             }
         }
     }
 
     @Override
     public boolean proceedAccept(JingleIQ iq, CallSession session) {
        setSessionStartTime(session, System.currentTimeMillis());
         return true;
     }
 
     @Override
     public void proceedInfo(JingleIQ iq, CallSession session) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private void setSessionStartTime(final CallSession session, final long time) {
         if (session != null) {
             session.setStartTime(time);
         }
     }
 
     private void setSessionFinishTime(final CallSession session, final long time) {
         if (session != null) {
             session.setFinishTime(time);
         }
     }
 
     @Override
     public boolean prepareInitiate(Message msg, CallSession session, final SipChannel sipChannel) {
         return true;
     }
 
     @Override
     public JingleIQ proceedSIPInitiate(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     @Override
     public void proceedSIPInfo(JingleIQ iq, CallSession session, SipChannel channel) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public JingleIQ proceedSIPEarlyMedia(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     @Override
     public JingleIQ proceedSIPTerminate(JingleIQ iq, CallSession session, SipChannel channel) {
         setSessionFinishTime(session, System.currentTimeMillis());
         chargeCall(iq, session);
         return iq;
     }
 
     @Override
     public JingleIQ proceedSIPAccept(JingleIQ iq, CallSession session, SipChannel channel) {
        setSessionStartTime(session, System.currentTimeMillis());
         return iq;
     }
 
     public ChargeServiceProcessor getChargeServiceProcessor() {
         return chargeServiceProcessor;
     }
 
     public void setChargeServiceProcessor(ChargeServiceProcessor chargeServiceProcessor) {
         log.debug("Added Charge Service Processor");
         this.chargeServiceProcessor = chargeServiceProcessor;
     }
 
     @Override
     public void receivedResult(IqRequest iqRequest) {
         log.info("Charged Call: " + iqRequest.getRequest());
     }
 
     @Override
     public void receivedError(IqRequest iqRequest) {
         log.error("Failed to Charge: " + iqRequest.getRequest());
     }
 
     @Override
     public void timeoutRequest(IqRequest iqRequest) {
         log.error("Timeout to Charge: " + iqRequest.getRequest());
     }
 }
