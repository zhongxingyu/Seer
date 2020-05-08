 /*
 	Copyright © 2013 Mael Le Guével
 	This work is free. You can redistribute it and/or modify it under the
 	terms of the Do What The Fuck You Want To Public License, Version 2,
 	as published by Sam Hocevar. See the COPYING file for more details.
 */
 package fr.mael.microrss.dao.impl;
 
 import org.hibernate.Query;
 import org.springframework.stereotype.Repository;
 
 import fr.mael.microrss.dao.ArticleDao;
 import fr.mael.microrss.domain.Article;
 import fr.mael.microrss.domain.Feed;
 
 @Repository
 public class ArticleDaoImpl extends GenericDaoImpl<Article> implements ArticleDao {
 
 	@Override
 	public Article findLastArticle(Feed feed) {
 		StringBuffer query = new StringBuffer("select a from Article a ");
 		query.append("inner join a.feeds f ");
 		query.append("where a.created = (select max(aa.created) from Article aa inner join aa.feeds ff where ff in (:feed)) ");
 		query.append("and f in (:feed) ");
 		query.append("order by a.id desc ");
 		Query q = getSession().createQuery(query.toString());
 		q.setEntity("feed", feed);
 		q.setMaxResults(1);
 		return (Article) q.uniqueResult();
 	}
 
 	@Override
 	public Long nbArticlesForFeed(Feed feed) {
 		StringBuffer query = new StringBuffer("select count(article) from Article article ");
 		query.append("inner join article.feeds f ");
 		query.append("where f in (:feed) ");
 		Query q = getSession().createQuery(query.toString());
 		q.setEntity("feed", feed);
 		return (Long) q.uniqueResult();
 	}
 
 	@Override
 	public Article getByGuid(String guid) {
 		StringBuffer query = new StringBuffer("from Article a ");
		query.append("inner join fetch a.feeds ");
		query.append("where guid = :guid");
 		Query q = getSession().createQuery(query.toString());
 		q.setString("guid", guid);
 		return (Article) q.uniqueResult();
 	}
 
 }
