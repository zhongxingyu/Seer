 //-------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //-------------------------------------------------------------------------
 
 package cytoscape.actions;
 
 import java.awt.event.ActionEvent;
 import javax.swing.AbstractAction;
 
 import cytoscape.giny.*;
 
 import cytoscape.dialogs.preferences.*;
 import cytoscape.Cytoscape;
 import cytoscape.view.CyNetworkView;
 import cytoscape.util.*;
 import cytoscape.util.CytoscapeAction;
 
 public class PreferenceAction extends CytoscapeAction  {

    public final static String MENU_LABEL = "Preferences...";
     
     private PreferencesDialog preferencesDialog = null;
     
     public PreferenceAction () {
        super (MENU_LABEL);
         setPreferredMenu( "Edit" );
     }
 
     public void actionPerformed(ActionEvent e) {
 	if (preferencesDialog == null) {
             preferencesDialog = new PreferencesDialog(
 			Cytoscape.getDesktop().getMainFrame());
 	}
 	preferencesDialog.refresh();
         preferencesDialog.setVisible(true);
 
     } // actionPerformed
 }
 
