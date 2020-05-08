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
 package org.dlw.ai.blackboard;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dlw.ai.blackboard.util.SystemConstants;
 
 /**
  * <p>
  * The Cryptographer class represents the host of the blackboard problem. He
  * mediates and tends the problem until it is solved. This class provides the
  * management logic behind the resolution of a cryptogram puzzle.
  * </p>
  * 
  * <p>
  * This class is not extendable and therefore not part of the API. Its use is
  * specific to the problem domain being solved by
  * {@link org.dlw.ai.blackboard.Main}.
  * </p>
  * 
  * @author dlwhitehurst
  * @version 1.0.0-RC
  * 
  */
 public final class Cryptographer {
 
     /**
      * Attribute blackboard where problem is solved
      */
     private Blackboard blackboard;
 
     /**
      * Attribute controller for problem solving logic
      */
     private Controller controller;
 
     /**
      * Attribute brain or source of knowledge
      */
     private Brain brain;
 
     /**
      * Attribute class logger
      */
     private static final Log log = LogFactory.getLog(Cryptographer.class);
 
     /**
      * Public method to decipher the coded cipher text
      * 
      * @param ciphertext
      *            the String to be decoded or translated into a meaningful
      *            sentence
      * @return String decrypted
      */
     public String decipher(String ciphertext) {
 
         /**
          * Reset the domain objects and all knowledge sources and clean the
          * blackboard for our new problem.
          */
 
         controller.reset();
 
         /**
          * Connect to the brain and wake the system
          */
 
         controller.connect();
 
         /**
          * Clean the blackboard
          */
 
         blackboard.reset();
 
         /**
          * Assert the problem at the blackboard
          */
 
         if (!blackboard.assertProblem(ciphertext)) {
             if (log.isErrorEnabled()) {
                 log.error(SystemConstants.NO_ASSERT_ERROR);
             }
 
             return SystemConstants.NO_ASSERT_ERROR;
         }
 
         /**
          * Kick start the controller
          */
         return runController();
 
     }
 
     /**
      * @param blackboard
      *            the blackboard to set
      */
     public void setBlackboard(Blackboard blackboard) {
         this.blackboard = blackboard;
     }
 
     /**
      * @return the blackboard
      */
     public Blackboard getBlackboard() {
         return blackboard;
     }
 
     /**
      * @param controller
      *            the controller to set
      */
     public void setController(Controller controller) {
         this.controller = controller;
     }
 
     /**
      * @return the controller
      */
     public Controller getController() {
         return controller;
     }
 
     /**
      * @param brain
      *            the brain to set
      */
     public void setBrain(Brain brain) {
         this.brain = brain;
     }
 
     /**
      * @return the brain
      */
     public Brain getBrain() {
         return brain;
     }
 
     /**
      * Private method to loop while the controller processes hints to solve the
      * puzzle.
      * 
      * @return
      */
     private String runController() {
 
         while (!controller.isSolved() || controller.unableToProceed()) {
 
             controller.processNextHint();
 
             if (blackboard.isSolved()) {
                 this.getController().done();
                 return blackboard.retrieveSolution().value();
             }
             break; // TODO - remove
            
         }
 
         return SystemConstants.NO_SOLVE_ERROR;
     }
 
 }
