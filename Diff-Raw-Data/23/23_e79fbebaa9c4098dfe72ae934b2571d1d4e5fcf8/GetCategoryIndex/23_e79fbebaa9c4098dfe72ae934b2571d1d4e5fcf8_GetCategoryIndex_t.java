 package q.web.category;
 
 import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
 import java.util.List;
 
 import q.dao.CategoryDao;
 import q.dao.DaoHelper;
 import q.dao.GroupDao;
 import q.dao.page.GroupRecommendPage;
 import q.domain.Category;
 import q.domain.Group;
 import q.web.Resource;
 import q.web.ResourceContext;
 
 /**
  * @author Zhehao
  * @author seanlinwang
  * @date Feb 15, 2011
 *
  */
 public class GetCategoryIndex extends Resource {
 
 	private CategoryDao categoryDao;
 
 	public void setCategoryDao(CategoryDao categoryDao) {
 		this.categoryDao = categoryDao;
 	}
 
 	private GroupDao groupDao;
 
 	public void setGroupDao(GroupDao groupDao) {
 		this.groupDao = groupDao;
 	}
 
 	@Override
 	public void execute(ResourceContext context) throws SQLException {
 		List<Category> categories = categoryDao.getAllCategorys();
 		DaoHelper.injectCategoriesWithPromotedGroups(groupDao, categories);
		List<Long> groupIds = new ArrayList<Long>();
 		if (!context.isApiRequest()) {
 			long loginId = context.getCookiePeopleId();
 			if (loginId > 0) {
 				List<Group> groups = groupDao.getGroupsByJoinPeopleId(loginId);
 				context.setModel("groups", groups);
				for(Group group : groups) {
					groupIds.add(group.getId());
				}
 			}
 			List<Group> recommendGroups = null;
 			GroupRecommendPage page = new GroupRecommendPage();
 			if (loginId > 0) {
 				page.setPeopleId(loginId);
 			}
 			recommendGroups = groupDao.getRecommendGroupsByPage(page);
			if(groupIds.size() > 0) {
				for (int i = recommendGroups.size() - 1; i >= 0 ; i--) { //must delete by desc
					Group group = (Group)recommendGroups.get(i);
					if(groupIds.contains(group.getId())) {
						recommendGroups.remove(i);
					}
				}
			}
 			context.setModel("recommendGroups", recommendGroups);
 			context.setModel("cats", categories);
 		} else {
 			context.setModel("cats", categories);
 		}
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
