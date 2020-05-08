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
 package org.eclipse.graphiti.services;
 
 import org.eclipse.graphiti.mm.StyleContainer;
 import org.eclipse.graphiti.mm.algorithms.AbstractText;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Image;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.styles.AbstractStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.AdaptedGradientColoredAreas;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Font;
 import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
 import org.eclipse.graphiti.mm.algorithms.styles.RenderingStyle;
 import org.eclipse.graphiti.mm.algorithms.styles.Style;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.util.IColorConstant;
 import org.eclipse.graphiti.util.PredefinedColoredAreas;
 
 /**
  * The interface IGaService provides convenient services for the creation and
  * layout of graphics algorithm.
  * 
  * @noimplement This interface is not intended to be implemented by clients.
  * @noextend This interface is not intended to be extended by clients.
  */
 public interface IGaService extends IGaCreateService, IGaLayoutService {

	/**
	 * @since 0.9
	 */
	public static final String DEFAULT_FONT = "Arial";

	/**
	 * @since 0.9
	 */
	public static final int DEFAULT_FONT_SIZE = 8;

 	/**
 	 * Delete font.
 	 * 
 	 * @param abstractText
 	 *            the abstract text
 	 * 
 	 * @deprecated As of release 0.8.0, replaced by
 	 *             {@link #deleteFont(Font font)}
 	 */
 	@Deprecated
 	public void deleteFont(AbstractText abstractText);
 
 	/**
 	 * Deletes the given font.
 	 * 
 	 * @param font
 	 *            the font to delete
 	 * 
 	 * @since 0.8
 	 */
 	public void deleteFont(Font font);
 
 	/**
 	 * Deletes the given color.
 	 * 
 	 * @param color
 	 *            the color to delete
 	 * 
 	 * @since 0.8
 	 */
 	public void deleteColor(Color color);
 
 	/**
 	 * Deletes the RenderingStyle from the given AbstractStyle.
 	 * 
 	 * @param abstractStyle
 	 *            the abstract style from which to delete the RenderingStyle.
 	 */
 	public void deleteRenderingStyle(AbstractStyle abstractStyle);
 
 	/**
 	 * Searches for a style with the given id. The search scope is the given
 	 * style container (which will usually be the diagram).
 	 * 
 	 * @param styleContainer
 	 *            the style container
 	 * @param id
 	 *            style id
 	 * @return the found style; null if not found
 	 */
 	public Style findStyle(StyleContainer styleContainer, String id);
 
 	/**
 	 * Gets the angle.
 	 * 
 	 * @param at
 	 *            the abstract text
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the angle
 	 */
 	public int getAngle(AbstractText at, boolean checkStyles);
 
 	/**
 	 * Gets the background color.
 	 * 
 	 * @param ga
 	 *            the graphics algorithm
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the background color
 	 */
 	public Color getBackgroundColor(GraphicsAlgorithm ga, boolean checkStyles);
 
 	/**
 	 * Gets the font.
 	 * 
 	 * @param at
 	 *            the abstract text
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the font
 	 */
 	public Font getFont(AbstractText at, boolean checkStyles);
 
 	/**
 	 * Gets the foreground color.
 	 * 
 	 * @param ga
 	 *            the graphics algorithm
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the foreground color
 	 */
 	public Color getForegroundColor(GraphicsAlgorithm ga, boolean checkStyles);
 
 	/**
 	 * Gets the horizontal alignment.
 	 * 
 	 * @param at
 	 *            the abstract text
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the horizontal alignment
 	 */
 	public Orientation getHorizontalAlignment(AbstractText at, boolean checkStyles);
 
 	/**
 	 * Gets the line style.
 	 * 
 	 * @param ga
 	 *            the graphics algorithm
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the line style
 	 */
 	public LineStyle getLineStyle(GraphicsAlgorithm ga, boolean checkStyles);
 
 	/**
 	 * Gets the line width.
 	 * 
 	 * @param ga
 	 *            the graphics algorithm
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the line width
 	 */
 	public int getLineWidth(GraphicsAlgorithm ga, boolean checkStyles);
 
 	/**
 	 * Gets the rendering style.
 	 * 
 	 * @param ga
 	 *            the graphics algorithm
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the rendering style
 	 */
 	public RenderingStyle getRenderingStyle(GraphicsAlgorithm ga, boolean checkStyles);
 
 	/**
 	 * Gets the transparency.
 	 * 
 	 * @param ga
 	 *            the graphics algorithm
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the transparency
 	 */
 	public double getTransparency(GraphicsAlgorithm ga, boolean checkStyles);
 
 	/**
 	 * Gets the vertical alignment.
 	 * 
 	 * @param at
 	 *            the abstract text
 	 * @param checkStyles
 	 *            the check styles
 	 * @return the vertical alignment
 	 */
 	public Orientation getVerticalAlignment(AbstractText at, boolean checkStyles);
 
 	/**
 	 * Sets the provided {@link AbstractStyle} (could be a
 	 * {@link GraphicsAlgorithm} or a {@link Style}) to ignore all locally set
 	 * attributes and instead use the ones provided by the style set to the
 	 * {@link AbstractStyle}.
 	 * 
 	 * @param abstractStyle
 	 *            The abstract style (style or graphics algorithm)
 	 */
 	public void ignoreAll(AbstractStyle abstractStyle);
 
 	/**
 	 * Checks if is filled.
 	 * 
 	 * @param ga
 	 *            the graphics algorithm
 	 * @param checkStyles
 	 *            the check styles
 	 * @return true, if is filled
 	 */
 	public boolean isFilled(GraphicsAlgorithm ga, boolean checkStyles);
 
 	/**
 	 * Checks if line is visible.
 	 * 
 	 * @param ga
 	 *            the graphics algorithm
 	 * @param checkStyles
 	 *            the check styles
 	 * @return true, if line is visible
 	 */
 	public boolean isLineVisible(GraphicsAlgorithm ga, boolean checkStyles);
 
 	/**
 	 * Checks if is proportional.
 	 * 
 	 * @param image
 	 *            the image
 	 * @param checkStyles
 	 *            the check styles
 	 * @return true, if is proportional
 	 */
 	public boolean isProportional(Image image, boolean checkStyles);
 
 	/**
 	 * Checks if is stretch h.
 	 * 
 	 * @param image
 	 *            the image
 	 * @param checkStyles
 	 *            the check styles
 	 * @return true, if is stretch h
 	 */
 	public boolean isStretchH(Image image, boolean checkStyles);
 
 	/**
 	 * Checks if is stretch v.
 	 * 
 	 * @param image
 	 *            the image
 	 * @param checkStyles
 	 *            the check styles
 	 * @return true, if is stretch v
 	 */
 	public boolean isStretchV(Image image, boolean checkStyles);
 
 	/**
 	 * Provides a color instance with the given color constant by either
 	 * creating a new one and aggregating it to the diagram or finding it in the
 	 * diagrams palette of colors.
 	 * 
 	 * @param diagram
 	 *            the diagram that aggregates the colors
 	 * @param colorConstant
 	 *            which contains the RGB values.
 	 * @return the color instance
 	 */
 	public Color manageColor(Diagram diagram, IColorConstant colorConstant);
 
 	/**
 	 * Provides a color instance with the given RGB values by either creating a
 	 * new one and aggregating it to the diagram or finding it in the diagrams
 	 * palette of colors.
 	 * 
 	 * @param diagram
 	 *            the diagram that aggregates the colors
 	 * @param red
 	 *            the red
 	 * @param green
 	 *            the green
 	 * @param blue
 	 *            the blue
 	 * @return the color instance
 	 */
 	public Color manageColor(Diagram diagram, int red, int green, int blue);
 
 	/**
 	 * Provides the font instance for the default font (Arial in size 8) by
 	 * either creating a new one and aggregating it to the diagram or finding it
 	 * in the diagrams list of fonts.
 	 * 
 	 * @param diagram
 	 *            the diagram that aggregates the fonts
 	 * @return the font instance
 	 * 
 	 * @since 0.9
 	 */
 	Font manageDefaultFont(Diagram diagram);
 
 	/**
 	 * Provides the font instance for the default font (Arial in size 8) by
 	 * either creating a new one and aggregating it to the diagram or finding it
 	 * in the diagrams list of fonts.
 	 * 
 	 * @param diagram
 	 *            the diagram that aggregates the fonts
 	 * @param isItalic
 	 *            the is italic
 	 * @param isBold
 	 *            the is bold
 	 * @return the font instance
 	 * 
 	 * @since 0.9
 	 */
 	Font manageDefaultFont(Diagram diagram, boolean isItalic, boolean isBold);
 
 	/**
 	 * Provides a font instance by either creating a new one and aggregating it
 	 * to the diagram or finding it in the diagrams list of fonts.
 	 * 
 	 * @param diagram
 	 *            the diagram that aggregates the fonts
 	 * @param name
 	 *            the name of the font
 	 * @param size
 	 *            the size of the font
 	 * @return the font instance
 	 * 
 	 * @since 0.8
 	 */
 	public Font manageFont(Diagram diagram, String name, int size);
 
 	/**
 	 * Provides a font instance by either creating a new one and aggregating it
 	 * to the diagram or finding it in the diagrams list of fonts.
 	 * 
 	 * @param diagram
 	 *            the diagram that aggregates the fonts
 	 * @param name
 	 *            the name of the font
 	 * @param size
 	 *            the size of the font
 	 * @param isItalic
 	 *            the is italic
 	 * @param isBold
 	 *            the is bold
 	 * @return the font instance
 	 * 
 	 * @since 0.8
 	 */
 	public Font manageFont(Diagram diagram, String name, int size, boolean isItalic, boolean isBold);
 
 	/**
 	 * Move polyline point.
 	 * 
 	 * @param polyline
 	 *            the polyline
 	 * @param index
 	 *            the index
 	 * @param deltaX
 	 *            the delta x
 	 * @param deltaY
 	 *            the delta y
 	 */
 	public void movePolylinePoint(Polyline polyline, int index, int deltaX, int deltaY);
 
 	/**
 	 * Sets a RenderingStyle with given adapted gradient colored areas for the
 	 * given {@link AbstractStyle}. The {@link AdaptedGradientColoredAreas} are
 	 * defined and created in {@link PredefinedColoredAreas}.
 	 * 
 	 * @param abstractStyle
 	 *            the abstract style for which to set the rendering style.
 	 * @param adaptedGradientColoredAreas
 	 *            The {@link AdaptedGradientColoredAreas} gradient colored
 	 *            areas.
 	 */
 	public void setRenderingStyle(AbstractStyle abstractStyle, AdaptedGradientColoredAreas adaptedGradientColoredAreas);
 
 }
