 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
  *
  * Contributor(s):
  *
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 package org.netbeans.modules.javafx.source.tasklist;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 import java.util.WeakHashMap;
 import javax.tools.Diagnostic;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.spi.tasklist.FileTaskScanner;
 import org.netbeans.spi.tasklist.Task;
 import org.openide.filesystems.FileObject;
 
 /**
  *
  * @author karol 
  */
//FIXME This line must be added to the org.netbeans.modules.tasklist.filter.TypesFilter to make this scanner works "enabledProviders.add("org.netbeans.modules.javafx.source.tasklist.JavaFXTaskListProvider"); //NOI18N"
//TODO This class needs to be rewritten to actually be a provider. It'd be better to use PushTaskScanner then FileTaskScanner
 public final class JavaFXTaskListProvider extends FileTaskScanner {
 
     //private Callback callback;
     private WeakHashMap<FileObject, List<Task>> tasksMap;
     private WeakHashMap<FileObject, org.netbeans.api.javafx.source.Task<CompilationController>> runTasksMap;
 
     public JavaFXTaskListProvider() {
        super("JavaFX Task Provider", "JavaFX Task Provider", null); //NOI18N
     }
 
     @Override
     public List<? extends Task> scan(final FileObject fo) {
         if (!fo.getExt().equals("fx")) { //NOI18N
             return Collections.EMPTY_LIST;
         }
 
         try {
             //FIXME No guarantees that this thread is in AWT! Tasks for in tasksMap may not be properly updated after scan() method is finished
             JavaFXSource jfxs = JavaFXSource.forFileObject(fo);
             if (jfxs == null) {
                 return tasksMap.get(fo);
             }
             if (runTasksMap.get(fo) == null) {
                 runTasksMap.put(fo, new ErrorTask());
             }
             jfxs.runUserActionTask(runTasksMap.get(fo), true);
         } catch (IOException e) {
             e.printStackTrace();
         }
         if (tasksMap.get(fo) == null) {
             return Collections.EMPTY_LIST;
         }
 
        return tasksMap.get(fo);
     }
 
     public static JavaFXTaskListProvider create() {
         return new JavaFXTaskListProvider();
     }
 
     @Override
     public void attach(Callback callback) {
         //this.callback = callback;
         if (tasksMap == null) {
             tasksMap = new WeakHashMap<FileObject, List<Task>>();
         }
         if (runTasksMap == null) {
             runTasksMap = new WeakHashMap<FileObject, org.netbeans.api.javafx.source.Task<CompilationController>>();
         }
     }
 
     private class ErrorTask implements org.netbeans.api.javafx.source.Task<CompilationController> {
 
         public void run(CompilationController compilationController) throws Exception {
             FileObject fileObject = compilationController.getFileObject();
             
             if (tasksMap.get(fileObject) == null) {
                 tasksMap.put(fileObject, new ArrayList<Task>());
             }
             List<Task> localTasks = new ArrayList<Task>();
             for (Diagnostic diagnostic : compilationController.getDiagnostics()) {
                 if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                     Task task = Task.create(fileObject, "nb-tasklist-error", diagnostic.getMessage(Locale.getDefault()), (int) diagnostic.getLineNumber()); //NOI18N
                     localTasks.add(task);
                 }
             }
             boolean refresh = false;
             if (localTasks.size() != tasksMap.get(fileObject).size() || !localTasks.containsAll(tasksMap.get(fileObject))) {
                 refresh = true;
             }
             if (refresh) {
                 tasksMap.put(fileObject, localTasks);
                 //callback.refresh(fileObject);
             }
         }
     }
 }
