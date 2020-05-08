 package com.zarcode.data.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import ch.hsr.geohash.GeoHash;
 import ch.hsr.geohash.WGS84Point;
 import ch.hsr.geohash.queries.GeoHashCircleQuery;
 
 import com.google.appengine.api.datastore.DatastoreNeedIndexException;
 import com.google.appengine.api.datastore.DatastoreTimeoutException;
 import com.zarcode.common.GeoUtil;
 import com.zarcode.data.model.GeoHash2ResourceMapDO;
 import com.zarcode.data.model.WaterResourceDO;
 import com.zarcode.platform.dao.BaseDao;
 import com.zarcode.utils.SearchJanitorUtils;
 
 public class WaterResourceDao extends BaseDao {
 	
 	private Logger logger = Logger.getLogger(WaterResourceDao.class.getName());
 	
     public static final int MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH = 5;
     
     public static final int MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX = 200;
 	
 	/**
 	 * 10 miles (16093.44 meters)
 	 */
 	private static final double DEFAULT_RADIUS = 16093.44;
 	
 	public void insertResource(WaterResourceDO d) {
 		int i = 0;
 		WaterResourceDO res = null;
 		if (d != null) {
 			d.postCreation();
   	      	pm.makePersistent(d); 
         	res = d;
         	
         	//////////////////////////////////////////////////////////////////////
         	//
         	// for every point in the polygon of the water resource, add a search 
         	// entry
         	//
         	//////////////////////////////////////////////////////////////////////
         	
         	WGS84Point pt = null;
         	GeoHash2ResourceMapDO pair = null;
         	List<WGS84Point> list = d.getPolygon();
         	if (list != null && list.size() > 0) {
         		for (i=0; i<list.size(); i++) {
         			pt = list.get(i);
         			pair = new GeoHash2ResourceMapDO();
         			pair.setRegion(d.getRegion());
         			pair.setMap(d.getMap());
         			pair.setResourceId(d.getResourceId());
         			GeoHash geoHashKey = GeoHash.withCharacterPrecision(pt.getLat(), pt.getLng(), 12);
         			pair.setGeoHashKey(geoHashKey.toBase32());
         			pm.makePersistent(pair);
         		}
         	}
         	else {
         		logger.warning("No polygon points to update for searching ...");
         	}
 		}
 	}
 	
     public List<WaterResourceDO> search(String queryString) {
 	    int parameterCounter = 0;
 	    List<WaterResourceDO> result = null;
 	   
 	    if (queryString != null) {
 	    	
 		    queryString = queryString.trim();
 		    logger.info("queryString=" + queryString);
 		    
 		    StringBuffer queryBuffer = new StringBuffer();
 		    queryBuffer.append("SELECT FROM " + WaterResourceDO.class.getName() + " WHERE ");
 		    Set<String> queryTokens = SearchJanitorUtils.getTokensForIndexingOrQuery(queryString, MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH);
 		    List<String> parametersForSearch = new ArrayList<String>(queryTokens);
 		    StringBuffer declareParametersBuffer = new StringBuffer();
 		    
 		    while (parameterCounter < queryTokens.size()) {
 		    	queryBuffer.append("fts == param" + parameterCounter);
 		        declareParametersBuffer.append("String param" + parameterCounter);
 		        if (parameterCounter + 1 < queryTokens.size()) {
 		        	queryBuffer.append(" && ");
 		            declareParametersBuffer.append(", ");
 		        }
 		        parameterCounter++;
 		    }
 		
 		    logger.info("QUERY: " + queryBuffer.toString());
 		    Query query = pm.newQuery(queryBuffer.toString());
 		    query.declareParameters(declareParametersBuffer.toString());
 		    
 		    try {
 		    	result = (List<WaterResourceDO>) query.executeWithArray(parametersForSearch.toArray());
 		    } 
 		    catch (DatastoreTimeoutException e) {
 		    	logger.severe(e.getMessage());
 		    	logger.severe("datastore timeout at: " + queryString);// + " - timestamp: " + discreteTimestamp);
 		    } 
 		    catch(DatastoreNeedIndexException e) {
 		    	logger.severe(e.getMessage());
 		    	logger.severe("datastore need index exception at: " + queryString);// + " - timestamp: " + discreteTimestamp);
 		    }
 	    }
 	    return result;
 	
 	} // search
     
     public void updateFTS(WaterResourceDO res) {
     	StringBuffer sb = new StringBuffer();
     	sb.append(res.getContent());
     	Set<String> new_ftsTokens = SearchJanitorUtils.getTokensForIndexingOrQuery(sb.toString(), MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX);
     	Set<String> ftsTokens = res.getFts();
     	ftsTokens.clear();
 
     	for (String token : new_ftsTokens) {
     		ftsTokens.add(token);
         }               
     }
 
 	
 	public void addObject(Object dataObject) {
 		WaterResourceDO res = (WaterResourceDO)dataObject;
 		insertResource(res);
 	}
 	
 	public void updateLastUpdate(String resKey) {
 		WaterResourceDO res = null;
		List<WaterResourceDO> list = null;
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
			Query query = pm.newQuery(WaterResourceDO.class);
		    query.setFilter("resKey == resKeyParam");
		    query.declareParameters("String resKeyParam");
		    list = (List<WaterResourceDO>)query.execute(resKey);
		    if (list != null && list.size() > 0) {
		    	res = list.get(0);
		    }
		    if (res != null) {
		    	res = pm.getObjectById(WaterResourceDO.class, res.getResourceId());
		    	res.setLastUpdate(new Date());
		    }
 			tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 	}
 	
 	public void deleteAll() {
   	   	pm.deletePersistentAll(WaterResourceDO.class);
 	}
 	
 	public long deleteByRegion(String region) {
 		long rows = 0;
 		Query query = pm.newQuery(WaterResourceDO.class);
 	    query.setFilter("region == regionParam");
 	    query.declareParameters("String regionParam");
 	    rows = query.deletePersistentAll(region);
 	    if (rows > 0) {
 	    	logger.info("Successfully deleted " + rows + " WaterResourceDO object(s) for region=" + region);
 	    }
 	    else {
 	    	logger.warning("Unable to delete WaterResourceDO object(s) for region=" + region);
 	    }
 	    query = pm.newQuery(GeoHash2ResourceMapDO.class);
 	    query.setFilter("region == regionParam");
 	    query.declareParameters("String regionParam");
 	    rows = query.deletePersistentAll(region);
 	    if (rows > 0) {
 	    	logger.info("Successfully deleted " + rows + " GeoHash2ResourceMapDO object(s) for region=" + region);
 	    }
 	    else {
 	    	logger.warning("Unable to delete GeoHash2ResourceMapDO object(s) for region=" + region);
 	    }
 	    return rows;
 	}
 
 	public WaterResourceDO getResourceByName(String name) {
 		WaterResourceDO res = null;
 		List<WaterResourceDO> list = null;
 		Query query = pm.newQuery(WaterResourceDO.class, "name == nameParam");
 		query.declareParameters("String nameParam");
 		list = (List<WaterResourceDO>)query.execute(name);
 		if (list != null && list.size() > 0) {
 			res = list.get(0);
 		}
 		return res;
 	}
 	
 	public WaterResourceDO getResourceByRegion(String region) {
 		WaterResourceDO res = null;
 		List<WaterResourceDO> list = null;
 		Query query = pm.newQuery(WaterResourceDO.class, "region == regionParam");
 		query.declareParameters("String regionParam");
 		list = (List<WaterResourceDO>)query.execute(region);
 		if (list != null && list.size() > 0) {
 			res = list.get(0);
 		}
 		return res;
 	}
 	
 	public List<WaterResourceDO> searchByKeyword(String keyword) {
 		WaterResourceDO res = null;
 		List<WaterResourceDO> list = null;
 		
 		keyword = (keyword != null ? keyword.toLowerCase() : "").trim();
 		
 		Query query = pm.newQuery(WaterResourceDO.class);
 		query.setFilter("name >= :1 && name < :2");
 		list = (List<WaterResourceDO>)query.execute(keyword, (keyword + "\ufffd"));
 		if (list == null || list.size() == 0) {
 			logger.warning("Search returned nothing!!!!");
 		}
 		return list;
 	}
 	
 	public WaterResourceDO findBestResource(double lat, double lng) {
 		int i =0;
 		WaterResourceDO best = null;
 		WaterResourceDO r = null;
 		List<WGS84Point> polygon = null;
 		
 		WGS84Point pt = new WGS84Point(lat, lng);
 		
 		List<WaterResourceDO> res = findClosest(lat, lng, 1);
 		if (res != null && res.size() > 0) {
 			for (i=0; i<res.size(); i++) {
 				r = res.get(i);
 				polygon = r.getPolygon();
 				if (GeoUtil.containsPoint(polygon, pt)) {
 					if (best == null) {
 						best = r;
 					}
 					else {
 						// get smaller polygon
 						if (r.getApproxSize() < best.getApproxSize()) {
 							best = r;
 						}
 					}
 				}
 			}
 		}
 		return best;
 		
 	} // findBestResource
 	
 	public List<WaterResourceDO> findClosest(double lat, double lng, int retryLimit) {
 		int i = 0;
 		int retryCounter = 0;
 		boolean foundOneOrMore = true;
 		List<GeoHash2ResourceMapDO> res0 = null;
 		List<WaterResourceDO> res1 = null;
 		List<GeoHash> geoKeys = null;
 		GeoHashCircleQuery geoQuery = null;
 		double radius = DEFAULT_RADIUS/2;
 		
 		logger.info("Starting with lat=" + lat + " lng=" + lng + " radius=" + radius);
 		
 		WGS84Point pt = new WGS84Point(lat, lng);
 		
 		geoQuery = new GeoHashCircleQuery(pt, radius);
 		geoKeys = geoQuery.getSearchHashes();
 		
 		while (retryCounter < retryLimit) {
 			if (geoKeys != null && geoKeys.size() > 0) {
 				res0 = _findClosest(geoKeys);
 				if (res0 != null && res0.size() > 0) {
 					foundOneOrMore = true;
 					break;
 				}
 				retryCounter++;
 				radius = radius * 2;
 				geoQuery = new GeoHashCircleQuery(pt, radius);
 				geoKeys = geoQuery.getSearchHashes();
 				logger.info("Trying again with radius=" + radius);
 			}
 			else {
 				geoQuery = new GeoHashCircleQuery(pt, radius);
 				geoKeys = geoQuery.getSearchHashes();
 				logger.info("Trying again with radius=" + radius);
 			}
 		}
 		
 		if (foundOneOrMore) {
 			HashMap<Long, Integer> dupMap = new HashMap<Long, Integer>();
 			List<Long> keys = new ArrayList<Long>();
 			for (i=0; i<res0.size(); i++) {
 				Long resourceId = res0.get(i).getResourceId(); 
 				if (!dupMap.containsKey(resourceId)) {
 					keys.add(resourceId);
 					dupMap.put(resourceId, 1);
 				}
 			}
 			if (keys != null && keys.size() > 0) {
 				logger.info("Querying with " + keys.size() + " key(s)");
 				Query q = pm.newQuery("select from " + WaterResourceDO.class.getName() + " where :keys.contains(resourceId)");
 				res1 = (List<WaterResourceDO>) q.execute(keys);
 				logger.info("Returning matches -- " + (res1 == null ? 0 : res1.size()));
 			}
 			else {
 				logger.warning("Cannot execute query -- keyset is EMPYT!!");
 			}
 		}
 		return res1;
 		
 	} // findClosest
 	
 	/**
 	 * This method is using geo-hashing to find polygons closest to my location.
 	 * 
 	 * @param geoKeys
 	 * @return
 	 */
 	private List<GeoHash2ResourceMapDO> _findClosest(List<GeoHash> geoKeys) {
 		int i = 0;
 		GeoHash hash = null;
 		List<GeoHash2ResourceMapDO> res = null;
 		
 		logger.info("# of geo hash key(s) found: " + geoKeys.size());
 		
 		//
 		// only get keys of objects
 		//
 		StringBuilder sb = new StringBuilder();
 		sb.append("(");
 		int keyCount = geoKeys.size();
 		String geoHashKeyStr = null;
 		for (i=0; i<keyCount; i++) {
 			hash = geoKeys.get(i);
 			geoHashKeyStr = hash.toBase32();
 			logger.info( i + ") geoHashKeyStr: " + geoHashKeyStr);
 			if (geoHashKeyStr.length() == 6) {
 				sb.append("geoHashKey6 == ");
 				sb.append("'");
 				sb.append(geoHashKeyStr);
 				sb.append("'");
 			}
 			else if (geoHashKeyStr.length() == 5) {
 				sb.append("geoHashKey4 == ");
 				sb.append("'");
 				sb.append(geoHashKeyStr.substring(0, 4));
 				sb.append("'");
 			}
 			else if (geoHashKeyStr.length() == 4) {
 				sb.append("geoHashKey4 == ");
 				sb.append("'");
 				sb.append(geoHashKeyStr);
 				sb.append("'");
 			}
 			else if (geoHashKeyStr.length() == 3) {
 				sb.append("geoHashKey2 == ");
 				sb.append("'");
 				sb.append(geoHashKeyStr.substring(0, 2));
 				sb.append("'");
 			}
 			else if (geoHashKeyStr.length() == 2) {
 				sb.append("geoHashKey2 == ");
 				sb.append("'");
 				sb.append(geoHashKeyStr);
 				sb.append("'");
 			}
 			if ((i+1) < keyCount) {
 				sb.append(" || ");
 			}
 		}
 		sb.append(")");
 		logger.info("Query string: " + sb.toString());
 		Query query = pm.newQuery(GeoHash2ResourceMapDO.class, sb.toString());
 		res = (List<GeoHash2ResourceMapDO>)query.execute();
 		
 		logger.info("_findClosest(): Exit");
 		
 		return res;
 	}
 	
 
 	
 }
