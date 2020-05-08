 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
package ${packageInPathFormat};
 
 import cytoscape.Cytoscape;
 import cytoscape.util.CytoscapeAction;
 import java.awt.event.ActionEvent;
 import javax.swing.JOptionPane;
 
 /**
  * A simple action.  Change the names as appropriate and
  * then fill in your expected behavior in the actionPerformed()
  * method.
  */
 public class MyAction extends CytoscapeAction {
 
 		private static final long serialVersionUID = 1234567890123456789L;
 
 		public MyAction() {
 			// Give your action a name here
 			super("Sample plugin");
 
 			// Set the menu you'd like here.  Plugins don't need
 			// to live in the Plugins menu, so choose whatever
 			// is appropriate!
 	        setPreferredMenu("Plugins");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			// Do something useful here!
 			JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Executed the Sample plugin!");	
 		}
 }
