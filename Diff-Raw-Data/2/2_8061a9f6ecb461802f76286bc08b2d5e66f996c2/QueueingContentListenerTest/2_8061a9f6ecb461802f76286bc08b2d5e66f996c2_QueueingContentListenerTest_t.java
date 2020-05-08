 package org.atlasapi.persistence.content;
 
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.persistence.content.ContentListener;
 import org.atlasapi.persistence.content.QueueingContentListener;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.integration.junit4.JMock;
 import org.jmock.lib.concurrent.DeterministicScheduler;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.google.common.collect.Lists;
 
 @RunWith(JMock.class)
 public class QueueingContentListenerTest {
     
 	private final Mockery context = new Mockery();
 	
 	private static final Publisher anyPublisher = Publisher.BBC;
     private ContentListener delegate = context.mock(ContentListener.class);
     private DeterministicScheduler scheduler = new DeterministicScheduler();
     
     private QueueingContentListener contentListener = new QueueingContentListener(scheduler, delegate);
     
     @Before
     public void setUp() throws Exception {
        contentListener.start();
     }
     
     @Test
     public void testBrandUpdate() throws Exception {
         List<Brand> brands = Lists.newArrayList();
 		brands.add(new Brand("uri", "curie", anyPublisher));
         
         contentListener.brandChanged(brands, null);
         
         brands = Lists.newArrayList();
         brands.add(new Brand("uri2", "curie2", anyPublisher));
         
         contentListener.brandChanged(brands, null);
         
         scheduler.tick(20, TimeUnit.SECONDS);
         
         final List<Brand> finalBrands = Lists.newArrayList();
         finalBrands.add(new Brand("uri", "curie", anyPublisher));
         finalBrands.add(new Brand("uri2", "curie2", anyPublisher));
         
         context.checking(new Expectations() {{ 
             one(delegate).brandChanged(finalBrands, null);
             one(delegate).itemChanged(Lists.<Item>newArrayList(), null);
         }});
         
         scheduler.tick(60, TimeUnit.SECONDS);
         
         context.checking(new Expectations() {{ 
             one(delegate).brandChanged(Lists.<Brand>newArrayList(), null);
             one(delegate).itemChanged(Lists.<Item>newArrayList(), null);
         }});
         
         scheduler.tick(120, TimeUnit.SECONDS);
     }
     
     @Test
     public void testBootstrap() throws Exception {
         final List<Item> items = Lists.newArrayList();
         items.add(new Item("uri", "curie", anyPublisher));
         
         context.checking(new Expectations() {{ 
             one(delegate).itemChanged(items, ContentListener.changeType.BOOTSTRAP);
         }});
         
         contentListener.itemChanged(items, ContentListener.changeType.BOOTSTRAP);
     }
 }
