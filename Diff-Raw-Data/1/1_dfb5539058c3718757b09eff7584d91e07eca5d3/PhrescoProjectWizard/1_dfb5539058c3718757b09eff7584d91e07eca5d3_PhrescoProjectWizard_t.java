 /*
  * ###
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 
import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ProjectAdministrator;
 import com.photon.phresco.model.ProjectInfo;
 import com.photon.phresco.model.Technology;
 import com.photon.phresco.ui.Activator;
 import com.photon.phresco.ui.util.PhrescoUtils;
 import com.photon.phresco.ui.wizards.pages.AppInfoPage;
 import com.photon.phresco.ui.wizards.pages.ConfigurationsPage;
 import com.photon.phresco.ui.wizards.pages.CoreModuleFeaturesPage;
 import com.photon.phresco.ui.wizards.pages.CustomModuleFeaturesPage;
 import com.photon.phresco.ui.wizards.pages.JsLibraryFeaturePage;
 import com.photon.phresco.util.Credentials;
 
 /**
  * Phresco project wizard
  * 
  * @author arunachalam.lakshmanan@photoninfotech.net
  */
 public class PhrescoProjectWizard extends Wizard implements INewWizard {
 
 	private AppInfoPage appInfoPage;
 	private CoreModuleFeaturesPage featuresPage;
 	private CustomModuleFeaturesPage customModuleFeaturesPage;
 	private JsLibraryFeaturePage featurePageJsLibrary;
 	private ConfigurationsPage configurationsPage;
 
 	private ProjectInfo projectInfo;
 	private User user;
 	private String path;
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 
 
 		ImageDescriptor myImage = ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault().getBundle(),
 				new Path("icons/phresco.png"),null));
 		super.setDefaultPageImageDescriptor(myImage);
 		super.setNeedsProgressMonitor(true);
 		super.setWindowTitle("Phresco");
 		
 		try {
 			doLogin();
 		} catch (PhrescoException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void doLogin() throws PhrescoException {
 		try {
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			String username = "suresh_ma";//store.getString(PreferenceConstants.USER_NAME);
 			String password = "SureshE3510";//store.getString(PreferenceConstants.PASSWORD);
 			Credentials credentials = new Credentials(username, password);
 			User user = administrator.doLogin(credentials);
 			System.out.println("user.getDisplayName()::" + user.getDisplayName());
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException();
 		}
 	}
 	
 	@Override
 	public void addPages() {
 		super.addPages();
 		featuresPage = new CoreModuleFeaturesPage("CoreModule");
 		customModuleFeaturesPage = new CustomModuleFeaturesPage("CustomModules");
 		featurePageJsLibrary = new JsLibraryFeaturePage("JsLibraries");
 		appInfoPage = new AppInfoPage("AppInfoPage",featuresPage,customModuleFeaturesPage,featurePageJsLibrary);
 		configurationsPage = new ConfigurationsPage("Configuration");
 	
 		addPage(appInfoPage);
 		addPage(featuresPage);
 		addPage(customModuleFeaturesPage);
 		addPage(featurePageJsLibrary);
 		//TODO This needs to be added to the project properties
 		//addPage(configurationsPage);
 	}
 
 	@Override
 	public boolean performFinish() {
 		 if(!appInfoPage.isPageComplete()) {
 			  return false;
 		  }
 		
 		//pilot project
 		//List<ProjectInfo> pilots = appInfoPage.pilots;
 		projectInfo = new ProjectInfo();
 		projectInfo.setCustomerId("photon");
 		//int pilotIndex = appInfoPage.pilotProjectCombo.getSelectionIndex();
 		//ProjectInfo pilot = null;
 //		if(pilots !=null && pilots.size()> 0 && pilotIndex>-1){
 //			projectInfo = pilots.get(pilotIndex);
 //		}
 		//project name
 		projectInfo.setName(appInfoPage.projectTxt.getText());
 		//project code
 		String projectName = "PHR_" + appInfoPage.projectTxt.getText();
 		projectInfo.setCode(projectName);
 		//project projectcode
 		projectInfo.setProjectCode(projectName);
 		//project description
 		//projectInfo.setDescription(appInfoPage.descriptionTxt.getText());
 		//project version
 		if(appInfoPage.versionTxt.getText().isEmpty()){
 			projectInfo.setVersion("1.0.0");
 		}else {
 			projectInfo.setVersion(appInfoPage.versionTxt.getText());
 		}
 		//project Application Type
 		projectInfo.setApplication(appInfoPage.appTypeConstant);
 		//project Technology
 		Technology technology = appInfoPage.technologies.get(appInfoPage.technologyCombo.getSelectionIndex());
 		projectInfo.setTechnology(technology);
 		projectInfo.setTechId(technology.getId());
 		//project technology version
 				
 		//path = "C:/PHRESCO/workspace/projects/" + projectName;
 		path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + "/workspace/projects/" + projectName;	
 		//String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString() + "/workspace/projects/" + projectName;	
 		//TODO:set the env variable PHRESCO_HOME value :: " + ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()
 		//User user = doLogin();
 		try {
 		      getContainer().run(true, true, new IRunnableWithProgress() {
 		         public void run(IProgressMonitor monitor)
 		            throws InvocationTargetException, InterruptedException
 		         {
 		            PhrescoUtils.createProject(projectInfo, user, path,monitor);
 		         }
 		      });
 		   }
 		   catch (InvocationTargetException e) {
 		      e.printStackTrace();
 		      return false;
 		   }
 		   catch (InterruptedException e) {
 		      // User canceled, so stop but don't close wizard.
 		      return false;
 		   }
 		return true;
 	}
 }
