 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 //-------------------------------------------------------------------------
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 import javax.swing.JFileChooser;
 import java.io.File;
 
 import cytoscape.view.NetworkView;
 import cytoscape.data.servers.BioDataServer;
 import cytoscape.data.Semantics;
 //-------------------------------------------------------------------------
 /**
  * Action allows the loading of a BioDataServer from the gui.
  *
  * added by dramage 2002-08-20
  */
 public class LoadBioDataServerAction extends AbstractAction {
     NetworkView networkView;
     
     public LoadBioDataServerAction(NetworkView networkView) {
         super("Bio Data Server...");
         this.networkView = networkView;
     }
 
     public void actionPerformed(ActionEvent e) {
         File currentDirectory = networkView.getCytoscapeObj().getCurrentDirectory();
         JFileChooser chooser = new JFileChooser(currentDirectory);
        //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
         if (chooser.showOpenDialog (networkView.getMainFrame()) == chooser.APPROVE_OPTION) {
             currentDirectory = chooser.getCurrentDirectory();
             networkView.getCytoscapeObj().setCurrentDirectory(currentDirectory);
             String bioDataDirectory = chooser.getSelectedFile().toString();
             BioDataServer bioDataServer = null;
             //bioDataServer = BioDataServerFactory.create (bioDataDirectory);
             try {
                 bioDataServer = new BioDataServer (bioDataDirectory);
                 networkView.getCytoscapeObj().setBioDataServer(bioDataServer);
             } catch (Exception e0) {
                 String es = "cannot create new biodata server at " + bioDataDirectory;
                 networkView.getCytoscapeObj().getLogger().warning(es);
                 return;
             }
             //now that we have a bioDataServer, we probably want to use it to
             //provide naming services for the objects in the network. We delegate
             //to a static method that can handle this
             Semantics.applyNamingServices(networkView.getNetwork(),
                                           networkView.getCytoscapeObj());
             //recalculating the appearances may be necessary if the above method
             //assigned new attributes 
             networkView.redrawGraph(false, true);
         }
     }
 }
 
 
