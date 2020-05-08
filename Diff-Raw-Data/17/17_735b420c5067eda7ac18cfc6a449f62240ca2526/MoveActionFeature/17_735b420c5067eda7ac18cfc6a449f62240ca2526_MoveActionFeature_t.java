 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and implementation
  */
 package org.modelexecution.xmof.diagram.features;
 
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.Action;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.InputPin;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.OutputPin;
 import org.modelexecution.xmof.Syntax.Actions.BasicActions.Pin;
 
 public class MoveActionFeature extends DefaultMoveShapeFeature {
 
 	public MoveActionFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	@Override
 	public void moveShape(IMoveShapeContext context) {
 		super.moveShape(context);
 		Object bo = getBusinessObjectForPictogramElement(context
 				.getPictogramElement());
 		if (bo instanceof Action) {
 			Action action = (Action) bo;
 			relocatePins(action, context);
 		}
 	}
 
 	private void relocatePins(Action action, IMoveShapeContext context) {
 		for (int pinNumber = 1; pinNumber <= action.getOutput().size(); pinNumber++) {
 			OutputPin pin = action.getOutput().get(pinNumber - 1);
			movePin(pin, context);
 		}
 
 		for (int pinNumber = 1; pinNumber <= action.getInput().size(); pinNumber++) {
 			InputPin pin = action.getInput().get(pinNumber - 1);
			movePin(pin, context);
		}
	}

	private void movePin(Pin pin, IMoveShapeContext context) {
		PictogramElement pinShape = getPinShape(pin);
		if(pinShape != null) {
 			GraphicsAlgorithm pinArea = pinShape.getGraphicsAlgorithm();
 			pinArea.setX(pinArea.getX() + context.getDeltaX());
 			pinArea.setY(pinArea.getY() + context.getDeltaY());
 		}
 	}
	
 	private PictogramElement getPinShape(Pin pin) {
 		return getFeatureProvider().getPictogramElementForBusinessObject(pin);
 	}
 
 }
