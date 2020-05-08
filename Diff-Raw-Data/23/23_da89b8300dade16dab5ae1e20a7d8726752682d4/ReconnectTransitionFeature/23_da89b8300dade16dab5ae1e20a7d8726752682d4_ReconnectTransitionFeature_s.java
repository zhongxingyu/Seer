 package com.vectorsf.jvoice.diagram.core.pattern.transition;
 
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
 import org.eclipse.graphiti.features.impl.DefaultReconnectionFeature;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 
 import com.vectorsf.jvoice.diagram.core.features.CoreFeatureProvider;
 import com.vectorsf.jvoice.model.operations.FinalState;
 import com.vectorsf.jvoice.model.operations.InitialState;
 import com.vectorsf.jvoice.model.operations.InputState;
 import com.vectorsf.jvoice.model.operations.MenuState;
 import com.vectorsf.jvoice.model.operations.PromptState;
 import com.vectorsf.jvoice.model.operations.State;
 import com.vectorsf.jvoice.model.operations.SwitchState;
 import com.vectorsf.jvoice.model.operations.Transition;
 
 public class ReconnectTransitionFeature extends DefaultReconnectionFeature {
 
 	private CoreFeatureProvider fp;
 	private State oldTargetState;
 	private Transition transition;
 
 	public ReconnectTransitionFeature(CoreFeatureProvider fp) {
 		super(fp);
 		this.fp = fp;
 	}
 
 	@Override
 	public CoreFeatureProvider getFeatureProvider() {
 		return fp;
 	}
 
 	@Override
 	public boolean canReconnect(IReconnectionContext context) {
 
 		PictogramElement targetPictogramElement = context
 				.getTargetPictogramElement();
 
 		if (context.getReconnectType().equals(
 				ReconnectionContext.RECONNECT_SOURCE)) {
 
			Connection connection = context.getConnection();
			transition = (Transition) getBusinessObjectForPictogramElement(connection);
			oldTargetState = transition.getSource();

 			if (!(targetPictogramElement instanceof ContainerShape)) {
 				return false;
 			}
 
 			Object targetBO = getBusinessObjectForPictogramElement(targetPictogramElement);
 
 			if (targetBO instanceof MenuState) {
 				return false;
 			} else if (oldTargetState instanceof InputState
 					|| targetBO instanceof InputState) {
 				return false;
 			} else if (oldTargetState instanceof PromptState
 					|| targetBO instanceof PromptState) {
 				return false;
 			} else if (oldTargetState instanceof SwitchState
 					|| targetBO instanceof SwitchState) {
 				return false;
 			} else if (targetBO instanceof FinalState) {
 				return false;
 			} else if (targetBO instanceof InitialState) {
 				boolean valido = verificaInitial(context);
 				if (valido) {
 					return super.canReconnect(context);
 				} else {
 					return false;
 				}
 			} else {
 				return super.canReconnect(context);
 			}
 		}
 
 		if (!(targetPictogramElement instanceof ContainerShape)) {
 			return false;
 		}
 
 		Object targetBO = getBusinessObjectForPictogramElement(targetPictogramElement);
 		if (targetBO instanceof InitialState) {
 			return false;
 		}
 
 		return super.canReconnect(context);
 	}
 
 	private boolean verificaInitial(IReconnectionContext context) {
 		PictogramElement targetPictogramElement = context
 				.getTargetPictogramElement();
 
 		State targetState = (State) getBusinessObjectForPictogramElement(targetPictogramElement);
 
 		if (targetState.getOutgoingTransitions().size() > 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public void preReconnect(IReconnectionContext context) {
 		Connection connection = context.getConnection();
 		PictogramElement targetPictogramElement = context
 				.getTargetPictogramElement();
 
 		transition = (Transition) getBusinessObjectForPictogramElement(connection);
 		State targetState = (State) getBusinessObjectForPictogramElement(targetPictogramElement);
 
 		if (context.getReconnectType().equals(
 				ReconnectionContext.RECONNECT_SOURCE)) {
 			oldTargetState = transition.getSource();
 			oldTargetState.getOutgoingTransitions().remove(transition);
 			targetState.getOutgoingTransitions().add(transition);
 
 		} else {
 			oldTargetState = transition.getTarget();
 			transition.setTarget(targetState);
 			oldTargetState.getIncomingTransitions().remove(transition);
 			targetState.getIncomingTransitions().add(transition);
 		}
 
 	}
 
 	@Override
 	public void postReconnect(IReconnectionContext context) {
 		layoutPictogramElement(context.getConnection());
 	}
 }
