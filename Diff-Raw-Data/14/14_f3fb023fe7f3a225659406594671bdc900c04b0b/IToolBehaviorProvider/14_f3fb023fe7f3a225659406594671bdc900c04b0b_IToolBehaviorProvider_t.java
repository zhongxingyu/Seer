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
  *    mwenz - Bug 341224: Allow to hide the selection and marquee tools in the palette
  *    mwenz - Bug 363796 - Make setting of selection width of connections public
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.tb;
 
import java.awt.Polygon;

 import org.eclipse.graphiti.DiagramScrollingBehavior;
 import org.eclipse.graphiti.IExecutionInfo;
 import org.eclipse.graphiti.features.context.ICustomContext;
 import org.eclipse.graphiti.features.context.IDoubleClickContext;
 import org.eclipse.graphiti.features.context.IPictogramElementContext;
 import org.eclipse.graphiti.features.custom.ICustomFeature;
 import org.eclipse.graphiti.features.impl.IIndependenceSolver;
 import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.ContainerShape;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.palette.IPaletteCompartmentEntry;
 import org.eclipse.graphiti.util.ILocationInfo;
 
 /**
  * The Interface IToolBehaviorProvider.
  * 
  * @noimplement This interface is not intended to be implemented by clients,
  *              extend {@link DefaultToolBehaviorProvider} instead
  * @noextend This interface is not intended to be extended by clients.
  */
 public interface IToolBehaviorProvider {
 
 	/**
 	 * Returns the context button pad data for the given pictogram element
 	 * context. The context button pad data defines, which context buttons to
 	 * show for a given pictogram element. Can return null, if no there is no
 	 * context button pad for the given pictogram element.
 	 * 
 	 * @param context
 	 *            The pictogram element context, for which to return the context
 	 *            button pad data.
 	 * @return The context button pad data for the given pictogram element
 	 *         context.
 	 */
 	IContextButtonPadData getContextButtonPad(IPictogramElementContext context);
 
 	/**
 	 * Returns the context menu for the current mouse location.
 	 * 
 	 * @param context
 	 *            the custom context which contains the info about the location
 	 *            where the context menu appears.
 	 * @return the context menu
 	 */
 	IContextMenuEntry[] getContextMenu(ICustomContext context);
 
 	/**
 	 * Return the palette entries for the palette of the graphical editor.
 	 * Typically these entries are a subset of CreateFeatures and
 	 * CreateConnectionFeatures which are provided by the feature provider.
 	 * 
 	 * @return the palette entries
 	 */
 	IPaletteCompartmentEntry[] getPalette();
 
 	/**
 	 * Returns a feature which will be executed at at double click. For that
 	 * purpose a custom feature is used, because custom features appear in the
 	 * context menu and the double click feature should also appear in the
 	 * context menu (usual UI guideline).
 	 * 
 	 * @param context
 	 *            contains information where the double click gesture has
 	 *            happened
 	 * @return the feature to execute
 	 */
 	ICustomFeature getDoubleClickFeature(IDoubleClickContext context);
 
 	/**
 	 * Returns decorators which will be used at rendering time to decorate the
 	 * graphical representation of the given pictogram element.<br>
 	 * Currently only decorators of type {@link IImageDecorator} are supported.
 	 * 
 	 * @param pe
 	 *            the pictogram element
 	 * @return the decorators
 	 */
 	IDecorator[] getDecorators(PictogramElement pe);
 
 	/**
 	 * Returns the adapter for the specified key. This method will be called in
 	 * the getAdapter() method of the graphical editor.
 	 * 
 	 * @param type
 	 *            the type
 	 * @return the adapter
 	 */
 	Object getAdapter(Class<?> type);
 
 	/**
 	 * Override this method if you want to change the default selection on mouse
 	 * click.
 	 * 
 	 * @param originalPe
 	 *            the original pictogram element
 	 * @param oldSelection
 	 *            the old selection
 	 * @return an active pictogram element to be selected instead; return
 	 *         <code>null</code> if there should not be a special selection
 	 *         behavior; if there should not be a selection simply return the
 	 *         diagram
 	 */
 	PictogramElement getSelection(PictogramElement originalPe, PictogramElement[] oldSelection);
 
 	/**
 	 * Override this method if you want to change the default scrolling behavior
 	 * of the diagram. The default is DiagramScrollingBehavior.GEF_DEFAULT: the
 	 * empty diagram comes up without scroll bars. The scroll bars start to
 	 * appear when objects are moved to the outside of the currently visible
 	 * area. For permanently visible scroll bars, return the value
 	 * DiagramScrollingBehavior.SCROLLBARS_PERMANENTLY_VISIBLE.
 	 * 
 	 * @return The DiagramScrollingBehavior
 	 * @deprecated Scroll bar based infinite canvas is a workaround for GEF
 	 *             limitations.
 	 * 
 	 * @see DefaultToolBehaviorProvider#getDiagramScrollingBehavior()
 	 */
 	@Deprecated
 	DiagramScrollingBehavior getDiagramScrollingBehavior();
 
 	/**
 	 * Gets the zoom levels.
 	 * 
 	 * @return the zoom levels
 	 */
 	double[] getZoomLevels();
 
 	/**
 	 * Dispose.
 	 */
 	void dispose();
 
 	/**
 	 * Return the selection info for the given shape.
 	 * 
 	 * @param shape
 	 *            the shape
 	 * @return the shape selection info
 	 */
 	ISelectionInfo getSelectionInfoForShape(Shape shape);
 
 	/**
 	 * Return the selection info for the given connection.
 	 * 
 	 * @param connection
 	 *            the connection
 	 * @return the connection selection info
 	 */
 	ISelectionInfo getSelectionInfoForConnection(Connection connection);
 
 	/**
 	 * Returns the location info which will be used for direct editing if the
 	 * framework cannot decide this. E.g. a shape is selected and the user
 	 * presses F2 but the mouse is outside the shape.
 	 * 
 	 * @param pe
 	 *            the active and selected pictogram element
 	 * @param currentLocationInfo
 	 *            the current location info determined by the framework
 	 * @return the location info for the given selected pictogram element
 	 */
 	ILocationInfo getLocationInfo(PictogramElement pe, ILocationInfo currentLocationInfo);
 
 	/**
 	 * Called before execution on stack. Overriding this method can be necessary
 	 * if any additional actions have to be processed before the stack will be
 	 * executed.
 	 * 
 	 * @param executionInfo
 	 *            info about content to be executed
 	 */
 	void preExecute(IExecutionInfo executionInfo);
 
 	/**
 	 * Called after execution on stack. Overriding this method can be necessary
 	 * if any additional actions have to be processed after the stack will be
 	 * executed. As example a tool could process an automatic layout of the
 	 * diagram after each diagram modification.
 	 * 
 	 * @param executionInfo
 	 *            info about content to be executed
 	 */
 	void postExecute(IExecutionInfo executionInfo);
 
 	/**
 	 * Returns the tooltip to be attached to the graphical representation of the
 	 * given graphics algorithm.
 	 * 
 	 * @param graphicsAlgorithm
 	 *            the graphics algorithm
 	 * @return the tooltip
 	 */
 	String getToolTip(GraphicsAlgorithm graphicsAlgorithm);
 
 	/**
 	 * The returned graphics algorithm defines the selection border and the
 	 * rectangle where the context buttons appear at.
 	 * 
 	 * @param pe
 	 *            the pictogram element
 	 * @return the graphics algorithm which defines the selection border
 	 */
 	GraphicsAlgorithm getSelectionBorder(PictogramElement pe);
 
 	/**
 	 * The returned graphics algorithm defines the technical container for
 	 * active children. Currently implementers have to secure to deliver a
 	 * (Rounded)Rectangle. Does not make sense for other types of graphics
 	 * algorithm.
 	 * 
 	 * @param cs
 	 *            the container shape
 	 * @return the graphics algorithm acting as technical container
 	 */
 	GraphicsAlgorithm getContentArea(ContainerShape cs);
 
 	/**
 	 * The returned graphics algorithm's define the area where the user can
 	 * click to select the shape.
 	 * 
 	 * @param pe
 	 *            the pictogram element
 	 * @return the click area
 	 */
 	GraphicsAlgorithm[] getClickArea(PictogramElement pe);
 
 	/**
 	 * Provides the graphics algorithm that defines the outline for the given
 	 * pictogram element's chopbox Anchor.
 	 * 
 	 * @param pe
 	 *            the given pictogram element
 	 * @return the graphics algorithm that defines the outline for connections
 	 *         from or to the shape's chopbox anchor
 	 */
 	GraphicsAlgorithm getChopboxAnchorArea(PictogramElement pe);
 
 	/**
 	 * Returns a tooltip for the workbench titlebar.
 	 * 
 	 * @return a tooltip or <code>null</code> to indicate that the default from
 	 *         the graphics framework will be used
 	 */
 	String getTitleToolTip();
 
 	/**
 	 * Indicates whether guides will be shown or not.
 	 * 
 	 * @return <code>true</code>, if guides should be shown
 	 */
 	boolean isShowGuides();
 	
 	/**
 	 * Indicates whether flyout palette will be shown or not.
 	 * 
 	 * @return <code>true</code>, if flyout palette should be shown
 	 * @since 0.9
 	 */
 	boolean isShowFlyoutPalette();
 
 	/**
 	 * Indicates whether the selection tool shall be added to the palette or not
 	 * 
 	 * @return <code>true</code>, if the selection tool shall be added,
 	 *         <code>false</code> otherwise
 	 * 
 	 * @since 0.9
 	 */
 	boolean isShowSelectionTool();
 
 	/**
 	 * Indicates whether the marquee tool shall be added to the palette or not
 	 * 
 	 * @return <code>true</code>, if the marquee tool shall be added,
 	 *         <code>false</code> otherwise
 	 * 
 	 * @since 0.9
 	 */
 	boolean isShowMarqueeTool();
 
 	/**
 	 * Indicates if the selection of multiple elements is enabled. Override this
 	 * method and return <code>false</code> if an editor with single selection
 	 * behavior is needed.
 	 * 
 	 * @return <code>false</code>, if single selection is enforced
 	 */
 	boolean isMultiSelectionEnabled();
 
 	/**
 	 * Indicates if the selection of connections is enabled.
 	 * 
 	 * @return <code>true</code>, if selection of connections is enabled
 	 */
 	boolean isConnectionSelectionEnabled();
 
 	/**
 	 * Returns the contributor ID for the tabbed property sheet page.
 	 * 
 	 * @return the contributor ID for the tabbed property sheet page.
 	 */
 	public String getContributorId();
 
 	/**
 	 * Decides if business objects are equal. The framework uses this method
 	 * only for EMF business objects. For the non-EMF case,
 	 * {@link IIndependenceSolver} is responsible.
 	 * 
 	 * @return true if the business objects are deemed equal, false otherwise.
 	 * @since 0.8
 	 */
 	public boolean equalsBusinessObjects(Object o1, Object o2);
 
 	/**
	 * Is asked to return the selection width (the tolerance area a user can
	 * click to still select) a lathy object (e.g. {@link Polyline} or
	 * {@link Polygon} for {@link Shape}s and {@link Connection}s) in the
	 * diagram. The method is called when a new object is drawn for the first
	 * time onto a diagram (on creation of the object or on opening the
	 * diagram).
	 * 
	 * @param polyline
	 *            the {@link Polyline} object to get the selection width for
	 * @return an int representing the allowed tolerance for clicking in pixels
	 * 
 	 * @since 0.9
 	 */
 	public int getLineSelectionWidth(Polyline polyline);
 }
