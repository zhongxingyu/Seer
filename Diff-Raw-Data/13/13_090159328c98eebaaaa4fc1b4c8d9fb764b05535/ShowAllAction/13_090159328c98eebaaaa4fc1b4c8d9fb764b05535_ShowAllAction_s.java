 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 //-------------------------------------------------------------------------
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 
 import cytoscape.view.NetworkView;
 //-------------------------------------------------------------------------
 public class ShowAllAction extends AbstractAction {
     NetworkView networkView;
     
     public ShowAllAction(NetworkView networkView) {
         super();
         this.networkView = networkView;
     }
     
     public void actionPerformed(ActionEvent e) {
         GinyUtils.unHideAll(networkView.getView());
        networkView.redrawGraph(false, true);
     }
 }
 
