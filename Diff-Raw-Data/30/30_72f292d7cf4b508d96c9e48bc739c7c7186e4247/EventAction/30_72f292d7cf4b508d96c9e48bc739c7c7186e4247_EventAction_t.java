 package gossip.server.action;
 
 import java.sql.Date;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import edu.bit.dlde.utils.DLDEConfiguration;
 import edu.bit.dlde.utils.DLDELogger;
 import gossip.dao.EventDAO;
 
 /**
  * 访问事件的action。各种事件被缓存在memcached里面。
  * 
  * @author lins
  */
 @Controller
 @RequestMapping("/events")
 public class EventAction {
 	private DLDELogger logger;
 
 	public DLDELogger getLogger() {
 		return logger;
 	}
 
 	public void setLogger(DLDELogger logger) {
 		this.logger = logger;
 	}
 
 	private EventDAO eventDao;
 
 	public EventDAO getEventDao() {
 		return eventDao;
 	}
 
 	public void setEventDao(EventDAO eventDao) {
 		this.eventDao = eventDao;
 	}
 
 	/** 每一页由X个事件组成 **/
 	static final int X = 10;
 	/** memcached里面数据的过期时间 **/
 	static final int expiration = Integer.valueOf(DLDEConfiguration
 			.getInstance("gossip-server.properties").getValue("expiration"));
 
 	/**
 	 * 两种途径获得第N个事件集，每个事件集由X个事件组成。 第一种途径是/events?pageNo={pageNo}&limit={limit}
 	 * 第二种途径是
 	 * /events?year={year}&month={month}&day={day}pageNo={pageNo}&limit={limit}
 	 * 
 	 * @param pageNo
 	 *            page从0开始，没一页含有X个事件。
 	 * @return 返回一个由JSONArray表示的事件集
 	 */
 	@RequestMapping(value = "", method = RequestMethod.POST)
 	@ResponseBody
 	public JSONObject getEventList(
 			@RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
 			@RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
 			@RequestParam(value = "year", required = false, defaultValue = "0") int year,
 			@RequestParam(value = "month", required = false, defaultValue = "1") int month,
 			@RequestParam(value = "day", required = false, defaultValue = "1") int day) {
 		if (year != 0) {
 			return getEventListTimeConstrain(pageNo, limit, year, month, day);
 		} else {
 			return getEventListSimply(pageNo, limit);
 		}
 	}
 
 	private JSONObject getEventListTimeConstrain(int pageNo, int limit,
 			int year, int month, int day) {
 		JSONObject result = new JSONObject();
 
 		JSONArray jsonArry = null, tmp = new JSONArray();
 		@SuppressWarnings("deprecation")
 		Date date = new Date(year - 1900, month - 1, day);
 		/** 先初始化某天的所有events，最多尝试5次，否则返回空 **/
 		jsonArry = eventDao.getEventJSONByDate(date);
 		/** 起始于page*limit，终止于(page + 1) * limit - 1 **/
 		int begin = (pageNo - 1) * limit;
 		for (int i = begin; i < begin + limit && i < jsonArry.size(); i++) {
 			tmp.add(jsonArry.get(i));
 		}
 		result.accumulate("total", jsonArry.size());
 		result.accumulate("pageNo", pageNo);
 		result.accumulate("limit", limit);
 		result.accumulate("events", tmp);
 		logger.info("Succeed to response events in format of JSON...");
 		return result;
 	}
 
 	private JSONObject getEventListSimply(int pageNo, int limit) {
 		JSONArray jsonArray = new JSONArray();
		JSONObject json = null;
		JSONObject eventRanking = eventDao.getEventRanking();
		JSONObject result = new JSONObject();
 
 		/** 起始于page*limit，终止于(page + 1) * limit - 1 **/
 		int begin = (pageNo - 1) * limit;
 		for (int i = begin; i < begin + limit; i++) {
 			Object o = eventRanking.get(i + "");
 			if (o == null)
 				break;
 			json = eventDao.getEventJSONById((Integer) o);
 			if (json != null) {
 				if (!jsonArray.contains(json))
 					jsonArray.add(json);
 			} else {// 不存在则提示没有
 				logger.debug("erro ranking!");
 			}
 		}
 		int total=eventRanking.size();
 		int totalPage=total%10==0?total/10:total/10+1;
 		int pageBegin=pageNo-5>0?pageNo-5:1;
 		int pageEnd;
 		if(totalPage<=11)
 			pageEnd=pageNo;
 		else if(pageNo<=6)
 			pageEnd=10;
 		else{
 			pageEnd=pageNo+4;
 			if(pageEnd>=totalPage){
 				pageBegin=totalPage-limit-1;
 				pageEnd=totalPage-1;
 			}
 
 		}
 		result.accumulate("total", total);
 		result.accumulate("pageBegin", pageBegin);
 		result.accumulate("pageEnd", pageEnd);
 		result.accumulate("pageNo", pageNo);
 		result.accumulate("limit", limit);
 		result.accumulate("events", jsonArray);
 		logger.info("Succeed to response events in format of JSON...");
 
 		return result;
 	}
 
 	final int LOCAL_MAX = 30;
 
 	/**
 	 * 获得event
 	 * 
 	 * @param id
 	 *            对应数据库里面event的id
 	 * @return 返回一个由JSONArray表示的事件集
 	 */
 	@RequestMapping(value = "/{id}", method = RequestMethod.POST)
 	@ResponseBody
 	public JSONObject getEventByDBid(@PathVariable int id) {
 		return eventDao.getEventJSONById(id);
 	}
 
 	/**
 	 * 获得event，底层会调用{@link getEventByDBid()}
 	 * 
 	 * @param rank
 	 *            对应事件的排名
 	 * @return event 事件在数据库中的id
 	 */
 	@RequestMapping(value = "/rank", method = RequestMethod.GET)
 	@ResponseBody
 	public JSONObject getEventByRankNum(
 			@RequestParam(value = "id", required = true) int rank) {
 		JSONObject eventRanking = eventDao.getEventRanking();
 		int id;
 
 		if (!eventRanking.containsKey(rank + "")) {// 不存在则提示没有
 			logger.debug("erro ranking!");
 			return null;
 		}
 		id = (Integer) eventRanking.get(rank + "");
 		
 		return eventDao.getEventJSONById(id);
 	}
 
 	@RequestMapping(value = "/search", method = RequestMethod.GET)
 	@ResponseBody
 	public JSONObject searchEvents(
 			@RequestParam(value = "q", required = true) String query) {
 		return null;
 	}
 
 	// public static void main(String[] args) {
 	// EventAction action = new EventAction();
 	// HashSet<EventModel> eventSet = action.listEvents();
 	// for (EventModel model : eventSet) {
 	// System.out.println("********************************");
 	// Compare.doc_compare(true, model.getPages());
 	// }
 	// System.out.println("" + eventSet.size());
 	// }
 	public static void main(String[] args) {
 		JSONObject json = new JSONObject();
 		json = null;
 		EventAction action = new EventAction();
 		json = action.getEventListSimply(1, 15);
 	}
 
 }
