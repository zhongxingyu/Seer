 package com.youthministry.service.impl;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.youthministry.dao.EventDao;
 import com.youthministry.dao.GroupDao;
 import com.youthministry.domain.Group;
 import com.youthministry.genericdao.GenericDao;
 import com.youthministry.service.GroupService;
 
 @Transactional(readOnly=true)
 public class GroupServiceImpl implements GroupService {
 
 	private GroupDao groupDao;
 	
 	public GroupDao getGroupDao() {
 		return groupDao;
 	}
 
 	public void setGroupDao(GroupDao groupDao) {
 		this.groupDao = groupDao;
 	}
 		
 	@Transactional(readOnly=false)
 	@Override
 	public void addGroup(Group group) {
 		getGroupDao().create(group);
 	}
 
 	@Transactional(readOnly=false)
 	@Override
 	public void updateGroup(Group group) {
 		getGroupDao().update(group);
 	}
 
 	@Transactional(readOnly=false)
 	@Override
 	public void deleteGroup(Group group) {
 		getGroupDao().delete(group);
 	}
 
 	@Transactional(readOnly=true)
 	@Override
 	public Group getGroupById(Long id) {
		return (Group) getGroupDao().read(id);
 	}
 
 	@Override
 	public Group getGroupByName(String name) {
 		return getGroupDao().findByName(name).get(0);
 	}
 
 	@Override
 	public List<Group> getGroups() {
 		return getGroupDao().findAll();
 	}
 
 }
