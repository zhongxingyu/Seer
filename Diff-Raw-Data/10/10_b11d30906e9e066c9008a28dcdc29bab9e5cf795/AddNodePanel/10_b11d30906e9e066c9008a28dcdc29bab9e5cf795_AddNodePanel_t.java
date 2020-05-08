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
 
 import autograph.Graph;
 import autograph.GraphHelper;
 import autograph.GraphPanel;
 import autograph.Node;
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
 		
 		// Panel Scroll Pane
 		panelScrollPane = new JScrollPane();
 		panelScrollPane.setBorder(null);
 		panelScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		
 		// Add Node Panel
 		AddNodePanel = new JPanel();
 		AddNodePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
 		
 		// Add Node Title Label
 		AddNodeTitleLabel = new JLabel();
 		AddNodeTitleLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
 		AddNodeTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		AddNodeTitleLabel.setText("Add Node");
 		
 		// Label Subtitle Label
 		LabelSubtitleLabel = new JLabel();
 		LabelSubtitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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
 		LabelColorBtn = new JButton();
 		LabelColorBtn.setText("Select Color");
 		LabelColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		LabelColorBtn.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				LabelColorBtnActionPerformed(evt);
 			}
 		});
 		
 		// Node Subtitle Label
 		NodeSubtitleLabel = new JLabel();
 		NodeSubtitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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
 		FillColorBtn = new JButton();
 		FillColorBtn.setText("Select Color");
 		FillColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		FillColorBtn.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				FillColorBtnActionPerformed(evt);
 			}
 		});
 		
 		// Node Shape Combo Box
 		NodeShapeComboBox = new JComboBox();
 		NodeShapeComboBox.setModel(new DefaultComboBoxModel(new String[] { "Circle", "Oval", "Square", "Rectangle", "Triangle" }));
 		
 		// Border Color Button
 		BorderColorBtn = new JButton();
 		BorderColorBtn.setText("Select Color");
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
 												.addComponent(LabelTextField, 0, GroupLayout.DEFAULT_SIZE, 198)
 												.addComponent(LabelFontComboBox, 0, GroupLayout.DEFAULT_SIZE, 198)
 												.addGroup(AddNodePanelLayout.createSequentialGroup()
 														.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
 																						.addComponent(NodeShapeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 																						.addGroup(AddNodePanelLayout.createSequentialGroup()
 																								.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																										.addComponent(NodeShapeLabel)
 																										.addComponent(LabelColorLabel))
 																										.addGap(18, 18, 18)
 																										.addGroup(AddNodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																												.addComponent(LabelColorBtn))))
 																												.addGap(0, 45, Short.MAX_VALUE))
 																												.addComponent(paneSeparator)
 																												.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AddNodePanelLayout.createSequentialGroup()
 																														.addGap(0, 0, Short.MAX_VALUE)
 																														.addComponent(addButton)))
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
 
 		panelScrollPane.setViewportView(AddNodePanel);
 
 		javax.swing.GroupLayout layout = new GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(filler1, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 0, Short.MAX_VALUE))
 						.addGroup(layout.createSequentialGroup()
 								.addComponent(panelScrollPane, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
 								.addContainerGap())
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
						.addComponent(panelScrollPane, GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
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
 		}
 	}
 
 	/*
 	 * The Fill Color Button was clicked
 	 */
 	protected void FillColorBtnActionPerformed(MouseEvent evt) {
 		Color newColor = JColorChooser.showDialog(AddNodePanel.this, "Choose Fill Color", fillColor);
 
 		if (newColor != null) {
 			fillColor = newColor;
 		}
 	}
 
 	/*
 	 * The Border Color Button was clicked
 	 */
 	protected void BorderColorBtnActionPerformed(MouseEvent evt) {
 		Color newColor = JColorChooser.showDialog(AddNodePanel.this, "Choose Border Color", borderColor);
 
 		if (newColor != null) {
 			borderColor = newColor;
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
 				nodeLabel = null;
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
 
 		// Redraw the graph with the new node
 		currentPanel.repaint();
 		int newWidth = GraphHelper.mGetPreferredImageWidth(currentGraph);
 		currentPanel.setPreferredSize(new Dimension(newWidth, newWidth));
 		currentPane.revalidate();
 		GraphHelper.mDrawForceDirectedGraph(currentPanel);
 		//mainWindow.resetSidePane();
 	}
 
 	// Variables declarations
 	private JPanel AddNodePanel;
 	private JLabel AddNodeTitleLabel;
 	private JButton BorderColorBtn;
 	private JLabel BorderColorLabel;
 	private JButton FillColorBtn;
 	private JLabel FillColorLabel;
 	private JButton LabelColorBtn;
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
 	private JScrollPane panelScrollPane;
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
