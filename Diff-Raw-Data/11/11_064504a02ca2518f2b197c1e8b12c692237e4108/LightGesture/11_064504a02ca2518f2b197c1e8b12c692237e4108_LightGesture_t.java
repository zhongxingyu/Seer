 package com.smarthome;
 
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 
 
 public class LightGesture extends Gesture {
 	private boolean on = false;
 	private String action;
 	
 	public LightGesture() {}
 	
 	public LightGesture(float x1, float y1, float x2, float y2, String action) {
 		super(x1, y1, x2, y2);
 		this.action = action;
 	}
 	public void click(SmartHomeActivity activity, boolean isLong) {
 		System.out.println("Light "+(on ? "on" : "off"));
 		on = !on;
 		
 		for (LightController lc : LightController.byRole(action)) {
 			lc.light.setPower(on ? 5f : 0f);
 		}
 		
 		try {
 			sendMessageToProxy send = new sendMessageToProxy();
 //			send.execute("172.16.0.200", "12349", "LP.LIGHTCONTROL", "topic", JSONBuilder.light(action, on ? 255 : 0, on ? 255 : 0, on ? 255 : 0, 0));	
 			send.execute("172.16.0.200", "12349", "LP.LIGHTCONTROL", "topic", JSONBuilder.light(action, on, 255));
 		} catch(Exception e) {
 			System.out.println("Senden Fehlgeschlagen");
 			on = !on;
 		}
 		super.click(activity, isLong);
 	}
 	public String toString() {
 		return super.toString() + " to light " + action + " (currently " + (on ? "on" : "off") + ")";
 	}
 	public void appear(SmartHomeActivity activity) {
 		if (images.size() == 0) {
 	        ImageView image = new ImageView(activity);
 	        image.setImageResource(R.drawable.lamp);
 	        activity.prepareImage((int)x1, (int)y1, (int)x2, (int)y2, image, 128, 128);
 	        image.setScaleType(ScaleType.FIT_XY);
	        images.add(image); 
 		}
 		activity.imagePane.addView(images.get(0));
 	}
 	public void disappear(SmartHomeActivity activity) {
 		activity.imagePane.removeView(images.get(0));
 	}
 }
