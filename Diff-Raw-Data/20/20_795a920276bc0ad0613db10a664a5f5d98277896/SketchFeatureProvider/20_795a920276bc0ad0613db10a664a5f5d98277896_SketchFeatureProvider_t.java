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
 package org.eclipse.graphiti.testtool.sketch;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IAddFeature;
 import org.eclipse.graphiti.features.ICopyFeature;
 import org.eclipse.graphiti.features.ICreateConnectionFeature;
 import org.eclipse.graphiti.features.ICreateFeature;
 import org.eclipse.graphiti.features.IDeleteFeature;
 import org.eclipse.graphiti.features.IDirectEditingFeature;
 import org.eclipse.graphiti.features.ILayoutFeature;
 import org.eclipse.graphiti.features.IMoveShapeFeature;
 import org.eclipse.graphiti.features.IPasteFeature;
 import org.eclipse.graphiti.features.IResizeShapeFeature;
 import org.eclipse.graphiti.features.context.IAddContext;
 import org.eclipse.graphiti.features.context.ICopyContext;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDeleteContext;
 import org.eclipse.graphiti.features.context.IDirectEditingContext;
 import org.eclipse.graphiti.features.context.ILayoutContext;
 import org.eclipse.graphiti.features.context.IMoveShapeContext;
 import org.eclipse.graphiti.features.context.IPasteContext;
 import org.eclipse.graphiti.features.context.IResizeShapeContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.mm.algorithms.AbstractText;
 import org.eclipse.graphiti.mm.algorithms.Ellipse;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.MultiText;
 import org.eclipse.graphiti.mm.algorithms.Polygon;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.algorithms.styles.Style;
 import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.testtool.sketch.features.AddAnythingFeature;
 import org.eclipse.graphiti.testtool.sketch.features.AddDiagramFeature;
 import org.eclipse.graphiti.testtool.sketch.features.AddLinkFeature;
 import org.eclipse.graphiti.testtool.sketch.features.BackgroundColorFeature;
 import org.eclipse.graphiti.testtool.sketch.features.ChangeAlignmentFeature;
 import org.eclipse.graphiti.testtool.sketch.features.CornerDimensionFeature;
 import org.eclipse.graphiti.testtool.sketch.features.FontColorFeature;
 import org.eclipse.graphiti.testtool.sketch.features.ForegroundColorFeature;
 import org.eclipse.graphiti.testtool.sketch.features.GradientColorFeature;
 import org.eclipse.graphiti.testtool.sketch.features.LayoutContainerShapeGhostAndInnerShapeFeature;
 import org.eclipse.graphiti.testtool.sketch.features.LineStyleFeature;
 import org.eclipse.graphiti.testtool.sketch.features.LineWidthFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SendToBackFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SendToFrontFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SketchDeleteFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SketchFontFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SketchGhostLayoutFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SketchLabelDirectEditingFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SketchLayoutFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SketchMoveShapeFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SketchResizeShapeFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SketchTextDirectEditingFeature;
 import org.eclipse.graphiti.testtool.sketch.features.SwitchModeFeature;
 import org.eclipse.graphiti.testtool.sketch.features.ToggleDecorator;
 import org.eclipse.graphiti.testtool.sketch.features.TransparencyFeature;
 import org.eclipse.graphiti.testtool.sketch.features.bd.CreateChannelFeature;
 import org.eclipse.graphiti.testtool.sketch.features.create.CreateCsGhostAndInnerShapeFeature;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateCanFigureFeature;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateFreeformConnectionFeature;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateGaContainerFeature;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateGaContainerFeatureWithGhost;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateGaShapeFeature;
 import org.eclipse.graphiti.testtool.sketch.features.create.SketchCreateGaShapeFeatureWithGhost;
 import org.eclipse.graphiti.testtool.sketch.features.pool.CreateLaneFeauture;
 import org.eclipse.graphiti.testtool.sketch.features.pool.CreatePoolFeature;
 import org.eclipse.graphiti.testtool.sketch.features.pool.LayoutPoolFeature;
 import org.eclipse.graphiti.testtool.sketch.features.style.CreateDefaultStylesFeature;
 import org.eclipse.graphiti.testtool.sketch.features.style.IgnoreAllFeature;
 import org.eclipse.graphiti.testtool.sketch.features.style.MakeStyleFeature;
 import org.eclipse.graphiti.testtool.sketch.features.style.SetStyleFeature;
 import org.eclipse.graphiti.testtool.sketch.features.style.UnsetStyleFeature;
 import org.eclipse.graphiti.ui.features.DefaultFeatureProvider;
 
 /**
  * The Class SketchFeatureProvider.
  */
 public class SketchFeatureProvider extends DefaultFeatureProvider {
 
 	public static final String CF_RECTANGLE_MULTI_TEXT = "Rectangle";
 
 	public static final String CF_RECTANGLE_SINGLE_TEXT = "Rectangle Single Text";
 
 	private static final String NO_MOVE = "no move";
 
 	private static final boolean TEST_RESIZE_SHAPE_FEATURE = false;
 
 	private final int TRANSPARENCY_STEP_SIZE = 5;
 
 	private final int LINE_WIDTH_STEP_SIZE = 1;
 
 	private boolean testMode = false; // if true, then this feature provider is used in the test suite 
 
 	private final Dimension CORNER_DIMENSION[] = new Dimension[] { new Dimension(0, 0), new Dimension(4, 4), new Dimension(8, 8),
 			new Dimension(20, 20), new Dimension(21, 21), new Dimension(100, 100), new Dimension(101, 101), new Dimension(200, 200),
 			new Dimension(100, 20), new Dimension(20, 100), };
 
 	/**
 	 * Instantiates a new sketch feature provider.
 	 * 
 	 * @param dtp
 	 *            the dtp
 	 */
 	public SketchFeatureProvider(IDiagramTypeProvider dtp) {
 		super(dtp);
 	}
 
 	@Override
 	public ICreateFeature[] getCreateFeatures() {
 
 		List<ICreateFeature> features = new ArrayList<ICreateFeature>();
 
 		features.add(new CreateCsGhostAndInnerShapeFeature(this, "CS + Ghost + inner Shape", "create CS with Ghost and inner Shape"));
 
 		features.add(createCreateGaContainerFeature("Rectangle Container", "draw rectangle", Rectangle.class));
 		features.add(createCreateGaContainerFeature("Rounded Rectangle Container", "draw rounded rectangle", RoundedRectangle.class));
 		features.add(createCreateGaContainerFeature("Ellipse Container", "draw ellipse", Ellipse.class));
 		features.add(createCreateGaContainerFeature("Triangle Container", "draw triangle", Polygon.class));
 
 		features.add(createCreateGaShapeFeature(CF_RECTANGLE_MULTI_TEXT, "draw rectangle", Rectangle.class));
 		features.add(createCreateGaShapeFeatureSingleText(CF_RECTANGLE_SINGLE_TEXT, "draw rectangle with a single line text",
 				Rectangle.class));
 		features.add(createCreateGaShapeFeature("Rounded Rectangle", "draw rounded rectangle", RoundedRectangle.class));
 		features.add(createCreateGaShapeFeature("Ellipse", "draw ellipse", Ellipse.class));
 		features.add(createCreateGaShapeFeature("Triangle", "draw triangle", Polygon.class));
 		features.add(createCreateGaShapeFeature("Arrow", "draw Arrow outline", Polygon.class));
 		features.add(createCreateGaShapeFeature("Line", "draw line", Polyline.class));
 
 		features.add(createCreateGaShapeFeatureWithGhost("Rectangle Ghost", "rectangle with ghost", Rectangle.class));
 		features
 				.add(createCreateGaShapeFeatureWithGhost("Rounded Rectangle Ghost", "rounded rectangle with ghost", RoundedRectangle.class));
 		features.add(createCreateGaShapeFeatureWithGhost("Ellipse Ghost", "ellipse with ghost", Ellipse.class));
 
 		features
 				.add(createCreateGaContainerFeatureWithGhost("Rectangle Container Ghost", "rectangle container with ghost", Rectangle.class));
 		features.add(createCreateGaContainerFeatureWithGhost("Rounded Container Rectangle Ghost", "rounded rectangle container with ghost",
 				RoundedRectangle.class));
 		features.add(createCreateGaContainerFeatureWithGhost("Ellipse Container Ghost", "ellipse container with ghost", Ellipse.class));
 
 		features.add(new CreatePoolFeature(this, "Pool", "create a pool"));
 
 		features.add(new SketchCreateCanFigureFeature(this, "Can figure", "example for a platform ga"));
 
 		return features.toArray(new ICreateFeature[0]);
 	}
 
 	@Override
 	public ICustomFeature[] getCustomFeatures(ICustomContext context) {
 
 		List<ICustomFeature> retList = new ArrayList<ICustomFeature>();
 
 		for (int i = 0; i <= 10; i += LINE_WIDTH_STEP_SIZE) {
 			retList.add(new LineWidthFeature(this, i));
 		}
 
 		retList.add(new LineStyleFeature(this, LineStyle.SOLID));
 		retList.add(new LineStyleFeature(this, LineStyle.DASH));
 		retList.add(new LineStyleFeature(this, LineStyle.DASHDOT));
 		retList.add(new LineStyleFeature(this, LineStyle.DASHDOTDOT));
 		retList.add(new LineStyleFeature(this, LineStyle.DOT));
 
 		for (int i = 0; i < CORNER_DIMENSION.length; i++) {
 			retList.add(new CornerDimensionFeature(this, CORNER_DIMENSION[i]));
 		}
 
 		retList.add(new SendToFrontFeature(this));
 		retList.add(new SendToBackFeature(this));
 
 		retList.add(new ForegroundColorFeature(this));
 		retList.add(new BackgroundColorFeature(this));
 		retList.add(new GradientColorFeature(this));
 		retList.add(new FontColorFeature(this));
 
 		retList.add(new SketchFontFeature(this));
 
 		retList.add(new CreateLaneFeauture(this));
 
 		retList.add(new ChangeAlignmentFeature(this, "Left", Orientation.ALIGNMENT_LEFT, null));
 		retList.add(new ChangeAlignmentFeature(this, "Center", Orientation.ALIGNMENT_CENTER, null));
 		retList.add(new ChangeAlignmentFeature(this, "Right", Orientation.ALIGNMENT_RIGHT, null));
 
 		retList.add(new ChangeAlignmentFeature(this, "Top", null, Orientation.ALIGNMENT_TOP));
 		retList.add(new ChangeAlignmentFeature(this, "Middle", null, Orientation.ALIGNMENT_CENTER));
 		retList.add(new ChangeAlignmentFeature(this, "Bottom", null, Orientation.ALIGNMENT_BOTTOM));
 
 		retList.add(new ChangeAlignmentFeature(this, "Top Left", Orientation.ALIGNMENT_LEFT, Orientation.ALIGNMENT_TOP));
 		retList.add(new ChangeAlignmentFeature(this, "Top Right", Orientation.ALIGNMENT_RIGHT, Orientation.ALIGNMENT_TOP));
 		retList.add(new ChangeAlignmentFeature(this, "Middle Center", Orientation.ALIGNMENT_CENTER, Orientation.ALIGNMENT_CENTER));
 		retList.add(new ChangeAlignmentFeature(this, "Bottom Left", Orientation.ALIGNMENT_LEFT, Orientation.ALIGNMENT_BOTTOM));
 		retList.add(new ChangeAlignmentFeature(this, "Bottom Right", Orientation.ALIGNMENT_RIGHT, Orientation.ALIGNMENT_BOTTOM));
 
 		for (int i = 0; i < 100; i += TRANSPARENCY_STEP_SIZE) {
 			retList.add(new TransparencyFeature(this, i));
 		}
 		retList.add(new TransparencyFeature(this, 100));
 
 		retList.add(new ToggleDecorator(this));
 
 		retList.add(new CreateDefaultStylesFeature(this));
 
 		retList.add(new MakeStyleFeature(this));
 
 		Collection<Style> styles = getDiagramTypeProvider().getDiagram().getStyles();
 		for (Style style : styles) {
 			createSetStyleFeatures(style, retList);
 		}
 
 		retList.add(new UnsetStyleFeature(this));
 
 		retList.add(new IgnoreAllFeature(this));
 
 		retList.add(new SwitchModeFeature(this));
 
 		return retList.toArray(new ICustomFeature[0]);
 	}
 
 	private void createSetStyleFeatures(Style parentStyle, List<ICustomFeature> retList) {
 		retList.add(new SetStyleFeature(parentStyle, this));
 		Collection<Style> styles = parentStyle.getStyles();
 		for (Style style : styles) {
 			createSetStyleFeatures(style, retList);
 		}
 	}
 
 	@Override
 	public ICreateConnectionFeature[] getCreateConnectionFeatures() {
 		List<ICreateConnectionFeature> retList = new ArrayList<ICreateConnectionFeature>();
 
 		retList.add(new SketchCreateFreeformConnectionFeature(this, "free", "freeform connection"));
 		retList.add(new CreateChannelFeature(this, "channel", "channel connection"));
 
 		return retList.toArray(new ICreateConnectionFeature[0]);
 	}
 
 	private ICreateFeature createCreateGaContainerFeature(String name, String description, Class<? extends GraphicsAlgorithm> gaType) {
 		ICreateFeature createFeature = new SketchCreateGaContainerFeature(this, name, description, gaType);
 		return createFeature;
 	}
 
 	private ICreateFeature createCreateGaShapeFeature(String name, String description, Class<? extends GraphicsAlgorithm> gaType) {
 		ICreateFeature createFeature = new SketchCreateGaShapeFeature(this, name, description, gaType);
 		return createFeature;
 	}
 
 	private ICreateFeature createCreateGaShapeFeatureSingleText(String name, String description, Class<? extends GraphicsAlgorithm> gaType) {
 		ICreateFeature createFeature = new SketchCreateGaShapeFeature(this, name, description, gaType, false);
 		return createFeature;
 	}
 
 	private ICreateFeature createCreateGaShapeFeatureWithGhost(String name, String description, Class<? extends GraphicsAlgorithm> gaType) {
 		// breaks the build: ICreateFeature createFeature = new
 		// SketchCreateGaShapeFeatureWithGhost(this, name, description,
 		// gaType);
 		ICreateFeature createFeature = new SketchCreateGaShapeFeatureWithGhost(this, name, description, gaType);
 		return createFeature;
 	}
 
 	private ICreateFeature createCreateGaContainerFeatureWithGhost(String name, String description, Class<? extends GraphicsAlgorithm> gaType) {
 		// breaks the build: ICreateFeature createFeature = new
 		// SketchCreateGaShapeFeatureWithGhost(this, name, description,
 		// gaType);
 		ICreateFeature createFeature = new SketchCreateGaContainerFeatureWithGhost(this, name, description, gaType);
 		return createFeature;
 	}
 
 	@Override
 	public IMoveShapeFeature getMoveShapeFeature(IMoveShapeContext context) {
 		Shape shape = context.getShape();
 		if (shape != null) {
 			GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
 			if (checkNoMove(ga)) {
 				return null;
 			}
 		}
 		return new SketchMoveShapeFeature(this);
 	}
 
 	private boolean checkNoMove(GraphicsAlgorithm ga) {
 		if (ga instanceof AbstractText) {
 			AbstractText at = (AbstractText) ga;
 			if (NO_MOVE.equalsIgnoreCase(at.getValue())) {
 				return true;
 			}
 		} else {
 			List<GraphicsAlgorithm> gaChildren = ga.getGraphicsAlgorithmChildren();
 			for (GraphicsAlgorithm child : gaChildren) {
 				if (checkNoMove(child)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public IAddFeature getAddFeature(IAddContext context) {
 		IAddFeature ret = super.getAddFeature(context);
 
 		Object newObject = context.getNewObject();
 		if (newObject instanceof Diagram) {
 			ret = new AddDiagramFeature(this);
 		} else if (!(context.getTargetContainer() instanceof Diagram) && !(newObject instanceof PictogramElement)) {
 			// works only container shapes, because drop is only allowed on container shapes
 			ret = new AddLinkFeature(this);
 		} else {
 			return new AddAnythingFeature(this);
 		}
 
 		return ret;
 	}
 
 	@Override
 	public ILayoutFeature getLayoutFeature(ILayoutContext context) {
 		PictogramElement pe = context.getPictogramElement();
 		if (SketchUtil.getLabelGa(pe) != null) {
 			return new SketchLayoutFeature(this);
 		} else if (SketchUtil.getGhostGa(pe) != null) {
 			return new SketchGhostLayoutFeature(this);
 		} else if (SketchUtil.isPoolPe(pe)) {
 			return new LayoutPoolFeature(this);
 		} else {
 			boolean check = context.getPictogramElement() instanceof ContainerShape;
			if (check) {
				ContainerShape cs = (ContainerShape) pe;
				List<Shape> children = cs.getChildren();
				if (!children.isEmpty()) {
					Shape firstChild = children.get(0);
					if (firstChild != null) {
						GraphicsAlgorithm ga = firstChild.getGraphicsAlgorithm();
						check = check && ga instanceof Ellipse;
						if (check) {
							return new LayoutContainerShapeGhostAndInnerShapeFeature(this);
						}
 					}
 				}
 			}
 		}
 		return super.getLayoutFeature(context);
 	}
 
 	@Override
 	public IDirectEditingFeature getDirectEditingFeature(IDirectEditingContext context) {
 		IDirectEditingFeature ret = null;
 		PictogramElement pe = context.getPictogramElement();
 		AbstractText labelGa = SketchUtil.getLabelGa(pe);
 		if (labelGa != null) {
 			if (labelGa instanceof MultiText) {
 				ret = new SketchLabelDirectEditingFeature(this);
 			} else {
 				ret = new SketchTextDirectEditingFeature(this);
 			}
 		}
 
 		return ret;
 	}
 
 	@Override
 	public IResizeShapeFeature getResizeShapeFeature(IResizeShapeContext context) {
 		final PictogramElement pe = context.getPictogramElement();
 		if (pe instanceof ConnectionDecorator) {
 			return null;
 		}
 		if (pe.getGraphicsAlgorithm() instanceof Polyline) {
 			return null;
 		}
 
 		if (TEST_RESIZE_SHAPE_FEATURE) {
 			return new SketchResizeShapeFeature(this);
 		} else {
 			return super.getResizeShapeFeature(context);
 		}
 	}
 
 	@Override
 	public ICopyFeature getCopyFeature(ICopyContext context) {
 		//return new DefaultCopyFeature(this);
 		return null;
 	}
 
 	@Override
 	public IPasteFeature getPasteFeature(IPasteContext context) {
 		//return new DefaultPasteFeature(this);
 		return null;
 	}
 
 	@Override
 	public IDeleteFeature getDeleteFeature(IDeleteContext context) {
 		IDeleteFeature ret = null;
 		ret = new SketchDeleteFeature(this);
 		return ret;
 	}
 
 	public void setTestMode(boolean testMode) {
 		this.testMode = testMode;
 	}
 
 	public boolean isTestMode() {
 		return testMode;
 	}
 }
