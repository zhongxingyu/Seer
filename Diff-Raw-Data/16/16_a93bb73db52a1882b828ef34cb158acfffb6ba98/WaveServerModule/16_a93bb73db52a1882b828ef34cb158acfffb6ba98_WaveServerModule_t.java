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
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 
 import org.waveprotocol.box.server.CoreSettings;
 import org.waveprotocol.box.server.common.HashedVersionFactoryImpl;
 import org.waveprotocol.box.server.frontend.ClientFrontend;
 import org.waveprotocol.box.server.frontend.ClientFrontendImpl;
 import org.waveprotocol.box.server.frontend.WaveClientRpcImpl;
 import org.waveprotocol.box.server.util.URLEncoderDecoderBasedPercentEncoderDecoder;
 import org.waveprotocol.box.server.waveserver.WaveClientRpc.ProtocolWaveClientRpc;
 import org.waveprotocol.wave.crypto.CachedCertPathValidator;
 import org.waveprotocol.wave.crypto.CertPathStore;
 import org.waveprotocol.wave.crypto.DefaultCacheImpl;
 import org.waveprotocol.wave.crypto.DefaultTimeSource;
 import org.waveprotocol.wave.crypto.DefaultTrustRootsProvider;
 import org.waveprotocol.wave.crypto.DisabledCertPathValidator;
 import org.waveprotocol.wave.crypto.TimeSource;
 import org.waveprotocol.wave.crypto.TrustRootsProvider;
 import org.waveprotocol.wave.crypto.VerifiedCertChainCache;
 import org.waveprotocol.wave.crypto.WaveCertPathValidator;
 import org.waveprotocol.wave.crypto.WaveSignatureVerifier;
 import org.waveprotocol.wave.model.id.IdURIEncoderDecoder;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.version.HashedVersionFactory;
 
 import java.io.IOException;
 
 /**
  * Guice Module for the prototype Server.
  *
  */
 public class WaveServerModule extends AbstractModule {
   private final boolean enableFederation;
 
   public WaveServerModule(boolean enableFederation) {
     this.enableFederation = enableFederation;
   }
 
   private static final IdURIEncoderDecoder URI_CODEC =
       new IdURIEncoderDecoder(new URLEncoderDecoderBasedPercentEncoderDecoder());
   private static final HashedVersionFactory HASH_FACTORY = new HashedVersionFactoryImpl(URI_CODEC);
 
   @Override
   protected void configure() {
     bind(TimeSource.class).to(DefaultTimeSource.class).in(Singleton.class);
 
     if (enableFederation) {
       bind(SignatureHandler.class)
       .toProvider(SigningSignatureHandler.SigningSignatureHandlerProvider.class);
     } else {
       bind(SignatureHandler.class)
       .toProvider(NonSigningSignatureHandler.NonSigningSignatureHandlerProvider.class);
     }
 
     try {
       bind(WaveSignatureVerifier.class).toConstructor(WaveSignatureVerifier.class.getConstructor(
           WaveCertPathValidator.class, CertPathStore.class));
       bind(VerifiedCertChainCache.class).to(DefaultCacheImpl.class).in(Singleton.class);
       bind(DefaultCacheImpl.class).toConstructor(
           DefaultCacheImpl.class.getConstructor(TimeSource.class));
     } catch (NoSuchMethodException e) {
       throw new IllegalStateException(e);
     }
 
     bind(TrustRootsProvider.class).to(DefaultTrustRootsProvider.class).in(Singleton.class);
     bind(CertificateManager.class).to(CertificateManagerImpl.class).in(Singleton.class);
     bind(WaveletProvider.class).to(WaveServerImpl.class).in(Singleton.class);
     bind(SearchProvider.class).to(WaveServerImpl.class).in(Singleton.class);
     bind(WaveBus.class).to(WaveServerImpl.class).in(Singleton.class);
     bind(HashedVersionFactory.class).toInstance(HASH_FACTORY);
     bind(ClientFrontend.class).to(ClientFrontendImpl.class).in(Singleton.class);
     bind(ProtocolWaveClientRpc.Interface.class).to(WaveClientRpcImpl.class).in(Singleton.class);
    bind(WaveletStore.class).to(DeltaStoreBasedWaveletStore.class).in(Singleton.class);
   }
 
   @Provides
   private LocalWaveletContainer.Factory provideLocalWaveletContainerFactory(
       final WaveletStore waveletStore) {
     return new LocalWaveletContainer.Factory() {
       @Override
       public LocalWaveletContainer create(WaveletName waveletName) throws IOException {
         return new LocalWaveletContainerImpl(waveletStore.open(waveletName));
       }
     };
   }
 
   @Provides
   private RemoteWaveletContainer.Factory provideRemoteWaveletContainerFactory(
       final WaveletStore waveletStore) {
     return new RemoteWaveletContainer.Factory() {
       @Override
       public RemoteWaveletContainer create(WaveletName waveletName) throws IOException {
         return new RemoteWaveletContainerImpl(waveletStore.open(waveletName));
       }
     };
   }
 
   @Provides
   private WaveCertPathValidator provideWaveCertPathValidator(
       @Named(CoreSettings.WAVESERVER_DISABLE_SIGNER_VERIFICATION) boolean disableSignerVerification,
       TimeSource timeSource, VerifiedCertChainCache certCache,
       TrustRootsProvider trustRootsProvider) {
     if (disableSignerVerification) {
       return new DisabledCertPathValidator();
     } else {
       return new CachedCertPathValidator(certCache, timeSource, trustRootsProvider);
     }
   }
 }
