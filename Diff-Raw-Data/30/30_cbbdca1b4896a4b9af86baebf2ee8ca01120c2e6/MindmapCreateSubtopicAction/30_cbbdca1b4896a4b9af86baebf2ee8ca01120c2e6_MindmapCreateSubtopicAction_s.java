/*******************************************************************************
 * Copyright (c) 2006 Borland Software Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *     Borland Software Corporation - initial API and implementation
 ******************************************************************************/
 package org.eclipse.gmf.examples.mindmap.diagram.part;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPartViewer;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CompoundCommand;
 import org.eclipse.gmf.examples.mindmap.diagram.edit.parts.MapEditPart;
 import org.eclipse.gmf.examples.mindmap.diagram.edit.parts.TopicEditPart;
 import org.eclipse.gmf.examples.mindmap.diagram.providers.MindmapElementTypes;
 import org.eclipse.gmf.runtime.common.core.command.ICommand;
 import org.eclipse.gmf.runtime.diagram.ui.commands.DeferredCreateConnectionViewAndElementCommand;
 import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewAndElementRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequestFactory;
 import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
 import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
 import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 public class MindmapCreateSubtopicAction implements IObjectActionDelegate {
 
 	public final static String ID = "org.eclipse.gmf.examples.mindmap.popup.MindmapCreateSubtopicActionID";
 
 	private TopicEditPart selectedElement;
 
 	public void run(IAction action) {
 		CompoundCommand cc = new CompoundCommand("Create Subtopic and Link");
 
 		// Create the new topic for the other end.
 		CreateViewRequest topicRequest = CreateViewRequestFactory.getCreateShapeRequest(MindmapElementTypes.Topic_1001, selectedElement.getDiagramPreferencesHint());
 
 		Point p = selectedElement.getFigure().getBounds().getTopRight().getCopy();
 		selectedElement.getFigure().translateToAbsolute(p);
 		int edgeCount = selectedElement.getNotationView().getSourceEdges().size();
 		// A quick hack to get subtopics to layout to the right, from top to bottom
 		int offset = (edgeCount * 50) - 100;
 		topicRequest.setLocation(p.translate(100, offset));
 
 		MapEditPart mapEditPart = (MapEditPart) selectedElement.getParent();
 		Command createTopicCmd = mapEditPart.getCommand(topicRequest);
 		IAdaptable topicViewAdapter = (IAdaptable) ((List) topicRequest.getNewObject()).get(0);
 
 		cc.add(createTopicCmd);
 
 		// create the subtopics link command
 		ICommand createSubTopicsCmd = new DeferredCreateConnectionViewAndElementCommand(new CreateConnectionViewAndElementRequest(MindmapElementTypes.TopicSubtopics_3001,
 				((IHintedType) MindmapElementTypes.TopicSubtopics_3001).getSemanticHint(), selectedElement.getDiagramPreferencesHint()), new EObjectAdapter((EObject) selectedElement.getModel()),
 				topicViewAdapter, selectedElement.getViewer());
 
 		cc.add(new ICommandProxy(createSubTopicsCmd));
 
 		selectedElement.getDiagramEditDomain().getDiagramCommandStack().execute(cc);
 
 		// put the new topic in edit mode
 		final EditPartViewer viewer = selectedElement.getViewer();
 		final EditPart elementPart = (EditPart) viewer.getEditPartRegistry().get(topicViewAdapter.getAdapter(View.class));
 		if (elementPart != null) {
 			Display.getCurrent().asyncExec(new Runnable() {
 
 				public void run() {
 					viewer.setSelection(new StructuredSelection(elementPart));
 					Request der = new Request(RequestConstants.REQ_DIRECT_EDIT);
 					elementPart.performRequest(der);
 				}
 			});
 		}
 	}
 
 	public void selectionChanged(IAction action, ISelection selection) {
 		selectedElement = null;
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 			if (structuredSelection.getFirstElement() instanceof TopicEditPart) {
 				selectedElement = (TopicEditPart) structuredSelection.getFirstElement();
 			}
 		}
 	}
 
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 	}
 
 }
