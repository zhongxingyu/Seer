 /*
  * AbstractWizardWindow.java (JMetricVis)
  * Copyright 2011 Luke Plaster. All rights reserved.
  */
 package org.lukep.javavis.ui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.border.EmptyBorder;
 
 import org.lukep.javavis.ui.UIMain;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 abstract class AbstractWizardWindow extends JDialog implements ActionListener {
 
 	AbstractWizardWindow thisInstance;
 	UIMain uiInstance;
 	
 	private JPanel pnlForm;
 	
 	protected JButton btnPerformAction;
 	private JButton btnCancel;
 	
 	protected boolean btnPerformActionEnabled = false;
 	
 	private JLabel lblStatus;
 	
 	private JProgressBar progressBar;
 	
 	protected KeyListener validationListener = new KeyListener() {
 		@Override
 		public void keyTyped(KeyEvent e) {
 			refreshActionable();
 		}
 		@Override
 		public void keyReleased(KeyEvent e) {
 		}
 		@Override
 		public void keyPressed(KeyEvent e) {
 		}
 	};
 	
 	public AbstractWizardWindow(Frame parent, UIMain uiInstance, String title, ImageIcon icon) {
 		super(parent, title);
 		setModal(true);
 		setBounds(200, 200, 500, 300);
 		setLocationRelativeTo(parent);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		getContentPane().setLayout(new BorderLayout(0, 0));
 		
 		this.thisInstance = this;
 		this.uiInstance = uiInstance;
 		
 		initialise(title, icon);
 		initialiseFormControls();
 		pack();
 	}
 	
 	private void initialise(String title, ImageIcon icon) {
 		JPanel pnlHeader = new JPanel();
 		pnlHeader.setBackground(Color.WHITE);
 		getContentPane().add(pnlHeader, BorderLayout.NORTH);
 		pnlHeader.setLayout(new BorderLayout(0, 0));
 		
 		JLabel lblHeader = new JLabel(title);
 		lblHeader.setFont(new Font("Tahoma", Font.BOLD, 17));
 		pnlHeader.add(lblHeader, BorderLayout.CENTER);
 		
 		JPanel pnlIcon = new JPanel();
 		pnlIcon.setBorder(new EmptyBorder(0, 5, 0, 6));
 		pnlIcon.setBackground(Color.WHITE);
 		pnlHeader.add(pnlIcon, BorderLayout.WEST);
 		
 		JLabel lblIcon = new JLabel("");
 		pnlIcon.add(lblIcon);
 		lblIcon.setIcon(icon);
 		
 		JSeparator separator = new JSeparator();
 		pnlHeader.add(separator, BorderLayout.SOUTH);
 		
 		// --- end header, begin form
 		
 		pnlForm = new JPanel();
 		pnlForm.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(new JScrollPane(pnlForm), BorderLayout.CENTER);
 		pnlForm.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		// --- end form, begin footer
 		
 		JPanel pnlFooter = new JPanel();
 		getContentPane().add(pnlFooter, BorderLayout.SOUTH);
 		pnlFooter.setLayout(new BorderLayout(0, 0));
 		
 		progressBar = new JProgressBar();
 		pnlFooter.add(progressBar, BorderLayout.NORTH);
 		
 		JPanel pnlButtons = new JPanel();
 		pnlButtons.setBorder(new EmptyBorder(5, 0, 5, 5));
 		pnlFooter.add(pnlButtons, BorderLayout.EAST);
 		pnlButtons.setLayout(new GridLayout(0, 2, 10, 0));
 		
 		btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(this);
 		pnlButtons.add(btnCancel);
 		
 		btnPerformAction = new JButton("Create Project");
 		btnPerformAction.addActionListener(this);
 		btnPerformAction.setEnabled(false);
 		pnlButtons.add(btnPerformAction);
 		
 		JPanel pnlStatus = new JPanel();
 		pnlStatus.setBorder(new EmptyBorder(0, 10, 0, 0));
 		pnlFooter.add(pnlStatus, BorderLayout.WEST);
 		pnlStatus.setLayout(new BorderLayout(0, 0));
 		
 		lblStatus = new JLabel("");
 		pnlStatus.add(lblStatus);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		try {
 			if (btnCancel == e.getSource()) {
 				setVisible(false);
 				dispose();
 			} else if (btnPerformAction == e.getSource()) {
 				if (validateFormControls()) {
 					lockFormControls();
 					performAction();
 				}
 			}
 		} catch (Exception ex) {
 			JOptionPane.showMessageDialog(this, "Exception: " + ex.getLocalizedMessage(), 
 					"An error occurred!", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	protected void addFormControl(Component comp, Object constraints) {
 		pnlForm.add(comp, constraints);
 	}
 	
 	protected void setProgramStatus(String status, boolean indeterminate, int progress) {
 		progressBar.setIndeterminate(indeterminate);
 		progressBar.setValue(progress);
 		
 		lblStatus.setText(status);
 		lblStatus.repaint();
 		
 		uiInstance.setProgramStatus(status);
 	}
 	
 	protected void refreshActionable() {
 		if (validateFormControls()) {
 			if (!btnPerformActionEnabled) {
 				btnPerformAction.setEnabled(true);
 				btnPerformActionEnabled = true;
 			}
 		} else if (btnPerformActionEnabled) {
 			btnPerformAction.setEnabled(false);
 			btnPerformActionEnabled = false;
 		}
 	}
 	
 	protected abstract void initialiseFormControls();
 	protected abstract boolean validateFormControls();
 	protected abstract void lockFormControls();
 	protected abstract void performAction() throws Exception;
 
 }
