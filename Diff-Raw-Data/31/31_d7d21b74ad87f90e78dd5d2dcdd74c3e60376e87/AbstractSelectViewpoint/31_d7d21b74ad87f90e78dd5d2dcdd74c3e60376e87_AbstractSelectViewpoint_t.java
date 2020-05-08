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
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PlatformUI;
 import org.obeonetwork.dsl.smartdesigner.GraphicalElement;
 import org.obeonetwork.dsl.smartdesigner.design.Activator;
 import org.obeonetwork.dsl.smartdesigner.design.dialogs.SelectViewpointDialog;
 import org.obeonetwork.dsl.smartdesigner.design.dialogs.ViewpointID;
 import org.obeonetwork.dsl.smartdesigner.design.util.BasicDiagramUtil;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import fr.obeo.dsl.viewpoint.DDiagram;
 import fr.obeo.dsl.viewpoint.DDiagramElement;
 import fr.obeo.dsl.viewpoint.description.Layer;
 import fr.obeo.dsl.viewpoint.description.OptionalLayer;
 import fr.obeo.dsl.viewpoint.diagram.tools.api.editor.DDiagramEditor;
 import fr.obeo.dsl.viewpoint.diagram.ui.tools.internal.commands.ChangeLayerActivationCommand;
 import fr.obeo.dsl.viewpoint.tools.api.ui.IExternalJavaAction;
 
 /**
  * 
  * @author Stephane Drapeau - Obeo
  * 
  */
 public abstract class AbstractSelectViewpoint implements IExternalJavaAction {
 
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
 
 		DDiagram diagram = null;
 		Object o = parameters.get("diagram");
 		if (o instanceof DDiagram) {
 			diagram = (DDiagram) o;
 		} else if (o instanceof DDiagramElement) {
 			diagram = ((DDiagramElement) o).getParentDiagram();
 		}
 		if (diagram == null) {
 			return;
 		}
 
 		Map<String, Map<String, List<EClass>>> viewpoints = getViewpoints();
 		Set<ViewpointID> selectedViewpoints = Sets.newHashSet();
 
 		SelectViewpointDialog dialog = new SelectViewpointDialog(PlatformUI
 				.getWorkbench().getActiveWorkbenchWindow().getShell(),
 				getModel(viewpoints), selectedViewpoints, "Select viewpoints",
 				Activator.getViewpointImage(), 450, 400);
 		dialog.open();
 
 		if (dialog.isCanceled()) {
 			return;
 		}
 
 		Set<EClass> elementsToShow = Sets.newHashSet();
 		for (ViewpointID selectedViewpoint : selectedViewpoints) {
 			Map<String, List<EClass>> v = viewpoints.get(selectedViewpoint
 					.getViewpointType());
 			if (v != null) {
 				List<EClass> vv = v.get(selectedViewpoint.getViewpointName());
 				if (vv != null) {
 					elementsToShow.addAll(vv);
 					
 				}
 			}
 		}
 		
 		Map<EClass, List<GraphicalElement>> graphicalElements = BasicDiagramUtil
 				.getGraphicalElements(diagram);
 		
 		for (List<GraphicalElement> l : graphicalElements.values()) {
 			for (GraphicalElement ge : l) {
 				if (elementsToShow.size() == 0) {
 					ge.setHidden(false);
 				} else {
 					if (!(elementsToShow.contains(ge.getSemanticElement()
 							.eClass()))) {
 						ge.setHidden(true);
 					} else {
 						ge.setHidden(false);
 					}
 				}
 			}
 		}
 
 		Iterator<Layer> layers = diagram.getDescription().getAllLayers()
 				.iterator();
 
 		IEditorPart ep = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 				.getActivePage().getActiveEditor();
 		if (ep instanceof DDiagramEditor) {
 			DDiagramEditor diagramEditor = (DDiagramEditor) ep;
 
 			List<Layer> layersToAdd = new ArrayList<Layer>();
 			List<Layer> layersToRemove = new ArrayList<Layer>();
 			while (layers.hasNext()) {
 				Layer layer = layers.next();
 				if (layer instanceof OptionalLayer) {
 					boolean found=false;
 					Iterator<ViewpointID> it = selectedViewpoints.iterator();
 					while(!found && it.hasNext()){
 						ViewpointID vpID = it.next();
 						found = layer.getName().equals(vpID.getViewpointName());
 					}
 					if (found) {
 						layersToAdd.add(layer);
 					} else {
 						layersToRemove.add(layer);
 					}
 				}
 			}
 			for (Layer layer : layersToAdd) {
 				ChangeLayerActivationCommand changeLayerActivationCommand = new ChangeLayerActivationCommand(
 						diagramEditor.getSession()
 								.getTransactionalEditingDomain(),
 								diagram, layer);
 				diagramEditor.getSession().getTransactionalEditingDomain()
 						.getCommandStack()
 						.execute(changeLayerActivationCommand);
 			}
 			for (Layer layer : layersToRemove) {
 				ChangeLayerActivationCommand changeLayerActivationCommand = new ChangeLayerActivationCommand(
 						diagramEditor.getSession()
 								.getTransactionalEditingDomain(),
 								diagram, layer);
 				diagramEditor.getSession().getTransactionalEditingDomain()
 						.getCommandStack()
 						.execute(changeLayerActivationCommand);
 			}
 
 			for (Layer layer : layersToAdd) {
 				if (layer instanceof OptionalLayer) {
 					diagramEditor.getPaletteManager().showLayer(layer);
 				}
 			}
 			for (Layer layer : layersToRemove) {
 				if (layer instanceof OptionalLayer) {
 					diagramEditor.getPaletteManager().hideLayer(layer);
 				}
 			}
 
 		}
		diagram.refresh();
 
 	}
 
 	private final Map<String, Set<ViewpointID>> getModel(
 			Map<String, Map<String, List<EClass>>> viewpoints) {
 		Map<String, Set<ViewpointID>> result = Maps.newTreeMap();
 		for (Entry<String, Map<String, List<EClass>>> viewpoint : viewpoints
 				.entrySet()) {
 			Set<ViewpointID> set = Sets.newTreeSet(new Comparator<ViewpointID>() {
 				@Override
 				public int compare(ViewpointID o1, ViewpointID o2) {
 					return o1.getViewpointName().compareTo(o2.getViewpointName());
 				}
 			});
 			for (Entry<String, List<EClass>> entry : viewpoint.getValue()
 					.entrySet()) {
 				set.add(new ViewpointID(viewpoint.getKey(), entry.getKey()));
 			}
 			result.put(viewpoint.getKey(), set);
 		}
 		return result;
 	}
 
 	public abstract Map<String, Map<String, List<EClass>>> getViewpoints();
 
 }
