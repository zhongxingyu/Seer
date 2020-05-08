 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering, Technische Universitaet Muenchen. All rights
  * reserved. This program and the accompanying materials are made available under the terms of the Eclipse Public
  * License v1.0 which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.common.dnd;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecp.common.Activator;
 import org.eclipse.emf.ecp.common.commands.ECPCommand;
 import org.eclipse.emf.ecp.common.model.ECPWorkspaceManager;
 import org.eclipse.emf.ecp.common.model.NoWorkspaceException;
 import org.eclipse.emf.ecp.common.model.workSpaceModel.ECPProject;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.TreeItem;
 
 /**
  * This is the central drop adapter for ECP views. This class acts as a dispatcher. It has a map of (EClass,
  * MEDropAdapter) which contains a reference to a specific drop adapter for each model element type.
  * 
  * @author Hodaie
  */
 public class ComposedDropAdapter extends DropTargetAdapter {
 
 	private StructuredViewer viewer;
 
 	private List<EObject> source;
 	private EObject target;
 	private EObject targetConatiner;
 	private EObject dropee;
 
 	private Map<EClass, MEDropAdapter> dropAdapters;
 
 	/**
 	 * this is used for performance, so that drop method do not need to find the appropriate drop adapter again.
 	 */
 	private MEDropAdapter targetDropAdapter;
 
 	/**
 	 * Actually I should be able to get event feedback from event.feedback But the problem is, the event feedback is
 	 * correctly set in dragOver() method, but in drop() method it is set to 1 (only selection). That's why I save event
 	 * feedback at the end of dragOver() in a variable, and check this variable in drop() instead of event.feedback
 	 */
 	private int eventFeedback;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param viewer viewer
 	 */
 	public ComposedDropAdapter(StructuredViewer viewer) {
 
 		super();
 		this.viewer = viewer;
 
 		dropAdapters = new HashMap<EClass, MEDropAdapter>();
 		IConfigurationElement[] confs = Platform.getExtensionRegistry().getConfigurationElementsFor(
			"org.eclipse.emf.ecp.common.dropadapter");
 		for (IConfigurationElement element : confs) {
 			try {
 				MEDropAdapter dropAdapter = (MEDropAdapter) element.createExecutableExtension("class");
 				dropAdapter.init(viewer);
 				dropAdapters.put(dropAdapter.isDropAdapterfor(), dropAdapter);
 
 			} catch (CoreException e) {
 				// TODO: ChainSaw logging done
 				Activator.getDefault().logException(e.getMessage(), e);
 			}
 		}
 
 	}
 
 	/**
 	 * @param event DropTargetEvent
 	 */
 	@Override
 	public void drop(final DropTargetEvent event) {
 
 		new ECPCommand(target) {
 
 			@Override
 			protected void doRun() {
 				if ((eventFeedback & DND.FEEDBACK_INSERT_AFTER) == DND.FEEDBACK_INSERT_AFTER) {
 					targetDropAdapter.dropMove(targetConatiner, target, source, true);
 				} else if ((eventFeedback & DND.FEEDBACK_INSERT_BEFORE) == DND.FEEDBACK_INSERT_BEFORE) {
 					targetDropAdapter.dropMove(targetConatiner, target, source, false);
 				} else {
 					targetDropAdapter.drop(event, target, source);
 				}
 
 			}
 
 		}.run(true);
 
 	}
 
 	/**
 	 * This is called continually from dragOver() event handler. This checks drop target and drop source to be not Null,
 	 * and sets the target, source, and dropee fields.
 	 * 
 	 * @param event DropTargetEvent
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private boolean extractDnDSourceAndTarget(DropTargetEvent event) {
 		boolean result = true;
 		List<Object> tmpSource = (List<Object>) DragSourcePlaceHolder.getDragSource();
 		if (tmpSource == null) {
 			result = false;
 		}
 
 		for (Object obj : tmpSource) {
 			if (!(obj instanceof EObject)) {
 				result = false;
 			}
 		}
 
 		source = (List<EObject>) DragSourcePlaceHolder.getDragSource();
 		if (source.size() == 0) {
 			return false;
 		}
 
 		// take care that you cannot drop anything on project (project is not a
 		// ModelElement)
 		if (event.item == null || event.item.getData() == null || !(event.item.getData() instanceof EObject)) {
 			result = false;
 		}
 
 		// TODO: ChainSaw - How to retrieve the ECPProject of target and dropee?
 		// check if source and target are in the same project
 
 		if (result) {
 			dropee = source.get(0);
 			target = (EObject) event.item.getData();
 			ECPProject targetProject = null;
 			ECPProject dropeeProject = null;
 			try {
 				targetProject = ECPWorkspaceManager.getInstance().getWorkSpace().getProject(target);
 				dropeeProject = ECPWorkspaceManager.getInstance().getWorkSpace().getProject(dropee);
 			} catch (NoWorkspaceException e) {
 				Activator.getDefault().logException(e);
 				result = false;
 			}
 
 			if (!targetProject.equals(dropeeProject)) {
 				result = false;
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dragOver(DropTargetEvent event) {
 		source = null;
 		target = null;
 		targetConatiner = null;
 		targetDropAdapter = null;
 		eventFeedback = 1;
 
 		event.detail = DND.DROP_COPY;
 		if (!extractDnDSourceAndTarget(event)) {
 			event.detail = DND.DROP_NONE;
 			return;
 		}
 
 		setInitialEventFeedback(event);
 		eventFeedback = event.feedback;
 
 		if ((eventFeedback & DND.FEEDBACK_INSERT_AFTER) == DND.FEEDBACK_INSERT_AFTER
 			|| (eventFeedback & DND.FEEDBACK_INSERT_BEFORE) == DND.FEEDBACK_INSERT_BEFORE) {
 			targetConatiner = target.eContainer();
 			targetDropAdapter = getTargetDropAdapter(targetConatiner.eClass());
 
 		} else {
 			targetDropAdapter = getTargetDropAdapter(target.eClass());
 
 		}
 		if (targetDropAdapter == null) {
 			event.detail = DND.DROP_NONE;
 		} else if (!targetDropAdapter.canDrop(eventFeedback, event, source, target, dropee)) {
 			event.detail = DND.Drop;
 		} else if (targetDropAdapter.canDrop(eventFeedback, event, source, target, dropee)) {
 			event.detail = DND.DROP_COPY;
 		}
 
 	}
 
 	/**
 	 * This method searches drop adaptors map recursively to find the appropriate drop adapter for this model element
 	 * type or one of its super types in model hierarchy.
 	 * 
 	 * @param targetEClass
 	 * @return specific drop target for this model element type or one of its super types in model hierarchy.
 	 */
 	private MEDropAdapter getTargetDropAdapter(EClass targetEClass) {
 
 		MEDropAdapter ret = dropAdapters.get(targetEClass);
 		if (ret == null) {
 			EClass superTypeHavingADropAdapter = getSuperTypeHavingADropAdapter(targetEClass.getESuperTypes());
 			if (superTypeHavingADropAdapter != null && superTypeHavingADropAdapter != targetEClass) {
 				ret = getTargetDropAdapter(superTypeHavingADropAdapter);
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * This is used by getDropTarget() method. It takes super classes of targetEClass and tries to find a unique drop
 	 * adapter that matches one of super types. If there are more than one matching drop adapters, an exception is
 	 * thrown. If there is no matching drop adapter, this method searches recursively until it finds one, or throws the
 	 * exception.
 	 * 
 	 * @param superClazz super classes of targetEClass. If there is no match at the first call of method, this will be a
 	 *            collection of super classes of each input super class.
 	 * @return an EClass that is both super class of targetEClass (directly of indirectly) and has a drop adapter.
 	 */
 	private EClass getSuperTypeHavingADropAdapter(Collection<EClass> superClazz) {
 
 		EClass ret = null;
 		if (superClazz.size() == 0) {
 			return EcorePackage.eINSTANCE.getEObject();
 		}
 
 		Set<EClass> intersection = new HashSet<EClass>(dropAdapters.keySet());
 		intersection.retainAll(superClazz);
 
 		// check if intersection contains many classes, but if they are in an inheritance hierarchy keep only the
 		// deepest class.
 		// This must be discussed as a potential modeling problem.
 
 		if (intersection.size() > 1) {
 			Set<EClass> toBeRemoved = new HashSet<EClass>();
 			for (EClass eClass1 : intersection) {
 				for (EClass eClass2 : intersection) {
 					if (!eClass2.equals(eClass1)
 						&& (eClass2.isSuperTypeOf(eClass1) || eClass2.equals(EcorePackage.eINSTANCE.getEObject()))) {
 						toBeRemoved.add(eClass2);
 					}
 				}
 			}
 			intersection.removeAll(toBeRemoved);
 		}
 
 		if (intersection.size() > 1) {
 			throw new IllegalStateException("More than one drop adapter for this type found!");
 
 		} else if (intersection.size() == 0) {
 			Set<EClass> eclazz = new HashSet<EClass>();
 			for (EClass superClass : superClazz) {
 				eclazz.addAll(superClass.getESuperTypes());
 			}
 			ret = getSuperTypeHavingADropAdapter(eclazz);
 		} else {
 			ret = (EClass) intersection.toArray()[0];
 		}
 
 		return ret;
 	}
 
 	/**
 	 * This sets the initial event feedback, and is also responsible for showing INSERT_AFTER and INSERT_BEFORE
 	 * feedbacks according to mouse cursor position.
 	 * 
 	 * @param event DropTargetEvent
 	 */
 	private void setInitialEventFeedback(DropTargetEvent event) {
 		event.feedback = DND.FEEDBACK_SELECT;
 
 		if (event.item != null) {
 			Rectangle rect = ((TreeItem) event.item).getBounds();
 			Point pt = viewer.getControl().toControl(event.x, event.y);
 			if (pt.y < rect.y + 5) {
 				event.feedback = DND.FEEDBACK_INSERT_BEFORE;
 			}
 			if (pt.y > rect.y + rect.height - 5) {
 				event.feedback = DND.FEEDBACK_INSERT_AFTER;
 			}
 
 		}
 		event.feedback |= DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
 
 	}
 
 }
