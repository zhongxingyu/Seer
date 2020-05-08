 package manager.panel;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.UIManager;
 import javax.swing.border.EtchedBorder;
 
 import manager.util.OverlayPanel;
 import manager.util.WhiteLabel;
 import Utils.Constants;
 
 /**
*
* Authorship: Aaron Harris
 */
 
 public class PartsManagerPanel extends JPanel implements ActionListener {
 	private JPanel pnlButtons;
 	private JPanel pnlView;
 	private JPanel pnlEdit;
 	private JPanel pnlAdd;
 	private JComboBox cbPart;
 	private String[] backupFields; // used for temporarily storing old field data in case a user wants to revert
 	private JTextField tfName;
 	private JButton btnNewPart;
 	private JSpinner spinner;
 	private JTextArea textArea;
 	private JComboBox cbImges;
 	private JButton btnEditPartType;
 	private JButton btnDeletePartType;
 	private JButton btnCreatePartType;
 	private JButton btnClearFields;
 	private JButton btnSaveChanges;
 	private JButton btnCnclChanges;
 	
 	/**
 	 * Create the panel.
 	 */
 	public PartsManagerPanel() {
 		setLayout(new GridLayout(1, 1));
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		JPanel managerPanel = new JPanel();
 		managerPanel.setOpaque(false);
 		
 		add(tabbedPane);
 		tabbedPane.addTab("Part Manager", managerPanel);
 		managerPanel.setLayout(new BorderLayout(0, 0));
 		
 		JPanel pnlPartChooser = new JPanel();
 		pnlPartChooser.setOpaque(false);
 		managerPanel.add(pnlPartChooser, BorderLayout.NORTH);
 		
 		cbPart = new JComboBox();
 		cbPart.addActionListener(this);
 		pnlPartChooser.add(cbPart);
 		
 		btnNewPart = new JButton("New Part Type");
 		btnNewPart.addActionListener(this);
 		pnlPartChooser.add(btnNewPart);
 		
 		JPanel pnlForm = new OverlayPanel();
 		
 		// pnlForm.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		managerPanel.add(pnlForm, BorderLayout.CENTER);
 		GridBagLayout gbl_pnlForm = new GridBagLayout();
 		gbl_pnlForm.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0};
 		gbl_pnlForm.columnWeights = new double[]{0.0, 0.0};
 //		gbl_pnlForm.columnWidths = new int[]{0, 0, 0};
 //		gbl_pnlForm.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
 //		gbl_pnlForm.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 //		gbl_pnlForm.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		pnlForm.setLayout(gbl_pnlForm);
 		
 		WhiteLabel lblNumber = new WhiteLabel("Part No:");
 		GridBagConstraints gbc_lblNumber = new GridBagConstraints();
 		gbc_lblNumber.anchor = GridBagConstraints.EAST;
 		gbc_lblNumber.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNumber.gridx = 0;
 		gbc_lblNumber.gridy = 0;
 		pnlForm.add(lblNumber, gbc_lblNumber);
 		
 		spinner = new JSpinner();
 		spinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(0), null, new Integer(1)));
 		GridBagConstraints gbc_spinner = new GridBagConstraints();
 		gbc_spinner.anchor = GridBagConstraints.WEST;
 		gbc_spinner.insets = new Insets(0, 0, 5, 0);
 		gbc_spinner.gridx = 1;
 		gbc_spinner.gridy = 0;
 		pnlForm.add(spinner, gbc_spinner);
 		
 		WhiteLabel lblName = new WhiteLabel("Name:");
 		GridBagConstraints gbc_lblName = new GridBagConstraints();
 		gbc_lblName.anchor = GridBagConstraints.EAST;
 		gbc_lblName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblName.gridx = 0;
 		gbc_lblName.gridy = 1;
 		pnlForm.add(lblName, gbc_lblName);
 		
 		tfName = new JTextField();
 		GridBagConstraints gbc_tfName = new GridBagConstraints();
 		gbc_tfName.anchor = GridBagConstraints.WEST;
 		gbc_tfName.insets = new Insets(0, 0, 5, 0);
 		gbc_tfName.gridx = 1;
 		gbc_tfName.gridy = 1;
 		pnlForm.add(tfName, gbc_tfName);
 		tfName.setColumns(10);
 		
 		WhiteLabel lblDescription = new WhiteLabel("Description:");
 		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
 		gbc_lblDescription.anchor = GridBagConstraints.EAST;
 		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDescription.gridx = 0;
 		gbc_lblDescription.gridy = 2;
 		pnlForm.add(lblDescription, gbc_lblDescription);
 		
 		JPanel panel = new JPanel();
 		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.insets = new Insets(0, 0, 5, 0);
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 2;
 		pnlForm.add(panel, gbc_panel);
 		panel.setLayout(new GridLayout(1, 1));
 		
 		textArea = new JTextArea();
 		textArea.setFont(UIManager.getFont("Button.font"));
 		panel.add(textArea);
 		
 		WhiteLabel lblImagePath = new WhiteLabel("Image:");
 		GridBagConstraints gbc_lblImagePath = new GridBagConstraints();
 		gbc_lblImagePath.anchor = GridBagConstraints.EAST;
 		gbc_lblImagePath.insets = new Insets(0, 0, 0, 5);
 		gbc_lblImagePath.gridx = 0;
 		gbc_lblImagePath.gridy = 3;
 		pnlForm.add(lblImagePath, gbc_lblImagePath);
 		
 		cbImges = new JComboBox();
 		GridBagConstraints gbc_cbImges = new GridBagConstraints();
 		gbc_cbImges.anchor = GridBagConstraints.WEST;
 		gbc_cbImges.gridx = 1;
 		gbc_cbImges.gridy = 3;
 		pnlForm.add(cbImges, gbc_cbImges);
 		
 		pnlButtons = new JPanel();
 		pnlButtons.setOpaque(false);
 		managerPanel.add(pnlButtons, BorderLayout.SOUTH);
 		pnlButtons.setLayout(new CardLayout(0, 0));
 		
 		pnlView = new JPanel();
 		pnlView.setOpaque(false);
 		pnlButtons.add(pnlView, "View Part Type");
 		
 		btnEditPartType = new JButton("Edit Part Type");
 		btnEditPartType.addActionListener(this);
 		pnlView.add(btnEditPartType);
 		
 		btnDeletePartType = new JButton("Delete Part Type");
 		btnDeletePartType.addActionListener(this);
 		pnlView.add(btnDeletePartType);
 		
 		pnlEdit = new JPanel();
 		pnlEdit.setOpaque(false);
 		pnlButtons.add(pnlEdit, "Edit Part Type");
 		
 		btnSaveChanges = new JButton("Save Changes");
 		btnSaveChanges.addActionListener(this);
 		pnlEdit.add(btnSaveChanges);
 		
 		btnCnclChanges = new JButton("Cancel Changes");
 		btnCnclChanges.addActionListener(this);
 		pnlEdit.add(btnCnclChanges);
 		
 		pnlAdd = new JPanel();
 		pnlAdd.setOpaque(false);
 		pnlButtons.add(pnlAdd, "Add Part Type");
 		
 		btnCreatePartType = new JButton("Create Part Type");
 		btnCreatePartType.addActionListener(this);
 		pnlAdd.add(btnCreatePartType);
 		
 		btnClearFields = new JButton("Clear Fields");
 		btnClearFields.addActionListener(this);
 		pnlAdd.add(btnClearFields);
 	}
 	
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == cbPart) {
 			viewPart((String) cbPart.getSelectedItem());
 		} else if (ae.getSource() == btnNewPart) {
 			showAddPanel();
 		} else if (ae.getSource() == btnEditPartType) {
 			editPart((String) cbPart.getSelectedItem());
 		} else if (ae.getSource() == btnDeletePartType) {
 			deletePart((String) cbPart.getSelectedItem());
 		} else if (ae.getSource() == btnSaveChanges) {
 //		TODO: savePartEdit((String) cbPart.getSelectedItem());
 		} else if (ae.getSource() == btnCnclChanges) {
 			cancelEdit();
 			viewPart((String) cbPart.getSelectedItem());
 		} else if (ae.getSource() == btnCreatePartType) {
 			createPart();
 		} else if (ae.getSource() == btnClearFields) {
 			clearFields();
 		}
 	}
 	
 	protected void showAddPanel() {
 		// change combo box to "Select Part..."
 		CardLayout cl = (CardLayout)(pnlButtons.getLayout());
         cl.show(pnlButtons, "Add Part Type");
         clearFields();
         enableFields();
 	}
 	
 	protected void viewPart(String part) {
 		CardLayout cl = (CardLayout)(pnlButtons.getLayout());
         cl.show(pnlButtons, "View Part Type");
         disableFields();
         // Show the PartType data in form elements
 	}
 	
 	protected void editPart(String part) {
 		CardLayout cl = (CardLayout)(pnlButtons.getLayout());
         cl.show(pnlButtons, "Edit Part Type");
         // "back-up" the original values in case a user decides to cancel changes
         backupFields[0] = tfName.getText();
         enableFields();        
 	}
 	
 	protected void cancelEdit() {
 		if (backupFields[0] != null) tfName.setText(backupFields[0]);
 	}
 	
 	protected void createPart() {
 		String partName = tfName.getText();
 		// creates the part
 		viewPart(partName);		
 	}
 	
 	protected void deletePart(String part) {
         int choice = JOptionPane.showConfirmDialog(null,
         		"Are you sure you want to delete this part type?\nNote: the action cannot be undone.",
                 "Delete Part",
                 JOptionPane.YES_NO_OPTION);
         if (choice == 0) ; // delete part
         // remove the part option from cbPart
         // view the next item in the list, or no item if there are no items in the list
 	}
 	
 	protected void clearFields() {
 		tfName.setText("");
 	}
 	
 	protected void toggleFields() {
 		if (tfName.isEnabled()) tfName.setEnabled(false);
 		else tfName.setEnabled(true);
 	}
 	protected void enableFields() {
 		if (tfName.isEnabled()) toggleFields();
 	}
 	protected void disableFields() {
 		if (!tfName.isEnabled()) toggleFields();
 	}
 	
 	public void paintComponent(Graphics gg) {
 		Graphics2D g = (Graphics2D) gg;
 		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
 	}
 }
