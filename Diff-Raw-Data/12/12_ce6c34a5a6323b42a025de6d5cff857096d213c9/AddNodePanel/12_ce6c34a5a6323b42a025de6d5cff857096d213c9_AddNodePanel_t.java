 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package autograph.ui;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import autograph.Edge;
 import autograph.Graph;
 import autograph.GraphHelper;
 import autograph.GraphPanel;
 import autograph.Node;
 import autograph.ui.ColorPickerButton;
 import autograph.exception.CannotAddEdgeException;
 import autograph.exception.CannotAddNodeException;
 
 public class AddNodePanel extends JPanel {
 
 	/**
 	 * Creates new form AddNodePanel
 	 */
 	public AddNodePanel() {
 		initComponents();
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 */
 	@SuppressWarnings("unchecked")
 	private void initComponents() {
 
 		// Setup
 		GraphicsEnvironment ge;
 		ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
 		String[] fontNames = ge.getAvailableFontFamilyNames();
 
 		setBackground(new java.awt.Color(255, 255, 255));
 		setMinimumSize(new java.awt.Dimension(200, 512));
 		setPreferredSize(new java.awt.Dimension(200, 512));
 
 		// The Colors
 		labelColor = Color.BLACK;
 		fillColor = Color.WHITE;
 		borderColor = Color.BLACK;
 
 		// Add Node Panel
 		AddNodePanel = new JPanel();
 		AddNodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
 		AddNodePanel.setPreferredSize(new java.awt.Dimension(200, 512));
 
 		// Add Node Title Label
 		AddNodeTitleLabel = new JLabel();
 		AddNodeTitleLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); 
 		AddNodeTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		AddNodeTitleLabel.setText("Add Node");
 
 		// Label Subtitle Label
 		LabelSubtitleLabel = new JLabel();
 		LabelSubtitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); 
 		LabelSubtitleLabel.setText("Label");
 
 		// Label Text Label
 		LabelTextLabel = new JLabel();
 		LabelTextLabel.setText("Label Text");
 
 		// Label Text Field
 		LabelTextField = new JTextField();
 
 		// Label Font Label
 		LabelFontLabel = new JLabel();
 		LabelFontLabel.setText("Label Font");
 
 		// Label Font Combo Box
 		LabelFontComboBox = new JComboBox();
 		LabelFontComboBox.setModel(new DefaultComboBoxModel(fontNames));
 		LabelFontComboBox.setSelectedItem("Courier");
 
 		// Label Color Label
 		LabelColorLabel = new JLabel();
 		LabelColorLabel.setText("Label Color");
 
 		// Label Color Button
 		LabelColorBtn = new ColorPickerButton();
 		LabelColorBtn.setText("   ");
 		LabelColorBtn.setColor(labelColor);
 		
 		LabelColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		LabelColorBtn.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				LabelColorBtnActionPerformed(evt);
 			}
 		});
 
 		// Node Subtitle Label
 		NodeSubtitleLabel = new JLabel();
 		NodeSubtitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
 		NodeSubtitleLabel.setText("Node");
 
 		// Node Shape Label
 		NodeShapeLabel = new JLabel();
 		NodeShapeLabel.setText("Node Shape");
 
 		// Border Color Label
 		BorderColorLabel = new JLabel();
 		BorderColorLabel.setText("Border Color");
 
 		// Fill Color Label
 		FillColorLabel = new JLabel();
 		FillColorLabel.setText("Fill Color");
 
 		// Fill Color Button
 		FillColorBtn = new ColorPickerButton();
 		FillColorBtn.setText("   ");
 		FillColorBtn.setColor(fillColor);
 		FillColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		FillColorBtn.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				FillColorBtnActionPerformed(evt);
 			}
 		});
 
 		// Node Shape Combo Box
 		NodeShapeComboBox = new JComboBox();
 		NodeShapeComboBox.setModel(new DefaultComboBoxModel(new String[] { "Circle", "Square", "Triangle" }));
 
 		// Border Color Button
 		BorderColorBtn = new ColorPickerButton();
 		BorderColorBtn.setText("   ");
 		BorderColorBtn.setColor(borderColor);
 		BorderColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		BorderColorBtn.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				BorderColorBtnActionPerformed(evt);
 			}
 		});
 
 		// Pane Separator
 		paneSeparator = new JSeparator();
 
 		// Add Node Button
 		addButton = new JButton();
 		addButton.setText("Add Node");
 		addButton.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				AddNodeBtnActionPerformed(evt);
 			}
 		});
 
 		// Filler
 		filler1 = new Box.Filler(new java.awt.Dimension(200, 0), new java.awt.Dimension(200, 0), new java.awt.Dimension(200, 32767));
 
 		// Layout
 		javax.swing.GroupLayout AddNodePanelLayout = new GroupLayout(AddNodePanel);
 		AddNodePanel.setLayout(AddNodePanelLayout);
 		AddNodePanelLayout.setHorizontalGroup(
 				AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(AddNodePanelLayout.createSequentialGroup()
 						.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addComponent(LabelSubtitleLabel)
 								.addComponent(NodeSubtitleLabel))
 								.addGap(0, 0, Short.MAX_VALUE))
 								.addGroup(AddNodePanelLayout.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 												.addComponent(AddNodeTitleLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 												.addComponent(LabelTextField, 0, GroupLayout.DEFAULT_SIZE, 190)
 												.addComponent(LabelFontComboBox, 0, GroupLayout.DEFAULT_SIZE, 190)
 												.addComponent(paneSeparator, 0, GroupLayout.DEFAULT_SIZE, 186)
 												.addGroup(javax.swing.GroupLayout.Alignment.LEADING, AddNodePanelLayout.createSequentialGroup()
 														.addGap(0, 0, Short.MAX_VALUE)
 														.addComponent(addButton))
 														.addGroup(AddNodePanelLayout.createSequentialGroup()
 																.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
 																		.addGroup(AddNodePanelLayout.createSequentialGroup()
 																				.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																						.addComponent(LabelTextLabel)
 																						.addComponent(LabelFontLabel)
 																						.addComponent(BorderColorLabel)
 																						.addComponent(FillColorLabel))
 																						.addGap(18, 18, 18)
 																						.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																								.addComponent(FillColorBtn)
 																								.addComponent(BorderColorBtn)
 																								.addComponent(LabelColorBtn)
 																								.addComponent(NodeShapeComboBox, 0, GroupLayout.DEFAULT_SIZE, 120)))
 																								.addGroup(AddNodePanelLayout.createSequentialGroup()
 																										.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																												.addComponent(NodeShapeLabel)
 																												.addComponent(LabelColorLabel))
 																												.addGap(18, 18, 18)
 																												.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING))))
 																												.addGap(0, 0, Short.MAX_VALUE)))
 																												.addContainerGap())
 
 				);
 		AddNodePanelLayout.setVerticalGroup(
 				AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(AddNodePanelLayout.createSequentialGroup()
 						.addComponent(AddNodeTitleLabel)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 						.addComponent(LabelSubtitleLabel)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(LabelTextLabel)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(LabelTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addGap(18, 18, 18)
 						.addComponent(LabelFontLabel)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(LabelFontComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addGap(18, 18, 18)
 						.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 								.addComponent(LabelColorLabel)
 								.addComponent(LabelColorBtn))
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 								.addComponent(paneSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
 								.addGap(3, 3, 3)
 								.addComponent(NodeSubtitleLabel)
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 										.addComponent(NodeShapeLabel)
 										.addComponent(NodeShapeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 										.addGap(18, 18, 18)
 										.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(BorderColorLabel)
 												.addComponent(BorderColorBtn))
 												.addGap(18, 18, 18)
 												.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 														.addComponent(FillColorLabel)
 														.addComponent(FillColorBtn))
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
 														.addComponent(addButton)
 														.addContainerGap())
 				);
 
 		javax.swing.GroupLayout layout = new GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(filler1, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 0, Short.MAX_VALUE))
 						.addGroup(layout.createSequentialGroup()
 								.addComponent(AddNodePanel, GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
 								.addContainerGap())
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(AddNodePanel, GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(filler1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 0, 0))
 				);
 	}
 
 	/*
 	 * The Label Color Button was clicked
 	 */
 	protected void LabelColorBtnActionPerformed(MouseEvent evt) {
 		Color newColor = JColorChooser.showDialog(AddNodePanel.this, "Choose Label Color", labelColor);
 
 		if (newColor != null) {
 			labelColor = newColor;
 			LabelColorBtn.setColor(labelColor);
 		}
 	}
 
 	/*
 	 * The Fill Color Button was clicked
 	 */
 	protected void FillColorBtnActionPerformed(MouseEvent evt) {
 		Color newColor = JColorChooser.showDialog(AddNodePanel.this, "Choose Fill Color", fillColor);
 
 		if (newColor != null) {
 			fillColor = newColor;
 			FillColorBtn.setColor(fillColor);
 		}
 	}
 
 	/*
 	 * The Border Color Button was clicked
 	 */
 	protected void BorderColorBtnActionPerformed(MouseEvent evt) {
 		Color newColor = JColorChooser.showDialog(AddNodePanel.this, "Choose Border Color", borderColor);
 
 		if (newColor != null) {
 			borderColor = newColor;
 			BorderColorBtn.setColor(borderColor);
 		}
 	}
 
 	/*
 	 * The Add Node Button has been clicked
 	 */
 	private void AddNodeBtnActionPerformed(java.awt.event.MouseEvent evt) {
 		// Graph/Window pointers
 		mainWindowTabbedPane = mainWindow.getMainWindowPane();
 		JScrollPane currentPane = (JScrollPane)mainWindowTabbedPane.getSelectedComponent();
 		GraphPanel currentPanel = (GraphPanel)currentPane.getViewport().getView();
 		Graph currentGraph = currentPanel.mGetGraph();
 		int numNodes = currentGraph.mGetNodeList().size();
 		int numEdges = currentGraph.mGetEdgeList().size();
 
 		// If auto Label Nodes is on
 		if(mainWindow.isAutoLabelNodes()) {
 			// Make a Node Label
 			if(LabelTextField.getText().isEmpty()) {
 				nodeLabel = "Node" + Integer.toString(numNodes + 1);
 			}
 			// Retrieve the node label
 			else {
 				nodeLabel = LabelTextField.getText();
 			}
 		}
 		// Otherwise...
 		else {
 			// They need to enter a Node Label
 			if(LabelTextField.getText().isEmpty()) {
				nodeLabel = "";
 				//JOptionPane.showMessageDialog(AddNodePanel.this, "Please specify a Node Label!", "Attention!", JOptionPane.WARNING_MESSAGE);
 				//return;
 			}
 			// Retrieve the node label
 			else {
 				nodeLabel = LabelTextField.getText();
 			}
 		}
 
 		// Setup the new Node
 		Node newNode = new Node(Integer.toString(numNodes), nodeLabel, (String)NodeShapeComboBox.getSelectedItem(), null);
 		// Set the font
 		newNode.mSetFont(Font.decode((String)LabelFontComboBox.getSelectedItem()));
 		//Set the colors
 		newNode.mSetFillColor(fillColor);
 		newNode.mSetLabelColor(labelColor);
 		newNode.mSetBorderColor(borderColor);
 
 		// Try to add the node
 		try {
 			currentGraph.mAddNode(newNode);
 		} catch (CannotAddNodeException e) {
 			e.printStackTrace();
 		}
 
 		// If the user wants to Auto Connect Nodes - Connect the nodes!
 		if(mainWindow.isAutoConnectNodes()) {
 			// Make sure there was a node previous to the one being added
 			if(numNodes > 0) {
 				// If auto Label Edges is on
 				if(mainWindow.isAutoLabelEdges()) {
 					// Make a Edge Label
 					edgeLabel = "Edge" + Integer.toString(numEdges + 1);
 				}
 				// Otherwise...
 				else {
 					// No label!
 					edgeLabel = "";
 				}
 
 				// Try to add the edge
 				try {
 					//KMW Note: do all of this in the try, because if it fails to create the edge we don't want to
 					//          add it to the edge list.
 					Edge.mValidateEdge(Integer.toString(numEdges), currentGraph.mGetNodeList().get(numNodes - 1), newNode, currentGraph);
 					// Create the new edge
 					Edge newEdge = new Edge(Integer.toString(numEdges), edgeLabel, currentGraph.mGetNodeList().get(numNodes - 1), newNode, "NODIRECTION", "SOLID");
 					newEdge.mSetPairPosition(currentGraph.mCheckForEdgeTwin(newEdge));
 					
 					if(newEdge != null) {
 						currentGraph.mAddEdge(newEdge);
 					}
 				} catch (CannotAddEdgeException e) {
 					JOptionPane errorDialog = new JOptionPane();
 					JOptionPane.showMessageDialog(errorDialog, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 					e.printStackTrace();
 				}
 			}
 		}
 
 		if(nodeLabel == null) nodeLabel = "No Label";
 		// Add to the AddEdge lists
 		AddEdgePanel.SelectEndNodeComboBox.addItem(Integer.toString(numNodes) + " - " + nodeLabel);
 		AddEdgePanel.SelectStartNodeComboBox.addItem(Integer.toString(numNodes) + " - " + nodeLabel);
 
 		// Redraw the graph with the new node
 		GraphHelper.mDrawForceDirectedGraph(currentPanel);
 		currentPanel.repaint();
 		int newWidth = GraphHelper.mGetPreferredImageWidth(currentGraph);
 		currentPanel.setPreferredSize(new Dimension(newWidth, newWidth));
 		currentPane.revalidate();
 		//mainWindow.resetSidePane();
 	}
 
 	// Variables declarations
 	private JPanel AddNodePanel;
 	private JLabel AddNodeTitleLabel;
 	private ColorPickerButton BorderColorBtn;
 	private JLabel BorderColorLabel;
 	private ColorPickerButton FillColorBtn;
 	private JLabel FillColorLabel;
 	private ColorPickerButton LabelColorBtn;
 	private JLabel LabelColorLabel;
 	private JComboBox LabelFontComboBox;
 	private JLabel LabelFontLabel;
 	private JLabel LabelSubtitleLabel;
 	private JTextField LabelTextField;
 	private JLabel LabelTextLabel;
 	private JComboBox NodeShapeComboBox;
 	private JLabel NodeShapeLabel;
 	private JLabel NodeSubtitleLabel;
 	private Box.Filler filler1;
 	private JButton addButton;
 	private JSeparator paneSeparator;
 
 	protected JTabbedPane mainWindowTabbedPane;
 	protected JScrollPane currentPane;
 	protected GraphPanel currentPanel;
 	protected Graph currentGraph;
 	protected String nodeLabel;
 	protected String edgeLabel;
 
 	protected Color labelColor;
 	protected Color fillColor;
 	protected Color borderColor;
 }
