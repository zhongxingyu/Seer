 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
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
 package org.eclipse.graphiti.tests.cases;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.createNiceMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.isA;
 import static org.easymock.EasyMock.replay;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import junit.framework.Assert;
 
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.impl.AddBendpointContext;
 import org.eclipse.graphiti.features.context.impl.AreaAnchorContext;
 import org.eclipse.graphiti.features.context.impl.MoveBendpointContext;
 import org.eclipse.graphiti.features.context.impl.MoveConnectionDecoratorContext;
 import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
 import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
 import org.eclipse.graphiti.features.context.impl.RemoveBendpointContext;
 import org.eclipse.graphiti.features.context.impl.RemoveContext;
 import org.eclipse.graphiti.features.context.impl.ResizeShapeContext;
 import org.eclipse.graphiti.features.impl.DefaultAddBendpointFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveAnchorFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveBendpointFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveConnectionDecoratorFeature;
 import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
 import org.eclipse.graphiti.features.impl.DefaultReconnectionFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveBendpointFeature;
 import org.eclipse.graphiti.features.impl.DefaultRemoveFeature;
 import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
 import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
 import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.platform.IDiagramEditor;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.tests.reuse.GFAbstractTestCase;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class DefaultFeaturesTest extends GFAbstractTestCase {
 
 	// Fixture
 	private Diagram d;
 	private Shape s1;
 	private Shape s2;
 	private ChopboxAnchor a1;
 	private BoxRelativeAnchor a2;
 	private ContainerShape s3;
 	private FreeFormConnection c;
 	private ChopboxAnchor a3;
 
 	private static IFeatureProvider fpMock;
 
 	@BeforeClass
 	public static void prepareClass() {
 		fpMock = createNiceMock(IFeatureProvider.class);
 		IDiagramTypeProvider dtpMock = createMock(IDiagramTypeProvider.class);
		IDiagramEditor editorMock = createNiceMock(IDiagramEditor.class); // nice mock returns defaults
 		expect(fpMock.getDiagramTypeProvider()).andReturn(dtpMock).anyTimes();
 		expect(fpMock.getRemoveFeature(isA(RemoveContext.class))).andReturn(new DefaultRemoveFeature(fpMock));
 		expect(dtpMock.getDiagramEditor()).andReturn(editorMock).anyTimes();
 		replay(fpMock, dtpMock, editorMock);
 	}
 
 	@Before
 	public void initializeTest() {
 		d = Graphiti.getPeCreateService().createDiagram("tutorial", "test", true);
 		assertNotNull(d);
 		s1 = Graphiti.getPeCreateService().createShape(d, true);
 		s1.setGraphicsAlgorithm(Graphiti.getGaCreateService().createRectangle(d));
 		assertNotNull(s1);
 		s2 = Graphiti.getPeCreateService().createShape(d, true);
 		s2.setGraphicsAlgorithm(Graphiti.getGaCreateService().createRectangle(d));
 		s2.getGraphicsAlgorithm().setX(20);
 		assertNotNull(s2);
 		c = Graphiti.getPeCreateService().createFreeFormConnection(d);
 		assertNotNull(c);
 		a1 = Graphiti.getPeCreateService().createChopboxAnchor(s1);
 		assertNotNull(a1);
 		a2 = Graphiti.getPeCreateService().createBoxRelativeAnchor(s2);
 		a2.setRelativeHeight(0.5);
 		a2.setRelativeWidth(0.5);
 		assertNotNull(a2);
 		c.setStart(a1);
 		c.setEnd(a2);
 		s3 = Graphiti.getPeCreateService().createContainerShape(d, true);
 		s3.setGraphicsAlgorithm(Graphiti.getGaCreateService().createRectangle(d));
 		s3.getGraphicsAlgorithm().setX(50);
 		a3 = Graphiti.getPeCreateService().createChopboxAnchor(s3);
 		ResourceImpl resource = new ResourceImpl();
 		resource.getContents().add(d);
 
 	}
 
 	@Test
 	public void move() {
 		ILocation connectionMidpoint = Graphiti.getPeLayoutService().getConnectionMidpoint(c, 0.5);
 		Assert.assertEquals(10, connectionMidpoint.getX());
 		Assert.assertEquals(0, connectionMidpoint.getY());
 
 		// Move while container remains the same
 		DefaultMoveShapeFeature moveFeature = new DefaultMoveShapeFeature(fpMock);
 		MoveShapeContext moveShapeContext = new MoveShapeContext(s1);
 		moveShapeContext.setX(10);
 		moveShapeContext.setY(0);
 		moveShapeContext.setSourceContainer(d);
 		moveShapeContext.setTargetContainer(d);
 		boolean canExecute = moveFeature.canExecute(moveShapeContext);
 		assertTrue(canExecute);
 		moveFeature.execute(moveShapeContext);
 		connectionMidpoint = Graphiti.getPeLayoutService().getConnectionMidpoint(c, 0.5);
 		Assert.assertEquals(15, connectionMidpoint.getX());
 		Assert.assertEquals(0, connectionMidpoint.getY());
 
 		// Move to different target container
 		moveShapeContext.setX(50);
 		moveShapeContext.setY(0);
 		moveShapeContext.setSourceContainer(d);
 		moveShapeContext.setTargetContainer(s3);
		// canExecute() would fail in default implementation, since source and target container differ
 		moveFeature.execute(moveShapeContext);
 		assertEquals(50, s1.getGraphicsAlgorithm().getX());
 		assertEquals(s3, s1.getContainer());
 
 	}
 
 	@Test
 	public void reconnect() {
 		DefaultReconnectionFeature reconnectionFeature = new DefaultReconnectionFeature(fpMock);
		ReconnectionContext context = new ReconnectionContext(c, a2, a3);
 		assertTrue(reconnectionFeature.canExecute(context));
 		assertTrue(reconnectionFeature.canReconnect(context));
 		reconnectionFeature.execute(context);
 		assertEquals(a3, c.getEnd());
 	}
 
 	@Test
 	public void bendpoints() {
 		{
 			DefaultAddBendpointFeature addBendpointFeature = new DefaultAddBendpointFeature(fpMock);
 			AddBendpointContext context = new AddBendpointContext(c, 10, 0, 0);
 			assertTrue(addBendpointFeature.canExecute(context));
 			assertTrue(addBendpointFeature.canAddBendpoint(context));
 			addBendpointFeature.execute(context);
 			assertEquals(1, c.getBendpoints().size());
 		}
 
 		{
 			org.eclipse.graphiti.mm.algorithms.styles.Point point = c.getBendpoints().get(0);
 			DefaultMoveBendpointFeature moveBendpointFeature = new DefaultMoveBendpointFeature(fpMock);
 			MoveBendpointContext context = new MoveBendpointContext(point);
 			context.setConnection(c);
 			context.setLocation(10, 5);
 			assertTrue(moveBendpointFeature.canExecute(context));
 			assertTrue(moveBendpointFeature.canMoveBendpoint(context));
 			moveBendpointFeature.execute(context);
 			assertEquals(5, c.getBendpoints().get(0).getY());
 		}
 
 		{
 			DefaultRemoveBendpointFeature removeBendpointFeature = new DefaultRemoveBendpointFeature(fpMock);
 			RemoveBendpointContext context = new RemoveBendpointContext(c, c.getBendpoints().get(0));
 			context.setBendpointIndex(0);
 			assertTrue(removeBendpointFeature.canExecute(context));
 			assertTrue(removeBendpointFeature.canRemoveBendpoint(context));
 			removeBendpointFeature.execute(context);
 			assertTrue(c.getBendpoints().isEmpty());
 		}
 	}
 
 	@Test
 	public void resize() {
 		DefaultResizeShapeFeature feature = new DefaultResizeShapeFeature(fpMock);
 		ResizeShapeContext context = new ResizeShapeContext(s3);
 		context.setSize(5, 5);
 		assertTrue(feature.canExecute(context));
 		assertTrue(feature.canResizeShape(context));
 		feature.execute(context);
 		assertEquals(5, s3.getGraphicsAlgorithm().getHeight());
 	}
 
 	@Test
 	public void remove() {
 		DefaultRemoveFeature feature = new DefaultRemoveFeature(fpMock);
 		RemoveContext context = new RemoveContext(s1);
 		assertTrue(feature.canExecute(context));
 		assertTrue(feature.canRemove(context));
 		feature.execute(context);
 		// Check if Shape got removed
 		assertFalse(d.getChildren().contains(s1));
		// Connection should be removed as well, since it is connected to the shape
 		assertTrue(d.getConnections().isEmpty());
 	}
 
 	@Test
 	public void moveAnchor() {
 		DefaultMoveAnchorFeature feature = new DefaultMoveAnchorFeature(fpMock);
 		s2.getGraphicsAlgorithm().setWidth(10);
 		s2.getGraphicsAlgorithm().setHeight(10);
 		AreaAnchorContext context = new AreaAnchorContext(a2);
 		context.setX(1);
 		context.setY(1);
 		assertTrue(feature.canExecute(context));
 		assertTrue(feature.canMoveAnchor(context));
 		feature.execute(context);
 		Assert.assertEquals(0.1, a2.getRelativeWidth());
 		Assert.assertEquals(0.1, a2.getRelativeHeight());
 	}
 
 	@Test
 	public void moveConnectionDecorator() {
 		ConnectionDecorator dec = Graphiti.getPeCreateService().createConnectionDecorator(c, true, 0.5, true);
 		Graphiti.getGaCreateService().createDefaultText(d, dec, "TEST");
 		DefaultMoveConnectionDecoratorFeature feature = new DefaultMoveConnectionDecoratorFeature(fpMock);
 		MoveConnectionDecoratorContext context = new MoveConnectionDecoratorContext(dec, 5, 5, true);
 		assertTrue(feature.canExecute(context));
 		assertTrue(feature.canMoveConnectionDecorator(context));
 		feature.execute(context);
 		Assert.assertEquals(dec.getGraphicsAlgorithm().getX(), 5);
 
 	}
 
 	@After
 	public void uninitializeTest() {
 
 	}
 
 	@AfterClass
 	public static void afterClass() {
 		fpMock = null;
 	}
 
 }
