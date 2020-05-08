 package de.ncoder.elements.gui.dialog;
 
 import java.awt.Dialog;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EmptyBorder;
 
 public class ExceptionDialog extends JDialog {
 	private static final long serialVersionUID = 1L;
 	private JLabel exception;
 	private JCheckBox checkbox;
 	private JEditorPane debugInformation;
 	private JScrollPane scrollPane;
 	private JButton btnOk;
 
 	public ExceptionDialog(Window owner, String title, String message, Exception e) {
 		super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
 		createLayout();
 		exception.setText("<html>" + message + "</html>");
 		Writer stackTraceWriter = new StringWriter();
 		e.printStackTrace(new PrintWriter(stackTraceWriter));
 		debugInformation.setText(stackTraceWriter.toString());
 	}
 
 	public void createLayout() {
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		JPanel pane = new JPanel();
 		setContentPane(pane);
 		pane.setBorder(new EmptyBorder(10, 10, 10, 10));
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		getContentPane().setLayout(gridBagLayout);
 
 		exception = new JLabel("Exception");
 		exception.setBorder(new EmptyBorder(10, 10, 10, 10));
 		GridBagConstraints gbc_excpetion = new GridBagConstraints();
		gbc_excpetion.weightx = 1.0;
 		gbc_excpetion.fill = GridBagConstraints.HORIZONTAL;
 		gbc_excpetion.anchor = GridBagConstraints.NORTH;
 		gbc_excpetion.insets = new Insets(0, 0, 5, 0);
 		gbc_excpetion.gridx = 0;
 		gbc_excpetion.gridy = 0;
 		getContentPane().add(exception, gbc_excpetion);
 
 		checkbox = new JCheckBox("Debug Information");
 		checkbox.setBorder(new EmptyBorder(10, 10, 10, 10));
 		checkbox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				scrollPane.setVisible(checkbox.isSelected());
 				pack();
 			}
 		});
 		GridBagConstraints gbc_checkbox = new GridBagConstraints();
 		gbc_checkbox.insets = new Insets(0, 0, 5, 0);
 		gbc_checkbox.fill = GridBagConstraints.HORIZONTAL;
 		gbc_checkbox.gridx = 0;
 		gbc_checkbox.gridy = 1;
 		getContentPane().add(checkbox, gbc_checkbox);
 
 		scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.weighty = 1.0;
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 0;
 		gbc_scrollPane.gridy = 2;
 		getContentPane().add(scrollPane, gbc_scrollPane);
 
 		debugInformation = new JEditorPane();
 		scrollPane.setViewportView(debugInformation);
 		scrollPane.setVisible(false);
 		debugInformation.setText("DebugInformation");
 
 		btnOk = new JButton("Ok");
 		GridBagConstraints gbc_btnOk = new GridBagConstraints();
 		gbc_btnOk.anchor = GridBagConstraints.NORTH;
 		gbc_btnOk.insets = new Insets(0, 0, 5, 0);
 		gbc_btnOk.gridx = 0;
 		gbc_btnOk.gridy = 3;
 		btnOk.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		getContentPane().add(btnOk, gbc_btnOk);
 		pack();
 	}
 
 	@Override
 	public void pack() {
 		super.pack();
 		if (checkbox.isSelected()) {
 			setSize(1000, 1000);
 		} else {
 			setSize(500, 200);
 		}
 	}
 }
