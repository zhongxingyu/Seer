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
 import org.eclipse.ui.PlatformUI;
 import org.obeonetwork.dsl.smartdesigner.Diagram;
 import org.obeonetwork.dsl.smartdesigner.GraphicalElement;
 import org.obeonetwork.dsl.smartdesigner.design.Activator;
 import org.obeonetwork.dsl.smartdesigner.design.dialogs.ShadeGraphicalElementDialog;
 import org.obeonetwork.dsl.smartdesigner.design.util.BasicDiagramUtil;
 import org.obeonetwork.dsl.smartdesigner.design.util.EMFUtil;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import fr.obeo.dsl.viewpoint.DDiagram;
 import fr.obeo.dsl.viewpoint.tools.api.ui.IExternalJavaAction;
 
 /**
  * Java action that manages the Shade action.
  * 
  * @author Stephane Drapeau - Obeo
  * 
  */
 public abstract class AbstractShadeGraphicalElement implements
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
 
 		boolean selectionIsDiagram = false;
 		// Check the selections
 		for (Object s : selections) {
 			if (s instanceof GraphicalElement) {
 				// If the selection is a GraphicalElement =>
 				// setHidden(!isHidden())
 				GraphicalElement ge = (GraphicalElement) s;
 				ge.setHidden(!ge.isHidden());
 			} else if (s instanceof Diagram) {
 				// The selection contains the diagram
 				selectionIsDiagram = true;
 			}
 		}
 
 		if (!selectionIsDiagram) {
 			// If the selection is not the Diagram, the treatment on the
 			// GraphicalElement is done.
 			return;
 		}
 
 		DDiagram diagram = null;
 		Object o = parameters.get("diagram");
 		if (o instanceof DDiagram) {
 			diagram = (DDiagram) o;
 		} else {
 			return;
 		}
 
 		Map<EClass, List<GraphicalElement>> graphicalElements = BasicDiagramUtil
 				.getGraphicalElements(diagram);
 		Set<GraphicalElement> selectedEObjects = BasicDiagramUtil
 				.getHiddenElements(diagram);
 
 		ShadeGraphicalElementDialog dialog = new ShadeGraphicalElementDialog(
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 				getModel(graphicalElements), selectedEObjects,
 				"Select elements to shade/unshade", Activator
 						.getShadeGraphicalElementImage(), 400, 400);
 		dialog.open();
 
 		if (!dialog.isCanceled()) {
 			for (List<GraphicalElement> l : graphicalElements.values()) {
 				for (GraphicalElement ge : l) {
 					ge.setHidden(selectedEObjects.contains(ge));
 				}
 			}
 		}
 	}
 
 	public abstract Map<EClass, List<EClass>> getArchitectures();
 
 	/**
 	 * Construct the model to display in the tree of the dialog.
 	 * <p>
 	 * The structure of the model is the following: Map<Architecture,
 	 * Map<MetaType,Set<GraphicalElement>>>
 	 * 
 	 * @param graphicalElements
 	 * @return
 	 */
 	private final Map<EObject, Map<EClass, Set<GraphicalElement>>> getModel(
 			Map<EClass, List<GraphicalElement>> graphicalElements) {
 		Map<EObject, Map<EClass, Set<GraphicalElement>>> result = Maps
 				.newTreeMap(new Comparator<EObject>() {
 					@Override
 					public int compare(EObject o1, EObject o2) {
 						return EMFUtil.retrieveNameFrom(o1).compareTo(
 								EMFUtil.retrieveNameFrom(o2));
 					}
 				});
 
 		Map<EClass, List<EClass>> architectures = this.getArchitectures();
 
 		for (Entry<EClass, List<EClass>> architectureEntry : architectures
 				.entrySet()) {
 			Map<EClass, Set<GraphicalElement>> metaType = Maps
 					.newTreeMap(new Comparator<EClass>() {
 						@Override
 						public int compare(EClass o1, EClass o2) {
 							return o1.getName().compareTo(o2.getName());
 						}
 					});
 			for (EClass metaTypeEntry : architectureEntry.getValue()) {
 				List<GraphicalElement> elements = graphicalElements
 						.get(metaTypeEntry);
 				if (elements != null) {
 					Set<GraphicalElement> set = Sets
 							.newTreeSet(new Comparator<GraphicalElement>() {
 								@Override
 								public int compare(GraphicalElement o1,
 										GraphicalElement o2) {
 									return EMFUtil.retrieveNameFrom(
 											o1.getSemanticElement()).compareTo(
 											EMFUtil.retrieveNameFrom(o2
 													.getSemanticElement()));
 								}
 							});
 					set.addAll(elements);
 					metaType.put(metaTypeEntry, set);
 				}
 			}
 			result.put(architectureEntry.getKey(), metaType);
 		}
 		return result;
 	}
 
 }
