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
 
 package org.jinglenodes.session;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.jingle.processor.JingleException;
 import org.xmpp.packet.JID;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.header.CallIdHeader;
 import org.zoolu.sip.header.FromHeader;
 import org.zoolu.sip.header.ToHeader;
 import org.zoolu.sip.message.JIDFactory;
 import org.zoolu.sip.message.Message;
 import org.zoolu.sip.message.Participants;
 import org.zoolu.sip.message.SipParsingException;
 import org.zoolu.tools.ConcurrentTimelineHashMap;
 import org.zoolu.tools.NamingThreadFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Default Implementation for CallSession Mapper
  *
  * @author Thiago Rocha Camargo (thiago@jinglenodes.org) - Jingle Nodes
  *         Date: Mar 7, 2012
  *         Time: 8:12:10 AM
  */
 public class DefaultCallSessionMapper implements CallSessionMapper {
     final private static Logger log = Logger.getLogger(DefaultCallSessionMapper.class);
     final protected ConcurrentTimelineHashMap<String, CallSession> sessionMap = new ConcurrentTimelineHashMap<String, CallSession>();
     final protected ScheduledThreadPoolExecutor purgeTimer;
     final private int maxSessionTtl; // in Seconds
     final private int unfinishedSessionTtl;
    final protected int purgeTime; // in Seconds
 
     public DefaultCallSessionMapper() {
         this(1500, 120, 200);
     }
 
     public DefaultCallSessionMapper(final int maxSessionTtl, final int purgeTime, final int unfinishedSessionTtl) {
         this.purgeTime = purgeTime;
         this.maxSessionTtl = maxSessionTtl;
         this.unfinishedSessionTtl = unfinishedSessionTtl;
         this.purgeTimer = new ScheduledThreadPoolExecutor(5, new NamingThreadFactory("Session Cleaner Thread"));
         this.purgeTimer.scheduleWithFixedDelay(new SessionCleanerTask(sessionMap, purgeTimer, 1000, SessionCleanerTask.inactiveSessionFilter), purgeTime, purgeTime, TimeUnit.SECONDS);
         this.purgeTimer.scheduleWithFixedDelay(new SessionCleanerTask(sessionMap, purgeTimer, -1, SessionCleanerTask.inactiveSessionFilter), purgeTime * 15, purgeTime * 15, TimeUnit.SECONDS);
         this.purgeTimer.scheduleWithFixedDelay(new SessionCleanerTask(sessionMap, purgeTimer, -1, SessionCleanerTask.sessionTtlFilter), purgeTime * 20, purgeTime * 20, TimeUnit.SECONDS);
     }
 
     public CallSession getSession(final String id) {
         if (id == null) {
             return null;
         }
         final CallSession callSession;
         callSession = sessionMap.get(id);
         return callSession;
     }
 
     public void addSession(final CallSession callSession) {
         sessionMap.put(callSession.getId(), callSession);
     }
 
     protected CallSession createSession(final Message message) throws JingleException {
         JID user;
         try {
             final Participants participants = Participants.getParticipants(message);
             user = participants.getInitiator();
         } catch (SipParsingException e) {
             user = null;
         }
         final String id = getSessionId(message);
         log.trace("Creating callSession. Id: " + id);
         final CallSession callSession = new CallSession(id, user);
         addSession(callSession);
         return callSession;
     }
 
     protected CallSession createSession(final JingleIQ jingle) {
         final String id = getSessionId(jingle);
         log.trace("Creating callSession. Id: " + id);
         final CallSession callSession = new CallSession(id, new JID(jingle.getJingle().getInitiator()));
         addSession(callSession);
         return callSession;
     }
 
     public void removeSession(final CallSession callSession) {
         if (callSession != null) {
             sessionMap.remove(callSession.getId());
         }
     }
 
     public CallSession getSession(final Message message) throws JingleException {
         CallSession callSession = null;
         final String id = getSessionId(message);
         if (id != null) {
             callSession = getSession(id);
         }
         return callSession;
     }
 
     public CallSession getSession(final JingleIQ iq) {
         CallSession callSession = null;
         final String id = getSessionId(iq);
         if (id != null) {
             callSession = getSession(id);
         }
         return callSession;
     }
 
     public CallSession addSentRequest(final Message message) throws JingleException {
         CallSession callSession = getSession(message);
         if (callSession != null) {
             callSession.addSentRequest(message);
         } else {
             callSession = createSession(message);
             callSession.addSentRequest(message);
         }
         return callSession;
     }
 
     public CallSession addSentResponse(final Message message) throws JingleException {
         CallSession callSession = getSession(message);
         if (callSession != null) {
             callSession.addSentResponse(message);
         } else {
             callSession = createSession(message);
             callSession.addSentResponse(message);
         }
         return callSession;
     }
 
     public CallSession addReceivedRequest(final Message message) throws JingleException {
         CallSession callSession = getSession(message);
         if (callSession != null) {
             callSession.addReceivedRequest(message);
         } else {
             callSession = createSession(message);
             callSession.addReceivedRequest(message);
         }
         return callSession;
     }
 
     public CallSession addReceivedResponse(final Message message) throws JingleException {
         CallSession callSession = getSession(message);
         if (callSession != null) {
             callSession.addReceivedResponse(message);
         } else {
             callSession = createSession(message);
             callSession.addReceivedResponse(message);
         }
         return callSession;
     }
 
     public CallSession addSentJingle(final JingleIQ jingle) {
         CallSession callSession = getSession(jingle);
         if (callSession != null) {
             callSession.addSentJingle(jingle);
         } else {
             callSession = createSession(jingle);
             callSession.addSentJingle(jingle);
         }
         return callSession;
     }
 
     public CallSession addReceivedJingle(final JingleIQ jingle) {
         CallSession callSession = getSession(jingle);
         if (callSession != null) {
             callSession.addReceivedJingle(jingle);
         } else {
             callSession = createSession(jingle);
             callSession.addReceivedJingle(jingle);
         }
         return callSession;
     }
 
     public String getSessionId(final Message message) throws JingleException {
         if (message != null) {
             final CallIdHeader header = message.getCallIdHeader();
             if (header != null) {
                 final String cid = header.getCallId();
                final String p="null";
                return cid+"x"+p;
             }
             throw new JingleException("Could NOT Calculate CallSession ID:" + message);
         }
         throw new JingleException("Could NOT Calculate CallSession ID.");
     }
 
     public String getHigher(final String a, final String b){
         final String p;
 
         if(a.compareToIgnoreCase(b)>=0){
             p=a;
         }else {
             p=b;
         }
 
         return p;
     }
 
     public String getSessionId(final JingleIQ iq) {
         final String id = iq.getJingle().getSid();
        final String p = "null";
        return id+"x"+p;
     }
 
     public Collection<CallSession> getSessions() {
         return sessionMap.values();
     }
 
     public List<CallSession> getTimeoutSessions(final int ms, final int max) {
 
         final List<CallSession> timeout = new ArrayList<CallSession>();
         final long now = System.currentTimeMillis();
 
         try {
             int i = 0;
             for (final CallSession callSession : sessionMap.values()) {
 
                 if (!callSession.isActive()) {
                     sessionMap.remove(callSession.getId());
                     continue;
                 }
                 final long delta = now - callSession.getLastTimestamp();
                 if (delta > ms) {
                     timeout.add(callSession);
                 }
                 if (i++ > max) {
                     break;
                 }
             }
         } catch (Throwable e) {
             log.error("Severe Error when getting timed out CallSession.", e);
         }
 
         return timeout;
     }
 
     public void clearSessionFromUser(final JID user) {
         if (user == null) {
             return;
         }
 
         try {
             for (final CallSession callSession : sessionMap.values()) {
                 for (final JID u : callSession.getUsers()) {
                     if (u != null) {
                         if (u.toBareJID().equals(user.toBareJID())) {
                             callSession.deactivate();
                             sessionMap.remove(callSession.getId());
                         }
                     }
                 }
             }
        } catch (final Throwable e) {
             log.error("Severe Error when cleaning up Sessions.", e);
         }
     }
 
     public int getPendingSessionCount() {
         return sessionMap.size();
     }
 
     public int getSessionCount() {
         return sessionMap.size();
     }
 
     public void destroy() {
         clear();
         purgeTimer.shutdownNow();
     }
 
     public void clear() {
         sessionMap.clear();
     }
 
     public int getUnfinishedSessionTtl() {
         return unfinishedSessionTtl;
     }
 
     public int getMaxSessionTtl() {
         return maxSessionTtl;
     }
 }
