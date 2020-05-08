 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2m.engine.sdk.tom.properties.listeners;
 
 import net.atos.optimus.m2m.engine.sdk.tom.TomPackage;
 import net.atos.optimus.m2m.engine.sdk.tom.Transformation;
 import net.atos.optimus.m2m.engine.sdk.tom.properties.Activator;
 import net.atos.optimus.m2m.engine.sdk.tom.properties.transformations.TransformationGenerationData;
 import net.atos.optimus.m2m.engine.sdk.tom.properties.transformations.TransformationGenerationJob;
 import net.atos.optimus.m2m.engine.sdk.tom.properties.transformations.TransformationGeneratorDialog;
 import net.atos.optimus.m2m.engine.sdk.tom.properties.zones.TransformationGeneratorSelectorZone;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
 import org.eclipse.gmf.runtime.emf.type.core.commands.SetValueCommand;
 import org.eclipse.gmf.runtime.emf.type.core.requests.SetRequest;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * A listener dedicated to launch the transformation generation process
  * 
  * @author tnachtergaele <nachtergaele.thomas@gmail.com>
  * 
  *
  */
 
 public class TransformationGeneratorListener implements SelectionListener {
 
 	/** The listened text zone */
 	protected TransformationGeneratorSelectorZone textZone;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param textZone
 	 *            the listened text zone.
 	 */
 	public TransformationGeneratorListener(TransformationGeneratorSelectorZone textZone) {
 		this.textZone = textZone;
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if (!(this.textZone.giveEObject() instanceof Transformation)) {
 			return;
 		}
 
 		Transformation transformation = (Transformation) this.textZone.giveEObject();
 
 		// Retrieve the java project
		IProject project = WorkspaceSynchronizer.getFile(transformation.eResource()).getProject();
 		IJavaProject javaProject = JavaCore.create(project);
 
 		// Create a new shell
 		Shell shell = new Shell(Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault(),
 				SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
 
 		// Initialize the transformation data
 		TransformationGenerationData transformationData = new TransformationGenerationData();
 		String transformationName = transformation.getName();
 		if (transformationName != null) {
 			transformationData.setTrn(transformationName);
 			transformationData.setFactory(transformationName + TransformationGenerationData.FACTORYDEFAULT);
 		}
 
 		String transformationFactoryFullName = transformation.getFactory();
 		if (transformationFactoryFullName != null
 				&& transformationFactoryFullName.endsWith(TransformationGenerationData.FACTORYDEFAULT)) {
 			int indexEndPackage = transformationFactoryFullName.lastIndexOf('.');
 			if (indexEndPackage != -1) {
 				String packageName = transformationFactoryFullName.substring(0, indexEndPackage);
 				String factoryName = transformationFactoryFullName.substring(indexEndPackage + 1);
 				transformationData.setPackage(packageName);
 				transformationData.setFactory(factoryName);
 				if (transformationData.getTrn() == "") {
 					int indexFactory = factoryName.lastIndexOf(TransformationGenerationData.FACTORYDEFAULT);
 					transformationData.setTrn(factoryName.substring(0, indexFactory));
 				}
 			}
 		}
 
 		TransformationGeneratorDialog dialog = new TransformationGeneratorDialog(shell, javaProject, transformationData);
 		if (dialog.open() == Window.OK) {
 			TransformationGenerationJob job = new TransformationGenerationJob(transformationData);
 			job.setProject(javaProject);
 			job.schedule();
 		}
 
 		Object factoryFullName = transformationData.getPackage() + "." + transformationData.getFactory();
 		EStructuralFeature factoryFeature = this.textZone.getAssociatedFeature();
 
 		/* Set the value in the model */
 		SetRequest request = new SetRequest(this.textZone.giveEditingDomain(), this.textZone.giveEObject(),
 				factoryFeature, factoryFullName);
 		SetValueCommand command = new SetValueCommand(request);
 		try {
 			command.execute(new NullProgressMonitor(), null);
 		} catch (ExecutionException e1) {
 			Activator
 					.getDefault()
 					.getLog()
 					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Listener creation fail on the feature : "
 							+ factoryFeature.getClass().getCanonicalName(), e1));
 		}
 
 		EStructuralFeature nameFeature = TomPackage.eINSTANCE.getTransformationReference_Name();
 
 		/* Set the value in the model */
 		request = new SetRequest(this.textZone.giveEditingDomain(), this.textZone.giveEObject(), nameFeature,
 				transformationData.getTrn());
 		command = new SetValueCommand(request);
 		try {
 			command.execute(new NullProgressMonitor(), null);
 		} catch (ExecutionException e1) {
 			Activator
 					.getDefault()
 					.getLog()
 					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Listener creation fail on the feature : "
 							+ factoryFeature.getClass().getCanonicalName(), e1));
 		}
 
 		this.textZone.refreshZoneAndDiagram();
 
 	}
 
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 	}
 
 }
