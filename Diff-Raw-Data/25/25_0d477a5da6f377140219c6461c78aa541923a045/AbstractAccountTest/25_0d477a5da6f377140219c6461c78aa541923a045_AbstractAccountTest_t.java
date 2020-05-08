 /*  Copyright 2008 Edward Yakop.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos.domain.model.account.assembly;
 
 import org.qi4j.bootstrap.AssemblyException;
 import org.qi4j.bootstrap.ModuleAssembly;
 import org.qi4j.entity.index.rdf.assembly.RdfMemoryStoreAssembler;
 import org.qi4j.entity.memory.MemoryEntityStoreService;
 import org.qi4j.library.rdf.entity.EntitySerializer;
 import org.qi4j.spi.entity.UuidIdentityGeneratorService;
 import org.qi4j.test.AbstractQi4jTest;
 
 /**
  * @author edward.yakop@gmail.com
  * @since 0.5
  */
 abstract class AbstractAccountTest extends AbstractQi4jTest
 {
     public void assemble( ModuleAssembly module )
         throws AssemblyException
     {
         module.addAssembler( new RdfMemoryStoreAssembler() );
         module.addServices( MemoryEntityStoreService.class, UuidIdentityGeneratorService.class );
         module.addAssembler( new AccountAssembler() );
     }
 }
