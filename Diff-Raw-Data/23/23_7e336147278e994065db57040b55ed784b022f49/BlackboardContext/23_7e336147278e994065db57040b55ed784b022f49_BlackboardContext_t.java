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
 import org.dlw.ai.blackboard.util.UniversalContext;
 
 /**
  * <p>
  * This singleton class can be used by any class to obtain the blackboard and 
  * controller instances.
  * </p>
  * 
  * <p>
  * This class cannot be extended and therefore not part of the API. Its use is
  * specific to the problem domain being solved by
  * {@link org.dlw.ai.blackboard.Main}.
  * </p>
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0-RC
  * 
  */
 public final class BlackboardContext {
 
     /**
      * Attribute singleton instance
      */
     private static BlackboardContext instance;
     
     /**
      * Attribute blackboard
      */
     private Blackboard blackboard;
 
     /**
      * Attribute controller
      */
     private Controller controller;
 
     /**
      * Hidden constructor
      */
     private BlackboardContext() {
         this.blackboard = (Blackboard) UniversalContext.getApplicationContext().getBean("blackboard");
         this.controller = (Controller) UniversalContext.getApplicationContext().getBean("controller");
     }
     
     /**
      * Method to return singleton instance
     * @return {@link BlackboardContext}
      */
     public static BlackboardContext getInstance() {
         if (instance == null) {
             instance = new BlackboardContext();
         }
         return instance;
     }
 
     /**
      * @return the {@link Blackboard} object
      */
     public final Blackboard getBlackboard() {
         return blackboard;
     }
 
     /**
      * @param blackboard
      *            the {@link Blackboard} object to set
      */
     public final void setBlackboard(Blackboard blackboard) {
         this.blackboard = blackboard;
     }
 
     /**
      * @return the {@link Controller} object
      */
     public final Controller getController() {
         return controller;
     }
 
     /**
      * @param controller
      *            the {@link Controller} object to set
      */
     public final void setController(Controller controller) {
         this.controller = controller;
     }
 
     @Override
     public boolean equals(Object o) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public int hashCode() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     @Override
     public String toString() {
         return SystemConstants.BLACKBOARD_CONTEXT;
     }
 
 }
