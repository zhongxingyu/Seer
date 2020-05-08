 package Configurator;
 
 import Util.Messages;
 import Util.Strings;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.List;
 
 /*******************************************************************************
  * CLASS: Settings
  *
  * FUNCTIONS: Settings(String title, String[] items, Configurator m)
  *            boolean getToggleableState(int toggleable)
  *            public void setToggleableState(int toggleable, boolean s)
  *            void updateLanguage()
  *
  * REVISIONS: 2013-05-18
  *            Aaron Lee
  *            Luke Tao
  *            John Payment
  * 
  * DATE: 2013-01-18
  *
  * DESIGNER: Team Cirno
  *
  * PROGRAMMER: Team Cirno
  *
  * NOTES: The submenu used by the settings screen and relevant controls.
  *******************************************************************************/
 public class Settings extends List
 {
 	/**
 	 * Identifier of the language
 	 */
 	public static final int ENGLISH = 0;
 
 	/**
 	 * Identifier of the language
 	 */
 	public static final int FRENCH = 1;
 
 	/**
 	 * Identifier of the current language that is selected.
 	 */
 	public static int CURRENT_LANGUAGE = ENGLISH;
 
 	/**
 	 * The index of mandown toggle item; 0
 	 */
 	//public static final int MANDOWN = 0;
 	
 	/**
 	 * The index of fall detection toggle item; 1
 	 */
 	//public static final int FALL = 1;
 	
 	/**
 	 * The index of GPS toggle item; 2
 	 */
 	//public static final int GPS = 0;
 
 	/**
	 * The index of login item; 0
 	 */
	public static final int LOGIN = 0;
 	
 	/**
	 * The index of language item; 1
 	 */
	public static final int LANG = 1;
 	
 	/**
 	 * The MIDlet this screen is a part of.
 	 */
 	private Configurator m;
 	
 	/**
 	 * The togleable item for mandown.
 	 */
 	private Toggleable mandown;
 	
 	/**
 	 * The toggleable item for fall detection.
 	 */
 	private Toggleable fall;
 	
 	/**
 	 * The togleable item for GPS.
 	 */
 	private Toggleable gps;
 
 	/*************************************************************************** 
 	 * FUNCTION: Settings
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REFACTORS: 2013-05-18
 	 *            Aaron Lee
 	 *            Luke Tao
 	 *            John Payment
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: Settings(String title, String[] items, Configurator m)
 	 *            String title - the title of the screen
 	 *            String[] items - array of menu items
 	 *            Configurator m - the Configurator this screen is part of
 	 * 
 	 * NOTES: Constructor for the Settings class
 	 ***************************************************************************/
 	public Settings(String title, String[] items, Configurator m)
 	{
 		super(title, List.IMPLICIT, items, null);
 		this.m = m;
 		/*mandown = new Toggleable(Messages.MANDOWN, false);
 		fall = new Toggleable(Messages.FALL_DETECTION, false);
 		gps = new Toggleable(Messages.GPS, false);*/
 
 		addCommand(new Command("Exit", Command.EXIT, 1));
 		addCommand(new Command("Select", Command.OK, 1));
 
 		setCommandListener(new SettingListener());
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: getToggleableState
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REFACTORS: 2013-05-18
 	 *            Aaron Lee
 	 *            Luke Tao
 	 *            John Payment
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: boolean getToggleableState(int toggleable)
 	 *            int toggleable - the index of the toggleable
 	 * 
 	 * RETURNS: true if valid toggleable is on; otherwise false
 	 * 
 	 * NOTES: Returns the state of the toggleable.
 	 ***************************************************************************/
 	public boolean getToggleableState(int toggleable)
 	{
 		switch (toggleable)
 		{
 			/*case MANDOWN:
 				return mandown.state;
 			case FALL:
 				return fall.state;
 			case GPS:
 				return gps.state;*/
 			default:
 				return false;
 		}
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: getToggleableState
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REFACTORS: 2013-05-18
 	 *            Aaron Lee
 	 *            Luke Tao
 	 *            John Payment
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void setToggleableState(int toggleable, boolean s)
 	 *            int toggleable - the index of the toggleable
 	 *            boolean s - State of the toggleable
 	 * 
 	 * RETURNS: void
 	 * 
 	 * NOTES: Sets the state of Toggleable.
 	 ***************************************************************************/
 	public void setToggleableState(int toggleable, boolean s)
 	{
 		switch (toggleable)
 		{
 			/*case MANDOWN:
 				mandown.setState(s);
 				break;
 			case FALL:
 				fall.setState(s);
 				break;
 			case GPS:
 				gps.setState(s);
 				break;*/
 			default:
 				throw new IllegalArgumentException("use a real index");
 		}
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: updateLanguage
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * REFACTORS: 2013-05-18
 	 *            Aaron Lee
 	 *            Luke Tao
 	 *            John Payment
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void updateLanguage()
 	 * 
 	 * RETURNS: void
 	 * 
 	 * NOTES: Updates the language to current selected language.
 	 ***************************************************************************/
 	public void updateLanguage()
 	{
 		String str[] = Strings.getAllMessages();
 		setTitle(str[Messages.SETTINGS]);
 		/*set(MANDOWN, mandown.toString(), null);
 		set(FALL, fall.toString(), null);
 		set(GPS, gps.toString(), null);*/
 		set(LOGIN, str[Messages.LOGIN], null);
 		set(LANG, str[Messages.LANGUAGES], null);
 	}
 
 	/**
 	 * Command listener.
 	 */
 	private class SettingListener implements CommandListener
 	{
 		public void commandAction(Command c, Displayable d)
 		{
 			List l = (List)d;
 			switch (c.getCommandType())
 			{
 				case Command.SCREEN:
 				case Command.OK:
 					switch (l.getSelectedIndex())
 					{
 						case Settings.LOGIN:
 							Configurator.moveto(m.getLoginScreen());
 							break;
 						case Settings.LANG:
 							Configurator.moveto(m.getLanguageScreen());
 							break;
 						/*case Settings.MANDOWN:
 							mandown.toggle();
 							l.set(MANDOWN, mandown.toString(), null);
 							break;
 						case Settings.FALL:
 							fall.toggle();
 							l.set(FALL, fall.toString(), null);
 							break;
 						case Settings.GPS:
 								gps.toggle();
 								l.set(GPS, gps.toString(), null);
 								break;*/
 						default:
 							break;
 					}
 					break;
 				default:
 					m.destroyApp(false);
 					m.notifyDestroyed();
 					break;
 			}
 		}
 	}
 }
