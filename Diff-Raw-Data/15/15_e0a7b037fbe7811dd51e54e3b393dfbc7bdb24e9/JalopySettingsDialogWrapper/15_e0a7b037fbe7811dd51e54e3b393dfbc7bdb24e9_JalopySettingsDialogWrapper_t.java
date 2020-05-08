 /*
  * IntelliJ IDEA Jalopy Formatter plugin
  * Copyright (C) 2012  Alexey Hanin <mail@alexeyhanin.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alexeyhanin.intellij.jalopyplugin.swing;
 
 import com.alexeyhanin.intellij.jalopyplugin.exception.JalopyPluginRuntimeException;
 
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.ui.DialogWrapper;
 
import de.hunsicker.jalopy.storage.Convention;
 import de.hunsicker.jalopy.swing.PreviewFrameHelper;
 import de.hunsicker.jalopy.swing.SettingsContainer;
 import de.hunsicker.jalopy.swing.ValidationException;
 
 import org.jetbrains.annotations.Nullable;
 
 import java.awt.GridLayout;
 
import java.io.IOException;

 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 public class JalopySettingsDialogWrapper extends DialogWrapper {
 
     private SettingsContainer settingsContainer;
 
     @Nullable
     @Override
     protected String getDimensionServiceKey() {
         return "JalopyPlugin.SettingsDialog.Dimensions";
     }
 
     public JalopySettingsDialogWrapper(final Project project) {
         super(project);
         setTitle("Jalopy Settings");
         init();
     }
 
     @Nullable
     @Override
     protected JComponent createCenterPanel() {
 
         final JPanel rootPanel = new JPanel(new GridLayout(1, 1));
         settingsContainer = new SettingsContainer(
             PreviewFrameHelper.createPreviewFrame(this.getWindow()));
         rootPanel.add(settingsContainer);
 
         return rootPanel;
     }
 
     @Override
     protected void doOKAction() {
         try {
             settingsContainer.updateSettings();
         } catch (final ValidationException e) {
             throw new JalopyPluginRuntimeException(
                 "Unexpected exception while trying to update Jalopy settings.");
         }
 
        try {
            Convention.getInstance().flush();
        } catch (final IOException ex) {
            throw new JalopyPluginRuntimeException("Unable to save Jalopy settings", ex);
        }

         super.doOKAction();
     }
 }
