 package structure;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import object.GameObject;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import engine.open2d.draw.Plane;
 import engine.open2d.texture.AnimatedTexture.Playback;
 
 import android.content.Context;
 import android.util.Log;
 
 public class ActionDataTool {
 	private static String NAME = "name";
 	private static String HITBOX = "hit_box";
 	private static String HURTBOX = "hurt_box";
 	private static String LEFT = "left";
 	private static String BOTTOM = "bottom";
 	private static String RIGHT = "right";
 	private static String TOP = "top";
 	private static String ACTIVE_FRAME = "active_frame";
 	private static String SELECT_BOX = "select";
 	
 	private static String X_INIT_SPEED = "x_init_speed";
 	private static String Y_INIT_SPEED = "y_init_speed";
 	private static String X_ACCEL = "x_accel";
 	private static String Y_ACCEL = "y_accel";
 	
 	private static String HIT_STOP = "hit_stop";
 	
 	private static String PLANE_WIDTH = "width";
 	private static String PLANE_HEIGHT = "height";
 	private static String PLANE_ROWS = "rows";
 	private static String PLANE_COLUMNS = "columns";
 	private static String PLANE_PLAY_BACK = "play_back";
 
 	private static String ACTION_PROPERTIES = "action_properties";
 	private static String INTERACTION_PROPERTIES = "interaction_properties";
 	
 	private static String MODIFIERS = "modifiers";
 	public static String ACTIVE_AFTER = "active_after";
 	public static String REVERSE_X = "reverse_x";
 	
 	public static String TRIGGER_CHANGE = "trigger_change";
 	public static String TRIGGER_CANCEL = "trigger_cancel";
 	public static String HIT_TRIGGER = "hit_trigger";
 	public static String WALL_TRIGGER = "wall_trigger";
 	public static String GROUND_TRIGGER = "ground_trigger";
 	public static String PLAYED_TRIGGER = "played_trigger";
 	public static String STOPPED_TRIGGER = "stopped_trigger";
 	
 	public static String SWIPE_F_TRIGGER = "swipe_f_trigger";
 	public static String SWIPE_U_TRIGGER = "swipe_u_trigger";
 	public static String SWIPE_B_TRIGGER = "swipe_b_trigger";
 	public static String SWIPE_D_TRIGGER = "swipe_d_trigger";
 	
 	public static String DTAP_F_TRIGGER = "dtap_f_trigger";
 	public static String DTAP_U_TRIGGER = "dtap_u_trigger";
 	public static String DTAP_B_TRIGGER = "dtap_b_trigger";
 	public static String DTAP_D_TRIGGER = "dtap_d_trigger";
 	
 	Context context;
 	String currentFile;
 	public ActionDataTool(Context context){
 		this.context = context;
 	}
 	
 	public void readFile(int resourceId){
 		InputStream inputStream = context.getResources().openRawResource(resourceId);
 
 		InputStreamReader inputreader = new InputStreamReader(inputStream);
 		BufferedReader buffreader = new BufferedReader(inputreader);
 		String line;
 		StringBuilder text = new StringBuilder();
 
 		try {
 			while (( line = buffreader.readLine()) != null) {
 				text.append(line);
 				text.append('\n');
 			}
 		} catch (IOException e) {
 			Log.e("STREAM ERROR", e.getMessage());
 			return;
 		}
 
 		this.currentFile = text.toString();
 
 	}
 	
 	public List<ActionData> parseFrameData(){
 		if(this.currentFile == null){
 			Log.w("JSON PARSE WARNING", "NO FILE LOADED");
 			return null;
 		}
 		
 		List<ActionData> actionData = new LinkedList<ActionData>();
 		
 		try {
 			JSONObject parser = new JSONObject(this.currentFile);
 			Iterator<?> keys = parser.keys();
 			while(keys.hasNext()){
 				String key = (String)keys.next();
 				JSONObject actionJSON = parser.getJSONObject(key);
 
 				String name = actionJSON.getString(NAME);
 				ActionData data = new ActionData(name);
 				
 				if(actionJSON.has(HITBOX)){
 					List<HitBox> hitBoxes = parseHitBoxData(actionJSON.getJSONArray(HITBOX));
 					data.setHitBoxes(hitBoxes);
 				}
 
 				if(actionJSON.has(HURTBOX)){
 					List<HurtBox> hurtBoxes = parseHurtBoxData(actionJSON.getJSONArray(HURTBOX));
 					data.setHurtBoxes(hurtBoxes);
 				}
 				
 				if(actionJSON.has(ACTION_PROPERTIES)){
 					ActionProperties properties = parseActionProperties(actionJSON.getJSONObject(ACTION_PROPERTIES));
 					data.setActionProperties(properties);
 				}
 
 				if(actionJSON.has(INTERACTION_PROPERTIES)){
 					ActionProperties properties = parseActionProperties(actionJSON.getJSONObject(INTERACTION_PROPERTIES));
					InteractionProperties interProperties = new InteractionProperties();
 					interProperties.copyActionProperties(properties);
 					data.setInterProperties(interProperties);
 				}
 				
 				PlaneData planeData = parsePlaneData(actionJSON.getJSONObject("plane_data"));
 				data.setPlaneData(planeData);
 				
 				actionData.add(data);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return actionData;
 	}
 	
 	public PlaneData parsePlaneData(JSONObject planeData) throws JSONException{
 		float width = (float) planeData.getDouble(PLANE_WIDTH);
 		float height = (float) planeData.getDouble(PLANE_HEIGHT);
 		int rows = planeData.getInt(PLANE_ROWS);
 		int columns = planeData.getInt(PLANE_COLUMNS);
 		
 		Playback playback = Playback.PLAY;
 		if(planeData.has(PLANE_PLAY_BACK)){
 			String name = planeData.getString(PLANE_PLAY_BACK);
 			playback = Playback.getPlaybackFromName(name);
 		}
 
 		return new PlaneData(width,height,rows,columns,playback);
 	}
 
 	
 	public List<HitBox> parseHitBoxData(JSONArray hitBoxData) throws JSONException{
 		List<HitBox> hitBoxes = new LinkedList<HitBox>();
 		
 		for(int index = 0; index < hitBoxData.length(); index++){
 			JSONObject boundsData = hitBoxData.getJSONObject(index);
 			float left = (float) boundsData.getDouble(LEFT);
 			float bottom = (float) boundsData.getDouble(BOTTOM);
 			float right = (float) boundsData.getDouble(RIGHT);
 			float top = (float) boundsData.getDouble(TOP);
 			HashSet<Integer> activeFrames;
 			if(boundsData.has(ACTIVE_FRAME)){
 				activeFrames = new HashSet<Integer>();
 				JSONArray actives = boundsData.getJSONArray(ACTIVE_FRAME);
 				for(int i = 0; i < actives.length(); i++){
 					activeFrames.add(actives.getInt(i));
 				}
 			} else {
 				activeFrames = new HashSet<Integer>();
 			}
 			HitBox box = new HitBox(left, top, right, bottom, activeFrames);
 			if(boundsData.has(SELECT_BOX)){
 				box.setSelected((float)boundsData.getDouble(SELECT_BOX));
 			}
 			hitBoxes.add(box);
 		}
 		
 		return hitBoxes;
 	}
 	
 	public List<HurtBox> parseHurtBoxData(JSONArray hitBoxData) throws JSONException{
 		List<HurtBox> hurtBoxes = new LinkedList<HurtBox>();
 		
 		for(int index = 0; index < hitBoxData.length(); index++){
 			JSONObject boundsData = hitBoxData.getJSONObject(index);
 			float left = (float) boundsData.getDouble(LEFT);
 			float bottom = (float) boundsData.getDouble(BOTTOM);
 			float right = (float) boundsData.getDouble(RIGHT);
 			float top = (float) boundsData.getDouble(TOP);
 			HashSet<Integer> activeFrames;
 			if(boundsData.has(ACTIVE_FRAME)){
 				activeFrames = new HashSet<Integer>();
 				JSONArray actives = boundsData.getJSONArray(ACTIVE_FRAME);
 				for(int i = 0; i < actives.length(); i++){
 					activeFrames.add(actives.getInt(i));
 				}
 			} else {
 				activeFrames = new HashSet<Integer>();
 			}
 			HurtBox box = new HurtBox(left, top, right, bottom, activeFrames);
 			if(boundsData.has(SELECT_BOX)){
 				box.setSelected((float)boundsData.getDouble(SELECT_BOX));
 			}
 			hurtBoxes.add(box);
 		}
 		
 		return hurtBoxes;
 	}
 	
 	public ActionProperties parseActionProperties(JSONObject propertyData) throws JSONException{
 		ActionProperties actionProperties = new ActionProperties();
 		
 		if(propertyData.has(X_INIT_SPEED)){
 			actionProperties.setxInitSpeed((float)propertyData.getDouble(X_INIT_SPEED));
 		}
 		
 		if(propertyData.has(Y_INIT_SPEED)){
 			actionProperties.setyInitSpeed((float)propertyData.getDouble(Y_INIT_SPEED));
 		}
 		
 		if(propertyData.has(X_ACCEL)){
 			actionProperties.setxAccel((float)propertyData.getDouble(X_ACCEL));
 		}
 		
 		if(propertyData.has(Y_ACCEL)){
 			actionProperties.setyAccel((float)propertyData.getDouble(Y_ACCEL));
 		}
 		
 		//trigger parsing
 		if(propertyData.has(TRIGGER_CHANGE)){
 			JSONObject triggerJSON = propertyData.getJSONObject(TRIGGER_CHANGE);
 			if(triggerJSON.has(HIT_TRIGGER)){
 				String value = triggerJSON.getString(HIT_TRIGGER);
 				actionProperties.addTrigger(HIT_TRIGGER, value);
 			}
 
 			if(triggerJSON.has(GROUND_TRIGGER)){
 				String value = triggerJSON.getString(GROUND_TRIGGER);
 				actionProperties.addTrigger(GROUND_TRIGGER, value);
 			}
 			
 			if(triggerJSON.has(WALL_TRIGGER)){
 				String value = triggerJSON.getString(WALL_TRIGGER);
 				actionProperties.addTrigger(WALL_TRIGGER, value);
 			}
 			
 			if(triggerJSON.has(PLAYED_TRIGGER)){
 				String value = triggerJSON.getString(PLAYED_TRIGGER);
 				actionProperties.addTrigger(PLAYED_TRIGGER, value);
 			}
 			
 			if(triggerJSON.has(STOPPED_TRIGGER)){
 				String value = triggerJSON.getString(STOPPED_TRIGGER);
 				actionProperties.addTrigger(STOPPED_TRIGGER, value);
 			}
 		}
 		
 		if(propertyData.has(MODIFIERS)){
 			JSONObject modifiersJSON = propertyData.getJSONObject(MODIFIERS);
 			if(modifiersJSON.has(ACTIVE_AFTER)){
 				int value = modifiersJSON.getInt(ACTIVE_AFTER);
 				actionProperties.addModifier(ACTIVE_AFTER, value);
 			}
 			
 			if(modifiersJSON.has(REVERSE_X)){
 				int value = modifiersJSON.getInt(REVERSE_X);
 				actionProperties.addModifier(REVERSE_X, value);
 			}
 		}
 		
 		if(propertyData.has(TRIGGER_CANCEL)){
 			JSONObject cancelJSON = propertyData.getJSONObject(TRIGGER_CANCEL);
 			if(cancelJSON.has(SWIPE_F_TRIGGER)){
 				String value = cancelJSON.getString(SWIPE_F_TRIGGER);
 				actionProperties.addCancel(SWIPE_F_TRIGGER, value);
 			}
 			
 			if(cancelJSON.has(SWIPE_B_TRIGGER)){
 				String value = cancelJSON.getString(SWIPE_B_TRIGGER);
 				actionProperties.addCancel(SWIPE_B_TRIGGER, value);
 			}
 			
 			if(cancelJSON.has(SWIPE_U_TRIGGER)){
 				String value = cancelJSON.getString(SWIPE_U_TRIGGER);
 				actionProperties.addCancel(SWIPE_U_TRIGGER, value);
 			}
 			
 			if(cancelJSON.has(SWIPE_D_TRIGGER)){
 				String value = cancelJSON.getString(SWIPE_D_TRIGGER);
 				actionProperties.addCancel(SWIPE_D_TRIGGER, value);
 			}
 			
 			if(cancelJSON.has(DTAP_F_TRIGGER)){
 				String value = cancelJSON.getString(DTAP_F_TRIGGER);
 				actionProperties.addCancel(DTAP_F_TRIGGER, value);
 			}
 			
 			if(cancelJSON.has(DTAP_B_TRIGGER)){
 				String value = cancelJSON.getString(DTAP_B_TRIGGER);
 				actionProperties.addCancel(DTAP_B_TRIGGER, value);
 			}
 			
 			if(cancelJSON.has(DTAP_U_TRIGGER)){
 				String value = cancelJSON.getString(DTAP_U_TRIGGER);
 				actionProperties.addCancel(DTAP_U_TRIGGER, value);
 			}
 			
 			if(cancelJSON.has(DTAP_D_TRIGGER)){
 				String value = cancelJSON.getString(DTAP_D_TRIGGER);
 				actionProperties.addCancel(DTAP_D_TRIGGER, value);
 			}
 		}
 		
 		return actionProperties;
 	}
 	
 	public InteractionProperties parseInteractionProperties(JSONObject interData) throws JSONException{
 		InteractionProperties interProperties = new InteractionProperties();
 		
 		if(interData.has(HIT_STOP)){
 			interProperties.setHitStop(interData.getInt(HIT_STOP));
 		}
 		
 		return interProperties;
 	}
 }
