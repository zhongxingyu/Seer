 package ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 
import plugin.ILoadableApplication;
 import extension.IPluginListenerExtension;
 
 public class BasicBrahmaUI implements IPluginListenerExtension {
 	private JFrame frame;
	private List<ILoadableApplication> plugins;
 	
 	public BasicBrahmaUI() {
		this.plugins = new ArrayList<ILoadableApplication>();
 		// TODO: Make a frame class. this.frame = ?;
 	}
 	
 	@Override
	public void pluginLoaded(ILoadableApplication plugin) {
 		this.plugins.add(plugin);
 		// TODO: Ad to frame
 	}
 
 	@Override
	public void pluginUnloaded(ILoadableApplication plugin) {
 		this.plugins.remove(plugin);
 		// TODO: Remove from frame
 	}
 	
 	
 
 }
