 package bouncingball;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JPanel;
 
 import plugin.Plugin;
 
 /**
  * An extension plugin.
  * 
  * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
  *
  */
 public class BouncingBallPlugin extends Plugin {
 	public static final String PLUGIN_ID = "Bouncing Ball";
 	
 	private BBPanel panel;
 	private PluginState state;
 	
 	public BouncingBallPlugin() {
 		
 		super(PLUGIN_ID);
 		state= PluginState.STOPPED;
 	}
 
 	@Override
 	public void layout(JPanel parentPanel) {
 		parentPanel.setLayout(new BorderLayout());
 		panel = new BBPanel();
 		parentPanel.add(panel);
 		state= PluginState.STOPPED;
 	}
 
 	@Override
 	public void start() {
 		// Not much to do here
 		state= PluginState.RUNNING;
 	}
 
 	@Override
 	public void stop() {
 		state= PluginState.STOPPED;
 		panel.stop();
 	}
 	
 	// For now we need to declare dummy main method
 	// to include in manifest file
 	public static void main(String[] args) {
 	}
 
 	@Override
 	public void pause() {
 		state= PluginState.PAUSED;
 		panel.stop();
 		
 	}
 
 	@Override
 	public void load() {
 		// TODO Auto-generated method stub
 		state= PluginState.STOPPED;
 		
 	}
 
 	@Override
 	public PluginState getState() {
 		// TODO Auto-generated method stub
 		return state;
 	}
 }
