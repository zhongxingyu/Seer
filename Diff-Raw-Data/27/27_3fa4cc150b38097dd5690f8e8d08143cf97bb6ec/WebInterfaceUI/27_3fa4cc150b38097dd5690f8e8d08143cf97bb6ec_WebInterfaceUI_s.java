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
 
 package com.dmdirc.addons.ui_web;
 
 import com.dmdirc.Channel;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.Query;
 import com.dmdirc.Server;
 import com.dmdirc.WritableFrameContainer;
 import com.dmdirc.addons.ui_web.uicomponents.WebMainWindow;
 import com.dmdirc.addons.ui_web.uicomponents.WebStatusBar;
 import com.dmdirc.config.prefs.PreferencesInterface;
 import com.dmdirc.ui.core.components.StatusBarManager;
 import com.dmdirc.ui.core.dialogs.sslcertificate.SSLCertificateDialogModel;
 import com.dmdirc.ui.interfaces.ChannelWindow;
 import com.dmdirc.ui.interfaces.InputWindow;
 import com.dmdirc.ui.interfaces.MainWindow;
 import com.dmdirc.ui.interfaces.QueryWindow;
 import com.dmdirc.ui.interfaces.ServerWindow;
 import com.dmdirc.ui.interfaces.StatusBar;
 import com.dmdirc.ui.interfaces.UIController;
 import com.dmdirc.ui.interfaces.UpdaterDialog;
 import com.dmdirc.ui.interfaces.Window;
 import com.dmdirc.updater.Update;
 
 import java.net.URI;
 import java.util.List;
 
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.security.Constraint;
 import org.mortbay.jetty.security.ConstraintMapping;
 import org.mortbay.jetty.security.SecurityHandler;
 
 /**
  * Creates and manages the web server and handles UI-wide events.
  *
  * @author chris
  */
 public class WebInterfaceUI implements UIController {
 
     /** The domain used for config settings. */
     public static final String DOMAIN = "plugin-webui";
 
     /** The web server we're using. */
     private final org.mortbay.jetty.Server webServer;
 
     /** The window manager for this UI. */
     private final WebWindowManager windowManager;
 
     /**
      * Creates a new WebInterfaceUI belonging to the specified plugin.
      *
      * @param plugin The plugin which owns this Web UI
      */
     public WebInterfaceUI(final WebInterfacePlugin plugin) {
         final SecurityHandler sh = new SecurityHandler();
         final Constraint constraint = new Constraint();
         final ConstraintMapping cm = new ConstraintMapping();
 
         constraint.setName("DMDirc Web UI");
         constraint.setRoles(new String[]{"user"});
         constraint.setAuthenticate(true);
 
         cm.setConstraint(constraint);
         cm.setPathSpec("/*");
 
         sh.setUserRealm(new WebUserRealm());
         sh.setConstraintMappings(new ConstraintMapping[]{cm});
 
         webServer = new org.mortbay.jetty.Server(5978);
 
         webServer.setHandlers(new Handler[]{
             sh,
             new RootRequestHandler(),
             new StaticRequestHandler(),
             new DMDircRequestHandler(),
             new DynamicRequestHandler(this),
         });
 
         try {
             webServer.start();
         } catch (Exception ex) {
             // Break horribly!
         }
 
         windowManager = new WebWindowManager(this);
 
         StatusBarManager.getStatusBarManager().registerStatusBar(new WebStatusBar());
     }
 
     public WebWindowManager getWindowManager() {
         return windowManager;
     }
 
     /**
      * Adds the specified handler to the webserver.
      *
      * @param newHandler The handler to add.
      */
     public void addWebHandler(final Handler newHandler) {
         webServer.addHandler(newHandler);
     }
 
     /** {@inheritDoc} */
     @Override
     public MainWindow getMainWindow() {
         return new WebMainWindow();
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Should not be used externally - use the
      * {@link com.dmdirc.ui.core.components.StatusBarManager} instead.
      */
     @Override
     @Deprecated
     public StatusBar getStatusBar() {
         return new WebStatusBar();
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Controllers should listen for window events using a
      * {@link FrameListener} and create windows as needed.
      */
     @Override @Deprecated
     public ChannelWindow getChannel(final Channel channel) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Controllers should listen for window events using a
      * {@link FrameListener} and create windows as needed.
      */
     @Override @Deprecated
     public ServerWindow getServer(final Server server) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Controllers should listen for window events using a
      * {@link FrameListener} and create windows as needed.
      */
     @Override @Deprecated
     public QueryWindow getQuery(final Query query) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Controllers should listen for window events using a
      * {@link FrameListener} and create windows as needed.
      */
     @Override @Deprecated
     public Window getWindow(final FrameContainer<?> owner) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Controllers should listen for window events using a
      * {@link FrameListener} and create windows as needed.
      */
     @Override @Deprecated
     public InputWindow getInputWindow(final WritableFrameContainer<?> owner) {
         throw new UnsupportedOperationException();
     }
 
     /** {@inheritDoc} */
     @Override
     public UpdaterDialog getUpdaterDialog(final List<Update> updates) {
         //TODO FIXME
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void showFirstRunWizard() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Migration wizard is no longer used or needed
      */
     @Override
     @Deprecated
     public void showMigrationWizard() {
         //TODO FIXME
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void showChannelSettingsDialog(final Channel channel) {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void showServerSettingsDialog(final Server server) {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void initUISettings() {
         // Do nothing
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Use {@link WindowManager#getActiveWindow()} instead
      */
     @Override @Deprecated
     public Window getActiveWindow() {
         return null;
     }
 
     /**
      * {@inheritDoc}
      *
      * @deprecated Use {@link WindowManager#getActiveWindow()} instead
      */
     @Override @Deprecated
     public Server getActiveServer() {
         //TODO FIXME
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void showURLDialog(final URI url) {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void showFeedbackNag() {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void showMessageDialog(final String title, final String message) {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public String getUserInput(final String prompt) {
         //TODO FIXME
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void showSSLCertificateDialog(final SSLCertificateDialogModel model) {
        //TODO FIXME
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public PreferencesInterface getPluginPrefsPanel() {
         //TODO FIXME
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public PreferencesInterface getUpdatesPrefsPanel() {
         //TODO FIXME
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public PreferencesInterface getUrlHandlersPrefsPanel() {
         //TODO FIXME
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public PreferencesInterface getThemesPrefsPanel() {
         //TODO FIXME
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
 }
