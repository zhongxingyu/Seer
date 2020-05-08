 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.session;
 
 import java.io.IOException;
 import java.security.SecureRandom;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.nginious.http.HttpCookie;
 import com.nginious.http.HttpRequest;
 import com.nginious.http.HttpResponse;
 import com.nginious.http.HttpSession;
 import com.nginious.http.common.PathParameters;
 
 /**
  * A HTTP session manager which stores session in memory map. Each HTTP session is identified by
  * a session id which can be used to retrieve the HTTP session in later HTTP requests. The session id
  * is unique per session and is randomly generated. The session id is stored in a HTTP cookie which is
  * sent to the server in subsequent requests and is used by this session manager to retrieve the
  * HTTP session. 
  * 
  * <p>
  * A separate scavenger thread is used to periodically traverse the memory map to identify and remove
  * unused HTTP session. A HTTP session is considered unused if it has not been used for more than 30
  * minutes.
  * </p>
  * 
  * @author Bojan Pisler, NetDigital Sweden AB
  *
  */
 public class HttpInMemorySessionManager implements HttpSessionManager {
 	
 	private static final long SLEEP_TIME_MILLIS = 30000L;
 	
 	private static final long MAX_INACTIVE_TIME_MILLIS = 1800000L;
 	
 	private static final String COOKIE_NAME = "JSESSIONID";
 	
 	private ConcurrentHashMap<String, HttpSessionImpl> sessions;
 	
 	private Random random;
 	
 	private HttpInMemorySessionScavenger scavenger;
 	
 	private Thread scavengerThread;
 	
 	/**
 	 * Constructs a new HTTP in memory session manager.
 	 */
 	public HttpInMemorySessionManager() {
 		this.sessions = new ConcurrentHashMap<String, HttpSessionImpl>();
 		
 		try {
 			this.random = new SecureRandom();
 		} catch(Exception e) {
 			this.random = new Random();
 		}
 	}
 	
 	/**
 	 * Starts this HTTP in memory session manager.
 	 */
 	public void start() {
 		if(this.scavenger != null) {
 			return;
 		}
 		
 		this.scavenger = new HttpInMemorySessionScavenger();
 		this.scavengerThread = new Thread(this.scavenger);
 		scavengerThread.setName("HttpInMemorySessionScavenger");
 		scavengerThread.start();
 	}
 	
 	/**
 	 * Stops this HTTP in memory session manager.
 	 */
 	public void stop() {
 		if(this.scavenger == null) {
 			return;
 		}
 		
 		scavenger.stop();
 		scavengerThread.interrupt();
 		this.scavenger = null;
 		this.scavengerThread = null;
 		sessions.clear();
 	}
 	
 	/**
 	 * Gets HTTP session from in memory map using the session id found in a cookie from the specified
 	 * HTTP request. If no HTTP session is found and the specified create flag is <code>true</code> a
 	 * new HTTP session is created.
 	 * 
 	 * @param request the HTTP request
 	 * @param create whether or not to create a new session if no cookies exists in HTTP request
 	 * @throws IOException if unable to get HTTP session
 	 */
 	public HttpSession getSession(HttpRequest request, boolean create) {
 		HttpCookie cookie = request.getCookie(COOKIE_NAME);
 		HttpSessionImpl session = null;
 		String sessionId = null;
 		
 		if(cookie != null) {
 			sessionId = cookie.getValue();
 			session = sessions.get(sessionId);
 			
 			if(session != null) {
 				session.setLastAccessedTime();
 			}
 		}
 		
 		while(create && session == null) {
 			sessionId = createSessionId();
 			session = new HttpSessionImpl();
 			
 			HttpSession existingSession = sessions.putIfAbsent(sessionId, session);
 			
 			if(existingSession != null) {
 				sessionId = null;
 				session = null;
 			} else {
 				session.setSessionId(sessionId);
 			}
 		}
 		
 		return session;
 	}
 	
 	/**
 	 * Stores the specified HTTP session. A session cookie is created and added to the specified
 	 * HTTP request.
 	 * 
 	 * @param request the HTTP request
 	 * @param response the HTTP response
 	 * @param session the HTTP session
 	 */
 	public void storeSession(HttpRequest request, HttpResponse response, HttpSession session) {
 		PathParameters params = new PathParameters(request);
 		String path = params.get(0);
 		
		if(path == null) {
 			path = "/";
 		} else {
 			path = "/" + path;
 		}
 		
 		if(session.isInvalidated()) {
 			HttpCookie cookie = new HttpCookie();
 			cookie.setName(COOKIE_NAME);
 			cookie.setPath(path);
 			cookie.setMaxAge(-HttpSessionConstants.MAX_AGE);
 			cookie.setValue(session.getSessionId());
 			response.addCookie(cookie);
 			sessions.remove(session.getSessionId());
 		} else if(session.isNew()) {
 			HttpCookie cookie = new HttpCookie();
 			cookie.setName(COOKIE_NAME);
 			cookie.setValue(session.getSessionId());
 			cookie.setPath(path);
 			cookie.setMaxAge(HttpSessionConstants.MAX_AGE);
 			response.addCookie(cookie);
 		}
 	}
 	
 	/*
 	 * Creates unique session id
 	 */
 	private String createSessionId() {
 		boolean found = false;
 		String id = null;
 		
 		while(!found) {
 			long part1 = random.nextLong();
 			long part2 = random.nextLong();
 			id = Long.toString(part1, 16) + Long.toString(part2, 16);
 			found = !sessions.containsKey(id);
 		}
 		
 		return id;
 	}
 	
 	private class HttpInMemorySessionScavenger implements Runnable {
 		
 		private boolean stopped;
 		
 		HttpInMemorySessionScavenger() {
 			super();
 		}
 		
 		void stop() {
 			this.stopped = true;
 		}
 		
 		public void run() {
 			while(!this.stopped) {
 				try {
 					Thread.sleep(SLEEP_TIME_MILLIS);
 				} catch(InterruptedException e) {}
 				
 				if(!this.stopped) {
 					Iterator<String> sessionIds = sessions.keySet().iterator();
 					
 					while(sessionIds.hasNext() && !this.stopped) {
 						String sessionId = sessionIds.next();
 						HttpSession session = sessions.get(sessionId);
 						long lastAccessedTime = session.getLastAccessedTime();
 						
 						if(lastAccessedTime + MAX_INACTIVE_TIME_MILLIS < System.currentTimeMillis()) {
 							sessions.remove(sessionId);
 						}
 					}
 				}
 			}
 		}
 	}
 }
