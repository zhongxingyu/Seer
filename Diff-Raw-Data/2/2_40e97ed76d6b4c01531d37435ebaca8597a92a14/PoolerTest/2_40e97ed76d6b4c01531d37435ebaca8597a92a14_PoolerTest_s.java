 package pl.softmil.test.utils.pool;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 import java.util.*;
 
 import org.hamcrest.Matchers;
 import org.junit.*;
 import org.junit.rules.ExpectedException;
 
 public class PoolerTest {
     @Rule
     public ExpectedException expectedException = ExpectedException.none();
     @Test
     public void testPoolTooManyElements() {
         Pooler<String> pooler = new Pooler<String>(Collections.<String>emptyList());
         expectedException.expect(IllegalArgumentException.class);
         pooler.poolXElements(1);
     }
     
     @Test
     public void testPoolAllElements() {
         Pooler<String> pooler = new Pooler<String>(new LinkedList<String>(){{add("1");add("2");}});
         List<String> poolXElements = pooler.poolXElements(2);
         
         assertThat(poolXElements, notNullValue());
         assertThat(poolXElements.size(), equalTo(2));
         assertThat(poolXElements, hasItem("1"));
         assertThat(poolXElements, hasItem("2"));
     }
     
     @Test
     public void testPoolSomeElements() {
         Pooler<String> pooler = new Pooler<String>(new LinkedList<String>(){{add("1");add("2");}});
         List<String> poolXElements = pooler.poolXElements(1);
         
         assertThat(poolXElements, notNullValue());
         assertThat(poolXElements.size(), equalTo(1));
        assertThat(poolXElements, either(Matchers.<String>hasItem("1")).or(Matchers.<String>hasItem("2")));
     }
 
 }
