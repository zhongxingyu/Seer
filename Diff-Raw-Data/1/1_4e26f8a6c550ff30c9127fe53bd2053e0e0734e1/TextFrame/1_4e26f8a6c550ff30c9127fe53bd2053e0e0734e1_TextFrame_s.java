 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.components.frames;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.ChannelCopyAction;
 import com.dmdirc.addons.ui_swing.actions.CommandAction;
 import com.dmdirc.addons.ui_swing.actions.HyperlinkCopyAction;
 import com.dmdirc.addons.ui_swing.actions.InputFieldCopyAction;
 import com.dmdirc.addons.ui_swing.actions.NicknameCopyAction;
 import com.dmdirc.addons.ui_swing.actions.SearchAction;
 import com.dmdirc.addons.ui_swing.components.SwingSearchBar;
 import com.dmdirc.addons.ui_swing.dialogs.paste.PasteDialogFactory;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
 import com.dmdirc.addons.ui_swing.textpane.ClickTypeValue;
 import com.dmdirc.addons.ui_swing.textpane.MouseEventType;
 import com.dmdirc.addons.ui_swing.textpane.TextPane;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneControlCodeCopyAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneCopyAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneEndAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneFactory;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneHomeAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneListener;
 import com.dmdirc.addons.ui_swing.textpane.TextPanePageDownAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPanePageUpAction;
 import com.dmdirc.commandparser.PopupManager;
 import com.dmdirc.commandparser.PopupMenu;
 import com.dmdirc.commandparser.PopupMenuItem;
 import com.dmdirc.commandparser.PopupType;
 import com.dmdirc.commandparser.parsers.CommandParser;
 import com.dmdirc.events.LinkChannelClickedEvent;
 import com.dmdirc.events.LinkNicknameClickedEvent;
 import com.dmdirc.events.LinkUrlClickedEvent;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.interfaces.FrameCloseListener;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.plugins.PluginManager;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.messages.ColourManager;
 
 import com.google.common.eventbus.EventBus;
 
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.datatransfer.Clipboard;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Implements a generic (internal) frame.
  */
 public abstract class TextFrame extends JPanel implements com.dmdirc.interfaces.ui.Window,
         ConfigChangeListener, TextPaneListener, FrameCloseListener {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 5;
     /** The channel object that owns this frame. */
     protected final FrameContainer frameParent;
     /** Frame output pane. */
     private TextPane textPane;
     /** search bar. */
     private SwingSearchBar searchBar;
     /** Command parser for popup commands. */
     private final CommandParser commandParser;
     /** Manager used to activate/deactivate windows. */
     private final ActiveFrameManager activeFrameManager;
     /** Manager to use for building popups. */
     private final PopupManager popupManager;
     /** Bus to despatch events on. */
     private final EventBus eventBus;
     /** Clipboard to copy and paste from. */
     private final Clipboard clipboard;
     /** Boolean to determine if this frame should be popped out of main client. */
     private boolean popout;
     /** DesktopWindowFrame to use for this TextFrame if it is to be popped out of the client. */
     private DesktopWindowFrame popoutFrame;
     /** Desktop place holder object used if this frame is popped out. */
     private DesktopPlaceHolderFrame popoutPlaceholder;
     /** Icon manager to retrieve icons from. */
     private final IconManager iconManager;
 
     /**
      * Creates a new instance of Frame.
      *
      * @param owner         FrameContainer owning this frame.
      * @param commandParser The command parser to use for this frame.
      * @param deps          Collection of TextPane dependencies.
      */
     protected TextFrame(
             final FrameContainer owner,
             final CommandParser commandParser,
             final TextFrameDependencies deps) {
         this.activeFrameManager = deps.activeFrameManager;
         this.popupManager = deps.popupManager;
         this.frameParent = owner;
         this.iconManager = deps.iconManager;
         this.eventBus = deps.eventBus;
         this.commandParser = commandParser;
         this.clipboard = deps.clipboard;
 
         final AggregateConfigProvider config = owner.getConfigManager();
 
         owner.addCloseListener(this);
        owner.setTitle(frameParent.getTitle());
 
         initComponents(deps.textPaneFactory);
         setFocusable(true);
 
         getTextPane().addTextPaneListener(this);
 
         config.addChangeListener("ui", "foregroundcolour", this);
         config.addChangeListener("ui", "backgroundcolour", this);
         config.addChangeListener("ui", "frameBufferSize", this);
         updateColours();
 
         setLayout(new MigLayout("fill"));
     }
 
     /**
      * Determines if this frame should be popped out of the client or not. Once this is set to true
      * it will pop out of the client as a free floating Desktop window.
      *
      * If this is set to false then the desktop window for this frame is disposed of and this frame
      * is returned to the client.
      *
      * @param popout Should this frame pop out?
      */
     public void setPopout(final boolean popout) {
         this.popout = popout;
         if (popout) {
             popoutPlaceholder = new DesktopPlaceHolderFrame();
             popoutFrame = new DesktopWindowFrame(this);
             popoutFrame.display();
         } else if (popoutFrame != null) {
             popoutPlaceholder = null;
             popoutFrame.dispose();
             popoutFrame = null;
         }
         // Call setActiveFrame again so the contents of the frame manager are updated.
         if (equals(activeFrameManager.getActiveFrame())) {
             activeFrameManager.setActiveFrame(this);
         }
     }
 
     /**
      * Returns the frame for the free floating desktop window associated with this TextFrame. If one
      * does not exist then null is returned.
      *
      * @return Desktop window frame or null if does not exist
      */
     public DesktopWindowFrame getPopoutFrame() {
         return popoutFrame;
     }
 
     /**
      * Checks if this frame should be popped out of the client or not. Returns our place holder
      * frame if it is to be used or this TextFrame if it is not to be popped out.
      *
      * @return JPanel to use by the client in the window pane
      */
     public JPanel getDisplayFrame() {
         if (popout) {
             return popoutPlaceholder;
         } else {
             return this;
         }
     }
 
     /**
      * Called when the frame has been selected in the UI.
      */
     public void activateFrame() {
         UIUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 frameParent.clearNotification();
             }
         });
     }
 
     /**
      * Initialises the components for this frame.
      */
     private void initComponents(final TextPaneFactory textPaneFactory) {
         setTextPane(textPaneFactory.getTextPane(this));
 
         searchBar = new SwingSearchBar(this, iconManager);
         searchBar.setVisible(false);
 
         getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
                         "pageUpAction");
 
         getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
                         "pageDownAction");
 
         getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "searchAction");
 
         getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                 UIUtilities.getCtrlDownMask()), "searchAction");
 
         getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
                                 UIUtilities.getCtrlDownMask()), "homeAction");
 
         getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
                 put(KeyStroke.getKeyStroke(KeyEvent.VK_END,
                                 UIUtilities.getCtrlDownMask()), "endAction");
 
         getSearchBar().getTextField().getInputMap().put(KeyStroke.getKeyStroke(
                 KeyEvent.VK_C, UIUtilities.getCtrlMask()), "textpaneCopy");
         getSearchBar().getTextField().getInputMap().put(KeyStroke.getKeyStroke(
                 KeyEvent.VK_C, UIUtilities.getCtrlMask()
                 & KeyEvent.SHIFT_DOWN_MASK), "textpaneCopy");
         getSearchBar().getTextField().getActionMap().put("textpaneCopy",
                 new InputFieldCopyAction(getTextPane(),
                         getSearchBar().getTextField()));
 
         getActionMap().put("pageUpAction",
                 new TextPanePageUpAction(getTextPane()));
         getActionMap().put("pageDownAction",
                 new TextPanePageDownAction(getTextPane()));
         getActionMap().put("searchAction", new SearchAction(searchBar));
         getActionMap().put("homeAction", new TextPaneHomeAction(getTextPane()));
         getActionMap().put("endAction", new TextPaneEndAction(getTextPane()));
     }
 
     @Override
     public FrameContainer getContainer() {
         return frameParent;
     }
 
     /**
      * Returns the text pane for this frame.
      *
      * @return Text pane for this frame
      */
     public final TextPane getTextPane() {
         return textPane;
     }
 
     @Override
     public final String getName() {
         if (frameParent == null) {
             return "";
         }
 
         return frameParent.getName();
     }
 
     /**
      * Sets the frames text pane.
      *
      * @param newTextPane new text pane to use
      */
     protected final void setTextPane(final TextPane newTextPane) {
         textPane = newTextPane;
     }
 
     @Override
     public void mouseClicked(final ClickTypeValue clicktype,
             final MouseEventType eventType, final MouseEvent event) {
         if (event.isPopupTrigger()) {
             showPopupMenuInternal(clicktype, event.getPoint());
         }
         if (eventType == MouseEventType.CLICK && event.getButton() == MouseEvent.BUTTON1) {
             switch (clicktype.getType()) {
                 case CHANNEL:
                     eventBus.post(new LinkChannelClickedEvent(this, clicktype.getValue()));
                     break;
                 case NICKNAME:
                     eventBus.post(new LinkNicknameClickedEvent(this, clicktype.getValue()));
                     break;
                 case HYPERLINK:
                     eventBus.post(new LinkUrlClickedEvent(this, clicktype.getValue()));
                     break;
             }
         }
     }
 
     /**
      * What popup type should be used for popup menus for nicknames.
      *
      * @return Appropriate popuptype for this frame
      */
     public abstract PopupType getNicknamePopupType();
 
     /**
      * What popup type should be used for popup menus for channels.
      *
      * @return Appropriate popuptype for this frame
      */
     public abstract PopupType getChannelPopupType();
 
     /**
      * What popup type should be used for popup menus for hyperlinks.
      *
      * @return Appropriate popuptype for this frame
      */
     public abstract PopupType getHyperlinkPopupType();
 
     /**
      * What popup type should be used for popup menus for normal clicks.
      *
      * @return Appropriate popuptype for this frame
      */
     public abstract PopupType getNormalPopupType();
 
     /**
      * A method called to add custom popup items.
      *
      * @param popupMenu Popup menu to add popup items to
      */
     public abstract void addCustomPopupItems(final JPopupMenu popupMenu);
 
     /**
      * Shows a popup menu at the specified point for the specified click type.
      *
      * @param type  ClickType Click type
      * @param point Point Point of the click
      */
     private void showPopupMenuInternal(final ClickTypeValue type,
             final Point point) {
         final JPopupMenu popupMenu;
 
         final String[] parts = type.getValue().split("\n");
         final Object[][] arguments = new Object[parts.length][1];
 
         int i = 0;
         for (final String part : parts) {
             arguments[i++][0] = part;
         }
 
         switch (type.getType()) {
             case CHANNEL:
                 popupMenu = getPopupMenu(getChannelPopupType(), arguments);
                 popupMenu.add(new ChannelCopyAction(clipboard, type.getValue()));
                 if (popupMenu.getComponentCount() > 1) {
                     popupMenu.addSeparator();
                 }
 
                 break;
             case HYPERLINK:
                 popupMenu = getPopupMenu(getHyperlinkPopupType(), arguments);
                 popupMenu.add(new HyperlinkCopyAction(clipboard, type.getValue()));
                 if (popupMenu.getComponentCount() > 1) {
                     popupMenu.addSeparator();
                 }
 
                 break;
             case NICKNAME:
                 popupMenu = getPopupMenu(getNicknamePopupType(), arguments);
                 if (popupMenu.getComponentCount() > 0) {
                     popupMenu.addSeparator();
                 }
 
                 popupMenu.add(new NicknameCopyAction(clipboard, type.getValue()));
                 break;
             default:
                 popupMenu = getPopupMenu(null, arguments);
                 break;
         }
 
         popupMenu.add(new TextPaneCopyAction(getTextPane()));
         popupMenu.add(new TextPaneControlCodeCopyAction(textPane));
 
         addCustomPopupItems(popupMenu);
 
         popupMenu.show(this, (int) point.getX(), (int) point.getY());
     }
 
     /**
      * Shows a popup menu at the specified point for the specified click type.
      *
      * @param type  ClickType Click type
      * @param point Point Point of the click (Must be screen coords)
      */
     public void showPopupMenu(final ClickTypeValue type,
             final Point point) {
         SwingUtilities.convertPointFromScreen(point, this);
         showPopupMenuInternal(type, point);
     }
 
     /**
      * Builds a popup menu of a specified type.
      *
      * @param type      type of menu to build
      * @param arguments Arguments for the command
      *
      * @return PopupMenu
      */
     public JPopupMenu getPopupMenu(final PopupType type,
             final Object[][] arguments) {
         JPopupMenu popupMenu = new JPopupMenu();
 
         if (type != null) {
             popupMenu = (JPopupMenu) populatePopupMenu(popupMenu,
                     popupManager.getMenu(type, getContainer().getConfigManager()), arguments);
         }
 
         return popupMenu;
     }
 
     /**
      * Populates the specified popupmenu.
      *
      * @param menu      Menu component
      * @param popup     Popup to get info from
      * @param arguments Arguments for the command
      *
      * @return Populated popup
      */
     private JComponent populatePopupMenu(final JComponent menu,
             final PopupMenu popup,
             final Object[][] arguments) {
         for (final PopupMenuItem menuItem : popup.getItems()) {
             if (menuItem.isDivider()) {
                 menu.add(new JSeparator());
             } else if (menuItem.isSubMenu()) {
                 menu.add(populatePopupMenu(new JMenu(menuItem.getName()),
                         menuItem.getSubMenu(), arguments));
             } else {
                 menu.add(new JMenuItem(new CommandAction(commandParser, this,
                         menuItem.getName(), menuItem.getCommand(arguments))));
             }
 
         }
         return menu;
     }
 
     /**
      * Gets the search bar.
      *
      * @return the frames search bar
      */
     public final SwingSearchBar getSearchBar() {
         return searchBar;
     }
 
     @Override
     public void configChanged(final String domain, final String key) {
         if (getContainer().getConfigManager() == null || getTextPane() == null) {
             return;
         }
 
         if ("ui".equals(domain) && ("foregroundcolour".equals(key)
                 || "backgroundcolour".equals(key))) {
             updateColours();
         }
     }
 
     @Override
     public void windowClosing(final FrameContainer window) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 setVisible(false);
                 getTextPane().close();
             }
         });
     }
 
     /**
      * Returns the IconManager for this frame.
      *
      * @return This frame's IconManager
      */
     public IconManager getIconManager() {
         return getContainer().getIconManager();
     }
 
     /**
      * Updates colour settings from their config values.
      */
     private void updateColours() {
         final ColourManager colourManager = new ColourManager(getContainer().getConfigManager());
         getTextPane().setForeground(UIUtilities.convertColour(
                 colourManager.getColourFromString(
                         getContainer().getConfigManager().getOptionString(
                                 "ui", "foregroundcolour"), null)));
         getTextPane().setBackground(UIUtilities.convertColour(
                 colourManager.getColourFromString(
                         getContainer().getConfigManager().getOptionString(
                                 "ui", "backgroundcolour"), null)));
     }
 
     /** Disposes of this window, removing any listeners. */
     public void dispose() {
         frameParent.getConfigManager().removeListener(this);
         frameParent.removeCloseListener(this);
     }
 
     /**
      * Bundle of dependencies required by {@link TextFrame}.
      *
      * <p>
      * Because of the number of dependencies and the amount of subclassing, collect the dependencies
      * together here so they can be easily modified without having to modify all subclasses.
      */
     public static class TextFrameDependencies {
 
         final TextPaneFactory textPaneFactory;
         final SwingController controller;
         final Provider<Window> mainWindow;
         final PopupManager popupManager;
         final EventBus eventBus;
         final AggregateConfigProvider globalConfig;
         final PasteDialogFactory pasteDialog;
         final PluginManager pluginManager;
         final IconManager iconManager;
         final ActiveFrameManager activeFrameManager;
         final Clipboard clipboard;
         final CommandController commandController;
 
         @Inject
         public TextFrameDependencies(
                 final TextPaneFactory textPaneFactory,
                 final SwingController controller,
                 @MainWindow final Provider<Window> mainWindow,
                 final PopupManager popupManager,
                 final EventBus eventBus,
                 final PasteDialogFactory pasteDialog,
                 final PluginManager pluginManager,
                 @GlobalConfig final IconManager iconManager,
                 @GlobalConfig final AggregateConfigProvider globalConfig,
                 final ActiveFrameManager activeFrameManager,
                 final Clipboard clipboard,
                 final CommandController commandController) {
             this.textPaneFactory = textPaneFactory;
             this.controller = controller;
             this.mainWindow = mainWindow;
             this.popupManager = popupManager;
             this.eventBus = eventBus;
             this.globalConfig = globalConfig;
             this.pasteDialog = pasteDialog;
             this.pluginManager = pluginManager;
             this.iconManager = iconManager;
             this.activeFrameManager = activeFrameManager;
             this.clipboard = clipboard;
             this.commandController = commandController;
         }
 
     }
 
 }
