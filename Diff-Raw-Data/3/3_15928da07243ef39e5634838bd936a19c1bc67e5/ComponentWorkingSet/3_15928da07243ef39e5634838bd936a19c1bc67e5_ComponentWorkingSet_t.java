 /*
  * Created on Mar 24, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.navigator.internal.workingsets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.Assert;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPersistableElement;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.IWorkingSetManager;
 import org.eclipse.ui.internal.AbstractWorkingSetManager;
 import org.eclipse.ui.internal.IWorkbenchConstants;
 import org.eclipse.ui.internal.WorkbenchPlugin;
 import org.eclipse.ui.internal.WorkingSet;
 import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
 import org.eclipse.ui.internal.registry.WorkingSetRegistry;
 import org.eclipse.wst.common.componentcore.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.ComponentType;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 
 /**
  * @author Admin
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class ComponentWorkingSet extends WorkingSet {
 	
 	static final String FACTORY_ID = "org.eclipse.jst.j2ee.navigator.internal.workingsets.ComponentWorkingSetFactory"; //$NON-NLS-1$
 	
 	public static final String COMPONENT_WORKING_SET_ID = "org.eclipse.jst.j2ee.navigator.ui.ComponentWorkingSetPage";
 	
 	//private ComponentWorkingSetDescriptor descriptor;
 	
 	private String name;
 
     private ArrayList elements;
 
     private String editPageId;
 
     private IMemento workingSetMemento;
     
     private IWorkingSetManager manager;
     
     private String typeId;
        
     public static final String TAG_TYPE_ID = "typeId"; //$NON-NLS-1$
 	    
 	
     /**
      * Creates a new working set.
      * 
      * @param name the name of the new working set. Should not have 
      * 	leading or trailing whitespace.
      * @param element the content of the new working set. 
      * 	May be empty but not <code>null</code>.
      */
     public ComponentWorkingSet(String name, IAdaptable[] elements) {
        super(name,elements);
     }
 
     /**
      * Creates a new working set from a memento.
      * 
      * @param name the name of the new working set. Should not have 
      * 	leading or trailing whitespace.
      * @param memento persistence memento containing the elements of  
      * 	the working set.
      */
     ComponentWorkingSet(String aName, String aTypeId, IMemento memento) {
     	super(aName,new IAdaptable[0]);
     	name = aName;
         typeId = aTypeId;
         workingSetMemento = memento;
     }
 
     /**
 	 * @param descriptor2
 	 */
 	public ComponentWorkingSet(ComponentWorkingSetDescriptor aDescriptor) {
 		this(aDescriptor.getLabel(), new IAdaptable[0] );
 		name = aDescriptor.getLabel();
 		typeId = aDescriptor.getTypeId();
 		editPageId = aDescriptor.getId();
 		//descriptor = aDescriptor;
 	}
 
 	/**
      * Tests the receiver and the object for equality
      * 
      * @param object object to compare the receiver to
      * @return true=the object equals the receiver, the name is the same.
      * 	false otherwise
      */
     public boolean equals(Object object) {
         if (this == object) {
             return true;
         }
         if (object instanceof ComponentWorkingSet) {
             ComponentWorkingSet workingSet = (ComponentWorkingSet) object;
             String objectPageId = workingSet.getId();
             String pageId = getId();
             boolean pageIdEqual = (objectPageId == null && pageId == null)
                     || (objectPageId != null && objectPageId.equals(pageId));
             String objectTypeId = workingSet.getTypeId();
             String typeId = getTypeId();
             boolean typeIdEqual = (objectTypeId == null && typeId == null)
             || (objectTypeId != null && objectTypeId.equals(typeId));
             return workingSet.getName().equals(getName())
                     && workingSet.getElementsArray().equals(getElementsArray())
                     && pageIdEqual
 					&& typeIdEqual;
         }
         return false;
     }
 
     /**
 	 * {@inheritDoc}
 	 */
 	public boolean isEditable() {
 		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
 		String id= getId();
 		if (id == null)
 			return false;
 		WorkingSetDescriptor descriptor= registry.getWorkingSetDescriptor(id);
 		if (descriptor == null)
 			return false;
 		return descriptor.isEditable();
 	}
 //    
     /**
      * Returns the receiver if the requested type is either IWorkingSet 
      * or IPersistableElement.
      * 
      * @param adapter the requested type
      * @return the receiver if the requested type is either IWorkingSet 
      * 	or IPersistableElement.
      */
     public Object getAdapter(Class adapter) {
         if (adapter == IWorkingSet.class
                 || adapter == IPersistableElement.class) {
             return this;
         }
         return null;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkingSet
      */
     public IAdaptable[] getElements() {
         ArrayList list = getElementsArray();
         return (IAdaptable[]) list.toArray(new IAdaptable[list.size()]);
     }
 
     /**
      * Returns the elements array list. Lazily restores the elements from
      * persistence memento. 
      * 
      * @return the elements array list
      */
     private ArrayList getElementsArray() {
         if (elements == null) {
             restoreWorkingSet();
             workingSetMemento = null;
         }
         return elements;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IPersistableElement
      */
     public String getFactoryId() {
         return FACTORY_ID;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkingSet
      */
     public String getId() {
     	if (editPageId == null)
     		editPageId = COMPONENT_WORKING_SET_ID;
         return editPageId;
     }
 
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkingSet
     */
     public String getName() {
         return name;
     }
 
     /**
      * Returns the hash code.
      * 
      * @return the hash code.
      */
     public int hashCode() {
         int hashCode = name.hashCode() & typeId.hashCode();
 
         if (editPageId != null) {
             hashCode &= editPageId.hashCode();
         }
         return hashCode;
    }
 
     /**
      * Recreates the working set elements from the persistence memento.
      */
     private void restoreWorkingSet() {
     	updateElements();
     }
 
     /**
      * Implements IPersistableElement.
      * Persist the working set name and working set contents. 
      * The contents has to be either IPersistableElements or provide 
      * adapters for it to be persistent.
      * 
      * @see org.eclipse.ui.IPersistableElement#saveState(IMemento)
      */
     public void saveState(IMemento memento) {
         if (workingSetMemento != null) {
             // just re-save the previous memento if the working set has 
             // not been restored
             memento.putMemento(workingSetMemento);
         } else {
             memento.putString(IWorkbenchConstants.TAG_NAME, getName());
             memento.putString(IWorkbenchConstants.TAG_EDIT_PAGE_ID, getId());
             memento.putString(TAG_TYPE_ID,typeId);
        }
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkingSet
      */
     public void setElements(IAdaptable[] newElements) {
         internalSetElements(newElements);
         fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE, null);
     }
 
     /**
      * Create a copy of the elements to store in the receiver.
      * 
      * @param elements the elements to store a copy of in the 
      * 	receiver.
      */
     private void internalSetElements(IAdaptable[] newElements) {
         Assert.isNotNull(newElements,
                 "Working set elements array must not be null"); //$NON-NLS-1$
 
         elements = new ArrayList(newElements.length);
         for (int i = 0; i < newElements.length; i++) {
             elements.add(newElements[i]);
         }
     }
 //
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkingSet
      */
     public void setId(String pageId) {
         editPageId = pageId;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkingSet
      */
     public void setName(String newName) {
         Assert.isNotNull(newName, "Working set name must not be null"); //$NON-NLS-1$
         
         name = newName;
         fireWorkingSetChanged(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE, null);
     }
     
     public void connect(IWorkingSetManager manager) {
		// TODO Should be re-added. MDE
    	// Assert.isTrue(this.manager == null, "A working set can only be connected to one manager"); //$NON-NLS-1$
     	this.manager= manager;
     }
     
       public void disconnect() {
     	this.manager= null;
       }
   
     private void fireWorkingSetChanged(String property, Object oldValue) {
     	AbstractWorkingSetManager receiver= manager != null
 			? (AbstractWorkingSetManager)manager
 			: (AbstractWorkingSetManager)WorkbenchPlugin.getDefault().getWorkingSetManager();
 		receiver.workingSetChanged(this, property, oldValue);
     }
     
     
     
     public ImageDescriptor getImage() {
     	ComponentWorkingSetRegistry registry = ComponentWorkingSetRegistry.getInstance();
 		ComponentWorkingSetDescriptor descriptor = null;
 		
 		descriptor = registry.getWorkingSetDescriptor(getId(), typeId);
 		if (descriptor == null) {
 			return null;
 		}
 		return descriptor.getIcon();
 	 	    
     }
 	 
 	public String getTypeId() {
 		return typeId;
 	}
 	
 	private void updateElements() {
 	//	if (workingSet instanceof ComponentWorkingSet) {
 	//		ComponentWorkingSet commonWorkingSet = (ComponentWorkingSet) workingSet;
 			List result= new ArrayList();
 			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 			for (int i= 0; i < projects.length; i++) {
 				if (containsModuleType(projects[i],this.getTypeId())) {
 					result.add(projects[i]);
 				}
 			}
 			this.setElements((IAdaptable[])result.toArray(new IAdaptable[result.size()]));
 	//	}
 	}
 
 	/**
 	 * @param project
 	 * @param typeId
 	 * @return
 	 */
 	private boolean containsModuleType(IProject project, String typeId) {
 		boolean bReturn = false;
 		if (project.isAccessible()) {
 			synchronized (this) {
 				StructureEdit moduleCore = null;
 				try {
 					moduleCore = StructureEdit.getStructureEditForRead(project);
 					WorkbenchComponent[] workBenchModules = moduleCore.getWorkbenchModules(); 
 				    for (int i = 0; i < workBenchModules.length; i++) {
 		                 WorkbenchComponent module = workBenchModules[i];
 		                 ComponentType componentType = module.getComponentType() ;
 		                 if (typeId.equals(componentType.getComponentTypeId())) {
 		                 	bReturn = true;
 		                 	break;
 		                 }
 				    }
 				} finally {
 					if (moduleCore != null)
 					 moduleCore.dispose();
 				}
 			}
 		}
 		return bReturn;
 	}
 	
 	
 
 }
