 /***************************************************************************************************
  * Copyright (c) 2005, 2006 IBM Corporation and others. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: 
  *   IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.util;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.internal.emf.resource.TranslatorResource;
 import org.eclipse.wst.xml.core.internal.emf2xml.EMF2DOMSSERenderer;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 
 
 public class FacesConfigArtifactEdit extends ArtifactEdit {
	private static String sFileName = null;
 	private FacesConfigType facesConfig = null;
 	private URI facesConfigURI = URI.createURI(IFacesConfigConstants.FACES_CONFIG_URI);
 	private boolean bRegistered = false;
 
 	public FacesConfigArtifactEdit(IProject aProject, boolean toAccessAsReadOnly) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly);
 	}
 
 	public static FacesConfigArtifactEdit getFacesConfigArtifactEditForRead(IProject aProject, String sConfigFile) {
		sFileName = sConfigFile;
 		FacesConfigArtifactEdit artifactEdit = null;
 		try {
 			artifactEdit = new FacesConfigArtifactEdit(aProject, true);
 		} catch (IllegalArgumentException iae) {
 		}
 		return artifactEdit;
 	}
 	public static FacesConfigArtifactEdit getFacesConfigArtifactEditForRead(IProject aProject) {
 		return getFacesConfigArtifactEditForRead(aProject, null);
 	}
 
 	public static FacesConfigArtifactEdit getFacesConfigArtifactEditForWrite(IProject aProject, String sConfigFile) {
		sFileName = sConfigFile;
 		FacesConfigArtifactEdit artifactEdit = null;
 		try {
 			artifactEdit = new FacesConfigArtifactEdit(aProject, false);
 		} catch (IllegalArgumentException iae) {
 		}
 		return artifactEdit;
 	}
 
 	public static FacesConfigArtifactEdit getFacesConfigArtifactEditForWrite(IProject aProject) {
 		return getFacesConfigArtifactEditForWrite(aProject, null);
 	}
 
 	public FacesConfigType getFacesConfig() {
 		if (facesConfig == null) {
 			// TODO: XN - does not seem a good way to check whether the resource has been registered each time.
 			// The following comment is from StrutsArchfactEdit:
 			// This is a hack to ensure that our Resource.Factory is used to
 			// create
 			// the Resource. It's the same hack used by JaxRPCMapArtifactEdit.
 			// The problem is that there is no way to register a Resource.Factory
 			// by, say, content type.
 			// The best we can do is register by last file segment, which isn't
 			// strictly correct.
 			// Plus, there's no way to deregister.
 			if (sFileName != null) {
 				if (!bRegistered) {
 				//if (FacesConfigResourceFactory.getRegisteredFactory(URI.createURI(sFileName)) == null) {
 					FacesConfigResourceFactory.register(sFileName);
 					bRegistered = true;
 				}
 			}
 			facesConfig = (FacesConfigType)getDeploymentDescriptorRoot(); 
 		}
 		return facesConfig;
 	}
 	
 	public URI getFacesConfigURI() {
 		return facesConfigURI;
 	}
 
 	public EObject getDeploymentDescriptorRoot() {
 		List contents = getDeploymentDescriptorResource().getContents();
 		if (contents.size() > 0)
 			return (EObject) contents.get(0);
 		return null;
 		// TODO: XN 
 		//addFacesConfigIfNecessary((IFacesConfigResource)getDeploymentDescriptorResource());
 		//return (EObject) contents.get(0);
 	}
 
 	public Resource getDeploymentDescriptorResource() {
 		if (sFileName != null) {
 			facesConfigURI = URI.createURI(sFileName);
 		} else {// default is "WEB-INF/faces-config.xml"
 			facesConfigURI = URI.createURI(IFacesConfigConstants.FACES_CONFIG_URI);
 		}
 		return getArtifactEditModel().getResource(facesConfigURI);
 	}
 
 	protected void addFacesConfigIfNecessary(TranslatorResource aResource) {
 		//TO: XN - See how to create one if the config file does not exist
 		/*if (aResource != null) {
 		    if(aResource.getContents() == null || aResource.getContents().isEmpty()) {
 		        FacesConfigType facesConfigNew = WebapplicationFactory.eINSTANCE.createWebApp();
 				aResource.getContents().add(facesConfigNew);
 				aResource.setModified(true);
 		    } 
 		    FacesConfigType facesConfig = (FacesConfigType)aResource.getContents().get(0);
 			URI moduleURI = getArtifactEditModel().getModuleURI();
 			try {
 				facesConfig.setDisplayName(StructureEdit.getDeployedName(moduleURI));
 			} catch (UnresolveableURIException e) {
 				//Ignore
 			}
 			aResource.setID(facesConfig, J2EEConstants.WEBAPP_ID);
 	
 			try{
 				aResource.saveIfNecessary();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}*/
 	}
 
 	/**
 	 * @return the DOM model for the Struts config. file.
 	 */
 	public IDOMModel getIDOMModel() {
 		// Modeled after SourceNestedEditorPage.getXMLModelFromResource().
 		// TODO - XN This is a little hackish because it depends on the resource's
 		// renderer being an EMF2DOMSedRenderer (which it is, but that could
 		// change).
 		IFacesConfigResource resource = (IFacesConfigResource)getDeploymentDescriptorResource();
 		if (resource != null) {
 			EMF2DOMSSERenderer renderer = (EMF2DOMSSERenderer) resource.getRenderer();
 			return renderer.getXMLModel();
 		}
 		return null;
 	}
 
 	public IFile getFile() {
 		IVirtualResource resource = getComponent().getRootFolder().findMember(facesConfigURI.toString());
 		if ((resource != null) && (resource.getType() == IVirtualResource.FILE))
 			return ((IVirtualFile) resource).getUnderlyingFile();
 		return null;
 	}
 }
