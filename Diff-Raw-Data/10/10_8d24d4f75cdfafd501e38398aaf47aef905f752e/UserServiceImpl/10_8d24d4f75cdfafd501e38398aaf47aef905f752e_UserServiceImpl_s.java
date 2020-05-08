 package com.wishop.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.wishop.dao.UserDAO;
 import com.wishop.model.User;
 import com.wishop.model.exceptions.WishopException;
 
 @Service
 @Component("userService")
 @Transactional(propagation=Propagation.REQUIRED, readOnly=true)
 public class UserServiceImpl extends BaseServiceImpl<UserDAO, User> implements UserService {
 	
 	private Md5PasswordEncoder passwordEncoder;
 	
 	@Autowired
 	public UserServiceImpl(UserDAO userDAO) {
 		super(userDAO);
 	}
 	
 	@Transactional(readOnly=false)
 	public void purge(User user) throws WishopException {
 		super.purge(user);
 	}
 	
 	public User getByEmail(String email) {
 		return this.getDao().getByEmail(email);
 	}
 
 	public List<User> getByLastName(String lastName) {
 		return this.getDao().getByLastName(lastName);
 	}
 
	public User getUserByIdAndPassword(User User, String rawPass) {
		return this.getDao().getUserByIdAndPassword(User.getId(), passwordEncoder.encodePassword(rawPass, User.getEmail()));
 	}
 
 	@Transactional(readOnly=false)
 	public void setAccountActive(User user, boolean isAccountActive) {
 		this.getDao().setAccountActive(user, isAccountActive);
 	}
 
 	@Override
 	public boolean isUniqueEmail(User user) {
 		return this.getDao().isUniqueEmail(user);
 	}
 }
