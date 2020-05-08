 package com.t_oster.notenschrank.data;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.t_oster.notenschrank.gui.SortingDialog;
 
 public class SettingsManager implements Serializable {
 	/**
 	 * 
 	 */
 	private static String SETTINGS_FILE = "settings.dat";
 	private static final long serialVersionUID = 2627505348844681752L;
 	// Static part for Singelton design pattern
 	private static SettingsManager instance;
 
 	public static SettingsManager getInstance() {
 		if (instance == null) {
 			try{
 				loadFromFile(new File(SETTINGS_FILE));
 			}
 			catch(Exception e){
 				System.err.println("Couldn't load settings. Applying default settings");
 				instance = new SettingsManager();
 			}
 		}
 		return instance;
 	}
 
 	public static SettingsManager loadFromFile(File f) throws IOException, ClassNotFoundException {
 		FileInputStream fin = new FileInputStream(f);
 		ObjectInputStream ois = new ObjectInputStream(fin);
 		instance = (SettingsManager) ois.readObject();
 		ois.close();
 		return getInstance();
 	}
 
 	// Attributes for Program Settings
 	private String programVersion = "0.1";
 	private String archivePath = "Archive";
 	private String stackPath = "Stack";
 	//We want to see the last page in preview instead of the first
 	private boolean showLastPage = false;
 	public boolean isShowLastPage() {
 		return showLastPage;
 	}
 
 	public void setShowLastPage(boolean showLastPage) {
 		this.showLastPage = showLastPage;
 	}
 
 	private Map<String, Integer> predefinedNumbers = new LinkedHashMap<String, Integer>();
 	private Set<String> defaultSongs = new LinkedHashSet<String>();
 	
 	public Set<String> getDefaultSongs(){
 		return defaultSongs;
 	}
 	
 	public void setDefaultSongs(Set<String> def){
 		this.defaultSongs=def;
 	}
 	
 	public SortingDialog.Layout sflayout = SortingDialog.Layout.VERTICAL;
 	private SettingsManager() {
 
 	}
 
 	public SortingDialog.Layout getPreferredSortingDialogLayout(){
		if (sflayout==null){
			sflayout=SortingDialog.Layout.VERTICAL;
		}
 		return sflayout;
 	}
 	
 	public File getTempFile(String ending) {
 		File result;
 		int i = 0;
 		do {
 			result = new File(System.getProperty("java.io.tmpdir")+"//tmp" + (i++) + "."+ending);
 		} while (result.exists());
 		return result;
 	}
 
 	public File getTempFile() {
 		return getTempFile("dat");
 	}
 	
 	public String getProgramVersion() {
 		return programVersion;
 	}
 
 	public File getArchivePath() {
 		return new File(archivePath);
 	}
 	
 	public void setArchivePath(String path){
 		this.archivePath = path;
 	}
 
 	public File getStackPath() {
 		return new File(stackPath);
 	}
 	
 	public void setStackPath(String path){
 		this.stackPath = path;
 	}
 
 	/**
 	 * Also using String instead of Voice because of HashMap
 	 * @param predefinedNumbers
 	 */
 	public void setPredefinedNumbers(Map<String, Integer> predefinedNumbers) {
 		this.predefinedNumbers = predefinedNumbers;
 	}
 	
 	/**
 	 * Saves the settings in the standard settings file
 	 * @throws IOException 
 	 */
 	public void save() throws IOException{
 		this.saveToFile(new File(SETTINGS_FILE));
 	}
 
 	public Map<String, Integer> getPredefinedNumbers() {
 		return predefinedNumbers;
 	}
 
 	public void saveToFile(File f) throws IOException {
 		FileOutputStream fout = new FileOutputStream(f);
 		ObjectOutputStream oos = new ObjectOutputStream(fout);
 		oos.writeObject(this);
 		oos.close();
 	}
 
 	
 
 }
