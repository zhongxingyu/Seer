 /*
  * Copyright (c) 2010, Paul Merlin. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package org.codeartisans.qipki.core.assembly.index;
 
 import org.qi4j.api.injection.scope.Service;
 import org.qi4j.api.injection.scope.Structure;
 import org.qi4j.api.mixin.Mixins;
 import org.qi4j.api.service.Activatable;
 import org.qi4j.api.service.ServiceComposite;
 import org.qi4j.api.unitofwork.UnitOfWork;
 import org.qi4j.api.unitofwork.UnitOfWorkFactory;
 import org.qi4j.index.reindexer.ReindexerService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Mixins( AutomaticReindexerService.Mixin.class )
 public interface AutomaticReindexerService
         extends ServiceComposite, Activatable
 {
 
     @SuppressWarnings( "PublicInnerClass" )
     abstract class Mixin
             implements AutomaticReindexerService
     {
 
         private static final Logger LOGGER = LoggerFactory.getLogger( AutomaticReindexerService.class );
         @Structure
         private UnitOfWorkFactory uowf;
         @Service
         private ReindexerService reindexer;
 
         @Override
         public void activate()
                 throws Exception
         {
            boolean doDiscard = false;
            UnitOfWork uow = uowf.currentUnitOfWork();
            if ( uow == null ) {
                uow = uowf.newUnitOfWork();
                doDiscard = true;
            }
 
            LOGGER.debug( "Will start reindexing now.." );
             long start = System.currentTimeMillis();
             reindexer.reindex();
            LOGGER.info( "Reindexing ended successfully and took {}ms", System.currentTimeMillis() - start );
 
            if ( doDiscard ) {
                uow.discard();
            }
         }
 
         @Override
         public void passivate()
                 throws Exception
         {
         }
 
     }
 
 }
