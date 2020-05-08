 /**
  *
  */
 package q.web.people;
 
 import java.util.List;
 
 import q.dao.PeopleDao;
 import q.dao.page.PeoplePage;
 import q.domain.People;
 import q.util.IdCreator;
 import q.web.Resource;
 import q.web.ResourceContext;
 import q.web.exception.PeopleNotLoginException;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Feb 23, 2011
 * 
  */
 public class GetFollowingIndex extends Resource {
 	private PeopleDao peopleDao;
 
 	public void setPeopleDao(PeopleDao peopleDao) {
 		this.peopleDao = peopleDao;
 	}
 
 	/*
 	 * (non-Javadoc)
	 * 
 	 * @see q.web.Resource#execute(q.web.ResourceContext)
 	 */
 	@Override
 	public void execute(ResourceContext context) throws Exception {
 		long loginPeopleId = context.getCookiePeopleId();
 
 		if (!context.isApiRequest()) {
 			People people = peopleDao.getPeopleById(loginPeopleId);
 			context.setModel("people", people);
 			PeoplePage page = new PeoplePage();
 			page.setSize(9);
 			List<People> recommendPeoples = peopleDao.getPeoplesByPage(page);
 			context.setModel("recommendPeoples", recommendPeoples);
 		} else {
 		}
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
