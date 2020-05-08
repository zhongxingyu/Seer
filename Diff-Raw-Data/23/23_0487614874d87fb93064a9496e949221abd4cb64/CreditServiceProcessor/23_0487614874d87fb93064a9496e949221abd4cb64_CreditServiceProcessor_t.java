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
 
 import java.util.IllegalFormatException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: thiago
  * Date: 3/26/12
  * Time: 1:54 PM
  */
 public class CreditServiceProcessor extends AbstractServiceProcessor {
     private final Logger log = Logger.getLogger(CreditServiceProcessor.class);
     private final Element requestElement;
     private final String xmlns;
     private CallSessionMapper sessionMapper;
     private String creditService;
 
     public CreditServiceProcessor(final String elementName, final String xmlns) {
         this.xmlns = xmlns;
         this.requestElement = DocumentHelper.createElement(new QName(elementName, new Namespace("", xmlns)));
     }
 
     @Override
     public IQ createServiceRequest(Object object, String fromNode, String toNode) {
         if (object instanceof JingleIQ) {
             final IQ request = new IQ(IQ.Type.set);
             if (toNode.indexOf("00") == 0) {
                 toNode = "+" + toNode.substring(2);
             }
             final JID to = JIDFactory.getInstance().getJID(null, creditService, null);
             final JID from = JIDFactory.getInstance().getJID(fromNode, this.getComponentJID().getDomain(), null);
             final JingleIQ jingleIQ = (JingleIQ) object;
             request.setTo(to);
             request.setFrom(from);
             request.setChildElement(requestElement.createCopy());
             final String toBareJid = JIDFactory.getInstance().getJID(toNode, creditService, null).toBareJID();
 
             final Element e = request.getChildElement();
             e.addAttribute("initiator", from.toBareJID());
             e.addAttribute("responder", toBareJid);
             e.addAttribute("sid", jingleIQ.getJingle().getSid());
             log.debug("createCreditRequest: " + request.toXML());
             return request;
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
         final SessionCredit sessionCredit = getSessionCredit(iq.getResult());
         if (iq.getOriginalPacket() instanceof JingleIQ) {
            log.debug("Credit Value Received: " + iq.getResult().toXML());
             final CallSession session = sessionMapper.getSession((JingleIQ) iq.getOriginalPacket());
             if (session != null) {
                 session.setSessionCredit(sessionCredit);
             }
         }
     }
 
     /*
       * Create the SessionCredit
       * the information retrieved from the iq
       */
     protected SessionCredit getSessionCredit(final IQ iq) {
         String credit = null;
         SessionCredit sessionCredit = new SessionCredit(SessionCredit.RouteType.pstn);
 
        log.debug("Get Credit Value Received: " + iq.toXML());

        Element e = iq.getChildElement();
        credit = e.attributeValue("maxseconds");
        if (credit != null) {
            try {
                final int seconds = Integer.parseInt(credit);
                sessionCredit.setMaxDurationInSeconds(seconds);
            } catch (IllegalFormatException ife) {
                log.error("Invalid Credit Value Received: " + iq.toXML(), ife);
             }
         }
 
         return sessionCredit;
     }
 
     @Override
     protected void handleError(IqRequest iqRequest) {
         log.error("Failed to Retrieve Account: " + iqRequest.getResult().toXML());
         final SessionCredit sessionCredit = new SessionCredit(SessionCredit.RouteType.pstn);
 
         if (iqRequest.getOriginalPacket() instanceof JingleIQ) {
             final CallSession session = sessionMapper.getSession((JingleIQ) iqRequest.getOriginalPacket());
             if (session != null) {
                 session.setSessionCredit(sessionCredit);
             }
         }
     }
 
     @Override
     protected void handleTimeout(IqRequest request) {
 
     }
 
     @Override
     public String getNamespace() {
         return xmlns;
     }
 
     public String getCreditService() {
         return creditService;
     }
 
     public void setCreditService(String creditService) {
         this.creditService = creditService;
     }
 
     public CallSessionMapper getSessionMapper() {
         return sessionMapper;
     }
 
     public void setSessionMapper(CallSessionMapper sessionMapper) {
         this.sessionMapper = sessionMapper;
     }
 
 }
