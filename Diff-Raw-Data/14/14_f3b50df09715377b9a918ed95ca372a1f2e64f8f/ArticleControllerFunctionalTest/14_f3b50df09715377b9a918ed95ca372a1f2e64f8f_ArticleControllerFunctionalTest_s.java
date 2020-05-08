 package uk.co.pc.web.controller.impl;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.*;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.util.MultiValueMap;
 import org.springframework.web.client.RestTemplate;
 
 import uk.co.pc.domain.dao.helper.DaoHelper;
 import uk.co.pc.domain.model.Article;
 import uk.co.pc.domain.model.ArticleBuilder;
 import uk.co.pc.service.impl.InvalidIdException;
 import uk.co.pc.web.bean.ArticleList;
 
 /**
  * Please run this test AFTER you have deployed the application into a web container (jetty, tomcat...)
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:applicationContext-testing.xml", "classpath:applicationContext-testing-helper.xml"})
 public class ArticleControllerFunctionalTest {
 
     private static final String BASEURL = "http://localhost:8080/article";
     private RestTemplate restTemplate;
     private ArticleBuilder givenArticle;
     
     @Autowired
     private DaoHelper daoHelper;
     
     @Before
     public void setup() {
     	givenArticle = new ArticleBuilder(daoHelper);
     }
 
     @Test
     public void getArticleById() {
     	// given	
     	Article articleInDb = givenArticle.withTitle("Clean Code")
     								  .withAuthor("Uncle Bob")
     								  .isInDatabase();
     	// when
     	final String articleByIdUrl = BASEURL + "/" + articleInDb.getId();
     	Article returnedArticle = restTemplate.getForObject(articleByIdUrl, Article.class);
 
     	// then
     	assertThat(returnedArticle, is(articleInDb));
     }
 
     @Test
     public void getArticlesByTitle() {
     	// given some articles in db
     	Article articleEvansInDb = givenArticle.withTitle("DDD").withAuthor("Evans").isInDatabase();
     	Article articleBobInDb = givenArticle.withTitle("Clean Code").withAuthor("Uncle Bob").isInDatabase();
     	Article articleOtherInDb = givenArticle.withTitle("DDD").withAuthor("Other").isInDatabase();
     	// and a title to search
     	String titleToSearch = "DDD";
     	
     	// when
     	final String articleByIdUrl = BASEURL + "?title=" + titleToSearch;
     	ArticleList returnedArticles = restTemplate.getForObject(articleByIdUrl, ArticleList.class);
 
     	// then
     	List<Article> actualArticles = returnedArticles.getArticles();
 		assertThat("size", actualArticles.size(), is(2)); 
     	assertThat("articles", actualArticles, hasItems(articleEvansInDb, articleOtherInDb));   
     }
 
     @Test
     public void getArticlesByAuthor() {
         fail("tbd");
     }
 
     @Test
     public void saveArticle() {
     	// given
     	MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
     	parameters.add("title", "DDD");
     	parameters.add("author", "Evans");
     	
     	// when
     	Article returnedArticle = restTemplate.postForObject(BASEURL, parameters, Article.class);
     	
     	// then
     	assertThat("id", returnedArticle.getId(), is(notNullValue()));
     	assertThat("title", returnedArticle.getTitle(), is("DDD"));
     	assertThat("author", returnedArticle.getAuthor(), is("Evans"));
     	
     }
     
    @Test(expected=Exception.class)
 	public void deleteArticle() throws Exception {
     	// given 
     	Article articleInDb = givenArticle.withTitle("Clean Code")
 										  .withAuthor("Uncle Bob")
 										  .isInDatabase();
 		// when
     	final String deleteArticleUrl = BASEURL + "/" + articleInDb.getId();
     	restTemplate.delete(deleteArticleUrl);
     	
     	// then article is not found
     	Article articleNotFound = daoHelper.getArticleFromDb(articleInDb.getId());
     	assertThat(articleNotFound, is(nullValue()));
     	
 	}
     
     /* Spring Injected Values/Collaborators */
 
     @Resource
     public void setRestTemplate(final RestTemplate restTemplate) {
         this.restTemplate = restTemplate;
     }
 
 }
