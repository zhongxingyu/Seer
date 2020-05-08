 /**
  *
  */
 package org.bitpipeline.lib.friendlyjson.complexdata;
 
 import java.awt.Image;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bitpipeline.lib.friendlyjson.JSONEntity;
 import org.bitpipeline.lib.friendlyjson.JSONMappingException;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * @author mtavares */
 public abstract class AbstractEquipment extends JSONEntity implements ViewableObjectInterface {
 	private transient static final String FIELD_NAME = "name";
 	private transient static final String FIELD_DESCRIPTION = "description";
 	private transient static final String FIELD_IMAGEURL = "imageURL";
 	
	private final Map<String, String> info = new HashMap<String, String> ();
 
 	transient private ViewableObjectInterface viewableImpl;
 
 	public AbstractEquipment (JSONObject json) throws JSONMappingException {
 		super (json);
 		// take care of the name, description and imageUrl
 		String name;
 		String description;
 		String imageUrl;
 		try {
 			name = json.getString (AbstractEquipment.FIELD_NAME);			
 		} catch (JSONException e) {
 			name = null;
 		}
 		try {
 			description = json.getString (AbstractEquipment.FIELD_DESCRIPTION);
 		} catch (JSONException e) {
 			description = null;
 		}
 		try {
 			imageUrl = json.getString (AbstractEquipment.FIELD_IMAGEURL);
 		} catch (JSONException e) {
 			imageUrl = null;
 		}
 		this.viewableImpl = new ViewableResourceBasedObject (name, imageUrl, description);
 	}
 
 	public AbstractEquipment (String name, String imageUrl, String description) {
 		this.viewableImpl = new ViewableResourceBasedObject (name, imageUrl, description);
 	}
 
 	public AbstractEquipment (String name, String imageUrl, String description, Map<String, String> info) {
 		this(name, imageUrl, description);
 		this.info.putAll (info);
 	}
 
 	public Image getImage () {
 		return this.viewableImpl.getImage ();
 	}
 
 	public String getImageURI () {
 		return this.viewableImpl.getImageURI ();
 	}
 
 	public String getName () {
 		return this.viewableImpl.getName ();
 	}
 
 	public String getDescription () {
 		return this.viewableImpl.getDescription ();
 	}
 
 	public String getInfo (String key) {
 		return this.info.get (key);
 	}
 
 	public String putInfo (String key, String value) {
 		return this.info.put (key, value);
 	}
 
 	public String removeInfo (String key) {
 		return this.info.remove (key);
 	}
 
 	public boolean isInfoEmpty () {
 		return this.info.isEmpty ();
 	}
 
 	public void clearInfo () {
 		this.info.clear ();
 	}
 	
 	@Override
 	public JSONObject toJson () throws JSONMappingException {
 		JSONObject json = super.toJson ();
 		if (this.viewableImpl != null) {
 			try {
 				json.put ("name", this.viewableImpl.getName ());
 				json.put ("description", this.viewableImpl.getDescription ());
 				json.put ("imageURL", this.viewableImpl.getImageURI ());
 			} catch (JSONException e) {
 				throw new JSONMappingException (e);
 			}
 	}
 		return json;
 	}
 }
