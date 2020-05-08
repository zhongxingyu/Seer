 /**
  *
  */
 package q.web.people;
 
import java.util.ArrayList;
 import java.util.List;
 
 import q.dao.PeopleDao;
 import q.dao.page.PeoplePage;
import q.dao.page.PeopleRelationPage;
 import q.domain.People;
import q.domain.PeopleRelationStatus;
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
			List<Long> peopleIds = new ArrayList<Long>();
			for (People p : recommendPeoples) {
				peopleIds.add(p.getId());
			}
			PeopleRelationPage prpage = new PeopleRelationPage();
			prpage.setStatus(PeopleRelationStatus.FOLLOWING);
			List<Long> follingIds = new ArrayList<Long>();
			follingIds = peopleDao.getAllFollowingId(loginPeopleId);
			for(int i=peopleIds.size() - 1; i>=0; i--) { //must delete by desc, or it'll be wrong
				long pid = ((People)recommendPeoples.get(i)).getId();
				if(follingIds.contains(pid)) {
					recommendPeoples.remove(i);
					follingIds.remove(pid);
				}

			}
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
