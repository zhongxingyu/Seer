 package edu.bu.cs673.AwesomeAlphabet.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Observable;
import java.util.Properties;
 
 
 /**
  * This class manages all themes used by the application.
  */
 public class ThemeManager extends Observable {
 
 	public static String DEFAULT_THEME_NAME = "No Theme";
 	
 	private ArrayList<Theme> m_themes;
 	private Theme m_currentTheme;
 	
 	
 	
 	/**
 	 * Constructor.
 	 */
 	public ThemeManager()
 	{
 		m_themes = new ArrayList<Theme>();
 		m_currentTheme = null;
 	}
 	
 
 	/**
 	 * Gets an iterator to be able to walk through all themes
 	 *  
 	 * @return  The iterator.
 	 */
 	public Iterator<Theme> getIterator()
 	{
 		return m_themes.iterator();
 	}
 	
 	
 	/**
 	 * Gets the Theme object based on the theme name.
 	 * 
 	 * @return  The theme object or null if the theme does not exist.
 	 */
 	public Theme getTheme(String themeName)
 	{
 		Iterator<Theme> it = m_themes.iterator();
 		Theme theme;
 		
 		while(it.hasNext())
 		{
 			theme = it.next();
 			if(theme.getThemeName().compareTo(themeName) == 0)
 				return theme;
 		}
 			
 		return null;
 	}
 	
 	
 	/**
 	 * Determines if Theme Manager contains the specified theme.
 	 * 
 	 * @param themeName  Theme Name.
 	 * @return  True if Theme Manager contains the specified theme.
 	 */
 	public boolean hasTheme(String themeName)
 	{
 		return getTheme(themeName) != null;
 	}
 	
 	
 	/**
 	 * Adds a theme to the Theme Manager.
 	 * 
 	 * @param themeName  The theme name.
 	 * @return  True if theme was added successfully or already exists.
 	 */
 	public boolean addTheme(String themeName)
 	{
 		if(hasTheme(themeName))
 			return true;
 		
 		m_themes.add(new Theme(themeName));
 		setChanged();
 		notifyObservers();
 		return true;
 	}
 	
 	
 	/**
 	 * Deletes a theme from the Theme Manager.
 	 * 
 	 * @param themeName  The theme name.
 	 * @return  True if theme was deleted successfully or does not exist.
 	 */
 	public boolean deleteTheme(String themeName)
 	{
 		Theme theme = getTheme(themeName);
 		
 		if(theme == null)
 			return true;
 		
 		m_themes.remove(theme);
 		setChanged();
 		notifyObservers();
 		return true;
 	}
 	
 	
 	/**
 	 * Changes the name of an existing theme.
 	 * 
 	 * @param oldThemeName  Old theme name.
 	 * @param newThemeName  New theme name.
 	 * @return True if theme name was changed.
 	 *         False if old theme not found.
 	 */
 	public boolean changeThemeName(String oldThemeName, String newThemeName)
 	{
 		Theme theme = getTheme(oldThemeName);
 		
 		if(theme == null)
 			return false;
 		
 		if(theme.changeThemeName(newThemeName))
 		{
 			setChanged();
 			notifyObservers();
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	
 	/**
 	 * Sets the current theme.
 	 * 
 	 * @param themeName  Theme name.
 	 * @return  True if current theme was set.
 	 *          False if theme is invalid (ThemeManger does
 	 *          not contain specified theme).
 	 */
 	public boolean setCurrentTheme(String themeName)
 	{
 		if(themeName == null)
 		{
 			if(m_currentTheme != null)
 				setChanged();
 			m_currentTheme = null;
 			
 			notifyObservers();
 			return true;
 		}
 			
 		Theme theme = getTheme(themeName);
 		
 		if(theme == null)
 			return false;
 		
 		if(m_currentTheme != theme)
 			setChanged();
 		m_currentTheme = theme;
 		
 		notifyObservers();
 		return true;
 	}
 	
 	
 	/**
 	 * Gets the current theme.
 	 * 
 	 * @return  The current theme.
 	 */
 	public Theme getCurrentTheme()
 	{
 		return m_currentTheme;
 	}
 }
