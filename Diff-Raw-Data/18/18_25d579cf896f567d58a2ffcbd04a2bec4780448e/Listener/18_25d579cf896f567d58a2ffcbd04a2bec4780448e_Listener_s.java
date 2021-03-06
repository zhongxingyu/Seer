 package floobits;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.openapi.components.ApplicationComponent;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.editor.EditorFactory;
 import com.intellij.openapi.editor.LogicalPosition;
 import com.intellij.openapi.editor.event.*;
 import com.intellij.openapi.fileEditor.FileDocumentManager;
 import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
 import com.intellij.openapi.vfs.*;
 import com.intellij.openapi.vfs.newvfs.BulkFileListener;
 import com.intellij.openapi.vfs.newvfs.events.*;
 import java.util.ArrayList;
 import com.intellij.util.messages.MessageBusConnection;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.openapi.editor.Document;
 
 import org.jetbrains.annotations.NotNull;
 
 import com.intellij.openapi.vfs.VirtualFileManager;
 
 public class Listener implements ApplicationComponent, BulkFileListener, DocumentListener, SelectionListener, FileDocumentManagerListener, CaretListener {
 
     private final MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect();
     private final EditorEventMulticaster em = EditorFactory.getInstance().getEventMulticaster();
 
 
     public Listener() {
         VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
             public void beforePropertyChange(final VirtualFilePropertyEvent event) {
                 if (event.getPropertyName().equals(VirtualFile.PROP_NAME)) {
                     VirtualFile parent = event.getParent();
                     String parentPath = parent.getPath();
                     String newValue = parentPath + "/" + event.getNewValue().toString();
                     String oldValue = parentPath + "/" + event.getOldValue().toString();
                     FlooHandler instance = FlooHandler.getInstance();
                     if (instance != null) {
                         instance.untellij_renamed(oldValue, newValue);
                     }
                 }
             }
         });
     }
 
 
 
     public void initComponent() {
         connection.subscribe(VirtualFileManager.VFS_CHANGES, this);
         em.addDocumentListener(this);
         em.addSelectionListener(this);
     }
 
     public void disposeComponent() {
         connection.disconnect();
         em.removeSelectionListener(this);
         em.removeDocumentListener(this);
     }
 
     @Override
     public void beforeDocumentSaving(@NotNull Document document) {
         GetPath.getPath(new GetPath(document) {
             @Override
             public void if_path(String path, FlooHandler flooHandler) {
                 flooHandler.untellij_saved(path);
             }
         });
     }
 
     @Override
     public void documentChanged(DocumentEvent event) {
         Flog.info("Document changed.");
         FlooHandler flooHandler = FlooHandler.getInstance();
         if (flooHandler == null) {
             return;
         }
         Document document = event.getDocument();
         if (document == null) {
             Flog.warn("No document? %s", event);
             return;
         }
         VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
         if (virtualFile == null) {
             Flog.warn("No virtual file for document %s", event);
             return;
         }
         flooHandler.untellij_changed(virtualFile);
     }
 
     @Override
     public void caretPositionChanged(final CaretEvent event) {
         Document document = event.getEditor().getDocument();
         final Editor[] editors = EditorFactory.getInstance().getEditors(document);
         GetPath.getPath(new GetPath(document) {
             @Override
             public void if_path(String path, FlooHandler flooHandler) {
                 if (editors.length <= 0) {
                     return;
                 }
                 Editor editor = editors[0];
                 ArrayList<ArrayList<Integer>> range = new ArrayList<ArrayList<Integer>>();
                 Integer offset = editor.getCaretModel().getOffset();
                 range.add(new ArrayList<Integer>(Arrays.asList(offset, offset)));
                 flooHandler.untellij_selection_change(path, range);
             }
         });
     }
 
     @Override
     public void selectionChanged(final SelectionEvent event) {
         Document document = event.getEditor().getDocument();
         GetPath.getPath(new GetPath(document) {
             @Override
             public void if_path(String path, FlooHandler flooHandler) {
                 TextRange[] textRanges = event.getNewRanges();
                 ArrayList<ArrayList<Integer>> ranges = new ArrayList<ArrayList<Integer>>();
                 for(TextRange r : textRanges) {
                     ranges.add(new ArrayList<Integer>(Arrays.asList(r.getStartOffset(), r.getEndOffset())));
                 }
                 flooHandler.untellij_selection_change(path, ranges);
             }
         });
     }
 
     @Override
     public void before(@NotNull List<? extends VFileEvent> events) {
         Flog.info("Before");
     }
     @Override
     public void after(@NotNull List<? extends VFileEvent> events) {
         FlooHandler handler = FlooHandler.getInstance();
         if (handler == null) {
             return;
         }
         for (VFileEvent event : events) {
             Flog.info(" after event type %s", event.getClass().getSimpleName());
             if (event == null) {
                 continue;
             }
             if (event instanceof VFileMoveEvent) {
                 Flog.info("move event %s", event);
                 VirtualFile oldParent = ((VFileMoveEvent) event).getOldParent();
                 VirtualFile newParent = ((VFileMoveEvent) event).getNewParent();
                 String oldPath = oldParent.getPath();
                 String newPath = newParent.getPath();
                 VirtualFile virtualFile = event.getFile();
                 ArrayList<VirtualFile> files;
                 try {
                     files = Utils.getAllNestedFiles(virtualFile, new Ignore());
                 } catch (IOException e) {
                     Flog.warn("Unable to get nested files for move event %s.", virtualFile);
                     continue;
                 }
                 for (VirtualFile file: files) {
                     String newFilePath = file.getPath();
                     String oldFilePath = newFilePath.replace(newPath, oldPath);
                     FlooHandler.getInstance().untellij_renamed(oldFilePath, newFilePath);
                 }
                 continue;
             }
             if (event instanceof VFileDeleteEvent) {
                 Flog.info("deleting a file %s", event.getPath());
                 handler.untellij_deleted_directory(Utils.getAllNestedFilePaths(event.getFile()));
                 continue;
             }
             if (event instanceof VFileCopyEvent) {
                // We get one copy event per file copied, which makes this easy.
                 Flog.info("Copying a file %s", event);
                 VirtualFile newParent = ((VFileCopyEvent) event).getNewParent();
                 String newChildName = ((VFileCopyEvent) event).getNewChildName();
                 String path = event.getPath();
                 VirtualFile[] children = newParent.getChildren();
                 VirtualFile copiedFile = null;
                 for (VirtualFile child : children) {
                     if (child.getName().equals(newChildName)) {
                         copiedFile = child;
                     }
                 }
                 if (copiedFile == null) {
                     Flog.warn("Couldn't find copied virtual file %s", path);
                     continue;
                 }
                 Utils.createFile(copiedFile);
                 continue;
             }
             if (event instanceof VFileCreateEvent) {
                 Flog.info("creating a file %s", event);
                 ArrayList<VirtualFile> createdFiles = null;
                 try {
                     createdFiles = (Utils.getAllNestedFiles(event.getFile(), new Ignore()));
                 } catch (IOException e) {
                     Flog.warn("Unable to delete files %s", e);
                     continue;
                 }
                 for (final VirtualFile createdFile : createdFiles) {
                     Utils.createFile(createdFile);
                 }
                 continue;
             }
             if (event instanceof VFileContentChangeEvent) {
                 ArrayList<VirtualFile> changedFiles = null;
                 try {
                     changedFiles = Utils.getAllNestedFiles(event.getFile(), new Ignore());
                 } catch (IOException e) {
                     Flog.warn("Unable to change file. %s %s", e, event);
                     continue;
                 }
                 for (VirtualFile file : changedFiles) {
                     handler.untellij_changed(file);
                 }
                 continue;
             }
         }
     }
 
     @NotNull
     @Override
     public String getComponentName() {
         return "Floobits";  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void unsavedDocumentsDropped() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void beforeFileContentReload(VirtualFile file, @NotNull Document document) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void fileWithNoDocumentChanged(@NotNull VirtualFile file) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void fileContentReloaded(VirtualFile file, @NotNull Document document) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void beforeAllDocumentsSaving() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
     @Override
     public void beforeDocumentChange(DocumentEvent event) {
 //        Flog.info(String.format("beforeDocumentChange, %s", event));
     }
 }
