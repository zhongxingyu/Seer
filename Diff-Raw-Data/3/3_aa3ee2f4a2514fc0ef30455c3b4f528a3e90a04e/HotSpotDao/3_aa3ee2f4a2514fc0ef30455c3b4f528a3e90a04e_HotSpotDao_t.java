 package com.zarcode.data.dao;
 
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import ch.hsr.geohash.GeoHash;
 import ch.hsr.geohash.WGS84Point;
 import ch.hsr.geohash.queries.GeoHashCircleQuery;
 
 import com.zarcode.data.model.HotSpotDO;
 import com.zarcode.platform.dao.BaseDao;
 import com.zarcode.platform.loader.AbstractLoaderDao;
 
 public class HotSpotDao extends BaseDao implements AbstractLoaderDao {
 	
 	private Logger logger = Logger.getLogger(HotSpotDao.class.getName());
 
 	public static final int PAGESIZE = 50;
 
 	/**
 	 * 10 miles (16093.44 meters)
 	 */
 	private static final double DEFAULT_RADIUS = 16093.44;
 	
 	private static final String SEQ_KEY = "SINGLETON";
 	
 	private static final long CURRENT_VER = 0;
 	
 	private int version = 0;
 	
 	public void loadObject(Object dataObject) {
 		addHotSpot((HotSpotDO)dataObject);
 	}
 	
 	public void deleteInstance(HotSpotDO spot) {
 		long rows = 0;
 		pm.deletePersistent(spot);
 	}
 	
 
 	/**
 	 * Adds hotspot to object model.
 	 * 
 	 * @param spot
 	 * @return
 	 */
 	public HotSpotDO addHotSpot(HotSpotDO spot) {
 		HotSpotDO res = null;
 		Long spotId = null;
 		Date now = new Date();
 		if (spot != null) {
 			Long tm = now.getTime();
 			spot.setHotSpotId(null);
 			spot.setCreateDate(new Date());
   	      	pm.makePersistent(spot); 
   	      	res = spot;
   	      	spotId = spot.getHotSpotId();
   	      	logger.info("Added new hotspot --> " + spot);
 		}
         return res; 
 	}
 	
 	/**
 	 * This method updates an existing hotspot.
 	 * 
 	 * @param spot
 	 * @return
 	 */
 	public HotSpotDO updateHotSpot(HotSpotDO spot) {
 		HotSpotDO res = null;
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			res = pm.getObjectById(HotSpotDO.class, spot.getHotSpotId());
 			res.setDesc(spot.getDesc());
 			res.setNotes(spot.getNotes());
 			res.setCategory(spot.getCategory());
 			res.setPublicFlag(spot.getPublicFlag());
 			tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Gets hotspots for requested water resource.
 	 * 
 	 * @param resKey
 	 * @return
 	 */
 	public List<HotSpotDO> getHotSpotsByResKey(String resKey) {
 		int i = 0;
 		List<HotSpotDO> list = null;
 		StringBuilder sb = new StringBuilder();
 		sb.append("(");
 		sb.append("resKey == ");
		sb.append("'");
 		sb.append(resKey);
		sb.append("'");
 		sb.append(" && publicFlag == ");
 		sb.append(true);
 		sb.append(")");
 		Query query = pm.newQuery(HotSpotDO.class, sb.toString());
 		query.setOrdering("category asc, createDate desc");
 		list = (List<HotSpotDO>)query.execute();
 		return list;
 	}
 	
 	/**
 	 * This method retrieves all of the hotspots owned by specific user.
 	 * 
 	 * @param idClear
 	 * @return
 	 */
 	public List<HotSpotDO> getHotSpotsByIdClear(String idClear) {
 		int i = 0;
 		List<HotSpotDO> list = null;
 		StringBuilder sb = new StringBuilder();
 		sb.append("(");
 		sb.append("idClear == '");
 		sb.append(idClear);
 		sb.append("')");
 		Query query = pm.newQuery(HotSpotDO.class, sb.toString());
 		query.setOrdering("category asc, createDate desc");
 		list = (List<HotSpotDO>)query.execute();
 		return list;
 	}
 	
 	/**
 	 * Gets hotspot instance based upon actual hotspot id.
 	 * 
 	 * @param spotId
 	 * @return
 	 */
 	public HotSpotDO getHotSpotById(Long spotId) {
 		HotSpotDO res = null;
 		res = pm.getObjectById(HotSpotDO.class, spotId);
 		return res;
 	}
 	
 	/**
 	 * This method increments the approval rating of this hotspot.
 	 * 
 	 * @param spotId
 	 */
 	public void incrementRating(Long spotId) {
 		HotSpotDO res = null;
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			res = pm.getObjectById(HotSpotDO.class, spotId);
 			int count = res.getRating();
 			count++;
 			res.setRating(count);
 			tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 	}
 
 	/**
 	 * This method will find all of the HotSpots based upon the provided lat-lng.
 	 * 
 	 * @param lat
 	 * @param lng
 	 * @return
 	 */
 	public List<HotSpotDO> getHotSpotsByLatLng(double lat, double lng) {
 		int i = 0;
 		int retryCounter = 0;
 		List<HotSpotDO> res = null;
 		List<GeoHash> geoKeys = null;
 		GeoHashCircleQuery geoQuery = null;
 		double radius = DEFAULT_RADIUS;
 		
 		logger.info("Starting with lat=" + lat + " lng=" + lng + " radius=" + radius);
 		
 		WGS84Point pt = new WGS84Point(lat, lng);
 		
 		geoQuery = new GeoHashCircleQuery(pt, radius);
 		geoKeys = geoQuery.getSearchHashes();
 		
 		while (retryCounter < 2) {
 			if (geoKeys != null && geoKeys.size() > 0) {
 				res = _findClosest(geoKeys);
 				if (res != null && res.size() > 0) {
 					break;
 				}
 				retryCounter++;
 				radius = radius * 2;
 				geoQuery = new GeoHashCircleQuery(pt, radius);
 				geoKeys = geoQuery.getSearchHashes();
 				logger.info("Trying again with radius=" + radius);
 			}
 			else {
 				radius = radius * 2;
 				geoQuery = new GeoHashCircleQuery(pt, radius);
 				geoKeys = geoQuery.getSearchHashes();
 				logger.info("Trying again with radius=" + radius);
 			}
 		}
 		
 		List<HotSpotDO> results = null;
 		if (res != null && res.size() > 0) {
 			results = res;
 		}
 		
 		return results;
 	}
 	
 	private List<HotSpotDO> _findClosest(List<GeoHash> geoKeys) {
 		int i = 0;
 		GeoHash hash = null;
 		List<HotSpotDO> res = null;
 		
 		logger.info("# of geo hash key(s) found: " + geoKeys.size());
 		
 		Transaction tx = pm.currentTransaction();
 		try {
 			// tx.begin();
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
 			Query query = pm.newQuery(HotSpotDO.class, sb.toString());
 			res = (List<HotSpotDO>)query.execute();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		
 		logger.info("_findClosest(): Exit");
 		
 		return res;
 	}
 
 	/*
 	public List<BuzzMsgDO> getEventsByIds(List<Long> keys) {
 		int i = 0; 
 		BuzzMsgDO event = null;
 		List<BuzzMsgDO> list = null;
 		list = (List<BuzzMsgDO>)pm.getObjectsById(keys);
 		return list;
 	}
 	*/
 	
 }
