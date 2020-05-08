 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.ipc.legacy;
 
 import org.jboss.netty.channel.ChannelPipeline;
 
 import com.google.inject.Binder;
 import com.google.inject.Key;
 import com.google.inject.Module;
 
 import de.cosmocode.palava.concurrent.BackgroundSchedulerModule;
 import de.cosmocode.palava.concurrent.DefaultThreadProviderModule;
 import de.cosmocode.palava.concurrent.ExecutorModule;
 import de.cosmocode.palava.core.DefaultRegistryModule;
 import de.cosmocode.palava.core.inject.TypeConverterModule;
 import de.cosmocode.palava.core.lifecycle.LifecycleModule;
 import de.cosmocode.palava.ipc.DefaultIpcCallFilterChainFactoryModule;
 import de.cosmocode.palava.ipc.IpcEventModule;
 import de.cosmocode.palava.ipc.IpcModule;
 import de.cosmocode.palava.ipc.command.localvm.LocalIpcCommandExecutorModule;
 import de.cosmocode.palava.ipc.netty.Boss;
 import de.cosmocode.palava.ipc.netty.ChannelPipelineFactoryModule;
 import de.cosmocode.palava.ipc.netty.NettyServiceModule;
 import de.cosmocode.palava.ipc.netty.Worker;
 import de.cosmocode.palava.ipc.session.store.IpcSessionStore;
 import de.cosmocode.palava.ipc.session.store.StoreIpcSessionModule;
 import de.cosmocode.palava.jmx.FakeMBeanServerModule;
 import de.cosmocode.palava.store.MemoryStoreModule;
 import de.cosmocode.palava.store.Store;
 
 /**
  * Test module.
  *
  * @since 1.0 
  * @author Willi Schoenborn
  */
 public final class LegacyTestModule implements Module {
 
     @Override
     public void configure(Binder binder) {
         binder.install(new TypeConverterModule());
         binder.install(new LifecycleModule());
         binder.install(new DefaultRegistryModule());
        binder.install(new IpcModule());
        binder.install(new LocalIpcCommandExecutorModule());
        binder.install(new FakeMBeanServerModule());
         binder.install(new DefaultThreadProviderModule());
         binder.install(new ExecutorModule(Boss.class, Boss.NAME));
         binder.install(new ExecutorModule(Worker.class, Worker.NAME));
        binder.install(new BackgroundSchedulerModule());
         binder.install(new IpcEventModule());
        binder.install(new NettyServiceModule());
        binder.install(new StoreIpcSessionModule());
         binder.install(new DefaultIpcCallFilterChainFactoryModule());
         binder.install(new MemoryStoreModule());
         binder.bind(Store.class).annotatedWith(IpcSessionStore.class).to(Store.class);
        binder.install(new LegacyNettyModule());
         binder.install(new ChannelPipelineFactoryModule());
         binder.bind(ChannelPipeline.class).to(Key.get(ChannelPipeline.class, Legacy.class));
     }
 
 }
