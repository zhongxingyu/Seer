 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package autograph.ui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.MouseEvent;
 
 import javax.swing.*;
 
 import autograph.Graph;
 import autograph.GraphPanel;
 import autograph.Node;
 
 public class EditNodePanel extends JPanel {
 
 	/**
 	 * Creates new form EditNodePanel
 	 */
 	public EditNodePanel() {
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
 
 		// Edit Node Panel
 		EditNodePanel = new JPanel();
 		EditNodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
 		EditNodePanel.setPreferredSize(new java.awt.Dimension(200, 512));
 
 		// Edit Node Title Label
 		EditNodeTitleLabel = new JLabel();
 		EditNodeTitleLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); 
 		EditNodeTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		EditNodeTitleLabel.setText("Edit Node");
 
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
 
 		// Edit Node Button
 		SaveButton = new JButton();
 		SaveButton.setText("Save Changes");
 		SaveButton.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				SaveBtnActionPerformed(evt);
 			}
 		});
 
 		// Filler
 		panelFiller = new Box.Filler(new java.awt.Dimension(200, 0), new java.awt.Dimension(200, 0), new java.awt.Dimension(200, 32767));
 
 		// Layout
 		javax.swing.GroupLayout EditNodePanelLayout = new GroupLayout(EditNodePanel);
 		EditNodePanel.setLayout(EditNodePanelLayout);
 		EditNodePanelLayout.setHorizontalGroup(
 				EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(EditNodePanelLayout.createSequentialGroup()
 						.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addComponent(LabelSubtitleLabel)
 								.addComponent(NodeSubtitleLabel))
 								.addGap(0, 0, Short.MAX_VALUE))
 								.addGroup(EditNodePanelLayout.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 												.addComponent(EditNodeTitleLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 												.addComponent(LabelTextField, 0, GroupLayout.DEFAULT_SIZE, 190)
 												.addComponent(LabelFontComboBox, 0, GroupLayout.DEFAULT_SIZE, 190)
 												.addComponent(paneSeparator, 0, GroupLayout.DEFAULT_SIZE, 186)
 												.addGroup(javax.swing.GroupLayout.Alignment.LEADING, EditNodePanelLayout.createSequentialGroup()
 														.addGap(0, 0, Short.MAX_VALUE)
 														.addComponent(SaveButton))
 														.addGroup(EditNodePanelLayout.createSequentialGroup()
 																.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
 																		.addGroup(EditNodePanelLayout.createSequentialGroup()
 																				.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																						.addComponent(LabelTextLabel)
 																						.addComponent(LabelFontLabel)
 																						.addComponent(BorderColorLabel)
 																						.addComponent(FillColorLabel))
 																						.addGap(18, 18, 18)
 																						.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																								.addComponent(FillColorBtn)
 																								.addComponent(BorderColorBtn)
 																								.addComponent(LabelColorBtn)
 																								.addComponent(NodeShapeComboBox, 0, GroupLayout.DEFAULT_SIZE, 120)))
 																								.addGroup(EditNodePanelLayout.createSequentialGroup()
 																										.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																												.addComponent(NodeShapeLabel)
 																												.addComponent(LabelColorLabel))
 																												.addGap(18, 18, 18)
 																												.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING))))
 																												.addGap(0, 0, Short.MAX_VALUE)))
 																												.addContainerGap())
 
 				);
 		EditNodePanelLayout.setVerticalGroup(
 				EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(EditNodePanelLayout.createSequentialGroup()
 						.addComponent(EditNodeTitleLabel)
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
 						.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 								.addComponent(LabelColorLabel)
 								.addComponent(LabelColorBtn))
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 								.addComponent(paneSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
 								.addGap(3, 3, 3)
 								.addComponent(NodeSubtitleLabel)
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 										.addComponent(NodeShapeLabel)
 										.addComponent(NodeShapeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 										.addGap(18, 18, 18)
 										.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(BorderColorLabel)
 												.addComponent(BorderColorBtn))
 												.addGap(18, 18, 18)
 												.addGroup(EditNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 														.addComponent(FillColorLabel)
 														.addComponent(FillColorBtn))
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
 														.addComponent(SaveButton)
 														.addContainerGap())
 				);
 
 		javax.swing.GroupLayout layout = new GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(panelFiller, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 0, Short.MAX_VALUE))
 						.addGroup(layout.createSequentialGroup()
 								.addComponent(EditNodePanel, GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
 								.addContainerGap())
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(EditNodePanel, GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
 						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(panelFiller, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 0, 0))
 				);
 	}
 
 	// The Save Button is clicked
 	protected void SaveBtnActionPerformed(MouseEvent evt) {
 		// Graph/Window pointers
 		mainWindowTabbedPane = mainWindow.getMainWindowPane();
 		JScrollPane currentPane = (JScrollPane)mainWindowTabbedPane.getSelectedComponent();
 		GraphPanel currentPanel = (GraphPanel)currentPane.getViewport().getView();
 		Graph currentGraph = currentPanel.mGetGraph();
 		int numNodes = currentGraph.mGetNodeList().size();
 
 		// If more than 1 node is selected
 		if(currentGraph.vSelectedItems.mGetSelectedNodes().size() > 1) {
 			Node currentNode;
 
 			for(int i = 0; i < currentGraph.vSelectedItems.mGetSelectedNodes().size(); i++) {
 
 				// Get the current node
 				currentNode = currentGraph.vSelectedItems.mGetSelectedNodes().get(i);
 
 				// Set the node's new attributes
 				currentNode.mSetBorderColor(borderColor);
 				currentNode.mSetFillColor(fillColor);
 				currentNode.mSetLabelColor(labelColor);
 				currentNode.mSetFont(Font.decode((String)LabelFontComboBox.getSelectedItem()));
 				currentNode.mSetShape((String)NodeShapeComboBox.getSelectedItem());
 
 				// If auto Label Nodes is on
 				if(mainWindow.isAutoLabelNodes()) {
 					// Make a Node Label
 					if(LabelTextField.getText().isEmpty()) {
 						nodeLabel = "Node" + currentNode.mGetId();
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
 
 				currentNode.mSetLabel(nodeLabel);
 			}
 		}
 		// Otherwise only 1 node is selected
 		else {
 			// Get the current node
 			Node currentNode = currentGraph.vSelectedItems.mGetSelectedNodes().get(0);
 
 			// Set the node's new attributes
 			currentNode.mSetBorderColor(borderColor);
 			currentNode.mSetFillColor(fillColor);
 			currentNode.mSetLabelColor(labelColor);
 			currentNode.mSetFont(Font.decode((String)LabelFontComboBox.getSelectedItem()));
 			currentNode.mSetShape((String)NodeShapeComboBox.getSelectedItem());
 
 			// If auto Label Nodes is on
 			if(mainWindow.isAutoLabelNodes()) {
 				// Make a Node Label
 				if(LabelTextField.getText().isEmpty()) {
 					nodeLabel = "Node" + currentNode.mGetId();
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
 
 			currentNode.mSetLabel(nodeLabel);
 		}
 		
 		// Clear the old list
 		AddEdgePanel.SelectEndNodeComboBox.removeAllItems();
 		AddEdgePanel.SelectEndNodeComboBox.addItem("");
 		AddEdgePanel.SelectStartNodeComboBox.removeAllItems();
 		AddEdgePanel.SelectStartNodeComboBox.addItem("");
 		// Add to the AddEdge lists
 		for(int i = 0; i < numNodes; i++) {
 			AddEdgePanel.SelectEndNodeComboBox.addItem(currentGraph.mGetNodeList().get(i).mGetId() + " - " + currentGraph.mGetNodeList().get(i).mGetLabel());
 			AddEdgePanel.SelectStartNodeComboBox.addItem(currentGraph.mGetNodeList().get(i).mGetId() + " - " + currentGraph.mGetNodeList().get(i).mGetLabel());
 		}
 		
 		currentGraph.vSelectedItems.mClearSelectedItems();
 		mainWindow.resetSidePane();
 		currentPanel.repaint();
 	}
 
 	/*
 	 * The Label Color Button was clicked
 	 */
 	protected void LabelColorBtnActionPerformed(MouseEvent evt) {
 		Color newColor = JColorChooser.showDialog(EditNodePanel.this, "Choose Label Color", labelColor);
 
 		if (newColor != null) {
 			labelColor = newColor;
 			LabelColorBtn.setColor(labelColor);
 		}
 	}
 
 	/*
 	 * The Fill Color Button was clicked
 	 */
 	protected void FillColorBtnActionPerformed(MouseEvent evt) {
 		Color newColor = JColorChooser.showDialog(EditNodePanel.this, "Choose Fill Color", fillColor);
 
 		if (newColor != null) {
 			fillColor = newColor;
 			FillColorBtn.setColor(fillColor);
 		}
 	}
 
 	/*
 	 * The Border Color Button was clicked
 	 */
 	protected void BorderColorBtnActionPerformed(MouseEvent evt) {
 		Color newColor = JColorChooser.showDialog(EditNodePanel.this, "Choose Border Color", borderColor);
 
 		if (newColor != null) {
 			borderColor = newColor;
 			BorderColorBtn.setColor(borderColor);
 		}
 	}
 
 	protected void updateFields() {
 		// Graph/Window pointers
 		mainWindowTabbedPane = mainWindow.getMainWindowPane();
 		JScrollPane currentPane = (JScrollPane)mainWindowTabbedPane.getSelectedComponent();
 		GraphPanel currentPanel = (GraphPanel)currentPane.getViewport().getView();
 		Graph currentGraph = currentPanel.mGetGraph();
 
 		// If there is more than 1 node selected
 		if(currentGraph.vSelectedItems.mGetSelectedNodes().size() > 1) {
 
 			// Set the values to defaults, as multiple nodes are selected
 			LabelTextField.setText("");
 			labelColor = Color.BLACK;
 			fillColor = Color.WHITE;
 			borderColor = Color.BLACK;
 			LabelFontComboBox.setSelectedItem("Courier");
 			NodeShapeComboBox.setSelectedItem("Circle");
 		}
 		// Otherwise... (only one node is selected)
 		else {
 			Node selectedNode = currentGraph.vSelectedItems.mGetSelectedNodes().get(0); 
 			LabelTextField.setText(selectedNode.mGetLabel());
 			labelColor = selectedNode.mGetLabelColor();
 			fillColor = selectedNode.mGetFillColor();
 			borderColor = selectedNode.mGetBorderColor();
 			String temp = selectedNode.mGetShape().toString();
 			temp = makeRealWord(temp);
 			NodeShapeComboBox.setSelectedItem(temp);
 			LabelFontComboBox.setSelectedItem(selectedNode.mGetFont().getFontName());
 			NodeShapeComboBox.revalidate();
 		}
 	}
 
 	private String makeRealWord(String fakeWord) {
 		String realWord = fakeWord.substring(0,1).toUpperCase();
 		realWord += fakeWord.substring(1).toLowerCase();
 		return realWord;
 	}
 
 	// Variables declarations
 	private JPanel EditNodePanel;
 	private JLabel EditNodeTitleLabel;
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
 	private Box.Filler panelFiller;
 	private JButton SaveButton;
 	private JSeparator paneSeparator;
 
 	protected JTabbedPane mainWindowTabbedPane;
 	protected JScrollPane currentPane;
 	protected GraphPanel currentPanel;
 	protected Graph currentGraph;
 	protected String nodeLabel;
 
 	protected Color labelColor;
 	protected Color fillColor;
 	protected Color borderColor;
 }
