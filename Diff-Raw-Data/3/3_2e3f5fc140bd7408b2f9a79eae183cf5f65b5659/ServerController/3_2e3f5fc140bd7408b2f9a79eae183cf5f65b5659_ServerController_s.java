 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.sdk.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
 import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
 import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
 import org.eclipse.jdt.ui.jarpackager.JarPackageData;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.widgets.Shell;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.sdk.SDKInfo;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class ServerController implements ServerConstants {
 
     protected File root;
 
     protected volatile int state = STOPPED;
 
     protected ListenerList listeners;
 
     protected ServerLogTail logFile;
 
     public ServerController(SDKInfo info) {
         this(info.getInstallDirectory());
     }
 
     public ServerController(File root) {
         this.root = root;
         listeners = new ListenerList();
     }
 
     public void addServerLifeCycleListener(ServerLifeCycleListener listener) {
         listeners.add(listener);
     }
 
     public void removeServerLifeCycleListener(ServerLifeCycleListener listener) {
         listeners.remove(listener);
     }
 
     protected void fireServerStarting() {
         state = STARTING;
         openLogFile();
         for (Object listener : listeners.getListeners()) {
             ((ServerLifeCycleListener) listener).serverStateChanged(this, state);
         }
     }
 
     protected void fireServerStarted() {
         state = STARTED;
         for (Object listener : listeners.getListeners()) {
             ((ServerLifeCycleListener) listener).serverStateChanged(this, state);
         }
     }
 
     protected void fireServerStopping() {
         state = STOPPING;
         for (Object listener : listeners.getListeners()) {
             ((ServerLifeCycleListener) listener).serverStateChanged(this, state);
         }
     }
 
     protected void fireServerStopped() {
         closeLogFile();
         state = STOPPED;
         for (Object listener : listeners.getListeners()) {
             ((ServerLifeCycleListener) listener).serverStateChanged(this, state);
         }
     }
 
     protected void fireConsoleText(String text) {
         for (Object listener : listeners.getListeners()) {
             ((ServerLifeCycleListener) listener).handleConsoleText(this, text);
         }
     }
 
     protected void fireConsoleError(Throwable t) {
         for (Object listener : listeners.getListeners()) {
             ((ServerLifeCycleListener) listener).handleConsoleError(this, t);
         }
     }
 
     protected synchronized void openLogFile() {
         if (logFile == null) {
             logFile = new ServerLogTail(this);
             logFile.tailAsync();
         }
     }
 
     protected synchronized void closeLogFile() {
         if (logFile != null) {
             logFile.close();
             logFile = null;
         }
     }
 
     public ServerLogTail getLogFile() {
         return logFile;
     }
 
     public boolean start() throws IOException {
         return start(false);
     }
 
     public boolean stop() throws IOException {
         return stop(false);
     }
 
     public synchronized boolean startAsJob() throws IOException {
         if (state != STOPPED) {
             return false;
         }
         new StartServer(this).runAsJob("Starting Nuxeo Server");
         return true;
     }
 
     public synchronized boolean start(boolean block) throws IOException {
         if (state != STOPPED) {
             return false;
         }
         if (block) {
             new StartServer(this).run();
         } else {
             new StartServer(this).runAsync();
         }
         return true;
     }
 
     public synchronized boolean stopAsJob() throws IOException {
         if (state == STOPPED) {
             return false;
         }
         new StopServer(this).runAsJob("Stopping Nuxeo Server");
         return true;
     }
 
     public synchronized boolean stop(boolean block) throws IOException {
         if (state == STOPPED) {
             return false;
         }
         if (block) {
             new StopServer(this).run();
         } else {
             new StopServer(this).runAsync();
         }
         return true;
     }
 
     public synchronized int getState() {
         return state;
     }
 
     public synchronized InputStream getConsoleStream() throws IOException {
         if (state == STOPPED) {
             return null;
         }
         return new FileInputStream(new File(root, "log/server.log"));
     }
 
     public boolean deploy(IProject project, Shell shell) {
         JarPackageData jarData = new JarPackageData();
         jarData.setBuildIfNeeded(true);
         jarData.setExportWarnings(true);
         jarData.setCompress(true);
         jarData.setOverwrite(true);
         jarData.setIncludeDirectoryEntries(true);
         IFile mf = project.getFile("src/main/resources/META-INF/MANIFEST.MF");
         if (mf.exists()) {
             jarData.setGenerateManifest(false);
             jarData.setManifestLocation(mf.getFullPath());
         }
        IPath path = Path.fromOSString(new File(root.getAbsolutePath(),
                "nxserver/plugins").getAbsolutePath());
         jarData.setJarLocation(path.append(project.getName() + ".jar"));
         try {
             jarData.setElements(collectElementsToExport(project));
         } catch (Exception e) {
             UI.showError("Failed to export project", e);
         }
         IJarExportRunnable op = jarData.createJarExportRunnable(shell);
         if (!executeOperation(shell, op)) {
             return false;
         }
         IStatus status = op.getStatus();
         if (!status.isOK()) {
             ErrorDialog.openError(shell, "Jar Export", null, status);
             return !(status.matches(IStatus.ERROR));
         }
         return true;
     }
 
     public PrintStream getStdout() {
         return null;
     }
 
     public PrintStream getStderr() {
         return null;
     }
 
     protected boolean executeOperation(Shell shell, IRunnableWithProgress op) {
         try {
             new BusyIndicatorRunnableContext().run(false, true, op);
         } catch (InterruptedException e) {
             return false;
         } catch (InvocationTargetException ex) {
             if (ex.getTargetException() != null) {
                 ExceptionHandler.handle(ex, shell, "JAR Export Error",
                         "Creation of JAR failed");
                 return false;
             }
         }
         return true;
     }
 
     protected Object[] collectElementsToExport(IProject project)
             throws JavaModelException {
         IJavaProject jp = JavaCore.create(project);
         ArrayList<Object> result = new ArrayList<Object>();
         // IFolder folder = project.getFolder("src/main/java");
         // if (folder.exists()) {
         // result.add(jp.getPackageFragmentRoot(folder));
         // }
         // folder = project.getFolder("src/main/resources");
         // if (folder.exists()) {
         // result.add(jp.getPackageFragmentRoot(folder));
         // }
         // return result;
         IFolder testSrc = project.getFolder("src/test");
         IPath test = testSrc.exists() ? testSrc.getFullPath() : null;
         IPackageFragmentRoot[] roots = jp.getPackageFragmentRoots();
         for (IPackageFragmentRoot root : roots) {
             if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                 if (test == null || !test.isPrefixOf(root.getPath())) {
                     System.out.println(root.getElementName() + " => "
                             + root.getPath());
                     result.add(root);
                 }
             }
         }
         return result.toArray(new Object[result.size()]);
     }
 
 }
