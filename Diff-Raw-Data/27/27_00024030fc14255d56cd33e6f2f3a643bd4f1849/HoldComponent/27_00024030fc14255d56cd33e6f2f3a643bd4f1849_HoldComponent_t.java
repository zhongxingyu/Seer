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
 
 import fr.liglab.adele.cube.autonomicmanager.RuntimeModelController;
 import fr.liglab.adele.cube.extensions.AbstractResolver;
 import fr.liglab.adele.cube.extensions.Extension;
 import fr.liglab.adele.cube.extensions.core.model.Component;
 import fr.liglab.adele.cube.extensions.core.model.Node;
 import fr.liglab.adele.cube.metamodel.InvalidNameException;
 import fr.liglab.adele.cube.metamodel.ManagedElement;
 import fr.liglab.adele.cube.metamodel.Reference;
 import fr.liglab.adele.cube.util.model.ModelUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: debbabi
  * Date: 4/28/13
  * Time: 8:24 PM
  */
 public class HoldComponent extends AbstractResolver {
 
 
     public HoldComponent(Extension extension) {
         super(extension);
     }
 
     public String getName() {
         return this.getClass().getSimpleName();
     }
 
     /**
      *
      * @param me Node
      * @param value uuid of Component
      * @return
      */
     public boolean check(ManagedElement me, String value) {
         if (me != null && value != null) {
             Reference r = me.getReference(Node.CORE_NODE_COMPONENTS);
             if (r != null) {
                 for (String ref : r.getReferencedElements()) {
                     if (ref.equalsIgnoreCase(value)) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     public boolean perform(ManagedElement me, String value) {
        System.out.println("\n@@@@@ ---> performing holdComponent between node "+me.getUUID()+" and component "+ value + "\n");
         if (me != null && value != null) {
             Reference r = me.getReference(Node.CORE_NODE_COMPONENTS);
             if (r == null) {
                 try {
                     r = me.addReference(Node.CORE_NODE_COMPONENTS, false);
                 } catch (InvalidNameException e) {
                     e.printStackTrace();
                 }
             }
             r.addReferencedElement(value);
            RuntimeModelController rmc = getExtension().getAutonomicManager().getRuntimeModelController();
            System.out.println("\n@@@@@ ===> adding node "+me.getUUID()+" to component "+ value + "\n");
            try {
                rmc.addReferencedElement(value, Component.CORE_COMPONENT_NODE, me.getUUID());
            } catch (InvalidNameException e) {
                e.printStackTrace();
            }
             return true;
         }
         return false;
     }
 
     /**
      *
      * @param me Component
      * @param description of Node
      * @return
      */
     public List<String> find(ManagedElement me, ManagedElement description) {
         List<String> result = new ArrayList<String>();
         if (me != null) {
             Reference r = me.getReference(Component.CORE_COMPONENT_NODE);
             if (r != null) {
                 for (String tmp : r.getReferencedElements()) {
 
                     ManagedElement node_tmp = getExtension().getAutonomicManager().getRuntimeModelController().getCopyOfManagedElement(tmp);
                     if (ModelUtils.compareTwoManagedElements(description, node_tmp) == 0) {
                         result.add(tmp);
                     }
                 }
             }
         }
         return result;
     }
 
 }
