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
  *    Patch 184530 from Bug 331829 contributed by Henrik Rentz-Reichert
  *    mwenz - Bug 331715: Support for rectangular grids in diagrams
  *    mwenz - Bug 352220 - Possibility to disable guides
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.internal.services.impl;
 
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.graphiti.IExecutionInfo;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.datatypes.IRectangle;
 import org.eclipse.graphiti.features.IFeatureAndContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
 import org.eclipse.graphiti.internal.IDiagramVersion;
 import org.eclipse.graphiti.internal.datatypes.impl.LocationImpl;
 import org.eclipse.graphiti.internal.datatypes.impl.RectangleImpl;
 import org.eclipse.graphiti.internal.services.GraphitiInternal;
 import org.eclipse.graphiti.internal.util.LookManager;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.mm.MmFactory;
 import org.eclipse.graphiti.mm.Property;
 import org.eclipse.graphiti.mm.PropertyContainer;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.algorithms.styles.PrecisionPoint;
 import org.eclipse.graphiti.mm.algorithms.styles.StylesFactory;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
 import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.CurvedConnection;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.ManhattanConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.PictogramLink;
 import org.eclipse.graphiti.mm.pictograms.PictogramsFactory;
 import org.eclipse.graphiti.mm.pictograms.PictogramsPackage;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeService;
 import org.eclipse.graphiti.util.ILocationInfo;
 import org.eclipse.graphiti.util.ILook;
 import org.eclipse.graphiti.util.LocationInfo;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public final class PeServiceImpl implements IPeService {
 
 	private static final ArrayList<Property> EMPTY_PROPERTIES_LIST = new ArrayList<Property>(0);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createBoxRelativeAnchor(org.
 	 * eclipse.graphiti.mm.pictograms.AnchorContainer)
 	 */
 	public BoxRelativeAnchor createBoxRelativeAnchor(AnchorContainer anchorContainer) {
 
 		BoxRelativeAnchor ret = PictogramsFactory.eINSTANCE.createBoxRelativeAnchor();
 		ret.setVisible(true);
 		ret.setActive(true);
 		ret.setRelativeHeight(0);
 		ret.setRelativeWidth(0);
 		ret.setParent(anchorContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createChopboxAnchor(org.eclipse
 	 * .graphiti.mm.pictograms.AnchorContainer)
 	 */
 	public ChopboxAnchor createChopboxAnchor(AnchorContainer anchorContainer) {
 
 		ChopboxAnchor ret = PictogramsFactory.eINSTANCE.createChopboxAnchor();
 		ret.setVisible(false);
 		ret.setActive(false);
 		ret.setParent(anchorContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createConnectionDecorator(org
 	 * .eclipse.graphiti.mm.pictograms.Connection, boolean, double, boolean)
 	 */
 	public ConnectionDecorator createConnectionDecorator(Connection connection, boolean active, double location, boolean isRelative) {
 		ConnectionDecorator ret = PictogramsFactory.eINSTANCE.createConnectionDecorator();
 		ret.setActive(active);
 		ret.setLocation(location);
 		ret.setLocationRelative(isRelative);
 		ret.setConnection(connection);
 		ret.setVisible(true);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createContainerShape(org.eclipse
 	 * .graphiti.mm.pictograms.ContainerShape, boolean)
 	 */
 	public ContainerShape createContainerShape(ContainerShape parentContainerShape, boolean active) {
 		ContainerShape ret = PictogramsFactory.eINSTANCE.createContainerShape();
 		ret.getProperties().addAll(EMPTY_PROPERTIES_LIST);
 		ret.setVisible(true);
 		ret.setActive(active);
 		ret.setContainer(parentContainerShape);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createDiagram(java.lang.String,
 	 * java.lang.String, boolean)
 	 */
 	public Diagram createDiagram(String diagramTypeId, String diagramName, boolean snap) {
 		return createDiagram(diagramTypeId, diagramName, LookManager.getLook().getMinorGridLineDistance(), snap);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createDiagram(java.lang.String,
 	 * java.lang.String, int, boolean)
 	 */
 	public Diagram createDiagram(String diagramTypeId, String diagramName, int gridUnit, boolean snap) {
 		// Reduce file footprint: use default -1 to avoid storing of verticalGridUnit attribute
 		return createDiagram(diagramTypeId, diagramName, gridUnit, -1, snap);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createDiagram(java.lang.String,
 	 * java.lang.String, int, boolean)
 	 */
 	public Diagram createDiagram(String diagramTypeId, String diagramName, int horizontalGridUnit, int verticalGridUnit, boolean snap) {
 
 		if (diagramTypeId == null || diagramName == null) {
 			return null;
 		}
 
 		final ILook look = LookManager.getLook();
 
 		Diagram ret = PictogramsFactory.eINSTANCE.createDiagram();
 		ret.setDiagramTypeId(diagramTypeId);
 		ret.setGridUnit(horizontalGridUnit);
 		if (verticalGridUnit != -1) {
 			ret.setVerticalGridUnit(verticalGridUnit);
 		}
 		ret.setSnapToGrid(snap);
 		ret.setVisible(true);
 		ret.eSet(PictogramsPackage.eINSTANCE.getDiagram_Version(), IDiagramVersion.CURRENT);
 
 		IGaService gaService = Graphiti.getGaService();
 		Rectangle rectangle = gaService.createRectangle(ret);
 		rectangle.setForeground(gaService.manageColor(ret, look.getMinorGridLineColor())); // 206,
 		// 224,
 		// 242
 		rectangle.setBackground(gaService.manageColor(ret, look.getGridBackgroundColor()));
 		gaService.setSize(rectangle, 1000, 1000);
 
 		ret.setName(diagramName);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createFixPointAnchor(org.eclipse
 	 * .graphiti.mm.pictograms.AnchorContainer)
 	 */
 	public FixPointAnchor createFixPointAnchor(AnchorContainer anchorContainer) {
 		FixPointAnchor ret = PictogramsFactory.eINSTANCE.createFixPointAnchor();
 		ret.setVisible(true);
 		ret.setActive(true);
 		ret.setParent(anchorContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createFreeFormConnection(org
 	 * .eclipse.graphiti.mm.pictograms.Diagram)
 	 */
 	public FreeFormConnection createFreeFormConnection(Diagram diagram) {
 		FreeFormConnection ret = PictogramsFactory.eINSTANCE.createFreeFormConnection();
 		ret.setVisible(true);
 		ret.setActive(true);
 		ret.setParent(diagram);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeCreateService#createManhattanConnection
 	 * (org.eclipse.graphiti.mm.pictograms.Diagram)
 	 */
 	public ManhattanConnection createManhattanConnection(Diagram diagram) {
 		ManhattanConnection ret = PictogramsFactory.eINSTANCE.createManhattanConnection();
 		ret.setVisible(true);
 		ret.setActive(true);
 		ret.setParent(diagram);
 		return ret;
 	}
 
 	public CurvedConnection createCurvedConnection(double[] xy, Diagram diagram) {
 		assert (xy != null && xy.length % 2 == 0);
 		CurvedConnection ret = PictogramsFactory.eINSTANCE.createCurvedConnection();
 		ret.setVisible(true);
 		ret.setActive(true);
 		ret.setParent(diagram);
 		for (int i = 0; i < xy.length; i += 2) {
 			PrecisionPoint point = StylesFactory.eINSTANCE.createPrecisionPoint();
 			point.setX(xy[i]);
 			point.setY(xy[i + 1]);
 			ret.getControlPoints().add(point);
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#createShape(org.eclipse.graphiti
 	 * .mm.pictograms.ContainerShape, boolean)
 	 */
 	public Shape createShape(ContainerShape parentContainerShape, boolean active) {
 		Shape ret = PictogramsFactory.eINSTANCE.createShape();
 		ret.getProperties().addAll(EMPTY_PROPERTIES_LIST);
 		ret.setVisible(true);
 		ret.setActive(active);
 
 		ret.setContainer(parentContainerShape);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#deletePictogramElement(org.eclipse
 	 * .graphiti.mm.pictograms.PictogramElement)
 	 */
 	public void deletePictogramElement(PictogramElement pe) {
 		if (pe instanceof ContainerShape) {
 			ContainerShape cs = (ContainerShape) pe;
 			List<Shape> childList = cs.getChildren();
 			List<Shape> l = new ArrayList<Shape>();
 			for (Shape shape : childList) {
 				l.add(shape);
 			}
 			for (Shape shape : l) {
 				deletePictogramElement(shape);
 			}
 		}
 		if (pe instanceof AnchorContainer) {
 			AnchorContainer ac = (AnchorContainer) pe;
 			Collection<Anchor> anchorList = ac.getAnchors();
 			List<Anchor> l = new ArrayList<Anchor>();
 			for (Anchor anchor : anchorList) {
 				l.add(anchor);
 			}
 			for (Anchor anchor : l) {
 				deletePictogramElement(anchor);
 			}
 		}
 		GraphicsAlgorithm graphicsAlgorithm = pe.getGraphicsAlgorithm();
 		if (graphicsAlgorithm != null) {
 			EcoreUtil.delete(graphicsAlgorithm, true);
 		}
 		PictogramLink linkForPictogramElement = Graphiti.getLinkService().getLinkForPictogramElement(pe);
 		if (linkForPictogramElement != null) {
 			EcoreUtil.delete(linkForPictogramElement, true);
 		}
 		EcoreUtil.delete(pe, true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getActiveContainerPe(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithm)
 	 */
 	public PictogramElement getActiveContainerPe(GraphicsAlgorithm ga) {
 		if (ga == null) {
 			throw new IllegalArgumentException("Parameter must not be null"); //$NON-NLS-1$
 
 		}
 		while (ga.getPictogramElement() == null) {
 			ga = ga.getParentGraphicsAlgorithm();
 			if (ga == null) {
 				return null;
 			}
 		}
 
 		PictogramElement pe = ga.getPictogramElement();
 
 		while ((pe != null) && !pe.isActive()) {
 			pe = getPictogramElementParent(pe);
 		}
 		return pe;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getActiveContainerPe(org.eclipse
 	 * .graphiti.mm.pictograms.PictogramElement)
 	 */
 	public PictogramElement getActiveContainerPe(PictogramElement pictogramElement) {
 		PictogramElement pe = getPictogramElementParent(pictogramElement);
 		while ((pe != null) && !pe.isActive()) {
 			pe = getPictogramElementParent(pe);
 		}
 		return pe;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getAllConnections(org.eclipse
 	 * .graphiti.mm.pictograms.Anchor)
 	 */
 	public List<Connection> getAllConnections(Anchor anchor) {
 		List<Connection> connections = new ArrayList<Connection>();
 		connections.addAll(anchor.getIncomingConnections());
 		connections.addAll(anchor.getOutgoingConnections());
 		return connections;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getAllConnections(org.eclipse
 	 * .graphiti.mm.pictograms.AnchorContainer)
 	 */
 	public List<Connection> getAllConnections(AnchorContainer anchorContainer) {
 		List<Connection> connections = new ArrayList<Connection>();
 		Collection<Anchor> anchors = anchorContainer.getAnchors();
 
 		for (Anchor anchor : anchors) {
 			connections.addAll(getAllConnections(anchor));
 		}
 		return connections;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getAllContainedPictogramElements
 	 * (org.eclipse.graphiti.mm.pictograms.PictogramElement)
 	 */
 	public Collection<PictogramElement> getAllContainedPictogramElements(PictogramElement pe) {
 		List<PictogramElement> ret = new ArrayList<PictogramElement>();
 		Collection<PictogramElement> peChildren = getPictogramElementChildren(pe);
 		ret.addAll(peChildren);
 		for (PictogramElement peChild : peChildren) {
 			ret.addAll(getAllContainedPictogramElements(peChild));
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getAllContainedShapes(org.eclipse
 	 * .graphiti.mm.pictograms.ContainerShape)
 	 */
 	public Collection<Shape> getAllContainedShapes(ContainerShape cs) {
 		ArrayList<Shape> ret = new ArrayList<Shape>();
 		Collection<Shape> children = cs.getChildren();
 		for (Shape childShape : children) {
 			ret.add(childShape);
 			if (childShape instanceof ContainerShape) {
 				ContainerShape childCs = (ContainerShape) childShape;
 				ret.addAll(getAllContainedShapes(childCs));
 			}
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getChopboxAnchor(org.eclipse
 	 * .graphiti.mm.pictograms.AnchorContainer)
 	 */
 	public Anchor getChopboxAnchor(AnchorContainer anchorContainer) {
 		Collection<Anchor> existingAnchors = anchorContainer.getAnchors();
 		for (Anchor anchor : existingAnchors) {
 			if (anchor instanceof ChopboxAnchor) {
 				return anchor;
 			}
 		}
 		return null;
 	}
 
 	private static java.awt.Point getChopboxLocationOnBox(java.awt.Point outsidePoint, java.awt.Rectangle box) {
 		java.awt.Rectangle r = new java.awt.Rectangle(box.x - 1, box.y - 1, box.width + 1, box.height + 1);
 
 		float centerX = r.x + 0.5f * r.width;
 		float centerY = r.y + 0.5f * r.height;
 
 		if (r.isEmpty() || (outsidePoint.x == (int) centerX && outsidePoint.y == (int) centerY))
 			return new java.awt.Point((int) centerX, (int) centerY); // This
 		// avoids
 		// divide-by-zero
 
 		float dx = outsidePoint.x - centerX;
 		float dy = outsidePoint.y - centerY;
 
 		// r.width, r.height, dx, and dy are guaranteed to be non-zero.
 		float scale = 0.5f / Math.max(Math.abs(dx) / r.width, Math.abs(dy) / r.height);
 
 		dx *= scale;
 		dy *= scale;
 		centerX += dx;
 		centerY += dy;
 
 		return new java.awt.Point(Math.round(centerX), Math.round(centerY));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getConnectionMidpoint(org.eclipse
 	 * .graphiti.mm.pictograms.Connection, double)
 	 */
 	public ILocation getConnectionMidpoint(Connection c, double d) {
 		ILocation ret = null;
 
 		Anchor startAnchor = c.getStart();
 		ILocation startLocation = getLocationRelativeToDiagram(startAnchor);
 		java.awt.Point startPoint = new java.awt.Point(startLocation.getX(), startLocation.getY());
 
 		Anchor endAnchor = c.getEnd();
 		ILocation endLocation = getLocationRelativeToDiagram(endAnchor);
 		java.awt.Point endPoint = new java.awt.Point(endLocation.getX(), endLocation.getY());
 
 		// special solutions for chopbox anchors
 		if (startAnchor instanceof ChopboxAnchor || endAnchor instanceof ChopboxAnchor) {
 			if (startAnchor instanceof ChopboxAnchor) {
 				ChopboxAnchor cbStartAnchor = (ChopboxAnchor) startAnchor;
 				GraphicsAlgorithm parentGa = cbStartAnchor.getParent().getGraphicsAlgorithm();
 				Shape shape = (Shape) cbStartAnchor.getParent();
 				ILocation location = getLocationRelativeToDiagram(shape);
 				java.awt.Rectangle parentRect = new java.awt.Rectangle(location.getX(), location.getY(), parentGa.getWidth(),
 						parentGa.getHeight());
 
 				java.awt.Point pointNextToStartAnchor = new java.awt.Point(startPoint.x, startPoint.y);
 
 				if (c instanceof FreeFormConnection) {
 					FreeFormConnection ffc = (FreeFormConnection) c;
 					List<Point> bendpoints = ffc.getBendpoints();
 					if (!bendpoints.isEmpty()) {
 						Point firstBendpoint = bendpoints.get(0);
 						pointNextToStartAnchor.setLocation(firstBendpoint.getX(), firstBendpoint.getY());
 					}
 				}
 
 				java.awt.Point chopboxLocationOnBox = getChopboxLocationOnBox(pointNextToStartAnchor, parentRect);
 
 				startPoint.setLocation(chopboxLocationOnBox);
 			}
 
 			if (endAnchor instanceof ChopboxAnchor) {
 				ChopboxAnchor cbEndAnchor = (ChopboxAnchor) endAnchor;
 				GraphicsAlgorithm parentGa = cbEndAnchor.getParent().getGraphicsAlgorithm();
 				Shape shape = (Shape) cbEndAnchor.getParent();
 				ILocation location = getLocationRelativeToDiagram(shape);
 				java.awt.Rectangle parentRect = new java.awt.Rectangle(location.getX(), location.getY(), parentGa.getWidth(),
 						parentGa.getHeight());
 
 				java.awt.Point pointNextToEndAnchor = new java.awt.Point(endPoint.x, endPoint.y);
 
 				if (c instanceof FreeFormConnection) {
 					FreeFormConnection ffc = (FreeFormConnection) c;
 					List<Point> bendpoints = ffc.getBendpoints();
 					if (!bendpoints.isEmpty()) {
 						Point lastBendpoint = bendpoints.get(bendpoints.size() - 1);
 						pointNextToEndAnchor.setLocation(lastBendpoint.getX(), lastBendpoint.getY());
 					}
 				}
 
 				java.awt.Point chopboxLocationOnBox = getChopboxLocationOnBox(pointNextToEndAnchor, parentRect);
 
 				endPoint.setLocation(chopboxLocationOnBox);
 			}
 		}
 
 		if (c instanceof FreeFormConnection) {
 			FreeFormConnection ffc = (FreeFormConnection) c;
 			List<Point> bendpoints = ffc.getBendpoints();
 
 			java.awt.Point[] awtPointsArray = new java.awt.Point[bendpoints.size() + 2];
 			{
 				awtPointsArray[0] = startPoint;
 				int i = 1;
 				for (Iterator<Point> iter = bendpoints.iterator(); iter.hasNext();) {
 					Point pictogramsPoint = iter.next();
 					awtPointsArray[i] = new java.awt.Point(pictogramsPoint.getX(), pictogramsPoint.getY());
 					i++;
 				}
 				awtPointsArray[i] = endPoint;
 			}
 
 			double completeDistance = getDistance(awtPointsArray);
 			double absDistanceToRelPoint = completeDistance * d;
 
 			double distanceSum = 0;
 			for (int i = 0; i < awtPointsArray.length - 1; i++) {
 				double oldDistanceSum = distanceSum;
 				java.awt.Point currentPoint = awtPointsArray[i];
 				java.awt.Point nextPoint = awtPointsArray[i + 1];
 				double additionalDistanceToNext = getDistance(currentPoint, nextPoint);
 				distanceSum += additionalDistanceToNext;
 				if (distanceSum >= absDistanceToRelPoint) {
 					double thisRelative = ((completeDistance * d) - oldDistanceSum) / additionalDistanceToNext;
 					ret = getMidpoint(currentPoint.x, currentPoint.y, nextPoint.x, nextPoint.y, thisRelative);
 					break; // or return ret;
 				}
 			}
 		} else {
 			int midX = (int) Math.round((startPoint.x + d * (endPoint.x - startPoint.x)));
 			int midY = (int) Math.round((startPoint.y + d * (endPoint.y - startPoint.y)));
 			ret = new LocationImpl(midX, midY);
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getDiagramForAnchor(org.eclipse
 	 * .graphiti.mm.pictograms.Anchor)
 	 */
 	public Diagram getDiagramForAnchor(Anchor anchor) {
 		Diagram ret = null;
 		AnchorContainer ac = anchor.getParent();
 		if (ac instanceof Connection) {
 			Connection connection = (Connection) ac;
 			ret = connection.getParent();
 		} else if (ac instanceof Shape) {
 			Shape shape = (Shape) ac;
 			ret = getDiagramForShape(shape);
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getDiagramForPictogramElement
 	 * (org.eclipse.graphiti.mm.pictograms.PictogramElement)
 	 */
 	public Diagram getDiagramForPictogramElement(PictogramElement pe) {
 		Diagram ret = null;
 		if (pe instanceof Diagram) {
 			ret = (Diagram) pe;
 		} else if (pe instanceof ConnectionDecorator) {
 			ret = getDiagramForPictogramElement(((ConnectionDecorator) pe).getConnection());
 		} else if (pe instanceof Shape) {
 			ret = getDiagramForShape(((Shape) pe).getContainer());
 		} else if (pe instanceof Anchor) {
 			ret = getDiagramForAnchor(((Anchor) pe));
 		} else if (pe instanceof Connection) {
 			ret = ((Connection) pe).getParent();
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getDiagramForShape(org.eclipse
 	 * .graphiti.mm.pictograms.Shape)
 	 */
 	public Diagram getDiagramForShape(Shape shape) {
 		Diagram ret = null;
 		if (shape instanceof Diagram) {
 			ret = (Diagram) shape;
 		} else if (shape instanceof ConnectionDecorator) {
 			ret = getDiagramForPictogramElement(((ConnectionDecorator) shape).getConnection());
 		} else {
 			ret = getDiagramForShape(shape.getContainer());
 		}
 		return ret;
 	}
 
 	private static double getDistance(java.awt.Point start, java.awt.Point end) {
 		int xDist = end.x - start.x;
 		int yDist = end.y - start.y;
 		double ret = Math.sqrt((xDist * xDist) + (yDist * yDist));
 		return ret;
 	}
 
 	private static double getDistance(java.awt.Point[] points) {
 		double ret = 0;
 		for (int i = 0; i < points.length - 1; i++) {
 			java.awt.Point currentPoint = points[i];
 			java.awt.Point nextPoint = points[i + 1];
 			ret += getDistance(currentPoint, nextPoint);
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getElementsNotInDiagram(org.
 	 * eclipse.emf.ecore.EObject[], org.eclipse.graphiti.mm.pictograms.Diagram)
 	 */
 	public EObject[] getElementsNotInDiagram(EObject[] elements, Diagram diagram) {
 		final String SIGNATURE = "getElementsNotInDiagram(EObject[] elements, Diagram diag)"; //$NON-NLS-1$
 		boolean info = T.racer().info();
 		if (info) {
 			T.racer().entering(PeServiceImpl.class, SIGNATURE, new Object[] { elements, diagram });
 		}
 
 		if (elements.length == 0) {
 			return new EObject[0];
 		}
 
 		if (diagram == null) {
 			throw new IllegalArgumentException("Diagram must not be null"); //$NON-NLS-1$
 		}
 
 		Set<EObject> notLinkedPEs = new HashSet<EObject>(elements.length);
 		for (int i = 0; i < elements.length; i++) {
 			notLinkedPEs.add(elements[i]);
 		}
 
 		TreeIterator<EObject> iterator = diagram.eAllContents();
 		while (iterator.hasNext()) {
 			EObject object = iterator.next();
 			if (object instanceof PictogramElement) {
 				PictogramLink link = ((PictogramElement) object).getLink();
 				if (link != null) {
 					EList<EObject> businessObjects = link.getBusinessObjects();
 					for (Iterator<EObject> iterator2 = businessObjects.iterator(); iterator2.hasNext();) {
 						EObject businessObject = iterator2.next();
 						EObject toRemove = null;
 						for (Iterator<EObject> iterator3 = notLinkedPEs.iterator(); iterator3.hasNext();) {
 							EObject notLinkedObject = iterator3.next();
 							if (GraphitiInternal.getEmfService().getDTPForDiagram(diagram).getCurrentToolBehaviorProvider()
 									.equalsBusinessObjects(notLinkedObject, businessObject)) {
 								toRemove = notLinkedObject;
 							}
 						}
 						if (toRemove != null) {
 							notLinkedPEs.remove(toRemove);
 						}
 					}
 				}
 			}
 		}
 
 		EObject[] result = new EObject[notLinkedPEs.size()];
 		result = notLinkedPEs.toArray(result);
 		return result;
 
 		// // get current development component
 		// ResourceSet resourceSet = diagram.eResource().getResourceSet();
 		// IProject project =
 		// ResourceSetManager.getProjectForResourceSet(resourceSet);
 		// IDevelopmentComponent dc =
 		// DiiLibAPI.getResourceService().getDevelopmentComponent(project);
 		//
 		// final Index index = OxActivator.getDefault().getOxUtil().getIndex(
 		// dc.getContainingSoftwareComponent().getContainingDevelopmentConfiguration());
 		//
 		// QueryProcessor processor = new QueryProcessorImpl(index);
 		// TypeScopeProvider diagramScope =
 		// processor.getInclusivePartitionScopeProvider(diagram.eResource().getURI());
 		//
 		// List<URI> elementUriList = new ArrayList<URI>();
 		// for (int i = 0; i < elements.length; i++) {
 		// URI elementUri = EcoreUtil.getURI(elements[i]);
 		// elementUriList.add(elementUri);
 		// }
 		// URI[] elementUris = elementUriList.toArray(new URI[0]);
 		//
 		//		SelectEntry[] selectEntries = new SelectEntry[] { new SelectAlias("el") }; //$NON-NLS-1$
 		//		FromType pl = new FromType("pl", EcoreUtil.getURI(LinksPackage.Literals.PICTOGRAM_LINK), false, diagramScope); //$NON-NLS-1$
 		//		FromFixedSet el = new FromFixedSet("el", EcoreUtil.getURI(EcorePackage.Literals.EOBJECT), elementUris); //$NON-NLS-1$
 		//
 		// FromEntry[] fromEntries = new FromEntry[] { pl, el };
 		//		WhereRelationReference pictogramLinkPred = new WhereRelationReference("pl", "businessObjects", "el"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		// WhereEntry[] whereEntries = new WhereEntry[] { pictogramLinkPred };
 		//
 		// Query queryAst = new Query(selectEntries, fromEntries, whereEntries);
 		//
 		// // get nwdi scope for query
 		// OxUtil util = OxActivator.getDefault().getOxUtil();
 		// QueryContext scopeProvider =
 		// util.getQueryContextFactory().createInnerQueryContext(resourceSet,
 		// Collections.singletonList(dc));
 		//
 		// ResultSet resultSetFromAst = processor.execute(queryAst,
 		// scopeProvider);
 		//
 		//		URI[] uris = resultSetFromAst.getUris("el"); //$NON-NLS-1$
 		// List<URI> linkedUris = Arrays.asList(uris);
 		// elementUriList.removeAll(linkedUris);
 		//
 		// EObject ret[] = new EObject[elementUriList.size()];
 		// for (int i = 0; i < ret.length; i++) {
 		// ret[i] = resourceSet.getEObject(elementUriList.get(i), true);
 		// }
 		// return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getGaBoundsForAnchor(org.eclipse
 	 * .graphiti.mm.pictograms.Anchor)
 	 */
 	public IRectangle getGaBoundsForAnchor(Anchor anchor) {
 		IRectangle ret = new RectangleImpl(0, 0);
 
 		GraphicsAlgorithm parentGa = anchor.getParent().getGraphicsAlgorithm();
 		if (parentGa != null) {
 			if (anchor.getReferencedGraphicsAlgorithm() != null) {
 				GraphicsAlgorithm referencedGa = anchor.getReferencedGraphicsAlgorithm();
 				int relX = GaServiceImpl.getRelativeX(referencedGa, parentGa);
 				int relY = GaServiceImpl.getRelativeY(referencedGa, parentGa);
 				ret = new RectangleImpl(relX, relY, referencedGa.getWidth(), referencedGa.getHeight());
 			} else {
 				ret = new RectangleImpl(0, 0, parentGa.getWidth(), parentGa.getHeight());
 			}
 		}
 
 		return ret;
 	}
 
 	private static GraphicsAlgorithm getGraphicsAlgorithmForLocation(GraphicsAlgorithm graphicsAlgorithm, int x, int y) {
 		if (graphicsAlgorithm == null) {
 			return null;
 		}
 
 		List<GraphicsAlgorithm> children = graphicsAlgorithm.getGraphicsAlgorithmChildren();
 		for (GraphicsAlgorithm childGa : children) {
 			int newX = x - childGa.getX();
 			int newY = y - childGa.getY();
 			GraphicsAlgorithm ga = getGraphicsAlgorithmForLocation(childGa, newX, newY);
 			if (ga != null) {
 				return ga;
 			}
 		}
 
 		IDimension size = Graphiti.getGaService().calculateSize(graphicsAlgorithm);
 		if (x >= 0 && x < size.getWidth() && y >= 0 && y < size.getHeight()) {
 			return graphicsAlgorithm;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns the size of the pictogram element (more exactly: size of its
 	 * graphics algorithm)
 	 * 
 	 * @param pe
 	 *            given pictogram element
 	 * @return the size
 	 */
 	private static int getHeightOfPictogramElement(PictogramElement pe) {
 		int ret = 0;
 		GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 		if (ga != null) {
 			ret = ga.getHeight();
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getIncomingConnections(org.eclipse
 	 * .graphiti.mm.pictograms.AnchorContainer)
 	 */
 	public List<Connection> getIncomingConnections(AnchorContainer anchorContainer) {
 		List<Connection> connections = new ArrayList<Connection>();
 		Collection<Anchor> anchors = anchorContainer.getAnchors();
 
 		for (Anchor anchor : anchors) {
 			connections.addAll(anchor.getIncomingConnections());
 		}
 		return connections;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getLinkedPictogramElements(org
 	 * .eclipse.emf.ecore.EObject[], org.eclipse.graphiti.mm.pictograms.Diagram)
 	 */
 	public Object[] getLinkedPictogramElements(EObject[] elements, Diagram diagram) {
 		final String SIGNATURE = "getLinkedPictogramElements(EObject[] elements, Diagram diag)"; //$NON-NLS-1$
 		boolean info = T.racer().info();
 		if (info) {
 			T.racer().entering(PeServiceImpl.class, SIGNATURE, new Object[] { elements, diagram });
 		}
 
 		if (diagram == null) {
 			throw new IllegalArgumentException("Diagram must not be null"); //$NON-NLS-1$
 		}
 
 		Set<PictogramElement> linkedPEs = new HashSet<PictogramElement>();
 
 		TreeIterator<EObject> iterator = diagram.eAllContents();
 		while (iterator.hasNext()) {
 			EObject object = iterator.next();
 			if (object instanceof PictogramElement) {
 				PictogramLink link = ((PictogramElement) object).getLink();
 				if (link != null) {
 					EList<EObject> businessObjects = link.getBusinessObjects();
 					for (Iterator<EObject> iterator2 = businessObjects.iterator(); iterator2.hasNext();) {
 						EObject eObject = iterator2.next();
 						for (int i = 0; i < elements.length; i++) {
 							if (GraphitiInternal.getEmfService().getDTPForDiagram(diagram).getCurrentToolBehaviorProvider()
 									.equalsBusinessObjects(elements[i], eObject)) {
 								linkedPEs.add(link.getPictogramElement());
 							}
 						}
 					}
 				}
 			}
 		}
 
 		Object[] result = new Object[linkedPEs.size()];
 		result = linkedPEs.toArray(result);
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getLocationInfo(org.eclipse.
 	 * graphiti.mm.pictograms.Shape, int, int)
 	 */
 	public ILocationInfo getLocationInfo(Shape shape, int x, int y) {
 		if (shape instanceof ContainerShape) {
 			ContainerShape containerShape = (ContainerShape) shape;
 			List<Shape> children = containerShape.getChildren();
 			for (Shape childShape : children) {
 				GraphicsAlgorithm childGa = childShape.getGraphicsAlgorithm();
 				if (childGa != null) {
 					int newX = x - childGa.getX();
 					int newY = y - childGa.getY();
 					ILocationInfo locationInfo = getLocationInfo(childShape, newX, newY);
 					if (locationInfo != null) {
 						return locationInfo;
 					}
 				}
 			}
 		}
 
 		GraphicsAlgorithm ga = getGraphicsAlgorithmForLocation(shape.getGraphicsAlgorithm(), x, y);
 		if (ga != null) {
 			return new LocationInfo(shape, ga);
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getLocationRelativeToDiagram
 	 * (org.eclipse.graphiti.mm.pictograms.Anchor)
 	 */
 	public ILocation getLocationRelativeToDiagram(Anchor anchor) {
 		int x = getRelativeToDiagramX(anchor);
 		int y = getRelativeToDiagramY(anchor);
 		ILocation ret = new LocationImpl(x, y);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getLocationRelativeToDiagram
 	 * (org.eclipse.graphiti.mm.pictograms.Shape)
 	 */
 	public ILocation getLocationRelativeToDiagram(Shape shape) {
 		int x = getRelativeToDiagramX(shape);
 		int y = getRelativeToDiagramY(shape);
 		ILocation ret = new LocationImpl(x, y);
 		return ret;
 	}
 
 	private static ILocation getMidpoint(int startX, int startY, int endX, int endY, double d) {
 		int midX = (int) Math.round((startX + d * (endX - startX)));
 		int midY = (int) Math.round((startY + d * (endY - startY)));
 		return new LocationImpl(midX, midY);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getOutgoingConnections(org.eclipse
 	 * .graphiti.mm.pictograms.AnchorContainer)
 	 */
 	public List<Connection> getOutgoingConnections(AnchorContainer anchorContainer) {
 		List<Connection> connections = new ArrayList<Connection>();
 		Collection<Anchor> anchors = anchorContainer.getAnchors();
 
 		for (Anchor anchor : anchors) {
 			connections.addAll(anchor.getOutgoingConnections());
 		}
 		return connections;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getPictogramElementChildren(
 	 * org.eclipse.graphiti.mm.pictograms.PictogramElement)
 	 */
 	public Collection<PictogramElement> getPictogramElementChildren(PictogramElement pe) {
 		List<PictogramElement> retList = new ArrayList<PictogramElement>();
 
 		if (pe instanceof Diagram) {
 			Diagram diagram = (Diagram) pe;
 			retList.addAll(diagram.getConnections());
 		}
 		if (pe instanceof ContainerShape) {
 			ContainerShape containerShape = (ContainerShape) pe;
 			retList.addAll(containerShape.getChildren());
 		}
 		if (pe instanceof Connection) {
 			Connection connection = (Connection) pe;
 			retList.addAll(connection.getConnectionDecorators());
 		}
 		if (pe instanceof AnchorContainer) {
 			AnchorContainer anchorContainer = (AnchorContainer) pe;
 			retList.addAll(anchorContainer.getAnchors());
 		}
 
 		return retList;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getPictogramElementParent(org
 	 * .eclipse.graphiti.mm.pictograms.PictogramElement)
 	 */
 	public PictogramElement getPictogramElementParent(PictogramElement pe) {
 		if (pe instanceof ConnectionDecorator) {
 			return ((ConnectionDecorator) pe).getConnection();
 		} else if (pe instanceof Shape) {
 			return ((Shape) pe).getContainer();
 		} else if (pe instanceof Connection) {
 			return ((Connection) pe).getParent();
 		} else if (pe instanceof Anchor) {
 			return ((Anchor) pe).getParent();
 		} else {
 			return null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#getProperty(org.eclipse.graphiti
 	 * .mm.pictograms.PropertyContainer, java.lang.String)
 	 */
 	public Property getProperty(PropertyContainer propertyContainer, String key) {
 		if (propertyContainer == null || key == null || !GraphitiInternal.getEmfService().isObjectAlive(propertyContainer)) {
 			return null;
 		}
 		Collection<Property> collection = propertyContainer.getProperties();
 		for (Iterator<Property> iter = collection.iterator(); iter.hasNext();) {
 			Property property = iter.next();
 			if (key.equals(property.getKey())) {
 				return property;
 			}
 		}
 		return null;
 	}
 
 	public String getPropertyValue(PropertyContainer propertyContainer, String key) {
 		Property property = getProperty(propertyContainer, key);
 		if (property != null) {
 			return property.getValue();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the x coordinate of the anchor relative to the diagram
 	 * 
 	 * @param anchor
 	 *            the given anchor
 	 * @return the x coordinate
 	 */
 	private int getRelativeToDiagramX(Anchor anchor) {
 		int ret = 0;
 		if (anchor == null) {
 			return ret;
 		}
 		if (anchor instanceof FixPointAnchor) {
 			FixPointAnchor fpa = (FixPointAnchor) anchor;
 			IRectangle gaBoundsForAnchor = getGaBoundsForAnchor(anchor);
 			ret = gaBoundsForAnchor.getX() + fpa.getLocation().getX();
 		} else if (anchor instanceof BoxRelativeAnchor) {
 			BoxRelativeAnchor bra = (BoxRelativeAnchor) anchor;
 			IRectangle gaBoundsForAnchor = getGaBoundsForAnchor(anchor);
 			ret = gaBoundsForAnchor.getX() + (int) Math.round(gaBoundsForAnchor.getWidth() * bra.getRelativeWidth());
 		} else if (anchor instanceof ChopboxAnchor) {
 			ret = Math.round(getWidthOfPictogramElement(anchor.getParent()) / 2);
 		}
 
 		if (anchor != null) {
 			AnchorContainer anchorContainer = anchor.getParent();
 			if (anchorContainer instanceof Shape) {
 				Shape shape = (Shape) anchorContainer;
 				ret = ret + getRelativeToDiagramX(shape);
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Returns the x coordinate of the shape relative to the diagram
 	 * 
 	 * @param shape
 	 *            the given shape
 	 * @return the x coordinate
 	 */
 	private int getRelativeToDiagramX(Shape shape) {
 		int ret = 0;
 
 		if (!(shape instanceof ConnectionDecorator)) {
 			while (shape != null && !(shape instanceof Diagram)) {
 				ret = ret + getXOfPictogramElement(shape);
 				shape = shape.getContainer();
 			}
 		} else {
 			ConnectionDecorator decorator = (ConnectionDecorator) shape;
 			ILocation midpoint = getConnectionMidpoint(decorator.getConnection(), decorator.getLocation());
 			ret = decorator.getGraphicsAlgorithm().getX() + midpoint.getX();
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the y coordinate of the anchor relative to the diagram
 	 * 
 	 * @param anchor
 	 *            the given anchor
 	 * @return the y coordinate
 	 */
 	private int getRelativeToDiagramY(Anchor anchor) {
 		int ret = 0;
 		if (anchor == null) {
 			return ret;
 		}
 		if (anchor instanceof FixPointAnchor) {
 			FixPointAnchor fpa = (FixPointAnchor) anchor;
 			IRectangle gaBoundsForAnchor = getGaBoundsForAnchor(anchor);
 			ret = gaBoundsForAnchor.getY() + fpa.getLocation().getY();
 		} else if (anchor instanceof BoxRelativeAnchor) {
 			BoxRelativeAnchor bra = (BoxRelativeAnchor) anchor;
 			IRectangle gaBoundsForAnchor = getGaBoundsForAnchor(anchor);
 			ret = gaBoundsForAnchor.getY() + (int) Math.round(gaBoundsForAnchor.getHeight() * bra.getRelativeHeight());
 		} else if (anchor instanceof ChopboxAnchor) {
 			ret = Math.round(getHeightOfPictogramElement(anchor.getParent()) / 2);
 		}
 
 		AnchorContainer anchorContainer = anchor.getParent();
 		if (anchorContainer instanceof Shape) {
 			Shape shape = (Shape) anchorContainer;
 			ret = ret + getRelativeToDiagramY(shape);
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Returns the y coordinate of the shape relative to the diagram
 	 * 
 	 * @param shape
 	 *            the given shape
 	 * @return the y coordinate
 	 */
 	private int getRelativeToDiagramY(Shape shape) {
 		int ret = 0;
 		if (!(shape instanceof ConnectionDecorator)) {
 			while (shape != null && !(shape instanceof Diagram)) {
 				ret = ret + getYOfPictogramElement(shape);
 				shape = shape.getContainer();
 			}
 		} else {
 			ConnectionDecorator decorator = (ConnectionDecorator) shape;
 			ILocation midpoint = getConnectionMidpoint(decorator.getConnection(), decorator.getLocation());
 			ret = decorator.getGraphicsAlgorithm().getY() + midpoint.getY();
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the size of the pictogram element (more exactly: size of its
 	 * graphics algorithm)
 	 * 
 	 * @param pe
 	 *            given pictogram element
 	 * @return the size
 	 */
 	private static int getWidthOfPictogramElement(PictogramElement pe) {
 		int ret = 0;
 		GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 		if (ga != null) {
 			ret = ga.getWidth();
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the location of the given pictogram element - more exactly the
 	 * location of the pictogram element's graphics algorithm.
 	 * 
 	 * @param pe
 	 *            the given pictogram element
 	 * @return the location
 	 */
 	protected static int getXOfPictogramElement(PictogramElement pe) {
 		int ret = 0;
 		GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 		if (ga != null) {
 			ret = ga.getX();
 		}
 		return ret;
 	}
 
 	/**
 	 * Returns the location of the given pictogram element - more exactly the
 	 * location of the pictogram element's graphics algorithm.
 	 * 
 	 * @param pe
 	 *            the given pictogram element
 	 * @return the location
 	 */
 	protected static int getYOfPictogramElement(PictogramElement pe) {
 		int ret = 0;
 		GraphicsAlgorithm ga = pe.getGraphicsAlgorithm();
 		if (ga != null) {
 			ret = ga.getY();
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#moveBendpoints(org.eclipse.graphiti
 	 * .ei.IExecutionInfo)
 	 */
 	public void moveBendpoints(IExecutionInfo executionInfo) {
 		Set<FreeFormConnection> connections = new HashSet<FreeFormConnection>();
 		Set<AnchorContainer> anchorContainers = new HashSet<AnchorContainer>();
 		final IFeatureAndContext[] featureAndContexts = executionInfo.getExecutionList();
 		int deltaX = 0;
 		int deltaY = 0;
 
 		/* get move vector */
 		for (IFeatureAndContext featureAndContext : featureAndContexts) {
 			IContext context = featureAndContext.getContext();
 			if (context instanceof MoveShapeContext) {
 				deltaX = ((MoveShapeContext) context).getDeltaX();
 				deltaY = ((MoveShapeContext) context).getDeltaY();
 				break;
 			}
 		}
 
 		/* any move needed? */
 		if (deltaX == 0 && deltaY == 0) {
 			return;
 		}
 
 		/* get all AnchorContainers to be moved */
 		for (IFeatureAndContext featureAndContext : featureAndContexts) {
 			IContext context = featureAndContext.getContext();
 			if (context instanceof MoveShapeContext) {
 				PictogramElement pe = ((MoveShapeContext) context).getPictogramElement();
 				if (pe instanceof AnchorContainer) {
 					anchorContainers.add((AnchorContainer) pe);
 				}
 			}
 		}
 
 		/*
 		 * examine the outgoing connection of each AnchorContainer to be moved
 		 * and check whether it equals an incoming connection on a different
 		 * AnchorContainer to be moved
 		 */
 		for (AnchorContainer anchorContainerToBeMoved : anchorContainers) {
 			List<Connection> outgoingConnections = getOutgoingConnections(anchorContainerToBeMoved);
 
 			for (Connection outgoingConnection : outgoingConnections) {
 				if (outgoingConnection instanceof FreeFormConnection) {
 					/*
 					 * check whether the outgoing Connection is an incoming
 					 * connection on some other AnchorContainer
 					 */
 					for (AnchorContainer anchorContainer : anchorContainers) {
 						if (anchorContainerToBeMoved != anchorContainer) {
 							List<Connection> incomingConnections = getIncomingConnections(anchorContainer);
 							if (incomingConnections.contains(outgoingConnection)) {
 								connections.add((FreeFormConnection) outgoingConnection);
 							}
 						}
 					}
 				}
 			}
 
 		}
 
 		/* finally move the bend points */
 		for (Connection connection : connections) {
 			if (connection instanceof FreeFormConnection) {
 				final FreeFormConnection freeFormConnection = (FreeFormConnection) connection;
 				List<Point> bendPoints = freeFormConnection.getBendpoints();
 				for (int i = 0; i < bendPoints.size(); i++) {
 					Point oldPoint = bendPoints.get(i);
 					Point newPoint = Graphiti.getGaCreateService().createPoint(oldPoint.getX() + deltaX, oldPoint.getY() + deltaY);
 					bendPoints.set(i, newPoint);
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#removeProperty(org.eclipse.graphiti
 	 * .mm.pictograms.PropertyContainer, java.lang.String)
 	 */
 	public boolean removeProperty(PropertyContainer propertyContainer, String key) {
 		Property property = getProperty(propertyContainer, key);
 		if (property != null) {
 			propertyContainer.getProperties().remove(property);
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#sendToBack(org.eclipse.graphiti
 	 * .mm.pictograms.Shape)
 	 */
 	public void sendToBack(Shape shape) {
 		ContainerShape parentContainerShape = shape.getContainer();
 		List<Shape> brotherAndSisterList = parentContainerShape.getChildren();
 		brotherAndSisterList.remove(shape);
 		brotherAndSisterList.add(0, shape);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IPeService#sendToFront(org.eclipse.graphiti
 	 * .mm.pictograms.Shape)
 	 */
 	public void sendToFront(Shape shape) {
 		ContainerShape parentContainerShape = shape.getContainer();
 		List<Shape> brotherAndSisterList = parentContainerShape.getChildren();
 		brotherAndSisterList.remove(shape);
 		brotherAndSisterList.add(shape);
 	}
 
 	public void setPropertyValue(PropertyContainer propertyContainer, String key, String value) {
 		if (propertyContainer == null) {
 			throw new InvalidParameterException("propertyContainer must not be null"); //$NON-NLS-1$
 		}
 
 		Property property = getProperty(propertyContainer, key);
 		if (property != null) {
 			removeProperty(propertyContainer, key);
 		}
 		property = MmFactory.eINSTANCE.createProperty();
 		property.setKey(key);
 		property.setValue(value);
 		propertyContainer.getProperties().add(property);
 	}
 }
