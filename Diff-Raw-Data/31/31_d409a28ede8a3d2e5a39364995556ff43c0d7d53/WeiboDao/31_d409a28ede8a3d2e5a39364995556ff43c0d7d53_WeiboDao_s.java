 /**
  * 
  */
 package q.dao;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import q.dao.page.WeiboPage;
 import q.dao.page.WeiboReplyPage;
 import q.domain.Weibo;
 import q.domain.WeiboJoinGroup;
 import q.domain.WeiboReply;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Feb 21, 2011
  * 
  */
 public interface WeiboDao {
 
 	/**
 	 * @param weibo
 	 * @throws SQLException
 	 */
 	void addWeibo(Weibo weibo) throws SQLException;
 
 	/**
 	 * @param weiboId
 	 * @param groupId
 	 * @throws SQLException
 	 */
 	void addWeiboJoinGroup(long weiboId, long senderId, long groupId) throws SQLException;
 
 	/**
 	 * @param page
 	 * @return
 	 * @throws SQLException
 	 */
	List<Long> getWeiboIdsByPage(WeiboPage page) throws SQLException;
 
 	/**
 	 * @param page
 	 * @return
 	 * @throws SQLException
 	 */
 	List<Weibo> getWeibosByPage(WeiboPage page) throws SQLException;
 
 	/**
 	 * @param page
 	 * @return
 	 * @throws SQLException
 	 */
 	List<Weibo> getFollowingWeibosByPage(WeiboPage page) throws SQLException;
 
 	/**
 	 * @param weiboId
 	 * @return
 	 */
 	Weibo getWeiboById(long weiboId) throws SQLException;
 
 	/**
 	 * @param page
 	 * @return
 	 */
 	List<WeiboReply> getWeiboRepliesByPage(WeiboReplyPage page) throws SQLException;
 
 	/**
 	 * @param reply
 	 */
 	void addWeiboReply(WeiboReply reply) throws SQLException;
 
 	/**
 	 * @param peopleId
 	 * @return
 	 */
 	int getPeopleWeiboNumByPeopleId(long peopleId) throws SQLException;
 
 	/**
 	 * @param replyWeiboId
 	 * @return
 	 * @throws SQLException
 	 */
 	WeiboReply getWeiboReplyById(long weiboReplyId) throws SQLException;
 
 	/**
 	 * @param id
 	 * @param groupId
 	 * @return
 	 * @throws SQLException
 	 */
 	WeiboJoinGroup getWeiboJoinGroupByWeiboIdAndGroupId(long id, long groupId) throws SQLException;
 
 	/**
 	 * @param id
 	 * @return
 	 */
 	WeiboJoinGroup getWeiboJoinGroupByWeiboId(long weiboId) throws SQLException;
 
 	/**
 	 * @param ids
 	 * @param needDesc
 	 * @return
 	 * @throws SQLException
 	 */
 	List<Weibo> getWeibosByIds(List<Long> ids, boolean needDesc) throws SQLException;
 
 	/**
 	 * @param sourceReplyIds
 	 * @param b
 	 * @return
 	 */
 	List<WeiboReply> getWeiboRepliesByIds(List<Long> replyIds, boolean needDesc) throws SQLException;
 
 	/**
 	 * @param i
 	 * @return
 	 */
 	List<Weibo> getHotWeibos(int i) throws SQLException;
 
 	/**
 	 * @param groupIds
 	 * @param limit
 	 * @param start
 	 * @return
 	 */
 	List<Weibo> getHotWeibosByGroupIds(List<Long> groupIds, int limit, int start) throws SQLException;
 
 	/**
 	 * @param groupId
 	 * @param limit
 	 * @param start
 	 * @return
 	 */
 	List<Weibo> getHotGroupWeibosByGroupId(long groupId, int limit, int start) throws SQLException;
 
 	/**
 	 * @param senderId
 	 * @param weiboId
 	 * @return
 	 */
 	int deleteWeiboBySenderIdAndWeiboId(long senderId, long weiboId) throws SQLException;
 
 	/**
 	 * @param quoteWeiboId
 	 */
 	int incrWeiboRetweetNumByWeiboId(long quoteWeiboId) throws SQLException;
 
 	/**
 	 * @param quoteWeiboId
 	 */
 	int decrWeiboRetweetNumberByWeiboId(long quoteWeiboId) throws SQLException;
 
 	/**
 	 * @param quoteWeiboId
 	 * @return The number of rows effected.
 	 */
 	int incrWeiboReplyNumByReplyId(long quoteWeiboId) throws SQLException;
 
 	/**
 	 * @param loginId
 	 * @param id
 	 * @return
 	 */
 	int deleteWeiboReplyBySenderIdAndReplyId(long loginId, long id) throws SQLException;
 
 	/**
 	 * @param quoteWeiboId
 	 * @return 
 	 */
 	int decrWeiboReplyNumByWeiboId(long quoteWeiboId) throws SQLException;
 
 }
