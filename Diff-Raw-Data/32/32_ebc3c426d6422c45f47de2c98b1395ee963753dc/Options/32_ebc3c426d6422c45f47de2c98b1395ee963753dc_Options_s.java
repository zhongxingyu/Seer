 package gui;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JColorChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 /**
  * Store options.
  */
 public final class Options {
 	static Preferences prefs;
 	static JMenu optionsMenu;
 			
 	// Used for the main display. 
 	public static int DisplayWidth = 600;
 	public static int DisplayHeight = 800;
 	public static int DisplayTop = 100;
 	public static int DisplayLeft = 100;
 	public static boolean DisplayToolbar = true;
 	
 	// Keyboard shortcuts.
 	public static String CommandRun = "F5";
 	public static String CommandFormat = "F6";
 	
 	// Options menu options.
 	public static boolean EmacsKeybindings = false;
 	public static boolean ConfirmOnRunClose = true;
 	
 	// Syntax highlighting.
 	public static Map<String, Color> Colors;
 	public static Map<String, Integer> Keywords;
 	public static int FontSize = 12;
 
 	/**
      * Initialize.
      */
     static {
     	prefs = Preferences.userNodeForPackage(new Options().getClass());
     	
     	DisplayTop = prefs.getInt("Display/Top", DisplayTop);
     	DisplayLeft = prefs.getInt("Display/Left", DisplayLeft);
     	DisplayWidth = prefs.getInt("Display/Width", DisplayWidth);
     	DisplayHeight = prefs.getInt("Display/Height", DisplayHeight);
     	DisplayToolbar = prefs.getBoolean("Display/Toolbar", DisplayToolbar);
     	
     	CommandRun = prefs.get("Command/Run", CommandRun);
     	CommandFormat = prefs.get("Command/Format", CommandFormat);
 
     	EmacsKeybindings = prefs.getBoolean("Options/EmacsKeybindings", EmacsKeybindings);
     	ConfirmOnRunClose = prefs.getBoolean("Options/ConfirmOnRunClose", ConfirmOnRunClose);
     	
     	Colors = new HashMap<String, Color>();
     	Colors.put("default", new Color(prefs.getInt("Colors/default", 0x000000)));
     	Colors.put("comment", new Color(prefs.getInt("Colors/comment", 0x006600)));
     	Colors.put("keyword", new Color(prefs.getInt("Colors/keyword", 0x000099)));
     	Colors.put("string", new Color(prefs.getInt("Colors/string", 0xFF8C00)));
     	Colors.put("bracket", new Color(prefs.getInt("Colors/bracket", 0x00FFFF)));
     	
     	Keywords = new HashMap<String, Integer>();
     	String keywordString = prefs.get("Keywords", 
     			"define\t2\nlambda\t2\nif\t4\ncond\t2\nand\t5\nor\t4\n" +
     			"+\t2\n-\t2\n*\t2\n/\t2\nadd1\t6\nsub1\t6\nlist\t6\n" +
     			"cons\t6\ncar\t5\ncdr\t\n");
     	for (String pair : keywordString.split("\n")) {
     		String[] parts = pair.split("\t");
     		if (parts.length == 2) {
     			try {
     				Keywords.put(parts[0], Integer.parseInt(parts[1]));
     			} catch (NumberFormatException ex) {
     				ErrorFrame.log("Unable to load keyword '" + parts[0] + "', unknown indentation format: " + parts[1]);
     			}
     		}
     	}
     	
     	FontSize = prefs.getInt("FontSize", FontSize);
     	
     	SchemeDocument.reload();
     }
     
     /**
      * Save to preferences on dispose.
      */
     public static void save() {
     	prefs.putInt("Display/Top", DisplayTop);
     	prefs.putInt("Display/Left", DisplayLeft);
     	prefs.putInt("Display/Width", DisplayWidth);
     	prefs.putInt("Display/Height", DisplayHeight);
     	prefs.putBoolean("Display/Toolbar", DisplayToolbar);
     	
     	prefs.put("Command/Run", CommandRun);
     	prefs.put("Command/Format", CommandFormat);
     	
     	prefs.putBoolean("Options/EmacsKeybindings", EmacsKeybindings);
     	prefs.putBoolean("Options/ConfirmOnRunClose", ConfirmOnRunClose);
     	
     	for (String key : Colors.keySet())
     		prefs.putInt("Colors/" + key, Colors.get(key).getRGB());
     			
 		StringBuilder keywordString = new StringBuilder();
     	for (String key : Keywords.keySet())
     	{
     		keywordString.append(key);
     		keywordString.append("\t");
     		keywordString.append(Keywords.get(key));
     		keywordString.append("\n");
     	}
     	prefs.put("Keywords", keywordString.toString());
     	
     	prefs.putInt("FontSize", FontSize);
     }
     
     /**
      * Build the options menu (only build once).
      * @return An options menu.
      */
     public static JMenu buildOptionsMenu() {
     	if (optionsMenu == null) {
     		optionsMenu = new JMenu("Options");
     		
     		/* uncomment when we have emacs keybindings to add
     		JCheckBoxMenuItem emacs = new JCheckBoxMenuItem("Enable emacs keybindings", EmacsKeybindings);
     		emacs.addItemListener(new ItemListener() {
 				public void itemStateChanged(ItemEvent arg0) {
 					EmacsKeybindings = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
 				}
     		});
     		optionsMenu.add(emacs);
     		*/
     		
     		JCheckBoxMenuItem confirm = new JCheckBoxMenuItem("Confirm on Run/Close", ConfirmOnRunClose);
     		confirm.addItemListener(new ItemListener() {
 				public void itemStateChanged(ItemEvent arg0) {
 					ConfirmOnRunClose = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
 				}
     		});
     		optionsMenu.add(confirm);
     		
     		JCheckBoxMenuItem toolbar = new JCheckBoxMenuItem("Display toolbar", DisplayToolbar);
     		toolbar.addItemListener(new ItemListener() {
 				public void itemStateChanged(ItemEvent arg0) {
 					DisplayToolbar = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
 					MainFrame.me().ToolBar.setVisible(DisplayToolbar);
 				}
     		});
     		optionsMenu.add(toolbar);
     		
     		JMenu colorMenu = new JMenu("Colors");
     		for (final String key : Colors.keySet()) {
     			String fixed = ("" + key.charAt(0)).toUpperCase() + key.substring(1);
     			final JMenuItem item = new JMenuItem(fixed);
     			item.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						Color newColor = JColorChooser.showDialog(item, "Choose color for " + key, Colors.get(key));
 						Colors.put(key, newColor);
 						SchemeDocument.reload();
 						MainFrame.me().Documents.ReloadAll();
 					}
     			});
     			colorMenu.add(item);
     		}
     		optionsMenu.add(colorMenu);
     		
    		JMenu fontSizeMenu = new JMenu("Font size");
     		for (int i = 8; i <= 30; i += 2) {
     			final int fontSize = i;
    			final JMenuItem item = new JMenuItem("" + fontSize);
     			item.addActionListener(new ActionListener() {
     				public void actionPerformed(ActionEvent arg0) {
 						FontSize = fontSize;
 						SchemeDocument.reload();
 						MainFrame.me().Documents.ReloadAll();
 					}
     			});
     			fontSizeMenu.add(item);
     		}
     		optionsMenu.add(fontSizeMenu);
     	}
     	
     	return optionsMenu;
     }
     
     // Hide this.
     private Options() {};
 }
