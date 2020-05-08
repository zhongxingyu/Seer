 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing;
 
 import com.dmdirc.Channel;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.Server;
 import com.dmdirc.addons.ui_swing.components.addonpanel.AddonPanel;
 import com.dmdirc.addons.ui_swing.components.addonpanel.PluginPanel;
 import com.dmdirc.addons.ui_swing.components.addonpanel.ThemePanel;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.addons.ui_swing.components.statusbar.FeedbackNag;
 import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
 import com.dmdirc.addons.ui_swing.dialogs.DialogKeyListener;
 import com.dmdirc.addons.ui_swing.dialogs.StandardMessageDialog;
 import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
 import com.dmdirc.addons.ui_swing.dialogs.error.ErrorListDialog;
 import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
 import com.dmdirc.addons.ui_swing.dialogs.prefs.URLConfigPanel;
 import com.dmdirc.addons.ui_swing.dialogs.prefs.UpdateConfigPanel;
 import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
 import com.dmdirc.addons.ui_swing.dialogs.url.URLDialog;
 import com.dmdirc.addons.ui_swing.wizard.WizardListener;
 import com.dmdirc.addons.ui_swing.wizard.firstrun.SwingFirstRunWizard;
 import com.dmdirc.config.Identity;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.config.prefs.PluginPreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesInterface;
 import com.dmdirc.config.prefs.PreferencesDialogModel;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.ui.interfaces.Window;
 import com.dmdirc.util.validators.NumericalValidator;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.plugins.BasePlugin;
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.core.components.StatusBarManager;
 import com.dmdirc.ui.core.util.URLHandler;
 import com.dmdirc.ui.interfaces.InputWindow;
 import com.dmdirc.ui.interfaces.UIController;
 import com.dmdirc.util.ReturnableThread;
 
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.awt.KeyboardFocusManager;
 import java.awt.Toolkit;
 import java.awt.Dialog.ModalityType;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.swing.JMenuItem;
 
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import net.miginfocom.layout.PlatformDefaults;
 
 /**
  * Controls the main swing UI.
  */
 public class SwingController extends BasePlugin implements UIController {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
 
     /** Window factory. */
     private final SwingWindowFactory windowFactory =
             new SwingWindowFactory(this);
     /** Waiting on mainframe creation. */
     private final AtomicBoolean mainFrameCreated = new AtomicBoolean(false);
     /** URL Handler to use. */
     private final URLHandler urlHandler = new URLHandler(this);
     /** Singleton instance of MainFrame. */
     private MainFrame me;
     /** Status bar. */
     private SwingStatusBar statusBar;
     /** Top level window list. */
     private final List<java.awt.Window> windows;
     /** Error dialog. */
     private ErrorListDialog errorDialog;
     /** DMDirc event queue. */
     private DMDircEventQueue eventQueue;
     /** Key listener to handle dialog key events. */
     private DialogKeyListener keyListener;
 
     /** Instantiates a new SwingController. */
     public SwingController() {
         super();
 
         setAntiAlias();
         windows = new ArrayList<java.awt.Window>();
     }
 
     /**
      * Make swing not use Anti Aliasing if the user doesn't want it.
      */
     public final void setAntiAlias() {
         // For this to work it *HAS* to be before anything else UI related.
         final boolean aaSetting = IdentityManager.getGlobalConfig().
                 getOptionBool("ui", "antialias");
         System.setProperty("awt.useSystemAAFontSettings",
                 Boolean.toString(aaSetting));
         System.setProperty("swing.aatext", Boolean.toString(aaSetting));
     }
 
     /**
      * Does the main frame exist?
      *
      * @return true iif mainframe exists
      */
     protected boolean hasMainFrame() {
         return mainFrameCreated.get();
     }
 
     /**
      * Returns a URL Handler which may be used when working with the Swing UI.
      *
      * @return A URL handler for use with the swing UI
      * @since 0.6.5
      */
     public URLHandler getURLHandler() {
         return urlHandler;
     }
 
     /**
      * Returns the window factory used by this controller.
      *
      * @return This controller's window factory
      * @since 0.6.4
      */
     public SwingWindowFactory getWindowFactory() {
         return windowFactory;
     }
 
     /**
      * Retrieves the main window used by this UI.
      *
      * @return This UI's main window
      */
     public MainFrame getMainFrame() {
         return me;
     }
 
     /**
      * Retrieves the Swing Status Bar used by this UI.
      *
      * @return This UI's status bar
      */
     public SwingStatusBar getSwingStatusBar() {
         return statusBar;
     }
 
     /** {@inheritDoc} */
     @Override
     public void showFirstRunWizard() {
         final Semaphore semaphore = new Semaphore(0);
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 final WizardListener listener = new WizardListener() {
 
                     /** {@inheritDoc} */
                     @Override
                     public void wizardFinished() {
                         semaphore.release();
                     }
 
                     /** {@inheritDoc} */
                     @Override
                     public void wizardCancelled() {
                         semaphore.release();
                     }
                 };
                 final SwingFirstRunWizard wizard = new SwingFirstRunWizard(me,
                         SwingController.this);
                 wizard.getWizardDialog().addWizardListener(listener);
                 wizard.display();
             }
         });
         semaphore.acquireUninterruptibly();
     }
 
     /** {@inheritDoc} */
     @Override
     public void showChannelSettingsDialog(final Channel channel) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 ChannelSettingsDialog.showChannelSettingsDialog(channel, me,
                         (InputWindow) getWindowFactory().getSwingWindow(
                         channel));
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void showServerSettingsDialog(final Server server) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 ServerSettingsDialog.showServerSettingsDialog(server, me);
             }
         });
     }
 
     /**
      * Updates the look and feel to the current config setting.
      */
     public void updateLookAndFeel() {
         try {
             UIManager.setLookAndFeel(UIUtilities.getLookAndFeel(IdentityManager.
                     getGlobalConfig().getOption("ui", "lookandfeel")));
             final int state = UIUtilities.invokeAndWait(
                     new ReturnableThread<Integer>() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     setObject(me.getExtendedState());
                 }
             });
             UIUtilities.invokeLater(new Runnable() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     SwingUtilities.updateComponentTreeUI(errorDialog);
                 }
             });
             for (final java.awt.Window window : getTopLevelWindows()) {
                 UIUtilities.invokeLater(new Runnable() {
 
                     /** {@inheritDoc} */
                     @Override
                     public void run() {
                         SwingUtilities.updateComponentTreeUI(window);
                         if (window != me) {
                             window.pack();
                         }
                     }
                 });
             }
             UIUtilities.invokeLater(new Runnable() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     me.setExtendedState(state);
                 }
             });
         } catch (ClassNotFoundException ex) {
             Logger.userError(ErrorLevel.LOW,
                     "Unable to change Look and Feel: " + ex.getMessage());
         } catch (InstantiationException ex) {
             Logger.userError(ErrorLevel.LOW,
                     "Unable to change Look and Feel: " + ex.getMessage());
         } catch (IllegalAccessException ex) {
             Logger.userError(ErrorLevel.LOW,
                     "Unable to change Look and Feel: " + ex.getMessage());
         } catch (UnsupportedLookAndFeelException ex) {
             Logger.userError(ErrorLevel.LOW,
                     "Unable to change Look and Feel: " + ex.getMessage());
         }
     }
 
     /**
      * Initialises the global UI settings for the Swing UI.
      */
     private void initUISettings() {
         // This will do nothing on non OS X Systems
         if (Apple.isApple()) {
             final Apple apple = Apple.getApple();
 
             apple.setUISettings();
             apple.setListener();
         }
 
         final Font defaultFont = new Font(Font.DIALOG, Font.TRUETYPE_FONT, 12);
         if (UIManager.getFont("TextField.font") == null) {
             UIManager.put("TextField.font", defaultFont);
         }
         if (UIManager.getFont("TextPane.font") == null) {
             UIManager.put("TextPane.font", defaultFont);
         }
 
         try {
             UIUtilities.initUISettings();
             UIManager.setLookAndFeel(UIUtilities.getLookAndFeel(IdentityManager.
                     getGlobalConfig().getOption("ui", "lookandfeel")));
         } catch (UnsupportedOperationException ex) {
             Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
         } catch (UnsupportedLookAndFeelException ex) {
             Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
         } catch (IllegalAccessException ex) {
             Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
         } catch (InstantiationException ex) {
             Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
         } catch (ClassNotFoundException ex) {
             Logger.userError(ErrorLevel.LOW, "Unable to set UI Settings");
         }
 
         if ("Metal".equals(UIManager.getLookAndFeel().getName())
                 || Apple.isAppleUI()) {
             PlatformDefaults.setPlatform(PlatformDefaults.WINDOWS_XP);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void showURLDialog(final URI url) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 URLDialog.showURLDialog(url, me, urlHandler);
 
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void showFeedbackNag() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 new FeedbackNag(SwingController.this);
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void showMessageDialog(final String title, final String message) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 new StandardMessageDialog(me, ModalityType.MODELESS, title,
                         message).display();
             }
         });
     }
 
     /**
      * Shows the error dialog.
      */
     public void showErrorDialog() {
         errorDialog.display();
     }
 
     /** {@inheritDoc} */
     @Override
     public PreferencesInterface getPluginPrefsPanel() {
         return UIUtilities.invokeAndWait(new ReturnableThread<AddonPanel>() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 setObject(new PluginPanel(me, SwingController.this));
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public PreferencesInterface getUpdatesPrefsPanel() {
         return UIUtilities.invokeAndWait(
                 new ReturnableThread<PreferencesInterface>() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 setObject(new UpdateConfigPanel(SwingController.this));
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public PreferencesInterface getUrlHandlersPrefsPanel() {
         return UIUtilities.invokeAndWait(
                 new ReturnableThread<PreferencesInterface>() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 setObject(new URLConfigPanel(me, urlHandler));
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public PreferencesInterface getThemesPrefsPanel() {
         return UIUtilities.invokeAndWait(
                 new ReturnableThread<PreferencesInterface>() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 setObject(new ThemePanel(me, SwingController.this));
             }
         });
     }
 
     /**
      * Returns the current look and feel.
      *
      * @return Current look and feel
      */
     public static String getLookAndFeel() {
         return UIManager.getLookAndFeel().getName();
     }
 
     /** {@inheritDoc} */
     @Override
     public void onLoad() {
         if (GraphicsEnvironment.isHeadless()) {
             throw new IllegalStateException(
                     "Swing UI can't be run in a headless environment");
         }
 
         eventQueue = new DMDircEventQueue(this);
         keyListener = new DialogKeyListener();
         Toolkit.getDefaultToolkit().getSystemEventQueue().push(eventQueue);
         KeyboardFocusManager.getCurrentKeyboardFocusManager().
                 addKeyEventDispatcher(keyListener);
 
         UIUtilities.invokeAndWait(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 initUISettings();
                 me = new MainFrame(SwingController.this);
                 me.setVisible(true);
                 mainFrameCreated.set(true);
                 statusBar = me.getStatusBar();
                 errorDialog = new ErrorListDialog(me);
                 StatusBarManager.getStatusBarManager().registerStatusBar(
                         statusBar);
             }
         });
 
         if (!mainFrameCreated.get()) {
             throw new IllegalStateException(
                     "Main frame not created. Unable to continue.");
         }
 
        WindowManager.addFrameListener(windowFactory);
     }
 
     /** {@inheritDoc} */
     @Override
     public void onUnload() {
         errorDialog.dispose();
         WindowManager.removeFrameListener(windowFactory);
         mainFrameCreated.set(false);
         me.dispose();
         windowFactory.dispose();
         StatusBarManager.getStatusBarManager().registerStatusBar(statusBar);
         eventQueue.pop();
         KeyboardFocusManager.getCurrentKeyboardFocusManager().
                 removeKeyEventDispatcher(keyListener);
         for (java.awt.Window window : getTopLevelWindows()) {
             window.dispose();
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void domainUpdated() {
         final Identity defaults = IdentityManager.getAddonIdentity();
 
 
         defaults.setOption("ui", "textPaneFontName",
                 UIManager.getFont("TextPane.font").getFamily());
         defaults.setOption("ui", "textPaneFontSize",
                 UIManager.getFont("TextPane.font").getSize());
     }
 
     /**
      * Returns the preferences dialog instance creating if required.
      *
      * @return Swing prefs dialog
      */
     public SwingPreferencesDialog getPrefsDialog() {
         return SwingPreferencesDialog.getSwingPreferencesDialog(this);
     }
 
     /** {@inheritDoc} */
     @Override
     public void showConfig(final PreferencesDialogModel manager) {
         manager.getCategory("GUI").addSubCategory(createGeneralCategory());
 
     }
 
     /**
      * Creates the "Advanced" category.
      *
      * @return Newly created preferences category
      */
     private PreferencesCategory createGeneralCategory() {
         final PreferencesCategory general = new PluginPreferencesCategory(
                 getPluginInfo(), "Swing UI", "These config options apply "
                 + "only to the swing UI.", "category-gui");
 
         final Map<String, String> lafs = new HashMap<String, String>();
         final Map<String, String> framemanagers = new HashMap<String, String>();
         final Map<String, String> fmpositions = new HashMap<String, String>();
 
         framemanagers.put(
                 "com.dmdirc.addons.ui_swing.framemanager.tree.TreeFrameManager",
                 "Treeview");
         framemanagers.put(
                 "com.dmdirc.addons.ui_swing.framemanager.buttonbar.ButtonBar",
                 "Button bar");
 
         fmpositions.put("top", "Top");
         fmpositions.put("bottom", "Bottom");
         fmpositions.put("left", "Left");
         fmpositions.put("right", "Right");
 
         final LookAndFeelInfo[] plaf = UIManager.getInstalledLookAndFeels();
 
         lafs.put("Native", "Native");
         for (LookAndFeelInfo laf : plaf) {
             lafs.put(laf.getName(), laf.getName());
         }
 
         general.addSetting(new PreferencesSetting("ui", "lookandfeel",
                 "Look and feel", "The Java look and feel to use", lafs));
         general.addSetting(new PreferencesSetting("ui", "framemanager",
                 "Window manager", "Which window manager should be used?",
                 framemanagers));
         general.addSetting(new PreferencesSetting("ui", "framemanagerPosition",
                 "Window manager position", "Where should the window "
                 + "manager be positioned?", fmpositions));
         general.addSetting(new PreferencesSetting(PreferencesType.FONT,
                 "ui", "textPaneFontName", "Textpane font",
                 "Font for the textpane"));
         general.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                 "ui", "textPaneFontSize", "Textpane font size",
                 "Font size for the textpane"));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 "ui", "sortrootwindows", "Sort root windows",
                 "Sort child windows in the frame managers?"));
         general.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 "ui", "sortchildwindows", "Sort child windows",
                 "Sort root windows in the frame managers?"));
 
         general.addSubCategory(createNicklistCategory());
         general.addSubCategory(createTreeViewCategory());
         general.addSubCategory(createAdvancedCategory());
 
         return general;
     }
 
     /**
      * Creates the "Advanced" category.
      *
      * @return Newly created preferences category
      */
     private PreferencesCategory createAdvancedCategory() {
         final PreferencesCategory advanced = new PluginPreferencesCategory(
                 getPluginInfo(), "Advanced", "");
 
         advanced.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALINTEGER,
                 new NumericalValidator(10, -1), "ui", "frameBufferSize",
                 "Window buffer size", "The maximum number of lines in a window"
                 + " buffer"));
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 getDomain(), "mdiBarVisibility", "MDI Bar Visibility",
                 "Controls the visibility of the MDI bar"));
         advanced.addSetting(
                 new PreferencesSetting(PreferencesType.BOOLEAN, "ui",
                 "useOneTouchExpandable", "Use one touch expandable split "
                 + "panes?", "Use one touch expandable arrows for "
                 + "collapsing/expanding the split panes"));
         advanced.addSetting(new PreferencesSetting(PreferencesType.INTEGER,
                 getDomain(), "windowMenuItems", "Window menu item count",
                 "Number of items to show in the window menu"));
         advanced.addSetting(
                 new PreferencesSetting(PreferencesType.INTEGER, getDomain(),
                 "windowMenuScrollInterval", "Window menu scroll interval",
                 "Number of milliseconds to pause when autoscrolling in the "
                 + "window menu"));
         advanced.addSetting(
                 new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(),
                 "showtopicbar", "Show topic bar",
                 "Shows a graphical topic bar in channels."));
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 getDomain(),
                 "shownicklist", "Show nicklist?",
                 "Do you want the nicklist visible"));
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 getDomain(), "showfulltopic", "Show full topic in topic bar?",
                "Do you want to show the full topic in the topic bar or just"
                + "first line?"));
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 getDomain(), "hideEmptyTopicBar", "Hide empty topic bar?",
                 "Do you want to hide the topic bar when there is no topic"));
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 getDomain(), "textpanelinenotification",
                 "New line notification", "Do you want to be notified about new "
                 + "lines whilst scrolled up?"));
 
         return advanced;
     }
 
     /**
      * Creates the "Treeview" category.
      *
      * @return Newly created preferences category
      */
     private PreferencesCategory createTreeViewCategory() {
         final PreferencesCategory treeview = new PluginPreferencesCategory(
                 getPluginInfo(), "Treeview", "", "treeview");
 
         treeview.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALCOLOUR,
                 "treeview", "backgroundcolour", "Treeview background colour",
                 "Background colour to use for the treeview"));
         treeview.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALCOLOUR,
                 "treeview", "foregroundcolour", "Treeview foreground colour",
                 "Foreground colour to use for the treeview"));
         treeview.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALCOLOUR,
                 "ui", "treeviewRolloverColour", "Treeview rollover colour",
                 "Background colour to use when the mouse cursor is over a "
                 + "node"));
         treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 "ui", "treeviewActiveBold", "Active node bold",
                 "Make the active node bold?"));
         treeview.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALCOLOUR,
                 "ui", "treeviewActiveBackground", "Active node background",
                 "Background colour to use for active treeview node"));
         treeview.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALCOLOUR,
                 "ui", "treeviewActiveForeground", "Active node foreground",
                 "Foreground colour to use for active treeview node"));
         treeview.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 getDomain(), "showtreeexpands", "Show expand/collapse handles",
                 "Do you want to show tree view collapse/expand handles"));
 
         return treeview;
     }
 
     /**
      * Creates the "Nicklist" category.
      *
      * @return Newly created preferences category
      */
     private PreferencesCategory createNicklistCategory() {
         final PreferencesCategory nicklist = new PluginPreferencesCategory(
                 getPluginInfo(), "Nicklist", "", "nicklist");
 
         nicklist.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALCOLOUR,
                 "ui", "nicklistbackgroundcolour", "Nicklist background colour",
                 "Background colour to use for the nicklist"));
         nicklist.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALCOLOUR,
                 "ui", "nicklistforegroundcolour", "Nicklist foreground colour",
                 "Foreground colour to use for the nicklist"));
         nicklist.addSetting(new PreferencesSetting(
                 PreferencesType.OPTIONALCOLOUR,
                 "ui", "nickListAltBackgroundColour",
                 "Alternate background colour",
                 "Background colour to use for every other nicklist entry"));
         nicklist.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 "nicklist", "sortByMode", "Sort nicklist by user mode",
                 "Sort nicknames by the modes that they have?"));
         nicklist.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN,
                 "nicklist", "sortByCase", "Sort nicklist by case",
                 "Sort nicknames in a case-sensitive manner?"));
 
         return nicklist;
     }
 
     /**
      * Adds a top level window to the window list.
      *
      * @param source New window
      */
     protected void addTopLevelWindow(final java.awt.Window source) {
         synchronized (windows) {
             windows.add(source);
         }
     }
 
     /**
      * Deletes a top level window to the window list.
      *
      * @param source Old window
      */
     protected void delTopLevelWindow(final java.awt.Window source) {
         synchronized (windows) {
             windows.remove(source);
         }
     }
 
     /**
      * Returns a list of top level windows.
      *
      * @return Top level window list
      */
     public List<java.awt.Window> getTopLevelWindows() {
         synchronized (windows) {
             return windows;
         }
     }
 
     /**
      * Returns an instance of SwingController. This method is exported for use
      * in other plugins.
      *
      * @return A reference to this SwingController.
      */
     public UIController getController() {
         return this;
     }
 
     /**
      * Adds the specified menu item to the named parent menu, creating the
      * parent menu if required.
      *
      * @param parentMenu Parent menu name
      * @param menuItem Menu item to add
      */
     public void addMenuItem(final String parentMenu, final JMenuItem menuItem) {
         me.getJMenuBar().addMenuItem(parentMenu, menuItem);
     }
 
     /** {@inheritDoc} */
     @Override
     public void requestWindowFocus(final Window window) {
         if (window instanceof TextFrame) {
             getMainFrame().setActiveFrame((TextFrame) window);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void requestWindowFocus(final FrameContainer container) {
         requestWindowFocus(getWindowFactory().getSwingWindow(container));
     }
 
 }
