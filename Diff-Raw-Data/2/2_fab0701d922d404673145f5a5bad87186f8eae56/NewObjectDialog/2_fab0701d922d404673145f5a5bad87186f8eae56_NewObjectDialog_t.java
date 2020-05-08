 /* The contents of this file are subject to the license and copyright terms
  * detailed in the license directory at the root of the source tree (also 
  * available online at http://www.fedora.info/license/).
  */
 
 package fedora.client;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import javax.swing.*;
 
 import fedora.common.Constants;
 
 import fedora.client.actions.ViewObject;
 import fedora.client.utility.ingest.AutoIngestor;
 
 import fedora.server.management.FedoraAPIM;
 import fedora.server.utilities.StreamUtility;
 
 /**
  * Launch a dialog for entering information for a new object (title, content
  * model, and possibly a specified pid), then create the object on the server
  * and launch an editor on it.
  * 
  * @author cwilper@cs.cornell.edu
  */
 public class NewObjectDialog
         extends JDialog
         implements Constants, ItemListener {
 
 	private static final long serialVersionUID = 1L;
 	
 	private JTextField m_labelField;
 
 	private JTextField m_cModelField;
 
 	private JCheckBox m_customPIDCheckBox;
 
 	private JTextField m_customPIDField;
 
 	private JButton m_okButton;
 
 	private FedoraAPIM m_apim;
 
 	// for the checkbox
 	public void itemStateChanged(ItemEvent e) {
 		if (e.getStateChange() == ItemEvent.DESELECTED) {
 			// disable text entry
 			m_customPIDField.setEditable(false);
 		} else if (e.getStateChange() == ItemEvent.SELECTED) {
 			// enable text entry
 			m_customPIDField.setEditable(true);
 		}
 	}
 
 	public NewObjectDialog() {
 		super(JOptionPane.getFrameForComponent(Administrator.getDesktop()),
 				"New Object", true);
 
 		JPanel inputPane = new JPanel();
 		inputPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory
 				.createCompoundBorder(BorderFactory.createEmptyBorder(6, 6, 6,
 						6), BorderFactory.createEtchedBorder()), BorderFactory
 				.createEmptyBorder(6, 6, 6, 6)));
 
 		GridBagLayout gridBag = new GridBagLayout();
 		inputPane.setLayout(gridBag);
 
 		JLabel labelLabel = new JLabel("Label");
 		JLabel cModelLabel = new JLabel("Content Model");
 		m_customPIDCheckBox = new JCheckBox("Use Custom PID");
 		m_customPIDCheckBox.addItemListener(this);
 
 		m_labelField = new JTextField(
 				"Enter a one-line description of the object.");
 		m_cModelField = new JTextField("");
 		m_customPIDField = new JTextField();
 		m_customPIDField.setEditable(false);
 
 		addLabelValueRows(new JComponent[] { labelLabel, cModelLabel,
 				m_customPIDCheckBox }, new JComponent[] { m_labelField,
 				m_cModelField, m_customPIDField }, gridBag, inputPane);
 
 		CreateAction createAction = new CreateAction();
 		CreateListener createListener = new CreateListener(createAction);
 		JButton okButton = new JButton(createAction);
 		okButton.registerKeyboardAction(createListener, KeyStroke.getKeyStroke(
 				KeyEvent.VK_ENTER, 0, false), JButton.WHEN_IN_FOCUSED_WINDOW);
 		okButton.setText("Create");
 		JButton cancelButton = new JButton(new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent evt) {
 				dispose();
 			}
 		});
 		cancelButton.setText("Cancel");
 		JPanel buttonPane = new JPanel();
 		buttonPane.add(okButton);
 		buttonPane.add(cancelButton);
 		Container contentPane = getContentPane();
 		contentPane.setLayout(new BorderLayout());
 		contentPane.add(inputPane, BorderLayout.CENTER);
 		contentPane.add(buttonPane, BorderLayout.SOUTH);
 
 		pack();
 		setLocation(Administrator.INSTANCE.getCenteredPos(getWidth(),
 				getHeight()));
 		setVisible(true);
 	}
 
 	public void addLabelValueRows(JComponent[] labels, JComponent[] values,
 			GridBagLayout gridBag, Container container) {
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(0, 6, 6, 6);
 		for (int i = 0; i < labels.length; i++) {
 			c.anchor = GridBagConstraints.EAST;
 			c.gridwidth = GridBagConstraints.RELATIVE; // next-to-last
 			c.fill = GridBagConstraints.NONE; // reset to default
 			c.weightx = 0.0; // reset to default
 			gridBag.setConstraints(labels[i], c);
 			container.add(labels[i]);
 
 			c.gridwidth = GridBagConstraints.REMAINDER; // end row
 			if (!(values[i] instanceof JComboBox)) {
 				c.fill = GridBagConstraints.HORIZONTAL;
 			} else {
 				c.anchor = GridBagConstraints.WEST;
 			}
 			c.weightx = 1.0;
 			gridBag.setConstraints(values[i], c);
 			container.add(values[i]);
 		}
 
 	}
 
 	public class CreateAction extends AbstractAction {
 
 		private static final long serialVersionUID = 1L;
 
 		public void actionPerformed(ActionEvent evt) {
 			try {
 				String pid = null;
 				String label = m_labelField.getText();
 				String cModel = m_cModelField.getText();
 				boolean ok = true;
 				if (m_labelField.getText().equals("")) {
 					JOptionPane.showMessageDialog(Administrator.getDesktop(),
 							"Label must be non-empty", "Error",
 							JOptionPane.ERROR_MESSAGE);
 					ok = false;
 				}
 				if (m_customPIDCheckBox.isSelected()) {
 					pid = m_customPIDField.getText();
 					if (m_customPIDField.getText().indexOf(":") < 1) {
 						JOptionPane
 								.showMessageDialog(
 										Administrator.getDesktop(),
 										"Custom PID should be of the form \"namespace:alphaNumericName\"",
 										"Error", JOptionPane.ERROR_MESSAGE);
 						ok = false;
 					}
 				}
 
 				if (ok) {
 					dispose();
 					// Serialize the most basic
 					StringBuffer xml = new StringBuffer();
 					xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 					xml
 							.append("<foxml:digitalObject xmlns:xsi=\"" + XSI.uri + "\"\n");
 					xml
 							.append("           xmlns:foxml=\"" + FOXML.uri + "\"\n");
 					xml
 							.append("           xsi:schemaLocation=\"" + FOXML.uri + " " + FOXML1_1.xsdLocation + "\"");
 					xml.append("\n           VERSION=\"1.1\"\n");
 					if (pid != null) {
 						xml.append("\n           PID=\""
 								+ StreamUtility.enc(pid) + "\">\n");
 					} else {
 						xml.append(">\n");
 					}
 					xml.append("  <foxml:objectProperties>\n");
 					xml.append("    <foxml:property NAME=\"" + RDF.TYPE.uri + "\" VALUE=\"FedoraObject\"/>\n");
 					xml.append("    <foxml:property NAME=\"" + MODEL.LABEL.uri + "\" VALUE=\""
 									+ StreamUtility.enc(label) + "\"/>\n");
 					xml.append("    <foxml:property NAME=\"" + MODEL.CONTENT_MODEL.uri + "\" VALUE=\""
 									+ StreamUtility.enc(cModel) + "\"/>\n");
                    xml.append("    <foxml:property NAME=\"" + MODEL.OWNER.uri + "\" VALUE=\"" + Administrator.getUser() + "\"/>");
 					xml.append("  </foxml:objectProperties>\n");
 					xml.append("</foxml:digitalObject>");
 					String objXML = xml.toString();
 
 					ByteArrayInputStream in = new ByteArrayInputStream(objXML
 							.getBytes("UTF-8"));
 					String newPID = AutoIngestor.ingestAndCommit(
 							Administrator.APIA, Administrator.APIM, in,
                             FOXML1_1.uri,
 							"Created with Admin GUI \"New Object\" command");
 					new ViewObject(newPID).launch();
 				}
 			} catch (Exception e) {
 				String msg = e.getMessage();
 				if (msg == null)
 					msg = e.getClass().getName();
 				Administrator.showErrorDialog(Administrator.getDesktop(),
 						"Error Creating Object", msg, e);
 			}
 		}
 
 	}
 
 	public class CreateListener implements ActionListener {
 
 		private CreateAction m_createAction;
 
 		public CreateListener(CreateAction createAction) {
 			this.m_createAction = createAction;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			m_createAction.actionPerformed(e);
 		}
 	}
 
 }
