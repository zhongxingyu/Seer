 package com.gemantic.killer.service.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.QueryResults;
 import org.drools.runtime.rule.QueryResultsRow;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.util.CollectionUtils;
 
 import com.gemantic.common.exception.ServiceDaoException;
 import com.gemantic.common.exception.ServiceException;
 import com.gemantic.killer.common.model.Message;
 import com.gemantic.killer.exception.ServiceErrorCode;
 import com.gemantic.killer.model.Room;
 import com.gemantic.killer.service.MemberService;
 import com.gemantic.killer.service.RoomService;
 import com.gemantic.killer.service.SessionService;
 import com.gemantic.killer.util.MapDataUtil;
 
 @Component
 public class MemberServiceImpl implements MemberService {
 
 	private static final Log log = LogFactory.getLog(MemberServiceImpl.class);
 	private Map<Long, Set<Long>> room_member = new HashMap<Long, Set<Long>>();
 
 	private Map<Long, Set<Long>> room_readys = new HashMap<Long, Set<Long>>();
 
 	private Map<Long, Set<Long>> room_lives = new HashMap<Long, Set<Long>>();
 
 	private Map<Long, Long> uid_rid = new HashMap<Long, Long>();
 
 	private int playerLimit = 2;
 
 	@Autowired
 	private RoomService roomService;
 
 
 	@Autowired
 	private SessionService sessionService;
 
 	@Override
 	public List<Long> getMembers(Long roomID) throws ServiceException, ServiceDaoException {
		try{
 		Room r = this.roomService.getRoom(roomID);
 		Message m = new Message("-500", "get", "member");
 		m.setVersion(r.getVersion());
 		m.setWhere(r.getId().toString());
 		StatefulKnowledgeSession ksession = sessionService.getSesseion(m);
 
 		QueryResults results = ksession.getQueryResults("member of room");
 		List<Long> ids = new ArrayList();
 		for (QueryResultsRow row : results) {
 			Set persons = (Set) row.get("ids");
 			for (Object person : persons) {
 				if(person==null){
 					continue;
 				}
 				ids.add(Long.valueOf((String) person));
 			}
 			log.info(persons);
 
 		}
 		return new ArrayList<Long>(ids);
		}catch(Throwable t){
			log.error(t+" get member error "+roomID);
			t.printStackTrace();
			return new ArrayList();
		}
 	}
 
 	@Override
 	public void newUserEnterRoom(Long roomID, Long userID) throws ServiceException, ServiceDaoException {
 
 		MapDataUtil.superAdditionValue(room_member, roomID, MapDataUtil.Type_Set, userID);
 		this.uid_rid.put(userID, roomID);
 
 	}
 
 	@Override
 	public boolean containUser(Long roomID, Long userID) throws ServiceException, ServiceDaoException {
 		if (room_member.containsKey(roomID)) {
 			Set<Long> uids = room_readys.get(roomID);
 			if (CollectionUtils.isEmpty(uids)) {
 				return false;
 			} else {
 				return uids.contains(userID);
 			}
 
 		} else {
 			return false;
 		}
 
 	}
 
 	@Override
 	public void ready(Long roomID, Long userID) throws ServiceException, ServiceDaoException {
 		log.info(roomID + " has ready " + userID);
 		Set<Long> uids = room_readys.get(roomID);
 		if (uids == null) {
 			uids = new HashSet();
 		}
 		if (uids.size() >= this.playerLimit) {
 			throw new ServiceException(ServiceErrorCode.Room_Ready_Count_Limit);
 		} else {
 			MapDataUtil.superAdditionValue(room_readys, roomID, MapDataUtil.Type_Set, userID);
 
 		}
 		log.info(roomID + " has ready  success " + userID);
 
 	}
 
 	public int getPlayerLimit() {
 		return playerLimit;
 	}
 
 	public void setPlayerLimit(int playerLimit) {
 		this.playerLimit = playerLimit;
 	}
 
 	@Override
 	public Set<Long> getMembersStatus(Long rid) throws ServiceException, ServiceDaoException {
 
 		Set<Long> rids = this.room_readys.get(rid);
 		if (CollectionUtils.isEmpty(rids)) {
 			rids = new HashSet();
 		}
 
 		return rids;
 	}
 
 	@Override
 	public void userLogOut(Long rid, Long uid) throws ServiceException, ServiceDaoException {
 		MapDataUtil.superSubtractValue(room_member, rid, MapDataUtil.Type_Set, uid);
 		MapDataUtil.superSubtractValue(room_readys, rid, MapDataUtil.Type_Set, uid);
 		this.uid_rid.remove(uid);
 	}
 
 	@Override
 	public void die(Long rid, Long uid) throws ServiceException, ServiceDaoException {
 		MapDataUtil.superSubtractValue(this.room_lives, rid, MapDataUtil.Type_Set, uid);
 
 	}
 
 	@Override
 	public Set<Long> getLivers(Long rid) throws ServiceException, ServiceDaoException {
 
 		Set<Long> ids = this.room_lives.get(rid);
 		if (CollectionUtils.isEmpty(ids)) {
 			ids = new HashSet();
 		}
 		return ids;
 	}
 
 	@Override
 	public void initLivers(Long rid) throws ServiceException, ServiceDaoException {
 		Set<Long> ready = this.room_readys.get(rid);
 		Set<Long> livers = new HashSet();
 		livers.addAll(ready);
 		this.room_lives.put(rid, livers);
 
 	}
 
 	@Override
 	public void clearLivers(Long rid) throws ServiceException, ServiceDaoException {
 		this.room_lives.put(rid, new HashSet());
 
 	}
 
 	@Override
 	public void clearReadys(Long rid) throws ServiceException, ServiceDaoException {
 		this.room_readys.put(rid, new HashSet());
 
 	}
 
 	public Map<Long, Long> getUid_rid() {
 		return uid_rid;
 	}
 
 	public void setUid_rid(Map<Long, Long> uid_rid) {
 		this.uid_rid = uid_rid;
 	}
 
 	@Override
 	public Room getRoomOfUser(Long uid) throws ServiceException, ServiceDaoException {
 		// TODO Auto-generated method stub
 		Long rid = this.uid_rid.get(uid);
 		if (rid == null) {
 			return null;
 		} else {
 			return this.roomService.getRoom(rid);
 		}
 
 	}
 
 	@Override
 	public Map<Long, Long> batchGetRoomOfUser(List<Long> userIDS) throws ServiceException, ServiceDaoException {
 		Map<Long, Long> result = new HashMap();
 		for (Long uid : userIDS) {
 			Room r=this.getRoomOfUser(uid);
 			if(r!=null){
 				result.put(uid, this.uid_rid.get(uid));
 			}
 			
 		}
 		return result;
 	}
 
 }
