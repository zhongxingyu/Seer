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
 
 package org.waveprotocol.wave.examples.fedone.frontend;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 
 import org.waveprotocol.wave.examples.client.common.ClientUtils;
 import org.waveprotocol.wave.examples.fedone.common.CoreWaveletOperationSerializer;
 import org.waveprotocol.wave.examples.fedone.common.DeltaSequence;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersionFactory;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveletProvider;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveletSnapshotBuilder;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.DocumentSnapshot;
import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.WaveletSnapshot;
 import org.waveprotocol.wave.federation.FederationErrors;
import org.waveprotocol.wave.federation.FederationErrorProto.FederationError;
 import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletOperation;
 import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
 import org.waveprotocol.wave.model.id.IdConstants;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.core.CoreWaveletData;
 import org.waveprotocol.wave.waveserver.federation.SubmitResultListener;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 
 /**
  * Implements the client front-end.
  *
  * This class maintains a list of wavelets accessible by local participants by
  * inspecting all updates it receives (there is no need to inspect historic
  * deltas as they would have been received as updates had there been an
  * addParticipant). Updates are aggregated in a special index Wave which is
  * stored with the WaveServer.
  *
  * When a wavelet is added and it's not at version 0, buffer updates until a
  * request for the wavelet's history has completed.
  */
 public class ClientFrontendImpl implements ClientFrontend {
   private static final Log LOG = Log.get(ClientFrontendImpl.class);
 
   private final static AtomicInteger channel_counter = new AtomicInteger(0);
 
   /** Information we hold in memory for each wavelet, including index wavelets. */
   private static class PerWavelet {
     private final Set<ParticipantId> participants;
     final AtomicLong timestamp;  // last modified time
     private final ProtocolHashedVersion version0;
     private ProtocolHashedVersion currentVersion;
     private String digest;
 
     PerWavelet(WaveletName waveletName, HashedVersion hashedVersionZero) {
       this.participants = Collections.synchronizedSet(Sets.<ParticipantId>newHashSet());
       this.timestamp = new AtomicLong(0);
       this.version0 = CoreWaveletOperationSerializer.serialize(hashedVersionZero);
       this.currentVersion = version0;
       this.digest = "";
     }
 
     synchronized ProtocolHashedVersion getCurrentVersion() {
       return currentVersion;
     }
 
     synchronized void setCurrentVersion(ProtocolHashedVersion version) {
       this.currentVersion = version;
     }
   }
 
   /** Maps wavelets to the participants currently on that wavelet */
   @VisibleForTesting final Map<ParticipantId, UserManager> perUser;
   private final Map<WaveletName, PerWavelet> perWavelet;
   private final WaveletProvider waveletProvider;
 
   @Inject
   public ClientFrontendImpl(final HashedVersionFactory hashedVersionFactory,
       WaveletProvider waveletProvider) {
     this.waveletProvider = waveletProvider;
     waveletProvider.setListener(this);
     MapMaker mapMaker = new MapMaker();
     perWavelet = mapMaker.makeComputingMap(new Function<WaveletName, PerWavelet>() {
       @Override
       public PerWavelet apply(WaveletName waveletName) {
         return new PerWavelet(waveletName, hashedVersionFactory.createVersionZero(waveletName));
       }
     });
 
     perUser = mapMaker.makeComputingMap(new Function<ParticipantId, UserManager>() {
       @Override
       public UserManager apply(ParticipantId from) {
         return new UserManager(hashedVersionFactory);
       }
     });
   }
 
   @Override
   public void openRequest(ParticipantId participant, WaveId waveId, Set<String> waveletIdPrefixes,
       int maximumInitialWavelets, boolean snapshotsEnabled,
       final List<WaveClientRpc.WaveletVersion> knownWavelets, OpenListener openListener) {
     String channel_id = generateChannelID();
 
     LOG.info("received openRequest from " + participant + " for waveId " + waveId + " snapshots: "
         + snapshotsEnabled);
     if (waveletIdPrefixes == null || waveletIdPrefixes.isEmpty()) {
       waveletIdPrefixes = ImmutableSet.of("");
     }
     final boolean isIndexWave = IndexWave.isIndexWave(waveId);
     UserManager userManager = perUser.get(participant);
     synchronized (userManager) {
       Set<WaveletId> waveletIds = userManager.subscribe(waveId, waveletIdPrefixes, channel_id,
           openListener);
       // Send this listener all deltas on relevant wavelets that we've already
       // sent out to other listeners, so that the listener can catch up with
       // those.
       // TODO: Because of the way we create the fake digest edit ops for the
       // index wave, if the listener is subscribing to the index wave then it
       // may be that this requires fewer ops than the existing listeners have
       // seen, leaving this listener at a different end version than other
       // listeners on the same wavelet. Fix!
       Map<WaveletName, ProtocolHashedVersion> knownWaveletVersions = Maps.newHashMap();
       if (knownWavelets != null) {
         for (WaveClientRpc.WaveletVersion waveletAndVersion : knownWavelets) {
           knownWaveletVersions.put(
               WaveletName.of(waveId, WaveletId.deserialise(waveletAndVersion.getWaveletId())),
               waveletAndVersion.getHashedVersion());
         }
       }
       for (WaveletId waveletId : waveletIds) {
         WaveletName waveletName = WaveletName.of(waveId, waveletId);
 
         // The WaveletName by which the waveletProvider knows the relevant deltas
         WaveletName sourceWaveletName;
         if (isIndexWave) {
           sourceWaveletName = WaveletName.of(IndexWave.waveIdFromIndexWavelet(waveletName),
              new WaveletId(waveletId.getDomain(), IdConstants.CONVERSATION_ROOT_WAVELET));
         } else {
           sourceWaveletName = waveletName;
         }
         ProtocolHashedVersion startVersion;
         if (knownWaveletVersions.containsKey(waveletName)) { // Commented out for now.
           startVersion = knownWaveletVersions.get(waveletName);
           // TODO(arb): needs to be startVersion++
           // Known version is current version. We want current version + 1 to now.
 
         } else {
           startVersion = perWavelet.get(sourceWaveletName).version0;
         }
         ProtocolHashedVersion endVersion = userManager.getWaveletVersion(sourceWaveletName);
 
         List<ProtocolWaveletDelta> deltaList;
         WaveletSnapshotAndVersions snapshot;
 
         if (isIndexWave || !snapshotsEnabled) {
           DeltaSequence deltaSequence = new DeltaSequence(
               waveletProvider.getHistory(sourceWaveletName, startVersion, endVersion),
               endVersion);
           if (isIndexWave) { // Construct fake index wave deltas from the deltas
             String newDigest = perWavelet.get(sourceWaveletName).digest;
             // Nuke the request, and just return new deltas from v0.
             if (startVersion.getVersion() != 0) {
               LOG.warning("resetting index wave version from: " + startVersion);
             }
             startVersion = perWavelet.get(sourceWaveletName).version0;
             deltaSequence = IndexWave.createIndexDeltas(startVersion.getVersion(), deltaSequence,
                 "", newDigest);
           }
           deltaList = deltaSequence;
           endVersion = deltaSequence.getEndVersion();
           snapshot = null;
         } else {
           // TODO(arb): when we have uncommitted deltas, look them up here.
           deltaList = Collections.emptyList();
           WaveletSnapshotBuilder<WaveletSnapshotAndVersions> snapshotBuilder =
               new WaveletSnapshotBuilder<WaveletSnapshotAndVersions>() {
                 @Override
                 public WaveletSnapshotAndVersions build(CoreWaveletData waveletData,
                     HashedVersion currentVersion, ProtocolHashedVersion committedVersion) {
                   return new WaveletSnapshotAndVersions(serializeSnapshot(waveletData),
                       currentVersion, committedVersion);
                 }
               };
           snapshot = waveletProvider.getSnapshot(sourceWaveletName, snapshotBuilder);
         }
 
         // TODO: Once we've made sure that all listeners have received the
         // same number of ops for the index wavelet, enable the following check:
         //if (!deltaList.getEndVersion().equals(userManager.getWaveletVersion(waveletName))) {
         //  throw new IllegalStateException(..)
         // }
 
         LOG.info("snapshot in response is: " + (snapshot == null));
         if (snapshot == null) {
           // TODO(arb): get the LCV - maybe add to waveletProvider?
           openListener.onUpdate(waveletName, snapshot, deltaList, endVersion,
               null, false, channel_id);
         } else {
           openListener.onUpdate(waveletName, snapshot, deltaList, endVersion,
               snapshot.committedVersion, false, channel_id);
         }
       }
 
       final WaveletName dummyWaveletName = createDummyWaveletName(waveId);
 
       if (waveletIds.size() == 0) {
         // there were no wavelets, send just a channelid
         LOG.info("sending just a channel id for " + dummyWaveletName);
         openListener.onUpdate(dummyWaveletName,
             null, new ArrayList<ProtocolWaveletDelta>(), null, null, false, channel_id);
       }
 
       LOG.info("sending marker for " + dummyWaveletName);
       openListener.onUpdate(dummyWaveletName,
           null, new ArrayList<ProtocolWaveletDelta>(), null, null, true, null);
     }
   }
 
   private String generateChannelID() {
     return "ch" + channel_counter.addAndGet(1);
   }
 
   /**
    * Serializes a WaveletData into a WaveletSnapshot protobuffer.
    *
    * @param snapshot the snapshot
    * @return the new protobuffer
    */
   private WaveletSnapshot serializeSnapshot(CoreWaveletData snapshot) {
     WaveletSnapshot.Builder snapshotBuilder = WaveletSnapshot.newBuilder();
     Map<String, BufferedDocOp> documentMap = snapshot.getDocuments();
     for (Entry<String,BufferedDocOp> document : documentMap.entrySet()) {
       DocumentSnapshot.Builder documentBuilder = DocumentSnapshot.newBuilder();
       documentBuilder.setDocumentId(document.getKey());
       documentBuilder.setDocumentOperation(
           CoreWaveletOperationSerializer.serialize(document.getValue()));
       snapshotBuilder.addDocument(documentBuilder.build());
     }
     for (ParticipantId participant : snapshot.getParticipants()) {
       snapshotBuilder.addParticipantId(participant.toString());
     }
     return snapshotBuilder.build();
   }
 
   private static class SubmitResultListenerAdapter implements SubmitResultListener {
     private final SubmitResultListener listener;
 
     public SubmitResultListenerAdapter(SubmitResultListener listener) {
       this.listener = listener;
     }
 
     @Override
     public void onFailure(FederationError error) {
       listener.onFailure(error);
     }
 
     @Override
     public void onSuccess(int operationsApplied,
         ProtocolHashedVersion hashedVersionAfterApplication,
         long applicationTimestamp) {
       listener.onSuccess(operationsApplied, hashedVersionAfterApplication, applicationTimestamp);
     }
   }
 
   private boolean isWaveletWritable(WaveletName waveletName) {
     return !IndexWave.isIndexWave(waveletName.waveId);
   }
 
   @Override
   public void submitRequest(final WaveletName waveletName, final ProtocolWaveletDelta delta,
       final String channelId, final SubmitResultListener listener) {
     final ParticipantId author = new ParticipantId(delta.getAuthor());
     if (!isWaveletWritable(waveletName)) {
       listener.onFailure(FederationErrors.badRequest("Wavelet " + waveletName + " is readonly"));
     } else {
       perUser.get(author).submitRequest(channelId, waveletName);
       waveletProvider.submitRequest(waveletName, delta, channelId,
           new SubmitResultListenerAdapter(listener) {
         @Override
         public void onSuccess(int operationsApplied,
             ProtocolHashedVersion hashedVersionAfterApplication, long applicationTimestamp) {
           super.onSuccess(operationsApplied, hashedVersionAfterApplication,
               applicationTimestamp);
           perWavelet.get(waveletName).timestamp.set(applicationTimestamp);
           perUser.get(author).submitResponse(channelId, waveletName,
               hashedVersionAfterApplication);
         }
 
         @Override
         public void onFailure(FederationError error) {
           super.onFailure(error);
           perUser.get(author).submitResponse(channelId, waveletName, null);
         }
       });
     }
   }
 
   @Override
   public void waveletCommitted(WaveletName waveletName, ProtocolHashedVersion version) {
     for (ParticipantId participant : perWavelet.get(waveletName).participants) {
       // TODO(arb): commits? channelId
       perUser.get(participant).onCommit(waveletName, version, null);
     }
   }
 
   private void participantAddedToWavelet(WaveletName waveletName, ParticipantId participant) {
     perWavelet.get(waveletName).participants.add(participant);
     perUser.get(participant).addWavelet(waveletName);
   }
 
   private void participantRemovedFromWavelet(WaveletName waveletName, ParticipantId participant) {
     perWavelet.get(waveletName).participants.remove(participant);
     perUser.get(participant).removeWavelet(waveletName);
   }
 
   /**
    * Sends new deltas to a particular user on a particular wavelet, and also
    * generates fake deltas for the index wavelet. If the user was added,
    * requests missing deltas from the waveletProvider. Updates the participants
    * of the specified wavelet if the participant was added or removed.
    *
    * @param waveletName which the deltas belong to
    * @param participant on the wavelet
    * @param newDeltas newly arrived deltas of relevance for participant.
    *        Must not be empty.
    * @param add whether the participant is added by the first delta
    * @param remove whether the participant is removed by the last delta
    * @param oldDigest The digest text of the wavelet before the deltas are
    *                  applied (but including all changes from preceding deltas)
    * @param newDigest The digest text of the wavelet after the deltas are applied
    * @param channelId The channel ID of this client (or null if not supported by the client)
    */
   @VisibleForTesting
   void participantUpdate(WaveletName waveletName, ParticipantId participant,
       DeltaSequence newDeltas, boolean add, boolean remove, String oldDigest, String newDigest,
       String channelId) {
     final DeltaSequence deltasToSend;
     if (add && newDeltas.getStartVersion().getVersion() > 0) {
       ProtocolHashedVersion version0 = perWavelet.get(waveletName).version0;
       ProtocolHashedVersion firstKnownDelta = newDeltas.getStartVersion();
       deltasToSend = newDeltas.prepend(
           waveletProvider.getHistory(waveletName, version0, firstKnownDelta));
       oldDigest = "";
     } else {
       deltasToSend = newDeltas;
     }
     if (add) {
       participantAddedToWavelet(waveletName, participant);
     }
     perUser.get(participant).onUpdate(waveletName, deltasToSend, channelId);
     if (remove) {
       participantRemovedFromWavelet(waveletName, participant);
     }
 
     // Construct and publish fake index wave deltas
     if (IndexWave.canBeIndexed(waveletName)) {
       WaveletName indexWaveletName = IndexWave.indexWaveletNameFor(waveletName.waveId);
       if (add) {
         participantAddedToWavelet(indexWaveletName, participant);
       }
       ProtocolHashedVersion indexVersion =
           perUser.get(participant).getWaveletVersion(indexWaveletName);
 
       DeltaSequence indexDeltas = IndexWave.createIndexDeltas(indexVersion.getVersion(),
           deltasToSend, oldDigest, newDigest);
       if (!indexDeltas.isEmpty()) {
         perUser.get(participant).onUpdate(indexWaveletName, indexDeltas, channelId);
       }
       if (remove) {
         participantRemovedFromWavelet(indexWaveletName, participant);
       }
     }
   }
 
   /**
    * Based on deltas we receive from the wave server, pass the appropriate
    * membership changes and deltas from both the affected wavelets and the
    * corresponding index wave wavelets on to the UserManagers.
    */
   @Override
   public void waveletUpdate(WaveletName waveletName, List<ProtocolWaveletDelta> newDeltas,
       ProtocolHashedVersion endVersion, Map<String, BufferedDocOp> documentState,
       String channelId) {
     if (newDeltas.isEmpty()) {
       return;
     }
 
     final PerWavelet waveletInfo = perWavelet.get(waveletName);
     final ProtocolHashedVersion expectedVersion;
     final String oldDigest;
     final Set<ParticipantId> remainingParticipants;
 
     synchronized (waveletInfo) {
       expectedVersion = waveletInfo.getCurrentVersion();
       oldDigest = waveletInfo.digest;
       remainingParticipants = Sets.newHashSet(waveletInfo.participants);
     }
 
     DeltaSequence deltaSequence = new DeltaSequence(newDeltas, endVersion);
 
     Preconditions.checkState(expectedVersion.equals(deltaSequence.getStartVersion()),
         "Expected deltas starting at version %s, got %s",
         expectedVersion, deltaSequence.getStartVersion().getVersion());
 
     String newDigest = digest(ClientUtils.renderSnippet(documentState, 80));
 
     // Participants added during the course of newDeltas
     Set<ParticipantId> newParticipants = Sets.newHashSet();
 
     for (int i = 0; i < newDeltas.size(); i++) {
       ProtocolWaveletDelta delta = newDeltas.get(i);
       // Participants added or removed in this delta get the whole delta
       for (ProtocolWaveletOperation op : delta.getOperationList()) {
         if (op.hasAddParticipant()) {
           ParticipantId p = new ParticipantId(op.getAddParticipant());
           remainingParticipants.add(p);
           newParticipants.add(p);
         }
         if (op.hasRemoveParticipant()) {
           ParticipantId p = new ParticipantId(op.getRemoveParticipant());
           remainingParticipants.remove(p);
           participantUpdate(waveletName, p,
               deltaSequence.subList(0, i + 1), newParticipants.remove(p), true, oldDigest, "",
               channelId);
         }
       }
     }
 
     // Send out deltas to those who end up being participants at the end
     // (either because they already were, or because they were added).
     for (ParticipantId p : remainingParticipants) {
       boolean isNew = newParticipants.contains(p);
       participantUpdate(waveletName, p, deltaSequence, isNew, false, oldDigest, newDigest,
           channelId);
     }
 
     synchronized (waveletInfo) {
       waveletInfo.setCurrentVersion(deltaSequence.getEndVersion());
       waveletInfo.digest = newDigest;
     }
   }
 
   /** Constructs a digest of the specified String. */
   private static String digest(String text) {
     int digestEndPos = text.indexOf('\n');
     if (digestEndPos < 0) {
       return text;
     } else {
       return text.substring(0, Math.min(80, digestEndPos));
     }
   }
 
   @VisibleForTesting
   static DeltaSequence createUnsignedDeltas(List<ProtocolWaveletDelta> deltas) {
     Preconditions.checkArgument(!deltas.isEmpty(), "No deltas specified");
     ProtocolWaveletDelta lastDelta = Iterables.getLast(deltas);
     long endVersion = lastDelta.getHashedVersion().getVersion() + lastDelta.getOperationCount();
     return new DeltaSequence(deltas,
         CoreWaveletOperationSerializer.serialize(HashedVersion.unsigned(endVersion)));
   }
 
   @VisibleForTesting
   static WaveletName createDummyWaveletName(WaveId waveId) {
     final WaveletName dummyWaveletName =
       WaveletName.of(waveId, new WaveletId(waveId.getDomain(), "dummy+root"));
     return dummyWaveletName;
   }
 }
