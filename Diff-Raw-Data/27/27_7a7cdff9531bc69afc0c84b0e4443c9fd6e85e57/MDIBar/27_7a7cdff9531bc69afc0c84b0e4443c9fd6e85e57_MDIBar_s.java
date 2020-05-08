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
 
 package com.dmdirc.addons.ui_swing.components;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.ui_swing.SelectionListener;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.addons.ui_swing.events.SwingWindowAddedEvent;
 import com.dmdirc.addons.ui_swing.events.SwingWindowDeletedEvent;
 import com.dmdirc.addons.ui_swing.injection.SwingEventBus;
 import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigChangeListener;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.ui.IconManager;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import net.miginfocom.swing.MigLayout;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Provides an MDI style bar for closing frames.
  */
 @Singleton
 public class MDIBar extends JPanel implements SelectionListener, ActionListener,
         ConfigChangeListener {
 
     /** A version number for this class. */
     private static final long serialVersionUID = -8028057596226636245L;
     /** Icon size for the close button. */
     private static final int ICON_SIZE = 12;
     /** Button to close frames. */
     private final NoFocusButton closeButton;
     /** Active frame manager. */
     private final ActiveFrameManager activeFrameManager;
     /** Config manager to get settings from. */
     private final AggregateConfigProvider config;
     /** Option domain. */
     private final String configDomain;
     /** Are we meant to be visible? */
     private boolean visibility;
 
     /**
      * Instantiates a new MDI bar.
      *
      * @param globalConfig       The config to read settings from.
      * @param iconManager        The manager to use to retrieve icons.
      * @param domain             The domain to read settings from under.
      * @param activeFrameManager Active frame manager.
      */
     @Inject
     public MDIBar(
             @GlobalConfig final AggregateConfigProvider globalConfig,
             @GlobalConfig final IconManager iconManager,
             @PluginDomain(SwingController.class) final String domain,
             final ActiveFrameManager activeFrameManager,
             @SwingEventBus final DMDircMBassador eventBus) {
         this.activeFrameManager = activeFrameManager;
         config = globalConfig;
         configDomain = domain;
         visibility = config.getOptionBool(configDomain, "mdiBarVisibility");
 
         closeButton = new NoFocusButton(iconManager.getScaledIcon("close-12", ICON_SIZE, ICON_SIZE));
 
         setOpaque(false);
         setLayout(new MigLayout("hmax 17, ins 1 0 0 0, fill"));
         add(closeButton, "w 17!, h 17!, right");
 
         eventBus.subscribe(this);
 
         activeFrameManager.addSelectionListener(this);
         closeButton.addActionListener(this);
         config.addChangeListener(configDomain, "mdiBarVisibility", this);
 
         check();
     }
 
     @Override
     public void setEnabled(final boolean enabled) {
         closeButton.setEnabled(enabled);
     }
 
     /**
      * Checks whether this MDI bar should be visible and active and sets itself accordingly.
      */
     private void check() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 setVisible(visibility);
                 setEnabled(activeFrameManager.getActiveFrame() != null);
             }
         });
     }
 
     @Handler
     public void windowAdded(final SwingWindowAddedEvent event) {
         check();
     }
 
     @Handler
     public void windowDeleted(final SwingWindowDeletedEvent event) {
         check();
     }
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (activeFrameManager.getActiveFrame() == null) {
             return;
         }
         if (closeButton.equals(e.getSource())) {
             activeFrameManager.getActiveFrame().getContainer().close();
         }
     }
 
     @Override
     public void configChanged(final String domain, final String key) {
         visibility = config.getOptionBool(configDomain, "mdiBarVisibility");
         check();
     }
 
     @Override
     public void selectionChanged(final TextFrame window) {
         check();
     }
 
 }
