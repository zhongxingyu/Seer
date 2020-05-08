 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.xmof.debug.launch;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.modelexecution.xmof.configuration.ConfigurationObjectMap;
 import org.modelexecution.xmof.configuration.profile.ProfileApplicationGenerator;
 import org.modelexecution.xmof.configuration.profile.XMOFConfigurationProfilePlugin;
 import org.modelexecution.xmof.debug.XMOFDebugPlugin;
 import org.modelexecution.xmof.debug.internal.process.InternalXMOFProcess;
 import org.modelexecution.xmof.debug.internal.process.InternalXMOFProcess.Mode;
 import org.modelexecution.xmof.vm.XMOFBasedModel;
 import org.modelversioning.emfprofile.Profile;
 import org.modelversioning.emfprofile.registry.IProfileRegistry;
 
 public class XMOFLaunchDelegate extends LaunchConfigurationDelegate {
 
 	private static final String XMOF_EXEC_LABEL = "xMOF Execution Process";
 	private ResourceSet resourceSet;
 	private ConfigurationObjectMap configurationMap;
 
 	@Override
 	public void launch(ILaunchConfiguration configuration, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 
 		resourceSet = new ResourceSetImpl();
 
 		XMOFBasedModel model = getXMOFBasedModel(configuration);
 		InternalXMOFProcess xMOFProcess = new InternalXMOFProcess(model,
 				getProcessMode(mode));
 
 		installConfigurationProfileApplicationGenerator(configuration,
 				xMOFProcess);
 
 		IProcess process = DebugPlugin.newProcess(launch, xMOFProcess,
 				XMOF_EXEC_LABEL);
 
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
 			// TODO set debug target
 			System.out.println("Should debug:" + process);
 		}
 	}
 
 	private XMOFBasedModel getXMOFBasedModel(ILaunchConfiguration configuration)
 			throws CoreException {
 
 		Collection<EObject> inputModelElements = loadInputModelElements(configuration);
 
 		if (useConfigurationMetamodel(configuration)) {
 			String confMetamodelPath = getConfigurationMetamodelPath(configuration);
 			Collection<EPackage> configurationPackages = loadConfigurationMetamodel(confMetamodelPath);
 			configurationMap = new ConfigurationObjectMap(inputModelElements,
 					configurationPackages);
 			return new XMOFBasedModel(
 					configurationMap.getConfigurationObjects());
 		} else {
 			return new XMOFBasedModel(inputModelElements);
 		}
 	}
 
 	private boolean useConfigurationMetamodel(ILaunchConfiguration configuration)
 			throws CoreException {
 		return configuration.getAttribute(
 				XMOFDebugPlugin.ATT_USE_CONFIGURATION_METAMODEL, false);
 	}
 
 	private String getConfigurationMetamodelPath(
 			ILaunchConfiguration configuration) throws CoreException {
 		return configuration
 				.getAttribute(XMOFDebugPlugin.ATT_CONFIGURATION_METAMODEL_PATH,
 						(String) null);
 	}
 
 	private Collection<EPackage> loadConfigurationMetamodel(
 			String confMetamodelPath) {
 		Resource resource = loadResource(confMetamodelPath);
 		Collection<EPackage> confMMPackages = new ArrayList<EPackage>();
 		for (EObject eObject : resource.getContents()) {
 			if (eObject instanceof EPackage) {
 				confMMPackages.add((EPackage) eObject);
 			}
 		}
 		return confMMPackages;
 	}
 
 	private Resource loadResource(String path) {
 		return resourceSet.getResource(
 				URI.createPlatformResourceURI(path, true), true);
 	}
 
 	private Collection<EObject> loadInputModelElements(
 			ILaunchConfiguration configuration) throws CoreException {
 		String modelPath = getModelPath(configuration);
 		Collection<EObject> inputModelElements = getInputModelElements(modelPath);
 		return inputModelElements;
 	}
 
 	private String getModelPath(ILaunchConfiguration configuration)
 			throws CoreException {
 		return configuration.getAttribute(XMOFDebugPlugin.ATT_MODEL_PATH,
 				(String) null);
 	}
 
 	private Collection<EObject> getInputModelElements(String modelPath) {
 		Resource resource = loadResource(modelPath);
 		return resource.getContents();
 	}
 
 	private Mode getProcessMode(String mode) {
 		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
 			return Mode.DEBUG;
 		} else {
 			return Mode.RUN;
 		}
 	}
 
 	private void installConfigurationProfileApplicationGenerator(
			ILaunchConfiguration configuration, InternalXMOFProcess xMOFProcess) {
 		Collection<Profile> configurationProfiles = getConfigurationProfile(
 				configuration, xMOFProcess.getModel());
 		if (configurationProfiles.size() > 0 && configurationMap != null) {
 			Resource profileApplicationResource = loadConfigurationProfileApplicationResource(
 					configuration, xMOFProcess.getModel());
 			ProfileApplicationGenerator generator = new ProfileApplicationGenerator(
 					xMOFProcess.getModel(), configurationProfiles,
 					configurationMap);
 			generator.setProfileApplicationResource(profileApplicationResource);
 			xMOFProcess.getVirtualMachine()
 					.addVirtualMachineListener(generator);
 		}
 	}
 
 	private Collection<Profile> getConfigurationProfile(
 			ILaunchConfiguration configuration, XMOFBasedModel model) {
 		// TODO decide based on a user selection in the launch config UI and
 		// don't take all
 		Collection<Profile> profiles = IProfileRegistry.eINSTANCE
 				.getRegisteredProfiles();
 		Collection<Profile> configProfiles = new ArrayList<Profile>();
 		for (Profile profile : profiles) {
 			if (isConfigurationProfile(profile, model)) {
 				configProfiles.add(profile);
 			}
 		}
 		return configProfiles;
 	}
 
 	private boolean isConfigurationProfile(Profile profile, XMOFBasedModel model) {
 		for (EPackage ePackage : model.getMetamodelPackages()) {
 			if (isConfigurationProfileForPackage(profile, ePackage)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isConfigurationProfileForPackage(Profile profile,
 			EPackage ePackage) {
 		// TODO fix this preliminary solution
 		// we should check the actual stereotypes
 		return profile.getName().equals(ePackage.getName() + "Profile");
 	}
 
 	private Resource loadConfigurationProfileApplicationResource(
			ILaunchConfiguration configuration, XMOFBasedModel model) {
 		URI uri = getConfigurationProfileApplicationURI(configuration);
 		if (uri == null) {
			uri = getConfigurationProfileApplicationURI(model);
 		}
 		return resourceSet.createResource(uri);
 	}
 
 	private URI getConfigurationProfileApplicationURI(
 			ILaunchConfiguration configuration) {
 		// TODO load from configuration once we have it there
 		return null;
 	}
 
	private URI getConfigurationProfileApplicationURI(XMOFBasedModel model) {
		EPackage ePackage = model.getMetamodelPackages().get(0);
		URI metamodelURI = ePackage.eResource().getURI();
		return URI.createURI("platform:/resource/" //$NON-NLS-1$
				+ metamodelURI.toPlatformString(false)
 				+ XMOFConfigurationProfilePlugin.RUNTIME_EMFPROFILE_EXTENSION);
 	}
 
 	@Override
 	public boolean buildForLaunch(ILaunchConfiguration configuration,
 			String mode, IProgressMonitor monitor) throws CoreException {
 		return false;
 	}
 
 }
