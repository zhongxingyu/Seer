 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
  *
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  */
 package org.netbeans.modules.javafx.source.tasklist;
 
 import com.sun.tools.mjavac.util.JCDiagnostic;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Logger;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.modules.parsing.api.indexing.IndexingManager;
 import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
 import org.netbeans.spi.tasklist.PushTaskScanner;
 import org.netbeans.spi.tasklist.Task;
 import org.netbeans.spi.tasklist.TaskScanningScope;
 import org.openide.filesystems.FileChangeAdapter;
 import org.openide.filesystems.FileChangeListener;
 import org.openide.filesystems.FileEvent;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.URLMapper;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 import org.openide.util.RequestProcessor;
 
 /**
  *
  * @author Karol Harezlak
  */
 public class JavaFXTaskListProvider extends PushTaskScanner {
 
    private static final Logger LOG = Logger.getAnonymousLogger("Task List Errors"); //NOI18N
     private static final String TASK_LIST_NAME = NbBundle.getMessage(JavaFXTaskListProvider.class, "LABEL_TL_JAVAFX_ISSUES"); //NOI18N
     private static final String FX_EXT = "fx"; //NOI18N
     private final HashMap<FileObject, FileChangeListener> projectDirs = new HashMap<FileObject, FileChangeListener>();
     private final HashMap<FileObject, RequestProcessor.Task> waitingTasks = new HashMap<FileObject, RequestProcessor.Task>();
     private final HashMap<FileObject, Future<Void>> scannerTasks = new HashMap<FileObject, Future<Void>>();
     private AtomicBoolean active = new AtomicBoolean();
     private RequestProcessor.Task majorTask;
     private RequestProcessor processor;
 
     private JavaFXTaskListProvider() {
         super(TASK_LIST_NAME, TASK_LIST_NAME, TASK_LIST_NAME);
     }
 
     public static final PushTaskScanner create() {
         return new JavaFXTaskListProvider();
     }
 
     @Override
     public void setScope(TaskScanningScope taskScanningScope, final Callback callback) {
         if (callback == null) {
             active.set(false);
             if (majorTask != null) {
                 majorTask.cancel();
                 //LOG.info("Major task has been CANCELED " + majorTask.hashCode()); //NOI18N
                 majorTask = null;
             }
             for (FileObject fileObject : projectDirs.keySet()) {
                 fileObject.removeRecursiveListener(projectDirs.get(fileObject));
             }
             for (RequestProcessor.Task task : waitingTasks.values()) {
                 task.cancel();
                 //LOG.info("Waiting task has been CANCELED " + task); //NOI18N
             }
             for (Future<Void> future : scannerTasks.values()) {
                 future.cancel(true);
                 //LOG.info("Scanner Task has been CANCELED " + future); //NOI18N
             }
             projectDirs.clear();
             waitingTasks.clear();
             scannerTasks.clear();
             processor.stop();
             processor.shutdown();
             return;
         }
         active.set(true);
         processor = new RequestProcessor("Error Task List Processor"); //NOI18N
         Iterator<FileObject> iterator = taskScanningScope.iterator();
         Collection<FileObject> fileObjects = new HashSet<FileObject>();
         while (iterator.hasNext()) {
             FileObject fileObject = iterator.next();
             if (!fileObject.getExt().equals(FX_EXT)) {
                 continue;
             }
             //callback.setTasks(fileObject, Collections.EMPTY_LIST);
             fileObjects.add(fileObject);
             final FileObject projectDir = FileOwnerQuery.getOwner(fileObject).getProjectDirectory();
 
             if (!projectDirs.keySet().contains(projectDir)) {
                 FileChangeListener listener = new FileChangeAdapter() {
 
                     @Override
                     public void fileDataCreated(FileEvent fe) {
                         if (fe.getFile().getExt().equals(FX_EXT) && !active.get()) {
                             //LOG.info("File created: " + fe.getFile().getName());  //NOI18N
                             updateTask(fe.getFile(), callback, 4000);
                         }
                         super.fileDataCreated(fe);
                     }
 
                     @Override
                     public void fileFolderCreated(FileEvent fe) {
                         if (!active.get()) {
                             return;
                         }
                         for (FileObject child : fe.getFile().getChildren()) {
                             if (child.getExt().equals(FX_EXT)) {
                                 //LOG.info("Folder created and update: " + fe.getFile().getName());  //NOI18N
                                 updateTask(child, callback, 4000);
                             }
                         }
                         super.fileFolderCreated(fe);
                     }
 
                     @Override
                     public synchronized void fileChanged(FileEvent fe) {
                         if (fe.getFile().getExt().equals(FX_EXT)) {
                             if (!active.get()) {
                                 return;
                             }
                             //LOG.info("File changed: " + fe.getFile().getName());  //NOI18N
                             callback.setTasks(fe.getFile(), Collections.EMPTY_LIST);
                             updateTask(fe.getFile(), callback, 4000);
                         }
                         super.fileChanged(fe);
                     }
 
                     @Override
                     public synchronized void fileDeleted(FileEvent fe) {
                         if (!active.get()) {
                             return;
                         }
                         if (fe.getFile() == projectDir) {
                             fe.getFile().removeRecursiveListener(this);
                         } else if (fe.getFile().getExt().equals(FX_EXT)) {
                             //LOG.info("File removed: " + fe.getFile().getName());  //NOI18N
                             cancelRemoveTasks(fe.getFile());
                             callback.setTasks(fe.getFile(), Collections.EMPTY_LIST);
                         }
                         super.fileDeleted(fe);
                     }
                 };
                 projectDirs.put(projectDir, listener);
                 projectDir.addRecursiveListener(listener);
             }
         }
         if (!fileObjects.isEmpty()) {
             updateTasks(fileObjects, callback, 5000);
         }
     }
 
     private void createTask(FileObject fileObject, Callback callback) {
         //LOG.info("Number of register scanner tasks: " + scannerTasks.size());
         if (scannerTasks.size() > 7) {
             //LOG.info("No more scanner tasks can be registered: " + scannerTasks.size());
             return;
         } 
         if (!fileObject.isValid() || !ErrorsCache.isInError(fileObject, false)) {
             callback.setTasks(fileObject, Collections.EMPTY_LIST);
             return;
         }
         JavaFXSource jfxs = JavaFXSource.forFileObject(fileObject);
         if (jfxs == null) {
             return;
         }
         try {
             Future<Void> future = jfxs.runWhenScanFinished(new ScannerTask(callback), true);
             //LOG.info("Scanning task created and scheduled, file: " + fileObject.getName()); //NOI18N
             scannerTasks.put(fileObject, future);
         } catch (IOException ex) {
             Exceptions.printStackTrace(ex);
             return;
         }
 
     }
 
     private void cancelRemoveTasks(FileObject fileObject) {
         RequestProcessor.Task waitingTask = waitingTasks.get(fileObject);
         if (waitingTask != null) {
             waitingTask.cancel();
             //LOG.info("Waiting task has been CANCELED and REMOVED, file " + fileObject); //NOI18N
         }
         waitingTasks.remove(fileObject);
         Future<Void> future = scannerTasks.get(fileObject);
         if (future != null) {
             future.cancel(true);
             //LOG.info("Task Scaner has been CANCELED and REMOVED " + future+ " for file " + fileObject); //NOI18N
         }
         scannerTasks.remove(fileObject);
 
     }
 
     private synchronized void updateTask(final FileObject fileObject, final Callback callback, final int delay) {
         cancelRemoveTasks(fileObject);
         RequestProcessor.Task task = processor.create(new Runnable() {
 
             public void run() {
                 if (!active.get() || !fileObject.isValid() || !ErrorsCache.isInError(fileObject, false)) {
                     return;
                 }
                 if (IndexingManager.getDefault().isIndexing()) {
                     synchronized (this) {
                         final RequestProcessor.Task task = waitingTasks.get(fileObject);
                         if (task != null) {
                             task.schedule(delay);
                         }
                     }
                     //LOG.info("Task " + fileObject.getName() + " has to wait " + delay + " ms"); //NOI18N
                 } else {
                     createTask(fileObject, callback);
                 }
             }
         });
         waitingTasks.put(fileObject, task);
         task.setPriority(Thread.MIN_PRIORITY);
         task.schedule(delay);
     }
 
     private synchronized void updateTasks(final Collection<FileObject> fileObjects, final Callback callback, final int delay) {
         majorTask = processor.create(new Runnable() {
 
             public void run() {
                 if (!active.get()) {
                     return;
                 }
                 if (IndexingManager.getDefault().isIndexing()) {
                     if (majorTask != null) {
                         majorTask.schedule(delay);
                         //LOG.info("Major task " + majorTask.hashCode() + " has to wait " + delay + " ms"); //NOI18N
                     } else {
                         return;
                     }
                 } else {
                     for (FileObject fileObject : fileObjects) {
                         if (majorTask == null) {
                             return;
                         }
                         createTask(fileObject, callback);
                     }
                     majorTask = null;
                 }
             }
         });
         if (majorTask == null) {
             return;
         }
         majorTask.setPriority(Thread.MIN_PRIORITY);
         majorTask.schedule(delay);
         //LOG.info("Major task has been CREATED " + majorTask.hashCode()); //NOI18N
     }
 
     private class ScannerTask
             implements org.netbeans.api.javafx.source.Task<CompilationController> {
 
         private final Callback callback;
 
         public ScannerTask(Callback callback) {
             this.callback = callback;
         }
 
         public void run(final CompilationController compilationController) throws Exception {
             if (!compilationController.isErrors() || !active.get()) {
                 return;
             }
             //LOG.info("Scanning " + compilationController.getFileObject().getName()); //NOI18N
             refreshTasks(compilationController);
         }
 
         private void refreshTasks(CompilationController compilationController) {
             List<Diagnostic> diagnostics = compilationController.getDiagnostics();
             Map<FileObject, List<Task>> localTasks = new HashMap<FileObject, List<Task>>();
             for (Diagnostic diagnostic : diagnostics) {
                 if (diagnostic.getKind() != Diagnostic.Kind.ERROR) {
                     continue;
                 }
                 JCDiagnostic jcd = ((JCDiagnostic) diagnostic);
                 FileObject currentFileObject = null;
                 try {
                     currentFileObject = URLMapper.findFileObject(jcd.getSource().toUri().toURL());
                 } catch (MalformedURLException ex) {
                     ex.printStackTrace();
                 }
                 if (currentFileObject == null) {
                     continue;
                 }
                 Task task = Task.create(currentFileObject, "nb-tasklist-error", diagnostic.getMessage(Locale.getDefault()), (int) diagnostic.getLineNumber()); //NOI18N
                 localTasks.put(currentFileObject, addTask(localTasks.get(currentFileObject), task));
             }
             for (FileObject file : localTasks.keySet()) {
                 List<Task> list = localTasks.get(file);
                 if (list != null) {
                     callback.setTasks(file, list);
                 }
                 list.clear();
             }
             localTasks.clear();
             synchronized (this) {
                 waitingTasks.remove(compilationController.getFileObject());
                 scannerTasks.remove(compilationController.getFileObject());
                 //LOG.info("Scanning task is finished and has been REMOVED from waiting tasks list" + compilationController.getFileObject().getName()); //NOI18N
             }
         }
 
         private List<Task> addTask(List<Task> list, Task task) {
             if (list == null) {
                 list = new ArrayList<Task>();
             }
             list.add(task);
 
             return list;
         }
     }
 }
