 /**
  * 
  */
 package cytoscape.actions;
 
 import java.awt.event.ActionEvent;
 
 import cytoscape.Cytoscape;
 
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.util.CytoscapeAction;
 
 import cytoscape.dialogs.plugins.PluginUpdateDialog;
 
 import cytoscape.plugin.PluginInfo;
 import cytoscape.plugin.PluginManager;
 import cytoscape.plugin.ManagerException;
 import cytoscape.plugin.PluginStatus;
 
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 public class PluginUpdateAction extends CytoscapeAction {
 	public PluginUpdateAction() {
 		super("Update Plugins");
 		setPreferredMenu("Plugins");
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		PluginUpdateDialog Dialog = new PluginUpdateDialog(Cytoscape
 				.getDesktop());
 
 		if (!PluginManager.usingWebstartManager()) {
 			boolean updateFound = false;
 			PluginManager Mgr = PluginManager.getPluginManager();
 			// Find updates
 			for (PluginInfo Current : Mgr.getPlugins(PluginStatus.CURRENT)) {
 
 			// Configure JTask Dialog Pop-Up Box
 			JTaskConfig jTaskConfig = new JTaskConfig();
 			jTaskConfig.setOwner(Cytoscape.getDesktop());
 			jTaskConfig.displayCloseButton(false);
 			jTaskConfig.displayStatus(true);
 			jTaskConfig.setAutoDispose(true);
 			jTaskConfig.displayCancelButton(false);
 
 				
 				try {
 					List<PluginInfo> Updates = Mgr.findUpdates(Current, jTaskConfig);
 					if (Updates.size() > 0) {
 						Dialog.addCategory(Current.getCategory(), Current,
 								Updates);
 						updateFound = true;
 					}
 				} catch (org.jdom.JDOMException jde) {
 					System.err.println("Failed to retrieve updates for "
 							+ Current.getName() + ", XML incorrect at "
 							+ Current.getDownloadUrl());
 					System.err.println(jde.getMessage());
 					// jde.printStackTrace();
 				} catch (java.io.IOException ioe) {
 					System.err.println("Failed to read XML file for "
 							+ Current.getName() + " at "
 							+ Current.getDownloadUrl());
 					ioe.printStackTrace();
 				}
 
 			}
 			if (updateFound) {
 				Dialog.setVisible(true);
 			} else {
 				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
						"No updates avaialbe for currently installed plugins.",
 						"Plugin Updates", JOptionPane.INFORMATION_MESSAGE);
 			}
 		} else {
 			JOptionPane
 					.showMessageDialog(
 							Cytoscape.getDesktop(),
 							"Plugin updates are not available when using Cytoscape through webstart",
 							"Plugin Update", JOptionPane.INFORMATION_MESSAGE);
 		}
 	}
 
 }
