 package com.hgst.checkalertgroup;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.hgst.checkalertgroup.db.DBManager;
 import com.hgst.checkalertgroup.db.QueryProcesser;
 import com.hgst.checkalertgroup.io.CheckResultWriter;
 import com.hgst.checkalertgroup.model.CheckResult;
 import com.hgst.checkalertgroup.model.ResultType;
 import com.hgst.checkalertgroup.util.Combinatory;
 
 
 public class CheckAlertGroup implements Runnable {
 
 	private String url;
 	private String driver;
 	private String user;
 	private String password;
 	private DBManager dbm;
 	
 	private Env env;
 	private CheckResultWriter writer;
 	
 	private List<String> groupNames;
 	private List<String> withoutAreaRule = new ArrayList<String>();
 	private List<String> areaSameResult = new ArrayList<String>() ;
 	private List<String> areaDiffResult = new ArrayList<String>();
 	
 	private List<CheckResult> checkResults = new ArrayList<CheckResult>();
 	
 	public CheckAlertGroup(){}
 	
 	public CheckAlertGroup(Env env, CheckResultWriter writer) throws ClassNotFoundException {
 		this.env = env;
 		this.writer = writer;
 		this.url = this.env.get(Env.KEY_DB_URL);
 		this.driver = this.env.get(Env.KEY_DB_DRIVER);
 		this.user = this.env.get(Env.KEY_DB_USER);
 		this.password = this.env.get(Env.KEY_DB_PASSWORD);
 		dbm = new DBManager(url, driver, user, password);
 	}
 
 	@Override
 	public void run() {
 		connectionInfo();
 		
 		Date start = new Date();
 		System.out.println("begin time: " + start);
 		System.out.println("processing");
 		
 		groupNames = getAllGroupNames();
 		
 		// only check area parameter.
 		//checkArea(groupNames);
 		//writer.write(withoutAreaRule, areaSameResult, areaDiffResult);
 		
 		// check all of the invalid parameter
 		check(groupNames);
 		writer.write(checkResults);
 		
 		Date end = new Date();
		double cost = (end.getTime() - start.getTime()) / (1000 * 60) ;
 		System.out.println("finish. ");
		System.out.println("finish time: " + end + ". elapsed time: " + cost + " min.");
 	}
 	
 	List<String> getAllGroupNames() {
 		String sql = "SELECT DISTINCT GROUP" + 
 					"  FROM (SELECT OBJECTNAME, PARAMETERVALUE, LEFT(OBJECTNAME, LENGTH(OBJECTNAME) - 9) AS GROUP" +
 					"          FROM RULEDB.RULEPARAMTABLE" +
 					"          WHERE OBJECTNAME LIKE '%SeqInputGroupsOOC%' AND PARAMETERNAME = 'Name'" +
 					"          ORDER BY OBJECTNAME) AS T";
 		List<String> groupNames = dbm.executeQuery(sql, new QueryProcesser<List<String>>() {
 			@Override
 			public List<String> process(Statement stmt, ResultSet rs) throws SQLException {
 				List<String> groupNames = new ArrayList<String>();
 				while(rs.next()) {
 					groupNames.add(rs.getString("GROUP"));
 				}
 				return groupNames;
 			}
 		});
 		return groupNames;
 	}
 	
 	/**
 	 * check all of the the parameter
 	 * @param groupNames
 	 */
 	void check(List<String> groupNames) {
 		Connection conn = null; 
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = dbm.getConnection();
 			stmt = conn.createStatement();
 			int i = 1;
 			for(String groupName : groupNames) {
 				
 				if(i++ % 40 == 0)
 					System.out.println(".");
 				else 
 					System.out.print(".");
 				
 				// get parameter values
 				String sql = getParameterValueSQL(groupName);
 				List<String> paramValues = new ArrayList<String>();
 				rs = stmt.executeQuery(sql);
 				while(rs.next()) {
 					paramValues.add(rs.getString("PARAMETERVALUE"));
 				}
 				
 				int pSize = paramValues.size();
 				if(pSize <= 1) {
 					continue;
 				}
 				
 				// check
 				String checkSQL = getCheckSQL(paramValues);
 				rs = stmt.executeQuery(checkSQL);
 				while(rs.next()) {
 					int count = rs.getInt("C");
 					int parameterCount = rs.getInt("PARAMETERCOUNT");
 					String parameterValue = rs.getString("PARAMETERVALUE");
 					
 					if(count == 0) {
 						checkResults.add(new CheckResult(groupName, ResultType.RESULT_EMPTY, "query result is empty."));
 					} else if(parameterCount != pSize) {
 						extractInvalidParamters(paramValues, parameterValue);
 						checkResults.add(new CheckResult(groupName, ResultType.INVALID_PARAMTER, "invalid parameters", paramValues));
 					}
 					break;
 				}
 			}
 			
 			System.out.println();
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			dbm.close(conn, stmt, rs);
 		}
 	}
 	
 	public String getCheckSQL(List<String> paramValues) {
 		List<String> sqls = new ArrayList<String>();
 		Combinatory<String> comb = new Combinatory<String>(paramValues.toArray(new String[paramValues.size()]), String.class);
 		for(int i = 1, len = paramValues.size(); i <= len; i++) {
 			List<String[]> conditionList = comb.combine(i);
 			for(String[] conditions : conditionList) {
 				sqls.add(generateOneSQL(conditions));
 			}
 		}
 		
 		StringBuilder sbResult = new StringBuilder();
 		sbResult.append("SELECT C, PARAMETERVALUE, PARAMETERCOUNT FROM (");
 		sbResult.append(join(sqls.toArray(new String[sqls.size()]), " UNION "));
 		sbResult.append(") T ");
 		sbResult.append("ORDER BY T.C ASC, T.PARAMETERCOUNT ASC");
 		
 		return sbResult.toString();
 	}
 	
 	public String generateOneSQL(String[] conditions) {
 		StringBuilder sbSQL = new StringBuilder();
 		sbSQL.append("SELECT COUNT(0) C, '")
 			.append(join(conditions, ","))
 			.append("' PARAMETERVALUE, ")
 			.append(conditions.length)
 			.append(" PARAMETERCOUNT ");
 		sbSQL.append("FROM EVENTS.HMNY_ACT WHERE ");
 		sbSQL.append(join(wrapWhere(conditions), " AND "));
 		return sbSQL.toString();
 	}
 	
 	public String join(String[] arrays, String conjunction) {
 		StringBuilder sb = new StringBuilder();
 		boolean first = true;
 		for(String item : arrays) {
 			if(first) first = false;
 			else sb.append(conjunction);
 			sb.append(item);
 		}
 		return sb.toString();
 	}
 	
 	private String[] wrapWhere(String[] conditions) {
 		String[] wrap = new String[conditions.length];
 		for(int i = 0; i < conditions.length; i++) {
 			wrap[i] = "EVENTGROUPS LIKE '%" + conditions[i] + "%'";
 		}
 		return wrap;
 	}
 	
 	private void extractInvalidParamters(List<String> params, String validParameters) {
 		String[] invalids = validParameters.split(",");
 		for(String invalid : invalids) {
 			params.remove(invalid);
 		}
 	}
 	
 	
 	/**
 	 * Only Check Area Parameter.
 	 * 
 	 * @param groupNames
 	 */
 	void checkArea(List<String> groupNames) {
 		Connection conn = null; 
 		Statement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = dbm.getConnection();
 			stmt = conn.createStatement();
 			for(String groupName : groupNames) {
 				
 				// get parameter values
 				String sql = getParameterValueSQL(groupName);
 				List<String> paramValues = new ArrayList<String>();
 				rs = stmt.executeQuery(sql);
 				while(rs.next()) {
 					paramValues.add(rs.getString("PARAMETERVALUE"));
 				}
 				
 				// check if has area value
 				if(areaRuleNotExist(paramValues)) {
 					withoutAreaRule.add(groupName);
 					continue;
 				}
 				
 				// check if has the same result
 				String checkSQL = getCheckAreaSQL(paramValues);
 				rs = stmt.executeQuery(checkSQL);
 				while(rs.next()) {
 					if(rs.getInt(1) == 1) { // same result whether the areaRule exist or not
 						areaSameResult.add(groupName);
 					} else { // different result
 						areaDiffResult.add(groupName);
 					}
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			dbm.close(conn, stmt, rs);
 		}
 	}
 	
 	private String getCheckAreaSQL(List<String> rules) {
 		String sqlMain = "SELECT COUNT(0) FROM ", 
 				sqlFrom = "SELECT COUNT(0) AS PART FROM EVENTS.HMNY_ACT WHERE ";
 		String allWhere = "", partWhere = "";
 		for(String rule : rules) {
 			allWhere += " AND EVENTGROUPS LIKE '%" + rule + "%'";
 			if(!containArea(rule)) {
 				partWhere += " AND EVENTGROUPS LIKE '%" + rule + "%'";
 			}
 		}
 		if(allWhere.length() > 5) {
 			allWhere = allWhere.substring(4); // delete the first "AND"
 		}
 		if(partWhere.length() > 5) {
 			partWhere = partWhere.substring(4); // delete the first "AND"
 		}
 		String sql = sqlMain + "(" + (sqlFrom + allWhere) + " UNION " + (sqlFrom + partWhere) + ")";
 		return sql;
 	}
 
 	private String getParameterValueSQL(String groupName) {
 		String sql = "SELECT PARAMETERVALUE" +
 					"  FROM RULEDB.RULEPARAMTABLE" +
 					"  WHERE PARAMETERNAME = 'Name'" +
 					"    AND OBJECTNAME LIKE '"+groupName+"%'";
 		return sql;
 	}
 	
 	private boolean areaRuleNotExist(List<String> rules) {
 		if (rules.size() > 0) {
 			for (String rule : rules) {
 				if (containArea(rule))
 					return false;
 			}
 		}
 		return true;
 	}
 	
 	private boolean containArea(String ruleName) {
 		return ruleName.indexOf("/area/") > 0;
 	}
 	
 	void connectionInfo() {
 		System.out.println("----------------");
 		System.out.println("url:\t" + url);
 		System.out.println("user:\t" + user);
 //		System.out.println("password:\t" + password);
 		System.out.println("driver:\t" + driver);
 		System.out.println("----------------");
 	}
 	
 	// 使用模板
 	List<String> getAllFoods() {
 		String sql = "select * from 1000funs.food";
 		List<String> foodNames = dbm.executeQuery(sql, new QueryProcesser<List<String>>() {
 
 			@Override
 			public List<String> process(Statement stmt, ResultSet rs) throws SQLException {
 				List<String> foodNames = new ArrayList<String>();
 				while(rs.next()) {
 					foodNames.add(rs.getString("food_name"));
 				}
 				
 				//这里可以使用stmt对象再做进一步的查询处理
 				String sql2 = "select * FROM 1000funs.package_group";
 				rs = stmt.executeQuery(sql2);
 				while(rs.next()) {
 					foodNames.add(rs.getString("group_name"));
 				}
 				
 				return foodNames;
 			}
 			
 		});
 		System.out.println(foodNames);
 		return foodNames;
 	}
 
 }
