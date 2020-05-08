 package org.reactome.cytoscape3;
 
 /**
  * This class manages the menu action performed
  * when the user clicks User Guide. Currently it
  * links to the online user guide, but all of the
  * pieces are in place to create an offline user
  * guide except for the actual user guide document
  * itself.
  * @author Eric T Dawson
  */
 import java.awt.event.ActionEvent;
 
 import org.cytoscape.application.swing.AbstractCyAction;
 
 public class UserGuideAction extends AbstractCyAction {
 
     // There is no need to cache this URL since it should be managed by PlugInScopeObjectManager
 //    private String userGuideURL = PlugInScopeObjectManager.getManager().getUserGuideURL();
 
     public UserGuideAction() {
         //Add the 'User Guide' item to the ReactomeFI menu
         super("User Guide");
        setPreferredMenu("Apps.Reactome FI");
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         //Pop open a browser window pointing to the
         //online Reactome FI app user guide.
         String url = PlugInScopeObjectManager.getManager().getProperties().getProperty("userGuideURL");
         if (url == null) {
             PlugInUtilities.showErrorMessage("Error in Showing User Guide",
                                              "User guide URL has not set up. No user guide can be shown.");
             return;
         }
         PlugInUtilities.openURL(url);
         
         //Pop up a JEditorPane which contains the offline
         //user guide.
         //	ActionDialogs gui = new ActionDialogs("UGA");
         //	gui.setLocationRelativeTo(desktopApp.getJFrame());
         //	gui.setModal(true);
         //	gui.setVisible(true);
     }
 
 }
