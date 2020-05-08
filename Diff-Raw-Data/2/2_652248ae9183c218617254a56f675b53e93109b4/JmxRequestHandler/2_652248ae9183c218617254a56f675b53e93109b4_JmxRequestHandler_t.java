 /*
  *  Copyright 2013 JMX Daemon contributors
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.hibnet.jmxdaemon;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.DownstreamMessageEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JmxRequestHandler extends SimpleChannelHandler {
 
     static final String REQ_CMD_GET = "GET";
 
     static final String REQ_CMD_CLOSE = "CLOSE";
 
     static final String RESP_OK = "OK";
 
     static final String RESP_ERR = "ERR";
 
     static final String RESP_SEP = "\n";
 
     static final String RESP_ERR_NO_CMD = "NO_CMD";
 
     static final String RESP_ERR_UNKNOWN_CMD = "UNKNOWN_CMD";
 
     static final String RESP_ERR_ARGS_LEN = "INVALID_ARGUMENT_LENGTH";
 
     static final String RESP_ERR_CONN = "CONNECTION_FAILED";
 
     static final String RESP_ERR_GET_ATT = "GET_ATTRIBUTE_FAILED";
 
     static final String RESP_ERR_FORMAT = "FORMAT_OUTPUT_FAILED";
 
     static final String RESP_ERR_IO = "IO_ERROR";
 
     private static final Logger log = LoggerFactory.getLogger(JmxRequestHandler.class);
 
     private Map<String, JmxConnectionHolder> connectionCache = new ConcurrentHashMap<>();
 
     private JmxConnectionHolder getConnection(StringBuilder response, String url) {
         try {
             return getConnection(url);
         } catch (Exception e) {
             response.append(RESP_ERR);
             response.append(RESP_SEP);
             response.append(RESP_ERR_CONN);
             response.append(RESP_SEP);
             writeExceptionMessage(response, e);
             return null;
         }
     }
 
     private JmxConnectionHolder getConnection(String url) throws IOException {
         JmxConnectionHolder connection = connectionCache.get(url);
         if (connection == null) {
             synchronized (connectionCache) {
                 connection = connectionCache.get(url);
                 if (connection == null) {
                     connection = new JmxConnectionHolder(url);
                     connectionCache.put(url, connection);
                 }
             }
         }
         return connection;
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
         List<String> request = parseRequest((String) e.getMessage());
         StringBuilder response = new StringBuilder();
         if (request.size() == 0 || (request.size() == 1 && request.get(0).length() == 0)) {
             response.append(RESP_ERR);
             response.append(RESP_SEP);
             response.append(RESP_ERR_NO_CMD);
         } else if (request.get(0).equals(REQ_CMD_GET)) {
             if (request.size() < 5 || (request.size() - 2) % 2 != 1) {
                 response.append(RESP_ERR);
                 response.append(RESP_SEP);
                 response.append(RESP_ERR_ARGS_LEN);
                 response.append(RESP_SEP);
                response.append("Expecting an even number of arguments and at least 4 but there are "
                         + (request.size() - 1));
             } else {
                 String url = request.get(1);
                 JmxConnectionHolder connection = getConnection(response, url);
                 if (connection != null) {
                     String format = request.get(2);
                     ArrayList<Object> values = new ArrayList<>();
                     for (int i = 3; i < request.size(); i += 2) {
                         Object value;
                         try {
                             value = connection.getAttribute(request.get(i), request.get(i + 1));
                             values.add(value);
                         } catch (IOException ex) {
                             log.warn("IO error on connection {}", url, ex);
                             connection.close();
                             values = null;
                             response.append(RESP_ERR);
                             response.append(RESP_SEP);
                             response.append(RESP_ERR_IO);
                             response.append(RESP_SEP);
                             writeExceptionMessage(response, ex);
                             break;
                         } catch (Exception ex) {
                             log.warn("Error on {} for bean '{}' getting attribute '{}': {} ({})", url, request.get(i),
                                     request.get(i + 1), ex.getMessage(), ex.getClass().getSimpleName(), ex);
                             values = null;
                             response.append(RESP_ERR);
                             response.append(RESP_SEP);
                             response.append(RESP_ERR_GET_ATT);
                             response.append(RESP_SEP);
                             response.append("Failed to get on '" + url + "' bean '" + request.get(i) + "' attribute '"
                                     + request.get(i + 1) + "':  ");
                             writeExceptionMessage(response, ex);
                             break;
                         }
                     }
                     if (values != null) {
                         try {
                             String output = String.format(format, values.toArray());
                             response.append(RESP_OK);
                             response.append(RESP_SEP);
                             response.append(output);
                         } catch (Exception ex) {
                             log.warn("Incorrect format '{}'", format, ex);
                             response.append(RESP_ERR);
                             response.append(RESP_SEP);
                             response.append(RESP_ERR_FORMAT);
                             response.append(RESP_SEP);
                             writeExceptionMessage(response, ex);
                         }
                     }
                 }
             }
         } else if (request.get(0).equals(REQ_CMD_CLOSE)) {
             if (request.size() != 2) {
                 response.append(RESP_ERR);
                 response.append(RESP_SEP);
                 response.append(RESP_ERR_ARGS_LEN);
                 response.append(RESP_SEP);
                 response.append("Expecting 1 argument but there was " + (request.size() - 1));
             } else {
                 String url = request.get(1);
                 JmxConnectionHolder connection = getConnection(response, url);
                 if (connection != null) {
                     connection.close();
                     response.append(RESP_OK);
                 }
             }
         } else {
             response.append(RESP_ERR);
             response.append(RESP_SEP);
             response.append(RESP_ERR_UNKNOWN_CMD);
             response.append(RESP_SEP);
             response.append(request.get(0));
         }
         response.append(RESP_SEP);
 
         // send the response back
         Channel channel = e.getChannel();
         ChannelFuture channelFuture = Channels.future(e.getChannel());
         ChannelEvent responseEvent = new DownstreamMessageEvent(channel, channelFuture, response.toString(),
                 channel.getRemoteAddress());
         ctx.sendDownstream(responseEvent);
         channelFuture.addListener(new ChannelFutureListener() {
             @Override
             public void operationComplete(ChannelFuture future) throws Exception {
                 future.getChannel().close();
             }
         });
 
         super.messageReceived(ctx, e);
     }
 
     private List<String> parseRequest(String input) {
         List<String> request = new ArrayList<>();
         int p = 0;
         while (p < input.length()) {
             p = skipWhiteSpace(input, p);
             if (p >= input.length()) {
                 break;
             }
             if (input.charAt(p) == '\'' || input.charAt(p) == '\"') {
                 char delim = input.charAt(p);
                 StringBuilder buffer = new StringBuilder();
                 while (p < input.length()) {
                     int end = input.indexOf(delim, p + 1);
                     if (end == -1) {
                         buffer.append(input.substring(p + 1));
                         p = input.length();
                         break;
                     }
                     int i = input.indexOf('\\', p + 1);
                     if (i == -1 || i > end) {
                         buffer.append(input.substring(p + 1, end));
                         p = end + 1;
                         break;
                     }
                     buffer.append(input.substring(p + 1, i));
                     if (i + 1 < input.length()) {
                         buffer.append(input.charAt(i + 1));
                     }
                     p = i + 2;
                 }
                 request.add(buffer.toString());
             } else {
                 int i = Integer.MAX_VALUE;
                 for (char c : new char[] { ' ', '\n', '\r', '\t' }) {
                     int ic = input.indexOf(c, p);
                     if (ic != -1) {
                         i = Math.min(i, ic);
                     }
                 }
                 if (i == Integer.MAX_VALUE) {
                     request.add(input.substring(p));
                     p = input.length();
                 } else {
                     request.add(input.substring(p, i));
                     p = i + 1;
                 }
             }
         }
         return request;
     }
 
     private int skipWhiteSpace(String input, int p) {
         while (p < input.length()
                 && (input.charAt(p) == ' ' || input.charAt(p) == '\n' || input.charAt(p) == '\r' || input.charAt(p) == '\t')) {
             p++;
         }
         return p;
     }
 
     private void writeExceptionMessage(StringBuilder response, Throwable ex) {
         response.append(ex.getClass().getSimpleName() + ": "
                 + ex.getMessage().replaceAll("\n", " ").replaceAll("\t", " "));
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
         if (e.getCause() instanceof IOException && e.getCause().getMessage().equals("Connection reset by peer")) {
             // we don't care
             return;
         }
         log.warn("Unexpected Error", e.getCause());
         ctx.getChannel().close();
     }
 
     public void closeJmxConnections() {
         synchronized (connectionCache) {
             for (JmxConnectionHolder connection : connectionCache.values()) {
                 try {
                     connection.close();
                 } catch (Throwable e) {
                     log.warn("Error while closing connection {}", connection, e);
                 }
             }
             connectionCache.clear();
         }
     }
 }
