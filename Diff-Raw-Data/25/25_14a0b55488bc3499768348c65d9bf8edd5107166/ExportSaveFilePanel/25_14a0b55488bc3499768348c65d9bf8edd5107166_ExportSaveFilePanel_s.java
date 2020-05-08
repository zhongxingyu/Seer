 /**
  * Projekt ANNtool 
  *
  * Copyright (c) 2011 github.com/timaschew/jANN
  * 
  * Sofia
  */
 package de.unikassel.ann.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import de.unikassel.ann.config.NetConfig;
 import de.unikassel.ann.controller.Settings;
 import de.unikassel.ann.io.NetIO;
 import de.unikassel.ann.model.DataPairSet;
 import de.unikassel.ann.model.UserSession;
 
 /**
  * @author Sofia
  * 
  */
 public class ExportSaveFilePanel extends JDialog {
 
 	private JPanel exportSaveFilePanel;
 	private JFileChooser fileSaveChooser;
 	private JCheckBox netCB;
 	private JCheckBox testDataCB;
 	private JCheckBox trainingDataCB;
 	private JButton btnExport;
 	public JComboBox fileSessionsCombo;
 	public DefaultComboBoxModel fileSessionsCombomodel;
 	private String parentCaller;
 	private JButton btnCancel;
 
 	private static final String PATH = System.getProperty("user.home");
 
 	/**
 	 * 
 	 */
 	public ExportSaveFilePanel(final String parentCaller) {
 
 		exportSaveFilePanel = new JPanel();
 		exportSaveFilePanel.setLayout(new BorderLayout());
 
 		this.parentCaller = parentCaller;
 
 		setTitle(Settings.i18n.getString("exportSaveFilePanel.titel"));
 		setSize(250, 202);
 		setLocationRelativeTo(null);
 		setModal(true);
 		setResizable(false);
 
 		JLabel lblSession = new JLabel(Settings.i18n.getString("exportSaveFilePanel.lblSession"));
 		JLabel lblNetz = new JLabel(Settings.i18n.getString("exportSaveFilePanel.lblNetz"));
 		JLabel lblTrainingData = new JLabel(Settings.i18n.getString("exportSaveFilePanel.lblTrainingData"));
 		JLabel lblTestData = new JLabel(Settings.i18n.getString("exportSaveFilePanel.lblTestData"));
 
 		netCB = new JCheckBox("");
 		netCB.setEnabled(true);
 		trainingDataCB = new JCheckBox("");
 		trainingDataCB.setEnabled(false);
 		testDataCB = new JCheckBox("");
 		testDataCB.setEnabled(false);
 
 		btnExport = new JButton(Settings.i18n.getString("exportSaveFilePanel.btnExport"));
 		btnExport.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent event) {
 				NetIO net = new NetIO();
 				String sessionName = Settings.getInstance().getCurrentSession().toString();
 				File exportFile = openFileChooser();
 
 				if (netCB.isSelected()) {
 					NetConfig config = Settings.getInstance().getCurrentSession().getNetworkConfig();
 					net.writeNet(exportFile, sessionName, config);
 				}
 				if (trainingDataCB.isSelected()) {
 
 					DataPairSet dataSet = Settings.getInstance().getCurrentSession().getNetworkConfig().getTrainingData();
 					net.writeDataSet(exportFile, sessionName, trainingDataCB.isSelected(), dataSet);
 				}
 				if (testDataCB.isSelected()) {
 					DataPairSet testDataSet = Settings.getInstance().getCurrentSession().getNetworkConfig().getTestData();
 					net.writeDataSet(exportFile, sessionName, testDataCB.isSelected(), testDataSet);
 				}
 
 				if (parentCaller.equals("EXIT")) {
					fileSessionsCombomodel.removeElement(exportFile.getName());
 				} else {
 					dispose();
 				}
 			}
 
 		});
 
 		btnCancel = new JButton(Settings.i18n.getString("exportSaveFilePanel.btnCancel"));
 		// wenn der Aufrufer der Exit Kommando ist, soll
 		// über das Cancel Button, die applikation geschlossen werden
 		// falls der User nicht alles geöffnete Sessions speichern möchte
 		if (parentCaller.equals("EXIT")) {
 			btnCancel.setText("Exit");
 		}
 
 		btnCancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				if (parentCaller.equals("EXIT")) {
 					System.exit(e.getID());
 				} else {
 					dispose();
 				}
 
 			}
 		});
 
 		fileSessionsCombo = new JComboBox();
 		fileSessionsCombomodel = new DefaultComboBoxModel();
 		List<UserSession> userSessionsListe = Settings.getInstance().getUserSessions();
 
 		for (int i = 0; i < userSessionsListe.size(); i++) {
			fileSessionsCombomodel.addElement(userSessionsListe.get(i).toString());
 			fileSessionsCombo.setModel(fileSessionsCombomodel);
 		}
 		fileSessionsCombo.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				if (parentCaller.equals("EXPORT") || parentCaller.equals("EXIT")) {
 					Settings.getInstance().loadSesson(fileSessionsCombo.getSelectedItem().toString());
 					// check if the traindata is null and setenable the checkbox, true of false
 					setTheCheckBoxs();
 				} else if (parentCaller.equals("CLOSE_CURRENT_SESSION")) {
 					setTheCheckBoxs();
 				}
 
 			}
 
 		});
 
 		GroupLayout groupLayout = new GroupLayout(getContentPane());
 		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
 				groupLayout
 						.createSequentialGroup()
 						.addGroup(
 								groupLayout
 										.createParallelGroup(Alignment.LEADING)
 										.addGroup(
 												groupLayout
 														.createSequentialGroup()
 														.addContainerGap()
 														.addGroup(
 																groupLayout
 																		.createParallelGroup(Alignment.LEADING)
 																		.addGroup(
 																				groupLayout
 																						.createSequentialGroup()
 																						.addGroup(
 																								groupLayout
 																										.createParallelGroup(
 																												Alignment.LEADING)
 																										.addComponent(lblNetz)
 																										.addComponent(lblTrainingData)
 																										.addComponent(lblTestData))
 																						.addGap(56)
 																						.addGroup(
 																								groupLayout
 																										.createParallelGroup(
 																												Alignment.LEADING)
 																										.addComponent(testDataCB)
 																										.addComponent(trainingDataCB)
 																										.addComponent(netCB)))
 																		.addGroup(
 																				groupLayout
 																						.createSequentialGroup()
 																						.addComponent(lblSession)
 																						.addPreferredGap(ComponentPlacement.RELATED, 33,
 																								Short.MAX_VALUE)
 																						.addComponent(fileSessionsCombo,
 																								GroupLayout.PREFERRED_SIZE, 143,
 																								GroupLayout.PREFERRED_SIZE).addGap(16))))
 										.addGroup(
 												groupLayout.createSequentialGroup().addGap(22).addComponent(btnExport).addGap(41)
 														.addComponent(btnCancel))).addContainerGap()));
 		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
 				groupLayout
 						.createSequentialGroup()
 						.addGap(8)
 						.addGroup(
 								groupLayout
 										.createParallelGroup(Alignment.BASELINE)
 										.addComponent(lblSession)
 										.addComponent(fileSessionsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
 												GroupLayout.PREFERRED_SIZE))
 						.addPreferredGap(ComponentPlacement.RELATED)
 						.addGroup(
 								groupLayout
 										.createParallelGroup(Alignment.LEADING)
 										.addGroup(
 												groupLayout
 														.createSequentialGroup()
 														.addComponent(netCB)
 														.addGap(12)
 														.addGroup(
 																groupLayout
 																		.createParallelGroup(Alignment.LEADING)
 																		.addGroup(
 																				groupLayout.createSequentialGroup()
 																						.addPreferredGap(ComponentPlacement.RELATED)
 																						.addComponent(trainingDataCB)
 																						.addPreferredGap(ComponentPlacement.UNRELATED)
 																						.addComponent(testDataCB))
 																		.addGroup(
 																				groupLayout.createSequentialGroup().addGap(30)
 																						.addComponent(lblTestData))))
 										.addGroup(
 												groupLayout.createSequentialGroup().addComponent(lblNetz)
 														.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblTrainingData)))
 						.addGap(16)
 						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(btnExport).addComponent(btnCancel))
 						.addContainerGap(12, Short.MAX_VALUE)));
 		getContentPane().setLayout(groupLayout);
 
 	}
 
 	private void setTheCheckBoxs() {
 		NetConfig netConfig = Settings.getInstance().getCurrentSession().getNetworkConfig();
 		if (netConfig.getNetwork() != null) {
 			netCB.setEnabled(true);
 			netCB.setSelected(true);
 		}
 		if (netConfig.getTrainingData() == null) {
 			trainingDataCB.setEnabled(false);
 		} else {
 			trainingDataCB.setEnabled(true);
 			trainingDataCB.setSelected(true);
 		}
 		if (netConfig.getTestData() == null) {
 			testDataCB.setEnabled(false);
 		} else {
 			testDataCB.setEnabled(true);
 			testDataCB.setSelected(true);
 		}
 	}
 
 	private File openFileChooser() {
 		fileSaveChooser = new JFileChooser();
 		FileFilter filter = new FileNameExtensionFilter("CSV Format", "csv");
 		fileSaveChooser.addChoosableFileFilter(filter);
 		// only csv files
 		fileSaveChooser.setAcceptAllFileFilterUsed(false);
 
 		int ret = fileSaveChooser.showSaveDialog(exportSaveFilePanel);
 		if (ret == JFileChooser.APPROVE_OPTION) {
 			return fileSaveChooser.getSelectedFile();
 		}
 		return null;
 	}
 }
