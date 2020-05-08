 /**
  * 
  */
 package com.foreveross.chameleon.phone.modules;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.foreveross.chameleon.Application;
 import com.foreveross.chameleon.TmpConstants;
 import com.foreveross.chameleon.push.cubeparser.type.AbstractMessage;
 import com.foreveross.chameleon.push.cubeparser.type.CommonModuleMessage;
 import com.foreveross.chameleon.push.cubeparser.type.ModuleMessage;
 import com.foreveross.chameleon.push.cubeparser.type.NoticeModuleMessageStub;
 import com.foreveross.chameleon.push.cubeparser.type.SystemMessage;
 import com.foreveross.chameleon.store.core.StaticReference;
 import com.foreveross.chameleon.util.Preferences;
 
 /**
  * [消息界面 模型-聚合根]<BR>
  * [功能详细描述]
  * 
  * @author 冯伟立
  * @version [CubeAndroid, 2013-10-11]
  */
 public class MessageFragmentModel {
 
 	private final static Logger log = LoggerFactory
 			.getLogger(MessageFragmentModel.class);
 	/**
 	 * [未读消息总条数]
 	 */
 	private int unreadMsgCount = 0;
 	/**
 	 * [消息总条数]
 	 */
 	private int msgCount = 0;
 	/**
 	 * [是否处于编辑状态 ]
 	 */
 	private boolean isEditing = false;
 	/**
 	 * [是否处于选中状态]
 	 */
 	private boolean selected = false;
 
 	/**
 	 * [组模型]<BR>
 	 * 1.groupName--->MsgGroup
 	 */
 	private Map<String, MsgModel> msgSortMap = new TreeMap<String, MsgModel>();
 
 	/**************************** 本模型为单例 ******************************************/
 	private static MessageFragmentModel messageFragmentModel = new MessageFragmentModel();
 
 	private MessageFragmentModel() {
 	}
 
 	public static MessageFragmentModel instance() {
 		return messageFragmentModel;
 	}
 
 	/**
 	 * [初始化]<BR>
 	 * 1.先异步取每组前十条<BR>
 	 * 2.再后续取以后的记录<BR>
 	 */
 	public synchronized void init() {
 		msgSortMap.clear();
 		cacheList.clear();
 		String userName = Preferences.getUserName(Application.sharePref);
 		List<AbstractMessage<?>> queryResult = new ArrayList<AbstractMessage<?>>();
 		try {
 			queryResult.addAll(StaticReference.defMf
 					.queryBuilder(SystemMessage.class).orderBy("sendTime", true).where().eq("userName", userName).
 					query());
 			queryResult.addAll(StaticReference.defMf
 					.queryBuilder(CommonModuleMessage.class)
 					.orderBy("sendTime", true).where().eq("userName", userName).
 					query());
 			queryResult.addAll(StaticReference.defMf
 					.queryBuilder(NoticeModuleMessageStub.class)
 					.orderBy("sendTime", true).where().eq("userName", userName).
 					query());
 		} catch (SQLException e) {
 			log.error("query  chanmeleonMessage error!", e);
 		}
 		addMessages(queryResult);
 	}
 
 	/**
 	 * [包含的所有组]
 	 */
 	private List<MsgModel> cacheList = new ArrayList<MsgModel>();
 
 	/**
 	 * [得到所有组，以List返回]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @return 2013-10-12 上午11:10:30
 	 */
 	public List<MsgModel> getMessageData() {
 		cacheList.clear();
 		cacheList.addAll(msgSortMap.values());
 		return cacheList;
 	}
 
 	/**
 	 * [计算已读、未读消息总条数]<BR>
 	 * [功能详细描述] 2013-10-12 上午11:40:29
 	 */
 	private void coupMsgInfo() {
 		unreadMsgCount = 0;
 		msgCount = 0;
 		Collection<MsgModel> msgModels = msgSortMap.values();
 		for (MsgModel msgModel : msgModels) {
 			unreadMsgCount += msgModel.getUnreadMsgCount();
 			msgCount += msgModel.getMsgCount();
 			msgModel.notifyUnreadChange();
 		}
 	}
 
 	private synchronized void addMessageInner(AbstractMessage<?> am) {
 		/** 将消息分组分类 */
 		MsgModel msgModel = msgSortMap.get(am.getGroupBelong());
 		if (msgModel == null) {
 			msgModel = new MsgModel();
 			msgModel.setGroupName(am.getGroupBelong());
 			if (am instanceof ModuleMessage) {
 				msgModel.setIdentifier(ModuleMessage.class.cast(am)
 						.getIdentifier());
 			}
 			msgModel.setEditable(isEditing);
 			msgSortMap.put(am.getGroupBelong(), msgModel);
 			cacheList.add(0, msgModel);
 			setExpendFirstMsgModel();
 		}
 		am.setEditable(isEditing);
 		am.setSelected(selected);
 		msgModel.addMessage(am);
 	}
 
 	public synchronized  void addMessage(AbstractMessage<?> am) {
 		addMessageInner(am);
 		coupMsgInfo();
 		notifyContentChange();
 	}
 
 	public Boolean getFirstMsgModelExpandState() {
 		if (cacheList.size() == 0) {
 			return null;
 		}
 		return cacheList.get(0).isExpend();
 	}
 
 	/**
 	 * [设置第一个为展开 ，其它合并]<BR>
 	 * [功能详细描述] 2013-10-12 上午11:43:48
 	 */
 	public synchronized void setExpendFirstMsgModel() {
 		for (int i = 0; i < cacheList.size(); i++) {
 			if (i == 0) {
 				cacheList.get(i).setExpend(true);
 				continue;
 			}
 			cacheList.get(i).setExpend(false);
 		}
 		notifyContentChange();
 	}
 
 	public synchronized void addMessages(List<AbstractMessage<?>> ams) {
 		for (AbstractMessage<?> am : ams) {
 			this.addMessageInner(am);
 		}
 		coupMsgInfo();
 		notifyContentChange();
 	}
 
 	private MsgModel getNoticeMsgModel() {
 		return msgSortMap.get("公告");
 	}
 
 	/**
 	 * [删除id对应的信息]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param messageIds
 	 *            2013-10-12 上午11:45:13
 	 */
 	public synchronized void removeNotices(Set<String> messageIds) {
 		MsgModel msgModel = getNoticeMsgModel();
 
 		if (msgModel != null) {
 			msgModel.removeMessages(messageIds);
 			coupMsgInfo();
			if (!msgModel.isEmpty()) {
 				msgSortMap.remove(msgModel.getGroupName());
 				cacheList.remove(msgModel);
 			}
 			notifyContentChange();
 		} else {
 			log.error("remove notices from db error!");
 		}
 	}
 
 	/**
 	 * [是否有选择项]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @return 2013-10-12 上午11:31:21
 	 */
 	public boolean hasSelected() {
 		for (MsgModel msgModel : cacheList) {
 			if (msgModel.hasMessageSelected()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * [读取消息]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param am
 	 *            2013-10-12 上午11:49:06
 	 */
 	public synchronized void readMsg(AbstractMessage<?> am) {
 		if (!am.isHasRead()) {
 			MsgModel msgModel = msgSortMap.get(am.getGroupBelong());
 			msgModel.readMessage(am);
 			coupMsgInfo();
 			notifyContentChange();
 		}
 	}
 
 	/**
 	 * [删除选中消息]<BR>
 	 * [功能详细描述] 2013-10-12 上午11:49:23
 	 */
 	public synchronized void deleteSelectedMsg() {
 		List<MsgModel> selectedMsgModels = getSelectedMsgModels();
 		for (MsgModel msgModel : selectedMsgModels) {
 			msgModel.removeSelectedMessages();
 		}
 		coupMsgInfo();
 		for (MsgModel msgModel : selectedMsgModels) {
 			if (msgModel.isEmpty()) {
 				msgSortMap.remove(msgModel.getGroupName());
 				cacheList.remove(msgModel);
 			}
 		}
 		notifyContentChange();
 	}
 
 	/**
 	 * [标识选中消息为已读]<BR>
 	 * [功能详细描述] 2013-10-12 上午11:49:36
 	 */
 	public synchronized void markSelectedMsgRead() {
 		List<MsgModel> selectedMsgModels = getSelectedMsgModels();
 		for (MsgModel msgModel : selectedMsgModels) {
 			msgModel.markSelectedMessages();
 		}
 		coupMsgInfo();
 		notifyContentChange();
 	}
 
 	/**
 	 * [读取消息]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param am
 	 *            2013-10-12 上午11:49:06
 	 */
 	public synchronized void readMessage(int groupPosition, int childPosition) {
 		MsgModel msgModel = cacheList.get(groupPosition);
 		msgModel.readMessage(childPosition);
 		coupMsgInfo();
 		notifyContentChange();
 	}
 
 	/**
 	 * [选中消息]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param groupPosition
 	 * @param childPosition
 	 * @param selected
 	 *            2013-10-12 上午11:50:23
 	 */
 	public synchronized void selectMessage(int groupPosition, int childPosition,
 			boolean selected) {
 		AbstractMessage<?> am = cacheList.get(groupPosition).getMsgList()
 				.get(childPosition);
 		am.setSelected(selected);
 		notifyContentChange();
 	}
 
 	/**
 	 * [删除消息组]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param groupName
 	 * @return 2013-10-12 上午11:50:38
 	 */
 	public synchronized boolean removeGroup(String groupName) {
 		MsgModel model = msgSortMap.remove(groupName);
 		model.removeAll();
 		cacheList.remove(model);
 		coupMsgInfo();
 		notifyContentChange();
 		return true;
 	}
 
 	/**
 	 * [只读对象]<BR>
 	 * //TODO 添加只读限定
 	 * 
 	 * @param groupPosition
 	 * @param childPosition
 	 * @return 2013-10-12 上午10:09:34
 	 */
 	public AbstractMessage<?> getReadOnlyMessage(int groupPosition,
 			int childPosition) {
 		AbstractMessage<?> am = cacheList.get(groupPosition).getMsgList()
 				.get(childPosition);
 		return am;
 	}
 
 	public synchronized void readAllRecordsByModule(String identifier) {
 		MsgModel msgModel = msgSortMap.get(identifier);
 		if (msgModel != null) {
 			msgModel.readAllMessages();
 			coupMsgInfo();
 			notifyContentChange();
 		}
 	}
 
 	public synchronized void readNotices(Set<String> noticeIds) {
 		MsgModel msgModel = getNoticeMsgModel();
 		if (msgModel != null) {
 			msgModel.readMessages(noticeIds);
 			coupMsgInfo();
 			notifyContentChange();
 		}
 
 	}
 
 	public synchronized void readNotice(String noticeId) {
 		Set<String> ids = new HashSet<String>();
 		ids.add(noticeId);
 		readNotices(ids);
 	}
 
 	/**
 	 * [得到有项目选中的MsgModel]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @return 2013-10-12 上午11:51:32
 	 */
 	private List<MsgModel> getSelectedMsgModels() {
 		List<MsgModel> list = new ArrayList<MsgModel>();
 		for (MsgModel msgModel : cacheList) {
 			if (msgModel.hasMessageSelected()) {
 				list.add(msgModel);
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * [设置选中]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param selected
 	 *            2013-10-12 上午11:52:37
 	 */
 	public synchronized  void setSelected(boolean selected) {
 		this.selected = selected;
 		for (MsgModel msgModel : cacheList) {
 			msgModel.setSelectAll(selected);
 		}
 		notifyContentChange();
 	}
 
 	/**
 	 * [设置删除]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param isEditing
 	 *            2013-10-12 上午11:52:46
 	 */
 	public synchronized void setEditing(boolean isEditing) {
 		this.isEditing = isEditing;
 		for (MsgModel msgModel : cacheList) {
 			msgModel.setEditable(isEditing);
 		}
 		notifyContentChange();
 	}
 
 	private transient List<ContentChangeListener> contentChangeListeners = new 
 
 ArrayList<ContentChangeListener>();
 
 	public void addContentChangeListener(
 			ContentChangeListener contentChangeListener) {
 		contentChangeListeners.add(contentChangeListener);
 	}
 
 	/**
 	 * [改变消息最新条数]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param unreadCount
 	 *            2013-10-11 下午4:27:52
 	 */
 
 	public void notifyContentChange() {
 		for (ContentChangeListener contentChangeListener : contentChangeListeners) {
 			if (contentChangeListener != null) {
 				contentChangeListener.onContentChange(unreadMsgCount, msgCount,
 						getMessageData());
 			}
 		}
 		changeMessageRecordCount(unreadMsgCount);
 	}
 
 	private void changeMessageRecordCount(int unreadCount) {
 		CubeModule cubeModule = CubeModuleManager.getInstance()
 				.getModuleByIdentify(TmpConstants.MESSAGE_RECORD_IDENTIFIER);
 		if (cubeModule != null) {
 			cubeModule.setMsgCount(unreadCount);
 		}
 	}
 
 	public void removeContentChangeListner(
 			ContentChangeListener contentChangeListener) {
 		contentChangeListeners.remove(contentChangeListener);
 
 	}
 
 	public int getUnreadMsgCount() {
 		return unreadMsgCount;
 	}
 
 	public int getMsgCount() {
 		return msgCount;
 	}
 
 	public boolean isEditing() {
 		return isEditing;
 	}
 
 	public boolean isSelected() {
 		return selected;
 	}
 
 }
