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
 
 package de.cosmocode.palava.jpa;
 
 import de.cosmocode.palava.ipc.Commands;
 import de.cosmocode.palava.ipc.FilterModule;
 
 /**
 * Binds the {@link TransactionFilter} class to all {@linkplain Command commands}
  * annotated with {@link Transactional}.
  *
  * @author Willi Schoenborn
  */
 public final class TransactionModule extends FilterModule {
 
     @Override
     protected void configure() {
         filter(Commands.annotatedWith(Transactional.class)).through(TransactionFilter.class);
     }
 
 }
