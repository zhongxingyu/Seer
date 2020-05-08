 package at.screenteam.oma;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.ContentHandler;
 import org.json.simple.parser.ParseException;
 
 public class JSONTransformer implements ContentHandler {
 
 	private JSONObject currObject;
 	private int level;
 	private String currKey;
 	private String currTable;	
 	private Set currTableFields;
 
 	private String dbPath;
 	private Connection conn;
 	private PreparedStatement stat;
 	
	private static final String DATETIME_REGEX = "^([0-9]{4})-([0-9]{2})-([0-9]{2}) ([0-9]{2}):([0-9]{2}):([0-9]{2})$";
	private static final String DATE_REGEX = "^([0-9]{4})-([0-9]{2})-([0-9]{2})$";
	private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
 	
 	public JSONTransformer(String dbPath) {
 		this.dbPath = dbPath;
 	}
 	
 	//////////////////////////////////////////
 	// Interface ContentHandler
 	//////////////////////////////////////////
 	
 	@Override
 	public void startJSON() throws ParseException, IOException {
 		this.level = 0;
 		this.connect();
 	}
 	
 	@Override
 	public void endJSON() throws ParseException, IOException {
 		this.disconnect();
 	}
 
 	@Override
 	public boolean startObject() throws ParseException, IOException {
 		this.currObject = new JSONObject();
 		this.level++;
 		return true;
 	}
 	
 	@Override
 	public boolean endObject() throws ParseException, IOException {
 		this.level--;		
 		if (this.level == 1) {
 			this.endData();
 		}		
 		return true;
 	}
 
 	@Override
 	public boolean startObjectEntry(String key) throws ParseException, IOException {
 		this.currKey = key;
 		return true;
 	}
 	
 	@Override
 	public boolean endObjectEntry() throws ParseException, IOException {
 		return true;
 	}
 	
 	@Override
 	public boolean startArray() throws ParseException, IOException {
 		if (this.level == 1) {
 			this.currTable = this.currKey;
 			this.stat = null;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean endArray() throws ParseException, IOException {
 		if (this.level == 1) {
 			this.endTable();
 		}
 		return true;
 	}
 
 	@Override
 	public boolean primitive(Object value) throws ParseException, IOException {
 		this.currObject.put(this.currKey, value);
 		return true;
 	}
 	
 	//////////////////////////////////////////
 	// Additional methods
 	//////////////////////////////////////////
 
 	public JSONObject getReturnObject() {
 		JSONObject obj = new JSONObject();
 		obj.put("machineID", this.currObject.get("machineID"));
 		obj.put("syncID", this.currObject.get("syncID"));
 		obj.put("syncTime", this.currObject.get("syncTime"));
 		return obj;
 	}
 	
 	//////////////////////////////////////////
 	// Auxiliary methods
 	//////////////////////////////////////////
 
 	private void endTable() {
 		try {
 			this.stat.executeBatch();
 			this.conn.commit();
 		} 
 		catch (SQLException ex) {
 			Logger.getLogger(JSONTransformer.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	private void endData() {
 		if (this.stat == null) {
 			this.prepareStatement();
 		}
 		
 		try {
 			int i = 1;
 			for (Object key: this.currTableFields) {
 				this.stat.setString(i, this.convert2sqlite(this.currObject.get(key).toString()));
 				i++;
 			}
 			this.stat.addBatch();
 		} 
 		catch (SQLException ex) {
 			Logger.getLogger(JSONTransformer.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	private void prepareStatement() {
 		try {
 			String fields = "";
 			String values = "";
 			this.currTableFields = this.currObject.keySet();
 			for (Object key: this.currTableFields) {
 				fields += ",`" + key + "`";
 				values += ",?";
 			}
 			fields = fields.substring(1);
 			values = values.substring(1);
 			this.stat = this.conn.prepareStatement("INSERT INTO " + this.currTable + "(" + fields + ") VALUES (" + values + ");");
 //			System.out.println("INSERT INTO " + this.currTable + "(" + fields + ") VALUES (" + values + ");");
 		} 
 		catch (SQLException ex) {
 			Logger.getLogger(JSONTransformer.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	private void connect() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			this.conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath);
 			this.conn.setAutoCommit(false);
 		} 
 		catch (Exception ex) {
 			Logger.getLogger(JSONTransformer.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	private void disconnect() {
 		try {
 			this.conn.close();
 		} 
 		catch (Exception ex) {
 			Logger.getLogger(JSONTransformer.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	public String convert2sqlite(String value) {
 		// return dates as timestamp (in seconds)
		if (value.length() == 19 && value.matches(DATETIME_REGEX)) {
			try {
				Date date = DATETIME_FORMAT.parse(value);
				long timestamp = date.getTime() / 1000;
				return Long.toString(timestamp);
			} 
			catch (java.text.ParseException ex) {
				// date could not be converted - return valus as it is
			}			
		}
		else if (value.length() == 10 && value.matches(DATE_REGEX)) {
 			try {
 				Date date = DATE_FORMAT.parse(value);
 				long timestamp = date.getTime() / 1000;
 				return Long.toString(timestamp);
 			} 
 			catch (java.text.ParseException ex) {
 				// date could not be converted - return valus as it is
 			}			
 		}
 		else {
 			// replace return+newline by only newline character
 			if (value.indexOf("\r\n") != -1) {
 				value = value.replaceAll("\r\n", "\n");
 			}
 		}
 		
 		// return value unchanges
 		return value;
 	}
 	
 }
