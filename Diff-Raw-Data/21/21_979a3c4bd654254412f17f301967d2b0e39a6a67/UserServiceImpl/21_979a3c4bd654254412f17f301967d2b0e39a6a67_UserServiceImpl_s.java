 package com.cn.ld.modules.user.service.impl;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.cache.annotation.EnableCaching;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;
 
 import com.cn.ld.modules.user.domain.User;
 import com.cn.ld.modules.user.repository.UserRepository;
 import com.cn.ld.modules.user.service.UserService;
 
 @EnableCaching
 @Service
 public class UserServiceImpl implements UserService ,UserDetailsService{
 	
 	@Autowired
 	UserRepository userRepository ;
 	
	@Cacheable(value = "user" , condition="#name =='java小生1'" ,unless="#result.age == 24")
 	public User findByName(String name) {
 		return userRepository.getByName(name);
 	}
 
 	@Override
 	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
 	{
 		User user = userRepository.getByName(username);
 		return user ;
 	}
 
 	@Override
 	public void addUser(User user) {
 		this.userRepository.addUser(user);
 	}
 
 	@Override
 	public List<User> getUserList() {
 		return this.userRepository.getUserList();
 	}
 
 	@Override
 	public void removeUser(User u) {
 		u = this.userRepository.getByName(u.getName());
 		this.userRepository.removeUser(u);
 	}
 }
