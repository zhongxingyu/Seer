 /**
  * 
  */
 package cz.zcu.kiv.kc.shell;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import cz.zcu.kiv.kc.plugin.Plugin;
 
 /**
  * @author 2540p
  *
  */
 public class PluginButtonListener implements ActionListener {
 	
 	private Plugin plugin;	
 	private ShellController controller;
 
 	public PluginButtonListener(Plugin plugin, ShellController controller) {
 		super();
 		this.plugin = plugin;
 		this.controller = controller;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
		plugin.executeAction(controller.getSelectedFiles(), controller.getDestiantionPath(), controller.getSourcePath());		
 	}
 
 }
