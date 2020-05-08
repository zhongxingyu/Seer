 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy
  * of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed
  * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
  * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  */
 package org.amplafi.flow.validation;
 
 import java.util.List;
 
 
 /**
  * Holds flow validation results.
  */
 public interface FlowValidationResult {
     /**
      * Checks for validation problems. This can be determined
      * from the existence of FlowValidationTrackings.
      *
      * @return true if there are no validation problems.
      */
     boolean isValid();
 
     /**
      * Adds a validation problem.
      *
      * @param tracking A validation issue to track.
      * @return this
      */
     FlowValidationResult addTracking(FlowValidationTracking... tracking);
     /**
     * add a {@link org.amplafi.flow.validation.FlowValidationTracking} if valid is false.
      * @param valid add if false
      * @param activityKey TODO
      * @param messageKey
      * @param messageParams
      * @return this
      */
     FlowValidationResult addTracking(boolean valid, String activityKey, String messageKey, Object...messageParams);
     /**
      * Get all trackings.
      * @return all trackings stored. Null is allowed.
      */
     List<FlowValidationTracking> getTrackings();
 
     /**
      * @param addedFlowValidationResult will be merged with 'this'.
      */
     void merge(FlowValidationResult addedFlowValidationResult);
 }
