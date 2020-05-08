 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.j2ee.internal.ui.J2EEModuleDependenciesPropertyPage.ClasspathEntryProxy;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.ui.Messages;
 import org.eclipse.wst.common.componentcore.ui.ModuleCoreUIPlugin;
 import org.eclipse.wst.common.componentcore.ui.internal.propertypage.verifier.DeploymentAssemblyVerifierHelper;
 import org.eclipse.wst.common.componentcore.ui.internal.propertypage.verifier.IDeploymentAssemblyVerifier;
 import org.eclipse.wst.common.componentcore.ui.internal.propertypage.verifier.VerifierRegistryReader;
 import org.eclipse.wst.common.componentcore.ui.propertypage.AddModuleDependenciesPropertiesPage.ComponentResourceProxy;
 import org.eclipse.wst.server.core.IRuntime;
 
 public class J2EEModuleDeploymentAssemblyVerifierHelper {
 	
 	
 	/**
 	 * This method will use the facet framework to find all registered verifiers based on installed facets and runtimes.
 	 * All the verifiers will have an opportunity to verify and return status on the proposed component mappings
 	 * @param component
 	 * @param runtime
 	 * @param resourceMappingsChanged 
 	 * @param resourceMappings 
 	 * @param currentReferences 
 	 * @param currentClasspathEntries
 	 * @return IStatus
 	 */
 	public static IStatus verify(IVirtualComponent component,IRuntime runtime, ArrayList<IVirtualReference> currentReferences, ArrayList<ComponentResourceProxy> resourceMappings, boolean resourceMappingsChanged, List<ClasspathEntryProxy> currentClasspathEntries) {
 		IProject project = component.getProject();
 		
 		List verifiers = DeploymentAssemblyVerifierHelper.collectAllVerifiers(project,runtime);
 		if (verifiers.isEmpty()) return Status.OK_STATUS;
 		MultiStatus masterStatus = new MultiStatus(ModuleCoreUIPlugin.PLUGIN_ID, IStatus.OK, Messages.DeploymentAssemblyVerifierHelper_0,null); 
 		
 		for (int i = 0; i < verifiers.size(); i++) {
 			if (!(verifiers.get(i) instanceof IConfigurationElement))
 				continue;
 			IDeploymentAssemblyVerifier verifier = null;
 			try {
 				verifier = (IDeploymentAssemblyVerifier) ((IConfigurationElement) verifiers.get(i)).createExecutableExtension(VerifierRegistryReader.VERIFIER_CLASS);
 				J2EEModuleDeploymentAssemblyVerifierData data = new J2EEModuleDeploymentAssemblyVerifierData(component, runtime,currentReferences,resourceMappings,resourceMappingsChanged,currentClasspathEntries );
 				IStatus verifyStatus = verifier.verify(data);
				masterStatus.add(verifyStatus);
 			} catch (Exception e) {
 				ModuleCoreUIPlugin.log(e);
 				continue;
 			}
 			
 		}
 		return masterStatus;
 	}	
 }
