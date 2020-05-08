 package org.csrdu.apps.nugradingtable;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import jxl.Cell;
 import jxl.Sheet;
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 
 public class ExcelImporter {
 
 	private Vector<StudentResult> results;
 
 	public ExcelImporter(Vector<StudentResult> results) {
 		this.results = results;
 	}
 
 	public void importDataFromFile(File file) {
 		// Vector data = new Vector();
 		int snoCol = -1;
 		int headerRow = -1;
 		int beginStudentRow = -1;
 		int endStudentRow = -1;
 		try {
 			Workbook workbook = null;
 			try {
 				workbook = Workbook.getWorkbook(file);
 			} catch (IOException ex) {
 				System.out.println("Error reading excel file.");
 				ex.printStackTrace();
 			}
 			Sheet sheet = workbook.getSheet(0);
 
 			// Find the column with SNO ...
 			System.out.println("Found total rows: " + sheet.getRows());
 
 			for (int j = 0; j < sheet.getRows(); j++) {
 				for (int i = 0; i < sheet.getColumns(); i++) {
 					Cell cell = sheet.getCell(i, j);
 					String cellContents = cell.getContents().trim();
					// System.out.println(cellContents);
					if (cellContents.toLowerCase().trim().contains("roll")) {
 						snoCol = i;
 						headerRow = j;
 					}
 				}
 			}
			if(headerRow == -1) {
				// System.out.println("Couldn't find header row.");
				JOptionPane.showMessageDialog(null, "Could not find header row.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return; 
			}
 			System.out.println("Found header row at: " + headerRow);
 
 			// go to left on header row and collect all headers...
 			Map<String, Integer> headers = new HashMap<String, Integer>();
 			Vector<String> headerNames = new Vector<String>();
 			Vector<Integer> headerCols = new Vector<Integer>();
 			{
 				int i = 0;
 				while (i < sheet.getColumns()
 						&& !sheet.getCell(i, headerRow).getContents().trim()
 								.equals("")) {
 					headers.put(sheet.getCell(i, headerRow).getContents(),new Integer(i));
 					headerNames.add(sheet.getCell(i, headerRow).getContents());
 					i++;
 				}
 			}
 
 			// create a dialog box and get the headers corresponding to required
 			// fields
 			showHeaderDialog(headers, headerNames, headerCols);
 
 			// find row for first and last student.
 			boolean beenEmpty = true;
 			for (int i = headerRow + 1; i < sheet.getRows(); i++) {
 				if (!sheet.getCell(snoCol, i).getContents().trim().equals("")) {
 					// we have content and this HAS to be the first student
 					if (beenEmpty) {
 						beginStudentRow = i;
 						beenEmpty = false; // no longer empty
 					}
 				}
 				if (sheet.getCell(snoCol, i).getContents().trim().equals("")) {
 					// we have not been empty and we just got empty. So, the row
 					// before this one was the last student and we're done
 					if (!beenEmpty) {
 						endStudentRow = i - 1;
 						break;
 					}
 				}
 			}
 			// if we still don't have endStudentRow, it's the last one.
 			if (endStudentRow == -1)
 				endStudentRow = sheet.getRows();
 
			if(beginStudentRow == -1) {
				// System.out.println("Couldn't find header row.");
				JOptionPane.showMessageDialog(null, "Could not find any student record.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return; 
			}
			
 			System.out.println("Students rows from " + beginStudentRow + " to "
 					+ endStudentRow);
 
 			// now we loop from beginStudentRow to endStudentRow and populate
 			// the data based on headerCols[x]
 			for (int row = beginStudentRow; row <= endStudentRow; row++){
 				StudentResult res = new StudentResult(); 
 				res.setsNo(new Integer(sheet.getCell(headerCols.get(0), row).getContents()));
 				res.setStudentID(sheet.getCell(headerCols.get(1), row).getContents());
 				res.setStudentName(sheet.getCell(headerCols.get(2), row).getContents());
 				res.setTotalMarks(new Double(sheet.getCell(headerCols.get(3), row).getContents()));
 				res.setProposedGrade(sheet.getCell(headerCols.get(4), row).getContents());
 				results.add(res);
 			}
 			
 			
 			// data.clear();
 			// System.out.println("Num rows: " + sheet.getRows());
 			// for (int j = 1; j < sheet.getRows(); j++) {
 			// Vector d = new Vector();
 			// for (int i = 0; i < sheet.getColumns(); i++) {
 			//
 			// Cell cell = sheet.getCell(i, j);
 			// System.out.println("Col (" + j + "," + i + "):  "
 			// + cell.getContents());
 			// d.add(cell.getContents());
 			//
 			// }
 			// d.add("\n");
 			// data.add(d);
 			// }
 			// } catch (BiffException e) {
 			// e.printStackTrace();
 			// }
 			// System.out.println("Done reading.");
 		} catch (BiffException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/** Show a dialog to get the mappings 
 	 * 
 	 * @param headers The headers collected from the sheet 
 	 * @param headerNames 
 	 * @param headerCols The column number returned 
 	 * */ 
 	private void showHeaderDialog(Map<String, Integer> headers, Vector<String> headerNames, Vector<Integer> headerCols) {
 		// setting options pane ... 
 		JPanel optionsPane = new JPanel();
 		optionsPane.setLayout(new GridBagLayout());
 		GridBagConstraints c2 = new GridBagConstraints();
 
 		
 		// add all components 
 		// sno 
 		JComboBox comboSno = new JComboBox();
 		for (String header : headerNames)
 			comboSno.addItem(header);
 
 		JLabel label = new JLabel("S.No.");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0; c2.gridy = 0; c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 		c2.gridx = 1; c2.gridy = 0; c2.weightx = 0.25;
 		optionsPane.add(comboSno, c2);
 		
 		// studentID 
 		JComboBox comboSID = new JComboBox();
 		for (String header : headerNames)
 			comboSID.addItem(header);
 
 		label = new JLabel("Student ID");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0; c2.gridy = 1; c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 		c2.gridx = 1; c2.gridy = 1; c2.weightx = 0.25;
 		optionsPane.add(comboSID, c2);
 
 		// student name 
 		JComboBox comboSName = new JComboBox();
 		for (String header : headerNames){
 			comboSName.addItem(header);
 		}
 			
 
 		label = new JLabel("Student Name");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0; c2.gridy = 2; c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 		c2.gridx = 1; c2.gridy = 2; c2.weightx = 0.25;
 		optionsPane.add(comboSName, c2);
 
 		// Total maarks
 		JComboBox comboTMarks = new JComboBox();
 		for (String header : headerNames)
 			comboTMarks.addItem(header);
 
 		label = new JLabel("Total Marks");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0; c2.gridy = 3; c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 		c2.gridx = 1; c2.gridy = 3; c2.weightx = 0.25;
 		optionsPane.add(comboTMarks, c2);
 		
 		
 		// Proposed Grade
 		JComboBox comboPropGrade = new JComboBox();
 		for (String header : headerNames)
 			comboPropGrade.addItem(header);
 
 		label = new JLabel("Proposed Grade");
 		c2.fill = GridBagConstraints.HORIZONTAL;
 		c2.gridx = 0; c2.gridy = 4; c2.weightx = 0.25;
 		optionsPane.add(label, c2);
 		c2.gridx = 1; c2.gridy = 4; c2.weightx = 0.25;
 		optionsPane.add(comboPropGrade, c2);
 		
 		
 		// let's try to predict the header columns 
 		for (int i = 0; i < headerNames.size(); i++){
			if(headerNames.get(i).trim().toLowerCase().equals("no."))
 				comboSno.setSelectedIndex(i);
			else if(headerNames.get(i).toLowerCase().contains("roll"))
 				comboSID.setSelectedIndex(i);
 			else if(headerNames.get(i).toLowerCase().contains("name"))
 				comboSName.setSelectedIndex(i);
 			else if(headerNames.get(i).toLowerCase().contains("total"))
 				comboTMarks.setSelectedIndex(i);
 			else if(headerNames.get(i).toLowerCase().contains("grade"))
 				comboPropGrade.setSelectedIndex(i);
 		}
 
 		
 		// now show the dialog 
 		JOptionPane.showMessageDialog(null, optionsPane, "Specify Column Mappings",
 				JOptionPane.PLAIN_MESSAGE);
 
 		// let's get the selected header columns 
 		headerCols.add(headers.get(comboSno.getSelectedItem()));
 		headerCols.add(headers.get(comboSID.getSelectedItem()));
 		headerCols.add(headers.get(comboSName.getSelectedItem()));
 		headerCols.add(headers.get(comboTMarks.getSelectedItem()));
 		headerCols.add(headers.get(comboPropGrade.getSelectedItem()));
 		return; 
 	}
 }
