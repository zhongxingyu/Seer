 package uk.co.pc.domain.dao.helper;
 
 import org.hibernate.SessionFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 import uk.co.pc.domain.model.Article;
 
@Transactional
 public class DaoHelper {
 
 	private SessionFactory sessionFactory;
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	public Article getArticleFromDb(Long id) {
 		return (Article) sessionFactory.getCurrentSession()
 				.createQuery("from Article where id=:id")
 				.setParameter("id", id)
 				.uniqueResult();
 	}
 
 	public Article insertArticleInDb(Article article) {
 		sessionFactory.getCurrentSession().save(article);
 		return article;			 
 	}
 }
