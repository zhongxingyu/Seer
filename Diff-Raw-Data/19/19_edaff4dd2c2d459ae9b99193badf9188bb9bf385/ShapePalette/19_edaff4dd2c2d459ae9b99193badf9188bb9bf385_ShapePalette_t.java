 /* -*-Java-*-
 ********************************************************************************
 *
 * File:         ShapePalette.java
 * RCS:          $Header: $
 * Description:
 * Author:       Allan Kuchinsky
 * Created:      Sun May 29 11:18:17 2005
 * Modified:     Sun Dec 17 05:33:30 2006 (Michael L. Creech) creech@w235krbza760
 * Language:     Java
 * Package:
 /*
  
  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)
 
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
  
 ********************************************************************************
 *
 * Revisions:
 *
 * Sun Dec 17 05:30:11 2006 (Michael L. Creech) creech@w235krbza760
 *  Added DragSourceContextCursorSetter parameter to addShape().
 * Mon Dec 04 11:57:11 2006 (Michael L. Creech) creech@w235krbza760
 *  Changed the JList to no longer use
 *  setFixedCellHeight() since BasicCytoShapeEntitys can now have
 *  different sizes.
 * Sun Aug 06 11:19:38 2006 (Michael L. Creech) creech@w235krbza760
 *  Added generated serial version UUID for serializable classes.
 ********************************************************************************
 */
 package cytoscape.editor.impl;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Color;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JEditorPane;
 import javax.swing.ListCellRenderer;
 import javax.swing.border.TitledBorder;
 
 import cytoscape.Cytoscape;
 import cytoscape.editor.CytoscapeEditorManager;
 import cytoscape.editor.DragSourceContextCursorSetter;
 import cytoscape.editor.event.BasicCytoShapeTransferHandler;
 import cytoscape.view.CyNetworkView;
 import ding.view.DGraphView;
 
 
 /**
  *
  * The <b>ShapePalette</b> class implements a palette from which the user drags and drops shapes onto the canvas
  * The dropping of shapes onto the canvas results in the addition of nodes and edges to the current Cytoscape
  * network, as defined by the behavior of the event handler that responds to the drop events.  For example, in the
  * simple "BioPAX-like" editor, there are node types for proteins, catalysis, small molecules, and biochemical
  * reactions, as well as a directed edge type.
  * <p>
  * The user interface for the ShapePalette is built upon the JList class.
  *
  * @author Allan Kuchinsky
  * @version 1.0
  *
  */
 public class ShapePalette extends JPanel {
 	// MLC 08/06/06:
 	private static final long serialVersionUID = -4018789452330887392L;
 
 	/**
 	 * mapping of shapes to their titles
 	 */
 	static Map<String, BasicCytoShapeEntity> _shapeMap = new HashMap<String, BasicCytoShapeEntity>();
 
 	/**
 	 * the user interface for the ShapePalette
 	 */
 	protected JList dataList;
 	protected DefaultListModel listModel;
 	private JPanel _controlPane;
 	protected JScrollPane scrollPane;
 	protected JPanel _shapePane;
 
 	/**
 	 * Creates a new ShapePalette object.
 	 */
 	public ShapePalette() {
 		super();
 
 		_controlPane = new JPanel();
 		_controlPane.setLayout(new BoxLayout(_controlPane, BoxLayout.Y_AXIS));
 		TitledBorder t2 = BorderFactory.createTitledBorder("Instructions:");
 		_controlPane.setBorder(t2);
 		String instructions = "<html><style type='text/css'>body{ font-family: sans-serif; font-size: 11pt; }</style><b>Drag and Drop:</b> <ul> <li>A node shape onto the network view.  <li>An edge shape onto the source node, then click on the target node.  </ul> <b>Double-click:</b> <ul> <li>To add nodes and edges specified in SIF format </ul>" + (System.getProperty("os.name").startsWith("Mac") ? "<b>CMD-click:</b>" : "<b>CTRL-click:</b>") + "<ul> <li>On empty space to create a node.  <li>On a node to begin an edge and specify the source node. Then click on the target node to finish the edge.  </ul></html>";
 		
 		JEditorPane instructionsArea = new JEditorPane("text/html",instructions);
 		// 32767 ????
		instructionsArea.setPreferredSize(new java.awt.Dimension(300, 300));
 		instructionsArea.setBackground(Cytoscape.getDesktop().getBackground());
 		_controlPane.add(instructionsArea);
 		_controlPane.setBackground(Cytoscape.getDesktop().getBackground());
 		
 		JPanel pnlSpecifyIdentifier = new JPanel();
 		// 32767 ????
 		pnlSpecifyIdentifier.setMaximumSize(new java.awt.Dimension(32767, 100));
 		JLabel chkSpecifyLabel = new JLabel("Specify Identifier:");
 		chkSpecifyIdentifier = new javax.swing.JCheckBox();
 		chkSpecifyIdentifier.setToolTipText("Checking the box will allow you to choose the identifier for added nodes and edges.");
 		pnlSpecifyIdentifier.setLayout(new java.awt.GridBagLayout());
 		pnlSpecifyIdentifier.setBorder(BorderFactory.createTitledBorder(""));
 		chkSpecifyIdentifier.addChangeListener(new javax.swing.event.ChangeListener() {
 				public void stateChanged(javax.swing.event.ChangeEvent evt) {
 					CheckBoxSpecifyIdentifierStateChanged(evt);
 				}
 			});
 		pnlSpecifyIdentifier.add(chkSpecifyLabel);
 		pnlSpecifyIdentifier.add(chkSpecifyIdentifier);
 		chkSpecifyIdentifier.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		chkSpecifyIdentifier.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 		chkSpecifyIdentifier.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
 		chkSpecifyIdentifier.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		gridBagConstraints.weightx = 1.0;
 		pnlSpecifyIdentifier.add(chkSpecifyIdentifier, gridBagConstraints);
 		
 		listModel = new DefaultListModel();
 		dataList = new JList(listModel);
 		dataList.setCellRenderer(new MyCellRenderer());
 		dataList.setDragEnabled(true);
 
 		dataList.setTransferHandler(new PaletteListTransferHandler());
 		_shapePane = new JPanel();
 		_shapePane.setLayout(new BoxLayout(_shapePane, BoxLayout.Y_AXIS));
 
 		scrollPane = new JScrollPane(_shapePane);
 
 		scrollPane.setBorder(BorderFactory.createEtchedBorder());
 		dataList.setBackground(Cytoscape.getDesktop().getBackground());
 		scrollPane.setBackground(Cytoscape.getDesktop().getBackground());
  
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		this.setLayout(new java.awt.GridBagLayout());
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		add(_controlPane, gridBagConstraints);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		add(pnlSpecifyIdentifier, gridBagConstraints);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridy = 2;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		this.add(scrollPane, gridBagConstraints);
 
 		CytoscapeEditorManager.setCurrentShapePalette(this);
 
 		CyNetworkView view = Cytoscape.getCurrentNetworkView();
 		CytoscapeEditorManager.setShapePaletteForView(view, this);
 
 		this.setBackground(Cytoscape.getDesktop().getBackground());
 		this.setVisible(true);
 	}
 
 	private javax.swing.JCheckBox chkSpecifyIdentifier;
 	public void CheckBoxSpecifyIdentifierStateChanged(javax.swing.event.ChangeEvent evt){
 		if (chkSpecifyIdentifier.isSelected()){
 			specifyIdentifier = true;
 		}
 		else {
 			specifyIdentifier = false;
 		}
 	}
     
 	public static boolean specifyIdentifier = false;
     
 	/**
 	 * clear the ShapePalette by removing all its shape components
 	 *
 	 */
 	public void clear() {
 		_shapePane.removeAll();
 	}
 
 	/**
 	 * add a shape to the palette
 	 * @param attributeName attribute name for the shape, should be one of "NodeType" or "EdgeType"
 	 * @param attributeValue value for the attribute assigned to the shape, for example a "NodeType" of "protein"
 	 * @param img the icon for the shape
 	 * @param name the title of the shape
 	 * @param cursorSetter a possibly null DragSourceContextCursorSetter used to specify
 	 *                     the cursor so show when dragging over the current network view.
 	 */
 
 	// MLC 12/16/06 BEGIN:
 	public void addShape(String attributeName, String attributeValue, Icon img, String name,
 	                     DragSourceContextCursorSetter cursorSetter) {
 		BasicCytoShapeEntity cytoShape = new BasicCytoShapeEntity(attributeName, attributeValue,
 		                                                          img, name, cursorSetter);
 		cytoShape.setTransferHandler(new BasicCytoShapeTransferHandler(cytoShape, null));
 		_shapeMap.put(cytoShape.getTitle(), cytoShape);
 
 		if (attributeName.equals(CytoscapeEditorManager.EDGE_TYPE)) {
 			CytoscapeEditorManager.addEdgeTypeForVisualStyle(Cytoscape.getCurrentNetworkView()
 									 .getVisualStyle(),
 			                                                 attributeValue);
 		}
 
 		_shapePane.add(cytoShape);
 	}
 
 	/**
 	 * show the palette in the WEST cytopanel
 	 *
 	 */
 	public void showPalette() {
 		CyNetworkView view = Cytoscape.getCurrentNetworkView();
 		CytoscapeEditorManager.setShapePaletteForView(view, this);
 		this.setVisible(true);
 	}
 
 	/**
 	 *
 	 * @param key the name of the shape to be returned
 	 * @return return the BasicCytoShapeEntity associated with the input shape name
 	 */
 	public static BasicCytoShapeEntity getBasicCytoShapeEntity(String key) {
 		Object val = _shapeMap.get(key);
 
 		if (val instanceof BasicCytoShapeEntity) {
 			return ((BasicCytoShapeEntity) val);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * renders each cell of the ShapePalette
 	 * @author Allan Kuchinsky
 	 * @version 1.0
 	 *
 	 */
 	class MyCellRenderer extends JLabel implements ListCellRenderer {
 		// This is the only method defined by ListCellRenderer.
 		// We just reconfigure the JLabel each time we're called.
 		// MLC 08/06/06:
 		private static final long serialVersionUID = -4704405703871398609L;
 
 		public Component getListCellRendererComponent(JList list, Object value, // value to display
 		                                              int index, // cell index
 		                                              boolean isSelected, // is the cell selected
 		                                              boolean cellHasFocus) // the list and the cell have the focus
 		{
 			if (value instanceof BasicCytoShapeEntity) {
 				BasicCytoShapeEntity cytoShape = (BasicCytoShapeEntity) value;
 				setText(cytoShape.getTitle());
 				setIcon(cytoShape.getIcon());
 				setToolTipText(cytoShape.getToolTipText());
 			}
 
 			if (isSelected) {
 				setBackground(list.getSelectionBackground());
 				setForeground(list.getSelectionForeground());
 			} else {
 				setBackground(list.getBackground());
 				setForeground(list.getForeground());
 			}
 
 			setEnabled(list.isEnabled());
 			setFont(list.getFont());
 			setOpaque(true);
 
 			return this;
 		}
 	}
 
 	/**
 	 * bundles up the name of the BasicCytoShapeEntity for export via drag/drop from the palette
 	 * @author Allan Kuchinsky
 	 * @version 1.0
 	 *
 	 */
 	class PaletteListTransferHandler extends StringTransferHandler {
 		// MLC 08/06/06:
 		private static final long serialVersionUID = -3858539899491771525L;
 
 		protected void cleanup(JComponent c, boolean remove) {
 		}
 
 		protected void importString(JComponent c, String str) {
 		}
 
 		//Bundle up the selected items in the list
 		//as a single string, for export.
 		protected String exportString(JComponent c) {
 			JList list = (JList) c;
 			Object val = list.getSelectedValue();
 
 			if (val instanceof BasicCytoShapeEntity) {
 				return ((BasicCytoShapeEntity) val).getTitle();
 			} else {
 				return null;
 			}
 		}
 	}
 }
