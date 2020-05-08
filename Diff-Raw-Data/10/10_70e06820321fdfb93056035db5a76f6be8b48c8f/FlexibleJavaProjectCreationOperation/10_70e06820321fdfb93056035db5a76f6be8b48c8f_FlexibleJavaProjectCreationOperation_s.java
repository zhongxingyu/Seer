 /*******************************************************************************
  * Copyright (c) 2003, 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.j2ee.project.datamodel.properties.IFlexibleJavaProjectCreationDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 
 public class FlexibleJavaProjectCreationOperation extends FlexibleProjectCreationOp implements IFlexibleJavaProjectCreationDataModelProperties{
 
     public FlexibleJavaProjectCreationOperation(IDataModel model) {
         super(model);
     }
     private void addServerTarget(IProgressMonitor monitor)  throws CoreException, InvocationTargetException, InterruptedException, ExecutionException{
         IDataModel serverModel = model.getNestedModel(NESTED_MODEL_SERVER_TARGET);
         IDataModelOperation op = (IDataModelOperation)serverModel.getDefaultOperation();
         op.execute(monitor, null);
     }
     
     public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
         IStatus status = super.execute(monitor, info);
         try {
             addServerTarget(monitor);
         } catch (ExecutionException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (CoreException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return status;
     }
 }
