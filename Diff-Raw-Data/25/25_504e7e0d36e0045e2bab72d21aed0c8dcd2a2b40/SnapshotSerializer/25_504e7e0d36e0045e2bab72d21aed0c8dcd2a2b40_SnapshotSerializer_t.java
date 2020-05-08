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
 
 package org.waveprotocol.wave.examples.client.webclient.common;
 
 import org.waveprotocol.wave.examples.fedone.waveserver.DocumentSnapshot;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveViewSnapshot;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveletSnapshot;
 import org.waveprotocol.wave.model.document.operation.DocInitialization;
 import org.waveprotocol.wave.model.document.operation.DocOp;
 import org.waveprotocol.wave.model.document.operation.impl.DocOpUtil;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.OperationException;
import org.waveprotocol.wave.model.testing.BasicFactories;
 import org.waveprotocol.wave.model.util.CollectionUtils;
 import org.waveprotocol.wave.model.wave.InvalidParticipantAddress;
 import org.waveprotocol.wave.model.wave.ParticipantId;
import org.waveprotocol.wave.model.wave.data.DocumentFactory;
 import org.waveprotocol.wave.model.wave.data.ObservableWaveletData;
 import org.waveprotocol.wave.model.wave.data.ReadableBlipData;
 import org.waveprotocol.wave.model.wave.data.ReadableWaveletData;
 import org.waveprotocol.wave.model.wave.data.WaveViewData;
 import org.waveprotocol.wave.model.wave.data.WaveletData;
import org.waveprotocol.wave.model.wave.data.impl.EmptyWaveletSnapshot;
 import org.waveprotocol.wave.model.wave.data.impl.WaveViewDataImpl;
import org.waveprotocol.wave.model.wave.data.impl.WaveletDataImpl;
 
 import java.util.Collection;
 
 
 /**
  * Utility class for serialising/deserialising model objects (and their
  * components) to/from their protocol buffer representations.
  *
  * NOTE: This class is mirrored in the server. Any changes here should also be
  * made in
  * {@link org.waveprotocol.wave.examples.fedone.common.SnapshotSerializer}
  *
  * @author Joseph Gentle (josephg@gmail.com)
  */
 public class SnapshotSerializer {
  private static final DocumentFactory<?> DOCUMENT_FACTORY = BasicFactories.muteDocumentFactory();
  
   private SnapshotSerializer() {
   }
 
   /**
    * Serializes a snapshot for a wavelet.
    *
    * @param wavelet wavelet to snapshot
    * @param hashedVersion hashed version of the wavelet
    * @return a wavelet snapshot that contains all the information in the
    *         original wavelet.
    */
   public static WaveletSnapshot serializeWavelet(ReadableWaveletData wavelet,
       HashedVersion hashedVersion) {
     WaveletSnapshot.Builder builder = WaveletSnapshot.newBuilder();
 
     builder.setWaveletId(wavelet.getWaveletId().serialise());
     for (ParticipantId participant : wavelet.getParticipants()) {
       builder.addParticipantId(participant.toString());
     }
     for (String id : wavelet.getDocumentIds()) {
       ReadableBlipData data = wavelet.getDocument(id);
       builder.addDocument(serializeDocument(data));
     }
 
     builder.setVersion(CoreWaveletOperationSerializer.serialize(hashedVersion));
     builder.setLastModifiedTime(wavelet.getLastModifiedTime());
     builder.setCreator(wavelet.getCreator().getAddress());
     builder.setCreationTime(wavelet.getCreationTime());
 
     return builder.build();
   }
  
  // TODO(josephg): This is copied from WaveletDataUtil, which is not included
  // in the client. Include a similar class in the client, and move this
  // function out of here.
  private static ObservableWaveletData createEmptyWavelet(
      WaveletName waveletName, ParticipantId author, long creationTimeStamp) {
    return WaveletDataImpl.Factory.create(DOCUMENT_FACTORY).create(new EmptyWaveletSnapshot(
        waveletName.waveId, waveletName.waveletId, author, creationTimeStamp));
  }
 
   /**
    * Deserializes the snapshot contained in the {@link WaveletSnapshot}
    * into a {@link WaveletData}.
    *
    * @param snapshot the {@link WaveletSnapshot} to deserialize.
    * @throws OperationException if the ops in the snapshot can not be applied.
    * @throws InvalidParticipantAddress
    */
   public static ObservableWaveletData deserializeWavelet(WaveletSnapshot snapshot, WaveId waveId)
       throws OperationException, InvalidParticipantAddress {
     WaveletName name = WaveletName.of(waveId, WaveletId.deserialise(snapshot.getWaveletId()));
    ObservableWaveletData wavelet = createEmptyWavelet(name,
         ParticipantId.of(snapshot.getCreator()), (long) snapshot.getCreationTime());
 
     for (String participant : snapshot.getParticipantIdList()) {
       wavelet.addParticipant(ParticipantId.of(participant));
     }
 
     for (DocumentSnapshot document : snapshot.getDocumentList()) {
       addDocumentSnapshotToWavelet(document, wavelet);
     }
 
     wavelet.setVersion((long) snapshot.getVersion().getVersion());
     wavelet.setLastModifiedTime((long) snapshot.getLastModifiedTime());
     // The creator and creation time are set when the empty wavelet template is
     // created above.
 
     return wavelet;
   }
 
   /**
    * Serializes a document to a document snapshot.
    * 
    * @param document The document to serialize
    * @return A snapshot of the given document
    */
   public static DocumentSnapshot serializeDocument(ReadableBlipData document) {
     DocumentSnapshot.Builder builder = DocumentSnapshot.newBuilder();
 
     builder.setDocumentId(document.getId());
     builder.setDocumentOperation(CoreWaveletOperationSerializer.serialize(
         document.getContent().asOperation()));
 
     builder.setAuthor(document.getAuthor().getAddress());
     for (ParticipantId participant : document.getContributors()) {
       builder.addContributor(participant.getAddress());
     }
     builder.setLastModifiedVersion(document.getLastModifiedVersion());
     builder.setLastModifiedTime(document.getLastModifiedTime());
 
     return builder.build();
   }
 
   private static void addDocumentSnapshotToWavelet(
       DocumentSnapshot snapshot, WaveletData container) throws InvalidParticipantAddress {
     DocOp op = CoreWaveletOperationSerializer.deserialize(snapshot.getDocumentOperation());
     DocInitialization docInit = DocOpUtil.asInitialization(op);
 
     Collection<ParticipantId> contributors = CollectionUtils.newArrayList();
     for (String p : snapshot.getContributorList()) {
       contributors.add(ParticipantId.of(p));
     }
     container.createBlip(
         snapshot.getDocumentId(),
         ParticipantId.of(snapshot.getAuthor()),
         contributors,
         docInit,
         (long) snapshot.getLastModifiedTime(),
         (long) snapshot.getLastModifiedVersion());
   }
   
   /**
    * Deserialize a wave view snapshot into a WaveViewData object
    * 
    * @param snapshot the snapshot to deserialize
    * @return the deserialized snapshot
    * @throws OperationException
    * @throws InvalidParticipantAddress
    */
   public static WaveViewData deserializeWave(WaveViewSnapshot snapshot)
       throws OperationException, InvalidParticipantAddress {
     WaveId waveId = WaveId.deserialise(snapshot.getWaveId());
     Collection<ObservableWaveletData> wavelets = CollectionUtils.newArrayList();
     for (WaveletSnapshot s : snapshot.getWaveletList()) {
       wavelets.add(deserializeWavelet(s, waveId));
     }
     
     return new WaveViewDataImpl(waveId, wavelets);
   }
 }
