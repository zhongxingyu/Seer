 package cs236369.hw5.dal;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import cs236369.hw5.Logger;
 
 public abstract class Base {
 	protected LinkedHashMap<String, String> fieldsTypes;
 	protected LinkedHashMap<String, Object> fieldsValues;
 	protected LinkedList<String> fields;
 	protected static String tableName;
 	protected String key;
 	static Class<?> cName;
 	int id;
 
 	Base() {
 		init();
 	}
 
 	Base(int id) {
 		init();
 		this.id = id;
 		Connection conn = Utils.getConnection();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try {
 			ps = conn.prepareStatement("SELECT * FROM " + getTableName() + " WHERE id = ?");
 			ps.setInt(1, id);
 			Logger.log(ps.toString());
 			rs = ps.executeQuery();
 			rs.next();
 			setFieldsFromResultSet(rs);
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			Utils.closeConnection(rs, ps, conn);
 			return;
 		}
 		Utils.closeConnection(rs, ps, conn);
 	}
 
 	public abstract String getTableName();
 
 	Base(ResultSet rs) {
 		init();
 		try {
 			id = rs.getInt("id");
			System.out.println("got id " + id);
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			setFieldsFromResultSet(rs);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	void init() {
 		initFields();
 	}
 
 	public String getTableNameIns() {
 		return tableName;
 	}
 
 
 	final void initFields() {
 		fieldsTypes = new LinkedHashMap<String, String>();
 		fieldsValues = new LinkedHashMap<String, Object>();
 		fields = new LinkedList<String>();
 		setFieldTypes();
 		for(Entry<String, String> entry : fieldsTypes.entrySet()) {
 			String name = entry.getKey();
 			if (!name.equals("id")){
 				fields.add(name);
 			}
 			String type = entry.getValue();
 			if (type.equals("string")) {
 				fieldsValues.put(name, "");
 			}
 			if (type.equals("int")) {
 				fieldsValues.put(name, 0);
 			}
 		}
 	}
 
 	abstract void setFieldTypes();
 
 	public int getId() {
 		return id;
 	}
 
 	void setFieldsFromResultSet(ResultSet rs) throws SQLException {
 		for(Entry<String, String> entry : fieldsTypes.entrySet()) {
 			String name = entry.getKey();
 			String type = entry.getValue();
 			Object value = null;
 			if (type.equals("string")) {
 				value = rs.getString(name);
 			}
 			if (type.equals("int")) {
 				value = rs.getInt(name);
 			}
 			fieldsValues.put(name, value);
 		}
 	}
 
 	public void setField(String name, Object value) {
 		fieldsValues.put(name, value);
 	}
 
 	public int getIntField(String name) {
 		return (Integer)fieldsValues.get(name);
 	}
 
 	public String getStringField(String name) {
 		return (String)fieldsValues.get(name);
 	}
 
 	public boolean validate() {
 		return true;
 	}
 
 	/* true if key with value newKey exists in the table*/
 	public boolean duplicate(String key, String newKey){
 
 		Connection conn = Utils.getConnection();
 		String query = "SELECT * FROM " + getTableName() + " WHERE " +  key + " = ?;";
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		try
 		{
 			ps = conn.prepareStatement(query);
 			ps.setString(1, newKey);
 			Logger.log(ps.toString());
 			rs = ps.executeQuery();
 			if (!rs.next()){
 				Utils.closeConnection(rs, ps, conn);
 				return false;
 			}
 			Utils.closeConnection(rs, ps, conn);
 		}catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	public boolean update(){
 		if (!validate()) {
 			return false;
 		}
 		Connection conn = Utils.getConnection();
 		String query = "UPDATE " + getTableName() + " SET ";
 		try
 		{
 			query += fields.get(0) + "= ?";
 			for (int i = 1 ; i < fields.size(); i++ ){
 				query += ", ";
 				query += fields.get(i) + "= ? ";
 			}
 			query += "WHERE id=?;";
 			PreparedStatement ps;
 			ps = conn.prepareStatement(query);
 			Collection<String> ct = fieldsTypes.values();
 			Iterator<String> itrT = ct.iterator();
 			Collection<Object> cv = fieldsValues.values();
 			Iterator<Object> itrV = cv.iterator();
 			int i = 1;
 			while(itrT.hasNext()){
 				if(itrT.next().equals("string")){
 					ps.setString(i,(String)itrV.next());
 				}
 				else{ //int
 					ps.setInt(i, (Integer)itrV.next());
 				}
 				i++;
 			}
 			ps.setInt(i, getId());
 			Logger.log(ps.toString());
 			ps.executeUpdate();
 			Utils.closeConnection(null, ps, conn);
 			return true;
 		}
 		catch (SQLException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean equals(Object other) {
 		if (!(other instanceof Base)) {
 			return false;
 		}
 		Base base = (Base)other;
 
 		if (!getTableName().equals(base.getTableName())) {
 			return false;
 		}
 		return base.getId() == getId();
 	}
 
 	public boolean save() {
 
 		if (!validate()) {
 			return false;
 		}
 		if (duplicate(this.key, (String)fieldsValues.get(this.key))){
 			//TODO ERROR
 			System.out.println("FAILED HEERR");
 			return false;
 		}
 		Connection conn = Utils.getConnection();
 		String prepStmt = "INSERT INTO " + getTableName() + " (" + fields.get(0);
 		for (int i = 1 ; i < fields.size(); i++ ){
 			prepStmt += " ," + fields.get(i);
 		}
 		prepStmt += ")";
 		prepStmt += " VALUES ( ?";
 
 		for (int i = 0 ; i < fieldsTypes.size() - 1 ; i++){
 			prepStmt += ", ?";
 		}
 		prepStmt += ");";
 		PreparedStatement ps = null;
 		try
 		{
 			ps = conn.prepareStatement(prepStmt);
 			Collection<String> ct = fieldsTypes.values();
 			Iterator<String> itrT = ct.iterator();
 			Collection<Object> cv = fieldsValues.values();
 			Iterator<Object> itrV = cv.iterator();
 			int i = 1;
 			while(itrT.hasNext()){
 				if(itrT.next().equals("string")){
 					ps.setString(i,(String)itrV.next());
 				}
 				else{ //int
 					ps.setInt(i, (Integer)itrV.next());
 				}
 				i++;
 			}
 			ps.executeUpdate();
 			Utils.closeConnection(null, ps, conn);
 			return true;
 		}catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Utils.closeConnection(null, ps, conn);
 		return false;
 	}
 
 	public boolean delete(){
 		if (!duplicate("id", new Integer(id).toString())){
 			return false;
 		}
 		Connection conn = Utils.getConnection();
 		String prepStmt = "DELETE FROM " + getTableName() + " WHERE id = ?;";
 		PreparedStatement ps = null;
 		try
 		{
 			ps = conn.prepareStatement(prepStmt);
 			ps.setInt(1, id);
 			Logger.log(ps.toString());
 			ps.executeUpdate();
 			Utils.closeConnection(null, ps, conn);
 			return true;
 		}catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Utils.closeConnection(null, ps, conn);
 		return false;
 	}
 }
