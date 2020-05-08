 /**
  * Copyright 2010 David L. Whitehurst
  * 
  * Licensed under the Apache License, Version 2.0 
  * (the "License"); You may not use this file except 
  * in compliance with the License. You may obtain a 
  * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, 
  * software distributed under the License is distributed on an 
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific 
  * language governing permissions and limitations under the 
  * License.
  * 
  */
 package org.dlw.ai.blackboard.domain;
 
 import org.dlw.ai.blackboard.Blackboard;
import org.dlw.ai.blackboard.BlackboardContext;
 import org.dlw.ai.blackboard.util.UniversalContext;
 
 /**
  * This class defines the abstract blackboard object. All objects that make up
  * part of the problem domain are classified as a blackboard object. The
  * blackboard object extends the domain because in the future other types of
  * domain objects may allow for the resolution of other problems that do not use
  * the blackboard model of opportunistic reasoning.
  * 
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0
  * 
  */
 public abstract class BlackboardObject extends Dependent {
 
     /**
      * Each blackboard object adds itself or registers with the blackboard. Each
      * object also represents itself within the blackboard domain.
      */
     public final void register() {
        Blackboard blackboard = BlackboardContext.getInstance().getBlackboard();
         blackboard.add(this);
     }
 
     /**
      * Each blackboard object can also remove itself or resign from the problem
      * domain, e.g. {@link CipherLetter} objects can remove themselves and allow
      * an {@link Letter} or plaintext equivalent to be its representative
      */
     public final void resign() {
        Blackboard blackboard = BlackboardContext.getInstance().getBlackboard();
         blackboard.remove(this);
     }
 
 }
