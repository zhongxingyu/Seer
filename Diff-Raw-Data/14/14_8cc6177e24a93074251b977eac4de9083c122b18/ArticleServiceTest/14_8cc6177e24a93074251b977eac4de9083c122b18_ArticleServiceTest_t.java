 package ohtu.refero.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import ohtu.refero.models.Article;
 import ohtu.refero.repositories.ArticleRepository;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring-context.xml",
                                    "file:src/main/webapp/WEB-INF/spring-database.xml"})
 
 public class ArticleServiceTest {
     
     @Autowired
     ArticleService a;
     Article b;
     
     @Before
     public void initialize() {
         b = new Article();
         b.setAuthor("author");
         b.setTitle("t");
         b.setJournal("j");
         b.setNumber(254);
         b.setReleaseYear(1999);
         b.setVolume(2);
         b.setId(Long.MIN_VALUE);
     }
     
     @Test
     public void addingNullReturnsNull() {
         assertEquals(null, a.save(null));
     }
     
     @Test
     public void addAndfindArticleTest() {
        Article article = a.save(b);
        assertEquals(a.findById(article.getId()), article);
     }
     
 //    @Test
 //    public void findAllArticles() {
 //        Random r = new Random();
 //        ArrayList<Article> l = new ArrayList<Article>();
 //        for (int i = 0; i < 25; i++) {
 //            Article newArticle = new Article();
 //            newArticle.setId(r.nextLong());
 //            l.add(newArticle);
 //            a.addArticle(newArticle);
 //        }
 //        
 //        for (Article article : l) {
 //            if (!a.getArticles().contains(article))
 //                fail("List was missing some articles");
 //        }
 //    }
 }
