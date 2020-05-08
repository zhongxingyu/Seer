 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.handlers.rename;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesDecorator;
 import org.eclipse.jubula.client.core.businessprocess.ProjectComponentNameMapper;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 
 
 /**
  * Handler for renaming a Component Name within a View.
  *
  * @author BREDEX GmbH
  * @created Mar 5, 2009
  */
 public class RenameComponentNameInViewHandler extends
         AbstractRenameComponentNameHandler {
     
     /**
      * {@inheritDoc}
      */
     public Object executeImpl(ExecutionEvent event) {
         IComponentNamePO compName = getSelectedComponentName();
         if (compName != null) {
            EntityManager renameSession = Persistor.instance().openSession();
             try {
                 ProjectComponentNameMapper compNameMapper =
                     new ProjectComponentNameMapper(
                             new ComponentNamesDecorator(
                                     renameSession), 
                                     GeneralStorage.getInstance().getProject());
                 String newName = getNewName(event, compNameMapper, compName);
                 if (newName != null) {
                     EntityTransaction tx = 
                         Persistor.instance().getTransaction(renameSession);
                     rename(compNameMapper, compName.getGuid(), newName);
                     Persistor.instance()
                         .commitTransaction(renameSession, tx);
                     compNameMapper.getCompNameCache()
                         .updateStandardMapperAndCleanup(
                                 GeneralStorage.getInstance().getProject()
                                     .getId());
                     IComponentNamePO eventCompName = 
                         ComponentNamesBP.getInstance().getCompNamePo(
                                 compName.getGuid());
                     DataEventDispatcher.getInstance().fireDataChangedListener(
                             eventCompName, DataState.Renamed, UpdateState.all);
                 }
             } catch (PMException e) {
                 PMExceptionHandler.handlePMExceptionForMasterSession(e);
             } catch (ProjectDeletedException e) {
                 PMExceptionHandler.handleGDProjectDeletedException();
            } finally {
                Persistor.instance().dropSession(renameSession);
             }
         }
         
         return null;
     }
     
 }
