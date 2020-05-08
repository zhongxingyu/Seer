 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
  * Portions Copyrighted 2009 Sun Microsystems, Inc.
  */
 
 package org.netbeans.api.javafx.source;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.swing.text.Document;
 import junit.framework.Assert;
 
 import org.netbeans.api.editor.mimelookup.MimePath;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.javafx.lexer.JFXTokenId;
 import org.netbeans.api.javafx.platform.JavaFXPlatform;
 import org.netbeans.api.lexer.Language;
 import org.netbeans.junit.NbTestCase;
 import org.netbeans.lib.lexer.test.TestLanguageProvider;
 import org.netbeans.modules.java.source.TreeLoader;
 import org.netbeans.modules.java.source.usages.BinaryAnalyser;
 import org.netbeans.modules.java.source.usages.ClassIndexImpl;
 import org.netbeans.modules.java.source.usages.ClassIndexManager;
 import org.netbeans.modules.javafx.source.parsing.JavaFXParserFactory;
 import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
 import org.netbeans.spi.java.classpath.ClassPathProvider;
 import org.netbeans.spi.java.classpath.support.ClassPathSupport;
 
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileSystem;
 import org.openide.filesystems.FileUtil;
 import org.openide.filesystems.MultiFileSystem;
 import org.openide.filesystems.Repository;
 import org.openide.filesystems.XMLFileSystem;
 import org.openide.loaders.DataObject;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.Lookups;
 import org.openide.util.lookup.ProxyLookup;
 
 /**
  * Based on java.editor
  */
 public class SourceTestBase extends NbTestCase {
 
     static {
         SourceTestBase.class.getClassLoader().setDefaultAssertionStatus(true);
         System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
         Assert.assertEquals(Lkp.class, Lookup.getDefault().getClass());
 
         TreeLoader.DISABLE_ARTIFICAL_PARAMETER_NAMES = true;
     }
 
     static final int FINISH_OUTTIME = 5 * 60 * 1000;
     private ClassPath bootPath;
 
     public static class Lkp extends ProxyLookup {
 
         private static Lkp DEFAULT;
 
         public Lkp() {
             Assert.assertNull(DEFAULT);
             DEFAULT = this;
         }
 
         public static void initLookups(Object[] objs) throws Exception {
             ClassLoader l = Lkp.class.getClassLoader();
             DEFAULT.setLookups(new Lookup [] {
                 Lookups.fixed(objs),
                 Lookups.metaInfServices(l),
                 Lookups.singleton(l)
             });
         }
     }
 
     public SourceTestBase(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
 
         // XXX: this call did not do anything
 //        GlobalSourcePathTestUtil.setUseLibraries(false);
 
 
         XMLFileSystem system = new XMLFileSystem();
         system.setXmlUrls(new URL[] {
             SourceTestBase.class.getResource("/org/netbeans/api/javafx/source/resources/layer.xml"),
         });
         Repository repository = new Repository(new MultiFileSystem(
                 new FileSystem[] {FileUtil.createMemoryFileSystem(), system}));
 
         ClassPathProvider cpp = new ClassPathProvider() {
             public ClassPath findClassPath(FileObject file, String type) {
                 try {
                     if (type == ClassPath.SOURCE) {
                         return ClassPathSupport.createClassPath(new FileObject[]{FileUtil.toFileObject(getWorkDir())});
                     }
                     if (type == ClassPath.COMPILE) {
                         return ClassPathSupport.createClassPath(new FileObject[0]);
                     }
                     if (type == ClassPath.BOOT) {
                         return getBootClassPath();
                     }
                 } catch (IOException ex) {
                     // XXX: describe here why it is safe to eat the exception
                 }
                 return null;
             }
         };
 /*        SharedClassObject loader = JavaFXDataLoader.findObject(JavaFXDataLoader.class, true);
 */
         MimeDataProvider mdp = new MimeDataProvider() {
             public Lookup getLookup(MimePath mimePath) {
                 return Lookups.fixed(/*new JavaFXEditorKit(),*/ new JavaFXParserFactory());
             }
         };
         Lkp.initLookups(new Object[] {repository, /*loader, */ cpp, mdp});
         TestLanguageProvider.register(JFXTokenId.language());
 
 /*        File cacheFolder = new File(getWorkDir(), "var/cache/index");
         cacheFolder.mkdirs();
         IndexUtil.setCacheFolder(cacheFolder);
  */
 //        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.javafx.editor.JavaFXEditorKit");
         final ClassPath sourcePath = ClassPathSupport.createClassPath(new FileObject[] {FileUtil.toFileObject(getDataDir())});
         final ClassIndexManager mgr  = ClassIndexManager.getDefault();
         for (ClassPath.Entry entry : sourcePath.entries()) {
             mgr.createUsagesQuery(entry.getURL(), true);
         }
         final ClasspathInfo cpInfo = ClasspathInfo.create(getBootClassPath(), ClassPathSupport.createClassPath(new URL[0]), sourcePath);
         assertNotNull(cpInfo);
         final JavaFXSource js = JavaFXSource.create(cpInfo, Collections.<FileObject>emptyList());
         assertNotNull(js);
         js.runUserActionTask(new Task<CompilationController>() {
             public void run(CompilationController parameter) throws Exception {
                 for (ClassPath.Entry entry : getBootClassPath().entries()) {
                     final URL url = entry.getURL();
                     final ClassIndexImpl cii = mgr.createUsagesQuery(url, false);
                     ClassIndexManager.getDefault().writeLock(new ClassIndexManager.ExceptionAction<Void>() {
                         public Void run() throws IOException, InterruptedException {
                             BinaryAnalyser ba = cii.getBinaryAnalyser();
                             ba.start(url, new AtomicBoolean(false), new AtomicBoolean(false));
                             ba.finish();
                             return null;
                         }
                     });
                 }
             }
         }, true);
 //        Utilities.setCaseSensitive(true);
     }
 
     @Override
     protected void tearDown() throws Exception {
         this.bootPath = null;
         super.tearDown();
     }
 
     private ClassPath getBootClassPath() {
         if (this.bootPath == null) {
             this.bootPath = ClassPathSupport.createProxyClassPath(
                     createClassPath(System.getProperty("sun.boot.class.path")),
                     JavaFXPlatform.getDefault().getBootstrapLibraries());
         }
         return this.bootPath;
     }
 
     private static ClassPath createClassPath(String classpath) {
         StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
         List/*<PathResourceImplementation>*/ list = new ArrayList();
         while (tokenizer.hasMoreTokens()) {
             String item = tokenizer.nextToken();
             File f = FileUtil.normalizeFile(new File(item));
             URL url = getRootURL(f);
             if (url!=null) {
                 list.add(ClassPathSupport.createResource(url));
             }
         }
         return ClassPathSupport.createClassPath(list);
     }
 
     // XXX this method could probably be removed... use standard FileUtil stuff
     private static URL getRootURL(File f) {
         URL url = null;
         try {
             if (isArchiveFile(f)) {
                 url = FileUtil.getArchiveRoot(f.toURI().toURL());
             } else {
                 url = f.toURI().toURL();
                 String surl = url.toExternalForm();
                 if (!surl.endsWith("/")) {
                     url = new URL(surl+"/");
                 }
             }
         } catch (MalformedURLException e) {
             throw new AssertionError(e);
         }
         return url;
     }
 
     private static boolean isArchiveFile(File f) {
         // the f might not exist and so you cannot use e.g. f.isFile() here
         String fileName = f.getName().toLowerCase();
         return fileName.endsWith(".jar") || fileName.endsWith(".zip");    //NOI18N
     }
 
     public static void testInsideSourceTask(String source, final Task<CompilationController> task) throws Exception {
         File f = File.createTempFile("Test", ".fx");
         toFile(f, source);
         FileObject fo = FileUtil.toFileObject(f);
         testInsideSourceTask(fo, task);
     }
 
     public static void testInsideSourceTask(FileObject fo, final Task<CompilationController> task) throws Exception {
         JavaFXSource src = JavaFXSource.forFileObject(fo);
         DataObject dobj = DataObject.find(fo);
         EditorCookie ec = dobj.getCookie(EditorCookie.class);
         Document doc = ec.openDocument();
         doc.putProperty(Language.class, JFXTokenId.language());
 
         final Exception[] exc = new Exception[1];
         src.runUserActionTask(new CancellableTask<CompilationController>() {
             public void cancel() {
                 if (task instanceof CancellableTask) {
                     ((CancellableTask)task).cancel();
                 }
             }
             public void run(CompilationController controller) throws Exception {
                 try {
                     task.run(controller);
                 } catch (Exception e) {
                     exc[0] = e;
                 }
             }
         }, true);
 
         if (exc[0] instanceof Exception) {
             throw (Exception)exc[0];
         }
     }
 
     private static void toFile(File f, String s) throws  Exception {
         OutputStream os = new FileOutputStream(f);
         Writer w = new OutputStreamWriter(os);
         w.write(s);
         w.close();
         os.close();
     }
 
 }
