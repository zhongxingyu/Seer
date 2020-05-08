 package com.cnnic.whois.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 import com.cnnic.whois.bean.PageBean;
 import com.cnnic.whois.bean.index.DomainIndex;
 import com.cnnic.whois.bean.index.EntityIndex;
 import com.cnnic.whois.bean.index.Index;
 import com.cnnic.whois.bean.index.NameServerIndex;
 import com.cnnic.whois.bean.index.SearchCondition;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 import com.cnnic.whois.service.DomainIndexService;
 import com.cnnic.whois.service.EntityIndexService;
 import com.cnnic.whois.service.NameServerIndexService;
 import com.cnnic.whois.service.QueryService;
 import com.cnnic.whois.service.index.SearchResult;
 import com.cnnic.whois.util.PermissionCache;
 import com.cnnic.whois.util.WhoisUtil;
 
 public class QueryDAO {
 	private static QueryDAO queryDAO = new QueryDAO();
 	private DataSource ds;
 	private PermissionCache permissionCache = PermissionCache
 			.getPermissionCache();
 	private DomainIndexService domainIndexService = DomainIndexService.getIndexService();
 	private NameServerIndexService nameServerIndexService = NameServerIndexService.getIndexService();
 	private EntityIndexService entityIndexService = EntityIndexService.getIndexService();
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
 			long startLowAddr, long endLowAddr, String role, String format)
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
 			
 			Map<String, Object> ipMap = query(connection, selectSql,
 					permissionCache.getIPKeyFileds(role), "$mul$IP", role, format);
 			if(ipMap != null){
 				map = rdapConformance(map);
 				map.putAll(ipMap);
 			}
 			if(map != null){
 				map.remove(WhoisUtil.getDisplayKeyName("StartLowAddress", format));
 				map.remove(WhoisUtil.getDisplayKeyName("EndLowAddress", format));
 				map.remove(WhoisUtil.getDisplayKeyName("StartHighAddress", format));
 				map.remove(WhoisUtil.getDisplayKeyName("EndHighAddress", format));
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
 		return map;
 	}
 	
 	public Map<String, Object> queryEntity(String queryPara, String role, String format)
 			throws QueryException, SQLException {
 		SearchResult<EntityIndex> result = entityIndexService.preciseQueryEntitiesByHandleOrName(queryPara);
 		String selectSql = WhoisUtil.SELECT_LIST_RIRENTITY;
 		Connection connection = null;
 		Map<String, Object> map = null;
 		try {
 			connection = ds.getConnection();
 			map = queryDAO.fuzzyQuery(connection, result,selectSql,"$mul$entity", role, format);
 			map = rdapConformance(map);
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
 	
 	public Map<String, Object> fuzzyQueryEntity(String fuzzyQueryParamName,String queryPara,
 			String role, String format, PageBean page)
 			throws QueryException, SQLException {
 		SearchResult<EntityIndex> result = entityIndexService
 				.fuzzyQueryEntitiesByHandleAndName(fuzzyQueryParamName,queryPara,page);
 		String selectSql = WhoisUtil.SELECT_LIST_RIRENTITY;
 		Connection connection = null;
 		Map<String, Object> map = null;
 		try {
 			connection = ds.getConnection();
 			Map<String, Object> entityMap = queryDAO.fuzzyQuery(connection, result,selectSql,"$mul$entity", role, format);
 			if(entityMap != null){
 				map = rdapConformance(map);
 				map.putAll(entityMap);
 				addTruncatedParamToMap(map, result);
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
 		return map;
 	}
 	
 	public Map<String, Object> fuzzyQueryNameServer(String queryInfo, String role, 
 			String format, PageBean page)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 		SearchCondition searchCondition = new SearchCondition(queryInfo);
 		int startPage = page.getCurrentPage() - 1;
 		startPage = startPage >= 0 ? startPage : 0;
 		int start = startPage * page.getMaxRecords();
 		searchCondition.setStart(start);
 		searchCondition.setRow(page.getMaxRecords());
 		SearchResult<NameServerIndex> result = nameServerIndexService.queryNameServers(searchCondition);
 		page.setRecordsCount(Long.valueOf(result.getTotalResults()).intValue());
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_NAMESREVER + "'"
 					+ queryInfo + "'";
 			Map<String, Object> nsMap = fuzzyQuery(connection, result,selectSql,
 					"$mul$nameServer", role, format);
 			if(nsMap != null){
 				map = rdapConformance(map);
 				map.putAll(nsMap);
 				addTruncatedParamToMap(map, result);
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
 		return map;
 	}
 
 	public Map<String, Object> fuzzyQueryDoamin(String domain, String domainPuny, String role, 
 			String format, PageBean page)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 		SearchCondition searchCondition = new SearchCondition("ldhName:"+domainPuny + " OR unicodeName:"+domain);
 		int startPage = page.getCurrentPage() - 1;
 		startPage = startPage >= 0 ? startPage : 0;
 		int start = startPage * page.getMaxRecords();
 		searchCondition.setStart(start);
 		searchCondition.setRow(page.getMaxRecords());
 		SearchResult<DomainIndex> result = domainIndexService.queryDomains(searchCondition);
 		page.setRecordsCount(Long.valueOf(result.getTotalResults()).intValue());
 		if(result.getResultList().size()==0){
 			return map;
 		}
 		try {
 			connection = ds.getConnection();
 			String sql = WhoisUtil.SELECT_LIST_DNRDOMAIN;
 			DomainIndex domainIndex = result.getResultList().get(0);
 			if("rirDomain".equals(domainIndex.getDocType())){
 				sql = WhoisUtil.SELECT_LIST_RIRDOMAIN;
 			}
 			Map<String, Object> domainMap = this.fuzzyQuery(connection, result,sql,"$mul$domains",
 					role, format);
 			if(domainMap != null){
 				map =  rdapConformance(map);
 				map.putAll(domainMap);
 				addTruncatedParamToMap(map, result);
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
 		return map;
 	}
 
 	private void addTruncatedParamToMap(Map<String, Object> map,
 			SearchResult<? extends Index> result) {
 		if(result.getTotalResults()>QueryService.MAX_SIZE_FUZZY_QUERY){
 			map.put(WhoisUtil.SEARCH_RESULTS_TRUNCATED_EKEY, true);
 		}
 	}
 	
 	private Map<String, Object> fuzzyQuery(Connection connection, SearchResult<? extends Index> domains,
 			String selectSql,String keyName, String role, String format)
 			throws SQLException {
 			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 			for(Index index:domains.getResultList()){
 				index.initPropValueMap();
 				List<String> keyFlieds = permissionCache.getKeyFiledsByClass(index, role);
 				Map<String, Object> map = new LinkedHashMap<String, Object>();
 				for (int i = 0; i < keyFlieds.size(); i++) {
 					Object resultsInfo = null;
 					if (keyFlieds.get(i).startsWith(WhoisUtil.ARRAYFILEDPRX)) {
 						String key = keyFlieds.get(i).substring(WhoisUtil.ARRAYFILEDPRX.length());
 						resultsInfo = index.getPropValue(key);
 						String[] values = null;
 						if (resultsInfo != null) {
 							values = resultsInfo.toString().split(WhoisUtil.VALUEARRAYPRX);
 						}
 						map.put(WhoisUtil.getDisplayKeyName(key, format), values);
 					} else if (keyFlieds.get(i).startsWith(WhoisUtil.EXTENDPRX)) {
 						resultsInfo = index.getPropValue(keyFlieds.get(i));
 						map.put(keyFlieds.get(i).substring(WhoisUtil.EXTENDPRX.length()), resultsInfo);
 					} else if (keyFlieds.get(i).startsWith(WhoisUtil.JOINFILEDPRX)) {
 						String key = keyFlieds.get(i).substring(WhoisUtil.JOINFILEDPRX.length());
 						Object value = queryJoinTable(keyFlieds.get(i),
 								index.getHandle(), selectSql, role,
 								connection, format);
 						if (value != null)
 							map.put(key, value);
 					} else {
 						resultsInfo = index.getPropValue(keyFlieds.get(i));
 						resultsInfo = resultsInfo==null?"":resultsInfo;
 						CharSequence id = "id";
 						if(!keyName.equals(WhoisUtil.JOINPUBLICIDS) && WhoisUtil.getDisplayKeyName(keyFlieds.get(i), format).substring(keyFlieds.get(i).length() - 2).equals(id) && !format.equals("application/html")){
 							continue;
 						}else{
 							map.put(WhoisUtil.getDisplayKeyName(keyFlieds.get(i), format), resultsInfo);//a different format have different name;
 						}
 					}
 				}
 				//vcard format
 				if(keyName.equals(WhoisUtil.MULTIPRXENTITY)){
 					list.add(WhoisUtil.toVCard(map, format));
 				}else{
 					list.add(map);
 				}
 			}
 			if (list.size() == 0){
 				return null;
 			}
 			Map<String, Object> mapInfo = new LinkedHashMap<String, Object>();
 			if (list.size() > 1) {
 				mapInfo.put(keyName, list.toArray());
 			} else {
 				mapInfo = list.get(0);
 			}
 			return mapInfo;
 	}
 	
 	/**
 	 * Connect to the database query DNRDoamin information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryDNRDoamin(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_DNRDOMAIN + "'"
 					+ queryInfo + "'";
 			Map<String, Object> domainMap = query(connection, selectSql,
 					permissionCache.getDNRDomainKeyFileds(role), "$mul$domains",
 					role, format);
 			if(domainMap != null){
 				map =  rdapConformance(map);
 				map.putAll(domainMap);
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
 	public Map<String, Object> queryRIRDoamin(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_RIRDOMAIN + "'"
 					+ queryInfo + "'";
 			Map<String, Object> domainMap = query(connection, selectSql,
 					permissionCache.getRIRDomainKeyFileds(role), "$mul$domains",
 					role, format);
 			if(domainMap != null){
 				map = rdapConformance(map);
 				map.putAll(domainMap);
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
 	public Map<String, Object> queryDNREntity(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_DNRENTITY + "'"
 					+ queryInfo + "'";
 			Map<String, Object> entityMap = query(connection, selectSql,
 					permissionCache.getDNREntityKeyFileds(role), "$mul$entity",
 					role, format);
 			if(entityMap != null){
 				map = rdapConformance(map);
 				map.putAll(entityMap);
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
 	public Map<String, Object> queryRIREntity(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_RIRENTITY + "'"
 					+ queryInfo + "'";
 			Map<String, Object> entityMap = query(connection, selectSql,
 					permissionCache.getRIREntityKeyFileds(role), "$mul$entity",
 					role, format);
 			if(entityMap != null){
 				map = rdapConformance(map);
 				map.putAll(entityMap);
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
 	public Map<String, Object> queryAS(int queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_AS1 + queryInfo
 					+ WhoisUtil.SELECT_LIST_AS2 + queryInfo
 					+ WhoisUtil.SELECT_LIST_AS3;
 			Map<String, Object> asMap = query(connection, selectSql,
 					permissionCache.getASKeyFileds(role), "$mul$autnum", role, format);
 			if(asMap != null){
 				map = rdapConformance(map);
 				map.putAll(asMap);
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
 	public Map<String, Object> queryNameServer(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 
 			String selectSql = WhoisUtil.SELECT_LIST_NAMESREVER + "'"
 					+ queryInfo + "'";
 			Map<String, Object> nsMap = query(connection, selectSql,
 					permissionCache.getNameServerKeyFileds(role),
 					"$mul$nameServer", role, format);
 			if(nsMap != null){
 				map = rdapConformance(map);
 				map.putAll(nsMap);
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
 	public Map<String, Object> queryLinks(String queryPara, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_LINK + "'" + queryPara
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getLinkKeyFileds(role), "$mul$link", role, format);
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
 	public Map<String, Object> queryPhones(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_PHONE + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getPhonesKeyFileds(role), "$mul$phones",
 					role, format);
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
 	public Map<String, Object> queryPostalAddress(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_POSTALADDRESS + "'"
 					+ queryInfo + "'";
 			map = query(connection, selectSql,
 					permissionCache.getPostalAddressKeyFileds(role),
 					"$mul$postalAddress", role, format);
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
 	public Map<String, Object> queryVariants(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_VARIANTS + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getVariantsKeyFileds(role),
 					"$mul$variants", role, format);
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
 	 * Connect to the database query SecureDNS information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> querySecureDNS(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_SECUREDNS + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getSecureDNSMapKeyFileds(role),
 					"$mul$secureDNS", role, format);
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
 	 * Connect to the database query DsData information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryDsData(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_DSDATA + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getDsDataMapKeyFileds(role),
 					"$mul$dsData", role, format);
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
 	 * Connect to the database query SecureDNS information
 	 * 
 	 * @param queryInfo
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	public Map<String, Object> queryKeyData(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_KEYDATA + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getKeyDataMapKeyFileds(role),
 					"$mul$keyData", role, format);
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
 	public Map<String, Object> queryDelegationKeys(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_DELEGATIONKEYS + "'"
 					+ queryInfo + "'";
 			map = query(connection, selectSql,
 					permissionCache.getDelegationKeyFileds(role),
 					"$mul$delegationKeys", role, format);
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
 	public Map<String, Object> queryNotices(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_NOTICES + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getNoticesKeyFileds(role), "$mul$notices",
 					role, format);
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
 	public Map<String, Object> queryRegistrar(String queryInfo, String role, 
 			boolean isJoinTable, String format)
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
 					"$mul$registrar", role, format);
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
 	public Map<String, Object> queryRemarks(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_REMARKS + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getRemarksKeyFileds(role), "$mul$remarks",
 					role, format);
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
 	public Map<String, Object> queryEvents(String queryInfo, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 
 		try {
 			connection = ds.getConnection();
 			String selectSql = WhoisUtil.SELECT_LIST_EVENTS + "'" + queryInfo
 					+ "'";
 			map = query(connection, selectSql,
 					permissionCache.getEventsKeyFileds(role), "$mul$events",
 					role, format);
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
 	 * Generate an error map collection
 	 * 
 	 * @param errorCode
 	 * @param title
 	 * @param description
 	 * @return map
 	 */
 	public Map<String, Object> getErrorMessage(String errorCode, String role, String format)
 			throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 		try {
 			connection = ds.getConnection();
 
 			String selectSql = WhoisUtil.SELECT_LIST_ERRORMESSAGE + "'"
 					+ errorCode + "'";
 			Map<String, Object> errorMessageMap = query(connection, selectSql,
 					permissionCache.getErrorMessageKeyFileds(role),
 					"$mul$errormessage", role, format);
 			if(errorMessageMap != null){
 				map =  rdapConformance(map);
 				map.putAll(errorMessageMap);
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
 			List<String> keyFlieds, String keyName, String role, String format)
 			throws SQLException {
 		PreparedStatement stmt = null; 
 		ResultSet results = null;
 		String entityHandle = null;
 
 		try {
 			stmt = connection.prepareStatement(sql);
 			results = stmt.executeQuery();
 
 			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 			
 			while (results.next()) {
 				//Iteration results, according to the parameters in the collection can get to the data in the query results, 
 				//according to the order of the data in the collection will be added to the map collection from the return
 				Map<String, Object> map = new LinkedHashMap<String, Object>();
 				for (int i = 0; i < keyFlieds.size(); i++) {
 					Object resultsInfo = null;
 					
 					if(keyName.equals(WhoisUtil.MULTIPRXIP) && keyFlieds.get(i).toString().equals("StartLowAddress")){
 						if((map.get("Start Address") == null && map.get("End Address") == null) || (map.get("startAddress") == null && map.get("endAddress") == null)){
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
 								map.put(WhoisUtil.getDisplayKeyName("Start_Address", format), startAddress);//a different fromat have different name;
 								map.put(WhoisUtil.getDisplayKeyName("End_Address", format), endAddress);
 							}
 						}
 					}
 					
 					if (keyFlieds.get(i).startsWith(WhoisUtil.ARRAYFILEDPRX)) {
 						String key = keyFlieds.get(i).substring(WhoisUtil.ARRAYFILEDPRX.length());
 						resultsInfo = results.getString(key);
 						String[] values = null;
 						if (resultsInfo != null) {
 							values = resultsInfo.toString().split(WhoisUtil.VALUEARRAYPRX);
 						}
 						map.put(WhoisUtil.getDisplayKeyName(key, format), values);
 					} else if (keyFlieds.get(i).startsWith(WhoisUtil.EXTENDPRX)) {
 						resultsInfo = results.getString(keyFlieds.get(i));
 						map.put(keyFlieds.get(i).substring(WhoisUtil.EXTENDPRX.length()), resultsInfo);
 					} else if (keyFlieds.get(i).startsWith(WhoisUtil.JOINFILEDPRX)) {
 						String key = keyFlieds.get(i).substring(WhoisUtil.JOINFILEDPRX.length());
 						String fliedName = "";
 						if (keyName.equals(WhoisUtil.MULTIPRXNOTICES) || keyName.equals(WhoisUtil.MULTIPRXREMARKS)) {
 							fliedName = keyName.substring(WhoisUtil.MULTIPRX.length()) + "Id";
 						} else if(keyName.equals(WhoisUtil.JOINNANOTICES) || keyName.equals(WhoisUtil.JOINREMARKS)){
 							fliedName = keyName.substring(WhoisUtil.JOINFILEDPRX.length()) + "Id";
 						}else if (keyName.equals("$mul$errormessage")){
 							fliedName = "Error_Code";
 						}else if (keyName.equals(WhoisUtil.JOINSECUREDNS) || keyName.equals("$mul$secureDNS")){
 							fliedName = "SecureDNSID";
 						}else if (keyName.equals(WhoisUtil.JOINDSDATA) || keyName.equals("$mul$dsData")){
 							fliedName = "DsDataID";
 						}else if (keyName.equals(WhoisUtil.JOINKEYDATA) || keyName.equals("$mul$keyData")){
 							fliedName = "KeyDataID";
 						}else {
 							fliedName = WhoisUtil.HANDLE;
 						}
 						
 						if (keyName.equals(WhoisUtil.JOINENTITESFILED))
 						{
 							entityHandle = results.getString(fliedName);
 						}
 
 						Object value = queryJoinTable(keyFlieds.get(i),
 								results.getString(fliedName), sql, role,
 								connection, format);
 						if (value != null)
 							map.put(key, value);
 					} else {
 						resultsInfo = results.getObject(keyFlieds.get(i)) == null ? "": results.getObject(keyFlieds.get(i));
 						
 						//resultsInfo = results.getObject(keyFlieds.get(i));
 						CharSequence id = "id";
 						if(!keyName.equals(WhoisUtil.JOINPUBLICIDS) && WhoisUtil.getDisplayKeyName(keyFlieds.get(i), format).substring(keyFlieds.get(i).length() - 2).equals(id) && !format.equals("application/html")){
 							continue;
 						}else{
 							map.put(WhoisUtil.getDisplayKeyName(keyFlieds.get(i), format), resultsInfo);//a different format have different name;
 						}
 					}
 				}
 				
 				//v4v6 addresses
 				if (keyName.equals("$mul$nameServer") || keyName.equals("$join$nameServer")){
 					Map<String, Object> map_IP = new LinkedHashMap<String, Object>();
 					Object IPAddressArray = map.get(WhoisUtil.getDisplayKeyName("IPV4_Addresses", format));
 					map_IP.put(WhoisUtil.IPV4PREFIX, IPAddressArray);
 					IPAddressArray = map.get(WhoisUtil.getDisplayKeyName("IPV6_Addresses", format));
 					map_IP.put(WhoisUtil.IPV6PREFIX, IPAddressArray);
 					map.put(WhoisUtil.IPPREFIX, map_IP);
 					map.remove(WhoisUtil.getDisplayKeyName("IPV4_Addresses", format));
 					map.remove(WhoisUtil.getDisplayKeyName("IPV6_Addresses", format));
 				}
 				
 				//asevent
 				if (keyName.equals(WhoisUtil.JOINENTITESFILED)){
 					if (map.containsKey("events")){
 						Map<String, Object> map_Events = new LinkedHashMap<String, Object>();
 						map_Events = (Map<String, Object>)map.get("events");
 						if (map_Events.containsKey("eventActor")){
 							String eventactor = (String)map_Events.get("eventActor");
 							if (entityHandle.equals(eventactor))
 							{
 								map_Events.remove("eventActor");
 								List<Map<String, Object>> listEvents = new ArrayList<Map<String, Object>>();
 								listEvents.add(map_Events);
 								map.put("asEventActor", listEvents.toArray());
 								map.remove("events");
 							}														
 						}
 						
 					}
 				}
 				
 				//vcard format
 				if(keyName.equals(WhoisUtil.JOINENTITESFILED) || keyName.equals(WhoisUtil.MULTIPRXENTITY)){
 					list.add(WhoisUtil.toVCard(map, format));
 				}else{
 					list.add(map);
 				}
 			}
 
 			if (list.size() == 0)
 				return null;
 			
 			Map<String, Object> mapInfo = new LinkedHashMap<String, Object>();
 			
 			// link , remark and notice change to array
 			if(keyName.equals(WhoisUtil.JOINLINKFILED)|| 
 					keyName.equals(WhoisUtil.JOINNANOTICES) ||
 					keyName.equals(WhoisUtil.JOINREMARKS) ||
 					keyName.equals(WhoisUtil.MULTIPRXLINK ) ||
					keyName.equals(WhoisUtil.MULTIPRXNOTICES )||
 					keyName.equals(WhoisUtil.MULTIPRXREMARKS) ||
 					keyName.equals(WhoisUtil.JOINPUBLICIDS) ||
 					keyName.equals(WhoisUtil.JOINDSDATA)||
 					keyName.equals(WhoisUtil.JOINKEYDATA)){
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
 			String role, Connection connection, String format) throws SQLException {
 		if (key.equals(WhoisUtil.JOINENTITESFILED)) {
 			String entitysql = WhoisUtil.SELECT_JOIN_LIST_JOINDNRENTITY;
 			if (sql.indexOf("ip") >= 0 || sql.indexOf("autnum") >= 0
 					|| sql.indexOf("RIRDomain") >= 0) {
 				entitysql = WhoisUtil.SELECT_JOIN_LIST_JOINRIRENTITY;
 				return querySpecificJoinTable(key, handle, entitysql, role,
 						connection, permissionCache.getRIREntityKeyFileds(role), format);
 			}else{
 				return querySpecificJoinTable(key, handle, entitysql, role,
 						connection, permissionCache.getDNREntityKeyFileds(role), format);
 			}
 			
 		} else if (key.equals(WhoisUtil.JOINLINKFILED)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_LINK, role, connection,
 					permissionCache.getLinkKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINPHONFILED)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_PHONE, role, connection,
 					permissionCache.getPhonesKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINPOSTATLADDRESSFILED)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_POSTALADDRESS, role, connection,
 					permissionCache.getPostalAddressKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINVARIANTS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_VARIANTS, role, connection,
 					permissionCache.getVariantsKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINDALEGATIONKEYS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_DELEGATIONKEYS, role,
 					connection, permissionCache.getDelegationKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINNAMESERVER)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_JOINNAMESERVER, role,
 					connection, permissionCache.getNameServerKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINNAREGISTRAR)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_REGISTRAR, role, connection,
 					permissionCache.getRegistrarKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINNANOTICES)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_NOTICES, role, connection,
 					permissionCache.getNoticesKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINEVENTS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_EVENTS, role, connection,
 					permissionCache.getEventsKeyFileds(role), format);
 		} else if (key.equals(WhoisUtil.JOINREMARKS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_REMARKS, role, connection,
 					permissionCache.getRemarksKeyFileds(role), format);
 		}else if (key.equals(WhoisUtil.JOINPUBLICIDS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_PUBLICIDS, role, connection,
 					permissionCache.getPublicIdsKeyFileds(role), format);
 		}else if (key.equals(WhoisUtil.JOINSECUREDNS)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_SECUREDNS, role, connection,
 					permissionCache.getSecureDNSMapKeyFileds(role), format);
 		}else if (key.equals(WhoisUtil.JOINDSDATA)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_DSDATA, role, connection,
 					permissionCache.getDsDataMapKeyFileds(role), format);
 		}else if (key.equals(WhoisUtil.JOINKEYDATA)) {
 			return querySpecificJoinTable(key, handle,
 					WhoisUtil.SELECT_JOIN_LIST_KEYDATA, role, connection,
 					permissionCache.getKeyDataMapKeyFileds(role), format);
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
 			String role, Connection connection, List<String> keyFlieds,
 			String format)
 			throws SQLException {
 
 		Map<String, Object> map = query(connection, sql + "'" + handle + "'",
 				keyFlieds, key, role, format);
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
 
 	public Map<String, Object> getHelp(String helpCode, String role,
 			String format) throws QueryException {
 		Connection connection = null;
 		Map<String, Object> map = null;
 		try {
 			connection = ds.getConnection();
 
 			String selectSql = WhoisUtil.SELECT_HELP + "'" + helpCode + "'";
 			Map<String, Object> helpMap = query(connection, selectSql,
 					permissionCache.getHelpKeyFileds(role),
 					"$mul$notices", role, format);
 			if(helpMap != null){
 				map = rdapConformance(map);
 				map.putAll(helpMap);
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
 		return map;
 	}
 	
 	private Map<String, Object> rdapConformance(Map<String, Object> map){
 		if(map == null){
 			map = new LinkedHashMap<String, Object>();
 		}
 		Object[] conform = new Object[1];
 		conform[0] = WhoisUtil.RDAPCONFORMANCE;
 		map.put(WhoisUtil.RDAPCONFORMANCEKEY, conform);
 		return map;
 	}
 }
