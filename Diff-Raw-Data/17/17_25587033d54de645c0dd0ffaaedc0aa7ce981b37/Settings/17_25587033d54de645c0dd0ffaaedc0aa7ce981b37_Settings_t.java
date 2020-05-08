 /*
  * @author Kyle Kemp
  */
 package connection;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import lombok.Getter;
 import lombok.Setter;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.RGB;
 
 import shared.Message;
 import shared.NSAlertBox;
 import shared.SWTResourceManager;
 import ui.room.Room;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 /**
  * The Class Settings.
  */
 public class Settings {
 	
 	//stuff to read and write files with
 	/** The filename. */
 	static String filename = "settings.xml";
 	
 	/** The xstream. */
 	static XStream xstream = new XStream(new Sun14ReflectionProvider( ), new DomDriver( ));
 	
 	//the sole Settings instance
 	/**
 	 * Gets the settings.
 	 *
 	 * @return the settings
 	 */
 	public static Settings getSettings() { return settings; }
 	
 	/** The settings. */
 	static Settings settings = Settings.readFromFile();
 	
 	//This keeps the list of servers the user has saved.
 	/** The conn settings. */
 	
 	/**
 	 * Gets the conn settings.
 	 *
 	 * @return the conn settings
 	 */
 	@Getter 
  /**
   * Sets the conn settings.
   *
   * @param connSettings the new conn settings
   */
  @Setter ArrayList<ConnectionSettings> connSettings;
 	
 	/**
 	 * Checks if is chat logs.
 	 *
 	 * @return true, if is chat logs
 	 */
 	@Getter /**
   * Sets the chat logs.
   *
   * @param chatLogs the new chat logs
   */
  @Setter boolean chatLogs;
 	
 	/**
 	 * Checks if is minimize tray.
 	 *
 	 * @return true, if is minimize tray
 	 */
 	@Getter 
  /**
   * Sets the minimize tray.
   *
   * @param minimizeTray the new minimize tray
   */
  @Setter boolean minimizeTray;
 	
 	//Other variables/objects to hold settings/preferences go here 
 	/** The nicks ignored. */
 	
 	/**
 	 * Gets the nicks ignored.
 	 *
 	 * @return the nicks ignored
 	 */
 	@Getter 
  /**
   * Sets the nicks ignored.
   *
   * @param nicksIgnored the new nicks ignored
   */
  @Setter ArrayList<String> nicksIgnored;
 	
 	/**
 	 * Gets the output colors.
 	 *
 	 * @return the output colors
 	 */
 	@Getter 
  /**
   * Sets the output colors.
   *
   * @param outputColors the output colors
   */
  @Setter HashMap<Short,String> outputColors;
 	
 	/**
 	 * Gets the room status colors.
 	 *
 	 * @return the room status colors
 	 */
 	@Getter 
  /**
   * Sets the room status colors.
   *
   * @param roomStatusColors the room status colors
   */
  @Setter HashMap<Integer,RGB> roomStatusColors;
 	
 	/**
 	 * Gets the quicklinks.
 	 *
 	 * @return the quicklinks
 	 */
 	@Getter 
  /**
   * Sets the quicklinks.
   *
   * @param quicklinks the quicklinks
   */
  @Setter HashMap<String,String> quicklinks;
 	
 	
 	//Other static variables
 	/** The Constant BACKGROUND. */
 	public static final short BACKGROUND = -1;
 	
 	//TextBoxComposite settings
 	/** The timestamps enabled. */
 	
 	/**
 	 * Checks if is timestamps enabled.
 	 *
 	 * @return true, if is timestamps enabled
 	 */
 	@Getter 
  /**
   * Sets the timestamps enabled.
   *
   * @param timestampsEnabled the new timestamps enabled
   */
  @Setter boolean timestampsEnabled;
 	
 	/**
 	 * Gets the timestamp format pattern.
 	 *
 	 * @return the timestamp format pattern
 	 */
 	@Getter 
  /**
   * Sets the timestamp format pattern.
   *
   * @param timestampFormatPattern the new timestamp format pattern
   */
  @Setter String timestampFormatPattern;
 	
 	
 	//creates default settings
 	/**
 	 * Instantiates a new settings.
 	 */
 	public Settings()
 	{
 		connSettings = new ArrayList<ConnectionSettings>();
 		nicksIgnored = new ArrayList<String>();
 		
 		
 		outputColors = new HashMap<Short,String>();
 		//These correspond to the types of Message
 		outputColors.put(Message.CONSOLE, "dark gray");
 		outputColors.put(Message.MSG, "black");
 		outputColors.put(Message.NOTICE, "red");
 		outputColors.put(Message.ACTION, "purple");
 		outputColors.put(Message.PM, "black");
 		outputColors.put(Settings.BACKGROUND, "white");
 		
 		roomStatusColors = new HashMap<Integer,RGB>();
 		//These correspond to the statuses of Room
 		roomStatusColors.put(Room.NORMAL, SWTResourceManager.getColor(SWT.COLOR_BLACK).getRGB());
 		roomStatusColors.put(Room.NEW_IRC_EVENT, SWTResourceManager.getColor(SWT.COLOR_MAGENTA).getRGB());
 		roomStatusColors.put(Room.NEW_MESSAGE, SWTResourceManager.getColor(SWT.COLOR_RED).getRGB());
 		roomStatusColors.put(Room.NAME_CALLED, SWTResourceManager.getColor(SWT.COLOR_GREEN).getRGB());
 		
 		timestampFormatPattern = "[hh:mm:ss]";
 		timestampsEnabled = true;
 		chatLogs=false;
 		minimizeTray=false;
 		
 		quicklinks = new HashMap<>();
 		quicklinks.put("google","http://google.com/?q=%INPUT%");
		
		//TODO add a default connection to our channel
 	}
 	
 	/**
 	 * Write to file.
 	 */
 	public static void writeToFile()
 	{
 		try {
 			xstream.toXML(settings,new FileOutputStream(filename));
 		} catch (FileNotFoundException e) {
 			org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.settings");
 			fLog.error("Unable to save settings.", e);
 		}
 		
 	}
 	
 	//if a settings file exists, returns that
 	//else returns default settings
 	/**
 	 * Read from file.
 	 *
 	 * @return the settings
 	 */
 	public static Settings readFromFile() 
 	{
 		File f = new File(filename);
 		if(f.exists())
 		{
 			try {
 				return (Settings)xstream.fromXML(new FileInputStream(filename));
 			} catch (Exception e) {
 				org.apache.log4j.Logger fLog = org.apache.log4j.Logger.getLogger("log.settings");
 				fLog.error("Unable to load settings, resetting to default.", e);
 				NSAlertBox alert = new NSAlertBox("Unable to load settings","The file settings.xml was corrupted or was not formatted correctly. Settings have reverted to their defaults.",SWT.OK,SWT.ICON_WARNING);
 				alert.go();
 				Settings defaultSettings = new Settings();
 				settings = defaultSettings;
 				writeToFile();
 				return defaultSettings;
 			}
 		}
 		return new Settings();
 	}
 
 }
