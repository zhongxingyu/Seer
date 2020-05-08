 package com.test;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayDeque;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.postgresql.PGConnection;
 import org.postgresql.ds.PGConnectionPoolDataSource;
 
 public class DataBaseFunctions {
 
 	static final String URL = "localhost";
 	static final String USER = "postgres";
 	static final String PASSWORD = "postgres";
 
 	static final String COLUMNS_Order = " " + "o.id AS Order_ID, "
 			+ "o.timestamp AS order_timestamp, " + "o.status AS order_status, "
 			+ "f.id AS facility_id, " + "f.name as facility_name";
 
 	static final String FROM_Order = " FROM facilities f JOIN orders o ON f.id = o.facility_id "
 			+ "JOIN drugs d ON o.drug_id = d.id JOIN categories c ON c.id = d.category_id ";
 
 	static final String GET_CATEGORY_NAME = "SELECT c.id AS category_id,c.name AS category_name FROM categories c";
 
 	static final String ADD_ORDER_START = "WITH meta AS (SELECT ? as fac_id,now() as ts, ? as stat) "
 			+ "INSERT INTO "
 			+ "orders (id,facility_id,drug_id,unit_number,timestamp,status) "
 			+ "VALUES ";
 	static final String ADD_ORDER_VAL = "(1+(select max(id) from orders),(SELECT fac_id FROM meta),?,?,(SELECT ts FROM meta),(SELECT stat FROM meta))";
 
 	static final String UPDATE_INVENTORY = "SELECT update_inventory(?,?,?)";
 
 	static final String UPDATE_ORDER_STATUS = "UPDATE orders SET status = ?"
 			+ " WHERE id = ?";
 
	static final String GET_DRUGS = "SELECT d.*,COALESCE(i.unit_number,0) "
 			+ "FROM drugs d "
 			+ "LEFT OUTER JOIN (SELECT * FROM inventories WHERE facility_id = ?) i "
 			+ "ON (d.id = i.drug_id)  ";
 
 	static final String ADD_DRUG = "INSERT INTO drugs(id, msdcode, "
 			+ "category_id, med_name, common_name, unit, unit_details, unit_price) "
 			+ "VALUES (default, ?, ?, ?, ?, ?, ?, ?)";
 
 	static final String UPDATE_DRUG_START = "UPDATE drugs ";
 	static final String UPDATE_DRUG_END = " WHERE id = ?";
 
 	private static PGConnectionPoolDataSource dataSourceWeb = null;
 
 	/**
 	 * Transforms the rows, received through the given ResultSet, into
 	 * JSONObjects and returns them as a JSONArray
 	 * 
 	 * @param resultSet
 	 *            ResultSet to be transformed
 	 * @return JSONArray containing JSONObjects
 	 * @throws SQLException
 	 */
 	private static JSONArray resultSetToJSONArray(ResultSet resultSet)
 			throws SQLException {
 
 		ResultSetMetaData resultMeta = resultSet.getMetaData();
 
 		int columnNumber = resultMeta.getColumnCount();
 		String[] columnNames = new String[columnNumber];
 		Integer[] columnTypes = new Integer[columnNumber];
 		for (int columnIndex = 1; columnIndex <= columnNumber; columnIndex++) {
 			columnNames[columnIndex - 1] = resultMeta
 					.getColumnLabel(columnIndex);
 			columnTypes[columnIndex - 1] = resultMeta
 					.getColumnType(columnIndex);
 
 		}
 
 		JSONArray resultArray = new JSONArray();
 		while (resultSet.next()) {
 			JSONObject jsonRow = resultSetRowToJSONObject(resultSet);
 			resultArray.add(jsonRow);
 		}
 		return resultArray;
 
 	}
 
 	private static void columnIntoJSONObject(String columnName,
 			ResultSet resultSet, int columnType, JSONObject jsonObject)
 			throws SQLException {
 		switch (columnType) {
 		case Types.INTEGER:
 			jsonObject.put(columnName, resultSet.getInt(columnName));
 			break;
 		case Types.TIMESTAMP:
 			jsonObject.put(columnName, resultSet.getTimestamp(columnName).toString());
 			break;
 		case Types.VARCHAR:
 		case Types.CHAR:
 			jsonObject.put(columnName, resultSet.getString(columnName));
 			break;
 		case Types.NUMERIC:
 		case Types.DOUBLE:
 			jsonObject.put(columnName, resultSet.getDouble(columnName));
 			break;
 		default:
 			break;
 		}
 	}
 
 	private static JSONObject resultSetRowToJSONObject(ResultSet resultSet)
 			throws SQLException {
 		ResultSetMetaData resultMeta = resultSet.getMetaData();
 
 		int columnNumber = resultMeta.getColumnCount();
 		String[] columnNames = new String[columnNumber];
 		Integer[] columnTypes = new Integer[columnNumber];
 		for (int columnIndex = 1; columnIndex <= columnNumber; columnIndex++) {
 			columnNames[columnIndex - 1] = resultMeta
 					.getColumnLabel(columnIndex);
 			columnTypes[columnIndex - 1] = resultMeta
 					.getColumnType(columnIndex);
 
 		}
 		// for (String name : columnNames)
 		// System.out.println(name);
 
 		JSONObject jsonRow = new JSONObject();
 		for (int columnIndex = 1; columnIndex <= columnNumber; columnIndex++) {
 			String columnName = columnNames[columnIndex - 1];
 
 			columnIntoJSONObject(columnName, resultSet,
 					columnTypes[columnIndex - 1], jsonRow);
 
 		}
 		return jsonRow;
 	}
 
 	/**
 	 * 
 	 * @return A connection to the database, currently having all rights.
 	 */
 	public static Connection getWebConnection() {
 		try {
 			if (dataSourceWeb == null) {
 				dataSourceWeb = new PGConnectionPoolDataSource();
 				dataSourceWeb.setUser(USER);
 				dataSourceWeb.setPassword(PASSWORD);
 				dataSourceWeb.setServerName(URL);
 				dataSourceWeb.setPortNumber(5433);
 				dataSourceWeb.setDatabaseName("chpv1_small");
 			}
 
 			Connection con = dataSourceWeb.getPooledConnection()
 					.getConnection();
 			PGConnection pgCon = (PGConnection) con;
 			pgCon.addDataType("drug_ext", Class.forName("com.test.PGDrug"));
 			return con;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 * @return JSONArray containing Categories, stored as JSONObjects
 	 * @throws SQLException
 	 */
 	public static JSONArray getCategories(Connection con) {
 		ResultSet resultSet;
 		JSONArray result = null;
 		try {
 			resultSet = con.createStatement().executeQuery(GET_CATEGORY_NAME);
 			result = resultSetToJSONArray(resultSet);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 * @param parameters
 	 *            JSON Object with the following parameters:<br>
 	 *            facility_id : (int),<br>
 	 *            status : (int),<br>
 	 * <br>
 	 *            Additionally Key-Value-Pairs in the form of (drug_id (int) :
 	 *            unit_number (int)) will have to be added
 	 * @return true if operation succeeded, false otherwise
 	 * @throws SQLException
 	 */
 	public static boolean addOrder(Connection con, JSONObject parameters) {
 		if (parameters == null)
 			return false;
 
 		Set keySet = parameters.keySet();
 		int keySize = keySet.size();
 
 		if (keySize < 3) {
 			System.err.println("Not enough Liis, try again!");
 			return false;
 		}
 		String facility_idS = (String) parameters.get("facility_id");
 		String order_statusS = (String) parameters.get("status");
 
 		if (facility_idS == null || order_statusS == null)
 			return false;
 		
 		Integer facility_id = Integer.valueOf(facility_idS);
 		Integer status = Integer.valueOf(order_statusS);
 
 		StringBuilder sb = new StringBuilder();
 		sb.append(ADD_ORDER_START);
 
 		int c = 1;
 		ArrayDeque<Integer[]> orderNums = new ArrayDeque<Integer[]>();
 		for (Object keyO : keySet) {
 			String key = (String) keyO;
 
 			if (!key.isEmpty() && key.matches("[0-9]*")) {
 				if (c > 1)
 					sb.append(",");
 				sb.append(ADD_ORDER_VAL);
 				Integer drug_id = Integer.valueOf(key);
 				Integer number = Integer.valueOf((String) parameters.get(keyO));
 				Integer[] one = { drug_id, number };
 				orderNums.add(one);
 				System.out.println("Parameters fround: " + drug_id + "|"
 						+ number);
 				c++;
 			}
 
 		}
 
 		PreparedStatement pstmt;
 		try {
 			pstmt = con.prepareStatement(sb.toString());
 			int p = 1;
 			pstmt.setInt(p++, facility_id);
 			pstmt.setInt(p++, status);
 
 			Integer[] orderNum;
 			System.out.println("OrderNums size: " + orderNums.size());
 			while ((orderNum = orderNums.poll()) != null) {
 				pstmt.setInt(p++, orderNum[0]);
 				pstmt.setInt(p++, orderNum[1]);
 			}
 			System.out.println(pstmt.toString());
 			pstmt.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 * @param parameters
 	 *            JSONObject with the following OPTIONAL parameters:<br>
 	 *            drug_id : (int),<br>
 	 *            category_id : (int)
 	 * @return JSONArray containing Drugs, stored as JSONObjects
 	 * 
 	 */
 	public static JSONArray getDrugs(Connection con, JSONObject parameters) {
 
 		String drug_idS = (String) parameters.get("drug_id");
 		String category_idS = 
 				(String) parameters.get("category_id");
 		
 		String facility_idS = (String) parameters.get("facility_id");
 		
 		if (facility_idS == null)
 			return null;
 		
 		
 
 		int p = 0;
 		String where = "";
 		if (drug_idS != null) {
 			where += p == 0 ? " WHERE " : " AND ";
 			where += "id = ?";
 			p++;
 		}
 
 		if (category_idS != null) {
 			where += p == 0 ? " WHERE " : " AND ";
 			where += "category_id = ?";
 		}
 
 		PreparedStatement pstmt;
 		try {
 			pstmt = con.prepareStatement(GET_DRUGS + where + " ORDER BY med_name ASC");
 			System.out.println(pstmt.toString());
 			Integer facility_id = Integer.valueOf(facility_idS);
 			
 			p = 1;
 			
 			pstmt.setInt(p++, facility_id);
 
 			if (drug_idS != null) {
 				Integer drug_id = Integer.valueOf(drug_idS);
 				pstmt.setInt(p++, drug_id);
 			}
 
 			if (category_idS != null) {
 				Integer category_id = Integer.valueOf(category_idS);
 				pstmt.setInt(p++, category_id);
 			}
 
 			ResultSet rs = pstmt.executeQuery();
 
 			return resultSetToJSONArray(rs);
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return new JSONArray();
 
 	}
 
 	/**
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 * @param parameters
 	 *            JSON Object with the following parameters:<br>
 	 *            order_id (int),<br>
 	 *            order_start (Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
 	 *            order_end (Timestamp: yyyy-[m]m-[d]d hh:mm:ss),<br>
 	 *            order_status (one of:
 	 *            1 (initiated),2 (sent),3 (delivered), 4(canceled)<br>
 	 *            facility_id (int),<br>
 	 *            facility_name (String)
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static JSONArray getOrderSummary(Connection con,
 			JSONObject parameters) {
 		if (parameters == null)
 			return null;
 
 		String order_id = (String) parameters.get("order_id");
 
 		String order_start_String = (String) parameters.get("order_start");
 		Timestamp order_start = order_start_String == null ? null
 				: java.sql.Timestamp.valueOf(order_start_String);
 		String order_end_String = (String) parameters.get("order_end");
 		Timestamp order_end = order_end_String == null ? null
 				: java.sql.Timestamp.valueOf(order_end_String);
 		String order_status = (String) parameters.get("order_status");
 
 		Integer facility_id = Integer.valueOf((String) parameters
 				.get("facility_id"));
 		String facility_name = (String) parameters.get("facility_name");
 
 		StringBuilder whereBuilder = new StringBuilder("");
 
 		String summarizeS = (String) parameters.get("summarize");
 		boolean summarize = summarizeS == null ? false : Boolean
 				.valueOf(summarizeS);
 
 		if (summarize)
 			whereBuilder.append("SELECT DISTINCT");
 		else
 			whereBuilder.append("SELECT");
 
 		whereBuilder.append(COLUMNS_Order);
 
 		if (summarize)
 			whereBuilder
 					.append(", sum(d.unit_price*o.unit_number) as total_costs");
 		else
 			whereBuilder
 					.append(", (d.*,c.name,o.unit_number)::drug_ext AS drug ");
 
 		whereBuilder.append(FROM_Order);
 
 		int c = 0;
 
 		if (order_id != null)
 			whereBuilder.append((c++ > 0 ? " AND " : "WHERE ")).append(
 					"o.id = ?");
 
 		if (order_start != null)
 			whereBuilder.append((c++ > 0 ? " AND " : "WHERE ")).append(
 					"o.timestamp >= ?");
 
 		if (order_end != null)
 			whereBuilder.append((c++ > 0 ? " AND " : "WHERE ")).append(
 					"o.timestamp <= ?");
 
 		if (order_status != null)
 			whereBuilder.append((c++ > 0 ? " AND " : "WHERE ")).append(
 					"o.status = ?");
 
 		if (facility_id != null)
 			whereBuilder.append((c++ > 0 ? " AND " : "WHERE ")).append(
 					"f.id = ?");
 
 		if (facility_name != null)
 			whereBuilder.append((c++ > 0 ? " AND " : "WHERE ")).append(
 					"f.name LIKE ('%'||?||'%')");
 
 		if (summarize)
 			whereBuilder
 					.append(" GROUP BY o.id, o.timestamp, o.status, f.id, f.name");
 
 		PreparedStatement pstmt;
 		JSONArray resultArray = null;
 		try {
 			pstmt = con.prepareStatement(whereBuilder.toString()
 					+ " ORDER BY o.id ASC");
 			int p = 1;
 
 			if (order_id != null)
 				pstmt.setInt(p++, Integer.valueOf(order_id));
 
 			if (order_start != null)
 				pstmt.setTimestamp(p++, order_start);
 
 			if (order_end != null)
 				pstmt.setTimestamp(p++, order_end);
 
 			if (order_status != null)
 				pstmt.setInt(p++, Integer.valueOf(order_status));
 
 			if (facility_id != null)
 				pstmt.setInt(p++, facility_id);
 
 			if (facility_name != null)
 				pstmt.setString(p++, facility_name);
 
 			System.out.println(pstmt.toString());
 
 			ResultSet rs = pstmt.executeQuery();
 
 			if (summarize)
 				return resultSetToJSONArray(rs);
 
 			resultArray = new JSONArray();
 
 			int currentOrderID = -1;
 
 			JSONObject jsonOrder = new JSONObject();
 			JSONArray drugs = new JSONArray();
 			boolean found_sth = false;
 			while (rs.next()) {
 				found_sth = true;
 				int row_order_id = rs.getInt("order_id");
 
 				if (currentOrderID != row_order_id) {
 
 					if (currentOrderID != -1) {
 						jsonOrder.put("drugs", drugs);
 						resultArray.add(jsonOrder);
 						drugs = new JSONArray();
 					}
 					jsonOrder = resultSetRowToJSONObject(rs);
 					jsonOrder.remove("unit_number");
 					// System.out.println(jsonOrder);
 					currentOrderID = row_order_id;
 				}
 
 				Object drugO = rs.getObject("drug");
 
 				PGDrug drug = (PGDrug) drugO;
 
 				JSONObject jsonDrug = drug.toJSONObject();
 
 				drugs.add(jsonDrug);
 
 			}
 			if (found_sth) {
 				jsonOrder.put("drugs", drugs);
 				resultArray.add(jsonOrder);
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("orderSummary finishes now. Whatever happens next is not its fault.");
 		return resultArray;
 	}
 
 	/**
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 * @param parameters
 	 *            JSON Object with the following parameters:<br>
 	 *            order_id (int),<br>
 	 *            status (int),<br>
 	 * @return true if operation succeeded, false otherwise
 	 * @throws SQLException
 	 */
 	public static boolean updateOrderStatus(Connection con,
 			JSONObject parameters) {
 		if (parameters == null)
 			return false;
 		Integer order_id = Integer.valueOf((String) parameters.get("order_id"));
 		Integer status = Integer.valueOf((String) parameters.get("status"));
 
 		if (order_id == null || status == null)
 			return false;
 
 		PreparedStatement pstmt;
 		try {
 			pstmt = con.prepareStatement(UPDATE_ORDER_STATUS);
 			pstmt.setInt(1, status);
 			pstmt.setInt(2, order_id);
 
 			pstmt.executeUpdate();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return false;
 
 	}
 
 	/**
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 * @param parameters
 	 *            JSON Object with the following parameters:<br>
 	 *            facility_id : (int),<br>
 	 * <br>
 	 *            Additionally Key-Value-Pairs in the form of (drug_id (int) :
 	 *            difference (int)) will have to be added
 	 * @return true if operation succeeded, false otherwise
 	 */
 	public static boolean updateInventory(Connection con, JSONObject parameters) {
 		if (parameters == null)
 			return false;
 
 		Integer facility_id = Integer.valueOf((String) parameters
 				.get("facility_id"));
 
 		if (facility_id == null)
 			return false;
 
 		Set<Map.Entry<Object, Object>> a = parameters.entrySet();
 
 		try {
 			PreparedStatement pstmt = con.prepareStatement(UPDATE_INVENTORY);
 			for (Iterator<Entry<Object, Object>> iterator = a.iterator(); iterator
 					.hasNext();) {
 				Entry<Object, Object> entry = iterator.next();
 				String key = (String) entry.getKey();
 				if (!key.isEmpty() && key.matches("[0-9]*")) {
 					pstmt.setInt(1, facility_id);
 					pstmt.setInt(2, Integer.valueOf(key));
 					pstmt.setInt(3, Integer.valueOf((String) entry.getValue()));
 					pstmt.executeQuery();
 				}
 			}
 			return true;
 		} catch (SQLException e) {
 			e.getNextException().printStackTrace();
 			e.printStackTrace();
 		}
 
 		return false;
 
 	}
 
 	/**
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 * @param parameters
 	 *            JSON Object with the following parameters:<br>
 	 *            Mandatory:<br>
 	 *            msdcode (int),<br>
 	 *            category_id (int),<br>
 	 *            med_name (String),<br>
 	 *            unit_price (Double)<br>
 	 *            Optional:<br>
 	 *            common_name (String),<br>
 	 *            unit (String),<br>
 	 *            unit_details (String)
 	 * @return true if operation succeeded, false otherwise
 	 * @throws SQLException
 	 */
 	public static boolean addDrug(Connection con, JSONObject parameters) {
 		String msdcodeS = (String) parameters.get("msdcode");
 		String category_idS = (String) parameters.get("category_id");
 		String med_name = (String) parameters.get("med_name");
 		String common_name = (String) parameters.get("common_name");
 		String unit = (String) parameters.get("unit");
 		String unit_details = (String) parameters.get("unit_details");
 		String unit_priceS = (String) parameters.get("unit_price");
 
 		if (msdcodeS == null || category_idS == null || med_name == null
 				|| unit_priceS == null)
 			return false;
 
 		Double unit_price = Double.valueOf(unit_priceS);
 
 		PreparedStatement pstmt;
 		try {
 			pstmt = con.prepareStatement(ADD_DRUG);
 			int p = 1;
 
 			pstmt.setInt(p++, Integer.valueOf(msdcodeS));
 
 			pstmt.setInt(p++, Integer.valueOf(category_idS));
 
 			pstmt.setString(p++, med_name);
 
 			if (common_name == null)
 				pstmt.setNull(p++, java.sql.Types.VARCHAR);
 			else
 				pstmt.setString(p++, common_name);
 
 			if (unit == null)
 				pstmt.setNull(p++, java.sql.Types.VARCHAR);
 			else
 				pstmt.setString(p++, unit);
 
 			if (unit_details == null)
 				pstmt.setNull(p++, java.sql.Types.VARCHAR);
 			else
 				pstmt.setString(p++, unit_details);
 
 			pstmt.setDouble(p++, unit_price);
 			System.out.println(pstmt.toString());
 			int result = pstmt.executeUpdate();
 
 			return result > 0;
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 * @param parameters
 	 *            JSON Object with the following parameters:<br>
 	 *            Mandatory:<br>
 	 *            id (int)<br>
 	 *            Optional:<br>
 	 *            msdcode (int),<br>
 	 *            category_id (int),<br>
 	 *            med_name (String),<br>
 	 *            common_name (String),<br>
 	 *            unit (String),<br>
 	 *            unit_details (String),<br>
 	 *            unit_price (Double)<br>
 	 * @return true if operation succeeded, false otherwise
 	 * @throws SQLException
 	 */
 	public static boolean updateDrug(Connection con, JSONObject parameters) {
 
 		String idS = (String) parameters.get("id");
 
 		if (idS == null)
 			return false;
 
 		int id = Integer.valueOf(idS);
 
 		String msdcodeS = (String) parameters.get("msdcode");
 		String category_idS = (String) parameters.get("category_id");
 		String med_name = (String) parameters.get("med_name");
 		String common_name = (String) parameters.get("common_name");
 		String unit = (String) parameters.get("unit");
 		String unit_details = (String) parameters.get("unit_details");
 		String unit_priceS = (String) parameters.get("unit_price");
 
 		String middle = "SET ";
 		int c = 0;
 
 		if (msdcodeS != null)
 			middle += c++ > 0 ? ", " : " " + "msdcode = ?";
 			
 		if (category_idS != null)
 			middle += c++ > 0 ? ", " : " " + "category_id = ?";
 			
 		if (med_name != null)
 			middle += c++ > 0 ? ", " : " " + "med_name = ?";
 
 		if (common_name != null)
 			middle += c++ > 0 ? ", " : " " + "common_name = ?";
 
 		if (unit != null)
 			middle += c++ > 0 ? ", " : " " + "unit = ?";
 
 		if (unit_details != null)
 			middle += c++ > 0 ? ", " : " " + "unit_details = ?";
 
 		if (unit_priceS != null)
 			middle += c++ > 0 ? ", " : " " + "unit_price = ?";
 
 		try {
 			PreparedStatement pstmt = con.prepareStatement(UPDATE_DRUG_START
 					+ middle + UPDATE_DRUG_END);
 			int p = 1;
 			if (msdcodeS != null)
 				pstmt.setInt(p++, Integer.valueOf(msdcodeS));
 
 			if (category_idS != null)
 				pstmt.setInt(p++, Integer.valueOf(category_idS));
 
 			if (med_name != null)
 				pstmt.setString(p++, med_name);
 
 			if (common_name != null)
 				pstmt.setString(p++, common_name);
 
 			if (unit != null)
 				pstmt.setString(p++, unit);
 
 			if (unit_details != null)
 				pstmt.setString(p++, unit_details);
 
 			if (unit_priceS != null)
 				pstmt.setDouble(p++, Double.valueOf(unit_priceS));
 
 			pstmt.setInt(p++, id);
 
 			int result = pstmt.executeUpdate();
 
 			return result > 0;
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * This function will print an exemplary Result of the getDrugs Function.
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 */
 	private static void testGetDrugs(Connection con) {
 		JSONObject input = new JSONObject();
 		input.put("facility_id", "1");
 		input.put("category_id", "2");
 		JSONArray result = getDrugs(con, input);
 		System.out.println(result.toJSONString());
 	}
 
 	/**
 	 * This function will print an exemplary Result of the getOrderSummary
 	 * Function.
 	 * 
 	 * @param con
 	 *            Connection to be used
 	 */
 	private static void testGetOrderSummary(Connection con) {
 		JSONObject input = new JSONObject();
 		input.put("facility_id", "1");
 		// input.put("order_start", "2013-09-21 00:00:00");
 		JSONArray result = getOrderSummary(con, input);
 		System.out.println(result.toJSONString());
 //		input.put("summarize", "false");
 //		result = getOrderSummary(con, input);
 //		System.out.println(result.toJSONString());
 	}
 
 	private static void testAddDrug(Connection con) {
 		JSONObject input = new JSONObject();
 		input.put("med_name", "Antimonogamysol");
 		input.put("msdcode", "12345");
 		input.put("category_id", "8");
 		input.put("common_name", "Hippierol");
 		input.put("unit", "Normalized Love Unit");
 		input.put("unit_details", "3% Weed / NLU");
 		input.put("unit_price", "1.2");
 
 		boolean result = addDrug(con, input);
 		System.out.println(result);
 
 	}
 
 	private static void testUpdateDrug(Connection con) {
 		JSONObject input = new JSONObject();
 		input.put("id", "55");
 		input.put("unit_price", "1305.54");
 
 		boolean result = updateDrug(con, input);
 		System.out.println(result);
 
 	}
 	public static void main(String[] args) {
 		Connection con = getWebConnection();
 		// testGetOrderSummary(con);
 //		testUpdateDrug(con);
 //		testGetOrderSummary(con);
 		 testGetDrugs(con);
 		// input.put("facility_id", "1");
 		// input.put("order_start", "2013-09-21 00:00:00");
 		// input.put("drug_common_name", "Asp");
 		// JSONArray arr = getOrderSummary(con, input);
 		// System.out.println("");
 		// JSONObject one = (JSONObject) arr.get(0);
 		// JSONArray drugs = (JSONArray) one.get("drugs");
 		// JSONObject drug = (JSONObject) drugs.get(0);
 		// System.out.println(drug);
 		// System.out.println(arr);
 		// arr = getCategoryNames(con);
 
 	}
 
 }
