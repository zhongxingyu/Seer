 /*
  * Copyright 2013 Ilya Gubarev.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ilyagubarev.algorithms.analysis.sorting;
 
 import java.io.Serializable;
 
 /**
  * Sorting method report identifier.
  *
  * @see Serializable
  *
 * @version 1.02, 30 September 2013
  * @since 23 September 2013
  * @author Ilya Gubarev
  */
 public final class SortReportId implements Serializable {
 
     private final String _sorterId;
     private final String _taskId;
 
     /**
      * Creates a new instance of SortReportId
      *
      * @param sorterId sorter identifier.
      * @param taskId sorting task identifier.
      */
     public SortReportId(String sorterId, String taskId) {
         if (sorterId == null) {
             throw new NullPointerException("sorter identifier is null");
         }
         if (taskId == null) {
             throw new NullPointerException("task identifier is null");            
         }
         _sorterId = sorterId;
         _taskId = taskId;
     }
 
     /**
      * Gets sorting method identifier.
      *
      * @return sorter identifier.
      */
     public String getSorterId() {
         return _sorterId;
     }
 
     /**
      * Gets sorting task identifier.
      *
      * @return task identifier.
      */
     public String getTaskId() {
         return _taskId;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
        hash = 37 * hash + _sorterId.hashCode();
        hash = 37 * hash + _taskId.hashCode();
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (object == null) {
             return false;
         }
         if (getClass() != object.getClass()) {
             return false;
         }
         SortReportId other = (SortReportId) object;
        String sorterId = other._sorterId;
        String taskId = other._taskId;
        if (!_sorterId.equals(sorterId)) {
             return false;
         }
        if (!_taskId.equals(taskId)) {
             return false;
         }
         return true;
     }
 }
