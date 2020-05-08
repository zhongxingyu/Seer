 import java.awt.Canvas;
 import java.awt.event.ActionListener;
 
 
 public class PluginAPI {
 	private IPlugin plugin;
 	private EventManager em;
 	private /*TODO*/Object ui;
 	
 	/**
 	 * One PluginAPI object is created per plugin
 	 * 
 	 * @param plugin
 	 * @param em
 	 * @param ui
 	 */
 	public PluginAPI(IPlugin plugin, EventManager em, /*TODO*/Object ui) {
 		this.plugin = plugin;
 		this.em = em;
 		this.ui = ui;
 	}
 	
 	/**
 	 * Registers an event on behalf of the associated plugin
 	 * 
 	 * @param eventName The name of the event to listen for
 	 * @param al The listener for the event
 	 * @return true if registration is successful, false otherwise
 	 */
 	public boolean registerEvent(String eventName, ActionListener al) {
 		return em.registerEvent(eventName, plugin.getId(), al);
 	}
 	
 	/**
 	 * Unregisters all handlers from the associated plugin for the given event
 	 * 
 	 * @param eventName Event to remove listeners for
 	 * @return true if unregistration successful, false otherwise
 	 */
 	public boolean unregisterEvent(String eventName) {
		return em.unregisterEvent(eventName, plugin.getId());
 	}
 	
 	/**
 	 * Gets the canvas so the plugin can draw on it
 	 * 
 	 * @return The plugin-accessable canvas
 	 */
 	public Canvas getCanvas() {
 		return null; //TODO return ui.canvas;
 	}
 	
 	/**
 	 * Displays a status message on the application status bar
 	 * @param status The status message to display
 	 */
 	public void displayStatus(String status) {
 		//TODO ui.statusbar.setText(status);
 	}
 	
 }
