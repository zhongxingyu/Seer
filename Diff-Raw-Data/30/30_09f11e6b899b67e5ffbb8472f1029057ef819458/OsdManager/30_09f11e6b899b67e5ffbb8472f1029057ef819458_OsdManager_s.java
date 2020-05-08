 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.osd;
 
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.config.prefs.CategoryChangeListener;
 import com.dmdirc.config.prefs.PluginPreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesDialogModel;
 import com.dmdirc.config.prefs.PreferencesInterface;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.config.prefs.SettingChangeListener;
 import com.dmdirc.events.ClientPrefsOpenedEvent;
 import com.dmdirc.interfaces.config.IdentityController;
 import com.dmdirc.plugins.PluginDomain;
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.ui.messages.ColourManager;
 import com.dmdirc.ui.messages.ColourManagerFactory;
 import com.dmdirc.util.validators.NumericalValidator;
 import com.dmdirc.util.validators.OptionalValidator;
 
 import java.awt.Window;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 import javax.inject.Inject;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Class to manage OSD Windows.
  */
 public class OsdManager implements CategoryChangeListener, PreferencesInterface,
         SettingChangeListener {
 
     /** The frame the OSD will be associated with. */
     private final Window mainFrame;
     /** List of OSD Windows. */
     private final List<OsdWindow> windowList = new ArrayList<>();
     /** List of messages to be queued. */
     private final Queue<QueuedMessage> windowQueue = new LinkedList<>();
     /** This plugin's settings domain. */
     private final String domain;
     /** Config OSD Window. */
     private OsdWindow osdWindow;
     /** X-axis position of OSD. */
     private int x;
     /** Y-axis potion of OSD. */
     private int y;
     /** Setting objects with registered change listeners. */
     private PreferencesSetting fontSizeSetting;
     private PreferencesSetting backgroundSetting;
     private PreferencesSetting foregroundSetting;
     private PreferencesSetting widthSetting;
     private PreferencesSetting timeoutSetting;
     private PreferencesSetting maxWindowsSetting;
     /** This plugin's plugin info. */
     private final PluginInfo pluginInfo;
     /** The controller to read/write settings with. */
     private final IdentityController identityController;
     /** The manager to use to parse colours. */
     private final ColourManager colourManager;
 
     @Inject
     public OsdManager(
             @MainWindow final Window mainFrame,
             final IdentityController identityController,
             final ColourManagerFactory colourManagerFactory,
             @PluginDomain(OsdPlugin.class) final PluginInfo pluginInfo) {
         this.mainFrame = mainFrame;
         this.identityController = identityController;
         this.colourManager = colourManagerFactory.getColourManager(identityController.getGlobalConfiguration());
         this.pluginInfo = pluginInfo;
         this.domain = pluginInfo.getDomain();
     }
 
     /**
      * Add messages to the queue and call displayWindows.
      *
      * @param timeout Time message will be displayed
      * @param message Message to be displayed.
      */
     public void showWindow(final int timeout, final String message) {
         windowQueue.add(new QueuedMessage(timeout, message));
         displayWindows();
     }
 
     /**
      * Displays as many windows as appropriate.
      */
     private synchronized void displayWindows() {
         final Integer maxWindows = identityController.getGlobalConfiguration()
                 .getOptionInt(domain, "maxWindows", false);
 
         QueuedMessage nextItem;
 
         while ((maxWindows == null || getWindowCount() < maxWindows)
                 && (nextItem = windowQueue.poll()) != null) {
             displayWindow(nextItem.getTimeout(), nextItem.getMessage());
         }
     }
 
     /**
      * Create a new OSD window with "message".
      * <p>
      * This method needs to be synchronised to ensure that the window list is not modified in
      * between the invocation of
      * {@link OsdPolicy#getYPosition(OsdManager, int)} and the point at which
      * the {@link OsdWindow} is added to the windowList.
      *
      * @see OsdPolicy#getYPosition(OsdManager, int)
      * @param message Text to display in the OSD window.
      */
     private synchronized void displayWindow(final int timeout, final String message) {
         final OsdPolicy policy = OsdPolicy.valueOf(
                 identityController.getGlobalConfiguration().getOption(domain, "newbehaviour")
                         .toUpperCase());
         final int startY = identityController.getGlobalConfiguration()
                 .getOptionInt(domain, "locationY");
 
         windowList.add(UIUtilities.invokeAndWait(() -> new OsdWindow(
                 mainFrame,
                 identityController, this, colourManager,
                 timeout, message, false,
                 identityController.getGlobalConfiguration().getOptionInt(
                         domain, "locationX"), policy.getYPosition(this, startY), domain)));
     }
 
     /**
      * Destroy the given OSD Window and check if the Queue has items, if so Display them.
      *
      * @param window The window that we are destroying.
      */
     public synchronized void closeWindow(final OsdWindow window) {
         final OsdPolicy policy = OsdPolicy.valueOf(
                 identityController.getGlobalConfiguration()
                 .getOption(domain, "newbehaviour").toUpperCase());
 
         int oldY = window.getDesiredY();
         final int closedIndex = windowList.indexOf(window);
 
         if (closedIndex == -1) {
             return;
         }
 
         windowList.remove(window);
 
         UIUtilities.invokeLater(window::dispose);
 
         final List<OsdWindow> newList = getWindowList();
         for (OsdWindow otherWindow : newList.subList(closedIndex, newList.size())) {
             final int currentY = otherWindow.getDesiredY();
             if (policy.changesPosition()) {
                 otherWindow.setDesiredLocation(otherWindow.getDesiredX(), oldY);
                 oldY = currentY;
             }
         }
         displayWindows();
     }
 
     /**
      * Destroy all OSD Windows.
      */
     public void closeAll() {
         getWindowList().forEach(this::closeWindow);
     }
 
     /**
      * Get the list of current OSDWindows.
      *
      * @return a List of all currently open OSDWindows.
      */
     public List<OsdWindow> getWindowList() {
         return new ArrayList<>(windowList);
     }
 
     /**
      * Get the count of open windows.
      *
      * @return Current number of OSD Windows open.
      */
     public int getWindowCount() {
         return windowList.size();
     }
 
     @Handler
     public void showConfig(final ClientPrefsOpenedEvent event) {
         final PreferencesDialogModel manager = event.getModel();
         x = identityController.getGlobalConfiguration()
                 .getOptionInt(domain, "locationX");
         y = identityController.getGlobalConfiguration()
                 .getOptionInt(domain, "locationY");
 
         final PreferencesCategory category = new PluginPreferencesCategory(
                 pluginInfo, "OSD",
                 "General configuration for OSD plugin.", "category-osd");
 
         fontSizeSetting = new PreferencesSetting(PreferencesType.INTEGER,
                 domain, "fontSize", "Font size", "Changes the font " + "size of the OSD",
                 manager.getConfigManager(),
                 manager.getIdentity()).registerChangeListener(this);
         backgroundSetting = new PreferencesSetting(PreferencesType.COLOUR,
                 domain, "bgcolour", "Background colour",
                 "Background colour for the OSD", manager.getConfigManager(),
                 manager.getIdentity()).registerChangeListener(this);
         foregroundSetting = new PreferencesSetting(PreferencesType.COLOUR,
                 domain, "fgcolour", "Foreground colour",
                 "Foreground colour for the OSD", manager.getConfigManager(),
                 manager.getIdentity()).registerChangeListener(this);
         widthSetting = new PreferencesSetting(PreferencesType.INTEGER,
                 domain, "width", "OSD Width", "Width of the OSD Window",
                 manager.getConfigManager(), manager.getIdentity())
                 .registerChangeListener(this);
         timeoutSetting = new PreferencesSetting(PreferencesType.OPTIONALINTEGER,
                 new OptionalValidator(new NumericalValidator(1, Integer.MAX_VALUE)),
                 domain, "timeout", "Timeout", "Length of time in "
                 + "seconds before the OSD window closes", manager.getConfigManager(),
                 manager.getIdentity());
         maxWindowsSetting = new PreferencesSetting(PreferencesType.OPTIONALINTEGER,
                 new OptionalValidator(new NumericalValidator(1, Integer.MAX_VALUE)),
                 domain, "maxWindows", "Maximum open windows",
                 "Maximum number of OSD windows that will be displayed at any given time",
                 manager.getConfigManager(), manager.getIdentity());
 
         category.addSetting(fontSizeSetting);
         category.addSetting(backgroundSetting);
         category.addSetting(foregroundSetting);
         category.addSetting(widthSetting);
         category.addSetting(timeoutSetting);
         category.addSetting(maxWindowsSetting);
 
         final Map<String, String> posOptions = new HashMap<>();
 
         //Populate policy MULTICHOICE
         for (OsdPolicy policy : OsdPolicy.values()) {
             posOptions.put(policy.name(), policy.getDescription());
         }
 
         category.addSetting(new PreferencesSetting(domain, "newbehaviour",
                 "New window policy", "What to do when an OSD Window "
                 + "is opened when there are other, existing windows open",
                 posOptions, manager.getConfigManager(), manager.getIdentity()));
 
         category.addChangeListener(this);
         manager.getCategory("Plugins").addSubCategory(category);
         manager.registerSaveListener(this);
     }
 
     @Override
     public void categorySelected(final PreferencesCategory category) {
         osdWindow = new OsdWindow(mainFrame, identityController, this, colourManager,
                 -1, "Please drag this OSD to position", true, x, y, domain);
         osdWindow.setBackgroundColour(backgroundSetting.getValue());
         osdWindow.setForegroundColour(foregroundSetting.getValue());
         osdWindow.setFontSize(Integer.parseInt(fontSizeSetting.getValue()));
     }
 
     @Override
     public void categoryDeselected(final PreferencesCategory category) {
         x = osdWindow.getLocationOnScreen().x;
         y = osdWindow.getLocationOnScreen().y;
 
         osdWindow.dispose();
         osdWindow = null;
     }
 
     @Override
     public void save() {
         identityController.getUserSettings().setOption(domain, "locationX", x);
         identityController.getUserSettings().setOption(domain, "locationY", y);
     }
 
     @Override
     public void settingChanged(final PreferencesSetting setting) {
         if (osdWindow == null) {
             // They've changed categories but are somehow poking settings.
             // Ignore the request.
             return;
         }
 
         if (setting.equals(fontSizeSetting)) {
             osdWindow.setFontSize(Integer.parseInt(setting.getValue()));
         } else if (setting.equals(backgroundSetting)) {
             osdWindow.setBackgroundColour(setting.getValue());
         } else if (setting.equals(foregroundSetting)) {
             osdWindow.setForegroundColour(setting.getValue());
         } else if (setting.equals(widthSetting)) {
             int width = 500;
             try {
                 width = Integer.parseInt(setting.getValue());
             } catch (NumberFormatException e) {
                 //Ignore
             }
             osdWindow.setSize(width, osdWindow.getHeight());
         }
     }
 
 }
