 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.ChannelCopyAction;
 import com.dmdirc.addons.ui_swing.actions.CommandAction;
 import com.dmdirc.addons.ui_swing.actions.HyperlinkCopyAction;
 import com.dmdirc.addons.ui_swing.actions.InputFieldCopyAction;
 import com.dmdirc.addons.ui_swing.actions.NicknameCopyAction;
 import com.dmdirc.addons.ui_swing.actions.SearchAction;
 import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
 import com.dmdirc.addons.ui_swing.components.SwingSearchBar;
 import com.dmdirc.addons.ui_swing.textpane.ClickTypeValue;
 import com.dmdirc.addons.ui_swing.textpane.MouseEventType;
 import com.dmdirc.addons.ui_swing.textpane.TextPane;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneCopyAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPanePageDownAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPanePageUpAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneHomeAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneEndAction;
 import com.dmdirc.addons.ui_swing.textpane.TextPaneListener;
 import com.dmdirc.commandparser.PopupManager;
 import com.dmdirc.commandparser.PopupMenu;
 import com.dmdirc.commandparser.PopupMenuItem;
 import com.dmdirc.commandparser.PopupType;
 import com.dmdirc.commandparser.parsers.GlobalCommandParser;
 import com.dmdirc.util.StringTranscoder;
 import com.dmdirc.config.ConfigManager;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.interfaces.FrameInfoListener;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.parser.common.ChannelJoinRequest;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.core.util.URLHandler;
 import com.dmdirc.ui.interfaces.InputWindow;
 import com.dmdirc.ui.interfaces.Window;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyVetoException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
 import java.nio.charset.UnsupportedCharsetException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JInternalFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 import javax.swing.plaf.basic.BasicInternalFrameUI;
 import javax.swing.plaf.synth.SynthLookAndFeel;
 
 import net.miginfocom.layout.PlatformDefaults;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Implements a generic (internal) frame.
  */
 public abstract class TextFrame extends JInternalFrame implements Window,
         PropertyChangeListener, InternalFrameListener, ConfigChangeListener,
         FrameInfoListener, TextPaneListener {
 
     /** Logger to use. */
     private static final java.util.logging.Logger LOGGER =
             java.util.logging.Logger.getLogger(TextFrame.class.getName());
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 5;
     /** The channel object that owns this frame. */
     protected final FrameContainer frameParent;
     /** Frame output pane. */
     private TextPane textPane;
     /** search bar. */
     private SwingSearchBar searchBar;
     /** String transcoder. */
     private StringTranscoder transcoder;
     /** Are we closing? */
     private boolean closing = false;
     /** Input window for popup commands. */
     private Window inputWindow;
     /** Swing controller. */
     private SwingController controller;
     /** Are we maximising/restoring? */
     private AtomicBoolean maximiseRestoreInProgress = new AtomicBoolean(false);
     /** Content pane. */
     private final JPanel panel;
 
     /**
      * Creates a new instance of Frame.
      *
      * @param owner FrameContainer owning this frame.
      * @param controller Swing controller
      */
     public TextFrame(final FrameContainer owner,
             final SwingController controller) {
         super();
         this.controller = controller;
         frameParent = owner;
 
         final ConfigManager config = owner.getConfigManager();
 
         setFrameIcon(IconManager.getIconManager().getIcon(owner.getIcon()));
         setTitle(frameParent.getTitle());
 
         owner.addFrameInfoListener(this);
 
         try {
             transcoder = new StringTranscoder(Charset.forName(
                     config.getOption("channel", "encoding")));
         } catch (UnsupportedCharsetException ex) {
             transcoder = new StringTranscoder(Charset.forName("UTF-8"));
         } catch (IllegalCharsetNameException ex) {
             transcoder = new StringTranscoder(Charset.forName("UTF-8"));
         } catch (IllegalArgumentException ex) {
             transcoder = new StringTranscoder(Charset.forName("UTF-8"));
         }
 
         inputWindow = this;
        while (!(inputWindow instanceof InputWindow) && inputWindow != null
                && inputWindow.getContainer().getParent() != null) {
             inputWindow = inputWindow.getContainer().getParent().getFrame();
         }
 
         initComponents();
         setMaximizable(true);
         setClosable(true);
         setResizable(true);
         setIconifiable(true);
         setFocusable(true);
         setPreferredSize(new Dimension(controller.getMainFrame().getWidth() / 2, controller.
                 getMainFrame().getHeight() / 3));
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
         addPropertyChangeListener("UI", this);
         addInternalFrameListener(this);
         getTextPane().addTextPaneListener(this);
 
         getTextPane().setBackground(config.getOptionColour("ui",
                 "backgroundcolour"));
         getTextPane().setForeground(config.getOptionColour("ui",
                 "foregroundcolour"));
 
         config.addChangeListener("ui", "foregroundcolour", this);
         config.addChangeListener("ui", "backgroundcolour", this);
         config.addChangeListener("ui", "frameBufferSize", this);
 
         addPropertyChangeListener("maximum", this);
 
         panel = new JPanel(new MigLayout("fill"));
         super.getContentPane().add(panel);
     }
 
     /** {@inheritDoc} */
     @Override
     public Container getContentPane() {
         return panel;
     }
 
     /**
      * Returns this text frames swing controller.
      * 
      * @return Swing controller
      */
     public SwingController getController() {
         return controller;
     }
 
     /** {@inheritDoc} */
     @Override
     @Deprecated
     public void setTitle(final String title) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 TextFrame.super.setTitle(title);
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void setVisible(final boolean isVisible) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 if (isVisible() && isVisible) {
                     open();
                 } else {
                     TextFrame.super.setVisible(isVisible);
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void open() {
         final boolean pref = frameParent.getConfigManager().getOptionBool("ui",
                 "maximisewindows");
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 if (!isVisible()) {
                     TextFrame.super.setVisible(true);
                 }
                 try {
                     if (pref || controller.getMainFrame().getMaximised()) {
                         if (!isMaximum()) {
                             setMaximum(true);
                         }
                     }
                 } catch (PropertyVetoException ex) {
                     //Ignore
                 }
                 try {
                     if (!isSelected()) {
                         setSelected(true);
                     }
                 } catch (PropertyVetoException ex) {
                     //Ignore
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void activateFrame() {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     if (isIcon()) {
                         setIcon(false);
                     }
                     if (!isVisible()) {
                         TextFrame.super.setVisible(true);
                     }
                     if (!isSelected()) {
                         setSelected(true);
                     }
                 } catch (PropertyVetoException ex) {
                     //Ignore
                 }
             }
         });
     }
 
     /** Closes this frame. */
     @Override
     public void close() {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 if (closing) {
                     return;
                 }
 
                 closing = true;
 
                 try {
                     if (!isClosed()) {
                         setClosed(true);
                     }
                 } catch (PropertyVetoException ex) {
                     Logger.userError(ErrorLevel.LOW, "Unable to close frame");
                 }
 
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void minimise() {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     if (!isIcon()) {
                         setIcon(true);
                     }
                 } catch (PropertyVetoException ex) {
                     Logger.userError(ErrorLevel.LOW, "Unable to minimise frame");
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void maximise() {
         LOGGER.finest("maximise(): About to invokeAndWait");
 
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 LOGGER.finest("maximise(): Running");
 
                 try {
                     if (isMaximum()) {
                         return;
                     }
 
                     maximiseRestoreInProgress.set(true);
                     LOGGER.finest("maximise(): About to set icon");
                     if (isIcon()) {
                         setIcon(false);
                     }
                     LOGGER.finest("maximise(): About to set maximum");
                     setMaximum(true);
                     LOGGER.finest("maximise(): Done?");
                 } catch (PropertyVetoException ex) {
                     Logger.userError(ErrorLevel.LOW, "Unable to minimise frame");
                 }
                 maximiseRestoreInProgress.set(false);
 
                 LOGGER.finest("maximise(): Done running");
             }
         });
         LOGGER.finest("maximise(): Done");
     }
 
     /** {@inheritDoc} */
     @Override
     public void restore() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 try {
                     if (!isMaximum()) {
                         return;
                     }
 
                     maximiseRestoreInProgress.set(true);
                     if (isIcon()) {
                         setIcon(false);
                     }
                     setMaximum(false);
                 } catch (PropertyVetoException ex) {
                     Logger.userError(ErrorLevel.LOW, "Unable to minimise frame");
                 }
                 maximiseRestoreInProgress.set(false);
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void toggleMaximise() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 if (isMaximum()) {
                     restore();
                 } else {
                     maximise();
                 }
             }
         });
     }
 
     /**
      * {@inheritDoc}
      * 
      * @deprecated Use corresponding methods in {@link FrameContainer} instead
      */
     @Override
     @Deprecated
     public final void addLine(final String line, final boolean timestamp) {
         frameParent.addLine(line, timestamp);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @deprecated Use corresponding methods in {@link FrameContainer} instead
      */
     @Override
     @Deprecated
     public final void addLine(final String messageType, final Object... args) {
         frameParent.addLine(messageType, args);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @deprecated Use corresponding methods in {@link FrameContainer} instead
      */
     @Override
     @Deprecated
     public final void addLine(final StringBuffer messageType, final Object... args) {
         frameParent.addLine(messageType, args);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @deprecated Call {@link IRCDocument#clear()} via {@link FrameContainer#getDocument()}
      */
     @Override
     @Deprecated
     public final void clear() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 getTextPane().clear();
             }
         });
     }
 
     /**
      * Initialises the components for this frame.
      */
     private void initComponents() {
         setTextPane(new TextPane(this));
 
         searchBar = new SwingSearchBar(this, controller.getMainFrame());
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
 
     /**
      * Removes and reinserts the border of an internal frame on maximising.
      * {@inheritDoc}
      * 
      * @param event Property change event
      */
     @Override
     public final void propertyChange(final PropertyChangeEvent event) {
         LOGGER.finer("Property change: name: " + event.getPropertyName()
                 + " value: " + event.getOldValue() + "->" + event.getNewValue());
         if ("UI".equals(event.getPropertyName())) {
             if (isMaximum()) {
                 hideTitlebar();
             }
         } else {
             if ((Boolean) event.getNewValue()) {
                 hideTitlebar();
             } else {
                 showTitlebar();
             }
         }
         LOGGER.finest("Done property change");
     }
 
     /** Hides the titlebar for this frame. */
     private void hideTitlebar() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                 setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                 ((BasicInternalFrameUI) getUI()).setNorthPane(null);
             }
         });
     }
 
     /** Shows the titlebar for this frame. */
     private void showTitlebar() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 final Class<?> c;
                 Object temp = null;
                 Constructor<?> constructor;
 
                 final String componentUI = (String) UIManager.get(
                         "InternalFrameUI");
 
                 if ("javax.swing.plaf.synth.SynthLookAndFeel".equals(componentUI)) {
                     temp = SynthLookAndFeel.createUI(TextFrame.this);
                 } else {
                     try {
                         c = getClass().getClassLoader().loadClass(componentUI);
                         constructor =
                                 c.getConstructor(new Class[]{
                                     javax.swing.JInternalFrame.class});
                         temp =
                                 constructor.newInstance(new Object[]{
                                     TextFrame.this});
                     } catch (ClassNotFoundException ex) {
                         Logger.appError(ErrorLevel.MEDIUM,
                                 "Unable to readd titlebar",
                                 ex);
                     } catch (NoSuchMethodException ex) {
                         Logger.appError(ErrorLevel.MEDIUM,
                                 "Unable to readd titlebar",
                                 ex);
                     } catch (InstantiationException ex) {
                         Logger.appError(ErrorLevel.MEDIUM,
                                 "Unable to readd titlebar",
                                 ex);
                     } catch (IllegalAccessException ex) {
                         Logger.appError(ErrorLevel.MEDIUM,
                                 "Unable to readd titlebar",
                                 ex);
                     } catch (InvocationTargetException ex) {
                         Logger.appError(ErrorLevel.MEDIUM,
                                 "Unable to readd titlebar",
                                 ex);
                     }
 
                 }
 
                 setBorder(UIManager.getBorder("InternalFrame.border"));
                 if (temp == null) {
                     temp = new BasicInternalFrameUI(TextFrame.this);
                 }
 
                 setUI((BasicInternalFrameUI) temp);
                 final int size = (int) PlatformDefaults.getPanelInsets(0).
                         getValue();
                 panel.setBorder(BorderFactory.createEmptyBorder(size, size,
                         size, size));
             }
         });
     }
 
     /**
      * Not needed for this class. {@inheritDoc}
      * 
      * @param event Internal frame event
      */
     @Override
     public void internalFrameOpened(final InternalFrameEvent event) {
         new LoggingSwingWorker() {
 
             /** {@inheritDoc} */
             @Override
             protected Object doInBackground() throws Exception {
                 frameParent.windowOpened();
                 return null;
             }
         }.execute();
     }
 
     /**
      * Not needed for this class. {@inheritDoc}
      * 
      * @param event Internal frame event
      */
     @Override
     public void internalFrameClosing(final InternalFrameEvent event) {
         new LoggingSwingWorker() {
 
             /** {@inheritDoc} */
             @Override
             protected Object doInBackground() throws Exception {
                 frameParent.windowClosing();
                 return null;
             }
         }.execute();
     }
 
     /**
      * Not needed for this class. {@inheritDoc}
      * 
      * @param event Internal frame event
      */
     @Override
     public void internalFrameClosed(final InternalFrameEvent event) {
         //Ignore.
     }
 
     /**
      * Makes the internal frame invisible. {@inheritDoc}
      * 
      * @param event Internal frame event
      */
     @Override
     public void internalFrameIconified(final InternalFrameEvent event) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 event.getInternalFrame().setVisible(false);
             }
         });
     }
 
     /**
      * Not needed for this class. {@inheritDoc}
      * 
      * @param event Internal frame event
      */
     @Override
     public void internalFrameDeiconified(final InternalFrameEvent event) {
         //Ignore.
     }
 
     /**
      * Activates the input field on frame focus. {@inheritDoc}
      * 
      * @param event Internal frame event
      */
     @Override
     public void internalFrameActivated(final InternalFrameEvent event) {
         LOGGER.finer(getName() + ": internalFrameActivated()");
 
         if (maximiseRestoreInProgress.get()) {
             return;
         }
         new LoggingSwingWorker() {
 
             /** {@inheritDoc} */
             @Override
             protected Object doInBackground() throws Exception {
                 LOGGER.finer(getName()
                         + ": internalFrameActivated(): doInBackground");
                 frameParent.windowActivated();
                 return null;
             }
         }.execute();
     }
 
     /**
      * Not needed for this class. {@inheritDoc}
      * 
      * @param event Internal frame event
      */
     @Override
     public void internalFrameDeactivated(final InternalFrameEvent event) {
         if (maximiseRestoreInProgress.get()) {
             return;
         }
         new LoggingSwingWorker() {
 
             /** {@inheritDoc} */
             @Override
             protected Object doInBackground() throws Exception {
                 frameParent.windowDeactivated();
                 return null;
             }
         }.execute();
     }
 
     /** {@inheritDoc} */
     @Override
     public FrameContainer getContainer() {
         return frameParent;
     }
 
     /** {@inheritDoc} */
     @Override
     public ConfigManager getConfigManager() {
         return getContainer().getConfigManager();
     }
 
     /**
      * Returns the text pane for this frame.
      *
      * @return Text pane for this frame
      */
     public final TextPane getTextPane() {
         return textPane;
     }
 
     /**
      * {@inheritDoc} 
      *
      * @deprecated Use {@link FrameContainer#getTranscoder()} instead
      */
     @Override
     @Deprecated
     public StringTranscoder getTranscoder() {
         return transcoder;
     }
 
     /** {@inheritDoc} */
     @Override
     public final String getName() {
         if (frameParent == null) {
             return "";
         }
 
         return frameParent.toString();
     }
 
     /**
      * Sets the frames text pane.
      *
      * @param newTextPane new text pane to use
      */
     protected final void setTextPane(final TextPane newTextPane) {
         this.textPane = newTextPane;
     }
 
     /** {@inheritDoc} */
     @Override
     public void mouseClicked(final ClickTypeValue clicktype,
             final MouseEventType eventType, final MouseEvent event) {
         if (event.isPopupTrigger()) {
             showPopupMenuInternal(clicktype, event.getPoint()   );
         }
         if (eventType == MouseEventType.CLICK
                 && event.getButton() == MouseEvent.BUTTON1) {
             switch (clicktype.getType()) {
                 case CHANNEL:
                     if (frameParent.getServer() != null && ActionManager.
                             processEvent(CoreActionType.LINK_CHANNEL_CLICKED,
                             null, this, clicktype.getValue())) {
                         frameParent.getServer().join(
                                 new ChannelJoinRequest(clicktype.getValue()));
                     }
                     break;
                 case HYPERLINK:
                     if (ActionManager.processEvent(
                             CoreActionType.LINK_URL_CLICKED, null, this,
                             clicktype.getValue())) {
                         URLHandler.getURLHander().launchApp(clicktype.getValue());
                     }
                     break;
                 case NICKNAME:
                     if (frameParent.getServer() != null && ActionManager.
                             processEvent(CoreActionType.LINK_NICKNAME_CLICKED,
                             null, this, clicktype.getValue())) {
                         getContainer().getServer().getQuery(clicktype
                                 .getValue()).activateFrame();
                     }
                     break;
                 default:
                     break;
             }
 
         }
     }
 
     /**
      * What popup type should be used for popup menus for nicknames
      * 
      * @return Appropriate popuptype for this frame
      */
     public abstract PopupType getNicknamePopupType();
 
     /**
      * What popup type should be used for popup menus for channels
      * 
      * @return Appropriate popuptype for this frame
      */
     public abstract PopupType getChannelPopupType();
 
     /**
      * What popup type should be used for popup menus for hyperlinks
      * 
      * @return Appropriate popuptype for this frame
      */
     public abstract PopupType getHyperlinkPopupType();
 
     /**
      * What popup type should be used for popup menus for normal clicks
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
      * Shows a popup menu at the specified point for the specified click type
      * 
      * @param type ClickType Click type
      * @param point Point Point of the click
      * @param argument Word under the click
      */
     private void showPopupMenuInternal(final ClickTypeValue type,
             final Point point) {
         final JPopupMenu popupMenu;
 
         switch (type.getType()) {
             case CHANNEL:
                 popupMenu = getPopupMenu(getChannelPopupType(), type.getValue());
                 popupMenu.add(new ChannelCopyAction(type.getValue()));
                 if (popupMenu.getComponentCount() > 1) {
                     popupMenu.addSeparator();
                 }
 
                 break;
             case HYPERLINK:
                 popupMenu = getPopupMenu(getHyperlinkPopupType(), type.getValue());
                 popupMenu.add(new HyperlinkCopyAction(type.getValue()));
                 if (popupMenu.getComponentCount() > 1) {
                     popupMenu.addSeparator();
                 }
 
                 break;
             case NICKNAME:
                 popupMenu = getPopupMenu(getNicknamePopupType(), type.getValue());
                 if (popupMenu.getComponentCount() > 0) {
                     popupMenu.addSeparator();
                 }
 
                 popupMenu.add(new NicknameCopyAction(type.getValue()));
                 break;
             default:
                 popupMenu = getPopupMenu(null, type.getValue());
                 break;
         }
 
         popupMenu.add(new TextPaneCopyAction(getTextPane()));
 
         addCustomPopupItems(popupMenu);
 
         popupMenu.show(this, (int) point.getX(), (int) point.getY());
     }
 
     /**
      * Shows a popup menu at the specified point for the specified click type
      * 
      * @param type ClickType Click type
      * @param point Point Point of the click (Must be screen coords)
      */
     public void showPopupMenu(final ClickTypeValue type,
             final Point point) {
         SwingUtilities.convertPointFromScreen(point, this);
         showPopupMenuInternal(type, point);
     }
 
     /**
      * Builds a popup menu of a specified type
      * 
      * @param type type of menu to build
      * @param arguments Arguments for the command
      * 
      * @return PopupMenu
      */
     public JPopupMenu getPopupMenu(
             final PopupType type,
             final Object... arguments) {
         JPopupMenu popupMenu = new JPopupMenu();
 
         if (type != null) {
             popupMenu = (JPopupMenu) populatePopupMenu(popupMenu,
                     PopupManager.getMenu(type, getConfigManager()),
                     arguments);
         }
 
         return popupMenu;
     }
 
     /**
      * Populates the specified popupmenu
      * 
      * @param menu Menu component
      * @param popup Popup to get info from
      * @param arguments Arguments for the command
      * 
      * @return Populated popup
      */
     private JComponent populatePopupMenu(final JComponent menu,
             final PopupMenu popup,
             final Object... arguments) {
         for (PopupMenuItem menuItem : popup.getItems()) {
             if (menuItem.isDivider()) {
                 menu.add(new JSeparator());
             } else if (menuItem.isSubMenu()) {
                 menu.add(populatePopupMenu(new JMenu(menuItem.getName()),
                         menuItem.getSubMenu(), arguments));
             } else {
                 menu.add(new JMenuItem(new CommandAction(inputWindow == null ? GlobalCommandParser.
                         getGlobalCommandParser()
                         : ((InputWindow) inputWindow).getCommandParser(),
                         (InputWindow) inputWindow, menuItem.getName(),
                         menuItem.getCommand(arguments))));
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
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain,
             final String key) {
         if (getConfigManager() == null) {
             return;
         }
 
         if ("ui".equals(domain)) {
             if ("foregroundcolour".equals(key) && getTextPane() != null) {
                 getTextPane().setForeground(getConfigManager().
                         getOptionColour("ui", "foregroundcolour"));
             } else if ("backgroundcolour".equals(key) && getTextPane()
                     != null) {
                 getTextPane().setBackground(getConfigManager().
                         getOptionColour("ui", "backgroundcolour"));
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void iconChanged(final FrameContainer window, final String icon) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 setFrameIcon(IconManager.getIconManager().getIcon(icon));
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void nameChanged(final FrameContainer window, final String name) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public void titleChanged(final FrameContainer window, final String title) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 // TODO: Can be changed back to setTitle once the deprecated
                 // method is removed
                 TextFrame.super.setTitle(title);
             }
         });
     }
 }
