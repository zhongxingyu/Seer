 package org.pcmiel.blog.dao;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.pcmiel.blog.entity.Authority;
 import org.pcmiel.blog.entity.BlogUser;
 import org.pcmiel.blog.entity.Post;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.stereotype.Repository;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.ReflectionUtils;
 
 @Repository("userDao")
 public class UserDaoImpl implements UserDao {
 
 	@Resource(name = "sessionFactory")
 	private SessionFactory sessionFactory;
 
 	public BlogUser getUserByUserName(String userName) {
 		BlogUser userResult = null;
 		List<BlogUser> usersList;
 		Session session = sessionFactory.getCurrentSession();
 		Query query = session
 				.createQuery("FROM  BlogUser WHERE username= :userName");
 		query.setParameter("userName", userName);
 		usersList = (List<BlogUser>) query.list();
		userResult = (BlogUser) query.uniqueResult();
		// if (usersList.size() > 0) {
		// userResult = usersList.get(0);
		// }
 		return userResult;
 	}
 
 	public void addNewUser(BlogUser user) {
 		Session session = sessionFactory.getCurrentSession();
 		session.save(user);
 	}
 
 	public void addNewAuthority(Authority authority) {
 		Session session = sessionFactory.getCurrentSession();
 		session.save(authority);
 	}
 
 	public Authority getAuthority(String authorityName) {
 		Authority authority = null;
 		List<Authority> authorities;
 		Session session = sessionFactory.getCurrentSession();
 		Query query = session
 				.createQuery("FROM  Authority WHERE authority= :authorityName");
 		query.setParameter("authorityName", authorityName);
 		authorities = (List<Authority>) query.list();
 		authority = (Authority) query.uniqueResult();
 		// if (authorities.size() > 0) {
 		// authority = authorities.get(0);
 		// }
 		return authority;
 	}
 
 	public List<BlogUser> getAllUsers() {
 		Session session = sessionFactory.getCurrentSession();
 		Query query = session.createQuery("FROM  BlogUser");
 		// test();
 		return (List<BlogUser>) query.list();
 	}
 
 	public void addFollowing(BlogUser user, Set<BlogUser> following) {
 		Session session = sessionFactory.getCurrentSession();
 		Set<BlogUser> actualFollowing = user.getFollowing();
 		actualFollowing.addAll(following);
 		user.setFollowing(actualFollowing);
 		session.save(user);
 	}
 
 	public void removeFollowing(BlogUser user, Set<BlogUser> following) {
 		Session session = sessionFactory.getCurrentSession();
 		Set<BlogUser> actualFollowing = user.getFollowing();
 		actualFollowing.removeAll(following);
 		user.setFollowing(actualFollowing);
 		session.save(user);
 	}
 }
