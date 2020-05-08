 package com.gemantic.killer.service.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.runtime.rule.FactHandle;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.gemantic.common.exception.ServiceDaoException;
 import com.gemantic.common.exception.ServiceException;
 import com.gemantic.killer.common.model.Message;
 import com.gemantic.killer.common.model.Operater;
 import com.gemantic.killer.model.Room;
 import com.gemantic.killer.model.User;
 import com.gemantic.killer.service.MessageService;
 import com.gemantic.killer.service.RoomService;
 import com.gemantic.killer.service.RoomTimerService;
 import com.gemantic.killer.service.SessionService;
 import com.gemantic.killer.util.MessageUtil;
 import com.gemantic.labs.killer.model.Records;
 import com.gemantic.labs.killer.service.RecordService;
 import com.gemantic.labs.killer.service.UsersService;
 
 //这段代码有点乱.有时间整理一下.
 //太多需要重构的地方了.判断是哪一个Process启动.不应该通过配置文件.而是应该通过游戏房间的状态.
 //用户名什么时候应该传进去.Snapshot应该如何处理.
 @Component
 public class MessageServiceSingleDroolsImpl implements MessageService {
 
 	private static final Log log = LogFactory.getLog(MessageServiceSingleDroolsImpl.class);
 
 	private static final Log recordLog = LogFactory.getLog("gameRecord");
 
 	@Autowired
 	private RoomTimerService roomTimerSevice;
 	@Autowired
 	private SessionService sessionService;
 
 	@Autowired
 	private UsersService userService;
 
 	@Autowired
 	private RoomService roomService;
 
 	@Autowired
 	private RecordService recordService;
 
 	@Resource(name = "roomAction")
 	private Set<String> roomAction = new HashSet();
 
 	public List<Message> generate(Message message, Room room) throws ServiceException, ServiceDaoException {
 		// 根据不同的房间ID创建不同的Session.这样怎么能支持扩展性呢.Session能否序列化.除非支持按房间分布Service的Session.
 
 		Long start=System.currentTimeMillis();
 		Operater operater = new Operater(message);
 		try{
 		process(operater, room);
 		}catch(Throwable t){
 			t.printStackTrace();
 			log.error(t);
 			log.error("error room is "+room +" message is "+message);
 			log.info("because room error so retract room ");
 			roomService.removeRoom(room.getId());
 			
 		}
 		// 应该是开始游戏之后才记录.
 
 		if (CollectionUtils.isEmpty(operater.getTimerMessages())) {
 
 			log.info(message.getId() + " not have time ");
 
 		} else {
 			log.info(message.getId() + " have time " + operater.getTimerMessages());
 			roomTimerSevice.nextMessages(operater.getTimerMessages());
 		}
 		// 怎么对顺序排序
 		log.info(message.getId()+" user time is "+(System.currentTimeMillis()-start));
 
 		log.info(operater.getNextMessages());
 		return operater.getNextMessages();
 
 	}
 
 	private Operater process(Operater operater, Room r) throws ServiceException, ServiceDaoException {
 
 		Long roomID = Long.valueOf(operater.getMessage().getWhere());
 		// 大爷的.这个时候还没有Room
 		//log.info(operater + " =========== start,room  ======================== " + r);
 		Long start = System.currentTimeMillis();
 		StatefulKnowledgeSession ksession = sessionService.getSesseion(operater.getMessage());
 
 		FactHandle fo = ksession.insert(operater);
 
 		//log.info(operater + " =========== after insert");
 		FactHandle fm = ksession.insert(operater.getMessage());
 		if (IsRoomMessage(operater.getMessage(), r)) {
 			//log.info("room operator " + operater.getMessage());
 			ksession.startProcess("room");
 		} else {
 			//log.info("game operator " + operater.getMessage());
 			ksession.startProcess("game");
 		}
 
 		ksession.fireAllRules();
 		ksession.retract(fo);
 		ksession.retract(fm);
 
 		// 什么时候关闭Session呢.规则里触发游戏结束的事件.
 
 		//log.info(" use time " + (System.currentTimeMillis() - start));
 		//log.info(operater + " =========== over");
 		List<Message> messages = new ArrayList();
 		messages = operater.getNextMessages();
 		
 		if (operater.getGameStart()) {
 			// 创建的时候不会更新.因为创建的时候不会是Start
 			//log.info("game start");// 主要是在Session里.愁死我了.Gameover的时候不能把Session给Remove了.
 			r.setStartAt(operater.getMessage().getTime());
 
 			r.setStatus(Room.status_start);
 			r.setPlayers(operater.getPlayers());
 			this.roomService.updateRoom(r);
 
 		}
 
 		Long prevStart=r.getStartAt();
 		Long end=System.currentTimeMillis();
 		Long time=end-prevStart;
 		
 		if (operater.getGameOver()) {
 
 		//	log.info("game over");// 主要是在Session里.愁死我了.Gameover的时候不能把Session给Remove了.
 
 			this.roomTimerSevice.removeRoomTimer(Long.valueOf(operater.getMessage().getWhere()));
 			operater.getTimerMessages().clear();
 
 			r.setStartAt(System.currentTimeMillis() * 2);// 设置游戏的开始时间远远大于现在.
 			r.setStatus(Room.status_wait);
 
 			this.roomService.updateRoom(r);
 			// 从哪知道游戏里的玩家呢
 
			if(r.getVersion().equals("killer_police_1.0")||r.getVersion().equals("ghost_simple_1.0")){
 				
 				for (Message m : messages) {
 					if ("decryption" == m.getPredict()) {
 						Long uid = Long.valueOf(m.getSubject());
 						User u = this.userService.getObjectById(uid);
 						u.setMoney(u.getMoney() + 2000);//
 						this.userService.update(u);
 					}
 
 				}
 				
 			}
 			
 			
 			if(r.getPlayers().size()>=6&&time>3*60*1000){
 				//六人局才发钱和超过三分钟才给钱存战例
 				for (Message m : messages) {
 					if ("decryption" == m.getPredict()) {
 						Long uid = Long.valueOf(m.getSubject());
 						User u = this.userService.getObjectById(uid);
 						u.setMoney(u.getMoney() + 1000);//
 						this.userService.update(u);
 					}
 
 				}
 				if(r.getVersion().equals("simple_1.0")){
 					
 					// 更新战例记录
 					Records record = new Records();
 					record.setId(operater.getRecordID());
 					record.setPath("record/" + operater.getRecordID()+".txt");
 					record.setTime(time);
 					record.setRoom(r);
 					record.setVersion(r.getVersion());		
 					
 					List<Long> ls=r.getPlayers();
 				    List<User> users=this.userService.getObjectsByIds(ls);
 				    Map<Long,String> uid_names=new HashMap();
 					for(User user:users){
 						uid_names.put(user.getId(), user.getName());
 					}
 					record.setUid_names(uid_names);
 					this.recordService.insert(record);
 				//	log.info(" insert record " + record);
 				}
 				
 				
 			}
 			
 			
 			
 
 			
 
 		}
 
 		// 开始之后就没有了计房间的状态了.
 
 		//log.info(roomID + " room is  empty ?  " + operater.getRoomEmpty());
 
 		if (operater.getRoomEmpty()) {
 			log.info("room empty ");
 
 			this.roomService.removeRoom(roomID);
 			this.sessionService.removeSession(operater.getMessage());
 			//log.info("room over ===================" + r);
 
 		}
 
 		return operater;
 	}
 
 	private boolean IsRoomMessage(Message message, Room r) {
 
 		if("video_1.0".equals(r.getVersion())){
 			return true;
 		}
 		
 		if( (Room.status_start == r.getStatus() || "start".equals(message.getPredict()))){
 			return false;
 		}else {
 			return true;
 
 		}
 
 		/*
 		 * String predict = message.getPredict();
 		 * 
 		 * if (predict.equals("query") && !gameOver) { //
 		 * 如果是查询,而且游戏又没有进行完.走的Game规则的查询接口. return false;
 		 * 
 		 * }
 		 * 
 		 * return this.roomAction.contains(message.getPredict());
 		 */
 	}
 
 	public Set<String> getRoomAction() {
 		return roomAction;
 	}
 
 	public void setRoomAction(Set<String> roomAction) {
 		this.roomAction = roomAction;
 	}
 
 	@Override
 	public String getSnapshots(Message queryMessage, Room room) throws ServiceException, ServiceDaoException {
 		Operater operator = new Operater(queryMessage);
 		// 这儿还是会是空的
 		process(operator, room);
 
 		return operator.getSnapshots();
 	}
 
 	@Override
 	public List<Message> createRoom(Room room) throws ServiceException, ServiceDaoException {
 		Message createMessage = MessageUtil.parse(room.getVersion(), room.getCreaterID() + ",create,-500,1000000,78," + room.getId(), "我创建了房间");
 
 		Operater operator = new Operater(createMessage);
 		operator.setSetting(room.getSetting());
 		process(operator, room);
 
 		if (CollectionUtils.isEmpty(operator.getTimerMessages())) {
 
 		} else {
 			roomTimerSevice.nextMessages(operator.getTimerMessages());
 		}
 
 		return operator.getNextMessages();
 
 	}
 
 	@Override
 	public List<Message> updateSetting(Room room) throws ServiceException, ServiceDaoException {
 		Message createMessage = MessageUtil.parse(room.getVersion(), room.getCreaterID() + ",setting,update,1000000,78," + room.getId(), "");
 
 		Operater operator = new Operater(createMessage);
 		operator.setSetting(room.getSetting());
 		process(operator, room);
 		// 怎么对顺序排序
 
 		return operator.getNextMessages();
 
 	}
 
 }
