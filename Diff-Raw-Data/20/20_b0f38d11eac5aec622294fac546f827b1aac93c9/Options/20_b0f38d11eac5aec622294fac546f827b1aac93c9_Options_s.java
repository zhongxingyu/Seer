 package wombat;
 
 import gui.MainFrame;
 import gui.SchemeDocument;
 import gui.SyntaxDialog;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
import java.awt.FontMetrics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
import java.awt.font.FontRenderContext;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JColorChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import util.files.RecentDocumentManager;
 
 /**
  * Store options.
  */
 public final class Options {
 	static Preferences prefs = Preferences.userNodeForPackage(new Options().getClass());
 	static JMenu optionsMenu;
 			
 	// Used for the main display. 
 	public static int DisplayWidth;
 	public static int DisplayHeight;
 	public static int DisplayTop;
 	public static int DisplayLeft;
 	public static boolean DisplayToolbar;
 	
 	// Keyboard shortcuts.
 	public static String CommandRun;
 	public static String CommandFormat;
 	
 	// Options menu options.
 	public static boolean EmacsKeybindings;
 	public static boolean ConfirmOnRun;
 	public static boolean ConfirmOnClose;
 	
 	// Syntax highlighting.
 	public static Map<String, Color> Colors;
 	public static Map<String, Integer> Keywords;
 	public static int FontSize;
 	public static int FontWidth;
 	
 	// Recently used documents.
 	public static String RecentDocuments;
 
 	/**
      * Initialize.
      */
     static { load(); }
     
     private static void load() {
     	DisplayTop = prefs.getInt("Display/Top", 100);
     	DisplayLeft = prefs.getInt("Display/Left", 100);
     	DisplayWidth = prefs.getInt("Display/Width", 800);
     	DisplayHeight = prefs.getInt("Display/Height", 600);
     	DisplayToolbar = prefs.getBoolean("Display/Toolbar", true);
     	
     	CommandRun = prefs.get("Command/Run", "F5");
     	CommandFormat = prefs.get("Command/Format", "F6");
 
     	EmacsKeybindings = prefs.getBoolean("Options/EmacsKeybindings", false);
     	ConfirmOnRun = prefs.getBoolean("Options/ConfirmOnRun", true);
     	ConfirmOnClose = prefs.getBoolean("Options/ConfirmOnClose", true);
     	
     	Colors = new HashMap<String, Color>();
     	Colors.put("default", new Color(prefs.getInt("Colors/default", 0x000000)));
     	Colors.put("comment", new Color(prefs.getInt("Colors/comment", 0x006600)));
     	Colors.put("keyword", new Color(prefs.getInt("Colors/keyword", 0x000099)));
     	Colors.put("string", new Color(prefs.getInt("Colors/string", 0xFF8C00)));
     	Colors.put("bracket", new Color(prefs.getInt("Colors/bracket", 0x00FFFF)));
     	Colors.put("invalid-bracket", new Color(prefs.getInt("Colors/invalid-bracket", 0xFF0000)));
     	
     	Keywords = new HashMap<String, Integer>();
     	
     	FontSize = prefs.getInt("FontSize", 12);
     	calculateFontWidth();
     	
     	SchemeDocument.reload();
     	
     	RecentDocumentManager.setFiles(prefs.get("RecentDocuments", null));
     	SyntaxDialog.setSyntax(prefs.get("Syntax", null));
     }
     
     private static void calculateFontWidth() {
    	Component c = new Component(){};
     	FontWidth = c.getFontMetrics(new Font("Monospaced", Font.PLAIN, FontSize)).charWidth(' ');
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
     	prefs.putBoolean("Options/ConfirmOnRun", ConfirmOnRun);
     	prefs.putBoolean("Options/ConfirmOnClose", ConfirmOnClose);
     	
     	for (String key : Colors.keySet())
     		prefs.putInt("Colors/" + key, Colors.get(key).getRGB());
     	
     	prefs.putInt("FontSize", FontSize);
     	
     	prefs.put("RecentDocuments", RecentDocumentManager.getFiles());
     	prefs.put("Syntax", SyntaxDialog.getSyntax());
     }
     
     /**
      * Build the options menu (only build once).
      * @return An options menu.
      */
     public static JMenu buildOptionsMenu(final MainFrame main) {
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
     		
     		JCheckBoxMenuItem confirmRun = new JCheckBoxMenuItem("Confirm on Run", ConfirmOnRun);
     		confirmRun.addItemListener(new ItemListener() {
 				public void itemStateChanged(ItemEvent arg0) {
 					ConfirmOnRun = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
 				}
     		});
     		optionsMenu.add(confirmRun);
     		
     		JCheckBoxMenuItem confirmClose = new JCheckBoxMenuItem("Confirm on Close", ConfirmOnClose);
     		confirmClose.addItemListener(new ItemListener() {
 				public void itemStateChanged(ItemEvent arg0) {
 					ConfirmOnClose = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
 				}
     		});
     		optionsMenu.add(confirmClose);
     		
     		JCheckBoxMenuItem toolbar = new JCheckBoxMenuItem("Display toolbar", DisplayToolbar);
     		toolbar.addItemListener(new ItemListener() {
 				public void itemStateChanged(ItemEvent arg0) {
 					DisplayToolbar = ((JCheckBoxMenuItem) arg0.getSource()).isSelected();
 					main.toggleToolbar(DisplayToolbar);
 				}
     		});
     		optionsMenu.add(toolbar);
     		
     		optionsMenu.addSeparator();
     		
     		JMenuItem resetSyntax = new JMenuItem("Keywords");
         	resetSyntax.addActionListener(new ActionListener() {
     			@Override
     			public void actionPerformed(ActionEvent e) {
     				SyntaxDialog.show();
     			}    		
         	});
         	optionsMenu.add(resetSyntax);
     		
     		JMenu colorMenu = new JMenu("Colors");
     		JMenuItem resetColors = new JMenuItem("Reset colors");
     		resetColors.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					Colors.put("default", new Color(0x000000));
 	    	    	Colors.put("comment", new Color(0x006600));
 	    	    	Colors.put("keyword", new Color(0x000099));
 	    	    	Colors.put("string", new Color(0xFF8C00));
 	    	    	Colors.put("bracket", new Color(0x00FFFF));
 	    	    	Colors.put("invalid-bracket", new Color(0x00FFFF));
 	    	    	SchemeDocument.reload();
 					DocumentManager.ReloadAll();
 				};
     		});
     		colorMenu.add(resetColors);
     		colorMenu.addSeparator();
     		
     		for (final String key : Colors.keySet()) {
     			String fixed = ("" + key.charAt(0)).toUpperCase() + key.substring(1);
     			final JMenuItem item = new JMenuItem(fixed);
     			item.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						Color newColor = JColorChooser.showDialog(item, "Choose color for " + key, Colors.get(key));
 						Colors.put(key, newColor);
 						SchemeDocument.reload();
 						DocumentManager.ReloadAll();
 					}
     			});
     			colorMenu.add(item);
     		}
     		optionsMenu.add(colorMenu);
     		
     		final JMenu fontSizeMenu = new JMenu("Font size");
     		for (int i = 8; i <= 30; i += 2) {
     			final int fontSize = i;
     			final JCheckBoxMenuItem item = new JCheckBoxMenuItem("" + fontSize, false);
     			if (fontSize == FontSize)
     				item.setSelected(true);
     			item.addActionListener(new ActionListener() {
     				public void actionPerformed(ActionEvent arg0) {
     					for (int i = 0; i < fontSizeMenu.getItemCount(); i++)
     						((JCheckBoxMenuItem) fontSizeMenu.getItem(i)).setSelected(false);
     					((JCheckBoxMenuItem) arg0.getSource()).setSelected(true);
     					
 						FontSize = fontSize;
 						SchemeDocument.reload();
 						DocumentManager.ReloadAll();
 						
 						calculateFontWidth();
 					}
     			});
     			fontSizeMenu.add(item);
     		}
     		optionsMenu.add(fontSizeMenu);
     	}
     	
     	return optionsMenu;
     }
     
     // Hide this.
     Options() {};
 }
