 /**
  * Copyright 2010 Google Inc.
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
 
 package org.waveprotocol.wave.examples.fedone.robots.passive;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.wave.api.Context;
 import com.google.wave.api.data.converter.ContextResolver;
 import com.google.wave.api.data.converter.EventDataConverter;
 import com.google.wave.api.event.Event;
 import com.google.wave.api.event.EventType;
 import com.google.wave.api.event.WaveletParticipantsChangedEvent;
 import com.google.wave.api.event.WaveletSelfAddedEvent;
 import com.google.wave.api.event.WaveletSelfRemovedEvent;
 import com.google.wave.api.impl.EventMessageBundle;
 import com.google.wave.api.robot.Capability;
 import com.google.wave.api.robot.RobotName;
 
 import org.waveprotocol.wave.examples.fedone.common.VersionedWaveletDelta;
 import org.waveprotocol.wave.examples.fedone.robots.util.ConversationUtil;
 import org.waveprotocol.wave.model.conversation.Conversation;
 import org.waveprotocol.wave.model.conversation.ConversationBlip;
 import org.waveprotocol.wave.model.conversation.ConversationListenerImpl;
 import org.waveprotocol.wave.model.conversation.ObservableConversation;
 import org.waveprotocol.wave.model.conversation.WaveletBasedConversation;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.operation.SilentOperationSink;
 import org.waveprotocol.wave.model.operation.core.CoreWaveletDelta;
 import org.waveprotocol.wave.model.operation.wave.ConversionUtil;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperationContext;
 import org.waveprotocol.wave.model.schema.SchemaCollection;
 import org.waveprotocol.wave.model.version.DistinctVersion;
 import org.waveprotocol.wave.model.wave.ObservableWavelet;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.ParticipationHelper;
 import org.waveprotocol.wave.model.wave.WaveletListener;
 import org.waveprotocol.wave.model.wave.data.DocumentFactory;
 import org.waveprotocol.wave.model.wave.data.DocumentOperationSink;
 import org.waveprotocol.wave.model.wave.data.MuteDocumentFactory;
 import org.waveprotocol.wave.model.wave.data.ObservableWaveletData;
 import org.waveprotocol.wave.model.wave.data.impl.WaveletDataImpl;
 import org.waveprotocol.wave.model.wave.opbased.OpBasedWavelet;
 import org.waveprotocol.wave.model.wave.opbased.WaveletListenerImpl;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Generates Robot API Events from operations applied to a Wavelet.
  *
  * @author ljvderijk@google.com (Lennard de Rijk)
  */
 public class EventGenerator {
 
   private static class EventGeneratingWaveletListener extends WaveletListenerImpl {
     @SuppressWarnings("unused")
     private final Map<EventType, Capability> capabilities;
 
     /**
      * Creates a {@link WaveletListener} which will generate events according to
      * the capabilities.
      *
      * @param capabilities the capabilities which we are interested in.
      */
     public EventGeneratingWaveletListener(Map<EventType, Capability> capabilities) {
       this.capabilities = capabilities;
     }
     // TODO(ljvderijk): implement more events. This class should listen for
     // non-conversational blip changes and robot data documents as indicated by
     // IdConstants.ROBOT_PREFIX
   }
 
   private static class EventGeneratingConversationListener extends ConversationListenerImpl {
     private final Map<EventType, Capability> capabilities;
     private final Conversation conversation;
     private final EventMessageBundle messages;
     private final ParticipantId robotId;
 
     // Event collectors
     private final List<String> participantsAdded = Lists.newArrayList();
     private final List<String> participantsRemoved = Lists.newArrayList();
 
     // Changes for each delta
     private ParticipantId deltaAuthor;
     private Long deltaTimestamp;
 
     /**
      * Creates a {@link ObservableConversation.Listener} which will generate
      * events according to the capabilities.
      *
      * @param conversation the conversation we are observing.
      * @param capabilities the capabilities which we are interested in.
      * @param messages the bundle to put the events in.
      */
     public EventGeneratingConversationListener(Conversation conversation,
         Map<EventType, Capability> capabilities, EventMessageBundle messages, RobotName robotName) {
       this.conversation = conversation;
       this.capabilities = capabilities;
       this.messages = messages;
       this.robotId = ParticipantId.ofUnsafe(robotName.toParticipantAddress());
     }
 
     /**
      * Prepares this listener for events coming from a single delta.
      *
      * @param author the author of the delta.
      * @param timestamp the timestamp of the delta.
      */
     public void deltaBegin(ParticipantId author, long timestamp) {
       Preconditions.checkState(
           deltaAuthor == null && deltaTimestamp == null, "DeltaEnd wasn't called");
       deltaAuthor = author;
       deltaTimestamp = timestamp;
     }
 
     @Override
     public void onParticipantAdded(ParticipantId participant) {
       if (capabilities.containsKey(EventType.WAVELET_PARTICIPANTS_CHANGED)) {
         boolean removedBefore = participantsRemoved.remove(participant.getAddress());
         if (!removedBefore) {
           participantsAdded.add(participant.getAddress());
         }
       }
 
      // This deviates from Google Wave production which always sends this event,
       // even if it wasn't present in your capabilities.
       if (capabilities.containsKey(EventType.WAVELET_SELF_ADDED) && participant.equals(robotId)) {
         // The robot has been added
         String rootBlipId = getRootBlipId(conversation);
        WaveletSelfAddedEvent event = new WaveletSelfAddedEvent(
             null, null, deltaAuthor.getAddress(), deltaTimestamp, rootBlipId);
        addEvent(event, capabilities, rootBlipId, messages);
       }
     }
 
     @Override
     public void onParticipantRemoved(ParticipantId participant) {
       if (capabilities.containsKey(EventType.WAVELET_PARTICIPANTS_CHANGED)) {
         participantsRemoved.add(participant.getAddress());
       }
 
       if (capabilities.containsKey(EventType.WAVELET_SELF_REMOVED) && participant.equals(robotId)) {
         String rootBlipId = getRootBlipId(conversation);
        WaveletSelfRemovedEvent event = new WaveletSelfRemovedEvent(
             null, null, deltaAuthor.getAddress(), deltaTimestamp, rootBlipId);
        addEvent(event, capabilities, rootBlipId, messages);
       }
     }
 
     /**
      * Generates the events that are collected over the span of one delta.
      *
      */
     public void deltaEnd() {
       if (!participantsAdded.isEmpty() || !participantsRemoved.isEmpty()) {
         String rootBlipId = getRootBlipId(conversation);
 
        WaveletParticipantsChangedEvent event =
             new WaveletParticipantsChangedEvent(null, null, deltaAuthor.getAddress(),
                 deltaTimestamp, rootBlipId, participantsAdded, participantsRemoved);
        addEvent(event, capabilities, rootBlipId, messages);
       }
       clearOncePerDeltaCollectors();
 
       deltaAuthor = null;
       deltaTimestamp = null;
     }
 
     /**
      * Clear the data structures responsible for collecting data for events that
      * should only be fired once per delta.
      */
     private void clearOncePerDeltaCollectors() {
       participantsAdded.clear();
       participantsRemoved.clear();
     }
   }
 
   /**
    * Adds an {@link Event} to the given {@link EventMessageBundle}.
    *
    * If a blip id is specified this will be added to the
    * {@link EventMessageBundle}'s required blips list with the context given by
    * the robot's capabilities. If a robot does not specify a context for this
    * event the default context will be used. Ergo this code is not responsible
    * for filtering operations that a robot is not interested in.
    *
    * @param event to add.
    * @param capabilities the capabilities to get the context from.
    * @param blipId id of the blip this event is related to, may be null.
    * @param messages {@link EventMessageBundle} to edit.
    */
   private static void addEvent(Event event, Map<EventType, Capability> capabilities, String blipId,
       EventMessageBundle messages) {
     // Add the given blip to the required blip lists with the context specified
     // by the robot's capabilities.
     if (!Strings.isNullOrEmpty(blipId)) {
       Capability capability = capabilities.get(event.getType());
 
       List<Context> contexts;
       if (capability == null) {
         contexts = Capability.DEFAULT_CONTEXT;
       } else {
         contexts = capability.getContexts();
       }
       messages.requireBlip(blipId, contexts);
     }
     // Add the event to the bundle.
     messages.addEvent(event);
   }
 
   /**
    * Returns the blip id of the first blip in the root thread.
    *
    * @param conversation the conversation to get the blip id from.
    */
   private static String getRootBlipId(Conversation conversation) {
     ConversationBlip rootBlip = conversation.getRootThread().getFirstBlip();
     String rootBlipId = (rootBlip != null) ? rootBlip.getId() : "";
     return rootBlipId;
   }
 
   // TODO(ljvderijk): Schemas should be enforced, see issue 109.
   private final static DocumentFactory<DocumentOperationSink> DOCUMENT_FACTORY =
       new MuteDocumentFactory(SchemaCollection.empty());
 
   /**
    * The name of the Robot to which this {@link EventGenerator} belongs. Used
    * for events where "self" is important.
    */
   private final RobotName robotName;
 
   /** Used to create conversations. */
   private final ConversationUtil conversationUtil;
 
   /**
    * Constructs a new {@link EventGenerator} for the robot with the given name.
    *
    * @param robotName the name of the robot.
    * @param conversationUtil used to create conversations.
    */
   public EventGenerator(RobotName robotName, ConversationUtil conversationUtil) {
     this.robotName = robotName;
     this.conversationUtil = conversationUtil;
   }
 
   /**
    * Generates the {@link EventMessageBundle} for the specified capabilities.
    *
    * @param waveletAndDeltas for which the events are to be generated
    * @param capabilities the capabilities to filter events on
    * @param converter converter for generating the API implementations of
    *        WaveletData and BlipData.
    * @returns true if an event was generated, false otherwise
    */
   public EventMessageBundle generateEvents(WaveletAndDeltas waveletAndDeltas,
       Map<EventType, Capability> capabilities, EventDataConverter converter) {
     EventMessageBundle messages = new EventMessageBundle(robotName.toEmailAddress(), "");
     if (robotName.hasProxyFor()) {
       // This robot is proxying so set the proxy field.
       messages.setProxyingFor(robotName.getProxyFor());
     }
 
     ObservableWaveletData snapshot = WaveletDataImpl.Factory.create(DOCUMENT_FACTORY).create(
         waveletAndDeltas.getSnapshotBeforeDeltas());
 
     OpBasedWavelet wavelet =
         new OpBasedWavelet(snapshot.getWaveId(), snapshot, WaveletOperationContext.Factory.READONLY,
             ParticipationHelper.IGNORANT, SilentOperationSink.VOID, SilentOperationSink.VOID);
 
     ObservableConversation conversation = getRootConversation(wavelet);
 
     if (conversation == null) {
       return messages;
     }
 
     // Start listening
     EventGeneratingConversationListener conversationListener =
         new EventGeneratingConversationListener(conversation, capabilities, messages, robotName);
     conversation.addListener(conversationListener);
     EventGeneratingWaveletListener waveletListener =
         new EventGeneratingWaveletListener(capabilities);
     wavelet.addListener(waveletListener);
 
     try {
       for (VersionedWaveletDelta vWDelta : waveletAndDeltas.getDeltas()) {
         CoreWaveletDelta delta = vWDelta.delta;
 
         // TODO(ljvderijk): Set correct timestamp once wavebus sends them along
         long timestamp = 0L;
         conversationListener.deltaBegin(delta.getAuthor(), timestamp);
 
         List<WaveletOperation> ops = ConversionUtil.fromCoreWaveletDelta(
             delta, timestamp, DistinctVersion.NO_DISTINCT_VERSION);
         for (WaveletOperation op : ops) {
           op.apply(snapshot);
         }
         conversationListener.deltaEnd();
       }
     } catch (OperationException e) {
       throw new IllegalStateException("Operation failed to apply when generating events", e);
     } finally {
       conversation.removeListener(conversationListener);
       wavelet.removeListener(waveletListener);
     }
 
     if (messages.getEvents().isEmpty()) {
       // No events found, no need to resolve contexts
       return messages;
     }
 
     // Resolve the context of the bundle now that all events have been
     // processed.
     ContextResolver.resolveContext(messages, wavelet, conversation, converter);
 
     return messages;
   }
 
   /**
    * Returns the root conversation from the given wavelet. Or null if there is
    * none.
    *
    * @param wavelet the wavelet to get the conversation from.
    */
   private ObservableConversation getRootConversation(ObservableWavelet wavelet) {
     if (!WaveletBasedConversation.waveletHasConversation(wavelet)) {
       // No conversation present, bail.
       return null;
     }
 
     ObservableConversation conversation = conversationUtil.getConversation(wavelet).getRoot();
     if (conversation.getRootThread().getFirstBlip() == null) {
       // No root blip is present, this will cause Robot API code
       // to fail when resolving the context of events. This might be fixed later
       // on by making changes to the ContextResolver.
       return null;
     }
     return conversation;
   }
 }
