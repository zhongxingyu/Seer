 package bndtools.launch;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 
 import aQute.bnd.build.Project;
 import aQute.bnd.build.ProjectLauncher;
 import bndtools.Central;
 import bndtools.Plugin;
 
 public class OSGiRunLaunchDelegate extends AbstractOSGiLaunchDelegate {
 
     private ProjectLauncher bndLauncher = null;
 
     @Override
     public void launch(final ILaunchConfiguration configuration, String mode, final ILaunch launch, IProgressMonitor monitor) throws CoreException {
         SubMonitor progress = SubMonitor.convert(monitor, 2);
 
         waitForBuilds(progress.newChild(1, SubMonitor.SUPPRESS_NONE));
 
         try {
             Project project = getBndProject(configuration);
             synchronized (project) {
                 bndLauncher = project.getProjectLauncher();
             }
             configureLauncher(configuration);
             bndLauncher.prepare();
 
             boolean dynamic = configuration.getAttribute(LaunchConstants.ATTR_DYNAMIC_BUNDLES, LaunchConstants.DEFAULT_DYNAMIC_BUNDLES);
             if (dynamic)
                 registerLaunchPropertiesRegenerator(project, launch);
         } catch (Exception e) {
             throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "Error obtaining OSGi project launcher.", e));
         }
 
 
         super.launch(configuration, mode, launch, progress.newChild(1, SubMonitor.SUPPRESS_NONE));
     }
 
     private void configureLauncher(ILaunchConfiguration configuration) throws CoreException {
         boolean clean = configuration.getAttribute(LaunchConstants.ATTR_CLEAN, LaunchConstants.DEFAULT_CLEAN);
         bndLauncher.setKeep(!clean);
 
         bndLauncher.setTrace(enableTraceOption(configuration));
     }
 
     /**
      * Registers a resource listener with the project model file to update the
      * launcher when the model or any of the run-bundles changes. The resource
      * listener is automatically unregistered when the launched process
      * terminates.
      *
      * @param project
      * @param launch
      * @throws CoreException
      */
     private void registerLaunchPropertiesRegenerator(final Project project, final ILaunch launch) throws CoreException {
         final IResource targetResource = getTargetResource(launch.getLaunchConfiguration());
         final IPath bndbndPath = Central.toPath(project, project.getPropertiesFile());
 
         final IPath targetPath;
         try {
             targetPath = Central.toPath(project, project.getTarget());
         } catch (Exception e) {
             throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "Error querying project output folder", e));
         }
         final IResourceChangeListener resourceListener = new IResourceChangeListener() {
             public void resourceChanged(IResourceChangeEvent event) {
                 try {
                     final AtomicBoolean update = new AtomicBoolean(false);
 
                     // Was the properties file (bnd.bnd or *.bndrun) included in the delta?
                     IResourceDelta propsDelta = event.getDelta().findMember(bndbndPath);
                     if (propsDelta == null && targetResource.getType() == IResource.FILE)
                         propsDelta = event.getDelta().findMember(targetResource.getFullPath());
                     if (propsDelta != null) {
                         if (propsDelta.getKind() == IResourceDelta.CHANGED) {
                             update.set(true);
                         }
                     }
 
                     // Check for bundles included in the launcher's runbundles list
                     if (!update.get()) {
                         final Set<String> runBundleSet = new HashSet<String>(bndLauncher.getRunBundles());
                         event.getDelta().accept(new IResourceDeltaVisitor() {
                             public boolean visit(IResourceDelta delta) throws CoreException {
                                 // Short circuit if we have already found a match
                                 if (update.get())
                                     return false;
 
                                 IResource resource = delta.getResource();
                                 if (resource.getType() == IResource.FILE) {
                                     boolean isRunBundle = runBundleSet.contains(resource.getLocation().toPortableString());
                                     update.compareAndSet(false, isRunBundle);
                                     return false;
                                 }
 
                                 // Recurse into containers
                                 return true;
                             }
                         });
                     }
 
                     // Was the target path included in the delta? This might mean that sub-bundles have changed
                     boolean targetPathChanged = event.getDelta().findMember(targetPath) != null;
                     update.compareAndSet(false, targetPathChanged);
 
                     if(update.get()) {
                         bndLauncher.update();
                     }
                 } catch (Exception e) {
                     IStatus status = new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "Error updating launch properties file.", e);
                     Plugin.log(status);
                 }
             }
         };
         ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
 
         // Register a listener for termination of the launched process
         Runnable onTerminate = new Runnable() {
             public void run() {
                 ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
             }
         };
         DebugPlugin.getDefault().addDebugEventListener(new TerminationListener(launch, onTerminate));
     }
 
     @Override
     protected ProjectLauncher getProjectLauncher() throws CoreException {
         if (bndLauncher == null)
             throw new CoreException(new Status(IStatus.ERROR, Plugin.PLUGIN_ID, 0, "Bnd launcher was not initialised.", null));
         return bndLauncher;
     }
 
 }
