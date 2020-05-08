 package misc;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import gui.MainFrame;
 import gui.controller.SettingsController;
 
 /**
  * This class provides an instance for reading and writing settings into an external
  * file.
  * 
  * This class uses the Singleton-Pattern to ensure a problem-free read/write access to
  * the external setting file. You may get the instance of this class with:
  * {@link Settings#getInstance()}.
  * 
  * @version 1.0
  */
 public class Settings {
 	/**
 	 * the timeout time of the verifier
 	 */
 	private int timeout;
 	/**
 	 * the maximum amount of memory which can be used by the verifier
 	 */
 	private int memoryLimit;
 	/**
 	 * instance of this class
 	 */
 	private static Settings settings;
 	/**
 	 * indicates whether the settings have been changed
 	 */
 	private boolean settingsChanged;
 	private SettingsController controller;
 	
 	/**
 	 * Constructs a new instance of this class.
 	 */
 	private Settings() {
 		this.settingsChanged = false;
 		loadSettings();
 	}
 	/**
 	 * Returns the instance of this class.
 	 * 
 	 * The instance will be created if it doesn't exist already.
 	 * 
 	 * @return the instance of this class
 	 */
 	public static Settings getInstance() {
 		return settings == null ? (settings = new Settings()) : settings;
 	}
 	/**
 	 * Returns the timeout time for the verifier.
 	 * 
 	 * @return the timeout time for the verifier
 	 */
 	public int getTimeout() {
 		return this.timeout;
 	}
 	/**
 	 * Returns the maximum amount of memory which may be used by the verifier.
 	 * 
 	 * @return the maximum amount of memory for the verifier
 	 */
 	public int getMemoryLimit() {
 		return this.memoryLimit;
 	}
 	/**
 	 * Sets the timeout time to the specified value.
 	 * 
 	 * @param timeout the new timeout time
 	 */
 	public void setTimeout(int timeout) {
 		if(timeout < 0) {
 			throw new IllegalArgumentException("Illegal Argument!"
 					+ "The timout of the verifier has to be a positive integer.");
 		}
 		if(this.timeout != timeout) {
 			this.timeout = timeout;
 			this.settingsChanged = true;
 		}
 	}
 	/**
 	 * Sets the maximum amount of memory which may be used by the verifier to the
 	 * specified value.
 	 * 
 	 * @param memoryLimit the maximum amount of memory usable by the verifier
 	 */
 	public void setMemoryLimit(int memoryLimit) {
 		if(memoryLimit < 0) {
 			throw new IllegalArgumentException("Illegal Argument!"
 					+ "The memory limit of the verifier has to be a positive integer.");
 		}
 		if(this.memoryLimit != memoryLimit) {
 			this.memoryLimit = memoryLimit;
 			this.settingsChanged = true;
 		}
 	}
 	/**
 	 * Returns <code>true</code> when the settings have been changed and <code>false</code>
 	 * otherwise.
 	 * 
 	 * @return <code>true</code> when the settings have been changed, <code>false</code> otherwise
 	 */
 	public boolean settingsChanged() {
 		return this.settingsChanged;
 	}
 	/**
 	 * Saves the settings in an external file. The settings will not be saved if they
 	 * are unchanged (@see {@link Settings#settingsChanged()}).
 	 */
 	public void saveSettings() throws IOException {
		if(USERHOME == null) {
			return;
		}
		String path = USERHOME + System.getProperty("file.separator") + FILENAME;
 		this.settingsChanged = false;
 		String settingString = this.timeout + System.getProperty("line.separator") + this.memoryLimit;
 
    	File file = new File(path);
 		if(!file.exists()) {
 			file.createNewFile();
 		}
 		
		Writer output = new BufferedWriter(new FileWriter(path));
 	    try {
 	    	output.write(settingString);
 	    } finally {
 	    	output.close();
 	    }
 	}
 	
 	public void addControler(SettingsController controller) {
 		this.controller = controller;
 	}
 	
 	/**
 	 * Loads the settings from an external file.
 	 */
 	private void loadSettings() {
		if(USERHOME == null) {
			this.memoryLimit = DEFAULT_MEMORYLIMIT;
			this.timeout = DEFAULT_TIMEOUT;
			return;
		}
    	File file = new File(USERHOME + System.getProperty("file.separator") + FILENAME);
 		if(!file.exists()) {
 			this.memoryLimit = DEFAULT_MEMORYLIMIT;
 			this.timeout = DEFAULT_TIMEOUT;
 			this.settingsChanged = true;
 		} else {
 			StringBuilder contents = new StringBuilder();
 			try {
 				BufferedReader input = new BufferedReader(new FileReader(file));
 				try {
 					String line = null;
 					while((line = input.readLine()) != null) {
 						contents.append(line);
 						contents.append(System.getProperty("line.separator"));
 					}
 				} finally {
 					input.close();
 				}
 			} catch(IOException ioe) {
 				//TODO Fehlermeldung anlegen
 				this.memoryLimit = DEFAULT_MEMORYLIMIT;
 				this.timeout = DEFAULT_TIMEOUT;
 			}
 			String elements[] = contents.toString().split(System.getProperty("line.separator"));
 			if(elements.length != 2) {
 				//TODO Fehlermeldung anlegen & evt dynamischer machen
 			}
 			this.timeout = Integer.parseInt(elements[0]);
 			this.memoryLimit = Integer.parseInt(elements[1]);
 		}
 	}
 	
 	private static final int DEFAULT_TIMEOUT = 60;
 	private static final int DEFAULT_MEMORYLIMIT = 100;
	private static final String FILENAME = ".psesettings";
	private static final String USERHOME = System.getProperty("user.home");
 }
