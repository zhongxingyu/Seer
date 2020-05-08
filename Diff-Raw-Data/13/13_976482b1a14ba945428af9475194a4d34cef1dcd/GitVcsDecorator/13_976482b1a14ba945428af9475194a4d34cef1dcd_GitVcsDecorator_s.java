 package kkckkc.jsourcepad.vcs;
 
 import com.google.common.base.Function;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Project;
 import kkckkc.jsourcepad.model.bundle.EnvironmentProvider;
 import kkckkc.jsourcepad.ui.FileTreeModel;
 import kkckkc.jsourcepad.util.io.ErrorDialog;
 import kkckkc.jsourcepad.util.io.ScriptExecutor;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class GitVcsDecorator extends AbstractVcsDecorator {
 
     private Project project;
     private List<Request> requestQueue = Lists.newArrayList();
     private Lock requestQueueLock = new ReentrantLock();
     private RequestProcessor requestProcessor = new RequestProcessor();
 
     @Autowired
     public void setProject(Project project) {
         this.project = project;
     }
 
     @Override
     protected void getStates(final FileTreeModel.Node parent, final FileTreeModel.Node[] children, final Function<VcsState[], Void> continuation) {
         File gitRoot = getGitRoot(parent.getFile());
         if (gitRoot == null) return;
 
         synchronized (requestQueue) {
             requestQueue.add(new Request(gitRoot, children, continuation));
         }
 
         Application.get().getThreadPool().submit(requestProcessor);
     }
 
     private File getGitRoot(File file) {
         File dir = file;
         while (dir != null) {
             if (dir.isDirectory() && new File(dir, ".git").exists()) return dir;
             if (project.getProjectDir().equals(dir)) break;
             dir = dir.getParentFile();
         }
         return null;
     }
 
     private VcsState getState(String status) {
         if (status == null) return null;
         if (status.startsWith("?")) return VcsState.UNKNOWN;
         if (status.equals(" M") ||
             status.equals("M ")) return VcsState.MODIFIED;
         if (status.equals("A ") ||
             status.equals("AM") ||
             status.equals("AD")) return VcsState.ADDED;
         if (status.equals("R") ||
             status.equals("RM") ||
             status.equals("RD")) return VcsState.MODIFIED;
         if (status.equals("C ") ||
             status.equals("CM") ||
             status.equals("CD")) return VcsState.MODIFIED;
         if (status.equals("D ") ||
             status.equals("DM")) return VcsState.DELETED;
         if (status.equals("DD") ||
             status.equals("AU") ||
             status.equals("UD") ||
             status.equals("UA") ||
             status.equals("DU") ||
             status.equals("AA") ||
             status.equals("UU")) return VcsState.CONFLICT;
         return null;
     }
 
     class Request {
         private File gitRoot;
         private FileTreeModel.Node[] children;
         private Function<VcsState[],Void> continuation;
 
         public Request(File gitRoot, FileTreeModel.Node[] children, Function<VcsState[], Void> continuation) {
             this.gitRoot = gitRoot;
             this.children = children;
             this.continuation = continuation;
         }
 
         public File getGitRoot() {
             return gitRoot;
         }
 
         public FileTreeModel.Node[] getChildren() {
             return children;
         }
 
         public Function<VcsState[], Void> getContinuation() {
             return continuation;
         }
     }
 
     class RequestProcessor implements Runnable {
         private Lock processorLock = new ReentrantLock();
 
         @Override
         public void run() {
             // Ensure only one RequestProcessor will run at the same time
             processorLock.lock();
             try {
                 File gitRoot = null;
 
                 // Abort if the queue is empty
                 synchronized (requestQueue) {
                     if (requestQueue.isEmpty()) return;
                     gitRoot = requestQueue.get(0).getGitRoot();
                 }
 
                 // Get the actual statuses
                 Map<File, String> statuses = Maps.newHashMap();
                 executeGit(gitRoot, statuses);
 
                 // Move requests with same root another list
                 // This operation should be quick and thus locking should be fine
                 List<Request> requestsWithSameRoot = Lists.newArrayList();
                 synchronized (requestQueue) {
                     Iterator<Request> it = requestQueue.iterator();
                     while (it.hasNext()) {
                         Request request = it.next();
                         if (request.getGitRoot().equals(gitRoot)) {
                             it.remove();
                             requestsWithSameRoot.add(request);
                         }
                     }
                 }
 
                 // Update the state of all nodes of all applicable requests
                 for (Request request : requestsWithSameRoot) {
                     FileTreeModel.Node[] children = request.getChildren();
                     VcsState[] states = new VcsState[children.length];
                     for (int i = 0; i < children.length; i++) {
                         states[i] = getState(statuses.get(children[i].getFile()));
 
                     }
                     request.getContinuation().apply(states);
                 }
 
             } finally {
                 processorLock.unlock();
             }
         }
 
         private void executeGit(final File gitRoot, final Map<File, String> statuses) {
             ScriptExecutor scriptExecutor = new ScriptExecutor(
                     "git status --porcelain",
                     Application.get().getThreadPool());
             scriptExecutor.setDelay(0);
             scriptExecutor.setDirectory(gitRoot);
             scriptExecutor.setShowStderr(false);
 
             try {
                 ScriptExecutor.Execution execution = scriptExecutor.execute(new ScriptExecutor.CallbackAdapter() {
                     @Override
                     public void onSuccess(ScriptExecutor.Execution execution) {
                         for (String line : Splitter.on("\n").omitEmptyStrings().split(execution.getStdout())) {
                             String status = line.substring(0, 2);
                             String file = line.substring(3);
                             statuses.put(new File(gitRoot, file), status);
                         }
                     }
 
                     @Override
                    public void onFailure(ScriptExecutor.Execution execution) {
                        ErrorDialog errorDialog = Application.get().getErrorDialog();
                        errorDialog.show("Script Execution Failed...", execution.getStderr(), null);
                     }
                 }, new StringReader(""), EnvironmentProvider.getStaticEnvironment());
 
                 execution.waitForCompletion();
             } catch (IOException ioe) {
                 throw new RuntimeException(ioe);
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             } catch (ExecutionException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 }
