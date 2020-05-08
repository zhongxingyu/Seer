 package de.articmodding.TroubleTicket;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusListener;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 
 public class TicketEditView extends JFrame {
 
 	private static final long serialVersionUID = -2376096001972689127L;
 
 	private JPanel contentPane = null;
 	private JLabel prioLabel = new JLabel("Priorität");
 	private JLabel statusLabel = new JLabel("Status");
 	private JLabel erstellerLabel = new JLabel("Ersteller");
 	private JLabel erstellerEmailLabel = new JLabel("Ersteller E-Mail");
 	private JLabel betreffLabel = new JLabel("Betreff");
 	private JLabel textLabel = new JLabel("Text");
 	private JLabel replyTableLabel = new JLabel("bisherige Antworten");
 	private JLabel replyTextLabel = new JLabel("Antwort-Betreff");
 	private JLabel replyLabel = new JLabel("Antwort");
 	private JComboBox prioBox = new JComboBox();
 	private JComboBox statusBox = new JComboBox();
 	private JTextField erstellerField = new JTextField();
 	private JTextField erstellerEmailField = new JTextField();
 	private JTextField betreffField = new JTextField();
 	private JTextArea textArea = new JTextArea();
 	private JTable replyTable = new JTable();
 	private JTextField replyBetreff = new JTextField();
 	private JTextArea replyArea = new JTextArea();
 	private JButton okButton = new JButton("OK");
 	private JButton exitButton = new JButton("Abbrechen");
 	
 	public TicketEditView() {
 		super("Ticket");
 		setBounds(300, 250, 600, 600);
 		setContentPane(getJContentPane());
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 	}
 	/*
 	 * Getters and Setters
 	 */	
 	public void setPrioritaet(Integer prioritaet) {
 		prioBox.setSelectedIndex(prioritaet);		
 	}	
 	public int getPrioritaet() {
 		return prioBox.getSelectedIndex();
 	}
 	
 	public void setStatus(int i) {
 		statusBox.setSelectedIndex(i);
 	}
 	public int getStatus() {
 		return statusBox.getSelectedIndex();
 	}
 	
 	public void setErsteller(String ersteller) {
 		erstellerField.setText(ersteller);
 	}
 	public String getErsteller() {
 		return erstellerField.getText();
 	}
 	
 	public void setErstellerEmail(String email) {
 		erstellerEmailField.setText(email);
 	}
 	public String getErstellerEmail() {
 		return erstellerEmailField.getText();
 	}
 	
 	public void setBetreff(String betreff) {
 		betreffField.setText(betreff);
 	}
 	public String getBetreff() {
 		return betreffField.getText();
 	}
 	
 	public void setText(String text) {
 		textArea.setText(text);
 	}
 	public String getText() {
 		return textArea.getText();
 	}
 	
 	public void setReplyBetreff(String lastBetreff) {
 		replyBetreff.setText(lastBetreff);
 	}
 	public String getReplyText() {
 		return replyArea.getText();
 	}
 	
 	public void setReplyText(String reply) {
 		replyArea.setText(reply);
 	}
 	public String getReplyBetreff() {
 		return replyBetreff.getText();
 	}
 	
 	/*
 	 * other Methods and Functions
 	 */
 	private Container getJContentPane() {
 		if(contentPane == null) {
 			
 			GridBagConstraints prioLabelContraints = new GridBagConstraints();
 			prioLabelContraints.gridx = 0;
 			prioLabelContraints.gridy = 0;
 			prioLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			prioLabelContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints statusLabelContraints = new GridBagConstraints();
 			statusLabelContraints.gridx = 0;
 			statusLabelContraints.gridy = 1;
 			statusLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			statusLabelContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints erstellerLabelContraints = new GridBagConstraints();
 			erstellerLabelContraints.gridx = 0;
 			erstellerLabelContraints.gridy = 2;
 			erstellerLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			erstellerLabelContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints erstellerEmailLabelContraints = new GridBagConstraints();
 			erstellerEmailLabelContraints.gridx = 0;
 			erstellerEmailLabelContraints.gridy = 3;
 			erstellerEmailLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			erstellerEmailLabelContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints betreffLabelContraints = new GridBagConstraints();
 			betreffLabelContraints.gridx = 0;
 			betreffLabelContraints.gridy = 4;
 			betreffLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			betreffLabelContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints textLabelContraints = new GridBagConstraints();
 			textLabelContraints.gridx = 0;
 			textLabelContraints.gridy = 5;
 			textLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			textLabelContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints replyTableLabelContraints = new GridBagConstraints();
 			replyTableLabelContraints.gridx = 0;
 			replyTableLabelContraints.gridy = 6;
 			replyTableLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			replyTableLabelContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints replyTextLabelContraints = new GridBagConstraints();
 			replyTextLabelContraints.gridx = 0;
 			replyTextLabelContraints.gridy = 7;
 			replyTextLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			replyTextLabelContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints replyLabelContraints = new GridBagConstraints();
 			replyLabelContraints.gridx = 0;
 			replyLabelContraints.gridy = 8;
 			replyLabelContraints.anchor = GridBagConstraints.NORTHWEST;
 			replyLabelContraints.insets = new Insets(1, 1, 1, 1);
 
 			GridBagConstraints prioBoxContraints = new GridBagConstraints();
 			prioBoxContraints.fill = GridBagConstraints.HORIZONTAL;
 			prioBoxContraints.anchor = GridBagConstraints.WEST;
 			prioBoxContraints.gridx = 1;
 			prioBoxContraints.gridy = 0;
 			prioBoxContraints.weightx = 2;
 			prioBoxContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints statusBoxContraints = new GridBagConstraints();
 			statusBoxContraints.fill = GridBagConstraints.HORIZONTAL;
 			statusBoxContraints.anchor = GridBagConstraints.WEST;
 			statusBoxContraints.gridx = 1;
 			statusBoxContraints.gridy = 1;
 			statusBoxContraints.insets = new Insets(1, 1, 1, 1);
 			
 			GridBagConstraints erstellerTextContraints = new GridBagConstraints();
 			erstellerTextContraints.fill = GridBagConstraints.HORIZONTAL;
 			erstellerTextContraints.anchor = GridBagConstraints.WEST;
 			erstellerTextContraints.gridx = 1;
 			erstellerTextContraints.gridy = 2;
 			erstellerTextContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints erstellerEmailTextContraints = new GridBagConstraints();
 			erstellerEmailTextContraints.fill = GridBagConstraints.HORIZONTAL;
 			erstellerEmailTextContraints.anchor = GridBagConstraints.WEST;
 			erstellerEmailTextContraints.gridx = 1;
 			erstellerEmailTextContraints.gridy = 3;
 			erstellerEmailTextContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints betreffTextContraints = new GridBagConstraints();
 			betreffTextContraints.fill = GridBagConstraints.HORIZONTAL;
 			betreffTextContraints.anchor = GridBagConstraints.WEST;
 			betreffTextContraints.gridx = 1;
 			betreffTextContraints.gridy = 4;
 			betreffTextContraints.insets = new Insets(1, 1, 1, 1);
 
 			GridBagConstraints textareaContraints = new GridBagConstraints();
 			textareaContraints.fill = GridBagConstraints.BOTH;
 			textareaContraints.gridx = 1;
 			textareaContraints.gridy = 5;
 			textareaContraints.weighty = 2;
 			textareaContraints.insets = new Insets(1, 1, 1, 1);
 
 			GridBagConstraints replyTableContraints = new GridBagConstraints();
 			replyTableContraints.fill = GridBagConstraints.BOTH;
 			replyTableContraints.gridx = 1;
 			replyTableContraints.gridy = 6;
 			replyTableContraints.weighty = 2;
 			replyTableContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints replyTextContraints = new GridBagConstraints();
 			replyTextContraints.fill = GridBagConstraints.HORIZONTAL;
 			replyTextContraints.gridx = 1;
 			replyTextContraints.gridy = 7;
 			replyTextContraints.insets = new Insets(1, 1, 1, 1);
 			GridBagConstraints replyAreaContraints = new GridBagConstraints();
 			replyAreaContraints.fill = GridBagConstraints.BOTH;
 			replyAreaContraints.gridx = 1;
 			replyAreaContraints.gridy = 8;
 			replyAreaContraints.weighty = 2;
 			replyAreaContraints.insets = new Insets(1, 1, 1, 1);
 			
 			GridBagConstraints okButtonContraints = new GridBagConstraints();
 			okButtonContraints.gridx = 0;
 			okButtonContraints.gridy = 9;
 			okButtonContraints.anchor = GridBagConstraints.EAST;
 			okButtonContraints.insets = new Insets(1, 1, 1, 1);			
 			GridBagConstraints abbrechenButtonContraints = new GridBagConstraints();
 			abbrechenButtonContraints.gridx = 1;
 			abbrechenButtonContraints.gridy = 9;
 			abbrechenButtonContraints.anchor = GridBagConstraints.WEST;
 			abbrechenButtonContraints.insets = new Insets(1, 1, 1, 1);
 			
 			erstellerField.setEditable(false);
 			erstellerEmailField.setEditable(false);
 			betreffField.setEditable(false);
 			textArea.setEditable(false);
 			replyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			
 			contentPane = new JPanel();
 			contentPane.setLayout(new GridBagLayout());
 			contentPane.add(prioLabel, prioLabelContraints);
 			contentPane.add(statusLabel, statusLabelContraints);
 			contentPane.add(erstellerLabel, erstellerLabelContraints);
 			contentPane.add(erstellerEmailLabel, erstellerEmailLabelContraints);
 			contentPane.add(betreffLabel, betreffLabelContraints);
 			contentPane.add(textLabel, textLabelContraints);
 			contentPane.add(replyTableLabel, replyTableLabelContraints);
 			contentPane.add(replyTextLabel, replyTextLabelContraints);
 			contentPane.add(replyLabel, replyLabelContraints);
 			
 			contentPane.add(prioBox, prioBoxContraints);
 			contentPane.add(statusBox, statusBoxContraints);
 			contentPane.add(erstellerField, erstellerTextContraints);
 			contentPane.add(erstellerEmailField, erstellerEmailTextContraints);
 			contentPane.add(betreffField, betreffTextContraints);
 			contentPane.add(textArea, textareaContraints);
 			contentPane.add(new JScrollPane(replyTable), replyTableContraints);
 			contentPane.add(replyBetreff, replyTextContraints);
 			contentPane.add(replyArea, replyAreaContraints);
 			contentPane.add(okButton, okButtonContraints);
 			contentPane.add(exitButton, abbrechenButtonContraints);
 		}
 		return contentPane;
 	}
 
 	public void setOkButtonListener(ActionListener al) {
 		okButton.addActionListener(al);
 	}	
 	public void setExitButtonListener(ActionListener al) {
 		exitButton.addActionListener(al);
 	}
 	public void setTableListener(FocusListener fl) {
 		replyTable.addFocusListener(fl);
 	}
 	public void setTableSelectionListener(ListSelectionListener lsl) {
 		ListSelectionModel rowSelectionModel = replyTable.getSelectionModel();
 		rowSelectionModel.addListSelectionListener(lsl);
 	}
 	public int getSelectedTableRow() {
 		return replyTable.getSelectedRow();
 	}	
 	public void refreshTable() {
 		replyTable.repaint();
 	}
 	public void setTableModel(AbstractTableModel tableModel) {
 		replyTable.setModel(tableModel);
 	}
 
 	public void addPrioritaet(Object obj) {
 		prioBox.addItem(obj);
 	}
 	public void addStatus(Object obj) {
 		statusBox.addItem(obj);
 	}
 
 	public void reset() {
 		prioBox.setSelectedIndex(0);
 		statusBox.setSelectedIndex(0);
 		erstellerField.setText("");
 		erstellerEmailField.setText("");
 		betreffField.setText("");
 		textArea.setText("");
		replyBetreff.setText("");
 		replyArea.setText("");
 	}
 
 	public void setReplyTextEtidable(boolean b) {
 		replyArea.setEditable(b);
 	}
 	public void setReplyBetreffEtidable(boolean b) {
 		replyBetreff.setEditable(b);
 	}
 
 	/*
 	 * Overrides
 	 */
 	@Override
 	public void dispose() {
 		ActionEvent event = new ActionEvent(this, 0, "enable");
 		exitButton.getActionListeners()[0].actionPerformed(event);
 		super.dispose();
 	}
 }
