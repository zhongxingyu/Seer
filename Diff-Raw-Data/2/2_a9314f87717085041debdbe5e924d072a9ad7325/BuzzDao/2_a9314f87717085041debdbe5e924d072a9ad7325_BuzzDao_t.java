 package com.zarcode.data.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import ch.hsr.geohash.GeoHash;
 import ch.hsr.geohash.WGS84Point;
 import ch.hsr.geohash.queries.GeoHashCircleQuery;
 
 import com.zarcode.data.model.CommentDO;
 import com.zarcode.data.model.EventSequenceDO;
 import com.zarcode.data.model.BuzzMsgDO;
 import com.zarcode.data.model.UserDO;
 import com.zarcode.platform.dao.BaseDao;
 import com.zarcode.platform.loader.AbstractLoaderDao;
 
 public class BuzzDao extends BaseDao implements AbstractLoaderDao {
 	
 	private Logger logger = Logger.getLogger(BuzzDao.class.getName());
 
 	public static final int PAGESIZE = 50;
 
 	/**
 	 * 10 miles (16093.44 meters)
 	 */
 	private static final double DEFAULT_RADIUS = 16093.44;
 	
 	private static final String SEQ_KEY = "SINGLETON";
 	
 	private static final String UNKNOWN = "UNKNOWN";
 	
 	private static final long CURRENT_VER = 0;
 	
 	private int version = 0;
 	
 	private void createSequenceSingleton() {
 		EventSequenceDO seq = null;
 		seq = new EventSequenceDO();
     	seq.setId(SEQ_KEY);
     	seq.setSequenceNum(new Long(0));
     	seq.setSequenceVer(new Long(0));
     	pm.makePersistent(seq);
 	}
 	
 	public void loadObject(Object dataObject) {
 		addMsg((BuzzMsgDO)dataObject);
 	}
 	
 	public long deleteAll(Class cls) {
 		long rows = 0;
 		Query q = pm.newQuery(cls);
 		rows = q.deletePersistentAll();
 		return rows;
 	}
 	
 	public void deleteInstance(BuzzMsgDO m) {
 		long rows = 0;
 		pm.deletePersistent(m);
 	}
 	
 	public void deleteComment(CommentDO comment) {
 		long rows = 0;
 		pm.deletePersistent(comment);
 	}
 	
 	public BuzzMsgDO addMsg(BuzzMsgDO event) {
 		BuzzMsgDO res = null;
 		Long eventId = null;
 		Date now = new Date();
 		if (event != null) {
 			Long tm = now.getTime();
 			event.setMsgId(null);
 			event.setCreateDate(new Date());
 			event.setTimestamp(tm);
 			event.setVersion(this.version);
   	      	pm.makePersistent(event); 
   	      	res = event;
   	      	eventId = event.getMsgId();
   	      	logger.info("Added new event --> " + event);
 		}
         return res; 
 	}
 	
 	public CommentDO addComment(CommentDO comment) {
 		CommentDO res = null;
 		Long commentId = null;
 		Date now = new Date();
 		if (comment != null) {
 			Long tm = now.getTime();
 			comment.setCommentId(null);
 			comment.setCreateDate(new Date());
 			comment.setTimestamp(tm);
   	      	pm.makePersistent(comment); 
   	      	res = comment;
   	      	commentId = comment.getCommentId();
   	      	logger.info("Added new comment --> " + comment);
 		}
         return res; 
 	}
 	
 	private List<BuzzMsgDO> getAllByKeys(List<Long> listOfKeys) {
 		int i = 0;
 		Long key = null;
 		BuzzMsgDO event = null;
 		List<BuzzMsgDO> listOfEvents = null;
 	
 		if (listOfKeys != null) {
 			listOfEvents =  new ArrayList<BuzzMsgDO>();
 			for (i=0; i<listOfKeys.size(); i++) {
 				key = listOfKeys.get(i);
 				logger.info("Get BuzzMsgDO by key=" + key);
 				event = (BuzzMsgDO)pm.getObjectById(BuzzMsgDO.class, key);
 				listOfEvents.add(event);
 			}
 		}
 		return listOfEvents;
 		
 	}
 	
 	/**
 	 * This method returns all the comments related to a buzzMsg.
 	 * 
 	 * @param msgEvent
 	 * @return
 	 */
 	public List<CommentDO> getComments4BuzzMsg(BuzzMsgDO msgEvent) {
 		int i = 0;
 		List<CommentDO> list = null;
 		Date now = new Date();
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("( msgId == ");
 		sb.append(msgEvent.getMsgId());
 		sb.append(")");
 		Query query = pm.newQuery(CommentDO.class, sb.toString());
 		query.setOrdering("timestamp asc");
 		list = (List<CommentDO>)query.execute();
 		if (list != null && list.size() > 0) {
 			int len = list.size();
 			for (i=0; i<len; i++) {
 				CommentDO comm = (list.get(i));
 				comm.postReturn();
 			}
 		}
 			
 		return list;
 	}
 	
 	/**
 	 * This method gets of the comments from a specific water resource and hashes them into
 	 * a list for quick retrieval for returning buzz messages back to the client.
 	 * 
 	 * @param resKey
 	 * @return HashMap with msgId as the key and related comments as the result
 	 */
 	public HashMap<Long, List<CommentDO>> generateCommentTableByResKey(String resKey) {
 		int i = 0;
 		Long buzzMsgId = null;
 		List<CommentDO> list = null;
 		List<CommentDO> workingList = null;
 		Date now = new Date();
 		HashMap<Long, List<CommentDO>> commentTbl = null;
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("( resKey == '");
 		sb.append(resKey);
 		sb.append("')");
 		Query query = pm.newQuery(CommentDO.class, sb.toString());
 		query.setOrdering("timestamp asc");
 		list = (List<CommentDO>)query.execute();
 		if (list != null && list.size() > 0) {
 			commentTbl = new HashMap<Long, List<CommentDO>>();
 			int len = list.size();
 			for (i=0; i<len; i++) {
 				CommentDO comm = list.get(i);
 				comm.postReturn();
 				buzzMsgId = comm.getMsgId();
 				if (commentTbl.containsKey(buzzMsgId)) {
 					workingList = (List<CommentDO>)commentTbl.get(buzzMsgId);
 					if (workingList != null) {
 						workingList.add(comm);
 					}
 				}
 				else {
 					workingList = new ArrayList<CommentDO>();
 					workingList.add(comm);
 					commentTbl.put(buzzMsgId, workingList);
 				}
 			}
 		}
 		return commentTbl;
 	}
 	
 	/**
 	 * This method updates the dynamics data for each buzz message.
 	 * 
 	 * @param listOfEvents
 	 * @return
 	 */
 	private List<BuzzMsgDO> postQuery(List<BuzzMsgDO> listOfEvents, String resKey) {
 		int i = 0;
 		int j = 0;
 		BuzzMsgDO msg = null;
 		List<CommentDO> comments = null;
 	
 		HashMap<Long, List<CommentDO>> commentTbl = generateCommentTableByResKey(resKey);
 		int len = listOfEvents.size();
 		for (i=0; i<len; i++) {
 			msg = (listOfEvents.get(i));
			if (commentTbl != null && commentTbl.containsKey(msg.getMsgId())) {
 				comments = commentTbl.get(msg.getMsgId());
 				msg.postReturn(comments);
 			}
 			else {
 				msg.postReturn(null);
 			}
 		}
 		return listOfEvents;
 	}
 	
 	
 	/*
 	public List<BuzzMsgDO> getNextEventsByResourceId(Long resourceId) {
 		int i = 0;
 		List<Long> listOfKeys = null;
 		List<BuzzMsgDO> listOfEvents = null;
 		CommentDO comment = null;
 		Transaction tx = pm.currentTransaction();
 		Date now = new Date();
 		UserDO user = null;
 		
 		logger.info("Getting messages by resourceId=" + resourceId);
 		
 		try {
 			tx.begin();
 			StringBuilder sb = new StringBuilder();
 			sb.append("(");
 			sb.append("timestamp > ");
 			sb.append(0);
 			sb.append(" && version == ");
 			sb.append(CURRENT_VER);
 			sb.append(" && resourceId == ");
 			sb.append(resourceId);
 			sb.append(")");
 			Query query = pm.newQuery(BuzzMsgDO.class, sb.toString());
 			query.setOrdering("timestamp desc");
 			listOfEvents = (List<BuzzMsgDO>)query.execute();
 			if (listOfEvents != null && listOfEvents.size() > 0) {
 				listOfEvents = postQuery(listOfEvents, resourceId);
 			}
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		return listOfEvents;
 	}
 	*/
 	
 	public List<BuzzMsgDO> getBuzzMsgsByResKey(String resKey) {
 		int i = 0;
 		List<Long> listOfKeys = null;
 		List<BuzzMsgDO> listOfEvents = null;
 		CommentDO comment = null;
 		Transaction tx = pm.currentTransaction();
 		Date now = new Date();
 		UserDO user = null;
 		
 		logger.info("Getting messages by resourceId=" + resKey);
 		
 		try {
 			tx.begin();
 			StringBuilder sb = new StringBuilder();
 			sb.append("(");
 			sb.append("timestamp > ");
 			sb.append(0);
 			sb.append(" && version == ");
 			sb.append(CURRENT_VER);
 			sb.append(" && resKey == '");
 			sb.append(resKey);
 			sb.append("')");
 			Query query = pm.newQuery(BuzzMsgDO.class, sb.toString());
 			query.setOrdering("timestamp desc");
 			listOfEvents = (List<BuzzMsgDO>)query.execute();
 			if (listOfEvents != null && listOfEvents.size() > 0) {
 				listOfEvents = postQuery(listOfEvents, resKey);
 			}
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		return listOfEvents;
 	}
 	
 	/*
 	public List<BuzzMsgDO> getPrevEventsByResourceId(Long resourceId, Long lastSeq) {
 		int i = 0;
 		List<Long> listOfKeys = null;
 		List<BuzzMsgDO> listOfEvents = null;
 		Transaction tx = pm.currentTransaction();
 		Date now = new Date();
 		try {
 			tx.begin();
 			StringBuilder sb = new StringBuilder();
 			sb.append("(");
 			sb.append("sequenceNum < ");
 			sb.append(lastSeq);
 			sb.append(" && sequenceVer == ");
 			sb.append(CURRENT_VER);
 			sb.append(" && resourceId == ");
 			sb.append(resourceId);
 			sb.append(")");
 			Query query = pm.newQuery(BuzzMsgDO.class, sb.toString());
 			query.setRange(0, PAGESIZE);
 			query.setOrdering("sequenceNum");
 			listOfEvents = (List<BuzzMsgDO>)query.execute();
 			if (listOfEvents != null && listOfEvents.size() > 0) {
 				listOfEvents = postQuery(listOfEvents, resourceId);
 			}
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		return listOfEvents;
 	}
 	*/
 	
 	public List<BuzzMsgDO> getAllMsgs() {
 		int i = 0;
 		List<Long> listOfKeys = null;
 		List<BuzzMsgDO> listOfEvents = null;
 		Query query = pm.newQuery(BuzzMsgDO.class);
 		query.setOrdering("timestamp asc");
 		listOfEvents = (List<BuzzMsgDO>)query.execute();
 		return listOfEvents;
 	}
 	
 	public List<BuzzMsgDO> getNextEvents(Long lastSeq, Long seqVer) {
 		int i = 0;
 		List<Long> listOfKeys = null;
 		List<BuzzMsgDO> listOfEvents = null;
 		Transaction tx = pm.currentTransaction();
 		Long limit = lastSeq + PAGESIZE;
 		try {
 			tx.begin();
 			StringBuilder sb = new StringBuilder();
 			sb.append("(");
 			sb.append("sequenceNum > ");
 			sb.append(lastSeq);
 			sb.append(" && sequenceNum < ");
 			sb.append(limit);
 			sb.append(" && sequenceVer == ");
 			sb.append(seqVer);
 			sb.append(")");
 			Query query = pm.newQuery(BuzzMsgDO.class, sb.toString());
 			query.setOrdering("sequenceNum");
 			listOfEvents = (List<BuzzMsgDO>)query.execute();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		return listOfEvents;
 	}
 	
 	public List<BuzzMsgDO> getPrevEvents(Long firstSeq, Long seqVer) {
 		List<Long> listOfKeys = null;
 		List<BuzzMsgDO> listOfEvents = null;
 		Transaction tx = pm.currentTransaction();
 		Long start = firstSeq - PAGESIZE;
 		
 		if (start < 1) {
 			logger.warning("getPrevEvents(): start sequence is less than 1");
 			return listOfEvents;
 		}
 		try {
 			tx.begin();
 			//
 			// only get keys of objects
 			//
 			StringBuilder sb = new StringBuilder();
 			sb.append("select eventId from ");
 			sb.append(BuzzMsgDO.class.getName());
 			sb.append("where sequenceNum > ");
 			sb.append(start);
 			sb.append(" && sequenceNum < ");
 			sb.append(firstSeq);
 			sb.append(" && sequenceVer == ");
 			sb.append(seqVer);
 			Query query = pm.newQuery(sb.toString());
 			query.setOrdering("sequenceNum");
 			listOfKeys = (List<Long>)query.execute();
 			//
 			// now get all of the objects for these keys
 			//
 			listOfEvents = (List<BuzzMsgDO>)pm.getObjectsById(listOfKeys);
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		return listOfEvents;
 	}
 	
 	public BuzzMsgDO getMsgById(Long msgId) {
 		BuzzMsgDO res = null;
 		res = pm.getObjectById(BuzzMsgDO.class, msgId);
 		List<CommentDO> comments = getComments4BuzzMsg(res);
 		res.postReturn(comments);
 		return res;
 	}
 	
 	/*
 	public void incrementCommentCounter(Long eventId) {
 		BuzzMsgDO res = null;
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			res = pm.getObjectById(BuzzMsgDO.class, eventId);
 			int count = res.getCommentCounter();
 			count++;
 			res.setCommentCounter(count);
 			tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 	}
 	*/
 
 	/**
 	 * This method finds the resources in your area and returns a list of buzz messages regardless
 	 * if the user is inside of the polygon or not.
 	 */
 	public List<BuzzMsgDO> findClosest(double lat, double lng, long last, boolean next) {
 		int i = 0;
 		int retryCounter = 0;
 		List<BuzzMsgDO> res = null;
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
 		
 		List<BuzzMsgDO> results = null;
 		
 		if (res != null && res.size() > 0) {
 			if (last > 0) {
 				results = new ArrayList<BuzzMsgDO>();
 				BuzzMsgDO e = null;
 				int leng = res.size();
 				for (i=0; i<leng; i++) {
 					e = res.get(i);
 					if (next) {
 						if (e.getTimestamp() > last) {
 							results.add(e);
 						}
 					}
 					else {
 						if (e.getTimestamp() < last) {
 							results.add(e);
 						}
 					}
 				}
 			}
 			else {
 				results = res;
 			}
 		}
 		
 		return results;
 	}
 	
 	private List<BuzzMsgDO> _findClosest(List<GeoHash> geoKeys) {
 		int i = 0;
 		GeoHash hash = null;
 		List<BuzzMsgDO> res = null;
 		
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
 			Query query = pm.newQuery(BuzzMsgDO.class, sb.toString());
 			query.setOrdering("sequenceNum");
 			res = (List<BuzzMsgDO>)query.execute();
 			// tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		
 		logger.info("_findClosest(): Exit");
 		
 		return res;
 	}
 	
 	public List<BuzzMsgDO> getMsgsByIds(List<Long> keys) {
 		int i = 0; 
 		List<BuzzMsgDO> list = null;
 		list = (List<BuzzMsgDO>)pm.getObjectsById(keys);
 		return list;
 	}
 	
 }
