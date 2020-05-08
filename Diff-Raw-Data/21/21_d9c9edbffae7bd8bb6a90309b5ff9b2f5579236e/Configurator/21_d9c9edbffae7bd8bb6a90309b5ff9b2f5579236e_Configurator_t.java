 package Configurator;
 
 import Util.FileIO;
 import Util.Messages;
 import Util.Path;
 import Util.Strings;
 import java.io.IOException;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.midlet.*;
 
 /*******************************************************************************
  * CLASS: Configurator
  *
  * FUNCTIONS: Configurator()
  *            boolean moveto(Displayable d)
  *            void init()
  *            void startApp()
  *            void pauseApp()
  *            void destroyApp(boolean unconditional)
  *            Settings getSettingScreen()
  *            Languages getLanguageScreen()
  *            Login getLoginScreen()
  *
  * DATE: 2013-01-18
  *
  * REVISIONS: 2013-05-18
  *            Aaron Lee
  *            Steve Lo
  *            Luke Tao
  *            John Payment
  *
  * DESIGNER: Team Cirno
  *
  * PROGRAMMER: Team Cirno
  *
  * NOTES: This is a Java Mobile App intended to allow the user to change the
  *        operation setings of the Safetyline applications used by their phone.
  *******************************************************************************/
 public class Configurator extends MIDlet
 {
 	/**
 	 * Number of lines in the Settings file. (language preference, mandown, fall detection)
 	 */
 	public static final int SETTINGS_LINES = 2;
 
 	/**
 	 * the display. (screen of the phone, for more information, please read J2ME
 	 * documentation)
 	 */
 	private static Display disp;
 
 	/**
 	 * the settings screen.
 	 */
 	private Settings settings;
 
 	/**
 	 * the languages screen.
 	 */
 	private Languages languages;
 
 	/**
 	 * the login screen.
 	 */
 	private Login login;
 
 	/**
 	 * Structure to hold the data of the Settings file
 	 */
 	private SettingStruct settingStruct;
 
 	/*************************************************************************** 
 	 * FUNCTION: Configurator
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REVISIONS: 2013-05-15
 	 *            Luke Tao
 	 *            Aaron Lee
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: Configurator()
 	 * 
 	 * NOTES: Constructor for Configurator Class. It reads from a file to set up
 	 *        the Settings (Language, mandown, fall detection, GPS)
 	 * 
 	 *        If the file doesn't exist, it will load English as preferred 
 	 *        language, mandown as off and fall detection as off.
 	 ***************************************************************************/
 	public Configurator()
 	{
 		settingStruct = new SettingStruct();
 		try
 		{
 			String path = Path.ROOT_PATH + Path.SETTINGS_FILENAME;
 			FileIO f = new FileIO();
 			String[] str = f.readFromAbsolutePath(path);
 			if(str == null || str.length == 0)
 			{
 				f.writeTextFile(Path.SETTINGS_FILENAME, "English\nF\n");
 			} else
 			{
 				if(str != null && str.length == SETTINGS_LINES)
 				{
 					settingStruct.Language = str[0];
 					/*settingStruct.Mandown = str[1].equals("T");
 					settingStruct.Fall = str[2].equals("T");
 					settingStruct.Gps = str[3].equals("T");*/
 				}
 			}
 		} catch(IOException ex)
 		{
 			notifyDestroyed();
 		}
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: moveto
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: boolean moveto(Displayable d)
 	 *            Displayable d - the screen you want to change to
 	 * 
 	 * RETURN: true if successfully changed screens; otherwise false
 	 * 
 	 * NOTES: Changes the displayed screen
 	 ***************************************************************************/
 	public static boolean moveto(Displayable d)
 	{
 		if (d != null)
 		{
 			disp.setCurrent(d);
 			return true;
 		}
 		
 		return false;
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: init
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REVISIONS: 2013-05-15
 	 *            Luke Tao
 	 *            Aaron Lee
 	 *            John Payment
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: boolean init()
 	 * 
 	 * RETURN: void
 	 * 
 	 * NOTES: Initializes the display and screens.
 	 ***************************************************************************/
 	private void init()
 	{
 		disp = Display.getDisplay(this);
 		String[] str = Strings.getAllMessages();
 		String[] languageItems = new String[]{ str[Messages.ENGLISH], str[Messages.FRENCH] };
 
 		String[] settingsItemgs = new String[]
 		{
 			/*str[Messages.TOGGLE] + " " + str[Messages.MANDOWN] + ": " + str[(settingStruct.Mandown) ?
 				Messages.ON :
 				Messages.OFF],
 			str[Messages.TOGGLE] + " " + str[Messages.FALL_DETECTION] + ": " + str[(settingStruct.Fall ?
 				Messages.ON :
 				Messages.OFF)],
 			str[Messages.TOGGLE] + " " + str[Messages.GPS] + ": " + str[(settingStruct.Gps ?
 				Messages.ON :
 				Messages.OFF)],*/
                                
                    str[Messages.LOGIN],
                    str[Messages.LANGUAGES]
 		};
 
 		settings = new Settings(str[Messages.SETTINGS], settingsItemgs, this);
 		/*settings.setToggleableState(Settings.FALL, settingStruct.Fall);
 		settings.setToggleableState(Settings.MANDOWN, settingStruct.Mandown);
 		settings.setToggleableState(Settings.GPS, settingStruct.Gps);*/
 
 		login = new Login(str[Messages.LOGIN], this);
 
 		languages = new Languages(str[Messages.LANGUAGES], languageItems, this);
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: startApp
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void startApp()
 	 * 
 	 * NOTES: Called when the app is launched.
 	 ***************************************************************************/
 	public void startApp()
 	{
 		init();
 		disp.setCurrent(settings);
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: pauseApp
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void pauseApp()
 	 * 
 	 * NOTES: Default function inherited by the parent class.
 	 ***************************************************************************/
 	public void pauseApp()
 	{
 		notifyDestroyed();
 	}
 
 	/**
 	 * Structure that holds all the data related to Settings.
 	 */
 	private class SettingStruct
 	{
 		public String Language;
 		public boolean Mandown;
 		public boolean Fall;
 		public boolean Gps;
 		public SettingStruct()
 		{
 			Language = "English";
 		}
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: destroyApp
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REVISIONS: 2013-05-18
 	 *            Aaron Lee
 	 *            Luke Tao
 	 *            John Payment
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void destroyApp(boolean unconditional)
 	 *            boolean unconditional - if the destruction must happen or not
 	 * 
 	 * NOTES: 
 	 ***************************************************************************/
 	public void destroyApp(boolean unconditional)
 	{
 		try
 		{
 			String path = Path.ROOT_PATH + Path.SETTINGS_FILENAME;
 			StringBuffer buff = new StringBuffer();
 			FileIO f = new FileIO();
 			String[] lines = f.readFromAbsolutePath(path);
 			
 			buff.append(lines[0])
 				.append("\n")
 				/*.append((settings.getToggleableState(Settings.MANDOWN) ? "T" : "F"))
 				.append("\n")
 				.append((settings.getToggleableState(Settings.FALL) ? "T" : "F"))
 				.append("\n")
 				.append((settings.getToggleableState(Settings.GPS) ? "T" : "F"))*/
 				.append("\n");
 			
 			f.writeTextFile(Path.SETTINGS_FILENAME, buff.toString());
 			notifyDestroyed();
 		} catch(IOException ex)
 		{
 			notifyDestroyed();
 		}
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: getSettingScreen
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: Settings getSettingScreen()
 	 * 
 	 * RETURN: the settings object of the screen
 	 * 
 	 * NOTES: Getting function for the screen's settings
 	 ***************************************************************************/
 	public Settings getSettingScreen()
 	{
 		return settings;
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: getLanguageScreen
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: Languages getLanguageScreen()
 	 * 
 	 * RETURN: the language object of the screen
 	 * 
 	 * NOTES: Getting function for the screen's language
 	 ***************************************************************************/
 	public Languages getLanguageScreen()
 	{
 		return languages;
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: getLoginScreen
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: Login getLoginScreen()
 	 * 
 	 * RETURN: the Login object of the screen
 	 * 
 	 * NOTES: Getting function for the login screen
 	 ***************************************************************************/
 	/**
 	 * Login screen getter.
 	 *
 	 * @return login screen.
 	 */
 	public Login getLoginScreen()
 	{
 		return login;
 	}
 }
