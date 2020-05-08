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
 package org.waveprotocol.box.webclient.client;
 
 import com.google.common.base.Preconditions;
 import com.google.gwt.core.client.JsArray;
 
 import org.waveprotocol.box.server.waveserver.DocumentSnapshot;
 import org.waveprotocol.box.server.waveserver.ProtocolSubmitRequest;
 import org.waveprotocol.box.server.waveserver.ProtocolSubmitResponse;
 import org.waveprotocol.box.server.waveserver.ProtocolWaveletUpdate;
 import org.waveprotocol.box.server.waveserver.WaveletSnapshot;
 import org.waveprotocol.box.webclient.common.CoreWaveletOperationSerializer;
 import org.waveprotocol.box.webclient.common.WaveletOperationSerializer;
 import org.waveprotocol.box.webclient.util.Log;
 import org.waveprotocol.box.webclient.util.URLEncoderDecoderBasedPercentEncoderDecoder;
 import org.waveprotocol.box.webclient.waveclient.common.SubmitResponseCallback;
 import org.waveprotocol.wave.concurrencycontrol.channel.WaveViewService;
 import org.waveprotocol.wave.concurrencycontrol.common.Delta;
 import org.waveprotocol.wave.concurrencycontrol.common.ResponseCode;
 import org.waveprotocol.wave.federation.ProtocolHashedVersion;
 import org.waveprotocol.wave.federation.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.document.operation.DocInitialization;
 import org.waveprotocol.wave.model.document.operation.impl.DocOpUtil;
 import org.waveprotocol.wave.model.id.IdFilter;
 import org.waveprotocol.wave.model.id.IdURIEncoderDecoder;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.id.WaveletIdSerializer;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperationContext;
 import org.waveprotocol.wave.model.util.CollectionUtils;
 import org.waveprotocol.wave.model.version.HashedVersion;
 import org.waveprotocol.wave.model.version.HashedVersionFactory;
 import org.waveprotocol.wave.model.version.HashedVersionZeroFactoryImpl;
 import org.waveprotocol.wave.model.wave.Constants;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.DocumentFactory;
 import org.waveprotocol.wave.model.wave.data.ObservableWaveletData;
 import org.waveprotocol.wave.model.wave.data.impl.WaveletDataImpl;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Implements the {@link WaveViewService} using RPCs.
  */
 public final class RemoteWaveViewService implements WaveViewService, WaveWebSocketCallback {
 
   private static final Log LOG = Log.get(RemoteWaveViewService.class);
 
   /**
    * Provides an update notification by lazily extracting and deserializing
    * components out of a serialized update message.
    */
   private class WaveViewServiceUpdateImpl implements WaveViewServiceUpdate {
     private final ProtocolWaveletUpdate update;
 
     // Cache expensive values
     private List<Delta> deltas;
     private ObservableWaveletData snapshot;
 
     WaveViewServiceUpdateImpl(ProtocolWaveletUpdate update) {
       this.update = update;
     }
 
     @Override
     public boolean hasChannelId() {
       return update.hasChannelId();
     }
 
     @Override
     public String getChannelId() {
       return update.getChannelId();
     }
 
     @Override
     public boolean hasCurrentVersion() {
       return update.hasResultingVersion();
     }
 
     @Override
     public HashedVersion getCurrentVersion() {
       return deserialize(update.getResultingVersion());
     }
 
     @Override
     public boolean hasDeltas() {
       return update.getAppliedDeltaArray() != null && update.getAppliedDeltaArray().length() > 0;
     }
 
     @Override
     public List<Delta> getDeltaList() {
       return deltas == null //
           ? deltas = deserialize(update.getAppliedDeltaArray(), update.getResultingVersion())
           : deltas;
     }
 
     @Override
     public boolean hasLastCommittedVersion() {
       return update.hasCommitNotice();
     }
 
     @Override
     public HashedVersion getLastCommittedVersion() {
       return deserialize(update.getResultingVersion());
     }
 
     @Override
     public boolean hasWaveletId() {
       return update.hasWaveletName();
     }
 
     @Override
     public WaveletId getWaveletId() {
       return deserialize(update.getWaveletName()).waveletId;
     }
 
     @Override
     public boolean hasWaveletSnapshot() {
       return update.hasSnapshot();
     }
 
     @Override
     public ObservableWaveletData getWaveletSnapshot() {
       return snapshot == null ? snapshot = deserialize(waveId, update.getSnapshot()) : snapshot;
     }
 
     @Override
     public boolean hasMarker() {
      return update.hasMarker();
     }
   }
 
   /**
    * The box server uses an incompatible signature scheme to the wave-protocol
    * libraries. This manager resolves those incompatibilities.
    */
   private static class VersionSignatureManager {
     private static final HashedVersionFactory HASHER = new HashedVersionZeroFactoryImpl(
         new IdURIEncoderDecoder(new URLEncoderDecoderBasedPercentEncoderDecoder()));
 
     /** Most recent signed versions. */
     private final Map<WaveletName, ProtocolHashedVersion> versions = CollectionUtils.newHashMap();
 
     /**
      * Records a signed server version.
      */
     void updateHistory(WaveletName wavelet, ProtocolHashedVersion update) {
       ProtocolHashedVersion current = versions.get(wavelet);
       if (current != null && current.getVersion() > update.getVersion()) {
         LOG.info("Ignoring superceded hash update: " + update);
         return;
       }
       versions.put(wavelet, update);
     }
 
     /**
      * Finds the most recent signed version for a delta.
      */
     ProtocolHashedVersion getServerVersion(WaveletName wavelet, Delta delta) {
       HashedVersion version;
       if (delta.getVersion() == 0) {
         return serialize(HASHER.createVersionZero(wavelet));
       } else {
         ProtocolHashedVersion current = versions.get(wavelet);
         Preconditions.checkNotNull(current);
         double prevVersion = current.getVersion();
         double deltaVersion = delta.getVersion();
         if (deltaVersion != prevVersion) {
           throw new IllegalArgumentException(
               "Client delta expressed against non-server version.  Server version: " + prevVersion
                   + ", client delta: " + deltaVersion);
         }
         return current;
       }
     }
   }
 
   private final WaveId waveId;
   private final RemoteViewServiceMultiplexer mux;
   private final DocumentFactory<?> docFactory;
   private final VersionSignatureManager versions = new VersionSignatureManager();
 
   /** Filter for client-side filtering. */
   // TODO: remove after Issue 124 is addressed.
   // http://code.google.com/p/wave-protocol/issues/detail?id=124
   private IdFilter filter;
 
   /** Callback once opened. */
   private OpenCallback callback;
 
   /**
    * Creates a service.
    *
    * @param waveId wave this service serves
    * @param mux underlying communication channel
    * @param docFactory document factory to use when deserializing snapshots
    */
   public RemoteWaveViewService(
       WaveId waveId, RemoteViewServiceMultiplexer mux, DocumentFactory<?> docFactory) {
     this.waveId = waveId;
     this.mux = mux;
     this.docFactory = docFactory;
   }
 
   //
   // ViewService API.
   //
 
   @Override
   public void viewOpen(final IdFilter filter,
       final Map<WaveletId, List<HashedVersion>> knownWavelets, final OpenCallback callback) {
     LOG.info("viewOpen called on " + waveId + " with " + filter);
 
     // Some legacy hack.  Important updates are sent to a "dummy+root" wavelet.
     // TODO: remove this once Issue 125 is fixed.
     // http://code.google.com/p/wave-protocol/issues/detail?id=125
     Set<String> newPrefixes = new HashSet<String>(filter.getPrefixes());
     newPrefixes.add("dummy");
     this.filter = IdFilter.of(filter.getIds(), newPrefixes);
     this.callback = callback;
 
     mux.open(waveId, filter, this);
   }
 
   @Override
   public String viewSubmit(
       final WaveletName wavelet, Delta delta, String channelId, final SubmitCallback callback) {
     ProtocolSubmitRequest submitRequest = ProtocolSubmitRequest.create();
     submitRequest.setWaveletName(serialize(wavelet));
     submitRequest.setDelta(serialize(wavelet, delta));
     submitRequest.setChannelId(channelId);
 
     mux.submit(submitRequest, new SubmitResponseCallback() {
       @Override
       public void run(ProtocolSubmitResponse response) {
         HashedVersion resultVersion = HashedVersion.unsigned(0);
         if (response.hasHashedVersionAfterApplication()) {
           resultVersion = CoreWaveletOperationSerializer.deserialize(
               response.getHashedVersionAfterApplication());
           versions.updateHistory(wavelet, response.getHashedVersionAfterApplication());
         }
         callback.onSuccess(resultVersion, response.getOperationsApplied(), null, ResponseCode.OK);
       }
     });
 
     // We don't support the getDebugProfiling thing anyway.
     return null;
   }
 
   @Override
   public void viewClose(final WaveId waveId, final String channelId, final CloseCallback callback) {
     Preconditions.checkArgument(this.waveId.equals(waveId));
     LOG.info("closing channel " + waveId);
     callback.onSuccess();
     mux.close(waveId, this);
   }
 
   @Override
   public String debugGetProfilingInfo(final String requestId) {
     throw new UnsupportedOperationException();
   }
 
   //
   // Incoming updates.
   //
 
   @Override
   public void onWaveletUpdate(ProtocolWaveletUpdate update) {
     if (shouldAccept(update)) {
       // Update last-known-version map, so that outgoing deltas can be appropriately rewritten.
       if (update.hasResultingVersion()) {
         versions.updateHistory(getTarget(update), update.getResultingVersion());
       }
 
       // Adapt broken parts of the box server, to make them speak the proper wave protocol:
       // 1. Channel id must be in its own message.
       // 2. Synthesize the open-finished marker that the box server leaves out.
       if (update.hasChannelId()
           && (update.hasCommitNotice() || update.hasMarker() || update.hasSnapshot()
               || update.getAppliedDeltaArray().length() > 0)) {
         ProtocolWaveletUpdate fake =
             ProtocolWaveletUpdate.create().setChannelId(update.getChannelId());
         update.clearChannelId();
         callback.onUpdate(deserialize(fake));
         callback.onUpdate(deserialize(update));
       } else {
         callback.onUpdate(deserialize(update));
       }
     }
   }
 
   private WaveViewServiceUpdateImpl deserialize(ProtocolWaveletUpdate update) {
     return new WaveViewServiceUpdateImpl(update);
   }
 
   /** @return the target wavelet of an update. */
   private WaveletName getTarget(ProtocolWaveletUpdate update) {
     WaveletName name = deserialize(update.getWaveletName());
     Preconditions.checkState(name.waveId.equals(waveId));
     return name;
   }
 
   /** @return true if this update matches this service's filter. */
   private boolean shouldAccept(ProtocolWaveletUpdate update) {
     return IdFilter.accepts(filter, getTarget(update).waveletId);
   }
 
   //
   // Serialization.
   //
 
   private ProtocolWaveletDelta serialize(WaveletName wavelet, Delta delta) {
     ProtocolWaveletDelta protocolDelta = ProtocolWaveletDelta.create();
     for (WaveletOperation op : delta) {
       protocolDelta.addOperation(WaveletOperationSerializer.serialize(op));
     }
     // Swap the signed version for a hashed version.
     protocolDelta.setHashedVersion(versions.getServerVersion(wavelet, delta));
     return protocolDelta;
   }
 
   private static List<Delta> deserialize(
       JsArray<ProtocolWaveletDelta> deltas, ProtocolHashedVersion end) {
     if (deltas == null) {
       return null;
     } else {
       List<Delta> parsed = new ArrayList<Delta>();
 
       for (int i = 0; i < deltas.length(); i++) {
         ProtocolHashedVersion thisEnd = //
           i < deltas.length() - 1 ? deltas.get(i + 1).getHashedVersion() : end;
           parsed.add(deserialize(deltas.get(i), thisEnd));
       }
 
       return parsed;
     }
   }
 
   private static Delta deserialize(ProtocolWaveletDelta delta, ProtocolHashedVersion end) {
     ParticipantId author = new ParticipantId(delta.getAuthor());
     WaveletOperationContext woc = new WaveletOperationContext(author, Constants.NO_TIMESTAMP, 1);
     return WaveletOperationSerializer.deserialize(delta, deserialize(end), woc);
   }
 
   private ObservableWaveletData deserialize(WaveId waveId, WaveletSnapshot snapshot) {
     ParticipantId creator = new ParticipantId(snapshot.getParticipantId(0));
     HashedVersion version = deserialize(snapshot.getVersion());
     long lmt = (long) snapshot.getLastModifiedTime();
     long ctime = (long) snapshot.getCreationTime();
     WaveletId id = WaveletIdSerializer.INSTANCE.fromString(snapshot.getWaveletId());
     long lmv = version.getVersion();
 
     WaveletDataImpl waveletData =
         new WaveletDataImpl(id, creator, ctime, lmv, version, lmt, waveId, docFactory);
     for (String participant : snapshot.getParticipantIdList()) {
       waveletData.addParticipant(new ParticipantId(participant));
     }
     for (int i = 0; i < snapshot.getDocumentCount(); i++) {
       DocumentSnapshot docSnapshot = snapshot.getDocument(i);
       DocInitialization docOp = DocOpUtil.asInitialization(
           WaveletOperationSerializer.deserialize(docSnapshot.getDocumentOperation()));
       String docId = docSnapshot.getDocumentId();
       waveletData.createDocument(docId, docOp);
     }
     return waveletData;
   }
 
   private static String serialize(WaveletName wavelet) {
     return RemoteViewServiceMultiplexer.serialize(wavelet);
   }
 
   private static WaveletName deserialize(String wavelet) {
     return RemoteViewServiceMultiplexer.deserialize(wavelet);
   }
 
   private static ProtocolHashedVersion serialize(HashedVersion version) {
     return CoreWaveletOperationSerializer.serialize(version);
   }
 
   private static HashedVersion deserialize(ProtocolHashedVersion version) {
     return CoreWaveletOperationSerializer.deserialize(version);
   }
 }
