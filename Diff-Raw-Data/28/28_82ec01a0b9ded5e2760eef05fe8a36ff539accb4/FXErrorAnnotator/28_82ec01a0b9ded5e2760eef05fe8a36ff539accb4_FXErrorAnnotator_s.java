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
 
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.WeakHashMap;
 import javax.swing.Action;
import javax.tools.Diagnostic;
 import org.netbeans.api.fileinfo.NonRecursiveFolder;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 import org.netbeans.api.project.ProjectUtils;
 import org.netbeans.api.project.SourceGroup;
 import org.netbeans.modules.masterfs.providers.AnnotationProvider;
 import org.netbeans.modules.masterfs.providers.InterceptionListener;
 import org.openide.ErrorManager;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileStateInvalidException;
 import org.openide.filesystems.FileStatusEvent;
 import org.openide.filesystems.FileUtil;
 import org.openide.util.Lookup;
 
 import org.openide.util.RequestProcessor;
 import static org.openide.util.ImageUtilities.assignToolTipToImage;
 import static org.openide.util.ImageUtilities.loadImage;
 import static org.openide.util.ImageUtilities.mergeImages;
 import static org.openide.util.NbBundle.getMessage;
 
 /**
  *
  * @author answer
  */
 public class FXErrorAnnotator extends AnnotationProvider {
 
     private static final String ERROR_BADGE_URL = "org/netbeans/modules/javafx/source/resources/icons/error-badge.gif";
     private static final Image ERROR_BADGE_SINGLE;
     private static final Image ERROR_BADGE_FOLDER;
 
     static {
         URL errorBadgeIconURL = FXErrorAnnotator.class.getClassLoader().getResource(ERROR_BADGE_URL);
         String errorBadgeSingleTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + getMessage(FXErrorAnnotator.class, "TP_ErrorBadgeSingle");
         ERROR_BADGE_SINGLE = assignToolTipToImage(loadImage(ERROR_BADGE_URL), errorBadgeSingleTP); // NOI18N
         String errorBadgeFolderTP = "<img src=\"" + errorBadgeIconURL + "\">&nbsp;" + getMessage(FXErrorAnnotator.class, "TP_ErrorBadgeFolder");
         ERROR_BADGE_FOLDER = assignToolTipToImage(loadImage(ERROR_BADGE_URL), errorBadgeFolderTP); // NOI18N
     }
 
     @Override
     public String annotateName(String name, Set files) {
         return null;
     }
 
     @Override
     public Image annotateIcon(Image icon, int iconType, Set files) {
         boolean inError = false;
         boolean singleFile = files.size() == 1;
 
         if (files instanceof NonRecursiveFolder) {
             FileObject folder = ((NonRecursiveFolder) files).getFolder();
             inError = isInError(folder, false, true);
             singleFile = false;
         } else {
             for (Object o : files) {
                 if (o instanceof FileObject) {
                     FileObject f = (FileObject) o;
 
                     if (f.isFolder()) {
                         singleFile = false;
                         if (isInError(f, true, !inError)) {
                             inError = true;
                             continue;
                         }
                         if (inError)
                             continue;
                     } else {
                         if (f.isData() && "fx".equals(f.getExt())) {
                             if (isInError(f, true, !inError)) {
                                 inError = true;
                             }
                         }
                     }
                 }
             }
         }
 
         if (inError) {
             //badge:
             Image i = mergeImages(icon, singleFile ? ERROR_BADGE_SINGLE : ERROR_BADGE_FOLDER, 0, 8);
             Iterator<? extends AnnotationProvider> it = Lookup.getDefault().lookupAll(AnnotationProvider.class).iterator();
             boolean found = false;
 
             while (it.hasNext()) {
                 AnnotationProvider p = it.next();
 
                 if (found) {
                     Image res = p.annotateIcon(i, iconType, files);
 
                     if (res != null) {
                         return res;
                     }
                 } else {
                     found = p == this;
                 }
             }
 
             return i;
         }
 
         return null;
     }
 
     @Override
     public String annotateNameHtml(String name, Set files) {
         return null;
     }
 
     @Override
     public Action[] actions(Set files) {
         return null;
     }
 
     @Override
     public InterceptionListener getInterceptionListener() {
         return null;
     }
 
     public void updateAllInError() {
         try {
             File[] roots = File.listRoots();
             for (File root : roots) {
                 FileObject rootFO = FileUtil.toFileObject(root);
 
                 if (rootFO != null) {
                     fireFileStatusChanged(new FileStatusEvent(rootFO.getFileSystem(), true, false));
                 }
             }
         } catch (FileStateInvalidException ex) {
             ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
         }
     }
 
     public synchronized void updateInError(Set<URL> urls)  {
         Set<FileObject> toRefresh = new HashSet<FileObject>();
         for (Iterator<FileObject> it = knownFiles2Error.keySet().iterator(); it.hasNext(); ) {
             FileObject f = it.next();
             try {
                 if (urls.contains(f.getURL())) {
                     toRefresh.add(f);
                     Integer i = knownFiles2Error.get(f);
 
                     if (i != null) {
                         knownFiles2Error.put(f, i | INVALID);
 
                         enqueue(f);
                     }
                 }
             } catch (IOException e) {
                 ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
             }
         }
     }
 
     public void fireFileStatusChanged(Set<FileObject> fos) {
         if (fos.isEmpty())
             return ;
         try {
             fireFileStatusChanged(new FileStatusEvent(fos.iterator().next().getFileSystem(), fos, true, false));
         } catch (FileStateInvalidException ex) {
             ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
         }
     }
 
     public static FXErrorAnnotator getAnnotator() {
         for (AnnotationProvider ap : Lookup.getDefault().lookupAll(AnnotationProvider.class)) {
             if (ap.getClass() == FXErrorAnnotator.class) {
                 return (FXErrorAnnotator) ap;
             }
         }
 
         return null;
     }
 
     private static final int IN_ERROR_REC = 1;
     private static final int IN_ERROR_NONREC = 2;
     private static final int INVALID = 4;
 
     private Map<FileObject, Integer> knownFiles2Error = new WeakHashMap<FileObject, Integer>();
 
     private synchronized boolean isInError(FileObject file, boolean recursive, boolean forceValue) {
         boolean result = false;
         Integer i = knownFiles2Error.get(file);
 
         if (i != null) {
             result = (i & (recursive ? IN_ERROR_REC : IN_ERROR_NONREC)) != 0;
 
             if ((i & INVALID) == 0)
                 return result;
         }
 
         if (!forceValue) {
             if (i == null) {
                 knownFiles2Error.put(file, null);
             }
             return result;
         }
 
         enqueue(file);
         return result;
     }
 
     public boolean haveError(FileObject file, boolean recursive) {
         if (file.isData()) {
             JavaFXSource fxSource = JavaFXSource.forFileObject(file);
             if (fxSource == null)
                 return false;
             if (fxSource.currentInfo == null)
                 return false;
 //            List<Diagnostic> diag = fxSource.currentInfo.getDiagnostics();
 //            return !diag.isEmpty();
             return fxSource.currentInfo.isErrors();
         } else {
/* need to be implemented with java file checking for errors, temporary always without errors
             FileObject[] childs = file.getChildren();
             for(int i = 0;i < childs.length; i++){
                 if (haveError(childs[i], true)){
                     return true;
                 }
             }
 */
             return false;
         }
     }
 
     private void enqueue(FileObject file) {
         if (toProcess == null) {
             toProcess = new LinkedList<FileObject>();
             WORKER.schedule(50);
         }
 
         toProcess.add(file);
     }
 
     private Collection<FileObject> toProcess = null;
 
     private final RequestProcessor.Task WORKER = new RequestProcessor("ErrorAnnotator worker", 1).create(new Runnable() {
         public void run() {
             Collection<FileObject> toProcess;
 
             synchronized (FXErrorAnnotator.this) {
                 toProcess = FXErrorAnnotator.this.toProcess;
                 FXErrorAnnotator.this.toProcess = null;
             }
 
             for (FileObject f : toProcess) {
                 boolean recError = false;
                 boolean nonRecError = false;
                 if (f.isData()) {
                     recError = nonRecError = haveError(f, true);
                 } else {
                     boolean handled = false;
                     Project p = FileOwnerQuery.getOwner(f);
 
                     if (p != null) {
                         for (SourceGroup sg : ProjectUtils.getSources(p).getSourceGroups("java")) {
                             FileObject sgRoot = sg.getRootFolder();
 
                             if ((FileUtil.isParentOf(f, sgRoot) || f == sgRoot) && haveError(sgRoot, true)) {
                                 recError = true;
                                 nonRecError = false;
                                 handled = true;
                                 break;
                             }
  
                         }
                     }
 
                     if (!handled) {
                         recError = haveError(f, true);
                         nonRecError = haveError(f, false);
                     }
  
                 }
 
                 Integer value = (recError ? IN_ERROR_REC : 0) | (nonRecError ? IN_ERROR_NONREC : 0);
 
                 synchronized (FXErrorAnnotator.this) {
                     knownFiles2Error.put(f, value);
                 }
 
                 fireFileStatusChanged(Collections.singleton(f));
             }
         }
     });
 
 }
