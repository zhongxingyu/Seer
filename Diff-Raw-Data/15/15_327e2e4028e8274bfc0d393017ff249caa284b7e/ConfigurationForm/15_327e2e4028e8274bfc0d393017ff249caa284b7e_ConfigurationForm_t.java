 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.idea.ui;
 
 import com.google.jstestdriver.idea.JSTestDriverConfiguration;
 import com.google.jstestdriver.idea.ServerType;
 import com.intellij.openapi.fileChooser.FileChooser;
 import com.intellij.openapi.fileChooser.FileChooserDescriptor;
 import com.intellij.openapi.options.ConfigurationException;
 import com.intellij.openapi.options.SettingsEditor;
 import com.intellij.openapi.vfs.VirtualFile;
 import org.jetbrains.annotations.NotNull;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Configuration GUI
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class ConfigurationForm extends SettingsEditor<JSTestDriverConfiguration> {
 
   private JPanel rootComponent;
   private JLabel settingsFileLabel;
   private JTextField settingsFile;
   private JLabel serverPortLabel;
   private JTextField serverAddress;
   private JRadioButton atAddressRadioButton;
   private JRadioButton runningInIDERadioButton;
   private JButton fileChooserButton;
 
   protected void resetEditorFrom(JSTestDriverConfiguration config) {
     settingsFile.setText(config.getSettingsFile());
     serverAddress.setText(config.getServerAddress());
     if (config.getServerType() == ServerType.EXTERNAL) {
       atAddressRadioButton.setSelected(true);
     } else {
       runningInIDERadioButton.setSelected(true);
     }
   }
 
   protected void applyEditorTo(JSTestDriverConfiguration config) throws ConfigurationException {
     config.setSettingsFile(settingsFile.getText());
     config.setServerAddress(serverAddress.getText());
     if (atAddressRadioButton.isSelected()) {
       config.setServerType(ServerType.EXTERNAL);
     } else {
       config.setServerType(ServerType.INTERNAL);
     }
   }
 
   @NotNull
   protected JComponent createEditor() {
     fileChooserButton.addActionListener(new ActionListener() {
       private final FileChooserDescriptor chooseFilesOnlyDescriptor =
           new FileChooserDescriptor(true, false, false, false, false, false);
       public void actionPerformed(ActionEvent e) {
         final VirtualFile[] files = FileChooser.chooseFiles(rootComponent, chooseFilesOnlyDescriptor);
         if (files != null && files.length == 1) {
           String configFile = files[0].getPresentableUrl();
           settingsFile.setText(configFile);
         }
       }
     });
     return rootComponent;
   }
 
   protected void disposeEditor() {
   }
 
   public ConfigurationForm() {
     settingsFileLabel.setLabelFor(settingsFile);
     serverPortLabel.setLabelFor(serverAddress);
   }
 }
