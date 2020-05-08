 package org.jenkinsci.plugins.remote_terminal_access;
 
 import hudson.Launcher;
 import hudson.console.HyperlinkNote;
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 import hudson.model.BuildListener;
 import hudson.model.Environment;
 import hudson.model.EnvironmentList;
 import hudson.model.Result;
 import hudson.model.Run;
 import hudson.security.Permission;
 import hudson.security.PermissionGroup;
 import hudson.security.PermissionScope;
 import jenkins.model.Jenkins;
 import org.jenkinsci.plugins.remote_terminal_access.ssh.DiagnoseCommand;
 import org.kohsuke.ajaxterm.ProcessWithPty;
 import org.kohsuke.ajaxterm.Session;
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.HttpResponses;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.interceptor.RequirePOST;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * {@link Action} that attach to {@link AbstractBuild} to show the "interactive terminal" option in the UI.
  *
  * Also an {@link Environment} to make the build hang while the terminal is active.
  *
  * @author Kohsuke Kawaguchi
  */
 public class TerminalSessionAction extends Environment implements Action {
     private final AbstractBuild build;
     private final Launcher launcher;
     private final BuildListener listener;
 
     private volatile Session session;
     /**
      * Keeps track of SSH commands running on this workspace.
      */
     private final List<DiagnoseCommand> sshCommands = Collections.synchronizedList(new ArrayList<DiagnoseCommand>());
 
     public TerminalSessionAction(AbstractBuild build, Launcher launcher, BuildListener listener) {
         this.build = build;
         this.launcher = launcher;
         this.listener = listener;
     }
 
     @Override
     public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
         if (hasSession() && build.getResult()!=Result.ABORTED) {// if the build is already aborted, don't hang any further
             String url = HyperlinkNote.encodeTo("./" + getUrlName(), Messages.TerminalSessionAction_LinkedText());
             listener.getLogger().println(Messages.TerminalSessionAction_WaitingForTerminal(url));
             do {
                 Session s = session; // capture for consistency
                 if (s!=null)
                     s.join();
             } while(hasSession());
         }
 
         synchronized (sshCommands) {
             if (!sshCommands.isEmpty()) {
                 String url = HyperlinkNote.encodeTo("./" + getUrlName()+"/kill", Messages.TerminalSessionAction_KillNow());
                 listener.getLogger().println(Messages.TerminalSessionAction_WaitingForSsh(sshCommands.size(),url));
                 while (!sshCommands.isEmpty())
                     sshCommands.wait();
             }
         }
 
         return true;
     }
 
     public String getIconFileName() {
         return "monitor.png";
     }
 
     public String getDisplayName() {
         if (build.hasPermission(ACCESS))
             return Messages.TerminalSessionAction_DisplayName();
         else
             return null;    // hidden for users who don't have the permission
     }
 
     public String getUrlName() {
         return "interactiveTerminal";
     }
 
     public synchronized boolean hasSession() {
         return session!=null && session.isAlive();
     }
 
     /**
      * Starts a new terminal session if non exists.
      */
     @RequirePOST
     public HttpResponse doStartSession() throws IOException, InterruptedException {
         if (hasSession())
             return HttpResponses.redirectToDot();
         return doRestartSession();
     }
 
     /**
      * Relaunches a terminal session, even if one is live.
      */
     @RequirePOST
     public synchronized HttpResponse doRestartSession() throws IOException, InterruptedException {
         build.checkPermission(ACCESS);
 
         if (session!=null)
             session.interrupt();
 
         ProcessWithPty p = new ProcessWithPtyLauncher().shell().configure(build,listener).launch(Session.getAjaxTerm());
         session = new Session(80,25, p);
 
         return HttpResponses.redirectToDot();
     }
 
     /**
      * Handles ajaxterm update.
      */
     public void doU(StaplerRequest req, StaplerResponse rsp) throws IOException, InterruptedException {
         build.checkPermission(ACCESS);
 
         Session s = session; // capture for consistency
         if (s!=null)
             s.handleUpdate(req, rsp);
         else
             rsp.setStatus(404);
     }
 
     public void associate(DiagnoseCommand cmd) {
         synchronized (sshCommands) {
             sshCommands.add(cmd);
             sshCommands.notifyAll();
         }
     }
 
     public void unassociate(DiagnoseCommand cmd) {
         synchronized (sshCommands) {
             sshCommands.remove(cmd);
             sshCommands.notifyAll();
         }
     }
 
     /**
      * Interrupts the all pending SSH commands and terminate them all.
      */
     public HttpResponse doKill() {
         build.checkPermission(ACCESS);
         synchronized (sshCommands) {
             for (DiagnoseCommand cmd : sshCommands) {
                 cmd.destroy();
             }
         }
         return HttpResponses.forwardToPreviousPage();
     }
 
     public static TerminalSessionAction getFor(AbstractBuild<?,?> build) {
         EnvironmentList e = build.getEnvironments();
         if (e!=null)
             return e.get(TerminalSessionAction.class);
         return null;
     }
 
    public static final PermissionGroup PERMISSIONS = new PermissionGroup(Run.class, Messages._TerminalSessionAction_Permissions_Title());
     public static final Permission ACCESS = new Permission(PERMISSIONS,"Access",Messages._TerminalSessionAction_AccessPermission_Description(), Jenkins.ADMINISTER, PermissionScope.RUN);
 }
