 package id.co.bippo.jsfajaxpush;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 @Named @Stateless
 public class ArticleRepository {
 
 	@PersistenceContext private EntityManager em;
 	@Inject private Event<ArticleCreatedEvent> articleCreated;
 	
 	public List<Article> findArticlesAfter(long id) {
 		TypedQuery<Article> query = em.createQuery("SELECT a FROM Article a WHERE a.id > :id ORDER BY a.id", Article.class)
 			.setParameter("id", id);
 		return query.getResultList();
 	}
 
 	public List<Article> getArticles() {
 		TypedQuery<Article> query = em.createQuery("SELECT a FROM Article a ORDER BY a.id DESC", Article.class);
 		return query.getResultList();
 	}
 
 	public Article createArticle(Article article) {
 		article = em.merge(article);
 		articleCreated.fire(new ArticleCreatedEvent(article));
 		return article;
 	}
 	
 	public Article getLastArticle() {
		TypedQuery<Article> query = em.createQuery("SELECT a FROM Article a ORDER BY a.id DESC LIMIT 1", Article.class);
 		return query.getSingleResult();
 	}
 }
