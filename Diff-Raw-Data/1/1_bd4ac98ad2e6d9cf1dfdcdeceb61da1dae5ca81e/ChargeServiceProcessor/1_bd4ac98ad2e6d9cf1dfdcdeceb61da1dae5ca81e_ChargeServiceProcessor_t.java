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
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.dom4j.QName;
 import org.jinglenodes.session.CallSession;
 import org.jinglenodes.session.CallSessionMapper;
 import org.xmpp.component.AbstractServiceProcessor;
 import org.xmpp.component.IqRequest;
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.JID;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.message.JIDFactory;
 
 /**
  * Created by IntelliJ IDEA.
  * User: thiago
  * Date: 3/26/12
  * Time: 1:54 PM
  */
 public class ChargeServiceProcessor extends AbstractServiceProcessor {
     private final Logger log = Logger.getLogger(ChargeServiceProcessor.class);
     private final Element requestElement;
     private final String xmlns;
     private CallSessionMapper sessionMapper;
     private String chargeService;
 
     public ChargeServiceProcessor(final String elementName, final String xmlns) {
         this.xmlns = xmlns;
         this.requestElement = DocumentHelper.createElement(new QName(elementName, new Namespace("", xmlns)));
     }
 
     @Override
     public IQ createServiceRequest(Object object, String fromNode, String toNode) {
         if (object instanceof JingleIQ) {
             final JingleIQ jingleIQ = (JingleIQ) object;
             final CallSession session = sessionMapper.getSession(jingleIQ);
             if (session != null) {
                 final SessionCredit credit = session.getSessionCredit();
                 if (credit != null && !credit.isCharged()) {
                     if (toNode.indexOf("00") == 0) {
                         toNode = "+" + toNode.substring(2);
                     }
                     final JID to = JIDFactory.getInstance().getJID(null, chargeService, null);
                     final JID from = JIDFactory.getInstance().getJID(fromNode, this.getComponentJID().getDomain(), null);
                     final IQ request = new IQ(IQ.Type.set);
                     request.setTo(to);
                     request.setFrom(from);
                     final int callTime = credit.getStartTime() == 0 ? 0 : (int) Math.ceil((credit.getFinishTime() - credit.getStartTime()) / 1000);
                     final String toBareJid = JIDFactory.getInstance().getJID(toNode, chargeService, null).toBareJID();
 
                     final Element e = requestElement.createCopy();
                     e.addAttribute("initiator", from.toBareJID());
                     e.addAttribute("responder", toBareJid);
                     e.addAttribute("seconds", String.valueOf(callTime));
                    e.addAttribute("sid", session.getId());
                     request.setChildElement(e);
                     log.debug("createdCreditRequest: " + request.toXML());
                     credit.setCharged(true);
                     return request;
                 } else {
                     log.error("No credit found for creating Charge");
                 }
             } else {
                 log.error("No session found for creating Charge");
             }
         }
         return null;
     }
 
     @Override
     protected String getRequestId(Object obj) {
         if (obj instanceof JingleIQ) {
             final JingleIQ iq = (JingleIQ) obj;
             return iq.getJingle().getSid();
         }
         return null;
     }
 
     @Override
     protected void handleResult(IqRequest iq) {
         if (iq.getOriginalPacket() instanceof JingleIQ) {
             final CallSession session = sessionMapper.getSession((JingleIQ) iq.getOriginalPacket());
             if (session != null) {
                 final SessionCredit credit = session.getSessionCredit();
                 if (credit != null) {
                     credit.setCharged(true);
                 }
             }
         }
     }
 
     @Override
     protected void handleError(IqRequest iq) {
         log.error("Failed to Charge Account: " + iq.getResult().toXML());
     }
 
     @Override
     protected void handleTimeout(IqRequest request) {
 
     }
 
     @Override
     public String getNamespace() {
         return xmlns;
     }
 
     public String getChargeService() {
         return chargeService;
     }
 
     public void setChargeService(String chargeService) {
         this.chargeService = chargeService;
     }
 
     public CallSessionMapper getSessionMapper() {
         return sessionMapper;
     }
 
     public void setSessionMapper(CallSessionMapper sessionMapper) {
         this.sessionMapper = sessionMapper;
     }
 }
