 package com.mcvs.model;
 
 import java.util.*;
 import java.util.prefs.*;
 import java.io.*;
 
 import com.mcvs.core.*;
 import com.mcvs.core.platformManager.*;
 
 public class MCVSModel {
 	private static MCVSModel INSTANCE = null;
 	private FileManager fileManager;			//TODO Decide if fileManager should be static class
 	private PlatformManager platformManager;
 	private String currentVersion = null;
 	private Preferences prefs;
 	private FileLockManager locker = null;
 	
 	private MCVSModel() {
 		platformManager = PlatformManager.getInstance();
 		fileManager = FileManager.getInstance(platformManager.getHomeDirectory());
 		prefs = Preferences.userNodeForPackage(this.getClass());
 	}
 	
 	public static MCVSModel getInstance() {
 		if(INSTANCE==null) {
 			INSTANCE = new MCVSModel();
 		}
 		
 		return INSTANCE;
 	}
 	
	public boolean checkFirstRunDirectories() {
		if(prefs.get("FIRST_TIME_RUN_DIR", "").equals("")) {
			prefs.put("FIRST_TIME_RUN_DIR", "false");
			return true;
		}
		return false;
	}
	
 	public boolean checkFirstRun() {
 		if(prefs.get("FIRST_TIME_RUN", "").equals("")) {
 			prefs.put("FIRST_TIME_RUN", "false");
 			return true;
 		}
 		return false;
 	}
 	
 	/*
 	 * TODO Remove try statements from Model, replace with throws and handle
 	 * the exception in the Controller.
 	 */
 	public void launchMinecraft() {
 		try {
 			Runtime.getRuntime().exec(platformManager.getMinecraftRunDirectory());
 		} 
 		catch (IOException ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace();
 		}
 	}
 	
 	public void closeMCVS() {
 		System.exit(0);
 	}
 	
 	/*
 	 * Method used to return a list of entities read from each entity file in the
 	 * versions directory.
 	 * 
 	 * TODO Remove try statements from Model, replace with throws and handle
 	 * the exception in the Controller.
 	 */
 	public Vector<Entity> getEntities() {
 		Vector<Entity> entities = new Vector<Entity>();
 		File[] files = new File(platformManager.getVersionsDirectory()).listFiles();
 		
 		try {
 			for(int i=0; i<files.length; i++) {
 				if(files[i].isDirectory()) {
 					String[] temp;
 					temp = FileManager.readLinesFromFile(new File(files[i]+"/entity"));
 					entities.add(new Entity(temp[0], temp[1], files[i].getPath()));
 				}
 			}
 		}
 		catch (IOException ex) {
 			//TODO Add handler and error reporter
 			ex.printStackTrace();
 		}
 		
 		return entities;
 	}
 	
 	/*
 	 * Method used to get the current Minecraft version. If the read boolean is
 	 * true then the method will read the version saved in the currentVer file and
 	 * return the value; else just return the value in currentVersion
 	 * 
 	 * TODO Remove try statements from Model, replace with throws and handle
 	 * the exception in the Controller.
 	 */
 	public String getCurrentVersion(boolean read) {
 		if(read) {
 			String[] temp;
 			try {
 				temp = FileManager.readLinesFromFile(new File(platformManager.getAppDirectory()+"currentVer"));
 				currentVersion = temp[0];
 			} 
 			catch (IOException ex) {
 				// TODO Auto-generated catch block
 				ex.printStackTrace();
 			}
 		}
 		
 		return currentVersion;
 	}
 	
 	/*
 	 * Updates the currentVer text file with the version value
 	 * passed in by ver
 	 * 
 	 * TODO Remove try statements from Model, replace with throws and handle
 	 * the exception in the Controller.
 	 */
 	public void updateCurrentVersion(String ver) {
 		try {
 			FileManager.writeToFile(new File(platformManager.getAppDirectory()+"currentVer"), ver, platformManager.getUnhideFileCommand());
 			currentVersion = ver;
 		}
 		catch (IOException ex) {
 			//TODO Auto-generated catch block
 			ex.printStackTrace();
 		} catch (InterruptedException ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace();
 		}
 	}
 	
 	/*
 	 * Gets every minecraft version released
 	 * Returns null if error occurred, otherwise returns a String array.
 	 * 
 	 * TODO Remove try statements from Model, replace with throws and handle
 	 * the exception in the Controller.
 	 */
 	public String[] getAllMCVersions() {
 		/*String[] temp = null;
 		try {
 			temp = FileManager.readLinesFromFile(new File(platformManager.getAppDirectory()+"comboBoxVers"));
 		} 
 		catch (IOException ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace();
 		}*/
 		
 		return MinecraftVersions.getComboBoxVersions();
 	}
 	
 	/*
 	 * Reads the Minecraft Version Control version number from the 
 	 * MCVSver text file
 	 * 
 	 * TODO Remove try statements from Model, replace with throws and handle
 	 * the exception in the Controller.
 	 */
 	public String getMCVSVersion() {
 		String[] temp = null;
 		try {
 			temp = FileManager.readLinesFromFile(new File(platformManager.getAppDirectory()+"MCVSver"));
 			
 		} 
 		catch (IOException ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace();
 		}
 		
 		return temp[0];
 	}
 	
 	/*
 	 * Moves the file in the directory built by version and filename to the active
 	 * Minecraft directory.
 	 * 
 	 * TODO Implement more efficient method of storing files
 	 */
 	public void moveVersion(String version, String filename) throws IOException {
 		FileManager.moveFile(new File(platformManager.getVersionsDirectory()+version+"/"+filename),
 				new File(platformManager.getMinecraftDirectory()+"minecraft.jar"));
 	}
 	
 	public void addVersion(Entity source, Entity dest) throws IOException, InterruptedException {
 		if(!dest.getName().toUpperCase().endsWith(".JAR")) {
 			dest.setName(dest.getName()+".jar");
 		}
 		
 		FileManager.moveFile(new File(source.getDirectory()+source.getName()),
 				new File(platformManager.getVersionsDirectory()+dest.getVersion()+"/"+dest.getName()));
 		
 		FileManager.createFile(new File(platformManager.getVersionsDirectory()+dest.getVersion()+"/entity"), platformManager.getHideFileCommand());
 		FileManager.writeToFile(new File(platformManager.getVersionsDirectory()+dest.getVersion()+"/entity"), 
 				dest.getName()+"\n"+dest.getVersion().trim(), platformManager.getHideFileCommand());
 	}
 	
 	public boolean makeDirectory(String path) {
 		boolean created = new File(platformManager.getVersionsDirectory()+path).mkdir();
 		
 		return created;
 	}
 	
 	/*
 	 * Deletes a version file in the data directory. 
 	 * Returns true if the file was deleted, false if something went wrong.
 	 */
 	public boolean deleteVersionFile(String version) {
 		boolean deleted = FileManager.deleteDirectory(new File(platformManager.getVersionsDirectory()+version), true);
 		
 		return deleted;
 	}
 	
 	public boolean deleteJarFile(String filename) {
 		boolean deleted = FileManager.deleteFile(new File(platformManager.getVersionsDirectory()+filename));
 		return deleted;
 	}
 	
 	public boolean makeFile(String filename) throws IOException, InterruptedException {
 		boolean created = new File(platformManager.getVersionsDirectory()+filename).createNewFile();
 		return created;
 	}
 	
 	/*
 	 * Writes the name and version to a entity file
 	 */
 	public void writeEntityFile(String name, String ver) throws IOException, InterruptedException {
 		FileManager.writeToFile(new File(platformManager.getVersionsDirectory()+ver+"/entity"), name+"\n"+ver, platformManager.getHideFileCommand());
 	}
 	
 	public void renameFile(String oldName, String newName) throws IOException {
 		
 		FileManager.moveFile(new File(platformManager.getVersionsDirectory()+oldName),
 				new File(platformManager.getVersionsDirectory()+newName));
 		
 		boolean delete = this.deleteJarFile(oldName);
 	}
 	
 	public void createWorkingDirectory() throws IOException, InterruptedException {
 		boolean a = FileManager.createDirectory(new File(platformManager.getAppDirectory()), "");
 		FileManager.createDirectory(new File(platformManager.getDataDirectory()), "");
 		FileManager.createDirectory(new File(platformManager.getVersionsDirectory()), "");
 		FileManager.createFile(new File(platformManager.getAppDirectory()+"MCVSver"), "");
 		FileManager.createFile(new File(platformManager.getAppDirectory()+"currentVer"), "");
 		FileManager.createFile(new File(platformManager.getAppDirectory()+"comboBoxVers"), "");
 	}
 	
 	public void lockFile(String file) {
 		locker = new FileLockManager(new File(platformManager.getAppDirectory()+"/"+file));
 	}
 	
 	public PlatformManager getPlatformManger() {
 		return platformManager;
 	}
 }
