 /**
  *
  */
 package q.dao;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import q.dao.page.PeopleRelationPage;
 import q.domain.Category;
 import q.domain.Event;
 import q.domain.Favorite;
 import q.domain.Group;
 import q.domain.GroupJoinCategory;
 import q.domain.Message;
 import q.domain.MessageReply;
 import q.domain.People;
 import q.domain.PeopleJoinEvent;
 import q.domain.PeopleJoinGroup;
 import q.domain.PeopleRelation;
 import q.domain.Status;
 import q.domain.Weibo;
 import q.domain.WeiboModel;
 import q.domain.WeiboReply;
 import q.util.CollectionKit;
 import q.util.MapKit;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Feb 22, 2011
  * 
  */
 public class DaoHelper {
 	public static List<Long> convertPeopleJoinGroupsToPeopleIds(List<PeopleJoinGroup> joins) {
 		if (CollectionKit.isEmpty(joins)) {
 			return null;
 		}
 		List<Long> peopleIds = new ArrayList<Long>(joins.size());
 		for (PeopleJoinGroup join : joins) {
 			peopleIds.add(join.getPeopleId());
 		}
 		return peopleIds;
 	}
 
 	public static void injectWeiboModelsWithFavorite(FavoriteDao favoriteDao, List<? extends WeiboModel> weiboModels, long peopleId) throws SQLException {
 		if (CollectionKit.isNotEmpty(weiboModels)) {
 			List<Long> modelIds = new ArrayList<Long>(weiboModels.size());
 			for (WeiboModel model : weiboModels) {
 				modelIds.add(model.getId());
 			}
 			List<Long> favIds = favoriteDao.getFavoriteIdsByFromIdsAndCreatorId(modelIds, peopleId);
 			if (CollectionKit.isNotEmpty(favIds)) {
 				Set<Long> favSet = new HashSet<Long>(favIds);
 				for (WeiboModel model : weiboModels) {
 					if (favSet.contains(model.getId())) {
 						model.setFav(true);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param favoriteDao
 	 * @param weibo
 	 * @throws SQLException
 	 */
 	public static void injectWeiboWithFavorite(FavoriteDao favoriteDao, Weibo weibo, long peopleId) throws SQLException {
 		Favorite fav = favoriteDao.getWeiboFavoriteByWeiboIdAndCreatorId(weibo.getId(), peopleId);
 		if (null == fav || fav.isUnFav()) {
 			weibo.setFav(false);
 		} else {
 			weibo.setFav(true);
 		}
 
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param groupDao
 	 * @param weibos
 	 * @return
 	 * @throws SQLException
 	 */
 	public static void injectWeiboModelsWithFrom(GroupDao groupDao, List<? extends WeiboModel> weibos) throws SQLException {
 		if (CollectionKit.isNotEmpty(weibos)) {
 			Set<Long> groupIds = null;
 			for (WeiboModel weibo : weibos) {
 				if (weibo.isFromGroup()) {
 					if (groupIds == null) {
 						groupIds = new HashSet<Long>(weibos.size());// lazy init
 					}
 					groupIds.add(weibo.getFromId());
 				}
 			}
 			if (CollectionKit.isNotEmpty(groupIds)) {
 				Map<Long, String> groupIdNameMap = groupDao.getGroupIdNameMapByIds(groupIds);
 				for (WeiboModel weibo : weibos) {
 					if (weibo.isFromGroup()) {
 						weibo.setFromPostfix(groupIdNameMap.get(weibo.getFromId()));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param groupDao
 	 * @param events
 	 * @throws SQLException
 	 */
 	public static void injectEventsWithGroupName(GroupDao groupDao, List<Event> events) throws SQLException {
 		if (CollectionKit.isNotEmpty(events)) {
 			Set<Long> groupIds = new HashSet<Long>(events.size());
 			for (Event event : events) {
 				groupIds.add(event.getGroupId());
 			}
 			if (CollectionKit.isNotEmpty(groupIds)) {
 				Map<Long, String> groupIdNameMap = groupDao.getGroupIdNameMapByIds(groupIds);
 				for (Event event : events) {
 					event.setGroupName(groupIdNameMap.get(event.getGroupId()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param groupDao
 	 * @param event
 	 * @throws SQLException
 	 */
 	public static void injectEventWithGroupName(GroupDao groupDao, Event event) throws SQLException {
 		if (event != null) {
 			Group group = groupDao.getGroupById(event.getGroupId());
 			event.setGroupName(group.getName());
 		}
 
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param event
 	 * @throws SQLException
 	 */
 	public static void injectEventWithRealName(PeopleDao peopleDao, Event event) throws SQLException {
 		if (event != null) {
 			People people = peopleDao.getPeopleById(event.getCreatorId());
 			event.setCreatorRealName(people.getRealName());
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param hotWeibos
 	 * @throws SQLException
 	 */
 	public static void injectWeiboModelsWithSenderRealName(PeopleDao peopleDao, List<? extends WeiboModel> weiboModels) throws SQLException {
 		if (CollectionKit.isNotEmpty(weiboModels)) {
 			Set<Long> senderIds = new HashSet<Long>(weiboModels.size());
 			for (WeiboModel weibo : weiboModels) {
 				senderIds.add(weibo.getSenderId());
 				if (weibo.getQuoteSenderId() > 0) {
 					senderIds.add(weibo.getQuoteSenderId());
 				}
 			}
 			if (CollectionKit.isNotEmpty(senderIds)) {
 				Map<Long, String> peopleIdRealNameMap = peopleDao.getIdRealNameMapByIds(senderIds);
 				for (WeiboModel weibo : weiboModels) {
 					weibo.setSenderRealName(peopleIdRealNameMap.get(weibo.getSenderId()));
 					if (weibo.getQuoteSenderId() > 0) {
 						weibo.setQuoteSenderRealName(peopleIdRealNameMap.get(weibo.getQuoteSenderId()));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param weiboDao
 	 * @param favorites
 	 */
 	public static void injectFavoritesWithSource(WeiboDao weiboDao, List<Favorite> favorites) throws SQLException {
 		if (CollectionKit.isNotEmpty(favorites)) {
 			List<Long> sourceWeiboIds = null; // lazy init
 			List<Long> sourceReplyIds = null; // lazy inti
 			for (Favorite fav : favorites) { // extract weibo and reply sources
 				if (fav.isFromWeibo()) {
 					if (sourceWeiboIds == null) {
 						sourceWeiboIds = new ArrayList<Long>(favorites.size());
 					}
 					sourceWeiboIds.add(fav.getFromId());
 				}
 				if (fav.isFromReply()) {
 					if (sourceReplyIds == null) {
 						sourceReplyIds = new ArrayList<Long>(favorites.size());
 					}
 					sourceReplyIds.add(fav.getFromId());
 				}
 			}
 			Map<Long, Weibo> weiboMap = null;
 			if (CollectionKit.isNotEmpty(sourceWeiboIds)) {
 				weiboMap = new HashMap<Long, Weibo>(sourceWeiboIds.size());
 				List<Weibo> weibos = weiboDao.getWeibosByIds(sourceWeiboIds, false);
 				for (Weibo weibo : weibos) {
 					if (weibo.isDelete()) {
 						weibo.setContent(null);// XXX remove by sean
 					}
 					weiboMap.put(weibo.getId(), weibo);
 				}
 			}
 			Map<Long, WeiboReply> replyMap = null;
 			if (CollectionKit.isNotEmpty(sourceReplyIds)) {
 				replyMap = new HashMap<Long, WeiboReply>(sourceReplyIds.size());
 				List<WeiboReply> replies = weiboDao.getWeiboRepliesByIds(sourceReplyIds, false);
 				for (WeiboReply reply : replies) {
 					if (reply.isCommon()) {
 						replyMap.put(reply.getId(), reply);
 					}
 				}
 			}
 			for (Favorite fav : favorites) { // inject weibo and reply sources
 				if (weiboMap != null && fav.isFromWeibo()) {
 					Weibo weibo = weiboMap.get(fav.getFromId());
					if(weibo != null) {
						fav.setSource(weibo);
						fav.getSource().setFav(true);
					}
 				}
 				if (replyMap != null && fav.isFromReply()) {
					WeiboReply reply = replyMap.get(fav.getFromId());
					if (reply != null) {
						fav.setSource(reply);
						fav.getSource().setFav(true);
					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * @param messageDao
 	 * @param messages
 	 */
 	public static void injectMessagesWithLastReply(MessageDao messageDao, List<Message> messages) throws SQLException {
 		if (CollectionKit.isEmpty(messages)) {
 			return;
 		}
 		List<Long> replyIds = new ArrayList<Long>(messages.size());
 		for (Message message : messages) {
 			replyIds.add(message.getLastReplyId());
 		}
 		List<MessageReply> replies = messageDao.getMessageRepliesByIds(replyIds);
 		if (CollectionKit.isEmpty(replies)) {
 			return;
 		}
 		Map<Long, MessageReply> messageId2ReplyMap = new HashMap<Long, MessageReply>();
 		for (MessageReply reply : replies) {
 			messageId2ReplyMap.put(reply.getQuoteMessageId(), reply);
 		}
 		for (Message message : messages) {
 			message.setLastReply(messageId2ReplyMap.get(message.getId()));
 		}
 
 	}
 
 	public static Map<Long, People> injectMessagesWithSenderAndReceiversAndLastReplySender(PeopleDao peopleDao, List<Message> messages) throws SQLException {
 		if (CollectionKit.isEmpty(messages)) {
 			return null;
 		}
 		HashSet<Long> peopleIds = new HashSet<Long>(messages.size() * 2);
 		for (Message message : messages) {
 			peopleIds.add(message.getSenderId());
 			if (CollectionKit.isNotEmpty(message.getReceiverIds())) {
 				peopleIds.addAll(message.getReceiverIds());
 			}
 			MessageReply lastReply = message.getLastReply();
 			if (lastReply != null) {// TODO 测试数据比较混乱,先加上 sean
 				peopleIds.add(lastReply.getSenderId());
 			}
 		}
 		if (CollectionKit.isEmpty(peopleIds)) {
 			return null;
 		}
 		List<People> peoples = peopleDao.getPeoplesByIds(new ArrayList<Long>(peopleIds));
 		if (CollectionKit.isEmpty(peoples)) {
 			return null;
 		}
 
 		Map<Long, People> peopleMap = convertToMap(peoples);
 		for (Message message : messages) {
 			message.setSender(peopleMap.get(message.getSenderId()));
 			if (CollectionKit.isNotEmpty(message.getReceiverIds())) {
 				for (Long mid : message.getReceiverIds()) {
 					message.addReceiver(peopleMap.get(mid));
 				}
 			}
 			MessageReply lastReply = message.getLastReply();
 			if (lastReply != null) {
 				lastReply.setSender(peopleMap.get(lastReply.getSenderId()));
 			}
 		}
 		return peopleMap;
 	}
 
 	public static void injectMessageRepliesWithSenderAndReceivers(PeopleDao peopleDao, List<MessageReply> replies) throws SQLException {
 		if (!CollectionKit.isEmpty(replies)) {
 			ArrayList<Long> peopleIds = new ArrayList<Long>(); // people number
 																// is less than
 																// messages
 																// count + 1
 			for (MessageReply message : replies) {
 				peopleIds.add(message.getSenderId());
 			}
 			List<People> peoples = peopleDao.getPeoplesByIds(peopleIds);
 			if (!CollectionKit.isEmpty(peoples)) {
 				Map<Long, People> peopleMap = convertToMap(peoples);
 				for (int i = 0; i < replies.size(); i++) {
 					MessageReply message = replies.get(i);
 					message.setSender(peopleMap.get(message.getSenderId()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param events
 	 * @throws SQLException
 	 */
 	public static void injectEventsWithRealName(PeopleDao peopleDao, List<Event> events) throws SQLException {
 		if (CollectionKit.isNotEmpty(events)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(events.size());
 			for (Event event : events) {
 				peopleIds.add(event.getCreatorId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (Event event : events) {
 				event.setCreatorRealName(map.get(event.getCreatorId()));
 			}
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param weiboDao
 	 * @param groupDao
 	 * @param weiboId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static void injectWeiboWithSenderRealNameAndFrom(PeopleDao peopleDao, GroupDao groupDao, Weibo weibo) throws SQLException {
 		People people = peopleDao.getPeopleById(weibo.getSenderId());
 		weibo.setSenderRealName(people.getRealName());
 		if (weibo.isFromGroup()) {
 			Group group = groupDao.getGroupById(weibo.getFromId());
 			weibo.setFromPostfix(group.getName());
 		}
 	}
 
 	/**
 	 * @param groupDao
 	 * @param weibo
 	 * @throws SQLException
 	 */
 	public static void injectWeiboModelWithFrom(GroupDao groupDao, Weibo weibo) throws SQLException {
 		if (weibo.isFromGroup()) {
 			Group group = groupDao.getGroupById(weibo.getFromId());
 			weibo.setFromPostfix(group.getName());
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param weiboDao
 	 * @param groupDao
 	 * @param replyId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static void injectWeiboReplyWithSenderRealNameAndFrom(PeopleDao peopleDao, GroupDao groupDao, WeiboReply reply) throws SQLException {
 		People people = peopleDao.getPeopleById(reply.getSenderId());
 		reply.setSenderRealName(people.getRealName());
 		if (reply.isFromGroup()) {
 			Group group = groupDao.getGroupById(reply.getFromId());
 			reply.setFromPostfix(group.getName());
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param peoples
 	 * @param fromPeopleId
 	 * @throws SQLException
 	 */
 	public static void injectPeoplesWithVisitorRelation(PeopleDao peopleDao, List<People> peoples, Long fromPeopleId) throws SQLException {
 		if (CollectionKit.isEmpty(peoples)) {
 			return;
 		}
 		Set<Long> peopleIdSet = new HashSet<Long>(peoples.size());
 		for (People people : peoples) {
 			if (people.getId() != fromPeopleId) {
 				peopleIdSet.add(people.getId());
 			}
 		}
 		if (CollectionKit.isEmpty(peopleIdSet)) {
 			return;
 		}
 		PeopleRelationPage page = new PeopleRelationPage();
 		page.setToPeopleIds(new ArrayList<Long>(peopleIdSet));
 		page.setFromPeopleId(fromPeopleId);
 		List<PeopleRelation> relations = peopleDao.getPeopleRelationsByPage(page);
 		if (CollectionKit.isNotEmpty(relations)) {
 			Map<Long, PeopleRelation> relationMap = new HashMap<Long, PeopleRelation>();
 			for (PeopleRelation relation : relations) {
 				relationMap.put(relation.getToPeopleId(), relation);
 			}
 			for (People people : peoples) {
 				PeopleRelation relation = relationMap.get(people.getId());
 				if (relation == null) {
 					continue;
 				}
 				if (relation.isFollowing()) {
 					people.setFollowing();
 				}
 			}
 		}
 	}
 
 	public static void injectPeoplesWithSelf(List<People> peoples, long loginPeopleId) {
 		for (People people : peoples) {
 			if (loginPeopleId == people.getId()) {
 				people.setSelf(true);
 			}
 		}
 	}
 
 	public static void injectPeopleWithSelf(People people, long loginPeopleId) {
 		if (loginPeopleId == people.getId()) {
 			people.setSelf(true);
 		}
 	}
 
 	public static void injectPeopleWithVisitorRelation(PeopleDao peopleDao, People people, Long fromPeopleId) throws SQLException {
 		if (null == people) {
 			return;
 		}
 		PeopleRelation relation = peopleDao.getPeopleRelationByFromIdToId(fromPeopleId, people.getId());
 		if (null == relation) {
 			return;
 		}
 		if (relation.isFollowing()) {
 			people.setFollowing();
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param relations
 	 * @throws SQLException
 	 */
 	public static void injectPeopleRelationsWithFromRealName(PeopleDao peopleDao, List<PeopleRelation> relations) throws SQLException {
 		if (CollectionKit.isNotEmpty(relations)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(relations.size());
 			for (PeopleRelation relation : relations) {
 				peopleIds.add(relation.getFromPeopleId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (PeopleRelation relation : relations) {
 				relation.setFromPeopleRealName(map.get(relation.getFromPeopleId()));
 			}
 		}
 	}
 
 	public static void injectPeopleRelationsWithToRealName(PeopleDao peopleDao, List<PeopleRelation> relations) throws SQLException {
 		if (CollectionKit.isNotEmpty(relations)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(relations.size());
 			for (PeopleRelation relation : relations) {
 				peopleIds.add(relation.getToPeopleId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (PeopleRelation relation : relations) {
 				relation.setToPeopleRealName(map.get(relation.getToPeopleId()));
 			}
 
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param peopleJoinEvents
 	 * @throws SQLException
 	 */
 	public static void injectPeopleJoinEventsWithRealName(PeopleDao peopleDao, List<PeopleJoinEvent> joins) throws SQLException {
 		if (CollectionKit.isNotEmpty(joins)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(joins.size());
 			for (PeopleJoinEvent join : joins) {
 				peopleIds.add(join.getPeopleId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (PeopleJoinEvent join : joins) {
 				join.setPeopleRealName(map.get(join.getPeopleId()));
 			}
 		}
 
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param weibos
 	 */
 	public static void injectWeiboModelsWithPeople(PeopleDao peopleDao, List<? extends WeiboModel> weiboModels) throws SQLException {
 		if (CollectionKit.isEmpty(weiboModels)) {
 			return;
 		}
 
 		Set<Long> senderIds = new HashSet<Long>(weiboModels.size());
 		for (WeiboModel model : weiboModels) {
 			senderIds.add(model.getSenderId());
 			if (model.getQuote() != null) {
 				senderIds.add(model.getQuote().getSenderId());
 			}
 		}
 		if (CollectionKit.isEmpty(senderIds)) {
 			return;
 		}
 
 		List<People> peoples = peopleDao.getPeoplesByIds(new ArrayList<Long>(senderIds));
 		if (CollectionKit.isEmpty(peoples)) {
 			return;
 		}
 
 		Map<Long, People> peopleMap = convertToMap(peoples);
 		for (WeiboModel model : weiboModels) {
 			model.setPeople(peopleMap.get(model.getSenderId()));
 			if (model.getQuote() != null) {
 				model.getQuote().setPeople(peopleMap.get(model.getQuote().getSenderId()));
 			}
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param weibo
 	 * @throws SQLException
 	 */
 	public static void injectWeiboModelWithPeople(PeopleDao peopleDao, WeiboModel model) throws SQLException {
 		Set<Long> senderIds = new HashSet<Long>(2);
 		senderIds.add(model.getSenderId());
 		if (model.getQuote() != null) {
 			senderIds.add(model.getQuote().getSenderId());
 		}
 		if (CollectionKit.isEmpty(senderIds)) {
 			return;
 		}
 
 		List<People> peoples = peopleDao.getPeoplesByIds(new ArrayList<Long>(senderIds));
 		if (CollectionKit.isEmpty(peoples)) {
 			return;
 		}
 
 		Map<Long, People> peopleMap = convertToMap(peoples);
 		model.setPeople(peopleMap.get(model.getSenderId()));
 		if (model.getQuote() != null) {
 			model.getQuote().setPeople(peopleMap.get(model.getQuote().getSenderId()));
 		}
 	}
 
 	protected static Map<Long, People> convertToMap(List<People> peoples) {
 		Map<Long, People> peopleMap = new HashMap<Long, People>(peoples.size());
 		for (People people : peoples) {
 			peopleMap.put(people.getId(), people);
 		}
 		return peopleMap;
 	}
 
 	/**
 	 * @param weiboDao
 	 * @param weibos
 	 * @throws SQLException
 	 */
 	public static void injectWeiboModelsWithQuote(WeiboDao weiboDao, List<? extends WeiboModel> weiboModels) throws SQLException {
 		if (CollectionKit.isEmpty(weiboModels)) {
 			return;
 		}
 
 		Set<Long> quoteIds = new HashSet<Long>(weiboModels.size());
 		for (WeiboModel model : weiboModels) {
 			if (model.getQuoteWeiboId() > 0) {
 				quoteIds.add(model.getQuoteWeiboId());
 			}
 		}
 		if (CollectionKit.isEmpty(quoteIds)) {
 			return;
 		}
 
 		List<Weibo> quotes = weiboDao.getWeibosByIds(new ArrayList<Long>(quoteIds), true);
 		if (CollectionKit.isEmpty(quotes)) {
 			return;
 		}
 
 		Map<Long, Weibo> quoteMap = new HashMap<Long, Weibo>(quotes.size());
 		for (Weibo quote : quotes) {
 			quoteMap.put(quote.getId(), quote);
 		}
 		for (WeiboModel model : weiboModels) {
 			if (model.getQuoteWeiboId() > 0) {
 				Weibo quote = quoteMap.get(model.getQuoteWeiboId());
 				if (quote.getStatus() == Status.DELETE.getValue()) {
 					quote.setContent(null);
 				}
 				model.setQuote(quote);
 			}
 		}
 	}
 
 	public static void injectWeiboReplyWithReplyOrQuote(WeiboDao weiboDao, List<WeiboReply> weiboReplies) throws SQLException {
 		if (CollectionKit.isEmpty(weiboReplies)) {
 			return;
 		}
 
 		Set<Long> replyIds = new HashSet<Long>(weiboReplies.size());
 		Set<Long> quoteIds = new HashSet<Long>(weiboReplies.size());
 		for (WeiboReply reply : weiboReplies) {
 			if (reply.getReplyWeiboId() > 0) { // reply has higher priority to quote
 				replyIds.add(reply.getReplyWeiboId());
 			} else if (reply.getQuoteWeiboId() > 0) {
 				quoteIds.add(reply.getQuoteWeiboId());
 			}
 		}
 		if (CollectionKit.isEmpty(quoteIds) && CollectionKit.isEmpty(replyIds)) {
 			return; // return if no replyId and quoteId
 		}
 
 		Map<Long, Weibo> quoteMap = null;
 		if (CollectionKit.isNotEmpty(quoteIds)) {
 			List<Weibo> quotes = weiboDao.getWeibosByIds(new ArrayList<Long>(quoteIds), true);
 			if (CollectionKit.isNotEmpty(quotes)) {
 				quoteMap = new HashMap<Long, Weibo>(quotes.size());
 				for (Weibo quote : quotes) {
 					quoteMap.put(quote.getId(), quote);
 				}
 			}
 		}
 
 		Map<Long, WeiboReply> replyMap = null;
 		if (CollectionKit.isNotEmpty(replyIds)) {
 			List<WeiboReply> replies = weiboDao.getWeiboRepliesByIds(new ArrayList<Long>(replyIds), true);
 			if (CollectionKit.isNotEmpty(replies)) {
 				replyMap = new HashMap<Long, WeiboReply>(replies.size());
 				for (WeiboReply reply : replies) {
 					replyMap.put(reply.getId(), reply);
 				}
 			}
 		}
 
 		for (WeiboReply weiboReply : weiboReplies) {
 			if (weiboReply.getReplyWeiboId() > 0) { // reply has higher priority to quote
 				WeiboReply reply = replyMap.get(weiboReply.getReplyWeiboId());
 				if (reply.getStatus() == Status.DELETE.getValue()) {
 					reply.setContent(null);
 				}
 				weiboReply.setReply(reply);
 			} else if (weiboReply.getQuoteWeiboId() > 0) {
 				Weibo quote = quoteMap.get(weiboReply.getQuoteWeiboId());
 				if (quote.getStatus() == Status.DELETE.getValue()) {
 					quote.setContent(null);
 				}
 				weiboReply.setQuote(quote);
 			}
 		}
 
 	}
 
 	/**
 	 * @param weiboDao
 	 * @param weibo
 	 * @throws SQLException
 	 */
 	public static void injectWeiboModelWithQuote(WeiboDao weiboDao, Weibo weibo) throws SQLException {
 		Weibo quote = weiboDao.getWeiboById(weibo.getQuoteWeiboId());
 		if (quote.getStatus() == Status.DELETE.getValue()) {
 			quote.setContent(null);
 		}
 		weibo.setQuote(quote);
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param event
 	 * @throws SQLException
 	 */
 	public static void injectEventWithPeople(PeopleDao peopleDao, Event event) throws SQLException {
 		if (event == null) {
 			return;
 		}
 		event.setPeople(peopleDao.getPeopleById(event.getCreatorId()));
 	}
 
 	/**
 	 * @param groupDao
 	 * @param groups
 	 * @param loginId
 	 */
 	public static void injectGroupsWithJoined(GroupDao groupDao, List<Group> groups, long peopleId) throws SQLException {
 		if (CollectionKit.isEmpty(groups)) {
 			return;
 		}
 		Set<Long> groupIdSet = new HashSet<Long>(groups.size());
 		for (Group g : groups) {
 			groupIdSet.add(g.getId());
 		}
 		List<Long> joinedGroupIds = groupDao.getGroupIdsByJoinPeopleIdAndGroupIds(peopleId, new ArrayList<Long>(groupIdSet));
 		Set<Long> joinedGroupIdSet = new HashSet<Long>(joinedGroupIds);
 		for (Group g : groups) {
 			if (joinedGroupIdSet.contains(g.getId())) {
 				g.setJoined(true);
 			}
 		}
 	}
 
 	/**
 	 * @param groupDao
 	 * @param categorys
 	 * @throws SQLException
 	 */
 	public static void injectCategoriesWithPromotedGroups(GroupDao groupDao, List<Category> categorys) throws SQLException {
 		if (CollectionKit.isEmpty(categorys)) {
 			return;
 		}
 		List<Long> catIds = new ArrayList<Long>(categorys.size());
 		Map<Long, Category> catId2CatMap = new HashMap<Long, Category>(categorys.size());
 		for (Category cat : categorys) {
 			catIds.add(cat.getId());
 			catId2CatMap.put(cat.getId(), cat);
 		}
 		List<Group> promotedGroups = groupDao.getAllPromotedGroups(catIds);
 		if (CollectionKit.isNotEmpty(promotedGroups)) {
 			for (Group group : promotedGroups) {
 				Category category = catId2CatMap.get(group.getCategoryId());// 现在一个群只支持一个类目推荐位
 				if (category != null) {
 					category.addGroup(group);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param group
 	 */
 	public static void injectGroupWithCreator(PeopleDao peopleDao, Group group) throws SQLException {
 		if (group == null) {
 			return;
 		}
 		group.setCreator(peopleDao.getPeopleById(group.getCreatorId()));
 	}
 
 	/**
 	 * @param groupDao
 	 * @param categoryDao
 	 * @param group
 	 * @throws SQLException
 	 */
 	public static void injectGroupWithCategory(GroupDao groupDao, CategoryDao categoryDao, Group group) throws SQLException {
 		if (null == group) {
 			return;
 		}
 		List<GroupJoinCategory> joins = groupDao.getGroupJoinCategoriesByGroupIdAndStatus(group.getId(), Status.COMMON);
 		if (CollectionKit.isEmpty(joins)) {
 			return;
 		}
 		Category cat = categoryDao.getCategoryById(joins.get(0).getCategoryId());
 		group.setCategory(cat);
 	}
 
 }
