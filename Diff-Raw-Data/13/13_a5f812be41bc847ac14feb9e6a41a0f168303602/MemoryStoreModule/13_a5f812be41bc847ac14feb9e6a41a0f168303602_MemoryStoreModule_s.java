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
 
 package de.cosmocode.palava.store;
 
 import com.google.inject.Binder;
 import com.google.inject.Module;
 import com.google.inject.Singleton;
 
 /**
 * Binds the {@link Store} interface to {@link MemoryStore}.
  *
  * @author Willi Schoenborn
  */
 public final class MemoryStoreModule implements Module {
 
     @Override
     public void configure(Binder binder) {
         binder.bind(ByteStore.class).to(MemoryStore.class).in(Singleton.class);
         binder.bind(Store.class).to(ByteStore.class).in(Singleton.class);
     }
 
 }
