 package admin;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.regex.Pattern;
 
 import javax.swing.JComponent;
 
 
 public class Utils {
 	
 	//A clean way to handle themes with minimal code.
 	enum THEMES {
 		//ThemeName(Foreground Color,Background Color);
 		Western(new Color(255, 255, 255), new Color(79, 38, 100)), 
 			Snow(new Color(0, 0, 255), new Color(255, 255, 255)),
 				BandW(new Color(255,255,255),new Color(0,0,0));
 		Color fore, back;
 
 		THEMES(Color f, Color b) {
 			fore = f;
 			back = b;
 		}
 
 		public Color getBackground() {
 			return back;
 		}
 
 		public Color getForeground() {
 			return fore;
 		}
 	};
 
 	private static THEMES theme = THEMES.Western;
 
 	
 	/**
 	 * The background colour of current theme
 	 * @return Color get the background colour of current theme
 	 */
 	public static Color getThemeBG(){
 		return theme.getBackground();
 	}
 	
 	/**
 	 * The foreground colour of current theme
 	 * @return Color get the foreground colour of current theme
 	 */
 	public static Color getThemeFG(){
 		return theme.getForeground();
 	}
 	
 	/**
 	 * Change the current theme
 	 * @see getThemes() to get all themes
 	 * @param name The theme's name to change to
 	 */
 	public static void changeTheme(String name){
 		THEMES[] t = THEMES.values();
 		for(int i =0;i<t.length;i++){
 			if(t[i].name().equalsIgnoreCase(name))
 				theme = t[i];
 		}
 	}
 	
 	/**
 	 * Gets a string array of all themes possible
 	 * @return String array with the themes names
 	 */
 	public static String[] getThemes(){
 		THEMES[] t = THEMES.values();
 		String[] themeString = new String[t.length];
 		for(int i =0;i<t.length;i++){
 			themeString[i]=t[i].name();
 		}
 		return themeString;
 	}
 	
	//modified from http://today.java.net/pub/a/today/2003/10/14/swingcss.html
 	public static void style(Component comp) {
 	
 		comp.setForeground(theme.getForeground());
         comp.setBackground(theme.getBackground());
     	if(! (comp instanceof Container)) {
     	    return;
         }
        
         Component[] comps = ((Container)comp).getComponents();
         System.out.println(comps.length);
         for(int i=0; i<comps.length; i++) {
             style(comps[i]);
         }
     }
 	/**
 	 * Checks if string matches pattern.
 	 * @param val The string to check for validity
 	 * @param pattern A regex pattern that has all possible valid values
 	 * @return true if string matches pattern
 	 */
 	public static boolean checkString(String val,String pattern){
 		if(val==null)
 			return false;
 		if(val.length()==0)
 			return false;
 		return Pattern.matches(pattern, val);
 	}
 	
 	
 }
