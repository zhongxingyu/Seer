 package ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 
import plugin.IPlugin;
 import extension.IPluginListenerExtension;
 
 public class BasicBrahmaUI implements IPluginListenerExtension {
 	private JFrame frame;
	private List<IPlugin> plugins;
 	
 	public BasicBrahmaUI() {
		this.plugins = new ArrayList<IPlugin>();
 		// TODO: Make a frame class. this.frame = ?;
 	}
 	
 	@Override
	public void pluginLoaded(IPlugin plugin) {
 		this.plugins.add(plugin);
 		// TODO: Ad to frame
 	}
 
 	@Override
	public void pluginUnloaded(IPlugin plugin) {
 		this.plugins.remove(plugin);
 		// TODO: Remove from frame
 	}
 	
 	
 
 }
