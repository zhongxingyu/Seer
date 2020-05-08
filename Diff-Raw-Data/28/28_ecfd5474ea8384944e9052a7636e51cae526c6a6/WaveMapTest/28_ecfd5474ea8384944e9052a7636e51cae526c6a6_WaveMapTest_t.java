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
 
 package org.waveprotocol.box.server.waveserver;
 
 import com.google.common.util.concurrent.Futures;
 
 import junit.framework.TestCase;
 
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.waveprotocol.box.server.common.CoreWaveletOperationSerializer;
 import org.waveprotocol.box.server.persistence.memory.MemoryDeltaStore;
 import org.waveprotocol.box.server.util.URLEncoderDecoderBasedPercentEncoderDecoder;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignedDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.id.IdURIEncoderDecoder;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.wave.AddParticipant;
 import org.waveprotocol.wave.model.operation.wave.WaveletDelta;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperationContext;
 import org.waveprotocol.wave.model.version.HashedVersion;
 import org.waveprotocol.wave.model.version.HashedVersionZeroFactoryImpl;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.WaveViewData;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 /**
  * @author josephg@gmail.com (Joseph Gentle)
  * @author soren@google.com (Soren Lassen)
  */
 public class WaveMapTest extends TestCase {
   private static final String DOMAIN = "example.com";
   private static final WaveId WAVE_ID = WaveId.of(DOMAIN, "abc123");
   private static final WaveletId WAVELET_ID = WaveletId.of(DOMAIN, "conv+root");
   private static final WaveletName WAVELET_NAME = WaveletName.of(WAVE_ID, WAVELET_ID);
 
   private static final ParticipantId USER1 = ParticipantId.ofUnsafe("user1@" + DOMAIN);
   private static final ParticipantId USER2 = ParticipantId.ofUnsafe("user2@" + DOMAIN);
 
   private static final WaveletOperationContext CONTEXT =
     new WaveletOperationContext(USER1, 1234567890, 1);
 
   private static WaveletOperation addParticipantToWavelet(ParticipantId user) {
     return new AddParticipant(CONTEXT, user);
   }
 
   @Mock private WaveletNotificationDispatcher notifiee;
   @Mock private RemoteWaveletContainer.Factory remoteWaveletContainerFactory;
 
  private DeltaStore waveletStore;
   private WaveMap waveMap;
   private HashedVersionZeroFactoryImpl versionZeroFactory;
 
   @Override
   protected void setUp() throws Exception {
     MockitoAnnotations.initMocks(this);
 
     LocalWaveletContainer.Factory localWaveletContainerFactory =
         new LocalWaveletContainer.Factory() {
           @Override
           public LocalWaveletContainer create(WaveletNotificationSubscriber notifiee,
               WaveletName waveletName) {
             WaveletState waveletState = new MemoryWaveletState(waveletName);
             return new LocalWaveletContainerImpl(waveletName, notifiee,
                 Futures.immediateFuture(waveletState));
           }
         };
 
    waveletStore = new MemoryDeltaStore();
     waveMap = new WaveMap(
         waveletStore, notifiee, localWaveletContainerFactory, remoteWaveletContainerFactory);
 
     IdURIEncoderDecoder uriCodec =
         new IdURIEncoderDecoder(new URLEncoderDecoderBasedPercentEncoderDecoder());
     versionZeroFactory = new HashedVersionZeroFactoryImpl(uriCodec);
   }
 
   public void testSearchEmptyInboxReturnsNothing() {
     Collection<WaveViewData> results = waveMap.search(USER1, "in:inbox", 0, 20);
 
     assertEquals(0, results.size());
   }
 
   public void testSearchInboxReturnsWaveWithExplicitParticipant() throws Exception {
     submitDeltaToNewWavelet(WAVELET_NAME, USER1, addParticipantToWavelet(USER2));
 
     Collection<WaveViewData> results = waveMap.search(USER2, "in:inbox", 0, 20);
 
     assertEquals(1, results.size());
     assertEquals(WAVELET_NAME.waveId, results.iterator().next().getWaveId());
   }
 
   public void testSearchInboxDoesNotReturnWaveWithoutUser() throws Exception {
     submitDeltaToNewWavelet(WAVELET_NAME, USER1, addParticipantToWavelet(USER1));
 
     Collection<WaveViewData> results = waveMap.search(USER2, "in:inbox", 0, 20);
     assertEquals(0, results.size());
   }
 
   public void testSearchLimitEnforced() throws Exception {
     for (int i = 0; i < 10; i++) {
       WaveletName name = WaveletName.of(WaveId.of(DOMAIN, "w" + i), WAVELET_ID);
       submitDeltaToNewWavelet(name, USER1, addParticipantToWavelet(USER1));
     }
 
     Collection<WaveViewData> results = waveMap.search(USER1, "in:inbox", 0, 5);
 
     assertEquals(5, results.size());
   }
 
   public void testSearchIndexWorks() throws Exception {
     // For this test, we'll create 10 waves with wave ids "0", "1", ... "9" and then run 10
     // searches using offsets 0..9. The waves we get back can be in any order, but we must get
     // all 10 of the waves back exactly once each from the search query.
 
     for (int i = 0; i < 10; i++) {
       WaveletName name = WaveletName.of(WaveId.of(DOMAIN, String.valueOf(i)), WAVELET_ID);
       submitDeltaToNewWavelet(name, USER1, addParticipantToWavelet(USER1));
     }
 
     // The number of times we see each wave when we search
     int[] saw_wave = new int[10];
 
     for (int i = 0; i < 10; i++) {
       Collection<WaveViewData> results = waveMap.search(USER1, "in:inbox", i, 1);
       assertEquals(1, results.size());
       int index = Integer.parseInt(results.iterator().next().getWaveId().getId());
       saw_wave[index]++;
     }
 
     for (int i = 0; i < 10; i++) {
       // Each wave should appear exactly once in the results
       assertEquals(1, saw_wave[i]);
     }
   }
 
   // *** Helpers
 
   private void submitDeltaToNewWavelet(WaveletName name, ParticipantId user,
       WaveletOperation... ops) throws Exception {
     HashedVersion version = versionZeroFactory.createVersionZero(name);
     WaveletDelta delta = new WaveletDelta(user, version, Arrays.asList(ops));
     ProtocolWaveletDelta protoDelta = CoreWaveletOperationSerializer.serialize(delta);
 
     // Submitting the request will require the certificate manager to sign the delta. We'll just
     // leave it unsigned.
     ProtocolSignedDelta signedProtoDelta =
         ProtocolSignedDelta.newBuilder().setDelta(protoDelta.toByteString()).build();
 
     LocalWaveletContainer wavelet = waveMap.getOrCreateLocalWavelet(name);
     wavelet.submitRequest(name, signedProtoDelta);
   }
 }
