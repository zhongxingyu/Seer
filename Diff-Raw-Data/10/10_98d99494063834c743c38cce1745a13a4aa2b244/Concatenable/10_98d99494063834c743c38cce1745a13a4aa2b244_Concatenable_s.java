 package sfs.concatenable;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONObject;
 
 public abstract class Concatenable {
 
 	private List<Concatenable> jsonnables = new LinkedList<Concatenable>();
 
 	protected Concatenable() {
 
 	}
 
 	public Concatenable add(Concatenable jsonnable) {
 
 		if ( jsonnables.isEmpty() ) {
 			jsonnables.add( this );
 			jsonnables.add( jsonnable );
 		}
 		else {
 			jsonnables.add( jsonnable );
 		}
 
 		return this;
 	}
 
 	public String getJson() {
 
 		JSONObject json = new JSONObject();
		for ( Concatenable jsonnable : jsonnables ) {
			jsonnable.putJson( json );
 		}
 
 		return json.toString();
 	}
 
 	protected abstract void putJson(JSONObject json);
 }
