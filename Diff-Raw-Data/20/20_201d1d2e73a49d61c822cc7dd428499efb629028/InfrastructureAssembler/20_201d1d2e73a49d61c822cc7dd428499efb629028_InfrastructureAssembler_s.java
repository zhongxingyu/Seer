 /*
  * Copyright (c) 2008, Muhd Kamil Mohd Baki. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos.ui.wicket.bootstrap.assembler;
 
 import org.qi4j.bootstrap.Assembler;
 import org.qi4j.bootstrap.ModuleAssembly;
 import org.qi4j.bootstrap.AssemblyException;
 import org.qi4j.spi.entity.UuidIdentityGeneratorService;
 import org.qi4j.entity.memory.IndexedMemoryEntityStoreService;
 import org.qi4j.entity.index.rdf.RDFQueryService;
 import org.qi4j.structure.Visibility;
 
 public class InfrastructureAssembler implements Assembler
 {
     public void assemble( ModuleAssembly module ) throws AssemblyException
     {
         module.addServices(
             UuidIdentityGeneratorService.class,
            IndexedMemoryEntityStoreService.class,
             RDFQueryService.class
             ).visibleIn( Visibility.application ).instantiateOnStartup();
     }
 }
