 package q.web.category;
 
 import q.dao.CategoryDao;
 import q.dao.PeopleDao;
 import q.domain.Category;
 import q.domain.People;
 import q.util.IdCreator;
 import q.web.Resource;
 import q.web.ResourceContext;
 import q.web.exception.PeopleNotPermitException;
 
public class UpdateCategoryOrder extends Resource{
 	private CategoryDao categoryDao;
 
 	public void setCategoryDao(CategoryDao categoryDao) {
 		this.categoryDao = categoryDao;
 	}
 
 	private PeopleDao peopleDao;
 
 	public void setPeopleDao(PeopleDao peopleDao) {
 		this.peopleDao = peopleDao;
 	}
 
 	@Override
 	public void execute(ResourceContext context) throws Exception {
 		long categoryId = context.getLong("categoryId", 0);
 		int sortOrder = context.getInt("sortOrder", 0);
 		Category category = new Category();
 		category.setId(categoryId);
 		category.setSortOrder(sortOrder);
 		categoryDao.updateCategorySortOrder(category);
 	}
 
 	@Override
 	public void validate(ResourceContext context) throws Exception {
 		long loginPeopleId = context.getCookiePeopleId();
		if(IdCreator.isNotValidId(loginPeopleId)) {

 		}
 		People people = peopleDao.getPeopleById(loginPeopleId);
		if(people.isNotAdmin()) {
			throw new PeopleNotPermitException("people:无操作权限");
 		}
 	}
 
 }
