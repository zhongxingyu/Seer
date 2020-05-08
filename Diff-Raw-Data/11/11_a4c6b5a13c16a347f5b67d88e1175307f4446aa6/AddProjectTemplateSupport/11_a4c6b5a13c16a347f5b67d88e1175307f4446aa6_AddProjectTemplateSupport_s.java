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
 package org.jboss.tools.jst.web.project.handlers;
 
 import java.io.File;
 import java.util.*;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.osgi.util.NLS;
 
 import org.jboss.tools.common.meta.action.XActionInvoker;
 import org.jboss.tools.common.meta.action.impl.*;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelException;
 import org.jboss.tools.common.model.options.PreferenceModelUtilities;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.project.IModelNature;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.messages.xpl.WebUIMessages;
 import org.jboss.tools.jst.web.project.helpers.AbstractWebProjectTemplate;
 
 public class AddProjectTemplateSupport extends MultistepWizardSupport {
 	
 	public static String run(AbstractWebProjectTemplate template, String entity, String version) {
 		String actionPath = "CreateActions.CreateProjectTemplate"; //$NON-NLS-1$
 		Properties p = new Properties();
 		p.put("version", version); //$NON-NLS-1$
 		return run(template, entity, actionPath, p);
 	}
 
 	public static String run(AbstractWebProjectTemplate template, String entity, IProject project) {
 		String actionPath = "CreateActions.CreateProjectTemplate2"; //$NON-NLS-1$
 		Properties p = new Properties();
 		p.put("project", project); //$NON-NLS-1$
 		return run(template, entity, actionPath, p);
 	}
 
 	static String run(AbstractWebProjectTemplate template, String entity, String actionPath, Properties p) {
 		XModel model = PreferenceModelUtilities.getPreferenceModel();
 		p.put("template", template); //$NON-NLS-1$
 		XActionInvoker.invoke(entity, actionPath, model.getRoot(), p);
 		return p.getProperty("name"); //$NON-NLS-1$
 	}
 
 	static int NAME_STEP = 0;
 	static int RESOURCES_STEP = 1;
 	static int VELOCITY_STEP = 2;
 	static int PROPERTIES_STEP = 3;
 	
 	AddProjectTemplateResourcesStep resourcesStep;
 	AddProjectTemplateVelocityStep velocityStep;
 	AddProjectTemplatePropertiesStep propertiesStep;
 	
 	AbstractWebProjectTemplate template;
 	Set<String> templates;
 	String version;
 	IProject presetProject;
 	Map<String,XModel> models;
 	
 	protected MultistepWizardStep[] createSteps() {
 		return new MultistepWizardStep[]{
 			new AddProjectTemplateNameStep(),            //name
 			resourcesStep = new AddProjectTemplateResourcesStep(),  //resources
 			velocityStep = new AddProjectTemplateVelocityStep(),    //velocity
 			propertiesStep = new AddProjectTemplatePropertiesStep() //properties (velocity)
 		};		
 	}
 
 	public void reset() {
 		initSteps();
 		template = (AbstractWebProjectTemplate)getProperties().get("template"); //$NON-NLS-1$
 		setVersion(getProperties().getProperty("version")); //$NON-NLS-1$
 		presetProject = (IProject)getProperties().get("project"); //$NON-NLS-1$
 		if(presetProject == null) {
 			setProjectList();
 		} else {
 			setAttributeValue(NAME_STEP, "name", presetProject.getName()); //$NON-NLS-1$
 			setVersionList();
 			try {
 				prepareStep(RESOURCES_STEP);
 			} catch (XModelException e) {
 				WebModelPlugin.getPluginLog().logError(e);
 			}
 		}
 	}
 
     public String getSubtitle() {
     	int id = getStepId();
     	return id == NAME_STEP ? WebUIMessages.DEFINE_COMMON_TEMPLATE_PROPERTIES :
     		   id == RESOURCES_STEP ? WebUIMessages.SELECT_FOLDERS_AND_FILES :
     		   id == VELOCITY_STEP ? WebUIMessages.SELECT_FILES_THAT_ARE_VELOCITY_TEMPLATES : 
        		   id == PROPERTIES_STEP ? WebUIMessages.SET_VELOCITY_PROPERTIES : 
     			   "";  //$NON-NLS-1$
     }
 
 	void setVersion(String v) {
 		version = v;
 		templates = new HashSet<String>();
 		if(v != null) {
 			String[] ts = template.getTemplateList(version);
 			for (int i = 0; i < ts.length; i++) templates.add(ts[i]);
 		}
 	}
 	
 	void setVersionList() {
 		String[] s = template.getVersionList();
 		setValueList(NAME_STEP, "implementation", s); //$NON-NLS-1$
 		if(s.length != 0) {
 			setAttributeValue(NAME_STEP, "implementation", s[0]); //$NON-NLS-1$
 			setVersion(s[0]);
 		}
 	}
 	
 	public IProject getSelectedProject() {
 		if(presetProject != null) return presetProject;
 		String project = getAttributeValue(0, "project"); //$NON-NLS-1$
 		if(project == null || project.length() == 0) return null;
 		return ModelPlugin.getWorkspace().getRoot().getProject(project);
 	}
 	
 	void setProjectList() {
 		String nature = getEntityData()[0].getModelEntity().getAttribute("nature").getDefaultValue(); //$NON-NLS-1$
 		IProject[] ps = ModelPlugin.getWorkspace().getRoot().getProjects();
 		models = new TreeMap<String,XModel>();
 		for (int i = 0; i < ps.length; i++) {
 			if(!ps[i].isOpen()) continue;
 			IModelNature n = EclipseResourceUtil.getModelNature(ps[i], nature);
 			if(n != null) models.put(ps[i].getName(), n.getModel());
 		}
 		String[] pl = (String[])models.keySet().toArray(new String[0]);
 		setValueList(0, "project", pl); //$NON-NLS-1$
 		if(pl.length != 0) {
 			setAttributeValue(0, "project", pl[0]); //$NON-NLS-1$
 			setAttributeValue(0, "name", pl[0]); //$NON-NLS-1$
 		}
 	}
 
 	protected void prepareStep(int nextStep) throws XModelException {
 		if(nextStep == RESOURCES_STEP) {
 			getProperties().put("ResourcesStep", steps[RESOURCES_STEP]); //$NON-NLS-1$
 			resourcesStep.init();			
 			velocityStep.init();
 		} else if(nextStep == VELOCITY_STEP) {
 			velocityStep.init();
 		}
 	}	
 
 	protected void execute() throws XModelException {
 		Properties p0 = extractStepData(0);
 		String name = p0.getProperty("name"); //$NON-NLS-1$
 		IProject project = getSelectedProject();
 		XModel model = EclipseResourceUtil.getModelNature(project).getModel();
 		
 		String location = template.getProjectTemplatesLocation(version);
 		File target = new File(location, name);
 		
     	File source = project.getLocation().toFile();
 
 		resourcesStep.copyProjectToTemplate(target, source, model);
 		velocityStep.createPreprocessingFile(target);
 		propertiesStep.createPropertiesFile(target);
 
     	getProperties().setProperty("name", name); //$NON-NLS-1$
 	}
 
     protected DefaultWizardDataValidator validator = new ProjectTemplateValidator();
     
     public WizardDataValidator getValidator(int step) {
     	if(step == 0) {
     		validator.setSupport(this, step);
     		return validator;
     	} else {
     		return super.getValidator(step);
     	}
     }
     
     class ProjectTemplateValidator extends DefaultWizardDataValidator {
     	public void validate(Properties data) {
     		message = null;
     		super.validate(data);
     		if(message != null) return;
     		if(presetProject != null) {
     			setVersion(getAttributeValue(NAME_STEP, "implementation")); //$NON-NLS-1$
     		}
     		String name = data.getProperty("name"); //$NON-NLS-1$
     		if(templates.contains(name)) {
     			message = NLS.bind(WebUIMessages.PROJECT_TEMPLATE_ALREADY_EXISTS, name);
     			return;
     		}
     		String p = data.getProperty("project"); //$NON-NLS-1$
     		if(presetProject == null && !models.containsKey(p)) {
     			message = NLS.bind(WebUIMessages.CANNOT_CREATE_TEMPLATE_FOR_PROJECT, p);
     		}
     	}
     }
     
 }
