 package q.web.group;
 
 import java.util.List;
 
 import q.dao.DaoHelper;
 import q.dao.EventDao;
 import q.dao.GroupDao;
 import q.dao.PeopleDao;
 import q.dao.WeiboDao;
 import q.domain.Event;
 import q.domain.Group;
 import q.domain.People;
 import q.domain.Weibo;
 import q.util.CollectionKit;
 import q.web.Resource;
 import q.web.ResourceContext;
 
 public class GetGroupFeedFrame extends Resource {
 	private GroupDao groupDao;
 
 	public void setGroupDao(GroupDao groupDao) {
 		this.groupDao = groupDao;
 	}
 
 	private PeopleDao peopleDao;
 
 	public void setPeopleDao(PeopleDao peopleDao) {
 		this.peopleDao = peopleDao;
 	}
 
 	private EventDao eventDao;
 
 	public void setEventDao(EventDao eventDao) {
 		this.eventDao = eventDao;
 	}
 	
 	private WeiboDao weiboDao;
 
 	public void setWeiboDao(WeiboDao weiboDao) {
 		this.weiboDao = weiboDao;
 	}
 	
 	@Override
 	public void execute(ResourceContext context) throws Exception {
 		long loginPeopleId = context.getCookiePeopleId();
 		People people = this.peopleDao.getPeopleById(loginPeopleId);
 		context.setModel("people", people);
 		
		context.setModel("allGroups", this.groupDao.getAllGroups());
 		List<Long> groupIds = this.groupDao.getGroupIdsByPeopleId(loginPeopleId);
 		if (CollectionKit.isNotEmpty(groupIds)) {
 			List<Group> groups = this.groupDao.getGroupsByIds(groupIds);
 			context.setModel("groups", groups);
 			
 			List<Event> newEvents = this.eventDao.getEventsByGroupIds(groupIds, 4, 0);
 			context.setModel("newEvents", newEvents);
 			
 			List<Long> newPeopleIds = this.groupDao.getPeopleIdsByGroupIds(groupIds, 3, 0);
 			List<People> newPeoples = this.peopleDao.getPeoplesByIds(newPeopleIds);
 			context.setModel("newPeoples", newPeoples);
 			
 			List<Long> hotPeopleIds = this.groupDao.getHotGroupPeopleIds(groupIds, 3, 0);
 			List<People> hotPeoples = this.peopleDao.getPeoplesByIds(hotPeopleIds);
 			context.setModel("hotPeoples", hotPeoples);
 			
 			List<Weibo> hotWeibos = this.weiboDao.getHotWeibosByGroupIds(groupIds, 3, 0);
 			DaoHelper.injectWeiboModelsWithPeople(peopleDao, hotWeibos);
 			context.setModel("hotWeibos", hotWeibos);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see q.web.Resource#validate(q.web.ResourceContext)
 	 */
 	@Override
 	public void validate(ResourceContext context) throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 }
