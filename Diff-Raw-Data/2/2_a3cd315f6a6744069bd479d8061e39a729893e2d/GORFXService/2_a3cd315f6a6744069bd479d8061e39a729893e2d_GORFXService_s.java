 package de.zib.gndms.GORFX.service;
 /*
  * Copyright 2008-2010 Zuse Institute Berlin (ZIB)
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
 
 import de.zib.gndms.kit.action.ActionMeta;
 import de.zib.gndms.kit.config.ConfigMeta;
 import de.zib.gndms.rest.Facets;
 import de.zib.gndms.rest.Specifier;
 import org.springframework.http.ResponseEntity;
 
 import java.util.List;
 
 /**
  * @author try ma ik jo rr a zib
  *         Date: 08.02.11, Time: 12:14
  *
  * The FULL interface of the GORFX service.
  *
  * Clients and resources which should provide the full feature catalog of the gorfx service should implement this
  * interface.
  *
  * The additional calles are for system maintenance and one-shot tasks.
  *
  * Some general remarks on the parameters:
  * <ul>
  *  <li> The <b>dn</b> parameter is a identifier for the user responsible for the method invocation. It is required to check if
  *       the user has the necessary permissions for the call.  <br>
  *          Using REST the attribute should be provided using a http-header property called "DN".
  *  <li> <b>wid</b> stands for Workflow-ID and is used to associate calles to a single workflow mainly for logging and
  *      debugging. <br>
  *          Using REST the attribute should be provided using a http-header property "wid".
  * </ul>
  */
 public interface GORFXService extends GORFXServiceEssentials {
 
     /**
      * Lists all actions of the config facet.
      *
      * A config action is a configuration unit dedicated to a specific aspect of GNDMS.
      *
      * @param dn The dn of the user invoking the method.
      * @return List of config actions names.
      */
     ResponseEntity<List<String>> listConfigActions( String dn );
 
     /**
      * Delivers usage information about a specific config action.
      *
      * The usage information includes a description of the configuration action, together with a description of its
      * parameters.
      *
      * @param actionName The name of the action
      * @param dn The dn of the user invoking the method.
      * @return The "help" of the action.
      */
     ResponseEntity<ConfigMeta> getConfigActionInfo( String actionName, String dn );
 
     /**
      * Executes a config action with the given arguments.
      *
     * @note At time beeing the config actions are designed for interactive operation with human beings. This has two
      * consequences for this method signature.
      * <ul>
      *  <li> Currently action arguments are provided as a key:vales in string format, it is highly possible that this
      * will change to a real Map, this concerns the \c args parameter.
      *  <li> Same applies for the return value which is currently in string form.
      * </ul>
      * Will be encapsulated with some web pages in future versions, which will cause a change to a more machine-friendly
      * representation of the values.
      *
      * @param actionName The name of the config action to execute.
      * @param args The arguments of the action.
      * @param dn The dn of the user invoking the method.
      * @return The result state of the action.
      */
     ResponseEntity<String> callConfigAction( String actionName, String args, String dn );
 
     /**
      * Lists all batch actions.
      *
      * Batch actions are actions which perform simple tasks, which aren't part of a workflow. They are mostly used for
      * system maintenance.
      *
      * @return A list of action names.
      * @param dn
      */
     ResponseEntity<List<String>> listBatchActions( String dn );
 
     /**
      * Delivers usage information about a specific config action.
      *
      * The usage information includes a description of the action, together with a description of its
      * parameters.
      *
      * @param actionName The name of the action
      * @param dn The dn of the user invoking the method.
      * @return The "help" of the action.
      */
     ResponseEntity<ActionMeta> getBatchActionInfo( String actionName, String dn );
 
     /**
      * @brief Delivers the specifier of a running action.
      *
      * @param actionName The name of the action
      * @param id The id of the running action.
      * @param dn The dn of the user invoking the method.
      * @return HttpStatus 200 together with a task specifier if the batch exists, 404 otherwise.
      */
     ResponseEntity<Specifier<Facets>> getBatchAction( String actionName, String id, String dn );
 
     /**
      * Executes a config action with the given arguments.
      *
      * @see GORFXService.callConfigAction() for details.
      *
      * @param actionName The name of the action to execute.
      * @param args The arguments of the action.
      * @param dn The dn of the user invoking the method.
      * @return A batch action specifier which might contain the result object, or if it triggers task a
      * specifier for the task.
      */
     ResponseEntity<Specifier> callBatchAction( String actionName, String args, String dn );
 }
