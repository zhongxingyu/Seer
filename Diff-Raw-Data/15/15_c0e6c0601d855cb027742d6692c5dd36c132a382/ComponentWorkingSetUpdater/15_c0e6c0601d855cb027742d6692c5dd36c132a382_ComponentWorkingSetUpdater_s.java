 /*
  * Created on Mar 24, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.navigator.internal.workingsets;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.IWorkingSetUpdater;
 import org.eclipse.wst.common.componentcore.internal.ComponentType;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 
 /**
  * @author Admin
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class ComponentWorkingSetUpdater implements IWorkingSetUpdater,
 		IResourceChangeListener/*, IResourceDeltaVisitor*/ {
 	
 	public static final String ID= "org.eclipse.jst.j2ee.navigator.ui.ComponentWorkingSetPage"; //$NON-NLS-1$
 	
 	private List fWorkingSets;
 	
 	private HashMap projectStructureEdits;
 
 	private static class WorkingSetDelta {
 		private IWorkingSet fWorkingSet;
 		private List fElements;
 		private boolean fChanged;
 		public WorkingSetDelta(IWorkingSet workingSet) {
 			fWorkingSet= workingSet;
 			fElements= new ArrayList(Arrays.asList(workingSet.getElements()));
 		}
 		
 		public IWorkingSet getWorkingSet() {
 			return fWorkingSet;
 		}
 		
 		public int indexOf(Object element) {
 			return fElements.indexOf(element);
 		}
 		
 		public void add( Object element) {
 			synchronized (fWorkingSet) {
 				if (indexOf(element) ==-1) {
 					fElements.add(element);
 					fChanged= true;
 				}
 			}
 		}
 		public void remove(int index) {
 			if (fElements.remove(index) != null) {
 				fChanged= true;
 			}
 		}
 		public void process() {
 			if (fChanged) {
 				fWorkingSet.setElements((IAdaptable[])fElements.toArray(new IAdaptable[fElements.size()]));
 			}
 		}
 	 }
 	
 	/**
 	 * 
 	 */
 	public ComponentWorkingSetUpdater() {
 		super();
 		fWorkingSets= new ArrayList();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkingSetUpdater#add(org.eclipse.ui.IWorkingSet)
 	 */
 	public void add(IWorkingSet workingSet) {
 		//checkElementExistence(workingSet);
 		synchronized (fWorkingSets) {
 			
 			updateElements(workingSet);
 			fWorkingSets.add(workingSet);
 		}
 
 	}
 	
 
 	/**
 	 * @param project
 	 * @param typeId
 	 * @return
 	 */
 	private boolean containsModuleType(IProject project, String typeId) {
 		boolean bReturn = false;
 		try {
 		if (project.isAccessible()) {
 			synchronized (this) {
 				StructureEdit moduleCore = null;
 				moduleCore = getStructureEdit(project);
 				if (moduleCore == null) return false;
 				WorkbenchComponent[] workBenchModules = moduleCore.getWorkbenchModules();
 				if (workBenchModules != null){
 				    for (int i = 0; i < workBenchModules.length; i++) {
 		                 WorkbenchComponent module = workBenchModules[i];
 		                 ComponentType componentType = module.getComponentType() ;
 						 if (componentType == null) {
							 String msg = "Component Type is null for the module: " + module.getName() + " in project: " + project.getName();
							 Logger.getLogger().log(msg,Level.SEVERE);
 							 continue;
 						 }
 		                 if (typeId.equals(componentType.getComponentTypeId())) {
 		                 	bReturn = true;
 		                 	break;
 		                 }
 				    }
 				}
 				
 			}
 		}
 		} catch (Exception ex) {
 			Logger.getLogger().logError(ex);
 		}
 		return bReturn;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkingSetUpdater#remove(org.eclipse.ui.IWorkingSet)
 	 */
 	public boolean remove(IWorkingSet workingSet) {
 		boolean result;
 		synchronized(fWorkingSets) {
 			result= fWorkingSets.remove(workingSet);
 		}
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkingSetUpdater#contains(org.eclipse.ui.IWorkingSet)
 	 */
 	public boolean contains(IWorkingSet workingSet) {
 		synchronized(fWorkingSets) {
 			return fWorkingSets.contains(workingSet);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkingSetUpdater#dispose()
 	 */
 	public void dispose() {
 		synchronized(fWorkingSets) {
 			fWorkingSets.clear();
 		}
 		disposeStructureEdits();
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 	}
 
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 	
  	    IResourceDelta delta= event.getDelta();
  	    if (delta == null) return;
 		IResourceDelta[] affectedChildren= delta.getAffectedChildren(IResourceDelta.ADDED | IResourceDelta.REMOVED | IResourceDelta.CHANGED, IResource.PROJECT);
 		if (affectedChildren.length > 0) {
 			for (int i= 0; i < affectedChildren.length; i++) {
 				IResourceDelta projectDelta= affectedChildren[i];
 				IProject project = (IProject)projectDelta.getResource();
 			
 				IWorkingSet[] workingSets;
 				synchronized(fWorkingSets) {
 					workingSets= (IWorkingSet[])fWorkingSets.toArray(new IWorkingSet[fWorkingSets.size()]);
 				}
 				for (int w= 0; w < workingSets.length; w++) {
 					WorkingSetDelta workingSetDelta= new WorkingSetDelta(workingSets[w]);
 					processResourceDelta(workingSetDelta,projectDelta, project);
 					workingSetDelta.process();
 				}
 			}
 		}
 	}
 
 	
 	
 	
 	private void processResourceDelta(WorkingSetDelta result, IResourceDelta aDelta, IProject aProject) {
 		//IResource resource= aDelta.getResource();
 		
 		int index= result.indexOf(aProject);
 		int kind= aDelta.getKind();
 		int flags= aDelta.getFlags();
 		
 		
 		switch (aDelta.getKind()) {
 		case IResourceDelta.REMOVED :
 				 if (index != -1) {
 				 	result.remove(index) ;
 				 }
 				 disposeStructureEdits(aProject);
 				 break;
 		case IResourceDelta.ADDED : {
 				 ComponentWorkingSet workingSet = (ComponentWorkingSet) result.getWorkingSet();
 				 if (containsModuleType(aProject,workingSet.getTypeId())) {
 				 	if (index == -1)
 				 		result.add(aProject);
 				 }
 				 break;
 		}
 		
 		
 		case IResourceDelta.CHANGED :
 			// boolean natureMayHaveChanged = ((aDelta.getFlags() & IResourceDelta.DESCRIPTION) != 0) && ((aDelta.getFlags() & IResourceDelta.MARKERS) == 0);
 			boolean projectOpenStateChanged = ((aDelta.getFlags() & IResourceDelta.OPEN) != 0);
 			if (/*natureMayHaveChanged ||*/ projectOpenStateChanged) {
 				if (aProject.isOpen()) {
 					ComponentWorkingSet workingSet = (ComponentWorkingSet) result.getWorkingSet();
 					 if (containsModuleType(aProject,workingSet.getTypeId())) {
 						result.add(aProject);
 					 }
 				} else {
 					if (index != -1) {
 					 	result.remove(index) ;
 					 }
 					disposeStructureEdits(aProject);
 				}
 
 			} else {
 				 ComponentWorkingSet workingSet = (ComponentWorkingSet) result.getWorkingSet();
 				 if (containsModuleType(aProject,workingSet.getTypeId())) {
 				 	if (index == -1)
 				 		result.add(aProject);
 					} else {
 				 	if (index != -1) {
 				 		result.remove(index) ;
 				 	}
 				 }
 				 break;	
 			}
 			break;
 		}
 
 	}
 	
 	
 	private void updateElements(IWorkingSet workingSet) {
 		if (workingSet instanceof ComponentWorkingSet) {
 			ComponentWorkingSet componentWorkingSet = (ComponentWorkingSet) workingSet;
 			List result= new ArrayList();
 			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 			for (int i= 0; i < projects.length; i++) {
 				try {
 					if (containsModuleType(projects[i],componentWorkingSet.getTypeId())) {
 						result.add(projects[i]);
 					}
 				} catch (Exception ex) {
 					Logger.getLogger().logError(ex);
 				}
 			}
 			componentWorkingSet.setElements((IAdaptable[])result.toArray(new IAdaptable[result.size()]));
 		}
 	}
 
 	
 	 private void disposeStructureEdits() {
 			Set keys = getProjectStructureEdits().keySet();
 			for (Iterator iter = keys.iterator(); iter.hasNext();) {
 				IProject proj = (IProject) iter.next();
 				StructureEdit se =(StructureEdit)getProjectStructureEdits().get(proj);
 				if (se != null)
 					se.dispose();
 				
 			}
 	}
 		
 	private void disposeStructureEdits(IProject aProject) {
 		StructureEdit se =(StructureEdit)getProjectStructureEdits().get(aProject);
 		if (se != null)
 			se.dispose();
 		getProjectStructureEdits().remove(aProject);
 	}
 	
 	private HashMap getProjectStructureEdits() {
 		if (projectStructureEdits != null) 
 			return projectStructureEdits;
 		 
 		synchronized(this) {
 		   if (projectStructureEdits == null)
 			 projectStructureEdits = new HashMap();
 		}
 		return projectStructureEdits;
 		
 	}
 	
 	public StructureEdit getStructureEdit(IProject aProject) {
 		if (getProjectStructureEdits().get(aProject) == null)
 			getProjectStructureEdits().put(aProject,StructureEdit.getStructureEditForRead(aProject));
 		return (StructureEdit)getProjectStructureEdits().get(aProject);
 	}
 
 }
