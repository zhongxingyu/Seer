 package com.cn.ld.modules.user.repository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.util.Assert;
 
 import com.cn.ld.modules.user.domain.User;
 
 @Repository
 public class UserRepository {
 	private List<User> userList = new ArrayList<User>();
 
 	public UserRepository() {
 		for (int i = 0; i < 5; i++) {
			this.userList.add(new User(i,"java小生"+i, "111111", 23 + 1, '男'));
 		}
 	}
 
 	public List<User> getUserList() {
 		return userList;
 	}
 
 	public void addUser(User user) {
 		Assert.notNull(user, "user is null !");
 		this.userList.add(user);
 	}
 
 	public void removeUser(User user) {
 		Assert.isTrue(this.userList.contains(user),
 				"can't remove an inexistent object !");
 		this.userList.remove(user);
 	}
 
 	public User getByName(String name) {
 		Assert.notNull(name);
 		User user = new User();
 		for (User u : userList) {
 			user = name.equals(u.getName()) ? u : user;
 		}
 		return user;
 	}
 }
