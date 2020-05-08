 /**
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  * 
  */
 package org.obeonetwork.dsl.smartdesigner.design.actions;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.ui.PlatformUI;
 import org.obeonetwork.dsl.smartdesigner.Diagram;
 import org.obeonetwork.dsl.smartdesigner.GraphicalElement;
 import org.obeonetwork.dsl.smartdesigner.SmartdesignerFactory;
 import org.obeonetwork.dsl.smartdesigner.design.Activator;
 import org.obeonetwork.dsl.smartdesigner.design.dialogs.SelectConnectedElementsDialog;
 import org.obeonetwork.dsl.smartdesigner.design.util.BasicDiagramUtil;
 import org.obeonetwork.dsl.smartdesigner.design.util.EMFUtil;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
import fr.obeo.dsl.viewpoint.DDiagram;
 import fr.obeo.dsl.viewpoint.tools.api.ui.IExternalJavaAction;
 
 /**
  * Action to select a set of elements connected to a specific graphical element
  * and to add them in the diagram.
  * 
  * @author Stephane Drapeau - Obeo
  * 
  */
 public abstract class AbstractSelectConnectedElements implements
 		IExternalJavaAction {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.obeo.dsl.viewpoint.tools.api.ui.IExternalJavaAction#canExecute(java
 	 * .util.Collection)
 	 */
 	@Override
 	public boolean canExecute(Collection<? extends EObject> selections) {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.obeo.dsl.viewpoint.tools.api.ui.IExternalJavaAction#execute(java.util
 	 * .Collection, java.util.Map)
 	 */
 	@Override
 	public void execute(Collection<? extends EObject> selections,
 			Map<String, Object> parameters) {
		Object d = parameters.get("ddiagram");
		if (!(d instanceof DDiagram)) {
			return;
		}
		DDiagram ddiagram = (DDiagram)d;
		
 		Object c = parameters.get("container");
 		if (!(c instanceof GraphicalElement)) {
 			return;
 		}
 		GraphicalElement graphicalElement = (GraphicalElement) c;
 
 		Object l = parameters.get("existingSemanticElements");
 		if (!(l instanceof List)) {
 			return;
 		}
 		Set<EObject> existingElements = Sets.newHashSet();
 		existingElements.addAll((List) l);
 
 		Set<EObject> crossReferencedObjects = BasicDiagramUtil
 				.getConnectedElements(graphicalElement);
 
 		if (crossReferencedObjects.size() > 0) {
 			Map<EObject, Map<EClass, Set<EObject>>> model = getModel(
 					existingElements, crossReferencedObjects);
 			if (model == null) {
 				openNoElementDialogBox();
 			} else {
 				// Open a dialog box with all the connected elements.
 				SelectConnectedElementsDialog dialog = new SelectConnectedElementsDialog(
 						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 								.getShell(), model,
 						"Select connected elements to import",
 						Activator.getConnectedElementsImage(), 400, 400);
 				dialog.open();
 				if (!dialog.isCanceled()) {
 					createNewElements(graphicalElement, dialog.getSelectedEObjects());
 				}
 			}
			ddiagram.refresh();
 		} else {
 			openNoElementDialogBox();
 		}
 	}
 
 	private void openNoElementDialogBox() {
 		// There is no connected element => open a dialog box to
 		// inform the user.
 		MessageBox dialog = new MessageBox(PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getShell());
 		dialog.setText("No connected elements");
 		dialog.setMessage("No connected elements.");
 		dialog.open();
 	}
 
 	private void createNewElements(GraphicalElement container,
 			Set<EObject> selectedObjects) {
 		for (EObject selectedObject : selectedObjects) {
 			GraphicalElement ge = SmartdesignerFactory.eINSTANCE
 					.createGraphicalElement();
 			if (container.eContainer() instanceof Diagram) {
 				((Diagram) container.eContainer()).getElements().add(ge);
 			} else if (container.eContainer() instanceof GraphicalElement) {
 				((GraphicalElement) container.eContainer()).getChild().add(ge);
 			}
 			ge.setSemanticElement(selectedObject);
 		}
 	}
 
 	public abstract Map<EClass, List<EClass>> getArchitectures();
 
 	/**
 	 * @param existingElements
 	 * @param crossReferencedObjects
 	 * @return Null if no element found.
 	 */
 	private final Map<EObject, Map<EClass, Set<EObject>>> getModel(
 			Set<EObject> existingElements, Set<EObject> crossReferencedObjects) {
 		Map<EObject, Map<EClass, Set<EObject>>> result = Maps
 				.newTreeMap(new Comparator<EObject>() {
 					@Override
 					public int compare(EObject o1, EObject o2) {
 						return EMFUtil.retrieveNameFrom(o1).compareTo(
 								EMFUtil.retrieveNameFrom(o2));
 					}
 				});
 
 		Map<EClass, List<EClass>> architectures = this.getArchitectures();
 		boolean oneElement = false;
 		for (EObject eObject : crossReferencedObjects) {
 			if (!existingElements.contains(eObject)) {
 				for (Entry<EClass, List<EClass>> entry : architectures
 						.entrySet()) {
 					if (entry.getValue().contains(eObject.eClass())) {
 						Map<EClass, Set<EObject>> d = result
 								.get(entry.getKey());
 						if (d == null) {
 							d = Maps.newTreeMap(new Comparator<EClass>() {
 								@Override
 								public int compare(EClass o1, EClass o2) {
 									return o1.getName().compareTo(o2.getName());
 								}
 							});
 							result.put(entry.getKey(), d);
 						}
 						Set<EObject> eObjects = d.get(eObject.eClass());
 						if (eObjects == null) {
 							eObjects = Sets
 									.newTreeSet(new Comparator<EObject>() {
 										@Override
 										public int compare(EObject o1,
 												EObject o2) {
 											return EMFUtil
 													.retrieveNameFrom(o1)
 													.compareTo(
 															EMFUtil.retrieveNameFrom(o2));
 										}
 									});
 							d.put(eObject.eClass(), eObjects);
 							oneElement = true;
 						}
 						eObjects.add(eObject);
 					}
 				}
 			}
 		}
 		return oneElement ? result : null;
 	}
 
 }
