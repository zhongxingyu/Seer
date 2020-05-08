 /**
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.waveprotocol.wave.examples.fedone.agents.echoey;
 
 import com.google.common.collect.ImmutableMap;
 
 import org.waveprotocol.wave.examples.fedone.agents.agent.AbstractAgent;
 import org.waveprotocol.wave.examples.fedone.agents.agent.AgentConnection;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.examples.fedone.waveclient.common.ClientUtils;
 import org.waveprotocol.wave.examples.fedone.waveclient.console.ConsoleUtils;
 import org.waveprotocol.wave.model.document.operation.AnnotationBoundaryMap;
 import org.waveprotocol.wave.model.document.operation.Attributes;
 import org.waveprotocol.wave.model.document.operation.AttributesUpdate;
 import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
 import org.waveprotocol.wave.model.document.operation.DocOpCursor;
 import org.waveprotocol.wave.model.document.operation.impl.AttributesImpl;
 import org.waveprotocol.wave.model.document.operation.impl.BufferedDocOpImpl.DocOpBuilder;
 import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.WaveletData;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Example agent that echoes back operations.
  */
 public class Echoey extends AbstractAgent {
   private static final Log LOG = Log.get(Echoey.class);
   private static final String MAIN_DOCUMENT_ID = "main";
 
   /**
    * Main entry point.
    *
    * @param args program arguments.
    */
   public static void main(String[] args) {
     try {
       if (args.length == 3) {
         int port;
         try {
           port = Integer.parseInt(args[2]);
         } catch (NumberFormatException e) {
           throw new IllegalArgumentException("Must provide valid port.");
         }
 
         Echoey agent = new Echoey(args[0], args[1], port);
         agent.run();
       } else {
         System.out.println("usage: java Echoey <username> <hostname> <port>");
       }
     } catch (Exception e) {
      LOG.severe("Catastrophic failure", e);
       System.exit(1);
     }
 
     System.exit(0);
   }
 
   private Echoey(String username, String hostname, int port) {
     super(AgentConnection.newConnection(username, hostname, port));
   }
 
   private void appendLines(WaveletData wavelet, List<String> lines) {
     BufferedDocOp openDoc = wavelet.getDocuments().get(MAIN_DOCUMENT_ID);
     int docSize = (openDoc == null) ? 0 : ClientUtils.findDocumentSize(openDoc);
     DocOpBuilder builder = new DocOpBuilder();
 
     if (docSize > 0) {
       builder.retain(docSize);
     }
     builder.elementStart(ConsoleUtils.LINE, new AttributesImpl(
         ImmutableMap.of(ConsoleUtils.LINE_AUTHOR, getParticipantId())));
     builder.elementEnd();
     for (String line : lines) {
       if (!line.isEmpty()) {
         builder.characters(line);
         LOG.info("LINE: " + line);
       }
     }
     WaveletDocumentOperation op =
         new WaveletDocumentOperation(MAIN_DOCUMENT_ID, builder.finish());
     sendWaveletOperation(wavelet.getWaveletName(), op);
   }
 
   private void appendText(WaveletData wavelet, String text) {
     if (text.isEmpty()) {
       return;
     }
     List<String> lines = new ArrayList<String>(1);
     lines.add(text);
     appendLines(wavelet, lines);
   }
 
   @Override
   public void onDocumentChanged(WaveletData wavelet, WaveletDocumentOperation documentOperation) {
     LOG.info("onDocumentChanged: " + wavelet.getWaveletName());
     BufferedDocOp docOp = documentOperation.getOperation();
     // Rebuild a similar operation with only the author changed.
     final List<String> lines = new ArrayList<String>();
     docOp.apply(new DocOpCursor() {
         @Override
         public void annotationBoundary(AnnotationBoundaryMap map) {}
 
         @Override
         public void characters(String chars) {
           lines.add(chars);
         }
 
         @Override public void deleteCharacters(String chars) {}
 
         @Override public void deleteElementEnd() {}
 
         @Override public void deleteElementStart(String type, Attributes attrs) {}
         @Override public void elementEnd() {}
         @Override public void elementStart(String type, Attributes attrs) {
           if (type.equals(ConsoleUtils.LINE)){
             if (attrs.containsKey(ConsoleUtils.LINE_AUTHOR)) {
             }
           }
         }
         @Override public void replaceAttributes(Attributes oldAttrs, Attributes newAttrs) {}
         @Override public void retain(int itemCount) {}
         @Override public void updateAttributes(AttributesUpdate attrUpdate) {}
       });
     appendLines(wavelet, lines);
   }
 
   @Override
   public void onParticipantAdded(WaveletData wavelet, ParticipantId participant) {
     LOG.info("onParticipantAdded: " + participant.getAddress());
     appendText(wavelet, participant.getAddress() + " was added to this wavelet.");
   }
 
   @Override
   public void onParticipantRemoved(WaveletData wavelet, ParticipantId participant) {
     LOG.info("onParticipantRemoved: " + participant.getAddress());
     appendText(wavelet, participant.getAddress() + " was removed from this wavelet.");
   }
 
   @Override
   public void onSelfAdded(WaveletData wavelet) {
     LOG.info("onSelfAdded: " + wavelet.getWaveletName());
     appendText(wavelet, "I'm listening.");
   }
 
   @Override
   public void onSelfRemoved(WaveletData wavelet) {
     LOG.info("onSelfRemoved: " + wavelet.getWaveletName());
     appendText(wavelet, "Goodbye.");
   }
 }
