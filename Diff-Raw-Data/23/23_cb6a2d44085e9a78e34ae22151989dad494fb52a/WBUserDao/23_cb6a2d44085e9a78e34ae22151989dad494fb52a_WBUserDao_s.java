 /**
  * 
  */
 package com.k99k.khunter.dao;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.k99k.khunter.DaoInterface;
 import com.k99k.khunter.DaoManager;
 import com.k99k.khunter.DataSourceInterface;
 import com.k99k.khunter.KObjColumn;
 import com.k99k.khunter.KObjConfig;
 import com.k99k.khunter.KObjManager;
 import com.k99k.khunter.KObjSchema;
 import com.k99k.khunter.KObject;
 import com.k99k.khunter.MongoDao;
 import com.k99k.tools.StringUtil;
 import com.k99k.wb.acts.WBUser;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 /**
  * WB用户Dao,附加其他相关的操作实现
  * @author keel
  *
  */
 public class WBUserDao extends MongoDao {
 
 	/**
 	 * @param daoName
 	 * @param dataSource
 	 */
 	public WBUserDao(String daoName, DataSourceInterface dataSource) {
 		super(daoName, dataSource);
 	}
 	
 	private static DaoInterface wbTagsDao;
 	private static DaoInterface wbMentionDao;
 	private static DaoInterface wbMsgDao;
 	private static DaoInterface wbFansDao;
 	private static DaoInterface wbInboxDao;
 	private static DaoInterface wbSentDao;
 	private static DaoInterface wbUserDao;
 	private static DaoInterface wbCommDao;
 	//private static DaoInterface wbDMsgDao;
 	private static DaoInterface wbDMsgTBDao;
 	private static DaoInterface wbFavDao;
 	
 	private static KObjConfig wbTagConfig;
 	private static KObjConfig wbMentionConfig;
 	private static KObjConfig wbMsgConfig;
 	private static KObjConfig wbCommConfig;
 	//private static KObjConfig wbFansConfig;
 	private static KObjConfig wbInboxConfig;
 	private static KObjConfig wbSentConfig;
 	//private static KObjConfig wbDMsgConfig;
 	private static KObjConfig wbDMsgTBConfig;
 	//private static KObjConfig wbFanConfig;
 	private static KObjConfig wbFavConfig;
 	//private static KObjConfig wbFollowConfig;
 	
 	private static BasicDBObject noSubProp;
 	private static BasicDBObject wbcommProp;
 	
 	static final Logger log = Logger.getLogger(WBUserDao.class);
 	
 	public static final KObject newMsg(){
 		return wbMsgConfig.getKobjSchema().createEmptyKObj(wbMsgDao);
 	}
 	
 	public static final KObject newComm(){
 		return wbCommConfig.getKobjSchema().createEmptyKObj(wbCommDao);
 	}
 	
 	/**
 	 * 注意:这里的初始化操作在WBTalkAct的init方法中实现
 	 */
 	public static final void initKCs(){
 		wbTagConfig = KObjManager.findKObjConfig("wbtags");
 		wbMentionConfig = KObjManager.findKObjConfig("wb_mention");
 		wbMsgConfig = KObjManager.findKObjConfig("wbmsg");
 		wbCommConfig = KObjManager.findKObjConfig("wbcomm");
 		//wbFansConfig = KObjManager.findKObjConfig("wb_fans");
 		wbInboxConfig = KObjManager.findKObjConfig("wb_inbox");
 		wbSentConfig = KObjManager.findKObjConfig("wb_sent");
 		//wbDMsgConfig = KObjManager.findKObjConfig("wb_dmsg");
 		//wbFanConfig = KObjManager.findKObjConfig("wb_fans");
 		wbFavConfig = KObjManager.findKObjConfig("wb_favourite");
 		wbDMsgTBConfig =  KObjManager.findKObjConfig("wbmsg_d");
 		
 		wbTagsDao = DaoManager.findDao("wbTagsDao");
 		wbMentionDao = DaoManager.findDao("wbMentionDao");
 		wbMsgDao = DaoManager.findDao("wbMsgDao");
 		wbCommDao = DaoManager.findDao("wbCommDao");
 		wbFansDao = DaoManager.findDao("wbFansDao");
 		wbFavDao = DaoManager.findDao("wbFavouriteDao");
 		wbUserDao = DaoManager.findDao("wbUserDao");
 		wbSentDao = DaoManager.findDao("wbSentDao");
 		wbInboxDao = DaoManager.findDao("wbInboxDao");
 		//wbDMsgDao = DaoManager.findDao("wbAllDMsgDao");
 		wbDMsgTBDao = DaoManager.findDao("wbDMsgDao");
 		
 		Object[] kcs = new Object[]{
 				wbTagConfig,wbMentionConfig,wbMsgConfig,wbInboxConfig,wbSentConfig,
 				wbTagsDao,wbMentionDao,wbMsgDao,wbFansDao,wbUserDao,wbSentDao,wbInboxDao
 		};
 		for (int i = 0; i < kcs.length; i++) {
 			if (kcs[i] == null) {
 				log.error("[WBUserDao initKCs failed]:"+i);
 			}
 		}
 		//初始化不含子对象的属性，以便实现findOne重写
 		noSubProp = createFieldsByScheme("wbuser",false);
 		if (noSubProp.size() <= 0) {
 			log.error("[WBUserDao initKCs noSubProp error]");
 		}
 		wbcommProp =  createFieldsByScheme("wbcomm",true);
 		if (wbcommProp.size() <= 0) {
 			log.error("[WBUserDao initKCs wbcommProp error]");
 		}
 		log.info("WBUserDao initKCs done. noSubProp size:"+noSubProp.size()+" ; wbcommProp size:"+wbcommProp.size());
 	}
 	
 	/**
 	 * 根据KObjSchema产生包含或不包含子属性的属性Fields
 	 * @param schema KObjSchema的name
 	 * @param hasSub 是否包含子属性
 	 * @return BasicDBObject
 	 */
 	private static final BasicDBObject createFieldsByScheme(String schema,boolean hasSub){
 		KObjSchema sch = KObjManager.findSchema(schema);
 		BasicDBObject fields = new BasicDBObject();
 		KObject kobj  = new KObject();
 		for (Iterator<String> it = kobj.getPropMap().keySet().iterator(); it.hasNext();) {
 			String k = it.next();
 			fields.append(k, 1);
 		}
 		if (hasSub) {
 			for (Iterator<KObjColumn> iterator = sch.getColList().iterator(); iterator.hasNext();) {
 				KObjColumn kc = iterator.next();
 				fields.append(kc.getCol(), 1);
 			}
 			return fields;
 		}else{
 			for (Iterator<KObjColumn> iterator = sch.getColListWithNoSub().iterator(); iterator.hasNext();) {
 				KObjColumn kc = iterator.next();
 				fields.append(kc.getCol(), 1);
 			}
 			return fields;
 		}
 		
 	}
 	
 	
 	/**
 	 * 取出一个用户,注意这里不包含内嵌文档数据
 	 * @see com.k99k.khunter.MongoDao#findOne(long)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public KObject findOne(long id) {
 		try {
 			DBCollection coll = this.getColl();
 			DBObject o = coll.findOne(id,noSubProp);
 			if (o != null) {
 				return new KObject((Map<String, Object>)o);
 			}
 			return null;
 		} catch (Exception e) {
 			log.error("findOne by Id error!", e);
 			return null;
 		}
 	}
 
 	/**
 	 * 取出一个用户,注意这里不包含内嵌文档数据
 	 * @see com.k99k.khunter.MongoDao#findOne(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public KObject findOne(String name) {
 		try {
 			DBCollection coll = this.getColl();
 			DBObject o = coll.findOne(new BasicDBObject("name", name),noSubProp);
 			if (o != null) {
 				return new KObject((Map<String, Object>)o);
 			}
 			return null;
 		} catch (Exception e) {
 			log.error("findOne by name error!", e);
 			return null;
 		}
 	}
 	
 	/**
 	 * 保存User的属性,不包含有子属性的属性
 	 * @param userId
 	 * @param props
 	 * @return
 	 */
 	public boolean saveProp(long userId,Map<String,Object> props){
 		try {
 			DBCollection coll = this.getColl();
 			coll.update(new BasicDBObject("_id",userId),new BasicDBObject("$set",props),false,false);
 		} catch (Exception e) {
 			log.error("save error!", e); 
 			return false;
 		}
 		return true;
 	}
 	
 	//以下属性是用于查询的静态条件,节省开销
 	private static final BasicDBObject prop_id = new BasicDBObject("_id",1);
 	private static final BasicDBObject prop_msg_rt_id = new BasicDBObject("msg_id",1).append("rt_id", 1);
 	private static final BasicDBObject prop_fans_id = new BasicDBObject("fans_id",1);
 	private static final BasicDBObject prop_sum = new BasicDBObject("sum",1);
 	private static final BasicDBObject prop_notify_msg = new BasicDBObject("notify_msg",1).append("inbox_count", 1);
 	private static final BasicDBObject prop_id_desc = new BasicDBObject("_id",-1);
 	private static final BasicDBObject prop_follows_id = new BasicDBObject("follows.id",1);//.append("follows.both", 1);
 	private static final BasicDBObject prop_notify_msg_reset = new BasicDBObject("$set",new BasicDBObject("notify_msg",0));
 	private static final BasicDBObject prop_notify_dmsg_reset = new BasicDBObject("$set",new BasicDBObject("notify_dmsg",0));
 	private static final BasicDBObject prop_notify_mention_reset = new BasicDBObject("$set",new BasicDBObject("notify_mention",0));
 	private static final BasicDBObject prop_followers_count_inc = new BasicDBObject("friends_count",1);
 	private static final BasicDBObject prop_fans_count_inc = new BasicDBObject("followers_count",1).append("notify_fan", 1);
 	private static final BasicDBObject prop_followers_count_dec = new BasicDBObject("friends_count",-1);
 	private static final BasicDBObject prop_fans_count_dec = new BasicDBObject("followers_count",-1);
 	private static final BasicDBObject prop_statuses_count = new BasicDBObject("statuses_count",1);
 	private static final BasicDBObject prop_notify_mention_inc = new BasicDBObject("$inc",new BasicDBObject("notify_mention",1).append("mention_count",1));
 	private static final BasicDBObject prop_del_my_msg = new BasicDBObject("$inc",new BasicDBObject("statuses_count",-1).append("inbox_count", -1));
 	private static final BasicDBObject prop_dmsg_inc = new BasicDBObject("$inc", new BasicDBObject("notify_dmsg",1).append("dmsg_count", 1));
 	private static final BasicDBObject prop_rt_comm_inc = new BasicDBObject("$inc", new BasicDBObject("rt_comm_count",1));
 	private static final BasicDBObject prop_del_dmsg = new BasicDBObject("$inc",new BasicDBObject("dmsg_count",-1));
 	private static final BasicDBObject prop_follows_both = new BasicDBObject("follows.$.both",1);
 	private static final BasicDBObject prop_fans_both =new BasicDBObject("$set", new BasicDBObject("fans.$.both",1));
 	private static final BasicDBObject prop_fans_both_clear =new BasicDBObject("$set", new BasicDBObject("fans.$.both",0));
 	private static final BasicDBObject prop_rt_comm_count =new BasicDBObject("rt_comm_count",1);
 	private static final BasicDBObject prop_user_new =new BasicDBObject("notify_msg",1).append("notify_fan", 1).append("notify_dmsg", 1).append("notify_mention", 1);
 	
 	public static final int countMsgComms(long msgId){
 		BasicDBObject q = new BasicDBObject("_id",msgId);
 		Map<String,Object> re = wbMsgDao.findOneMap(q, prop_rt_comm_count);
 		if (re == null) {
 			return 0;
 		}
 		return Integer.parseInt(re.get("rt_comm_count").toString());
 	}
 	
 	public static final String hasNew(String userName){
 		BasicDBObject q = new BasicDBObject("name",userName);
 		Map<String,Object> re = wbUserDao.findOneMap(q, prop_user_new);
 		if (re == null) {
 			return "";
 		}
 		StringBuilder sb = new StringBuilder("[");
 		sb.append(re.get("notify_msg"));
 		sb.append(",");
 		sb.append(re.get("notify_fan"));
 		sb.append(",");
 		sb.append(re.get("notify_dmsg"));
 		sb.append(",");
 		sb.append(re.get("notify_mention"));
 		sb.append("]");
 		return sb.toString();
 	}
 	
 	
 	/**
 	 * 添加评论的同步操作,同时在原消息的转发数加一
 	 * @param newComm 带ID的新评论
 	 * @param rt_id 原消息ID
 	 * @param re_userId 原消息用户ID
 	 * @param userId 发表者ID
 	 * @param msg 评论内容
 	 * @param source 来源
 	 * @param place 发布地点
 	 * @param topics
 	 * @param mentions
 	 * @return
 	 */
 	public static final boolean addComm(KObject newComm,long rt_id,long re_userId,long userId,String msg,String source,String place,ArrayList<String> topics,ArrayList<String> mentions,boolean isRT){
 		//添加一个comm
 		newComm.setProp("text", msg);
 		newComm.setProp("source", source);
 		newComm.setProp("place", place);
 		newComm.setProp("user_id", userId);
 		newComm.setProp("re_msg_id", rt_id);
 		newComm.setProp("re_user_id", re_userId);
 		
 		if (topics != null) {
 			newComm.setProp("tags", topics);
 		}
 		if (mentions != null) {
 			newComm.setProp("mentions", mentions);
 		}
 		if (isRT) {
 			newComm.setProp("isRT", isRT);
 		}
 //		if (urls != null) {
 //			newComm.setProp("urls", urls);
 //		}
 		boolean re = wbCommDao.save(newComm);
 		incMsgRTorComm(rt_id);
 		return re;
 	}
 	
 	public static final void incMsgRTorComm(long rt_id){
 		BasicDBObject q = new BasicDBObject("_id",rt_id);
 		wbMsgDao.updateOne(q, prop_rt_comm_inc);
 	}
 	
 	public static final boolean addDMsg(long userId,long targetId,String talkMsg,String userName){
 		
 		KObject newMsg = wbDMsgTBConfig.getKobjSchema().createEmptyKObj(wbDMsgTBDao);
 		newMsg.setProp("text", talkMsg);
 		newMsg.setProp("creatorId", userId);
 		newMsg.setProp("to_id", targetId);
 		newMsg.setProp("creatorName", userName);
 		wbDMsgTBDao.save(newMsg);
 		
 		BasicDBObject q = new BasicDBObject("_id",userId);
 		return wbUserDao.updateOne(q, prop_dmsg_inc);
 	}
 	
 	/**
 	 * 获取某消息的评论和转发
 	 * @param msgId
 	 * @param page
 	 * @param pageSize
 	 * @return ArrayList<KObject>
 	 */
 	public static final ArrayList<KObject> readComms(long msgId,int page,int pageSize){
 		
 		KObject msg = wbMsgDao.findOne(msgId);
 		int msgCount = Integer.parseInt(msg.getProp("rt_comm_count").toString());
 		int skip = pagedSkip(page,msgCount,pageSize);
 		if (skip < 0) {
 			return null;
 		}
 		
 		ArrayList<KObject> list = new ArrayList<KObject>(pageSize);
 		List<Map<String,Object>> msgIds = wbCommDao.query(new BasicDBObject("re_msg_id",msgId).append("state", 0), wbcommProp,prop_id_desc, skip, pageSize, null);
 		for (Iterator<Map<String, Object>> it = msgIds.iterator(); it.hasNext();) {
 			Map<String, Object> m = it.next();
 			KObject user = WBUser.findWBUser((Long)m.get("user_id"));
 			m.put("user_name", user.getName());
 			m.put("user_screen", user.getProp("screen_name"));
 			list.add(new KObject(m));
 		}
 		return list;
 	}
 	
 	/**
 	 * 由页码,总数,每页项目数计算skip数
 	 * @param page
 	 * @param sum
 	 * @param pageSize
 	 * @return
 	 */
 	public static int pagedSkip(int page,int sum,int pageSize){
 		if (sum<=0 || pageSize<=0) {
 			return -1;
 		}
 		int pageCount = sum/pageSize;
 		int mod = sum%pageSize;
 		if (mod != 0) {
 			pageCount++;
 		}
 		if (page<=0) {
 			page = 1;
 		}
 		if (page > pageCount) {
 			page = pageCount;
 		}
 		int skip = pageSize*(page-1);
 		
 		return skip;
 	}
 	
 	public static int[] pagedSkipForSlice(int page,int sum,int pageSize){
 		if (sum<=0 || pageSize<=0) {
 			return null;
 		}
 		int[] ii = new int[2];
 		int pageCount = sum/pageSize;
 		int mod = sum%pageSize;
 		if (mod != 0) {
 			pageCount++;
 		}
 		
 		if (page<=0) {
 			page = 1;
 		}
 		if (page >= pageCount) {
 			//最后一页
 			ii[0] = 0-sum;
 			ii[1] = (mod==0)?pageSize:mod;
 			return ii;
 		}
 		
 		ii[0] = 0-pageSize*page;
 		ii[1] = pageSize;
 		return ii;
 	}
 	
 	/**
 	 * 获取某一个msg及其一页的comm/rt列表
 	 * @param msgId
 	 * @param page
 	 * @param pageSize
 	 * @return
 	 */
 	public static final ArrayList<KObject> readOneMsgList(long msgId,int page,int pageSize){
 		ArrayList<KObject> list = new ArrayList<KObject>(pageSize);
 		KObject org_msg = wbMsgDao.findOne(msgId);
 		list.add(org_msg);
 		ArrayList<KObject> sub = WBUserDao.readComms(msgId, page, pageSize);
 		if (sub != null &&  !sub.isEmpty()) {
 			list.addAll(sub);
 		}
 		return list;
 	}
 	
 	
 	
 	@SuppressWarnings("unchecked")
 	public static final ArrayList<Map<String, Object>> getFans(long userId,int page,int pageSize){
 		KObject user = wbUserDao.findOne(userId);
 		int msgCount = Integer.parseInt(user.getProp("followers_count").toString());
 		
 		int[] slice = pagedSkipForSlice(page,msgCount,pageSize);
 		ArrayList<Map<String, Object>> userIdList = new ArrayList<Map<String, Object>>(pageSize);
 		BasicDBObject fields = prop_id;
 		fields.append("fans", new BasicDBObject("$slice",slice));
 		List<Map<String,Object>> msgIds = wbUserDao.query(new BasicDBObject("_id",userId),fields,null, 0, 1, null);
 		if (!msgIds.isEmpty()) {
 			Map<String, Object> u = msgIds.get(0);
 			List<Map<String,Object>> msgIdList = (List<Map<String, Object>>) u.get("fans");
 			for (Iterator<Map<String, Object>> it = msgIdList.iterator(); it.hasNext();) {
 				Map<String, Object> m = it.next();
 				userIdList.add(m);
 			}
 		}
 		return userIdList;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static final ArrayList<Map<String, Object>> getFollows(long userId,int page,int pageSize){
 		KObject user = wbUserDao.findOne(userId);
 		int msgCount = Integer.parseInt(user.getProp("friends_count").toString());
 		int[] slice = pagedSkipForSlice(page,msgCount,pageSize);
 		ArrayList<Map<String, Object>> userIdList = new ArrayList<Map<String, Object>>(pageSize);
 		BasicDBObject fields = prop_id;
 		fields.append("follows", new BasicDBObject("$slice",slice));
 		List<Map<String,Object>> msgIds = wbUserDao.query(new BasicDBObject("_id",userId),fields,null, 0, 1, null);
 		if (!msgIds.isEmpty()) {
 			Map<String, Object> u = msgIds.get(0);
 			List<Map<String,Object>> msgIdList = (List<Map<String, Object>>) u.get("follows");
 			for (Iterator<Map<String, Object>> it = msgIdList.iterator(); it.hasNext();) {
 				Map<String, Object> m = it.next();
 				userIdList.add(m);
 			}
 		}
 		
 		return userIdList;
 	}
 	
 	
 	
 	/**
 	 * 获取某个话题的消息列表
 	 * @param msgId
 	 * @param page
 	 * @param pageSize
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static final ArrayList<KObject> readOneTopicList(String topic,int page,int pageSize){
 		Map<String,Object> t = wbTagsDao.findOneMap(new BasicDBObject("name",topic),prop_sum);
 		if (t == null || t.isEmpty()) {
 			return null;
 		}
 		int msgCount = Integer.parseInt(t.get("sum").toString());
 		int[] slice = pagedSkipForSlice(page,msgCount,pageSize);
 		
 		ArrayList<Long> msgIdList = new ArrayList<Long>(pageSize);
 		BasicDBObject fields = prop_id;
 		fields.append("tag_ids", new BasicDBObject("$slice",slice));
 		List<Map<String,Object>> msgIds = wbTagsDao.query(new BasicDBObject("name",topic), fields,null, 0, 1, null);
 		if (!msgIds.isEmpty()) {
 			Map<String,Object> mm = msgIds.get(0);
 			ArrayList<Long> ll = (ArrayList<Long>) mm.get("tag_ids");
 			msgIdList = ll;
 		}
 		
 		ArrayList<KObject> list = getMsgList(msgIdList,pageSize,prop_id_desc);	
 		return list;
 	}
 	
 	/**
 	 * 获取收藏消息列表
 	 * @param userId
 	 * @param page
 	 * @param pageSize
 	 * @return ArrayList<KObject>
 	 */
 	public static final ArrayList<KObject> readFavList(long userId,int page,int pageSize){
 		return readPagedList(wbFavDao,"favourites_count",userId,page,pageSize);
 	}
 	
 	
 	private static final BasicDBObject prop_favourites_count_inc =new BasicDBObject("$inc",new BasicDBObject("favourites_count",1));
 	private static final BasicDBObject prop_favourites_count_dec =new BasicDBObject("$inc",new BasicDBObject("favourites_count",-1));
 	private static final BasicDBObject prop_state_del_set =new BasicDBObject("$set",new BasicDBObject("state",-1));
 	
 	/**
 	 * 收藏一条消息
 	 * @param msgId
 	 * @param userId
 	 * @return
 	 */
 	public static final boolean addFavor(long msgId,long userId){
 		KObject fav = wbFavConfig.getKobjSchema().createEmptyKObj(wbFavDao);
 		fav.setProp("msg_id", msgId);
 		fav.setProp("user_id", userId);
 		boolean re = wbFavDao.save(fav);
 		wbUserDao.updateOne(new BasicDBObject("_id",userId),prop_favourites_count_inc);
 		return re;
 	}
 	
 	/**
 	 * 取消一条消息的收藏
 	 * @param favId wbFavourite 中的_id
 	 * @param userId
 	 * @return 删除失败或未找到删除对象返回false
 	 */
 	public static final boolean delFavor(long favId,long userId){
 		boolean re = wbFavDao.deleteOne(favId) != null;
 		if (re) {
 			wbUserDao.updateOne(new BasicDBObject("_id",userId), prop_favourites_count_dec);
 		}
 		return re;
 	}
 	
 	/**
 	 * 话题排行
 	 * @param userId
 	 * @param page
 	 * @param pageSize
 	 * @return
 	 */
 	public static final ArrayList<KObject> getTopicTopList(long userId,int page,int pageSize){
 		
 		
 		return null;
 	}
 	
 	
 	public static final ArrayList<KObject> readMentionList(long userId,int page,int pageSize){
 		KObject user = wbUserDao.findOne(userId);
 		int msgCount = Integer.parseInt(user.getProp("mention_count").toString());
 		int skip = pagedSkip(page,msgCount,pageSize);
 		if (skip < 0) {
 			return null;
 		}
 		
 		ArrayList<KObject> list = getMsgListWithRT(wbMentionDao,new BasicDBObject("user_name",user.getName()).append("state", 0),skip,pageSize);
 		wbUserDao.updateOne(new BasicDBObject("_id",userId), prop_notify_mention_reset);
 		return list;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static final ArrayList<KObject> readDMsg(long userId,int page,int pageSize){
 		KObject user = wbUserDao.findOne(userId);
 		int msgCount = Integer.parseInt(user.getProp("dmsg_count").toString());
 		int skip = pagedSkip(page,msgCount,pageSize);
 		if (skip < 0) {
 			return null;
 		}
 		ArrayList<KObject> list = new ArrayList<KObject>();
 		DBCollection coll = wbDMsgTBDao.getColl();
 		BasicDBObject q = new BasicDBObject("state", 0);
 		BasicDBList or = new BasicDBList();
 		or.add(new BasicDBObject("to_id",userId));
 		or.add(new BasicDBObject("creatorId",userId));
 		q.append("$or", or);
 		DBCursor cur = coll.find(q).sort(prop_id_desc).skip(skip).limit(pageSize);
 		while (cur.hasNext()) {
 			Map<String,Object> m = (Map<String,Object>) cur.next();
 			list.add(new KObject(m));
 		}
 		wbUserDao.updateOne(new BasicDBObject("_id",userId), prop_notify_dmsg_reset);
 		return list;
 	}
 	
 	/**
 	 * 删除私信
 	 * @param userId
 	 * @param msgId
 	 * @return 删除失败或未找到删除对象返回false
 	 */
 	public static final boolean delDMsg(long userId,long msgId){
 		if (wbDMsgTBDao.deleteOne(msgId) == null) {
 			return false;
 		}
 		wbUserDao.updateOne(new BasicDBObject("_id",userId), prop_del_dmsg);
 		return true;
 	}
 	
 	/**
 	 * 删除消息,将state置为-1，仅影响wbmsg表 
 	 * @param msgId
 	 * @return 删除失败或未找到删除对象返回false
 	 */
 	public static final boolean delMsg(long msgId){
 		return wbMsgDao.deleteOne(msgId) != null;
 	}
 	
 	/**
 	 * 删除一个用户自己发的消息(inbox和sent均减少),
 	 * TODO 未验证用户是否已发此消息 
 	 * @param userId
 	 * @param msgId
 	 * @return 删除失败或未找到删除对象返回false
 	 */
 	public static final boolean delSentMsg(long userId,long msgId){
 		BasicDBObject q = new BasicDBObject("user_id",userId).append("msg_id",msgId);
 		boolean re = (wbSentDao.deleteOne(q) !=null);
 		if (re) {
 			wbInboxDao.deleteOne(q);
 			wbMsgDao.deleteOne(msgId);
 			wbUserDao.updateOne(new BasicDBObject("_id",userId), prop_del_my_msg);
 		}
 		return re;
 	}
 	
 	public static final boolean isFollow(long userId,long targetId){
 		BasicDBObject q = new BasicDBObject("_id",userId).append("follows.id",targetId);
 		Object o = wbUserDao.findOneMap(q, prop_id);
 		return o!=null;
 	}
 	
 	public static final boolean isFan(long userId,long targetId){
 		BasicDBObject q = new BasicDBObject("_id",userId).append("fans.id",targetId);
 		Object o = wbUserDao.findOneMap(q, prop_id);
 		return o!=null;
 	}
 	
 	public static final boolean isBothFollow(long userId,long targetId){
 		BasicDBObject q = new BasicDBObject("_id",userId).append("follows.id",targetId).append("follows.both",1);
 		Object o = wbUserDao.findOneMap(q, prop_id);
 		return o!=null;
 	}
 	
 	/**
 	 * follow操作,原用户$addToSet一个follows,friends_count++,目标用户$addToSet一个fans,followers_count++;<br />
 	 * 注：follows和fans存储String：对方有关注自己则follows为"targetId,1",否则为"targetId,0"
 	 * @param userId 用户
 	 * @param targetId 需要follow的对象
 	 * @return 是否成功
 	 */
 	public static final boolean follow(long userId,long targetId){
 		if (userId == targetId) {
 			return false;
 		}
 		KObject user = wbUserDao.findOne(userId);
 		KObject target = wbUserDao.findOne(targetId);
 		if (user == null || target== null) {
 			return false;
 		}
 		try {
 			List<Map<String,Object>> myFollow = wbUserDao.query(new BasicDBObject("_id", userId).append("follows.id",targetId),prop_follows_id, null, 0, 0, null);
 			if (myFollow != null && myFollow.size()>0) {
 				return true;
 			}
 			
 			int isTargetFans = 0;
 			List<Map<String,Object>> targetFollow = wbUserDao.query(new BasicDBObject("_id",targetId).append("follows.id", userId),prop_follows_id, null, 0, 0, null);
 			if (targetFollow !=null && targetFollow.size() > 0) {
 				isTargetFans = 1;
 			}
 			
 			//更新原用户
 			BasicDBObject update =  new BasicDBObject("$addToSet",new BasicDBObject("follows",new BasicDBObject("id",targetId).append("both",isTargetFans).append("name",target.getName()).append("screen", target.getProp("screen_name"))));
 			update.put("$inc", prop_followers_count_inc);
 			wbUserDao.updateOne(new BasicDBObject("_id",userId),update);
 			//更新目标用户
 			BasicDBObject update2 =  new BasicDBObject("$addToSet",new BasicDBObject("fans",new BasicDBObject("id",userId).append("both",isTargetFans).append("name",user.getName()).append("screen", user.getProp("screen_name"))));
 			update2.put("$inc", prop_fans_count_inc);
 			if (isTargetFans == 1) {
 				update2.put("$set", prop_follows_both);
 				wbUserDao.updateOne(new BasicDBObject("_id",targetId).append("follows.id", userId),update2);
 				wbUserDao.updateOne(new BasicDBObject("_id",userId).append("fans.id", targetId),prop_fans_both);
 			}else{
 				wbUserDao.updateOne(new BasicDBObject("_id",targetId),update2);
 			}
 		} catch (Exception e) {
 			log.error("follow error!", e);
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * unFollow操作,原用户$pull一个follows,friends_count--,目标用户$pull一个fans,followers_count--;<br />
 	 * 注：follows和fans存储String：对方有关注自己则follows为"targetId,1",否则为"targetId,0"
 	 * @param userId 用户
 	 * @param targetId 需要follow的对象
 	 * @return 是否成功
 	 */
 	public static final boolean unFollow(long userId,long targetId){
 		if (userId == targetId) {
 			return false;
 		}
 		KObject user = wbUserDao.findOne(userId);
 		KObject target = wbUserDao.findOne(targetId);
 		if (user == null || target== null) {
 			return false;
 		}
 		try {
 			List<Map<String,Object>> myFollow = wbUserDao.query(new BasicDBObject("_id", userId).append("follows.id",targetId),prop_follows_id, null, 0, 0, null);
 			if (myFollow == null || myFollow.size()<=0) {
 				return true;
 			}
 			
 			int isTargetFans = 0;
 			List<Map<String,Object>> targetFollow = wbUserDao.query(new BasicDBObject("_id",targetId).append("follows.id", userId),prop_follows_id, null, 0, 0, null);
 			if (targetFollow !=null && targetFollow.size() > 0) {
 				isTargetFans = 1;
 			}
 			
 			//更新原用户
 			BasicDBObject update =  new BasicDBObject("$pull",new BasicDBObject("follows",new BasicDBObject("id",targetId)));
 			update.put("$inc", prop_followers_count_dec);
 			wbUserDao.updateOne(new BasicDBObject("_id",userId),update);
 			//更新目标用户
 			BasicDBObject update2 =  new BasicDBObject("$pull",new BasicDBObject("fans",new BasicDBObject("id",userId)));
 			update2.put("$inc", prop_fans_count_dec);
 			if (isTargetFans == 1) {
 				update2.put("$set", new BasicDBObject("follows.$.both",0));
 				wbUserDao.updateOne(new BasicDBObject("_id",targetId).append("follows.id", userId),update2);
 				wbUserDao.updateOne(new BasicDBObject("_id",userId).append("fans.id", targetId), prop_fans_both_clear);
 			}else{
 				wbUserDao.updateOne(new BasicDBObject("_id",targetId),update2);
 			}
 			
 		} catch (Exception e) {
 			log.error("follow error!", e);
 			return false;
 		}
 		return true;
 	}
 	
 	public static final ArrayList<KObject> readSentMsgs(long userId,int page,int pageSize){
 		return readPagedList(wbSentDao,"statuses_count",userId,page,pageSize);
 		
 	}
 	
 	/**
 	 * 读取未读的几条msg
 	 * @param userId
 	 * @param max 一次载入的最大数量
 	 * @return 若无则返回null,有则返回指定数量的ArrayList<KObject> 
 	 */
 	public static final ArrayList<KObject> readUnReadMsgs(long userId,int max){
 		KObject user = wbUserDao.findOne(userId);
 		int newMsg = Integer.parseInt(user.getProp("notify_msg").toString());
 		if (newMsg<=0) {
 			return null;
 		}
 		int num = (newMsg>max) ? max: newMsg;
 		return getInboxMsgList(userId,0,num);
 	}
 	
 	/**
 	 * 按页读取某用户的inbox msg
 	 * @param userId
 	 * @param page
 	 * @param pageSize
 	 * @return 若无则返回null,有则返回指定数量的ArrayList<KObject>
 	 */
 	public static final ArrayList<KObject> readOnePageMsgs(long userId,int page,int pageSize){
 		return readPagedList(wbInboxDao,"inbox_count",userId,page,pageSize);
 	}
 	
 	/**
 	 * 获取用户的msg列表
 	 * @param userId
 	 * @param skip
 	 * @param max
 	 * @return ArrayList<KObject> 
 	 */
 	private static final ArrayList<KObject> getInboxMsgList(long userId,int skip,int max){
 		ArrayList<KObject> list = getMsgListWithRT(wbInboxDao,new BasicDBObject("user_id",userId).append("state", 0),skip,max);
 		wbUserDao.updateOne(new BasicDBObject("_id",userId), prop_notify_msg_reset);
 		return list;
 	}
 	
 	private static final ArrayList<KObject> readPagedList(DaoInterface dao,String countPropName,long userId,int page,int pageSize){
 		KObject user = wbUserDao.findOne(userId);
 		int msgCount = Integer.parseInt(user.getProp(countPropName).toString());
 		int skip = pagedSkip(page,msgCount,pageSize);
 		if (skip < 0) {
 			return null;
 		}
 		
 		ArrayList<KObject> list = getMsgListWithRT(dao,new BasicDBObject("user_id",userId).append("state", 0),skip,pageSize);
 		
 		return list;
 	}
 	
 	
 	/**
 	 * 获取带RT的消息列表,KObject中rtmsg属性保留原RT的KObject对象
 	 * @param dao 用于获取msgId列表的dao
 	 * @param query 用于获取msgId列表的查询条件,如:new BasicDBObject("user_id",userId)
 	 * @param skip
 	 * @param initSize
 	 * @return
 	 */
 	private static final ArrayList<KObject> getMsgListWithRT(DaoInterface dao,BasicDBObject query,int skip,int initSize){
 		ArrayList<Long> msgIdList = new ArrayList<Long>(initSize);
 		ArrayList<Long> allIdList = new ArrayList<Long>(initSize*2);
 		List<Map<String,Object>> msgIds = dao.query(query, prop_msg_rt_id,prop_id_desc, skip, initSize, null);
 		if (msgIds==null || msgIds.isEmpty()) {
 			return null;
 		}
 		for (Iterator<Map<String, Object>> it = msgIds.iterator(); it.hasNext();) {
 			Map<String, Object> m = it.next();
 			long msg_id = (Long)m.get("msg_id");
 			msgIdList.add(msg_id);
 			allIdList.add(msg_id);
 			Object rto = m.get("rt_id");
 			long rt = (rto==null)?0:(Long)m.get("rt_id");
 			if (rt > 0) {
 				allIdList.add(rt);
 			}
 		}
 		
 		//如果算上转发的,大小应该x2
 		int max = initSize*2;
 		HashMap<Long,KObject> map = getMsgMap(allIdList,max);
 		ArrayList<KObject> list = new ArrayList<KObject>(initSize);
 		for (Iterator<Long> it = msgIdList.iterator(); it.hasNext();) {
 			long m_id = it.next();
 			KObject kobj = map.get(m_id);
 			if (kobj == null) {
 				continue;
 			}
 			Object rto = kobj.getProp("rt_id");
 			if (StringUtil.isDigits(rto)) {
 				long rt = (Long)rto;
 				if (rt > 0) {
 					//设置原msg为kobj中的rtmsg
 					kobj.setProp("rtmsg", map.get(rt));
 				}
 			}
 			list.add(kobj);
 		}
 		return list;
 	}
 	
 	
 	/**
 	 * 从msgId列表中取出MSG对象列表(state==0的对象)
 	 * @param msgIdList ArrayList<Long>
 	 * @param initSize 初始化list的大小,一般为每页的size大小
 	 * @return ArrayList<KObject>
 	 */
 	@SuppressWarnings("unchecked")
 	private static final ArrayList<KObject> getMsgList(ArrayList<Long> msgIdList,int initSize,DBObject sort){
 		ArrayList<KObject> list = new ArrayList<KObject>(initSize);		
 		DBCollection coll = wbMsgDao.getColl();
 		DBCursor cur = null;
 		if (sort == null) {
 			cur = coll.find(new BasicDBObject("_id",new BasicDBObject("$in",msgIdList)).append("state", 0));
 		}else{
 			cur = coll.find(new BasicDBObject("_id",new BasicDBObject("$in",msgIdList)).append("state", 0)).sort(sort);
 		}
 		while (cur.hasNext()) {
 			Map<String,Object> m = (Map<String,Object>) cur.next();
 			list.add(new KObject(m));
 		}
 		return list;
 	}
 	
 	/**
 	 * 从msgId列表中取出MSG对象Map(state==0的对象)
 	 * @param msgIdList ArrayList<Long>
 	 * @param initSize 初始化list的大小,一般为每页的size大小
 	 * @return ArrayList<KObject>
 	 */
 	@SuppressWarnings("unchecked")
 	private static final HashMap<Long,KObject> getMsgMap(ArrayList<Long> msgIdList,int initSize){
 		HashMap<Long,KObject> map = new HashMap<Long,KObject>(initSize);		
 		DBCollection coll = wbMsgDao.getColl();
 		DBCursor cur = coll.find(new BasicDBObject("_id",new BasicDBObject("$in",msgIdList)).append("state", 0));
 		while (cur.hasNext()) {
 			Map<String,Object> m = (Map<String,Object>) cur.next();
 			KObject ko = new KObject(m);
 			map.put(ko.getId(), ko);
 		}
 		return map;
 	}
 	
 	/**
 	 * 获取用户的sent msg列表
 	 * @param userId
 	 * @param skip
 	 * @param max
 	 * @return ArrayList<KObject> 
 	 */
 	public static final ArrayList<KObject> getSentMsgList(long userId,int skip,int max){
 		ArrayList<KObject> list = getMsgListWithRT(wbSentDao,new BasicDBObject("user_id",userId).append("state", 0),skip,max);
 		return list;
 	}
 	
 	/**
 	 * 创建一个topic,并将新的talk的id加入,如果topic已存在则不创建只加入<br />
 	 * <strong>注意:此操作不支持并行,适用于单线程队列</strong>
 	 * @param topic
 	 * @param talkId long,msgId
 	 */
 	public static final void addTopic(String topic,long talkId){
 		//save一个topic
 		KObject tp = wbTagsDao.findOne(topic);
 		if (tp == null) {
 			tp = wbTagConfig.getKobjSchema().createEmptyKObj(wbTagsDao);
 			tp.setProp("name", topic);
 			tp.setProp("sum", 1);
 			tp.setProp("tag_ids", new ArrayList<Long>().add(talkId));
 			wbTagsDao.save(tp);
 		}else{
 			//FIXME 在数量达到峰值时，需要保存到另外的存储表
 			BasicDBObject update = new BasicDBObject();
 			update.put("$push", new BasicDBObject("tag_ids",talkId));
 			update.put("$inc", prop_sum);
 			wbTagsDao.updateOne(new BasicDBObject("_id",tp.getId()), update);
 		}
 		BasicDBObject qu = new BasicDBObject("_id", talkId);
 		BasicDBObject ou = new BasicDBObject("$push",new BasicDBObject("tags",topic));
 		wbMsgDao.update(qu,ou,false,false);
 	}
 	
 	/**
 	 * 在提到的用户中添加相关的msgId,提到的用户增加一个提到消息数
 	 * @param mentionUserName 提到的用户名
 	 * @param talkId 原消息id
 	 */
 	public static final void addMention(String mentionUserName,long talkId){
 		
 		BasicDBObject q = new BasicDBObject("name",mentionUserName);
 		BasicDBObject u = prop_notify_mention_inc;
 		//不更新wbuser的mentions
 		//u.append("$push",new  BasicDBObject("mentions",mentionMsgId));
 		wbUserDao.updateOne(q, u);
 		//mention提到的用户,
 		KObject mt = wbMentionConfig.getKobjSchema().createEmptyKObj(wbMentionDao);
 		mt.setProp("user_name", mentionUserName);
 		mt.setProp("msg_id", talkId);
 		wbMentionDao.save(mt);
 		
 //		//更新wbMention表
 //		DBCollection coll = wbMentionDao.getColl();
 //		BasicDBObject q = new BasicDBObject();
 //		q.put("user_name", mentionUserName);
 //		BasicDBObject o = new BasicDBObject();
 //		o.append("$push", new BasicDBObject("mention_ids",talkId));
 //		coll.update(q, o, true, false);
 //		//更新user表的metion
 //		BasicDBObject qu = new BasicDBObject("_id", talkId);
 //		BasicDBObject ou = new BasicDBObject("$push",new BasicDBObject("mentions",mentionUserName));
 //		wbMsgDao.update(qu,ou,false,false);
 //		qu.put("notify", 4);
 //		ou = new BasicDBObject("$inc", new BasicDBObject("notify.$", 1));
 //		this.update(qu,ou,false,false);
 	}
 	
 	/**
 	 * 发表/转发/评论,添加一个talk msg(同步)
 	 * TODO 最好还是放到用户自己的数据中去，当消息获取超过用户data长度时，从公共消息列表中获取
 	 * @param newMsg 已带有ID的空消息
 	 * @param talkMsg
 	 * @param userId
 	 * @param userName
 	 * @param screenName
 	 * @param source
 	 * @param place
 	 * @param pic_url
 	 * @param urls
 	 * @param topics
 	 * @param mentions
 	 * @param isRT 是否转发
 	 * @param rt_id 转发的原消息ID
 	 * @param talk_state msg的状态,用于控制是否显示
 	 * @return
 	 */
 	public static final boolean addTalk(KObject newMsg,String talkMsg,long userId,String userName,String screenName,String source,String place,String pic_url,ArrayList<String> urls,ArrayList<String> topics,ArrayList<String> mentions,boolean isRT,long rt_id,int talk_state){
 		//KObject newO = newMsg;
 		newMsg.setProp("text", talkMsg);
 		newMsg.setProp("creatorId", userId);
 		newMsg.setProp("creatorName", userName);
 		newMsg.setProp("author_screen", screenName);
 		newMsg.setProp("source", source);
 		newMsg.setProp("place", place);
 		newMsg.setState(talk_state);
 		if (isRT) {
 			newMsg.setProp("isRT", true);
 			newMsg.setProp("rt_id", rt_id);
 		}else{
 			newMsg.setProp("isRT", false);
 		}
 		if (topics != null) {
 			newMsg.setProp("tags", topics);
 		}
 		if (mentions != null) {
 			newMsg.setProp("mentions", mentions);
 		}
 		if (pic_url != null) {
 			newMsg.setProp("pic_url", pic_url);
 		}
 		if (urls != null) {
 			newMsg.setProp("urls", urls);
 		}
 		return wbMsgDao.save(newMsg);
 		//return newMsg.getId();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static final ArrayList<Long> getAllFans(long userId){
 //		DBCollection coll = wbFansDao.getColl();
 //		ArrayList<Long> list = new ArrayList<Long>();
 //		//List<Map<String, Object>> re = wbFansDao.query(new BasicDBObject("user_id",userId), new BasicDBObject("fans_id",1), new BasicDBObject("hot",-1), 0, 0, null);
 //		DBCursor cur = coll.find(prop_fans_id, new BasicDBObject("user_id",userId)).sort(new BasicDBObject("hot",-1));
 //		while (cur.hasNext()) {
 //			list.add((Long)(cur.next().get("fans_id")));
 //		}
 		
 		KObject user = wbUserDao.findOne(userId);
 		int msgCount = Integer.parseInt(user.getProp("followers_count").toString());
 		ArrayList<Long> userIdList = new ArrayList<Long>(msgCount);
 		List<Map<String,Object>> msgIds = wbUserDao.query(new BasicDBObject("_id",userId),new BasicDBObject("fans",1),null, 0, 1, null);
 		if (!msgIds.isEmpty()) {
 			Map<String, Object> u = msgIds.get(0);
 			List<Map<String,Object>> msgIdList = (List<Map<String, Object>>) u.get("fans");
 			for (Iterator<Map<String, Object>> it = msgIdList.iterator(); it.hasNext();) {
 				Map<String, Object> m = it.next();
 				userIdList.add((Long)m.get("id"));
 			}
 		}
 		
 		return userIdList;
 	}
 	
 	public static final void updateUserAndSentForNewMsg(long userId,String msg,long msgId,boolean isRT,long rt_id){
 		BasicDBObject u = new BasicDBObject("$inc", prop_statuses_count);
 		if (!isRT) {
 			u.append("$set",new BasicDBObject("lastMsg",msg));
 		}
 		wbUserDao.updateOne(new BasicDBObject("_id",userId), u);
 		KObject newSent = wbSentConfig.getKobjSchema().createEmptyKObj(wbSentDao);
 		newSent.setProp("user_id", userId);
 		newSent.setProp("msg_id", msgId);
 		newSent.setProp("rt_id", rt_id);
 		wbSentDao.save(newSent);
 	}
 	
 	/**
 	 * 向fans推送新消息,同时也推送给自己
 	 * TODO 推送消息最好的实现:只更新fans的wbuser表的inbox[]字段,在相关fans进行消息查看操作时,由异步任务将新消息处理到wb_inbox表中
 	 * @param userId
 	 * @param msgId
 	 * @param rt_id 被转的id
 	 */
 	public static final void pushMsgToFans(long userId,long msgId,int state,long rt_id){
 		
 		
 		
 		//先向自己推送
 		KObject in = wbInboxConfig.getKobjSchema().createEmptyKObj(wbInboxDao);
 		in.setProp("user_id", userId);
 		in.setProp("msg_id", msgId);
 		in.setProp("rt_id", rt_id);
 		in.setState(state);
 		wbInboxDao.save(in);
 		wbUserDao.updateOne(new BasicDBObject("_id",userId), new BasicDBObject("$inc",prop_notify_msg));
 		
 		ArrayList<Long> fansIds = getAllFans(userId);
 		if (fansIds.isEmpty()) {
 			return;
 		}
 		//向fans推送
 		for (Iterator<Long> it = fansIds.iterator(); it.hasNext();) {
 			long fanId = it.next();
 			KObject inbox = wbInboxConfig.getKobjSchema().createEmptyKObj(wbInboxDao);
 			inbox.setProp("user_id", fanId);
 			inbox.setProp("msg_id", msgId);
 			inbox.setProp("rt_id", rt_id);
 			inbox.setProp("creatorId", userId);
 			wbInboxDao.save(inbox);
 			wbUserDao.updateOne(new BasicDBObject("_id",fanId), new BasicDBObject("$inc",prop_notify_msg));
 		}
 		
 	}
 	
 	public static void main(String[] args) {
 		
 	}
 
 }
