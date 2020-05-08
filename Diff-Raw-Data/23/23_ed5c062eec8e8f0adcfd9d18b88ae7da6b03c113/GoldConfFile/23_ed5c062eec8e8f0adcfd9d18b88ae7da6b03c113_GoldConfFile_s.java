 package org.bestgrid.virtscreen.model;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.vpac.grisu.X;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.control.clientexceptions.FileTransactionException;
 import org.vpac.grisu.frontend.control.login.LoginManager;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.GrisuRegistryManager;
 
 public class GoldConfFile {
 
 	public static void main(String[] args) throws Exception {
 
 		ServiceInterface si = LoginManager.loginCommandline("Local");
 
 		GoldConfFile conf = new GoldConfFile(si);
 
 		conf.setConfFile("/home/markus/Desktop/jack/exampleJob2/p110a_lead4x.conf");
 
 		for (String line : conf.getNewConfig()) {
 			X.p(line);
 		}
 
 		X.p("");
 		X.p("");
 
 		X.p("Parse messages: ");
 		X.p(conf.getParseMessages());
 
 	}
 
 	// gold specific variables
 	private LigandDataFile ligandFile = null;
 	private ProteinDataFile proteinDataFile = null;
 
 	private Directory directory = null;
 
 	private ConcatenatedOutput concatenatedOutput = null;
 
 	private ScoreParamFile scoreParamFile = null;
 
 	private final Set<AbstractGoldParameter> customparameters = new HashSet<AbstractGoldParameter>();
 
 	// internal variables
 	private final ServiceInterface si;
 
 	private final FileManager fm;
 	private String url;
 	private String parentUrl;
 
 	private File templateFile;
 
 	private List<String> configLines;
	private final List<AbstractGoldParameter> parameters = new LinkedList<AbstractGoldParameter>();
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 	public GoldConfFile(ServiceInterface si) {
 
 		this.si = si;
 
 		this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 
 	}
 
 	public void addListener(PropertyChangeListener l) {
 		pcs.addPropertyChangeListener(l);
 	}
 
 	public String getConcatenatedOutput() {
 		return getConcatenatedOutputObject().getValue();
 	}
 
 	private ConcatenatedOutput getConcatenatedOutputObject() {
 		return concatenatedOutput;
 	}
 
 	public String getConfFile() {
 		return this.url;
 	}
 
 	public String getConfFileDir() {
 		return parentUrl;
 	}
 
 	public String getDirectory() {
 		return getDirectoryObject().getValue();
 	}
 
 	private Directory getDirectoryObject() {
 		return directory;
 	}
 
 	public Set<String> getFilesToStageIn() {
 
 		Set<String> result = new HashSet<String>();
 
 		for (AbstractGoldParameter p : parameters) {
 			result.addAll(p.getFilesToStageIn());
 
 		}
 
 		return result;
 	}
 
 	public String getFixes() {
 
 		StringBuffer result = new StringBuffer();
 
 		for (AbstractGoldParameter p : parameters) {
 			String msg = p.getFixes();
 			if (StringUtils.isNotBlank(msg)) {
 				result.append("Parameter: " + p.getParameterName() + "\n");
 				result.append(msg);
 			}
 		}
 
 		return result.toString();
 	}
 
 	public File getJobConfFile() {
 
 		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
 		File newConfFile = new File(tmpDir, templateFile.getName());
 
 		newConfFile.delete();
 		newConfFile.deleteOnExit();
 
 		try {
 			FileUtils.writeLines(newConfFile, getNewConfig());
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		return newConfFile;
 	}
 
 	public String[] getLigandDataFiles() {
 		return ligandFile.getLigandFiles();
 	}
 
 	public int getLigandDockingAmount() {
 		return ligandFile.getLigandDockingAmount();
 	}
 
 	private LigandDataFile getLigandFileObject() {
 		return ligandFile;
 	}
 
 	public String getName() {
 		return this.templateFile.getName();
 	}
 
 	public List<String> getNewConfig() {
 		List<String> result = new LinkedList<String>();
 
 		for (AbstractGoldParameter p : parameters) {
 			result.add(p.getConfigLine());
 		}
 
 		return result;
 	}
 
 	public String getParseMessages() {
 
 		StringBuffer result = new StringBuffer();
 
 		for (AbstractGoldParameter p : parameters) {
 			String msg = p.getMessage();
 			if (StringUtils.isNotBlank(msg)) {
 				result.append("Parameter: " + p.getParameterName() + "\n");
 				result.append(msg);
 			}
 		}
 
 		return result.toString();
 	}
 
 	public String getProteinDataFile() {
 		return proteinDataFile.getValue();
 	}
 
 	private ProteinDataFile getProteinDataFileObject() {
 		return proteinDataFile;
 	}
 
 	public String getScoreParamFile() {
 		return scoreParamFile.getValue();
 	}
 
 	private ScoreParamFile getScoreParamFileObject() {
 		return scoreParamFile;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public boolean isValid() {
 
 		for (AbstractGoldParameter p : parameters) {
 			if (!p.isValid()) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public void removeListener(PropertyChangeListener l) {
 		pcs.removePropertyChangeListener(l);
 	}
 
 	public void setConcatenatedOutput(String conc) {
 		getConcatenatedOutputObject().setNewValue(conc);
 		pcs.firePropertyChange("concatenatedOutput", null, conc);
 
 	}
 
 	public void setConfFile(String url) throws FileTransactionException {
 		this.url = url;
 		this.parentUrl = FileManager.calculateParentUrl(this.url);
 		this.templateFile = this.fm.downloadFile(this.url);
 
 		try {
 			this.configLines = FileUtils.readLines(this.templateFile);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		// gold specific variables
 		ligandFile = new LigandDataFile();
 		proteinDataFile = new ProteinDataFile();
 		directory = new Directory();
 		concatenatedOutput = new ConcatenatedOutput();
 		scoreParamFile = new ScoreParamFile();
 
 		parameters.clear();
 		customparameters.clear();
 		customparameters.add(ligandFile);
 		customparameters.add(proteinDataFile);
 		customparameters.add(directory);
 		customparameters.add(concatenatedOutput);
 		customparameters.add(scoreParamFile);
 
 		for (String line : configLines) {
 
 			if (StringUtils.isBlank(line)) {
 				continue;
 			}
 
 			boolean custom = false;
 			Iterator<AbstractGoldParameter> i = customparameters.iterator();
 
 			while (i.hasNext()) {
 				AbstractGoldParameter temp = i.next();
 				if (temp.isResponsibleForLine(line)) {
 					temp.setServiceInterface(si);
 					temp.init(line, parentUrl);
 					parameters.add(temp);
 					custom = true;
 					i.remove();
 					break;
 				}
 			}
 
 			if (!custom) {
 				parameters.add(new GenericGoldParameter(si, line, parentUrl));
 			}
 
 		}
 
 		pcs.firePropertyChange("confFile", null, url);
 	}
 
 	public void setDirectory(String dir) {
 		getDirectoryObject().setNewConfigValue(dir);
 		pcs.firePropertyChange("directory", null, dir);
 
 	}
 
 	public void setLigandDataFiles(String[] files) {
 		getLigandFileObject().setLigandFiles(files);
 		pcs.firePropertyChange("ligandDataFiles", null, files);
 	}
 
 	public void setLigandDockingAmount(int amount) {
 		getLigandFileObject().setLigandDockingAmount(amount);
 		pcs.firePropertyChange("ligandDockingAmount", null, new Integer(amount));
 
 	}
 
 	public void setProteinDataFile(String file) {
 		getProteinDataFileObject().setNewValue(file);
 		pcs.firePropertyChange("proteinDataFile", null, file);
 	}
 
 	public void setScoreParamFile(String file) {
 		getScoreParamFileObject().setNewValue(file);
 		pcs.firePropertyChange("scoreParamFile", null, file);
 	}
 }
