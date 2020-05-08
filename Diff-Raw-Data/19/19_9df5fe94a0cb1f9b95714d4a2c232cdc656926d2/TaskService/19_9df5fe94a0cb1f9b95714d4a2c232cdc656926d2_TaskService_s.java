 package de.zib.gndms.common.GORFX.service;
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
 
 import de.zib.gndms.common.model.gorfx.types.TaskControl;
 import de.zib.gndms.common.model.gorfx.types.TaskFailure;
 import de.zib.gndms.common.model.gorfx.types.TaskResult;
 import de.zib.gndms.common.model.gorfx.types.TaskServiceConfig;
 import de.zib.gndms.common.model.gorfx.types.TaskServiceInfo;
 import de.zib.gndms.common.model.gorfx.types.TaskStatus;
 import de.zib.gndms.common.rest.Facets;
 
 import org.springframework.http.ResponseEntity;
 
 /**
  * @author try ma ik jo rr a zib
  * @date 02.03.11  17:38
  * @brief Interface for the task services.
  *
  * The task service is used to executed arbitrary tasks. This task can
  * but mustn't belong to an taskflow or a batch.
  */
 public interface TaskService {
 
     /** 
      * @brief Delivers infos about the service.
      * 
      * This information contain available task-types, together with
      * usage statistics on these types.
      *
      * @return An info object.
      */
    ResponseEntity<TaskServiceInfo> getServiceInfo();
 
     /** 
      * @brief Reads the service configuration
      * 
      * @param dn The dn of the requesting user.
      * 
      * @return The Service configuration.
      */
     ResponseEntity<TaskServiceConfig> getServiceConfig( String dn );
 
     /** 
      * @brief Changes the service configuration.
      * 
      * @param cfg The new configuration.
      * @param dn The dn of the user, which must be allowed to change
      * the config.
      * 
      * @return Check the HTTP-status of the response-entity
      *  - 202 means the new configuration was accepted.
      *  - 406 means the new configuration isn't acceptable.
      *    In this case the response body contains an error message.
      *  - 403 means the user isn't allowed to change the configuration.
      */
     ResponseEntity<String> setServiceConfig( TaskServiceConfig cfg, String dn );
 
     /** 
      * @brief Delivers the facets of a task.
      * 
      * @param id The id of the task.
      * @param dn The callers dn.
      * 
      * @return A list of facets of the task with \e id.
      */
     ResponseEntity<Facets> getTaskFacets( String id, String dn );
 
     /** 
      * @brief Removes a task.
      *
      * If the task is running it will be cancelled. It migth take some
      * time to remove a task, this its not gone instantly.
      * 
      * @param id The id of the task,
      * @param dn The callers dn.
      * @param wid A possible workflow id.
      * 
      * @return HTTP-Status 200 if deletion was successful, 403 if the
      * user wasn't allowed to remove the task.
      */
     ResponseEntity<Void> deleteTask( String id, String dn, String wid );
 
     /** 
      * @brief Delives the status of a task.
      * 
      * @param id The id of the task,
      * @param dn The callers dn.
      * @param wid A possible workflow id.
      * 
      * @return A status object of the task.
      */
     ResponseEntity<TaskStatus> getStatus( String id, String dn, String wid );
 
     /** 
      * @brief Can be used to control task execution.
      *
      * Common options are pause and abort of a task.
      * 
      * @param status The status the task should change to. Which
      * status is acceptable depends on the task kind and current
      * status.
      * @param id The id of the task,
      * @param dn The callers dn.
      * @param wid A possible workflow id.
      * 
      * @return HTTP-Status 
      *      - 200 if the status was changed successful, 
      *      - 403 if the user wasn't allowed to remove the task.
      *      - 400 if the status can't be changed to \c status.
      */
     ResponseEntity<Void> changeStatus( String id, TaskControl status, String dn, String wid );
 
     /** 
      * @brief Delivers the result of the task.
      * 
      * @param id The id of the task,
      * @param dn The callers dn.
      * @param wid A possible workflow id.
      * 
      * @return  HTTP-Status 
      *      - 200 if the result was available. The result will be the
      *      request body.
      *      - 403 if the user wasn't allowed to collect the result.
      *      - 404 if there wasn't any result yet.
      */
     @SuppressWarnings("rawtypes")
 	ResponseEntity<TaskResult> getResult( String id, String dn, String wid );
 
     /** 
      * @brief Delivers possible error information if the task failed.
      * 
      * @param id The id of the task,
      * @param dn The callers dn.
      * @param wid A possible workflow id.
      * 
      * @return  HTTP-Status 
      *      - 200 if failure information are available. The info will be the
      *      request body.
      *      - 403 if the user wasn't allowed to collect the information.
      *      - 404 if there wasn't any failure yet.
      */
     ResponseEntity<TaskFailure> getErrors( String id, String dn, String wid );
 }
