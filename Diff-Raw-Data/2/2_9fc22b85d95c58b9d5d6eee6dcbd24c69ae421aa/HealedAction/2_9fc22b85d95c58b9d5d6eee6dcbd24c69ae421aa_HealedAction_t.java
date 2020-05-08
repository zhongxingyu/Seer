 package com.attask.jenkins.healingmatrixproject;
 
 import hudson.console.AnnotatedLargeText;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixRun;
 import hudson.model.Action;
 import org.apache.commons.io.FileUtils;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.export.Exported;
 
 import java.io.*;
 import java.net.URLEncoder;
 import java.nio.charset.Charset;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Logger;
 
 /**
  * User: Joel Johnson
  * Date: 3/12/13
  * Time: 3:56 PM
  */
 public class HealedAction implements Action {
 	private static final Logger LOGGER = Logger.getLogger("healing-matrix-project");
 
 	private Map<String, List<File>> oldLogFiles;
 	private final Charset charSet;
 
 	public HealedAction(Charset charSet) {
 		oldLogFiles = new ConcurrentHashMap<String, List<File>>();
 		this.charSet = charSet == null ? Charset.defaultCharset() : charSet;
 	}
 
 	@Exported
 	public Map<String, List<File>> getOldLogFiles() {
 		return oldLogFiles;
 	}
 
 	public Charset getCharSet() {
 		return charSet == null ? Charset.defaultCharset() : charSet;
 	}
 
 	/**
 	 * Saves the old log file.
 	 */
 	public void addAutoHealedJob(MatrixRun run) throws IOException {
 		MatrixBuild parent = run.getParentBuild();
 		File rootDir = parent.getRootDir();
 		File oldLogDir = new File(rootDir, "autoHealedLogs");
 		if(!oldLogDir.exists() && !oldLogDir.mkdirs()) {
 			LOGGER.severe("Couldn't create directory: " + oldLogDir.getAbsolutePath());
 			return;
 		}
 
 		File fileToSave = run.getLogFile();
 
 		String runName = run.toString();
 
 		String newFileName = runName.replaceAll("[^A-Za-z0-9_-]", "_");
 		File newFile = new File(oldLogDir, newFileName);
 
 		//Lazy way to make sure we don't have duplicates.
 		int uniquifier = 0;
 		while(newFile.exists()) {
 			newFile = new File(oldLogDir, newFileName + (++uniquifier));
 		}
 
 		FileUtils.copyFile(fileToSave, newFile);
 
 		List<File> logFileList = oldLogFiles.get(runName);
 		if(logFileList == null) {
 			logFileList = new CopyOnWriteArrayList<File>();
 			oldLogFiles.put(runName, logFileList);
 		}
		logFileList.add(newFile);
 	}
 
 	public void doLog(StaplerRequest request, StaplerResponse response,
 					  @QueryParameter(value = "name", required = true) String name,
 					  @QueryParameter(value = "index", required = true) int index
 	) throws IOException {
 		List<File> files = getOldLogFiles().get(name);
 		File file = files.get(index);
 
 		AnnotatedLargeText<HealedAction> annotatedLargeText = new AnnotatedLargeText<HealedAction>(file, getCharSet(), true, this);
 		annotatedLargeText.writeLogTo(0, response.getOutputStream());
 	}
 
 	public String urlEncode(String toEncode) throws UnsupportedEncodingException {
 		return URLEncoder.encode(toEncode, "UTF-8");
 	}
 
 	public String getIconFileName() {
 		return "/plugin/healing-matrix-project/cross.png";
 	}
 
 	public String getDisplayName() {
 		return "Auto-Healed";
 	}
 
 	public String getUrlName() {
 		return "autoHealed";
 	}
 }
