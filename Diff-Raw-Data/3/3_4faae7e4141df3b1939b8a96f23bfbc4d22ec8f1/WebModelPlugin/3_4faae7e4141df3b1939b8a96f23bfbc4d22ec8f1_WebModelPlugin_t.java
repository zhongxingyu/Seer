 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web;
 
 import java.util.Iterator;
 import java.util.Properties;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.wst.validation.internal.plugin.ValidationPlugin;
 import org.jboss.tools.common.log.BaseUIPlugin;
 import org.jboss.tools.common.log.IPluginLog;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelConstants;
 import org.jboss.tools.common.model.XModelException;
 import org.jboss.tools.common.model.options.PreferenceModelUtilities;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.projecttemplates.ProjectTemplatesPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * 
  */
 public class WebModelPlugin extends BaseUIPlugin {
 
 	public static final String PLUGIN_ID = "org.jboss.tools.jst.web"; //$NON-NLS-1$
 
 	static WebModelPlugin instance;
 
 	public static WebModelPlugin getDefault() {
 		if(instance == null) {
 			Platform.getBundle(PLUGIN_ID);
 		}
 		return instance;
 	}
 
 	public static boolean isDebugEnabled() {
 		return getDefault().isDebugging(); 
 	}
 
 	public WebModelPlugin() {
 	    super();
 	    instance = this;
 	}
 
 	protected void initializeDefaultPluginPreferences() {
 		super.initializeDefaultPluginPreferences();
 		Properties p = new Properties();
 		p.setProperty(XModelConstants.WORKSPACE, EclipseResourceUtil.getInstallPath(this));
 		p.setProperty("initialModel", "true"); //$NON-NLS-1$ //$NON-NLS-2$
 		XModel initialModel = PreferenceModelUtilities.createPreferenceModel(p);
 		if (initialModel != null) {
 			Iterator preferences = WebPreference.getPreferenceList().iterator();
 			
 			while(preferences.hasNext()) {
 				Object preference = preferences.next();
 				if(preference instanceof WebPreference) {
 					try {
 						PreferenceModelUtilities.initPreferenceValue(initialModel,(WebPreference)preference);
 					} catch (XModelException e) {
 						ModelPlugin.getPluginLog().logError(e);
 					}
 				}
 			}
 			PreferenceModelUtilities.getPreferenceModel().save();
 		}
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 	}
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		ProjectTemplatesPlugin.getDefault();
 	}
 
 	static public ILaunchConfiguration findLaunchConfig(String name) throws CoreException {
 		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
 		ILaunchConfigurationType launchConfigurationType = launchManager.getLaunchConfigurationType( "org.eclipse.ant.AntLaunchConfigurationType" ); //$NON-NLS-1$
 		ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations( launchConfigurationType );
 
 		for (int i = 0; i < launchConfigurations.length; i++) { // can't believe there is no look up by name API
 			ILaunchConfiguration launchConfiguration = launchConfigurations[i];
 			if(launchConfiguration.getName().equals(name)) {
 				return launchConfiguration;
 			}
 		} 
 		return null;
 	}
 
 	public static String getTemplateStateLocation() {
 		return ProjectTemplatesPlugin.getTemplateStateLocation();
 	}
 
 	public static IPath getTemplateStatePath() {
 		return ProjectTemplatesPlugin.getTemplateStatePath();
 	}
 	
 	/**
 	 * @return IPluginLog object
 	 */
 	public static IPluginLog getPluginLog() {
 		return getDefault();
 	}
 
     private final static String JAVA_BUILDER_ID = "org.eclipse.jdt.core.javabuilder"; //$NON-NLS-1$
 
     /**
      * Adds the nature/builder to the project. Also adds WST validation builder. Sort Java, WST and the new builder in the following order: Java, WST, the new builder.
      * @param project
      * @param builderId
      * @param natureId
      * @throws CoreException
      */
     public static void addNatureToProjectWithValidationSupport(IProject project, String builderId, String natureId) throws CoreException {
    	EclipseResourceUtil.addNatureToProject(project, natureId);
 	    IProjectDescription desc = project.getDescription();
 	    ICommand[] existing = desc.getBuildSpec();
 	    boolean updated = false;
 	    int javaBuilderIndex = -1;
 	    ICommand javaBuilder = null;
 	    int wstValidationBuilderIndex = -1;
 	    ICommand wstValidationBuilder = null;
 	    int builderIndex = -1;
 	    ICommand builder = null;
     	for (int i = 0; i < existing.length; i++) {
     		if(JAVA_BUILDER_ID.equals(existing[i].getBuilderName())) {
     			javaBuilderIndex = i;
     			javaBuilder = existing[i];
     		} else if(ValidationPlugin.VALIDATION_BUILDER_ID.equals(existing[i].getBuilderName())) {
     			wstValidationBuilderIndex = i;
     			wstValidationBuilder = existing[i];
     		} else if(builderId.equals(existing[i].getBuilderName())) {
     			builderIndex = i;
     			builder = existing[i];
     		}
 		}
 
     	if(javaBuilderIndex==-1) {
 	    	getDefault().logError("Can't enable " + builderId + " support on the project " + project.getName() + " without Java builder.");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
 	    	return;
 	    }
 
 	    if(wstValidationBuilderIndex!=-1 && wstValidationBuilderIndex<javaBuilderIndex) {
 	    	existing[javaBuilderIndex] = wstValidationBuilder;
 	    	existing[wstValidationBuilderIndex] = javaBuilder;
 	    	int oldWstIndex = wstValidationBuilderIndex;
 	    	wstValidationBuilderIndex = javaBuilderIndex;
 	    	javaBuilderIndex = oldWstIndex;
 	    	updated = true;
 	    }
 
 	    if(builderIndex==-1) {
 	    	if(updated) {
 	    	    desc.setBuildSpec(existing);
 	    	    project.setDescription(desc, null);
 	    	    updated = false;
 	    	}
 		    desc = project.getDescription();
 		    existing = desc.getBuildSpec();
 	    	builderIndex = existing.length-1;
 	    	builder = getBuilder(project, builderId);
 	    }
 
 	    if(wstValidationBuilderIndex==-1) {
 	    	existing = appendBuilder(project, existing, ValidationPlugin.VALIDATION_BUILDER_ID);
 	    	wstValidationBuilderIndex = existing.length-1;
 	    	wstValidationBuilder = existing[wstValidationBuilderIndex];
 	    	updated = true;
 	    }
 
 	    if(wstValidationBuilderIndex<builderIndex) {
 	    	existing[wstValidationBuilderIndex] = builder;
 	    	existing[builderIndex] = wstValidationBuilder;
 	    	int oldWstIndex = wstValidationBuilderIndex;
 	    	wstValidationBuilderIndex = builderIndex;
 	    	builderIndex = oldWstIndex;
 	    	updated = true;
 	    }
 
 	    if(builderIndex<javaBuilderIndex) {
 	    	existing[javaBuilderIndex] = builder;
 	    	existing[builderIndex] = javaBuilder;
 	    	int oldJavaIndex = javaBuilderIndex;
 	    	javaBuilderIndex = builderIndex;
 	    	builderIndex = oldJavaIndex;
 	    	updated = true;
 	    }
 
 	    if(updated) {
 		    desc.setBuildSpec(existing);
 		    project.setDescription(desc, null);
 	    }
 	}
 
     private static ICommand getBuilder(IProject project, String builderId) throws CoreException {
 	    IProjectDescription desc = project.getDescription();
 	    ICommand[] existing = desc.getBuildSpec();
 	    for (ICommand command : existing) {
 	    	if(builderId.equals(command.getBuilderName())) {
 	    		return command;
 	    	}
 		}
 	    return null;
     }
 
     private static ICommand[] appendBuilder(IProject project, ICommand[] commands, String builderId) throws CoreException {
 	    ICommand[] cmds = new ICommand[commands.length + 1];
 	    ICommand newcmd = project.getDescription().newCommand();
 	    newcmd.setBuilderName(builderId);
 	    cmds[commands.length] = newcmd;
 	    System.arraycopy(commands, 0, cmds, 0, commands.length);
 	    return cmds;
     }
 
     public static boolean makeBuilderLast(IProject project, String builderId) throws CoreException {
 		IProjectDescription d = project.getDescription();
 		ICommand[] bs = d.getBuildSpec();
 		ICommand v = null;
 		boolean updated = false;
 		for (int i = 0; i < bs.length; i++) {
 			if(builderId.equals(bs[i].getBuilderName())) {
 				v = bs[i];
 			}
 			if(v != null) {
 				if(i + 1 < bs.length) {
 					bs[i] = bs[i + 1];
 					updated = true;
 				} else if(updated) {
 					bs[i] = v;
 				}
 			}
 		}
 		if(updated) {
 			d.setBuildSpec(bs);
 			project.setDescription(d, IProject.FORCE, new NullProgressMonitor());
 		}
 		return updated;
     }
 }
