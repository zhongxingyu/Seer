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
 
 package com.dmdirc.addons.ui_swing;
 
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.ui_swing.components.menubar.MenuBar;
 import com.dmdirc.addons.ui_swing.components.statusbar.FeedbackNag;
 import com.dmdirc.addons.ui_swing.components.statusbar.SwingStatusBar;
 import com.dmdirc.addons.ui_swing.dialogs.error.ErrorListDialog;
 import com.dmdirc.addons.ui_swing.dialogs.url.URLDialogFactory;
 import com.dmdirc.addons.ui_swing.events.SwingEventBus;
 import com.dmdirc.addons.ui_swing.framemanager.ctrltab.CtrlTabWindowManager;
 import com.dmdirc.addons.ui_swing.framemanager.tree.TreeFrameManagerProvider;
 import com.dmdirc.addons.ui_swing.injection.DialogProvider;
import com.dmdirc.addons.ui_swing.wizard.SwingWindowManager;
 import com.dmdirc.addons.ui_swing.wizard.firstrun.FirstRunWizardExecutor;
 import com.dmdirc.events.FeedbackNagEvent;
 import com.dmdirc.events.FirstRunEvent;
 import com.dmdirc.events.UnknownURLEvent;
 import com.dmdirc.ui.WindowManager;
 
 import java.awt.Window;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 import javax.swing.SwingUtilities;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Manages swing components and dependencies.
  */
 @Singleton
 public class SwingManager {
 
     /** The window factory in use. */
     private final Provider<SwingWindowFactory> windowFactory;
     private final Provider<MenuBar> menuBar;
     /** The status bar in use. */
     private final Provider<SwingStatusBar> statusBar;
     /** The window manager to listen on for events. */
     private final WindowManager windowManager;
     private final CtrlTabWindowManager ctrlTabManager;
     /** Provider of first run executors. */
     private final Provider<FirstRunWizardExecutor> firstRunExecutor;
     /** Provider of feedback nags. */
     private final Provider<FeedbackNag> feedbackNagProvider;
     /** Factory to use to create URL dialogs. */
     private final URLDialogFactory urlDialogFactory;
     /** Link handler for swing links. */
     private final SwingLinkHandler linkHandler;
     /** Bus to listen on for events. */
     private final DMDircMBassador eventBus;
     /** The event bus for this plugin. */
     private final SwingEventBus swingEventBus;
     /** The provider to use to create tree-based frame managers. */
     private final TreeFrameManagerProvider treeProvider;
     /** The provider to use to create new main frames. */
     private final Provider<MainFrame> mainFrameProvider;
     /** Swing window manager. */
     private final Provider<SwingWindowManager> swingWindowManager;
     /** Error list dialog provider. */
     private final DialogProvider<ErrorListDialog> errorListDialogProvider;
     /** The main frame of the Swing UI. */
     private MainFrame mainFrame;
     /** Swing UI initialiser. */
     private final SwingUIInitialiser uiInitialiser;
 
     /**
      * Creates a new instance of {@link SwingManager}.
      *
      * @param windowFactory           The window factory in use.
      * @param windowManager           The window manager to listen on for events.
      * @param mainFrameProvider       The provider to use for the main frame.
      * @param menuBar                 The menu bar to use for the main frame.
      * @param statusBar               The status bar to use in the main frame.
      * @param ctrlTabManager          The window manager that handles ctrl+tab behaviour.
      * @param firstRunExecutor        A provider of first run executors.
      * @param feedbackNagProvider     Provider of feedback nags.
      * @param urlDialogFactory        Factory to use to create URL dialogs.
      * @param linkHandler             The handler to use when users click links.
      * @param eventBus                The bus to listen on for events.
      * @param swingEventBus           The swing event bus to listen on for swing events.
      * @param treeProvider            Provider to use for tree-based frame managers.
      * @param swingWindowManager      Swing window manager
      * @param errorListDialogProvider Error list dialog provider
      * @param uiInitialiser           Initialiser to set system/swing settings.
      */
     @Inject
     public SwingManager(
             final Provider<SwingWindowFactory> windowFactory,
             final WindowManager windowManager,
             final Provider<MainFrame> mainFrameProvider,
             final Provider<MenuBar> menuBar,
             final Provider<SwingStatusBar> statusBar,
             final CtrlTabWindowManager ctrlTabManager,
             final Provider<FirstRunWizardExecutor> firstRunExecutor,
             final Provider<FeedbackNag> feedbackNagProvider,
             final URLDialogFactory urlDialogFactory,
             final SwingLinkHandler linkHandler,
             final DMDircMBassador eventBus,
             final SwingEventBus swingEventBus,
             final TreeFrameManagerProvider treeProvider,
             final Provider<SwingWindowManager> swingWindowManager,
             final DialogProvider<ErrorListDialog> errorListDialogProvider,
             final SwingUIInitialiser uiInitialiser) {
         this.windowFactory = windowFactory;
         this.windowManager = windowManager;
         this.menuBar = menuBar;
         this.statusBar = statusBar;
         this.mainFrameProvider = mainFrameProvider;
         this.ctrlTabManager = ctrlTabManager;
         this.firstRunExecutor = firstRunExecutor;
         this.feedbackNagProvider = feedbackNagProvider;
         this.urlDialogFactory = urlDialogFactory;
         this.linkHandler = linkHandler;
         this.eventBus = eventBus;
         this.swingEventBus = swingEventBus;
         this.treeProvider = treeProvider;
         this.swingWindowManager = swingWindowManager;
         this.errorListDialogProvider = errorListDialogProvider;
         this.uiInitialiser = uiInitialiser;
     }
 
     /**
      * Handles loading of the UI.
      */
     public void load() {
         uiInitialiser.load();
         this.mainFrame = mainFrameProvider.get();
         mainFrame.setMenuBar(menuBar.get());
         mainFrame.setWindowManager(ctrlTabManager);
         mainFrame.setStatusBar(statusBar.get());
         mainFrame.initComponents();
         swingEventBus.subscribe(mainFrame);
         swingEventBus.subscribe(ctrlTabManager);
 
         windowManager.addListenerAndSync(windowFactory.get());
         eventBus.subscribe(statusBar.get());
         eventBus.subscribe(this);
         eventBus.subscribe(mainFrame);
         eventBus.subscribe(linkHandler);
         SwingUtilities.invokeLater(() -> errorListDialogProvider.get().load());
     }
 
     /**
      * Handles unloading of the UI.
      */
     public void unload() {
         swingWindowManager.get().getTopLevelWindows().forEach(Window::dispose);
         windowManager.removeListener(windowFactory.get());
         windowFactory.get().dispose();
         SwingUtilities.invokeLater(() -> errorListDialogProvider.get().dispose());
         swingEventBus.unsubscribe(mainFrame);
         swingEventBus.unsubscribe(ctrlTabManager);
         mainFrame.dispose();
         eventBus.unsubscribe(statusBar.get());
         eventBus.unsubscribe(this);
         eventBus.unsubscribe(mainFrame);
         eventBus.unsubscribe(linkHandler);
         uiInitialiser.unload();
     }
 
     /**
      * Retrieves the main frame.
      *
      * @return A main frame instance.
      *
      * @deprecated Should be injected.
      */
     @Deprecated
     public MainFrame getMainFrame() {
         return mainFrame;
     }
 
     public TreeFrameManagerProvider getTreeProvider() {
         return treeProvider;
     }
 
     @Handler
     public void showFirstRunWizard(final FirstRunEvent event) {
         if (!event.isHandled()) {
             firstRunExecutor.get().showWizardAndWait();
             event.setHandled(true);
         }
     }
 
     @Handler
     public void showURLDialog(final UnknownURLEvent event) {
         if (!event.isHandled()) {
             event.setHandled(true);
             UIUtilities.invokeLater(() -> urlDialogFactory.getURLDialog(event.getURI()).display());
         }
     }
 
     @Handler
     public void showFeedbackNag(final FeedbackNagEvent event) {
         UIUtilities.invokeLater(feedbackNagProvider::get);
     }
 
 }
