 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2009 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.web;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionBindingEvent;
 import javax.servlet.http.HttpSessionBindingListener;
 
 /**
  * The session manager. This class allows keeping track of all active
  * sessions.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class SessionManager implements HttpSessionBindingListener {
 
     /**
     * The list of user agents not supporting cookies. Any session
     * created from one of these user agents should be immediately
     * invalidated to avoid tracking a large number of session
     * objects.
     */
    private static final String[] UNSUPPORTED_AGENTS = {
        "WebDAVFS/", "WebDAVLib/"
    };

    /**
      * The session manager attribute constant. The session value
      * stored in this attribute should be the singleton instance of
      * this session manager.
      */
     private static final String MANAGER_ATTRIBUTE = "manager";
 
     /**
      * The IP address attribute constant. The session value stored
      * in this attribute should be a string containing the source IP
      * address.
      */
     private static final String IP_ATTRIBUTE = "ip";
 
     /**
      * The user agent attribute constant. The session value stored
      * in this attribute should be a string containing the user agent
      * of the browser.
      */
     private static final String USER_AGENT_ATTRIBUTE = "userAgent";
 
     /**
      * The user attribute constant. The session value stored in this
      * attribute should be a string containing the user name.
      */
     private static final String USER_ATTRIBUTE = "user";
 
     /**
      * The map of currently active sessions. Each session is mapped
      * by its unique id in this map. Adding and removing sessions in
      * this map is handled by the session counter object added to
      * each session created.
      */
     private static HashMap sessions = new HashMap();
 
     /**
      * The map of currently active thread sessions. Each running
      * thread is mapped by its hash key to a session in this map.
      * Adding and removing threads in this map is handled by the
      * connectThread() and disconnectThread() methods.
      *
      * @see #connectThread(HttpSession)
      * @see #disconnectThread(HttpSession)
      */
     private static HashMap threads = new HashMap();
 
     /**
      * The single instance of this class.
      */
     private static SessionManager instance = new SessionManager();
 
     /**
      * Returns a collection of the currently active sessions.
      *
      * @return a collection of active HttpSessions
      */
     public static Collection getActiveSessions() {
         return sessions.values();
     }
 
     /**
      * Adds session management to an HTTP session if not already
      * added. This method is safe to call multiple times.
      *
      * @param session         the HTTP session
      * @param ip              the HTTP request IP address
      * @param agent           the HTTP user agent string
      *
      * @throws SecurityException if the session was already bound to
      *             another IP address or user agent string
      */
     public static void manage(HttpSession session, String ip, String agent)
     throws SecurityException {
 
         String  oldIp = getIp(session);
         String  oldAgent = getUserAgent(session);
         String  msg;
 
         if (session.getAttribute(MANAGER_ATTRIBUTE) == null) {
             session.setAttribute(MANAGER_ATTRIBUTE, getInstance());
             if (oldIp == null) {
                 session.setAttribute(IP_ATTRIBUTE, ip);
             } else if (!oldIp.equals(ip)) {
                 msg = "Attempt to re-bind HTTP session from IP '" + oldIp +
                       "' to '" + ip + "', invalidating session";
                 session.invalidate();
                 throw new SecurityException(msg);
             }
             if (oldAgent == null) {
                 session.setAttribute(USER_AGENT_ATTRIBUTE, agent);
             } else if (!oldAgent.equals(agent)) {
                 msg = "Attempt to re-bind HTTP session from user agent '" +
                       oldAgent + "' to '" + agent + "', invalidating session";
                 session.invalidate();
                 throw new SecurityException(msg);
             }
            // Invalidate sessions for browsers not supporting cookies 
            for (int i = 0; i < UNSUPPORTED_AGENTS.length; i++) {
                if (agent.startsWith(UNSUPPORTED_AGENTS[i])) {
                    session.invalidate();
                }
            }
         }
     }
 
     /**
      * Connects an HTTP session to the current thread.
      *
      * @param session        the HTTP session to connect
      */
     public static void connectThread(HttpSession session) {
         synchronized (threads) {
             threads.put(Thread.currentThread(), session);
         }
     }
 
     /**
      * Disconnects an HTTP session from the current thread.
      *
      * @param session        the HTTP session to connect
      */
     public static void disconnectThread(HttpSession session) {
         synchronized (threads) {
             threads.remove(Thread.currentThread());
         }
     }
 
     /**
      * Returns the HTTP session connected to the current thread.
      *
      * @return the HTTP session connected to the current thread, or
      *         null if none has been created
      */
     public static HttpSession getCurrentSession() {
         return getSession(Thread.currentThread());
     }
 
     /**
      * Returns the HTTP session with the specified identifier.
      *
      * @param id             the unique session identifier
      *
      * @return the HTTP session found, or
      *         null if not found
      */
     public static HttpSession getSession(String id) {
         synchronized (sessions) {
             return (HttpSession) sessions.get(id);
         }
     }
 
     /**
      * Returns the HTTP session connected to the specified thread.
      *
      * @param thread         the thread to search for
      *
      * @return the HTTP session found, or
      *         null if not found
      */
     public static HttpSession getSession(Thread thread) {
         synchronized (threads) {
             return (HttpSession) threads.get(thread);
         }
     }
 
     /**
      * Returns the IP address connected to an HTTP session.
      *
      * @param session         the HTTP session
      *
      * @return the IP address, or
      *         null if no IP address has been stored
      */
     public static String getIp(HttpSession session) {
         return (String) session.getAttribute(IP_ATTRIBUTE);
     }
 
     /**
      * Returns the browser user agent connected to an HTTP session.
      *
      * @param session         the HTTP session
      *
      * @return the browser user agent string, or
      *         null if no user agent string has been stored
      */
     public static String getUserAgent(HttpSession session) {
         return (String) session.getAttribute(USER_AGENT_ATTRIBUTE);
     }
 
     /**
      * Returns the user name connected to an HTTP session. The user
      * must have been previously authenticated.
      *
      * @param session         the HTTP session
      *
      * @return the user name, or
      *         null if no user is connected
      */
     public static String getUser(HttpSession session) {
         return (String) session.getAttribute(USER_ATTRIBUTE);
     }
 
     /**
      * Sets the user name connected to an HTTP session. The user must
      * have been previously authenticated. This method will check for
      * attempts to re-bind an HTTP session to a new user, which would
      * be a security violation.
      *
      * @param session        the HTTP session
      * @param user           the user name
      *
      * @throws SecurityException if the session was already bound to
      *             another user name
      */
     public static void setUser(HttpSession session, String user)
         throws SecurityException {
 
         String  old = getUser(session);
         String  msg;
 
         if (old != null && !old.equals(user)) {
             msg = "Attempt to re-bind HTTP session to new user, " +
                   "invalidating session";
             session.invalidate();
             throw new SecurityException(msg);
         } else if (old == null) {
             session.setAttribute(USER_ATTRIBUTE, user);
         }
     }
 
     /**
      * Returns the singleton session manager instance.
      *
      * @return the singleton session manager instance
      */
     private static SessionManager getInstance() {
         return instance;
     }
 
     /**
      * Creates a new session manager.
      */
     private SessionManager() {
         // No further initialization needed
     }
 
     /**
      * Notifies the manager that it is being bound to a session.
      *
      * @param event          the binding event
      */
     public void valueBound(HttpSessionBindingEvent event) {
         HttpSession  session = event.getSession();
 
         synchronized (sessions) {
             sessions.put(session.getId(), session);
         }
     }
 
     /**
      * Notifies the manager that it is being unbound from a
      * session.
      *
      * @param event          the unbinding event
      */
     public void valueUnbound(HttpSessionBindingEvent event) {
         HttpSession  session = event.getSession();
 
         synchronized (sessions) {
             sessions.remove(session.getId());
         }
     }
 }
