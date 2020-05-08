 package com.smarthome;
 
 public class LightGesture extends Gesture {
 	private boolean on = false;
 	private String action;
 	
 	public LightGesture() {}
 	
 	public LightGesture(float x1, float y1, float x2, float y2, String action) {
 		super(x1, y1, x2, y2);
 		this.action = action;
 	}
 	public void click(SmartHomeActivity activity) {
 		System.out.println("Light "+(on ? "on" : "off"));
 		on = !on;
 		sendMessageToProxy send = new sendMessageToProxy();
		send.execute("172.16.0.200", "12349", "LP.LIGHTCONTROL", "topic", JSONBuilder.light(action, on ? 255 : 0, on ? 255 : 0, on ? 255 : 0, 0));	
 	}
 }
