 package cn.edu.zju.plex.tdd.module;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.apache.log4j.Logger;
 
 import cn.edu.zju.plex.tdd.dao.DB4Tdd;
 
 import weibo4j.Timeline;
 import weibo4j.model.Paging;
 import weibo4j.model.Status;
 import weibo4j.model.StatusWapper;
 import weibo4j.model.WeiboException;
 
 /**
  * weibo crawler
  * 
  * @author WangFengwei
  */
 public class WeiboCrawler {
 
 	private final Logger LOG = Logger.getLogger(DB4Tdd.class);
 	private final long INTERVAL = 30000L; // 30 seconds?
 	private String accessToken = "2.00l9nr_DfUKrWDf655d3279arZgVvD";
 	private HashMap<String, String> targetUsers = DB4Tdd.getWeiboTargets();
 
 	public WeiboCrawler(String accessToken, HashMap<String, String> targetUsers) {
 		this.accessToken = accessToken;
 		this.targetUsers = targetUsers;
 	}
 
 	public WeiboCrawler() {
 	};
 
 	public ArrayList<Status> fetchAndStoreUpdate() {
 		ArrayList<Status> res = new ArrayList<Status>();
 
 		for (String wuid : targetUsers.keySet()) {
 			LOG.info("start fetching weibo updates for user:" + wuid);
 			String lastUpdateWeibo = targetUsers.get(wuid);
 			Timeline tm = new Timeline();
 			Paging paging = new Paging();
 			int perNum = 100;
 			paging.setCount(perNum);
 			boolean done = false; // if caught @lastUpdateWeibo
 			String latestWeibo = null;
 			int count = 0;
 			// 最多爬取最近的前1000条微博
 			for (int p = 1; p < 11 && !done; p++) {
 				paging.setPage(p);
 				try {
 					tm.client.setToken(accessToken);
 					try {
 						StatusWapper status = tm.getUserTimelineByUid(wuid,
 								paging, 0, 0);
 						for (Status s : status.getStatuses()) {
 							if (latestWeibo == null) {
 								latestWeibo = s.getId();
 							}
 
 							if (Long.valueOf(s.getId()) <= Long
 									.valueOf(lastUpdateWeibo)) {
 								done = true;
 								break;
 							}
 							count++;
 							DB4Tdd.insertWeibo(s);
 						}
 					} catch (WeiboException e) {
 						e.printStackTrace();
 						LOG.error(e.getMessage());
 					}
 
 					Thread.sleep(INTERVAL);
 
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 					LOG.error(e.getMessage());
 				}
 			}
 			LOG.info("Fetching weibo updates for user:" + wuid
 					+ " over, total:" + count);
 
			if (latestWeibo != null) {
 				lastUpdateWeibo = latestWeibo;
 				DB4Tdd.updateWeiboTargets(wuid, lastUpdateWeibo);
			}
			if(! done)
 				LOG.warn("not succeed in fetching weibo for user:" + wuid);
 
 			try {
 				Thread.sleep(INTERVAL * 4);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return res;
 	}
 
 }
