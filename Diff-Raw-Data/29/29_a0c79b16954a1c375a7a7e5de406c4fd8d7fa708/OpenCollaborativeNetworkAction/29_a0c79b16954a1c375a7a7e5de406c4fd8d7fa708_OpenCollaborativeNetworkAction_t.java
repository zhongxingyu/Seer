 package net.chunxie.snapshotexplorer.action;
 
 import net.chunxie.networkdata.entity.Snapshot;
import net.chunxie.networkgraph.cn.CollaborativeTopComponent;
 
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 /**
  * @author chun.xie
  * @todo Write some documentation!
  * @created 07/02/13 13:11
  */
 public class OpenCollaborativeNetworkAction extends AbstractAction {
 
     private final Snapshot snapshot;
     private static final String NETWORK_TITLE = "Collaborative Network(";
 
     public OpenCollaborativeNetworkAction(Snapshot snapshot) {
         this.snapshot = snapshot;
         putValue(Action.NAME, "Goto Collaborative Network");
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
        CollaborativeTopComponent cnc = CollaborativeTopComponent.getComponent(snapshot, null);
         cnc.setName(NETWORK_TITLE + snapshot.getShortDesc() + ")");
         cnc.open();
        cnc.requestActive();
     }
 }
