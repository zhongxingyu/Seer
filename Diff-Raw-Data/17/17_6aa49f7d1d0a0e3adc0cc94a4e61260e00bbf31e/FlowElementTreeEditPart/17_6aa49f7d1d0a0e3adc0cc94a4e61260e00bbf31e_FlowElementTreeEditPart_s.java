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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.bpmn2.FlowElement;
 import org.eclipse.bpmn2.FlowElementsContainer;
 import org.eclipse.bpmn2.FlowNode;
 import org.eclipse.bpmn2.Lane;
 import org.eclipse.bpmn2.LaneSet;
 import org.eclipse.bpmn2.SequenceFlow;
 
 public class FlowElementTreeEditPart extends AbstractGraphicsTreeEditPart {
 	
 	public FlowElementTreeEditPart(DiagramTreeEditPart dep, FlowElement flowElement) {
 		super(dep, flowElement);
 	}
 
 	public FlowElement getFlowElement() {
 		return (FlowElement) getModel();
 	}
 
 	// ======================= overwriteable behaviour ========================
 
 	/**
 	 * Creates the EditPolicies of this EditPart. Subclasses often overwrite
 	 * this method to change the behaviour of the editpart.
 	 */
 	@Override
 	protected void createEditPolicies() {
 	}
 
 	@Override
 	protected List<Object> getModelChildren() {
 		List<Object> retList = new ArrayList<Object>();
 		FlowElement elem = getFlowElement();
 
 		if (elem instanceof FlowElementsContainer) {
 			FlowElementsContainer container = (FlowElementsContainer)elem;
 			return getFlowElementsContainerChildren(container);
 		}
 		return retList;
 	}
 	
 	public static List<Object> getFlowElementsContainerChildren(FlowElementsContainer container) {
 		List<Object> retList = new ArrayList<Object>();
 		if (container.getLaneSets().size()==0)
 			retList.addAll(container.getFlowElements());
 		else {
 			for (LaneSet ls : container.getLaneSets()) {
 				retList.addAll(ls.getLanes());
 			}
 			// only add the flow element if it's not contained in a Lane
 			List<Object> laneElements = new ArrayList<Object>();
 			for (FlowElement fe : container.getFlowElements()) {
 				boolean inLane = false;
 				for (LaneSet ls : container.getLaneSets()) {
 					if (isInLane(fe,ls)) {
 						inLane = true;
 						break;
 					}
 				}
 				if (inLane)
 					laneElements.add(fe);
 				else
 					retList.add(fe);
 			}
 			
 			// don't include any sequence flows that connect flow
 			// nodes that are contained in Lanes
 			List<SequenceFlow> flows = new ArrayList<SequenceFlow>();
 			for (Object fn : laneElements) {
 				if (fn instanceof FlowNode) {
 					for (SequenceFlow sf : ((FlowNode)fn).getIncoming()) {
 						if (
 								laneElements.contains(sf.getSourceRef()) &&
 								laneElements.contains(sf.getTargetRef()) &&
 								!flows.contains(sf)) {
 							flows.add(sf);
 						}
 					}
 					for (SequenceFlow sf : ((FlowNode)fn).getOutgoing()) {
 						if (
 								laneElements.contains(sf.getSourceRef()) &&
 								laneElements.contains(sf.getTargetRef()) &&
 								!flows.contains(sf)) {
 							flows.add(sf);
 						}
 					}
 				}
 			}
 			retList.removeAll(flows);
 		}
 		return retList;
 	}
 	
 	public static boolean isInLane(FlowElement fe, LaneSet ls) {
 		if (ls==null || ls.getLanes().size()==0)
 			return false;
 		
 		for (Lane ln : ls.getLanes()) {
 			if (ln.getFlowNodeRefs().contains(fe))
 				return true;
 			if (isInLane(fe, ln.getChildLaneSet()))
 				return true;
 		}
 		return false;
 	}
 }
