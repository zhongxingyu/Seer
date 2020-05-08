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
 
 package com.dmdirc.addons.ui_swing.injection;
 
 import com.dmdirc.Channel;
 import com.dmdirc.ClientModule.UserConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
 import com.dmdirc.addons.ui_swing.SwingWindowFactory;
 import com.dmdirc.addons.ui_swing.dialogs.about.AboutDialog;
 import com.dmdirc.addons.ui_swing.dialogs.aliases.AliasManagerDialog;
 import com.dmdirc.addons.ui_swing.dialogs.channellist.ChannelListDialog;
 import com.dmdirc.addons.ui_swing.dialogs.channelsetting.ChannelSettingsDialog;
 import com.dmdirc.addons.ui_swing.dialogs.feedback.FeedbackDialog;
 import com.dmdirc.addons.ui_swing.dialogs.newserver.NewServerDialog;
 import com.dmdirc.addons.ui_swing.dialogs.prefs.SwingPreferencesDialog;
 import com.dmdirc.addons.ui_swing.dialogs.profile.ProfileManagerDialog;
 import com.dmdirc.addons.ui_swing.dialogs.serversetting.ServerSettingsDialog;
 import com.dmdirc.addons.ui_swing.dialogs.updater.SwingRestartDialog;
 import com.dmdirc.addons.ui_swing.dialogs.updater.SwingUpdaterDialog;
 import com.dmdirc.commandparser.auto.AutoCommandManager;
 import com.dmdirc.config.prefs.PreferencesManager;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.LifecycleController;
 import com.dmdirc.interfaces.config.ConfigProvider;
 import com.dmdirc.interfaces.config.IdentityFactory;
 import com.dmdirc.interfaces.ui.AboutDialogModel;
 import com.dmdirc.interfaces.ui.AliasDialogModel;
 import com.dmdirc.interfaces.ui.FeedbackDialogModel;
 import com.dmdirc.interfaces.ui.NewServerDialogModel;
 import com.dmdirc.interfaces.ui.ProfilesDialogModel;
 import com.dmdirc.plugins.ServiceManager;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.core.about.CoreAboutDialogModel;
 import com.dmdirc.ui.core.aliases.CoreAliasDialogModel;
 import com.dmdirc.ui.core.feedback.CoreFeedbackDialogModel;
 import com.dmdirc.ui.core.newserver.CoreNewServerDialogModel;
 import com.dmdirc.ui.core.profiles.CoreProfilesDialogModel;
 import com.dmdirc.ui.input.TabCompleterUtils;
 import com.dmdirc.ui.messages.ColourManagerFactory;
 
 import java.awt.Window;
 import java.awt.datatransfer.Clipboard;
 
 import javax.inject.Provider;
 import javax.inject.Qualifier;
 import javax.inject.Singleton;
 
 import dagger.Module;
 import dagger.Provides;
 
 /**
  * Facilitates injection of dialogs.
  */
 @Module(library = true, complete = false)
 public class DialogModule {
 
     /**
      * Qualifier that indicates a restart dialog is needed for updates to be applied.
      */
     @Qualifier
     public static @interface ForUpdates {
     }
 
     /**
      * Qualifier that indicates a restart dialog is needed for settings to be applied.
      */
     @Qualifier
     public static @interface ForSettings {
     }
 
     @Provides
     public AliasDialogModel getAliasDialogModel(final CoreAliasDialogModel model) {
         return model;
     }
 
     @Provides
     public NewServerDialogModel getNewServerDialogModel(final CoreNewServerDialogModel model) {
         return model;
     }
 
     @Provides
     public FeedbackDialogModel getFeedbackDialogModel(final CoreFeedbackDialogModel model) {
         return model;
     }
 
     @Provides
     public ProfilesDialogModel getProfileDialogModel(final CoreProfilesDialogModel model) {
         return model;
     }
 
     @Provides
     public AboutDialogModel getAboutDialogModel(final CoreAboutDialogModel model) {
         return model;
     }
 
     @Provides
     @Singleton
     public DialogProvider<NewServerDialog> getNewServerDialogProvider(
             final Provider<NewServerDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @Singleton
     public DialogProvider<ProfileManagerDialog> getNewProfileManagerDialogProvider(
             final Provider<ProfileManagerDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @Singleton
     public DialogProvider<AliasManagerDialog> getAliasManagerDialogProvider(
             final Provider<AliasManagerDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @Singleton
     public DialogProvider<FeedbackDialog> getFeedbackDialogProvider(
             final Provider<FeedbackDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @Singleton
     public DialogProvider<AboutDialog> getAboutDialogProvider(
             final Provider<AboutDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @Singleton
     public KeyedDialogProvider<Connection, ServerSettingsDialog> getServerSettingsDialogProvider(
             final PreferencesManager preferencesManager,
             final PrefsComponentFactory compFactory,
             final AutoCommandManager autoCommandManager,
             @MainWindow final Window parentWindow,
             final ColourManagerFactory colourManagerFactory,
             final IconManager iconManager) {
         return new KeyedDialogProvider<Connection, ServerSettingsDialog>() {
             @Override
             protected ServerSettingsDialog getInstance(final Connection key) {
                 return new ServerSettingsDialog(preferencesManager, compFactory, autoCommandManager,
                         key, parentWindow, colourManagerFactory, iconManager);
             }
         };
     }
 
     @Provides
     @Singleton
     public KeyedDialogProvider<Channel, ChannelSettingsDialog> getChannelSettingsDialogProvider(
             final IdentityFactory identityFactory,
             final SwingWindowFactory windowFactory,
             @UserConfig final ConfigProvider userConfig,
             final ServiceManager serviceManager,
             final PreferencesManager preferencesManager,
             final PrefsComponentFactory compFactory,
             @MainWindow final Window parentWindow,
             final Clipboard clipboard,
             final CommandController commandController,
             final DMDircMBassador eventBus,
             final ColourManagerFactory colourManagerFactory,
             final TabCompleterUtils tabCompleterUtils,
             final IconManager iconManager) {
         return new KeyedDialogProvider<Channel, ChannelSettingsDialog>() {
             @Override
             protected ChannelSettingsDialog getInstance(final Channel key) {
                 return new ChannelSettingsDialog(identityFactory, windowFactory,
                         userConfig, serviceManager, preferencesManager, compFactory, key,
                         parentWindow, clipboard, commandController, eventBus,
                         colourManagerFactory, tabCompleterUtils, iconManager);
             }
         };
     }
 
     @Provides
     @Singleton
     public DialogProvider<SwingPreferencesDialog> getSwingPreferencesDialogProvider(
             final Provider<SwingPreferencesDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @Singleton
     public DialogProvider<SwingUpdaterDialog> getSwingUpdaterDialogProvider(
             final Provider<SwingUpdaterDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @Singleton
     @ForUpdates
     public DialogProvider<SwingRestartDialog> getSwingRestartDialogProviderForUpdates(
             @ForUpdates final Provider<SwingRestartDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @Singleton
     @ForSettings
     public DialogProvider<SwingRestartDialog> getSwingRestartDialogProviderForSettings(
             @ForSettings final Provider<SwingRestartDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
     @Provides
     @ForUpdates
     public SwingRestartDialog getRestartDialogForUpdates(
             final MainFrame mainFrame,
             final LifecycleController lifecycleController) {
         return new SwingRestartDialog(mainFrame, lifecycleController, "finish updating");
     }
 
     @Provides
     @ForSettings
     public SwingRestartDialog getRestartDialogForSettings(
             final MainFrame mainFrame,
             final LifecycleController lifecycleController) {
         return new SwingRestartDialog(mainFrame, lifecycleController, "apply settings");
     }
 
     @Provides
     @Singleton
     public DialogProvider<ChannelListDialog> getChannelListDialogProvider(
             final Provider<ChannelListDialog> provider) {
         return new DialogProvider<>(provider);
     }
 
 }
