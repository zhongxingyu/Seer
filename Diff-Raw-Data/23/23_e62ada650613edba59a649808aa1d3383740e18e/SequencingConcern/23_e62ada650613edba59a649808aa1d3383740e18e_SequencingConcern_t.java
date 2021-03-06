 /*
  * Copyright 2006 Niclas Hedhman.
  *
  * Licensed  under the  Apache License,  Version 2.0  (the "License");
  * you may not use  this file  except in  compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed  under the  License is distributed on an "AS IS" BASIS,
  * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
  * implied.
  *
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.qi4j.tutorials.cargo.step2;
 
import org.qi4j.composite.ConcernOf;
 import org.qi4j.composite.scope.ThisCompositeAs;
 import org.qi4j.property.Property;
 
public class SequencingConcern extends ConcernOf<ShippingService>
     implements ShippingService
 {
     @ThisCompositeAs private HasSequence generator;
 
     public int makeBooking( Cargo cargo, Voyage voyage )
     {
         int ok = next.makeBooking( cargo, voyage );
         if( ok < 0 )
         {
             return ok;
         }
         Property<Integer> gen = generator.sequence();
         ok = gen.get();
         generator.sequence().set( ok + 1 );
         return ok;
     }
 }
