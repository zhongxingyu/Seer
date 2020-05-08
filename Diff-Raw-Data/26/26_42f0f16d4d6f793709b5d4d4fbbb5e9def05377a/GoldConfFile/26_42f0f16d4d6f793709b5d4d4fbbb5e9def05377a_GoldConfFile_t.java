 package org.bestgrid.virtscreen.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.exceptions.RemoteFileSystemException;
 import org.vpac.grisu.frontend.control.clientexceptions.FileTransactionException;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.GrisuRegistryManager;
 
 public class GoldConfFile {
 
 	public enum PARAMETER {
 		protein_datafile, ligand_data_file, directory, concatenated_output, score_param_file
 	}
 
 	private final String url;
 	private final ServiceInterface si;
 	private final FileManager fm;
 	private final File templateFile;
 
 	private File newConfFile;

 	private String[] externalLigand = null;
 	private int externalLigandAmount = -1;
 
 	private final List<String> configLines;
 
 	// private String currentJobDirectory = null;
 
 	private final Map<PARAMETER, String> parameters = new HashMap<PARAMETER, String>();
 
 	private final Map<String, String> inputFiles = new HashMap<String, String>();
 
 	private LigandFiles libFile = null;
 
 	private final String parentUrl;
 
 	public GoldConfFile(ServiceInterface si, String url)
 			throws FileTransactionException {
 		this.si = si;
 		this.url = url;
 		this.parentUrl = FileManager.calculateParentUrl(this.url);
 
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 
 		this.templateFile = this.fm.downloadFile(this.url);
 
 		try {
 			this.configLines = FileUtils.readLines(this.templateFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 		libFile = new LigandFiles(si);
 		libFile.setFiles(getLigandPaths());
 
 		String filePath = getProteinFilePath();
 
 		try {
 			if (!fm.fileExists(filePath)) {
 				String tmp = this.parentUrl + File.separator + filePath;
 				if (fm.fileExists(tmp)) {
 					filePath = tmp;
 				}
 			}
 		} catch (RemoteFileSystemException e) {
 			// Do nothing, will show up later when parsing file
 		}
 
 		setParameter(GoldConfFile.PARAMETER.protein_datafile,
 				FilenameUtils.getName(filePath));
 		inputFiles.put(FilenameUtils.getName(filePath), filePath);
 
 		String optionalParameterFile = getParameter(PARAMETER.score_param_file);
 		if (StringUtils.isNotBlank(optionalParameterFile)
 				&& !"DEFAULT".equalsIgnoreCase(optionalParameterFile)) {
 
 			try {
 				if (!fm.fileExists(optionalParameterFile)) {
 					String tmp = this.parentUrl + File.separator
 							+ optionalParameterFile;
 					if (fm.fileExists(tmp)) {
 						optionalParameterFile = tmp;
 					}
 				}
 			} catch (RemoteFileSystemException e) {
 				// Do nothing, will show up later when parsing file
 			}
 
 			setParameter(PARAMETER.score_param_file,
 					FilenameUtils.getName(optionalParameterFile));
 			inputFiles.put(FilenameUtils.getName(optionalParameterFile),
 					optionalParameterFile);
 		}
 
 	}
 
 	public String[] getLigandUrls() {
 		return this.libFile.getRemoteUrls();
 	}

 	public void setCostumLigandDataFiles(String[] ligand) {
		String[] temp = new String[ligand.length];
		for (int i = 0; i < ligand.length; i++) {
			temp[i] = LigandFiles.VS_LIBRARY_LOCAL_PATH + ligand[i];
		}
		this.externalLigand = temp;
 		this.externalLigandAmount = getLigandDockingAmount();
 	}
 
 	public Set<String> getFilesToStageIn() {
 		return new HashSet(this.inputFiles.values());
 	}
 
 	public void setParameter(PARAMETER key, String value) {
 		parameters.put(key, value);
 	}
 
 	public void updateConfFile() {
 
 		String[] tempArray = Arrays.copyOf(libFile.getRemotePaths(),
 				libFile.getRemotePaths().length + 1);
 		tempArray[tempArray.length - 1] = new Integer(getLigandDockingAmount())
 				.toString();
 		String newLigandValue = StringUtils.join(tempArray, " ");
 
 		parameters.put(PARAMETER.ligand_data_file, newLigandValue);
 
 		String resultsPath = getParameter(PARAMETER.directory);
 		String newResultsPath = "./" + FilenameUtils.getName(resultsPath);
 		parameters.put(PARAMETER.directory, newResultsPath);
 
 		String concat = getParameter(PARAMETER.concatenated_output);
 		String newConcat = newResultsPath + "/" + FilenameUtils.getName(concat);
 
 		parameters.put(PARAMETER.concatenated_output, newConcat);
 
 		for (PARAMETER key : parameters.keySet()) {
 
 			switch (key) {
 
 			case ligand_data_file:
				if (externalLigand != null && externalLigand.length > 0) {
					String temp = StringUtils.join(externalLigand, " ") + " "
							+ new Integer(getLigandDockingAmount()).toString();
 					setLigand_data_file(temp);
 				} else {
 					setLigand_data_file(parameters.get(key));
 				}
 				break;
 
 			default:
 
 				for (int i = 0; i < configLines.size(); i++) {
 					if (configLines.get(i).trim().startsWith(key.toString())) {
 						String tmp = key + " = " + parameters.get(key);
 						configLines.set(i, tmp);
 						break;
 					}
 				}
 			}
 		}
 
 		try {
 			getConfFile().delete();
 			getConfFile().deleteOnExit();
 			FileUtils.writeLines(getConfFile(), configLines);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	public boolean checkForValidity(StringBuffer logMessage, StringBuffer fixes) {
 
 		updateConfFile();
 
 		// check whether protein file exists
 		String protein = inputFiles
 				.get(getParameter(PARAMETER.protein_datafile));
 		logMessage.append("Checking " + PARAMETER.protein_datafile.toString()
 				+ "...\n");
 		if (!fm.isFile(protein)) {
 			logMessage.append("\t" + PARAMETER.protein_datafile.toString()
 					+ " \"" + protein + " \"can't be accessed.\n");
 			fixes.append("\t* Fix path for "
 					+ PARAMETER.protein_datafile.toString() + "\n");
 		} else {
 			logMessage.append("\t" + PARAMETER.protein_datafile.toString()
 					+ "\"" + protein + "\" exists and is file.\n");
 		}
 
 		// check whether ligand files exist remotely
 		logMessage.append("Checking " + PARAMETER.ligand_data_file + "...\n");
 		for (String url : libFile.getRemoteUrls()) {
 			logMessage.append("\tChecking remote library "
 					+ FilenameUtils.getName(url) + ": ");
 			try {
 				if (si.fileExists(url)) {
 					logMessage.append("Installed\n");
 				} else {
 					logMessage.append("Not  installed\n");
 					fixes.append("\t* contact BeSTGRID support (Markus and/or Yuriy) and tell them to get the library file \""
 							+ FilenameUtils.getName(url)
 							+ " \" installed on the cluster\n");
 				}
 			} catch (RemoteFileSystemException e) {
 				logMessage.append("Not  installed/accessible ("
 						+ e.getLocalizedMessage() + "\n");
 				fixes.append("Get library "
 						+ FilenameUtils.getName(url)
 						+ " installed on cluster / Ask BeSTGRID staff to look at access issues\n");
 			}
 		}
 
 		// check whether optional params file is accessible
 		String params = inputFiles
 				.get(getParameter(PARAMETER.score_param_file));
 		if (StringUtils.isNotBlank(params)) {
 			logMessage.append("Checking "
 					+ PARAMETER.score_param_file.toString() + "...\n");
 			if (!fm.isFile(params)) {
 				logMessage.append("\t" + PARAMETER.score_param_file.toString()
 						+ " \"" + params + "\" can't be accessed.\n");
 				fixes.append("\t* fix path for "
 						+ PARAMETER.score_param_file.toString() + "\n");
 			} else {
 				logMessage.append("\t" + PARAMETER.score_param_file.toString()
 						+ "\"" + params + "\" exists and is file.\n");
 			}
 		}
 
 		// output results file
 		logMessage.append("Checking output directory value...\n");
 		String directory = getParameter(PARAMETER.directory);
 		if (StringUtils.isNotBlank(directory)) {
 			logMessage.append("\tUsing directory: " + directory + "\n");
 		} else {
 			logMessage
 					.append("\tNot found... (not sure, is that ok -- please tell Markus\n)");
 		}
 
 		// concatenated output
 		logMessage.append("Checking for concatenated output value...\n");
 		String concat = getParameter(PARAMETER.concatenated_output);
 		if (StringUtils.isNotBlank(concat)) {
 			logMessage.append("\tUsing output file: " + concat + "\n");
 		} else {
 			logMessage
 					.append("\tNot found... (not sure, is that ok -- please tell Markus\n");
 		}
 
 		if (fixes.length() > 0) {
 			return false;
 		} else {
 			return true;
 		}
 
 	}
 
 	private void setLigand_data_file(String path) {
 
 		for (int i = 0; i < configLines.size(); i++) {
 			if (configLines.get(i).trim()
 					.startsWith(PARAMETER.ligand_data_file.toString())) {
 				String[] parts = configLines.get(i).split(" ");
 				String tmp = PARAMETER.ligand_data_file.toString() + " " + path;
 				configLines.set(i, tmp);
 				break;
 			}
 		}
 
 	}
 
 	public File getConfFile() {
 
 		if (newConfFile == null) {
 			// temp = File.createTempFile(
 			// FilenameUtils.getBaseName(file.getName()), "."
 			// + FilenameUtils.getExtension(file.getName()));
 			// temp = File.createTempFile("test", "suffix");
 			// temp.deleteOnExit();
 			File tmpDir = new File(System.getProperty("java.io.tmpdir"));
 			newConfFile = new File(tmpDir, templateFile.getName());
 			updateConfFile();
 		}
 
 		return newConfFile;
 
 	}
 
 	public String getName() {
 		return this.templateFile.getName();
 	}
 
 	// public void setJobDirectory(String jobDirectoryUrl) {
 	// currentJobDirectory = jobDirectoryUrl;
 	// }
 
 	private String getParameter(PARAMETER key) {
 
 		if (PARAMETER.ligand_data_file.equals(key)) {
 
 			for (int i = 0; i < configLines.size(); i++) {
 				if (configLines.get(i).trim()
 						.startsWith(PARAMETER.ligand_data_file.toString())) {
 					String temp = configLines.get(i)
 							.substring(key.toString().length()).trim();
 					return temp;
 				}
 			}
 
 		} else {
 			for (String line : this.configLines) {
 				if (line.trim().startsWith(key.toString())) {
 					return line.substring(line.indexOf("=") + 1).trim();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public String getLigandValue() {
 		return getParameter(PARAMETER.ligand_data_file);
 	}
 
 	public String[] getLigandPaths() {
 
 		String[] temp = getLigandValue().split("\\s");
 
 		return Arrays.copyOf(temp, temp.length - 1);
 
 	}

 	public void setCostumLigandAmount(int amount) {
 		this.externalLigandAmount = amount;
 	}
 
 	public int getLigandDockingAmount() {

		if (externalLigandAmount >= 0) {
 			return externalLigandAmount;
 		}
 
 		String[] temp = getLigandValue().split("\\s");
 		String temp2 = temp[temp.length - 1];
 
 		try {
 			return Integer.parseInt(temp2);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return -1;
 		}
 	}
 
 	public String getProteinFilePath() {
 		return getParameter(PARAMETER.protein_datafile);
 	}
 
 }
