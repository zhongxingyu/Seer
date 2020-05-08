 /*******************************************************************************
  * Copyright (c) 2005, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.core.environment;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.SimplePriorityClassDLTKExtensionManager;
 import org.eclipse.dltk.core.internal.environment.LocalEnvironment;
 
 public final class EnvironmentManager {
 	private static final String ENVIRONMENT_EXTENSION = DLTKCore.PLUGIN_ID
 			+ ".environment"; //$NON-NLS-1$
 	private static SimplePriorityClassDLTKExtensionManager manager = new SimplePriorityClassDLTKExtensionManager(
 			ENVIRONMENT_EXTENSION, "id"); //$NON-NLS-1$
 
 	private static ListenerList listeners = new ListenerList();
 
 	private EnvironmentManager() {
 	}
 
 	public static IEnvironment getEnvironment(IModelElement element) {
		if( element == null ) {
			return null;
		}
		IScriptProject scriptProject = element.getScriptProject();
		if( scriptProject == null ) {
			return null;
		}
		IProject project = scriptProject.getProject();
 		if (project == null)
 			return null;
 		
 		return getEnvironment(project);
 	}
 
 	public static IEnvironment getEnvironment(IProject project) {
 		IEnvironment[] environments = getEnvironments();
 		for (int i = 0; i < environments.length; i++) {
 			if( environments[i].hasProject(project)) {
 				return environments[i];
 			}
 		}
 		return null;
 	}
 
 	public static IEnvironment[] getEnvironments() {
 		List envList = new LinkedList();
 		Object[] objects = manager.getObjects();
 		for (int i = 0; i < objects.length; i++) {
 			IEnvironmentProvider provider = (IEnvironmentProvider) objects[i];
 			envList.addAll(Arrays.asList(provider.getEnvironments()));
 		}
 		IEnvironment[] environments = new IEnvironment[envList.size()];
 		envList.toArray(environments);
 		return environments;
 	}
 
 	public static boolean isLocal(IEnvironment env) {
 		return LocalEnvironment.ENVIRONMENT_ID.equals(env.getId());
 	}
 
 	public static IEnvironment getEnvironmentById(String envId) {
 		Object[] objects = manager.getObjects();
 		for (int i = 0; i < objects.length; i++) {
 			IEnvironmentProvider provider = (IEnvironmentProvider) objects[i];
 			IEnvironment env = provider.getEnvironment(envId);
 			if (env != null) {
 				return env;
 			}
 		}
 		return null;
 	}
 
 	public static void addEnvironmentChangedListener(
 			IEnvironmentChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	public static void removeEnvironmentChangedListener(
 			IEnvironmentChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	public static void environmentAdded(IEnvironment environment) {
 		Object[] environmentListeners = listeners.getListeners();
 		for (int i = 0; i < environmentListeners.length; i++) {
 			IEnvironmentChangedListener listener = (IEnvironmentChangedListener) environmentListeners[i];
 			listener.environmentAdded(environment);
 		}
 	}
 	public static void environmentRemoved(IEnvironment environment) {
 		Object[] environmentListeners = listeners.getListeners();
 		for (int i = 0; i < environmentListeners.length; i++) {
 			IEnvironmentChangedListener listener = (IEnvironmentChangedListener) environmentListeners[i];
 			listener.environmentRemoved(environment);
 		}
 	}
 	public static void environmentChanged(IEnvironment environment) {
 		Object[] environmentListeners = listeners.getListeners();
 		for (int i = 0; i < environmentListeners.length; i++) {
 			IEnvironmentChangedListener listener = (IEnvironmentChangedListener) environmentListeners[i];
 			listener.environmentChanged(environment);
 		}
 	}
 
 	public static IEnvironment getLocalEnvironment() {
 		return getEnvironmentById(LocalEnvironment.ENVIRONMENT_ID);
 	}
 }
