 /*
  * jPOS Project [http://jpos.org]
  * Copyright (C) 2000-2010 Alejandro P. Revilla
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.core;
 
 import java.util.List;
 
 import org.jpos.transaction.Context;
 import org.jpos.util.Log;
 
/**An extension of the org.jpos.transaction.Context class with some helpers
 * for PM.*/
 public class PMContext extends Context {
 
     public static final String PM_ERRORS = "PM_ERRORS";
 
     /**
      * @return the errors
      */
     public List<PMMessage> getErrors() {
         return (List<PMMessage>) get(PM_ERRORS);
     }
 
     /**
      * @param errors the errors to set
      */
     public void setErrors(List<PMMessage> errors) {
         put(PM_ERRORS, errors);
     }
 
     /**
      * Getter for PM singleton
      * @return
      */
     public PresentationManager getPresentationManager() {
         return PresentationManager.pm;
     }
 
     /**
      * Return the persistance manager of the PM
      * @return PersistenceManager
      */
     public PersistenceManager getPersistanceManager() {
         return getPresentationManager().getPersistenceManager();
     }
 
     /**
      * Return the PM log
      * @return
      */
     public Log getLog() {
         return getPresentationManager().getLog();
     }
 
     /**
      * @param entityContainer the entity_container to set
      */
     public void setEntityContainer(EntityContainer entityContainer) {
         put(PMCoreObject.PM_ENTITY_CONTAINER, entityContainer);
     }
 
     /**
      * @return the entity_container
      * @throws PMException
      */
     public EntityContainer getEntityContainer() throws PMException {
         EntityContainer entityContainer = (EntityContainer) get(PMCoreObject.PM_ENTITY_CONTAINER);
         if (entityContainer == null) {
             throw new PMException("pm_core.entity.not.found");
         }
         return entityContainer;
     }
 
     /**
      * Returns the entity container 
      * @param ignorenull If true, does not throws an exception on missing container
      * @return The container
      * @throws PMException
      */
     public EntityContainer getEntityContainer(boolean ignorenull) throws PMException {
         EntityContainer entityContainer = (EntityContainer) get(PMCoreObject.PM_ENTITY_CONTAINER);
         if (ignorenull) {
             return entityContainer;
         }
         if (entityContainer == null) {
             throw new PMException("pm_core.entity.not.found");
         }
         return entityContainer;
     }
 
     /**
      * Informs if there is a container in the context
      *
      * @return true if there is a container in the context
      */
     public boolean hasEntityContainer() {
         EntityContainer entityContainer = (EntityContainer) get(PMCoreObject.PM_ENTITY_CONTAINER);
         return entityContainer != null;
     }
 
     /**
      * @param operation the operation to set
      */
     public void setOperation(Operation operation) {
         put(PMCoreObject.PM_OPERATION, operation);
     }
 
     /**
      * @return the operation
      */
     public Operation getOperation() {
         return (Operation) get(PMCoreObject.PM_OPERATION);
     }
 
     /**
      * Return the entity in the container
      * @return The entity
      * @throws PMException
      */
     public Entity getEntity() throws PMException {
         return getEntityContainer().getEntity();
     }
 
     /**
      * Return the list of the container
      * @return The list
      * @throws PMException
      */
     public PaginatedList getList() throws PMException {
         return getEntityContainer().getList();
     }
 
     /**
      * Return the selected item of the container
      * @return The EntityInstanceWrapper
      * @throws PMException
      */
     public EntityInstanceWrapper getSelected() throws PMException {
         return getEntityContainer().getSelected();
     }
 
     /**
      * Indicate if there is a container with an entity
      * 
      * @return
      */
     public boolean hasEntity() {
         try {
             return (hasEntityContainer() && getEntityContainer().getEntity() != null);
         } catch (PMException e) {
             return false;
         }
     }
 }
