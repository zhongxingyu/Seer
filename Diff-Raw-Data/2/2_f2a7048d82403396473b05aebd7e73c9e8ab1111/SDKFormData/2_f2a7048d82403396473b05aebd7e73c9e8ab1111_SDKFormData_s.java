 /*
  * (C) Copyright 2006-2012 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  *     Vladimir Pasquier <vpasquier@nuxeo.com>
  */
 package org.nuxeo.ide.sdk.server.ui;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IPathVariableManager;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.common.forms.Form;
 import org.nuxeo.ide.common.forms.FormData;
 import org.nuxeo.ide.sdk.SDKInfo;
 import org.nuxeo.ide.sdk.SDKRegistry;
 import org.nuxeo.ide.sdk.templates.Constants;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class SDKFormData implements FormData {
 
     @Override
     public void load(Form form) throws Exception {
         SDKTableWidget w = (SDKTableWidget) form.getWidget("sdks");
         w.setDefaultSDK(SDKRegistry.getDefaultSDKId());
         form.setWidgetValue("nosdkcp", !SDKRegistry.useSDKClasspath());
     }
 
     @Override
     public void store(Form form) throws Exception {
         SDKTableWidget w = (SDKTableWidget) form.getWidget("sdks");
         SDKRegistry.save(w.getSDKs());
         SDKInfo sdk = w.getDefaultSDK();
         SDKRegistry.setDefaultSDK(sdk);
         SDKRegistry.setUseSDKClasspath(!(Boolean) form.getWidgetValue("nosdkcp"));
         // Create linked resource from the SDK
         setResourceVariable(Constants.NXSDK_BROWSER_LINK_FOLDER,
                 new Path(sdk.getPath()));
     }
 
     /**
      * Set a resource variable in Eclipse and create the related project
      * 
      * @param variableResourceName (value of the variable)
      * @param pathValue (value of the resource variable)
      */
     protected void setResourceVariable(String variableResourceName,
             IPath pathValue) {
         try {
             IWorkspace workspace = ResourcesPlugin.getWorkspace();
             IPathVariableManager pathMan = workspace.getPathVariableManager();
             IProject sdkProject = createProjectResource(variableResourceName,
                     workspace);
             if (pathMan.getValue(variableResourceName) != null) {
                 pathMan.getValue(variableResourceName).removeFileExtension();
             }
             if (pathMan.validateName(variableResourceName).isOK()
                     && pathMan.validateValue(pathValue).isOK()) {
                 pathMan.setValue(variableResourceName, pathValue);
                 CreateSDKLink(sdkProject, workspace, pathMan);
             }
         } catch (Exception e) {
             UI.showError("Unable to create link resource for sdk because of "
                     + e);
         }
     }
 
     /**
      * Create SDK link resource for browsing it
      * 
      * @param sdkProject
      * @param workspace
      * @param pathMan
      * @throws CoreException
      */
     protected void CreateSDKLink(IProject sdkProject, IWorkspace workspace,
             IPathVariableManager pathMan) throws CoreException {
         try {
             IPath pathFromVariable = pathMan.getValue(Constants.NXSDK_BROWSER_LINK_FOLDER);
             IFolder sdkLink = sdkProject.getFolder(Constants.NXSDK_BROWSER_LINK_FOLDER);
             // Recreate linked resource if exists (in case of updating the SDK)
             if (sdkLink.isLinked()) {
                 sdkLink.delete(true, null);
             }
             if (workspace.validateLinkLocation(sdkLink, pathFromVariable).isOK()) {
                sdkLink.createLink(pathFromVariable, IResource.FOLDER, null);
             }
         } catch (Exception e) {
             UI.showError("Unable to create link resource for sdk because of "
                     + e);
         }
     }
 
     /**
      * Create the related project for displaying the resource in a folder
      * 
      * @param variableResourceName
      * @param workspace
      * @return
      * @throws CoreException
      */
     protected IProject createProjectResource(String variableResourceName,
             IWorkspace workspace) throws CoreException {
         final IProject newProjectHandle = workspace.getRoot().getProject(
                 variableResourceName);
         if (newProjectHandle.exists())
             return newProjectHandle;
         try {
             IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
             newProjectHandle.create(description, null);
         } catch (Exception e) {
             UI.showError("Unable to create a new project in the Eclipse workspace: "
                     + e);
         }
         try {
             IFolder subFolder = newProjectHandle.getFolder(variableResourceName);
             newProjectHandle.open(null);
             if (!subFolder.exists())
                 subFolder.create(false, false, null);
         } catch (Exception e) {
             UI.showError("Unable to create a new folder in the project: " + e);
         }
         return newProjectHandle;
     }
 }
