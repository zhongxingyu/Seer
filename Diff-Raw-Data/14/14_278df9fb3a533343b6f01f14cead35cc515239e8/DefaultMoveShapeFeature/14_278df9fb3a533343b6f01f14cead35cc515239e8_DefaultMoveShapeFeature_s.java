 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.features.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 
 /**
  * The Class DefaultMoveShapeFeature.
  */
 public class DefaultMoveShapeFeature extends AbstractMoveShapeFeature {
 
 	/**
 	 * Creates a new {@link DefaultMoveShapeFeature}.
 	 * 
 	 * @param fp
 	 *            the feature provider
 	 */
 	public DefaultMoveShapeFeature(IFeatureProvider fp) {
 		super(fp);
 	}
 
 	/**
 	 * Sublasses can override this method.
 	 * 
 	 * @return true if moving to negative coordinates should not be possible
 	 */
 	protected boolean avoidNegativeCoordinates() {
 		return false;
 	}
 
 	public boolean canMoveShape(IMoveShapeContext context) {
		return context.getSourceContainer() != null && context.getSourceContainer().equals(context.getTargetContainer());
 	}
 
	final public void moveShape(IMoveShapeContext context) {
 		preMoveShape(context);
 		moveAllBendpoints(context);
 		internalMove(context);
 		postMoveShape(context);
 	}
 
 	/**
 	 * Move all bendpoints. Move bendpoints within a container shape.
 	 * 
 	 * @param context
 	 *            the context
 	 */
 	protected void moveAllBendpoints(IMoveShapeContext context) {
 		Shape shapeToMove = context.getShape();
 
 		int x = context.getX();
 		int y = context.getY();
 
 		int deltaX = x - shapeToMove.getGraphicsAlgorithm().getX();
 		int deltaY = y - shapeToMove.getGraphicsAlgorithm().getY();
 
 		if (deltaX != 0 || deltaY != 0) {
 			List<FreeFormConnection> connectionList = new ArrayList<FreeFormConnection>();
 
 			FreeFormConnection[] containerConnections = calculateContainerConnections(context);
 			for (int i = 0; i < containerConnections.length; i++) {
 				FreeFormConnection cc = containerConnections[i];
 				if (!connectionList.contains(cc)) {
 					connectionList.add(cc);
 				}
 			}
 
 			FreeFormConnection[] connectedConnections = calculateConnectedConnections(context);
 			for (int i = 0; i < connectedConnections.length; i++) {
 				FreeFormConnection cc = connectedConnections[i];
 				if (!connectionList.contains(cc)) {
 					connectionList.add(cc);
 				}
 			}
 
 			for (FreeFormConnection conn : connectionList) {
 				moveAllBendpointsOnFFConnection((FreeFormConnection) conn, deltaX, deltaY);
 			}
 		}
 	}
 
 	/**
 	 * Post move shape.
 	 * 
 	 * @param context
 	 *            the context
 	 */
 	protected void postMoveShape(IMoveShapeContext context) {
 	}
 
 	/**
 	 * Pre move shape.
 	 * 
 	 * @param context
 	 *            the context
 	 */
 	protected void preMoveShape(IMoveShapeContext context) {
 	}
 
 	/**
 	 * Internal move.
 	 * 
 	 * @param context
 	 *            the context
 	 */
 	protected void internalMove(IMoveShapeContext context) {
 		if (!getUserDecision()) {
 			return;
 		}
 		Shape shapeToMove = context.getShape();
 		ContainerShape oldContainerShape = context.getSourceContainer();
 		ContainerShape newContainerShape = context.getTargetContainer();
 
 		int x = context.getX();
 		int y = context.getY();
 
 		if (oldContainerShape != newContainerShape) {
 			// remember selection, because it is lost when temporarily removing
 			// the shapes.
 			PictogramElement[] currentSelection = getDiagramEditor().getSelectedPictogramElements();
 			// the following is a workaround due to an MMR bug
 			if (oldContainerShape != null) {
 				Collection<Shape> children = oldContainerShape.getChildren();
 				if (children != null) {
 					children.remove(shapeToMove);
 				}
 			}
 
 			shapeToMove.setContainer(newContainerShape);
 			if (shapeToMove.getGraphicsAlgorithm() != null) {
				Graphiti.getGaService().setLocation(shapeToMove.getGraphicsAlgorithm(), x, y, avoidNegativeCoordinates());
 			}
 			// restore selection
 			getDiagramEditor().setPictogramElementsForSelection(currentSelection);
 		} else { // move within the same container
 			if (shapeToMove.getGraphicsAlgorithm() != null) {
				Graphiti.getGaService().setLocation(shapeToMove.getGraphicsAlgorithm(), x, y, avoidNegativeCoordinates());
 			}
 		}
 	}
 
 	private FreeFormConnection[] calculateContainerConnections(IMoveShapeContext context) {
 		FreeFormConnection[] ret = new FreeFormConnection[0];
 
 		if (!(context.getShape() instanceof ContainerShape)) {
 			return ret;
 		}
 
 		List<FreeFormConnection> retList = new ArrayList<FreeFormConnection>();
 
 		Shape shapeToMove = context.getShape();
 
 		int x = context.getX();
 		int y = context.getY();
 
 		int deltaX = x - shapeToMove.getGraphicsAlgorithm().getX();
 		int deltaY = y - shapeToMove.getGraphicsAlgorithm().getY();
 
 		if (deltaX != 0 || deltaY != 0) {
 
 			List<Anchor> anchorsFrom = getAnchors(shapeToMove);
 			List<Anchor> anchorsTo = new ArrayList<Anchor>(anchorsFrom);
 
 			for (Anchor anchorFrom : anchorsFrom) {
 
 				Collection<Connection> outgoingConnections = anchorFrom.getOutgoingConnections();
 
 				for (Connection connection : outgoingConnections) {
 					for (Anchor anchorTo : anchorsTo) {
 
 						Collection<Connection> incomingConnections = anchorTo.getIncomingConnections();
 						if (incomingConnections.contains(connection)) {
 							if (connection instanceof FreeFormConnection) {
 								retList.add((FreeFormConnection) connection);
 							}
 						}
 					}
 				}
 			}
 		}
 		return retList.toArray(ret);
 	}
 
 	private FreeFormConnection[] calculateConnectedConnections(IMoveShapeContext context) {
 		List<FreeFormConnection> retList = new ArrayList<FreeFormConnection>();
 		Shape shapeToMove = context.getShape();
 
 		int x = context.getX();
 		int y = context.getY();
 
 		int deltaX = x - shapeToMove.getGraphicsAlgorithm().getX();
 		int deltaY = y - shapeToMove.getGraphicsAlgorithm().getY();
 
 		if (deltaX != 0 || deltaY != 0) {
 			List<Anchor> anchors = getAnchors(shapeToMove);
 
 			PictogramElement[] selectedPictogramElements = getDiagramEditor().getSelectedPictogramElements();
 			if (selectedPictogramElements != null) {
 				for (int i = 0; i < selectedPictogramElements.length; i++) {
 					PictogramElement selPe = selectedPictogramElements[i];
 					if (selPe instanceof Shape) {
 						Shape selShape = (Shape) selPe;
 						for (Anchor toAnchor : getAnchors(selShape)) {
 							EList<Connection> incomingConnections = toAnchor.getIncomingConnections();
 							for (Connection inConn : incomingConnections) {
 								if (inConn instanceof FreeFormConnection) {
 									Anchor startAnchor = inConn.getStart();
 									if (anchors.contains(startAnchor)) {
 										retList.add((FreeFormConnection) inConn);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return retList.toArray(new FreeFormConnection[0]);
 	}
 
 	private void moveAllBendpointsOnFFConnection(FreeFormConnection connection, int deltaX, int deltaY) {
 		List<Point> points = connection.getBendpoints();
 		for (int i = 0; i < points.size(); i++) {
 			Point point = points.get(i);
 			int oldX = point.getX();
 			int oldY = point.getY();
 			points.set(i, Graphiti.getGaCreateService().createPoint(oldX + deltaX, oldY + deltaY));
 		}
 	}
 
 	private List<Anchor> getAnchors(Shape theShape) {
 		List<Anchor> ret = new ArrayList<Anchor>();
 		ret.addAll(theShape.getAnchors());
 
 		if (theShape instanceof ContainerShape) {
 			ContainerShape containerShape = (ContainerShape) theShape;
 			List<Shape> children = containerShape.getChildren();
 			for (Shape shape : children) {
 				if (shape instanceof ContainerShape) {
 					ret.addAll(getAnchors((ContainerShape) shape));
 				} else {
 					ret.addAll(shape.getAnchors());
 				}
 			}
 		}
 		return ret;
 	}
 }
