 package q.web.category;
 
 import java.sql.SQLException;
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
 		if (!context.isApiRequest()) {
 			long loginId = context.getCookiePeopleId();
 			if (loginId > 0) {
 				List<Group> groups = groupDao.getGroupsByJoinPeopleId(loginId);
 				context.setModel("groups", groups);
 			}
 			List<Group> recommendGroups = null;
 			GroupRecommendPage page = new GroupRecommendPage();
 			if (loginId > 0) {
 				page.setPeopleId(loginId);
 			}
 			recommendGroups = groupDao.getRecommendGroupsByPage(page);
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
