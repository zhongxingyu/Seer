 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.domain.issue.models;
 
 import java.util.List;
 import org.openengsb.core.api.model.OpenEngSBModel;
 
 public interface Issue extends OpenEngSBModel {
 
     List<String> getComponents();
 
     void setComponents(List<String> components);
 
     String getId();
 
     void setId(String id);
     
     String getSummary();
 
     void setSummary(String summary);
 
     String getDescription();
 
     void setDescription(String description);
 
     String getOwner();
 
     void setOwner(String owner);
     
     String getReporter();
 
     void setReporter(String reporter);
 
     Priority getPriority();
 
     void setPriority(Priority priority);
 
     Status getStatus();
 
     void setStatus(Status status);
 
     String getDueVersion();
 
     void setDueVersion(String dueVersion);
 
     Type getType();
 
     void setType(Type type);
 }
