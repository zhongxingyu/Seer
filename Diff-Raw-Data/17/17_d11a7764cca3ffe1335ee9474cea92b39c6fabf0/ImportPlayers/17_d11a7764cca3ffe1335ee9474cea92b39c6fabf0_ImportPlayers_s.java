 package gui.popups;
 
 import database.players.Double;
 import database.players.Player;
 import database.players.Single;
 import database.players.Team2;
 import database.tournamentParts.Group;
 import database.tournamentParts.Tournament;
 import exceptions.InconsistentStateException;
 import exceptions.InputFormatException;
 import gui.Language;
 import gui.Main;
 import gui.templates.IconButton;
 import gui.templates.IconManager;
 import gui.templates.Watcher;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.table.DefaultTableModel;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
 import org.odftoolkit.odfdom.doc.table.OdfTable;
 
 @SuppressWarnings("serial")
 public class ImportPlayers extends Watcher {
 	private String fileName;
 	private boolean[] isMandatory;
 	private String[] columnNames;
 	private String[][] data;
 	private JTable table;
 	private JLabel lDataStartIdx, lDataEndIdx;
 	private JSpinner dataStartIdx, dataEndIdx;
 	private Action aCancel, aAccept;
 	private JButton bCancel, bAccept;
 	private List<JComboBox<String>> columnAssociations;
 	private List<JLabel> lbls;
 
 	public ImportPlayers(Main main, String fileName)
 			throws InconsistentStateException {
 		super(Language.get("importPlayers"), main);
 		Tournament t = main.getTournament();
 		this.fileName = fileName;
 		readFromFile();
 		table = new JTable(data, columnNames);
 		table.getColumnModel().getColumn(0).setMaxWidth(45);
 		dataStartIdx = new JSpinner(new SpinnerNumberModel(1, 1,
 				data.length - 1, 1));
 		dataEndIdx = new JSpinner(new SpinnerNumberModel(data.length, 1,
 				data.length, 1));
 		lDataStartIdx = new JLabel(Language.get("dataStartIdx"));
 		lDataEndIdx = new JLabel(Language.get("dataEndIdx"));
 		aCancel = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();
 			}
 		};
 		bCancel = new IconButton(aCancel, IconManager.getImageIcon("clear"));
 		aAccept = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					accept();
 					dispose();
 				} catch (InputFormatException e1) {
 					System.err
 							.println("Player names have the wrong format.");
 				} catch (InconsistentStateException e1) {
 					System.err
 							.println("Tournament state is inconsistent.");
 				}
 			}
 		};
 		bAccept = new IconButton(aAccept, IconManager.getImageIcon("add"));
 		columnAssociations = new ArrayList<JComboBox<String>>();
 		lbls = new ArrayList<JLabel>();
 		String[] dataNames = null;
 		if (t.getSingle()) {
 			dataNames = Single.getDataNames();
 			isMandatory = Single.mandatoryVec();
 		} else if (t.getDouble()) {
 			dataNames = Double.getDataNames();
 			isMandatory = Double.mandatoryVec();
 		} else if (t.get2Team()) {
 			dataNames = Team2.getDataNames();
 			isMandatory = Team2.mandatoryVec();
 		} else {
 			throw new InconsistentStateException();
 		}
 		List<String> comboBoxEntries = new ArrayList<String>();
 		comboBoxEntries.add(Language.get("notInTable"));
 		comboBoxEntries.addAll(Arrays.asList(columnNames));
 		comboBoxEntries.remove(1);
 		List<String> mandatoryEntries = new ArrayList<String>();
 		mandatoryEntries.addAll(comboBoxEntries);
 		mandatoryEntries.remove(0);
 		for (int i = 0; i < dataNames.length; i++) {
 			JComboBox<String> cb;
 			JLabel lbl;
 			if (isMandatory[i]) {
 				cb = new JComboBox<String>(
 						mandatoryEntries.toArray(new String[0]));
 				lbl = new JLabel(dataNames[i]);
 			} else {
 				cb = new JComboBox<String>(
 						comboBoxEntries.toArray(new String[0]));
 				lbl = new JLabel(dataNames[i] + " (" + Language.get("optional")
 						+ ")");
 			}
 			columnAssociations.add(cb);
 			lbls.add(lbl);
 		}
 		generateWindow();
 	}
 
 	protected void accept() throws InputFormatException,
 			InconsistentStateException {
 		List<String> toAdd = new ArrayList<String>();
 		Tournament t = main.getTournament();
 		String[] line;
 		for (int i = (Integer) dataStartIdx.getValue() - 1; i < Math.min(
 				(Integer) dataEndIdx.getValue(), data.length); i++) {
 			line = data[i];
 			toAdd.clear();
 			for (JComboBox<String> cb : columnAssociations) {
 				int idx = Arrays.asList(columnNames).indexOf(
 						cb.getSelectedItem());
 				if (idx != -1) {
 					toAdd.add(line[idx]);
 				} else {
 					toAdd.add("");
 				}
 			}
 			Player pl = null;
 			if (t.getSingle()) {
 				pl = new Single(t, toAdd.toArray(new String[0]));
 			} else if (t.getDouble()) {
 				pl = new Double(t, toAdd.toArray(new String[0]));
 			} else if (t.get2Team()) {
 				pl = new Team2(t, toAdd.toArray(new String[0]));
 			} else {
 				throw new InconsistentStateException();
 			}
 			t.getQualifying().unassignPlayer(pl, new Group(0));
 		}
 		main.refreshState();
 	}
 
 	private void readFromFile() {
 		if (fileName.toLowerCase().endsWith(".csv")) {
 			readFromCsv();
 		} else if (fileName.toLowerCase().endsWith(".xls")) {
 			readFromXls();
 		} else if (fileName.toLowerCase().endsWith(".xlsx")) {
 			readFromXlsx();
 		} else if (fileName.toLowerCase().endsWith(".ods")) {
 			readFromOds();
 		}
 	}
 
 	private void readFromCsv() {
 		String separator = ",|;|\t";
 		List<String[]> result = new ArrayList<String[]>();
 		BufferedReader in;
 		int maxColums = 0;
 		try {
 			in = new BufferedReader(new FileReader(new File(fileName)));
 			String line = null;
 			String[] splitted;
 			int lineNum = 1;
 			while ((line = in.readLine()) != null) {
 				splitted = (lineNum + ", " + line).replace("\"", "").split(
 						separator);
 				maxColums = Math.max(maxColums, splitted.length);
 				result.add(splitted);
 				lineNum++;
 			}
 		} catch (IOException e) {
 			System.err.println("File not accessible: " + fileName);
 		}
 		data = result.toArray(new String[][] {});
 		columnNames = new String[maxColums];
 		columnNames[0] = "#";
 		for (int i = 1; i < maxColums; i++) {
 			columnNames[i] = Language.get("column") + " " + i;
 		}
 	}
 
 	private void readFromXls() {
 		List<String[]> xlsData = new ArrayList<String[]>();
 		FileInputStream fis;
 		Workbook wb;
 		Sheet sheet;
 		try {
 			fis = new FileInputStream(new File(fileName));
 			wb = new HSSFWorkbook(fis);
 			sheet = wb.getSheetAt(0);
 			int maxColums = 0;
 			for (Row r : sheet) {
 				List<String> cells = new ArrayList<String>();
 				cells.add("" + (r.getRowNum() + 1));
 				int cellCount = 1;
 				for (Cell c : r) {
 					cellCount++;
 					switch (c.getCellType()) {
 					case Cell.CELL_TYPE_STRING:
 						cells.add(c.getStringCellValue());
 						break;
 					case Cell.CELL_TYPE_BLANK:
 						cells.add("");
 						break;
 					case Cell.CELL_TYPE_ERROR:
 						cells.add("");
 						break;
 					case Cell.CELL_TYPE_NUMERIC:
 						double dNum = c.getNumericCellValue();
 						int iNum = (new java.lang.Double(dNum)).intValue();
 						if (dNum - iNum != 0.0) {
 							cells.add(c.toString());
 						} else {
 							cells.add("" + iNum);
 						}
 						break;
 					default:
 						cells.add(c.toString());
 					}
 				}
 				maxColums = Math.max(maxColums, cellCount);
 				xlsData.add(cells.toArray(new String[0]));
 			}
 			data = xlsData.toArray(new String[][] {});
 			columnNames = new String[maxColums];
 			columnNames[0] = "#";
 			for (int i = 1; i < maxColums; i++) {
 				columnNames[i] = Language.get("column") + " " + i;
 			}
 		} catch (FileNotFoundException e) {
 			System.err.println("File not found " + fileName);
 		} catch (IOException e) {
 			System.err.println("I/O Exception " + fileName);
 		}
 	}
 
 	private void readFromXlsx() {
 		List<String[]> xlsData = new ArrayList<String[]>();
 		FileInputStream fis;
 		Workbook wb;
 		Sheet sheet;
 		try {
 			fis = new FileInputStream(new File(fileName));
 			wb = new XSSFWorkbook(fis);
 			sheet = wb.getSheetAt(0);
 			int maxColums = 0;
 			for (Row r : sheet) {
 				List<String> cells = new ArrayList<String>();
 				cells.add("" + (r.getRowNum() + 1));
 				int cellCount = 1;
 				for (Cell c : r) {
 					cellCount++;
 					switch (c.getCellType()) {
 					case Cell.CELL_TYPE_STRING:
 						cells.add(c.getStringCellValue());
 						break;
 					case Cell.CELL_TYPE_BLANK:
 						cells.add("");
 						break;
 					case Cell.CELL_TYPE_ERROR:
 						cells.add("");
 						break;
 					case Cell.CELL_TYPE_NUMERIC:
 						double dNum = c.getNumericCellValue();
 						int iNum = (new java.lang.Double(dNum)).intValue();
 						if (dNum - iNum != 0.0) {
 							cells.add(c.toString());
 						} else {
 							cells.add("" + iNum);
 						}
 						break;
 					default:
 						cells.add(c.toString());
 					}
 				}
 				maxColums = Math.max(maxColums, cellCount);
 				xlsData.add(cells.toArray(new String[0]));
 			}
 			data = xlsData.toArray(new String[][] {});
 			columnNames = new String[maxColums];
 			columnNames[0] = "#";
 			for (int i = 1; i < maxColums; i++) {
 				columnNames[i] = Language.get("column") + " " + i;
 			}
 		} catch (FileNotFoundException e) {
 			System.err.println("File not found " + fileName);
 		} catch (IOException e) {
 			System.err.println("I/O Exception " + fileName);
 		}
 	}
 
 	public void readFromOds() {
 		OdfSpreadsheetDocument ods;
 		try {
 			ods = OdfSpreadsheetDocument.loadDocument(new File(fileName));
 			OdfTable table = ods.getTableList().get(0);
			data = new String[table.getRowCount()][table.getColumnCount() + 1];
			for (int r = 0; r < table.getRowCount(); r++) {
 				data[r][0] = Integer.toString(r + 1);
				for (int c = 0; c < table.getColumnCount(); c++)
 					data[r][c + 1] = table.getCellByPosition(c, r).getDisplayText();
 			}
			columnNames = new String[table.getColumnCount() + 1];
 			columnNames[0] = "#";
			for (int i = 1; i < table.getColumnCount() + 1; i++)
 				columnNames[i] = Language.get("column") + " " + i;
 		} catch (FileNotFoundException e) {
 			System.err.println("File not found " + fileName);
 		} catch (Exception e) {
 			System.err.println("Unknown Error reading file " + fileName);
 		}
 	}
 
 	@Override
 	public void generateWindow() {
 		setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.gridwidth = 1;
 		gbc.gridwidth = 1;
 		gbc.weightx = 0;
 		gbc.weighty = 0;
 		gbc.insets = new Insets(5, 5, 5, 5);
 
 		add(lDataStartIdx, gbc);
 
 		gbc.gridx = 1;
 		gbc.weightx = 1;
 
 		add(dataStartIdx, gbc);
 
 		gbc.gridx = 0;
 		gbc.gridy = 1;
 		gbc.weightx = 0;
 
 		add(lDataEndIdx, gbc);
 
 		gbc.gridx = 1;
 		gbc.weightx = 1;
 
 		add(dataEndIdx, gbc);
 
 		for (int i = 0; i < lbls.size(); i++) {
 			gbc.gridx = 0;
 			gbc.gridy = 2 + i;
 			gbc.weightx = 0;
 
 			add(lbls.get(i), gbc);
 
 			gbc.gridx = 1;
 			gbc.weightx = 1;
 
 			add(columnAssociations.get(i), gbc);
 		}
 
 		gbc.gridy = 2 + lbls.size();
 		gbc.gridx = 0;
 		gbc.weighty = 1;
 		gbc.gridwidth = 2;
 
 		add(new JScrollPane(table), gbc);
 
 		JPanel pan = new JPanel();
 		pan.setLayout(new BorderLayout());
 		pan.add(bCancel, BorderLayout.WEST);
 		pan.add(bAccept, BorderLayout.EAST);
 
 		gbc.gridy = 3 + lbls.size();
 		gbc.weighty = 0;
 
 		add(pan, gbc);
 
 		pack();
 		setVisible(true);
 	}
 
 	@Override
 	public void refresh() {
 		repaint();
 	}
 
 	@Override
 	public void repaint() {
 		setTitle(Language.get("importPlayers"));
 		if (table != null & columnNames != null) {
 			for (int i = 1; i < columnNames.length; i++)
 				columnNames[i] = Language.get("column") + " " + i;
 			DefaultTableModel tm = new DefaultTableModel();
 			tm.setDataVector(data, columnNames);
 			table.setModel(tm);
 			table.getColumnModel().getColumn(0).setMaxWidth(45);
 		}
 		if (lDataStartIdx != null)
 			lDataStartIdx.setText(Language.get("dataStartIdx"));
 		if (lDataEndIdx != null)
 			lDataEndIdx.setText(Language.get("dataEndIdx"));
 		if (columnAssociations != null & lbls != null) {
 			String[] dataNames = null;
 			Tournament t = main.getTournament();
 			if (t.getSingle()) {
 				dataNames = Single.getDataNames();
 			} else if (t.getDouble()) {
 				dataNames = Double.getDataNames();
 			} else if (t.get2Team()) {
 				dataNames = Team2.getDataNames();
 			} else {
 				try {
 					throw new InconsistentStateException();
 				} catch (InconsistentStateException e) {
 					System.err
 							.println("Tournament has inconsistent state.");
 				}
 			}
 			List<String> comboBoxEntries = new ArrayList<String>();
 			comboBoxEntries.add(Language.get("notInTable"));
 			comboBoxEntries.addAll(Arrays.asList(columnNames));
 			comboBoxEntries.remove(1);
 			List<String> mandatoryEntries = new ArrayList<String>();
 			mandatoryEntries.addAll(comboBoxEntries);
 			mandatoryEntries.remove(0);
 			int sel = 0;
 			for (int i = 0; i < columnAssociations.size(); i++) {
 				sel = columnAssociations.get(i).getSelectedIndex();
 				if (isMandatory[i]) {
 					columnAssociations.get(i).setModel(
 							new DefaultComboBoxModel<String>(mandatoryEntries
 									.toArray(new String[0])));
 					lbls.get(i).setText(dataNames[i]);
 				} else {
 					columnAssociations.get(i).setModel(
 							new DefaultComboBoxModel<String>(comboBoxEntries
 									.toArray(new String[0])));
 					lbls.get(i).setText(
 							dataNames[i] + " (" + Language.get("optional")
 									+ ")");
 				}
 				columnAssociations.get(i).setSelectedIndex(sel);
 			}
 		}
 		super.repaint();
 	}
 
 	@Override
 	public void dispose() {
 		main.removeWatcher(this);
 		super.dispose();
 	}
 
 }
