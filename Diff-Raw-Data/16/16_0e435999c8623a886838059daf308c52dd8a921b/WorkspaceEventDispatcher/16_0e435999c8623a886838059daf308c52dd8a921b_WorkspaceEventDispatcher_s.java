 /*
  * File WsChangeListener.java.
  * Created by Donat Csikos<dcsikos@cern.ch> at 25 May 2012.
  */
 package hu.bme.incquery.deps.core;
 
 import hu.bme.incquery.deps.wsmodel.WWorkspace;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IElementChangedListener;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 
 public class WorkspaceEventDispatcher implements IElementChangedListener {
 
 	private final WsChangeListenerRepo listenerRepo;
 
 	private final BuildScheduler builder;
 
 	private List<IJavaProject> tracedProjects;
 	
 	private PreferenceStore prefs = PreferenceStore.getStore();
 
 	public WorkspaceEventDispatcher() {
 		listenerRepo = new WsChangeListenerRepo();
 		builder = new BuildScheduler(listenerRepo, this);
 	}
 
 	public void registerWorkspaceListener(WsChangeEventListener l) {
 		listenerRepo.addWsChangeEventListener(l);
 	}
 
 	public void deregisterWorkspaceListener(WsChangeEventListener l) {
 		listenerRepo.removeWsChangeEventListener(l);
 	}
 
 	public List<IJavaProject> getTracedProjects() {
 		if (tracedProjects == null) {
 			initTracedProjectList();
 		}
 		return tracedProjects;
 	}
 	
 	public void addTracedProjectAndDiscoverIt(IJavaProject project) {
 		if (tracedProjects == null) {
 			initTracedProjectList();
 		}
 		prefs.addTracedProject(project.getElementName());
 		tracedProjects.add(project);
 		builder.addProject(project);
 	}
 	
 	
 	public void removeTracedProjectWithStructure(IJavaProject project) {
 		if (tracedProjects == null) {
 			initTracedProjectList();
 		}
 		prefs.removeTracedProject(project.getElementName());
 		tracedProjects.remove(project);
 		builder.removeProject(project);
 	}
 
 	public boolean isTracedProject(IJavaProject project) {
 		if (tracedProjects == null) {
 			initTracedProjectList();
 		}
 		return tracedProjects.contains(project);
 	}
 
 	private void initTracedProjectList() {
 		try {
 			List<String> enabledProjectNames = prefs.tracedProjectNames();
 			tracedProjects = new LinkedList<IJavaProject>();
 			IWorkspace jdtWorkspace = ResourcesPlugin.getWorkspace();
 			IWorkspaceRoot jdtWsRoot = jdtWorkspace.getRoot();
 			for (IJavaProject project : JdtModelWalker.childrenOf(jdtWsRoot)) {
 				if (enabledProjectNames.contains(project.getElementName())) {
 					tracedProjects.add((IJavaProject) project);
 				}
 			}
 		} catch (CoreException e) {
 			// Should not happen.
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void elementChanged(final ElementChangedEvent event) {
 		IJavaElementDelta delta = event.getDelta();
 		
 		if (delta.getElement().getElementType() != IJavaElement.JAVA_MODEL) {
 			return;
 		}
 		if ((delta.getElement() instanceof IJavaModel)) {
 			try {
 				// Handle delta.
 				builder.updateWorkspaceModel(delta);
 			} catch (Exception e) {
 				// Print the reconcile process.
 				System.err.println("<<Model out of sync>>");
 				e.printStackTrace();
 				System.err.println("<<Reinitializing model>>");
 				builder.rebuildWorkspaceModel(tracedProjects);
 
 			}
 		}
 	}
 
 	public WWorkspace getWorkspace() {
 		return builder.getWorkspace();
 	}
 	
 }
