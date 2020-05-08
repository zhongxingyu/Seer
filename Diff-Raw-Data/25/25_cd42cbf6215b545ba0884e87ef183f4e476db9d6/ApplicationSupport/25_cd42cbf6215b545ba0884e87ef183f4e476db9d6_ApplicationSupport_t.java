 package core;
 /*
  * Ce code provient d'un exemple sur Internet, de David Geary,  la page suivante:
  * http://www.javaworld.com/javaworld/jw-05-2003/jw-0530-designpatterns-p2.html
  * 
  *  A Swing application faade
  *  Swing is one of my all-time favorite GUI frameworks, but even after writing 
  * a 1,600-page book on the subject, I have to refer to the book and Javadocs 
  * almost every time I write a Swing application. Just getting a Swing application 
  * off the ground can require heavy lifting, especially if you follow best 
  * practices by internationalizing your application. This section shows you how to 
  * implement a faade that encapsulates much of the grunt work involved in setting 
  * up a Swing application.
  */
 import java.awt.*;
 import java.awt.event.*;
 import java.text.MessageFormat;
 import java.util.*;
 import javax.swing.*;
 
 public final class ApplicationSupport {
    static private final String PREFS = "prefs";
    static private final String BUNDLE_BASENAME = "app", 
    							   LOCALE_KEY = "locale";
 
    static private final JPanel STATUS_AREA = new JPanel();
    static private final JLabel STATUS = new JLabel();
    static private ResourceBundle preferences, resources;
    static private Locale locale;
 
    static {
       try {
          preferences = ResourceBundle.getBundle(PREFS);
          locale = new Locale(preferences.getString(LOCALE_KEY));
       }
       catch(java.util.MissingResourceException ex) {
          System.err.println("ERROR: cannot find preferences properties file " + 
                             BUNDLE_BASENAME);
       }
 
       try {
          resources = ResourceBundle.getBundle(BUNDLE_BASENAME, locale);
       }
       catch(java.util.MissingResourceException ex) {
          System.err.println("ERROR: cannot find properties file for " + BUNDLE_BASENAME);
       }
 
    };
 
    // disallow direct instantiation
    private ApplicationSupport() {}
    public static void launch(final JFrame jFrame, String title,
                        final int xBounds, final int yBounds, 
                        final int width, int height) {
 	  
       jFrame.setTitle(title);
       jFrame.setBounds(xBounds,yBounds,width,height);
       jFrame.setVisible(true);
       jFrame.setResizable(true);
 
       STATUS.setHorizontalAlignment(JLabel.LEFT);
 
       STATUS_AREA.setBorder(BorderFactory.createEtchedBorder());
       STATUS_AREA.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
       STATUS_AREA.add(STATUS);
 
       jFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
       jFrame.addWindowListener(new WindowAdapter() {
          public void windowClosed(WindowEvent event) {
         	 System.exit(0);
          }
       });
    }
    public static Locale getLocale() {
       return locale;
    }
    public static JMenu addMenu(final JFrame jFrame, String titleKey,
                                String[] itemKeys) {
 
       JMenuBar menuBar = jFrame.getJMenuBar();
       if(menuBar == null) {
          menuBar = new JMenuBar();
          jFrame.setJMenuBar(menuBar);
       }
 
       JMenu menu = new JMenu(ApplicationSupport.getResource(titleKey));
 
       for(int i=0; i < itemKeys.length; ++i) {
          menu.add(new JMenuItem(ApplicationSupport.getResource(itemKeys[i])));
       }
       menuBar.add(menu);
       return menu;
    }
    public static JPanel getStatusArea() {
       return STATUS_AREA;
    }
    public static void showStatus(String statusString) {
       STATUS.setText(statusString);
    }
    public static String getResource(String key) {
       return (resources == null) ? null : resources.getString(key);
    }
    public static String formatMessage(String patternKey, String[] params) {
       String pattern = ApplicationSupport.getResource(patternKey);
       MessageFormat fmt = new MessageFormat(pattern);
       return fmt.format(params);
    }
 }
