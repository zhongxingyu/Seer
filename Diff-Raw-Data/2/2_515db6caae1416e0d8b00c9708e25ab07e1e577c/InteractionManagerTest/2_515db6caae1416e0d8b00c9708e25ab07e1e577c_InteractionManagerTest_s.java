 /*
  * Copyright 2012 INRIA
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
 package com.mymed.tests.unit.manager;
 
iimport static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.junit.Test;
 
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.model.data.interaction.MInteractionBean;
 public class InteractionManagerTest extends GeneralTest {
     /**
      * Create an interaction entry in the database
      */
     @Test
     public void testCreateInteraction() {
         try {
             interactionManager.create(interactionBean);
         } catch (final Exception ex) {
             fail(ex.getMessage());
         }
     }
 
     /**
      * Read the just created interaction entry from the database
      */
     @Test
     public void testReadInteraction() {
         try {
             final MInteractionBean readValue = interactionManager.read(INTERACTION_ID);
             assertEquals("The interaction beans are not the same\n", interactionBean, readValue);
         } catch (final Exception ex) {
             fail(ex.getMessage());
         }
     }
 
     /**
      * Update the interaction bean, and check that the new bean is not the same
      * as the old one
      * 
      * @throws IOBackEndException
      * @throws InternalBackEndException
      */
     @Test(expected = IOBackEndException.class)
     public void testUpdateInteraction() throws InternalBackEndException, IOBackEndException {
         final MInteractionBean newInteractionBean = interactionBean.clone();
         newInteractionBean.setFeedback(0.6);
 
         interactionManager.update(newInteractionBean);
     }
 
     /**
      * Delete the session created
      */
     public void testDeleteInteraction() {
         try {
             interactionManager.delete(INTERACTION_ID);
             final Map<byte[], byte[]> column = storageManager.selectAll("Interaction", INTERACTION_ID);
             assertTrue("The number of columns after a delete is not 0", column.isEmpty());
         } catch (final Exception ex) {
             fail(ex.getMessage());
         }
     }
 }
