 package eu.xebia.xke2013.dao;
 
 import eu.xebia.xke2013.model.Article;
import eu.xebia.xke2013.services.ArticleService;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
 import java.math.BigDecimal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional
 @ContextConfiguration(locations = {"classpath:applicationContext.xml"})
 public class ArticleDAOIntegrationTest {
 
     @Autowired
     private ArticleDAO articleDAO;
 
    @Autowired
    private ArticleService articleService;

    @PersistenceContext
    private EntityManager entityManager;

     @Test
     public void should_save_article() {
 
         //Given
         Article article = new Article();
         article.setCode("121");
         article.setDescription("description de mon article");
         article.setAmount(new BigDecimal("120.5"));
 
         //When
        articleService.saveArticle(article);
 
         //Then
         assertNotNull(article.getId());
 
     }
 
     @Test(expected = DataAccessException.class)
     public void should_find_article_when_code_is_ko() {
 
         //Given
         String code = "13";
 
         //When
         articleDAO.findArticleByCode(code);
 
     }
 
     @Test
     public void should_find_article_when_code_is_ok() {
 
         //Given
         String code = "X76543";
 
         //When
         Article article = articleDAO.findArticleByCode(code);
 
         //Then
         assertNotNull(article);
         assertEquals(article.getCode(), code);
 
     }
 }
