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
 import javax.swing.JTextArea;
 
 import java.awt.Component;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.text.NumberFormat;
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
 import fr.gouv.culture.vitam.database.model.DbSqlKeywords;
 import fr.gouv.culture.vitam.database.table.ConstanceSqlModel;
 import fr.gouv.culture.vitam.database.utils.FileExtensionFilter;
 import fr.gouv.culture.vitam.database.utils.StaticValues;
 import fr.gouv.culture.vitam.database.utils.swing.MyClipboardOwner;
 import fr.gouv.culture.vitam.writer.SimpleDelimiterWriter;
 
 import javax.swing.JTable;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 
 import org.waarp.common.database.DbConstant;
 import org.waarp.common.database.exception.WaarpDatabaseSqlException;
 
 import java.awt.Dimension;
 import java.io.File;
 
 /**
  * Dialog to handle configuration change in the GUI
  * 
  * @author "Frederic Bregier"
  * 
  */
 public class VitamDatabaseFreeSelectDialog extends JPanel {
 	private static final long serialVersionUID = 5129887729538501977L;
 	JFrame frame;
 	private DatabaseGui databaseGui;
 	private static boolean fromMain = false;
 	private JTable table;
 	private DbSchema dbschema;
 	private DbVitam2Database database;
 	private ConstanceSqlModel tableModel;
 	private JComboBox comboBoxTable;
 	private JFormattedTextField textPage;
 	private JLabel lblX;
 	private JFormattedTextField textRowPerPage;
 	private JButton butNext;
 	private JButton butPrev;
 	private JButton butFirst;
 	private JButton butLast;
 	private JComboBox comboBoxFields;
 	private JScrollPane scrollPane;
 	private JTextArea textSql;
 	private MyClipboardOwner clipboard;
 	private JButton btnExecute;
 	private JComboBox comboBoxSQL;
 	private JLabel lblMotsClefsSql;
 	private JButton btnExportCsv;
 
 	public void setDbVitam2Database(DbVitam2Database database, DbSchema schema) {
 		this.dbschema = schema;
 		this.database = database;
 		List<DbTable> tables = dbschema.getTables();
 		String[] tablenames = new String[tables.size()];
 		for (int i = 0; i < tables.size(); i++) {
 			tablenames[i] = tables.get(i).name;
 		}
 		// View
 		tableModel.initialize(database);
 		comboBoxTable.setModel(new DefaultComboBoxModel(tablenames));
 		initValueView();
 		initFieldView(tables.get(0));
 		textSql.setText("SELECT * FROM " + tables.get(0).name);
 		resizeTable(table);
 	}
 
 	/**
 	 * @param frame
 	 *            the parent frame
 	 * @param databaseGui
 	 *            the DatabaseGui associated
 	 */
 	public VitamDatabaseFreeSelectDialog(JFrame frame, DatabaseGui databaseGui) {
 		super(new BorderLayout());
 		this.databaseGui = databaseGui;
 		this.frame = frame;
 		setBorder(new CompoundBorder());
 
 		clipboard = new MyClipboardOwner();
 
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
 		tabbedPane.addTab("Database Free Select", null, xmlFilePanel, null);
 		GridBagLayout gbl_xmlFilePanel = new GridBagLayout();
 		gbl_xmlFilePanel.columnWidths = new int[] { 0, 21, 38, 86, 0, 0, 0, 45, 86, 72, 0, 0, 34, 0 };
 		gbl_xmlFilePanel.rowHeights = new int[] { 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0 };
 		gbl_xmlFilePanel.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0,
 				1.0, 0.0, 0.0, 1.0,
 				Double.MIN_VALUE };
 		gbl_xmlFilePanel.rowWeights = new double[] { 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
 				0.0,
 				0.0,
 				0.0 };
 		xmlFilePanel.setLayout(gbl_xmlFilePanel);
 
 		tableModel = new ConstanceSqlModel();
 
 		scrollPane = new JScrollPane();
 		scrollPane.setMinimumSize(new Dimension(23, 100));
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridwidth = 9;
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 2;
 		gbc_scrollPane.gridy = 0;
 		xmlFilePanel.add(scrollPane, gbc_scrollPane);
 
 		textSql = new JTextArea();
 		textSql.setLineWrap(true);
 		textSql.setPreferredSize(new Dimension(100, 90));
 		textSql.setMinimumSize(new Dimension(100, 90));
 		textSql.setSize(new Dimension(100, 90));
 		scrollPane.setViewportView(textSql);
 		textSql.setColumns(10);
 
 		btnExecute = new JButton("Execute");
 		btnExecute.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					tableModel.changeSelect(textSql.getText());
 				} catch (WaarpDatabaseSqlException e1) {
 					Exception etarget = e1;
 					if (etarget.getCause() != null)  {
 						etarget = (Exception) etarget.getCause();
 					}
 					JOptionPane.showMessageDialog(frame, "La requête est incorrecte: " + etarget.getMessage(),
 							"Attention", JOptionPane.WARNING_MESSAGE);
 				}
 				initValueView();
 				resizeTable(table);
 			}
 		});
 		GridBagConstraints gbc_btnExecute = new GridBagConstraints();
 		gbc_btnExecute.insets = new Insets(0, 0, 5, 5);
 		gbc_btnExecute.gridx = 11;
 		gbc_btnExecute.gridy = 0;
 		xmlFilePanel.add(btnExecute, gbc_btnExecute);
 
 		lblMotsClefsSql = new JLabel("Mots clefs SQL");
 		GridBagConstraints gbc_lblMotsClefsSql = new GridBagConstraints();
 		gbc_lblMotsClefsSql.gridwidth = 2;
 		gbc_lblMotsClefsSql.insets = new Insets(0, 0, 5, 5);
 		gbc_lblMotsClefsSql.anchor = GridBagConstraints.EAST;
 		gbc_lblMotsClefsSql.gridx = 2;
 		gbc_lblMotsClefsSql.gridy = 1;
 		xmlFilePanel.add(lblMotsClefsSql, gbc_lblMotsClefsSql);
 
 		comboBoxSQL = new JComboBox();
 		comboBoxSQL.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				DbSqlKeywords keyword = (DbSqlKeywords) comboBoxSQL.getSelectedItem();
 				if (keyword != null) {
 					clipboard.setClipboardContents(keyword.toString());
 					int caret = textSql.getCaretPosition();
 					textSql.insert(keyword.toString(), caret);
 					textSql.requestFocusInWindow();
 				}
 			}
 		});
 		comboBoxSQL
 				.setToolTipText("La sélection sera copiée dans le Presse-Papier et dans la zone de saisie SQL.");
 		comboBoxSQL.setPrototypeDisplayValue("INSERT INTO tablex ( fieldx ) VALUES ( valuex ) XXX");
 		comboBoxSQL.setModel(new DefaultComboBoxModel(DbSqlKeywords.values()));
 		GridBagConstraints gbc_comboBoxSQL = new GridBagConstraints();
 		gbc_comboBoxSQL.gridwidth = 6;
 		gbc_comboBoxSQL.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBoxSQL.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBoxSQL.gridx = 4;
 		gbc_comboBoxSQL.gridy = 1;
 		xmlFilePanel.add(comboBoxSQL, gbc_comboBoxSQL);
 
 		JLabel lblTable = new JLabel("Table");
 		lblTable.setMinimumSize(new Dimension(50, 14));
 		lblTable.setPreferredSize(new Dimension(40, 14));
 		GridBagConstraints gbc_lblTable = new GridBagConstraints();
 		gbc_lblTable.gridwidth = 2;
 		gbc_lblTable.insets = new Insets(0, 0, 5, 5);
 		gbc_lblTable.anchor = GridBagConstraints.EAST;
 		gbc_lblTable.gridx = 1;
 		gbc_lblTable.gridy = 2;
 		xmlFilePanel.add(lblTable, gbc_lblTable);
 
 		comboBoxTable = new JComboBox();
 		comboBoxTable
 				.setToolTipText("La sélection sera copiée dans le Presse-Papier et dans la zone de saisie SQL.");
 		comboBoxTable.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String get = (String) comboBoxTable.getSelectedItem();
 				if (dbschema != null) {
 					DbTable dbtable2 = dbschema.getTable(get);
 					if (dbtable2 != null) {
 						clipboard.setClipboardContents(get);
 						initFieldView(dbtable2);
 						int caret = textSql.getCaretPosition();
 						textSql.insert(get, caret);
 						textSql.requestFocusInWindow();
 					}
 				}
 			}
 		});
 		comboBoxTable.setModel(new DefaultComboBoxModel());
 		GridBagConstraints gbc_comboBox = new GridBagConstraints();
 		gbc_comboBox.gridwidth = 3;
 		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox.gridx = 3;
 		gbc_comboBox.gridy = 2;
 		xmlFilePanel.add(comboBoxTable, gbc_comboBox);
 
 		comboBoxFields = new JComboBox();
 		comboBoxFields
 				.setToolTipText("La sélection sera copiée dans le Presse-Papier et dans la zone de saisie SQL.");
 		comboBoxFields.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				DbField field = (DbField) comboBoxFields.getSelectedItem();
 				if (field != null) {
 					clipboard.setClipboardContents(field.toString());
 					int caret = textSql.getCaretPosition();
 					textSql.insert(field.toString(), caret);
 					textSql.requestFocusInWindow();
 				}
 			}
 		});
 		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
 		gbc_comboBox_1.gridwidth = 6;
 		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
 		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_comboBox_1.gridx = 6;
 		gbc_comboBox_1.gridy = 2;
 		xmlFilePanel.add(comboBoxFields, gbc_comboBox_1);
 
 		table = new JTable(tableModel) {
 			private static final long serialVersionUID = -8896530264117717457L;
 
 			protected JTableHeader createDefaultTableHeader() {
 				return new JTableHeader(columnModel) {
 					private static final long serialVersionUID = -1026143493017837335L;
 
 					public String getToolTipText(MouseEvent e) {
 						java.awt.Point p = e.getPoint();
 						int index = columnModel.getColumnIndexAtX(p.x);
 						if (index >= 0) {
 							int realIndex =
 									columnModel.getColumn(index).getModelIndex();
 							String columnName = tableModel.getColumnName(realIndex);
 							if (columnName != null) {
 								DbField field = (DbField) dbschema.getObject(columnName.replace(".",
 										DbSchema.FIELDSEP));
 								if (field != null) {
 									return field.getElement().asXML();
 								}
 								return columnName;
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
 		gbc_table.gridheight = 8;
 		gbc_table.gridwidth = 12;
 		gbc_table.insets = new Insets(0, 0, 5, 0);
 		gbc_table.fill = GridBagConstraints.BOTH;
 		gbc_table.gridx = 1;
 		gbc_table.gridy = 3;
 		xmlFilePanel.add(scrollpane, gbc_table);
 
 		butNext = new JButton(">>");
 		butNext.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				tableModel.next();
 				checkButtonView();
 				resizeTable(table);
 			}
 		});
 
 		textPage = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		textPage.setMinimumSize(new Dimension(40, 20));
 		textPage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					int page = Integer.parseInt(textPage.getText());
 					tableModel.setPage(page);
 					checkButtonView();
 					resizeTable(table);
 				} catch (NumberFormatException e2) {
 					checkButtonView();
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
 						writer.write(DbConstant.admin.session, textSql.getText());
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
 
 		JLabel lblPage = new JLabel("Page");
 		GridBagConstraints gbc_lblPage = new GridBagConstraints();
 		gbc_lblPage.insets = new Insets(0, 0, 0, 5);
 		gbc_lblPage.anchor = GridBagConstraints.EAST;
 		gbc_lblPage.gridx = 4;
 		gbc_lblPage.gridy = 11;
 		xmlFilePanel.add(lblPage, gbc_lblPage);
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.insets = new Insets(0, 0, 0, 5);
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.gridx = 5;
 		gbc_textField.gridy = 11;
 		xmlFilePanel.add(textPage, gbc_textField);
 		textPage.setColumns(10);
 
 		lblX = new JLabel("/ x");
 		GridBagConstraints gbc_lblX = new GridBagConstraints();
 		gbc_lblX.anchor = GridBagConstraints.WEST;
 		gbc_lblX.insets = new Insets(0, 0, 0, 5);
 		gbc_lblX.gridx = 6;
 		gbc_lblX.gridy = 11;
 		xmlFilePanel.add(lblX, gbc_lblX);
 		GridBagConstraints gbc_button_1 = new GridBagConstraints();
 		gbc_button_1.insets = new Insets(0, 0, 0, 5);
 		gbc_button_1.gridx = 8;
 		gbc_button_1.gridy = 11;
 		xmlFilePanel.add(butNext, gbc_button_1);
 
 		butPrev = new JButton("<<");
 		butPrev.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				tableModel.prev();
 				checkButtonView();
 				resizeTable(table);
 			}
 		});
 		GridBagConstraints gbc_button_0 = new GridBagConstraints();
 		gbc_button_0.insets = new Insets(0, 0, 0, 5);
 		gbc_button_0.gridx = 3;
 		gbc_button_0.gridy = 11;
 		xmlFilePanel.add(butPrev, gbc_button_0);
 
 		butFirst = new JButton("<|");
 		butFirst.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				tableModel.first();
 				checkButtonView();
 				resizeTable(table);
 			}
 		});
 		GridBagConstraints gbc_button_2 = new GridBagConstraints();
 		gbc_button_2.insets = new Insets(0, 0, 0, 5);
 		gbc_button_2.gridx = 2;
 		gbc_button_2.gridy = 11;
 		xmlFilePanel.add(butFirst, gbc_button_2);
 
 		butLast = new JButton("|>");
 		butLast.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				tableModel.last();
 				checkButtonView();
 				resizeTable(table);
 			}
 		});
 		GridBagConstraints gbc_button_3 = new GridBagConstraints();
 		gbc_button_3.insets = new Insets(0, 0, 0, 5);
 		gbc_button_3.gridx = 9;
 		gbc_button_3.gridy = 11;
 		xmlFilePanel.add(butLast, gbc_button_3);
 
 		textRowPerPage = new JFormattedTextField(NumberFormat.getIntegerInstance());
 		textRowPerPage.setMinimumSize(new Dimension(40, 20));
 		textRowPerPage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					int page = Integer.parseInt(textRowPerPage.getText());
 					tableModel.setRowPerPage(page);
 					initValueView();
 					resizeTable(table);
 				} catch (NumberFormatException e2) {
 					initValueView();
 					resizeTable(table);
 				}
 			}
 		});
 
 		JLabel lblLignepage = new JLabel("Ligne/Page");
 		GridBagConstraints gbc_lblLignepage = new GridBagConstraints();
 		gbc_lblLignepage.insets = new Insets(0, 0, 0, 5);
 		gbc_lblLignepage.anchor = GridBagConstraints.EAST;
 		gbc_lblLignepage.gridx = 10;
 		gbc_lblLignepage.gridy = 11;
 		xmlFilePanel.add(lblLignepage, gbc_lblLignepage);
 		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
 		gbc_textField_1.insets = new Insets(0, 0, 0, 5);
 		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_1.gridx = 11;
 		gbc_textField_1.gridy = 11;
 		xmlFilePanel.add(textRowPerPage, gbc_textField_1);
 		textRowPerPage.setColumns(10);
 
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
				File file = chooser.getSelectedFile();
				if (extension != null) {
					String extfile =  FileExtensionFilter.getExtension(file);
					if (extfile == null || extfile.equalsIgnoreCase(extension)) {
						file = new File(file.getAbsolutePath()+"."+extension);
					}
				}
				return file;
 			}
 		} else {
 			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 				return chooser.getSelectedFile();
 			}
 		}
 		return null;
 	}
 	
 	public void checkButtonView() {
 		int curpage = tableModel.getPage();
 		int lastpage = tableModel.getPageCount();
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
 		textPage.setText(Integer.toString(curpage));
 	}
 
 	public void initValueView() {
 		lblX.setText(" / " + tableModel.getPageCount());
 		textRowPerPage.setText(Integer.toString(tableModel.getRowPerPage()));
 		checkButtonView();
 	}
 
 	public void initFieldView(DbTable dbtable) {
 		if (dbtable != null) {
 			List<DbField> fields = dbtable.getFields();
 			comboBoxFields.setModel(new DefaultComboBoxModel(fields.toArray()));
 		}
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
