 package org.atlasapi.remotesite.pa;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Person;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.SystemOutAdapterLog;
 import org.atlasapi.remotesite.ContentWriters;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.time.TimeMachine;
 
 public class PaBaseProgrammeUpdaterTest extends TestCase {
 
     private PaProgDataProcessor programmeProcessor;
 
     private TimeMachine clock = new TimeMachine();
     private AdapterLog log = new SystemOutAdapterLog();
     private MongoDbBackedContentStore store;
     private ContentWriters contentWriters = new ContentWriters();
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         store = new MongoDbBackedContentStore(MongoTestHelper.anEmptyTestDatabase(), clock);
         contentWriters.add(store);
         programmeProcessor = new PaProgrammeProcessor(contentWriters, store, log);
     }
 
     public void testShouldCreateCorrectPaData() throws Exception {
         TestFileUpdater updater = new TestFileUpdater(programmeProcessor, log);
         updater.run();
         Identified content = null;
 
         // lazy
         for (int i = 0; i < 5; i++) {
             Thread.sleep(500);
             content = store.findByCanonicalUri("http://pressassociation.com/brands/122139");
             if (content != null)
                 continue;
         }
 
         assertNotNull(content);
         assertTrue(content instanceof Brand);
         Brand brand = (Brand) content;
         assertFalse(brand.getContents().isEmpty());
         assertNotNull(brand.getImage());
 
         Item item = brand.getContents().get(0);
         assertNotNull(item.getImage());
         assertFalse(item.getVersions().isEmpty());
         assertEquals(MediaType.VIDEO, item.getMediaType());
         assertEquals(Specialization.TV, item.getSpecialization());
 
         assertEquals(18, item.people().size());
         assertEquals(14, item.actors().size());
 
         Version version = item.getVersions().iterator().next();
         assertFalse(version.getBroadcasts().isEmpty());
 
         Broadcast broadcast = version.getBroadcasts().iterator().next();
         assertEquals("pa:71118471", broadcast.getId());
 
         updater.run();
 
         content = store.findByCanonicalUri("http://pressassociation.com/brands/122139");
         assertNotNull(content);
         assertTrue(content instanceof Brand);
         brand = (Brand) content;
         assertFalse(brand.getContents().isEmpty());
 
         item = brand.getContents().get(0);
         assertFalse(item.getVersions().isEmpty());
 
         version = item.getVersions().iterator().next();
         assertFalse(version.getBroadcasts().isEmpty());
 
         broadcast = version.getBroadcasts().iterator().next();
         assertEquals("pa:71118471", broadcast.getId());
 
         for (CrewMember crewMember : item.people()) {
             content = store.findByCanonicalUri(crewMember.getCanonicalUri());
             assertTrue(content instanceof Person);
             assertEquals(crewMember.name(), ((Person) content).name());
         }
     }
 
     static class TestFileUpdater extends PaBaseProgrammeUpdater {
 
         public TestFileUpdater(PaProgDataProcessor processor, AdapterLog log) {
             super(processor, log);
         }
 
         @Override
         public void run() {
             this.processFiles(ImmutableList.of(new File(Resources.getResource("20110115_tvdata.xml").getFile())));
         }
     }
 }
