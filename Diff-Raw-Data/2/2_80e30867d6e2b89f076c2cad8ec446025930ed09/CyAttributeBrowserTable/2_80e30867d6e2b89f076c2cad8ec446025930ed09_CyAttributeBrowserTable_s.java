 /*
  =====================================================================
 
  JSortTable.java
 
  Created by Claude Duguay
  Copyright (c) 2002
 
  =====================================================================
  */
 package browser.ui;
 
 import static browser.DataObjectType.EDGES;
 import static browser.DataObjectType.NETWORK;
 import static browser.DataObjectType.NODES;
 import giny.model.Edge;
 import giny.model.GraphObject;
 import giny.model.Node;
 import giny.view.EdgeView;
 import giny.view.NodeView;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.KeyboardFocusManager;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.JViewport;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 import org.cytoscape.equations.Equation;
 
 import browser.AttributeBrowser;
 import browser.DataObjectType;
 import browser.DataTableModel;
 import browser.SortTableModel;
 import browser.ValidatedObjectAndEditString;
 import browser.util.HyperLinkOut;
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesUtils;
 import cytoscape.data.SelectEvent;
 import cytoscape.data.SelectEventListener;
 import cytoscape.data.Semantics;
 import cytoscape.dialogs.NetworkMetaDataDialog;
 import cytoscape.logger.CyLogger;
 import cytoscape.util.CyFileFilter;
 import cytoscape.util.FileUtil;
 import cytoscape.util.OpenBrowser;
 import cytoscape.util.swing.ColumnResizer;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 import cytoscape.visual.GlobalAppearanceCalculator;
 import cytoscape.visual.VisualMappingManager;
 
 
 /**
  *  Based on JSortTable and completely rewritten by kono
  */
 public class CyAttributeBrowserTable extends JTable implements MouseListener, ActionListener,
                                                                PropertyChangeListener,
                                                                SelectEventListener, MouseMotionListener
 {
 	public static final int SELECTED_NODE = 1;
 	public static final int REV_SELECTED_NODE = 2;
 	public static final int SELECTED_EDGE = 3;
 	public static final int REV_SELECTED_EDGE = 4;
 
 	// Target network to watch selection
 	CyNetwork currentNetwork;
 
 	// Global calcs used for coloring
 	private VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
 	private GlobalAppearanceCalculator gac;
 	protected int sortedColumnIndex = -1;
 	protected boolean sortedColumnAscending = true;
 	private Color selectedNodeColor;
 	private Color selectedEdgeColor;
 	private Color reverseSelectedNodeColor;
 	private Color reverseSelectedEdgeColor;
 
 	// For right-click menu
 	private JPopupMenu rightClickPopupMenu;
 	private JPopupMenu rightClickHeaderPopupMenu;
 	private JPopupMenu cellMenu;
 	private JMenuItem openFormulaBuilderMenuItem = null;
 	private JMenuItem copyMenuItem = null;
 	private JMenuItem copyToCurrentSelectionMenuItem = null;
 	private JMenuItem copyFormulaToCurrentSelectionMenuItem = null;
 	private JMenuItem copyToEntireAttributeMenuItem = null;
 	private JMenuItem copyFormulaToEntireAttributeMenuItem = null;
 	private JMenu exportMenu = null;
 	private JMenuItem exportCellsMenuItem = null;
 	private JMenuItem exportTableMenuItem = null;
 	private JMenuItem selectAllMenuItem = null;
 	private JMenuItem newSelectionMenuItem = null;
 	private JCheckBoxMenuItem coloringMenuItem = null;
 	private Clipboard systemClipboard;
 	private StringSelection stsel;
 	private DataTableModel tableModel;
 	private DataObjectType objectType;
 	private Map<String, Map<String, String>> linkoutProps;
 	private CyLogger logger = null;
 	private FormulaBuilderDialog formulaBuilderDialog = null;
 	private int mouseX;
 	private static final Font BORDER_FONT = new Font("Sans-serif", Font.BOLD, 12);
 
 	private HashMap<String, Integer> columnWidthMap = new HashMap<String, Integer>();
 
 	// For turning off listener during session loading
 	private boolean ignore = false;
 
 	/**
 	 * String used to compare against os.name System property -
 	 * to determine if we are running on Windows platform.
 	 */
 	static final String MAC_OS_ID = "mac";
 	/**
 	 * Routine which determines if we are running on mac platform
 	 *
 	 * @return boolean
 	 */
 	private boolean isMacPlatform() {
 		String os = System.getProperty("os.name");
 
 		return os.regionMatches(true, 0, MAC_OS_ID, 0, MAC_OS_ID.length());
 	}
 
 	protected static final String LS = System.getProperty("line.separator");
 
 	public CyAttributeBrowserTable(DataTableModel model, DataObjectType objectType) {
 		super(model);
 		logger = CyLogger.getLogger(CyAttributeBrowserTable.class);
 		initSortHeader();
 
 		this.tableModel = model;
 		this.objectType = objectType;
 		
 		initialize();
 	}
 
 	// Initialize some attributes of this table
 	private void initialize() {
 		this.setSize(400, 300);
 		this.setCellSelectionEnabled(true);
 		this.getPopupMenu();
 		this.getHeaderPopupMenu();
 		
 
 		setKeyStroke();
 		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 
 		setSelectedColor(SELECTED_NODE);
 		setSelectedColor(REV_SELECTED_NODE);
 		setSelectedColor(SELECTED_EDGE);
 		setSelectedColor(REV_SELECTED_EDGE);
 
 		this.setDefaultRenderer(Object.class, new BrowserTableCellRenderer(false, objectType));
 		this.getColumnModel().addColumnModelListener(this);
 		this.setDefaultEditor(Object.class, new MultiLineTableCellEditor() );
 		
 		this.getTableHeader().addMouseMotionListener(this);
 	}
 
 	public void valueChanged(final ListSelectionEvent event) {
 		super.valueChanged(event);
 
 		final boolean singleCellSelected = getSelectedRowCount() == 1 && getSelectedColumnCount() == 1;
 		if (singleCellSelected) {
 			copyToCurrentSelectionMenuItem.setEnabled(true);
 			copyFormulaToCurrentSelectionMenuItem.setEnabled(true);
 			copyToEntireAttributeMenuItem.setEnabled(true);
 			copyFormulaToEntireAttributeMenuItem.setEnabled(true);
 		}
 		else {
 			copyToCurrentSelectionMenuItem.setEnabled(false);
 			copyFormulaToCurrentSelectionMenuItem.setEnabled(false);
 			copyToEntireAttributeMenuItem.setEnabled(false);
 			copyFormulaToEntireAttributeMenuItem.setEnabled(false);
 		}
 	}
 
 	private void setKeyStroke() {
 		KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
 		// Identifying the copy KeyStroke user can modify this
 		// to copy on some other Key combination.
 		this.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
 		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 	}
 
 	protected void setSelectedColor(final int type) {
 		gac = vmm.getVisualStyle().getGlobalAppearanceCalculator();
 
 		switch (type) {
 			case SELECTED_NODE:
 				selectedNodeColor = gac.getDefaultNodeSelectionColor();
 				break;
 			case REV_SELECTED_NODE:
 				reverseSelectedNodeColor = gac.getDefaultNodeReverseSelectionColor();
 				break;
 			case SELECTED_EDGE:
 				selectedEdgeColor = gac.getDefaultEdgeSelectionColor();
 				break;
 			case REV_SELECTED_EDGE:
 				reverseSelectedEdgeColor = gac.getDefaultEdgeReverseSelectionColor();
 				break;
 			default:
 				break;
 		}
 	}
 
 	protected Color getSelectedColor(final int type) {
 		Color newColor;
 		gac = vmm.getVisualStyle().getGlobalAppearanceCalculator();
 
 		switch (type) {
 			case SELECTED_NODE:
 				newColor = gac.getDefaultNodeSelectionColor();
 				break;
 			case REV_SELECTED_NODE:
 				newColor = gac.getDefaultNodeReverseSelectionColor();
 				break;
 			case SELECTED_EDGE:
 				newColor = gac.getDefaultEdgeSelectionColor();
 				break;
 			case REV_SELECTED_EDGE:
 				newColor = gac.getDefaultEdgeReverseSelectionColor();
 				break;
 			default:
 				newColor = null;
 				break;
 		}
 
 		return newColor;
 	}
 
 	protected Edge getEdge(final String edgeString) {
 		String[] edgeNameParts = edgeString.split(" \\(");
 		final Node source = Cytoscape.getCyNode(edgeNameParts[0]);
 		edgeNameParts = edgeNameParts[1].split("\\) ");
 
 		final String interaction = edgeNameParts[0];
 		final Node target = Cytoscape.getCyNode(edgeNameParts[1]);
 
 		return Cytoscape.getCyEdge(source, target, Semantics.INTERACTION, interaction, false);
 	}
 
 	private Map<String, GraphObject> paintNodesAndEdges(int idLocation) {
 		final int[] rowsSelected = getSelectedRows();
 		final Map<String, GraphObject> selectedMap = new HashMap<String, GraphObject>();
 		final int selectedRowLength = rowsSelected.length;
 		final CyNetworkView netView = Cytoscape.getCurrentNetworkView();
 
 		for (int idx = 0; idx < selectedRowLength; idx++) {
 			final ValidatedObjectAndEditString objectAndEditString =
 				(ValidatedObjectAndEditString)getValueAt(rowsSelected[idx], idLocation);
 			if (objectAndEditString == null)
 				continue;
 
 			final String selectedName = (String)objectAndEditString.getValidatedObject();
 			if (selectedName == null)
 				continue;
 
 			if (objectType == NODES) {
 				// Change node color
 				final Node selectedNode = Cytoscape.getCyNode(selectedName);
 				selectedMap.put(selectedName, selectedNode);
 
 				if (netView != Cytoscape.getNullNetworkView()) {
 					final NodeView nv = netView.getNodeView(selectedNode);
 					if (nv != null)
 						nv.setSelectedPaint(reverseSelectedNodeColor);
 				}
 			} else if (objectType == EDGES) {
 				final Edge selectedEdge = getEdge(selectedName);
 				selectedMap.put(selectedName, selectedEdge);
 
 				if (netView != Cytoscape.getNullNetworkView()) {
 					final EdgeView ev = netView.getEdgeView(selectedEdge);
 					if (ev != null)
 						ev.setSelectedPaint(reverseSelectedEdgeColor);
 				}
 			}
 		}
 
 		return selectedMap;
 	}
 
 	private void resetObjectColor(int idLocation) {
 		final CyNetworkView view = Cytoscape.getCurrentNetworkView();
 		if ((view == Cytoscape.getNullNetworkView()) || (view == null))
 			return;
 
 		final int rowCount = dataModel.getRowCount();
 		for (int idx = 0; idx < rowCount; idx++) {
 			final ValidatedObjectAndEditString val = (ValidatedObjectAndEditString)dataModel.getValueAt(idx, idLocation);
 			final String objectName;
 			try {
 				objectName = (String)val.getValidatedObject();
 			} catch (final Exception e) {
 				continue;
 			}
 
 			if (objectType == NODES) {
 				final Node selectedNode = Cytoscape.getCyNode(objectName);
 
 				// Set to the original color
 				if (selectedNode != null) {
 					final NodeView nv = view.getNodeView(selectedNode);
 					if (nv != null)
 						nv.setSelectedPaint(selectedNodeColor);
 				}
 			} else if (objectType == EDGES) {
 				final Edge selectedEdge = this.getEdge(objectName);
 				if (selectedEdge != null) {
 					final EdgeView ev = view.getEdgeView(selectedEdge);
 					if (ev != null)
 						ev.setSelectedPaint(selectedEdgeColor);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method initializes rightClickPopupMenu
 	 *
 	 * @return the inilialised pop-up menu
 	 */
 	public JPopupMenu getPopupMenu() {
 		if (rightClickPopupMenu != null)
 			return rightClickPopupMenu;
 
 		rightClickPopupMenu = new JPopupMenu();
 
 		openFormulaBuilderMenuItem = new JMenuItem("Open Formula Builder");
 		copyMenuItem = new JMenuItem("Copy");
 		copyToCurrentSelectionMenuItem = new JMenuItem("Copy to Current Selection");
 		copyToCurrentSelectionMenuItem.setEnabled(false);
 		copyFormulaToCurrentSelectionMenuItem = new JMenuItem("Copy Formula to Current Selection");
 		copyFormulaToCurrentSelectionMenuItem.setEnabled(false);
 		copyToEntireAttributeMenuItem = new JMenuItem("Copy to Entire Attribute");
 		copyToEntireAttributeMenuItem.setEnabled(false);
 		copyFormulaToEntireAttributeMenuItem = new JMenuItem("Copy Formula to Entire Attribute");
 		copyFormulaToEntireAttributeMenuItem.setEnabled(false);
 		newSelectionMenuItem = new JMenuItem("Select from Table");
 		exportMenu = new JMenu("Export...");
 		exportCellsMenuItem = new JMenuItem("Selected Cells");
 		exportTableMenuItem = new JMenuItem("Entire Table");
 		selectAllMenuItem = new JMenuItem("Select All");
 
 		coloringMenuItem = new JCheckBoxMenuItem("On/Off Coloring");
 
 		final JTable table = this;
 		openFormulaBuilderMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					final int cellRow = table.getSelectedRow();
 					final int cellColumn = table.getSelectedColumn();
 					if (cellRow == -1 || cellColumn == -1 || !tableModel.isCellEditable(cellRow, cellColumn))
 						JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 						                              "Can't enter a formula w/o a selected cell!",
 						                              "Information", JOptionPane.INFORMATION_MESSAGE);
 					else {
 						final String columnName = tableModel.getColumnName(cellColumn);
 						final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
 						initAttribNameToTypeMap(objectType, columnName, attribNameToTypeMap);
 						formulaBuilderDialog =
 							new FormulaBuilderDialog(tableModel, table, objectType, Cytoscape.getDesktop(),
 										 attribNameToTypeMap, columnName);
 						formulaBuilderDialog.setLocationRelativeTo(Cytoscape.getDesktop());
 						formulaBuilderDialog.setVisible(true);
 					}
 				}
 			});
 
 		copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					copyToClipBoard();
 				}
 			});
 
 		copyToCurrentSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					copyToCurrentSelection();
 				}
 			});
 
 		copyFormulaToCurrentSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					copyFormulaToCurrentSelection();
 				}
 			});
 
 		copyToEntireAttributeMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					copyToEntireAttribute();
 				}
 			});
 
 		copyFormulaToEntireAttributeMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					copyFormulaToEntireAttribute();
 				}
 			});
 
 		exportCellsMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					export(false);
 				}
 			});
 
 		exportTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					export(true);
 				}
 			});
 
 		selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					selectAll();
 				}
 			});
 
 		newSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent e) {
 					final int idLocation = getIdColumn();
 					final Map<String, GraphObject> selectedMap = paintNodesAndEdges(idLocation);
 					final CyNetwork curNet = Cytoscape.getCurrentNetwork();
 
 					final List<GraphObject> nonSelectedObjects = new ArrayList<GraphObject>();
 
 					GraphObject fromMap;
 
 					if (objectType == NODES) {
 						for (Object curNode : curNet.getSelectedNodes()) {
 							fromMap = selectedMap.get(((Node) curNode).getIdentifier());
 
 							if (fromMap == null) {
 								nonSelectedObjects.add((GraphObject) curNode);
 							}
 						}
 
 						resetObjectColor(idLocation);
 						curNet.setSelectedNodeState(nonSelectedObjects, false);
 					} else {
 						for (Object curEdge : curNet.getSelectedEdges()) {
 							fromMap = selectedMap.get(((Edge) curEdge).getIdentifier());
 
 							if (fromMap == null) {
 								nonSelectedObjects.add((GraphObject) curEdge);
 							}
 						}
 
 						resetObjectColor(idLocation);
 						curNet.setSelectedEdgeState(nonSelectedObjects, false);
 					}
 
 					if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
 						Cytoscape.getCurrentNetworkView().updateView();
 					}
 				}
 			});
 
 		coloringMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
 						if (coloringMenuItem.isSelected() == true) {
 							logger.debug("color ON");
 							setNewRenderer(true);
 						} else {
 							logger.debug("color OFF");
 							setNewRenderer(false);
 						}
 					}
 				}
 			});
 
 		exportMenu.add(exportCellsMenuItem);
 		exportMenu.add(exportTableMenuItem);
 
 		if (objectType != NETWORK)
 			rightClickPopupMenu.add(newSelectionMenuItem);
 
 		rightClickPopupMenu.add(openFormulaBuilderMenuItem);
 		rightClickPopupMenu.add(copyMenuItem);
 		rightClickPopupMenu.add(copyToCurrentSelectionMenuItem);
 		rightClickPopupMenu.add(copyFormulaToCurrentSelectionMenuItem);
 		rightClickPopupMenu.add(copyToEntireAttributeMenuItem);
 		rightClickPopupMenu.add(copyFormulaToEntireAttributeMenuItem);
 		rightClickPopupMenu.add(selectAllMenuItem);
 		rightClickPopupMenu.add(exportMenu);
 
 		if (objectType != NETWORK) {
 			rightClickPopupMenu.addSeparator();
 			rightClickPopupMenu.add(coloringMenuItem);
 		}
 
 		return rightClickPopupMenu;
 	}
 
 	private JPopupMenu getHeaderPopupMenu() {
 		if (objectType == NETWORK)
 			return null;
 
 		if (rightClickHeaderPopupMenu != null)
 			return rightClickHeaderPopupMenu;
 
 		rightClickHeaderPopupMenu = new JPopupMenu();
 		final JMenuItem openFormulaBuilderMenuItem = new JMenuItem("Open Formula Builder");
 		final TableColumnModel columnModel = getColumnModel();
 		final JTable table = this;
 		openFormulaBuilderMenuItem.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(final ActionEvent event) {
 					final int cellColumn = columnModel.getColumnIndexAtX(mouseX);
 					final String columnName = tableModel.getColumnName(cellColumn);
 					final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
 					initAttribNameToTypeMap(objectType, columnName, attribNameToTypeMap);
 					formulaBuilderDialog =
 						new FormulaBuilderDialog(tableModel, table, objectType, Cytoscape.getDesktop(),
 									 attribNameToTypeMap, columnName);
 					formulaBuilderDialog.setLocationRelativeTo(Cytoscape.getDesktop());
 					formulaBuilderDialog.setVisible(true);
 				}
 			});
 		rightClickHeaderPopupMenu.add(openFormulaBuilderMenuItem);
 
 		return rightClickHeaderPopupMenu;
 	}
 
 	private void setNewRenderer(boolean colorSwitch) {
 		this.setDefaultRenderer(Object.class, new BrowserTableCellRenderer(colorSwitch, objectType));
 		this.repaint();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param all DOCUMENT ME!
 	 */
 	public void export(final boolean all) {
 		// Do this in the GUI Event Dispatch thread...
 		SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					final String name;
 
 					try {
 						name = FileUtil.getFile("Export Table", FileUtil.SAVE,
 						                        new CyFileFilter[] {  }).toString();
 					} catch (Exception exp) {
 						// this is because the selection was canceled
 						return;
 					}
 
 					String export = exportTable(all);
 					export = export.replace("[", "\"");
 					export = export.replace("]", "\"");
 
 					try {
 						final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(name)));
 						writer.write(export);
 						writer.close();
 						export = null;
 					} catch (Exception ex) {
 						logger.error("Table Export Write error",ex);
 					}
 				}
 			});
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String exportTable() {
 		return exportTable("\t", LS, false);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param all DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String exportTable(boolean all) {
 		return exportTable("\t", LS, all);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param element_delim DOCUMENT ME!
 	 * @param eol_delim DOCUMENT ME!
 	 * @param all DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String exportTable(final String element_delim, final String eol_delim, boolean all) {
 		if (all == true) {
 			this.selectAll();
 		}
 
 		final int[] selectedCols = this.getSelectedColumns();
 		final StringBuffer buf = new StringBuffer();
 
 		for (int i = 0; i < selectedCols.length; i++) {
 			buf.append(this.getColumnName(selectedCols[i]) + "\t");
 		}
 
 		buf.append(LS);
 
 		return buf.toString() + copyToClipBoard();
 	}
 
 	private void getSelected() {
 		int selectedCol = this.getSelectedColumn();
 		this.getSelectedRows();
 	}
 
 	protected void initSortHeader() {
 		JTableHeader header = getTableHeader();
 		header.setDefaultRenderer(new SortHeaderRenderer());
 		header.addMouseListener(this);
 
 		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
 		//
 		// Event handler. Define actions when mouse is clicked.
 		//
 		addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					final int column = getColumnModel().getColumnIndexAtX(e.getX());
 					final int row = e.getY() / getRowHeight();
 
 					// Bail out if we're dealing w/ network attributes or we are at the ID column:
 					if (objectType == NETWORK || column == getIdColumn())
 
 					// Make sure the column and row we're clicking on actually exists!
 					if (column >= tableModel.getColumnCount() || row >= tableModel.getRowCount())
 						return;
 
 					ValidatedObjectAndEditString objectAndEditString = null;
 					try {
 						objectAndEditString = (ValidatedObjectAndEditString)getValueAt(row, column);
 					}
 					catch (Exception ex){
 						// If user clicks outside the table, just ignore
 						return;
 					}
 					
 					getSelected();
 
 					// If action is right click, then show edit pop-up menu
 					if ((SwingUtilities.isRightMouseButton(e)) || (isMacPlatform() && e.isControlDown())){
 						if (objectAndEditString != null) {
 							// Remove the last menuItem
 							rightClickPopupMenu.remove(rightClickPopupMenu.getComponentCount() - 1);
 
 							final Object validatedObject = objectAndEditString.getValidatedObject();
 							if (validatedObject != null){	
 
 								// If there is already an menuItem "Open this URL in a browser", remove it
 								for (int i=0; i<rightClickPopupMenu.getComponentCount(); i++){
 									Object obj = rightClickPopupMenu.getComponent(i);
 									if (obj instanceof JMenuItem){
 										JMenuItem menuItem = (JMenuItem) obj;
 										if (menuItem.getText().equalsIgnoreCase("<html>Open this URL in a browser</html>")){
 											rightClickPopupMenu.remove(i);
 											break;
 										}
 									}	
 								}
 								
 								// Check if this is a URL
 								URL url = null;
 								try {
									url = new URL((String)objectAndEditString.getValidatedObject());
 								} catch (final MalformedURLException e1) {
 									// If invalid, just ignore.
 								}
 								
 								if (url !=null){
 									// It is a URL, add a menuItem 'Open this URL in a browser'
 									rightClickPopupMenu.add(new OpenNewBrowser(url));						
 								}
 								
 								// Add last menuItem to right-click menu
 								rightClickPopupMenu.add(new HyperLinkOut(validatedObject.toString(), linkoutProps));
 							}
 							rightClickPopupMenu.show(e.getComponent(), e.getX(), e.getY());
 							
 						}
 					} else if (SwingUtilities.isLeftMouseButton(e) && (getSelectedRows().length != 0)) {
 						
 						showListContents(e);
 
 						if (row >= getRowCount() || row < 0 || column >= getColumnCount() || column < 0)
 							return;
 					}
 				} // mouseClicked
 				
 				
 				public void mouseReleased(MouseEvent e) {
 					// When the mouse is released, fire signal to pass the selected
 					// objects in the table.
 					// Get selected object names
 					final int[] rowsSelected = getSelectedRows();
 
 					if (rowsSelected.length == 0) {
 						return;
 					}
 
 					final int columnCount = getColumnCount();
 					int idLocation = 0;
 
 					// First, find the location of the ID column
 					for (int idx = 0; idx < columnCount; idx++) {
 						if (getColumnName(idx).equals(AttributeBrowser.ID)) {
 							idLocation = idx;
 
 							break;
 						}
 					}
 
 					// Initialize internal selection table
 					((DataTableModel) dataModel).resetSelectionFlags();
 
 					setSelectedColor(SELECTED_NODE);
 					setSelectedColor(REV_SELECTED_NODE);
 					setSelectedColor(SELECTED_EDGE);
 					setSelectedColor(REV_SELECTED_EDGE);
 
 					resetObjectColor(idLocation);
 					paintNodesAndEdges(idLocation);
 
 					if (Cytoscape.getCurrentNetworkView() != Cytoscape.getNullNetworkView()) {
 						Cytoscape.getCurrentNetworkView().updateView();
 					}
 				}
 			});
 	}
 
 	class OpenNewBrowser extends JMenuItem implements ActionListener{
 		URL url;
 		public OpenNewBrowser(URL url){
 			this.url=url;
 			this.setText("<html>Open this URL in a browser</html>");
 			this.addActionListener(this);
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			cytoscape.util.OpenBrowser.openURL(url.toString());
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public int getSortedColumnIndex() {
 		return sortedColumnIndex;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean isSortedColumnAscending() {
 		return sortedColumnAscending;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param event DOCUMENT ME!
 	 */
 	public void mouseReleased(MouseEvent event) {
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param event DOCUMENT ME!
 	 */
 	public void mousePressed(MouseEvent event) {
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param event DOCUMENT ME!
 	 */
 	public void mouseClicked(MouseEvent event) {
 		final int cursorType = getTableHeader().getCursor().getType();
 		if ((event.getButton() == MouseEvent.BUTTON1) && (cursorType != Cursor.E_RESIZE_CURSOR)
 		    && (cursorType != Cursor.W_RESIZE_CURSOR)) {
 			final int index = getColumnModel().getColumnIndexAtX(event.getX());
 
 			if (index >= 0) {
 				final int modelIndex = getColumnModel().getColumn(index).getModelIndex();
 
 				final SortTableModel model = (SortTableModel) getModel();
 
 				if (model.isSortable(modelIndex)) {
 					// toggle ascension, if already sorted
 					if (sortedColumnIndex == index) {
 						sortedColumnAscending = !sortedColumnAscending;
 					}
 
 					sortedColumnIndex = index;
 
 					model.sortColumn(modelIndex, sortedColumnAscending);
 				}
 			}
 		}
 		else if (event.getButton() == MouseEvent.BUTTON3) {
 			// Don't do anything if we're displaying network attributes:
 			if (objectType == NETWORK)
 				return;
 
 			final int column = getColumnModel().getColumnIndexAtX(event.getX());
 
 			// Make sure the column we're clicking on actually exists!
 			if (column >= tableModel.getColumnCount() || column < 0)
 				return;
 
 			// Ignore clicks on the ID column:
 			if (column == getIdColumn())
 				return;
 
 			mouseX = event.getX();
 			rightClickHeaderPopupMenu.show(event.getComponent(), event.getX(), event.getY());
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param event DOCUMENT ME!
 	 */
 	public void mouseEntered(MouseEvent event) {
 		
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param event DOCUMENT ME!
 	 */
 	public void mouseExited(MouseEvent event) {
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param event DOCUMENT ME!
 	 */
 	public void actionPerformed(ActionEvent event) {
 		// TODO Auto-generated method stub
 		if (event.getActionCommand().compareTo("Copy") == 0) {
 			logger.debug("Cells copied to clipboard.");
 			copyToClipBoard();
 		}
 	}
 
 	// Display elements in te list & map objects
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void showListContents(MouseEvent e) {
 		int column = 0;
 		int row = 0;
 		row = this.getSelectedRow();
 		column = this.getSelectedColumn();
 
 		DataTableModel model = (DataTableModel) tableModel;
 
 		// List tester = new ArrayList();
 		Class tester = model.getObjectTypeAt(this.getColumnName(column));
 
 		Object value = model.getValueAt(row, column);
 
 		if ((tester != null) && tester.equals(List.class)) {
 			int idCol = 0;
 
 			for (int i = 0; i < this.getColumnCount(); i++) {
 				if (this.getColumnName(i).equals(AttributeBrowser.ID)) {
 					idCol = i;
 
 					break;
 				}
 			}
 
 			final String idField = (String) ((ValidatedObjectAndEditString)this.getValueAt(row, idCol)).getValidatedObject();
 			List contents = (List) model.getValidatedObjectAndEditString(CyAttributes.TYPE_SIMPLE_LIST, idField,
 										     this.getColumnName(column)).getValidatedObject();
 			cellMenu = new JPopupMenu();
 
 			Object[] listItems = contents.toArray();
 
 			if (listItems.length != 0)
 				getCellContentView(CyAttributes.TYPE_SIMPLE_LIST, listItems, idField, e);
 		} else if ((value != null) && (value instanceof Map)
 		           && model.getValueAt(row, 0).equals(AttributeBrowser.NETWORK_METADATA)) {
 			NetworkMetaDataDialog mdd = new NetworkMetaDataDialog(Cytoscape.getDesktop(), false,
 			                                                      Cytoscape.getCurrentNetwork());
 			mdd.setLocationRelativeTo(Cytoscape.getDesktop());
 			mdd.setVisible(true);
 		} else if ((tester != null) && tester.equals(Map.class)) {
 			int idCol = 0;
 
 			for (int i = 0; i < this.getColumnCount(); i++) {
 				if (this.getColumnName(i).equals(AttributeBrowser.ID)) {
 					idCol = i;
 
 					break;
 				}
 			}
 
 			String idField = (String) this.getValueAt(row, idCol);
 
 			Map<String, Object> contents = (Map) model.getValidatedObjectAndEditString(CyAttributes.TYPE_SIMPLE_MAP,
 												   idField,
 												   this.getColumnName(column)).getValidatedObject();
 
 			if ((contents != null) && (contents.size() != 0)) {
 				Object[] listItems = new Object[contents.size()];
 				Object[] keySet = contents.keySet().toArray();
 
 				for (int i = 0; i < contents.keySet().size(); i++) {
 					// System.out.println("Key = " + key + ", Val = " +
 					// contents.get(key));
 					listItems[i] = keySet[i] + " = " + contents.get(keySet[i]);
 				}
 
 				cellMenu = new JPopupMenu();
 				getCellContentView(CyAttributes.TYPE_SIMPLE_MAP, listItems, idField, e);
 			}
 		}
 	}
 
 	private void getCellContentView(final Byte type, final Object[] listItems, final String idField,
 	                                final MouseEvent e)
 	{
 		final int column = getColumnModel().getColumnIndexAtX(e.getX());
 		final int row = e.getY() / getRowHeight();
 
 		JMenu curItem = null;
 
 		String dispName;
 
 		for (final Object item : listItems) {
 			dispName = item.toString();
 
 			if (dispName.length() > 60) {
 				dispName = dispName.substring(0, 59) + " ...";
 			}
 
 			curItem = new JMenu(dispName);
 			curItem.setBackground(Color.white);
 			curItem.add(getPopupMenu());
 
 			JMenuItem openURL = new JMenuItem("Open as hyper link...");
 
 			openURL.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						logger.debug(item.toString());
 						OpenBrowser.openURL(item.toString());
 					}
 				});
 			curItem.add(openURL);
 
 			JMenuItem copyAll = new JMenuItem("Copy all");
 			copyAll.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						StringBuilder builder = new StringBuilder();
 
 						for (Object oneEntry : listItems) {
 							builder.append(oneEntry.toString() + "\t");
 						}
 
 						stsel = new StringSelection(builder.toString());
 						systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 						systemClipboard.setContents(stsel, stsel);
 					}
 				});
 			curItem.add(copyAll);
 
 			JMenuItem copy = new JMenuItem("Copy one entry");
 			copy.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						stsel = new StringSelection(item.toString());
 						systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 						systemClipboard.setContents(stsel, stsel);
 					}
 				});
 			curItem.add(copy);
 
 			if (item.toString().startsWith("http://")) {				
 				curItem.getMenuComponent(1).setEnabled(true);
 			} else
 				curItem.getMenuComponent(1).setEnabled(false);
 
 			curItem.getMenuComponent(2).setEnabled(true);
 
 			if (type == CyAttributes.TYPE_SIMPLE_LIST) {
 				curItem.add(new HyperLinkOut(item.toString(), linkoutProps));
 			} else {
 				curItem.add(new HyperLinkOut(item.toString().split("=")[1], linkoutProps));
 			}
 
 			cellMenu.add(curItem);
 		}
 
 		final Border popupBorder = BorderFactory.createTitledBorder(null, idField,
 		                                                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                            BORDER_FONT, Color.BLUE);
 		cellMenu.setBorder(popupBorder);
 		cellMenu.setBackground(Color.WHITE);
 		cellMenu.show(e.getComponent(), e.getX(), e.getY());
 	}
 
 	private String copyToClipBoard() {
 		final StringBuffer sbf = new StringBuffer();
 
 		/*
 		 * Check to ensure we have selected only a contiguous block of cells.
 		 */
 		final int numcols = this.getSelectedColumnCount();
 		final int numrows = this.getSelectedRowCount();
 
 		final int[] rowsselected = this.getSelectedRows();
 		final int[] colsselected = this.getSelectedColumns();
 
 		// Return if no cell is selected.
 		if (numcols == 0 && numrows == 0)
 			return null;
 
 		if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0]
 		       && numrows == rowsselected.length)
 		      && (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
 			  && numcols == colsselected.length))) {
 			JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Invalid Copy Selection",
 			                              "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
 
 			return null;
 		}
 
 		for (int i = 0; i < numrows; i++) {
 			for (int j = 0; j < numcols; j++) {
 				final Object cellValue = this.getValueAt(rowsselected[i], colsselected[j]);
 				if (cellValue == null)
 					continue;
 
 				final String cellText = ((ValidatedObjectAndEditString)cellValue).getEditString();
 				sbf.append(cellText);
 
 				if (j < (numcols - 1))
 					sbf.append("\t");
 			}
 
 			sbf.append(LS);
 		}
 
 		stsel = new StringSelection(sbf.toString());
 		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		systemClipboard.setContents(stsel, stsel);
 
 		return sbf.toString();
 	}
 
 	private void copyToCurrentSelection() {
 		final int tableRow = this.getSelectedRow();
 		final int tableColumn = this.getSelectedColumn();
 		if (tableRow == -1 || tableColumn == -1)
 			return;
 
 		final String attribName = tableModel.getColumnName(tableColumn);
 		final String rowId = tableModel.getRowId(tableRow);
 		final CyAttributes attribs = tableModel.getCyAttributes();
 
 		// Check for copying of an empty cell:
 		if (attribs.getAttribute(rowId, attribName) == null) {
 			if (JOptionPane.showConfirmDialog(null, "Are you sure you want to copy an empty value?",
 			                                  "Confirmation", JOptionPane.YES_NO_OPTION)
 			    != JOptionPane.YES_OPTION)
 				return;
 		}
 
 		final List<GraphObject> selectedGraphObjects = tableModel.getObjects();
 		final StringBuilder errorMessage = new StringBuilder();
 		for (final GraphObject graphObject : selectedGraphObjects)
 			CyAttributesUtils.copyAttribute(attribs, rowId, graphObject.getIdentifier(),
 							attribName, /* copyEquation = */ false, errorMessage);
 		tableModel.updateColumn(attribs.getAttribute(rowId, attribName), tableColumn, tableRow);
 	}
 
 	private void copyFormulaToCurrentSelection() {
 		final int tableRow = this.getSelectedRow();
 		final int tableColumn = this.getSelectedColumn();
 		if (tableRow == -1 || tableColumn == -1)
 			return;
 
 		final String attribName = tableModel.getColumnName(tableColumn);
 		final String rowId = tableModel.getRowId(tableRow);
 		final CyAttributes attribs = tableModel.getCyAttributes();
 
 		// Check for copying of an empty cell:
 		if (attribs.getEquation(rowId, attribName) == null) {
 			if (JOptionPane.showConfirmDialog(null, "Are you sure you want to copy an empty equation?",
 			                                  "Confirmation", JOptionPane.YES_NO_OPTION)
 			    != JOptionPane.YES_OPTION)
 				return;
 		}
 
 		final List<GraphObject> selectedGraphObjects = tableModel.getObjects();
 		final StringBuilder errorMessage = new StringBuilder();
 		for (final GraphObject graphObject : selectedGraphObjects)
 			CyAttributesUtils.copyAttribute(attribs, rowId, graphObject.getIdentifier(),
 							attribName, /* copyEquation = */ true, errorMessage);
 		final Equation equation = attribs.getEquation(rowId, attribName);
 		tableModel.updateColumn(equation != null ? equation : attribs.getAttribute(rowId, attribName),
 		                        tableColumn, tableRow);
 	}
 
 	private void copyToEntireAttribute() {
 		final int tableRow = this.getSelectedRow();
 		final int tableColumn = this.getSelectedColumn();
 		if (tableRow == -1 || tableColumn == -1)
 			return;
 
 		final String attribName = tableModel.getColumnName(tableColumn);
 		final String rowId = tableModel.getRowId(tableRow);
 		final CyAttributes attribs = tableModel.getCyAttributes();
 
 		// Check for copying of an empty cell:
 		if (attribs.getAttribute(rowId, attribName) == null) {
 			if (JOptionPane.showConfirmDialog(null, "Are you sure you want to copy an empty value?",
 			                                  "Confirmation", JOptionPane.YES_NO_OPTION)
 			    != JOptionPane.YES_OPTION)
 				return;
 		}
 
 		final Iterable<String> ids = objectType.getAssociatedIdentifiers();
 		final StringBuilder errorMessage = new StringBuilder();
 		for (final String id : ids)
 			CyAttributesUtils.copyAttribute(attribs, rowId, id, attribName,
 			                                /* copyEquations = */false, errorMessage);
 
 
 		tableModel.updateColumn(attribs.getAttribute(rowId, attribName), tableColumn, tableRow);
 	}
 
 	private void copyFormulaToEntireAttribute() {
 		final int tableRow = this.getSelectedRow();
 		final int tableColumn = this.getSelectedColumn();
 		if (tableRow == -1 || tableColumn == -1)
 			return;
 
 		final String attribName = tableModel.getColumnName(tableColumn);
 		final String rowId = tableModel.getRowId(tableRow);
 		final CyAttributes attribs = tableModel.getCyAttributes();
 
 		// Check for copying of an empty cell:
 		if (attribs.getEquation(rowId, attribName) == null) {
 			if (JOptionPane.showConfirmDialog(null, "Are you sure you want to copy an empty equation?",
 			                                  "Confirmation", JOptionPane.YES_NO_OPTION)
 			    != JOptionPane.YES_OPTION)
 				return;
 		}
 
 		final Iterable<String> ids = objectType.getAssociatedIdentifiers();
 		final StringBuilder errorMessage = new StringBuilder();
 		for (final String id : ids)
 			CyAttributesUtils.copyAttribute(attribs, rowId, id, attribName,
 			                                /* copyEquations = */true, errorMessage);
 		final Equation equation = attribs.getEquation(rowId, attribName);
 		tableModel.updateColumn(equation != null ? equation : attribs.getAttribute(rowId, attribName),
 		                        tableColumn, tableRow);
 	}
 
 	private void adjustColWidth() {
 		final HashMap<String, Integer> widthMap = ColumnResizer.getColumnPreferredWidths(this);
 		
 		// Save the width if it does not exist
 		Iterator<String> it = widthMap.keySet().iterator();
 		while (it.hasNext()) {
 			String key = it.next();
 			
 			// make exception for the first column (ID), save it only when the table is not empty
 			if (this.getModel().getRowCount() == 0 && key.equalsIgnoreCase("ID"))
 				continue;
 			
 			if (!this.columnWidthMap.containsKey(key))
 				this.columnWidthMap.put(key, widthMap.get(key));
 		}
 		
 		// adjust column width
 		for (int i = 0; i < this.getColumnCount(); i++) {
 			TableColumn col = this.getColumnModel().getColumn(i);
 			if (this.columnWidthMap.containsKey(this.getColumnName(i)))
 				col.setPreferredWidth(this.columnWidthMap.get(this.getColumnName(i)).intValue());				
 		}
 	}
 	
 	public HashMap<String, Integer> getColumnWidthMap() {
 		return this.columnWidthMap;
 	}
 	
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getPropertyName().equals(Integer.toString(Cytoscape.SESSION_OPENED)))
 			ignore = true;
 		else if (e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED) || e.getPropertyName().equals(Cytoscape.SESSION_LOADED))
 			ignore = false;
 		
 		// Ignore all signal if this flag is on.
 		if (ignore)
 			return;
 		
 		
 		if (e.getPropertyName().equals(AttributeBrowser.RESTORE_COLUMN)
 		    && e.getNewValue() != null && e.getNewValue().equals(objectType))
 		{
 			this.adjustColWidth();
 			return;
 		}
 		
 		if (e.getPropertyName().equals(AttributeBrowser.CLEAR_INTERNAL_SELECTION)) {
 			if (e.getNewValue() != null && e.getNewValue().equals(objectType)) {
 				getSelectionModel().clearSelection();
 				Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 			}
 		}
 
 		if (e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
 			Cytoscape.getDesktop().getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 			AttributeBrowser.getPropertyChangeSupport().addPropertyChangeListener(this);
 		}
 
 		if (e.getPropertyName().equals(Cytoscape.NETWORK_CREATED)
 		    || e.getPropertyName().equals(Cytoscape.NETWORK_DESTROYED))
 			tableModel.setTableData();
 
 		if ((e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_FOCUS)
 		    || e.getPropertyName().equals(Cytoscape.SESSION_LOADED)
 		    || e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
 			if (currentNetwork != null)
 				currentNetwork.removeSelectEventListener(this);
 
 			// Change the target network
 			currentNetwork = Cytoscape.getCurrentNetwork();
 
 			if (currentNetwork != null) {
 				currentNetwork.addSelectEventListener(this);
 
 				if (objectType == NODES) {
 					tableModel.setTableData(new ArrayList(Cytoscape.getCurrentNetwork()
 					                                               .getSelectedNodes()), null);
 				} else if (objectType == EDGES) {
 					tableModel.setTableData(new ArrayList(Cytoscape.getCurrentNetwork()
 					                                               .getSelectedEdges()), null);
 				} else {
 					// Network Attribute
 					tableModel.setTableData(null, null);
 					adjustColWidth();
 				}
 			}
 
 			setSelectedColor(SELECTED_NODE);
 			setSelectedColor(REV_SELECTED_NODE);
 			setSelectedColor(SELECTED_EDGE);
 			setSelectedColor(REV_SELECTED_EDGE);
 		}
 
 		// If initialized, check linkout props.
 		if (e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
 			// First, load existing property
 			Properties props = CytoscapeInit.getProperties();
 
 			// Use reflection to get resource 
 			Class linkout = null;
 
 			try {
 				linkout = Class.forName("linkout.LinkOut");
 			} catch (ClassNotFoundException e1) {
 				logger.error("Could't create LinkOut class",e1);
 
 				return;
 			}
 
 			final ClassLoader cl = linkout.getClassLoader();
 
 			try {
 				props.load(cl.getResource("linkout.props").openStream());
 			} catch (IOException e1) {
 				logger.warn("Could't read linkout.props",e1);
 			}
 
 			linkoutProps = new HashMap<String, Map<String, String>>();
 
 			final String nodeLink = "nodelinkouturl";
 
 			String[] parts = null;
 
 			for (Entry<Object, Object> entry : props.entrySet()) {
 				Map<String, String> pair = null;
 
 				if (entry.getKey().toString().startsWith(nodeLink)) {
 					parts = entry.getKey().toString().split("\\.");
 
 					if (parts.length == 3) {
 						pair = linkoutProps.get(parts[1]);
 
 						if (pair == null) {
 							pair = new HashMap<String, String>();
 							linkoutProps.put(parts[1], pair);
 						}
 
 						pair.put(parts[2], entry.getValue().toString());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param arg0 DOCUMENT ME!
 	 */
 	public void onSelectEvent(SelectEvent event) {
 		if(this.objectType.equals(NETWORK))
 			return;
 		
 		if ((objectType == NODES)
 		    && ((event.getTargetType() == SelectEvent.SINGLE_NODE)
 		       || (event.getTargetType() == SelectEvent.NODE_SET))) {
 						
 			// node selection
 			tableModel.setSelectedColor(CyAttributeBrowserTable.SELECTED_NODE);
 			tableModel.setSelectedColor(CyAttributeBrowserTable.REV_SELECTED_NODE);
 
 			tableModel.setTableData(new ArrayList<GraphObject>(Cytoscape.getCurrentNetwork()
 			                                                            .getSelectedNodes()), null);
 		} else if ((objectType == EDGES)
 		           && ((event.getTargetType() == SelectEvent.SINGLE_EDGE)
 		               || (event.getTargetType() == SelectEvent.EDGE_SET)))
 		{
 			// edge selection
 			tableModel.setSelectedColor(CyAttributeBrowserTable.SELECTED_EDGE);
 			tableModel.setSelectedColor(CyAttributeBrowserTable.REV_SELECTED_EDGE);
 			tableModel.setTableData(new ArrayList<GraphObject>(Cytoscape.getCurrentNetwork()
 			                                                            .getSelectedEdges()), null);
 		}
 
 		this.adjustColWidth();
 		//ColumnResizer.adjustColumnPreferredWidths(this);
 		
 		setSelectedColor(SELECTED_NODE);
 		setSelectedColor(REV_SELECTED_NODE);
 		setSelectedColor(SELECTED_EDGE);
 		setSelectedColor(REV_SELECTED_EDGE);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void editingStopped(ChangeEvent e) {
 		super.editingStopped(e);
 		Cytoscape.getVisualMappingManager().getNetworkView().redrawGraph(false, true);
 
 		final int currentRow = getEditingRow();
 		final int currentColumn = getEditingColumn();
 
 		int nextRow, nextColumn;
 
 		final int rowCount = getRowCount();
 		if (currentRow < getRowCount() - 1) {
 			nextRow = currentRow + 1;
 			nextColumn = currentColumn;
 		} else {
 			nextRow = 0;
 
 			// First, find the location of the ID column
 			int idColumn = -1;
 			final int columnCount = getColumnCount();
 			for (int idx = 0; idx < columnCount; idx++) {
 				if (getColumnName(idx).equals(AttributeBrowser.ID)) {
 					idColumn = idx;
 					break;
 				}
 			}
 
 			nextColumn = currentColumn + 1;
 			if (nextColumn == idColumn)
 				++nextColumn;
 			if (nextColumn == columnCount)
 				nextColumn = 0;
 			if (nextColumn == idColumn)
 				++nextColumn;
 		}
 
 		changeSelection(nextRow, nextColumn, false, false);
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		// save the column width, if user adjust column width manually
 		if (e.getSource() instanceof JTableHeader){
 			final int index = getColumnModel().getColumnIndexAtX(e.getX());
 			if (index != -1){
 				int colWidth = getColumnModel().getColumn(index).getWidth();
 				this.columnWidthMap.put(this.getColumnName(index), new Integer(colWidth));				
 			}
 		}
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		
 	}
 
 	// =========================================================================================
 	// BEGIN special cell editing code... 
 	// 
 	public boolean editCellAt(int row, int column, EventObject e) {
 		if ((cellEditor != null) && !cellEditor.stopCellEditing()) {
 			return false;
 		}
 
 		if ((row < 0) || (row >= getRowCount()) || (column < 0) || (column >= getColumnCount())) {
 			return false;
 		}
 
 		if (!isCellEditable(row, column))
 			return false;
 
 		if (editorRemover == null) {
 			KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 			editorRemover = new CellEditorRemover(fm);
 			fm.addPropertyChangeListener("permanentFocusOwner", editorRemover);
 		}
 
 		TableCellEditor editor = getCellEditor(row, column);
 
 		if ((editor != null) && editor.isCellEditable(e)) {
 
 			// Do this first so that the bounds of the JTextArea editor
 			// will be correct.
 			setEditingRow(row);
 			setEditingColumn(column);
 			setCellEditor(editor);
 			editor.addCellEditorListener(this);
 
 			editorComp = prepareEditor(editor, row, column);
 
 			if (editorComp == null) {
 				removeEditor();
 				return false;
 			}
 
 			Rectangle cellRect = getCellRect(row, column, false);
 
 			if (editor instanceof MultiLineTableCellEditor) {
 				Dimension prefSize = editorComp.getPreferredSize();
 				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS,
 				                                            Boolean.TRUE);
 				editorComp.setBounds(cellRect.x, cellRect.y,
 				                     Math.max(cellRect.width, prefSize.width),
 				                     Math.max(cellRect.height, prefSize.height));
 				((JComponent) editorComp).putClientProperty(MultiLineTableCellEditor.UPDATE_BOUNDS,
 				                                            Boolean.FALSE);
 			} else
 				editorComp.setBounds(cellRect);
 
 			add(editorComp);
 			editorComp.validate();
 
 			return true;
 		}
 
 		return false;
 	}
 
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		if (isEditing()) {
 			Component component = getEditorComponent();
 			component.repaint();
 		}
 	}
 
 	public void removeNotify() {
 		KeyboardFocusManager.getCurrentKeyboardFocusManager()
 		                    .removePropertyChangeListener("permanentFocusOwner", editorRemover);
 		editorRemover = null;
 		super.removeNotify();
 	}
 
 	public void removeEditor() {
 		KeyboardFocusManager.getCurrentKeyboardFocusManager()
 		                    .removePropertyChangeListener("permanentFocusOwner", editorRemover);
 		editorRemover = null;
 
 		TableCellEditor editor = getCellEditor();
 
 		if (editor != null) {
 			editor.removeCellEditorListener(this);
 
 			Rectangle cellRect = getCellRect(editingRow, editingColumn, false);
 
 			if (editorComp != null) {
 				cellRect = cellRect.union(editorComp.getBounds());
 				remove(editorComp);
 			}
 
 			setCellEditor(null);
 			editorComp = null;
 			repaint(cellRect);
 		}
 	}
 
 	public boolean getScrollableTracksViewportHeight() {
 		if (getParent() instanceof JViewport)
 			return getParent().getHeight() > getPreferredSize().height;
 		else
 			return false;
 	}
 
 	private PropertyChangeListener editorRemover = null;
 
 	private class CellEditorRemover implements PropertyChangeListener {
 		KeyboardFocusManager focusManager;
 
 		public CellEditorRemover(KeyboardFocusManager fm) {
 			this.focusManager = fm;
 		}
 
 		public void propertyChange(PropertyChangeEvent ev) {
 			if (!isEditing() || (getClientProperty("terminateEditOnFocusLost") != Boolean.TRUE)) {
 				return;
 			}
 
 			Component c = focusManager.getPermanentFocusOwner();
 
 			while (c != null) {
 				if (c == CyAttributeBrowserTable.this) {
 					// focus remains inside the table
 					return;
 				} else if (c instanceof Window) {
 					if (c == SwingUtilities.getRoot(CyAttributeBrowserTable.this)) {
 						if (!getCellEditor().stopCellEditing()) {
 							getCellEditor().cancelCellEditing();
 						}
 					}
 
 					break;
 				}
 
 				c = c.getParent();
 			}
 		}
 	}
 
 	private static void initAttribNameToTypeMap(final DataObjectType objectType, final String columnName,
 	                                            final Map<String, Class> attribNameToTypeMap)
 	{
 		final CyAttributes cyAttribs;
 		switch (objectType) {
 		case NODES:
 			cyAttribs = Cytoscape.getNodeAttributes();
 			break;
 		case EDGES:
 			cyAttribs = Cytoscape.getEdgeAttributes();
 			break;
 		case NETWORK:
 			cyAttribs = Cytoscape.getNetworkAttributes();
 			break;
 		default:
 			throw new IllegalStateException("unknown DataObjectType: " + objectType + "!");
 		}
 
 		Util.initAttribNameToTypeMap(cyAttribs, columnName, attribNameToTypeMap);
 	}
 
 	/**
 	 *  @returns the column index of the ID column or -1 if there is no ID column
 	 */
 	private int getIdColumn() {
 		final int columnCount = getColumnCount();
 		for (int idx = 0; idx < columnCount; idx++) {
 			if (getColumnName(idx).equals(AttributeBrowser.ID))
 				return idx;
 		}
 
 		return -1;
 	}
 }
