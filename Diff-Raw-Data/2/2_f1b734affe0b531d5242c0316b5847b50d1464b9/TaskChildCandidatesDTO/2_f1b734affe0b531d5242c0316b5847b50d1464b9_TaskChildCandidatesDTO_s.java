 /*
  * Copyright 2010 Last.fm
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package fm.last.citrine.web;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import fm.last.citrine.model.Task;
 
 /**
 * Data transfer object for configuring relationships between a Job and its children.
  */
 public class TaskChildCandidatesDTO {
 
   private Task task = new Task();
   private Set<Long> candidateChildTaskIds = new HashSet<Long>();
   private Set<Long> childTaskIds = new HashSet<Long>();
   private String selectedGroupName;
 
   public TaskChildCandidatesDTO() {
   }
 
   public TaskChildCandidatesDTO(Task task) {
     this.task = task;
     for (Task child : task.getChildTasks()) {
       childTaskIds.add(child.getId());
     }
   }
 
   /**
    * @return the task.
    */
   public Task getTask() {
     return task;
   }
 
   /**
    * @param task the task to set.
    */
   public void setTask(Task task) {
     this.task = task;
   }
 
   /**
    * @return the candidateChildTaskIds
    */
   public Set<Long> getCandidateChildTaskIds() {
     return candidateChildTaskIds;
   }
 
   /**
    * @param candidateChildTaskIds the candidateChildTaskIds to set
    */
   public void setCandidateChildTaskIds(Set<Long> candidateChildTaskIds) {
     this.candidateChildTaskIds = candidateChildTaskIds;
   }
 
   /**
    * @return the childTaskIds.
    */
   public Set<Long> getChildTaskIds() {
     return childTaskIds;
   }
 
   /**
    * @param childTaskIds the childTaskIds to set.
    */
   public void setChildTaskIds(Set<Long> childTaskIds) {
     this.childTaskIds = childTaskIds;
   }
 
   /**
    * @return the selectedGroupName
    */
   public String getSelectedGroupName() {
     return selectedGroupName;
   }
 
   /**
    * @param selectedGroupName the selectedGroupName to set
    */
   public void setSelectedGroupName(String selectedGroupName) {
     this.selectedGroupName = selectedGroupName;
   }
   
 }
