 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Xavier Coulon - Initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder;
 
 import static org.jboss.tools.ws.jaxrs.core.internal.metamodel.builder.JaxrsMetamodelBuilder.SCALE;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.jboss.tools.ws.jaxrs.core.internal.utils.Logger;
 import org.jboss.tools.ws.jaxrs.core.metamodel.JaxrsMetamodelDelta;
 
 /** @author xcoulon */
 public class JavaElementChangedBuildJob extends Job {
 
 	private final JavaElementChangedProcessor javaElementChangedProcessor = new JavaElementChangedProcessor();
 
 	private final JaxrsMetamodelChangedProcessor jaxrsElementChangedProcessor = new JaxrsMetamodelChangedProcessor();
 
 	private final ElementChangedEvent event;
 	
 	public JavaElementChangedBuildJob(final ElementChangedEvent event) {
 		super("Incremental JAX-RS Metamodel build..."); //$NON-NLS-1$
 		this.event = event;
 		Logger.debug("Initiating an incremental JAX-RS Metamodel build after " + event); //$NON-NLS-1$
 	}
 	
 	public void execute() {
 		run(new NullProgressMonitor());
 	}
 	
 	@Override
 	protected IStatus run(final IProgressMonitor progressMonitor) {
 		try {
 			progressMonitor.beginTask("Building JAX-RS Metamodel", 4 * SCALE);
 			progressMonitor.worked(SCALE);
			Logger.debug("Building JAX-RS Metamodel after Java element changed...");
 			if (progressMonitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			// scan and filter delta, retrieve a list of java changes
 			final List<JavaElementDelta> affectedJavaElements = new JavaElementDeltaScanner().scanAndFilterEvent(event,
 					new SubProgressMonitor(progressMonitor, SCALE));
 			if (progressMonitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			final List<JaxrsMetamodelDelta> affectedMetamodels = javaElementChangedProcessor.processAffectedJavaElements(affectedJavaElements,
 					new SubProgressMonitor(progressMonitor, SCALE));
 			if (progressMonitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			jaxrsElementChangedProcessor.processAffectedMetamodels(affectedMetamodels, new SubProgressMonitor(
 					progressMonitor, SCALE));
 			if (progressMonitor.isCanceled()) {
 				return Status.CANCEL_STATUS;
 			}
 			new JaxrsElementChangedPublisher().publish(affectedMetamodels, new SubProgressMonitor(progressMonitor,
 					SCALE));
 		} catch (Throwable e) {
 			Logger.error("Failed to build or refresh the JAX-RS metamodel", e);
 		} finally {
 			progressMonitor.done();
 		}
 		return Status.OK_STATUS;
 	}
 }
