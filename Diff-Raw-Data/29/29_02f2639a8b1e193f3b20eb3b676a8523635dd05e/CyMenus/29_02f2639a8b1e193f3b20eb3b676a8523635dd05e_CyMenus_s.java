 /*
  File: CyMenus.java
 
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 package cytoscape.view;
 
 import giny.view.GraphViewChangeEvent;
 import giny.view.GraphViewChangeListener;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.help.CSH;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JMenu;
 import javax.swing.JSeparator;
 import javax.swing.JOptionPane;
 
 import cytoscape.actions.*;
 import cytoscape.layout.ui.LayoutMenuManager;
 import cytoscape.layout.ui.SettingsAction;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.util.CytoscapeMenuBar;
 import cytoscape.util.CytoscapeToolBar;
 import cytoscape.util.undo.RedoAction;
 import cytoscape.util.undo.UndoAction;
 import cytoscape.view.cytopanels.CytoPanelName;
 
 
 /**
  * Creates the menu and tool bars for a Cytoscape window object. It
  * also provides access to individual menus and items.<BR>
  * <p>
  * AddAction takes one more optional argument to specify index. Plugin
  * writers can use this function to specify the location of the menu item.
  * </p>
  */
 public class CyMenus implements GraphViewChangeListener {
 
 	boolean menusInitialized = false;
 
 	CytoscapeMenuBar menuBar;
 	CytoscapeToolBar toolBar;
 
 	JMenu fileMenu;
 	JMenu loadSubMenu;
 	JMenu saveSubMenu;
 	JMenu newSubMenu;
 	JMenu newSubMenu2;
 	JMenu editMenu;
 	JMenu viewMenu;
 	JMenu viewSubMenu;
 	JMenu selectMenu;
 	JMenu layoutMenu;
 	JMenu vizMenu;
 	JMenu helpMenu;
 	JMenu opsMenu;
 
 	JButton openSessionButton;
 	JButton saveButton;
 	JButton zoomInButton;
 	JButton zoomOutButton;
 	JButton zoomSelectedButton;
 	JButton zoomDisplayAllButton;
 	JButton showAllButton;
 	JButton hideSelectedButton;
 	JButton annotationButton;
 	JButton helpButton;
 	JButton vizButton;
 
 	/**
 	 * Creates a new CyMenus object. This will construct the basic bar objects, 
 	 * but won't fill them with menu items and associated action listeners.
 	 */
 	public CyMenus() {
 
 		toolBar = new CytoscapeToolBar();
 
 		menuBar = new CytoscapeMenuBar();
 
 		fileMenu = menuBar.getMenu("File");
 		newSubMenu = menuBar.getMenu("File.New", 0);
 		newSubMenu2 = menuBar.getMenu("File.New.Network");
 		loadSubMenu = menuBar.getMenu("File.Import", 1);
 		saveSubMenu = menuBar.getMenu("File.Export", 2);
 		editMenu = menuBar.getMenu("Edit");
 		viewMenu = menuBar.getMenu("View");
 		selectMenu = menuBar.getMenu("Select");
 		layoutMenu = menuBar.getMenu("Layout");
 		opsMenu = menuBar.getMenu("Plugins");
 		helpMenu = menuBar.getMenu("Help");
 	}
 
 	/**
 	 * Returns the main menu bar constructed by this object.
 	 */
 	public CytoscapeMenuBar getMenuBar() {
 		return menuBar;
 	}
 
 	/**
 	 * Returns the menu with items related to file operations.
 	 */
 	public JMenu getFileMenu() {
 		return fileMenu;
 	}
 
 	/**
 	 * Returns the submenu with items related to loading objects.
 	 */
 	public JMenu getLoadSubMenu() {
 		return loadSubMenu;
 	}
 
 	/**
 	 * Returns the submenu with items related to saving objects.
 	 */
 	public JMenu getSaveSubMenu() {
 		return saveSubMenu;
 	}
 
 	/**
 	 * returns the menu with items related to editing the graph.
 	 */
 	public JMenu getEditMenu() {
 		return editMenu;
 	}
 
 	/**
 	 * Returns the menu with items related to data operations.
 	 */
 	public JMenu getViewMenu() {
 		return viewMenu;
 	}
 
 	/**
 	 * Returns the menu with items related to selecting nodes and edges in the
 	 * graph.
 	 */
 	public JMenu getSelectMenu() {
 		return selectMenu;
 	}
 
 	/**
 	 * Returns the menu with items realted to layout actions.
 	 */
 	public JMenu getLayoutMenu() {
 		return layoutMenu;
 	}
 
 	/**
 	 * Returns the menu with items related to visualiation.
 	 */
 	public JMenu getVizMenu() {
 		return vizMenu;
 	}
 
 	/**
 	 * Returns the help menu.
 	 */
 	public JMenu getHelpMenu() {
 		return helpMenu;
 	}
 
 	/**
 	 * Returns the menu with items associated with plugins. Most plugins grab
 	 * this menu and add their menu option. 
 	 */
 	public JMenu getOperationsMenu() {
 		return opsMenu;
 	}
 
 	/**
 	 * Returns the File.New.Network menu.
 	 */
 	public JMenu getNewNetworkMenu() {
 		return newSubMenu2;
 	}
 
 	/**
 	 * Returns the toolbar object constructed by this class.
 	 */
 	public CytoscapeToolBar getToolBar() {
 		return toolBar;
 	}
 
 	/**
 	 * Add the menu item. 
 	 *
 	 * @param action
 	 */
 	public void addAction(CytoscapeAction action) {
 		addCytoscapeAction(action);
 	}
 
 	/**
 	 * Add the menu item in a specific position
 	 *
 	 * @param action
 	 * @param index
 	 */
 	public void addAction(CytoscapeAction action, int index) {
 		addCytoscapeAction(action, index);
 	}
 
 	/**
 	 * Takes a CytoscapeAction and will add it to the MenuBar or the Toolbar as
 	 * is appropriate.
 	 */
 	public void addCytoscapeAction(CytoscapeAction action) {
 		if (action.isInMenuBar()) {
 			getMenuBar().addAction(action);
 		}
 
 		if (action.isInToolBar()) {
 			getToolBar().addAction(action);
 		}
 	}
 
 	/**
 	 * Add the menu item in a specific position
 	 *
 	 * @param action
 	 * @param index
 	 */
 	public void addCytoscapeAction(CytoscapeAction action, int index) {
 		if (action.isInMenuBar()) {
 			getMenuBar().addAction(action, index);
 		}
 
 		if (action.isInToolBar()) {
 			getToolBar().addAction(action);
 		}
 	}
 
 	/**
 	 * @deprecated Will be removed April 2008. Item state is now handled by Actions directly. Don't use this.
 	 */
 	public void setVisualMapperItemsEnabled(boolean newState) { }
 
 	/**
 	 * @deprecated Will be removed April 2008. Item state is now handled by Actions directly. Don't use this.
 	 */
 	public void setOverviewEnabled(boolean newState) { }
 
 	/**
 	 * @deprecated Will be removed April 2008. Item state is now handled by Actions directly. Don't use this.
 	 */
 	public void setNodesRequiredItemsEnabled() { }
 
 	/**
 	 * We are not listening for any GraphViewChangeEvents.  MenuItems are responsible for
 	 * updating their own state, which is generally accomplished by implementing the menuSelected() 
 	 * method.
 	 *
 	 * @param e
 	 */
 	public void graphViewChanged(GraphViewChangeEvent e) { }
 
 	/**
 	 * Used to return the cytopanels menu.
 	 * @deprecated Will be removed April 2008. Cytopanels no longer have a separate menu (they're in View). 
 	 */
 	public JMenu getCytoPanelMenu() {
 		return null;
 	}
 
 	/**
 	 * Used to set up the CytoPanelMenu items. 
 	 * @deprecated Will be removed April 2008. Cytopanels are initialized in the Display* actions. Do not use.
 	 */
 	public void initCytoPanelMenus() { }
 
 	/**
 	 * This method should be called by the creator of this object after the
 	 * constructor has finished. It fills the previously created menu and tool
 	 * bars with items and action listeners that respond when those items are
 	 * activated. This needs to come after the constructor is done, because some
 	 * of the listeners try to access this object in their constructors.
 	 *
 	 * Any calls to this method after the first will do nothing.
 	 */
 	public void initializeMenus() {
 		if (!menusInitialized) {
 			menusInitialized = true;
 			fillMenuBar();
 			fillToolBar();
 		}
 	}
 
 	/**
 	 * Fills the previously created menu bar with a large number of items with
 	 * attached action listener objects.
 	 */
 	private void fillMenuBar() {
 
 		//
 		// File menu
 		// 
 
 		// New submenu
 		addAction(new NewSessionAction());
 		addAction(new NewWindowSelectedNodesOnlyAction());
 		addAction(new NewWindowSelectedNodesEdgesAction());
 		addAction(new CloneGraphInNewWindowAction());
 		addAction(new NewNetworkAction());
 
 		addAction(new OpenSessionAction(),1);
 		
 		addAction(new SaveSessionAction("Save"),2);
 		addAction(new SaveSessionAsAction("Save as..."),3);
 
 		fileMenu.add(new JSeparator(), 2);
 		fileMenu.add(new JSeparator(), 5);
 		
 		
 		// Import submenu
 		addAction(new ImportGraphFileAction(this));
 		addAction(new WebServiceNetworkImportAction());
 		
 		loadSubMenu.add(new JSeparator());
 
 		addAction(new ImportNodeAttributesAction());
 		addAction(new ImportEdgeAttributesAction());
 		addAction(new ImportExpressionMatrixAction());
 
 		loadSubMenu.add(new JSeparator());
 
 		addAction(new ImportVizmapAction());
 		
 		loadSubMenu.add(new JSeparator());
 
 		// Save submenu
 		addAction(new ExportAsXGMMLAction());
 		addAction(new ExportAsGMLAction());
 		addAction(new ExportAsInteractionsAction());
 		addAction(new ExportNodeAttributesAction());
 		addAction(new ExportEdgeAttributesAction());
 		addAction(new ExportVizmapAction());
 		addAction(new ExportAsGraphicsAction());
 
 		fileMenu.add(new JSeparator());
 		addAction(new PrintAction());
 		fileMenu.add(new JSeparator());
 		addAction(new ExitAction());
 
 		//
 		// Edit menu
 		//
 		addAction(new UndoAction());
 		addAction(new RedoAction());
 
 		editMenu.add(new JSeparator());
 
 		addAction(new CreateNetworkViewAction());
 		addAction(new DestroyNetworkViewAction());
 		addAction(new DestroyNetworkAction());
 
 		editMenu.add(new JSeparator());
 
 		addAction(new DeleteAction());
 		
 		editMenu.add(new JSeparator());
 
 		addAction(new PreferenceAction());
 		addAction(new BookmarkAction());
 		addAction(new ProxyServerAction());
 
 		//
 		// Select menu
 		//
 		SelectionModeAction sma  = new SelectionModeAction();
 		selectMenu.add(sma);
 		selectMenu.addMenuListener(sma);
 
 		addAction(new InvertSelectedNodesAction());
 		addAction(new HideSelectedNodesAction());
 		addAction(new UnHideSelectedNodesAction());
 
 		addAction(new SelectAllNodesAction());
 		addAction(new DeSelectAllNodesAction());
 		addAction(new SelectFirstNeighborsAction());
 		addAction(new SelectConnectedNodesAction());
 		addAction(new AlphabeticalSelectionAction());
 		addAction(new ListFromFileSelectionAction());
 
 		addAction(new InvertSelectedEdgesAction());
 		addAction(new HideSelectedEdgesAction());
 		addAction(new UnHideSelectedEdgesAction());
 		addAction(new SelectAllEdgesAction());
 		addAction(new SelectAdjacentEdgesAction());
 		addAction(new DeSelectAllEdgesAction());
 		addAction(new BendSelectedEdgesAction());
 		addAction(new StraightenSelectedEdgesAction());
 
 		selectMenu.addSeparator();
 
 		addAction(new SelectAllAction());
 		addAction(new DeselectAllAction());
 
 		selectMenu.addSeparator();
 
 		//
 		// View menu
 		// 
		addAction(new CytoPanelAction(CytoPanelName.WEST,true));
		addAction(new CytoPanelAction(CytoPanelName.SOUTH,true));
		addAction(new CytoPanelAction(CytoPanelName.EAST,false));
		addAction(new CytoPanelAction(CytoPanelName.SOUTH_WEST,false));
 		
 		viewMenu.add(new JSeparator());
 		
 		addAction(new ShowGraphicsDetailsAction());
 
 		viewMenu.add(new JSeparator());
 
 		addAction(new SetVisualPropertiesAction());
 		viewMenu.add(new JSeparator());
 		
 		viewMenu.add(new ArrangeAction());
 		
 		//
 		// Layout menu
 		//
 		layoutMenu.addSeparator();
 		addAction(new SettingsAction() );
 		layoutMenu.addSeparator();
 		layoutMenu.addMenuListener( new LayoutMenuManager() );
 
 		//
 		// Plugin menu
 		//
 		addAction(new PluginManagerAction());
 		addAction(new PluginUpdateAction());
 
 		opsMenu.addSeparator();
 
 		// 
 		// Help menu
 		//
 		addAction(new HelpContentsAction());
 		addAction(new HelpContactHelpDeskAction());
 
 		helpMenu.addSeparator();
 
 		addAction(new HelpAboutAction());
     addAction(new LoggerAction()) ;
   }
 
 	/**
 	 * Fills the toolbar for easy access to commonly used actions.
 	 */
 	private void fillToolBar() {
 		openSessionButton = toolBar.add(new OpenSessionAction(this, false));
 		openSessionButton.setToolTipText("Open Session File...");
 		openSessionButton.setIcon(new ImageIcon(getClass()
 		                                            .getResource("images/ximian/stock_open.png")));
 		openSessionButton.setBorderPainted(false);
 		openSessionButton.setRolloverEnabled(true);
 
 		saveButton = toolBar.add(new SaveSessionAction());
 		saveButton.setToolTipText("Save Current Session as...");
 		saveButton.setIcon(new ImageIcon(getClass().getResource("images/ximian/stock_save.png")));
 
 		saveButton.setBorderPainted(false);
 		saveButton.setRolloverEnabled(true);
 
 		toolBar.addSeparator();
 
 		final ZoomAction zoom_in = new ZoomAction(1.1);
 		zoomInButton = new JButton();
 		zoomInButton.setIcon(new ImageIcon(getClass().getResource("images/ximian/stock_zoom-in.png")));
 		zoomInButton.setToolTipText("Zoom In");
 		zoomInButton.setBorderPainted(false);
 		zoomInButton.setRolloverEnabled(true);
 		zoomInButton.addMouseListener(new MouseListener() {
 				public void mouseClicked(MouseEvent e) {
 					zoom_in.zoom();
 				}
 
 				public void mouseEntered(MouseEvent e) {
 				}
 
 				public void mouseExited(MouseEvent e) {
 				}
 
 				public void mousePressed(MouseEvent e) {
 					zoomInButton.setSelected(true);
 				}
 
 				public void mouseReleased(MouseEvent e) {
 					zoomInButton.setSelected(false);
 				}
 			});
 
 		final ZoomAction zoom_out = new ZoomAction(0.9);
 		zoomOutButton = new JButton();
 		zoomOutButton.setIcon(new ImageIcon(getClass()
 		                                        .getResource("images/ximian/stock_zoom-out.png")));
 		zoomOutButton.setToolTipText("Zoom Out");
 		zoomOutButton.setBorderPainted(false);
 		zoomOutButton.setRolloverEnabled(true);
 		zoomOutButton.addMouseListener(new MouseListener() {
 				public void mouseClicked(MouseEvent e) {
 					zoom_out.zoom();
 				}
 
 				public void mouseEntered(MouseEvent e) {
 				}
 
 				public void mouseExited(MouseEvent e) {
 				}
 
 				public void mousePressed(MouseEvent e) {
 					zoomOutButton.setSelected(true);
 				}
 
 				public void mouseReleased(MouseEvent e) {
 					zoomOutButton.setSelected(false);
 				}
 			});
 
 		zoomOutButton.addMouseWheelListener(new MouseWheelListener() {
 				public void mouseWheelMoved(MouseWheelEvent e) {
 					if (e.getWheelRotation() < 0) {
 						zoom_in.zoom();
 					} else {
 						zoom_out.zoom();
 					}
 				}
 			});
 		zoomInButton.addMouseWheelListener(new MouseWheelListener() {
 				public void mouseWheelMoved(MouseWheelEvent e) {
 					if (e.getWheelRotation() < 0) {
 						zoom_in.zoom();
 					} else {
 						zoom_out.zoom();
 					}
 				}
 			});
 
 		toolBar.add(zoomOutButton);
 		toolBar.add(zoomInButton);
 
 		zoomSelectedButton = toolBar.add(new ZoomSelectedAction());
 		zoomSelectedButton.setIcon(new ImageIcon(getClass()
 		                                             .getResource("images/ximian/stock_zoom-object.png")));
 		zoomSelectedButton.setToolTipText("Zoom Selected Region");
 		zoomSelectedButton.setBorderPainted(false);
 
 		zoomDisplayAllButton = toolBar.add(new FitContentAction());
 		zoomDisplayAllButton.setIcon(new ImageIcon(getClass()
 		                                               .getResource("images/ximian/stock_zoom-1.png")));
 		zoomDisplayAllButton.setToolTipText("Zoom out to display all of current Network");
 		zoomDisplayAllButton.setBorderPainted(false);
 
 		toolBar.addSeparator();
 
 		helpButton = new JButton();
 		helpButton.addActionListener( new ActionListener() { 
 			private CSH.DisplayHelpFromSource csh;
 			public void actionPerformed(ActionEvent e) {
 				if ( csh == null ) {
 					try {
 					csh = new CSH.DisplayHelpFromSource(CyHelpBroker.getHelpBroker());
 					} catch (Exception ex) {
 						JOptionPane.showMessageDialog(null, "Help cannot be started. Please see the manual on the Cytoscape website instead: http://cytoscape.org.", "ERROR", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 				}
 				csh.actionPerformed(e);
 			}
 		});
 		helpButton.setIcon(new ImageIcon(getClass().getResource("images/ximian/stock_help.png")));
 		helpButton.setToolTipText("Help");
 		helpButton.setBorderPainted(false);
 
 		toolBar.add(helpButton);
 
 		toolBar.addSeparator();
 
 		vizButton = toolBar.add(new SetVisualPropertiesAction(false));
 		vizButton.setIcon(new ImageIcon(getClass()
 		                                    .getResource("images/ximian/stock_file-with-objects.png")));
 		vizButton.setToolTipText("Open VizMapper\u2122");
 		vizButton.setBorderPainted(false);
 	} 
 }
