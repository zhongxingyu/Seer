 package com.ngdb.entities;
 
import static org.hibernate.criterion.Order.desc;
 import static org.hibernate.criterion.Projections.countDistinct;
 
 import java.math.BigInteger;
 import java.util.List;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Session;
 
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.shop.Wish;
 import com.ngdb.entities.user.User;
 
 public class WishBox {
 
 	@Inject
 	private Session session;
 
 	public Long getNumWishes() {
 		return (Long) session.createCriteria(Wish.class).setProjection(countDistinct("article")).uniqueResult();
 	}
 
 	public String getRankOf(Article article) {
 		List<Object[]> list = session.createSQLQuery("SELECT article_id,COUNT(*) FROM Wish GROUP BY article_id ORDER BY COUNT(*) DESC").list();
 		int rank = 1;
 		for (Object[] o : list) {
 			BigInteger articleId = (BigInteger) o[0];
 			if (article.getId().equals(articleId.longValue())) {
 				return Integer.toString(rank);
 			}
 		}
 		return "N/A";
 	}
 
 	public List<Wish> findAllWishes() {
		return session.createCriteria(Wish.class).addOrder(desc("modificationDate")).list();
 	}
 
 	public void add(User user, Article article) {
 		Wish wish = new Wish(user, article);
 		user.addToWishes(wish);
 		session.merge(wish);
 	}
 
 }
