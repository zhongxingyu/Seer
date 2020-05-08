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
 
 package com.dmdirc.addons.ui_web;
 
 import com.dmdirc.Channel;
 import com.dmdirc.FrameContainer;
 import com.dmdirc.Query;
 import com.dmdirc.Server;
 import com.dmdirc.WritableFrameContainer;
 import com.dmdirc.commandparser.parsers.CommandParser;
 import com.dmdirc.config.prefs.PreferencesInterface;
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
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.updater.Update;
 
 import com.dmdirc.addons.ui_web.uicomponents.WebChannelWindow;
 import com.dmdirc.addons.ui_web.uicomponents.WebFrameManager;
 import com.dmdirc.addons.ui_web.uicomponents.WebInputWindow;
 import com.dmdirc.addons.ui_web.uicomponents.WebMainWindow;
 import com.dmdirc.addons.ui_web.uicomponents.WebQueryWindow;
 import com.dmdirc.addons.ui_web.uicomponents.WebServerWindow;
 import com.dmdirc.addons.ui_web.uicomponents.WebStatusBar;
 import com.dmdirc.addons.ui_web.uicomponents.WebWindow;
 
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
    
     /**
      * The active window.
      * @Deprecated Should be done client side
      */
     public static WebWindow active;
     
     /** The web server we're using. */
     private final org.mortbay.jetty.Server webServer;
 
     /**
      * Creates a new WebInterfaceUI belonging to the specified plugin.
      * 
      * @param plugin The plugin which owns this Web UI
      */
     public WebInterfaceUI(final WebInterfacePlugin plugin) {
         WindowManager.addFrameListener(new WebFrameManager());
         
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
             new DynamicRequestHandler(),
         });
         
         try {
             webServer.start();
         } catch (Exception ex) {
             // Break horribly!
         }
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
 
     /** {@inheritDoc} */
     @Override
     public StatusBar getStatusBar() {
         return new WebStatusBar();
     }
 
     /** {@inheritDoc} */
     @Override
     public ChannelWindow getChannel(final Channel channel) {
         return new WebChannelWindow(channel);
     }
 
     /** {@inheritDoc} */
     @Override
     public ServerWindow getServer(final Server server) {
         return new WebServerWindow(server);
     }
 
     /** {@inheritDoc} */
     @Override
     public QueryWindow getQuery(final Query query) {
         return new WebQueryWindow(query);
     }
 
     /** {@inheritDoc} */
     @Override
     public Window getWindow(final FrameContainer owner) {
         return new WebWindow(owner);
     }
 
     /** {@inheritDoc} */
     @Override
     public InputWindow getInputWindow(final WritableFrameContainer owner,
             final CommandParser commandParser) {
         return new WebInputWindow(owner, commandParser);
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
 
     /** {@inheritDoc} */
     @Override
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
 
     /** {@inheritDoc} */
    @Override
     public Window getActiveWindow() {
         return active;
     }
 
     /** {@inheritDoc} */
    @Override
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
