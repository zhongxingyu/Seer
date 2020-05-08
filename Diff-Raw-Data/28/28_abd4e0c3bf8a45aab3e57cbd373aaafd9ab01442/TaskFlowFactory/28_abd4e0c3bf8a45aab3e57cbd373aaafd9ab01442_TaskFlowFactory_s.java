 package de.zib.gndms.logic.model.gorfx.taskflow;
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
 
 import de.zib.gndms.common.model.gorfx.types.Order;
import de.zib.gndms.common.model.gorfx.types.Quote;
 import de.zib.gndms.common.model.gorfx.types.TaskFlowInfo;
 import de.zib.gndms.logic.model.TaskAction;
 import de.zib.gndms.logic.model.gorfx.AbstractQuoteCalculator;
import de.zib.gndms.model.common.PersistentContract;
 import de.zib.gndms.model.gorfx.types.DelegatingOrder;
import de.zib.gndms.neomodel.gorfx.TaskFlow;
import de.zib.gndms.neomodel.common.Dao;
 import de.zib.gndms.neomodel.common.Session;
import de.zib.gndms.neomodel.gorfx.Task;
 import de.zib.gndms.stuff.GNDMSInjector;
 
 /**
  * @author try ma ik jo rr a zib
  * @date 14.02.11  14:27
  * @brief An interface for taskflow factories.
  *
  * Implementations of this interface should be able to compute quotes, and generate task instances.
  */
 public interface TaskFlowFactory<O extends Order, C extends AbstractQuoteCalculator<O>> {
 
 
     /**
      * @brief Delivers the key of this task flow.
      *
      * @return The taskflow key.
      */
     String getTaskFlowKey();
 
     /**
      * @brief Delivers the version of the plugin.
      *
      * @return An integer representing the version.
      */
     int getVersion();
 
     /** 
      * @brief Delivers a calculator for quotes of the taskflow.
      * 
      * @return A quote calculator.
      */
     C getQuoteCalculator();
 
     /** 
      * @brief Delivers information about the taskflow in general. 
      * 
      * The information should contain at least a description of the
      * task.
      *
      * @return A task info object.
      */
     TaskFlowInfo getInfo();
 
     /**
      * @brief Creates a new taskflow instance.
      *
      * The created task is registered in its factory.
      *
      * @return A taskflow object.
      */
     TaskFlow<O> create();
 
     /**
      * @brief Creates a new taskflow instance.
      *
      * The created task flow \bnot is registered in its factory.
      *
      * @return A taskflow object.
      */
     TaskFlow<O> createOrphan();
 
     /**
      * @brief Adds an orphan task flow to the facotry.
      *
      * @param taskflow The taskflow to add. Note the taskflow must have a unique id.
      *
      * @return \c true if the taskflow was successfully added. If the task is already registered this will fail.
      */
     boolean adopt( TaskFlow<O> taskflow );
 
     /**
      * @brief Finds an existing taskflow.
      *
      * @param id The id of the taskflow.
      * @return The taskflow or null if it doesn't exist.
      */
     TaskFlow<O> find( String id );
 
     /**
      * @brief Removes a taskflow.
      *
      * @param id The id of the taskflow.
      */
     void delete( String id );
 
     /**
      * @brief Delivers the class of the order type.
      *
      * @return The class of the taskflows order type.
      */
     Class<O> getOrderClass();
 
     /**
      * @brief Creates a task action.
      *
      * @return The newly created action.
      */
     TaskAction createAction();
 
     /**
      * @brief Delivers a list of keys of taskflows, this taskflow depends on.
      *
      * @return A list of taskflow keys or an empty list if it has no dependencies. Note it \b must \b not return null.
      */
     Iterable<String> depends();
 
     /**
      * Registers the TaskFlowType object of this factory.
      *
      * The TaskFlowType object is used to provide a configuration of the TaskFlow.
      *
      * @param session The session object which is necessary to access the database.
      */
     void registerType( Session session );
 
     DelegatingOrder<O> getOrderDelegate( O orq );
 
     /**
      * Addes an injector to a task factory.
      *
      * @param injector An injector which offers access to all members of the system-dictionary.
      */
     void setInjector( GNDMSInjector injector );
 }
