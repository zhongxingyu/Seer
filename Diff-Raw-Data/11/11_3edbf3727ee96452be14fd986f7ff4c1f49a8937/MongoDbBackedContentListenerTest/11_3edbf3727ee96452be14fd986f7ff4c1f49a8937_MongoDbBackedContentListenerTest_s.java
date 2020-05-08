 package org.atlasapi.search.loader;
 
 
 import static org.hamcrest.Matchers.hasItems;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentListener;
 import org.atlasapi.persistence.content.RetrospectiveContentLister;
 import org.atlasapi.search.loader.MongoDbBackedContentBootstrapper;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.ImmutableList;
 
 @RunWith(JMock.class)
 public class MongoDbBackedContentListenerTest  {
    
 	private final Mockery context = new Mockery();
 	
 	private RetrospectiveContentLister store = context.mock(RetrospectiveContentLister.class);
     private ContentListener listener = context.mock(ContentListener.class);
    
     private MongoDbBackedContentBootstrapper bootstrapper = new MongoDbBackedContentBootstrapper(listener, store);
     
     private final Item item1 = new Item("1", "1", Publisher.ARCHIVE_ORG);
     private final Item item2 = new Item("2", "2", Publisher.ARCHIVE_ORG);
     private final Item item3 = new Item("3", "3", Publisher.ARCHIVE_ORG);
     
     @Test
     public void testShouldAllContents() throws Exception {
         bootstrapper.setBatchSize(2);
         
         final List<Item> items1 = ImmutableList.of(item1, item2);
         final List<Item> items2 = ImmutableList.<Item>of(item3);
         
         context.checking(new Expectations() {{
            one(store).listAllRoots(null, 2); will(returnValue(items1));
            one(store).listAllRoots(item2.getCanonicalUri(), 2); will(returnValue(items2));
         }});
         
         context.checking(new Expectations() {{
             one(listener).itemChanged(with(hasItems(item1, item2)), with(ContentListener.ChangeType.BOOTSTRAP));
             one(listener).itemChanged(with(hasItems(item3)), with(ContentListener.ChangeType.BOOTSTRAP));
         }});
         
        bootstrapper.loadAll();
     }
 }
