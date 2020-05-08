 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.common.contracts;
 
import java.lang.reflect.Method;
 import org.dozer.DozerEventListener;
 import org.dozer.event.DozerEvent;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 import org.sola.services.common.repository.entities.ChildEntityInfo;
 
 /**
  * Hooks into the Dozer translation process to explicitly call the setter method
  * during translation of child entities. This is to ensure the setter logic is executed. 
  * @author soladev
  */
 public class GenericTranslatorListener implements DozerEventListener {
 
     /**
      * This listener method is triggered once before the mapping process begins.
      * @param event 
      */
     @Override
     public void mappingStarted(DozerEvent event) {
     }
 
     /**
      * This listener method is triggered before each field in the target object has been
      * assigned its destination value.
      * @param event 
      */
     @Override
     public void preWritingDestinationValue(DozerEvent event) {
     }
 
     /**
      * This listener method is triggered after each field in the target object has been
      * assigned its destination value. Dozer does not trigger the setter for child entities, so
      * this listener method checks to see if the destination value assigned is a child entity or 
      * a child entity list and if so, it triggers the appropriate setter method on the parent 
      * to execute any related setter functionality. 
      * Note that this method does not trigger the setter methods when the Destination Object is a TO. 
      * @param event 
      */
     @Override
     public void postWritingDestinationValue(DozerEvent event) {
        Object array = event.getFieldMap().getSrcFieldValue(event.getSourceObject());
        if (array != null && array instanceof byte[]) {
            try {
                String name = event.getFieldMap().getDestFieldName();
                name = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
                Method method = event.getDestinationObject().getClass().getMethod(name, byte[].class);
                method.invoke(event.getDestinationObject(), (byte[]) array);
            } catch (Exception e) {
                System.out.println("Error occured while assigning byte array value. " + e.getMessage());
            }
        }
        
         if (event.getDestinationObject() instanceof AbstractReadOnlyEntity) {
             // Parent object is an Entity, check the child fields and fire the setter if appropriate.  
             AbstractReadOnlyEntity parent = (AbstractReadOnlyEntity) event.getDestinationObject();
             ChildEntityInfo childInfo = parent.getChildEntityInfo(
                     event.getFieldMap().getDestFieldName());
             if (childInfo != null) {
                 parent.setEntityFieldValue(childInfo,
                         parent.getEntityFieldValue(childInfo));
             }
         }
     }
 
     /**
      * This listener method is triggered once at the completion of the mapping process. 
      * @param event 
      */
     @Override
     public void mappingFinished(DozerEvent event) {
     }
 }
