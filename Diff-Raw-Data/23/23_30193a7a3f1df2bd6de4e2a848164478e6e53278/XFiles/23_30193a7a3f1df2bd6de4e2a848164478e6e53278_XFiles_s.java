 /*
  * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
  *
  * license to be determined.
  */
 package com.echologic.xfiles;
 
 import com.intellij.openapi.components.ProjectComponent;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.wm.ToolWindow;
 import com.intellij.openapi.wm.ToolWindowAnchor;
 import com.intellij.openapi.wm.ToolWindowManager;
 
 /**
  * Configurable ProjectComponent classes appear under Project Settings in the settings panel.
  *
  * Configurable ApplicationComponent classes presumably appear under IDE settings in the settings panel.
  *
  * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
  */
 public class XFiles implements ProjectComponent {
 
     public static final String TOOL_WINDOW_ID = "XFiles";
 
     private Project project;
 
     public XFiles(Project project) {
         this.project = project;
     }
 
     // ProjectComponent methods
 
     public String getComponentName() {
         return "XFiles";
     }
 
     public void initComponent() {
     }
 
     public void disposeComponent() {
     }
 
     public void projectOpened() {
         ToolWindowManager manager = ToolWindowManager.getInstance(project);
         ToolWindow window = manager.registerToolWindow(TOOL_WINDOW_ID,
                                                        new XFilesToolWindow(project),
                                                        ToolWindowAnchor.LEFT);
         window.setTitle(project.getName());
         window.setIcon(XFilesIcons.XFILES_ICON);
     }
 
     public void projectClosed() {
         ToolWindowManager manager = ToolWindowManager.getInstance(project);
         manager.unregisterToolWindow(TOOL_WINDOW_ID);
     }
 }
