 /**
  * This file is part of Vitam Project.
  * 
  * Copyright 2010, Frederic Bregier, and individual contributors by the @author tags. See the
  * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
  * 
  * All Vitam Project is free software: you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
  * 
  * Vitam is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Vitam. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package fr.gouv.culture.vitam.dbgui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.swing.border.CompoundBorder;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import fr.gouv.culture.vitam.database.DbField;
 import fr.gouv.culture.vitam.database.DbSchema;
 import fr.gouv.culture.vitam.database.DbTable;
 import fr.gouv.culture.vitam.database.DbVitam2Database;
 import fr.gouv.culture.vitam.database.model.DbCondition;
 import fr.gouv.culture.vitam.database.model.DbSelect;
 import fr.gouv.culture.vitam.database.model.DbCondition.DbOperator;
 import fr.gouv.culture.vitam.database.table.ConstanceSelectModel;
 import fr.gouv.culture.vitam.database.utils.FileExtensionFilter;
 import fr.gouv.culture.vitam.database.utils.StaticValues;
 import fr.gouv.culture.vitam.database.utils.swing.CheckComboBox;
 import fr.gouv.culture.vitam.database.utils.swing.CheckComboBoxSelectionChangedListener;
 import fr.gouv.culture.vitam.writer.SimpleDelimiterWriter;
 
 import javax.swing.JTable;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 import org.waarp.common.database.DbConstant;
 import org.waarp.common.database.exception.WaarpDatabaseSqlException;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 /**
  * Dialog to handle configuration change in the GUI
  * 
  * @author "Frederic Bregier"
  * 
  */
 public class VitamDatabaseSelectDialog extends JPanel {
 	private static final long serialVersionUID = 5129887729538501977L;
 
 	JFrame frame;
 	private DatabaseGui databaseGui;
 	private static boolean fromMain = false;
 	private JTable table;
 	private DbSchema dbschema;
 	private DbSelect dbselect;
 	private DbVitam2Database database;
 	private ConstanceSelectModel selectModel;
 	private JComboBox comboBoxTable;
 	private JFormattedTextField textPage;
 	private JLabel lblX;
 	private JFormattedTextField textRowPerPage;
 	private JButton butNext;
 	private JButton butPrev;
 	private JButton butFirst;
 	private JButton butLast;
 	private CheckComboBox comboBoxFields;
 	private boolean[] fieldsChecked;
 	private List<DbField> allFields;
 	private JComboBox comboBoxOperator;
 	private JComboBox comboBoxOp1;
 	private JComboBox comboBoxOp2;
 	private JComboBox comboBoxOp3;
 	private DefaultComboBoxModel operatorModel = new DefaultComboBoxModel(
 			DbOperator.getComparator());
 	private DefaultComboBoxModel allFieldsModel;
 	private JButton btnAddCondition;
 	private JComboBox comboBoxConditions;
 	private JButton btnDelCondition;
 	private String biggest = "XXXXXXXXX";
 	private CheckComboBox comboBoxOrderAsc;
 	private CheckComboBox comboBoxOrderDesc;
 	private JButton btnSauveFiltre;
 	private JButton btnChargeFiltre;
 	private JButton btnExportCsv;
 
 	public void setDbVitam2Database(DbVitam2Database database, DbSchema schema) {
 		this.dbschema = schema;
 		this.database = database;
 		List<DbTable> tables = dbschema.getTables();
 		String[] tablenames = new String[tables.size()];
 		biggest = "XXXXXXXXX";
 		for (int i = 0; i < tables.size(); i++) {
 			tablenames[i] = tables.get(i).name;
 			if (biggest.length() < tablenames[i].length()) {
 				biggest = tablenames[i];
 			}
 		}
 		biggest += "XXX";
 		comboBoxTable.setPrototypeDisplayValue(biggest);
 		comboBoxTable.setModel(new DefaultComboBoxModel(tablenames));
 		// View
 		initDbSelect(tables.get(0));
 		allFields = new ArrayList<DbField>();
 		for (DbTable table : tables) {
 			for (DbField field : table.getFields()) {
 				allFields.add(field);
 				if (biggest.length() < field.toString().length()) {
 					biggest = field.toString();
 				}
 			}
 		}
 		biggest += "XXXXX";
 		List<Object> temp = new ArrayList<Object>();
 		temp.add("Valeur");
 		temp.addAll(allFields);
 		allFieldsModel = new DefaultComboBoxModel(temp.toArray());
 		selectModel.initialize(database, dbselect);
 		comboBoxOp1.setPrototypeDisplayValue(biggest);
 		comboBoxOp1.setModel(new DefaultComboBoxModel(tables.get(0).getFields().toArray()));
 		comboBoxOp2.setEnabled(true);
 		comboBoxOp2.setPrototypeDisplayValue(biggest);
 		comboBoxOp2.setModel(allFieldsModel);
 		comboBoxOp2.setEditable(true);
 		comboBoxOp3.setEnabled(false);
 		initValueView();
 		comboBoxOrderAsc.setPrototypeDisplayValue(biggest);
 		comboBoxOrderDesc.setPrototypeDisplayValue(biggest);
 		initFieldView();
 		resizeTable(table);
 	}
 
 	private void initDbSelect(DbTable table) {
 		this.dbselect = new DbSelect();
 		int i = 0;
 		for (DbField field : table.getFields()) {
 			i++;
 			if (i > 3) {
 				break;
 			}
 			dbselect.addSelected(field);
 		}
 		comboBoxConditions.setModel(new DefaultComboBoxModel((Object[]) dbselect.conditions
 				.toArray()));
 	}
 
 	/**
 	 * @param frame
 	 *            the parent frame
 	 * @param databaseGui
 	 *            the DatabaseGui associated
 	 */
 	public VitamDatabaseSelectDialog(JFrame frame, DatabaseGui databaseGui) {
 		super(new BorderLayout());
 
 		this.databaseGui = databaseGui;
 		this.frame = frame;
 		setBorder(new CompoundBorder());
 
 		JPanel buttonPanel = new JPanel();
 		GridBagLayout buttons = new GridBagLayout();
 		buttons.columnWidths = new int[] { 194, 124, 0, 0, 0 };
 		buttons.rowHeights = new int[] { 0, 0 };
 		buttons.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
 		buttons.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
 		buttonPanel.setLayout(buttons);
 		add(buttonPanel, BorderLayout.SOUTH);
 
 		JButton btnSaveConfig = new JButton("Close");
 		btnSaveConfig.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				close();
 			}
 		});
 		GridBagConstraints gbc_btnSaveConfig = new GridBagConstraints();
 		gbc_btnSaveConfig.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSaveConfig.gridx = 0;
 		gbc_btnSaveConfig.gridy = 0;
 		buttonPanel.add(btnSaveConfig, gbc_btnSaveConfig);
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		add(tabbedPane, BorderLayout.CENTER);
 		viewerPanel(tabbedPane);
 	}
 
 	private void viewerPanel(JTabbedPane tabbedPane) {
 		JPanel xmlFilePanel = new JPanel();
 		tabbedPane.addTab("Database Select", null, xmlFilePanel, null);
 		GridBagLayout gbl_xmlFilePanel = new GridBagLayout();
 		gbl_xmlFilePanel.columnWidths = new int[] { 21, 38, 86, 0, 0, 0, 0, 0, 45, 86, 0, 0, 0, 0,
 				72,
 				0,
 				0, 0, 0, 0 };
 		gbl_xmlFilePanel.rowHeights = new int[] { 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0 };
 		gbl_xmlFilePanel.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
 				1.0,
 				1.0, 0.0, 0.0, 0.0, 1.0,
 				1.0, 0.0, 1.0, 0.0, 1.0, 1.0 };
 		gbl_xmlFilePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
 				0.0,
 				0.0,
 				0.0 };
 		xmlFilePanel.setLayout(gbl_xmlFilePanel);
 
 		selectModel = new ConstanceSelectModel();
 		CheckComboBoxSelectionChangedListener listener = new CheckComboBoxSelectionChangedListener() {
 			@Override
 			public void selectionChanged(int idx) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				boolean[] revert = new boolean[fieldsChecked.length];
 				if (idx >= fieldsChecked.length) {
 					if (idx == fieldsChecked.length) {
 						// all
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							revert[i] = fieldsChecked[i];
 							fieldsChecked[i] = true;
 							dbselect.addSelected(allFields.get(i));
 						}
 					} else {
 						// none
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							fieldsChecked[i] = false;
 							dbselect.remSelected(allFields.get(i));
 							if (comboBoxOrderAsc.isCheckBoxSelected(i)) {
 								comboBoxOrderAsc.changeCheckBoxSelection(i);
 							}
 							if (comboBoxOrderDesc.isCheckBoxSelected(i)) {
 								comboBoxOrderDesc.changeCheckBoxSelection(i);
 							}
 						}
 					}
 				} else {
 					fieldsChecked[idx] = !fieldsChecked[idx];
 					if (fieldsChecked[idx]) {
 						dbselect.addSelected(allFields.get(idx));
 					} else {
 						dbselect.remSelected(allFields.get(idx));
 						if (comboBoxOrderAsc.isCheckBoxSelected(idx)) {
 							comboBoxOrderAsc.changeCheckBoxSelection(idx);
 						}
 						if (comboBoxOrderDesc.isCheckBoxSelected(idx)) {
 							comboBoxOrderDesc.changeCheckBoxSelection(idx);
 						}
 					}
 				}
 				try {
 					resetSelect();
 				} catch (WaarpDatabaseSqlException e) {
 					if (idx >= fieldsChecked.length) {
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							fieldsChecked[i] = revert[i];
 							if (!fieldsChecked[i]) {
 								dbselect.remSelected(allFields.get(i));
 								comboBoxFields.changeCheckBoxSelection(i);
 								if (comboBoxOrderAsc.isCheckBoxSelected(i)) {
 									comboBoxOrderAsc.changeCheckBoxSelection(i);
 								}
 								if (comboBoxOrderDesc.isCheckBoxSelected(i)) {
 									comboBoxOrderDesc.changeCheckBoxSelection(i);
 								}
 							}
 						}
 					} else {
 						fieldsChecked[idx] = !fieldsChecked[idx];
 						if (fieldsChecked[idx]) {
 							dbselect.addSelected(allFields.get(idx));
 							comboBoxFields.changeCheckBoxSelection(idx);
 						} else {
 							dbselect.remSelected(allFields.get(idx));
 							comboBoxFields.changeCheckBoxSelection(idx);
 							if (comboBoxOrderAsc.isCheckBoxSelected(idx)) {
 								comboBoxOrderAsc.changeCheckBoxSelection(idx);
 							}
 							if (comboBoxOrderDesc.isCheckBoxSelected(idx)) {
 								comboBoxOrderDesc.changeCheckBoxSelection(idx);
 							}
 						}
 					}
 					try {
 						resetSelect();
 					} catch (WaarpDatabaseSqlException e2) {
 					}
 					JOptionPane.showMessageDialog(frame,
 							"Le champ ajouté provoque un timeout, il est retiré.", "Attention",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				setCursor(null);
 			}
 		};
 
 		CheckComboBoxSelectionChangedListener listenerAsc = new CheckComboBoxSelectionChangedListener() {
 			@Override
 			public void selectionChanged(int idx) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				if (idx >= fieldsChecked.length) {
 					if (idx == fieldsChecked.length) {
 						// all
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							try {
 								dbselect.addOrderAsc(allFields.get(i));
 								if (!fieldsChecked[i]) {
 									comboBoxFields.changeCheckBoxSelection(i);
 									fieldsChecked[i] = true;
 								}
 							} catch (IllegalArgumentException e) {
 								System.err.println("Attention: " + e.toString());
 							}
 						}
 					} else {
 						// none
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							dbselect.remOrderAsc(allFields.get(i));
 						}
 					}
 				} else {
 					if (comboBoxOrderAsc.isCheckBoxSelected(idx)) {
 						try {
 							dbselect.addOrderAsc(allFields.get(idx));
 							if (!fieldsChecked[idx]) {
 								comboBoxFields.changeCheckBoxSelection(idx);
 								fieldsChecked[idx] = true;
 							}
 						} catch (IllegalArgumentException e) {
 							JOptionPane.showMessageDialog(frame, e.toString(), "Attention",
 									JOptionPane.WARNING_MESSAGE);
 							comboBoxOrderAsc.changeCheckBoxSelection(idx);
 						}
 					} else {
 						dbselect.remOrderAsc(allFields.get(idx));
 					}
 				}
 				try {
 					resetSelect();
 				} catch (WaarpDatabaseSqlException e) {
 					if (idx >= fieldsChecked.length) {
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							dbselect.remOrderAsc(allFields.get(i));
 							comboBoxOrderAsc.changeCheckBoxSelection(i);
 						}
 					} else {
 						comboBoxOrderAsc.changeCheckBoxSelection(idx);
 						dbselect.remOrderAsc(allFields.get(idx));
 					}
 					try {
 						resetSelect();
 					} catch (WaarpDatabaseSqlException e2) {
 					}
 					JOptionPane.showMessageDialog(frame,
 							"L'ordre ajouté provoque un timeout, il est retiré.", "Attention",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				setCursor(null);
 			}
 		};
 
 		CheckComboBoxSelectionChangedListener listenerDesc = new CheckComboBoxSelectionChangedListener() {
 			@Override
 			public void selectionChanged(int idx) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				if (idx >= fieldsChecked.length) {
 					if (idx == fieldsChecked.length) {
 						// all
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							try {
 								dbselect.addOrderDesc(allFields.get(i));
 								if (!fieldsChecked[i]) {
 									comboBoxFields.changeCheckBoxSelection(i);
 									fieldsChecked[i] = true;
 								}
 							} catch (IllegalArgumentException e) {
 								System.err.println("Attention: " + e.toString());
 							}
 						}
 					} else {
 						// none
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							dbselect.remOrderDesc(allFields.get(i));
 						}
 					}
 				} else {
 					if (comboBoxOrderDesc.isCheckBoxSelected(idx)) {
 						try {
 							dbselect.addOrderDesc(allFields.get(idx));
 							if (!fieldsChecked[idx]) {
 								comboBoxFields.changeCheckBoxSelection(idx);
 								fieldsChecked[idx] = true;
 							}
 						} catch (IllegalArgumentException e) {
 							JOptionPane.showMessageDialog(frame, e.toString(), "Attention",
 									JOptionPane.WARNING_MESSAGE);
 							comboBoxOrderDesc.changeCheckBoxSelection(idx);
 						}
 					} else {
 						dbselect.remOrderDesc(allFields.get(idx));
 					}
 				}
 				try {
 					resetSelect();
 				} catch (WaarpDatabaseSqlException e) {
 					if (idx >= fieldsChecked.length) {
 						for (int i = 0; i < fieldsChecked.length; i++) {
 							comboBoxOrderDesc.changeCheckBoxSelection(idx);
 							dbselect.remOrderDesc(allFields.get(i));
 						}
 					} else {
 						comboBoxOrderDesc.changeCheckBoxSelection(idx);
 						dbselect.remOrderDesc(allFields.get(idx));
 					}
 					try {
 						resetSelect();
 					} catch (WaarpDatabaseSqlException e2) {
 					}
 					JOptionPane.showMessageDialog(frame,
 							"L'ordre ajouté provoque un timeout, il est retiré.", "Attention",
 							JOptionPane.WARNING_MESSAGE);
 				}
 				setCursor(null);
 			}
 		};
 
 		comboBoxFields = new CheckComboBox(new HashSet<Object>(), true, "Champs selectionnes");
 		comboBoxFields.addSelectionChangedListener(listener);
 		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
 		gbc_comboBox_1.gridwidth = 3;
 		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox_1.gridx = 0;
 		gbc_comboBox_1.gridy = 0;
 		xmlFilePanel.add(comboBoxFields, gbc_comboBox_1);
 
 		comboBoxOrderAsc = new CheckComboBox(new HashSet<Object>(), true, "Ordre Ascendant");
 		comboBoxOrderAsc.addSelectionChangedListener(listenerAsc);
 		GridBagConstraints gbc_comboBox = new GridBagConstraints();
 		gbc_comboBox.gridwidth = 3;
 		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox.gridx = 3;
 		gbc_comboBox.gridy = 0;
 		xmlFilePanel.add(comboBoxOrderAsc, gbc_comboBox);
 
 		comboBoxOrderDesc = new CheckComboBox(new HashSet<Object>(), true, "Ordre Descendant");
 		comboBoxOrderDesc.addSelectionChangedListener(listenerDesc);
 		GridBagConstraints gbc_comboBoxDesc = new GridBagConstraints();
 		gbc_comboBoxDesc.gridwidth = 4;
 		gbc_comboBoxDesc.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxDesc.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxDesc.gridx = 6;
 		gbc_comboBoxDesc.gridy = 0;
 		xmlFilePanel.add(comboBoxOrderDesc, gbc_comboBoxDesc);
 
 		comboBoxConditions = new JComboBox();
 		GridBagConstraints gbc_comboBoxConditions = new GridBagConstraints();
 		gbc_comboBoxConditions.gridwidth = 6;
 		gbc_comboBoxConditions.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxConditions.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxConditions.gridx = 11;
 		gbc_comboBoxConditions.gridy = 0;
 		xmlFilePanel.add(comboBoxConditions, gbc_comboBoxConditions);
 
 		btnDelCondition = new JButton("Condition -");
 		btnDelCondition.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				DbCondition condition = (DbCondition) comboBoxConditions.getSelectedItem();
 				if (condition != null && condition instanceof DbCondition) {
 					dbselect.removeCondition(condition);
 					try {
 						resetSelect();
 					} catch (WaarpDatabaseSqlException e) {
 						dbselect.addCondition(condition);
 						try {
 							resetSelect();
 						} catch (WaarpDatabaseSqlException e2) {
 						}
 						JOptionPane.showMessageDialog(frame,
 								"La condition retirée provoque un timeout, elle est rajoutée.",
 								"Attention", JOptionPane.WARNING_MESSAGE);
 					}
 					checkButtonView();
 				}
 				setCursor(null);
 			}
 		});
 		GridBagConstraints gbc_btnDelCondition = new GridBagConstraints();
 		gbc_btnDelCondition.insets = new Insets(0, 0, 5, 5);
 		gbc_btnDelCondition.gridx = 17;
 		gbc_btnDelCondition.gridy = 0;
 		xmlFilePanel.add(btnDelCondition, gbc_btnDelCondition);
 
 		JLabel lblTable = new JLabel("Table");
 		lblTable.setMinimumSize(new Dimension(40, 14));
 		lblTable.setPreferredSize(new Dimension(40, 14));
 		GridBagConstraints gbc_lblTable = new GridBagConstraints();
 		gbc_lblTable.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTable.anchor = GridBagConstraints.EAST;
 		gbc_lblTable.gridx = 2;
 		gbc_lblTable.gridy = 1;
 		xmlFilePanel.add(lblTable, gbc_lblTable);
 
 		comboBoxOperator = new JComboBox();
 		comboBoxOperator.setPreferredSize(new Dimension(60, 20));
 		comboBoxOperator.setMinimumSize(new Dimension(60, 20));
 		comboBoxOperator.setPrototypeDisplayValue(" Not Between XXX");
 		comboBoxOperator.setModel(new DefaultComboBoxModel(DbOperator.values()));
 		comboBoxOperator.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				DbOperator operator = (DbOperator) comboBoxOperator.getSelectedItem();
 				switch (operator) {
 					case Between:
 					case NotBetween:
 						// 3
 						comboBoxOp2.setEnabled(true);
 						comboBoxOp2.setModel(new DefaultComboBoxModel());
 						comboBoxOp2.setEditable(true);
 						comboBoxOp3.setEnabled(true);
 						comboBoxOp3.setModel(new DefaultComboBoxModel());
 						comboBoxOp3.setEditable(true);
 						break;
 					case LENGTH:
 						// 3
 						comboBoxOp2.setEnabled(true);
 						comboBoxOp2.setModel(operatorModel);
 						comboBoxOp2.setEditable(false);
 						comboBoxOp3.setEnabled(true);
 						comboBoxOp3.setModel(new DefaultComboBoxModel());
 						comboBoxOp3.setEditable(true);
 						break;
 					case Different:
 					case Equal:
 					case Greater:
 					case GreaterOrEqual:
 					case Less:
 					case LessOrEqual:
 						// 2
 						comboBoxOp2.setEnabled(true);
 						comboBoxOp2.setModel(allFieldsModel);
 						comboBoxOp2.setEditable(true);
 						comboBoxOp3.setEnabled(false);
 						break;
 					case Like:
 					case NotLike:
 						// 2
 						comboBoxOp2.setEnabled(true);
 						comboBoxOp2.setModel(new DefaultComboBoxModel());
 						comboBoxOp2.setEditable(true);
 						comboBoxOp3.setEnabled(false);
 						break;
 					case IsNotNull:
 					case IsNull:
 						// 1
 						comboBoxOp2.setEnabled(false);
 						comboBoxOp3.setEnabled(false);
 						break;
 				}
 			}
 		});
 
 		comboBoxTable = new JComboBox();
 		comboBoxTable.setPreferredSize(new Dimension(60, 20));
 		comboBoxTable.setMinimumSize(new Dimension(60, 20));
 		comboBoxTable.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String get = (String) comboBoxTable.getSelectedItem();
 				if (dbschema != null) {
 					DbTable dbtable2 = dbschema.getTable(get);
 					if (dbtable2 != null) {
 						comboBoxOp1.setModel(new DefaultComboBoxModel(dbtable2.getFields()
 								.toArray()));
 					}
 				}
 			}
 		});
 		comboBoxTable.setModel(new DefaultComboBoxModel());
 		GridBagConstraints gbc_comboBoxTable = new GridBagConstraints();
 		gbc_comboBoxTable.gridwidth = 3;
 		gbc_comboBoxTable.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxTable.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxTable.gridx = 3;
 		gbc_comboBoxTable.gridy = 1;
 		xmlFilePanel.add(comboBoxTable, gbc_comboBoxTable);
 		GridBagConstraints gbc_comboBoxOperator = new GridBagConstraints();
 		gbc_comboBoxOperator.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxOperator.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxOperator.gridx = 6;
 		gbc_comboBoxOperator.gridy = 1;
 		xmlFilePanel.add(comboBoxOperator, gbc_comboBoxOperator);
 
 		comboBoxOp1 = new JComboBox();
 		comboBoxOp1.setPreferredSize(new Dimension(150, 20));
 		comboBoxOp1.setMinimumSize(new Dimension(150, 20));
 		GridBagConstraints gbc_comboBoxOp1 = new GridBagConstraints();
 		gbc_comboBoxOp1.gridwidth = 4;
 		gbc_comboBoxOp1.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxOp1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxOp1.gridx = 7;
 		gbc_comboBoxOp1.gridy = 1;
 		xmlFilePanel.add(comboBoxOp1, gbc_comboBoxOp1);
 
 		comboBoxOp2 = new JComboBox();
 		comboBoxOp2.setPreferredSize(new Dimension(150, 20));
 		comboBoxOp2.setMinimumSize(new Dimension(150, 20));
 		GridBagConstraints gbc_comboBoxOp2 = new GridBagConstraints();
 		gbc_comboBoxOp2.gridwidth = 4;
 		gbc_comboBoxOp2.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxOp2.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxOp2.gridx = 11;
 		gbc_comboBoxOp2.gridy = 1;
 		xmlFilePanel.add(comboBoxOp2, gbc_comboBoxOp2);
 
 		comboBoxOp3 = new JComboBox();
 		comboBoxOp3.setPreferredSize(new Dimension(100, 20));
 		comboBoxOp3.setMinimumSize(new Dimension(100, 20));
 		GridBagConstraints gbc_comboBoxOp3 = new GridBagConstraints();
 		gbc_comboBoxOp3.gridwidth = 2;
 		gbc_comboBoxOp3.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxOp3.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxOp3.gridx = 15;
 		gbc_comboBoxOp3.gridy = 1;
 		xmlFilePanel.add(comboBoxOp3, gbc_comboBoxOp3);
 		comboBoxOp3.setEnabled(false);
 
 		btnAddCondition = new JButton("Condition +");
 		btnAddCondition.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				DbOperator operator = (DbOperator) comboBoxOperator.getSelectedItem();
 				DbField field = (DbField) comboBoxOp1.getSelectedItem();
 				DbCondition condition = null;
 				switch (operator) {
 					case Between:
 					case NotBetween: {
 						// 3
 						String combo1 = (String) comboBoxOp2.getSelectedItem();
 						String combo2 = (String) comboBoxOp3.getSelectedItem();
 						Object[] objects = new Object[] { field, combo1, combo2 };
 						condition = new DbCondition(operator, objects);
 					}
 						break;
 					case LENGTH: {
 						// 3
 						DbOperator operator2 = (DbOperator) comboBoxOp2.getSelectedItem();
 						String combo2 = (String) comboBoxOp3.getSelectedItem();
 						Object[] objects = new Object[] { field, operator2, combo2 };
 						condition = new DbCondition(operator, objects);
 					}
 						break;
 					case Different:
 					case Equal:
 					case Greater:
 					case GreaterOrEqual:
 					case Less:
 					case LessOrEqual:
 					case Like:
 					case NotLike: {
 						// 2
 						Object object = comboBoxOp2.getSelectedItem();
 						Object[] objects = new Object[] { field, object };
 						condition = new DbCondition(operator, objects);
 					}
 						break;
 					case IsNotNull:
 					case IsNull: {
 						// 1
 						Object[] objects = new Object[] { field };
 						condition = new DbCondition(operator, objects);
 					}
 						break;
 				}
 				dbselect.addCondition(condition);
 				try {
 					resetSelect();
 				} catch (WaarpDatabaseSqlException e1) {
 					dbselect.removeCondition(condition);
 					try {
 						resetSelect();
 					} catch (WaarpDatabaseSqlException e2) {
 					}
 					JOptionPane.showMessageDialog(frame,
 							"La condition ajoutée provoque un timeout, elle est retirée.",
 							"Attention", JOptionPane.WARNING_MESSAGE);
 				}
 				initValueView();
 				setCursor(null);
 			}
 		});
 		GridBagConstraints gbc_btnAddCondition = new GridBagConstraints();
 		gbc_btnAddCondition.insets = new Insets(0, 0, 5, 5);
 		gbc_btnAddCondition.gridx = 17;
 		gbc_btnAddCondition.gridy = 1;
 		xmlFilePanel.add(btnAddCondition, gbc_btnAddCondition);
 
 		table = new JTable(selectModel) {
 			private static final long serialVersionUID = -8896530264117717457L;
 
 			protected JTableHeader createDefaultTableHeader() {
 				return new JTableHeader(columnModel) {
 					private static final long serialVersionUID = -1026143493017837335L;
 
 					public String getToolTipText(MouseEvent e) {
 						if (dbselect != null) {
 							java.awt.Point p = e.getPoint();
 							int index = columnModel.getColumnIndexAtX(p.x);
 							if (index >= 0) {
 								int realIndex =
 										columnModel.getColumn(index).getModelIndex();
 								return dbselect.selected.get(realIndex).getElement().asXML();
 							}
 						}
 						return null;
 					}
 				};
 			}
 		};
 		table.setAutoCreateRowSorter(true);
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		table.setShowGrid(true);
 
 		JScrollPane scrollpane = new JScrollPane(table);
 		GridBagConstraints gbc_table = new GridBagConstraints();
 		gbc_table.gridheight = 9;
 		gbc_table.gridwidth = 20;
 		gbc_table.insets = new Insets(0, 0, 5, 0);
 		gbc_table.fill = GridBagConstraints.BOTH;
 		gbc_table.gridx = 0;
 		gbc_table.gridy = 2;
 		xmlFilePanel.add(scrollpane, gbc_table);
 
 		textRowPerPage = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		textRowPerPage.setPreferredSize(new Dimension(40, 20));
 		textRowPerPage.setMinimumSize(new Dimension(60, 20));
 		textRowPerPage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				try {
 					int page = Integer.parseInt(textRowPerPage.getText());
 					selectModel.setRowPerPage(page);
 					initValueView();
 					resizeTable(table);
 				} catch (NumberFormatException e2) {
 					initValueView();
 					resizeTable(table);
 				}
 				setCursor(null);
 			}
 		});
 
 		butLast = new JButton("|>");
 		butLast.setMinimumSize(new Dimension(60, 23));
 		butLast.setMaximumSize(new Dimension(60, 23));
 		butLast.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				selectModel.last();
 				checkButtonView();
 				resizeTable(table);
 				setCursor(null);
 			}
 		});
 
 		butNext = new JButton(">>");
 		butNext.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				selectModel.next();
 				checkButtonView();
 				resizeTable(table);
 				setCursor(null);
 			}
 		});
 
 		textPage = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		textPage.setPreferredSize(new Dimension(40, 20));
 		textPage.setMinimumSize(new Dimension(40, 20));
 		textPage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				try {
 					int page = Integer.parseInt(textPage.getText());
 					selectModel.setPage(page);
 					checkButtonView();
 					resizeTable(table);
 				} catch (NumberFormatException e2) {
 					checkButtonView();
 					resizeTable(table);
 				}
 				setCursor(null);
 			}
 		});
 
 		butPrev = new JButton("<<");
 		butPrev.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				selectModel.prev();
 				checkButtonView();
 				resizeTable(table);
 				setCursor(null);
 			}
 		});
 
 		butFirst = new JButton("<|");
 		butFirst.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				selectModel.first();
 				checkButtonView();
 				resizeTable(table);
 				setCursor(null);
 			}
 		});
 
 		btnChargeFiltre = new JButton("Charge Filtre");
 		btnChargeFiltre.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				File file = openFile(null, "Choix d'un filtre à charger", false, "xml");
 				if (file != null) {
 					SAXReader saxReader = new SAXReader();
 					Document document;
 					try {
 						document = saxReader.read(file);
 					} catch (DocumentException e) {
 						JOptionPane.showMessageDialog(frame, "Le fichier n'est pas lisible.",
 								"Attention", JOptionPane.WARNING_MESSAGE);
 						return;
 					}
 					DbSelect dbSelect = DbSelect.fromElement(document.getRootElement(), dbschema);
 					dbselect = dbSelect;
 					selectModel.initialize(database, dbselect);
 					comboBoxConditions.setModel(new DefaultComboBoxModel(dbselect.conditions
 							.toArray()));
 					initValueView();
 					initFieldView();
 					resizeTable(table);
 				}
 			}
 		});
 
 		btnExportCsv = new JButton("Export CSV");
 		btnExportCsv.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				File file = openFile(null, "Export CSV", true, "csv");
 				if (file != null) {
 					SimpleDelimiterWriter writer = new SimpleDelimiterWriter(file, StaticValues.config.separator);
 					try {
 						writer.write(DbConstant.admin.session, dbselect);
 					} catch (WaarpDatabaseSqlException e) {
 						JOptionPane.showMessageDialog(frame, "Le fichier n'a pas pu être exporté.",
 								"Attention", JOptionPane.WARNING_MESSAGE);
 						return;
 					}
 				}
 			}
 		});
 		GridBagConstraints gbc_btnExportCsv = new GridBagConstraints();
 		gbc_btnExportCsv.insets = new Insets(0, 0, 0, 5);
 		gbc_btnExportCsv.gridx = 0;
 		gbc_btnExportCsv.gridy = 11;
 		xmlFilePanel.add(btnExportCsv, gbc_btnExportCsv);
 		GridBagConstraints gbc_btnChargeFiltre = new GridBagConstraints();
 		gbc_btnChargeFiltre.gridwidth = 2;
 		gbc_btnChargeFiltre.insets = new Insets(0, 0, 0, 5);
 		gbc_btnChargeFiltre.gridx = 1;
 		gbc_btnChargeFiltre.gridy = 11;
 		xmlFilePanel.add(btnChargeFiltre, gbc_btnChargeFiltre);
 		GridBagConstraints gbc_buttonFirst = new GridBagConstraints();
 		gbc_buttonFirst.anchor = GridBagConstraints.EAST;
 		gbc_buttonFirst.insets = new Insets(0, 0, 0, 5);
 		gbc_buttonFirst.gridx = 3;
 		gbc_buttonFirst.gridy = 11;
 		xmlFilePanel.add(butFirst, gbc_buttonFirst);
 		GridBagConstraints gbc_btnPrev = new GridBagConstraints();
 		gbc_btnPrev.anchor = GridBagConstraints.WEST;
 		gbc_btnPrev.insets = new Insets(0, 0, 0, 5);
 		gbc_btnPrev.gridx = 4;
 		gbc_btnPrev.gridy = 11;
 		xmlFilePanel.add(butPrev, gbc_btnPrev);
 
 		JLabel lblPage = new JLabel("Page");
 		lblPage.setPreferredSize(new Dimension(40, 14));
 		lblPage.setMaximumSize(new Dimension(50, 14));
 		lblPage.setMinimumSize(new Dimension(50, 14));
 		GridBagConstraints gbc_lblPage = new GridBagConstraints();
 		gbc_lblPage.insets = new Insets(0, 0, 0, 5);
 		gbc_lblPage.anchor = GridBagConstraints.EAST;
 		gbc_lblPage.gridx = 5;
 		gbc_lblPage.gridy = 11;
 		xmlFilePanel.add(lblPage, gbc_lblPage);
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.insets = new Insets(0, 0, 0, 5);
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.gridx = 6;
 		gbc_textField.gridy = 11;
 		xmlFilePanel.add(textPage, gbc_textField);
 		textPage.setColumns(10);
 
 		lblX = new JLabel("/ x");
 		lblX.setMinimumSize(new Dimension(40, 14));
 		GridBagConstraints gbc_lblX = new GridBagConstraints();
 		gbc_lblX.anchor = GridBagConstraints.WEST;
 		gbc_lblX.insets = new Insets(0, 0, 0, 5);
 		gbc_lblX.gridx = 7;
 		gbc_lblX.gridy = 11;
 		xmlFilePanel.add(lblX, gbc_lblX);
 		GridBagConstraints gbc_button_1 = new GridBagConstraints();
 		gbc_button_1.anchor = GridBagConstraints.EAST;
 		gbc_button_1.insets = new Insets(0, 0, 0, 5);
 		gbc_button_1.gridx = 8;
 		gbc_button_1.gridy = 11;
 		xmlFilePanel.add(butNext, gbc_button_1);
 		GridBagConstraints gbc_button_3 = new GridBagConstraints();
 		gbc_button_3.anchor = GridBagConstraints.WEST;
 		gbc_button_3.insets = new Insets(0, 0, 0, 5);
 		gbc_button_3.gridx = 10;
 		gbc_button_3.gridy = 11;
 		xmlFilePanel.add(butLast, gbc_button_3);
 
 		JLabel lblLignepage = new JLabel("Ligne/Page");
 		lblLignepage.setPreferredSize(new Dimension(70, 14));
 		lblLignepage.setMaximumSize(new Dimension(80, 14));
 		lblLignepage.setMinimumSize(new Dimension(80, 14));
 		GridBagConstraints gbc_lblLignepage = new GridBagConstraints();
 		gbc_lblLignepage.anchor = GridBagConstraints.EAST;
 		gbc_lblLignepage.insets = new Insets(0, 0, 0, 5);
 		gbc_lblLignepage.gridx = 11;
 		gbc_lblLignepage.gridy = 11;
 		xmlFilePanel.add(lblLignepage, gbc_lblLignepage);
 		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
 		gbc_textField_1.gridwidth = 4;
 		gbc_textField_1.insets = new Insets(0, 0, 0, 5);
 		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_1.gridx = 12;
 		gbc_textField_1.gridy = 11;
 		xmlFilePanel.add(textRowPerPage, gbc_textField_1);
 		textRowPerPage.setColumns(10);
 
 		btnSauveFiltre = new JButton("Sauve Filtre");
 		btnSauveFiltre.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				File file = openFile(null, "Choix d'un fichier pour sauvegarder le filtre", true,
 						"xml");
 				if (file != null) {
 					Element root = dbselect.toElement();
 					try {
 						FileOutputStream out = new FileOutputStream(file);
 						OutputFormat format = OutputFormat.createPrettyPrint();
 						format.setEncoding(StaticValues.CURRENT_OUTPUT_ENCODING);
 						XMLWriter writer = new XMLWriter(out, format);
 						writer.write(root);
 						writer.close();
 						out.close();
 					} catch (IOException e2) {
 						JOptionPane.showMessageDialog(frame, "Le fichier n'as pu être écrit : "
 								+ e2.toString(), "Attention", JOptionPane.WARNING_MESSAGE);
 						return;
 					}
 				}
 			}
 		});
 		GridBagConstraints gbc_btnSauveFiltre = new GridBagConstraints();
 		gbc_btnSauveFiltre.gridwidth = 2;
 		gbc_btnSauveFiltre.insets = new Insets(0, 0, 0, 5);
 		gbc_btnSauveFiltre.gridx = 16;
 		gbc_btnSauveFiltre.gridy = 11;
 		xmlFilePanel.add(btnSauveFiltre, gbc_btnSauveFiltre);
 		initValueView();
 	}
 
 	public File openFile(String currentValue, String text, boolean save, String extension) {
 		JFileChooser chooser = null;
 		if (currentValue != null) {
 			String file = StaticValues.resourceToFile(currentValue);
 			if (file != null) {
 				File ffile = new File(file).getParentFile();
 				chooser = new JFileChooser(ffile);
 			}
 		}
 		if (chooser == null) {
 			if (StaticValues.config.technicalFile != null) {
 				chooser = new JFileChooser(
 						new File(StaticValues.config.technicalFile).getParentFile());
 			} else {
 				chooser = new JFileChooser(System.getProperty("user.dir"));
 			}
 		}
 		if (extension != null) {
 			FileExtensionFilter filter = new FileExtensionFilter(extension, text);
 			chooser.setFileFilter(filter);
 		}
 		chooser.setDialogTitle(text);
 		if (save) {
 			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				return chooser.getSelectedFile();
 			}
 		} else {
 			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 				return chooser.getSelectedFile();
 			}
 		}
 		return null;
 	}
 
 	public void checkButtonView() {
 		int curpage = selectModel.getPage();
 		int lastpage = selectModel.getPageCount();
 		butFirst.setEnabled(true);
 		butLast.setEnabled(true);
 		butPrev.setEnabled(true);
 		butNext.setEnabled(true);
 		if (curpage == 1) {
 			butFirst.setEnabled(false);
 			butPrev.setEnabled(false);
 		}
 		if (curpage == lastpage) {
 			butLast.setEnabled(false);
 			butNext.setEnabled(false);
 		}
 		if (lastpage == -1) {
 			butLast.setEnabled(false);
 		}
 		textPage.setText(Integer.toString(curpage));
 	}
 
 	public void initValueView() {
 		lblX.setText(" / " + selectModel.getPageCount());
 		textRowPerPage.setText(Integer.toString(selectModel.getRowPerPage()));
 		checkButtonView();
 	}
 
 	public void initFieldView() {
 		if (dbselect != null) {
 			String[] fieldsnames = new String[allFields.size()];
 			fieldsChecked = new boolean[allFields.size()];
 			boolean[] orderAsc = new boolean[allFields.size()];
 			boolean[] orderDesc = new boolean[allFields.size()];
 			for (int i = 0; i < allFields.size(); i++) {
 				DbField field = allFields.get(i);
 				fieldsnames[i] = field.toString();
 				String name = field.toString();
 				if (dbselect.selectedFields.contains(name)) {
 					fieldsChecked[i] = true;
 				} else {
 					fieldsChecked[i] = false;
 				}
 				if (dbselect.orderByAsc.containsKey(name)) {
 					orderAsc[i] = true;
 				} else {
 					orderAsc[i] = false;
 				}
 				if (dbselect.orderByDesc.containsKey(name)) {
 					orderDesc[i] = true;
 				} else {
 					orderDesc[i] = false;
 				}
 			}
 			comboBoxFields.resetObjs(fieldsnames, fieldsChecked, "Champs selectionnes");
 			comboBoxOrderAsc.resetObjs(fieldsnames, orderAsc, "Ordre Ascendant");
 			comboBoxOrderDesc.resetObjs(fieldsnames, orderDesc, "Ordre Descendant");
 		}
 	}
 
 	public void resetSelect() throws WaarpDatabaseSqlException {
 		selectModel.setValidRows();
 		comboBoxConditions.setModel(new DefaultComboBoxModel(dbselect.conditions.toArray()));
 		resizeTable(table);
 	}
 
 	public void resizeTable(JTable table) {
 		DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
 		TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();
 		for (int i = 0; i < table.getColumnCount(); i++) {
 			TableColumn col = colModel.getColumn(i);
 			int width = 0;
 
 			TableCellRenderer colRenderer = col.getHeaderRenderer();
 			if (colRenderer == null) {
 				colRenderer = defaultRenderer;
 			}
 			Component comp = colRenderer.getTableCellRendererComponent(table, col.getHeaderValue(),
 					false,
 					false, 0, i);
 			width = Math.max(width, comp.getPreferredSize().width);
 			for (int r = 0; r < table.getRowCount(); r++) {
 				TableCellRenderer rowRenderer = table.getCellRenderer(r, i);
 				if (rowRenderer == null) {
 					rowRenderer = colRenderer;
 				}
 				comp = rowRenderer.getTableCellRendererComponent(table,
 						table.getValueAt(r, i),
 						false, false, r, i);
 				width = Math.max(width, comp.getPreferredSize().width);
 			}
 			col.setPreferredWidth(width + 4);
 		}
 	}
 
 	public void close() {
 		if (fromMain) {
 
 		} else {
 			this.databaseGui.setEnabled(true);
 			this.databaseGui.requestFocus();
 			this.frame.setVisible(false);
 		}
 		database.close();
 	}
 
 }
