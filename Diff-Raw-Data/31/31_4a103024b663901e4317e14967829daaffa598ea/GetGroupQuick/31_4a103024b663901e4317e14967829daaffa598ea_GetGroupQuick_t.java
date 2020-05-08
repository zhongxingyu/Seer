 package q.web.group;
 
import java.util.List;

import q.dao.GroupDao;
import q.domain.Group;
 import q.web.Resource;
 import q.web.ResourceContext;
 
 public class GetGroupQuick extends Resource{
	private GroupDao groupDao;
 
	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}
 	@Override
 	public void execute(ResourceContext context) throws Exception {
		String method=context.getString("method");
		if(method.equals("getMyGroups")){
		List<Group>groups=groupDao.getGroupsByPeopleId(context.getResourceIdLong());
		context.setModel("groups", groups);
		}
 	}
 
 	@Override
 	public void validate(ResourceContext context) throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 }
