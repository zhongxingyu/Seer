 package edu.tum.in.bruegge.epd.emfstore.helper;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.core.services.log.Logger;
 import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
 import org.eclipse.e4.ui.internal.workbench.ModelAssembler;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.workbench.IModelResourceHandler;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 
 public class EmfStoreResourceHandler implements IModelResourceHandler {
 	
 	@Inject
 	private Logger logger;
 	
 	@Inject
 	private IEclipseContext context;
 	
 	private ProjectSpace projectSpace;
 	
 	
 	@Override
 	public Resource loadMostRecentModel() {
 		EObject appModel = EmfStoreHelper.INSTANCE.getRoot();
 		MApplication appElement = (MApplication) appModel;
 		
 		// Convert XMIResource from EMFStore to E4XMIResource (required by several Eclipse Plugins)
//		Resource resource = new E4XMIResource(appModel.eResource().getURI());
		Resource resource = new E4XMIResource(URI.createFileURI("G:/Unicase/temp/model.txt"));
 		resource.getContents().add(appModel);
 		
 		// Add resource to ResourceSet (required by Eclipse Plugins)
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResources().add(resource);
 		
		logger.info("EMFStore Resource URI: " + appModel.eResource().getURI().toString());
		
 		// Put Application Model into Context
 		this.context.set(MApplication.class, appElement);
 		
 		// Loads model fragments
 		ModelAssembler contribProcessor = ContextInjectionFactory.make(ModelAssembler.class, this.context);
 		contribProcessor.processModel();
 		
 		return resource;
 	}
 
 	@Override
 	public void save() throws IOException {
 		// EmfStoreHelper.INSTANCE.commit();
 	}
 }
