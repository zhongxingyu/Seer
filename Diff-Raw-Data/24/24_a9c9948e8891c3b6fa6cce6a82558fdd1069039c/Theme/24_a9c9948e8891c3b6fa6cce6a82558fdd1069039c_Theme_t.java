 package edu.bu.cs673.AwesomeAlphabet.model;
 
 import java.util.Observable;
 
 public class Theme extends Observable {
 
 	public static String DEFAULT_THEME_NAME = "Default Theme";
	public static String ALL_THEMES = "All Themes";
 	
 	private String m_themeName;
 	private Database m_db;
 	
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param themeName  Theme Name
 	 */
 	public Theme(String themeName)
 	{
 		m_themeName = themeName;
 		m_db = Database.getDatabaseInstance();
 	}
 	
 	
 	/**
 	 * Gets the theme name.
 	 * 
 	 * @return  The theme name.
 	 */
 	public String getThemeName()
 	{
 		return m_themeName;
 	}
 	
 	
 	/**
 	 * Changes the theme name.
 	 * 
 	 * @param newThemeName  The new theme name.
 	 * @return              True if theme name was changed.
 	 */
 	public boolean changeThemeName(String newThemeName)
 	{
 		if(newThemeName == DEFAULT_THEME_NAME)
 			return false;
 		
 		if(m_db.changeThemeName(m_themeName, newThemeName) == false)
 			return false;
 		
 		m_themeName = newThemeName;
 		setChanged();
 		notifyObservers();
 		return true;
 	}
 }
