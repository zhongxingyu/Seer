 /*
  * Copyright (c) United Parcel Service of America, Inc.
  * All Rights Reserved.
  *                                                                                    
  * The use, disclosure, reproduction, modification, transfer, or transmittal          
  * of this work for any purpose in any form or by any means without the               
  * written permission of United Parcel Service is strictly prohibited.                
  * Confidential, Unpublished Property of United Parcel Service.                       
  * Use and Distribution Limited Solely to Authorized Personnel.                       
  */
 package org.intellij.plugins.junitgen.ui;
 
 import com.intellij.openapi.options.ConfigurationException;
 import com.intellij.openapi.options.SearchableConfigurable;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Comparing;
 import org.intellij.plugins.junitgen.JUnitGeneratorSettings;
 import org.intellij.plugins.junitgen.util.JUnitGeneratorUtil;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import javax.swing.*;
 
 /**
  * JUnitGenerator configuration UI.
  * Could be accessed via File->Settings->(IDE Settings) JUnit Generator
  *
  * @author Alex Nazimok (SCI)
  * @since <pre>Sep 7, 2004</pre>
  */
 public abstract class JUnitGeneratorConfigurable implements SearchableConfigurable {
 
     private JUnitGeneratorSettings settings;
     private JUnitGeneratorConfiguration configuration;
     private final Project project;
 
     protected JUnitGeneratorConfigurable(JUnitGeneratorSettings settings, @Nullable Project project) {
         this.settings = settings;
         this.project = project;
     }
 
     /**
      * the global configuration instance
      */
     public static class App extends JUnitGeneratorConfigurable {
         public App() {
             super(JUnitGeneratorSettings.getInstance(), null);
         }
 
         @NotNull
         @Override
         public String getId() {
             return "plugins.junitgenerator.application";
         }
     }
 
     /**
      * Ask for the project specific instance, regardless of the configuration
      */
     public static class Prj extends JUnitGeneratorConfigurable {
         Prj(Project project) {
             super(JUnitGeneratorSettings.getProjectInstance(project), project);
         }
 
         @NotNull
         @Override
         public String getId() {
             return "plugins.junitgenerator.project";
         }
     }
 
     public String getDisplayName() {
         return "JUnit Generator";
     }
 
     public Icon getIcon() {
         return JUnitGeneratorUtil.ICON;
     }
 
     @NotNull
     public String getHelpTopic() {
         return "help.junitgen.configuration";
     }
 
 
     public JComponent createComponent() {
         if (this.configuration == null) {
             this.configuration = new JUnitGeneratorConfiguration(this.settings, this.project);
         }
         return this.configuration.getPane();
     }
 
     /**
      * Compare the data to see if we are modified
      * we are modified if we are the project settings instance and are using project settings
      * OR we are the global instance and have a delta with the form
      *
      * @return true if the settings should be 'applied'
      */
     public boolean isModified() {
 
         boolean delta = !JUnitGeneratorUtil.isEqual(this.settings.getVmTemplates(), this.configuration.getVmTemplates()) ||
                 !Comparing.equal(this.settings.getListOverloadedMethodsBy(), this.configuration.getListOverloadedMethodsBy()) ||
                 !Comparing.equal(this.settings.getOutputFilePattern(), this.configuration.getOutput()) ||
                 this.settings.isCombineGetterAndSetter() != this.configuration.isCombineGetterAndSetter() ||
                 this.settings.isGenerateForOverloadedMethods() != this.configuration.isGenerateForOverloadedMethods() ||
                 this.settings.isUseProjectSettings() != this.configuration.isUseProjectSettings() ||
                 !Comparing.equal(this.settings.getSelectedTemplateKey(), this.configuration.getSelectedTemplateName());
         return this.configuration != null &&
                 ((this.project == null && delta) ||
                         (this.project != null && this.configuration.isUseProjectSettings() && delta) ||
                         (this.project != null && this.settings.isUseProjectSettings() != this.configuration.isUseProjectSettings()));
     }
 
     public void apply() throws ConfigurationException {
         if (this.configuration != null) {
             //if we have a project object and we are using project settings, say so
             this.settings.setUseProjectSettings(this.project != null && this.configuration.isUseProjectSettings());
             //if we are in a project and use project settings
             // or are the global instance, get them from the form
             if (this.project == null || this.settings.isUseProjectSettings()) {
                 this.settings.setCombineGetterAndSetter(this.configuration.isCombineGetterAndSetter());
                 this.settings.setGenerateForOverloadedMethods(this.configuration.isGenerateForOverloadedMethods());
                 this.settings.setListOverloadedMethodsBy(this.configuration.getListOverloadedMethodsBy());
                 this.settings.setOutputFilePattern(this.configuration.getOutput());
                 this.settings.setVmTemplates(this.configuration.getVmTemplates());
                 this.settings.setSelectedTemplateKey(this.configuration.getSelectedTemplateName());
             }
         }
     }
 
     public void disposeUIResources() {
        this.configuration = null;
     }
 
     public void reset() {
         if (this.configuration != null) {
             this.configuration.setSettings(this.settings);
         }
     }
 
     @Override
     public Runnable enableSearch(String s) {
         return null;
     }
 }
