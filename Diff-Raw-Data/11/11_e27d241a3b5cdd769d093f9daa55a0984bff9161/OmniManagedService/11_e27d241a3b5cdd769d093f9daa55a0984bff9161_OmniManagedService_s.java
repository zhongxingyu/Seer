 /*
  * Copyright 2013 The Fictitious OMNI Corporation
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.talend.example.omni.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.apache.camel.Body;
 import org.apache.camel.Exchange;
 import org.apache.camel.Header;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class OmniManagedService {
     private static final Logger LOG = LoggerFactory.getLogger(OmniManagedService.class);
     private static final Random RAND = new Random();
 
     private Map<String, Map<String, Object>> pendingTasks = new HashMap<String, Map<String, Object>>();
 
     @SuppressWarnings("unchecked")
     public boolean newTask(Exchange exchange) {
         Map<String, Object> task = exchange.getIn().getBody(Map.class);
         String key = (String)task.get("mediaKey");
         boolean pending = pendingTasks.containsKey(key);
         LOG.info("FILTER task key='{}'. Task {} tracked", key, pending ? "ALREADY" : "NOT");
         return !pending;
     }
 
     public boolean random(Exchange exchange) {
         return RAND.nextBoolean();
     }
 
     public void trackTask(Map<String, Object> task) {
         LOG.info("TRACKING task key='{}'", task.get(OmniConstants.TASK_KEY));
         task.put(OmniConstants.TASK_STATUS, OmniConstants.STATUS_PENDING);
         pendingTasks.put(getKey(task), task);
     }
 
     public void scheduleTask(@Body String key, 
             @Header(value = OmniConstants.TASK_PROCESS_URI) String bpStart,
             @Header(value = OmniConstants.TASK_TRIGGER_URI) String bpTrigger) {
         LOG.info("SCHEDULING task key='{}'", key);
 
         Map<String, Object> task = pendingTasks.get(key);
         task.put(OmniConstants.TASK_PROCESS_URI, bpStart);
         task.put(OmniConstants.TASK_TRIGGER_URI, bpTrigger);
         
         // Invoke Baton.verifyFile and the taskId
         String taskId = "00000137a00ef4180475d64b000a0004001c009c"; // Baton.verifyFile(...)
         task.put(OmniConstants.TASK_ID, taskId);
         task.put(OmniConstants.TASK_STATUS, OmniConstants.STATUS_SCHEDULED);
         LOG.info("BATON verifyFile scheduled task key='{}' with taskId='{}'", key, taskId);
     }
 
     public List<String> monitorScheduledTasks() {
         List<String> result = new ArrayList<String>();
         for (Map<String, Object> task : pendingTasks.values()) {
             if (OmniConstants.STATUS_SCHEDULED.equals((String)task.get(OmniConstants.TASK_STATUS))) {
                 result.add((String)task.get(OmniConstants.TASK_ID));
             }
         }
         return result;
     }
 
    public Map<String, Object> completeTask(@Body String key) {
        Map<String, Object> task = pendingTasks.get(key);
         task.put(OmniConstants.TASK_STATUS, OmniConstants.STATUS_COMPLETE);
         return task;
     }
 
     private String getKey(Map<String, Object> task) {
         return (String)task.get(OmniConstants.TASK_KEY);
     }
 }
