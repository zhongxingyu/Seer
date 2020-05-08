 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 355347 - Remove setters of Graphiti's Font Interface
  *    jpasch - Bug 352542 - Add "plain"-create methods for working with styles
  *    mwenz - Bug 364126 - Make GaServiceImpl extensible
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.services.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.graphiti.datatypes.IDimension;
 import org.eclipse.graphiti.internal.datatypes.impl.DimensionImpl;
 import org.eclipse.graphiti.internal.pref.GFPreferences;
 import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
 import org.eclipse.graphiti.mm.StyleContainer;
 import org.eclipse.graphiti.mm.algorithms.AbstractText;
 import org.eclipse.graphiti.mm.algorithms.AlgorithmsFactory;
 import org.eclipse.graphiti.mm.algorithms.Ellipse;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Image;
 import org.eclipse.graphiti.mm.algorithms.MultiText;
 import org.eclipse.graphiti.mm.algorithms.PlatformGraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polygon;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.AbstractStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.AdaptedGradientColoredAreas;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Font;
 import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.algorithms.styles.RenderingStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.Style;
 import org.eclipse.graphiti.mm.algorithms.styles.StylesFactory;
 import org.eclipse.graphiti.mm.algorithms.styles.StylesPackage;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.IGaService;
 import org.eclipse.graphiti.services.IPeService;
 import org.eclipse.graphiti.util.ColorConstant;
 import org.eclipse.graphiti.util.IColorConstant;
 
 /**
  * Provides the default implementation for the Graphiti
  * {@link GraphicsAlgorithm} related services. Usually clients consume this
  * service via {@link Graphiti#getGaService()} or
  * {@link Graphiti#getGaCreateService()} and get the default behavior, but
  * Clients can subclass this to modify the default attributes that will be set
  * for {@link Text} or {@link MultiText} graphics algorithms like the default
  * font by overriding
  * {@link #setDefaultTextAttributes(Diagram, AbstractText, String, boolean)}.
  * Also default attributes of other graphics algorithms can be influenced by
  * overriding {@link #setDefaultGraphicsAlgorithmAttributes(GraphicsAlgorithm)}.
  * Note that in this case Graphiti does not provide any means to manage the
  * service class instance and to access it from any place.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @since 0.9
  */
 public class GaServiceImpl implements IGaService {
 	
 	private static final IDimension calculatePolylineMinSize(Polyline polyline) {
 		Collection<Point> points = polyline.getPoints();
 
 		int minX = points.isEmpty() ? 0 : ((Point) points.toArray()[0]).getX();
 		int minY = points.isEmpty() ? 0 : ((Point) points.toArray()[0]).getY();
 		int maxX = minX;
 		int maxY = minY;
 
 		for (Iterator<Point> iter = points.iterator(); iter.hasNext();) {
 			Point point = iter.next();
 			int x = point.getX();
 			int y = point.getY();
 			minX = Math.min(minX, x);
 			minY = Math.min(minY, y);
 			maxX = Math.max(maxX, x);
 			maxY = Math.max(maxY, y);
 		}
 		return new DimensionImpl(Math.abs(maxX - minX) + 1, Math.abs(maxY - minY) + 1);
 	}
 
 	private static final int fitColorInt(int c) {
 		c = Math.max(0, c);
 		c = Math.min(255, c);
 		return c;
 	}
 
 	private static final Integer getAngle(Style style) {
 		Integer angle = style.getAngle();
 		if (angle == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getAngle(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return angle;
 		}
 	}
 
 	private static final Color getBackgroundColor(Style style) {
 		Color bg = style.getBackground();
 		if (bg == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getBackgroundColor(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return bg;
 		}
 	}
 
 	private static final Font getFont(Style style) {
 		Font font = style.getFont();
 		if (font == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getFont(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return font;
 		}
 	}
 
 	private static final Color getForegroundColor(Style style) {
 		Color fg = style.getForeground();
 		if (fg == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getForegroundColor(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return fg;
 		}
 	}
 
 	private static final Orientation getHorizontalAlignment(Style style) {
 		Orientation ha = style.getHorizontalAlignment();
 		if (ha == Orientation.UNSPECIFIED) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getHorizontalAlignment(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return ha;
 		}
 	}
 
 	private static final LineStyle getLineStyle(Style style) {
 		LineStyle ls = style.getLineStyle();
 		if (ls == LineStyle.UNSPECIFIED) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getLineStyle(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return ls;
 		}
 	}
 
 	private static final Integer getLineWidth(Style style) {
 		Integer lw = style.getLineWidth();
 		if (lw == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getLineWidth(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return lw;
 		}
 	}
 
 	private static final RenderingStyle getRenderingStyle(Style style) {
 		RenderingStyle rs = style.getRenderingStyle();
 		if (rs == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getRenderingStyle(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return rs;
 		}
 	}
 
 	private static final Double getTransparency(Style style) {
 		Double trans = style.getTransparency();
 		if (trans == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getTransparency(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return trans;
 		}
 	}
 
 	private static final Orientation getVerticalAlignment(Style style) {
 		Orientation va = style.getVerticalAlignment();
 		if (va == Orientation.UNSPECIFIED) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return getVerticalAlignment(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return va;
 		}
 	}
 
 	private static final Boolean isFilled(Style style) {
 		Boolean filled = style.getFilled();
 		if (filled == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return isFilled(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return filled;
 		}
 	}
 
 	private static final Boolean isLineVisible(Style style) {
 		Boolean lv = style.getLineVisible();
 		if (lv == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return isLineVisible(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return lv;
 		}
 	}
 
 	private static final Boolean isProportional(Style style) {
 		Boolean prop = style.getProportional();
 		if (prop == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return isProportional(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return prop;
 		}
 	}
 
 	private static final Boolean isStretchH(Style style) {
 		Boolean sh = style.getStretchH();
 		if (sh == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return isStretchH(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return sh;
 		}
 	}
 
 	private static final Boolean isStretchV(Style style) {
 		Boolean sv = style.getStretchV();
 		if (sv == null) {
 			StyleContainer styleContainer = style.getStyleContainer();
 			if (styleContainer instanceof Style) {
 				Style parentStyle = (Style) styleContainer;
 				return isStretchV(parentStyle);
 			} else {
 				return null;
 			}
 		} else {
 			return sv;
 		}
 	}
 
 	private static final void setContainer(GraphicsAlgorithm ga, GraphicsAlgorithmContainer gaContainer) {
 		if (gaContainer instanceof PictogramElement) {
 			PictogramElement pe = (PictogramElement) gaContainer;
 			pe.setGraphicsAlgorithm(ga);
 		} else if (gaContainer instanceof GraphicsAlgorithm) {
 			GraphicsAlgorithm parentGa = (GraphicsAlgorithm) gaContainer;
 			parentGa.getGraphicsAlgorithmChildren().add(ga);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.eclipse.graphiti.services.IGaLayoutService#
 	 * calculateSizeOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm)
 	 */
 	public final IDimension calculateSize(GraphicsAlgorithm ga) {
 		IDimension ret = null;
 		if (ga instanceof Polyline) {
 			Polyline pl = (Polyline) ga;
 			ret = calculatePolylineMinSize(pl);
 		} else {
 			ret = new DimensionImpl(ga.getWidth(), ga.getHeight());
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.eclipse.graphiti.services.IGaLayoutService#
 	 * calculateSizeOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final IDimension calculateSize(GraphicsAlgorithm ga, boolean considerLineWidth) {
 		IDimension ret = calculateSize(ga);
 		if (considerLineWidth) {
 			int lineWidth = getLineWidth(ga, true);
 			if (lineWidth > 1) {
 				int extent = lineWidth - 1;
 				ret.setWidth(ret.getWidth() + extent);
 				ret.setHeight(ret.getHeight() + extent);
 			}
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createDefaultMultiText
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithmContainer)
 	 */
 	public final MultiText createDefaultMultiText(Diagram diagram, GraphicsAlgorithmContainer gaContainer) {
 		return createDefaultMultiText(diagram, gaContainer, ""); //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createDefaultMultiText
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithmContainer,
 	 * java.lang.String)
 	 */
 	public final MultiText createDefaultMultiText(Diagram diagram, GraphicsAlgorithmContainer gaContainer, String value) {
 		return (MultiText) createText(diagram, gaContainer, true, value, true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createDefaultText(org.
 	 * eclipse.graphiti.mm.pictograms.GraphicsAlgorithmContainer)
 	 */
 	public final Text createDefaultText(Diagram diagram, GraphicsAlgorithmContainer gaContainer) {
 		return createDefaultText(diagram, gaContainer, ""); //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createDefaultText(org.
 	 * eclipse.graphiti.mm.pictograms.GraphicsAlgorithmContainer,
 	 * java.lang.String)
 	 */
 	public final Text createDefaultText(Diagram diagram, GraphicsAlgorithmContainer gaContainer, String value) {
 		return (Text) createText(diagram, gaContainer, false, value, true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createEllipse(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer)
 	 */
 	public final Ellipse createEllipse(GraphicsAlgorithmContainer gaContainer) {
 		Ellipse ret = AlgorithmsFactory.eINSTANCE.createEllipse();
 		setDefaultGraphicsAlgorithmAttributes(ret);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 	
 	public final Ellipse createPlainEllipse(GraphicsAlgorithmContainer gaContainer) {
 		Ellipse ret = AlgorithmsFactory.eINSTANCE.createEllipse();
 		resetAll(ret);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createImage(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, java.lang.String)
 	 */
 	public final Image createImage(GraphicsAlgorithmContainer gaContainer, String imageId) {
 		Image ret = AlgorithmsFactory.eINSTANCE.createImage();
 		setDefaultGraphicsAlgorithmAttributes(ret);
 		ret.setId(imageId);
 		ret.setProportional(false);
 		ret.setStretchH(false);
 		ret.setStretchV(false);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 	
 	public final Image createPlainImage(GraphicsAlgorithmContainer gaContainer, String imageId) {
 		Image ret = AlgorithmsFactory.eINSTANCE.createImage();
 		resetAll(ret);
 		ret.setId(imageId);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createInvisibleRectangle
 	 * (org.eclipse.graphiti.mm.pictograms.PictogramElement)
 	 */
 	public final Rectangle createInvisibleRectangle(PictogramElement pe) {
 		final Rectangle ret = createRectangle(pe);
 		if (GFPreferences.getInstance().areInvisibleRectanglesShown()) {
 			IPeService peService = Graphiti.getPeService();
 			final Color bg = manageColor(peService.getDiagramForPictogramElement(pe), IColorConstant.LIGHT_GRAY);
 			ret.setBackground(bg);
 			final Color fg = manageColor(peService.getDiagramForPictogramElement(pe), IColorConstant.YELLOW);
 			ret.setForeground(fg);
 			ret.setLineWidth(2);
 			ret.setTransparency(0.75);
 		} else {
 			ret.setFilled(false);
 			ret.setLineVisible(false);
 		}
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createMultiText(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer)
 	 */
 	public final MultiText createMultiText(GraphicsAlgorithmContainer gaContainer) {
 		return createMultiText(gaContainer, ""); //$NON-NLS-1$
 	}
 	
 	public final MultiText createPlainMultiText(GraphicsAlgorithmContainer gaContainer) {
 		return createPlainMultiText(gaContainer, ""); //$NON-NLS-1$
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createMultiText(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, java.lang.String)
 	 */
 	public final MultiText createMultiText(GraphicsAlgorithmContainer gaContainer, String value) {
 		return (MultiText) createText(null, gaContainer, true, value, false);
 	}
 	
 	public final MultiText createPlainMultiText(GraphicsAlgorithmContainer gaContainer, String value) {
 		return (MultiText) createPlainText(null, gaContainer, true, value);
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createMultiText(org.eclipse
 	 * .graphiti.mm.pictograms.Diagram,
 	 * org.eclipse.graphiti.mm.GraphicsAlgorithmContainer, java.lang.String,
 	 * java.lang.String, int)
 	 */
 	public final MultiText createMultiText(Diagram diagram, GraphicsAlgorithmContainer gaContainer, String value,
 			String fontName, int fontSize) {
 		return createMultiText(diagram, gaContainer, value, fontName, fontSize, false, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createMultiText(org.eclipse
 	 * .graphiti.mm.pictograms.Diagram,
 	 * org.eclipse.graphiti.mm.GraphicsAlgorithmContainer, java.lang.String,
 	 * java.lang.String, int, boolean, boolean)
 	 */
 	public final MultiText createMultiText(Diagram diagram, GraphicsAlgorithmContainer gaContainer, String value,
 			String fontName, int fontSize,
 			boolean isFontItalic, boolean isFontBold) {
 		MultiText text = createMultiText(gaContainer, value);
 		Font font = manageFont(diagram, fontName, fontSize, isFontItalic, isFontBold);
 		text.setFont(font);
 		return text;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.eclipse.graphiti.services.IGaCreateService#
 	 * createPlatformGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithmContainer,
 	 * java.lang.String)
 	 */
 	public final PlatformGraphicsAlgorithm createPlatformGraphicsAlgorithm(GraphicsAlgorithmContainer gaContainer,
 			String id) {
 		PlatformGraphicsAlgorithm ret = AlgorithmsFactory.eINSTANCE.createPlatformGraphicsAlgorithm();
 		setDefaultGraphicsAlgorithmAttributes(ret);
 		ret.setId(id);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 	
 	public final PlatformGraphicsAlgorithm createPlainPlatformGraphicsAlgorithm(GraphicsAlgorithmContainer gaContainer,
 			String id) {
 		PlatformGraphicsAlgorithm ret = AlgorithmsFactory.eINSTANCE.createPlatformGraphicsAlgorithm();
 		resetAll(ret);
 		ret.setId(id);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPoint(org.eclipse
 	 * .emf.ecore.EObject, int, int)
 	 */
 	public final Point createPoint(int x, int y) {
 		return createPoint(x, y, 0, 0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPoint(org.eclipse
 	 * .emf.ecore.EObject, int, int, int, int)
 	 */
 	public final Point createPoint(int x, int y, int before, int after) {
 		// StructureFieldContainer<Point> container = new StructureFieldContainer<Point>();
 		// container.put(Point.DESCRIPTORS.X(), x);
 		// container.put(Point.DESCRIPTORS.Y(), y);
 		// container.put(Point.DESCRIPTORS.BEFORE(), before);
 		// container.put(Point.DESCRIPTORS.AFTER(), after);
 		Point ret = StylesFactory.eINSTANCE.createPoint();
 		ret.setX(x);
 		ret.setY(y);
 		ret.setBefore(before);
 		ret.setAfter(after);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPointList(org.eclipse
 	 * .emf.ecore.EObject, int[])
 	 */
 	public final List<Point> createPointList(int[] xy) {
 		assert (xy != null && xy.length % 2 == 0);
 		List<Point> points = new ArrayList<Point>(xy.length / 2);
 		for (int i = 0; i < xy.length; i += 2) {
 			points.add(createPoint(xy[i], xy[i + 1]));
 		}
 		return points;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPointList(org.eclipse
 	 * .emf.ecore.EObject, int[], int[])
 	 */
 	public final List<Point> createPointList(int[] xy, int beforeAfter[]) {
 		assert (xy != null && xy.length % 2 == 0);
 		assert (beforeAfter != null && beforeAfter.length == xy.length);
 		List<Point> points = new ArrayList<Point>(xy.length / 2);
 		for (int i = 0; i < xy.length; i += 2) {
 			points.add(createPoint(xy[i], xy[i + 1], beforeAfter[i], beforeAfter[i + 1]));
 		}
 		return points;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPolygon(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer)
 	 */
 	public final Polygon createPolygon(GraphicsAlgorithmContainer gaContainer) {
 		Polygon ret = AlgorithmsFactory.eINSTANCE.createPolygon();
 		setDefaultGraphicsAlgorithmAttributes(ret);
 		ret.setFilled(true);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 	
 	public final Polygon createPlainPolygon(GraphicsAlgorithmContainer gaContainer) {
 		Polygon ret = AlgorithmsFactory.eINSTANCE.createPolygon();
 		resetAll(ret);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPolygon(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, java.util.Collection)
 	 */
 	public final Polygon createPolygon(GraphicsAlgorithmContainer gaContainer, Collection<Point> points) {
 		Polygon ret = createPolygon(gaContainer);
 		ret.getPoints().addAll(points);
 		return ret;
 	}
 	
 	public final Polygon createPlainPolygon(GraphicsAlgorithmContainer gaContainer, Collection<Point> points) {
 		Polygon ret = createPlainPolygon(gaContainer);
 		ret.getPoints().addAll(points);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPolygon(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, int[])
 	 */
 	public final Polygon createPolygon(GraphicsAlgorithmContainer gaContainer, int[] xy) {
 		List<Point> points = createPointList(xy);
 		Polygon ret = createPolygon(gaContainer, points);
 		return ret;
 	}
 	
 	public final Polygon createPlainPolygon(GraphicsAlgorithmContainer gaContainer, int[] xy) {
 		List<Point> points = createPointList(xy);
 		Polygon ret = createPlainPolygon(gaContainer, points);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPolygon(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, int[], int[])
 	 */
 	public final Polygon createPolygon(GraphicsAlgorithmContainer gaContainer, int[] xy, int beforeAfter[]) {
 		List<Point> points = createPointList(xy, beforeAfter);
 		Polygon ret = createPolygon(gaContainer, points);
 		return ret;
 	}
 
 	public final Polygon createPlainPolygon(GraphicsAlgorithmContainer gaContainer, int[] xy, int beforeAfter[]) {
 		List<Point> points = createPointList(xy, beforeAfter);
 		Polygon ret = createPlainPolygon(gaContainer, points);
 		return ret;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPolyline(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer)
 	 */
 	public final Polyline createPolyline(GraphicsAlgorithmContainer gaContainer) {
 		Polyline ret = AlgorithmsFactory.eINSTANCE.createPolyline();
 		setDefaultGraphicsAlgorithmAttributes(ret);
 		ret.setFilled(false);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 	
 	public final Polyline createPlainPolyline(GraphicsAlgorithmContainer gaContainer) {
 		Polyline ret = AlgorithmsFactory.eINSTANCE.createPolyline();
 		resetAll(ret);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPolyline(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, java.util.Collection)
 	 */
 	public final Polyline createPolyline(GraphicsAlgorithmContainer gaContainer, Collection<Point> points) {
 		Polyline ret = createPolyline(gaContainer);
 		ret.getPoints().addAll(points);
 		return ret;
 	}
 	
 	public final Polyline createPlainPolyline(GraphicsAlgorithmContainer gaContainer, Collection<Point> points) {
 		Polyline ret = createPlainPolyline(gaContainer);
 		ret.getPoints().addAll(points);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPolyline(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, int[])
 	 */
 	public final Polyline createPolyline(GraphicsAlgorithmContainer gaContainer, int[] xy) {
 		List<Point> points = createPointList(xy);
 		Polyline ret = createPolyline(gaContainer, points);
 		return ret;
 	}
 	
 	public final Polyline createPlainPolyline(GraphicsAlgorithmContainer gaContainer, int[] xy) {
 		List<Point> points = createPointList(xy);
 		Polyline ret = createPlainPolyline(gaContainer, points);
 		return ret;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createPolyline(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, int[], int[])
 	 */
 	public final Polyline createPolyline(GraphicsAlgorithmContainer gaContainer, int[] xy, int beforeAfter[]) {
 		List<Point> points = createPointList(xy, beforeAfter);
 		Polyline ret = createPolyline(gaContainer, points);
 		return ret;
 	}
 	
 	public final Polyline createPlainPolyline(GraphicsAlgorithmContainer gaContainer, int[] xy, int beforeAfter[]) {
 		List<Point> points = createPointList(xy, beforeAfter);
 		Polyline ret = createPlainPolyline(gaContainer, points);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createRectangle(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer)
 	 */
 	public final Rectangle createRectangle(GraphicsAlgorithmContainer gaContainer) {
 		Rectangle ret = AlgorithmsFactory.eINSTANCE.createRectangle();
 		setDefaultGraphicsAlgorithmAttributes(ret);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 	
 	public final Rectangle createPlainRectangle(GraphicsAlgorithmContainer gaContainer) {
 		Rectangle ret = AlgorithmsFactory.eINSTANCE.createRectangle();
 		resetAll(ret);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createRoundedRectangle
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithmContainer, int, int)
 	 */
 	public final RoundedRectangle createRoundedRectangle(GraphicsAlgorithmContainer gaContainer, int cornerWidth,
 			int cornerHeight) {
 		RoundedRectangle ret = AlgorithmsFactory.eINSTANCE.createRoundedRectangle();
 		setDefaultGraphicsAlgorithmAttributes(ret);
 		ret.setCornerWidth(cornerWidth);
 		ret.setCornerHeight(cornerHeight);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 	
 	public final RoundedRectangle createPlainRoundedRectangle(GraphicsAlgorithmContainer gaContainer, int cornerWidth,
 			int cornerHeight) {
 		RoundedRectangle ret = AlgorithmsFactory.eINSTANCE.createRoundedRectangle();
 		resetAll(ret);
 		ret.setCornerWidth(cornerWidth);
 		ret.setCornerHeight(cornerHeight);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createShiftedColor(org
 	 * .eclipse.graphiti.mm.datatypes.Color, int,
 	 * org.eclipse.graphiti.mm.pictograms.Diagram)
 	 */
 	public final Color createShiftedColor(Color color, int shift, Diagram diagram) {
 		if (color == null) {
 			throw new IllegalArgumentException("color must not be null"); //$NON-NLS-1$
 		}
 		int red = color.getRed();
 		int green = color.getGreen();
 		int blue = color.getBlue();
 
 		red = fitColorInt(red + shift);
 		green = fitColorInt(green + shift);
 		blue = fitColorInt(blue + shift);
 
 		Color ret = manageColor(diagram, red, green, blue);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createShiftedColor(org
 	 * .eclipse.graphiti.util.IColorConstant, int)
 	 */
 	public final IColorConstant createShiftedColor(IColorConstant colorConstant, int shift) {
 		int red = colorConstant.getRed();
 		int green = colorConstant.getGreen();
 		int blue = colorConstant.getBlue();
 
 		red = fitColorInt(red + shift);
 		green = fitColorInt(green + shift);
 		blue = fitColorInt(blue + shift);
 
 		IColorConstant ret = new ColorConstant(red, green, blue);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createStyle(org.eclipse
 	 * .graphiti.mm.pictograms.StyleContainer, java.lang.String)
 	 */
 	public final Style createStyle(StyleContainer styleContainer, String id) {
 		Style ret = StylesFactory.eINSTANCE.createStyle();
 		ret.setId(id);
 		ret.setStyleContainer(styleContainer);
 		return ret;
 	}
 	
 	public final Style createPlainStyle(StyleContainer styleContainer, String id) {
 		Style ret = StylesFactory.eINSTANCE.createStyle();
 		resetAll(ret);
 		ret.setId(id);
 		ret.setStyleContainer(styleContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createText(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer)
 	 */
 	public final Text createText(GraphicsAlgorithmContainer gaContainer) {
 		return createText(gaContainer, ""); //$NON-NLS-1$
 	}
 	
 	public final Text createPlainText(GraphicsAlgorithmContainer gaContainer) {
 		return createPlainText(gaContainer, ""); //$NON-NLS-1$
 	}
 
 
 	private final AbstractText createText(Diagram diagram, GraphicsAlgorithmContainer gaContainer, boolean multiText,
 			String value,
 			boolean createFont) {
 		AbstractText ret = multiText ? AlgorithmsFactory.eINSTANCE.createMultiText() : AlgorithmsFactory.eINSTANCE.createText();
 		setDefaultTextAttributes(diagram, ret, createFont);
 		ret.setValue(value);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 	
 	private final AbstractText createPlainText(Diagram diagram, GraphicsAlgorithmContainer gaContainer,
 			boolean multiText, String value) {
 		AbstractText ret = multiText ? AlgorithmsFactory.eINSTANCE.createMultiText() : AlgorithmsFactory.eINSTANCE.createText();
 		resetAll(ret);
 		ret.setValue(value);
 		setContainer(ret, gaContainer);
 		return ret;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createText(org.eclipse
 	 * .graphiti.mm.pictograms.Diagram,
 	 * org.eclipse.graphiti.mm.GraphicsAlgorithmContainer, java.lang.String,
 	 * java.lang.String, int)
 	 */
 	public final Text createText(Diagram diagram, GraphicsAlgorithmContainer gaContainer, String value,
 			String fontName, int fontSize) {
 		return createText(diagram, gaContainer, value, fontName, fontSize, false, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createText(org.eclipse
 	 * .graphiti.mm.pictograms.Diagram,
 	 * org.eclipse.graphiti.mm.GraphicsAlgorithmContainer, java.lang.String,
 	 * java.lang.String, int, boolean, boolean)
 	 */
 	public final Text createText(Diagram diagram, GraphicsAlgorithmContainer gaContainer, String value,
 			String fontName, int fontSize,
 			boolean isFontItalic, boolean isFontBold) {
 		Text text = createText(gaContainer, value);
 		Font font = manageFont(diagram, fontName, fontSize, isFontItalic, isFontBold);
 		text.setFont(font);
 		return text;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaCreateService#createText(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithmContainer, java.lang.String)
 	 */
 	public final Text createText(GraphicsAlgorithmContainer gaContainer, String value) {
 		return (Text) createText(null, gaContainer, false, value, false);
 	}
 	
 
 	public final Text createPlainText(GraphicsAlgorithmContainer gaContainer, String value) {
 		return (Text) createPlainText(null, gaContainer, false, value);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#deleteFont(org.eclipse.graphiti
 	 * .mm.pictograms.AbstractText)
 	 */
 	public final void deleteFont(AbstractText abstractText) {
 		final Font oldFont = abstractText.getFont();
 		deleteEObject(oldFont);
 	}
 
 	public final void deleteFont(Font font) {
 		deleteEObject(font);
 	}
 
 	public final void deleteColor(Color color) {
 		deleteEObject(color);
 	}
 
 	private final void deleteEObject(EObject eo) {
 		if (eo != null) {
 			EcoreUtil.delete(eo, true);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#deleteRenderingStyle(org.eclipse
 	 * .graphiti.mm.pictograms.AbstractStyle)
 	 */
 	public final void deleteRenderingStyle(AbstractStyle abstractStyle) {
 		// it is not sufficient to call abstractStyle.setRenderingStyle(null),
 		// because then the old RenderingStyle would be left as garbage in the
 		// model.
 		deleteEObject(abstractStyle.getRenderingStyle());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#findStyle(org.eclipse.graphiti
 	 * .mm.pictograms.StyleContainer, java.lang.String)
 	 */
 	public final Style findStyle(StyleContainer styleContainer, String id) {
 		Collection<Style> styles = styleContainer.getStyles();
 		for (Style childStyle : styles) {
 			if (id.equals(childStyle.getId())) {
 				return childStyle;
 			}
 			Style findStyle = findStyle(childStyle, id);
 			if (findStyle != null) {
 				return findStyle;
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getAngle(org.eclipse.graphiti
 	 * .mm.pictograms.AbstractText, boolean)
 	 */
 	public final int getAngle(AbstractText at, boolean checkStyles) {
 		Integer angle = at.getAngle();
 		if (angle == null) {
 			if (checkStyles) {
 				Style style = at.getStyle();
 				if (style != null) {
 					Integer styleValue = getAngle(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return angle;
 		}
 		return 0; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getBackgroundColor(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final Color getBackgroundColor(GraphicsAlgorithm ga, boolean checkStyles) {
 		Color bc = ga.getBackground();
 		if (bc == null) {
 			if (checkStyles) {
 				Style style = ga.getStyle();
 				if (style != null) {
 					Color styleValue = getBackgroundColor(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return bc;
 		}
 		return null; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getFont(org.eclipse.graphiti
 	 * .mm.pictograms.AbstractText, boolean)
 	 */
 	public final Font getFont(AbstractText at, boolean checkStyles) {
 		Font font = at.getFont();
 		if (font == null) {
 			if (checkStyles) {
 				Style style = at.getStyle();
 				if (style != null) {
 					Font styleValue = getFont(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return font;
 		}
 		return null; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getForegroundColor(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final Color getForegroundColor(GraphicsAlgorithm ga, boolean checkStyles) {
 		Color fc = ga.getForeground();
 		if (fc == null) {
 			if (checkStyles) {
 				Style style = ga.getStyle();
 				if (style != null) {
 					Color styleValue = getForegroundColor(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return fc;
 		}
 		return null; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getHorizontalAlignment(org.eclipse
 	 * .graphiti.mm.pictograms.AbstractText, boolean)
 	 */
 	public final Orientation getHorizontalAlignment(AbstractText at, boolean checkStyles) {
 		Orientation ha = at.getHorizontalAlignment();
 		if (ha == Orientation.UNSPECIFIED) {
 			if (checkStyles) {
 				Style style = at.getStyle();
 				if (style != null) {
 					Orientation styleValue = getHorizontalAlignment(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return ha;
 		}
 		return Orientation.ALIGNMENT_LEFT; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getLineStyle(org.eclipse.graphiti
 	 * .mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final LineStyle getLineStyle(GraphicsAlgorithm ga, boolean checkStyles) {
 		LineStyle ls = ga.getLineStyle();
 		if (ls == LineStyle.UNSPECIFIED) {
 			if (checkStyles) {
 				Style style = ga.getStyle();
 				if (style != null) {
 					LineStyle styleValue = getLineStyle(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return ls;
 		}
 		return LineStyle.SOLID; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getLineWidth(org.eclipse.graphiti
 	 * .mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final int getLineWidth(GraphicsAlgorithm ga, boolean checkStyles) {
 		Integer lw = ga.getLineWidth();
 		if (lw == null) {
 			if (checkStyles) {
 				Style style = ga.getStyle();
 				if (style != null) {
 					Integer styleValue = getLineWidth(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return lw;
 		}
 		return 1; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getRenderingStyle(org.eclipse
 	 * .graphiti.mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final RenderingStyle getRenderingStyle(GraphicsAlgorithm ga, boolean checkStyles) {
 		RenderingStyle rs = ga.getRenderingStyle();
 		if (rs == null) {
 			if (checkStyles) {
 				Style style = ga.getStyle();
 				if (style != null) {
 					RenderingStyle styleValue = getRenderingStyle(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return rs;
 		}
 		return null; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getTransparency(org.eclipse.
 	 * graphiti.mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final double getTransparency(GraphicsAlgorithm ga, boolean checkStyles) {
 		Double transparency = ga.getTransparency();
 		if (transparency == null) {
 			if (checkStyles) {
 				Style style = ga.getStyle();
 				if (style != null) {
 					Double styleValue = getTransparency(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return transparency;
 		}
 		return 0; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#getVerticalAlignment(org.eclipse
 	 * .graphiti.mm.pictograms.AbstractText, boolean)
 	 */
 	public final Orientation getVerticalAlignment(AbstractText at, boolean checkStyles) {
 		Orientation va = at.getVerticalAlignment();
 		if (va == Orientation.UNSPECIFIED) {
 			if (checkStyles) {
 				Style style = at.getStyle();
 				if (style != null) {
 					Orientation styleValue = getVerticalAlignment(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return va;
 		}
 		return Orientation.ALIGNMENT_CENTER; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#ResetAll(org.eclipse.graphiti
 	 * .mm.pictograms.AbstractStyle)
 	 */
 	public final void resetAll(AbstractStyle abstractStyle) {
 		abstractStyle.setBackground(null);
 		// Boolean filled is set to unsettable (not to null)
 		abstractStyle.unsetFilled();
 		abstractStyle.setForeground(null);
 		abstractStyle.setLineStyle(LineStyle.UNSPECIFIED);
 		// Boolean lineVisible is set to unsettable (not to null)
 		abstractStyle.unsetLineVisible();
 		abstractStyle.setLineWidth(null);
 		deleteRenderingStyle(abstractStyle);
 		abstractStyle.setTransparency(null);
 		if (abstractStyle instanceof AbstractText) {
 			AbstractText text = (AbstractText) abstractStyle;
 			text.setAngle(null);
 			text.setFont(null);
 			text.setHorizontalAlignment(Orientation.UNSPECIFIED);
 			text.setVerticalAlignment(Orientation.UNSPECIFIED);
 		} else if (abstractStyle instanceof Image) {
 			Image image = (Image) abstractStyle;
 			image.setProportional(null);
 			image.setStretchH(null);
 			image.setStretchV(null);
 		} else if (abstractStyle instanceof Style) {
 			Style style = (Style) abstractStyle;
 			style.setAngle(null);
 			style.setFont(null);
 			style.setHorizontalAlignment(Orientation.UNSPECIFIED);
 			style.setVerticalAlignment(Orientation.UNSPECIFIED);
 			style.setProportional(null);
 			style.setStretchH(null);
 			style.setStretchV(null);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#isFilled(org.eclipse.graphiti
 	 * .mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final boolean isFilled(GraphicsAlgorithm ga, boolean checkStyles) {
 		// Check if Boolean filled is unsettable
 		if (!ga.isSetFilled()) {
 			if (checkStyles) {
 				Style style = ga.getStyle();
 				if (style != null) {
 					Boolean styleValue = isFilled(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return ga.getFilled();
 		}
 		return true; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#isLineVisible(org.eclipse.graphiti
 	 * .mm.pictograms.GraphicsAlgorithm, boolean)
 	 */
 	public final boolean isLineVisible(GraphicsAlgorithm ga, boolean checkStyles) {
 		// Check if Boolean lineVisible is unsettable
 		if (!ga.isSetLineVisible()) {
 			if (checkStyles) {
 				Style style = ga.getStyle();
 				if (style != null) {
 					Boolean styleValue = isLineVisible(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return ga.getLineVisible();
 		}
 		return true; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#isProportional(org.eclipse.graphiti
 	 * .mm.pictograms.Image, boolean)
 	 */
 	public final boolean isProportional(Image image, boolean checkStyles) {
 		Boolean prop = image.getProportional();
 		if (prop == null) {
 			if (checkStyles) {
 				Style style = image.getStyle();
 				if (style != null) {
 					Boolean styleValue = isProportional(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return prop;
 		}
 		return false; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#isStretchH(org.eclipse.graphiti
 	 * .mm.pictograms.Image, boolean)
 	 */
 	public final boolean isStretchH(Image image, boolean checkStyles) {
 		Boolean sh = image.getStretchH();
 		if (sh == null) {
 			if (checkStyles) {
 				Style style = image.getStyle();
 				if (style != null) {
 					Boolean styleValue = isStretchH(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return sh;
 		}
 		return false; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#isStretchV(org.eclipse.graphiti
 	 * .mm.pictograms.Image, boolean)
 	 */
 	public final boolean isStretchV(Image image, boolean checkStyles) {
 		Boolean sv = image.getStretchV();
 		if (sv == null) {
 			if (checkStyles) {
 				Style style = image.getStyle();
 				if (style != null) {
 					Boolean styleValue = isStretchV(style);
 					if (styleValue != null)
 						return styleValue;
 				}
 			}
 		} else {
 			return sv;
 		}
 		return false; // default value
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#manageColor(org.eclipse.graphiti
 	 * .mm.pictograms.Diagram, org.eclipse.graphiti.util.IColorConstant)
 	 */
 	public final Color manageColor(Diagram diagram, IColorConstant colorConstant) {
 		return manageColor(diagram, colorConstant.getRed(), colorConstant.getGreen(), colorConstant.getBlue());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#manageColor(org.eclipse.graphiti
 	 * .mm.pictograms.Diagram, int, int, int)
 	 */
	public final Color manageColor(Diagram diagram, int red, int green, int blue) {
 		Collection<Color> colors = diagram.getColors();
 		for (Color existingColor : colors) {
 			if (existingColor.getRed() == red && existingColor.getGreen() == green && existingColor.getBlue() == blue) {
 				return existingColor;
 			}
 		}
 
 		Color newColor = StylesFactory.eINSTANCE.createColor();
 		newColor.eSet(StylesPackage.eINSTANCE.getColor_Red(), red);
 		newColor.eSet(StylesPackage.eINSTANCE.getColor_Green(), green);
 		newColor.eSet(StylesPackage.eINSTANCE.getColor_Blue(), blue);
 		colors.add(newColor);
 		return newColor;
 	}
 
 	public final Font manageDefaultFont(Diagram diagram) {
 		return manageDefaultFont(diagram, false, false);
 	}
 
 	public final Font manageDefaultFont(Diagram diagram, boolean isItalic, boolean isBold) {
 		return manageFont(diagram, DEFAULT_FONT, DEFAULT_FONT_SIZE, isItalic, isBold);
 	}
 
 	public final Font manageFont(Diagram diagram, String name, int size) {
 		return manageFont(diagram, name, size, false, false);
 	}
 
 	public final Font manageFont(Diagram diagram, String name, int size, boolean isItalic, boolean isBold) {
 		EList<Font> fonts = diagram.getFonts();
 		for (Font font : fonts) {
 			if (font.getName().equals(name) && font.getSize() == size && font.isBold() == isBold && font.isItalic() == isItalic)
 				return font;
 		}
 		Font newFont = StylesFactory.eINSTANCE.createFont();
 		newFont.eSet(StylesPackage.eINSTANCE.getFont_Name(), name);
 		newFont.eSet(StylesPackage.eINSTANCE.getFont_Size(), size);
 		newFont.eSet(StylesPackage.eINSTANCE.getFont_Italic(), isItalic);
 		newFont.eSet(StylesPackage.eINSTANCE.getFont_Bold(), isBold);
 		fonts.add(newFont);
 		return newFont;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#movePolylinePoint(org.eclipse
 	 * .graphiti.mm.pictograms.Polyline, int, int, int)
 	 */
 	public final void movePolylinePoint(Polyline polyline, int index, int deltaX, int deltaY) {
 		Point point = polyline.getPoints().get(index);
 		int oldX = point.getX();
 		int oldY = point.getY();
 
 		polyline.getPoints().set(index, createPoint(oldX + deltaX, oldY + deltaY));
 	}
 
 	/**
 	 * Sets the default attributes of the newly created Graphiti
 	 * {@link GraphicsAlgorithm}s. The default implementation sets the location
 	 * (X and Y values) and the size (Width and height values) to 0, the
 	 * {@link LineStyle} to {@link LineStyle#SOLID solid}, the line width to 1
 	 * and the transparency to 0.
 	 * 
 	 * @param graphicsAlgorithm
 	 *            the {@link GraphicsAlgorithm} to set the defaults for
 	 */
 	protected void setDefaultGraphicsAlgorithmAttributes(GraphicsAlgorithm graphicsAlgorithm) {
 		setLocationAndSize(graphicsAlgorithm, 0, 0, 0, 0);
 		graphicsAlgorithm.setLineStyle(LineStyle.SOLID);
 		graphicsAlgorithm.setLineWidth(1);
 		graphicsAlgorithm.setTransparency(0d);
 	}
 
 	/**
 	 * Sets the default attributes of newly created {@link AbstractText}
 	 * graphics algorithms ({@link Text} and {@link MultiText}). The default
 	 * implementation sets the default attributes for all graphics algorithms
 	 * {@link #setDefaultGraphicsAlgorithmAttributes(GraphicsAlgorithm)}, filled
 	 * to <code>false</code> and the font to {@link IGaService#DEFAULT_FONT}
 	 * with size {@link IGaService#DEFAULT_FONT_SIZE} (Arial in size 8).
 	 * 
 	 * @param diagram
 	 *            the diagram to use for managing the {@link Font}
 	 * @param abstractText
 	 *            the {@link AbstractText} to set the attributes for
 	 * @param createFont
 	 *            <code>true</code> in case the font shall be created,
 	 *            <code>false</code> otherwise
 	 */
 	protected void setDefaultTextAttributes(Diagram diagram, AbstractText abstractText, boolean createFont) {
 		setDefaultGraphicsAlgorithmAttributes(abstractText);
 		abstractText.setFilled(false);
 		if (createFont && diagram != null) {
 			Font font = manageFont(diagram, DEFAULT_FONT, DEFAULT_FONT_SIZE);
 			abstractText.setFont(font);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaLayoutService#setHeightOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm, int)
 	 */
 	public final void setHeight(GraphicsAlgorithm ga, int height) {
 		ga.setHeight(height);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.eclipse.graphiti.services.IGaLayoutService#
 	 * setLocationAndSizeOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm, int, int, int,
 	 * int)
 	 */
 	public final void setLocationAndSize(GraphicsAlgorithm ga, int x, int y, int width, int height) {
 		setLocationAndSize(ga, x, y, width, height, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seeorg.eclipse.graphiti.services.IGaLayoutService#
 	 * setLocationAndSizeOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm, int, int, int,
 	 * int, boolean)
 	 */
 	public final void setLocationAndSize(GraphicsAlgorithm ga, int x, int y, int width, int height,
 			boolean avoidNegativeCoordinates) {
 		setLocation(ga, x, y, avoidNegativeCoordinates);
 		setSize(ga, width, height);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaLayoutService#setLocationOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm, int, int)
 	 */
 	public final void setLocation(GraphicsAlgorithm ga, int x, int y) {
 		setLocation(ga, x, y, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaLayoutService#setLocationOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm, int, int, boolean)
 	 */
 	public final void setLocation(GraphicsAlgorithm ga, int x, int y, boolean avoidNegativeCoordinates) {
 
 		if (ga == null) {
 			return;
 		}
 
 		if (avoidNegativeCoordinates) {
 			x = Math.max(x, 0);
 			y = Math.max(y, 0);
 		}
 
 		int oldX = ga.getX();
 		if (oldX != x) {
 			ga.setX(x);
 		}
 		int oldY = ga.getY();
 		if (oldY != y) {
 			ga.setY(y);
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaLayoutService#setSizeOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm, int, int)
 	 */
 	public final void setSize(GraphicsAlgorithm ga, int width, int height) {
 		setWidth(ga, width);
 		setHeight(ga, height);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaLayoutService#setWidthOfGraphicsAlgorithm
 	 * (org.eclipse.graphiti.mm.pictograms.GraphicsAlgorithm, int)
 	 */
 	public final void setWidth(GraphicsAlgorithm ga, int width) {
 		ga.setWidth(width);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.graphiti.services.IGaService#setRenderingStyle(org.eclipse
 	 * .graphiti.mm.pictograms.AbstractStyle,
 	 * org.eclipse.graphiti.mm.pictograms.AdaptedGradientColoredAreas)
 	 */
 	public final void setRenderingStyle(AbstractStyle abstractStyle,
 			AdaptedGradientColoredAreas adaptedGradientColoredAreas) {
 		if (adaptedGradientColoredAreas != null && adaptedGradientColoredAreas.getAdaptedGradientColoredAreas() != null
 				&& !adaptedGradientColoredAreas.getAdaptedGradientColoredAreas().isEmpty()
 				&& adaptedGradientColoredAreas.getGradientType() != null) {
 			// set the RenderingStyle with AdaptedGradientColoredAreas
 			RenderingStyle renderingStyle = abstractStyle.getRenderingStyle();
 			if (renderingStyle == null) {
 				renderingStyle = StylesFactory.eINSTANCE.createRenderingStyle();
 				abstractStyle.setRenderingStyle(renderingStyle);
 			}
 			renderingStyle.setAdaptedGradientColoredAreas(adaptedGradientColoredAreas);
 		} else {
 			throw new IllegalArgumentException("Object AdaptedGradientColoredAreas or its attributes must not be null or empty"); //$NON-NLS-1$
 		}
 	}
 }
