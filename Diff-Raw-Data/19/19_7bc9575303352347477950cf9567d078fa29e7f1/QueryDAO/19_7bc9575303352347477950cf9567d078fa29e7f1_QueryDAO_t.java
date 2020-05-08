 package com.cnnic.whois.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 import com.cnnic.whois.util.PermissionCache;
 import com.cnnic.whois.util.WhoisUtil;
 
 public class QueryDAO {
 	private static QueryDAO queryDAO = new QueryDAO();
 	private DataSource ds;
 	private PermissionCache permissionCache = PermissionCache
 			.getPermissionCache();
 
 	/**
 	 * Connect to the datasource in the constructor
 	 * 
 	 * @throws IllegalStateException
 	 */
 	private QueryDAO() throws IllegalStateException {
 		try {
 			InitialContext ctx = new InitialContext();
 			ds = (DataSource) ctx.lookup(WhoisUtil.JNDI_NAME);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new IllegalStateException(e.getMessage());
 		}
 	}
 
 	/**
 	 * Get QueryDAO objects
 	 * 
 	 * @return QueryDAO objects
 	 */
 	public static QueryDAO getQueryDAO() {
 		return queryDAO;
 	}
 
 	/**
 	 * Connect to the database query ip information
 	 * 
 	 * @param startHighAddr
 	 * @param endHighAddr
 	 * @param startLowAddr
 	 * @param endLowAddr
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryIP(long startHighAddr, long endHighAddr,
 			long startLowAddr, long endLowAddr, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 		String selectSql = "";
 		try {
 			connection = ds.getConnection();
 			if (startHighAddr == 0) { //If fuzzy matching with the SQL statement
 				selectSql = (endLowAddr == 0) ? (WhoisUtil.SELECT_LIST_IPv4_1
 						+ startLowAddr + WhoisUtil.SELECT_LIST_IPv4_2
 						+ startLowAddr + WhoisUtil.SELECT_LIST_IPv4_3)
 						: (WhoisUtil.SELECT_LIST_IPv4_4 + startLowAddr
 								+ WhoisUtil.SELECT_LIST_IPv4_2 + endLowAddr + WhoisUtil.SELECT_LIST_IPv4_5);
 			} else {
 				selectSql = (endLowAddr == 0) ? (WhoisUtil.SELECT_LIST_IPv6_1
 						+ startHighAddr + WhoisUtil.SELECT_LIST_IPv6_2
 						+ startHighAddr + WhoisUtil.SELECT_LIST_IPv6_3
 						+ startLowAddr + WhoisUtil.SELECT_LIST_IPv6_4
 						+ startHighAddr + WhoisUtil.SELECT_LIST_IPv6_5
 						+ startHighAddr + WhoisUtil.SELECT_LIST_IPv6_6
 						+ startLowAddr + WhoisUtil.SELECT_LIST_IPv6_7)
 
 						: (WhoisUtil.SELECT_LIST_IPv6_8 + startHighAddr
 								+ WhoisUtil.SELECT_LIST_IPv6_2 + startHighAddr
 								+ WhoisUtil.SELECT_LIST_IPv6_3 + startLowAddr
 								+ WhoisUtil.SELECT_LIST_IPv6_4 + endHighAddr
 								+ WhoisUtil.SELECT_LIST_IPv6_5 + endHighAddr
 								+ WhoisUtil.SELECT_LIST_IPv6_6 + endLowAddr
 								+ WhoisUtil.SELECT_LIST_IPv6_7 + WhoisUtil.SELECT_LIST_IPv6_9);
 			}
 			map = query(connection, selectSql,
 					permissionCache.getIPKeyFileds(role), "$mul$IP", role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query DNRDoamin information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryDNRDoamin(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_DNRDOMAIN + "'"
 					+ queryInfo + "'";
 			map = query(connection, selectSql,
 					permissionCache.getDNRDomainKeyFileds(role), "$mul$domain",
 					role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query RIRDoamin information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryRIRDoamin(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_RIRDOMAIN + "'"
 					+ queryInfo + "'";
 
 			map = query(connection, selectSql,
 					permissionCache.getRIRDomainKeyFileds(role), "$mul$domain",
 					role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query DNREntity information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryDNREntity(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_DNRENTITY + "'"
 					+ queryInfo + "'";
 			map = query(connection, selectSql,
 					permissionCache.getDNREntityKeyFileds(role), "$mul$entity",
 					role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query RIREntity information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryRIREntity(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_RIRENTITY + "'"
 					+ queryInfo + "'";
 			map = query(connection, selectSql,
 					permissionCache.getRIREntityKeyFileds(role), "$mul$entity",
 					role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query AS information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryAS(int queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_AS1 + queryInfo
 					+ WhoisUtil.SELECT_LIST_AS2 + queryInfo
 					+ WhoisUtil.SELECT_LIST_AS3;
 			map = query(connection, selectSql,
 					permissionCache.getASKeyFileds(role), "$mul$autnum", role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query nameServer information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryNameServer(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 
 			String selectSql = WhoisUtil.SELECT_LIST_NAMESREVER + "'"
 					+ queryInfo + "'";
 			map = query(connection, selectSql,
 					permissionCache.getNameServerKeyFileds(role),
 					"$mul$nameServer", role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query link information
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryLinks(String queryPara, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_LINK + "'" + queryPara
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getLinkKeyFileds(role), "$mul$link", role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query phone information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryPhones(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_PHONE + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getPhonesKeyFileds(role), "$mul$phones",
 					role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query postalAddress information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryPostalAddress(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_POSTALADDRESS + "'"
 					+ queryInfo + "'";
 			map = query(connection, selectSql,
 					permissionCache.getPostalAddressKeyFileds(role),
 					"$mul$postalAddress", role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query variant information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryVariants(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_VARIANTS + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getVariantsKeyFileds(role),
 					"$mul$variants", role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query delegationKey information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryDelegationKeys(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_DELEGATIONKEYS + "'"
 					+ queryInfo + "'";
 			map = query(connection, selectSql,
 					permissionCache.getDelegationKeyFileds(role),
 					"$mul$delegationKeys", role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query notices information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryNotices(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_NOTICES + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getNoticesKeyFileds(role), "$mul$notices",
 					role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query registrar information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryRegistrar(String queryInfo, String role,boolean isJoinTable)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			
 			String selectSql = WhoisUtil.SELECT_LIST_JOIN_REGISTRAR + "'"
 			+ queryInfo + "'";
 			
 			if(!isJoinTable)
 			selectSql = WhoisUtil.SELECT_LIST_REGISTRAR + "'"
 					+ queryInfo + "'";
 			
 			map = query(connection, selectSql,
 					permissionCache.getRegistrarKeyFileds(role),
 					"$mul$registrar", role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query remarks information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryRemarks(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_REMARKS + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getRemarksKeyFileds(role), "$mul$remarks",
 					role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Connect to the database query events information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryEvents(String queryInfo, String role)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_EVENTS + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getEventsKeyFileds(role), "$mul$events",
 					role);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * According to the table field collections and SQL to obtain the
 	 * corresponding data information
 	 * 
 	 * @param connection
 	 * @param sql
 	 * @param keyFlieds
 	 * @param keyName
 	 * @param role
 	 * @return map collection
 	 * @throws SQLException
 	 */
 	private Map<String, Object> query(Connection connection, String sql,
 			List<String> keyFlieds, String keyName, String role)
 			throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet results = null;
 
 		try {
 			stmt = connection.prepareStatement(sql);
 			results = stmt.executeQuery();
 
 			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 			while (results.next()) {
 
 				Map<String, Object> map = new LinkedHashMap<String, Object>();
 				
 				//Iteration results, according to the parameters in the collection can get to the data in the query results, 
 				//according to the order of the data in the collection will be added to the map collection from the return
 				
 				//the first most top conformance object
 				if(!keyName.startsWith(WhoisUtil.JOINFILEDPRX)){
 					Object[] conform = new Object[1];
 					conform[0] = "rdap_level_0";
 					map.put("rdapConformance", conform);
 				}
 				
 				for (int i = 0; i < keyFlieds.size(); i++) {
 					String resultsInfo = "";
 					
 					if(keyName.equals(WhoisUtil.MULTIPRXIP) && keyFlieds.get(i).toString().equals("StartLowAddress")// || 
 							//keyFlieds.get(i).toString().equals("EndLowAddress") ||
 							//keyFlieds.get(i).toString().equals("StartHighAddress") ||
 							//keyFlieds.get(i).toString().equals("EndHighAddress")
 							){
 						if(map.get("Start Address") == null && map.get("End Address") == null){
 							String ipVersion = results.getString("IP_Version");
 							
 							String startHighAddress = results.getString("StartHighAddress");
 							String startLowAddress = results.getString("StartLowAddress");
 							String endHighAddress = results.getString("EndHighAddress");
 							String endLowAddress = results.getString("EndLowAddress");
 							
 							String startAddress = "";
 							String endAddress = "";
 							
 							if (ipVersion != null) {
 								if (ipVersion.toString().indexOf("v6") != -1) { //judgment is IPv6 or IPv4
 									startAddress = WhoisUtil.ipV6ToString(
 											Long.parseLong(startHighAddress),
 											Long.parseLong(startLowAddress));
 									endAddress = WhoisUtil.ipV6ToString(
 											Long.parseLong(endHighAddress),
 											Long.parseLong(endLowAddress));
 								} else {
 									startAddress = WhoisUtil.longtoipV4(Long
 											.parseLong(startLowAddress));
 									endAddress = WhoisUtil
 											.longtoipV4(Long.parseLong(endLowAddress));
 								}
 								map.put("Start Address", startAddress);
 								map.put("End Address", endAddress);
 							}
 						}
 					}
 					
 					if (keyFlieds.get(i).startsWith(WhoisUtil.ARRAYFILEDPRX)) {
 						String key = keyFlieds.get(i).substring(
 								WhoisUtil.ARRAYFILEDPRX.length());
 						resultsInfo = results.getString(key);
 						String[] values = null;
 						if (resultsInfo != null) {
 							values = resultsInfo.split(WhoisUtil.VALUEARRAYPRX);
 						}
 						map.put(key.replaceAll("_", " "), values);
 					} else if (keyFlieds.get(i).startsWith(WhoisUtil.EXTENDPRX)) {
 						resultsInfo = results.getString(keyFlieds.get(i));
 						map.put(keyFlieds.get(i).substring(
 								WhoisUtil.EXTENDPRX.length()), resultsInfo);
 					} else if (keyFlieds.get(i).startsWith(
 							WhoisUtil.JOINFILEDPRX)) {
 						String key = keyFlieds.get(i).substring(
 								WhoisUtil.JOINFILEDPRX.length());
 						String fliedName = "";
 						if (keyName.equals("$mul$notices")
 								|| keyName.equals("$mul$remarks")) {
 							fliedName = keyName.substring("$mul$".length())
 									+ "Id";
 						} else {
 							fliedName = WhoisUtil.HANDLE;
 						}
 
 						Object value = queryJoinTable(keyFlieds.get(i),
 								results.getString(fliedName), sql, role,
 								connection);
 						if (value != null)
 							map.put(key, value);
 					} else {
 						//System.out.println(results.getString(keyFlieds.get(i)));
 						resultsInfo = results.getString(keyFlieds.get(i)) == null ? "": results.getString(keyFlieds.get(i));
 						String key = keyFlieds.get(i).replaceAll("_", " ");
 						map.put(key, resultsInfo);
 					}
 				}
 				map.remove("StartLowAddress");
 				map.remove("EndLowAddress");
 				map.remove("StartHighAddress");
 				map.remove("EndHighAddress");
 				
				if (keyName.equals("$mul$nameServer")){
					Map<String, Object> map_IP = new LinkedHashMap<String, Object>();
					Object IPAddressArray = map.get("IPV4 Addresses");
					map_IP.put(WhoisUtil.IPV4PREFIX, IPAddressArray);
					IPAddressArray = map.get("IPV6 Addresses");
					map_IP.put(WhoisUtil.IPV6PREFIX, IPAddressArray);
					map.put(WhoisUtil.IPPREFIX, map_IP);
					map.remove("IPV4 Addresses");
					map.remove("IPV6 Addresses");
				}
				
 				//vcard format
 				if(keyName.equals(WhoisUtil.JOINENTITESFILED) || keyName.equals(WhoisUtil.MULTIPRXENTITY)){
 					list.add(WhoisUtil.toVCard(map));
 				}else{
 					list.add(map);
 				}
 			}
 
 			if (list.size() == 0)
 				return null;
 			Map<String, Object> mapInfo = new LinkedHashMap<String, Object>();
 			//System.out.println(keyName+"=====================");
 			// link , remark and notice change to array
 			if(keyName.equals(WhoisUtil.JOINLINKFILED)|| 
 					keyName.equals(WhoisUtil.JOINNANOTICES) ||
 					keyName.equals(WhoisUtil.JOINREMARKS) ||
 					keyName.equals(WhoisUtil.MULTIPRXLINK ) ||
 					keyName.equals(WhoisUtil.MULTIPRXNOTICES )||
 					keyName.equals(WhoisUtil.MULTIPRXREMARKS)) {
 				mapInfo.put(keyName, list.toArray());
 			}else{
 				if (list.size() > 1) {
 					mapInfo.put(keyName, list.toArray());
 				} else {
 					mapInfo = list.get(0);
 				}
 			}
 			return mapInfo;
 		} finally {
 			if (results != null) {
 				try {
 					results.close();
 				} catch (SQLException se) {
 				}
 			}
 			if (stmt != null) {
 				try {
 					stmt.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Determine the different types of schedule and query information according
 	 * to the parameters
 	 * 
 	 * @param key
 	 * @param handle
 	 * @param sql
 	 * @param role
 	 * @param connection
 	 * @return Returns the schedule information content
 	 * @throws SQLException
 	 */
 	public Object queryJoinTable(String key, String handle, String sql,
 			String role, Connection connection) throws SQLException {
 		if (key.equals(WhoisUtil.JOINENTITESFILED)) {
 			String entitysql = WhoisUtil.SELECT_JOIN_LIST_JOINDNRENTITY;
 			if (sql.indexOf("ip") >= 0 || sql.indexOf("autnum") >= 0
 					|| sql.indexOf("RIRDomain") >= 0) {
 				entitysql = WhoisUtil.SELECT_JOIN_LIST_JOINRIRENTITY;
 				return querySpecificJoinTable(key, handle, entitysql, role,
 						connection, permissionCache.getRIREntityKeyFileds(role));
 			}else{
 				return querySpecificJoinTable(key, handle, entitysql, role,
 						connection, permissionCache.getDNREntityKeyFileds(role));
 			}
 			
 		} else if (key.equals(WhoisUtil.JOINLINKFILED)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_LINK, role, connection,
 					permissionCache.getLinkKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINPHONFILED)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_PHONE, role, connection,
 					permissionCache.getPhonesKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINPOSTATLADDRESSFILED)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_POSTALADDRESS, role, connection,
 					permissionCache.getPostalAddressKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINVARIANTS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_VARIANTS, role, connection,
 					permissionCache.getVariantsKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINDALEGATIONKEYS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_DELEGATIONKEYS, role,
 					connection, permissionCache.getDelegationKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINNAMESERVER)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_JOINNAMESERVER, role,
 					connection, permissionCache.getNameServerKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINNAREGISTRAR)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_REGISTRAR, role, connection,
 					permissionCache.getRegistrarKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINNANOTICES)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_NOTICES, role, connection,
 					permissionCache.getNoticesKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINEVENTS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_EVENTS, role, connection,
 					permissionCache.getEventsKeyFileds(role));
 		} else if (key.equals(WhoisUtil.JOINREMARKS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_REMARKS, role, connection,
 					permissionCache.getRemarksKeyFileds(role));
 		}
 
 		return null;
 	}
 
 	/**
 	 * query schedule information
 	 * 
 	 * @param key
 	 * @param handle
 	 * @param sql
 	 * @param role
 	 * @param connection
 	 * @param keyFlieds
 	 * @return Returns the schedule information content
 	 * @throws SQLException
 	 */
 	public Object querySpecificJoinTable(String key, String handle, String sql,
 			String role, Connection connection, List<String> keyFlieds)
 			throws SQLException {
 
 		Map<String, Object> map = query(connection, sql + "'" + handle + "'",
 				keyFlieds, key, role);
 		if (map != null) {
 			if (null == map.get(key)) {
 				return map;
 			} else if (!map.isEmpty()) {
 				return map.get(key);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Query redirect information
 	 * 
 	 * @param tableName
 	 * @param queryInfo
 	 * @throws QueryException
 	 * @throws RedirectExecption
 	 *             When the throw this exception query data, and the data
 	 *             content stored to the anomaly.
 	 */
 	public void queryRedirection(String tableName, String queryInfo)
 			throws QueryException, RedirectExecption {
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		ResultSet results = null;
 		String selectSql = "";
 
 		if (tableName.equals(WhoisUtil.AUTNUM)) {
 			int queryPara = Integer.parseInt(queryInfo);
 			selectSql = WhoisUtil.SELECT_URL_AUTNUM_EDIRECTION1 + queryPara
 					+ WhoisUtil.SELECT_URL_AUTNUM_EDIRECTION2 + queryPara;
 		} else {
 			queryInfo = queryInfo.substring(queryInfo.lastIndexOf(".") + 1);
 			selectSql = WhoisUtil.SELECT_URL_DOAMIN_REDIRECTION + "'"
 					+ queryInfo + "'";
 		}
 
 		try {
 			connection = ds.getConnection();
 			stmt = connection.prepareStatement(selectSql);
 			results = stmt.executeQuery();
 			if (results.next()) {
 				throw new RedirectExecption(results.getString("redirectURL"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 	}
 
 	/**
 	 * Query IPRedirection information
 	 * 
 	 * @param startHighAddr
 	 * @param endHighAddr
 	 * @param startLowAddr
 	 * @param endLowAddr
 	 * @throws QueryException
 	 * @throws RedirectExecption
 	 *             When the throw this exception query data, and the data
 	 *             content stored to the anomaly.
 	 */
 	public void queryIPRedirection(long startHighAddr, long endHighAddr,
 			long startLowAddr, long endLowAddr) throws QueryException,
 			RedirectExecption {
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		ResultSet results = null;
 
 		String selectSql = "";
 		if (startHighAddr == 0) {
 			selectSql = (WhoisUtil.SELECT_URL_IPV4_REDIRECTION1 + startLowAddr
 					+ WhoisUtil.SELECT_URL_IPV4_REDIRECTION2 + startLowAddr + WhoisUtil.SELECT_URL_IPV4_REDIRECTION3);
 		} else {
 
 			selectSql = WhoisUtil.SELECT_URL_IPV6_REDIRECTION1 + startHighAddr
 					+ WhoisUtil.SELECT_URL_IPV6_REDIRECTION2 + startHighAddr
 					+ WhoisUtil.SELECT_URL_IPV6_REDIRECTION3 + startLowAddr
 					+ WhoisUtil.SELECT_URL_IPV6_REDIRECTION4 + startHighAddr
 					+ WhoisUtil.SELECT_URL_IPV6_REDIRECTION5 + startHighAddr
 					+ WhoisUtil.SELECT_URL_IPV6_REDIRECTION6 + startLowAddr
 					+ WhoisUtil.SELECT_URL_IPV6_REDIRECTION7;
 
 		}
 		try {
 			connection = ds.getConnection();
 			stmt = connection.prepareStatement(selectSql);
 			results = stmt.executeQuery();
 			if (results.next()) {
 				throw new RedirectExecption(results.getString("redirectURL"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			throw new QueryException(e);
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException se) {
 				}
 			}
 		}
 	}
 }
