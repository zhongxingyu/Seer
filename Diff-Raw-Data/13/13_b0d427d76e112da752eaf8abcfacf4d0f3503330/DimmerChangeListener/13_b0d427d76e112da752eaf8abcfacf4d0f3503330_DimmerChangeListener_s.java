 package gui.listeners;
 
 import javax.swing.JSpinner;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.json.JSONObject;
 
 import classes.NSocket;
 
 public class DimmerChangeListener implements ChangeListener {
 	private String location;
 	private String device;
 	
 	public DimmerChangeListener(String location, String device) {
 		this.location = location;
 		this.device = device;
 	}
 	
 	public void stateChanged(ChangeEvent e) {
 		JSpinner spinner = (JSpinner)e.getSource();
 		JSONObject json = new JSONObject();
 		JSONObject code = new JSONObject();
 		json.put("message", "send");
 		code.put("location", this.location);
 		code.put("device", this.device);
 		code.put("state", "on");
		code.put("value", spinner.getValue().toString());
 		json.put("code", code);
 		NSocket.write(json.toString());
 	}
 }
