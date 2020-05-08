 package net.chunxie.snapshotexplorer.action;
 
 import net.chunxie.networkdata.api.SnapshotService;
 import net.chunxie.networkdata.entity.*;
import net.chunxie.networkgraph.cn.CollaborativeTopComponent;
 import net.chunxie.snapshotexplorer.SnapshotExplorerTopComponent;
 import net.chunxie.snapshotexplorer.node.RootNode;
 import net.chunxie.snapshotexplorer.node.factory.SnapshotChildFactory;
 import org.netbeans.api.progress.ProgressHandle;
 import org.netbeans.api.progress.ProgressHandleFactory;
 import org.openide.nodes.Children;
 import org.openide.util.Cancellable;
 import org.openide.util.RequestProcessor;
 import org.openide.util.Task;
 import org.openide.util.TaskListener;
 
 import javax.swing.SwingUtilities;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author chun.xie
  * @todo Write some documentation!
  * @created 07/02/13 13:06
  */
 public class AcquireDataAction implements ActionListener {
 
     private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
     private RequestProcessor.Task theTask = null;
     private SnapshotExplorerTopComponent parent = null;
 
     public AcquireDataAction(SnapshotExplorerTopComponent topComponent) {
         this.parent = topComponent;
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         parent.getLoadButton().setEnabled(false);
         final ProgressHandle ph = ProgressHandleFactory.createHandle("loading snapshot data...", new Cancellable() {
 
             @Override
             public boolean cancel() {
                 return handleCancel();
             }
         });
 
         String yearRaw = (String) parent.getYearCombo().getSelectedItem();
         Integer monthRaw = parent.getMonthCombo().getSelectedIndex();
         Forum forumRaw = (Forum) parent.getForumCombo().getSelectedItem();
 
         Integer year = Integer.parseInt(yearRaw);
         Integer month = monthRaw + 1;
         Integer duration = parent.getDurationCombo().getSelectedIndex() + 1;
 
         // set up the conditions of a snapshot
         final Snapshot snapshot = new Snapshot(year, month, duration, -1);
         Forum f = new Forum(forumRaw.getForumuri());
         f.setForumtitle(forumRaw.getForumtitle());
         snapshot.setForum(f);
 
         String key = snapshot.toString();
         if (SnapshotExplorerTopComponent.snapshotMap.containsKey(key)) {
             parent.treeview.setVisible(true);
             parent.getLoadButton().setEnabled(true);
         } else {
             final SnapshotService service = parent.service;
             final List<ForumThread> resultList = service.findSnapshotThreads(snapshot);
             f.setThreads(resultList);
 
             Runnable runnable = new Runnable() {
 
                 private final int NUM = resultList.size();
 
                 @Override
                 public void run() {
                     ph.start(); //we must start the PH before we swith to determinate
                     ph.switchToDeterminate(NUM);
                     Map<String, Contributor> map = snapshot.getSnapshotContributors();
                     for (int i = NUM - 1; i >= 0; i--) {
                         Map<String, Contributor> iteratorMap = new HashMap<String, Contributor>();
                         ForumThread thread = resultList.get(i);
                         List<ForumMessage> messages = service.findSnapshotMessagesWithinThread(snapshot, thread);
                         thread.setMessages(messages);
                         for (ForumMessage m : messages) {
                             Contributor c = m.getOriginContributor();
                             String key = c.getContributor();
                             iteratorMap.put(key, c);
                         }
 
                         if (iteratorMap.size() > 1) {
                             for (ForumMessage m : messages) {
                                 Integer point = m.getAwardedpoints();
                                 Contributor c = m.getOriginContributor();
                                 String key = c.getContributor();
                                 if (map.containsKey(key)) {
                                     Contributor tmp = map.get(key);
                                     tmp.setPartialPoints(tmp.getPartialPoints() + point);
                                 } else {
                                     c.setPartialPoints(point);
                                     map.put(key, c);
                                 }
                                 snapshot.getSnapshotMessages().add(m);
                             }
                         } else {
                             // remove the thread from the list
                             resultList.remove(i);
                         }
                         ph.progress(NUM - i);
                     }
 
                 }
             };
 
             theTask = RP.create(runnable); //the task is not started yet
             theTask.addTaskListener(new TaskListener() {
 
                 @Override
                 public void taskFinished(Task task) {
                     ph.finish();
                     SnapshotExplorerTopComponent.snapshotMap.put(snapshot.toString(), snapshot);
                     parent.getExplorerManager().setRootContext(new RootNode(Children.create(new SnapshotChildFactory(SnapshotExplorerTopComponent.snapshotMap), true)));
                     parent.treeview.setVisible(true);
                     parent.getLoadButton().setEnabled(true);
                     //Need this call to be run on the EDT (This is where Netbeans barks at you
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                            CollaborativeTopComponent cnc = CollaborativeTopComponent.getComponent(snapshot, null);
                             cnc.setName("Collaborative Network(" + snapshot.getShortDesc() + ")");
                             cnc.open();
                            cnc.requestActive();
                         }
                     });
                 }
             });
             theTask.schedule(0); //start the task
         }
     }
 
     private boolean handleCancel() {
         if (null == theTask) {
             parent.getLoadButton().setEnabled(true);
             return false;
         }
         return theTask.cancel();
     }
 }
