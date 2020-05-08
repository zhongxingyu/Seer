 package edu.ucla.cens.budburst.models;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Context;
 import edu.ucla.cens.budburst.Budburst;
 import edu.ucla.cens.budburst.data.Row;
 import edu.ucla.cens.budburst.data.SyncableRow;
 
 public class SpeciesRow extends SyncableRow {
 
 	public String species_name;
 	public String common_name;
 	public Long protocol_id;
	private final HashMap<String, ArrayList<Row>> phenophases = new HashMap<String, ArrayList<Row>>();
 
 	public ArrayList<Row> phenophases(String type) {
		// TODO: Why does it not null this out when a new one is created?
		// if (!phenophases.containsKey(type)) {
		if (_id > Budburst.MAX_PREDEFINED_SPECIES)
			phenophases.put(type, Budburst.getDatabaseManager().getDatabase("phenophase").find("type='" + type + "'"));
		else {
			phenophases.put(type, hasMany("phenophase", "protocol_id", protocol_id.toString(), "type='" + type + "'"));
		}
		// }
 
 		return phenophases.get(type);
 	}
 
 	public String getImagePath() {
 		// Hack to display the icon for user defined plants
 		// Don't blame me I didn't decide to do it this way.
 		if (_id > Budburst.MAX_PREDEFINED_SPECIES)
 			return Budburst.SPECIES_PATH + "999.jpg";
 		return Budburst.SPECIES_PATH + _id + ".jpg";
 	}
 
 	public InputStream getImageStream(Context ctx) {
 		try {
 			// Hack to display the icon for user defined plants
 			// Don't blame me I didn't decide to do it this way.
 			if (_id > Budburst.MAX_PREDEFINED_SPECIES)
 				return ctx.getAssets().open("species_images/999.jpg");
 
 			return ctx.getAssets().open("species_images/" + _id + ".jpg");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
