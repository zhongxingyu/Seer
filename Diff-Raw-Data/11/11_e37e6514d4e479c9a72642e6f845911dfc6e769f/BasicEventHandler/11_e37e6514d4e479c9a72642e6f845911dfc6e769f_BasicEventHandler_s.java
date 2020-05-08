 /*
  * Copyright 2013 Simon Taddiken
  *
  * This file is part of Polly HTTP API.
  *
  * Polly HTTP API is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, either version 3 of the License, or (at 
  * your option) any later version.
  *
  * Polly HTTP API is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for 
  * more details.
  *
  * You should have received a copy of the GNU General Public License along 
  * with Polly HTTP API. If not, see http://www.gnu.org/licenses/.
  */
 package de.skuzzle.polly.http.internal;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 import de.skuzzle.polly.http.api.AlternativeAnswerException;
 import de.skuzzle.polly.http.api.HttpCookie;
 import de.skuzzle.polly.http.api.HttpEvent.RequestMode;
 import de.skuzzle.polly.http.api.handler.HttpEventHandler;
 import de.skuzzle.polly.http.api.HttpException;
 import de.skuzzle.polly.http.api.HttpServer;
 import de.skuzzle.polly.http.api.answers.DefaultAnswers;
 import de.skuzzle.polly.http.api.answers.HttpAnswer;
 import de.skuzzle.polly.http.api.answers.HttpAnswerHandler;
 
 
 
 class BasicEventHandler implements HttpHandler {
 
     private final HttpServerImpl server;
     
     
     
     /**
      * Regex for splitting GET style parameters from the request uri
      */
     private final static Pattern GET_PARAMETERS = Pattern.compile(
         "(\\w+)=([^&]+)");
     
     
     
     public BasicEventHandler(HttpServerImpl server) {
         this.server = server;
     }
     
     
     
     private final void parseParameters(String in, Map<String, String> params) {
         final Matcher m = GET_PARAMETERS.matcher(in);
     
         while (m.find()) {
             final String key = in.substring(m.start(1), m.end(1));
             String value = in.substring(m.start(2), m.end(2));
             try {
                 value = URLDecoder.decode(value, this.server.getEncoding());
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
 
             if (params.containsKey(key)) {
                 final String val = params.get(key);
                 params.put(key, val + ";" + value);
             } else {
                 params.put(key, value);
             }
         }
     }
 
 
 
     private final void parsePostParameters(HttpExchange t, Map<String, String> result) 
             throws IOException {
         BufferedReader r = null;
         try {
             r = new BufferedReader(new InputStreamReader(t.getRequestBody(), 
                     this.server.getEncoding()));
             String line = null;
             while ((line = r.readLine()) != null) {
                 if (!line.equals("")) {
                     parseParameters(line, result);
                 }
             }
         } finally {
             if (r != null) {
                 r.close();
             }
         }
     }
 
 
 
     private final Map<String, String> parseCookies(HttpExchange t) {
         final List<String> cookies = t.getRequestHeaders().get("Cookie");
         if (cookies == null) {
             return Collections.emptyMap();
         }
         final Map<String, String> result = new HashMap<String, String>();
         for (final String cookie : cookies) {
             final String[] s = cookie.split("=");
             if (s.length != 2) {
                 continue;
             }
             result.put(s[0], s[1]);
         }
         return result;
     }
     
     
     
     private final HttpEventImpl createEvent(final HttpExchange t) throws IOException {
         final String requestUri = t.getRequestURI().toString();
         final Map<String, String> get = new HashMap<>();
         final Map<String, String> post = new HashMap<>();
         
         
         // set stream to count incoming traffic
         final TrafficInformationImpl temporary = new TrafficInformationImpl(null);
         final CountingInputStream in = new CountingInputStream(
             t.getRequestBody(), temporary);
         t.setStreams(in, null);
         
         
         // extract GET parameters
         final int questIdx = requestUri.indexOf('?');
         final String plainUri;
         if (questIdx != -1) {
             final String[] parts = requestUri.split("\\?", 2);
             this.parseParameters(parts[1], get);
             plainUri = requestUri.substring(0, questIdx);
         } else {
             plainUri = requestUri;
         }
         
         // extract POST parameters
         this.parsePostParameters(t, post);
 
         // extract cookies
         final Map<String, String> cookies = this.parseCookies(t);
         
         // get session
         final HttpSessionImpl session;
         switch (this.server.getSessionType()) {
         case HttpServer.SESSION_TYPE_COOKIE:
             session = this.server.byID(t, cookies);
             break;
         case HttpServer.SESSION_TYPE_GET:
             session = this.server.byID(t, get);
             break;
         case HttpServer.SESSION_TYPE_IP:
             session = this.server.byIP(t.getRemoteAddress());
             break;
         default:
             // should not be reachable anyway
             throw new RuntimeException("illegal session type: " + 
                 this.server.getSessionType());
         }
         
         final RequestMode mode;
         final String m = t.getRequestMethod().toLowerCase();
         if (m.equals("get")) {
             mode = RequestMode.GET;
         } else {
             mode = RequestMode.POST;
         }
         
         // as session is resolved now, update the session's traffic
         final TrafficInformationImpl ti = session.getTrafficInfo();
         ti.updateFrom(temporary);
         
         final HttpEventImpl event = new HttpEventImpl(this.server, mode, 
             t.getRequestURI(), t.getRemoteAddress(), plainUri, session, cookies, 
             get, post) {
             
             public void discard() {
                 super.discard();
                 t.close();
             }
         };
         return event;
     }
     
     
     
     @Override
     public void handle(HttpExchange t) throws IOException {
 
         this.server.cleanSessions();
         // copy list to avoid further synchronization
         final HttpEventImpl httpEvent = this.createEvent(t);
         this.server.fireOnRequest(httpEvent);
         
         final HttpSessionImpl session = (HttpSessionImpl) httpEvent.getSession();
         session.addEvent(httpEvent.copy());
         session.setLastAction(new Date());
         
         // handle the event
         final List<HttpEventHandler> handler;
         final String registered;
         synchronized (this.server.getHandlers()) {
             registered = this.server.getHandlers().findKey(httpEvent.getPlainUri());
             handler = new ArrayList<>(
                 this.server.getHandlers().get(registered));
         }
         
         if (handler == null || handler.isEmpty()) {
             this.handleAnswer(DefaultAnswers.FILE_NOT_FOUND, t, httpEvent);
             return;
         }
 
         final Iterator<HttpEventHandler> it = handler.iterator();
         final HttpEventHandler first = it.next();
         final HttpEventHandler chain = new HttpEventHandlerChain(it);
         HttpAnswer answer;
         try {
             answer = first.handleHttpEvent(registered, httpEvent, chain);
             if (answer != null) {
                 this.handleAnswer(answer, t, httpEvent);
                 return;
             }
         } catch (AlternativeAnswerException e) {
             this.handleAnswer(e.getAnswer(), t, httpEvent);
             return;
         } catch (HttpException e) {
             // consume and send "file not found" below
             e.printStackTrace();
         }
         
         // event could not be handled
         this.handleAnswer(DefaultAnswers.FILE_NOT_FOUND, t, httpEvent);
     }
 
     
     
     private final void handleAnswer(HttpAnswer answer, HttpExchange t, 
             final HttpEventImpl httpEvent) throws IOException {
         
         if (httpEvent.isDiscarded()) {
             // nothing to do here if already discarded
             return;
         }
         
         final HttpSessionImpl session = (HttpSessionImpl) httpEvent.getSession();
         
         // set stream to count outgoing traffic and report whether it was closed
         final TrafficInformationImpl ti = session.getTrafficInfo();
         final CountingOutputStream out = new CountingOutputStream(
             t.getResponseBody(), ti) {
             
             @Override
             public void close() throws IOException {
                 super.close();
                 httpEvent.fireOnClose();
             }
         };
         t.setStreams(null, out);
         
         
         // null answer means to discard this event
         if (answer == null) {
             t.close();
             return;
         }
 
         try {
             // add cookies as response header
             final Collection<HttpCookie> cookies = new ArrayList<>(answer.getCookies());
 
             if (this.server.getSessionType() == HttpServer.SESSION_TYPE_COOKIE && 
                     session.isPending()) {
                 
                 session.setPending(false);
                 // we must send a new session cookie
                 cookies.add(new HttpCookie(HttpServerImpl.SESSION_ID_NAME, 
                     session.getId(), this.server.sessionLiveTime() / 1000));
             } else if (session.shouldKill()) {
                 // send cookie with max age 0 to remove it at client side
                 cookies.add(new HttpCookie(HttpServer.SESSION_ID_NAME, 
                     session.getId(), 0));
                 this.server.killSession(session);
             }
             
             if (!cookies.isEmpty()) {
                 t.getResponseHeaders().add("Set-Cookie", 
                     this.generateCookieString(cookies));
             }
             
             t.getResponseHeaders().putAll(answer.getResponseHeaders());
             
             if (!httpEvent.isDiscarded()) {
                 t.sendResponseHeaders(answer.getResponseCode(), 0);
                 
                 // handle different types of answers
                 final HttpAnswerHandler handler = this.server.getHandler(answer);
                 if (handler == null) {
                     // TODO: react
                     throw new RuntimeException("no handler");
                 }
                 handler.handleAnswer(answer, httpEvent, t.getResponseBody());
             }
         } finally {
             t.close();
         }
     }
     
     
     
     private final String generateCookieString(Collection<HttpCookie> cookies) {
         final StringBuilder b = new StringBuilder();
         final Iterator<HttpCookie> it = cookies.iterator();
         
         while (it.hasNext()) {
             final HttpCookie next = it.next();
             b.append(next.toString());
             
             if (it.hasNext()) {
                 b.append(",");
             }
         }
         return b.toString();
     }
 }
