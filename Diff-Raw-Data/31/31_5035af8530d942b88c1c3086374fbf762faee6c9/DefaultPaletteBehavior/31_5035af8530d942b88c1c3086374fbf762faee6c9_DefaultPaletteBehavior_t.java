 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2011, 2011 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Bug 336488 - DiagramEditor API
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import org.eclipse.gef.KeyHandler;
 import org.eclipse.gef.Tool;
 import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.tools.ConnectionCreationTool;
 import org.eclipse.gef.tools.CreationTool;
 import org.eclipse.gef.ui.palette.DefaultPaletteViewerPreferences;
 import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
 import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
 import org.eclipse.gef.ui.palette.PaletteViewer;
 import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
 import org.eclipse.gef.ui.palette.PaletteViewerProvider;
 import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
 import org.eclipse.graphiti.ui.internal.GraphitiUIPlugin;
 import org.eclipse.graphiti.ui.internal.editor.GFPaletteRoot;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 
 /**
  * This class can be subclassed by clients to adapt the palette appearance and
 * behavior of the Graphiti diagram Editor. The API is very much aligned with
  * the way GEF handles the palette within its editors, see
 * {@link GraphicalEditorWithFlyoutPalette} for more information on that. To
 * exchange the default implementation you have to return an instance of your
 * subclass in the method {@link DiagramEditor#createPaletteBehaviour()}.<br>
 * Note that there is always a 1:1 relation with a {@link DiagramEditor}.
  * 
  * @since 0.9
  */
 public class DefaultPaletteBehavior {
 
 	/**
 	 * Property name for storing the location (east, west) of the palette within
 	 * the editor in an Eclipse preference store.
 	 */
 	protected static final String PROPERTY_PALETTE_DOCK_LOCATION = "Dock location"; //$NON-NLS-1$

 	/**
 	 * Property name for storing the size of the palette within the editor in an
 	 * Eclipse preference store.
 	 */
 	protected static final String PROPERTY_PALETTE_SIZE = "Palette Size"; //$NON-NLS-1$

 	/**
 	 * Property name for storing the state (collapsed, expanded, hidden) of the
 	 * palette within the editor in an Eclipse preference store.
 	 */
 	protected static final String PROPERTY_PALETTE_STATE = "Palette state"; //$NON-NLS-1$
 
 	/**
 	 * The initial size of the palette.
 	 */
 	protected static final int DEFAULT_PALETTE_SIZE = 130;
 
	/**
	 * The associated {@link DiagramEditor}
	 */
 	protected final DiagramEditor diagramEditor;

 	private PaletteRoot paletteRoot;
 
 	/**
 	 * Creates a new standard palette behaviour for a Graphiti diagram editor.
 	 * The passed {@link DiagramEditor} is closely linked to this instance (1:1
 	 * relation) and both instances will have a common lifecycle.
 	 * 
 	 * @param diagramEditor
 	 *            The associated {@link DiagramEditor}.
 	 */
 	public DefaultPaletteBehavior(DiagramEditor diagramEditor) {
 		super();
 		this.diagramEditor = diagramEditor;
 	}
 
 	/**
 	 * Creates the {@link PaletteRoot} of this editor. To retrieve the
 	 * {@link PaletteRoot} object use {@link #getPaletteRoot()} instead which
	 * will return an already existing instance or create a new one by
 	 * delegating to this method.
 	 * 
 	 * @return a new Graphiti specific {@link PaletteRoot} instance
 	 * @see org.eclipse.graphiti.ui.editor.GraphicalEditorIncludingPalette#getPaletteRoot()
 	 */
 	protected PaletteRoot createPaletteRoot() {
 		return new GFPaletteRoot(diagramEditor.getDiagramTypeProvider());
 	}
 
 	/**
 	 * Returns the already existing {@link PaletteRoot} instance for the
	 * {@link DiagramEditor} associated the this palette behavior or creates a
 	 * new {@link PaletteRoot} instance in case none exists.
 	 * 
 	 * @return a new Graphiti specific {@link PaletteRoot} instance
 	 */
 	public PaletteRoot getPaletteRoot() {
 		if (paletteRoot == null) {
 			paletteRoot = createPaletteRoot();
 		}
 		return paletteRoot;
 	}
 
 	/**
 	 * Initializes the used GEF palette viewer to display the palette as
 	 * defined. The default implementation initializes the preference store with
 	 * the GEF {@link DefaultPaletteViewerPreferences} and triggers a refresh of
 	 * the palette.
 	 */
 	public void initializeViewer() {
 		// Set preference-store for palette
 		PaletteViewer paletteViewer = diagramEditor.getEditDomain().getPaletteViewer();
 		if (paletteViewer != null) {
 			IPreferenceStore store = GraphitiUIPlugin.getDefault().getPreferenceStore();
 			paletteViewer.setPaletteViewerPreferences(new DefaultPaletteViewerPreferences(store));
 
 			// Refresh the PaletteViewer
 			// This can be achieved by firing a font-change-event from the
 			// IPreferenceStore. It would be nicer, if the PaletteViewer would
 			// have some kind of refresh()-method directly.
 			store.firePropertyChangeEvent(PaletteViewerPreferences.PREFERENCE_FONT, null, null);
 		}
 	}
 
 	/**
 	 * Returns the Graphiti specific preferences for the palette. This method
 	 * will be called by the GEF {@link GraphicalEditorWithFlyoutPalette} during
 	 * initialization.
 	 * 
 	 * @return a Graphiti specific instanceof {@link FlyoutPreferences}.
 	 */
 	public FlyoutPreferences getPalettePreferences() {
 		return new FlyoutPreferences() {
 			public int getDockLocation() {
 				return getPreferenceStore().getInt(PROPERTY_PALETTE_DOCK_LOCATION);
 			}
 
 			public int getPaletteState() {
 				// TODO ? Move isShowFlyoutPalette from TBP to
 				// DefaultPaletteBehaviour?
 				if (!diagramEditor.getDiagramTypeProvider().getCurrentToolBehaviorProvider().isShowFlyoutPalette()) {
 					return 8; // FlyoutPaletteComposite.STATE_HIDDEN is private
 				}
 				return getPreferenceStore().getInt(PROPERTY_PALETTE_STATE);
 			}
 
 			public int getPaletteWidth() {
 				return getPreferenceStore().getInt(PROPERTY_PALETTE_SIZE);
 			}
 
 			public void setDockLocation(int location) {
 				getPreferenceStore().setValue(PROPERTY_PALETTE_DOCK_LOCATION, location);
 			}
 
 			public void setPaletteState(int state) {
 				getPreferenceStore().setValue(PROPERTY_PALETTE_STATE, state);
 			}
 
 			public void setPaletteWidth(int width) {
 				getPreferenceStore().setValue(PROPERTY_PALETTE_SIZE, width);
 			}
 		};
 	}
 
 	/**
 	 * Returns the PaletteViewerProvider, which can be used to create a new
 	 * PaletteViewer. This method can be overwritten to return a subclass of the
 	 * PaletteViewerProvider, which configures the PaletteViewer with a
 	 * different ContextMenu, with a PaletteCustomizer or with a different
 	 * IPreferencesStore. Do not call this method directly, instead call
 	 * getPaletteViewerProvider(), which buffers the created object.
 	 * <p>
 	 * By default this method returns a new PaletteViewerProvider.
 	 * 
 	 * @return The PaletteViewerProvider, which can be used to create a new
 	 *         PaletteViewer.
 	 */
 	protected PaletteViewerProvider createPaletteViewerProvider() {
 		return new PaletteViewerProvider(diagramEditor.getEditDomain()) {
 			private KeyHandler paletteKeyHandler = null;
 
 			protected void configurePaletteViewer(PaletteViewer viewer) {
 				super.configurePaletteViewer(viewer);
 				viewer.getKeyHandler().setParent(getPaletteKeyHandler());
 				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
 			}
 
 			/**
 			 * @return Palette Key Handler for the palette
 			 */
 			private KeyHandler getPaletteKeyHandler() {
 				if (paletteKeyHandler == null) {
 					paletteKeyHandler = new KeyHandler() {
 						/**
 						 * Processes a <i>key released </i> event. This method
 						 * is called by the Tool whenever a key is released, and
 						 * the Tool is in the proper state. Overridden to
 						 * support pressing the enter key to create a shape or
 						 * connection (between two selected shapes)
 						 * 
 						 * @param event
 						 *            the KeyEvent
 						 * @return <code>true</code> if KeyEvent was handled in
 						 *         some way
 						 */
 						public boolean keyReleased(KeyEvent event) {
 							if (event.keyCode == SWT.Selection) {
 								Tool tool = getEditDomain().getPaletteViewer().getActiveTool().createTool();
 								if (tool instanceof CreationTool || tool instanceof ConnectionCreationTool) {
 									tool.keyUp(event, diagramEditor.getGraphicalViewer());
 									// Deactivate current selection
 									getEditDomain().getPaletteViewer().setActiveTool(null);
 									return true;
 								}
 							}
 							return super.keyReleased(event);
 						}
 					};
 				}
 				return paletteKeyHandler;
 			}
 		};
 	}
 
 	/**
 	 * Refreshes the palette.
 	 */
 	public void refreshPalette() {
 		PaletteRoot pr = getPaletteRoot();
 		if (pr instanceof GFPaletteRoot) {
 			GFPaletteRoot gpr = (GFPaletteRoot) pr;
 			gpr.updatePaletteEntries();
 		}
 	}
 
 	/**
 	 * Disposes this instance. Must be called before closing the associated
	 * Graphiti diagram editor. The default implementation clears the
	 * {@link #paletteRoot} reference.
 	 */
 	public void dispose() {
 		paletteRoot = null;
 	}
 
 	/**
	 * Returns the associated Graphiti diagram editor.
 	 * 
 	 * @return the associated {@link DiagramEditor} instance
 	 */
 	protected DiagramEditor getDiagramEditor() {
 		return diagramEditor;
 	}
 
 	private IPreferenceStore getPreferenceStore() {
 		IPreferenceStore ps = GraphitiUIPlugin.getDefault().getPreferenceStore();
 		ps.setDefault(DefaultPaletteBehavior.PROPERTY_PALETTE_STATE, FlyoutPaletteComposite.STATE_PINNED_OPEN);
 		ps.setDefault(DefaultPaletteBehavior.PROPERTY_PALETTE_SIZE, DEFAULT_PALETTE_SIZE);
 		return ps;
 	}
 }
