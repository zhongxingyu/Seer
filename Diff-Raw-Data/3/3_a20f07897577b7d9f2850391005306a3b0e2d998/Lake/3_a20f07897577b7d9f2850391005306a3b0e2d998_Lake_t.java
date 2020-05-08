 package com.zarcode.data.resources.v1;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.UriInfo;
 
 import ch.hsr.geohash.GeoHash;
 import ch.hsr.geohash.WGS84Point;
 
 import com.google.gdata.data.maps.FeatureEntry;
 import com.zarcode.common.GeoUtil;
 import com.zarcode.data.dao.BuzzDao;
 import com.zarcode.data.dao.PegCounterDao;
 import com.zarcode.data.dao.UserDao;
 import com.zarcode.data.dao.WaterResourceDao;
 import com.zarcode.data.gdata.MapClient;
 import com.zarcode.data.maint.PegCounter;
 import com.zarcode.data.model.BuzzMsgDO;
 import com.zarcode.data.model.UserDO;
 import com.zarcode.data.model.WaterResourceDO;
 import com.zarcode.data.resources.ResourceBase;
 
 @Path("/v1/lakes")
 public class Lake extends ResourceBase {
 	
 	private Logger logger = Logger.getLogger(Lake.class.getName());
 	
 	private static final int MAX_RESOURCES_RETURNED = 20;
 	
 	@Context 
 	UriInfo uriInfo = null;
     
 	@Context 
     Request request = null;
 	
 	String container = null;
 	
 	/*
 	@GET 
 	@Path("/dumpMap")
 	@Produces("text/plain")
 	public String queryMap() {
 		int i = 0;
 		int radius = 30;
 		StringBuilder sb = null;
 		List<EventDO> list = null;
 		BuzzDao eventDao = null;
 		List<FeatureEntry> features = null;
 		MapClient mapClient = null;
 		int counter = 0;
 		
 		logger.info("Entered");
 		
 		try {
 			//
 			// get keys from Google Map 
 			//
 			mapClient = new MapClient();
 			mapClient.initialize();
 			features = mapClient.getAllFeatures();
 		
 			//
 			// get event based upon keys from map
 			//
 			if (features != null && features.size() > 0) {
 				logger.info("Found feature(s) -- >" + features.size());
 				sb = new StringBuilder();
 				FeatureEntry entry = null;
 				for (i=0; i<features.size(); i++) {
 					entry = features.get(i);
 					sb.append(entry.getTitle().getPlainText());
 					sb.append("\n");
 					counter++;
 				}
 			}
 			else {
 				logger.warning("Unable to find any event in the radius");
 			}
 			sb.append("\n");
 			sb.append("TOTAL: " + counter);
 		}
 		catch (Exception e) {
 			logger.severe(Util.getStackTrace(e));
 		}
 		return (sb == null ? "<EMPTY RESULTS>" : sb.toString());
 	}
 	*/
 	
 	@GET 
 	@Path("/showGeoHash")
 	@Produces("text/plain")
 	public String showGeoHash(@QueryParam("lat") double lat, @QueryParam("lng") double lng, @QueryParam("radius") int radius) {
 		int i = 0;
 		StringBuilder sb = null;
 		List<BuzzMsgDO> list = null;
 		BuzzDao eventDao = null;
 		List<FeatureEntry> features = null;
 		MapClient mapClient = null;
 		int counter = 0;
 		logger.info("lat=" + lat + " lng=" + lng + " radius=" + radius);
 		GeoHash hash = GeoHash.withCharacterPrecision(lat, lng, 12);
 		return hash.toBase32();
 	}
 
 	/*
 	public String findClosest(@QueryParam("lat") double lat, @QueryParam("lng") double lng) {
 		int i = 0;
 		StringBuilder sb = null;
 		List<EventDO> list = null;
 		BuzzDao eventDao = null;
 		List<FeatureEntry> features = null;
 		MapClient mapClient = null;
 		int counter = 0;
 		GeoHash hash = null;
 		
 		logger.info("lat=" + lat + " lng=" + lng);
 		WGS84Point pt = new WGS84Point(lat, lng);
 		GeoHashCircleQuery query = new GeoHashCircleQuery(pt, 30);
 		List<GeoHash> res = query.getSearchHashes();
 		
 		if (res != null && res.size() > 0) {
 			for (i=0; i<res.size(); i++) {
 				hash = res.get(0);
 				if (sb == null) {
 					sb = new StringBuilder();
 				}
 				sb.append((i+1));
 				sb.append(") ");
 				sb.append(hash.toBase32());
 				sb.append("\n");
 			}
 		}
 		return (sb == null ? "<EMPTY RESULTS>" : sb.toString());
 	}
 	*/
 	
 	@GET 
 	@Path("/closest")
 	@Produces("application/json")
 	public List<WaterResourceDO> findClosest(@QueryParam("lat") double lat, @QueryParam("lng") double lng) {
 		int i = 0;
 		double dist = 0;
 		StringBuilder sb = null;
 		WaterResourceDao waterResDao = null;
 		List<WaterResourceDO> emptySet = new ArrayList<WaterResourceDO>();
 		WGS84Point ctr = null;
 		GeoHash hash = null;
 		Date start = new Date();
 		
 		logger.info("lat=" + lat + " lng=" + lng);
 		WGS84Point pt = new WGS84Point(lat, lng);
 		waterResDao = new WaterResourceDao();
 		List<WaterResourceDO> results = waterResDao.findClosest(lat, lng, 0.5, 3);
 		
 		if (results.size() > 0) {
 			WaterResourceDO lake = null;
 			for (i=0; i<results.size(); i++) {
 				lake = results.get(i);
 				lake.postReturn();
 				ctr = GeoUtil.getPolygonCentroid(lake.getPolygon());
 				dist = GeoUtil.distanceBtwAB(lat, lng, ctr.getLat(), ctr.getLng());
 				lake.setDistanceAway(dist);
 			}
 			Collections.sort(results);
 		}
		if (results != null && results.size() > MAX_RESOURCES_RETURNED) {
			results = results.subList(0, MAX_RESOURCES_RETURNED);
		}
 		Date end = new Date();
 		logger.info("Duration: " + (end.getTime() - start.getTime()) + " msec(s)");
 	
 		return (results == null ? emptySet : results);
 	}
 	
 	
 	@GET 
 	@Path("/search/{keyword}")
 	@Produces("application/json")
 	public List<WaterResourceDO> searchLakes(@PathParam("keyword") String keyword, @QueryParam("lat") double lat, @QueryParam("lng") double lng) {
 		int i = 0;
 		double dist = 0;
 		StringBuilder sb = null;
 		WaterResourceDao waterResDao = null;
 		List<WaterResourceDO> emptySet = new ArrayList<WaterResourceDO>();
 		WGS84Point ctr = null;
 		List<WaterResourceDO> results = null;
 		
 		Date start = new Date();
 		logger.info("keyword=" + keyword);
 		if (keyword != null && keyword.length() > 0) {
 			waterResDao = new WaterResourceDao();
 			results = waterResDao.search(keyword);
 			logger.info("# of matches: " + (results == null ? 0 : results.size()));
 			if (results.size() > MAX_RESOURCES_RETURNED) {
 				results = results.subList(0, MAX_RESOURCES_RETURNED);
 			}
 			if (results.size() > 0) {
 				WaterResourceDO lake = null;
 				List<UserDO> users = null;
 				for (i=0; i<results.size(); i++) {
 					lake = results.get(i);
 					lake.postReturn();
 					ctr = GeoUtil.getPolygonCentroid(lake.getPolygon());
 					dist = GeoUtil.distanceBtwAB(lat, lng, ctr.getLat(), ctr.getLng());
 					lake.setDistanceAway(dist);
 				}
 				Collections.sort(results);
 			}
 		}
 		Date end = new Date();
 		logger.info("Duration: " + (end.getTime() - start.getTime()) + " msec(s)");
 	
 		return (results == null ? emptySet : results);
 	}
 	
 	/*
 	@GET 
 	@Path("/selectMap")
 	@Produces("text/plain")
 	public String selectMap(@QueryParam("lat") double lat, @QueryParam("lng") double lng, @QueryParam("radius") int radius) {
 		int i = 0;
 		StringBuilder sb = null;
 		List<EventDO> list = null;
 		BuzzDao eventDao = null;
 		List<FeatureEntry> features = null;
 		MapClient mapClient = null;
 		int counter = 0;
 		
 		logger.info("Incoming search -- lat=" + lat + " lng=" + lng + " radius=" + radius);
 		
 		try {
 			//
 			// get keys from Google Map 
 			//
 			mapClient = new MapClient();
 			mapClient.initialize();
 			features = mapClient.findClosest(lat, lng, radius);
 		
 			//
 			// get event based upon keys from map
 			//
 			if (features != null && features.size() > 0) {
 				logger.info("Found feature(s) -- >" + features.size());
 				sb = new StringBuilder();
 				FeatureEntry entry = null;
 				for (i=0; i<features.size(); i++) {
 					entry = features.get(i);
 					sb.append(entry.getTitle().getPlainText());
 					sb.append("\n");
 					counter++;
 				}
 			}
 			else {
 				logger.warning("Unable to find any event in the radius");
 			}
 			sb.append("\n");
 			sb.append("TOTAL: " + counter);
 		}
 		catch (Exception e) {
 			logger.severe(Util.getStackTrace(e));
 		}
 		return (sb == null ? "<EMPTY RESULTS>" : sb.toString());
 	}
 	*/
 }
