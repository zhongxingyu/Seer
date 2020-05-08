 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.fumldebug.debugger.uml2.provider;
 
 import java.util.Collection;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.modelexecution.fuml.convert.ConverterRegistry;
 import org.modelexecution.fuml.convert.IConversionResult;
 import org.modelexecution.fuml.convert.IConverter;
 import org.modelexecution.fumldebug.debugger.provider.IActivityProvider;
 
 import fUML.Syntax.Activities.IntermediateActivities.Activity;
 import fUML.Syntax.Classes.Kernel.NamedElement;
 
 public class UML2ActivityProvider implements IActivityProvider {
 
 	private final ConverterRegistry converterRegistry = ConverterRegistry
 			.getInstance();
 
 	private ResourceSet resourceSet;
 	private Resource emfResource;
 	private IFile iFile;
 
 	private IConversionResult conversionResult;
 
 	public UML2ActivityProvider(IResource iResource) {
		if (iResource instanceof IFile && iResource.exists()) {
 			throw new IllegalArgumentException(
 					"Resource is not a valid file or does not exist.");
 		}
 
 		this.iFile = (IFile) iResource;
 		initializeResourceSet();
 		initializeResource();
 	}
 
 	private void initializeResourceSet() {
 		resourceSet = new ResourceSetImpl();
 	}
 
 	private void initializeResource() {
 		loadResource();
 		convertResource();
 	}
 
 	private void loadResource() {
 		emfResource = loadResource(iFile);
 	}
 
 	private Resource loadResource(IFile file) {
 		return resourceSet.getResource(createURI(file), true);
 	}
 
 	private URI createURI(IFile file) {
 		return URI.createURI("platform:/resource/" //$NON-NLS-1$
 				+ file.getProject().getName() + "/" //$NON-NLS-1$
 				+ file.getProjectRelativePath());
 	}
 
 	private void convertResource() {
 		IConverter converter = converterRegistry.getConverter(emfResource);
 		conversionResult = converter.convert(emfResource);
 	}
 
 	@Override
 	public IResource getResource() {
 		return iFile;
 	}
 
 	@Override
 	public Collection<Activity> getActivities() {
 		return conversionResult.getActivities();
 	}
 
 	@Override
 	public Activity getActivity(String name) {
 		for (Activity activity : conversionResult.getAllActivities()) {
 			if (equalsName(name, activity)) {
 				return activity;
 			}
 		}
 		return null;
 	}
 
 	private boolean equalsName(String name, Activity activity) {
 		return name.equals(activity.name)
 				|| name.equals(activity.qualifiedName);
 	}
 
 	@Override
 	public String getSourceFileName(NamedElement namedElement) {
 		return iFile.getName();
 	}
 
 	@Override
 	public void unload() {
 		emfResource.unload();
 	}
 
 }
