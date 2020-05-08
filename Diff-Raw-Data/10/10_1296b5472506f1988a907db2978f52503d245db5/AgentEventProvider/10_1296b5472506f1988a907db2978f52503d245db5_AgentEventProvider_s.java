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
 
 package org.waveprotocol.wave.examples.fedone.agents.agent;
 
 import org.waveprotocol.wave.examples.fedone.common.CommonConstants;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.examples.fedone.waveclient.common.WaveletOperationListener;
 import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.WaveletData;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Provides agent events.
  */
 public class AgentEventProvider implements WaveletOperationListener, AgentEventListener {
   private static final Log LOG = Log.get(AgentEventProvider.class);
 
   private final AgentConnection connection;
   private final Set<AgentEventListener> listeners = new HashSet<AgentEventListener>();
 
   /**
    * Constructor.
    *
    * @param connection a connection to the server.
    */
   AgentEventProvider(AgentConnection connection) {
     this.connection = connection;
     this.connection.addWaveletOperationListener(this);
   }
 
   /**
    * Adds an event listener to this agent.
    *
    * @param listener the event listener.
    */
   void addAgentEventListener(AgentEventListener listener) {
     listeners.add(listener);
   }
 
   /**
    * Should updates by the passed author onto the given wavelet be ignored by
    * agents.
    */
   private boolean isIgnored(String author, WaveletData wavelet) {
     return wavelet.getWaveletName().waveId.equals(CommonConstants.INDEX_WAVE_ID);
   }
 
   /**
    * Returns true if this was an event originated by the agent itself.
    */
   private boolean isSelfEvent(String author, WaveletData wavelet) {
    return author.equals(connection.getParticipantId());
   }
 
   @Override
   public void noOp(String author, WaveletData wavelet) {}
 
   @Override
   public void onDeltaSequenceEnd(WaveletData wavelet) {}
 
   @Override
   public void onDeltaSequenceStart(WaveletData wavelet) {}
 
   @Override
   public void onCommitNotice(WaveletData wavelet, HashedVersion version) {}
 
   @Override
   public void onDocumentChanged(WaveletData wavelet, WaveletDocumentOperation documentOperation) {
     for (AgentEventListener l : listeners) {
       l.onDocumentChanged(wavelet, documentOperation);
     }
   }
 
   @Override
   public void onParticipantAdded(WaveletData wavelet, ParticipantId participant) {
     for (AgentEventListener l : listeners) {
       l.onParticipantAdded(wavelet, participant);
     }
   }
 
   @Override
   public void onParticipantRemoved(WaveletData wavelet, ParticipantId participant) {
     for (AgentEventListener l : listeners) {
       l.onParticipantRemoved(wavelet, participant);
     }
   }
 
   @Override
   public void onSelfAdded(WaveletData wavelet) {
     for (AgentEventListener l : listeners) {
       l.onSelfAdded(wavelet);
     }
   }
 
   @Override
   public void onSelfRemoved(WaveletData wavelet) {
     for (AgentEventListener l : listeners) {
       l.onSelfRemoved(wavelet);
     }
   }
 
   @Override
   public void participantAdded(String author, WaveletData wavelet, ParticipantId participantId) {
     if (isIgnored(author, wavelet)) {
       return;
     }
 
    if (participantId.getAddress().equals(connection.getParticipantId())) {
       onSelfAdded(wavelet);
     } else {
       onParticipantAdded(wavelet, participantId);
     }
   }
 
   @Override
   public void participantRemoved(String author, WaveletData wavelet, ParticipantId participantId) {
     if (isIgnored(author, wavelet)) {
       return;
     }
 
    if (participantId.getAddress().equals(connection.getParticipantId())) {
       onSelfRemoved(wavelet);
     } else {
       onParticipantRemoved(wavelet, participantId);
     }
   }
 
   /**
    * Start listening for events.
    */
   void startListening() {
     boolean done = false;
     while (!done) {
       try {
         Thread.sleep(1000);
       } catch (InterruptedException e) {
         LOG.info("Aborting.");
         return;
       }
     }
   }
 
   @Override
   public void waveletDocumentUpdated(String author, WaveletData wavelet, WaveletDocumentOperation operation) {
     if (isIgnored(author, wavelet) || isSelfEvent(author, wavelet)) {
       return;
     }
     onDocumentChanged(wavelet, operation);
   }
 }
