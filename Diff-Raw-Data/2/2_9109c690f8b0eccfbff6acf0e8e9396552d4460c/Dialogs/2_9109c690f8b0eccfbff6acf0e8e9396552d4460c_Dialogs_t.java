 /*
  * Copyright (C) 2013 Universitat Pompeu Fabra
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.gwaspi.gui.utils;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.filechooser.FileFilter;
 import org.gwaspi.constants.cDBSamples;
 import org.gwaspi.constants.cExport;
 import org.gwaspi.constants.cExport.ExportFormat;
 import org.gwaspi.constants.cGlobal;
 import org.gwaspi.constants.cImport;
 import org.gwaspi.constants.cImport.ImportFormat;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.GenotypeEncoding;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.constants.cNetCDF.Defaults.StrandType;
 import org.gwaspi.global.Config;
 import org.gwaspi.model.MatricesList;
 import org.gwaspi.model.Matrix;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.OperationsList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Dialogs {
 
 	private static final Logger log
 			= LoggerFactory.getLogger(Dialogs.class);
 
 	private static JFileChooser fc;
 
 	private Dialogs() {
 	}
 
 	//<editor-fold defaultstate="expanded" desc="DIALOG BOXES">
 	public static Operation showOperationCombo(int matrixId, OPType filterOpType) throws IOException {
 		Operation selectedOP = null;
 		List<Operation> operationsList = OperationsList.getOperationsList(matrixId);
 
 		if (!operationsList.isEmpty()) {
 			List<String> operationsNames = new ArrayList<String>();
 			List<Operation> operationAL = new ArrayList<Operation>();
 			for (int i = 0; i < operationsList.size(); i++) {
 				Operation op = operationsList.get(i);
 				if (op.getOperationType().equals(filterOpType)) {
 					StringBuilder sb = new StringBuilder();
 					sb.append("OP: ");
 					sb.append(op.getId());
 					sb.append(" - ");
 					sb.append(op.getFriendlyName());
 					operationsNames.add(sb.toString());
 					operationAL.add(op);
 				}
 			}
 
 			String selectedRow = (String) JOptionPane.showInputDialog(
 					null,
 					"Choose Operation to use...",
 					"Available Census",
 					JOptionPane.QUESTION_MESSAGE,
 					null,
 					operationsNames.toArray(new Object[operationsNames.size()]),
 					0);
 
 			if (selectedRow != null) {
 				selectedOP = operationAL.get(operationsNames.indexOf(selectedRow));
 			}
 		}
 
 		return selectedOP;
 	}
 
 	public static Operation showOperationCombo(int matrixId, List<String> filterOpTypeAL, String title) throws IOException {
 		Operation selectedOP = null;
 		List<Operation> operationsList = OperationsList.getOperationsList(matrixId);
 
 		if (!operationsList.isEmpty()) {
 			List<String> operationsNames = new ArrayList<String>();
 			List<Operation> operationAL = new ArrayList<Operation>();
 			for (int i = 0; i < operationsList.size(); i++) {
 				Operation op = operationsList.get(i);
 				if (filterOpTypeAL.contains(op.getOperationType().toString())) {
 					StringBuilder sb = new StringBuilder();
 					sb.append("OP: ");
 					sb.append(op.getId());
 					sb.append(" - ");
 					sb.append(op.getFriendlyName());
 					operationsNames.add(sb.toString());
 					operationAL.add(op);
 				}
 			}
 
 			if (!operationAL.isEmpty()) {
 				String selectedRow = (String) JOptionPane.showInputDialog(
 						null,
 						"Choose " + title + " to use...",
 						"Available Operations",
 						JOptionPane.QUESTION_MESSAGE,
 						null,
 						operationsNames.toArray(new Object[operationsNames.size()]),
 						0);
 
 				if (selectedRow != null) {
 					selectedOP = operationAL.get(operationsNames.indexOf(selectedRow));
 				}
 			}
 		}
 
 		return selectedOP;
 	}
 
 	public static Operation showOperationSubOperationsCombo(int matrixId, int parentOpId, OPType filterOpType, String title) throws IOException {
 		Operation selectedSubOp = null;
 		List<Operation> operationsList = OperationsList.getOperationsList(matrixId, parentOpId);
 
 		if (!operationsList.isEmpty()) {
 			List<String> operationsNames = new ArrayList<String>();
 			List<Operation> operationAL = new ArrayList<Operation>();
 			for (int i = 0; i < operationsList.size(); i++) {
 				Operation op = operationsList.get(i);
 				if (op.getOperationType().equals(filterOpType)) {
 					StringBuilder sb = new StringBuilder();
 					sb.append("OP: ");
 					sb.append(op.getId());
 					sb.append(" - ");
 					sb.append(op.getFriendlyName());
 					operationsNames.add(sb.toString());
 					operationAL.add(op);
 				}
 			}
 
 			String selectedRow = (String) JOptionPane.showInputDialog(
 					null,
 					"Choose " + title + " to use...",
 					"Available Operations",
 					JOptionPane.QUESTION_MESSAGE,
 					null,
 					operationsNames.toArray(new Object[operationsNames.size()]),
 					0);
 
 			if (selectedRow != null) {
 				selectedSubOp = operationAL.get(operationsNames.indexOf(selectedRow));
 			}
 		}
 
 		return selectedSubOp;
 	}
 
 	public static ImportFormat showTechnologySelectCombo() {
 		ImportFormat[] formats = cImport.ImportFormat.values();
 
 		ImportFormat technology = (ImportFormat) JOptionPane.showInputDialog(
 				null,
 				"What format?",
 				"Platform, Format or Technology",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				formats,
 				formats[0]);
 
 		return technology;
 	}
 
 	public static ExportFormat showExportFormatsSelectCombo() {
 		ExportFormat[] formats = cExport.ExportFormat.values();
 
 		ExportFormat expFormat = (ExportFormat) JOptionPane.showInputDialog(
 				null,
 				"What format?",
 				"Export Format",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				formats,
 				formats[0]);
 
 		return expFormat;
 	}
 
 	public static String showPhenotypeColumnsSelectCombo() {
 		String[] phenotype = cDBSamples.f_PHENOTYPES_COLUMNS;
 
 		String expPhenotype = (String) JOptionPane.showInputDialog(
 				null,
 				"What phenotype?",
 				"Phenotype column to use",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				phenotype,
 				phenotype[0]);
 
 		return expPhenotype;
 	}
 
 	public static StrandType showStrandSelectCombo() {
 		StrandType[] strandFlags = cNetCDF.Defaults.StrandType.values();
 
 		StrandType strandType = (StrandType) JOptionPane.showInputDialog(
 				null,
 				"What strand are the genotypes located on?",
 				"Genotypes Strand",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				strandFlags,
 				strandFlags[0]);
 
 		return strandType;
 
 	}
 
 	public static String showChromosomeSelectCombo() {
 		String[] chroms = cNetCDF.Defaults.Chromosomes;
 
 		String chr = (String) JOptionPane.showInputDialog(
 				null,
 				"What chromosome are the genotypes placed at?",
 				"Chromosome",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				chroms,
 				chroms[0]);
 
 		return chr;
 	}
 
 	public static GenotypeEncoding showGenotypeCodeSelectCombo() {
 		GenotypeEncoding[] gtCode = cNetCDF.Defaults.GenotypeEncoding.values();
 
 		GenotypeEncoding strandType = (GenotypeEncoding) JOptionPane.showInputDialog(
 				null,
 				"What code are the genotypes noted in?",
 				"Genotype Encoding",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				gtCode,
 				gtCode[0]);
 
 		return strandType;
 	}
 
 	public static int showMatrixSelectCombo() throws IOException {
 		List<Matrix> matrixList = MatricesList.getMatrixList();
 		//String[] matrixNames = new String[matrices.matrixList.size()];
 		List<String> matrixNames = new ArrayList<String>();
 		List<Integer> matrixIDs = new ArrayList<Integer>();
 		for (int i = 0; i < matrixList.size(); i++) {
 			Matrix mx = matrixList.get(i);
 			StringBuilder mn = new StringBuilder();
 			mn.append("SID: ");
 			mn.append(mx.getStudyId());
 			mn.append(" - MX: ");
 			mn.append(mx.getMatrixMetadata().getMatrixFriendlyName());
 			//matrixNames[i]=mn.toString();
 			matrixNames.add(mn.toString());
 			matrixIDs.add(mx.getId());
 		}
 
 		String selectedRow = (String) JOptionPane.showInputDialog(
 				null,
 				"What code are the genotypes noted in?",
 				"Genotype Encoding",
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				matrixNames.toArray(new Object[matrixNames.size()]),
 				0);
 
 		int selectedMatrix = Integer.MIN_VALUE;
 		if (selectedRow != null) {
 			selectedMatrix = matrixIDs.get(matrixNames.indexOf(selectedRow));
 		}
 		return selectedMatrix;
 	}
 
 	public static Integer showConfirmDialogue(String message) {
 		return JOptionPane.showConfirmDialog(null, message, "Confirm?", JOptionPane.QUESTION_MESSAGE);
 	}
 
 	public static void showWarningDialogue(String message) {
 		JOptionPane.showMessageDialog(null, message, "Warning!", JOptionPane.WARNING_MESSAGE);
 	}
 
 	public static void showInfoDialogue(String message) {
 		JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	public static int showOptionDialogue(String title, String message, String button1, String button2, String button3) {
 		Object[] options = {button1,
 			button2,
 			button3};
 		return JOptionPane.showOptionDialog(
 				null,
 				message,
 				title,
 				JOptionPane.YES_NO_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE,
 				null,
 				options,
 				options[2]);
 	}
 
 	public static String showInputBox(String message) {
 		return JOptionPane.showInputDialog(null, message, "Input text...", JOptionPane.PLAIN_MESSAGE);
 	}
 	//</editor-fold>
 
 	// <editor-fold defaultstate="expanded" desc="FILE OPEN DIALOGUES">
 	public static void selectAndSetFileDialog(ActionEvent evt, JButton openButton, JTextField textField, final String filter) {
 		selectAndSetFileInCurrentDirDialog(evt, openButton, cGlobal.HOMEDIR, textField, filter);
 	}
 
 	public static void selectAndSetFileInCurrentDirDialog(ActionEvent evt, JButton openButton, String dir, JTextField textField, final String filter) {
 		// Create a file chooser
 		fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 
 		// getting the latest opened dir
 		try {
 //			File tmpFile = new File(dir);
 //			if(!tmpFile.exists()){
 			String tmpDir = Config.getConfigValue(Config.PROPERTY_LAST_OPENED_DIR, cGlobal.HOMEDIR);
 			fc.setCurrentDirectory(new File(tmpDir));
 //			}
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 
 		// displaying only necessary files as requested by "filter"
 		fc.setFileFilter(new FileFilter() {
 			public boolean accept(File f) {
 				return f.getName().toLowerCase().endsWith(filter) || f.isDirectory();
 			}
 
 			public String getDescription() {
 				String filterDesc;
 				if (filter.equals("")) {
 					filterDesc = "All files";
 				} else {
 					filterDesc = filter + " files";
 				}
 				return filterDesc;
 			}
 		});
 
 		int returnVal = fc.showOpenDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame);
 
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			File file = fc.getSelectedFile();
 			textField.setText(file.getAbsolutePath());
 
 			// setting the directory to latest opened dir
 			try {
 				Config.setConfigValue(Config.PROPERTY_LAST_OPENED_DIR, file.getParent());
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 
 	public static File selectAndSetDirectoryDialog(ActionEvent evt, JButton openButton, JTextField textField, String dir, final String filter) {
 
 		File resultFile = null;
 		// Create a file chooser
 		fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 		// Handle open button action.
 		if (evt.getSource() == openButton) {
 
 			// getting the latest opened dir
 			try {
 //				File tmpFile = new File(dir);
 //				if(!tmpFile.exists()){
 				String tmpDir = Config.getConfigValue(Config.PROPERTY_LAST_OPENED_DIR, cGlobal.HOMEDIR);
 				fc.setCurrentDirectory(new File(tmpDir));
 //				}
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 
 			fc.setFileFilter(new FileFilter() {
 				public boolean accept(File f) {
 					return f.getName().toLowerCase().endsWith(filter) || f.isDirectory();
 				}
 
 				public String getDescription() {
 					String filterDesc;
 					if (filter.isEmpty()) {
 						filterDesc = "All files";
 					} else {
 						filterDesc = filter + " files";
 					}
 					return filterDesc;
 				}
 			});
 			int returnVal = fc.showOpenDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				resultFile = fc.getSelectedFile();
 				textField.setText(resultFile.getPath());
 
 				// setting the directory to latest opened dir
 				try {
 					Config.setConfigValue(Config.PROPERTY_LAST_OPENED_DIR, resultFile.getParent());
 				} catch (IOException ex) {
 					log.error(null, ex);
 				}
 			}
 		}
 
 		return resultFile;
 	}
 
 	public static File selectDirectoryDialog(int okOption) {
 
 		File resultFile = null;
 		// Create a file chooser
 		fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
 		// Handle open button action.
 		if (okOption == JOptionPane.OK_OPTION) {
 
 			// getting the latest opened dir
 			try {
 				String dir = Config.getConfigValue(Config.PROPERTY_LAST_OPENED_DIR, cGlobal.HOMEDIR);
 				fc.setCurrentDirectory(new File(dir));
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 
 			int returnVal = fc.showOpenDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				resultFile = fc.getSelectedFile();
 
 				// setting the directory to latest opened dir
 				try {
					Config.setConfigValue(Config.PROPERTY_LAST_OPENED_DIR, resultFile);
 				} catch (IOException ex) {
 					log.error(null, ex);
 				}
 			}
 		}
 		return resultFile;
 	}
 
 	public static File selectFilesAndDirectoriesDialog(int okOption) {
 
 		File resultFile = null;
 		// Create a file chooser
 		fc = new JFileChooser();
 		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 
 		// Handle open button action.
 		if (okOption == JOptionPane.OK_OPTION) {
 
 			// getting the last opened dir
 			try {
 				String dir = Config.getConfigValue(Config.PROPERTY_LAST_OPENED_DIR, cGlobal.HOMEDIR);
 				fc.setCurrentDirectory(new File(dir));
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 
 			int returnVal = fc.showOpenDialog(org.gwaspi.gui.StartGWASpi.mainGUIFrame);
 
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				resultFile = fc.getSelectedFile();
 
 				// setting the directory to last opened dir
 				try {
 					Config.setConfigValue(Config.PROPERTY_LAST_OPENED_DIR, resultFile.getParent());
 				} catch (IOException ex) {
 					log.error(null, ex);
 				}
 			}
 		}
 		return resultFile;
 	}
 	// </editor-fold>
 }
