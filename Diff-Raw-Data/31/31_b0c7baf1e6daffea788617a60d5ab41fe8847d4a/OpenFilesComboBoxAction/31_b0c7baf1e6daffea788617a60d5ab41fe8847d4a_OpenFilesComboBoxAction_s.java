 /*
  * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
  *
  * license to be determined.
  */
 package com.echologic.xfiles;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.AnActionEvent;
 import com.intellij.openapi.actionSystem.Presentation;
 import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.fileEditor.FileEditorManager;
 import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
 import com.intellij.openapi.fileEditor.FileEditorManagerListener;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.project.ProjectManager;
 import com.intellij.openapi.project.ProjectManagerListener;
 import com.intellij.openapi.vcs.FileStatusManager;
 import com.intellij.openapi.vcs.FileStatus;
 import com.intellij.openapi.vfs.VirtualFile;
 
 /**
  * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
  */
 public class OpenFilesComboBoxAction extends AnAction
     implements ProjectManagerListener, CustomComponentAction, FileEditorManagerListener, ActionListener
 {
 
     private static Logger log = Logger.getInstance(OpenFilesComboBoxAction.class.getName());
 
     private FileStatusManager fileStatusManager;
     private FileEditorManager fileEditorManager;
 
     private JComboBox comboBox;
     private OpenFilesComboBoxModel model;
 
     public OpenFilesComboBoxAction() {
         log.debug("constructed");
         ProjectManager projectManager = ProjectManager.getInstance();
         projectManager.addProjectManagerListener(this);
     }
 
     /**
      * This method is not expected to be called.
      */
     public void actionPerformed(AnActionEvent e) {
         log.debug("actionPerformed from " + e.getPlace());
     }
 
     // ProjectManagerListener methods
 
     public void projectOpened(Project project) {
         log.debug("projectOpened " + project.getName());
 
         fileEditorManager = FileEditorManager.getInstance(project);
         fileStatusManager = FileStatusManager.getInstance(project);
 
         fileEditorManager.addFileEditorManagerListener(this);
 
         comboBox.setRenderer(new XFilesListCellRenderer(fileStatusManager));
     }
 
     public boolean canCloseProject(Project project) {
         log.debug("canCloseProject: true");
         return true;
     }
 
     public void projectClosing(Project project) {
         log.debug("projectClosing " + project.getName());
     }
 
     public void projectClosed(Project project) {
         fileEditorManager.removeFileEditorManagerListener(this);
 
         fileEditorManager = null;
         fileStatusManager = null;
 
         log.debug("projectClosed " + project.getName());
     }
 
     // CustomComponentAction methods
 
     public JComponent createCustomComponent(Presentation presentation) {
 
         if (comboBox == null) {
             log.debug("createCustomComponent: creating new component");
             model = new OpenFilesComboBoxModel();
             comboBox = new JComboBox(model);
             comboBox.setFocusable(false);
             comboBox.setMaximumRowCount(50);
             comboBox.addActionListener(this);
             comboBox.setActionCommand("select-file");
 
             // TODO: consider another tool bar action to close the currently open file?!?
             // not sure this is useful... really want close on various open tabs
         } else {
             log.debug("createCustomComponent: reusing existing component");
         }
 
         return comboBox;
     }
 
     // FileEditorManager methods
 
     public void fileOpened(FileEditorManager source, VirtualFile file) {
         log.debug("file opened " + file.getName());
         model.addElement(file);
     }
 
     public void fileClosed(FileEditorManager source, VirtualFile file) {
         log.debug("file closed " + file.getName());
         model.removeElement(file);
     }
 
     public void selectionChanged(FileEditorManagerEvent event) {
         String oldFile = null;
         String newFile = null;
 
         if (event.getOldFile() != null)
             oldFile = event.getOldFile().getName();
 
         if (event.getNewFile() != null)
             newFile = event.getNewFile().getName();
 
         log.debug("selection changed: old file " + oldFile + " new file " + newFile);
 
         if (newFile != null) {
             model.setSelectedItem(event.getNewFile());
         }
     }
 
     // ActionListener methods
 
     /**
      * This method is called by the open files combobox when a new file is selected. This
      * selection is passed on to the underlying FileEditorManager to select the specified
      * file.
      */
     public void actionPerformed(ActionEvent event) {
         VirtualFile file = (VirtualFile) model.getSelectedItem();
         if (file != null) {
             log.debug("actionPerformed: command " + event.getActionCommand() + "; file" + file.getName());
             fileEditorManager.openFile(file, true);
             FileStatus status = fileStatusManager.getStatus(file);
            comboBox.setToolTipText("[" + status.getText() + "/" + status + "] " + file.getPath());
         } else {
             log.debug("actionPerformed: command " + event.getActionCommand());
         }
     }
 
 }
 
 
