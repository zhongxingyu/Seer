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
 
 package org.waveprotocol.wave.examples.fedone.waveserver;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.inject.internal.Preconditions;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import org.waveprotocol.wave.crypto.SignatureException;
 import org.waveprotocol.wave.crypto.UnknownSignerException;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
 import static org.waveprotocol.wave.examples.fedone.common.WaveletOperationSerializer.serialize;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveletContainer.State;
 import org.waveprotocol.wave.federation.FederationErrorProto.FederationError;
 import org.waveprotocol.wave.federation.FederationErrors;
 import org.waveprotocol.wave.federation.Proto.ProtocolAppliedWaveletDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignature;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignedDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignerInfo;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.waveserver.FederationHostBridge;
 import org.waveprotocol.wave.waveserver.FederationRemoteBridge;
 import org.waveprotocol.wave.waveserver.SubmitResultListener;
 import org.waveprotocol.wave.waveserver.WaveletFederationListener;
 import org.waveprotocol.wave.waveserver.WaveletFederationListener.Factory;
 import org.waveprotocol.wave.waveserver.WaveletFederationProvider;
 
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * The main class that services the FederationHost, FederationRemote and ClientFrontend.
  *
  *
  */
 @Singleton
 public class WaveServerImpl implements WaveServer {
 
   private static final Log LOG = Log.get(WaveServerImpl.class);
 
   protected static final long HISTORY_REQUEST_LENGTH_LIMIT_BYTES = 1024 * 1024;
 
   // -------------------------------------------------------------------------------------------
   // PRIVATES INITIALIZED IN CONSTRUCTOR.
   // -------------------------------------------------------------------------------------------
 
   private final CertificateManager certificateManager;
   private final Factory federationHostFactory;
   private final RemoteWaveletContainer.Factory remoteWaveletContainerFactory;
   private final LocalWaveletContainer.Factory localWaveletContainerFactory;
   private final WaveletFederationProvider federationRemote;
   private WaveletListener clientListener = null;
 
   // -------------------------------------------------------------------------------------------
   // MAPS FOR WAVES AND FEDERATION HOSTS.
   // -------------------------------------------------------------------------------------------
 
   private final Map<WaveId, Map<WaveletId, WaveletContainer>> waveMap =
     new MapMaker().makeComputingMap(
         new Function<WaveId, Map<WaveletId, WaveletContainer>>() {
           @Override
           public Map<WaveletId, WaveletContainer> apply(WaveId from) {
             return Maps.newHashMap();
           }
         });
 
   /** List of federation hosts for which we have listeners */
   private final Map<String, WaveletFederationListener> federationHosts =
       // Add a new entry to the map on demand.
       new MapMaker().makeComputingMap(
         new Function<String, WaveletFederationListener>() {
           @Override
           public WaveletFederationListener apply(String domain) {
             return federationHostFactory.listenerForDomain(domain);
           }
         }
       );
 
   // -------------------------------------------------------------------------------------------
   // IMPLEMENTATION OF THE WAVELET-FEDERATION-LISTENER.FACTORY INTERFACE USED BY THE FED HOST.
   // -------------------------------------------------------------------------------------------
 
   /**
    * Listener for notifications coming from the Federation Remote. For now we accept updates
    * for wavelets on any domain.
    */
   public WaveletFederationListener listenerForDomain(final String domain) {
     return new WaveletFederationListener() {
 
       @Override
       public void waveletDeltaUpdate(final WaveletName waveletName,
           List<ByteString> rawAppliedDeltas, final WaveletUpdateCallback callback) {
         Preconditions.checkArgument(!rawAppliedDeltas.isEmpty());
         WaveletContainer wavelet = getWavelet(waveletName);
 
         if (wavelet != null && wavelet.getState() == State.CORRUPTED) {
           // TODO: throw away whole wavelet and start again
           LOG.info("Received update for corrupt wavelet");
           callback.onFailure(FederationErrors.badRequest("Corrupt wavelet"));
           return;
         }
 
         // Turn raw serialised ByteStrings in to a more useful representation
         List<ByteStringMessage<ProtocolAppliedWaveletDelta>> appliedDeltas = Lists.newArrayList();
         for (ByteString delta : rawAppliedDeltas) {
           try {
             appliedDeltas.add(ByteStringMessage.from(
                 ProtocolAppliedWaveletDelta.getDefaultInstance(), delta));
           } catch (InvalidProtocolBufferException e) {
             LOG.info("Invalid applied delta protobuf for incoming " + waveletName, e);
             safeMarkWaveletCorrupted(wavelet);
             callback.onFailure(FederationErrors.badRequest("Invalid applied delta protocol buffer"));
             return;
           }
         }
 
         // Update wavelet container with the applied deltas
         String error = null;
 
         try {
           final RemoteWaveletContainer remoteWavelet = getOrCreateRemoteWavelet(waveletName);
 
           // Update this remote wavelet with the immediately incoming delta, providing a callback
           // so that incoming historic deltas (as well as this delta) can be given to the
           // clientListener at a later point
           remoteWavelet.update(appliedDeltas, domain, federationRemote, certificateManager,
               new RemoteWaveletDeltaCallback() {
                 @Override
                 public void onSuccess(DeltaSequence result) {
                   if (clientListener != null) {
                     Map<String, BufferedDocOp> documentState =
                         getWavelet(waveletName).getWaveletData().getDocuments();
                     clientListener.waveletUpdate(waveletName, result, result.getEndVersion(),
                         documentState);
                   } else {
                     LOG.warning("Got valid deltaSequence for " + waveletName
                         + ", clientListener is null");
                   }
                 }
 
                 @Override
                 public void onFailure(String errorMessage) {
                   LOG.warning("Update failed: e" + errorMessage);
                   callback.onFailure(FederationErrors.badRequest(errorMessage));
                 }
               });
           // TODO: when we support federated groups, forward to federationHosts too.
         } catch(WaveletStateException e) {
           // HACK(jochen): TODO: fix the case of the missing history! ###
           LOG.severe("DANGER WILL ROBINSON, WAVELET HISTORY IS INCOMPLETE!!!", e);
           error = e.getMessage();
         } catch (HostingException e) {
           error = e.getMessage();
         } catch (WaveServerException e) {
           error = e.getMessage();
         } catch (IllegalArgumentException e) {
           error = "Bad wavelet name " + e.getMessage();
         }
         if (error == null) {
           callback.onSuccess();
         } else {
           LOG.warning("incoming waveletUpdate: bad update, " + error);
           callback.onFailure(FederationErrors.badRequest(error));
         }
       }
 
       @Override
       public void waveletCommitUpdate(WaveletName waveletName,
           ProtocolHashedVersion committedVersion, WaveletUpdateCallback callback) {
         Preconditions.checkNotNull(committedVersion);
         if (clientListener != null) {
           clientListener.waveletCommitted(waveletName, committedVersion);
         } else {
           LOG.warning("Client listener is null");
         }
         // Pretend we've committed it, there is no persistence
         LOG.fine("Responding with success to wavelet commit on " + waveletName);
         callback.onSuccess();
       }
     };
   }
 
   /**
    * Set wavelet as corrupted, if not null.
    *
    * @param wavelet to set as corrupted, if not null
    */
   private void safeMarkWaveletCorrupted(WaveletContainer wavelet) {
     if (wavelet != null) {
       wavelet.setState(State.CORRUPTED);
     }
   }
 
   // -------------------------------------------------------------------------------------------
   // METHODS IMPLEMENTING THE WAVELET-FEDERATION-PROVIDER INTERFACE USED BY THE FED REMOTE.
   // -------------------------------------------------------------------------------------------
 
   @Override
   public void setListener(WaveletListener listener) {
     clientListener = listener;
   }
 
   @Override
   public void submitRequest(WaveletName waveletName, ProtocolSignedDelta signedDelta,
       SubmitResultListener listener) {
     // Disallow creation of wavelets by remote users.
     try {
       ByteStringMessage<ProtocolWaveletDelta> delta = ByteStringMessage.from(
           ProtocolWaveletDelta.getDefaultInstance(), signedDelta.getDelta());
       if (delta.getMessage().getHashedVersion().getVersion() == 0) {
         LOG.warning("Remote user tried to submit delta at version 0 - disallowed. " + signedDelta);
         listener.onFailure(FederationErrors.badRequest("Remote users may not create wavelets."));
         return;
       }
     } catch (InvalidProtocolBufferException e) {
       listener.onFailure(FederationErrors.badRequest("Signed delta contains invalid delta"));
       return;
     }
 
     try {
       checkWaveletHosting(true, waveletName);
       certificateManager.verifyDelta(signedDelta);
       submitDelta(waveletName, signedDelta, listener);
     } catch (HostingException e) {
       LOG.warning("Remote tried to submit to local wavelet", e);
       listener.onFailure(FederationErrors.badRequest("Local wavelet update"));
     } catch (SignatureException e) {
       LOG.warning("Submit request: Delta failed verification. WaveletName: " + waveletName +
           " delta: " + signedDelta, e);
       listener.onFailure(FederationErrors.badRequest("Remote verification failed"));
     } catch (UnknownSignerException e) {
       LOG.warning("Submit request: unknown signer.  WaveletName: " + waveletName +
           "delta: " + signedDelta, e);
       listener.onFailure(FederationErrors.badRequest("Unknown signer"));
     }
   }
 
   @Override
   public void requestHistory(WaveletName waveletName, String domain,
       ProtocolHashedVersion startVersion, ProtocolHashedVersion endVersion,
       long lengthLimit, HistoryResponseListener listener) {
     WaveletContainer wc = getWavelet(waveletName);
     if (wc == null) {
       listener.onFailure(FederationErrors.badRequest("Wavlet " + waveletName + " does not exist."));
       LOG.info("Request history: " + domain + " requested non-existant wavelet: " +
           waveletName);
     } else {
       // TODO: once we support federated groups, expand support to request
       // remote wavelets too.
       if (!isLocalWavelet(waveletName)) {
         listener.onFailure(FederationErrors.badRequest("Wavelet " + waveletName
                                                        + " not hosted here."));
         LOG.info("Federation remote for domain: " + domain + " requested a remote wavelet: " +
             waveletName);
       } else {
         try {
           NavigableSet<ByteStringMessage<ProtocolAppliedWaveletDelta>> deltaHistory =
               wc.requestHistory(startVersion, endVersion);
           List<ByteString> deltaHistoryBytes = Lists.newArrayList();
           for (ByteStringMessage<ProtocolAppliedWaveletDelta> d : deltaHistory) {
             deltaHistoryBytes.add(d.getByteString());
           }
 
           // Now determine whether we received the entire requested wavelet history.
           LOG.info("Found deltaHistory between " + startVersion + " - " + endVersion
               + ", returning to requester domain " + domain + " -- " + deltaHistory);
           ProtocolHashedVersion hashedEndVersion =
               ProtocolHashedVersion.newBuilder().setHistoryHash(endVersion.getHistoryHash())
                   .setVersion(endVersion.getVersion()).build();
           listener.onSuccess(deltaHistoryBytes, hashedEndVersion, endVersion.getVersion());
           // TODO: ### check length limit ??
 //           else {
 //            ProtocolAppliedWaveletDelta lastDelta = deltaHistory.last();
 //            long lastVersion = lastDelta.getHashedVersionAppliedAt().getVersion() +
 //                lastDelta.getOperationsApplied();
 //            listener.onSuccess(deltaHistory, lcv.getVersion(),
 //                lastVersion);
 //          }
         } catch (WaveletStateException e) {
           LOG.severe("Error retrieving wavelet history: " + waveletName + " " + startVersion +
               " - " + endVersion);
           listener.onFailure(FederationErrors.badRequest(
               "Server error while retrieving wavelet history."));
         }
       }
     }
   }
 
   @Override
   public void getDeltaSignerInfo(ByteString signerId,
       WaveletName waveletName, ProtocolHashedVersion deltaEndVersion,
       DeltaSignerInfoResponseListener listener) {
     WaveletContainer wavelet = getWavelet(waveletName);
 
     if (wavelet == null) {
       LOG.info("getDeltaSignerInfo for nonexistent wavelet " + waveletName);
       listener.onFailure(FederationErrors.badRequest("Wavelet does not exist"));
     } else if (!(wavelet instanceof LocalWaveletContainer)) {
       LOG.info("getDeltaSignerInfo for remote wavelet " + waveletName);
       listener.onFailure(FederationErrors.badRequest("Wavelet is not locally hosted"));
     } else {
       LocalWaveletContainer localWavelet = (LocalWaveletContainer) wavelet;
       if (localWavelet.isDeltaSigner(deltaEndVersion, signerId)) {
         ProtocolSignerInfo signerInfo = certificateManager.retrieveSignerInfo(signerId);
         if (signerInfo == null) {
           // Oh no!  We are supposed to store it, and we already know they did sign this delta.
           LOG.severe("No stored signer info for valid getDeltaSignerInfo on " + waveletName);
           listener.onFailure(FederationErrors.badRequest("Unknown signer info"));
         } else {
           listener.onSuccess(signerInfo);
         }
       } else {
         LOG.info("getDeltaSignerInfo was not authrorised for wavelet " + waveletName
             + ", end version " + deltaEndVersion);
         listener.onFailure(FederationErrors.badRequest("Not authorised to get signer info"));
       }
     }
   }
 
   @Override
   public void postSignerInfo(String destinationDomain, ProtocolSignerInfo signerInfo,
       PostSignerInfoResponseListener listener) {
     try {
       certificateManager.storeSignerInfo(signerInfo);
     } catch (SignatureException e) {
       String error = "verification failure from domain " + signerInfo.getDomain();
       LOG.warning("incoming postSignerInfo: " + error, e);
       listener.onFailure(FederationErrors.badRequest(error));
       return;
     }
     listener.onSuccess();
   }
 
   // -------------------------------------------------------------------------------------------
   // METHODS IMPLEMENTING THE WAVELET-PROVIDER INTERFACE USED BY THE CLIENT FRONTEND.
   // -------------------------------------------------------------------------------------------
 
   @Override
   public NavigableSet<ProtocolWaveletDelta> requestHistory(WaveletName waveletName,
       ProtocolHashedVersion startVersion, ProtocolHashedVersion endVersion) {
     WaveletContainer wc = getWavelet(waveletName);
     if (wc == null) {
       LOG.info("Client request for history made for non-existant wavelet: " + waveletName);
       return null;
     } else {
       NavigableSet<ProtocolWaveletDelta> deltaHistory = null;
       try {
         deltaHistory = wc.requestTransformedHistory(startVersion, endVersion);
       } catch (AccessControlException e) {
         LOG.warning("Client requested history with incorrect hashedVersion: " + waveletName + " " +
             " startVersion: " + startVersion + " endVersion: " + endVersion);
       } catch (WaveletStateException e) {
         if (e.getState() == State.LOADING) {
           LOG.severe("Client Frontend requested history for a remote wavelet that is not yet" +
           "available - this should not happen as we have not sent an update for the wavelet.");
         } else {
           LOG.severe("Error retrieving wavelet history: " + waveletName + " " + startVersion +
               " - " + endVersion);
         }
       }
       return deltaHistory;
     }
   }
 
   @Override
   public void submitRequest(WaveletName waveletName, ProtocolWaveletDelta delta,
       SubmitResultListener listner) {
     // The serialised version of this delta happens now.  This should be the only place, ever!
     ProtocolSignedDelta signedDelta = certificateManager.signDelta(
         ByteStringMessage.fromMessage(delta));
     submitDelta(waveletName, signedDelta, listner);
   }
 
   // -------------------------------------------------------------------------------------------
   //  CONSTRUCTOR AND PRIVATE UTILITY METHODS.
   // -------------------------------------------------------------------------------------------
 
   /**
    * Constructor.
    *
    * @param certificateManager provider of certificates; it also determines which
    *        domains this wave server regards as local wavelets.
    * @param federationHostFactory factory that returns federation host instance listening
    *        on a given domain.
    * @param federationRemote
    * @param localWaveletContainerFactory
    * @param remoteWaveletContainerFactory
    */
   @Inject
   public WaveServerImpl(CertificateManager certificateManager,
       @FederationHostBridge WaveletFederationListener.Factory federationHostFactory,
       @FederationRemoteBridge WaveletFederationProvider federationRemote,
       LocalWaveletContainer.Factory localWaveletContainerFactory,
       RemoteWaveletContainer.Factory remoteWaveletContainerFactory) {
     this.certificateManager = certificateManager;
     this.federationHostFactory = federationHostFactory;
     this.federationRemote = federationRemote;
 
     this.localWaveletContainerFactory = localWaveletContainerFactory;
     this.remoteWaveletContainerFactory = remoteWaveletContainerFactory;
 
     LOG.info("Wave Server configured to host local domains: "
         + certificateManager.getLocalDomains().toString());
 
     // Preemptively add our own signer info to the certificate manager
     try {
       certificateManager.storeSignerInfo(
           certificateManager.getLocalSigner().getSignerInfo().toProtoBuf());
     } catch (SignatureException e) {
       LOG.severe("Failed to add our own signer info to the certificate store", e);
     }
   }
 
   private boolean isLocalWavelet(WaveletName waveletName) {
     LOG.info("### WS is local? " + waveletName + " = " + certificateManager.getLocalDomains().
         contains(waveletName.waveletId.getDomain()));
 
     return certificateManager.getLocalDomains().contains(waveletName.waveletId.getDomain());
   }
 
   private boolean checkWaveletHosting(boolean isLocal, WaveletName waveletName)
       throws HostingException {
     boolean l = isLocalWavelet(waveletName);
     if (l != isLocal) {
       throw new HostingException("Wavelet (" + waveletName + ") which is " +
           (l ? "local" : "remote") + " should have been " + (l ? "remote." : "local."));
     }
     return l == isLocal;
   }
 
   /**
    * Returns a container for a remote wavelet. If it doesn't exist, it will be created.
    * This method is only called in response to a Federation Remote doing an update
    * or commit on this wavelet.
    *
    * @param waveletName name of wavelet
    * @throws HostingException if the name refers to a local wavelet.
    * @return an existing or new instance.
    * @throws IllegalArgumentException on bad wavelet name
    */
   private RemoteWaveletContainer getOrCreateRemoteWavelet(WaveletName waveletName) throws
       HostingException {
     checkWaveletHosting(false, waveletName);
     synchronized (waveMap) {
       Map<WaveletId, WaveletContainer> wave = waveMap.get(waveletName.waveId);
       // This will blow up if we messed up and put a local wavelet in by mistake.
       RemoteWaveletContainer wc = (RemoteWaveletContainer) wave.get(waveletName.waveletId);
       if (wc == null) {
         wc = remoteWaveletContainerFactory.create(waveletName);
         wave.put(waveletName.waveletId, wc);
       }
       return wc;
     }
   }
 
   /**
    * Returns a container for a local wavelet. If it doesn't exist, it will be created.
    * Local wavelets are retrieved or created by a federation host or client on behalf
    * of a participant. The participant must have permission to access the wavelet if
    * it already exists.
    *
    * @param waveletName name of wavelet
    * @param participantId on who's behalf this wavelet is to be accessed.
    * @throws HostingException if the name refers to a local wavelet.
    * @throws AccessControlException if the participant may not access the wavelet.
    * @return an existing or new instance.
    * @throws WaveletStateException if the wavelet is in a bad state.
    */
   private LocalWaveletContainer getOrCreateLocalWavelet(WaveletName waveletName,
       ParticipantId participantId) throws
       HostingException, AccessControlException, WaveletStateException {
     checkWaveletHosting(true, waveletName);
     synchronized (waveMap) {
       Map<WaveletId, WaveletContainer> wave = waveMap.get(waveletName.waveId);
       // This will blow up if we messed up and put a remote wavelet in by mistake.
       LocalWaveletContainer wc = (LocalWaveletContainer) wave.get(waveletName.waveletId);
       if (wc == null) {
         wc = localWaveletContainerFactory.create(waveletName);
         // TODO: HACK(Jochen): do we need a namespace policer here ??? ###
         wave.put(waveletName.waveletId, wc);
       } else {
         if (!wc.checkAccessPermission(participantId)) {
           throw new AccessControlException(participantId + " is not a participant: " + waveletName);
         }
       }
       return wc;
     }
   }
 
   /**
    * Returns a generic wavelet container, when the caller doesn't need to validate whether
    * its a local or remote wavelet.
    *
    * @param waveletName name of wavelet.
    * @return an wavelet container or null if it doesn't exist.
    */
   private WaveletContainer getWavelet(WaveletName waveletName) {
     synchronized (waveMap) {
       Map<WaveletId, WaveletContainer> wave = waveMap.get(waveletName.waveId);
       return wave.get(waveletName.waveletId);
     }
   }
 
   /**
    * Callback interface for sending a list of certificates to a domain.
    */
   private interface PostSignerInfoCallback {
     public void done(int successCount);
   }
 
   /**
    * Submit the delta to local or remote wavelets, return results via listener.
    * Also broadcast updates to federationHosts and clientFrontend.
    *
    * TODO: for now a the WaveletFederationProvider will have
    *               made sure this is a local wavelet. Once we support
    *               federated groups, that test should be removed.
    */
   private void submitDelta(final WaveletName waveletName, final ProtocolSignedDelta delta,
       final SubmitResultListener resultListener) {
     ByteStringMessage<ProtocolWaveletDelta> waveletDelta;
     try {
       waveletDelta = ByteStringMessage.from(
           ProtocolWaveletDelta.getDefaultInstance(), delta.getDelta());
     } catch (InvalidProtocolBufferException e) {
       throw new IllegalArgumentException("Signed delta does not contain valid wavelet delta", e);
     }
 
     if (isLocalWavelet(waveletName)) {
       DeltaApplicationResult submitResult;
       final ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta;
       LocalWaveletContainer wc = null;
 
       try {
         LOG.info("## WS: Got submit: " + waveletName + " delta: " + waveletDelta);
 
         wc = getOrCreateLocalWavelet(waveletName,
             new ParticipantId(waveletDelta.getMessage().getAuthor()));
 
         /*
          * Synchronise on the wavelet container so that updates passed to clientListener and
          * Federation listeners are ordered correctly. The application of deltas can happen in any
          * order (due to OT).
          *
          * TODO(thorogood): This basically creates a second write lock (like the one held within
          * wc.submitRequest) and extends it out around the broadcast code. Ideally what should
          * happen is the update is pushed onto a queue which is handled in another thread.
          */
         synchronized (wc) {
           submitResult = wc.submitRequest(waveletName, delta);
           appliedDelta = submitResult.getAppliedDelta();
 
           // return result to caller.
           ProtocolHashedVersion resultingVersion =
               serialize(HashedVersion.getHashedVersionAfter(appliedDelta));
           LOG.info("## WS: Submit result: " + waveletName + " appliedDelta: " + appliedDelta);
           resultListener.onSuccess(appliedDelta.getMessage().getOperationsApplied(),
               resultingVersion, appliedDelta.getMessage().getApplicationTimestamp());
 
           // Send the results to the client frontend
           if (clientListener != null) {
             Map<String, BufferedDocOp> documentState =
                 getWavelet(waveletName).getWaveletData().getDocuments();
             LOG.info("Sending update to client listener: " + submitResult.getDelta());
             clientListener.waveletUpdate(waveletName, ImmutableList.of(submitResult.getDelta()),
                 submitResult.getHashedVersionAfterApplication(), documentState);
           }
 
           // Broadcast results to the remote servers, but make sure they all have our signatures
          for (final String hostDomain : getParticipantDomains(wc)) {
             final WaveletFederationListener host = federationHosts.get(hostDomain);
             host.waveletDeltaUpdate(waveletName, ImmutableList.of(appliedDelta.getByteString()),
                 new WaveletFederationListener.WaveletUpdateCallback() {
                   @Override
                   public void onSuccess() {
                   }
 
                   @Override
                   public void onFailure(FederationError error) {
                     LOG.warning("outgoing waveletDeltaUpdate failure: " + error);
                   }
                 });
 
             // TODO: if persistence is added, don't send commit notice
             host.waveletCommitUpdate(waveletName, resultingVersion,
                 new WaveletFederationListener.WaveletUpdateCallback() {
                   @Override
                   public void onSuccess() {
                   }
 
                   @Override
                   public void onFailure(FederationError error) {
                     LOG.warning("outgoing waveletCommitUpdate failure: " + error);
                   }
             });
           }
         }
       } catch (AccessControlException e) {
         resultListener.onFailure(FederationErrors.badRequest(e.getMessage()));
         return;
       } catch (OperationException e) {
         resultListener.onFailure(FederationErrors.badRequest(e.getMessage()));
         return;
       } catch (WaveletStateException e) {
         resultListener.onFailure(FederationErrors.badRequest(e.getMessage()));
         return;
       } catch (IllegalArgumentException e) {
         resultListener.onFailure(FederationErrors.badRequest(e.getMessage()));
         return;
       } catch (HostingException e) {
         throw new IllegalStateException("Should not get HostingException after " +
                                         "checking isLocalWavelet", e);
       } catch (InvalidProtocolBufferException e) {
         resultListener.onFailure(FederationErrors.badRequest(e.getMessage()));
         return;
       } catch (InvalidHashException e) {
         resultListener.onFailure(FederationErrors.badRequest(e.getMessage()));
         return;
       } catch (EmptyDeltaException e) {
         // This is okay, just succeed silently.  Use an empty timestamp since nothing was applied.
         resultListener.onSuccess(0, serialize(wc.getCurrentVersion()), 0);
       }
     } else {
       // For remote wavelets post required signatures to the authorative server then send delta
       postAllSignerInfo(delta.getSignatureList(), waveletName.waveletId.getDomain(),
           new PostSignerInfoCallback() {
             @Override public void done(int successCount) {
               LOG.info("Remote: successfully sent " + successCount + " of "
                   + delta.getSignatureCount() + " certs to " + waveletName.waveletId.getDomain());
               federationRemote.submitRequest(waveletName, delta, resultListener);
             }
           });
     }
   }
 
   /**
    * Post a list of certificates to a domain and run a callback when all are finished.  The
    * callback will run whether or not all posts succeed.
    *
    * @param sigs list of signatures to post signer info for
    * @param domain to post signature to
    * @param callback to run when all signatures have been posted, successfully or unsuccessfully
    */
   private void postAllSignerInfo(final List<ProtocolSignature> sigs, final String domain,
       final PostSignerInfoCallback callback) {
 
     // In the current implementation there should only be a single signer
     if (sigs.size() != 1) {
       LOG.warning(sigs.size() + " signatures to broadcast, expecting exactly 1");
     }
 
     final AtomicInteger resultCount = new AtomicInteger(sigs.size());
     final AtomicInteger successCount = new AtomicInteger(0);
 
     for (final ProtocolSignature sig : sigs) {
       final ProtocolSignerInfo psi = certificateManager.retrieveSignerInfo(sig.getSignerId());
 
       if (psi == null) {
         LOG.warning("Couldn't find signer info for " + sig);
         if (resultCount.decrementAndGet() == 0) {
           LOG.info("Finished signature broadcast with " + successCount.get()
               + " successful, running callback");
           callback.done(successCount.get());
         }
       } else {
         federationRemote.postSignerInfo(domain, psi, new PostSignerInfoResponseListener() {
           @Override
           public void onFailure(FederationError error) {
             LOG.warning("Failed to post " + sig + " to " + domain + ": " + error);
             countDown();
           }
 
           @Override
           public void onSuccess() {
             LOG.info("Successfully broadcasted " + sig + " to " + domain);
             successCount.incrementAndGet();
             countDown();
           }
 
           private void countDown() {
             if (resultCount.decrementAndGet() == 0) {
               LOG.info("Finished signature broadcast with " + successCount.get()
                   + " successful, running callback");
               callback.done(successCount.get());
             }
           }
         });
       }
     }
   }
 
   private Set<String> getParticipantDomains(LocalWaveletContainer lwc) {
     Set<String> hosts = Sets.newHashSet();
     Set<String> localDomains = certificateManager.getLocalDomains();
     for (ParticipantId p : lwc.getParticipants()) {
       String domain = p.getDomain();
       if (localDomains.contains(domain)) {
         // Ignore, don't re-federate to a local domain.
       } else {
         hosts.add(p.getDomain());
       }
     }
     return ImmutableSet.copyOf(hosts);
   }
 }
