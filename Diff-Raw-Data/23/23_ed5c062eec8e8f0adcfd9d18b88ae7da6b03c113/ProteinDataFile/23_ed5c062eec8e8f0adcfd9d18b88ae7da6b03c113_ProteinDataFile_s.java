 package org.bestgrid.virtscreen.model;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.model.FileManager;
 
 public class ProteinDataFile extends AbstractGoldParameter {
 
 	private String newLine;
 	private boolean valid = false;
 
 	@Override
 	protected boolean configLineIsValid() {
 		return valid;
 	}
 
 	@Override
 	protected String getNewConfigLine() {
 		return newLine;
 	}
 
 	@Override
 	public String getParameterName() {
 		return "protein_datafile";
 	}
 
 	@Override
 	public void initParameter() {
 		setNewValue(getValue(getOriginalLine()));
 	}
 
 	@Override
 	boolean isOptional() {
 		return false;
 	}
 
 	@Override
 	boolean isResponsibleForLine(String line) {
 		return hasKey(line, getParameterName());
 	}
 
 	@Override
 	protected void setNewConfigValue(String value) {
 		if (StringUtils.isBlank(value)) {
 			throw new IllegalArgumentException(
 					"New value for concatenated_output is blank");
 		}
 
 		setProteinDataFile(value);
 
 	}
 
 	public void setProteinDataFile(String file) {
 
 		removeMessage("parse");
 
 		StringBuffer msg = new StringBuffer();
 		StringBuffer fix = new StringBuffer();
 
 		boolean parsingSuccessful = true;
 		boolean isLocalFile = false;
 		getFilesToStageIn().clear();
 
 		if (FileManager.isLocal(file)) {
 			try {
 				final File localFile = new File(new URI(
 						FileManager.ensureUriFormat(file)));
 
 				if (localFile.exists() && localFile.isFile()) {
 					msg.append("     -> File "
 							+ file
 							+ " exists on local filesystem. File will be uploaded when job is created.\n");
 					addFileToStageIn(file);
 					isLocalFile = true;
 				} else {
 					if (localFile.getName().equals(file)) {
 						// means it can also be remote file in remote working
 						// dir...
 						if (StringUtils.isNotBlank(getWorkingDir())) {
 							file = getWorkingDir() + "/" + file;
 							isLocalFile = false;
 						} else {
 							isLocalFile = true;
 						}
 					} else {
 						isLocalFile = true;
 					}
 
 					if (isLocalFile) {
 
 						msg.append("     -> File "
 								+ file
 								+ " does not exist on local filesystem or is directory.\n");
 						fix.append("Please check or change protein data file path: "
 								+ file);
 						parsingSuccessful = false;
 					}
 				}
 
 			} catch (URISyntaxException e) {
 				msg.append("     -> Could not parse url for file: " + file
 						+ "\n");
 				parsingSuccessful = false;
 				fix.append("   Please check url: " + file);
 				isLocalFile = true;
 
 			}
 		}
 		if (!isLocalFile) {
 			if (getFileManager().isFile(file)) {
 				msg.append("     -> File "
 						+ file
 						+ " exists on remote filesystem. File will be copied to job directory when job is created.\n");
 				addFileToStageIn(file);
 			} else {
 				msg.append("     -> File " + file
 						+ " either does not exists or could not be accessed.\n");
 				parsingSuccessful = false;
 				fix.append("Please change path of make sure file exists: "
 						+ file);
 			}
 
 		}
 
 		if (!parsingSuccessful) {
 			valid = false;
 			// msg.append("\nProtein data file is not valid or inaccessable.\n");
 			getFilesToStageIn().clear();
 		} else {
 			// msg.append("\nProtein data file is valid and accessable.\n");
 			valid = true;
 		}
 
 		addMessage("parse", msg.toString());
 		addFix("parse", fix.toString());
 
 		newLine = replaceValue(getOriginalLine(), FilenameUtils.getName(file));
 	}
 
 }
