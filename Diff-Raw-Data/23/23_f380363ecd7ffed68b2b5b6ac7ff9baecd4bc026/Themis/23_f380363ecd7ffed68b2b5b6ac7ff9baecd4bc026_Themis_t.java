 package org.iugonet.www;
 
 import java.awt.GridBagLayout;
 import javax.swing.JPanel;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JButton;
 import java.awt.GridLayout;
 import java.awt.FlowLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 import javax.swing.JList;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.undo.UndoableEditSupport;
 
 public class Themis extends Project {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JCheckBox chckbxUseSingleDay;
 
 	protected UndoableEditSupport undoableEditSupport;
 
 	public Themis() {
 		super();
 		undoableEditSupport = new UndoableEditSupport(this);
 
 		readConf(getClass().getClassLoader().getResource("conf/Themis.xml"));
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 100, 100, 0, 0, 0, 0, 0, 0 };
 		gridBagLayout.rowHeights = new int[] { 60, 0, 50, 0, 0, 0, 0, 0, 0, 0,
 				0, 0 };
 		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 0.0,
 				0.0, 0.0, Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
 				0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		JPanel panel_3 = new JPanel();
 		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
 		gbc_panel_3.anchor = GridBagConstraints.WEST;
 		gbc_panel_3.fill = GridBagConstraints.VERTICAL;
 		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_3.gridx = 0;
 		gbc_panel_3.gridy = 0;
 		add(panel_3, gbc_panel_3);
 
 		JLabel lblDataSelection = new JLabel("Data Selection:");
 		panel_3.add(lblDataSelection);
 
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.insets = new Insets(0, 0, 5, 5);
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		add(panel, gbc_panel);
 
 		JScrollPane scrollPane = new JScrollPane();
 		panel.add(scrollPane);
 
 		JPanel panel_4 = new JPanel();
 		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
 		gbc_panel_4.fill = GridBagConstraints.BOTH;
 		gbc_panel_4.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_4.gridx = 0;
 		gbc_panel_4.gridy = 0;
 		add(panel_4, gbc_panel_4);
 
 		JPanel panel_5 = new JPanel();
 		GridBagConstraints gbc_panel_5 = new GridBagConstraints();
 		gbc_panel_5.fill = GridBagConstraints.BOTH;
 		gbc_panel_5.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_5.gridx = 0;
 		gbc_panel_5.gridy = 0;
 		add(panel_5, gbc_panel_5);
 
 		JLabel lblDataLoaded = new JLabel("Data Loaded:");
 		panel_5.add(lblDataLoaded);
 
 		JPanel panel_1 = new JPanel();
 		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
 		gbc_panel_1.fill = GridBagConstraints.BOTH;
 		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_1.gridx = 0;
 		gbc_panel_1.gridy = 0;
 		add(panel_1, gbc_panel_1);
 		FlowLayout fl_panel_1 = new FlowLayout(FlowLayout.CENTER, 5, 5);
 		fl_panel_1.setAlignOnBaseline(true);
 		panel_1.setLayout(fl_panel_1);
 
 		JLabel lblInstrumentType = new JLabel("Instrument Type:");
 		GridBagConstraints gbc_lblInstrumentType = new GridBagConstraints();
 		gbc_lblInstrumentType.anchor = GridBagConstraints.WEST;
 		gbc_lblInstrumentType.insets = new Insets(0, 0, 5, 5);
 		gbc_lblInstrumentType.gridx = 0;
 		gbc_lblInstrumentType.gridy = 1;
 		add(lblInstrumentType, gbc_lblInstrumentType);
 
 		String[] hoge = getInstrumentTypes();
 		JComboBox comboBox = new JComboBox(hoge);
 		GridBagConstraints gbc_comboBox = new GridBagConstraints();
 		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox.gridx = 1;
 		gbc_comboBox.gridy = 1;
 		add(comboBox, gbc_comboBox);
 
 		JLabel lblOutputCoordinates = new JLabel("Output Coordinates:");
 		GridBagConstraints gbc_lblOutputCoordinates = new GridBagConstraints();
 		gbc_lblOutputCoordinates.anchor = GridBagConstraints.EAST;
 		gbc_lblOutputCoordinates.insets = new Insets(0, 0, 5, 5);
 		gbc_lblOutputCoordinates.gridx = 3;
 		gbc_lblOutputCoordinates.gridy = 1;
 		add(lblOutputCoordinates, gbc_lblOutputCoordinates);
 
 		String[] str = getOutputCoordinates();
 		JComboBox comboBox_1 = new JComboBox(str);
 		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
 		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox_1.gridx = 4;
 		gbc_comboBox_1.gridy = 1;
 		add(comboBox_1, gbc_comboBox_1);
 
 		JPanel panel_2 = new JPanel();
 		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
 		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
 		gbc_panel_2.fill = GridBagConstraints.BOTH;
 		gbc_panel_2.gridx = 0;
 		gbc_panel_2.gridy = 2;
 		add(panel_2, gbc_panel_2);
 		panel_2.setLayout(new GridLayout(0, 1, 0, 0));
 
 		JLabel lblStartTime = new JLabel("Start Time:");
 		panel_2.add(lblStartTime);
 
 		JLabel label = new JLabel("");
 		panel_2.add(label);
 
 		JLabel label_1 = new JLabel("");
 		panel_2.add(label_1);
 
 		JLabel label_2 = new JLabel("");
 		panel_2.add(label_2);
 
 		JLabel label_3 = new JLabel("");
 		panel_2.add(label_3);
 
 		JLabel label_4 = new JLabel("");
 		panel_2.add(label_4);
 
 		JLabel label_5 = new JLabel("");
 		panel_2.add(label_5);
 
 		JLabel label_6 = new JLabel("");
 		panel_2.add(label_6);
 
 		JLabel lblDataLoaded_2 = new JLabel("Data Loaded:");
 		GridBagConstraints gbc_lblDataLoaded_2 = new GridBagConstraints();
 		gbc_lblDataLoaded_2.anchor = GridBagConstraints.WEST;
 		gbc_lblDataLoaded_2.insets = new Insets(0, 0, 5, 0);
 		gbc_lblDataLoaded_2.gridx = 6;
 		gbc_lblDataLoaded_2.gridy = 0;
 		add(lblDataLoaded_2, gbc_lblDataLoaded_2);
 
 		textField_1 = new JTextField(getStartTime());
 		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
 		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
 		gbc_textField_1.gridx = 1;
 		gbc_textField_1.gridy = 2;
 		add(textField_1, gbc_textField_1);
 		// textField.setText("2007-03-23/00:00:00");
 		textField_1.setColumns(10);
 
 		JButton btnMyCalendar_1 = new JButton("");
 		btnMyCalendar_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println("Start Time");
 				MyCalendar calendar = new MyCalendar();
 				calendar.setLocationRelativeTo(null);
 				calendar.setVisible(true);
 			}
 		});
 		GridBagConstraints gbc_btnMyCalendar_1 = new GridBagConstraints();
 		gbc_btnMyCalendar_1.insets = new Insets(0, 0, 5, 5);
 		gbc_btnMyCalendar_1.gridx = 2;
 		gbc_btnMyCalendar_1.gridy = 2;
 		add(btnMyCalendar_1, gbc_btnMyCalendar_1);
 		btnMyCalendar_1.setIcon(new ImageIcon(Themis.class
 				.getResource("/calendar-alt-1-20.png")));
 
 		chckbxUseSingleDay = new JCheckBox("Use Single Day");
 		chckbxUseSingleDay.setSelected(true);
 		GridBagConstraints gbc_chckbxUseSingleDay = new GridBagConstraints();
 		gbc_chckbxUseSingleDay.anchor = GridBagConstraints.WEST;
 		gbc_chckbxUseSingleDay.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxUseSingleDay.gridx = 1;
 		gbc_chckbxUseSingleDay.gridy = 4;
 		add(chckbxUseSingleDay, gbc_chckbxUseSingleDay);
 
 		JLabel lblStopTime_1 = new JLabel("Stop Time:");
 		GridBagConstraints gbc_lblStopTime_1 = new GridBagConstraints();
 		gbc_lblStopTime_1.anchor = GridBagConstraints.EAST;
 		gbc_lblStopTime_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblStopTime_1.gridx = 0;
 		gbc_lblStopTime_1.gridy = 3;
 		add(lblStopTime_1, gbc_lblStopTime_1);
 		if (chckbxUseSingleDay.isSelected()) {
 			lblStopTime_1.setEnabled(false);
 		}
 
 		textField_2 = new JTextField(getStopTime());
 		// textField_2.setText("2007-03-24/00:00:00");
 		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
 		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
 		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_2.gridx = 1;
 		gbc_textField_2.gridy = 3;
 		add(textField_2, gbc_textField_2);
 		textField_2.setColumns(10);
 		if (chckbxUseSingleDay.isSelected()) {
 			textField_2.setEnabled(false);
 		}
 
 		JButton btnMyCalendar_2 = new JButton("");
 		btnMyCalendar_2.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Stop Time");
 				MyCalendar calendar = new MyCalendar();
 				calendar.setLocationRelativeTo(null);
 				calendar.setVisible(true);
 			}
 		});
 		GridBagConstraints gbc_btnMyCalendar_2 = new GridBagConstraints();
 		gbc_btnMyCalendar_2.insets = new Insets(0, 0, 5, 5);
 		gbc_btnMyCalendar_2.gridx = 2;
 		gbc_btnMyCalendar_2.gridy = 3;
 		add(btnMyCalendar_2, gbc_btnMyCalendar_2);
 		btnMyCalendar_2.setIcon(new ImageIcon(Themis.class
 				.getResource("/calendar-alt-1-20.png")));
 		if (chckbxUseSingleDay.isSelected()) {
 			btnMyCalendar_2.setEnabled(false);
 		}
 
 		JLabel label_7 = new JLabel("");
 		GridBagConstraints gbc_label_7 = new GridBagConstraints();
 		gbc_label_7.insets = new Insets(0, 0, 5, 5);
 		gbc_label_7.gridx = 1;
 		gbc_label_7.gridy = 5;
 		add(label_7, gbc_label_7);
 
 		JLabel lblAllskyGroundStation = new JLabel("All-Sky Ground Station:");
 		GridBagConstraints gbc_lblAllskyGroundStation = new GridBagConstraints();
 		gbc_lblAllskyGroundStation.anchor = GridBagConstraints.WEST;
 		gbc_lblAllskyGroundStation.insets = new Insets(0, 0, 5, 5);
 		gbc_lblAllskyGroundStation.gridx = 0;
 		gbc_lblAllskyGroundStation.gridy = 8;
 		add(lblAllskyGroundStation, gbc_lblAllskyGroundStation);
 
 		JLabel lblLevel = new JLabel("Level 1:");
 		GridBagConstraints gbc_lblLevel = new GridBagConstraints();
 		gbc_lblLevel.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLevel.gridx = 1;
 		gbc_lblLevel.gridy = 8;
 		add(lblLevel, gbc_lblLevel);
 
 		JLabel lblLevel_1 = new JLabel("Level 2:");
 		GridBagConstraints gbc_lblLevel_1 = new GridBagConstraints();
 		gbc_lblLevel_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLevel_1.gridx = 3;
 		gbc_lblLevel_1.gridy = 8;
 		add(lblLevel_1, gbc_lblLevel_1);
 
 		JList list = new JList();
 		GridBagConstraints gbc_list = new GridBagConstraints();
 		gbc_list.insets = new Insets(0, 0, 5, 5);
 		gbc_list.fill = GridBagConstraints.BOTH;
 		gbc_list.gridx = 0;
 		gbc_list.gridy = 7;
 		add(list, gbc_list);
 
 		// JList list_1 = new JList();
 		JList list_1 = getList0();
 		GridBagConstraints gbc_list_1 = new GridBagConstraints();
 		gbc_list_1.insets = new Insets(0, 0, 5, 5);
 		gbc_list_1.fill = GridBagConstraints.BOTH;
 		gbc_list_1.gridx = 0;
 		gbc_list_1.gridy = 9;
 		add(list_1, gbc_list_1);
 
 		JList list_2 = getList1();
 		GridBagConstraints gbc_list_2 = new GridBagConstraints();
 		gbc_list_2.insets = new Insets(0, 0, 5, 5);
 		gbc_list_2.fill = GridBagConstraints.BOTH;
 		gbc_list_2.gridx = 1;
 		gbc_list_2.gridy = 9;
 		add(list_2, gbc_list_2);
 
 		JList list_3 = getList2();
 		GridBagConstraints gbc_list_3 = new GridBagConstraints();
 		gbc_list_3.insets = new Insets(0, 0, 5, 5);
 		gbc_list_3.fill = GridBagConstraints.BOTH;
 		gbc_list_3.gridx = 3;
 		gbc_list_3.gridy = 9;
 		add(list_3, gbc_list_3);
 
 		JButton button = new JButton("");
 		button.setIcon(new ImageIcon(Themis.class
 				.getResource("/arrow-1-right20.png")));
 		GridBagConstraints gbc_button = new GridBagConstraints();
 		gbc_button.insets = new Insets(0, 0, 5, 5);
 		gbc_button.gridx = 5;
 		gbc_button.gridy = 9;
 		add(button, gbc_button);
 
 		JButton btnClearProbestation = new JButton("Clear Probe/Station");
 		btnClearProbestation.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		GridBagConstraints gbc_btnClearProbestation = new GridBagConstraints();
 		gbc_btnClearProbestation.insets = new Insets(0, 0, 0, 5);
 		gbc_btnClearProbestation.gridx = 0;
 		gbc_btnClearProbestation.gridy = 10;
 		add(btnClearProbestation, gbc_btnClearProbestation);
 
 		JButton btnClearDataType = new JButton("Clear Data Type");
 		GridBagConstraints gbc_btnClearDataType = new GridBagConstraints();
 		gbc_btnClearDataType.insets = new Insets(0, 0, 0, 5);
 		gbc_btnClearDataType.gridx = 3;
 		gbc_btnClearDataType.gridy = 10;
 		add(btnClearDataType, gbc_btnClearDataType);
 
 		JButton btnNewButton_2 = new JButton("");
 		btnNewButton_2.setIcon(new ImageIcon(Themis.class
 				.getResource("/trash-empty20.png")));
 		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
 		gbc_btnNewButton_2.insets = new Insets(0, 0, 0, 5);
 		gbc_btnNewButton_2.gridx = 5;
 		gbc_btnNewButton_2.gridy = 10;
 		add(btnNewButton_2, gbc_btnNewButton_2);
 
 		JButton btnDeleteAllData = new JButton("Delete All Data");
 		GridBagConstraints gbc_btnDeleteAllData = new GridBagConstraints();
 		gbc_btnDeleteAllData.gridx = 6;
 		gbc_btnDeleteAllData.gridy = 10;
 		add(btnDeleteAllData, gbc_btnDeleteAllData);
 	}
 
 	public String[] getInstrumentTypes() {
 		String[] result = { "1", "2" };
 		return result;
 	}
 
 	public String[] getOutputCoordinates() {
 		String[] result = { "1", "2" };
 		return result;
 	}
 
 	public String getStartTime() {
 		return "1";
 	}
 
 	public String getStopTime() {
 		return "1";
 	}
 
 	public JList getList0() {
 		String[] result = { root.getChild("Tab").getChild("Name").getValue(),
 				"1", "2" };
 		JList jList = new JList(result);
 		return jList;
 	}
 
 	public JList getList1() {
 		String[] result = { "1", "2" };
 		JList jList = new JList(result);
 		return jList;
 	}
 
 	public JList getList2() {
 		String[] result = { "1", "2" };
 		JList jList = new JList(result);
 		return jList;
 	}
 
 }
