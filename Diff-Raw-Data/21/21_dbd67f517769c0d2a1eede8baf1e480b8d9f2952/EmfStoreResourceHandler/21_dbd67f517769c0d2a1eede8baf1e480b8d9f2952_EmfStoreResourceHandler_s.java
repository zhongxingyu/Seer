 package edu.tum.in.bruegge.epd.emfstore4appmodel;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.core.services.log.Logger;
 import org.eclipse.e4.ui.internal.workbench.E4Workbench;
 import org.eclipse.e4.ui.internal.workbench.E4XMIResource;
 import org.eclipse.e4.ui.internal.workbench.ModelAssembler;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.workbench.IModelResourceHandler;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.common.model.IdEObjectCollection;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ProjectChangeObserver;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 
 import edu.tum.in.bruegge.epd.emfstore.helper.EmfStoreHelper;
 
 public class EmfStoreResourceHandler implements IModelResourceHandler {
 	
 	@Inject
 	private Logger logger;
 	
 	@Inject
 	private IEclipseContext context;
 	
 	@Inject
 	@Named(E4Workbench.INITIAL_WORKBENCH_MODEL_URI)
 	private URI applicationDefinitionInstance;
 
 	
 	private ProjectSpace projectSpace;
 	
 	@PostConstruct
 	void init() {
 		System.out.println("TEST");
 	}
 	
 	@Override
 	public Resource loadMostRecentModel() {
 		
 		
 		EObject appModel = EmfStoreHelper.INSTANCE.getRoot();
 		MApplication appElement = (MApplication) appModel;
		
		Resource resource = new E4XMIResource(); // new XMIResourceImpl()
 		resource.getContents().add(appModel);
		resource.setURI(this.applicationDefinitionInstance);
 		
 
		
 		this.context.set(MApplication.class, appElement);
 		/**loads Code from fragments and modifies model; not necessary in our scenario right now**/
//		ModelAssembler contribProcessor = ContextInjectionFactory.make(ModelAssembler.class, this.context);
//		contribProcessor.processModel();
 		
 		return resource;
 	}
 
 	@Override
 	public void save() throws IOException {
 		System.out.println("save");
		// TODO commit()
 	}
 }
