 package de.unikassel.ann.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import de.unikassel.ann.controller.Settings;
 import de.unikassel.ann.io.NetIO;
 
 public class ImportFilePanel extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private JPanel searchFilePanel;
 
 	private JCheckBox topologieCB;
 	private JCheckBox synapseCB;
 	private JCheckBox trainigDataCB;
 
 	private JFileChooser fileopen;
 
 	private JTextField textFieldFileName;
 
 	/**
 	 * Create the panel.
 	 */
 	public ImportFilePanel() {
 
 		searchFilePanel = new JPanel();
 		searchFilePanel.setLayout(new BorderLayout());
 
 		setTitle(Settings.i18n.getString("importFilePanel.titel"));
 		setSize(272, 210);
 		setLocationRelativeTo(null);
 		setModal(true);
 		setResizable(false);
 
 		JPanel importDialogPanel = new JPanel();
 
 		JLabel lblImportFile = new JLabel(Settings.i18n.getString("importFilePanel.lblImportFile"));
 		JButton btnSearch = new JButton();
 		btnSearch.setIcon(new ImageIcon(this.getClass().getClassLoader().getResource("img/search-icon.png")));
 
 		//
 		btnSearch.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent event) {
 				openFileChooser();
 
 			}
 
 		});
 
 		JLabel lblTopology = new JLabel(Settings.i18n.getString("importFilePanel.lblTopology"));
 		topologieCB = new JCheckBox("");
 		topologieCB.setEnabled(false);
 		JLabel lblSynapse = new JLabel(Settings.i18n.getString("importFilePanel.lblSynapse"));
 		synapseCB = new JCheckBox("");
 		synapseCB.setEnabled(false);
 		JLabel lblTrainingData = new JLabel(Settings.i18n.getString("importFilePanel.lblTrainingData"));
 		trainigDataCB = new JCheckBox("");
 		trainigDataCB.setEnabled(false);
		JButton btnImport = new JButton(Settings.i18n.getString("importFilePanel.btnImport"));
 		// TODO Nach Import JungView Aktualisieren
 		JButton btnCancel = new JButton(Settings.i18n.getString("importFilePanel.btnCancel"));
 
 		textFieldFileName = new JTextField(10);
 		textFieldFileName.setEditable(false);
 
 		topologieCB.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				if (topologieCB.isSelected()) {
 					synapseCB.setEnabled(true);
 				} else {
					if (synapseCB.isSelected()) {
						synapseCB.setSelected(false);
						synapseCB.setEnabled(false);
					}
 					synapseCB.setEnabled(false);
 				}
 			}
 		});
 
 		synapseCB.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				if (synapseCB.isSelected() && !topologieCB.isSelected()) {
 					topologieCB.setSelected(true);
 				}
 			}
 		});
 
 		btnImport.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				if (textFieldFileName.getText().equals("")) {
 					JFrame frame = new JFrame();
 					JOptionPane.showMessageDialog(frame, "Sie haben keine Datei ausgewählt", "Warnung", JOptionPane.WARNING_MESSAGE);
 				} else {
 					Settings.getInstance().loadNetworkFromFile(fileopen.getSelectedFile(), topologieCB.isSelected(),
 							synapseCB.isSelected(), trainigDataCB.isSelected());
 					dispose();
 				}
 
 			}
 
 		});
 		btnCancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent event) {
 				dispose();
 			}
 		});
 
 		GroupLayout gl_importDialogPanel = new GroupLayout(importDialogPanel);
 		gl_importDialogPanel
 				.setHorizontalGroup(gl_importDialogPanel.createParallelGroup(Alignment.LEADING)
 						.addGroup(
 								gl_importDialogPanel
 										.createSequentialGroup()
 										.addContainerGap()
 										.addGroup(
 												gl_importDialogPanel
 														.createParallelGroup(Alignment.LEADING)
 														.addGroup(
 																gl_importDialogPanel
 																		.createParallelGroup(Alignment.TRAILING)
 																		.addComponent(btnImport)
 																		.addGroup(
 																				gl_importDialogPanel.createParallelGroup(Alignment.LEADING)
 																						.addComponent(lblTopology).addComponent(lblSynapse)
 																						.addComponent(lblTrainingData)))
 														.addComponent(lblImportFile))
 										.addPreferredGap(ComponentPlacement.RELATED)
 										.addGroup(
 												gl_importDialogPanel
 														.createParallelGroup(Alignment.LEADING)
 														.addGroup(
 																Alignment.TRAILING,
 																gl_importDialogPanel
 																		.createSequentialGroup()
 																		.addGroup(
 																				gl_importDialogPanel.createParallelGroup(Alignment.LEADING)
 																						.addComponent(synapseCB).addComponent(topologieCB)
 																						.addComponent(trainigDataCB)).addGap(96))
 														.addGroup(
 																gl_importDialogPanel
 																		.createSequentialGroup()
 																		.addGroup(
 																				gl_importDialogPanel
 																						.createParallelGroup(Alignment.TRAILING)
 																						.addComponent(textFieldFileName,
 																								GroupLayout.PREFERRED_SIZE,
 																								GroupLayout.DEFAULT_SIZE,
 																								GroupLayout.PREFERRED_SIZE)
 																						.addComponent(btnCancel))
 																		.addPreferredGap(ComponentPlacement.RELATED)
 																		.addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 35,
 																				GroupLayout.PREFERRED_SIZE).addGap(15)))));
 		gl_importDialogPanel.setVerticalGroup(gl_importDialogPanel.createParallelGroup(Alignment.LEADING).addGroup(
 				gl_importDialogPanel
 						.createSequentialGroup()
 						.addGap(10)
 						.addGroup(
 								gl_importDialogPanel
 										.createParallelGroup(Alignment.BASELINE)
 										.addComponent(btnSearch)
 										.addComponent(textFieldFileName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
 												GroupLayout.PREFERRED_SIZE).addComponent(lblImportFile))
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addGroup(
 								gl_importDialogPanel
 										.createParallelGroup(Alignment.LEADING)
 										.addGroup(
 												gl_importDialogPanel.createSequentialGroup().addComponent(lblTopology)
 														.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblSynapse)
 														.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblTrainingData))
 										.addGroup(
 												gl_importDialogPanel.createSequentialGroup().addComponent(topologieCB)
 														.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(synapseCB)
 														.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(trainigDataCB)))
 						.addGap(18)
 						.addGroup(
 								gl_importDialogPanel.createParallelGroup(Alignment.BASELINE).addComponent(btnCancel)
 										.addComponent(btnImport)).addContainerGap(14, Short.MAX_VALUE)));
 		importDialogPanel.setLayout(gl_importDialogPanel);
 		getContentPane().add(importDialogPanel);
 
 	}
 
 	private void openFileChooser() {
 
 		fileopen = new JFileChooser();
 		FileFilter filter = new FileNameExtensionFilter("csv files", "csv");
 		fileopen.addChoosableFileFilter(filter);
 		// only csv files
 		fileopen.setAcceptAllFileFilterUsed(false);
 
 		int ret = fileopen.showDialog(searchFilePanel, "Open file");
 
 		if (ret == JFileChooser.APPROVE_OPTION) {
 			textFieldFileName.setText(fileopen.getSelectedFile().getName());
 
 			NetIO reader = new NetIO();
 			try {
 				reader.readConfigFile(fileopen.getSelectedFile());
 				if (reader.topoBeanList != null) {
 					topologieCB.setEnabled(true);
 					topologieCB.setSelected(true);
					System.out.println("topo");
 				}
 				if (reader.synapsesBanList != null) {
 					// Falls in der gelesene Datei keine topology existiert
 					if (checkingIfTopologyIsSelected()) {
 						synapseCB.setEnabled(true);
 						synapseCB.setSelected(true);
 					} else {
 						synapseCB.setEnabled(true);
 						synapseCB.setSelected(true);
 					}
 
 				}
 				if (reader.trainigBeanList != null) {
 					trainigDataCB.setEnabled(true);
 					trainigDataCB.setSelected(true);
 				}
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				System.out.println("IO:");
 			} catch (ClassNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 				System.out.println("class");
 			}
 
 		}
 
 	}
 
 	/**
 	 * @return
 	 */
 	private boolean checkingIfTopologyIsSelected() {
 		if (topologieCB.isSelected()) {
 			return true;
 		}
 
 		return false;
 	}
 
 }
