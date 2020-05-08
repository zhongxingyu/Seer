 package com.msc.dao.impl;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.msc.dao.UserDao;
 import com.msc.domain.User;
 
 @Repository
 @Transactional
 public class UserDaoImpl implements UserDao
 {
 	@Autowired(required = true)
 	private SessionFactory sessionFactory;
 	
 	@Override
 	public User findById(Long id)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<User> findAll()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void save(User user)
 	{
		try
		{
			this.sessionFactory.getCurrentSession().save(user);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
 	}
 
 	@Override
 	public boolean doesUsernameExist(String username)
 	{
		int i = this.sessionFactory.getCurrentSession().createQuery("from User u where u.username = ?")
 			.setString(0, username)
 			.list()
			.size();
		System.out.println("i " + i);
		return i > 0 ? true : false;
		
 	}
 
 	@Override
 	public boolean doesEmailExist(String email)
 	{
 		return this.sessionFactory.getCurrentSession().createQuery("from User u where u.email = ?")
 			.setString(0, email)
 			.list()
 			.size() > 0 ? true : false;
 	}
 
 }
