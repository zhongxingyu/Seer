 package org.xtest.ui.runner;
 
 import static com.google.common.collect.Sets.newHashSet;
 
 import java.net.URL;
 import java.net.URLClassLoader;
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
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.resource.XtextResourceSet;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.xtest.XTestRunner;
 import org.xtest.interpreter.XTestInterpreter;
 import org.xtest.results.XTestResult;
 import org.xtest.ui.mediator.ValidationStartedEvent;
 import org.xtest.xTest.Body;
 import org.xtest.xTest.impl.BodyImplCustom;
 
 import com.google.common.eventbus.EventBus;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 /**
  * Custom implementation of XTestRunner for use in the UI that overrides the classloader of the
  * xtest interpreter with one that recognizes classes in the runtime java project
  * 
  * @author Michael Barry
  */
 @SuppressWarnings("restriction")
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
     public XTestResult run(final Body main, CancelIndicator monitor) {
         eventBus.post(new ValidationStartedEvent(main.eResource().getURI()));
         String name = "Running " + ((BodyImplCustom) main).getFileName();
         ArrayBlockingQueue<XTestResult> resultQueue = new ArrayBlockingQueue<XTestResult>(1);
         Job job = new TestRunnerJob(name, resultQueue, main);
         job.schedule();
         XTestResult jobResult = null;
 
         // TODO should be able to specify a maximum allowed time for tests to run and cancel after
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
         XTestResult result = jobResult == null ? new XTestResult(main) : jobResult;
         return result;
     }
 
     @Override
     protected XTestInterpreter getInterpreter(Resource resource) {
         // BORROWED FROM
         // org.eclipse.xtext.purexbase.ui.autoedit.ReplAutoEdit.java
         // in order to use the classloader of the java project in the running
         // instance of eclipse rather than the classloader of this class itself
         XTestInterpreter interpreter = interpreterProvider.get();
         if (resource instanceof XtextResource) {
             ResourceSet set = resource.getResourceSet();
             ClassLoader cl = getClass().getClassLoader();
             if (set instanceof XtextResourceSet) {
                 Object context = ((XtextResourceSet) set).getClasspathURIContext();
                 if (context instanceof IJavaProject) {
                     try {
                         final IJavaProject jp = (IJavaProject) context;
                         IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
                         final IWorkspaceRoot root = jp.getProject().getWorkspace().getRoot();
                         Set<URL> urls = newHashSet();
                         for (int i = 0; i < classpath.length; i++) {
                             final IClasspathEntry entry = classpath[i];
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
                                 if (outputLocation == null) {
                                     // Modified from getContainerForLocation to getProject because
                                     // on my setup, getContainerForLocation was returning null. Also
                                     // added null checks for safety
                                     IProject project = jp.getProject().getWorkspace().getRoot()
                                             .getProject(entry.getPath().toString());
                                     if (project == null) {
                                         project = (IProject) jp.getProject().getWorkspace()
                                                 .getRoot().getContainerForLocation(entry.getPath());
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
                                         urls.add(new URL(folder.getRawLocationURI().toASCIIString()
                                                 + "/"));
                                     }
                                 }
                             } else {
                                // Change "toURL" to "toURI.toURL" since toURI is deprecated (does
                                // not properly escape character sequences)
                                urls.add(entry.getPath().toFile().toURI().toURL());
                             }
                         }
                         cl = new URLClassLoader(urls.toArray(new URL[urls.size()]));
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
             interpreter.setClassLoader(cl);
         }
         return interpreter;
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
            CancelIndicator indicator = new ProgressMonitorCancelIndicator(arg0);
            XTestResult xtestResult = UiXTestRunner.super.run(main, indicator);
            result.offer(xtestResult);
             return Status.OK_STATUS;
         }
     }
 }
