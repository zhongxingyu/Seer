 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  * All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  *
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.bpmn2.modeler.ui.editor;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.ui.editor.DefaultPersistencyBehavior;
 import org.eclipse.graphiti.ui.editor.DiagramBehavior;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.widgets.Display;
 
 public class BPMN2PersistencyBehavior extends DefaultPersistencyBehavior {
 
 	BPMN2Editor editor;
 	
 	public BPMN2PersistencyBehavior(DiagramBehavior diagramBehavior) {
 		super(diagramBehavior);
 		editor = (BPMN2Editor)diagramBehavior.getDiagramContainer();
 	}
     @Override
     public Diagram loadDiagram(URI modelUri) {
     	Diagram diagram = super.loadDiagram(modelUri);
 
     	return diagram;
     }
     
 	protected IRunnableWithProgress createOperation(final Set<Resource> savedResources,
 			final Map<Resource, Map<?, ?>> saveOptions) {
 		// Do the work within an operation because this is a long running
 		// activity that modifies the workbench.
 		final IRunnableWithProgress operation = new IRunnableWithProgress() {
 			// This is the method that gets invoked when the operation runs.
 			public void run(IProgressMonitor monitor) {
 				// Save the resources to the file system.
 				try {
 					savedResources.addAll(save(diagramBehavior.getEditingDomain(), saveOptions, monitor));
				} catch (final WrappedException e) {
 					final String msg = e.getMessage().replaceAll("\tat .*", "").replaceFirst(".*Exception: ","").trim();
 					Display.getDefault().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							MessageDialog.openError(Display.getDefault().getActiveShell(), "Can not save file", msg);
 						}
 					});
 					monitor.setCanceled(true);
 					throw e;
 				}
 			}
 		};
 		return operation;
 	}
 
 }
