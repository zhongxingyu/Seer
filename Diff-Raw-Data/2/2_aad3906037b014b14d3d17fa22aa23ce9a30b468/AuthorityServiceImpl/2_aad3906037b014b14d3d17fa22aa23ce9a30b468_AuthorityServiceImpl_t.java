 package com.bigcay.exhubs.service.impl;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Service;
 
 import com.bigcay.exhubs.global.GlobalManager;
 import com.bigcay.exhubs.model.Group;
 import com.bigcay.exhubs.model.Role;
 import com.bigcay.exhubs.model.User;
 import com.bigcay.exhubs.repository.GroupRepository;
 import com.bigcay.exhubs.repository.RoleRepository;
 import com.bigcay.exhubs.repository.UserRepository;
 import com.bigcay.exhubs.service.AuthorityService;
 
 @Service
 public class AuthorityServiceImpl implements AuthorityService {
 
 	@Autowired
 	private UserRepository userRepository;
 
 	@Autowired
 	private RoleRepository roleRepository;
 
 	@Autowired
 	private GroupRepository groupRepository;
 
 	@Override
 	public User findUserById(Integer id) {
 		return userRepository.findOne(id);
 	}
 
 	@Override
 	public Role findRoleById(Integer id) {
 		return roleRepository.findOne(id);
 	}
 
 	@Override
 	public Group findGroupById(Integer id) {
 		return groupRepository.findOne(id);
 	}
 
 	@Override
 	public List<Role> findAllRoles() {
 		return roleRepository.findAll();
 	}
 
 	@Override
 	public User persist(User user) {
 		if (user.getId() != null) {
 			user.setUpdateDateTime(new Date());
 		}
 		return userRepository.save(user);
 	}
 
 	@Override
 	public Page<User> findPageableUsers(Integer pageNumber) {
 		PageRequest pageRequest = new PageRequest(pageNumber, GlobalManager.DEFAULT_PAGE_SIZE, Sort.Direction.ASC, "id");
 		return userRepository.findAll(pageRequest);
 	}
 
 	@Override
 	public boolean updateUserActiveFlag(Integer updateId, boolean activeFlag) {
 
 		User targetUser = userRepository.findOne(updateId);
 
 		if (targetUser == null || targetUser.getActiveFlag().booleanValue() == activeFlag) {
 			return false;
 		} else {
 			targetUser.setActiveFlag(activeFlag);
 			this.persist(targetUser);
 			return true;
 		}
 	}
 
 	@Override
 	public User findUserByUserId(String userId) {
 		return userRepository.findByUserId(userId);
 	}
 
 	@Override
 	public Set<Role> findRolesByGroupId(Integer groupId) {
 		return groupRepository.findOne(groupId).getRoles();
 	}
 
 	@Override
 	public List<Group> findAllGroups() {
 		return groupRepository.findAll();
 	}
 
 	@Override
 	public Group persist(Group group) {
 		return groupRepository.save(group);
 	}
 
 	@Override
 	public boolean deleteGroup(Integer groupId) {
 		// TO-DO - additional conditions here
 		groupRepository.delete(groupId);
		return true;
 	}
 
 }
