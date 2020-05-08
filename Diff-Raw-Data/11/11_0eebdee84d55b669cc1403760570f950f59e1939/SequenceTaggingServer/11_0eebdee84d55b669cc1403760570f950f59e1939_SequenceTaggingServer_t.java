 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icepush;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.icepush.http.Request;
 import org.icepush.http.Response;
 import org.icepush.http.ResponseHandler;
 import org.icepush.http.Server;
 import org.icepush.http.standard.RequestProxy;
 import org.icepush.util.Slot;
 
 public class SequenceTaggingServer implements Server {
     private static final Logger LOGGER = Logger.getLogger(SequenceTaggingServer.class.getName());
 
     private Server server;
     private Slot sequenceNo;
     private List<String> participatingPushIDList = new ArrayList<String>();
     private boolean participatingPushIDsChanged;
     
     public SequenceTaggingServer(Slot sequenceNo, Server server) {
         this.sequenceNo = sequenceNo;
         this.server = server;
     }
 
     public void service(Request request) throws Exception {
         List<String> currentParticipatingPushIDList =
             Arrays.asList(request.getParameterAsStrings("ice.pushid"));
         participatingPushIDsChanged =
             !participatingPushIDList.containsAll(currentParticipatingPushIDList) ||
             !currentParticipatingPushIDList.containsAll(participatingPushIDList);
         if (participatingPushIDsChanged) {
             participatingPushIDList = currentParticipatingPushIDList;
         }
         server.service(new TaggingRequest(request));
     }
 
     public void shutdown() {
         server.shutdown();
     }
 
     public class TaggingRequest extends RequestProxy {
         public TaggingRequest(Request request) {
             super(request);
         }
 
         public void respondWith(final ResponseHandler handler) throws Exception {
             request.respondWith(new TaggingResponseHandler(handler));
         }
 
         private class TaggingResponseHandler implements ResponseHandler {
             private final ResponseHandler handler;
 
             public TaggingResponseHandler(ResponseHandler handler) {
                 this.handler = handler;
             }
 
             public void respond(Response response) throws Exception {
                 try {
                     long previousSequenceNo;
                     try {
                         previousSequenceNo = request.getHeaderAsLong("ice.push.sequence");
                         if (previousSequenceNo >= sequenceNo.getLongValue()) {
                             sequenceNo.setLongValue(previousSequenceNo + 1);
                         } else if (participatingPushIDsChanged) {
                             sequenceNo.setLongValue(sequenceNo.getLongValue() + 1);
                         } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                 LOGGER.log(
                                    Level.FINE,
                                     "Request's 'ice.push.sequence' [" + previousSequenceNo + "] is less than " +
                                         "the server-side sequence number [" + sequenceNo.getLongValue() + "].");
                             }
                             sequenceNo.setLongValue(sequenceNo.getLongValue() + 1);
                         }
                     } catch (RuntimeException e) {
                         // No sequence number found.
                         if (sequenceNo.getLongValue() == 0) {
                             // Start with sequence number
                             sequenceNo.setLongValue((long)1);
                         } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                 LOGGER.log(
                                    Level.FINE,
                                     "Request's 'ice.push.sequence' header is missing, " +
                                         "while server-side sequence number is '" + sequenceNo.getLongValue() + "'.");
                             }
                             sequenceNo.setLongValue(sequenceNo.getLongValue() + 1);
                         }
                     }
                     response.setHeader("ice.push.sequence", sequenceNo.getLongValue());
                 } finally {
                     handler.respond(response);
                 }
             }
         }
     }
 }
