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
 package org.eclipse.graphiti.ui.tests;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.createNiceMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.reset;
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 import java.util.Vector;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.draw2d.Bendpoint;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.datatypes.ILocation;
 import org.eclipse.graphiti.datatypes.IRectangle;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.IFeature;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.IMappingProvider;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.context.IAreaContext;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.ICreateContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.context.impl.AddBendpointContext;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.context.impl.AddContext;
 import org.eclipse.graphiti.features.context.impl.AreaAnchorContext;
 import org.eclipse.graphiti.features.context.impl.AreaContext;
 import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
 import org.eclipse.graphiti.features.context.impl.CreateContext;
 import org.eclipse.graphiti.features.context.impl.CustomContext;
 import org.eclipse.graphiti.features.context.impl.DirectEditingContext;
 import org.eclipse.graphiti.features.context.impl.LayoutContext;
 import org.eclipse.graphiti.features.context.impl.LocationContext;
 import org.eclipse.graphiti.features.context.impl.MoveBendpointContext;
 import org.eclipse.graphiti.features.context.impl.MoveConnectionDecoratorContext;
 import org.eclipse.graphiti.features.context.impl.MoveContext;
 import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
 import org.eclipse.graphiti.features.context.impl.PrintContext;
 import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
 import org.eclipse.graphiti.features.context.impl.RemoveBendpointContext;
 import org.eclipse.graphiti.features.context.impl.ResizeContext;
 import org.eclipse.graphiti.features.context.impl.ResizeShapeContext;
 import org.eclipse.graphiti.features.context.impl.SaveImageContext;
 import org.eclipse.graphiti.features.context.impl.SplitConnectionContext;
 import org.eclipse.graphiti.internal.ExternalPictogramLink;
 import org.eclipse.graphiti.internal.command.CommandContainer;
 import org.eclipse.graphiti.internal.command.DirectEditingFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.ICommand;
 import org.eclipse.graphiti.internal.command.MoveShapeFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.command.ResizeShapeFeatureCommandWithContext;
 import org.eclipse.graphiti.internal.datatypes.impl.DimensionImpl;
 import org.eclipse.graphiti.internal.datatypes.impl.LocationImpl;
 import org.eclipse.graphiti.internal.datatypes.impl.RectangleImpl;
 import org.eclipse.graphiti.mm.algorithms.AbstractText;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Image;
 import org.eclipse.graphiti.mm.algorithms.PlatformGraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Font;
 import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.RenderingStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.Style;
 import org.eclipse.graphiti.mm.pictograms.Anchor;
 import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.palette.impl.StackEntry;
 import org.eclipse.graphiti.platform.IPlatformImageConstants;
 import org.eclipse.graphiti.platform.ga.RendererContext;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.tb.ContextButtonEntry;
 import org.eclipse.graphiti.tb.ContextEntryHelper;
 import org.eclipse.graphiti.tb.ContextMenuEntry;
 import org.eclipse.graphiti.tb.DynamicContextMenuEntry;
 import org.eclipse.graphiti.tb.IContextEntry;
 import org.eclipse.graphiti.tb.ImageDecorator;
 import org.eclipse.graphiti.tests.reuse.GFAbstractTestCase;
 import org.eclipse.graphiti.ui.editor.DiagramEditorFactory;
 import org.eclipse.graphiti.ui.internal.command.AddModelObjectCommand;
 import org.eclipse.graphiti.ui.internal.command.ContextEntryCommand;
 import org.eclipse.graphiti.ui.internal.command.CreateModelObjectCommand;
 import org.eclipse.graphiti.ui.internal.command.GFCommand;
 import org.eclipse.graphiti.ui.internal.config.IConfigurationProvider;
 import org.eclipse.graphiti.ui.internal.parts.directedit.IDirectEditHolder;
 import org.eclipse.graphiti.ui.internal.requests.ContextButtonDragRequest;
 import org.eclipse.graphiti.ui.internal.requests.GFDirectEditRequest;
 import org.eclipse.graphiti.ui.internal.util.DataTypeTransformation;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.graphics.FontData;
 import org.junit.Test;
 
 /**
  *
  */
 public class PackageTest extends GFAbstractTestCase {
 
 	private static final String DUMMY = "dummy";
 
 	@Test
 	public void testInternalRequests() throws Exception {
 		// test
 		GFDirectEditRequest myDirectEditRequest = new GFDirectEditRequest();
 
 		IDirectEditHolder directEditHolderMock = createNiceMock(IDirectEditHolder.class);
 		replay(directEditHolderMock);
 
 		myDirectEditRequest.setDirectEditingContext(directEditHolderMock);
 
 		IDirectEditHolder directEditHolder = myDirectEditRequest.getDirectEditHolder();
 		assertEquals(directEditHolderMock, directEditHolder);
 
 		// test
 		ContextButtonDragRequest myContextButtonDragRequest = new ContextButtonDragRequest();
 
 		ContextButtonEntry contextButtonEntry = new ContextButtonEntry(createMock(IFeature.class), createMock(IContext.class));
 		contextButtonEntry.addDragAndDropFeature(null);
 		contextButtonEntry.getDragAndDropFeatures();
 		contextButtonEntry.addContextButtonMenuEntry(contextButtonEntry);
 		contextButtonEntry.getContextButtonMenuEntries();
 
 		myContextButtonDragRequest.setContextButtonEntry(contextButtonEntry);
 
 		ContextButtonEntry contextButtonEntry2 = myContextButtonDragRequest.getContextButtonEntry();
 		assertEquals(contextButtonEntry, contextButtonEntry2);
 	}
 
 	@Test
 	public void testCommand() throws Exception {
 		ICommand commandMock = createNiceMock(ICommand.class);
 		replay(commandMock);
 
 		// Connection connectionMock = createNiceMock(Connection.class);
 		// replay(connectionMock);
 
 		// #########################################################
 
 		IMoveShapeFeature moveShapeFeatureMock = createNiceMock(IMoveShapeFeature.class);
 		replay(moveShapeFeatureMock);
 
 		// IMoveShapeContext moveShapeContextMock =
 		// createNiceMock(IMoveShapeContext.class);
 		// expect(moveShapeContextMock.get)
 		// replay(moveShapeContextMock);
 		Shape shapeMock = createNiceMock(Shape.class);
 		replay(shapeMock);
 
 		MoveShapeContext moveShapeContext = new MoveShapeContext(shapeMock);
 		moveShapeContext.setLocation(10, 10);
 		moveShapeContext.getShape();
 		moveShapeContext.getTargetConnection();
 		moveShapeContext.getPictogramElement();
 		moveShapeContext.getDeltaX();
 		moveShapeContext.getDeltaY();
 		moveShapeContext.getSourceContainer();
 		moveShapeContext.getTargetConnection();
 		moveShapeContext.getTargetContainer();
 		moveShapeContext.getX();
 		moveShapeContext.getY();
 
 		MoveShapeFeatureCommandWithContext myMoveShapeFeatureCommandWithContext = new MoveShapeFeatureCommandWithContext(
 				moveShapeFeatureMock, moveShapeContext);
 
 		myMoveShapeFeatureCommandWithContext.canExecute();
 		myMoveShapeFeatureCommandWithContext.execute();
 
 		// #########################################################
 
 		// testorg.eclipse.graphiti.command.CommandContainer
 		IFeatureProvider featureProviderMock = createNiceMock(IFeatureProvider.class);
 		replay(featureProviderMock);
 
 		commandMock = createNiceMock(ICommand.class);
 		replay(commandMock);
 
 		CommandContainer myCommandContainer = new CommandContainer(featureProviderMock);
 
 		myCommandContainer.add(myMoveShapeFeatureCommandWithContext);
 		myCommandContainer.add(commandMock);
 
 		myCommandContainer.getCommands();
 		myCommandContainer.getDescription();
 		myCommandContainer.getFeatureProvider();
 		assertTrue(myCommandContainer.containsCommands());
 		myCommandContainer.canExecute();
 		myCommandContainer.undo();
 		myCommandContainer.execute();
 		myCommandContainer.undo();
 
 		// #########################################################
 
 		// testorg.eclipse.graphiti.command.ResizeShapeFeatureCommandWithContext
 		IResizeShapeContext resizeShapeContextMock = createNiceMock(IResizeShapeContext.class);
 		replay(resizeShapeContextMock);
 
 		IFeature featureMock = createNiceMock(IResizeShapeFeature.class);
 		replay(featureMock);
 
 		ResizeShapeFeatureCommandWithContext myResizeShapeFeatureCommandWithContext = new ResizeShapeFeatureCommandWithContext(featureMock,
 				resizeShapeContextMock);
 
 		myResizeShapeFeatureCommandWithContext.canExecute();
 		myResizeShapeFeatureCommandWithContext.execute();
 
 		// #########################################################
 
 		IDirectEditingContext directEditingContextMock = createNiceMock(IDirectEditingContext.class);
 		replay(directEditingContextMock);
 
 		IDirectEditingFeature directEditingFeatureMock = createNiceMock(IDirectEditingFeature.class);
 		replay(directEditingFeatureMock);
 
 		DirectEditingFeatureCommandWithContext myDirectEditingFeatureCommandWithContext = new DirectEditingFeatureCommandWithContext(
 				directEditingFeatureMock, directEditingContextMock, new String("Value"));
 
 		myDirectEditingFeatureCommandWithContext.execute();
 	}
 
 	@Test
 	public void testDatatypesImpl() throws Exception {
 
 		// test DimensionImpl
 		final int WIDTH = 30;
 		final int HEIGHT = 20;
 		final int WIDTH_DIFF = 10;
 		final int HEIGHT_DIFF = 5;
 		final int WIDTH_2 = WIDTH + WIDTH_DIFF;
 		final int HEIGHT_2 = HEIGHT + HEIGHT_DIFF;
 
 		DimensionImpl d1 = new DimensionImpl(WIDTH, HEIGHT);
 		assertEquals(WIDTH, d1.getWidth());
 		assertEquals(HEIGHT, d1.getHeight());
 
 		d1.hashCode();
 
 		DimensionImpl d2 = new DimensionImpl(d1);
 		assertEquals(WIDTH, d2.getWidth());
 		assertEquals(HEIGHT, d2.getHeight());
 
 		d2.scale(2);
 		assertEquals(2 * WIDTH, d2.getWidth());
 		assertEquals(2 * HEIGHT, d2.getHeight());
 
 		d2.setDimension(WIDTH, HEIGHT);
 		assertEquals(WIDTH, d2.getWidth());
 		assertEquals(HEIGHT, d2.getHeight());
 
 		d2.expand(WIDTH_DIFF, HEIGHT_DIFF);
 		assertEquals(WIDTH_2, d2.getWidth());
 		assertEquals(HEIGHT_2, d2.getHeight());
 
 		d2.setDimension(d1);
 		assertEquals(WIDTH, d2.getWidth());
 		assertEquals(HEIGHT, d2.getHeight());
 
 		IDimension d3 = d1.getDimensionCopy();
 		assertTrue(d1.equals(d1));
 		assertTrue(d1.equals(d3));
 		assertFalse(d1.equals(DUMMY));
 
 		// #########################################################
 
 		// test LocationImpl
 		final int X = 15;
 		final int Y = 25;
 		final int DX = 5;
 		final int DY = 10;
 		final int X2 = X + DX;
 		final int Y2 = Y + DY;
 
 		LocationImpl l1 = new LocationImpl(X, Y);
 		assertEquals(X, l1.getX());
 		assertEquals(Y, l1.getY());
 
 		l1.hashCode();
 		l1.toString();
 
 		LocationImpl l2 = new LocationImpl(l1);
 		assertEquals(X, l2.getX());
 		assertEquals(Y, l2.getY());
 
 		l2.scale(2);
 		assertEquals(2 * X, l2.getX());
 		assertEquals(2 * Y, l2.getY());
 
 		l2.setLocation(X, Y);
 		assertEquals(X, l2.getX());
 		assertEquals(Y, l2.getY());
 
 		l2.translate(DX, DY);
 		assertEquals(X2, l2.getX());
 		assertEquals(Y2, l2.getY());
 
 		l2.setLocation(l1);
 		assertEquals(X, l2.getX());
 		assertEquals(Y, l2.getY());
 
 		ILocation l3 = l1.getLocationCopy();
 		assertTrue(l1.equals(l1));
 		assertTrue(l1.equals(l3));
 		assertFalse(l1.equals(DUMMY));
 
 		// #########################################################
 
 		// test LocationImpl
 		RectangleImpl r1 = new RectangleImpl(WIDTH, HEIGHT);
 		// RectangleImpl r2 = new RectangleImpl(r1);
 
 		r1.hashCode();
 		r1.toString();
 
 		r1.contains(l3);
 
 		r1.expand(WIDTH_DIFF, HEIGHT_DIFF);
 		r1.scale(2);
 		r1.translate(DX, DY);
 
 		IDimension d4 = r1.getDimensionCopy();
 		ILocation l4 = r1.getLocationCopy();
 		IRectangle r2 = r1.getRectangleCopy();
 
 		r1.setDimension(d4);
 		r1.setDimension(WIDTH, HEIGHT);
 
 		r1.setLocation(l4);
 		r1.setLocation(0, 0);
 
 		r1.setRectangle(r2);
 
 		assertTrue(r1.equals(r1));
 		assertTrue(r1.equals(r2));
 		assertFalse(r1.equals(DUMMY));
 	}
 
 	@Test
 	@SuppressWarnings("deprecation")
 	public void testTb() throws Exception {
 
 		// ContextButtonEntry and ContextEntryHelper
 		ContextButtonEntry cbe = new ContextButtonEntry(null, null);
 		ContextEntryHelper.markAsUpdateContextEntry(cbe);
 		ContextEntryHelper.markAsCollapseContextEntry(cbe, true);
 		ContextEntryHelper.markAsCollapseContextEntry(cbe, false);
 		ContextEntryHelper.createCollapseContextButton(false, null, null);
 
 		new ContextButtonEntry(null, null, 0);
 		new ContextButtonEntry(null, null, 0, 0);
 
 		// ImageRenderingDecorator
 		final int X = 10;
 		final int Y = 20;
 
 		ImageDecorator rd = new ImageDecorator(IPlatformImageConstants.IMG_ECLIPSE_ERROR);
 		rd.setX(X);
 		rd.setY(Y);
 		rd.setMessage(DUMMY);
 
 		rd.getX();
 		rd.getY();
 		rd.getMessage();
 		rd.getImageId();
 
 		// DynamicContextMenuEntry
 		DynamicContextMenuEntry cme = new DynamicContextMenuEntry(null, null);
 		cme.setText(DUMMY);
 		cme.setMinimumSubmenuEntries(5);
 		cme.add(new ContextMenuEntry(null, null));
 		assertFalse(cme.isSubmenu());
 	}
 
 	@Test
 	public void testUiInternalCommand() throws Exception {
 		// test
 		IFeatureProvider featureProviderMock = createNiceMock(IFeatureProvider.class);
 		replay(featureProviderMock);
 
 		IDiagramTypeProvider diagramTypeProviderMock = createNiceMock(IDiagramTypeProvider.class);
 		expect(diagramTypeProviderMock.getFeatureProvider()).andReturn(null).anyTimes();
 		replay(diagramTypeProviderMock);
 
 		IConfigurationProvider configurationProviderMock = createNiceMock(IConfigurationProvider.class);
 		expect(configurationProviderMock.getDiagramTypeProvider()).andReturn(diagramTypeProviderMock).anyTimes();
 		replay(configurationProviderMock);
 
 		ContainerShape containerShapeMock = createNiceMock(ContainerShape.class);
 		replay(containerShapeMock);
 
 		IAdaptable adaptableMock = createNiceMock(IAdaptable.class);
 		// RefObject aRefObject = new ContainerShapeImpl();
 		// expect(adaptableMock.getAdapter(RefObject.class)).andReturn(aRefObject).anyTimes();
 		expect(adaptableMock.getAdapter(EObject.class)).andReturn(containerShapeMock).anyTimes();
 		replay(adaptableMock);
 
 		ISelection selection = new StructuredSelection(new Object[] { adaptableMock });
 
 		Rectangle rectangle = new Rectangle();
 
 		AddModelObjectCommand myAddModelObjectCommand = new AddModelObjectCommand(configurationProviderMock, containerShapeMock, selection,
 				rectangle);
 
 		assertFalse(myAddModelObjectCommand.canUndo());
 
 		myAddModelObjectCommand.canExecute();
 
 		reset(diagramTypeProviderMock);
 		expect(diagramTypeProviderMock.getFeatureProvider()).andReturn(featureProviderMock).anyTimes();
 		replay(diagramTypeProviderMock);
 
 		myAddModelObjectCommand.canExecute();
 
 		myAddModelObjectCommand.execute();
 
 		IContextEntry contextEntryMock = createNiceMock(IContextEntry.class);
 		replay(contextEntryMock);
 
 		ContextEntryCommand myContextEntryCommand = new ContextEntryCommand(contextEntryMock);
 
 		myContextEntryCommand.execute();
 
 		// test
 		GFCommand myGraphitiCommand = new GFCommand(configurationProviderMock, "Test");
 		myGraphitiCommand.toString();
 		myGraphitiCommand = new GFCommand(configurationProviderMock);
 
 		IContext contextMock = createNiceMock(IContext.class);
 		replay(contextMock);
 		myGraphitiCommand.setContext(contextMock);
 
 		IContext context = myGraphitiCommand.getContext();
 		assertEquals(contextMock, context);
 
 		IFeature featureMock = createNiceMock(IFeature.class);
 		replay(featureMock);
 		myGraphitiCommand.setFeature(featureMock);
 
 		IFeature feature = myGraphitiCommand.getFeature();
 		assertEquals(featureMock, feature);
 
 		// test
 		ICreateFeature createFeatureMock = createNiceMock(ICreateFeature.class);
 		replay(createFeatureMock);
 
 		ICreateContext createContextMock = createNiceMock(ICreateContext.class);
 		replay(createContextMock);
 
 		CreateModelObjectCommand myCreateModelObjectCommand = new CreateModelObjectCommand(configurationProviderMock, createFeatureMock,
 				createContextMock, rectangle);
 
 		myCreateModelObjectCommand.canExecute();
 
 		myCreateModelObjectCommand.canUndo();
 
 		myCreateModelObjectCommand.undo();
 
 		// myCreateModelObjectCommand.execute();
 	}
 
 	@Test
 	public void testUiInternalUtil() throws Exception {
 		DataTypeTransformation myDataTypeTransformation = new DataTypeTransformation();
 		assertNotNull(myDataTypeTransformation);
 
 		org.eclipse.graphiti.mm.algorithms.styles.Point pointMock = createNiceMock(org.eclipse.graphiti.mm.algorithms.styles.Point.class);
 		replay(pointMock);
 
 		Point draw2dPoint = DataTypeTransformation.toDraw2dPoint(pointMock);
 		assertNotNull(draw2dPoint);
 
 		Bendpoint draw2dBendPoint = DataTypeTransformation.toDraw2dBendPoint(pointMock);
 		assertNotNull(draw2dBendPoint);
 
 		Vector<org.eclipse.graphiti.mm.algorithms.styles.Point> points = new Vector<org.eclipse.graphiti.mm.algorithms.styles.Point>();
 		points.add(pointMock);
 		points.add(pointMock);
 
 		DataTypeTransformation.toDraw2dPointList(points);
 
 		Color colorMock = createNiceMock(Color.class);
 		replay(colorMock);
 
 		EList<Color> colors = new BasicEList<Color>();
 		colors.add(colorMock);
 
 		Diagram diagramMock = createNiceMock(Diagram.class);
 		expect(diagramMock.getColors()).andReturn(colors).anyTimes();
 		replay(diagramMock);
 
 		assertTrue(DataTypeTransformation.toDraw2dLineStyle(LineStyle.DASH) == Graphics.LINE_DASH);
 		assertTrue(DataTypeTransformation.toDraw2dLineStyle(LineStyle.DASHDOT) == Graphics.LINE_DASHDOT);
 		assertTrue(DataTypeTransformation.toDraw2dLineStyle(LineStyle.DASHDOTDOT) == Graphics.LINE_DASHDOTDOT);
 		assertTrue(DataTypeTransformation.toDraw2dLineStyle(LineStyle.DOT) == Graphics.LINE_DOT);
 		assertTrue(DataTypeTransformation.toDraw2dLineStyle(LineStyle.SOLID) == Graphics.LINE_SOLID);
 		assertTrue(DataTypeTransformation.toDraw2dLineStyle(null) == Graphics.LINE_SOLID);
 
 		Font fontMock = createNiceMock(Font.class);
 		expect(fontMock.isItalic()).andReturn(true).anyTimes();
 		expect(fontMock.isBold()).andReturn(true).anyTimes();
 		expect(fontMock.getSize()).andReturn(10).anyTimes();
 		expect(fontMock.getName()).andReturn("Arial").anyTimes();
 		replay(fontMock);
 
 		FontData fontData = DataTypeTransformation.toFontData(fontMock);
 		assertNotNull(fontData);
 		DataTypeTransformation.toFontData(null);
 	}
 
 	@Test
 	public void testFeaturesContextImpl() throws Exception {
 		String s = null;
 
 		org.eclipse.graphiti.mm.pictograms.Connection connectionMock = createNiceMock(org.eclipse.graphiti.mm.pictograms.Connection.class);
 		replay(connectionMock);
 
 		Shape shapeMock = createNiceMock(Shape.class);
 		replay(shapeMock);
 
 		SplitConnectionContext mySplitConnectionContext = new SplitConnectionContext(connectionMock, shapeMock);
 
 		org.eclipse.graphiti.mm.pictograms.Connection con = mySplitConnectionContext.getConnection();
 		assertNotNull(con);
 		assertEquals(connectionMock, con);
 
 		Shape shape = mySplitConnectionContext.getShape();
 		assertNotNull(shape);
 		assertEquals(shapeMock, shape);
 
 		s = mySplitConnectionContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test AreaAnchorContext
 		Anchor anchorMock = createNiceMock(Anchor.class);
 		replay(anchorMock);
 
 		AreaAnchorContext myAreaAnchorContext = new AreaAnchorContext(anchorMock);
 
 		Anchor anchor = myAreaAnchorContext.getAnchor();
 		assertNotNull(anchor);
 		assertEquals(anchorMock, anchor);
 
 		AnchorContainer containerMock = createNiceMock(AnchorContainer.class);
 		replay(containerMock);
 
 		myAreaAnchorContext.setSourceContainer(containerMock);
 		AnchorContainer sourceContainer = myAreaAnchorContext.getSourceContainer();
 		assertNotNull(sourceContainer);
 		assertEquals(containerMock, sourceContainer);
 
 		myAreaAnchorContext.setTargetContainer(containerMock);
 		AnchorContainer targetContainer = myAreaAnchorContext.getTargetContainer();
 		assertNotNull(targetContainer);
 		assertEquals(containerMock, targetContainer);
 
 		s = null;
 		s = myAreaAnchorContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test CreateConnectionContext
 		CreateConnectionContext myCreateConnectionContext = new CreateConnectionContext();
 
 		myCreateConnectionContext.setSourceAnchor(anchorMock);
 		anchor = myCreateConnectionContext.getSourceAnchor();
 		assertNotNull(anchor);
 		assertEquals(anchorMock, anchor);
 
 		myCreateConnectionContext.setTargetAnchor(anchorMock);
 		anchor = myCreateConnectionContext.getTargetAnchor();
 		assertNotNull(anchor);
 		assertEquals(anchorMock, anchor);
 
 		PictogramElement peMock = createNiceMock(PictogramElement.class);
 		replay(peMock);
 
 		myCreateConnectionContext.setSourcePictogramElement(peMock);
 		PictogramElement pictogramElement = myCreateConnectionContext.getSourcePictogramElement();
 		assertNotNull(pictogramElement);
 		assertEquals(peMock, pictogramElement);
 
 		myCreateConnectionContext.setTargetPictogramElement(peMock);
 		pictogramElement = myCreateConnectionContext.getTargetPictogramElement();
 		assertNotNull(pictogramElement);
 		assertEquals(peMock, pictogramElement);
 
 		s = null;
 		s = myCreateConnectionContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test AddBendpointContext
 		FreeFormConnection freeFormConnectionMock = createNiceMock(FreeFormConnection.class);
 		replay(freeFormConnectionMock);
 
 		AddBendpointContext myAddBendpointContext = new AddBendpointContext(freeFormConnectionMock, 0, 0, 0);
 
 		myAddBendpointContext.getBendpoint();
 
 		assertEquals(0, myAddBendpointContext.getBendpointIndex());
 
 		FreeFormConnection freeFormConnection = myAddBendpointContext.getConnection();
 		assertNotNull(freeFormConnection);
 		assertEquals(freeFormConnectionMock, freeFormConnection);
 
 		s = null;
 		s = myAddBendpointContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test MoveConnectionDecoratorContext
 		ConnectionDecorator connectionDecoratorMock = createNiceMock(ConnectionDecorator.class);
 		replay(connectionDecoratorMock);
 
 		MoveConnectionDecoratorContext myMoveConnectionDecoratorContext = new MoveConnectionDecoratorContext(connectionDecoratorMock, 0, 0,
 				true);
 
 		ConnectionDecorator connectionDecorator = myMoveConnectionDecoratorContext.getConnectionDecorator();
 		assertNotNull(connectionDecorator);
 		assertEquals(connectionDecoratorMock, connectionDecorator);
 
 		assertTrue(myMoveConnectionDecoratorContext.isExecuteAllowed());
 
 		s = null;
 		s = myMoveConnectionDecoratorContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test ReconnectionContext
		ReconnectionContext myReconnectionContext = new ReconnectionContext(connectionMock, anchorMock, anchorMock);
 
 		con = myReconnectionContext.getConnection();
 		assertNotNull(con);
 		assertEquals(connectionMock, con);
 
 		anchor = myReconnectionContext.getNewAnchor();
 		assertNotNull(anchor);
 		assertEquals(anchorMock, anchor);
 
 		anchor = myReconnectionContext.getOldAnchor();
 		assertNotNull(anchor);
 		assertEquals(anchorMock, anchor);
 
 		myReconnectionContext.setTargetPictogramElement(peMock);
 
 		pictogramElement = myReconnectionContext.getTargetPictogramElement();
 		assertNotNull(pictogramElement);
 		assertEquals(peMock, pictogramElement);
 
 		s = null;
 		s = myReconnectionContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		myReconnectionContext.setReconnectType("setReconnectType");
 		assertEquals("setReconnectType", myReconnectionContext.getReconnectType());
 
 		// test RemoveBendpointContext
 		org.eclipse.graphiti.mm.algorithms.styles.Point pointMock = createNiceMock(org.eclipse.graphiti.mm.algorithms.styles.Point.class);
 		replay(pointMock);
 
 		RemoveBendpointContext myRemoveBendpointContext = new RemoveBendpointContext(freeFormConnectionMock, pointMock);
 
 		myRemoveBendpointContext.setBendpointIndex(0);
 
 		assertEquals(0, myRemoveBendpointContext.getBendpointIndex());
 
 		freeFormConnection = myRemoveBendpointContext.getConnection();
 		assertNotNull(freeFormConnection);
 		assertEquals(freeFormConnectionMock, freeFormConnection);
 
 		org.eclipse.graphiti.mm.algorithms.styles.Point bendpoint = myRemoveBendpointContext.getBendpoint();
 		assertNotNull(bendpoint);
 		assertEquals(pointMock, bendpoint);
 
 		s = null;
 		s = myRemoveBendpointContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test MoveContext
 		MoveContext myMoveContext = new MoveContext();
 		assertNotNull(myMoveContext);
 		myMoveContext = new MoveContext(0, 0);
 		assertNotNull(myMoveContext);
 
 		// test MoveBendpointContext
 		MoveBendpointContext myMoveBendpointContext = new MoveBendpointContext(pointMock);
 
 		bendpoint = myMoveBendpointContext.getBendpoint();
 		assertNotNull(bendpoint);
 		assertEquals(pointMock, bendpoint);
 
 		myMoveBendpointContext.setBendpointIndex(0);
 		assertEquals(0, myMoveBendpointContext.getBendpointIndex());
 
 		myMoveBendpointContext.setConnection(freeFormConnectionMock);
 		freeFormConnection = myMoveBendpointContext.getConnection();
 		assertNotNull(freeFormConnection);
 		assertEquals(freeFormConnectionMock, freeFormConnection);
 
 		s = null;
 		s = myMoveBendpointContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test SaveImageContext
 		SaveImageContext mySaveImageContext = new SaveImageContext();
 
 		PictogramElement[] pictogramElements1 = new PictogramElement[] { peMock };
 		mySaveImageContext = new SaveImageContext(pictogramElements1);
 
 		PictogramElement[] pictogramElements2 = mySaveImageContext.getPictogramElements();
 		assertNotNull(pictogramElements2);
 		assertArrayEquals(pictogramElements1, pictogramElements2);
 
 		s = null;
 		s = mySaveImageContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test LocationContext
 		LocationContext myLocationContext = new LocationContext();
 
 		myLocationContext = new LocationContext(10, 10);
 		myLocationContext.setX(10);
 		myLocationContext.setY(10);
 		assertEquals(10, myLocationContext.getX());
 		assertEquals(10, myLocationContext.getY());
 
 		s = null;
 		s = myLocationContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// AreaContext
 		AreaContext myAreaContext = new AreaContext();
 
 		myAreaContext.setWidth(10);
 		assertEquals(10, myAreaContext.getWidth());
 
 		myAreaContext.setHeight(10);
 		assertEquals(10, myAreaContext.getHeight());
 
 		myAreaContext.setSize(20, 20);
 		assertEquals(20, myAreaContext.getWidth());
 		assertEquals(20, myAreaContext.getHeight());
 
 		s = myAreaContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test ResizeShapeContext
 		ResizeShapeContext myResizeShapeContext = new ResizeShapeContext(shapeMock);
 
 		shape = myResizeShapeContext.getShape();
 		assertNotNull(shape);
 		assertEquals(shapeMock, shape);
 
 		pictogramElement = myResizeShapeContext.getPictogramElement();
 		assertNotNull(pictogramElement);
 		assertEquals(shapeMock, pictogramElement);
 
 		s = null;
 		s = myResizeShapeContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test ResizeContext
 		ResizeContext myResizeContext = new ResizeContext();
 
 		myResizeContext.setHeight(10);
 		assertEquals(10, myResizeContext.getHeight());
 
 		myResizeContext.setWidth(15);
 		assertEquals(15, myResizeContext.getWidth());
 
 		myResizeContext.setSize(20, 20);
 		assertEquals(20, myResizeContext.getHeight());
 		assertEquals(20, myResizeContext.getWidth());
 
 		s = null;
 		s = myResizeContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test PrintContext
 		PrintContext myPrintContext = new PrintContext();
 		assertNotNull(myPrintContext);
 
 		// test MoveShapeContext
 		MoveShapeContext myMoveShapeContext = new MoveShapeContext(shapeMock);
 
 		shape = myMoveShapeContext.getShape();
 		assertNotNull(shape);
 		assertEquals(shapeMock, shape);
 
 		myMoveShapeContext.setDeltaX(10);
 		assertEquals(10, myMoveShapeContext.getDeltaX());
 
 		myMoveShapeContext.setDeltaY(15);
 		assertEquals(15, myMoveShapeContext.getDeltaY());
 
 		ContainerShape containerShapeMock = createNiceMock(ContainerShape.class);
 		replay(containerShapeMock);
 
 		myMoveShapeContext.setSourceContainer(containerShapeMock);
 		ContainerShape containerShape = myMoveShapeContext.getSourceContainer();
 		assertNotNull(containerShape);
 		assertEquals(containerShapeMock, containerShape);
 
 		myMoveShapeContext.setTargetContainer(containerShapeMock);
 		containerShape = myMoveShapeContext.getTargetContainer();
 		assertNotNull(containerShape);
 		assertEquals(containerShapeMock, containerShape);
 
 		myMoveShapeContext.setTargetConnection(connectionMock);
 		con = myMoveShapeContext.getTargetConnection();
 		assertNotNull(con);
 		assertEquals(connectionMock, con);
 
 		pictogramElement = myMoveShapeContext.getPictogramElement();
 		assertNotNull(pictogramElement);
 		assertEquals(shapeMock, pictogramElement);
 
 		s = null;
 		s = myMoveShapeContext.toString();
 		assertNotNull(s);
 		assertTrue(!("".equals(s)));
 
 		// test LayoutContext
 		LayoutContext myLayoutContext = new LayoutContext(peMock);
 		assertNotNull(myLayoutContext);
 
 		// test DirectEditingContext
 		GraphicsAlgorithm graphicsAlgorithmMock = createNiceMock(GraphicsAlgorithm.class);
 		replay(graphicsAlgorithmMock);
 
 		DirectEditingContext myDirectEditingContext = new DirectEditingContext(peMock, graphicsAlgorithmMock);
 		assertNotNull(myDirectEditingContext);
 
 		// test AddConnectionContext
 		AddConnectionContext myAddConnectionContext = new AddConnectionContext(anchorMock, anchorMock);
 
 		anchor = myAddConnectionContext.getSourceAnchor();
 		assertNotNull(anchor);
 		assertEquals(anchorMock, anchor);
 
 		anchor = myAddConnectionContext.getTargetAnchor();
 		assertNotNull(anchor);
 		assertEquals(anchorMock, anchor);
 
 		// test CustomContext
 		CustomContext myCustomContext = new CustomContext(pictogramElements1);
 
 		pictogramElements2 = myCustomContext.getPictogramElements();
 		assertNotNull(pictogramElements2);
 		assertArrayEquals(pictogramElements1, pictogramElements2);
 
 		myCustomContext.setInnerPictogramElement(peMock);
 		pictogramElement = myCustomContext.getInnerPictogramElement();
 		assertNotNull(pictogramElement);
 		assertEquals(peMock, pictogramElement);
 
 		myCustomContext.setInnerGraphicsAlgorithm(graphicsAlgorithmMock);
 		GraphicsAlgorithm graphicsAlgorithm = myCustomContext.getInnerGraphicsAlgorithm();
 		assertNotNull(graphicsAlgorithm);
 		assertEquals(graphicsAlgorithmMock, graphicsAlgorithm);
 
 		// test AddContext
 		IAreaContext areaContextMock = createNiceMock(IAreaContext.class);
 		replay(areaContextMock);
 
 		Object value = new Object();
 		AddContext myAddContext = new AddContext(areaContextMock, value);
 
 		myAddContext.setTargetConnection(connectionMock);
 		con = myAddContext.getTargetConnection();
 		assertNotNull(con);
 		assertEquals(connectionMock, con);
 
 		myAddContext.setTargetContainer(containerShapeMock);
 		containerShape = myAddContext.getTargetContainer();
 		assertNotNull(containerShape);
 		assertEquals(containerShapeMock, containerShape);
 
 		// test CreateContext
 		CreateContext myCreateContext = new CreateContext();
 
 		myCreateContext.setTargetConnection(connectionMock);
 		con = myCreateContext.getTargetConnection();
 		assertNotNull(con);
 		assertEquals(connectionMock, con);
 
 		myCreateContext.setTargetContainer(containerShapeMock);
 		containerShape = myCreateContext.getTargetContainer();
 		assertNotNull(containerShape);
 		assertEquals(containerShapeMock, containerShape);
 	}
 
 	@Test
 	public void testInternal() throws Exception {
 		// test constructor
 		ExternalPictogramLink myExternalPictogramLink = new ExternalPictogramLink();
 
 		try {
 			List<EObject> list = myExternalPictogramLink.getBusinessObjects();
 			assertNotNull(list);
 		} catch (UnsupportedOperationException e) {
 			// do nothing
 		}
 
 		org.eclipse.graphiti.mm.algorithms.styles.Point pointMock = createNiceMock(org.eclipse.graphiti.mm.algorithms.styles.Point.class);
 		replay(pointMock);
 
 		// test stub
 		EObject refObjectMock = createNiceMock(EObject.class);
 		replay(refObjectMock);
 		// myExternalPictogramLink.refSetValue(refObjectMock, new Object());
 		// myExternalPictogramLink.refInvokeOperation(new String(), new
 		// Vector<Object>());
 		// // assertNotNull(operation);
 		// myExternalPictogramLink.refInvokeOperation(refObjectMock, new
 		// Vector<Object>());
 		// // assertNotNull(operation2);
 		// myExternalPictogramLink.refGetValue(new String());
 		// // assertNotNull(value);
 		// myExternalPictogramLink.refGetValue(refObjectMock);
 		// // assertNotNull(value2);
 		// myExternalPictogramLink.refOutermostComposite();
 		// // assertNotNull(refOutermostComposite);
 		// boolean isInstanceOf =
 		// myExternalPictogramLink.erefIsInstanceOf(refObjectMock, true);
 		// isInstanceOf = !(isInstanceOf);
 		// boolean isInstanceOf2 =
 		// myExternalPictogramLink.refIsInstanceOf(refObjectMock, false);
 		// isInstanceOf2 = !(isInstanceOf2);
 		// myExternalPictogramLink.refImmediateComposite();
 		// // assertNotNull(refImmediateComposite)
 		// myExternalPictogramLink.refDelete();
 		// myExternalPictogramLink.refClass();
 		// // assertNotNull(refClass);
 		// myExternalPictogramLink.setWidth(10);
 		// myExternalPictogramLink.getWidth();
 		// myExternalPictogramLink.setHeight(10);
 		// myExternalPictogramLink.getHeight();
 		myExternalPictogramLink.getProperties();
 		// assertNotNull(properties);
 		// end of test stub
 
 		assertNull(myExternalPictogramLink.getPictogramElement());
 
 		PictogramElement pictogramElementMock = createNiceMock(PictogramElement.class);
 		replay(pictogramElementMock);
 		myExternalPictogramLink.setPictogramElement(pictogramElementMock);
 
 		PictogramElement pictogramElement = myExternalPictogramLink.getPictogramElement();
 		assertNotNull(pictogramElement);
 		assertTrue(pictogramElement.equals(pictogramElementMock));
 
 		// DiagramLink was removed
 		/*
 		 * DiagramLink diagramLinkMock = createNiceMock(DiagramLink.class);
 		 * myExternalPictogramLink.setDiagramLink(diagramLinkMock);
 		 * 
 		 * DiagramLink diagramLink = myExternalPictogramLink.getDiagramLink();
 		 * assertNotNull(diagramLink);
 		 * assertTrue(diagramLink.equals(diagramLinkMock));
 		 */
 
 		// ####################################################
 
 		Style styleContainerMock = createNiceMock(Style.class);
 		expect(styleContainerMock.getAngle()).andReturn(null);
 		expect(styleContainerMock.getStyleContainer()).andReturn(null);
 		replay(styleContainerMock);
 
 		Style styleMock = createNiceMock(Style.class);
 		expect(styleMock.getAngle()).andReturn(new Integer(-1));
 		expect(styleMock.getAngle()).andReturn(new Integer(-1));
 
 		expect(styleMock.getAngle()).andReturn(null);
 
 		expect(styleMock.getStyleContainer()).andReturn(styleContainerMock);
 		replay(styleMock);
 
 		AbstractText abstractTextMock = createNiceMock(AbstractText.class);
 		// run 1
 		expect(abstractTextMock.getAngle()).andReturn(null);
 		expect(abstractTextMock.getStyle()).andReturn(styleMock);
 		// run 2
 		expect(abstractTextMock.getAngle()).andReturn(null);
 		expect(abstractTextMock.getStyle()).andReturn(styleMock);
 		// run 3
 		expect(abstractTextMock.getAngle()).andReturn(null);
 		// run 4
 		expect(abstractTextMock.getAngle()).andReturn(new Integer(0));
 		expect(abstractTextMock.getAngle()).andReturn(new Integer(0));
 		replay(abstractTextMock);
 
 		IGaService gaService = Graphiti.getGaService();
 
 		gaService.getAngle(abstractTextMock, true);
 		gaService.getAngle(abstractTextMock, true);
 		gaService.getAngle(abstractTextMock, false);
 		gaService.getAngle(abstractTextMock, false);
 
 		// verify(styleMock);
 
 		// test public static Color getBackgroundColor(GraphicsAlgorithm ga,
 		// boolean checkStyles)
 		// and private static Color getBackgroundColor(Style style)
 		Color colorMock = createMock(Color.class);
 		replay(colorMock);
 
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getBackground()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getBackground()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getBackground()).andReturn(colorMock).times(2);
 		replay(styleMock);
 
 		GraphicsAlgorithm graphicsAlgorithmMock = createMock(GraphicsAlgorithm.class);
 		// run 1
 		expect(graphicsAlgorithmMock.getBackground()).andReturn(colorMock);
 		expect(graphicsAlgorithmMock.getBackground()).andReturn(colorMock);
 		// run 2
 		expect(graphicsAlgorithmMock.getBackground()).andReturn(null);
 		// run 3
 		expect(graphicsAlgorithmMock.getBackground()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(graphicsAlgorithmMock.getBackground()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		replay(graphicsAlgorithmMock);
 
 		gaService.getBackgroundColor(graphicsAlgorithmMock, false);
 		gaService.getBackgroundColor(graphicsAlgorithmMock, false);
 		gaService.getBackgroundColor(graphicsAlgorithmMock, true);
 		gaService.getBackgroundColor(graphicsAlgorithmMock, true);
 
 		// verify(styleMock);
 
 		// test public static Font getFont(AbstractText at, boolean checkStyles)
 		// and private static Font getFont(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getFont()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getFont()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getFont()).andReturn(createMock(Font.class));
 		expect(styleMock.getFont()).andReturn(createMock(Font.class));
 		replay(styleMock);
 
 		abstractTextMock = createNiceMock(AbstractText.class);
 		// run 1
 		expect(abstractTextMock.getFont()).andReturn(null);
 		// run 2
 		expect(abstractTextMock.getFont()).andReturn(createMock(Font.class));
 		expect(abstractTextMock.getFont()).andReturn(null);
 		// run 3
 		expect(abstractTextMock.getFont()).andReturn(null);
 		expect(abstractTextMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(abstractTextMock.getFont()).andReturn(null);
 		expect(abstractTextMock.getStyle()).andReturn(styleMock);
 		replay(abstractTextMock);
 
 		gaService.getFont(abstractTextMock, false);
 		gaService.getFont(abstractTextMock, true);
 		gaService.getFont(abstractTextMock, true);
 		gaService.getFont(abstractTextMock, true);
 
 		// verify(styleMock);
 
 		// test public static Color getForegroundColor(GraphicsAlgorithm ga,
 		// boolean checkStyles)
 		// and private static Color getForegroundColor(Style style)
 		colorMock = createMock(Color.class);
 		replay(colorMock);
 
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getForeground()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getForeground()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getForeground()).andReturn(colorMock).times(2);
 		replay(styleMock);
 
 		graphicsAlgorithmMock = createMock(GraphicsAlgorithm.class);
 		// run 1
 		expect(graphicsAlgorithmMock.getForeground()).andReturn(colorMock).times(2);
 		// run 2
 		expect(graphicsAlgorithmMock.getForeground()).andReturn(null);
 		// run 3
 		expect(graphicsAlgorithmMock.getForeground()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(graphicsAlgorithmMock.getForeground()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		replay(graphicsAlgorithmMock);
 
 		gaService.getForegroundColor(graphicsAlgorithmMock, false);
 		gaService.getForegroundColor(graphicsAlgorithmMock, false);
 		gaService.getForegroundColor(graphicsAlgorithmMock, true);
 		gaService.getForegroundColor(graphicsAlgorithmMock, true);
 
 		// verify(styleMock);
 
 		// test public static Orientation getHorizontalAlignment(AbstractText
 		// at, boolean checkStyles)
 		// and private static Orientation getHorizontalAlignment(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getHorizontalAlignment()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getHorizontalAlignment()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		// expect(styleMock.getHorizontalAlignment()).andReturn(createNiceMock(Orientation.class)).times(2);
 		replay(styleMock);
 
 		abstractTextMock = createMock(AbstractText.class);
 		// run 1
 		expect(abstractTextMock.getHorizontalAlignment()).andReturn(null);
 		// run 2
 		// expect(abstractTextMock.getHorizontalAlignment()).andReturn(createMock(Orientation.class));
 		expect(abstractTextMock.getHorizontalAlignment()).andReturn(null);
 		// run 3
 		expect(abstractTextMock.getHorizontalAlignment()).andReturn(null);
 		expect(abstractTextMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(abstractTextMock.getHorizontalAlignment()).andReturn(null);
 		expect(abstractTextMock.getStyle()).andReturn(styleMock);
 		replay(abstractTextMock);
 
 		gaService.getHorizontalAlignment(abstractTextMock, false);
 		gaService.getHorizontalAlignment(abstractTextMock, false);
 		gaService.getHorizontalAlignment(abstractTextMock, true);
 		gaService.getHorizontalAlignment(abstractTextMock, true);
 
 		// verify(styleMock);
 
 		// test public static Orientation getVerticalAlignment(AbstractText at,
 		// boolean checkStyles)
 		// and private static Orientation getVerticalAlignment(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getVerticalAlignment()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getVerticalAlignment()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		// expect(styleMock.getVerticalAlignment()).andReturn(createNiceMock(Orientation.class)).times(2);
 		replay(styleMock);
 
 		abstractTextMock = createMock(AbstractText.class);
 		// run 1
 		expect(abstractTextMock.getVerticalAlignment()).andReturn(null);
 		// run 2
 		// expect(abstractTextMock.getVerticalAlignment()).andReturn(createMock(Orientation.class));
 		expect(abstractTextMock.getVerticalAlignment()).andReturn(null);
 		// run 3
 		expect(abstractTextMock.getVerticalAlignment()).andReturn(null);
 		expect(abstractTextMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(abstractTextMock.getVerticalAlignment()).andReturn(null);
 		expect(abstractTextMock.getStyle()).andReturn(styleMock);
 		replay(abstractTextMock);
 
 		gaService.getVerticalAlignment(abstractTextMock, false);
 		gaService.getVerticalAlignment(abstractTextMock, false);
 		gaService.getVerticalAlignment(abstractTextMock, true);
 		gaService.getVerticalAlignment(abstractTextMock, true);
 
 		// verify(styleMock);
 
 		// test public static LineStyle getLineStyle(GraphicsAlgorithm ga,
 		// boolean checkStyles)
 		// and private static LineStyle getLineStyle(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getLineStyle()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getLineStyle()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		// expect(styleMock.getLineStyle()).andReturn(createNiceMock(LineStyle.class)).times(2);
 		replay(styleMock);
 
 		graphicsAlgorithmMock = createMock(GraphicsAlgorithm.class);
 		// run 1
 		// expect(graphicsAlgorithmMock.getLineStyle()).andReturn(createMock(LineStyle.class)).times(2);
 		// run 2
 		expect(graphicsAlgorithmMock.getLineStyle()).andReturn(null);
 		// run 3
 		expect(graphicsAlgorithmMock.getLineStyle()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(graphicsAlgorithmMock.getLineStyle()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		replay(graphicsAlgorithmMock);
 
 		gaService.getLineStyle(graphicsAlgorithmMock, false);
 		gaService.getLineStyle(graphicsAlgorithmMock, false);
 		// gaService.getLineStyle(graphicsAlgorithmMock, true);
 		// gaService.getLineStyle(graphicsAlgorithmMock, true);
 
 		// verify(styleMock);
 
 		// test public static int getLineWidth(GraphicsAlgorithm ga, boolean
 		// checkStyles) {
 		// and private static int getLineWidth(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getLineWidth()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getLineWidth()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getLineWidth()).andReturn(new Integer(5)).times(2);
 		replay(styleMock);
 
 		graphicsAlgorithmMock = createMock(GraphicsAlgorithm.class);
 		// run 1
 		expect(graphicsAlgorithmMock.getLineWidth()).andReturn(new Integer(6)).times(2);
 		// run 2
 		expect(graphicsAlgorithmMock.getLineWidth()).andReturn(null);
 		// run 3
 		expect(graphicsAlgorithmMock.getLineWidth()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(graphicsAlgorithmMock.getLineWidth()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		replay(graphicsAlgorithmMock);
 
 		gaService.getLineWidth(graphicsAlgorithmMock, false);
 		gaService.getLineWidth(graphicsAlgorithmMock, false);
 		gaService.getLineWidth(graphicsAlgorithmMock, true);
 		gaService.getLineWidth(graphicsAlgorithmMock, true);
 
 		// verify(styleMock);
 
 		// test public static RenderingStyle getRenderingStyle(GraphicsAlgorithm
 		// ga, boolean checkStyles)
 		// and private static RenderingStyle getRenderingStyle(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getRenderingStyle()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getRenderingStyle()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getRenderingStyle()).andReturn(createMock(RenderingStyle.class)).times(2);
 		replay(styleMock);
 
 		graphicsAlgorithmMock = createMock(GraphicsAlgorithm.class);
 		// run 1
 		expect(graphicsAlgorithmMock.getRenderingStyle()).andReturn(createMock(RenderingStyle.class)).times(2);
 		// run 2
 		expect(graphicsAlgorithmMock.getRenderingStyle()).andReturn(null);
 		// run 3
 		expect(graphicsAlgorithmMock.getRenderingStyle()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(graphicsAlgorithmMock.getRenderingStyle()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		replay(graphicsAlgorithmMock);
 
 		gaService.getRenderingStyle(graphicsAlgorithmMock, false);
 		gaService.getRenderingStyle(graphicsAlgorithmMock, false);
 		gaService.getRenderingStyle(graphicsAlgorithmMock, true);
 		gaService.getRenderingStyle(graphicsAlgorithmMock, true);
 
 		// verify(styleMock);
 
 		// test public static double getTransparency(GraphicsAlgorithm ga,
 		// boolean checkStyles)
 		// and private static double getTransparency(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getTransparency()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getTransparency()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getTransparency()).andReturn(new Double(1.0)).times(2);
 		replay(styleMock);
 
 		graphicsAlgorithmMock = createMock(GraphicsAlgorithm.class);
 		// run 1
 		expect(graphicsAlgorithmMock.getTransparency()).andReturn(new Double(0.0)).times(2);
 		// run 2
 		expect(graphicsAlgorithmMock.getTransparency()).andReturn(null);
 		// run 3
 		expect(graphicsAlgorithmMock.getTransparency()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(graphicsAlgorithmMock.getTransparency()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		replay(graphicsAlgorithmMock);
 
 		gaService.getTransparency(graphicsAlgorithmMock, false);
 		gaService.getTransparency(graphicsAlgorithmMock, false);
 		gaService.getTransparency(graphicsAlgorithmMock, true);
 		gaService.getTransparency(graphicsAlgorithmMock, true);
 
 		// verify(styleMock);
 
 		// test public static boolean isFilled(GraphicsAlgorithm ga, boolean
 		// checkStyles)
 		// and private static boolean isFilled(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getFilled()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getFilled()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getFilled()).andReturn(new Boolean(true)).times(2);
 		replay(styleMock);
 
 		graphicsAlgorithmMock = createNiceMock(GraphicsAlgorithm.class);
 		// run 1
 		expect(graphicsAlgorithmMock.getFilled()).andReturn(new Boolean(false)).times(2);
 		// run 2
 		expect(graphicsAlgorithmMock.getFilled()).andReturn(null);
 		// run 3
 		expect(graphicsAlgorithmMock.getFilled()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(graphicsAlgorithmMock.getFilled()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		replay(graphicsAlgorithmMock);
 
 		gaService.isFilled(graphicsAlgorithmMock, false);
 		gaService.isFilled(graphicsAlgorithmMock, false);
 		gaService.isFilled(graphicsAlgorithmMock, true);
 		gaService.isFilled(graphicsAlgorithmMock, true);
 
 		// verify(styleMock);
 
 		// test public static boolean isLineVisible(GraphicsAlgorithm ga,
 		// boolean checkStyles)
 		// and private static boolean isLineVisible(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getLineVisible()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getLineVisible()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getLineVisible()).andReturn(new Boolean(true)).times(2);
 		replay(styleMock);
 
 		graphicsAlgorithmMock = createMock(GraphicsAlgorithm.class);
 		// run 1
 		expect(graphicsAlgorithmMock.getLineVisible()).andReturn(new Boolean(false)).times(2);
 		// run 2
 		expect(graphicsAlgorithmMock.getLineVisible()).andReturn(null);
 		// run 3
 		expect(graphicsAlgorithmMock.getLineVisible()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(graphicsAlgorithmMock.getLineVisible()).andReturn(null);
 		expect(graphicsAlgorithmMock.getStyle()).andReturn(styleMock);
 		replay(graphicsAlgorithmMock);
 
 		gaService.isLineVisible(graphicsAlgorithmMock, false);
 		gaService.isLineVisible(graphicsAlgorithmMock, false);
 		gaService.isLineVisible(graphicsAlgorithmMock, true);
 		gaService.isLineVisible(graphicsAlgorithmMock, true);
 
 		// verify(styleMock);
 
 		// test public static boolean isProportional(Image image, boolean
 		// checkStyles)
 		// and private static boolean isProportional(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getProportional()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getProportional()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getProportional()).andReturn(new Boolean(true)).times(2);
 		replay(styleMock);
 
 		Image imageMock = createMock(Image.class);
 		// run 1
 		expect(imageMock.getProportional()).andReturn(new Boolean(false)).times(2);
 		// run 2
 		expect(imageMock.getProportional()).andReturn(null);
 		// run 3
 		expect(imageMock.getProportional()).andReturn(null);
 		expect(imageMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(imageMock.getProportional()).andReturn(null);
 		expect(imageMock.getStyle()).andReturn(styleMock);
 		replay(imageMock);
 
 		gaService.isProportional(imageMock, false);
 		gaService.isProportional(imageMock, false);
 		gaService.isProportional(imageMock, true);
 		gaService.isProportional(imageMock, true);
 
 		// verify(styleMock);
 
 		// test public static boolean isStretchH(Image image, boolean
 		// checkStyles)
 		// and private static boolean isStretchH(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getStretchH()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getStretchH()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getStretchH()).andReturn(new Boolean(true)).times(2);
 		replay(styleMock);
 
 		imageMock = createMock(Image.class);
 		// run 1
 		expect(imageMock.getStretchH()).andReturn(new Boolean(false)).times(2);
 		// run 2
 		expect(imageMock.getStretchH()).andReturn(null);
 		// run 3
 		expect(imageMock.getStretchH()).andReturn(null);
 		expect(imageMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(imageMock.getStretchH()).andReturn(null);
 		expect(imageMock.getStyle()).andReturn(styleMock);
 		replay(imageMock);
 
 		gaService.isStretchH(imageMock, false);
 		gaService.isStretchH(imageMock, false);
 		gaService.isStretchH(imageMock, true);
 		gaService.isStretchH(imageMock, true);
 
 		// verify(styleMock);
 
 		// test public static boolean isStretchV(Image image, boolean
 		// checkStyles)
 		// and private static boolean isStretchV(Style style)
 		styleMock = createNiceMock(Style.class);
 		expect(styleMock.getStretchV()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(styleMock);
 		expect(styleMock.getStretchV()).andReturn(null);
 		expect(styleMock.getStyleContainer()).andReturn(null);
 		expect(styleMock.getStretchV()).andReturn(new Boolean(true)).times(2);
 		replay(styleMock);
 
 		imageMock = createMock(Image.class);
 		// run 1
 		expect(imageMock.getStretchV()).andReturn(new Boolean(false)).times(2);
 		// run 2
 		expect(imageMock.getStretchV()).andReturn(null);
 		// run 3
 		expect(imageMock.getStretchV()).andReturn(null);
 		expect(imageMock.getStyle()).andReturn(styleMock);
 		// run 4
 		expect(imageMock.getStretchV()).andReturn(null);
 		expect(imageMock.getStyle()).andReturn(styleMock);
 		replay(imageMock);
 
 		gaService.isStretchV(imageMock, false);
 		gaService.isStretchV(imageMock, false);
 		gaService.isStretchV(imageMock, true);
 		gaService.isStretchV(imageMock, true);
 
 		// verify(styleMock);
 	}
 
 	@Test
 	public void testPaletteImpl() throws Exception {
 		StackEntry stackEntry = new StackEntry("label", "description", null);
 		stackEntry.addCreationToolEntry(null);
 		assertNotNull(stackEntry.getCreationToolEntries());
 		assertEquals("description", stackEntry.getDescription());
 	}
 
 	@Test
 	public void testPlatformGa() throws Exception {
 
 		PlatformGraphicsAlgorithm platformGraphicsAlgorithmMock = createNiceMock(PlatformGraphicsAlgorithm.class);
 		replay(platformGraphicsAlgorithmMock);
 
 		IDiagramTypeProvider diagramTypeProviderMock = createNiceMock(IDiagramTypeProvider.class);
 		IFeatureProvider featureProviderMock = createNiceMock(IFeatureProvider.class);
 		replay(featureProviderMock);
 		expect(diagramTypeProviderMock.getFeatureProvider()).andReturn(featureProviderMock);
 		replay(diagramTypeProviderMock);
 
 		// test constructor
 		RendererContext myRendererContext = new RendererContext(platformGraphicsAlgorithmMock, diagramTypeProviderMock);
 
 		// test getDiagramTypeProvider()
 		IDiagramTypeProvider diagramTypeProvider = myRendererContext.getDiagramTypeProvider();
 		assertNotNull(diagramTypeProvider);
 		assertTrue(diagramTypeProvider.equals(diagramTypeProviderMock));
 
 		// test getGraphicsAlgorithm()
 		GraphicsAlgorithm graphicsAlgorithm = myRendererContext.getGraphicsAlgorithm();
 		assertNotNull(graphicsAlgorithm);
 
 		// test getMappingProvider()
 		IMappingProvider mappingProvider = myRendererContext.getMappingProvider();
 		assertNotNull(mappingProvider);
 
 		// test getPlatformGraphicsAlgorithm()
 		PlatformGraphicsAlgorithm platformGraphicsAlgorithm = myRendererContext.getPlatformGraphicsAlgorithm();
 		assertNotNull(platformGraphicsAlgorithm);
 		assertTrue(platformGraphicsAlgorithm.equals(platformGraphicsAlgorithmMock));
 	}
 
 	@Test
 	public void testDiagramEditorFactory() {
 		TransactionalEditingDomain ted = DiagramEditorFactory.createResourceSetAndEditingDomain();
 		assertNotNull(ted);
 		ResourceSet rSet = ted.getResourceSet();
 		TransactionalEditingDomain ted2 = TransactionUtil.getEditingDomain(rSet);
 		assertEquals(ted, ted2);
 
 	}
 
 }
