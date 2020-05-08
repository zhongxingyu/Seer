 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.issuetracker.activecollab;
 
 import de.cosmocode.issuetracker.IssueTracker;
 import de.cosmocode.issuetracker.IssueTrackerException;
 import org.codehaus.jackson.JsonNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Tobias Sarnowski
  */
 final class ACIssue implements ActiveCollabIssue {
     private static final Logger LOG = LoggerFactory.getLogger(ACIssue.class);
 
     private final AC ac;
     private String id;
     private String title;
     private String description;
     private int visibility;
     private int milestoneId;
     private int parentId;
 
     ACIssue(AC ac, JsonNode json) {
         this.ac = ac;
 
         // {"id":56374,"type":"Ticket","name":"test","body":"testbla","state":3,"visibility":null,"created_on":"2010-09-01 13:50:01","created_by_id":170,"updated_on":null,"updated_by_id":null,"version":1,"permalink":"http://pt.cosmocode.de/public/index.php/projects/43/tickets/577","priority":null,"due_on":null,"completed_on":null,"completed_by_id":null,"tags":[],"project_id":43,"parent_id":null,"milestone_id":null,"permissions":{"can_edit":true,"can_delete":true,"can_change_visibility":true,"can_move":false,"can_copy":false,"can_change_complete_status":true},"ticket_id":577}
 
         id = Integer.toString(json.get("id").getIntValue());
        title = json.get("test").toString();
         description = json.get("body").toString();
         visibility = json.get("visibility").getIntValue();
         milestoneId = json.get("milestoneId").getIntValue();
         parentId = json.get("parentId").getIntValue();
     }
 
     @Override
     public String getId() {
         return id;
     }
 
     @Override
     public IssueTracker getIssueTracker() {
         return ac;
     }
 
     @Override
     public String getTitle() {
         return title;
     }
 
     @Override
     public void setTitle(String title) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public String getDescription() {
         return description;
     }
 
     @Override
     public void setDescription(String description) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void update() throws IssueTrackerException {
         ac.updateIssue(this);
     }
 
     @Override
     public int getVisibility() {
         return visibility;
     }
 
     @Override
     public void setVisibility(int visibility) {
         this.visibility = visibility;
     }
 
     @Override
     public int getMilestoneId() {
         return milestoneId;
     }
 
     @Override
     public void setMilestoneId(int milestoneId) {
         this.milestoneId = milestoneId;
     }
 
     @Override
     public int getParentId() {
         return parentId;
     }
 
     @Override
     public void setParentId(int parentId) {
         this.parentId = parentId;
     }
 
     @Override
     public String toString() {
         return "ACIssue{" +
                 "ac=" + ac +
                 ", id='" + id + '\'' +
                 '}';
     }
 }
