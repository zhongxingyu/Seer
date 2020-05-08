 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  * All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  *
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.bpmn2.modeler.ui.views.outline;
 
 import java.util.List;
 
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.di.BPMNDiagram;
 import org.eclipse.bpmn2.modeler.core.di.DIUtils;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.bpmn2.modeler.ui.util.PropertyUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.gef.editparts.AbstractTreeEditPart;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * 
  */
 public class AbstractGraphicsTreeEditPart extends AbstractTreeEditPart {
 
 	DiagramTreeEditPart diagramEditPart;
 	
 	public AbstractGraphicsTreeEditPart(DiagramTreeEditPart dep, Object model) {
 		super(model);
 		diagramEditPart = dep;
 	}
 
 	protected void setDiagramEditPart(DiagramTreeEditPart dep) {
 		diagramEditPart = dep;
 	}
 
 	@Override
 	public Object getAdapter(Class key) {
 		if (PictogramElement.class==key) {
 			EObject bpmnModel = (EObject)super.getModel();
 			if (bpmnModel instanceof BPMNDiagram) {
 				BPMNDiagram bpmnDiagram = (BPMNDiagram)bpmnModel;
 				bpmnModel = bpmnDiagram.getPlane().getBpmnElement();
 			}
 			
 			if (diagramEditPart!=null) {
 				// the model is actually a BPMN element - convert this
 				// to a PictogramElement for the SelectionSynchronizer
 				for (Diagram diagram : diagramEditPart.getAllDiagrams()) {
 					if (diagram!=null) {
 						List<PictogramElement> pes = Graphiti.getLinkService().getPictogramElements(diagram, bpmnModel);
 						for (PictogramElement pe : pes) {
 							if (pe instanceof ContainerShape)
 								return pe;
 							if (pe instanceof FreeFormConnection)
 								return pe;
 						}
 					}
 				}
 			}
 		}
 		return super.getAdapter(key);
 	}
 
 	protected void refreshChildren() {
 		if (children!=null) {
 			for (Object child : children) {
 				if (child instanceof AbstractGraphicsTreeEditPart) {
 					((AbstractGraphicsTreeEditPart)child).refreshChildren();
 				}
 			}
 		}
 		super.refreshChildren();
 	}
 	
 	/**
 	 * This method is called from refreshVisuals(), to display the image of the
 	 * TreeItem.
 	 * <p>
 	 * By default this method displays the image of the FIRST attribute of the
 	 * ModelObject as the TreeItem.
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
 	 */
 	@Override
 	protected Image getImage() {
 		EObject o = (EObject)getModel();
 		return PropertyUtil.getImage(o);
 	}
 
 	/**
 	 * This method is called by refreshVisuals(), to display the text of the
 	 * TreeItem.
 	 * <p>
 	 * By default this method displays the FIRST attribute of the model Object
 	 * as the TreeItem.
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
 	 */
 	@Override
 	protected String getText() {
 		String text = null;
 		if (getModel() instanceof EObject) {
 			EObject o = (EObject)getModel();
 			text = getText(o);
 		}
 		return text == null ? "" : text;
 	}
 	
 	protected String getText(EObject o) {
 		if (o==null)
 			return "";
 		String text = ModelUtil.getDisplayName(o);
 		if (text==null || text.isEmpty()) {
 			EStructuralFeature f = o.eClass().getEStructuralFeature("id");
 			if (f!=null)
 				text = o.eGet(f).toString();
 		}
 		return text;
 	}
 }
