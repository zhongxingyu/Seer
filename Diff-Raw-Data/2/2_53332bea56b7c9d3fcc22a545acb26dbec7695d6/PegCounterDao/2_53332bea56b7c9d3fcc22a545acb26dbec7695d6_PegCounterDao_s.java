 package com.zarcode.data.dao;
 
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 
 import com.zarcode.data.model.BucketDO;
 import com.zarcode.data.model.PegCounterDO;
 import com.zarcode.platform.dao.BaseDao;
 
 public class PegCounterDao extends BaseDao {
 	
 	private Logger logger = Logger.getLogger(PegCounterDao.class.getName());
 	
 	public static String NO_OF_ACTIVE_USERS = "NO_OF_ACTIVE_USERS";
 	public static String NO_BUZZ_MSG = "NO_BUZZ_MSG_";
 	public static String NO_BUZZ_COMMENTS = "NO_BUZZ_COMMENTS_";
 	public static String NO_HOT_SPOTS = "NO_HOTSPOTS_";
 
 	public PegCounterDO getPegCounter(String name) {
 		List<PegCounterDO> res = null;
 		PegCounterDO target = null;
 		StringBuilder sb = new StringBuilder();
 		Query query = pm.newQuery(PegCounterDO.class);
 		query.setFilter("name == nameParam");
		query.declareParameters("String name");
 		res = (List<PegCounterDO>)query.execute(name);
 		if (res != null && res.size() > 0) {
 			target = res.get(0);
 		}
 		return target;
 	}
 	
 	public PegCounterDO addPegCounter(PegCounterDO peg) {
 		PegCounterDO newPeg = null;
 		Transaction tx = pm.currentTransaction();
 		double lat = 0;
 		double lng = 0;
 		
 		try {
 			tx.begin();
 			Date now = new Date();
 			peg.setPegCounterId(null);
 			peg.setLastUpdate(new Date());
 			newPeg = pm.makePersistent(peg); 
 			//
 			// commit changes
 			//
 			tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 		return newPeg;
 	}
 	
 	public void increment(String pegName, long val) {
 		PegCounterDO res = null;
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			res = getPegCounter(pegName);
 			if (res == null) {
 				PegCounterDO p = new PegCounterDO();
 				p.setPegName(pegName);
 				res = addPegCounter(p);
 			}
 			Long count = res.getCounter();
 			count += val;
 			res.setLastUpdate(new Date());
 			res.setCounter(count);
 			tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 	}
 	
 	public void update(String pegName, long val) {
 		PegCounterDO res = null;
 		Transaction tx = pm.currentTransaction();
 		try {
 			tx.begin();
 			res = getPegCounter(pegName);
 			if (res == null) {
 				PegCounterDO p = new PegCounterDO();
 				p.setPegName(pegName);
 				res = addPegCounter(p);
 			}
 			res.setLastUpdate(new Date());
 			res.setCounter(val);
 			tx.commit();
 		}
 		finally {
 			if (tx.isActive()) {
 				tx.rollback();
 			}
 		}
 	}
 	
 }
