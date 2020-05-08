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
 package org.eclipse.bpmn2.modeler.core.validation;
 
 import java.util.List;
 
 import org.eclipse.bpmn2.Assignment;
 import org.eclipse.bpmn2.Association;
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.Bpmn2Package;
 import org.eclipse.bpmn2.CallActivity;
 import org.eclipse.bpmn2.CatchEvent;
 import org.eclipse.bpmn2.ChoreographyActivity;
 import org.eclipse.bpmn2.ChoreographyTask;
 import org.eclipse.bpmn2.CompensateEventDefinition;
 import org.eclipse.bpmn2.ComplexGateway;
 import org.eclipse.bpmn2.ConditionalEventDefinition;
 import org.eclipse.bpmn2.DataAssociation;
 import org.eclipse.bpmn2.DataObject;
 import org.eclipse.bpmn2.Definitions;
 import org.eclipse.bpmn2.EndEvent;
 import org.eclipse.bpmn2.Error;
 import org.eclipse.bpmn2.ErrorEventDefinition;
 import org.eclipse.bpmn2.Escalation;
 import org.eclipse.bpmn2.EscalationEventDefinition;
 import org.eclipse.bpmn2.EventBasedGateway;
 import org.eclipse.bpmn2.EventDefinition;
 import org.eclipse.bpmn2.ExclusiveGateway;
 import org.eclipse.bpmn2.FlowElement;
 import org.eclipse.bpmn2.FlowNode;
 import org.eclipse.bpmn2.FormalExpression;
 import org.eclipse.bpmn2.Gateway;
 import org.eclipse.bpmn2.GatewayDirection;
 import org.eclipse.bpmn2.Import;
 import org.eclipse.bpmn2.InclusiveGateway;
 import org.eclipse.bpmn2.InputOutputSpecification;
 import org.eclipse.bpmn2.InteractionNode;
 import org.eclipse.bpmn2.Interface;
 import org.eclipse.bpmn2.ItemDefinition;
 import org.eclipse.bpmn2.Message;
 import org.eclipse.bpmn2.MessageEventDefinition;
 import org.eclipse.bpmn2.MessageFlow;
 import org.eclipse.bpmn2.Operation;
 import org.eclipse.bpmn2.ParallelGateway;
 import org.eclipse.bpmn2.Process;
 import org.eclipse.bpmn2.Resource;
 import org.eclipse.bpmn2.ScriptTask;
 import org.eclipse.bpmn2.SendTask;
 import org.eclipse.bpmn2.SequenceFlow;
 import org.eclipse.bpmn2.Signal;
 import org.eclipse.bpmn2.SignalEventDefinition;
 import org.eclipse.bpmn2.StartEvent;
 import org.eclipse.bpmn2.ThrowEvent;
 import org.eclipse.bpmn2.TimerEventDefinition;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.validation.AbstractModelConstraint;
 import org.eclipse.emf.validation.EMFEventType;
 import org.eclipse.emf.validation.IValidationContext;
 
 public class BPMN2BatchValidationConstraint extends AbstractModelConstraint {
 
 	public final static String ERROR_ID = "org.eclipse.bpmn2.modeler.core.validation.error";
 	public final static String WARNING_ID = "org.eclipse.bpmn2.modeler.core.validation.warning";
 	
 	private boolean warnings = false;
 	
 	public BPMN2BatchValidationConstraint() {
 	}
 
 	@Override
 	public IStatus validate(IValidationContext ctx) {
 		EObject eObj = ctx.getTarget();
 		EMFEventType eType = ctx.getEventType();
 		String id = ctx.getCurrentConstraintId();
 		if (WARNING_ID.equals(id))
 			warnings = true;
 		else
 			warnings = false;
 		
 		// In the case of batch mode.
 		if (eType == EMFEventType.NULL) {
 			if (eObj instanceof Definitions) {
 				return validateDefinitions(ctx, (Definitions) eObj);
 			}
 			if (eObj instanceof BaseElement) {
 				return validateBaseElement(ctx, (BaseElement) eObj);
 			}
 		} else { // In the case of live mode.
 		}
 
 		return ctx.createSuccessStatus();
 	}
 	
 	private IStatus validateDefinitions(IValidationContext ctx, Definitions def) {
 		if (def.getTargetNamespace()==null || def.getTargetNamespace().isEmpty()) {
 			if (warnings) {
 			}
 			else {
 				ctx.addResult(def);
 				return ctx.createFailureStatus("No targetNamespace defined");
 			}			
 		}
 		
 		return ctx.createSuccessStatus();
 	}
 
 	public IStatus createFailureStatus(IValidationContext ctx, EObject object, Object... messageArgs) {
 		IStatus status = ctx.createFailureStatus(messageArgs);
 		ctx.addResult(object);
 		return status;
 	}
 
 	public IStatus createMissingFeatureStatus(IValidationContext ctx, EObject object, String featureName) {
 		EStructuralFeature feature = object.eClass().getEStructuralFeature(featureName);
 		String message = ModelUtil.getLabel(object) + " has no " + ModelUtil.getLabel(object, feature);
 		IStatus status = ctx.createFailureStatus(message);
 		ctx.addResult(object);
 		return status;
 	}
 
 	private IStatus validateBaseElement(IValidationContext ctx, BaseElement be) {
 
 		if (be instanceof Process) {
 			Process process = (Process) be;
 
 			if (warnings) {
 				// report warnings only
 				boolean foundStartEvent = false;
 				boolean foundEndEvent = false;
 				List<FlowElement> flowElements = process.getFlowElements();
 				for (FlowElement fe : flowElements) {
 					if (fe instanceof StartEvent) {
 						foundStartEvent = true;
 					}
 					if (fe instanceof EndEvent) {
 						foundEndEvent = true;
 					}
 				}
 				if (!foundStartEvent) {
 					return createFailureStatus(ctx, be, "Process has no Start Event");
 				}
 				if (!foundEndEvent) {
 					return createFailureStatus(ctx, be, "Process has no End Event");
 				}
 				if (isEmpty(process.getName())) {
 					return createMissingFeatureStatus(ctx,be,"name");
 				}
 			}
 			else {
 				// report errors only
 			}
 		}
 		else if (be instanceof Import) {
 			Import elem = (Import)be;
 			if (warnings) {
 			}
 			else {
 				if (isEmpty(elem.getLocation())) {
 					return createMissingFeatureStatus(ctx,be,"location");
 				}
 				if (isEmpty(elem.getNamespace())) {
 					return createMissingFeatureStatus(ctx,be,"namespace");
 				}
 				if (isEmpty(elem.getImportType())) {
 					return createMissingFeatureStatus(ctx,be,"importType");
 				}
 			}
 		}
 		else if (be instanceof Error) {
 			if (warnings) {
 				if (((Error)be).getStructureRef()==null) {
 					return createMissingFeatureStatus(ctx,be,"structureRef");
 				}
 			}
 		}
 		else if (be instanceof Escalation) {
 			if (warnings) {
 				if (((Escalation)be).getStructureRef()==null) {
 					return createMissingFeatureStatus(ctx,be,"structureRef");
 				}
 			}
 		}
 		else if (be instanceof Message) {
 			if (warnings) {
 				if (((Message)be).getItemRef()==null) {
 					return createMissingFeatureStatus(ctx,be,"itemRef");
 				}
 			}
 		}
 		else if (be instanceof Signal) {
 			if (warnings) {
 				if (((Signal)be).getStructureRef()==null) {
 					return createMissingFeatureStatus(ctx,be,"structureRef");
 				}
 			}
 		}
 		else if (be instanceof ItemDefinition) {
 			if (!warnings) {
 				if (((ItemDefinition)be).getStructureRef()==null) {
 					return createMissingFeatureStatus(ctx,be,"structureRef");
 				}
 			}
 		}
 		else if (be instanceof StartEvent) {
 			StartEvent elem = (StartEvent) be;
 			
 			if (!warnings) {
 				if (elem.getOutgoing() == null || elem.getOutgoing().size() < 1) {
 					return createMissingFeatureStatus(ctx,be,"outgoing");
 				}
 			}
 		}
 		else if (be instanceof EndEvent) {
 			EndEvent elem = (EndEvent) be;
 			
 			if (!warnings) {
 				if (elem.getIncoming() == null || elem.getIncoming().size() < 1) {
 					return createMissingFeatureStatus(ctx,be,"incoming");
 				}
 			}
 		}
 		else if (be instanceof ScriptTask) {
 			ScriptTask elem = (ScriptTask) be;
 			
 			if (warnings) {
 				if (isEmpty(elem.getScript())) {
 					return createMissingFeatureStatus(ctx,be,"script");
 				}
 				if (isEmpty(elem.getScriptFormat())) {
 					return createMissingFeatureStatus(ctx,be,"scriptFormat");
 				}
 			}
 		}
 		else if (be instanceof SendTask) {
 			SendTask elem = (SendTask) be;
 
 			if (!warnings) {
 				if (elem.getOperationRef() == null) {
 					return createMissingFeatureStatus(ctx,be,"operationRef");
 				}
 				if (elem.getMessageRef() == null) {
 					return createMissingFeatureStatus(ctx,be,"messageRef");
 				}
 			}
 		}
 		else if (be instanceof CatchEvent) {
 			CatchEvent elem = (CatchEvent) be;
 
 			if (warnings) {
 				if (elem.getOutgoing() == null || elem.getOutgoing().size() < 1) {
 					return createMissingFeatureStatus(ctx,be,"outgoing");
 				}
 			}
 			else {
 				List<EventDefinition> eventdefs = elem.getEventDefinitions();
 				if (eventdefs.size()==0) {
 					return createMissingFeatureStatus(ctx,be,"eventDefinitions");
 				}
 				
 				for (EventDefinition ed : eventdefs) {
 					if (ed instanceof TimerEventDefinition) {
 						TimerEventDefinition ted = (TimerEventDefinition) ed;
 						if (	ted.getTimeDate() == null
 								&& ted.getTimeDuration() == null
 								&& ted.getTimeCycle() == null
 						) {
 							return createFailureStatus(ctx,be,"Timer Event has no Timer definition");
 						}
 					} else if (ed instanceof SignalEventDefinition) {
 						if (((SignalEventDefinition) ed).getSignalRef() == null) {
 							return createFailureStatus(ctx,be,"Signal Event has no Signal definition");
 						}
 					} else if (ed instanceof ErrorEventDefinition) {
 						if (((ErrorEventDefinition) ed).getErrorRef() == null) {
 							return createFailureStatus(ctx,be,"Error Event has no Error definition");
 						}
 					} else if (ed instanceof ConditionalEventDefinition) {
 						FormalExpression conditionalExp = (FormalExpression) ((ConditionalEventDefinition) ed)
 								.getCondition();
 						if (conditionalExp==null || conditionalExp.getBody() == null) {
 							return createFailureStatus(ctx,be,"Conditional Event has no Condition Expression");
 						}
 					} else if (ed instanceof EscalationEventDefinition) {
 						if (((EscalationEventDefinition) ed).getEscalationRef() == null) {
 							return createFailureStatus(ctx,be,"Escalation Event has no Escalation definition");
 						}
 					} else if (ed instanceof MessageEventDefinition) {
 						if (((MessageEventDefinition) ed).getMessageRef() == null) {
 							return createFailureStatus(ctx,be,"Message Event has no Message definition");
 						}
 					} else if (ed instanceof CompensateEventDefinition) {
 						if (((CompensateEventDefinition) ed).getActivityRef() == null) {
 							return createFailureStatus(ctx,be,"Compensate Event has no Activity definition");
 						}
 					}
 				}
 			}
 			// no more validations on this
 			be = null;
 		}
 		else if (be instanceof ThrowEvent) {
 			ThrowEvent elem = (ThrowEvent) be;
 
 
 			if (warnings) {
 				if (elem.getOutgoing() == null || elem.getOutgoing().size() < 1) {
 					return createMissingFeatureStatus(ctx,be,"outgoing");
 				}
 			}
 			else {
 				List<EventDefinition> eventdefs = elem.getEventDefinitions();
 				if (eventdefs.size()==0) {
 					return createMissingFeatureStatus(ctx,be,"eventDefinitions");
 				}
 
 				for (EventDefinition ed : eventdefs) {
 					if (ed instanceof TimerEventDefinition) {
 						TimerEventDefinition ted = (TimerEventDefinition) ed;
 						if (	ted.getTimeDate() == null
 								&& ted.getTimeDuration() == null
 								&& ted.getTimeCycle() == null
 						) {
 							return createFailureStatus(ctx,be,"Timer Event has no Timer definition");
 						}
 					} else if (ed instanceof SignalEventDefinition) {
 						if (((SignalEventDefinition) ed).getSignalRef() == null) {
 							return createFailureStatus(ctx,be,"Signal Event has no Signal definition");
 						}
 					} else if (ed instanceof ErrorEventDefinition) {
 						if (((ErrorEventDefinition) ed).getErrorRef() == null) {
 							return createFailureStatus(ctx,be,"Error Event has no Error definition");
 						}
 					} else if (ed instanceof ConditionalEventDefinition) {
 						FormalExpression conditionalExp = (FormalExpression) ((ConditionalEventDefinition) ed)
 								.getCondition();
 						if (conditionalExp==null || conditionalExp.getBody() == null) {
 							return createFailureStatus(ctx,be,"Conditional Event has no Condition Expression");
 						}
 					} else if (ed instanceof EscalationEventDefinition) {
 						if (((EscalationEventDefinition) ed).getEscalationRef() == null) {
 							return createFailureStatus(ctx,be,"Escalation Event has no conditional Escalation definition");
 						}
 					} else if (ed instanceof MessageEventDefinition) {
 						if (((MessageEventDefinition) ed).getMessageRef() == null) {
 							return createFailureStatus(ctx,be,"Message Event has no Message definition");
 						}
 					} else if (ed instanceof CompensateEventDefinition) {
 						if (((CompensateEventDefinition) ed).getActivityRef() == null) {
 							return createFailureStatus(ctx,be,"Compensate Event has no Activity definition");
 						}
 					}
 				}
 			}
 			// no more validations on this
 			be = null;
 		}
 		else if (be instanceof SequenceFlow) {
 			SequenceFlow elem = (SequenceFlow) be;
 
 			if (!warnings) {
 				if (elem.getSourceRef() == null) {
 					return createMissingFeatureStatus(ctx,be,"sourceRef");
 				}
 				if (elem.getTargetRef() == null) {
 					return createMissingFeatureStatus(ctx,be,"targetRef");
 				}
 			}
 		}
 		else if (be instanceof Association) {
 			Association elem = (Association) be;
 
 			if (!warnings) {
 				if (elem.getSourceRef() == null) {
 					return createMissingFeatureStatus(ctx,be,"sourceRef");
 				}
 				if (elem.getTargetRef() == null) {
 					return createMissingFeatureStatus(ctx,be,"targetRef");
 				}
 			}
 		}
 		else if (be instanceof Gateway) {
 			Gateway elem = (Gateway) be;
 
 			if (!warnings) {
 				if (elem.getGatewayDirection() == null
 						|| elem.getGatewayDirection().getValue() == GatewayDirection.UNSPECIFIED.getValue()) {
 					ctx.addResult(Bpmn2Package.eINSTANCE.getGateway_GatewayDirection());
 					return createMissingFeatureStatus(ctx,be,"gatewayDirection");
 				}
 				if (elem instanceof ExclusiveGateway) {
 					if (elem.getGatewayDirection().getValue() != GatewayDirection.DIVERGING.getValue()
 							&& elem.getGatewayDirection().getValue() != GatewayDirection.CONVERGING.getValue()) {
 						return createFailureStatus(ctx,be,
 								"Invalid Gateway direction for Exclusing Gateway. It should be 'Converging' or 'Diverging'");
 					}
 				}
 				if (elem instanceof EventBasedGateway) {
 					if (elem.getGatewayDirection().getValue() != GatewayDirection.DIVERGING.getValue()) {
 						return createFailureStatus(ctx,be,
 								"Invalid Gateway direction for EventBased Gateway. It should be 'Diverging'");
 					}
 				}
 				if (elem instanceof ParallelGateway) {
 					if (elem.getGatewayDirection().getValue() != GatewayDirection.DIVERGING.getValue()
 							&& elem.getGatewayDirection().getValue() != GatewayDirection.CONVERGING.getValue()) {
 						return createFailureStatus(ctx,be,
 								"Invalid Gateway direction for Parallel Gateway. It should be 'Converging' or 'Diverging'");
 					}
 				}
 				if (elem instanceof InclusiveGateway) {
 					if (elem.getGatewayDirection().getValue() != GatewayDirection.DIVERGING.getValue()
 							&& elem.getGatewayDirection().getValue() != GatewayDirection.CONVERGING.getValue()) {
 						return createFailureStatus(ctx,be,
 								"Invalid Gateway direction for Inclusive Gateway. It should be 'Converging' or 'Diverging'");
 					}
 				}
 				if (elem instanceof ComplexGateway) {
 					if (elem.getGatewayDirection().getValue() != GatewayDirection.DIVERGING.getValue()
 							&& elem.getGatewayDirection().getValue() != GatewayDirection.CONVERGING.getValue()) {
 						return createFailureStatus(ctx,be,
 								"Invalid Gateway direction for Complex Gateway. It should be 'Converging' or 'Diverging'");
 					}
 				}
 			}
 		}
 		else if (be instanceof CallActivity) {
 			CallActivity elem = (CallActivity) be;
 
 			if (!warnings) {
 				if (elem.getCalledElementRef() == null) {
 					return createMissingFeatureStatus(ctx,be,"calledElementRef");
 				}
 			}
 		}
 		else if (be instanceof DataObject) {
 			DataObject elem = (DataObject) be;
 
 			if (!warnings) {
 				if (elem.getName() == null || elem.getName().length() < 1) {
 					return createMissingFeatureStatus(ctx,be,"name");
 				}
 			}
 		}
 		else if (be instanceof Interface) {
 			Interface elem = (Interface)be;
 			if (warnings) {
 			}
 			else {
 				if (isEmpty(elem.getOperations())) {
 					return createMissingFeatureStatus(ctx,be,"operations");
 				}
 				if (isEmpty(elem.getName())) {
 					return createMissingFeatureStatus(ctx,be,"name");
 				}
 			}
 		}
 		else if (be instanceof Operation) {
 			Operation elem = (Operation)be;
 			if (warnings) {
 			}
 			else {
 				if (isEmpty(elem.getInMessageRef())) {
 					return createMissingFeatureStatus(ctx,be,"inMessageRef");
 				}
 				if (isEmpty(elem.getName())) {
 					return createMissingFeatureStatus(ctx,be,"name");
 				}
 			}
 		}
 		else if (be instanceof DataAssociation) {
 			DataAssociation elem = (DataAssociation)be;
 			if (warnings) {
 			}
 			else {
				if (isEmpty(elem.getTargetRef()) && elem.getAssignment().size()==0 && elem.getTransformation()==null) {
 					return createMissingFeatureStatus(ctx,be,"targetRef");
 				}
 			}
 		}
 		else if (be instanceof Assignment) {
 			Assignment elem = (Assignment)be;
 			if (warnings) {
 			}
 			else {
 				if (isEmpty(elem.getFrom())) {
 					return createMissingFeatureStatus(ctx,be,"from");
 				}
 				if (isEmpty(elem.getTo())) {
 					return createMissingFeatureStatus(ctx,be,"to");
 				}
 			}
 		}
 		else if (be instanceof InputOutputSpecification) {
 			InputOutputSpecification elem = (InputOutputSpecification)be;
 			if (warnings) {
 			}
 			else {
 				if (isEmpty(elem.getInputSets())) {
 					return createMissingFeatureStatus(ctx,be,"inputSets");
 				}
 				if (isEmpty(elem.getOutputSets())) {
 					return createMissingFeatureStatus(ctx,be,"outputSets");
 				}
 			}
 		}
 		else if (be instanceof ChoreographyActivity) {
 			ChoreographyActivity elem = (ChoreographyActivity)be;
 			if (elem.getParticipantRefs().size()<2) {
 				return createFailureStatus(ctx,be,"ChoreographyActivity must have at least two Participants");
 			}
 			if (elem.getInitiatingParticipantRef()==null) {
 				return createFailureStatus(ctx,be,"ChoreographyActivity has no initiating Participant");
 			}
 		}
 		else if (be instanceof Resource) {
 			Resource elem = (Resource)be;
 			if (isEmpty(elem.getName())) {
 				return createMissingFeatureStatus(ctx,be,"name");
 			}
 		}
 		else if (be instanceof ChoreographyTask) {
 			ChoreographyTask elem = (ChoreographyTask)be;
 			for (MessageFlow mf : elem.getMessageFlowRef()) {
 				InteractionNode in = mf.getSourceRef();
 				if (!elem.getParticipantRefs().contains(in)) {
 					createFailureStatus(ctx,be,"Message Flow source is not a Participant of the Choreography Task");
 				}
 				in = mf.getTargetRef();
 				if (!elem.getParticipantRefs().contains(in)) {
 					createFailureStatus(ctx,be,"Message Flow target is not a Participant of the Choreography Task");
 				}
 			}
 		}
 		
 		
 		if (be instanceof FlowNode) {
 			return validateFlowNode(ctx, (FlowNode) be);
 		}
 
 		return ctx.createSuccessStatus();
 	}
 
 	private IStatus validateFlowNode(IValidationContext ctx, FlowNode fn) {
 		if (!warnings) {
 			boolean needIncoming = true;
 			boolean needOutgoing = true;
 			if (fn instanceof ThrowEvent)
 				needOutgoing = false;
 			if (fn instanceof CatchEvent)
 				needIncoming = false;
 			
 			if (needOutgoing) {
 				if ((fn.getOutgoing() == null || fn.getOutgoing().size() < 1)) {
 					return createMissingFeatureStatus(ctx,fn,"outgoing");
 				}
 			}
 			if (needIncoming) {
 				if ((fn.getIncoming() == null || fn.getIncoming().size() < 1)) {
 					return createMissingFeatureStatus(ctx,fn,"incoming");
 				}
 			}
 		}
 
 		return ctx.createSuccessStatus();
 	}
 
 	private static boolean isEmpty(Object object) {
 		if (object instanceof String) {
 			String str = (String) object;
 			return str == null || str.isEmpty();
 		}
 		else if (object instanceof List) {
 			return ((List)object).isEmpty();
 		}
 		else if (object==null)
 			return true;
 		return false;
 	}
 
 	private boolean containsWhiteSpace(String testString) {
 		if (testString != null) {
 			for (int i = 0; i < testString.length(); i++) {
 				if (Character.isWhitespace(testString.charAt(i))) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
