 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
  */
 package org.netbeans.api.javafx.source;
 
 import com.sun.tools.javafx.api.JavafxcTool;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.EventListenerList;
 import javax.tools.JavaFileManager;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.java.queries.SourceForBinaryQuery;
 import org.netbeans.api.java.queries.SourceForBinaryQuery.Result2;
 import org.netbeans.api.javafx.platform.JavaFXPlatform;
 import org.netbeans.modules.javafx.source.classpath.CachingFileManager;
 import org.netbeans.modules.javafx.source.classpath.ProxyFileManager;
 import org.netbeans.modules.javafx.source.classpath.SourceFileManager;
 import org.netbeans.spi.java.classpath.support.ClassPathSupport;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileStateInvalidException;
 import org.openide.util.Exceptions;
 import org.openide.util.WeakListeners;
 
 /**
  * @author nenik
  */
 public class ClasspathInfo {
 
     private static final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
     
     private ClassPath bootPath;
     private ClassPath compilePath, compilePathOrig;
     private ClassPath srcPath, srcPathOrig;
 
     private JavaFileManager fileManager;
     private ClassIndex usagesQuery;
 
     private final ClassPathListener cpListener;
     private EventListenerList listenerList;
 
     public static ClasspathInfo create(FileObject fo) {
         ClassPath bootPath = ClassPath.getClassPath(fo, ClassPath.BOOT);
         ClassPath compilePath = ClassPath.getClassPath(fo, ClassPath.COMPILE);
         ClassPath srcPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
         if (bootPath == null) {
             //javac requires at least java.lang
             bootPath = JavaFXPlatform.getDefaultFXPlatform().getBootstrapLibraries();
         }
         ClasspathInfo instance = new ClasspathInfo(bootPath, compilePath, srcPath);
         return instance;
     }
 
     public static ClasspathInfo create(ClassPath bootPath, ClassPath compilePath, ClassPath srcPath) {
         return new ClasspathInfo(bootPath, compilePath, srcPath);
     }
 
     private ClasspathInfo(ClassPath bootPath, ClassPath compilePath, ClassPath srcPath) {
         this.bootPath = bootPath;
         this.compilePath = this.compilePathOrig = compilePath;
         this.srcPath = this.srcPathOrig = srcPath;
 
 
         this.cpListener = new ClassPathListener();
         this.bootPath.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener, this.bootPath));
        if (compilePathOrig != null) {
            this.compilePathOrig.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener, this.compilePath));
        }
        if (this.srcPathOrig != null) {
            this.srcPathOrig.addPropertyChangeListener(WeakListeners.propertyChange(this.cpListener, this.srcPath));
        }
     }
 
     public ClassPath getClassPath(PathKind pathKind) {
         switch (pathKind) {
             case BOOT:
                 return bootPath;
             case COMPILE:
                 return compilePath;
             case SOURCE:
                 return srcPath;
             default:
                 assert false : "Unknown path type";     //NOI18N
                 return null;
         }
     }
 
     public static enum PathKind {
         BOOT,
         COMPILE,
         SOURCE,
 //	OUTPUT,
     }
 
     /*
     private static File getSymbolsForUrl(URL url) throws IOException {
     // Danger, Will Robinson!
 
     try {
     ClassLoader friend = org.netbeans.api.java.source.JavaSource.class.getClassLoader();
     Class cIndex = friend.loadClass("org.netbeans.modules.java.source.usages.Index");
     //public static File getClassFolder (final URL url) throws IOException {
     Method mGetClassFolder = cIndex.getMethod("getClassFolder", URL.class);
     File file = (File)mGetClassFolder.invoke(null, url);
     //        return Index.getClassFolder(sourceUrl);
     return file;
     } catch (Exception e) {
     throw (IOException)new IOException().initCause(e);
     }
     }
     private static List<URL> getSymbolsForPath(ClassPath cp) throws IOException {
     List<URL> l = new ArrayList<URL>();
     for (ClassPath.Entry e : cp.entries()) {
     URL src = e.getURL();
 
     File cacheFolder = getSymbolsForUrl(src);
     System.err.println("sym for " + src + " = " + cacheFolder);
     URL cacheUrl = cacheFolder.toURI().toURL();
     if (!cacheFolder.exists()) {
     cacheUrl = new URL (cacheUrl.toExternalForm()+"/");     //NOI18N
     }
     //            _cache.add(ClassPathSupport.createResource(cacheUrl));
 
 
     l.add(cacheUrl);
     }
     return l;
     }
      */
     synchronized JavaFileManager getFileManager(JavafxcTool tool) {
         if (fileManager == null) {
             recalculateClassPaths();
             /*            List<URL> userJavaClasses = null;
             try {
             userJavaClasses = getSymbolsForPath(srcPath);
             } catch (IOException ioe) {
             ioe.printStackTrace();
             }*/
             ClassPath cachedSrcPath = srcPath;
             /*            if (userJavaClasses != null) {
             URL[] urls = userJavaClasses.toArray(new URL[userJavaClasses.size()]);
             cachedSrcPath = ClassPathSupport.createProxyClassPath(srcPath, ClassPathSupport.createClassPath(urls));
             System.err.println("cached=" + cachedSrcPath);
             }*/
             fileManager = new ProxyFileManager(
                     new CachingFileManager(bootPath, true), // cacheFile, ignoreExcludes
                     new CachingFileManager(compilePath, false), // ignoreExcludes
                     new SourceFileManager(cachedSrcPath),
                     new MemoryFileManager());
         }
         return this.fileManager;
     }
 
     // XXX: Temporal, until there is a file manager implementation
     String getBootPath() {
         return bootPath.toString(ClassPath.PathConversionMode.WARN);
     }
 
     String getCompilePath() {
         return compilePath.toString(ClassPath.PathConversionMode.WARN);
     }
 
     String getSrcPath() {
         return srcPath.toString(ClassPath.PathConversionMode.WARN);
     }
 
     synchronized void addChangeListener(ChangeListener listener) {
         if (listenerList == null) {
             listenerList = new EventListenerList();
         }
         listenerList.add(ChangeListener.class, listener);
     }
 
     synchronized void removeChangeListener(ChangeListener listener) {
         listenerList.remove(ChangeListener.class, listener);
     }
 
     public synchronized ClassIndex getClassIndex() {
         if (usagesQuery == null) {
             usagesQuery = ClassIndex.forClasspathInfo(this);
         }
         return usagesQuery;
     }
 
     private void fireChangeListenerStateChanged() {
         ChangeEvent e = null;
         if (listenerList == null) {
             return;
         }
         Object[] listeners = listenerList.getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ChangeListener.class) {
                 if (e == null) {
                     e = new ChangeEvent(this);
                 }
                 ((ChangeListener) listeners[i + 1]).stateChanged(e);
             }
         }
     }
 
     synchronized private void recalculateClassPaths() {
         List<URL> srcUrls = new ArrayList<URL>();
         List<URL> clsUrls = new ArrayList<URL>();
 
         if (compilePathOrig != null) {
             for(ClassPath.Entry entry : compilePathOrig.entries()) {
                 Result2 rslt = SourceForBinaryQuery.findSourceRoots2(entry.getURL());
                 if (rslt.preferSources()) {
                     for(FileObject root : rslt.getRoots()) {
                         try {
                             srcUrls.add(root.getURL());
                         } catch (FileStateInvalidException ex) {
                             Exceptions.printStackTrace(ex);
                         }
                     }
                 } else {
                     clsUrls.add(entry.getURL());
                 }
             }
         }
 
 
         if (srcPathOrig != null) {
             for(ClassPath.Entry entry : srcPathOrig.entries()) {
                 srcUrls.add(entry.getURL());
             }
         }
 
         if (clsUrls.isEmpty()) {
             compilePath = EMPTY_PATH;
         } else {
             compilePath = ClassPathSupport.createClassPath(clsUrls.toArray(new URL[clsUrls.size()]));
         }
 
         if (srcUrls.isEmpty()) {
             srcPath = EMPTY_PATH;
         } else {
             srcPath = ClassPathSupport.createClassPath(srcUrls.toArray(new URL[srcUrls.size()]));
         }
     }
     
     private class ClassPathListener implements PropertyChangeListener {
         public void propertyChange (PropertyChangeEvent event) {
             if (ClassPath.PROP_ROOTS.equals(event.getPropertyName())) {
                 synchronized (this) {
                     // Kill FileManager
                     fileManager = null;
                 }
                 fireChangeListenerStateChanged();
             }
         }
     }
 
 }
