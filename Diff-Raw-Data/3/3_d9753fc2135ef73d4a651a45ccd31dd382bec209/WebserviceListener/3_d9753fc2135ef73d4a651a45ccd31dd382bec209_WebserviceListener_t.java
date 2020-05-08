 package org.eclipse.jst.j2ee.internal.webservice.startup;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.ui.IStartup;
 import org.eclipse.wst.common.project.facet.core.internal.FacetedProjectPropertyTester;
 import org.eclipse.wst.project.facet.ProductManager;
 
 public class WebserviceListener implements IStartup, IResourceChangeListener, IResourceDeltaVisitor {
 	
 	private boolean listening = false;
 	    
     /* The constants/facets/file extns are duplicated here to avoid loading plugins */
 	private static final String PROJECT_FACET = "projectFacet"; //$NON-NLS-1$     
     
 	private static final String APPCLIENT_FACET = "jst.appclient"; //$NON-NLS-1$ 
     private static final String WEB_FACET = "jst.web"; //$NON-NLS-1$ 
     private static final String EJB_FACET = "jst.ejb"; //$NON-NLS-1$ 
     
 	private static final String WSDL_EXT = "wsdl"; //$NON-NLS-1$
 	private static final String WSIL_EXT = "wsil"; //$NON-NLS-1$
 	private static final String WEB_SERVICES_CLIENT_SHORTNAME    = "webservicesclient.xml"; //$NON-NLS-1$
 	private static final String WEB_SERVICES_DD_URI			= 	"webservices.xml"; //$NON-NLS-1$
 	
     private static final FacetedProjectPropertyTester facetPropertyTester = new FacetedProjectPropertyTester();
 
     private static WebserviceListener INSTANCE;
     
     public static WebserviceListener getInstance() {
 		return INSTANCE;
 	}
 	/**
 	 * @param project
 	 * @return
 	 * method copied from WebServicesViewerSynchronization for performance reasons (not load plugins)
 	 */
 	private static final boolean isInteresting(IProject project) {
 		return hasFacet(project, WEB_FACET) || 
 			hasFacet(project, EJB_FACET) || 
 			hasFacet(project, APPCLIENT_FACET);
 	}
 	/**
 	 * @param element
 	 * @param facet
 	 * @return
 	 * method copied from WebServicesViewerSynchronization for performance reasons (not load plugins)
 	 */
 	private static final boolean hasFacet(Object element, String facet) {
 		return facetPropertyTester.test(element, PROJECT_FACET, new Object[] {}, facet);
 	}
 	
 	/**
 	 * @param aFile
 	 * @return
 	 * method copied from WebServicesManager for performance reasons (not load plugins)
 	 */
 	private static final boolean isFileInteresting(IFile aFile) {
 		if (aFile != null && aFile.getFileExtension() != null) {
 			String extension = aFile.getFileExtension();
 			return extension.equals(WSDL_EXT) || extension.equals(WSIL_EXT) 
 			|| aFile.getName().equals(WEB_SERVICES_CLIENT_SHORTNAME) 
 			|| aFile.getName().equals(WEB_SERVICES_DD_URI);
 		}
 		return false;
 	}
 
 	
 	public boolean isListening() {
 		return listening;
 	}
 	public void earlyStartup() {
 		if (ProductManager.shouldUseViewerSyncForWebservices()) {
 			INSTANCE = this;
 			startListening();
 		} else {
			//Change to no-op
			//org.eclipse.jst.j2ee.internal.webservice.WebServiceViewerSynchronization.setAreThereWebServices(false);
 		}
 	}
 
 	public void resourceChanged(IResourceChangeEvent event) {
 		try {
 			event.getDelta().accept(this);
 		} catch (CoreException e) {
 		} 
 	}
 
 	public boolean visit(IResourceDelta delta) throws CoreException { 
 		
 		IResource resource = delta.getResource();
 		switch (resource.getType()) {
 			case IResource.ROOT :
 				return true;
 			case IResource.PROJECT: 
 				if(isListening() &&  (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED)){
 					if(isInteresting(resource.getProject())) {
 						return true;
 					}
 				}
 				break;
 			case IResource.FOLDER :
 				if(isListening() && (delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.CHANGED)) {
 					return true;
 				}
 				break;
 			case IResource.FILE :
 				if(isListening() && delta.getKind() == IResourceDelta.ADDED) {
 					if(isFileInteresting((IFile)resource)){
 						stopListening();
 						// this will cause the plug-in to start our goal was to delay it until now
 						if(org.eclipse.jst.j2ee.internal.webservice.WebServiceViewerSynchronization.isThereWebServicesPreferenceSet()){
 							org.eclipse.jst.j2ee.internal.webservice.WebServiceViewerSynchronization.setAreThereWebServices(true);
 						}
 					}
 				}				
 		}
 		
 		return false;
 	}
 	
 	public void startListening() {
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 		listening = true;
 	}
 
 	public void stopListening() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		listening = false;
 	}
 }
