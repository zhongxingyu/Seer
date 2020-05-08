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
 
 package org.waveprotocol.wave.examples.client.common;
 
 import static org.waveprotocol.wave.examples.fedone.common.CommonConstants.INDEX_WAVE_ID;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.google.protobuf.RpcCallback;
 import com.google.protobuf.RpcController;
 
 import org.waveprotocol.wave.examples.fedone.common.CoreWaveletOperationSerializer;
 import org.waveprotocol.wave.examples.fedone.common.DocumentConstants;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersionFactory;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersionZeroFactoryImpl;
 import org.waveprotocol.wave.examples.fedone.common.VersionedWaveletDelta;
 import org.waveprotocol.wave.examples.fedone.frontend.IndexWave;
 import org.waveprotocol.wave.examples.fedone.rpc.ClientRpcChannel;
 import org.waveprotocol.wave.examples.fedone.rpc.ClientRpcChannelImpl;
 import org.waveprotocol.wave.examples.fedone.util.BlockingSuccessFailCallback;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.examples.fedone.util.SuccessFailCallback;
 import org.waveprotocol.wave.examples.fedone.util.URLEncoderDecoderBasedPercentEncoderDecoder;
 import org.waveprotocol.wave.examples.fedone.util.WaveletDataUtil;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolOpenRequest;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitRequest;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitResponse;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveClientRpc;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveletUpdate;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.WaveletSnapshot;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
 import org.waveprotocol.wave.model.document.operation.impl.DocOpUtil;
 import org.waveprotocol.wave.model.id.IdGenerator;
 import org.waveprotocol.wave.model.id.IdGeneratorImpl;
 import org.waveprotocol.wave.model.id.IdURIEncoderDecoder;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.id.URIEncoderDecoder.EncodingException;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.operation.core.CoreAddParticipant;
 import org.waveprotocol.wave.model.operation.core.CoreNoOp;
 import org.waveprotocol.wave.model.operation.core.CoreRemoveParticipant;
 import org.waveprotocol.wave.model.operation.core.CoreWaveletDelta;
 import org.waveprotocol.wave.model.operation.core.CoreWaveletDocumentOperation;
 import org.waveprotocol.wave.model.operation.core.CoreWaveletOperation;
 import org.waveprotocol.wave.model.util.Pair;
 import org.waveprotocol.wave.model.version.DistinctVersion;
 import org.waveprotocol.wave.model.wave.Constants;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.BlipData;
 import org.waveprotocol.wave.model.wave.data.ObservableWaveletData;
 import org.waveprotocol.wave.model.wave.data.WaveletData;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Handler;
 import java.util.logging.SimpleFormatter;
 import java.util.logging.StreamHandler;
 
 /**
  * The "backend" of a basic wave client, designed for user interfaces to
  * interact with.
  */
 public class ClientBackend {
 
   /**
    * Factory to create a client backend.
    */
   public interface Factory {
     /**
      * Creates a ClientBacked tied permanently and connected to a given server
      * and user.
      *
      * @param userAtDomain the user and their domain (for example, foo@bar.org).
      * @param server to connect to (for example, acmewave.com).
      * @param port to connect to the server with.
      * @throws IOException if the client backend can't connect to the server.
      * @return the new ClientBackend.
      */
    ClientBackend create(String userAtDomain, String server, int port) throws IOException;
   }
 
   /**
    * Factory for the various RPC objects used by the client backend.
    */
   public interface RpcObjectFactory {
     /**
     * @return a {@link ClientRpcChannel} connected to the given server and
      *         port.
      */
    ClientRpcChannel createClientChannel(String server, int port) throws IOException;
 
     /**
     * @return an RPC server interface backed by the given
     *         {@link ClientRpcChannel}.
      */
    ProtocolWaveClientRpc.Interface createServerInterface(ClientRpcChannel channel);
   }
 
   /**
    * The default ClientBackend factory.
    */
   public static class DefaultFactory implements Factory {
     @Override
    public ClientBackend create(String userAtDomain, String server, int port) throws IOException {
       return new ClientBackend(userAtDomain, server, port);
     }
   }
 
   /**
   * Container for data to {@link WaveletOperationListener}s on events.
    */
   private static class WaveletEventData {
 
     private final String author;
     private final HashedVersion hashedVersion;
     private final WaveletData waveletData;
     private final CoreWaveletOperation waveletOperation;
     // Hack to mark whether this is actually the "end of a delta sequence", in which case the
     // above data is irrelevant.
     private final boolean isDeltaSequenceEnd;
     // Another hack to mark whether this is a commit notice event.
     // TODO(Michael): Get rid of these hacks!
     private final boolean isCommitNotice;
 
     /**
      * Standard constructor.
      */
     WaveletEventData(String author, WaveletData waveletData,
         CoreWaveletOperation waveletOperation) {
       this.author = author;
       this.hashedVersion = null;
       this.waveletData = waveletData;
       this.waveletOperation = waveletOperation;
       this.isDeltaSequenceEnd = false;
       this.isCommitNotice = false;
     }
 
     /**
      * Constructor to set this to a delta sequence end marker.
      */
     WaveletEventData(WaveletData waveletData) {
       this.author = null;
       this.hashedVersion = null;
       this.waveletData = waveletData;
       this.waveletOperation = null;
       this.isDeltaSequenceEnd = true;
       this.isCommitNotice = false;
     }
 
     /**
      * Constructor for commit notice events.
      */
     WaveletEventData(WaveletData waveletData, HashedVersion hashedVersion) {
       this.author = null;
       this.hashedVersion = hashedVersion;
       this.waveletData = waveletData;
       this.waveletOperation = null;
       this.isDeltaSequenceEnd = false;
       this.isCommitNotice = true;
     }
 
     String getAuthor() {
       return author;
     }
 
     HashedVersion getHashedVersion() {
       return hashedVersion;
     }
 
     WaveletData getWaveletData() {
       return waveletData;
     }
 
     CoreWaveletOperation getWaveletOperation() {
       return waveletOperation;
     }
 
     boolean isDeltaSequenceEnd() {
       return isDeltaSequenceEnd;
     }
 
     boolean isCommitNotice() {
       return isCommitNotice;
     }
   }
 
   private static final Log LOG = Log.get(ClientBackend.class);
 
   private static final ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
   static {
     LOG.getLogger().addHandler(new StreamHandler(logOutput, new SimpleFormatter()));
     LOG.getLogger().setUseParentHandlers(false);
   }
 
   /** Id URI encoder and decoder. */
   private static final IdURIEncoderDecoder URI_CODEC = new IdURIEncoderDecoder(
       new URLEncoderDecoderBasedPercentEncoderDecoder());
 
   /** User id of the user of the backend (encapsulating both user and server). */
   private final ParticipantId userId;
 
   /** Waves this backend is aware of. */
   private final Map<WaveId, ClientWaveView> waves = Maps.newHashMap();
 
   /** RPC controllers for the open wave connections. */
   private final Map<WaveId, RpcController> waveControllers = Maps.newHashMap();
 
   /** Listeners waiting on wave updates. */
   private final Set<WaveletOperationListener> waveletOperationListeners = Sets.newHashSet();
 
   /** Id generator used for this (server, user) pair. */
   private final IdGenerator idGenerator;
 
   /** The port number through which this backend should connect to the server */
   private final int port;
 
   /** The server address that this backend should connect to */
   private final String server;
 
   /** RPC interface for communicating with server. */
   private final ProtocolWaveClientRpc.Interface rpcServer;
 
   /** RPC channel for communicating with server. */
   private final ClientRpcChannel rpcChannel;
 
   /** Producer/consumer event queue. */
   private final BlockingQueue<WaveletEventData> eventQueue =
       new LinkedBlockingQueue<WaveletEventData>();
 
   /**
    * HashedVersionFactory for creating version 0 hash only, used only when client creates a
    * wavelet.
    */
   private final HashedVersionFactory hashedVersionFactory;
 
   /**
    * Create new client backend tied permanently to a given server and user, using a default
   * {@link RpcObjectFactory} implementation. Open the client's index, and begin managing waves it
    * has access to.
    *
    * @param userAtDomain the user and their domain (for example, foo@bar.org).
    * @param server to connect to (for example, acmewave.com).
    * @param port to connect to the server with.
    * @throws IOException if we can't connect to the server.
    */
   public ClientBackend(final String userAtDomain, String server, int port) throws IOException {
     this(userAtDomain, server, port,
         new RpcObjectFactory() {
           @Override
           public ClientRpcChannel createClientChannel(String server, int port) throws IOException {
             return new ClientRpcChannelImpl(new InetSocketAddress(server, port));
           }
 
           @Override
           public ProtocolWaveClientRpc.Interface createServerInterface(ClientRpcChannel channel) {
             return ProtocolWaveClientRpc.newStub(channel);
           }
         }, new HashedVersionZeroFactoryImpl(URI_CODEC));
   }
 
   /**
    * Create new client backend tied permanently to a given server and user, open
    * that client's index, and begin managing waves it has access to.
    *
    * @param userAtDomain the user and their domain (for example, foo@bar.org).
    * @param server to connect to (for example, acmewave.com).
    * @param port to connect to the server with.
    * @param rpcObjectFactory to use for creating RPC objects.
    * @throws IOException if we can't connect to the server.
    */
   @Inject
   public ClientBackend(final String userAtDomain, String server, int port,
       RpcObjectFactory rpcObjectFactory, HashedVersionFactory hashedVersionFactory)
       throws IOException {
     Preconditions.checkNotNull(server, "Server not specified");
     Preconditions.checkArgument(port >= 0, "Port number must be greater than 0");
 
     this.userId = ParticipantId.ofUnsafe(userAtDomain);
     this.server = server;
     this.port = port;
     this.idGenerator = new IdGeneratorImpl(userId.getDomain(), new IdGeneratorImpl.Seed() {
       Random r = new Random();
 
       @Override
       public String get() {
         return Long.toString(Math.abs(r.nextLong()), 36);
       }
 
     });
     this.hashedVersionFactory = hashedVersionFactory;
 
     // Start pushing events to listeners in a separate thread.
     new Thread() {
       @Override
       public void run() {
         for (;;) {
           try {
             WaveletEventData nextEvent = eventQueue.take();
             if (nextEvent.isDeltaSequenceEnd()) {
               for (WaveletOperationListener listener : waveletOperationListeners) {
                 listener.onDeltaSequenceEnd(nextEvent.getWaveletData());
               }
             } else if (nextEvent.isCommitNotice()) {
               for (WaveletOperationListener listener : waveletOperationListeners) {
                 listener.onCommitNotice(nextEvent.getWaveletData(), nextEvent.getHashedVersion());
               }
             } else {
               notifyWaveletOperationListeners(nextEvent.getAuthor(), nextEvent.getWaveletData(),
                   nextEvent.getWaveletOperation());
             }
           } catch (InterruptedException e) {
             // TODO: stop?
           }
         }
       }
     }.start();
 
     // Connect to the specfied server and port.
     this.rpcChannel = rpcObjectFactory.createClientChannel(server, port);
     this.rpcServer = rpcObjectFactory.createServerInterface(rpcChannel);
 
     // Opening the index wave will kickstart the process of receiving waves.
     openWave(INDEX_WAVE_ID, "");
   }
 
   /**
    * @return the client backend log.
    */
   public static String getLog() {
     for (Handler handler : LOG.getLogger().getHandlers()) {
       handler.flush();
     }
     return logOutput.toString();
   }
 
   /**
    * Clear the log.
    */
   public static void clearLog() {
     logOutput.reset();
   }
 
   /**
    * Gracefully shut down the backend, closing all connections to the server and clearing the
    * collection of waves (since they will no longer be valid).  This renders the backend relatively
    * useless since no more updates from the index wave will be received, but it is still valid.
    */
   public void shutdown() {
     for (RpcController rpcController : waveControllers.values()) {
       LOG.info("Cancelling RpcController " + rpcController);
       rpcController.startCancel();
     }
 
     waves.clear();
     waveControllers.clear();
   }
 
   /**
    * Open a wave.  This method will return immediately and updates will be delivered internally
    * from the RPC interface, and externally to {@link WaveletOperationListener}s.
    *
    * @param waveId of wave to open
    * @param waveletIdPrefix filter such that the server will send wavelet updates for ids that
    * match any of this prefix
    */
   private void openWave(WaveId waveId, String waveletIdPrefix) {
     if (waveControllers.containsKey(waveId)) {
       throw new IllegalArgumentException(waveId + " is already open");
     } else {
       // The wave may already be there if created with createNewWave.
       if (!waves.containsKey(waveId)) {
         createWave(waveId);
       }
     }
 
     ProtocolOpenRequest.Builder openRequest = ProtocolOpenRequest.newBuilder();
 
     openRequest.setParticipantId(getUserId().getAddress());
     openRequest.setWaveId(waveId.serialise());
     openRequest.addWaveletIdPrefix(waveletIdPrefix);
 
     final RpcController rpcController = rpcChannel.newRpcController();
     waveControllers.put(waveId, rpcController);
 
     LOG.info("Opening wave " + waveId + " for prefix \"" + waveletIdPrefix + '"');
     rpcServer.open(
         rpcController,
         openRequest.build(),
         new RpcCallback<ProtocolWaveletUpdate>() {
           @Override public void run(ProtocolWaveletUpdate update) {
             if (update == null) {
               LOG.warning("RPC failed: " + rpcController.errorText());
             } else {
               receiveWaveletUpdate(update);
             }
           }
         }
     );
   }
 
   /**
    * Create a new conversation wave and tell the server about it, by adding ourselves as a
    * participant on the conversation root.
    *
    * @param callback callback invoked when the server rpc is complete
    * @return the {@link ClientWaveView} created
    */
   public ClientWaveView createConversationWave(
       SuccessFailCallback<ProtocolSubmitResponse, String> callback) {
     return createConversationWave(getIdGenerator().newWaveId(), callback);
   }
 
   /**
    * Create a new conversation wave with a given wave id and tell the server about it, by adding
    * ourselves as a participant on the conversation root.
    *
    * @param newWaveId the id to give the new wave
    * @param callback callback invoked when the server rpc is complete
    * @return the {@link ClientWaveView} created
    */
   private ClientWaveView createConversationWave(WaveId newWaveId,
       SuccessFailCallback<ProtocolSubmitResponse, String> callback) {
     ClientWaveView waveView = createWave(newWaveId);
     WaveletId waveletId = getIdGenerator().newConversationRootWaveletId();
 
     // Add ourselves in the first operation.
     CoreAddParticipant addUserOp = new CoreAddParticipant(getUserId());
 
     // Create a document manifest in the second operation.
     CoreWaveletDocumentOperation addManifestOp = new CoreWaveletDocumentOperation(
         DocumentConstants.MANIFEST_DOCUMENT_ID, ClientUtils.createManifest());
 
     sendWaveletDelta(WaveletName.of(newWaveId, waveletId),
         new CoreWaveletDelta(getUserId(), ImmutableList.of(addUserOp, addManifestOp)), callback);
 
     return waveView;
   }
 
   /**
    * @param id of wave to get
    * @return wave with the given id, or null if not found
    */
   public ClientWaveView getWave(WaveId id) {
     return waves.get(id);
   }
 
   /**
    * @return the special wave containing the index data
    */
   public ClientWaveView getIndexWave() {
     return getWave(INDEX_WAVE_ID);
   }
 
   /**
    * Send a single wavelet operation over the wire.
    *
    * @param waveletName of the wavelet to apply the operation to
    * @param op to send
    * @param callback callback invoked when the server rpc is complete
    */
   public void sendWaveletOperation(WaveletName waveletName, CoreWaveletOperation op,
       SuccessFailCallback<ProtocolSubmitResponse, String> callback) {
     sendWaveletDelta(waveletName, new CoreWaveletDelta(getUserId(), ImmutableList.of(op)),
         callback);
   }
 
   /**
    * Send a single wavelet operation over the wire and wait for the roundtrip success: the submit
    * callback from our local server, and the wavelet to be updated to the version given in the
    * response.
    *
    * @param waveletName of the wavelet to apply the operation to
    * @param op to send
    * @param timeout used twice, so real timeout may be up to twice that specified
    * @param unit of timeout
    * @return true if the roundtrip trip was successful, false otherwise
    */
   public boolean sendAndAwaitWaveletOperation(WaveletName waveletName, CoreWaveletOperation op,
       long timeout, TimeUnit unit) {
     return sendAndAwaitWaveletDelta(waveletName, new CoreWaveletDelta(getUserId(),
         ImmutableList.of(op)), timeout, unit);
   }
 
   /**
    * Send a wavelet delta over the wire.
    *
    * @param waveletName of the wavelet that the delta applies to
    * @param delta to send
    * @param callback callback invoked when the server rpc is complete
    */
   public void sendWaveletDelta(WaveletName waveletName, CoreWaveletDelta delta,
       final SuccessFailCallback<ProtocolSubmitResponse, String> callback) {
     // Build the submit request.
     ProtocolSubmitRequest.Builder submitRequest = ProtocolSubmitRequest.newBuilder();
 
     try {
       submitRequest.setWaveletName(URI_CODEC.waveletNameToURI(waveletName));
     } catch (EncodingException e) {
       throw new IllegalArgumentException(e);
     }
 
     ClientWaveView wave = waves.get(waveletName.waveId);
     submitRequest.setDelta(CoreWaveletOperationSerializer.serialize(delta,
         wave.getWaveletVersion(waveletName.waveletId)));
 
     final RpcController rpcController = rpcChannel.newRpcController();
 
     LOG.info("Sending delta " + delta + " for " + waveletName);
     rpcServer.submit(rpcController, submitRequest.build(),
         new RpcCallback<ProtocolSubmitResponse>() {
           @Override
           public void run(ProtocolSubmitResponse response) {
             if (response == null) {
               callback.onFailure("null response");
             } else if (rpcController.failed()) {
               callback.onFailure(rpcController.errorText());
             } else {
               callback.onSuccess(response);
             }
           }
         });
   }
 
   /**
    * Send a wavelet delta over the wire and wait for the roundtrip success: the submit callback
    * from our local server, and the wavelet to be updated to the version given in the response.
    *
    * @param waveletName of the wavelet that the delta applies to
    * @param delta to send
    * @param timeout used twice, so real timeout may be up to twice that specified
    * @param unit of timeout
    * @return true if the roundtrip trip was successful, false otherwise
    */
   public boolean sendAndAwaitWaveletDelta(WaveletName waveletName, CoreWaveletDelta delta,
       long timeout, TimeUnit unit) {
     BlockingSuccessFailCallback<ProtocolSubmitResponse, String> callback =
         BlockingSuccessFailCallback.create();
     sendWaveletDelta(waveletName, delta, callback);
     Pair<ProtocolSubmitResponse, String> result = callback.await(timeout, unit);
     if (result == null) {
       LOG.warning("Error: submit result pair for " + waveletName + " was null, timed out?");
       return false;
     } else if (result.getFirst() == null) {
       LOG.warning("Error: submit response to " + waveletName + " was null: " + result.getSecond());
       return false;
     } else {
       return getWave(waveletName.waveId).awaitWaveletVersion(waveletName.waveletId,
           result.getFirst().getHashedVersionAfterApplication().getVersion(), timeout, unit);
     }
   }
 
   /**
    * Receive a protocol wavelet update from the wave server.
    *
    * @param waveletUpdate the wavelet update
    */
   public void receiveWaveletUpdate(ProtocolWaveletUpdate waveletUpdate) {
     LOG.info("Received update " + waveletUpdate);
 
     WaveletName waveletName = getWaveletName(waveletUpdate);
 
     // Apply operations to the wavelet.
     List<WaveletEventData> events = Lists.newArrayList();
 
     ObservableWaveletData wavelet;
     if (waveletUpdate.hasSnapshot()) {
       List<WaveletEventData> snapshotEvents = Lists.newArrayList();
       wavelet = receiveSnapshot(waveletUpdate, snapshotEvents);
       events.addAll(snapshotEvents);
     } else if (!waveletUpdate.getAppliedDeltaList().isEmpty()) {
       List<WaveletEventData> deltaEvents = Lists.newArrayList();
       wavelet = receiveDeltas(waveletUpdate, events);
       events.addAll(deltaEvents);
     } else if (waveletUpdate.hasMarker()) {
       // Don't do anything at the moment.
       return;
     } else {
       // TODO(ljvderijk): Might be simplified, might be removed when protocol
       // update is complete.
       ClientWaveView wave = getExistingWaveView(waveletName);
       wavelet = wave.getWavelet(waveletName.waveletId);
     }
 
     ClientWaveView wave = getExistingWaveView(waveletName);
 
     if (waveletUpdate.hasResultingVersion()) {
       wave.setWaveletVersion(waveletName.waveletId,
           CoreWaveletOperationSerializer.deserialize(waveletUpdate.getResultingVersion()));
     }
 
     // If we have been removed from this wavelet then remove the data too,
     // since if we're re-added then we will get a fresh snapshot or deltas
     // from version 0, not the latest version we've seen.
     if (!wavelet.getParticipants().contains(getUserId())) {
       wave.removeWavelet(waveletName.waveletId);
     }
 
     // If it was an update to the index wave, might need to open/close some
     // more waves.
     if (IndexWave.isIndexWave(wave)) {
       syncWithIndexWave(wave);
     }
 
     if (waveletUpdate.hasCommitNotice()) {
       events.add(new WaveletEventData(
           wavelet, CoreWaveletOperationSerializer.deserialize(waveletUpdate.getCommitNotice())));
     }
 
     events.add(new WaveletEventData(wavelet));
 
     // Push all events to the eventQueue.
     for (WaveletEventData waveletEventData : events) {
       eventQueue.offer(waveletEventData);
     }
   }
 
   /**
    * Handles the receiving of a snapshot contained in a
    * {@link ProtocolWaveletUpdate}.
    *
    * The wavelet must not exist yet in the {@link ClientWaveView}.
    *
    * @param waveletUpdate the update proto to handle.
    * @param events the list where this method keeps track of the events it has
    *        triggered.
    * @return the deserialized {@link ObservableWaveletData} stored in the
    *         snapshot.
    */
   private ObservableWaveletData receiveSnapshot(
       ProtocolWaveletUpdate waveletUpdate, List<WaveletEventData> events) {
     Preconditions.checkArgument(waveletUpdate.hasResultingVersion());
 
     WaveletName waveletName = getWaveletName(waveletUpdate);
 
     ClientWaveView wave = getExistingWaveView(waveletName);
     ObservableWaveletData wavelet = wave.getWavelet(waveletName.waveletId);
     Preconditions.checkState(wavelet == null, "Wavelet must be null");
 
     final WaveletSnapshot snapshot = waveletUpdate.getSnapshot();
     try {
       wavelet = CoreWaveletOperationSerializer.deserializeSnapshot(
           snapshot, waveletUpdate.getResultingVersion(), waveletName);
       wave.addWavelet(
           wavelet, CoreWaveletOperationSerializer.deserialize(waveletUpdate.getResultingVersion()));
 
       // Push the snapshot as operations to the event list
       for (ParticipantId participant : wavelet.getParticipants()) {
         events.add(new WaveletEventData(
             wavelet.getCreator().getAddress(), wavelet, new CoreAddParticipant(participant)));
       }
       for (String documentId : wavelet.getDocumentIds()) {
         BlipData doc = wavelet.getDocument(documentId);
         BufferedDocOp docOp = DocOpUtil.buffer(doc.getContent().asOperation());
         events.add(
             new WaveletEventData(wavelet.getCreator().getAddress(), wavelet,
                 new CoreWaveletDocumentOperation(documentId, docOp)));
       }
     } catch (OperationException e) {
       // It should be okay (if cheeky) for the client to just ignore failed
       // ops. In any case, this should never happen if our server is
       // behaving correctly.
       LOG.severe("OperationException when creating snapshot ", e);
     }
     return wavelet;
   }
 
   /**
    * Handles the deltas contained in a {@link ProtocolWaveletUpdate}.
    *
    * @param waveletUpdate the update proto to handle.
    * @param events the list where this method keeps track of the events it has
    *        triggered.
    * @return the {@link ObservableWaveletData} updated with the deltas present
    *         in the {@link ProtocolWaveletUpdate}.
    */
   private ObservableWaveletData receiveDeltas(
       ProtocolWaveletUpdate waveletUpdate, List<WaveletEventData> events) {
     Preconditions.checkArgument(waveletUpdate.hasResultingVersion());
     Preconditions.checkArgument(!waveletUpdate.getAppliedDeltaList().isEmpty());
 
     WaveletName waveletName = getWaveletName(waveletUpdate);
     ClientWaveView wave = getExistingWaveView(waveletName);
     ObservableWaveletData wavelet = wave.getWavelet(waveletName.waveletId);
 
     // TODO(ljvderijk): enable when protocol is fixed so that we always get a
     // snapshot first.
     // Preconditions.checkState(wavelet != null, "Wavelet must be present!");
 
     for (ProtocolWaveletDelta protobufDelta : waveletUpdate.getAppliedDeltaList()) {
       VersionedWaveletDelta versionedWaveletDelta =
           CoreWaveletOperationSerializer.deserialize(protobufDelta);
       CoreWaveletDelta delta = versionedWaveletDelta.delta;
 
       long dummyTimestamp = Constants.NO_TIMESTAMP;
 
       if (wavelet == null) {
         // TODO(ljvderijk): This should never happen, but it currently does.
         // Snapshot should be received first.
         // Instantiate a new wavelet
         wavelet =
             WaveletDataUtil.createEmptyWavelet(waveletName, delta.getAuthor(), dummyTimestamp);
         wave.addWavelet(wavelet, hashedVersionFactory.createVersionZero(waveletName));
       }
 
       DistinctVersion dummyVersion =
           DistinctVersion.of(wavelet.getVersion() + delta.getOperations().size(), 0);
       try {
         WaveletDataUtil.applyWaveletDelta(delta, wavelet, dummyVersion, dummyTimestamp);
       } catch (OperationException e) {
         LOG.severe("Operations failed to apply", e);
       }
 
       // All operations were successful so put them in the list of events.
       for (CoreWaveletOperation op : delta.getOperations()) {
         events.add(new WaveletEventData(delta.getAuthor().getAddress(), wavelet, op));
       }
     }
     return wavelet;
   }
 
   /**
    * Creates a new, empty wave view and stores it in {@code waves}.
    * @param waveId the new wave id
   * @return the new wave's {@link ClientWaveView}
    */
   private ClientWaveView createWave(WaveId waveId) {
     ClientWaveView wave = new ClientWaveView(hashedVersionFactory, waveId);
     waves.put(waveId, wave);
     return wave;
   }
 
   /**
    * Returns the {@link WaveletName} stored in the
    * {@link ProtocolWaveletUpdate}.
    *
    * @param waveletUpdate the update containing the name
    * @throws IllegalArgumentException if the data in the
    *         {@link ProtocolWaveletUpdate} could not be decoded.
    */
   private WaveletName getWaveletName(ProtocolWaveletUpdate waveletUpdate) {
     WaveletName waveletName;
     try {
       waveletName = URI_CODEC.uriToWaveletName(waveletUpdate.getWaveletName());
     } catch (EncodingException e) {
       throw new IllegalArgumentException(e);
     }
     return waveletName;
   }
 
   /**
    * Returns a {@link ClientWaveView} for the given {@link WaveletName}. The
    * {@link ClientWaveView} for this wave must already exist.
    *
    * @param waveletName contains the waveId of the {@link ClientWaveView} to be
    *        returned.
    * @throws IllegalArgumentException if the {@link ClientWaveView} does not
    *         exist.
    */
   private ClientWaveView getExistingWaveView(WaveletName waveletName) {
     ClientWaveView wave = waves.get(waveletName.waveId);
     Preconditions.checkArgument(
         wave != null, "Request for ClientWaveView with absent waveId " + waveletName.waveId);
     return wave;
   }
 
   /**
    * Synchronize with the index wave by opening any waves that appear in the index but that we
    * don't have an RPC open request to.
    *
    * @param indexWave to synchronize with
    */
   private void syncWithIndexWave(ClientWaveView indexWave) {
     List<IndexEntry> indexEntries = IndexWave.getIndexEntries(indexWave);
 
     for (IndexEntry indexEntry : indexEntries) {
       if (!waveControllers.containsKey(indexEntry.getWaveId())) {
         WaveId waveId = indexEntry.getWaveId();
         openWave(waveId, ClientUtils.getConversationRootId(waveId).getId());
       }
     }
   }
 
   /**
    * Notify all wavelet operation listeners of a wavelet operation.
    *
    * @param author the author who caused the operation
    * @param wavelet operated on
    * @param op the operation
    */
   private void notifyWaveletOperationListeners(String author, WaveletData wavelet,
       CoreWaveletOperation op) {
     for (WaveletOperationListener listener : waveletOperationListeners) {
       try {
         if (op instanceof CoreWaveletDocumentOperation) {
           CoreWaveletDocumentOperation waveletDocOp = (CoreWaveletDocumentOperation) op;
           listener.waveletDocumentUpdated(author, wavelet, waveletDocOp.getDocumentId(),
               waveletDocOp.getOperation());
         } else if (op instanceof CoreAddParticipant) {
           listener.participantAdded(author, wavelet, ((CoreAddParticipant) op).getParticipantId());
         } else if (op instanceof CoreRemoveParticipant) {
           listener.participantRemoved(author, wavelet,
               ((CoreRemoveParticipant) op).getParticipantId());
         } else if (op instanceof CoreNoOp) {
           listener.noOp(author, wavelet);
         }
       } catch (RuntimeException e) {
         LOG.severe("RuntimeException for listener " + listener, e);
       }
     }
   }
 
   /**
    * @return the port number that this client backend is bound to.
    */
   public int getPort() {
     return port;
   }
 
   /**
    * @return the server that this client backend is bound to.
    */
   public String getServer() {
     return server;
   }
 
   /**
    * @return the {@link ParticipantId} id of the user
    */
   public ParticipantId getUserId() {
     return userId;
   }
 
   /**
    * @return the id generator which generates wave, wavelet, and document ids
    */
   public IdGenerator getIdGenerator() {
     return idGenerator;
   }
 
   /**
    * Returns the set of wave IDs of the currently open waves, optionally including the index wave.
    *
    * @param includeIndexWave should the index wave be included in the returned set?
    * @return the set of wave Ids of the open waves.
    */
   public Set<WaveId> getOpenWaveIds(boolean includeIndexWave) {
     Set<WaveId> waveIds = waves.keySet();
     if (!includeIndexWave) {
       waveIds = Sets.newHashSet(waveIds);
       waveIds.remove(INDEX_WAVE_ID);
     }
     return Collections.unmodifiableSet(waveIds);
   }
 
   /**
    * @return the list of currently registered operation listeners
    */
   @VisibleForTesting
   public Set<WaveletOperationListener> getListeners() {
     return Collections.unmodifiableSet(waveletOperationListeners);
   }
 
   /**
    * Add a {@link WaveletOperationListener} to be notified whenever a wave is updated.
    *
    * @param listener new listener
    */
   public void addWaveletOperationListener(WaveletOperationListener listener) {
     waveletOperationListeners.add(listener);
   }
 
   /**
    * @param listener listener to be removed
    */
   public void removeWaveletOperationListener(WaveletOperationListener listener) {
     waveletOperationListeners.remove(listener);
   }
 
   /**
    * Waits until all accumulated events have been processed in the events thread.
    */
   @VisibleForTesting
   public void waitForAccumulatedEventsToProcess() {
     while (!eventQueue.isEmpty()) {
       Thread.yield();
     }
   }
 }
