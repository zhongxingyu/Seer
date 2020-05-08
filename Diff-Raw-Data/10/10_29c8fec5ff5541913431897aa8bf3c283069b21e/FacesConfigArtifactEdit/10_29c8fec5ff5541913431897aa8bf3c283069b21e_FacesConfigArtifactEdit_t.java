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
 import org.eclipse.jst.jsf.facesconfig.FacesConfigPlugin;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.xml.core.internal.emf2xml.EMF2DOMSSERenderer;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 
 
 /**
  * The artifact edit for the JSF Application Configuration (faces-config) model - may be used but not extended by clients.
  * 
  * This class maybe refactored to a different package in the future.
  */
 public final class FacesConfigArtifactEdit extends ArtifactEdit {

	private static final String EDIT_MODEL_ID = "jsf.facesconfig"; //$NON-NLS-1$

 	private String sFileName = null;
 	private FacesConfigType facesConfig = null;
 	private URI facesConfigURI = URI.createURI(IFacesConfigConstants.FACES_CONFIG_URI);
 	private boolean bRegistered = false;
 
 	/**
 	 * @param aProject
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public FacesConfigArtifactEdit(IProject aProject, boolean toAccessAsReadOnly) throws IllegalArgumentException {
		super(aProject, toAccessAsReadOnly, false, EDIT_MODEL_ID);
 	}
 
 	/**
 	 * @param aProject
 	 * @param sConfigFile
 	 * @return the faces config artifact for the file config file sConfig in project aProject.
 	 * Opened only for read access (no write)
 	 */
 	public static FacesConfigArtifactEdit getFacesConfigArtifactEditForRead(IProject aProject, String sConfigFile) {
 		FacesConfigArtifactEdit artifactEdit = null;
 		try {
 			artifactEdit = new FacesConfigArtifactEdit(aProject, true);
 			if (artifactEdit != null) {
 				artifactEdit.setFilename(sConfigFile);
 			}
 		} catch (IllegalArgumentException iae) {
             // suppress illegal argument exception
             FacesConfigPlugin.write(iae);
 		}
 		return artifactEdit;
 	}
 	/**
 	 * @param aProject
 	 * @return the default artifact edit
      * @deprecated Use getFacesConfigArtifactEditForRead(IProject, String) instead. This
      * method is dangerous because the caller is assuming that the "default"
      * model exists and knows nothing about where it is coming from.
 	 */
 	public static FacesConfigArtifactEdit getFacesConfigArtifactEditForRead(IProject aProject) {
 		return getFacesConfigArtifactEditForRead(aProject, null);
 	}
 
     /**
      * @param aProject
      * @param sConfigFile
      * @return the faces config artifact for the file config file sConfig in project aProject.
      * Opened for both write and read access
      */	
 	public static FacesConfigArtifactEdit getFacesConfigArtifactEditForWrite(IProject aProject, String sConfigFile) {
 		FacesConfigArtifactEdit artifactEdit = null;
 		try {
 			artifactEdit = new FacesConfigArtifactEdit(aProject, false);
 			if (artifactEdit != null) {
 				artifactEdit.setFilename(sConfigFile);
 			}
 		} catch (IllegalArgumentException iae) {
             // suppress illegal argument exception
             FacesConfigPlugin.write(iae);
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * @param aProject
 	 * @return the default artifact edit for read
      * @deprecated Use getFacesConfigArtifactEditForWrite(IProject, String) 
      * instead. This method is dangerous because the caller is assuming that
      * the "default" model exists and knows nothing about where it is coming
      * from.
 	 */
 	public static FacesConfigArtifactEdit getFacesConfigArtifactEditForWrite(IProject aProject) {
 		return getFacesConfigArtifactEditForWrite(aProject, null);
 	}
 
 	/**
 	 * @return the root model object for this artifact edit model
 	 */
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
 	
 	/**
 	 * @return the uri pointing to the source of this faces config artifact
 	 */
 	public URI getFacesConfigURI() {
 		return facesConfigURI;
 	}
 
     /**
      * @return the model root
      */
     public EObject getDeploymentDescriptorRoot()
     {
         Resource deploymentDescriptorResource = getDeploymentDescriptorResource();
         if (deploymentDescriptorResource != null)
         {
             List contents = deploymentDescriptorResource.getContents();
             if (contents.size() > 0)
             {
                 return (EObject) contents.get(0);
             }
         }
         return null;
         // TODO: XN
         // addFacesConfigIfNecessary((IFacesConfigResource)getDeploymentDescriptorResource());
         // return (EObject) contents.get(0);
     }
 
 	/**
 	 * @return the EMF resource retrieved for this artifact's uri or null if
 	 * this descriptor has not resource.
 	 */
 	public Resource getDeploymentDescriptorResource() {
 		if (sFileName != null) {
 			facesConfigURI = URI.createURI(sFileName);
 		} else {// default is "WEB-INF/faces-config.xml"
 			facesConfigURI = URI.createURI(IFacesConfigConstants.FACES_CONFIG_URI);
 		}
 		final ArtifactEditModel artifactEditModel = getArtifactEditModel();
 		if (artifactEditModel != null)
 		{
 		    return artifactEditModel.getResource(facesConfigURI);
 		}
 		return null;
 	}
 
 	/**
 	 * Sets an instance's filename, used to create a URI and load a model.
 	 * 
 	 * @param filename This instance's filename.
 	 */
 	public void setFilename(String filename) {
 		sFileName = filename;
		if (filename != null && filename.length() > 0) {
			facesConfigURI = URI.createURI(filename);
		}
 	}
 
 	// TODO: appears to be dead
 //	protected void addFacesConfigIfNecessary(TranslatorResource aResource) {
 //		//TO: XN - See how to create one if the config file does not exist
 //		/*if (aResource != null) {
 //		    if(aResource.getContents() == null || aResource.getContents().isEmpty()) {
 //		        FacesConfigType facesConfigNew = WebapplicationFactory.eINSTANCE.createWebApp();
 //				aResource.getContents().add(facesConfigNew);
 //				aResource.setModified(true);
 //		    } 
 //		    FacesConfigType facesConfig = (FacesConfigType)aResource.getContents().get(0);
 //			URI moduleURI = getArtifactEditModel().getModuleURI();
 //			try {
 //				facesConfig.setDisplayName(StructureEdit.getDeployedName(moduleURI));
 //			} catch (UnresolveableURIException e) {
 //				//Ignore
 //			}
 //			aResource.setID(facesConfig, J2EEConstants.WEBAPP_ID);
 //	
 //			try{
 //				aResource.saveIfNecessary();
 //			}catch(Exception e){
 //				e.printStackTrace();
 //			}
 //		}*/
 //	}
 
 	/**
 	 * @return the DOM model for the Struts config. file.
 	 */
 	public IDOMModel getIDOMModel() {
 		// Modeled after SourceNestedEditorPage.getXMLModelFromResource().
 		// TODO - XN This is a little hackish because it depends on the resource's
 		// renderer being an EMF2DOMSedRenderer (which it is, but that could
 		// change).
 		Resource resource = getDeploymentDescriptorResource();
 		if (resource instanceof IFacesConfigResource) {
 			EMF2DOMSSERenderer renderer = (EMF2DOMSSERenderer) 
 			    ((IFacesConfigResource)resource).getRenderer();
 			return renderer.getXMLModel();
 		}
 		return null;
 	}
 
 	/**
 	 * @return the IFile used to load the model or null if none.
 	 */
 	public IFile getFile() {
 		IVirtualResource resource = getComponent().getRootFolder().findMember(facesConfigURI.toString());
 		if ((resource != null) && (resource.getType() == IVirtualResource.FILE))
 			return ((IVirtualFile) resource).getUnderlyingFile();
 		return null;
 	}
     
     /**
      * @return true if the underlying model has been disposed
      */
     public boolean isDisposed()
     {
         return getArtifactEditModel().isDisposed();
     }
 }
