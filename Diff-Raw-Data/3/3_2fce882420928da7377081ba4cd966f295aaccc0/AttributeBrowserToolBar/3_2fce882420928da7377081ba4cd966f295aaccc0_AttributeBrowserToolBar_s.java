 /*
  Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)
 
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
 package browser.ui;
 
 
 import static browser.DataObjectType.EDGES;
 import static browser.DataObjectType.NETWORK;
 import static browser.DataObjectType.NODES;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JToolBar;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 
 import org.jdesktop.layout.GroupLayout;
 import org.jdesktop.layout.LayoutStyle;
 
 import browser.AttributeBrowser;
 import browser.AttributeModel;
 import browser.DataObjectType;
 import browser.DataTableModel;
 import browser.ValidatedObjectAndEditString;
 import cytoscape.Cytoscape;
 import cytoscape.actions.ImportEdgeAttributesAction;
 import cytoscape.actions.ImportExpressionMatrixAction;
 import cytoscape.actions.ImportNodeAttributesAction;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesUtils;
 import cytoscape.dialogs.NetworkMetaDataDialog;
 import cytoscape.logger.CyLogger;
 import cytoscape.util.swing.CheckBoxJList;
 
 
 /**
  *  Define toolbar for Attribute Browser.
  */
 public class AttributeBrowserToolBar extends JPanel implements PopupMenuListener, PropertyChangeListener {
 	private static final long serialVersionUID = -508393701912596399L;
 	
 	private final CyAttributes attributes;
 	private DataTableModel tableModel;
 	private final JTable table;
 	private final DataObjectType objectType;
 	private AttributeModel attrModel;
 	private String attributeType = null;
 	private CyLogger logger = null;
 	private List<String> orderedCol;
 
 	/**
 	 *  GUI components
 	 */
 	private JPopupMenu attributeSelectionPopupMenu = null;
 	private JScrollPane jScrollPane = null;
 	private JPopupMenu jPopupMenu1 = null;
 
 	private JMenuItem jMenuItemStringAttribute = null;
 	private JMenuItem jMenuItemIntegerAttribute = null;
 	private JMenuItem jMenuItemFloatingPointAttribute = null;
 	private JMenuItem jMenuItemBooleanAttribute = null;
 
 	private JMenuItem jMenuItemStringListAttribute = null;
 	private JMenuItem jMenuItemIntegerListAttribute = null;
 	private JMenuItem jMenuItemFloatingPointListAttribute = null;
 	private JMenuItem jMenuItemBooleanListAttribute = null;
 
 	private JToolBar browserToolBar = null;
 	private JButton selectButton = null;
 	private CheckBoxJList attributeList = null;
 	private JList attrDeletionList = null;
 	private JButton createNewAttributeButton = null;
 	private JButton deleteAttributeButton = null;
 	private JButton selectAllAttributesButton = null;
 	private JButton unselectAllAttributesButton = null;
 	private JButton matrixButton = null;
 	private JButton importButton = null;
 	
 	private ModDialog modDialog;
 	private FormulaBuilderDialog formulaBuilderDialog;
 
 
 	public AttributeBrowserToolBar(final DataTableModel tableModel, final CyAttributeBrowserTable table,
 	                               final AttributeModel a_model, final List<String> orderedCol,
 	                               final DataObjectType graphObjectType)
 	{
 		super();
 
 		this.tableModel = tableModel;
 		this.table      = table;
 		this.attributes = graphObjectType.getAssociatedAttributes();
 		this.objectType = graphObjectType;
 		this.attrModel  = a_model;
 		this.orderedCol = orderedCol;
 
 		logger = CyLogger.getLogger(AttributeBrowserToolBar.class);
 
 		initializeGUI();
 
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(Cytoscape.NEW_ATTRS_LOADED, this);
 	}
 
 	private void initializeGUI() {
 		this.setLayout(new BorderLayout());
 
 		this.setPreferredSize(new Dimension(210, 32));
 		this.add(getJToolBar(), java.awt.BorderLayout.CENTER);
 
 		getAttributeSelectionPopupMenu();
 		getJPopupMenu1();
 
 		modDialog = new ModDialog(tableModel, objectType, Cytoscape.getDesktop());
 		attrModButton.setVisible(objectType != NETWORK);
 	}
 
 	public void propertyChange(PropertyChangeEvent e) {
 		// This will handle the case for the change of attribute userVisibility
 		if (e.getPropertyName() == Cytoscape.NEW_ATTRS_LOADED && e.getOldValue() == attributes) {
 			final Set<String> newAttrNames = (Set<String>)e.getNewValue();
 			final int MAX_DISPLAY_COUNT = 10;
 			int displayCount = 0;
 			for (final String newAttrName : newAttrNames) {
 				orderedCol.add(newAttrName);
 				if (++displayCount > MAX_DISPLAY_COUNT)
 					break;
 			}
 			tableModel.setTableData(null, orderedCol);
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getToBeDeletedAttribute() {
 		return attrDeletionList.getSelectedValue().toString();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param tableModel DOCUMENT ME!
 	 */
 	public void setTableModel(DataTableModel tableModel) {
 		this.tableModel = tableModel;
 	}
 
 	/**
 	 * This method initializes jPopupMenu
 	 *
 	 * @return javax.swing.JPopupMenu
 	 */
 	private JPopupMenu getAttributeSelectionPopupMenu() {
 		if (attributeSelectionPopupMenu == null) {
 			attributeSelectionPopupMenu = new JPopupMenu();
 			attributeSelectionPopupMenu.add(getJScrollPane());
 			attributeSelectionPopupMenu.addPopupMenuListener(this);
 		}
 
 		return attributeSelectionPopupMenu;
 	}
 
 	/**
 	 * This method initializes jScrollPane
 	 *
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getJScrollPane() {
 		if (jScrollPane == null) {
 			jScrollPane = new JScrollPane();
 			jScrollPane.setPreferredSize(new Dimension(600, 300));
 			jScrollPane.setViewportView(getSelectedAttributeList());
 		}
 
 		return jScrollPane;
 	}
 
 	/**
 	 * This method initializes jPopupMenu1
 	 *
 	 * @return javax.swing.JPopupMenu
 	 */
 	private JPopupMenu getJPopupMenu1() {
 		if (jPopupMenu1 == null) {
 			jPopupMenu1 = new JPopupMenu();
 			jPopupMenu1.add(getJMenuItemIntegerAttribute());
 			jPopupMenu1.add(getJMenuItemStringAttribute());
 			jPopupMenu1.add(getJMenuItemFloatingPointAttribute());
 			jPopupMenu1.add(getJMenuItemBooleanAttribute());
 			jPopupMenu1.add(getJMenuItemIntegerListAttribute());
 			jPopupMenu1.add(getJMenuItemStringListAttribute());
 			jPopupMenu1.add(getJMenuItemFloatingPointListAttribute());
 			jPopupMenu1.add(getJMenuItemBooleanListAttribute());
 		}
 
 		return jPopupMenu1;
 	}
 
 	/**
 	 * This method initializes jMenuItemStringAttribute
 	 *
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getJMenuItemStringAttribute() {
 		if (jMenuItemStringAttribute == null) {
 			jMenuItemStringAttribute = new JMenuItem();
 			jMenuItemStringAttribute.setText("String Attribute");
 			jMenuItemStringAttribute.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						createNewAttribute("String");
 					}
 				});
 		}
 
 		return jMenuItemStringAttribute;
 	}
 
 	/**
 	 * This method initializes jMenuItemIntegerAttribute
 	 *
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getJMenuItemIntegerAttribute() {
 		if (jMenuItemIntegerAttribute == null) {
 			jMenuItemIntegerAttribute = new JMenuItem();
 			jMenuItemIntegerAttribute.setText("Integer Attribute");
 			jMenuItemIntegerAttribute.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						createNewAttribute("Integer");
 					}
 				});
 		}
 
 		return jMenuItemIntegerAttribute;
 	}
 
 	/**
 	 * This method initializes jMenuItemFloatingPointAttribute
 	 *
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getJMenuItemFloatingPointAttribute() {
 		if (jMenuItemFloatingPointAttribute == null) {
 			jMenuItemFloatingPointAttribute = new JMenuItem();
 			jMenuItemFloatingPointAttribute.setText("Floating Point Attribute");
 			jMenuItemFloatingPointAttribute.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						createNewAttribute("Floating Point");
 					}
 				});
 		}
 
 		return jMenuItemFloatingPointAttribute;
 	}
 
 	/**
 	 * This method initializes jMenuItemBooleanAttribute
 	 *
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getJMenuItemBooleanAttribute() {
 		if (jMenuItemBooleanAttribute == null) {
 			jMenuItemBooleanAttribute = new JMenuItem();
 			jMenuItemBooleanAttribute.setText("Boolean Attribute");
 			jMenuItemBooleanAttribute.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						createNewAttribute("Boolean");
 					}
 				});
 		}
 
 		return jMenuItemBooleanAttribute;
 	}
 
 	/**
 	 * This method initializes jMenuItemStringListAttribute
 	 *
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getJMenuItemStringListAttribute() {
 		if (jMenuItemStringListAttribute == null) {
 			jMenuItemStringListAttribute = new JMenuItem();
 			jMenuItemStringListAttribute.setText("String List Attribute");
 			jMenuItemStringListAttribute.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						createNewAttribute("String List");
 					}
 				});
 		}
 
 		return jMenuItemStringListAttribute;
 	}
 
 	/**
 	 * This method initializes jMenuItemIntegerListAttribute
 	 *
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getJMenuItemIntegerListAttribute() {
 		if (jMenuItemIntegerListAttribute == null) {
 			jMenuItemIntegerListAttribute = new JMenuItem();
 			jMenuItemIntegerListAttribute.setText("Integer List Attribute");
 			jMenuItemIntegerListAttribute.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						createNewAttribute("Integer List");
 					}
 				});
 		}
 
 		return jMenuItemIntegerListAttribute;
 	}
 
 	/**
 	 * This method initializes jMenuItemFloatingPointListAttribute
 	 *
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getJMenuItemFloatingPointListAttribute() {
 		if (jMenuItemFloatingPointListAttribute == null) {
 			jMenuItemFloatingPointListAttribute = new JMenuItem();
 			jMenuItemFloatingPointListAttribute.setText("Floating Point List Attribute");
 			jMenuItemFloatingPointListAttribute.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						createNewAttribute("Floating Point List");
 					}
 				});
 		}
 
 		return jMenuItemFloatingPointListAttribute;
 	}
 
 	/**
 	 * This method initializes jMenuItemBooleanListAttribute
 	 *
 	 * @return javax.swing.JMenuItem
 	 */
 	private JMenuItem getJMenuItemBooleanListAttribute() {
 		if (jMenuItemBooleanListAttribute == null) {
 			jMenuItemBooleanListAttribute = new JMenuItem();
 			jMenuItemBooleanListAttribute.setText("Boolean List Attribute");
 			jMenuItemBooleanListAttribute.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent e) {
 						createNewAttribute("Boolean List");
 					}
 				});
 		}
 
 		return jMenuItemBooleanListAttribute;
 	}
 
 	/**
 	 * This method initializes jToolBar
 	 *
 	 * @return javax.swing.JToolBar
 	 */
 	private JToolBar getJToolBar() {
 		if (browserToolBar == null) {
 			browserToolBar = new JToolBar();
 			browserToolBar.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					AttributeBrowser.getPropertyChangeSupport().firePropertyChange(AttributeBrowser.CLEAR_INTERNAL_SELECTION, null, objectType);
 				}
 			});
 			browserToolBar.setMargin(new java.awt.Insets(0, 0, 3, 0));
 			browserToolBar.setPreferredSize(new Dimension(200, 30));
 			browserToolBar.setFloatable(false);
 			browserToolBar.setOrientation(JToolBar.HORIZONTAL);
 
 			final GroupLayout buttonBarLayout = new GroupLayout(browserToolBar);
 			browserToolBar.setLayout(buttonBarLayout);
 
 			// Layout information.
 			if (objectType == NODES) {
 				buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(GroupLayout.LEADING)
 				                                                  .add(buttonBarLayout.createSequentialGroup()
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getSelectButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           GroupLayout.DEFAULT_SIZE,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getNewButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getSelectAllButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getUnselectAllButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getDeleteButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED,
 				                                                                                       28,
 				                                                                                       Short.MAX_VALUE)
 				                                                                      .add(getAttrModButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           28,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getFunctionBuilderButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           28,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getImportButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           28,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getMatrixButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)));
 				buttonBarLayout.setVerticalGroup(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     selectButton,
 				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                     27,
 				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     createNewAttributeButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                	 selectAllAttributesButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                	 unselectAllAttributesButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     deleteAttributeButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
 				                                                                    .add(matrixButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                                    .add(importButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                                    .add(attrModButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                                    .add(formulaBuilderButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 			} else if (objectType == NETWORK) {
 				buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(GroupLayout.LEADING)
 				                                                  .add(buttonBarLayout.createSequentialGroup()
 				                                                                      .add(getSelectButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           GroupLayout.DEFAULT_SIZE,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getNewButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getSelectAllButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getUnselectAllButton())
  				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getDeleteButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED,
 				                                                                                       320,
 				                                                                                       Short.MAX_VALUE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getAttrModButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           28,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getFunctionBuilderButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           28,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)));
 
 				buttonBarLayout.setVerticalGroup(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     selectButton,
 				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                     27,
 				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     createNewAttributeButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                	 selectAllAttributesButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                	 unselectAllAttributesButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     deleteAttributeButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
 				                                                                    .add(attrModButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                                    .add(formulaBuilderButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 			} else {
 				buttonBarLayout.setHorizontalGroup(buttonBarLayout.createParallelGroup(GroupLayout.LEADING)
 				                                                  .add(buttonBarLayout.createSequentialGroup()
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getSelectButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           GroupLayout.DEFAULT_SIZE,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getNewButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getSelectAllButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getUnselectAllButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getDeleteButton())
 				                                                                      .addPreferredGap(LayoutStyle.RELATED,
 				                                                                                       150,
 				                                                                                       Short.MAX_VALUE)
 				                                                                      .add(getAttrModButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           28,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 										       		      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getFunctionBuilderButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           28,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)
 				                                                                      .add(getImportButton(),
 				                                                                           GroupLayout.PREFERRED_SIZE,
 				                                                                           28,
 				                                                                           GroupLayout.PREFERRED_SIZE)
 				                                                                      .addPreferredGap(LayoutStyle.RELATED)));
 				buttonBarLayout.setVerticalGroup(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     selectButton,
 				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                     27,
 				                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     createNewAttributeButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                	 selectAllAttributesButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                	 unselectAllAttributesButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(org.jdesktop.layout.GroupLayout.CENTER,
 				                                                     deleteAttributeButton,
 				                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 				                                                     27, Short.MAX_VALUE)
 				                                                .add(buttonBarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
 				                                                                    .add(importButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                                    .add(formulaBuilderButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				                                                                    .add(attrModButton,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 				                                                                         27,
 				                                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 			}
 		}
 
 		return browserToolBar;
 	}
 
 	/**
 	 * This method initializes jButton
 	 *
 	 * @return javax.swing.JButton
 	 */
 	private JButton getSelectButton() {
 		if (selectButton == null) {
 			selectButton = new JButton();
 			selectButton.setBorder(null);
 			selectButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			selectButton.setIcon(new ImageIcon(AttributeBrowser.class.getResource("images/stock_select-row.png")));
 			selectButton.setToolTipText("Select Attributes");
 
 			selectButton.addMouseListener(new MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						attributeList.setSelectedItems(orderedCol);
 						attributeSelectionPopupMenu.show(e.getComponent(), e.getX(), e.getY());
 					}
 				});
 		}
 
 		return selectButton;
 	}
 
 	private JButton getImportButton() {
 		if (importButton == null) {
 			importButton = new JButton();
 			importButton.setBorder(null);
 			importButton.setIcon(new ImageIcon(AttributeBrowser.class.getResource("images/stock_open.png")));
 			importButton.setToolTipText("Import attributes from file...");
 			importButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			importButton.addMouseListener(new java.awt.event.MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						importAttributes();
 					}
 				});
 		}
 
 		return importButton;
 	}
 
 	private JButton getMatrixButton() {
 		if (matrixButton == null) {
 			matrixButton = new JButton();
 			matrixButton.setBorder(null);
 			matrixButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			matrixButton.setIcon(new javax.swing.ImageIcon(AttributeBrowser.class.getResource("images/microarray_24.png")));
 			matrixButton.setToolTipText("Import Expression Matrix Data...");
 
 			matrixButton.addMouseListener(new java.awt.event.MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						importMatrix();
 					}
 				});
 		}
 
 		return matrixButton;
 	}
 
 	private JButton attrModButton = null;
 
 	private JButton getAttrModButton() {
 		if (attrModButton == null) {
 			attrModButton = new JButton();
 			attrModButton.setBorder(null);
 			attrModButton.setIcon(new javax.swing.ImageIcon(AttributeBrowser.class.getResource("images/stock_insert-columns.png")));
 			attrModButton.setToolTipText("Attribute Batch Editor");
 			attrModButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 
 			attrModButton.addMouseListener(new java.awt.event.MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						modDialog.setLocationRelativeTo(Cytoscape.getDesktop());
 						modDialog.setVisible(true);
 					}
 				});
 		}
 
 		return attrModButton;
 	}
 
 	private JButton formulaBuilderButton = null;
 
 	private JButton getFunctionBuilderButton() {
 		if (formulaBuilderButton == null) {
 			formulaBuilderButton = new JButton();
 			formulaBuilderButton.setBorder(null);
 			formulaBuilderButton.setIcon(new ImageIcon(AttributeBrowser.class.getResource("images/fx.png")));
 			formulaBuilderButton.setToolTipText("Function Builder");
 			formulaBuilderButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
 
 			formulaBuilderButton.addMouseListener(new java.awt.event.MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						// Do not allow opening of the formula builder dialog while a cell is being edited!
 						if (table.getCellEditor() != null)
 							return;
 
 						final int cellRow = table.getSelectedRow();
 						final int cellColumn = table.getSelectedColumn();
 						if (cellRow == -1 || cellColumn == -1 || !tableModel.isCellEditable(cellRow, cellColumn))
 							JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 							                              "Can't enter a formula w/o a selected cell!",
 							                              "Information", JOptionPane.INFORMATION_MESSAGE);
 						else {
 							final String attrName = getAttribName(cellRow, cellColumn);
 							final Map<String, Class> attribNameToTypeMap = new HashMap<String, Class>();
 							Util.initAttribNameToTypeMap(objectType, attrName, attribNameToTypeMap);
 							formulaBuilderDialog =
 								new FormulaBuilderDialog(tableModel, table, objectType, Cytoscape.getDesktop(),
 								                         attribNameToTypeMap, attrName);
 							formulaBuilderDialog.setLocationRelativeTo(Cytoscape.getDesktop());
 							formulaBuilderDialog.setVisible(true);
 						}
 					}
 				});
 		}
 
 		return formulaBuilderButton;
 	}
 
 	private String getAttribName(final int cellRow, final int cellColumn) {
 		if (objectType == NETWORK)
 			return ((ValidatedObjectAndEditString)(tableModel.getValueAt(cellRow, 0))).getValidatedObject().toString();
 		else
 			return tableModel.getColumnName(cellColumn);
 	}
 
 	protected void editMetadata() {
 		NetworkMetaDataDialog mdd = new NetworkMetaDataDialog(Cytoscape.getDesktop(), false,
 		                                                      Cytoscape.getCurrentNetwork());
 		mdd.setVisible(true);
 	}
 
 	protected void importAttributes() {
 		if (objectType == NODES) {
 			ImportNodeAttributesAction nodeAction = new ImportNodeAttributesAction();
 			nodeAction.actionPerformed(null);
 		} else if (objectType == EDGES) {
 			ImportEdgeAttributesAction edgeAction = new ImportEdgeAttributesAction();
 			edgeAction.actionPerformed(null);
 		} else { // case for Network
 			logger.warn("Network Attribute import not implemented yet");
 		}
 	}
 
 	protected void importMatrix() {
 		ImportExpressionMatrixAction matrixAction = new ImportExpressionMatrixAction();
 		matrixAction.actionPerformed(null);
 	}
 
 	private JButton getDeleteButton() {
 		if (deleteAttributeButton == null) {
 			deleteAttributeButton = new JButton();
 			deleteAttributeButton.setBorder(null);
 			deleteAttributeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			deleteAttributeButton.setIcon(new javax.swing.ImageIcon(AttributeBrowser.class
 			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     .getResource("images/stock_delete.png")));
 			deleteAttributeButton.setToolTipText("Delete Attributes...");
 
 			// Create pop-up window for deletion
 			deleteAttributeButton.addMouseListener(new java.awt.event.MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						removeAttribute(e);
 					}
 				});
 		}
 
 		return deleteAttributeButton;
 	}
 
 	private JButton getSelectAllButton() {
 		if (selectAllAttributesButton == null) {
 			selectAllAttributesButton = new JButton();
 			selectAllAttributesButton.setBorder(null);
 			selectAllAttributesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			selectAllAttributesButton.setIcon(new javax.swing.ImageIcon(AttributeBrowser.class.getResource("images/select_all.png")));
 			selectAllAttributesButton.setToolTipText("Select All Attributes");
 
 			selectAllAttributesButton.addMouseListener(new java.awt.event.MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						updateList(attrModel.getAttributeNames());
 						try {
 //							getUpdatedSelectedList();
 							updateSelectedColumn();
 							tableModel.setTableData(null, orderedCol);
 						} catch (Exception ex) {
 							attributeList.clearSelection();
 						}
 					}
 				});
 		}
 
 		return selectAllAttributesButton;
 	}
 
 	private JButton getUnselectAllButton() {
 		if (unselectAllAttributesButton == null) {
 			unselectAllAttributesButton = new JButton();
 			unselectAllAttributesButton.setBorder(null);
 			unselectAllAttributesButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			unselectAllAttributesButton.setIcon(new javax.swing.ImageIcon(AttributeBrowser.class.getResource("images/unselect_all.png")));
 			unselectAllAttributesButton.setToolTipText("Unselect All Attributes");
 
 			unselectAllAttributesButton.addMouseListener(new java.awt.event.MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						final List<String> emptyList = new ArrayList<String>();
 						updateList(emptyList);
 						try {
 //							getUpdatedSelectedList();
 							updateSelectedColumn();
 							tableModel.setTableData(null, orderedCol);
 						} catch (Exception ex) {
 							attributeList.clearSelection();
 						}
 					}
 				});
 		}
 		return unselectAllAttributesButton;
 	}
 
 	private void removeAttribute(final MouseEvent e) {
 		final String[] attrArray = getAttributeArray();
 		Arrays.sort(attrArray);
 
 		final DeletionDialog dDialog = new DeletionDialog(Cytoscape.getDesktop(), true, attrArray,
 		                                                  attributeType, tableModel);
 
 		dDialog.pack();
 		dDialog.setLocationRelativeTo(browserToolBar);
 		dDialog.setVisible(true);
 		attrModel.sortAttributes();
 
 		final List<String> atNames = new ArrayList<String>();
 		for (String attName: CyAttributesUtils.getVisibleAttributeNames(attributes))
 			atNames.add(attName);
 		final List<String> toBeRemoved = new ArrayList<String>();
 		for (String colName: orderedCol) {
 			if (atNames.contains(colName) == false)
 				toBeRemoved.add(colName);
 		}
 		
 		for (String rem: toBeRemoved)
 			orderedCol.remove(rem);
 
 		tableModel.setTableData(null, orderedCol);
 		AttributeBrowser.firePropertyChange(AttributeBrowser.RESTORE_COLUMN, null, objectType);
 		
 	}
 	
 
 	private JList getSelectedAttributeList() {
 		if (attributeList == null) {
 			attributeList = new CheckBoxJList();
 			attributeList.setModel(attrModel);
 			attributeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 			attributeList.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					if (SwingUtilities.isRightMouseButton(e)) {
 						attributeSelectionPopupMenu.setVisible(false);
 					}
 				}
 			});
 		}
 		return attributeList;
 	}
 
 	private String[] getAttributeArray() {
 		final CyAttributes currentAttributes;
 
 		if (objectType == NODES) {
 			attributeType = "Node";
 			currentAttributes = Cytoscape.getNodeAttributes();
 		} else if (objectType == EDGES) {
 			attributeType = "Edge";
 			currentAttributes = Cytoscape.getEdgeAttributes();
 		} else if (objectType == NETWORK) {
 			attributeType = "Network";
 			currentAttributes = Cytoscape.getNetworkAttributes();
 		} else {
 			return new String[0];
 		}
 
 		return CyAttributesUtils.getVisibleAttributeNames(currentAttributes).toArray(new String[0]);
 	}
 
 	/**
 	 * This method initializes createNewAttributeButton
 	 *
 	 * @return javax.swing.JButton
 	 */
 	private JButton getNewButton() {
 		if (createNewAttributeButton == null) {
 			createNewAttributeButton = new JButton();
 			createNewAttributeButton.setBorder(null);
 
 			createNewAttributeButton.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
 			createNewAttributeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 			createNewAttributeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			createNewAttributeButton.setToolTipText("Create New Attribute");
 			createNewAttributeButton.setIcon(new javax.swing.ImageIcon(AttributeBrowser.class
 			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           .getResource("images/stock_new.png")));
 			createNewAttributeButton.addMouseListener(new java.awt.event.MouseAdapter() {
 					public void mouseClicked(java.awt.event.MouseEvent e) {
 						jPopupMenu1.show(e.getComponent(), e.getX(), e.getY());
 					}
 				});
 		}
 
 		return createNewAttributeButton;
 	}
 
 	// Create a whole new attribute and set a default value.
 	//
 	private void createNewAttribute(final String type) {
 		final String[] existingAttrs = CyAttributesUtils.getVisibleAttributeNames(attributes).toArray(new String[0]);
 		String newAttribName = null;
 		do {
 			newAttribName = JOptionPane.showInputDialog(this, "Please enter new attribute name: ",
 								    "Create New " + type + " Attribute",
 								    JOptionPane.QUESTION_MESSAGE);
 			if (newAttribName == null)
 				return;
 
 			if (Arrays.binarySearch(existingAttrs, newAttribName) >= 0) {
				newAttribName = null;
 				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 							      "Attribute " + newAttribName + " already exists.",
 							      "Error!", JOptionPane.ERROR_MESSAGE);
 			}
 		} while (newAttribName == null);
 
 		final String testVal = "dummy";
 
 		if (type.equals("String"))
 			attributes.setAttribute(testVal, newAttribName, new String());
 		else if (type.equals("Floating Point"))
 			attributes.setAttribute(testVal, newAttribName, new Double(0));
 		else if (type.equals("Integer"))
 			attributes.setAttribute(testVal, newAttribName, new Integer(0));
 		else if (type.equals("Boolean"))
 			attributes.setAttribute(testVal, newAttribName, new Boolean(false));
 		else if (type.equals("String List")) {
 			final List<String> newStringList = new ArrayList<String>();
 			newStringList.add("dummy");
 			attributes.setListAttribute(testVal, newAttribName, newStringList);
 		} else if (type.equals("Floating Point List")) {
 			final List<Double> newDoubleList = new ArrayList<Double>();
 			newDoubleList.add(0.0);
 			attributes.setListAttribute(testVal, newAttribName, newDoubleList);
 		} else if (type.equals("Integer List")) {
 			final List<Integer> newIntList = new ArrayList<Integer>();
 			newIntList.add(0);
 			attributes.setListAttribute(testVal, newAttribName, newIntList);
 		} else if (type.equals("Boolean List")) {
 			final List<Boolean> newBooleanList = new ArrayList<Boolean>();
 			newBooleanList.add(true);
 			attributes.setListAttribute(testVal, newAttribName, newBooleanList);
 		} else
 			throw new IllegalArgumentException("unknown attribute type \"" + type + "\"!");
 
 		attributes.deleteAttribute(testVal, newAttribName);
 
 		// Update list selection
 		orderedCol.add(newAttribName);
 		Cytoscape.getSwingPropertyChangeSupport()
 			.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 		Cytoscape.getPropertyChangeSupport().firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 
 		tableModel.setTableData(null, orderedCol);
 	}
 
 	@Override
 	public void popupMenuCanceled(PopupMenuEvent e) {}
 
 	@Override
 	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 		// Update actual table
 		try {
 			updateSelectedColumn();
 			tableModel.setTableData(null, orderedCol);
 		} catch (Exception ex) {
 			attributeList.clearSelection();
 		}
 	}
 	
 	private void updateSelectedColumn() {
 		
 		final Object[] selected = attributeList.getSelectedValues();
 		final List<String> list = new ArrayList<String>();
 		for(final Object name: selected)
 			list.add(name.toString());
 		updateList(list);
 	}
 	
 	public void setOrderedColumnList(final List<String> newSelection) {
 		orderedCol = new ArrayList<String>(newSelection);
 		attributeList.setSelectedItems(orderedCol);
 	}
 	public void updateList(List<String> newSelection) {
 		//System.out.println("Update called.  new List: " + newSelection);
 		//System.out.println("Update called.  OLD List: " + orderedCol);
 		final List<String> tempList = new ArrayList<String>(orderedCol);
 		
 		// Special cases: original or new List is empty
 		if(orderedCol.size() == 0 || orderedCol.size() == 1) {
 			orderedCol = new ArrayList<String>(newSelection);
 			attributeList.setSelectedItems(orderedCol);
 			return;
 		} else if(newSelection.size() == 0) {
 			orderedCol = new ArrayList<String>();
 			attributeList.setSelectedItems(orderedCol);
 			return;
 		}
 			
 		// First, remove unnecessary column from the original ordered list
 		for(final String currentColName: orderedCol) {
 			if(currentColName.equals("ID") == false && newSelection.contains(currentColName) == false) {
 				//System.out.println("  Removed: " + currentColName);
 				tempList.remove(currentColName);
 			}
 		}
 		
 		// Then add the new column anames to the end of list
 		for(final String currentColName: newSelection) {
 			if(currentColName.equals("ID") == false && tempList.contains(currentColName) == false) {
 				//System.out.println("  Adding: " + currentColName);
 				tempList.add(currentColName);
 			}
 		}
 		
 		// Copy it to the sorted list
 		orderedCol = new ArrayList<String>(tempList);
 		//System.out.println("======> ORDERED: " + orderedCol);
 		attributeList.setSelectedItems(orderedCol);
 	}
 
 	@Override
 	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
 }
