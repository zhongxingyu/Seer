 /**
  * 
  */
 package AP2DX.usarsim.specialized;
 
 import AP2DX.usarsim.UsarSimMessage;
 
 /**
  * @author jjwt
  *
  */
 public final class StateMessage extends UsarSimMessage {
 	private String type;
 	private float time;
 	private float frontSteer;
 	private float rearSteer;
 	private boolean lightToggle;
 	private int lightIntensity;
 	private int battery;
 	
 	public StateMessage(UsarSimMessage msg) {
 		super(msg.getMessageString());
 		this.parseValues();
 	}
 
 	private void parseValues() {
 		this.type = (values.get("type").toString());
		this.time = Float.parseFloat(values.get("Time"));
		this.frontSteer = Float.parseFloat(values.get("FrontSteer"));
		this.rearSteer = Float.parseFloat(values.get("RearSteer"));
		this.lightToggle = Boolean.parseBoolean(values.get("LightToggle"));
		this.lightIntensity = Integer.parseInt(values.get("LightIntensity"));
		this.battery = Integer.parseInt(values.get("Battery"));	
 	}
 
 	/**
 	 * @return the time
 	 */
 	public float getTime() {
 		return time;
 	}
 
 	/**
 	 * @return the frontSteer
 	 */
 	public float getFrontSteer() {
 		return frontSteer;
 	}
 
 	/**
 	 * @return the rearSteer
 	 */
 	public float getRearSteer() {
 		return rearSteer;
 	}
 
 	/**
 	 * @return the lightToggle
 	 */
 	public boolean isLightToggle() {
 		return lightToggle;
 	}
 
 	/**
 	 * @return the lightIntensity
 	 */
 	public int getLightIntensity() {
 		return lightIntensity;
 	}
 
 	/**
 	 * @return the battery
 	 */
 	public int getBattery() {
 		return battery;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 
 }
