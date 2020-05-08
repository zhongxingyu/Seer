 /*
 	Copyright © 2013 Mael Le Guével
 	This work is free. You can redistribute it and/or modify it under the
 	terms of the Do What The Fuck You Want To Public License, Version 2,
 	as published by Sam Hocevar. See the COPYING file for more details.
 */
 package fr.mael.microrss.dao;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import fr.mael.microrss.domain.Article;
 import fr.mael.microrss.domain.Feed;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "/fr/mael/microrss/conf/application-context-test.xml" })
 @Transactional
 public class ArticleDaoTest {
 
 	@Autowired
 	private ArticleDao articleDao;
 
 	@Test
 	public void testLastArticle() {
 		Feed feed = new Feed();
 		feed.setId(1);
 		Article article = articleDao.findLastArticle(feed);
 		Assert.assertEquals("http://blog.le-guevel.com/?p=296", article.getGuid());
 		feed.setId(2);
 		Article article2 = articleDao.findLastArticle(feed);
 		Assert.assertEquals("http://blog.case.edu/news/2005/11#004845", article2.getGuid());
 	}
 
 	@Test
 	public void testNbArticles() {
 		Feed feed = new Feed();
 		feed.setId(1);
 		Assert.assertEquals(new Long(8), articleDao.nbArticlesForFeed(feed));
 		feed.setId(2);
 		Assert.assertEquals(new Long(12), articleDao.nbArticlesForFeed(feed));
 		feed.setId(3);
 		Assert.assertEquals(new Long(0), articleDao.nbArticlesForFeed(feed));
 	}
 
	public void testStarAndRead() {

 	}
 
 }
