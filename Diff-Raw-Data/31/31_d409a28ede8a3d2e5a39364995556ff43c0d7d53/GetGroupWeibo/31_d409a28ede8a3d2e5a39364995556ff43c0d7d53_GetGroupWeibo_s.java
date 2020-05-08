 /**
  *
  */
 package q.web.weibo;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 
 import q.dao.DaoHelper;
 import q.dao.FavoriteDao;
 import q.dao.GroupDao;
 import q.dao.PeopleDao;
 import q.dao.WeiboDao;
 import q.dao.page.WeiboPage;
 import q.domain.Weibo;
 import q.util.CollectionKit;
 import q.util.IdCreator;
 import q.web.Resource;
 import q.web.ResourceContext;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Feb 16, 2011
  * 
  */
 public class GetGroupWeibo extends Resource {
 	private GroupDao groupDao;
 
 	public void setGroupDao(GroupDao groupDao) {
 		this.groupDao = groupDao;
 	}
 
 	private WeiboDao weiboDao;
 
 	public void setWeiboDao(WeiboDao weiboDao) {
 		this.weiboDao = weiboDao;
 	}
 
 	private PeopleDao peopleDao;
 
 	public void setPeopleDao(PeopleDao peopleDao) {
 		this.peopleDao = peopleDao;
 	}
 
 	private FavoriteDao favoriteDao;
 
 	public void setFavoriteDao(FavoriteDao favoriteDao) {
 		this.favoriteDao = favoriteDao;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.web.Resource#execute(q.web.ResourceContext)
 	 */
 	@Override
 	public void execute(ResourceContext context) throws Exception {
 		long groupId = context.getResourceIdLong();
 		long loginPeopleId = context.getCookiePeopleId();
 		int size = context.getInt("size", 10);
 		long startId = context.getIdLong("startId", IdCreator.MAX_ID);
 		int type = context.getInt("type", 0);
 		int asc = 1;
 		WeiboPage page = new WeiboPage();
 		if (type == asc) { // 1 indicate asc
 			page.setDesc(false);
 		} else {
 			page.setDesc(true);
 		}
 		boolean hasPrev = false;
 		boolean hasNext = false;
 		int fetchSize = size + 1;
 		page.setSize(fetchSize);
 		page.setGroupId(groupId);
 		if (startId > 0) {
 			page.setStartId(startId);
 		}
		List<Long> weiboIds = weiboDao.getWeiboIdsByPage(page);
 		List<Weibo> weibos = weiboDao.getWeibosByIds(weiboIds, true);
 		if (CollectionKit.isNotEmpty(weibos)) {
 			if (weibos.size() == fetchSize) {
 				if (type == asc) { // more than one previous page
 					hasPrev = true;
 				} else { // more than one next page
 					hasNext = true;
 				}
 				weibos.remove(weibos.size() - 1);// remove last one
 			}
 			if (type == asc) { // this action from next page
 				hasNext = true;
 			} else if (startId != 999999999999999999L) {// this action from previous page
 				hasPrev = true;
 			}
 			if (type == asc) { // reverse asc to desc
 				Weibo[] array = weibos.toArray(new Weibo[weibos.size()]);
 				CollectionUtils.reverseArray(array);
 				weibos = Arrays.asList(array);
 			}
 			DaoHelper.injectWeiboModelsWithQuote(weiboDao, weibos);
 			DaoHelper.injectWeiboModelsWithPeople(peopleDao, weibos);
 			DaoHelper.injectWeiboModelsWithFrom(groupDao, weibos);
 			if (loginPeopleId > 0) {
 				DaoHelper.injectWeiboModelsWithFavorite(favoriteDao, weibos, loginPeopleId);
 			}
 		}
 		Map<String, Object> api = new HashMap<String, Object>();
 		if (CollectionKit.isNotEmpty(weibos)) {
 			api.put("weibos", weibos);
 		}
 		api.put("hasPrev", hasPrev);
 		api.put("hasNext", hasNext);
 		context.setModel("api", api);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.web.Resource#validate(q.web.ResourceContext)
 	 */
 	@Override
 	public void validate(ResourceContext context) throws Exception {
 
 	}
 
 }
