 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.git.internal.preferences;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.fedoraproject.eclipse.packager.git.Activator;
 import org.fedoraproject.eclipse.packager.git.GitPreferencesConstants;
 import org.fedoraproject.eclipse.packager.git.GitUtils;
 
 /**
  * Class for initialization of Eclipse Fedora Packager preferences.
  */
 public class FedoraPackagerGitPreferenceInitializer extends AbstractPreferenceInitializer {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
 	 */
 	@Override
 	public void initializeDefaultPreferences() {
 		// set default preferences for this plug-in
		IEclipsePreferences node = new DefaultScope().getNode(Activator.PLUGIN_ID);
 		node.put(GitPreferencesConstants.PREF_CLONE_BASE_URL,
 				GitUtils.getDefaultGitBaseUrl());
 	}
 
 }
