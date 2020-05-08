 package com.ngdb.entities;
 
 import static org.hibernate.criterion.Order.desc;
 import static org.hibernate.criterion.Projections.countDistinct;
 
 import java.math.BigInteger;
 import java.util.List;
 
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Session;
 
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.shop.Wish;
 
 public class WishBox {
 
 	@Inject
 	private Session session;
 
 	public Long getNumWishes() {
 		return (Long) session.createCriteria(Wish.class).setCacheable(true).setProjection(countDistinct("article")).uniqueResult();
 	}
 
 	public int getRankOf(Article article) {
		List<Object[]> list = session.createSQLQuery("SELECT article_id,COUNT(*) FROM Wish GROUP BY article_id ORDER BY COUNT(*) DESC").setCacheable(true).list();
 		int rank = 1;
 		for (Object[] o : list) {
 			BigInteger articleId = (BigInteger) o[0];
 			if (article.getId().equals(articleId.longValue())) {
 				return rank;
 			}
 		}
 		return Integer.MAX_VALUE;
 	}
 
 	public List<Wish> findAllWishes() {
 		return session.createCriteria(Wish.class).setCacheable(true).addOrder(desc("modificationDate")).list();
 	}
 
 }
