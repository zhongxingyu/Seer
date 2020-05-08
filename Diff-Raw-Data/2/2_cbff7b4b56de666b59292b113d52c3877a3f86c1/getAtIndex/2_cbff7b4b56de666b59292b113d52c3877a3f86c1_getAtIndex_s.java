 /**
  * 
  */
 package q.web.weibo;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import q.biz.SearchService;
 import q.dao.WeiboDao;
 import q.dao.page.FavoritePage;
 import q.domain.Weibo;
 import q.util.CollectionKit;
 import q.util.IdCreator;
 import q.web.Resource;
 import q.web.ResourceContext;
 import q.web.exception.RequestParameterInvalidException;
 
 /**
  * @author seanlinwang at gmail dot com
  * @date May 14, 2011
  * 
  */
 public class getAtIndex extends Resource {
 	private WeiboDao weiboDao;
 
 	public void setWeiboDao(WeiboDao weiboDao) {
 		this.weiboDao = weiboDao;
 	}
 
 	private SearchService searchService;
 
 	public void setSearchService(SearchService searchService) {
 		this.searchService = searchService;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.web.Resource#execute(q.web.ResourceContext)
 	 */
 	@Override
 	public void execute(ResourceContext context) throws Exception {
 		String username = context.getLoginCookie().getUsername();
 		List<Long> bs = searchService.searchWeibo("@" + username);
 		int size = context.getInt("size", 10);
 		long startId = context.getIdLong("startId", IdCreator.MAX_ID);
 		int type = context.getInt("type", 0);
 		int asc = 1;
 		FavoritePage page = new FavoritePage();
 		if (type == asc) { // 1 indicate asc
 			page.setDesc(false);
 		} else {
 			page.setDesc(true);
 		}
 		boolean hasPrev = false;
 		boolean hasNext = false;
 		Map<String, Object> api = new HashMap<String, Object>();
 		if (CollectionKit.isNotEmpty(bs)) {
 			List<Weibo> weibos = weiboDao.getWeibosByIds(bs, true);
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
		if (context.getCookiePeopleId() > 0) {
 			throw new RequestParameterInvalidException("login:invalid");
 		}
 	}
 
 }
