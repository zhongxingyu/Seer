 package store;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 
 public class SelectBuilder {
 	// Contains all the condition format strings for the WHERE clause in the SQL
 	// string
 	private ArrayList<String> whereStrArr;
 	// List of operators to put into a map and check against those passed in
 	private final static String[] OPS_SET_VALS = { "LIKE", "IN", "BETWEEN",
 			"=", "<>", ">=", "<=" };
 	private final static HashSet<String> OPS = new HashSet<String>(
 			Arrays.asList(OPS_SET_VALS));
 	// Lists of different types of parameters
 	// Maps to arrays to allow for sql operators that take more than one
 	// condition
 	// Using HashMap to approximate a *sparse* array
 	private HashMap<Integer, Integer[]> intParams;
 	private HashMap<Integer, String[]> stringParams;
 	private HashMap<Integer, Double[]> doubleParams;
 	private int numParams; // Number of parameters in all of the hashmaps
 							// combined
 
 	private String[] columnArr; // Holds the columns to SELECT upon
 	private String tableName; // Holds the table name to SELECT upon
 
 	public static boolean isOperator(String op) {
 		return OPS.contains(op);
 	}
 
 	/**
 	 * Takes an array of column names and table name to get the results from.
 	 * 
 	 * @param columnArr
 	 *            Array of column names
 	 * @param tableName
 	 *            String name of the table to select from
 	 */
 	SelectBuilder(String[] columnArr, String tableName) {
 		// PRE: columnArr must not be null and have at least one element.
 		// tableName must not be null
 
 		this.columnArr = columnArr.clone();
 		this.tableName = tableName;
 		this.numParams = 0;
 		this.whereStrArr = new ArrayList<String>();
 		this.intParams = new HashMap<Integer, Integer[]>();
 		this.stringParams = new HashMap<Integer, String[]>();
 		this.doubleParams = new HashMap<Integer, Double[]>();
 
 	}
 
 	/**
 	 * Helper function to make the condition format string
 	 * 
 	 * @param colName
 	 *            String name of the column to apply the condition - no
 	 *            sanitization on colName
 	 * @param operator
 	 *            String name of the operator, not IN or BETWEEN which are
 	 *            special
 	 * @param conditionArr
 	 *            String array condition applied, NOT NULL
 	 * @param isAnd
 	 *            true if AND'ing in the condition otherwise false - meaningless
 	 *            for the first condition
 	 * @return Format string for a PreparedStatement
 	 * @throws SQLException
 	 *             if size of conditionArr does not match what is expected by
 	 *             the operator.
 	 * @throws Exception
 	 *             if the parameter, operator, is not a recognized operator
 	 */
 	private String makeFormatString(String colName, String operator,
 			Object[] conditionArr, boolean isAnd) throws SQLException,
 			Exception {
 		String condFormatStr = ""; // Holds the format string for this condition
 		if (conditionArr.length == 0) {
 			throw new SQLException("conditionArr cannot have length 0");
 		}
 		if (isOperator(operator)) {
 			// Setup the format string for this condition
 			if (whereStrArr.size() > 0) {
 				if (isAnd) {
 					condFormatStr += "AND ";
 				} else {
 					condFormatStr += "OR ";
 				}
 			}
 			condFormatStr += colName + " " + operator + " ";
 			if (operator.equalsIgnoreCase("BETWEEN")) {
 				if (conditionArr.length != 2) {
 					throw new SQLException("BETWEEN needs 2 args");
 				}
 				condFormatStr += "? AND ?";
 			} else if (operator.equalsIgnoreCase("IN")) {
 				condFormatStr += "(";
 				if (conditionArr.length != 0) // first one = no preceding comma
 					condFormatStr += "?";
 				for (int i = 1; i < conditionArr.length; i++) {
 					condFormatStr += ",?";
 				}
 				condFormatStr += ")";
 			} else {
 				condFormatStr += "?";
 			}
 			return condFormatStr;
 		} else {
 			throw new Exception(
 					String.format("%s is not an operator", operator));
 		}
 	}
 
 	/**
 	 * Adds the string condition to the where clause
 	 * 
 	 * @param colName
 	 *            String name of the column to apply the condition
 	 * @param operator
 	 *            String name of the operator, not IN or BETWEEN which are
 	 *            special
 	 * @param condition
 	 *            String array condition applied, NOT NULL
 	 * @param isAnd
 	 *            true if AND'ing in the condition otherwise false - meaningless
 	 *            for the first condition
 	 * @throws SQLException
 	 *             if size of conditionArr does not match what is expected by
 	 *             the operator.
 	 * @throws Exception
 	 *             if the parameter, operator, is not a recognized operator
 	 */
 	public void addStringCondition(String colName, String operator,
 			String[] conditionArr, boolean isAnd) throws SQLException,
 			Exception {
 		String condFormatStr = makeFormatString(colName, operator,
 				conditionArr, isAnd);
 
 		// Add to where-format-strings and string-parameters ArrayLists
 		whereStrArr.add(condFormatStr);
 		stringParams.put(numParams, conditionArr.clone());
 		numParams++;
 	}
 
 	/**
 	 * Adds the string condition to the where clause
 	 * 
 	 * @param colName
 	 *            String name of the column to apply the condition
 	 * @param operator
 	 *            String name of the operator, not IN or BETWEEN which are
 	 *            special
 	 * @param condition
 	 *            Integer condition applied
 	 * @throws SQLException
 	 *             if size of conditionArr does not match what is expected by
 	 *             the operator.
 	 * @throws Exception
 	 *             if the parameter, operator, is not a recognized operator
 	 */
 	public void addIntCondition(String colName, String operator,
 			Integer[] conditionArr, boolean isAnd) throws SQLException,
 			Exception {
 		String condFormatStr = makeFormatString(colName, operator,
 				conditionArr, isAnd);
 
 		// Add to where-format-strings and string-parameters ArrayLists
 		whereStrArr.add(condFormatStr);
 		intParams.put(numParams, conditionArr.clone());
 		numParams++;
 	}
 
 	/**
 	 * Adds the string condition to the where clause
 	 * 
 	 * @param colName
 	 *            String name of the column to apply the condition
 	 * @param operator
 	 *            String name of the operator, not IN or BETWEEN which are
 	 *            special
 	 * @param condition
 	 *            Double condition applied
 	 * @throws SQLException
 	 *             if size of conditionArr does not match what is expected by
 	 *             the operator.
 	 * @throws Exception
 	 *             if the parameter, operator, is not a recognized operator
 	 */
 	public void addDoubleCondition(String colName, String operator,
 			Double[] conditionArr, boolean isAnd) throws SQLException,
 			Exception {
 		String condFormatStr = makeFormatString(colName, operator,
 				conditionArr, isAnd);
 
 		// Add to where-format-strings and string-parameters ArrayLists
 		whereStrArr.add(condFormatStr);
 		doubleParams.put(numParams, conditionArr.clone());
 		numParams++;
 	}
 
 	/**
 	 * Executes the built select query on the passed connection
 	 * 
 	 * @param con
 	 *            Connection to execute the select query on
 	 * @return ResultSet of the SQL select statement
 	 * @throws SQLException
 	 *             if something goes wrong with the internal PreparedStatement
 	 *             or the passed connection
 	 */
 	public ResultSet executeSelect(Connection con) throws SQLException {
 		PreparedStatement ps;
 		String sqlString = "SELECT ";
 		String[] strParamArr;
 		Integer[] intParamArr;
 		Double[] dblParamArr;
 		int paramIndex = 1;
 		// Columns
 		if (this.columnArr.length > 0) {
 			sqlString += this.columnArr[0];
 		}
 		for (int i = 1; i < this.columnArr.length; i++) {
			sqlString += ", "+this.columnArr[i];
 		}
 		// Table
 		sqlString += " FROM " + tableName + " ";
 
 		if (numParams > 0) {
 			sqlString += "WHERE ";
 		}
 		// Hopefully a sane assertion
 		assert numParams == whereStrArr.size() : String.format(
 				"numParams: %d is not the same as whereStrArr.size: %d",
 				numParams, whereStrArr.size());
 
 		// Make that PreparedStatement string
 		for (int i = 0; i < whereStrArr.size(); i++) {
 			sqlString += " " + whereStrArr.get(i);
 		}
 
 		ps = con.prepareStatement(sqlString);
 
 		// Populate all the where params in order
 		for (int i = 0; i < numParams; i++) {
 			if ((strParamArr = stringParams.get(i)) != null) {
 				for (int j = 0; j < strParamArr.length; j++) {
 					ps.setString(paramIndex, strParamArr[j]);
 					paramIndex++;
 				}
 			} else if ((intParamArr = this.intParams.get(i)) != null) {
 				for (int j = 0; j < intParamArr.length; j++) {
 					ps.setInt(paramIndex, intParamArr[j]);
 					paramIndex++;
 				}
 			} else if ((dblParamArr = this.doubleParams.get(i)) != null) {
 				for (int j = 0; j < dblParamArr.length; j++) {
 					ps.setDouble(paramIndex, dblParamArr[j]);
 					paramIndex++;
 				}
 			}
 		}
 		// execute it baby
 		return ps.executeQuery();
 	}
 }
