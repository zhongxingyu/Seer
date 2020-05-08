 package org.xtest.ui.runner;
 
 import static com.google.common.collect.Sets.newHashSet;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.xtest.RunType;
 import org.xtest.XTestRunner;
 import org.xtest.interpreter.XTestInterpreter;
 import org.xtest.results.XTestResult;
 import org.xtest.runner.external.DependencyAcceptor;
 import org.xtest.runner.util.ClasspathUtils;
 import org.xtest.ui.mediator.ValidationStartedEvent;
 import org.xtest.ui.resource.XtestResource;
 import org.xtest.xTest.Body;
 import org.xtest.xTest.impl.BodyImplCustom;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.common.eventbus.EventBus;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 /**
  * Custom implementation of XTestRunner for use in the UI that overrides the classloader of the
  * xtest interpreter with one that recognizes classes in the runtime java project
  * 
  * @author Michael Barry
  */
 public class UiXTestRunner extends XTestRunner {
     /**
      * Time to wait between checking the caller's cancel indicator.
      */
     private static final long DELAY_BETWEEN_CHECKS = 10;
     @Inject
     private EventBus eventBus;
 
     @Inject
     private Provider<XTestInterpreter> interpreterProvider;
 
     @Override
     public XTestResult run(final Body main, RunType weight, CancelIndicator monitor) {
         eventBus.post(new ValidationStartedEvent(main.eResource().getURI()));
         XTestResult result;
         if (weight == RunType.LIGHTWEIGHT) {
             result = runInSeparateThread(main, weight, monitor);
         } else {
             result = runInThisThread(main, weight, monitor);
         }
 
         return result;
     }
 
     @Override
     protected XTestInterpreter getInterpreter(Resource resource) {
         // BORROWED FROM
         // org.eclipse.xtext.purexbase.ui.autoedit.ReplAutoEdit.java
         // in order to use the classloader of the java project in the running
         // instance of eclipse rather than the classloader of this class itself
         XTestInterpreter interpreter = interpreterProvider.get();
         if (resource instanceof XtestResource) {
             XtestResource xtestResource = (XtestResource) resource;
             Optional<DependencyAcceptor> acceptor = xtestResource.getAcceptor();
             ResourceSet set = resource.getResourceSet();
             ClassLoader cl = getClass().getClassLoader();
             if (set instanceof XtextResourceSet) {
                 Object context = ((XtextResourceSet) set).getClasspathURIContext();
                 if (context instanceof IJavaProject) {
                     try {
                         final IJavaProject jp = (IJavaProject) context;
                         ArrayList<IClasspathEntry> classpath = Lists.newArrayList(jp
                                 .getResolvedClasspath(true));
                         Set<IPath> visited = Sets.newHashSet();
                         final IWorkspaceRoot root = jp.getProject().getWorkspace().getRoot();
                         Set<URL> urls = newHashSet();
                         for (int i = 0; i < classpath.size(); i++) {
                             final IClasspathEntry entry = classpath.get(i);
                             // Avoid re-visiting entries in case there is a circular project
                             // dependency
                             if (!visited.contains(entry.getPath())) {
                                 visited.add(entry.getPath());
                                 if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                                     IPath outputLocation = entry.getOutputLocation();
                                     if (outputLocation == null) {
                                         outputLocation = jp.getOutputLocation();
                                     }
                                     IFolder folder = root.getFolder(outputLocation);
                                     if (folder.exists()) {
                                         urls.add(new URL(folder.getRawLocationURI().toASCIIString()
                                                 + "/"));
                                     }
                                 } else if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                                     IPath outputLocation = entry.getOutputLocation();
                                     IProject project = jp.getProject().getWorkspace().getRoot()
                                             .getProject(entry.getPath().toString());
                                     if (outputLocation == null) {
                                         // Modified from getContainerForLocation to getProject
                                         // because
                                         // on my setup, getContainerForLocation was returning null.
                                         // Also
                                         // added null checks for safety
                                         if (project == null) {
                                             project = (IProject) jp.getProject().getWorkspace()
                                                     .getRoot()
                                                     .getContainerForLocation(entry.getPath());
                                         }
                                         if (project != null) {
                                             IJavaProject javaProject = JavaCore.create(project);
                                            outputLocation = javaProject.getOutputLocation();
                                         }
                                     }
                                     // Added null check for safety
                                     if (outputLocation != null) {
                                         IFolder folder = root.getFolder(outputLocation);
                                         if (folder.exists()) {
                                             urls.add(new URL(folder.getRawLocationURI()
                                                     .toASCIIString() + "/"));
                                         }
                                     }
                                     if (project != null) {
                                         IClasspathEntry[] resolvedClasspath = JavaCore.create(
                                                 project).getResolvedClasspath(true);
                                         classpath.addAll(Lists.newArrayList(resolvedClasspath));
                                     }
                                 } else {
                                     IPath path = entry.getPath();
                                     path = ClasspathUtils.normalizePath(root, path);
                                     urls.add(path.toFile().toURI().toURL());
                                 }
                             }
                         }
                         URL[] array = urls.toArray(new URL[urls.size()]);
                         if (acceptor.isPresent()) {
                             cl = new RecordingClassLoader(array, acceptor.get());
                         } else {
                             cl = new URLClassLoader(array);
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
             interpreter.setClassLoader(cl);
         }
         return interpreter;
     }
 
     private XTestResult runInSeparateThread(final Body main, RunType weight, CancelIndicator monitor) {
         XTestResult result;
         String name = "Running " + ((BodyImplCustom) main).getFileName();
         ArrayBlockingQueue<XTestResult> resultQueue = new ArrayBlockingQueue<XTestResult>(1);
         TestRunnerJob job = new TestRunnerJob(name, resultQueue, main);
         job.schedule();
         XTestResult jobResult = UiXTestRunner.super.run(main, weight, monitor);
 
         // TODO should be able to specify a maximum allowed time for tests to run and cancel
         // after
         // that
         while (jobResult == null) {
             try {
                 jobResult = resultQueue.poll(DELAY_BETWEEN_CHECKS, TimeUnit.MILLISECONDS);
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
             if (monitor.isCanceled()) {
                 job.cancel();
                 break;
             }
         }
         result = jobResult == null ? new XTestResult(main) : jobResult;
         return result;
     }
 
     private XTestResult runInThisThread(final Body main, RunType weight, CancelIndicator monitor) {
         XTestResult result = UiXTestRunner.super.run(main, weight, monitor);
         return result;
     }
 
     /**
      * {@link CancelIndicator} that delegates to an {@link IProgressMonitor}
      * 
      * @author Michael Barry
      */
     private static class ProgressMonitorCancelIndicator implements CancelIndicator {
         private final IProgressMonitor monitor;
 
         private ProgressMonitorCancelIndicator(IProgressMonitor monitor) {
             this.monitor = monitor;
         }
 
         @Override
         public boolean isCanceled() {
             return monitor.isCanceled();
         }
     }
 
     private static class RecordingClassLoader extends URLClassLoader {
         private final DependencyAcceptor acceptor;
 
         public RecordingClassLoader(URL[] array, DependencyAcceptor acceptor) {
             super(array);
             this.acceptor = acceptor;
         }
 
         @Override
         protected Class<?> findClass(String name) throws ClassNotFoundException {
             Class<?> loadClass = super.findClass(name);
             String path = name.replace('.', '/').concat(".class");
             URL res = findResource(path);
             if (res != null) {
                 try {
                     URI uri = res.toURI();
                     acceptor.accept(uri);
                 } catch (URISyntaxException e) {
                 }
             }
             return loadClass;
         }
     }
 
     /**
      * Test runner job
      * 
      * @author Michael Barry
      */
     private class TestRunnerJob extends Job {
         private final Body main;
         private final ArrayBlockingQueue<XTestResult> result;
 
         private TestRunnerJob(String name, ArrayBlockingQueue<XTestResult> result, Body main) {
             super(name);
             this.result = result;
             this.main = main;
         }
 
         @Override
         protected void canceling() {
             super.canceling();
             getThread().interrupt();
         }
 
         @Override
         protected IStatus run(final IProgressMonitor arg0) {
             XTestResult xtestResult = new XTestResult(main);
             try {
                 CancelIndicator indicator = new ProgressMonitorCancelIndicator(arg0);
                 xtestResult = UiXTestRunner.super.run(main, RunType.LIGHTWEIGHT, indicator);
             } finally {
                 result.offer(xtestResult);
             }
             return Status.OK_STATUS;
         }
     }
 }
