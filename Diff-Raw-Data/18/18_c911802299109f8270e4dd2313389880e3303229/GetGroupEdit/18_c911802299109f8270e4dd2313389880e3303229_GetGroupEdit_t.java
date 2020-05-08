 package q.web.group;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import q.dao.CategoryDao;
 import q.dao.DaoHelper;
 import q.dao.GroupDao;
 import q.domain.Category;
 import q.domain.Group;
 import q.util.IdCreator;
 import q.web.Resource;
 import q.web.ResourceContext;
import q.web.exception.PeopleNotPermitException;
 import q.web.exception.RequestParameterInvalidException;
 
 /**
  * @author Zhehao
  * @author seanlinwang
  * @date Feb 15, 2011
  * 
  */
 public class GetGroupEdit extends Resource {
 
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
 		List<Category> categorys = categoryDao.getAllCategorys();
 		context.setModel("categorys", categorys);
 		long loginId = context.getCookiePeopleId();
 		if (loginId > 0) {
 			List<Group> groups = groupDao.getGroupsByJoinPeopleId(loginId);
 			context.setModel("groups", groups);
 		}
 		long groupId = context.getResourceIdLong();
 		Group group = groupDao.getGroupById(groupId);
 		DaoHelper.injectGroupWithCategory(groupDao, categoryDao, group);
 		context.setModel("group", group);
 	}
 
	/*
	 * (non-Javadoc)
	 * 
 	 * @see q.web.Resource#validate(q.web.ResourceContext)
 	 */
 	@Override
 	public void validate(ResourceContext context) throws Exception {
 		long groupId = context.getResourceIdLong();
 		long loginId = context.getCookiePeopleId();
		if (IdCreator.isNotValidId(groupId)) {
 			throw new RequestParameterInvalidException("group:invalid");
 		}
 		Group group = groupDao.getGroupById(groupId);
 		if (group == null) {
 			throw new RequestParameterInvalidException("group:invalid");
 		}
 		if (group.getCreatorId() != loginId) {
			throw new PeopleNotPermitException("login:invalid");
 		}

 	}
 }
