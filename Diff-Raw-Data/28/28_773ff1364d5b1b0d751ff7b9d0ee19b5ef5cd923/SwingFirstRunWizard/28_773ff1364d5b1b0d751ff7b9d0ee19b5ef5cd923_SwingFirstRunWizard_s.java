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
 
 package com.dmdirc.addons.ui_swing.wizard.firstrun;
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.ClientModule.UserConfig;
 import com.dmdirc.CorePluginExtractor;
import com.dmdirc.actions.ActionManager;
import com.dmdirc.actions.CoreActionType;
 import com.dmdirc.addons.ui_swing.Apple;
 import com.dmdirc.addons.ui_swing.MainFrame;
 import com.dmdirc.addons.ui_swing.dialogs.profiles.ProfileManagerDialog;
 import com.dmdirc.addons.ui_swing.injection.DialogProvider;
 import com.dmdirc.addons.ui_swing.wizard.Step;
 import com.dmdirc.addons.ui_swing.wizard.WizardDialog;
 import com.dmdirc.addons.ui_swing.wizard.WizardListener;
 import com.dmdirc.commandline.CommandLineOptionsModule.Directory;
 import com.dmdirc.commandline.CommandLineOptionsModule.DirectoryType;
import com.dmdirc.interfaces.ActionListener;
import com.dmdirc.interfaces.actions.ActionType;
 import com.dmdirc.interfaces.config.ConfigProvider;
 import com.dmdirc.interfaces.ui.FirstRunWizard;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.util.resourcemanager.ResourceManager;
 
 import java.awt.Dialog.ModalityType;
 import java.awt.Dimension;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 /** First run wizard, used to initially setup the client for the user. */
 @Singleton
 public class SwingFirstRunWizard implements WizardListener, FirstRunWizard {
 
     /** Wizard dialog. */
     private final WizardDialog wizardDialog;
     /** Global config. */
     private final ConfigProvider config;
     /** Extractor to use for core plugins. */
     private final CorePluginExtractor corePluginExtractor;
     /** Actions directory. */
     private final String actionsDirectory;
     /** Provider to use to obtain PMDs. */
     private final DialogProvider<ProfileManagerDialog> profileDialogProvider;
 
     /**
      * Instantiate the wizard.
      *
      * @param parentWindow          Parent window
      * @param config                Global config
      * @param actionsDirectory      Actions directory
      * @param pluginExtractor       Plugin extractor to use.
      * @param iconManager           Manager to use to find icons.
      * @param profileDialogProvider Provider to use to obtain PMDs.
      */
     @Inject
     public SwingFirstRunWizard(
             final MainFrame parentWindow,
             @UserConfig final ConfigProvider config,
             @Directory(DirectoryType.ACTIONS) final String actionsDirectory,
             final CorePluginExtractor pluginExtractor,
             @GlobalConfig final IconManager iconManager,
             final DialogProvider<ProfileManagerDialog> profileDialogProvider) {
         this.corePluginExtractor = pluginExtractor;
         this.config = config;
         this.actionsDirectory = actionsDirectory;
         this.profileDialogProvider = profileDialogProvider;
 
         wizardDialog = new WizardDialog("Setup wizard", new ArrayList<Step>(),
                 parentWindow, ModalityType.APPLICATION_MODAL);
         wizardDialog.setIconImage(iconManager.getImage("icon"));
         wizardDialog.addWizardListener(this);
         if (Apple.isAppleUI()) {
             wizardDialog.setMinimumSize(new Dimension(400, 500));
         } else {
             wizardDialog.setMinimumSize(new Dimension(400, 500));
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void wizardFinished() {
         if (ResourceManager.getResourceManager() == null) {
             return;
         }
         if (((ExtractionStep) wizardDialog.getStep(0)).getPluginsState()) {
             extractPlugins();
         }
         if (((ExtractionStep) wizardDialog.getStep(0)).getActionsState()) {
             extractActions();
         }
 
         config.setOption("updater", "enable",
                 ((CommunicationStep) wizardDialog.getStep(1)).checkUpdates());
         config.setOption("general", "submitErrors",
                 ((CommunicationStep) wizardDialog.getStep(1)).checkErrors());
 
         if (((ProfileStep) wizardDialog.getStep(2)).getProfileManagerState()) {
            ActionManager.getActionManager().registerListener(new ActionListener() {
                /** {@inheritDoc} */
                @Override
                public void processEvent(final ActionType type,
                        final StringBuffer format, final Object... arguments) {
                    profileDialogProvider.displayOrRequestFocus();
                }
            }, CoreActionType.CLIENT_OPENED);

         }
         wizardDialog.dispose();
     }
 
     /** {@inheritDoc} */
     @Override
     public void wizardCancelled() {
         wizardDialog.dispose();
     }
 
     /** {@inheritDoc} */
     @Override
     public void extractPlugins() {
         corePluginExtractor.extractCorePlugins(null);
     }
 
     /** {@inheritDoc} */
     @Override
     public void extractActions() {
         extractCoreActions();
     }
 
     /** Extracts the core actions. */
     public void extractCoreActions() {
         //Copy actions
         final Map<String, byte[]> resources =
                 ResourceManager.getResourceManager().
                 getResourcesStartingWithAsBytes("com/dmdirc/actions/defaults");
         for (Entry<String, byte[]> resource : resources.entrySet()) {
             try {
                 final String resourceName = actionsDirectory + resource.getKey()
                         .substring(27, resource.getKey().length());
                 final File newDir =
                         new File(resourceName.substring(0,
                         resourceName.lastIndexOf('/')) + "/");
 
                 if (!newDir.exists()) {
                     newDir.mkdirs();
                 }
 
                 final File newFile =
                         new File(newDir,
                         resourceName.substring(resourceName.lastIndexOf('/') + 1,
                         resourceName.length()));
 
                 if (!newFile.isDirectory()) {
                     ResourceManager.getResourceManager().
                             resourceToFile(resource.getValue(), newFile);
                 }
             } catch (IOException ex) {
                 Logger.userError(ErrorLevel.LOW, "Failed to extract actions");
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void display() {
         wizardDialog.addStep(new FirstRunExtractionStep());
         wizardDialog.addStep(new CommunicationStep());
         wizardDialog.addStep(new ProfileStep());
         wizardDialog.display();
     }
 
     /**
      * Returns the dialog associated with this wizard.
      *
      * @return Associated wizard dialog
      */
     public WizardDialog getWizardDialog() {
         return wizardDialog;
     }
 
 }
