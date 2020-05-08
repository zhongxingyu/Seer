 package ws.softlabs.lib.kino.dao.server.impl.pmf;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 
 import ws.softlabs.lib.kino.dao.server.intf.ShowDAO;
 import ws.softlabs.lib.kino.dao.server.model.pmf.*;
 import ws.softlabs.lib.kino.dao.server.service.pmf.PMF;
 import ws.softlabs.lib.kino.model.client.*;
 import ws.softlabs.lib.util.client.DateUtils;
 import ws.softlabs.lib.util.client.DayComparator;
 
 public class PMFShowDAOImpl implements ShowDAO {
 
 	private static final Logger log = 
 		Logger.getLogger("kino.pmfdao.impl." + PMFShowDAOImpl.class.getSimpleName());
 
 	public Show get(Long id) {
 		log.error("****** NOT IMPLEMENTED YET ********");
 		return null;
 	}
 	@SuppressWarnings("unchecked")
 	public Show get(Hall hall, Date dateTime) {
 		log.debug("ENTER (hall = " + hall + ", dateTime = " + dateTime + ")");
 		PersistenceManager pm = PMF.getPersistenceManager();
 		PHall phall = PMFDAOUtils.getPHall(hall);
 		if (phall != null) {
 			log.debug("Got PHall from datastore");
 			try {
 				Query query = pm.newQuery(PShow.class);
 				query.setFilter("hallKey == keyParam && timestamp == dateParam");
 				query.declareParameters("com.google.appengine.api.datastore.Key keyParam, " +
 						                "long dateParam");
 				
 				log.debug(query.toString());
 				log.debug("hall key = " + phall.getKey());
 				log.debug("timestamp: " + (dateTime.getTime() / 1000) * 1000);
 				List<PShow> pshows = 
 					(List<PShow>)query.execute(phall.getKey(), (dateTime.getTime() / 1000) * 1000);
 				
 				log.debug("query executed");
 				if(pshows != null && !pshows.isEmpty()) {
 					log.debug("daoResult size: " + pshows.size());
 					Show result = pshows.get(0).asShow(); 
 					log.debug("EXIT returning show: " + result);
 					return result;
 				} else {
 					log.debug("got no results from datastore");
 					log.debug("EXIT (NULL)");
 					return null;					
 				}
 			} catch (Exception e) {
 				log.error("EXIT (EXCEPTION): " + e);
 				return null;
 			} finally {
 				pm.close();
 			}
 		} else {
 			log.debug("EXIT (NULL) [phall = NULL]");
 			return null;
 		}
 	}
 	public List<Show> getList() {
 		log.error("****** NOT IMPLEMENTED YET ********");
 		return null;
 	}
 	@SuppressWarnings("unchecked")
 	public List<Show> getList(Hall hall, Date date) {
 		log.debug("ENTER (hall = " + hall + ", date = " + date + ")");
 		//DateTime dt = new DateTime(date);
 		long tsLow  = date.getTime();//dt.toDateMidnight().getMillis();
 		long tsHigh = (new DateTime(date)).plusDays(1).toDate().getTime();
 		
 		log.debug("timstamp min = " + tsLow);
 		log.debug("timstamp max = " + tsHigh);
 		
 		PersistenceManager pm = PMF.getPersistenceManager();
 		List<PShow> pshows = null;
 		List<Show>  result = null;
 		PHall phall = PMFDAOUtils.getPHall(hall);
 		if (phall != null) {
 			log.debug("Got PHall from datastore");
 			try {
 				Query query = pm.newQuery(PShow.class);
 				query.setFilter("hallKey   == keyParam  && " + 
 								"timestamp > sinceParam && " +
 								"timestamp <= tillParam ");
 				query.declareParameters("com.google.appengine.api.datastore.Key keyParam, " +
 										"long sinceParam, " +
 										"long tillParam");
 				query.setOrdering("timestamp");
 				
 				log.debug("query for shows: " + query.toString());
 				
 				pshows = 
 					(List<PShow>)query.execute(phall.getKey(), tsLow, tsHigh);
 				log.debug("executed query");
 				if(pshows != null && !pshows.isEmpty()) {
 					log.debug("got shows from datastore");
 					result = new ArrayList<Show>();
 					for (PShow ps : pshows) {
 						Show show = ps.asShow();
 						log.debug("adding show (" + show + ")");
 						result.add(show);
 					}
 					log.debug("EXIT returning list (size = " + result.size() + ")");
 					return result;
 				} else {
 					log.debug("got no results from datastore");
 					log.debug("EXIT (NULL)");
 					return null;					
 				}
 			} catch (Exception e) {
 				log.error("EXIT (EXCEPTION): " + e);
 				e.printStackTrace();
 				return null;
 			} finally {
 				pm.close();
 			}
 		} else {
 			log.debug("EXIT (NULL) [phall = NULL]");
 			return null;
 		}
 	}
 	public List<Show> getListSince(Hall hall, Date date) {
 		log.error("****** NOT IMPLEMENTED YET ********");
 		return null;
 	}
 	public boolean add(Show show) {
 		log.debug("ENTER (show = " + show +")");
 		if (show == null) {
 			log.debug("EXIT (false) [show = NULL]");
 			return false;
 		}		
 		PersistenceManager pm = PMF.getPersistenceManager();
 		PShow pshow = new PShow(show);
 		try {
 			pm.makePersistent(pshow);
 			log.debug("EXIT (true)");			
 			return true;
 		} catch (Exception e) {
 			log.error("EXIT (EXCEPTION): " + e);
 			//e.printStackTrace(System.err);
 			//return false;
 			return true;
 		} catch (Throwable t) {
 			log.error("EXIT (THROWABLE): " + t);
 			//t.printStackTrace();
 			//return false;
 			return true;
 		} finally {
 			pm.close();
 		}
 	}
 	public boolean update(Show show) {
 		log.error("****** NOT IMPLEMENTED YET ********");
 		return false;
 	}
 	public boolean remove(Show show) {
 		log.error("****** NOT IMPLEMENTED YET ********");
 		return false;
 	}
 	
 	public List<String> getDaysList(Theater theater, Date date) {
 		return getDaysList(theater, date, true);
 	}
 	@SuppressWarnings("unchecked")
 	private List<String> getDaysList(Theater theater, Date date, boolean close) {
 		log.debug("ENTER");
 		PersistenceManager pm = PMF.getPersistenceManager();
 
 		Set<String> result = new TreeSet<String>();
 		
 		List<PHall> phalls = PMFDAOUtils.getHallList(theater);
 		
 		if (phalls != null) {
 			for(PHall ph : phalls) {
 				try {
 					Query query = pm.newQuery(PShow.class);
 					query.setFilter("hallKey == keyParam && timestamp > sinceParam");
 					query.declareParameters("com.google.appengine.api.datastore.Key keyParam, " +
 											"long sinceParam");
 					query.setOrdering("timestamp");
 					List<PShow> pshows = (List<PShow>)query.execute(ph.getKey(), 
																	date.getTime());
							//DateUtils.dateToMidnight(new Date(System.currentTimeMillis())).getTime());
 					if (pshows != null) {
 						for(PShow ps : pshows)
 							result.add(DateUtils.dateToStringSpecial(ps.getDate()));
 
 						List<String> days = new ArrayList<String>(result); 
 						Collections.sort(days, new DayComparator());
 						log.debug("got " + days.size() + " days");
 						log.debug("EXIT");
 						return days;
 					}
 				} catch (Exception e) {
 					log.error("EXIT (EXCEPTION): " + e);
 					return null;
 				} finally {
 					if (close)
 						pm.close();
 				}
 			}
 		}
 		log.debug("can't get PHalls from DB");
 		log.debug("EXIT (NULL)");
 		return null;
 	}
 	public Long getNextId() {
 		// STUB - not needed for PMF
 		log.error("****** SHOULD NOT BE CALLED ********");
 		return null;
 	}
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<String> getRawList() {
 		PersistenceManager pm = PMF.getPersistenceManager();
 		List<PShow>  pshows = null;
 		List<String> result    = null;
 		try {
 			Query query = pm.newQuery(PShow.class);
 			query.setOrdering("hallKey, timestamp");
 			pshows = (List<PShow>)query.execute();
 			if(pshows != null && !pshows.isEmpty()) {
 				result = new ArrayList<String>();
 				for (PShow pt : pshows)
 					//result.add(pt.asShow().toString());
 					result.add(pt.toString());
 				return result;
 			} else {
 				return null;
 			}
 		} catch (Exception ex) {
 			return null;
 		} finally {
 			pm.close();
 		}
 	}
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<String> getRawDayList() {
 		PersistenceManager pm = PMF.getPersistenceManager();
 		List<PTheater> ptheaters = null;
 		Set<String>       result = new TreeSet<String>();
 		try {
 			Query query = pm.newQuery(PTheater.class);			
 			ptheaters = (List<PTheater>)query.execute();
 			if(ptheaters != null && !ptheaters.isEmpty()) {
 				for(PTheater pt : ptheaters) {
 					List<String> days = getDaysList(pt.asTheater(), new Date(), false);
 					if (days != null && !days.isEmpty())
 						result.addAll(days);
 				}
 				List<String> d = new ArrayList<String>(result); 
 				Collections.sort(d, new DayComparator());
 				return d;				
 			} else {
 				log.debug("EXIT (NULL)");
 				return null;
 			}
 		} catch (Exception ex) {
 			log.debug("EXIT (EXCEPTION) " + ex);
 			return null;
 		} finally {
 			pm.close();
 		}
 	}
 
 }
