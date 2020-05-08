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
 
 package org.waveprotocol.box.server.waveserver;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import org.jivesoftware.util.Base64;
 import org.waveprotocol.box.server.common.CoreWaveletOperationSerializer;
 import org.waveprotocol.box.server.common.DeltaSequence;
 import org.waveprotocol.box.server.util.Log;
 import org.waveprotocol.box.server.waveserver.CertificateManager.SignerInfoPrefetchResultListener;
 import org.waveprotocol.wave.crypto.SignatureException;
 import org.waveprotocol.wave.crypto.UnknownSignerException;
 import org.waveprotocol.wave.federation.FederationErrorProto.FederationError;
 import org.waveprotocol.wave.federation.Proto.ProtocolAppliedWaveletDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignature;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignedDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignerInfo;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.operation.core.CoreWaveletDelta;
 import org.waveprotocol.wave.model.version.HashedVersion;
 import org.waveprotocol.wave.waveserver.federation.WaveletFederationProvider;
 import org.waveprotocol.wave.waveserver.federation.WaveletFederationProvider.HistoryResponseListener;
 
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Remote wavelets differ from local ones in that deltas are not submitted for OT,
  * rather they are updated when a remote wave service provider has applied and sent
  * a delta.
  *
  *
  */
 class RemoteWaveletContainerImpl extends WaveletContainerImpl implements
     RemoteWaveletContainer {
   private static final Log LOG = Log.get(RemoteWaveletContainerImpl.class);
 
   /**
    * Stores all pending deltas for this wavelet, whos insertions would cause
    * discontinuous blocks of deltas. This must only be accessed under writeLock.
    */
   private final NavigableMap<HashedVersion, ByteStringMessage<ProtocolAppliedWaveletDelta>>
       pendingDeltas = Maps.newTreeMap();
 
   /**
    * Create a new RemoteWaveletContainerImpl. Just pass through to the parent
    * constructor.
    */
   public RemoteWaveletContainerImpl(WaveletName waveletName) {
     super(waveletName);
     state = State.LOADING;
   }
 
   /** Convenience method to assert state. */
   protected void assertStateOkOrLoading() throws WaveletStateException {
     if (state != State.LOADING) {
       assertStateOk();
     }
   }
 
   @Override
   public boolean committed(ProtocolHashedVersion hashedVersion) throws WaveletStateException {
     acquireWriteLock();
     try {
       assertStateOkOrLoading();
       lastCommittedVersion = hashedVersion;
 
       // Pass to clients iff our known version is here or greater.
       return currentVersion.getVersion() >= hashedVersion.getVersion();
     } finally {
       releaseWriteLock();
     }
   }
 
   @Override
   public void update(final List<ByteStringMessage<ProtocolAppliedWaveletDelta>> appliedDeltas,
       final String domain, final WaveletFederationProvider federationProvider,
       final CertificateManager certificateManager, final RemoteWaveletDeltaCallback deltaCallback)
       throws WaveServerException {
     LOG.info("Got update: " + appliedDeltas);
 
     // Fetch any signer info that we don't already have
     final AtomicInteger numSignerInfoPrefetched = new AtomicInteger(1); // extra 1 for sentinel
     SignerInfoPrefetchResultListener prefetchListener = new SignerInfoPrefetchResultListener() {
       @Override
       public void onFailure(FederationError error) {
         LOG.warning("Signer info prefetch failed: " + error);
         countDown();
       }
 
       @Override
       public void onSuccess(ProtocolSignerInfo signerInfo) {
         LOG.info("Signer info prefetch success for " + signerInfo.getDomain());
         countDown();
       }
 
       private void countDown() {
         if (numSignerInfoPrefetched.decrementAndGet() == 0) {
           try {
             internalUpdate(appliedDeltas, domain, federationProvider, certificateManager,
                 deltaCallback);
           } catch (WaveServerException e) {
             LOG.warning("Wave server exception when running update", e);
             deltaCallback.onFailure(e.getMessage());
           }
         }
       }
     };
 
     for (ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta : appliedDeltas) {
       ProtocolSignedDelta toVerify = appliedDelta.getMessage().getSignedOriginalDelta();
       ProtocolHashedVersion deltaEndVersion;
       try {
         deltaEndVersion = AppliedDeltaUtil.calculateHashedVersionAfter(appliedDelta);
       } catch (InvalidProtocolBufferException e) {
         LOG.warning("Skipping illformed applied delta " + appliedDelta, e);
         continue;
       }
       for (ProtocolSignature sig : toVerify.getSignatureList()) {
         if (certificateManager.retrieveSignerInfo(sig.getSignerId()) == null) {
           LOG.info("Fetching signer info " + Base64.encodeBytes(sig.getSignerId().toByteArray()));
           numSignerInfoPrefetched.incrementAndGet();
           certificateManager.prefetchDeltaSignerInfo(federationProvider, sig.getSignerId(),
               waveletName, deltaEndVersion, prefetchListener);
         }
       }
     }
 
     // If we didn't fetch any signer info, run internalUpdate immediately
     if (numSignerInfoPrefetched.decrementAndGet() == 0) {
       internalUpdate(appliedDeltas, domain, federationProvider, certificateManager, deltaCallback);
     }
   }
 
   /**
    * Called by {@link #update} when all signer info is guaranteed to be available.
    *
    * @param appliedDeltas
    * @param domain
    * @param federationProvider
    * @param certificateManager
    * @param deltaCallback
    * @throws WaveServerException
    */
   private void internalUpdate(List<ByteStringMessage<ProtocolAppliedWaveletDelta>> appliedDeltas,
       final String domain, final WaveletFederationProvider federationProvider,
       final CertificateManager certificateManager, final RemoteWaveletDeltaCallback deltaCallback)
       throws WaveServerException {
     LOG.info("Passed signer info check, now applying all " + appliedDeltas.size() + " deltas");
     acquireWriteLock();
     try {
       assertStateOkOrLoading();
       HashedVersion expectedVersion = currentVersion;
       boolean haveRequestedHistory = false;
 
       // Verify signatures of all deltas
       for (ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta : appliedDeltas) {
         try {
           certificateManager.verifyDelta(appliedDelta.getMessage().getSignedOriginalDelta());
         } catch (SignatureException e) {
           LOG.warning("Verification failure for " + domain + " incoming " + waveletName, e);
           throw new WaveServerException("Verification failure", e);
         } catch (UnknownSignerException e) {
           LOG.severe("Unknown signer for " + domain + " incoming " + waveletName +
               ", this is BAD! We were supposed to have prefetched it!", e);
           throw new WaveServerException("Unknown signer", e);
         }
       }
 
       // Insert all available deltas into pendingDeltas.
       for (ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta : appliedDeltas) {
         LOG.info("Delta incoming: " + appliedDelta);
 
         // Log any illformed signed original deltas. TODO: Check if this can be removed.
         try {
           ProtocolWaveletDelta actualDelta = ProtocolWaveletDelta.parseFrom(
               appliedDelta.getMessage().getSignedOriginalDelta().getDelta());
           LOG.info("actual delta: " + actualDelta);
         } catch (InvalidProtocolBufferException e) {
           e.printStackTrace();
         }
 
         ProtocolHashedVersion appliedAt;
         try {
           appliedAt = AppliedDeltaUtil.getHashedVersionAppliedAt(appliedDelta);
         } catch (InvalidProtocolBufferException e) {
           setState(State.CORRUPTED);
           throw new WaveServerException(
               "Authoritative server sent delta with badly formed original wavelet delta", e);
         }
 
         pendingDeltas.put(CoreWaveletOperationSerializer.deserialize(appliedAt), appliedDelta);
       }
 
       // Traverse pendingDeltas while we have any to process.
       List<CoreWaveletDelta> result = Lists.newLinkedList();
       while (pendingDeltas.size() > 0) {
         Map.Entry<HashedVersion, ByteStringMessage<ProtocolAppliedWaveletDelta>> first =
             pendingDeltas.firstEntry();
         HashedVersion appliedAt = first.getKey();
         ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta = first.getValue();
 
         // If we don't have the right version it implies there is a history we need, so set up a
         // callback to request it and fall out of this update
         if (appliedAt.getVersion() > expectedVersion.getVersion()) {
           LOG.info("Missing history from " + expectedVersion.getVersion() + "-"
               + appliedAt.getVersion() + ", requesting from upstream for " + waveletName);
 
           if (federationProvider != null) {
             // TODO: only one request history should be pending at any one time?
             // We should derive a new one whenever the active one is finished,
             // based on the current state of pendingDeltas.
             federationProvider.requestHistory(waveletName, domain,
                 CoreWaveletOperationSerializer.serialize(expectedVersion),
                 CoreWaveletOperationSerializer.serialize(appliedAt),
                 -1,
                 new HistoryResponseListener() {
                     @Override
                     public void onFailure(FederationError error) {
                       LOG.severe("Callback failure: " + error);
                     }
 
                     @Override
                     public void onSuccess(List<ByteString> deltaList,
                         ProtocolHashedVersion lastCommittedVersion, long versionTruncatedAt) {
                       LOG.info("Got response callback: " + waveletName + ", lcv "
                           + lastCommittedVersion + " deltaList length = " + deltaList.size());
 
                       // Turn the ByteStrings in to a useful representation
                       List<ByteStringMessage<ProtocolAppliedWaveletDelta>> appliedDeltaList =
                           Lists.newArrayList();
                       for (ByteString appliedDelta : deltaList) {
                         try {
                           LOG.info("Delta incoming from history: " + appliedDelta);
                           appliedDeltaList.add(
                               ByteStringMessage.parseProtocolAppliedWaveletDelta(appliedDelta));
                         } catch (InvalidProtocolBufferException e) {
                           LOG.warning("Invalid protocol buffer when requesting history!");
                           state = State.CORRUPTED;
                           break;
                         }
                       }
 
                       // Try updating again with the new history
                       try {
                         update(appliedDeltaList, domain, federationProvider, certificateManager,
                             deltaCallback);
                       } catch (WaveServerException e) {
                         // TODO: deal with this
                         LOG.severe("Exception when updating from history", e);
                       }
                     }
                 });
             haveRequestedHistory = true;
           } else {
             LOG.severe("History request resulted in non-contiguous deltas!");
           }
           break;
         }
 
         // This delta is at the correct (current) version - apply it.
         if (appliedAt.getVersion() == expectedVersion.getVersion()) {
           // Confirm that the applied at hash matches the expected hash.
           if (!appliedAt.equals(expectedVersion)) {
             state = State.CORRUPTED;
             throw new WaveServerException("Incoming delta applied at version "
                 + appliedAt.getVersion() + " is not applied to the correct hash");
           }
 
           LOG.info("Applying delta for version " + appliedAt.getVersion());
           try {
             DeltaApplicationResult applicationResult = transformAndApplyRemoteDelta(appliedDelta);
             long opsApplied = applicationResult.getHashedVersionAfterApplication().getVersion()
                     - expectedVersion.getVersion();
             if (opsApplied != appliedDelta.getMessage().getOperationsApplied()) {
               throw new OperationException("Operations applied here do not match the authoritative"
                   + " server claim (got " + opsApplied + ", expected "
                   + appliedDelta.getMessage().getOperationsApplied() + ".");
             }
             // Add transformed result to return list.
             result.add(applicationResult.getDelta());
             LOG.fine("Applied delta: " + appliedDelta);
           } catch (OperationException e) {
             state = State.CORRUPTED;
             throw new WaveServerException("Couldn't apply authoritative delta", e);
           } catch (InvalidProtocolBufferException e) {
             state = State.CORRUPTED;
             throw new WaveServerException("Couldn't apply authoritative delta", e);
           } catch (InvalidHashException e) {
             state = State.CORRUPTED;
             throw new WaveServerException("Couldn't apply authoritative delta", e);
           }
 
           // This is the version 0 case - now we have a valid wavelet!
           if (state == State.LOADING) {
             state = State.OK;
           }
 
           // TODO: does waveletData update?
           expectedVersion = currentVersion;
         } else {
           LOG.warning("Got delta from the past: " + appliedDelta);
         }
 
        pendingDeltas.remove(appliedAt);
       }
 
       if (!haveRequestedHistory) {
         DeltaSequence deltaSequence = new DeltaSequence(result, expectedVersion);
         if (LOG.isFineLoggable()) {
           LOG.fine("Returning contiguous block: " + deltaSequence);
         }
         deltaCallback.onSuccess(deltaSequence);
       } else if (!result.isEmpty()) {
         LOG.severe("History requested but non-empty result, non-contiguous deltas?");
       } else {
         LOG.info("History requested, ignoring callback");
       }
     } finally {
       releaseWriteLock();
     }
   }
 
   /**
    * Apply a serialised applied delta to a remote wavelet. This assumes the
    * caller has validated that the delta is at the correct version and can be
    * applied to the wavelet. Must be called with writelock held.
    *
    * @param appliedDelta that is to be applied to the wavelet in its serialised form
    * @return transformed operations are applied to this delta
    * @throws AccessControlException if the supplied Delta's historyHash does not
    *         match the canonical history.
    * @throws WaveServerException if the delta transforms away.
    */
   private DeltaApplicationResult transformAndApplyRemoteDelta(
       ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta) throws OperationException,
       AccessControlException, InvalidHashException, InvalidProtocolBufferException,
       WaveServerException {
 
     // The serialised hashed version should actually match the currentVersion at this point, since
     // the caller of transformAndApply delta will have made sure the applied deltas are ordered
     HashedVersion hashedVersion =
         CoreWaveletOperationSerializer.deserialize(
             AppliedDeltaUtil.getHashedVersionAppliedAt(appliedDelta));
     if (!hashedVersion.equals(currentVersion)) {
       throw new IllegalStateException("Applied delta does not apply at current version");
     }
 
     // Extract the serialised wavelet delta
     ByteStringMessage<ProtocolWaveletDelta> protocolDelta =
         ByteStringMessage.parseProtocolWaveletDelta(
             appliedDelta.getMessage().getSignedOriginalDelta().getDelta());
     CoreWaveletDelta delta = CoreWaveletOperationSerializer.deserialize(protocolDelta.getMessage());
 
     // Transform operations against earlier deltas, if necessary
     CoreWaveletDelta transformed = maybeTransformSubmittedDelta(delta);
     if (transformed.getTargetVersion().equals(delta.getTargetVersion())) {
       // No transformation took place.
       // As a sanity check, the hash from the applied delta should NOT be set (an optimisation, but
       // part of the protocol).
       if (appliedDelta.getMessage().hasHashedVersionAppliedAt()) {
         LOG.warning("Hashes are the same but applied delta has hashed_version_applied_at");
         // TODO: re-enable this exception for version 0.3 of the spec
 //        throw new InvalidHashException("Applied delta and its contained delta have same hash");
       }
     }
 
     if (transformed.getOperations().isEmpty()) {
       // The host shouldn't be forwarding empty deltas!
       state = State.CORRUPTED;
       throw new WaveServerException("Couldn't apply authoritative delta, " +
           "it transformed away at version " + transformed.getTargetVersion().getVersion());
     }
 
     // Apply operations.  These shouldn't fail since they're the authoritative versions, so if they
     // do then the wavelet is corrupted (and the caller of this method will sort it out).
     applyWaveletOperations(transformed, appliedDelta.getMessage().getApplicationTimestamp());
 
     return commitAppliedDelta(appliedDelta, transformed);
   }
 }
