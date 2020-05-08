 package at.screenteam.oma;
 
 
 import commandproxy.core.Command;
 import commandproxy.core.CommandException;
 import java.io.FileReader;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 /**
  * Reads the given JSON file using a stream parser and inserts the data into the given SQLite databse on the fly
  * Used for the initial (full) OMa sync:
  * - OMa downloads the complete database from the server, JSON encoded
  * - OMa writes this JSON encoded string into a temporary file
  * - OMa calls the DatabaseImporter to parse and import the data
  * - DatabaseImporter processes the data and returns machine id, sync id and sync time
  * - OMa deletes the temporary file and carries on
  * 
  * @author Martin Lenzelbauer <ml@screenteam.at>
  */
 public class JSON2SQLite implements Command {
 
 	@Override
 	public String getName() {
 		return "json2sqlite";
 	}
 	
 	/**
 	 * imports the JSON data into the SQLite database
 	 * @param params needs two params: jsonFile (path to file with JSON data) and dbFile (path to SQLite database file)
 	 * @return JSON object containing the machine id, sync id and sync time read from the JSON input file
 	 * @throws CommandException
 	 * @throws JSONException 
 	 */
 	@Override
 	public JSONObject execute(Map<String, String> params) throws CommandException, JSONException {
 		String jsonFile = params.get("jsonFile");
 		String dbFile = params.get("dbFile");
 		
 		if (jsonFile == null || dbFile == null) {
 			throw new CommandException("Parameter jsonFile und/oder dbFile fehlt!", this);
 		}
 		
 		try {
 			// parse JSON and insert into SQLite database
 			FileReader reader = new FileReader(jsonFile);
 			JSONParser parser = new JSONParser();
 			JSONTransformer transformer = new JSONTransformer(dbFile);		
 
 			parser.parse(reader, transformer);
 
 			reader.close();
 			
 			// return machine id, sync id and sync time as JSON object
 			return new JSONObject(transformer.getReturnObject());	
 		}
 		catch (Exception e) {
 			throw new CommandException(e.getMessage(), this);
 		}
 	}
 
 	
 	
 	public static void main(String[] args) {
 		Map<String, String> params = new HashMap<String, String>();
		params.put("jsonFile", "/Users/mrscreenteam/Library/Preferences/OMa/Local Store/OMa.db.json");
		params.put("dbFile", "/Users/mrscreenteam/Library/Preferences/OMa/Local Store/OMa.db");
 		
 		JSON2SQLite plugin = new JSON2SQLite();
 		try {
 			JSONObject result = plugin.execute(params);
 			System.out.println("result: " + result);
 		} 
 		catch (CommandException ex) {
 			Logger.getLogger(JSON2SQLite.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (JSONException ex) {
 			Logger.getLogger(JSON2SQLite.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 }
