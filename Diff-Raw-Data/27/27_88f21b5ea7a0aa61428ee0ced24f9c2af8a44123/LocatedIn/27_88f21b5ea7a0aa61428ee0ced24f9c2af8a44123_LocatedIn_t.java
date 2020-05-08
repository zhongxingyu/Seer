 /*
  * Copyright 2011-2013 Adele Research Group (http://adele.imag.fr/) 
  * LIG Laboratory (http://www.liglab.fr)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package fr.liglab.adele.cube.extensions.core.resolvers;
 
 import fr.liglab.adele.cube.extensions.AbstractUnaryResolver;
 import fr.liglab.adele.cube.extensions.Extension;
 import fr.liglab.adele.cube.metamodel.InvalidNameException;
 import fr.liglab.adele.cube.metamodel.ManagedElement;
 import fr.liglab.adele.cube.metamodel.PropertyExistException;
 import fr.liglab.adele.cube.metamodel.PropertyNotExistException;
 
 /**
  * Author: debbabi
  * Date: 4/29/13
  * Time: 2:07 AM
  */
 public class LocatedIn extends AbstractUnaryResolver {
 
     public LocatedIn(Extension extension) {
         super(extension);
     }
 
     public String getName() {
         return this.getClass().getSimpleName();
     }
 
     public boolean check(ManagedElement me, String value) {
         if (me != null && value != null) {
             if (me.getAutonomicManager() == null)
                 return false;
             else {
                System.out.println("me.am="+me.getAutonomicManager());
                System.out.println("value="+value);
                System.out.println("equal?"+me.getAutonomicManager().equalsIgnoreCase(value));
                 return me.getAutonomicManager().equalsIgnoreCase(value);
             }
         }
         return false;
     }
 
     public boolean perform(ManagedElement me, String value) {
         if (me != null && value != null) {
             me.setAutonomicManager(value);
             return true;
         }
         return false;
     }
 
 }
