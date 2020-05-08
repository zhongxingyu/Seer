 package control.stage;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import second.prototype.ContainerBox;
 
 import android.util.Log;
 
 /**
  * This class manages a single point
  * 
  * @author vincentlee
  *
  */
 
 public class PointBox {
 	public float x,y;
 	public int order;
 	public boolean inRange = false;
 	public boolean isVisible = false;
 	public boolean hasVisited = false;
 	private String name;
 	private String story;
 	private final int bLeng = ContainerBox.isTab?18:10;
 	
 	public HashMap<String,String> media = new HashMap<String,String>();
 	
 	public ArrayList<GameEvent> eventList = new ArrayList<GameEvent>();
 	
 	public PointBox(JSONObject json) throws NumberFormatException, JSONException {
 		x = Float.parseFloat(json.getString("Position X"));
 		y = Float.parseFloat(json.getString("Position Y"));
 		name = json.getString("Name");
 		story = json.getString("Story");
 		order = json.getInt("Order");
 		
 		media.put("Image",json.optString("Image"));
 		media.put("Movie",json.optString("Movie"));
 		
 		for(int i=0;i<json.getJSONArray("Events").length();i++){
 			GameEvent event = new GameEvent(json.getJSONArray("Events").getJSONObject(i));
 			eventList.add(event);
 		}
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getStory() {
 		return story;
 	}
 	
 	public String getBrief() {
		return story.substring(0, bLeng)+" ...";
 	}
 	
 	public void checkVisable(int progress) {
 		isVisible =  (progress>order);
 	}
 	
 	public void checkRange(float myX, float myY, float range) {
 		double distance = Math.pow((myX-x), 2.0) + Math.pow((myY-y), 2.0);
 		inRange = (distance<Math.pow(range, 2.0));
 	}
 	
 	public String[] getNextPoints() {
 		String[] names = new String[eventList.size()];
 		for(int i=0;i<names.length;i++){
 			names[i] = eventList.get(i).nextPoint;
 		}
 		return names;
 	}
 	
 	public int numOfEvents() {
 		return eventList.size();
 	}
 	
 	public void onClick() {
 		// TODO
 		Log.e("Point event",name+" Clicked");
 		hasVisited = true;
 		
 	}
 }
