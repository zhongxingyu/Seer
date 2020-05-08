 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package autograph.ui;
 
 import javax.swing.*;
 
 public class EditNodeEdgePanel extends JPanel {
 
 	/**
 	 * Creates new form EditNodeEdgePanel
 	 */
 	public EditNodeEdgePanel() {
 		initComponents();
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 */
 	@SuppressWarnings("unchecked")
 	private void initComponents() {
 
 		setBackground(new java.awt.Color(255, 255, 255));
 		setMinimumSize(new java.awt.Dimension(200, 300));
 		
 		// Edit Pane
 		EditPane = new JPanel();
 		EditPane.setPreferredSize(new java.awt.Dimension(200, 512));
 		
 		// Edit Title Label
 		EditTitleLabel = new JLabel();
 		EditTitleLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); 
 		EditTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		EditTitleLabel.setText("Edit Nodes/Edges");
 		
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
 		LabelFontComboBox.setModel(new DefaultComboBoxModel(new String[] { "Times New Roman", "Ariel", "Some other font", "More fonts", "Wingdings" }));
 		
 		// Label Color Label
 		LabelColorLabel = new JLabel();
 		LabelColorLabel.setText("Label Color");
 		
 		// Label Color Button
 		LabelColorBtn = new JButton();
 		LabelColorBtn.setText("[  ]");
 		LabelColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		
 		// Separator 1
 		jSeparator1 = new JSeparator();
 		
 		// Node Subtitle Label
 		NodeSubtitleLabel = new JLabel();
 		NodeSubtitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); 
 		NodeSubtitleLabel.setText("Node");
 		
 		// Node Shape Label
 		NodeShapeLabel = new JLabel();
 		NodeShapeLabel.setText("Node Shape");
 		
 		// Node Shape Combo Box
 		NodeShapeComboBox = new JComboBox();
 		NodeShapeComboBox.setModel(new DefaultComboBoxModel(new String[] { "AA", "BB", "CC", "DD", "EE", "FF" }));
 		
 		// Border Color Label
 		BorderColorLabel = new JLabel();
 		BorderColorLabel.setText("Border Color");
 		
 		// Border Color Button
 		BorderColorBtn = new JButton();
 		BorderColorBtn.setText("[  ]");
 		BorderColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		
 		// Fill Color Label
 		FillColorLabel = new JLabel();
 		FillColorLabel.setText("Fill Color");
 		
 		// Fill Color Button
 		FillColorBtn = new JButton();
 		FillColorBtn.setText("[  ]");
 		FillColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		
 		// Separator 2
 		jSeparator2 = new JSeparator();
 		
 		// Edge Subtitle Label
 		EdgeSubtitleLabel = new JLabel();
 		EdgeSubtitleLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); 
 		EdgeSubtitleLabel.setText("Edge");
 		
 		// Edge Design Label
 		EdgeDesignLabel = new JLabel();
 		EdgeDesignLabel.setText("Edge Design");
 		
 		// Edge Design Combo Box
 		EdgeDesignComboBox = new JComboBox();
 		EdgeDesignComboBox.setModel(new DefaultComboBoxModel(new String[] { "____", "_ _ _", "- - - -", "......", "===", " " }));
 		
 		// End Shape Label
 		EndShapeLabel = new JLabel();
 		EndShapeLabel.setText("End Shape");
 		
 		// End Shape Combo Box
 		EndShapeComboBox = new JComboBox();
 		EndShapeComboBox.setModel(new DefaultComboBoxModel(new String[] { "A-A", "A-B", "A-C", "B-B", "B-C", "C-C" }));
 		
 		// Edge Color Label
 		EdgeColorLabel = new JLabel();
 		EdgeColorLabel.setText("Edge Color");
 		
 		// Edge Color Button
 		EdgeColorBtn = new JButton();
 		EdgeColorBtn.setText("[  ]");
 		EdgeColorBtn.setMargin(new java.awt.Insets(2, 5, 2, 5));
 		
 		// Save Button
 		SaveButton = new JButton();
		SaveButton.setText("Save Changes");
 		
 		// Panel Filler
 		filler1 = new Box.Filler(new java.awt.Dimension(200, 0), new java.awt.Dimension(200, 0), new java.awt.Dimension(200, 32767));	
 
 		// Layout
 		GroupLayout EditPaneLayout = new GroupLayout(EditPane);
 		EditPane.setLayout(EditPaneLayout);
 		EditPaneLayout.setHorizontalGroup(
 				EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(EditPaneLayout.createSequentialGroup()
 						.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addComponent(LabelSubtitleLabel)
 								.addComponent(NodeSubtitleLabel)
 								.addComponent(EdgeSubtitleLabel))
 								.addGap(0, 0, Short.MAX_VALUE))
 								.addGroup(EditPaneLayout.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 												.addComponent(jSeparator2, GroupLayout.Alignment.TRAILING)
 												.addComponent(EditTitleLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 												.addComponent(LabelTextField)
 												.addComponent(LabelFontComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 												.addGroup(EditPaneLayout.createSequentialGroup()
 														.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																.addGroup(EditPaneLayout.createSequentialGroup()
 																		.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																				.addComponent(NodeShapeLabel)
 																				.addComponent(LabelColorLabel))
 																				.addGap(26, 26, 26)
 																				.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																						.addComponent(LabelColorBtn)
 																						.addComponent(NodeShapeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 																						.addGroup(EditPaneLayout.createSequentialGroup()
 																								.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																										.addComponent(LabelTextLabel)
 																										.addComponent(LabelFontLabel)
 																										.addComponent(BorderColorLabel)
 																										.addComponent(FillColorLabel))
 																										.addGap(24, 24, 24)
 																										.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																												.addComponent(FillColorBtn)
 																												.addComponent(BorderColorBtn)))
 																												.addGroup(EditPaneLayout.createSequentialGroup()
 																														.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																																.addComponent(EdgeDesignLabel)
 																																.addComponent(EndShapeLabel)
 																																.addComponent(EdgeColorLabel))
 																																.addGap(25, 25, 25)
 																																.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																																		.addComponent(EdgeColorBtn)
 																																		.addComponent(EndShapeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 																																		.addComponent(EdgeDesignComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
 																																		.addGap(0, 0, Short.MAX_VALUE))
 																																		.addComponent(jSeparator1)
 																																		.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditPaneLayout.createSequentialGroup()
 																																				.addGap(0, 0, Short.MAX_VALUE)
 																																				.addComponent(SaveButton)))
 																																				.addContainerGap())
 				);
 		EditPaneLayout.setVerticalGroup(
 				EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(EditPaneLayout.createSequentialGroup()
 						.addComponent(EditTitleLabel)
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
 						.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 								.addComponent(LabelColorLabel)
 								.addComponent(LabelColorBtn))
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 								.addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
 								.addGap(3, 3, 3)
 								.addComponent(NodeSubtitleLabel)
 								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 										.addComponent(NodeShapeLabel)
 										.addComponent(NodeShapeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 										.addGap(18, 18, 18)
 										.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 												.addComponent(BorderColorLabel)
 												.addComponent(BorderColorBtn))
 												.addGap(18, 18, 18)
 												.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 														.addComponent(FillColorLabel)
 														.addComponent(FillColorBtn))
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 														.addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
 														.addGap(11, 11, 11)
 														.addComponent(EdgeSubtitleLabel)
 														.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 														.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 																.addComponent(EdgeDesignLabel)
 																.addComponent(EdgeDesignComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 																.addGap(18, 18, 18)
 																.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 																		.addComponent(EndShapeLabel)
 																		.addComponent(EndShapeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 																		.addGap(18, 18, 18)
 																		.addGroup(EditPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 																				.addComponent(EdgeColorLabel)
 																				.addComponent(EdgeColorBtn))
 																				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																				.addComponent(SaveButton))
 				);
 
 		GroupLayout layout = new GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(filler1, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 0, Short.MAX_VALUE))
 						.addGroup(layout.createSequentialGroup()
 								.addComponent(EditPane, GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
 								.addContainerGap())
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addComponent(EditPane, GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
 						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 						.addComponent(filler1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addGap(0, 0, 0))
 				);
 	}
 	
 	// Variables declaration - do not modify
 	private JButton BorderColorBtn;
 	private JLabel BorderColorLabel;
 	private JButton EdgeColorBtn;
 	private JLabel EdgeColorLabel;
 	private JComboBox EdgeDesignComboBox;
 	private JLabel EdgeDesignLabel;
 	private JLabel EdgeSubtitleLabel;
 	private JLabel EditTitleLabel;
 	private JComboBox EndShapeComboBox;
 	private JLabel EndShapeLabel;
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
 	private JButton SaveButton;
 	private JPanel EditPane;
 	private JSeparator jSeparator1;
 	private JSeparator jSeparator2;
 }
