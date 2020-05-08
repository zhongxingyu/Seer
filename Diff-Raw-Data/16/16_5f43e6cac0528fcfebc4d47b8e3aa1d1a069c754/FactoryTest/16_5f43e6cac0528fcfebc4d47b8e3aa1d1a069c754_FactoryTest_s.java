 package nl.pwiddershoven.jfactory;
 
 import nl.pwiddershoven.jfactory.factories.OrderFactory;
 import nl.pwiddershoven.jfactory.models.Article;
 import nl.pwiddershoven.jfactory.models.Item;
 import nl.pwiddershoven.jfactory.utils.ReflectionUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.reflections.Reflections;
 
 import java.lang.reflect.Field;
 import java.util.Map;
 
 import static com.google.common.collect.Maps.newHashMap;
 import static junit.framework.Assert.assertEquals;
 import static nl.pwiddershoven.jfactory.Factory.build;
 
 public class FactoryTest {
 
     @Before
     public void setup() {
         // add package to scan for factory classes, so jFactory can find the factories
         Factory.addFactoryScanPackage("nl.pwiddershoven.jfactory.factories");
     }
 
     @Test
     public void buildObject() {
         // build a default Article, as defined in our factory
         Article article = build(Article.class);
         assertEquals("http://pwiddershoven.nl/blog/2011/01/05/airplayer.html", article.getGuid());
 
         // now build another article, but override some properties
         article = build(Article.class, "title", "foobar", "guid", "test");
         assertEquals("foobar", article.getTitle());
         assertEquals("test", article.getGuid());
     }
 
     @Test
     public void traits() {
         // you can also specify and use traits. jFactory evaluates the first passed attribute to see if it matches
         // the name of a registered trait
         Article article = build(Article.class, "read", "title", "test");
         assertEquals(true, article.isRead());
         assertEquals("test", article.getTitle());
 
         // apply the unread trait
         article = build(Article.class, "unread");
         assertEquals(false, article.isRead());
     }
 
     @Test
     public void lazyValues() throws InterruptedException {
         OrderFactory factory = new OrderFactory();
 
         // amount property is non lazy, initialized once at factory creation time
         assertEquals(1, factory.build().getAmount());
         assertEquals(1, factory.build().getAmount());
 
         // totalAmount is lazy, so evaluated each time you call build
         assertEquals(3, factory.build().getTotalAmount());
         assertEquals(4, factory.build().getTotalAmount());
         assertEquals(5, factory.build().getTotalAmount());
     }
 
     @Test
     public void sequences() throws Exception {
         // reset all sequences, so we can assume the first built object in this test has a sequence value of 1
         Field field = ObjectFactory.class.getDeclaredField("sequences");
         field.setAccessible(true);
         field.set(null, newHashMap());
 
         assertEquals("Article 1", build(Article.class).getTitle());
         assertEquals("Article 2", build(Article.class).getTitle());
         assertEquals("Article 3", build(Article.class).getTitle());
 
         assertEquals(1, build(Item.class).getQuantity());
         assertEquals(2, build(Item.class).getQuantity());
         assertEquals(3, build(Item.class).getQuantity());
     }
 }
