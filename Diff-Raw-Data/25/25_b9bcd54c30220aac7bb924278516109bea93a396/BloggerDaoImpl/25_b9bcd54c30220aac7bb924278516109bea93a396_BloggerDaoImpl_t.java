 package com.portal.dao;
 
 import java.util.Collection;
import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.portal.domain.core.Blogger;
 import com.portal.domain.core.Post;
 
 @Repository(value = "bloggerDao")
 public class BloggerDaoImpl implements BloggerDao {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	@Override
 	@Transactional(readOnly=true)
 	public Blogger get(long id) {
 		return em.find(Blogger.class, id);
 	}
 
 	@Override
 	@Transactional
 	public void addBlogger(Blogger blogger) {
 		em.persist(blogger);
 		//em.flush();
 	}
 
 	@Override
 	@Transactional(readOnly=true)
 	public Collection<Post> getPostsOfBlogger(Blogger blogger) {
 		Query query = em.createQuery("from Post as p where p.blogger = ?1");
 		query.setParameter(1, blogger);
 		return query.getResultList();
 		//return blogger.getPostList();
 	}
 
 	@Override
 	@Transactional(readOnly=true)
 	public Blogger getBloggerByEmail(String email) {
 		Query query = em.createQuery("from Blogger where email=?1");
 		query.setParameter(1, email);
 		Blogger blogger = null;
 		blogger = (Blogger) query.getSingleResult();
 		return blogger;
 	}
 
 	@Override
 	@Transactional(readOnly=true)
 	public Blogger getBloggerByLogin(String login) {
 		Query query = em.createQuery("from Blogger where login=?1");
 		query.setParameter(1, login);
 		Blogger blogger = null;
 		blogger = (Blogger) query.getSingleResult();
 		return blogger;
 	}
 }
