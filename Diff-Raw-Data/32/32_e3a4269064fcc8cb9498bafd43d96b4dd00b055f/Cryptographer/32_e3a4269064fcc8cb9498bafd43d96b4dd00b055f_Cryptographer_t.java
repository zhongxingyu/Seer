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
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0
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
      * Default constructor
      */
     public Cryptographer() {
     }
 
     /**
      * Public method to decipher the coded cipher text
      * 
      * @param ciphertext
      *            the String to be decoded or translated into a meaningful
      *            sentence
      * @return String decrypted
      */
     public String decipher(String ciphertext) {
 
         init();
 
         /**
          * Reset the domain objects and all knowledge sources and clean the
          * blackboard for our new problem.
          */
         blackboard.reset();
 
         /**
          * Assert the problem at the blackboard
          */
         if (!blackboard.assertProblem(ciphertext)) {
             return SystemConstants.NO_PROBLEM_ASSERT_ERROR;
         }
 
         /**
          * Reset the controller
          */
         controller.reset();
 
         /**
          * Connect to the brain and wake the system
          */
         controller.connect();
 
         /**
          * Start the controller
          */
         return runController();
 
     }
 
     /**
      * Initialize the cryptographer and get the context objects
      */
     private void init() {
         BlackboardContext context = BlackboardContext.getInstance();
         this.blackboard = context.getBlackboard();
         this.controller = context.getController();
     }
 
     /**
      * Private method to loop while the controller processes hints to solve the
      * puzzle.
      * 
      * @return solution as String
      */
     private String runController() {
 
         while (!controller.isSolved() || controller.unableToProceed()) {
 
             controller.processNextHint();
 
             if (blackboard.isSolved()) {
                 controller.done();
                 return blackboard.retrieveSolution().value();
             }
             break; // TODO - remove
         }
 
        return SystemConstants.NO_DECIPHER_ERROR;
     }
 
 }
