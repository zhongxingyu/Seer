 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jbpm.gd.jpdl.prefs;
 
 import java.io.File;
 
 import org.jboss.tools.jbpm.Constants;
 import org.jboss.tools.jbpm.preferences.PreferencesManager;
 import org.jbpm.gd.jpdl.Plugin;
 
 public class Jbpm3PreferencesManager extends PreferencesManager {
 		
	private static Jbpm3PreferencesManager INSTANCE = new Jbpm3PreferencesManager();
	
	public static PreferencesManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Jbpm3PreferencesManager();
		}
		return INSTANCE;
	}
	
 	protected void initialize() {
 		File installationsFile = 
 			Plugin.getDefault().getStateLocation().append("jbpm-installations.xml").toFile();
 		if (installationsFile.exists()) {
 			initializePreferredJbpmName();
 			loadInstallations(installationsFile);
 			saveInstallations();
 			loadInstallations();
 		}
 		installationsFile.delete();
		super.initialize();
 	}
 	
 	@SuppressWarnings("deprecation")
 	private void initializePreferredJbpmName() {
 		String preferredJbpmName = Plugin.getDefault().getPluginPreferences().getString(Constants.JBPM_NAME);
 		if (preferredJbpmName != null) {
 			setPreferredJbpmName(preferredJbpmName);
 		}
 	}
 	
 }
