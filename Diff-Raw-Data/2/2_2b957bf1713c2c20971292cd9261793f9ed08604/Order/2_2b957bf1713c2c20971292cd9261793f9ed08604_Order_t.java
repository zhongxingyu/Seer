 package de.zib.gndms.model.gorfx.types;
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
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
 
 /**
  * @author try ma ik jo rr a zib
  * @date 09.02.11 15:39
  *
  * @brief The interface for taskflow orders.
  *
  * An order serves as input for the taskflow, and steers the task execution.
  */
 public interface Order {
 
     /**
      * Delivers the type of the task flow.
      *
      * Useful to query the taskflow and its quote calculator from the TaskFlowProvider.
      * @return The task flow type id.
      */
     String getTaskFlowType();
 
 
     /**
      * Marker: that the task flow shouldn't be executed.
      *
      * This is a relict from the old ORQ schema. It isn't really usful anymore.
      *
      * @return If the values should only be estimated.
      */
     boolean isJustEstimate();
 
 }
