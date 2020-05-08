 package org.atlasapi.search.loader;
 
 import static org.hamcrest.Matchers.hasItems;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.ContentGroup;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.RetrospectiveContentLister;
 import org.atlasapi.search.searcher.ContentChangeListener;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.ImmutableList;
 
 @RunWith(JMock.class)
 public class MongoDbBackedContentListenerTest  {
    
 	private final Mockery context = new Mockery();
     
     private final Item item1 = new Item("1", "1", Publisher.ARCHIVE_ORG);
     private final Item item2 = new Item("2", "2", Publisher.ARCHIVE_ORG);
     private final Item item3 = new Item("3", "3", Publisher.ARCHIVE_ORG);
 	
     private ContentChangeListener listener = context.mock(ContentChangeListener.class);
     private final RetrospectiveContentLister lister = new RetrospectiveContentLister() {
 
         int taken = 0;
         List<Content> content = ImmutableList.<Content>of(item1, item2, item3);
         
         @Override
         public List<Content> listAllRoots(String fromId, int batchSize) {
             if(taken >= content.size()) {
                 return ImmutableList.of();
             }
             List<Content> result = content.subList(taken, Math.min(taken-batchSize, content.size()));
             taken -= batchSize;
             return result;
         }
         
         @Override
         public List<ContentGroup> listAllContentGroups(String fromId, int batchSize) {
             return ImmutableList.of();
         }
     };
    
     private MongoDbBackedContentBootstrapper bootstrapper = new MongoDbBackedContentBootstrapper(lister, true);
     
     @SuppressWarnings("unchecked")
     @Test
     public void testShouldAllContents() throws Exception {
         
         bootstrapper.setBatchSize(2);
         
         context.checking(new Expectations() {{
             one(listener).contentChange(with(hasItems(item1, item2)));
             one(listener).contentChange(with(any(List.class)));
         }});
         
         bootstrapper.loadAllIntoListener(listener);
     }
 }
