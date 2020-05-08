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
 
 package com.dmdirc.addons.ui_swing.framemanager.buttonbar;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.FrameContainerComparator;
 import com.dmdirc.addons.ui_swing.SwingWindowFactory;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.CloseFrameContainerAction;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
 import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
 import com.dmdirc.addons.ui_swing.framemanager.FrameManager;
 import com.dmdirc.addons.ui_swing.framemanager.FramemanagerPosition;
 import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
 import com.dmdirc.events.FrameIconChangedEvent;
 import com.dmdirc.events.FrameNameChangedEvent;
 import com.dmdirc.interfaces.NotificationListener;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.interfaces.ui.Window;
 import com.dmdirc.ui.Colour;
 import com.dmdirc.ui.WindowManager;
 
 import com.google.common.base.Optional;
 
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 
 import net.miginfocom.layout.PlatformDefaults;
 import net.miginfocom.swing.MigLayout;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * The button bar manager is a grid of buttons that presents a manager similar to that used by mIRC.
  */
 public final class ButtonBar implements FrameManager, ActionListener,
         ComponentListener, Serializable, NotificationListener,
         MouseListener, ConfigChangeListener {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 3;
     /** The default number of buttons per row or column. */
     private static final int NUM_CELLS = 1;
     /** The default height of buttons. */
     private static final int BUTTON_HEIGHT = 25;
     /** A map of windows to the buttons we're using for them. */
     private final Map<Window, FrameToggleButton> buttons;
     /** The scrolling panel for our ButtonBar. */
     private final JScrollPane scrollPane;
     /** The panel used for our buttons. */
     private final ButtonPanel buttonPanel;
     /** The position of this frame manager. */
     private final FramemanagerPosition position;
     /** The default width of buttons. */
     private int buttonWidth = 0;
     /** The parent for the manager. */
     private JComponent parent;
     /** The currently selected frame. */
     private transient FrameContainer selected;
     /** Selected window. */
     private Window activeWindow;
     /** Sort root windows prefs setting. */
     private boolean sortRootWindows;
     /** Sort child windows prefs setting. */
     private boolean sortChildWindows;
     /** UI Window Factory. */
     private final SwingWindowFactory windowFactory;
     /** Window management. */
     private final WindowManager windowManager;
     /** Global configuration to read settings from. */
     private final AggregateConfigProvider globalConfig;
     /** Active frame manager. */
     private final ActiveFrameManager activeFrameManager;
 
     /**
      * Creates a new instance of ButtonBar.
      *
     * @param windowFactory      The factory to use to retrieve window information.
     * @param windowManager      The window manager to use to read window state.
     * @param globalConfig       Global configuration to read settings from.
      * @param activeFrameManager The active window manager
     * @param eventBus           The event bus to subscribe to events on
      */
     @Inject
     public ButtonBar(
             final SwingWindowFactory windowFactory,
             @GlobalConfig final AggregateConfigProvider globalConfig,
             final WindowManager windowManager,
             final ActiveFrameManager activeFrameManager,
             final DMDircMBassador eventBus) {
         this.windowFactory = windowFactory;
         this.globalConfig = globalConfig;
         this.windowManager = windowManager;
         this.activeFrameManager = activeFrameManager;
 
         eventBus.subscribe(this);
 
         scrollPane = new JScrollPane();
         scrollPane.setBorder(null);
         scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setMinimumSize(new Dimension(0, BUTTON_HEIGHT
                 + ((int) PlatformDefaults.getUnitValueX("related")
                 .getValue()) * 2));
 
         position = FramemanagerPosition.getPosition(
                 globalConfig.getOption("ui", "framemanagerPosition"));
 
         if (position.isHorizontal()) {
             buttonPanel = new ButtonPanel(activeFrameManager,
                     new MigLayout("ins rel, fill, flowx"), this);
         } else {
             buttonPanel = new ButtonPanel(activeFrameManager,
                     new MigLayout("ins rel, fill, flowy"), this);
         }
         scrollPane.getViewport().addMouseWheelListener(buttonPanel);
         scrollPane.getViewport().add(buttonPanel);
 
         buttons = Collections.synchronizedMap(new HashMap<Window, FrameToggleButton>());
         sortChildWindows = globalConfig.getOptionBool("ui", "sortchildwindows");
         sortRootWindows = globalConfig.getOptionBool("ui", "sortrootwindows");
 
         globalConfig.addChangeListener("ui", "sortrootwindows", this);
         globalConfig.addChangeListener("ui", "sortchildwindows", this);
     }
 
     /**
      * Retrieves button height.
      *
      * @return Button height
      *
      * @since 0.6.4
      */
     public int getButtonHeight() {
         return BUTTON_HEIGHT;
     }
 
     /**
      * Returns the button object of the current selected window.
      *
      * @return Button object for the current selected window
      */
     public FrameToggleButton getSelectedButton() {
         return getButton(selected);
     }
 
     @Override
     public void setParent(final JComponent parent) {
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 ButtonBar.this.parent = parent;
                 scrollPane.setSize(parent.getWidth(), parent.getHeight());
 
                 parent.setVisible(false);
                 parent.setLayout(new MigLayout("ins 0"));
                 parent.add(scrollPane);
                 parent.addComponentListener(ButtonBar.this);
                 ButtonBar.this.buttonWidth = position.isHorizontal()
                         ? 150 : (parent.getWidth() / NUM_CELLS);
                 initButtons(windowManager.getRootWindows());
 
                 final TextFrame activeFrame = activeFrameManager.getActiveFrame();
                 if (activeFrame != null) {
                     selectionChanged(activeFrame);
                 }
                 parent.setVisible(true);
             }
         });
     }
 
     /**
      * Initialises buttons for the currently available windows. This should only be called once when
      * this buttonbar is made active in the client. See {@link #setParent}. This method essentially
      * does nothing if the client is started with the buttonbar enabled.
      *
      * @param windowCollection Collection of windows {@link FrameContainer}
      *
      * @since 0.6.4
      */
     private void initButtons(
             final Collection<FrameContainer> windowCollection) {
         TextFrame window;
         TextFrame parentWindow;
         for (FrameContainer frame : windowCollection) {
             window = windowFactory.getSwingWindow(frame);
             parentWindow = windowFactory.getSwingWindow(frame.getParent().orNull());
             if (window != null) {
                 windowAdded(new SwingWindowAddedEvent(Optional.fromNullable(parentWindow), window));
             }
 
             if (!frame.getChildren().isEmpty()) {
                 final ArrayList<FrameContainer> childList = new ArrayList<>(frame.getChildren());
                 initButtons(childList);
             }
         }
     }
 
     /**
      * Retreives the button object associated with {@link FrameContainer}.
      *
      * @param frame FrameContainer to find associated button for
      *
      * @return {@link FrameToggleButton} object asociated with this FrameContainer. Returns null if
      *         none exist
      */
     public FrameToggleButton getButton(final FrameContainer frame) {
         final Window window = windowFactory.getSwingWindow(frame);
         if (buttons.containsKey(window)) {
             return buttons.get(window);
         }
         return null;
     }
 
     /**
      * Adds buttons for the collection of windows that is passed to it. This method also iterates
      * through any children for each item in the collection.
      *
      * This method has no boundaries as to how many generations it can iterate through.
      *
      * @param windowCollection Collection of windows {@link FrameContainer}
      *
      * @since 0.6.4
      */
     private void displayButtons(
             final Collection<FrameContainer> windowCollection) {
         FrameToggleButton button;
         for (FrameContainer window : windowCollection) {
             button = getButton(window);
             if (button != null) {
                 button.setPreferredSize(
                         new Dimension(buttonWidth, BUTTON_HEIGHT));
                 buttonPanel.add(button);
                 if (!window.getChildren().isEmpty()) {
                     final ArrayList<FrameContainer> childList
                             = new ArrayList<>(window.getChildren());
                     if (sortChildWindows) {
                         Collections.sort(childList,
                                 new FrameContainerComparator());
                     }
                     displayButtons(childList);
                 }
             }
         }
     }
 
     /**
      * Removes all buttons from the bar and readds them.
      */
     private void relayout() {
         buttonPanel.setVisible(false);
         buttonPanel.removeAll();
 
         final ArrayList<FrameContainer> windowList = new ArrayList<>(windowManager.getRootWindows());
         if (sortRootWindows) {
             Collections.sort(windowList, new FrameContainerComparator());
         }
 
         displayButtons(windowList);
         buttonPanel.setVisible(true);
     }
 
     /**
      * Adds a button to the button array with the details from the specified container.
      *
      * @param source The Container to get title/icon info from
      */
     private void addButton(final Window source) {
         final FrameToggleButton button = new FrameToggleButton(
                 source.getContainer().getName(),
                 source.getContainer().getIconManager().getIcon(source.getContainer().getIcon()),
                 (TextFrame) source, source.getContainer());
         button.addActionListener(this);
         button.addMouseListener(this);
         button.setHorizontalAlignment(SwingConstants.LEFT);
         button.setMinimumSize(new Dimension(0, BUTTON_HEIGHT));
         button.setMargin(new Insets(0, 0, 0, 0));
         buttons.put(source, button);
     }
 
     @Override
     public boolean canPositionVertically() {
         return true;
     }
 
     @Override
     public boolean canPositionHorizontally() {
         return true;
     }
 
     @Handler
     public void windowAdded(final SwingWindowAddedEvent event) {
         final TextFrame window = event.getChildWindow();
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 addButton(window);
                 relayout();
                 window.getContainer().addNotificationListener(ButtonBar.this);
             }
         });
     }
 
     @Handler
     public void windowDeleted(final SwingWindowDeletedEvent event) {
         final TextFrame window = event.getChildWindow();
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 window.getContainer().removeNotificationListener(ButtonBar.this);
                 if (buttons.containsKey(window)) {
                     buttonPanel.setVisible(false);
                     buttonPanel.remove(buttons.get(window));
                     buttons.remove(window);
                     buttonPanel.setVisible(true);
                 }
             }
         });
     }
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         final FrameToggleButton button = (FrameToggleButton) e.getSource();
         final TextFrame frame = button.getTextFrame();
         if (frame != null && frame.equals(activeWindow)) {
             button.setSelected(true);
         }
 
         activeFrameManager.setActiveFrame(frame);
     }
 
     @Override
     public void componentResized(final ComponentEvent e) {
         buttonWidth = position.isHorizontal() ? 150 : (parent.getWidth() / NUM_CELLS);
         relayout();
     }
 
     @Override
     public void componentMoved(final ComponentEvent e) {
         // Do nothing
     }
 
     @Override
     public void componentShown(final ComponentEvent e) {
         // Do nothing
     }
 
     @Override
     public void componentHidden(final ComponentEvent e) {
         // Do nothing
     }
 
     @Override
     public void notificationSet(final FrameContainer window,
             final Colour colour) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 final FrameToggleButton button = getButton(window);
                 if (button != null) {
                     button.setForeground(UIUtilities.convertColour(colour));
                 }
             }
         });
     }
 
     @Override
     public void notificationCleared(final FrameContainer window) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 // TODO: Should this colour be configurable?
                 notificationSet(window, window.getNotification().or(Colour.BLACK));
             }
         });
     }
 
     @Override
     public void selectionChanged(final TextFrame window) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 activeWindow = window;
                 FrameToggleButton button;
                 button = getButton(selected);
                 if (selected != null && button != null) {
                     button.setSelected(false);
                 }
 
                 selected = window.getContainer();
                 button = getButton(window.getContainer());
                 if (button != null) {
                     scrollPane.getViewport().scrollRectToVisible(
                             button.getBounds());
                     button.setSelected(true);
                 }
             }
         });
     }
 
     @Handler
     public void iconChanged(final FrameIconChangedEvent event) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 final FrameToggleButton button = getButton(event.getContainer());
                 if (button != null) {
                     button.setIcon(event.getContainer().getIconManager().getIcon(event.getIcon()));
                 }
             }
         });
     }
 
     @Handler
     public void nameChanged(final FrameNameChangedEvent event) {
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 final FrameToggleButton button = getButton(event.getContainer());
                 if (button != null) {
                     button.setText(event.getName());
                 }
             }
         });
     }
 
     /**
      * Creates and displays a Popup menu for the button that was clicked.
      *
      * @param e MouseEvent for this event
      */
     public void processMouseEvents(final MouseEvent e) {
         if (e.isPopupTrigger()) {
             final FrameToggleButton button = (FrameToggleButton) e.getSource();
 
             final TextFrame frame = button.getTextFrame();
             if (frame == null) {
                 return;
             }
             final JPopupMenu popupMenu = frame.getPopupMenu(null,
                     new Object[][]{new Object[]{""}});
             frame.addCustomPopupItems(popupMenu);
             popupMenu.add(new JMenuItem(new CloseFrameContainerAction(frame.
                     getContainer())));
             popupMenu.show(button, e.getX(), e.getY());
         }
     }
 
     @Override
     public void mouseClicked(final MouseEvent e) {
         processMouseEvents(e);
     }
 
     @Override
     public void mousePressed(final MouseEvent e) {
         processMouseEvents(e);
     }
 
     @Override
     public void mouseReleased(final MouseEvent e) {
         processMouseEvents(e);
     }
 
     @Override
     public void mouseEntered(final MouseEvent e) {
         //Do nothing
     }
 
     @Override
     public void mouseExited(final MouseEvent e) {
         //Do nothing
     }
 
     @Override
     public void configChanged(final String domain, final String key) {
         switch (key) {
             case "sortrootwindows":
                 sortRootWindows = globalConfig.getOptionBool("ui", "sortrootwindows");
                 break;
             case "sortchildwindows":
                 sortChildWindows = globalConfig.getOptionBool("ui", "sortrootwindows");
                 break;
         }
         relayout();
     }
 
 }
