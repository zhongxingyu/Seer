 package net.sourceforge.eclipseccase.ui.actions;
 
 import java.util.Arrays;
 
 import net.sourceforge.eclipseccase.ClearcasePlugin;
 import net.sourceforge.eclipseccase.ClearcaseProvider;
 import net.sourceforge.eclipseccase.ui.CommentDialog;
 import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.window.Window;
 import org.eclipse.team.internal.ui.actions.TeamAction;
 
 public class CheckInAction extends ClearcaseWorkspaceAction {
 
     /*
      * Method declared on IActionDelegate.
      */
     public void run(IAction action) {
         String maybeComment = "";
         int maybeDepth = IResource.DEPTH_ZERO;
 
         if (!ClearcasePlugin.isUseClearDlg()
                 && ClearcasePlugin.isCommentCheckin()) {
             CommentDialog dlg = new CommentDialog(getShell(), "Checkin comment");
             if (dlg.open() == Window.CANCEL) return;
             maybeComment = dlg.getComment();
             maybeDepth = dlg.isRecursive() ? IResource.DEPTH_INFINITE
                     : IResource.DEPTH_ZERO;
         }
 
         // bug 859094 and feature request 718203: prompt for saving dirty
         // editors
         boolean canContinue = true;
         IFile[] unsavedFiles = getUnsavedFiles();
         if (unsavedFiles.length > 0)
                 canContinue = saveModifiedResourcesIfUserConfirms(unsavedFiles);
 
         if (canContinue) {
             final String comment = maybeComment;
             final int depth = maybeDepth;
 
             IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 
                 public void run(IProgressMonitor monitor) throws CoreException {
 
                     try {
                         IResource[] resources = getSelectedResources();
                         if (resources.length > 0) {
                             beginTask(monitor, "Checking in...",
                                     resources.length);
 
                             if (ClearcasePlugin.isUseClearDlg()) {
                                 monitor
                                         .subTask("Executing ClearCase user interface...");
                                ClearDlgHelper.add(resources);
                             } else {
                                 // Sort resources with directories last so that
                                 // the
                                 // modification of a
                                 // directory doesn't abort the modification of
                                 // files
                                 // within it.
                                 Arrays.sort(resources,
                                         new DirectoryLastComparator());
 
                                 for (int i = 0; i < resources.length; i++) {
                                     IResource resource = resources[i];
                                     ClearcaseProvider provider = ClearcaseProvider
                                             .getClearcaseProvider(resource);
                                     provider.setComment(comment);
                                     provider.checkin(
                                             new IResource[] { resource },
                                             depth, subMonitor(monitor));
                                 }
                             }
                         }
                     } finally {
                         monitor.done();
                     }
                 }
             };
 
             executeInForeground(runnable, TeamAction.PROGRESS_DIALOG,
                     "Checking in ClearCase resources");
         }
 
         updateActionEnablement();
     }
 
     protected boolean isEnabled() {
         IResource[] resources = getSelectedResources();
         if (resources.length == 0) return false;
         for (int i = 0; i < resources.length; i++) {
             IResource resource = resources[i];
             ClearcaseProvider provider = ClearcaseProvider
                     .getClearcaseProvider(resource);
             if (provider == null || provider.isUnknownState(resource)
                     || provider.isIgnored(resource)
                     || !provider.hasRemote(resource)) return false;
             if (!provider.isCheckedOut(resource)) return false;
         }
         return true;
     }
 
 }
