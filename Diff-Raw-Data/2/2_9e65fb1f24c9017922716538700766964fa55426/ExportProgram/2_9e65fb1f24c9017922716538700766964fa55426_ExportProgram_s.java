 package de.unisb.cs.depend.ccs_sem.plugin.actions;
 
 import java.io.File;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 import de.unisb.cs.depend.ccs_sem.exceptions.ExportException;
 import de.unisb.cs.depend.ccs_sem.exporters.Exporter;
 import de.unisb.cs.depend.ccs_sem.plugin.Global;
 import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSDocument;
 import de.unisb.cs.depend.ccs_sem.plugin.editors.CCSEditor;
 import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob;
 import de.unisb.cs.depend.ccs_sem.plugin.jobs.EvaluationJob.EvaluationStatus;
 import de.unisb.cs.depend.ccs_sem.plugin.views.CCSGraphView;
 import de.unisb.cs.depend.ccs_sem.semantics.types.Program;
 
 public class ExportProgram extends Action {
 
 
     public class ExporterJob extends Job {
 
         private final class SchedulingRule implements ISchedulingRule {
 
             private final File ruleFile;
 
             public SchedulingRule(File file) {
                 this.ruleFile = file;
             }
 
             public boolean isConflicting(ISchedulingRule rule) {
                 return contains(rule);
             }
 
             public boolean contains(ISchedulingRule rule) {
                 return getClass().equals(rule.getClass()) &&
                     ruleFile.equals(((SchedulingRule)rule).ruleFile);
             }
         }
 
         protected final File file;
         private final EvaluationStatus status;
 
         public ExporterJob(final File filename, EvaluationStatus status) {
             super("CCS Exporter");
             this.file = filename;
             this.status = status;
             setUser(true);
             setRule(new SchedulingRule(filename));
         }
 
         @Override
         protected IStatus run(IProgressMonitor monitor) {
             if (monitor != null)
                 monitor.beginTask("Exporting to " + file.getAbsolutePath(), IProgressMonitor.UNKNOWN);
             final Program program = status.getCcsProgram();
             final FutureTask<IStatus> future = new FutureTask<IStatus>(new Callable<IStatus>() {
                 public IStatus call() throws Exception {
                     try {
                         exporter.export(file, program);
                         return Status.OK_STATUS;
                     } catch (final ExportException e) {
                         return new Status(IStatus.WARNING, Global.getPluginID(),
                             "Could not export to " + file + ": " + e.getMessage());
                     }
                 }
             });
             final Thread thread = new Thread(future, "ccs export");
             thread.start();
             try {
                 return future.get(100, TimeUnit.MILLISECONDS);
             } catch (final InterruptedException e) {
                 future.cancel(true);
                 return new Status(IStatus.ERROR, Global.getPluginID(), "Interrupted");
             } catch (final ExecutionException e) {
                 return new Status(IStatus.ERROR, Global.getPluginID(), "Error exporting: " + e.getMessage());
             } catch (final TimeoutException e) {
                 if (monitor.isCanceled()) {
                     future.cancel(true);
                     try {
                         thread.join();
                     } catch (final InterruptedException e1) {
                         // ok, then give up
                     }
                 }
                 return Status.CANCEL_STATUS;
             } finally {
                 if (monitor != null)
                     monitor.done();
             }
         }
 
     }
 
     protected final Exporter exporter;
     private final String[] preferredExtensions;
     private final String[] preferredExtensionDescriptions;
 
     public ExportProgram(String label, Exporter exporter,
             String[] preferredExtensionsAndDesc) {
         super(label, AS_PUSH_BUTTON);
         if (preferredExtensionsAndDesc.length % 2 != 0)
             throw new IllegalArgumentException("preferredExtensions must have equals count of elements");
         this.exporter = exporter;
         preferredExtensions = new String[preferredExtensionsAndDesc.length/2+1];
         preferredExtensionDescriptions = new String[preferredExtensionsAndDesc.length/2+1];
         for (int i = 0; i < preferredExtensionsAndDesc.length; ++i)
             (i % 2 == 0 ? preferredExtensions : preferredExtensionDescriptions)[i/2] =
                 preferredExtensionsAndDesc[i];
         preferredExtensions[preferredExtensions.length-1] = Global.isWindows() ? "*.*" : "*";
         preferredExtensionDescriptions[preferredExtensionDescriptions.length-1] = "All files";
     }
 
     @Override
     public void run() {
         final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         final IWorkbenchPage activePage = activeWorkbenchWindow == null ? null : activeWorkbenchWindow.getActivePage();
         final IEditorPart editor = activePage == null ? null : activePage.getActiveEditor();
         if (!(editor instanceof CCSEditor))
             return;
 
         final CCSEditor ccsEditor = (CCSEditor) editor;
         final IDocument doc = ccsEditor.getDocument();
         if (!(doc instanceof CCSDocument))
             return;
 
         final CCSDocument ccsDoc = (CCSDocument) doc;
 
         final IWorkbenchPart graphFrame = activePage.findView(Global.getGraphViewId());
         boolean minimize = false;
         if (graphFrame instanceof CCSGraphView)
             minimize = ((CCSGraphView)graphFrame).isMinimize();
 
         final FileDialog saveDialog = new FileDialog(activeWorkbenchWindow.getShell(), SWT.SAVE);
         saveDialog.setText("Export to file...");
 
         saveDialog.setFilterNames(preferredExtensionDescriptions);
         saveDialog.setFilterExtensions(preferredExtensions);
         final String filename = saveDialog.open();
 
         final EvaluationJob evalJob = new EvaluationJob(ccsDoc.get(), minimize);
         evalJob.addJobChangeListener(new JobChangeAdapter() {
             @Override
             public void done(IJobChangeEvent event) {
                 final IStatus result = event.getResult();
                 if (result instanceof EvaluationStatus) {
                     final EvaluationStatus status = (EvaluationStatus) result;
                     if (status.getWarning() != null) {
                         MessageDialog.openWarning(activeWorkbenchWindow.getShell(),
                             "Could not export",
                             "Could not export to " + filename + ": " + status.getWarning());
                     } else if (!status.isOK()) {
                         MessageDialog.openWarning(activeWorkbenchWindow.getShell(),
                             "Could not export",
                             "Could not export to " + filename + ": parsing error");
                     } else if (status.getCcsProgram() == null) {
                         MessageDialog.openWarning(activeWorkbenchWindow.getShell(),
                             "Could not export",
                             "Could not export to " + filename + ": program could not be parsed");
                     } else {
                         final ExporterJob exporterJob = new ExporterJob(new File(filename), status);
                         exporterJob.schedule();
                     }
                 }
             }
         });
         evalJob.schedule();
     }
 
 }
