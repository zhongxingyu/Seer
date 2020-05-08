 /*
  * Copyright (C) 2005 Derek Scherger <derek@echologic.com> All Rights Reserved.
  *
  * license to be determined.
  */
 package com.echologic.xfiles;
 
 import com.intellij.openapi.actionSystem.ActionManager;
 import com.intellij.openapi.actionSystem.ActionPlaces;
 import com.intellij.openapi.actionSystem.ActionPopupMenu;
 import com.intellij.openapi.actionSystem.ActionToolbar;
 import com.intellij.openapi.actionSystem.AnAction;
 import com.intellij.openapi.actionSystem.DataConstants;
 import com.intellij.openapi.actionSystem.DefaultActionGroup;
 import com.intellij.openapi.actionSystem.DataProvider;
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
 import com.intellij.psi.PsiManager;
 import org.jetbrains.annotations.Nullable;
 
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.BorderLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
  */
 public class XFilesToolWindow extends JPanel implements DataProvider {
 
     private Logger log = Logger.getInstance(getClass().getName());
 
     private AnAction configure = new EditConfigurationsAction();
     private ScrollAction scrollToSource = ScrollAction.scrollToSource();
     private ScrollAction scrollFromSource = ScrollAction.scrollFromSource();
 
     private XFilesListModel model = new XFilesListModel();
     private JList list = new JList(model);
     private RefreshAction refresh = new RefreshAction(model);
 
     private FileEditorManager editor;
     private Project project;
 
     private int scrolling = 0;
 
     private FileEditorManagerListener editorListener = new FileEditorManagerListener() {
 
         private void addAndScroll(VirtualFile file) {
             // track whether we're scrolling or not so that we can decide whether
             // to open files or not... this seems like a rather crude hack and a
             // better solution would be good
             scrolling++;
 
             log.debug("addAndScroll " + file);
 
             XFilesVirtualFileFilter filter = refresh.getFilter();
             if (!model.contains(file) && filter != null && filter.accept(file)) {
                 model.addElement(file);
                 log.debug("added " + file);
             }
 
             int index = model.indexOf(file);
             if (index >= 0 && scrollFromSource.isSelected(null)) {
                 list.setSelectedIndex(index);
                 list.ensureIndexIsVisible(index);
                 log.debug("selected " + file);
             }
 
             scrolling--;
         }
 
         public void fileOpened(FileEditorManager source, VirtualFile file) {
             log.debug("file opened " + file.getName() + " scroll " + scrollFromSource.isSelected(null));
 
             addAndScroll(file);
         }
 
         public void fileClosed(FileEditorManager source, VirtualFile file) {
             log.debug("file closed " + file.getName());
             XFilesVirtualFileFilter filter = refresh.getFilter();
             int index = model.indexOf(file);
 
             if (index >= 0 && filter != null && !filter.accept(file))
                 model.removeElementAt(index);
 
             // TODO: set new selected element
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
 
             if (file != null) addAndScroll(file);
         }
     };
 
     private VirtualFileListener fileListener = new VirtualFileListener() {
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
 
     /**
      * There are problems with this listener!
      * - it apparently needs to call openFile to switch to the related editor tab
      * - but it can't safely call open file on files that are already open in the
      *   cases where a file editor event causes the list selection change or aa
      *   exception is thrown for attempting to fire an event while handling another
      *   event
      * - if a fileOpened or selectionChanged event causes the list selection
      *   change openFile must not be called
      */
     private ListSelectionListener listener = new ListSelectionListener() {
         // TODO: factor out this listener
 
         public void valueChanged(ListSelectionEvent e) {
             if (!e.getValueIsAdjusting()) {
 
                 // this seems like a safe assumption in SINGLE_SELETION mode
                 int selected = list.getSelectedIndex();
                 if (selected == -1) return;
                 VirtualFile file = (VirtualFile) model.getElementAt(selected);
 
                 // only open selected files when we are not scrolling in the
                 // editor listener above. otherwise we're firing events while
                 // handling events which is not allowed
 
                 if (scrollToSource.isSelected(null) && scrolling == 0) {
                     editor.openFile(file, false);
                 }
             }
         }
     };
 
     /**
      * Displays the same popup menu as you would get when right-clicking on
      * a file in the project view.
      */
     private MouseAdapter popupMouseListener = new MouseAdapter() {
         public void mousePressed(MouseEvent mouseEvent) {
             // Make the item under the mouse become selected. If the item
             // is already selected then don't change the selection at all
             // since there may be multiple files selected
             int i = list.locationToIndex(mouseEvent.getPoint());
             if (i >= 0 && !list.isSelectedIndex(i)) {
                 list.setSelectedIndex(i);
             }
         }
 
         public void mouseClicked(MouseEvent mouseEvent) {
             if (SwingUtilities.isRightMouseButton(mouseEvent) && list.getSelectedValue() != null) {
                 ActionManager am = ActionManager.getInstance();
 
                 // Don't know if I need to create the actionPopupMenu for each event or if
                 // it can be reused. To be safe, I'll just recreate each time.
                 DefaultActionGroup actionGroup = (DefaultActionGroup) am.getAction(ActionPlaces.PROJECT_VIEW_POPUP + "Menu");
                 ActionPopupMenu actionPopupMenu = am.createActionPopupMenu(ActionPlaces.PROJECT_VIEW_POPUP, actionGroup);
                 actionPopupMenu.getComponent().show(list, mouseEvent.getX(), mouseEvent.getY());
             }
         }
     };
 
 
     public XFilesToolWindow(Project project) {
         super(new BorderLayout());
         this.project = project;
         XFilesConfiguration configuration = (XFilesConfiguration) project.getComponent(XFilesConfiguration.class);
 
         scrollToSource.setSelected(configuration.SCROLL_TO_SOURCE);
         scrollFromSource.setSelected(configuration.SCROLL_FROM_SOURCE);
 
         FilterListComboBoxAction selections = new FilterListComboBoxAction(project, configuration, refresh);
 
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
         list.addMouseListener(popupMouseListener);
 
         // TODO: might want some icons in a gutter to indicate things?
         // for example a way to open/close selected files
 
         editor = FileEditorManager.getInstance(project);
         editor.addFileEditorManagerListener(editorListener);
 
         VirtualFileManager fileManager = VirtualFileManager.getInstance();
         fileManager.addVirtualFileListener(fileListener);
 
         ListSelectionModel selection = list.getSelectionModel();
         selection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         selection.addListSelectionListener(listener);
 
         JScrollPane scroller = new JScrollPane();
         scroller.getViewport().setView(list);
 
         add(scroller, BorderLayout.CENTER);
     }
 
     /**
      * Implements DataProvider interface so that the popup menu actions know
      * which files to operate on.
      *
      * @param string a value DataConstants indicating what kind of data is being
      *               requested
      * @return virtual files or psi elements of the selected files, and null for
      *         all other types of data requested 
      */
     @Nullable
     public Object getData(String string) {
         if (DataConstants.VIRTUAL_FILE.equals(string)) {
             return list.getSelectedValue();
         }
         else if (DataConstants.VIRTUAL_FILE_ARRAY.equals(string)) {
             Object[] selectedValues = list.getSelectedValues();
             VirtualFile[] files = new VirtualFile[selectedValues.length];
             System.arraycopy(selectedValues, 0, files, 0, selectedValues.length);
             return files;
         }
         else if (DataConstants.PSI_FILE.equals(string) || DataConstants.PSI_ELEMENT.equals(string)) {
             VirtualFile selectedValue = (VirtualFile) list.getSelectedValue();
            return PsiManager.getInstance(project).findFile(selectedValue);
         }
         return null;
     }
 
 }
