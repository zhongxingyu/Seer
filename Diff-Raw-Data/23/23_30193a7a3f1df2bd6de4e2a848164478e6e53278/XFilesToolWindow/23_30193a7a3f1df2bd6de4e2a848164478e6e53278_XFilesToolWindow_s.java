 /*
  * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
  *
  * license to be determined.
  */
 package com.echologic.xfiles;
 
 import java.awt.BorderLayout;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import com.intellij.openapi.actionSystem.ActionManager;
 import com.intellij.openapi.actionSystem.ActionToolbar;
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.DefaultActionGroup;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.fileEditor.FileEditorManager;
 import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
 import com.intellij.openapi.fileEditor.FileEditorManagerListener;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vcs.FileStatusManager;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.openapi.vfs.VirtualFileEvent;
 import com.intellij.openapi.vfs.VirtualFileListener;
 import com.intellij.openapi.vfs.VirtualFileManager;
 import com.intellij.openapi.vfs.VirtualFileMoveEvent;
 import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
 
 /**
  * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
  */
 public class XFilesToolWindow extends JPanel {
 
     private Logger log = Logger.getInstance(getClass().getName());
 
     private AnAction configure = new EditConfigurationsAction();
     private ScrollAction scrollToSource = ScrollAction.scrollToSource();
     private ScrollAction scrollFromSource = ScrollAction.scrollFromSource();
 
     private XFilesListModel model = new XFilesListModel();
     private JList list = new JList(model);
     private RefreshAction refresh = new RefreshAction(model);
 
     public XFilesToolWindow(Project project) {
         super(new BorderLayout());
 
         XFilesConfiguration configuration = (XFilesConfiguration) project.getComponent(XFilesConfiguration.class);
 
         scrollToSource.setSelected(configuration.SCROLL_TO_SOURCE);
         scrollFromSource.setSelected(configuration.SCROLL_FROM_SOURCE);
 
         FilterListComboBoxAction selections = new FilterListComboBoxAction(project, refresh);
         selections.setConfiguration(configuration);
 
         log.debug("filter list created; selected filter " + refresh.getFilter());
 
         DefaultActionGroup group = new DefaultActionGroup("xfiles group", false);
         group.add(refresh);
         group.add(selections);
         group.add(configure);
         group.add(scrollToSource);
         group.add(scrollFromSource);
 
         ActionManager actionManager = ActionManager.getInstance();
 
         ActionToolbar toolbar = actionManager.createActionToolbar("XFilesActionToolbar", group, true);
         add(toolbar.getComponent(), BorderLayout.NORTH);
 
         // TODO: consider re-ordering editor tabs to match selected files here?
         // TODO: consider a sync option to sync editors with our list
 
         FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);
         ListCellRenderer renderer = new XFilesListCellRenderer(fileStatusManager);
         list.setCellRenderer(renderer);
 
         // TODO: might want some icons in a gutter to indicate things?
         // for example a way to open/close selected files
 
         FileEditorManagerListener editorListener = new FileEditorManagerListener() {
             // TODO: factor out this listener
 
             private void addAndScroll(VirtualFile file) {
                 if (!model.contains(file) && refresh.getFilter().accept(file)) {
                     model.addElement(file);
                     log.debug("added " + file);
                 }
 
                 int index = model.indexOf(file);
                 if (index >= 0 && scrollFromSource.isSelected(null)) {
                     list.setSelectedIndex(index);
                     list.ensureIndexIsVisible(index);
                     log.debug("selected " + file);

                 }
             }
 
             public void fileOpened(FileEditorManager source, VirtualFile file) {
                 log.debug("file opened " + file.getName() + " scroll " + scrollFromSource.isSelected(null));
 
                 addAndScroll(file);
             }
 
             public void fileClosed(FileEditorManager source, VirtualFile file) {
                 log.debug("file closed " + file.getName());
                 int index = model.indexOf(file);
                 if (index >= 0)
                     model.removeElementAt(index);
             }
 
             public void selectionChanged(FileEditorManagerEvent event) {
                 String oldName = "";
                 String newName = "";
                 VirtualFile file = null;
 
                 if (event.getOldFile() != null)
                     oldName = event.getOldFile().getName();
 
                 if (event.getNewFile() != null) {
                     newName = event.getNewFile().getName();
                     file = event.getNewFile();
                 }
 
                 log.debug("selection changed: old file " + oldName + " new file " + newName + " scroll " + scrollFromSource.isSelected(null));
 
                 // TODO: we need to schedule this to run later, after this event has been handled
                 // do we do the standard swing invokeLater(runnable) thing or is there some other
                 // intellij magic way to do this?
 
                 if (file != null) addAndScroll(file);
             }
         };
 
         final FileEditorManager editor = FileEditorManager.getInstance(project);
         editor.addFileEditorManagerListener(editorListener);
 
         VirtualFileListener fileListener = new VirtualFileListener() {
             // TODO: factor out this listener
 
             public void propertyChanged(VirtualFilePropertyEvent event) {
                 // ignore changes to anything but the name property
                 if (!event.getPropertyName().equals(VirtualFile.PROP_NAME)) return;
 
                 log.debug("renamed " + event.getFile() + "@" + event.getFile().hashCode());
                 log.debug("   from " + event.getOldValue());
                 log.debug("     to " + event.getNewValue());
             }
 
             public void contentsChanged(VirtualFileEvent event) {
                 // changes to content may affect the changed/unchanged status
                 // so we could check the selected filter here and add the file if it is accepted
                 // however note that *lots* of contentsChanged events may be generated
                 // so this is likely to be slow so ignore this for now
             }
 
             public void fileCreated(VirtualFileEvent event) {
                 log.debug("created " + event.getFile() + "@" + event.getFile().hashCode());
             }
 
             public void fileDeleted(VirtualFileEvent event) {
                 log.debug("deleted " + event.getFile() + "@" + event.getFile().hashCode());
                 log.debug(" parent " + event.getParent());
                 log.debug("   name " + event.getFileName());
 
                 // note that getFile().getPath() returns only the name for deleted files
                 // so we need to append the name to getParent().getPath()
 
                 // wonder if event.getFile() will equals a file in our model if it was deleted?
             }
 
             public void fileMoved(VirtualFileMoveEvent event) {
                 log.debug("moved " + event.getFile() + "@" + event.getFile().hashCode());
                 log.debug(" from " + event.getOldParent());
                 log.debug("   to " + event.getNewParent());
             }
 
             // ignore events before changes actually occur
 
             public void beforePropertyChange(VirtualFilePropertyEvent event) {}
             public void beforeContentsChange(VirtualFileEvent event) {}
             public void beforeFileDeletion(VirtualFileEvent event) {}
             public void beforeFileMovement(VirtualFileMoveEvent event) {}
 
         };
 
         VirtualFileManager fileManager = VirtualFileManager.getInstance();
         fileManager.addVirtualFileListener(fileListener);
 
         ListSelectionListener listener = new ListSelectionListener() {
             // TODO: factor out this listener
 
             public void valueChanged(ListSelectionEvent e) {
                 if (!e.getValueIsAdjusting()) {
 
                     // this seems like a safe assumption in SINGLE_SELETION mode
                     int selected = list.getSelectedIndex();
                     if (selected == -1) return;
                     VirtualFile file = (VirtualFile) model.getElementAt(selected);
 
                    if (scrollToSource.isSelected(null)) {
                         editor.openFile(file, false);
                        log.debug("selected " + file);
                     }
                 }
             }
         };
 
         ListSelectionModel selection = list.getSelectionModel();
         selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         selection.addListSelectionListener(listener);
 
         JScrollPane scroller = new JScrollPane();
         scroller.getViewport().setView(list);
 
         add(scroller, BorderLayout.CENTER);
     }
 
 }
