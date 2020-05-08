 /**
  *
  */
 package q.web.message;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import q.biz.CacheService;
 import q.dao.DaoHelper;
 import q.dao.MessageDao;
 import q.dao.PeopleDao;
 import q.dao.page.MessageJoinPeoplePage;
 import q.dao.page.MessagePage;
 import q.domain.Message;
 import q.domain.MessageJoinPeople;
 import q.util.CollectionKit;
 import q.util.IdCreator;
 import q.web.Resource;
 import q.web.ResourceContext;
 import q.web.exception.PeopleNotLoginException;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Feb 21, 2011
  * 
  */
 public class GetMessageIndex extends Resource {
 
 	private MessageDao messageDao;
 
 	public void setMessageDao(MessageDao messageDao) {
 		this.messageDao = messageDao;
 	}
 
 	private PeopleDao peopleDao;
 
 	public void setPeopleDao(PeopleDao peopleDao) {
 		this.peopleDao = peopleDao;
 	}
 
 	private CacheService cacheService;
 
 	public void setCacheService(CacheService cacheService) {
 		this.cacheService = cacheService;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.web.Resource#execute(q.web.ResourceContext)
 	 */
 	@Override
 	public void execute(ResourceContext context) throws Exception {
 		long loginPeopleId = context.getCookiePeopleId();
 		this.cacheService.clearMessageNotify(loginPeopleId);
 		int size = context.getInt("size", 10);
 		long startId = context.getIdLong("startId", IdCreator.MAX_ID);
 		int type = context.getInt("type", 0);
 
 		MessageJoinPeoplePage joinPage = new MessageJoinPeoplePage();
 		joinPage.setIgnoreReplyNum(false);
 		joinPage.setReceiverId(loginPeopleId);
 		int asc = 1;
 		if (type == asc) { // 1 indicate asc
 			joinPage.setDesc(false);
 		} else {
 			joinPage.setDesc(true);
 		}
 		boolean hasPrev = false;
 		boolean hasNext = false;
 		int fetchSize = size + 1;// to discover has prev or has next
 		joinPage.setSize(fetchSize);
 		joinPage.setStartId(startId);
 		List<MessageJoinPeople> messageJoinPeoples = messageDao.getMessageJoinPeoplesByPage(joinPage);
 		Map<Long, MessageJoinPeople> messageId2MessageJoinPeopleMap = new HashMap<Long, MessageJoinPeople>();
 		List<Long> messageIds = new ArrayList<Long>(messageJoinPeoples.size());
 		for (MessageJoinPeople join : messageJoinPeoples) {
 			messageIds.add(join.getMessageId());
 			messageId2MessageJoinPeopleMap.put(join.getMessageId(), join);
 		}
 		if (CollectionKit.isNotEmpty(messageJoinPeoples)) {
 			if (messageIds.size() == fetchSize) {
 				if (type == asc) { // more than one previous page
 					hasPrev = true;
 				} else { // more than one next page
 					hasNext = true;
 				}
 				messageIds.remove(messageIds.size() - 1);// remove last one
 			}
 			if (type == asc) { // this action from next page
 				hasNext = true;
 			} else if (startId < IdCreator.MAX_ID) {// this action from previous
 													// page
 				hasPrev = true;
 			}
 			MessagePage messagePage = new MessagePage();
 			messagePage.setIds(messageIds);
 			List<Message> messages = messageDao.getMessagesByPage(messagePage);
 			Map<String, Object> api = new HashMap<String, Object>();
 			if (CollectionKit.isNotEmpty(messages)) {
 				Map<Long, List<Long>> messageId2ReceiverIdsMap = getMessageReceiversMap(messageIds);
 				messageId2ReceiverIdsMap.remove(loginPeopleId);
 				for (Message msg : messages) {
 					msg.setReceiverIds(messageId2ReceiverIdsMap.get(msg.getId())); // set
 																					// message
 																					// receiver
 					MessageJoinPeople join = messageId2MessageJoinPeopleMap.get(msg.getId());
 					if (join != null) {
 						msg.setReplyNum(join.getReplyNum());
 						msg.setLastReplyId(join.getLastReplyId());
 						msg.setLastReplySenderId(join.getLastReplySenderId());
 					} else {
 						log.error("invalid join -> messageId:%s,peopleId:%s", msg.getId(), loginPeopleId);
 					}
 				}
 				DaoHelper.injectMessagesWithLastReply(messageDao, messages); // inject
 																				// last
 																				// reply
 																				// using
 																				// lastReplyId
 				DaoHelper.injectMessagesWithSenderAndReceiversAndLastReplySender(peopleDao, messages); // inject
 																										// sender,
 																										// receivers,
 																										// lastReply.sender
 				Collections.sort(messages, new Comparator<Message>() {
 
 					@Override
 					public int compare(Message o1, Message o2) {
 						long cha = o1.getLastReplyId() - o2.getLastReplyId();
 						if (cha > 0) {
 							return -1;
 						} else if (cha < 0) {
 							return 1;
 						} else { // cha == 0
 							return 0;
 						}
 					}
 				}); // sort order by id desc
 				api.put("messages", messages);
 			}
 			api.put("hasPrev", hasPrev);
 			api.put("hasNext", hasNext);
 			context.setModel("api", api);
 		}
 
 	}
 
 	/**
 	 * 返回message id和参与者的映射
 	 * 
 	 * @param messageIds
 	 * @return
 	 * @throws SQLException
 	 */
 	private Map<Long, List<Long>> getMessageReceiversMap(List<Long> messageIds) throws SQLException {
 		if (CollectionKit.isEmpty(messageIds)) {
 			return null;
 		}
 		Map<Long, List<Long>> messageIdReceiversMap = new HashMap<Long, List<Long>>();
 		MessageJoinPeoplePage receiversPage = new MessageJoinPeoplePage();
 		receiversPage.setMessageIds(messageIds);
 		List<MessageJoinPeople> Joins = messageDao.getMessageJoinPeoplesByPage(receiversPage);
 		for (MessageJoinPeople join : Joins) {
 			if (join.getSenderId() != join.getReceiverId()) {
 				List<Long> receivers = messageIdReceiversMap.get(join.getMessageId());
 				if (null == receivers) {
 					receivers = new ArrayList<Long>();
 					messageIdReceiversMap.put(join.getMessageId(), receivers);
 				}
 				receivers.add(join.getReceiverId());
 			}
 		}
 		return messageIdReceiversMap;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.web.Resource#validate(q.web.ResourceContext)
 	 */
 	@Override
 	public void validate(ResourceContext context) throws Exception {
 		long loginPeopleId = context.getCookiePeopleId();
 		if (IdCreator.isNotValidId(loginPeopleId)) {
 			throw new PeopleNotLoginException();
 		}
 	}
 
 }
