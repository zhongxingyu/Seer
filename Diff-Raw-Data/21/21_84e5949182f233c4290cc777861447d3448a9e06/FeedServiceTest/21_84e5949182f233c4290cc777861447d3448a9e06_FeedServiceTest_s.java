 package ar.edu.utn.tacs.group5.service;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
 import org.slim3.tester.AppEngineTestCase;
 
 import ar.edu.utn.tacs.group5.model.Feed;
 
 public class FeedServiceTest extends AppEngineTestCase {
 
     private FeedService service = new FeedService();
 
     @Test
     public void insert() throws Exception {
         Feed feed = new Feed();
         service.insert(feed);
         assertThat(feed.getKey(), is(notNullValue()));
     }
 
 }
