 /**
  * 
  */
 package org.feature.cluster.model.editor.editors;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.feature.cluster.model.cluster.ClusterPackage;
 import org.feature.cluster.model.cluster.CoreGroup;
 import org.feature.cluster.model.cluster.Group;
 import org.feature.cluster.model.cluster.ViewPoint;
 import org.feature.cluster.model.cluster.impl.GroupImpl;
 
 /**
  * @author Tim Winkelmann
  *
  */
 public class ViewPointContentProvider implements ITreeContentProvider{
 	
 	private static Logger log = Logger.getLogger(ViewPointContentProvider.class);
 	
 	private Viewer viewer = null;
 	/** used to display the ViewPoint as root element.  */
 	private ViewPoint rootViewPoint = null;
 	private Adapter viewPointObserver = new AdapterImpl(){
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.emf.common.notify.impl.AdapterImpl#notifyChanged(org.eclipse.emf.common.notify.Notification)
 		 */
 		@Override
 		public void notifyChanged(Notification msg) {
 			viewPointChanged(msg);
 		}
 		
 	};
 	private Set<EObject> observedViewPoints = new HashSet<EObject>();
 	
 	protected void viewPointChanged(Notification msg) {
 		if (ClusterPackage.Literals.VIEW_POINT__CONTAINED_IN_GROUP.equals(msg.getFeature())) {
 			if (Notification.ADD == msg.getEventType()) {
 				log.debug("ViewPoint changed.");
 				addToViewer(msg.getNotifier(),msg.getNewValue());
 			}
 			if (Notification.REMOVE == msg.getEventType()) {
 				removeFromViewer(msg.getNotifier(), msg.getOldValue());
 			}
 		}
 	}
 
 	private void addToViewer(final Object parent, final Object newValue) {
 		Assert.isTrue(viewer instanceof AbstractTreeViewer);
 		final AbstractTreeViewer atv = (AbstractTreeViewer) viewer;
 		if (!atv.getControl().isDisposed()) {
 			atv.getControl().getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					if (!atv.getControl().isDisposed()) {
 						atv.add(parent, newValue);
 					}
 				}
 			});
 		}
 	}
 	
 	private void removeFromViewer(final Object parent, final Object oldValue) {
 		Assert.isTrue(viewer instanceof AbstractTreeViewer);
 		final AbstractTreeViewer atv = (AbstractTreeViewer) viewer;
 		if (!atv.getControl().isDisposed()) {
 			atv.getControl().getDisplay().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					if (!atv.getControl().isDisposed()) {
 						atv.remove(parent, new Object[]{oldValue});
 					}
 				}
 			});
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private void clearObserver(){
 		for(EObject group : observedViewPoints){
 			group.eAdapters().remove(viewPointObserver);
 		}
 		observedViewPoints.clear();
 	}
 	
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		//clear Observer
 		clearObserver();
 		
 		this.viewer = viewer;
 		if (newInput instanceof EObject) {
 			EObject ebj = (EObject) newInput;
 			TreeIterator<EObject> eAllContents = ebj.eAllContents();
 			while (eAllContents.hasNext()) {
 				EObject eObject = (EObject) eAllContents.next();
 				observeViewPoint(eObject);
 			}
 		}
 	}
 
 	@Override
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	@Override
 	public Object[] getChildren(Object parentElement) {
 		if (parentElement instanceof ViewPoint) {
 			final ViewPoint viewPoint = (ViewPoint) parentElement;
 			if (rootViewPoint == null || !rootViewPoint.equals(viewPoint)) {
 				observeViewPoint(viewPoint);
 				rootViewPoint = viewPoint;
 				ViewPoint[] viewPoints = new ViewPoint[1];
 				viewPoints[0] = viewPoint;
 				return viewPoints;
 			}else if (viewPoint.getContainedInGroup().isEmpty() && viewPoint.eContainer() instanceof CoreGroup) {
 				CoreGroup defaultGroupImpl = (CoreGroup) viewPoint.eContainer();
 				CoreGroup[] coreGroups = new CoreGroup[1];
 				coreGroups[0] = defaultGroupImpl;
 				return coreGroups;
 			} else {
 				EList<Group> groups = viewPoint.getContainedInGroup();
 				Group[] noRedundantGroups = catchSubGroup(groups);
 				return noRedundantGroups;
 			}
 		}else if(parentElement instanceof GroupImpl){
 			GroupImpl groupImpl = (GroupImpl) parentElement;
 			EObject eContainer = groupImpl.eContainer();
 			EObject[] groups = new EObject[1];
 			groups[0] = eContainer;
 			return groups;
 		} else {
 			return new Object[0];
 		}
 	}
 	
 	/**
 	 * 
 	 * @param eObject
 	 */
 	public void observeViewPoint(EObject eObject){
 		if (eObject instanceof ViewPoint) {
 			ViewPoint viewPoint = (ViewPoint) eObject;
 			if (!observedViewPoints.contains(viewPoint)) {
 				viewPoint.eAdapters().add(viewPointObserver);
 				observedViewPoints.add(viewPoint);
 			}
 		}
 	}
 	
 	/**
 	 * catches unnecessary subgroups and removes them from the array.
 	 * @param groups groups to be checked
 	 * @return subset of groups
 	 */
 	private Group[] catchSubGroup(EList<Group> groups) {
		Set<Group> listOfGroupsToRemove = new HashSet<Group>();
 		for (Group group : groups) {
 			EObject eContainer = group.eContainer();
 			while (eContainer instanceof Group) {
 				Group subGroup = (Group) eContainer;
 				for (Group group2 : groups) {
 					if (group2.getName().equals(subGroup.getName())) {
 						listOfGroupsToRemove.add(subGroup);
 					}
 				}
 				eContainer = subGroup.eContainer();
 			}
 		}
 		Group[] newGroups = new Group[groups.size() - listOfGroupsToRemove.size()];
 		int j = 0;
 		for (Group group : groups) {
 			if (!listOfGroupsToRemove.contains(group)) {
 				newGroups[j] = group;
 				j++;
 			}
 		}
 		return newGroups;
 	}
 	
 	@Override
 	public Object getParent(Object element) {
 		return null;
 	}
 
 	@Override
 	public boolean hasChildren(Object element) {
 		return getChildren(element).length > 0;
 	}
 	
 	@Override
 	public void dispose() {
 		clearObserver();
 	}
 }
