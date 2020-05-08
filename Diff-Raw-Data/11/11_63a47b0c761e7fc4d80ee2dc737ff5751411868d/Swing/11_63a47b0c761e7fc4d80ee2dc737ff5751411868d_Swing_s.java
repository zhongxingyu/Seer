 /*
  * Created On: Jun 3, 2006 2:16:48 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.util;
 
 import javax.swing.LookAndFeel;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 
 import com.thinkparity.codebase.OSUtil;
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
 import com.thinkparity.ophelia.browser.Constants.Colors;
 import com.thinkparity.ophelia.browser.application.browser.BrowserConstants;
 import com.thinkparity.ophelia.browser.util.swing.plaf.ThinkParityMenuItemUI;
 import com.thinkparity.ophelia.browser.util.swing.plaf.ThinkParityProgressBarUI;
 import com.thinkparity.ophelia.browser.util.swing.plaf.ThinkParityScrollBarUI;
 
 /**
  * thinkParity Swing
  * 
  * @author raymond@thinkparity.com
  * @version $Revision$
  */
 public class Swing {
 
     /**
      * Initialize thinkParity Swing.
      *
      */
     public static void init() {
         System.setProperty("sun.awt.noerasebackground", "true");
 
         UIDefaults defaults = UIManager.getDefaults();
         defaults.put("List.selectionBackground", Colors.Swing.DEFAULT_LIST_SELECTION_BG);
         defaults.put("List.selectionForeground", Colors.Swing.DEFAULT_LIST_SELECTION_FG);
         defaults.put("Menu.font", BrowserConstants.Fonts.DefaultFont);
         defaults.put("Menu.selectionBackground", Colors.Swing.MENU_SELECTION_BG);
         defaults.put("Menu.selectionForeground", Colors.Swing.MENU_SELECTION_FG);
         defaults.put("Menu.background", Colors.Swing.MENU_BG);
         defaults.put("Menu.foreground", Colors.Swing.MENU_FG);
         defaults.put("MenuItem.font", BrowserConstants.Fonts.DefaultFont);
         defaults.put("MenuItem.selectionBackground", Colors.Swing.MENU_ITEM_SELECTION_BG);
         defaults.put("MenuItem.selectionForeground", Colors.Swing.MENU_ITEM_SELECTION_FG);
         defaults.put("MenuItem.background", Colors.Swing.MENU_ITEM_BG);
         defaults.put("MenuItem.foreground", Colors.Swing.MENU_ITEM_FG);
         defaults.put("Menu.submenuPopupOffsetX", BrowserConstants.Menu.SUBMENU_POPUP_OFFSET_X);
         defaults.put("Menu.submenuPopupOffsetY", BrowserConstants.Menu.SUBMENU_POPUP_OFFSET_Y);
 
         try {
         	final LookAndFeel laf;
         	switch (OSUtil.getOS()) {
         	case WINDOWS_XP:
         		laf = new WindowsLookAndFeel();
         		break;
         	case LINUX:
         		laf = new MetalLookAndFeel();
         		break;
     		default:
     			throw Assert.createUnreachable("Unsupported operating system.");
         	}
         	UIManager.setLookAndFeel(laf);
         } catch(final Throwable t) {
             throw new RuntimeException(t);
         }
 
         defaults = UIManager.getDefaults();
         defaults.put("ProgressBarUI", ThinkParityProgressBarUI.class.getName());
         defaults.put("MenuItemUI", ThinkParityMenuItemUI.class.getName());
         defaults.put("ScrollBarUI", ThinkParityScrollBarUI.class.getName());
     }
 
     /**
      * Create Swing.
      * 
      */
     private Swing() { super(); }
 }
