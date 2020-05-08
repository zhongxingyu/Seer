 /*
 * Copyright (c) 2010, Rickard Oberg. All Rights Reserved.
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
 package org.codeartisans.qipki.core.dci;
 
 import org.qi4j.api.injection.scope.Structure;
 import org.qi4j.api.injection.scope.Uses;
 import org.qi4j.api.structure.Module;
 
 /**
  * Base class for DCI contexts.
  */
 public abstract class Context
 {
 
     /**
      * Provides access to the context map
      */
     @Uses
     protected InteractionContext context;
     @Structure
     protected Module module;
 
     /**
      * Create a new sub-context, initialized with the current context map.
      *
      * @param contextClass class of the subcontext
      * @param <T>
      * @return the new subcontext
      */
     protected <T> T subContext( Class<T> contextClass )
     {
         return module.objectBuilderFactory().newObjectBuilder( contextClass ).use( context ).newInstance();
     }
 
 }
