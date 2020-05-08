 package org.atlasapi.media.channel;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.isIn;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.atlasapi.media.entity.Publisher;
 import org.joda.time.LocalDate;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.intl.Countries;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 
 public class MongoChannelGroupStoreTest {
 
 
     private static final DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();
     
     private static final MongoChannelGroupStore store = new MongoChannelGroupStore(mongo);
 
     private static Long platformId;
     private static Long newPlatformId;
     private static Long region1Id;
     private static Long region2Id;
     private static Long region3Id;
     
     @BeforeClass
     public static void setup() {
         platformId = store.createOrUpdate(createPlatform()).getId();
         newPlatformId = store.createOrUpdate(createPlatform()).getId();
         region1Id = store.createOrUpdate(createRegion("region1")).getId();
         region2Id = store.createOrUpdate(createRegion("region2")).getId();
         region3Id = store.createOrUpdate(createRegion("region3")).getId();
     }
 
     private static Platform createPlatform() {
         Platform platform = new Platform();
         return platform;
     }
 
     private static Region createRegion(String title) {
         Region region = new Region();
         region.setAvailableCountries(ImmutableSet.of(Countries.GB));
         region.addTitle(title);
         region.setPublisher(Publisher.METABROADCAST);
         return region;
     }
     
     @Test
     public void testStoresAndRetrievesChannelGroup() {
         Optional<ChannelGroup> group = store.channelGroupFor(platformId);
         
         assertTrue(group.isPresent());
         assertThat(group.get().getId(), is(platformId));
     }
     
     @Test
     public void testRetrievesGroupsForIds() {
         ImmutableList<Long> requestedIds = ImmutableList.of(region1Id, platformId);
         Iterable<ChannelGroup> groups = store.channelGroupsFor(requestedIds);
         
         assertThat(Iterables.size(groups), is(2));
         assertThat(Iterables.get(groups, 0).getId(), isIn(requestedIds));
         assertThat(Iterables.get(groups, 1).getId(), isIn(requestedIds));
         
     }
     
     @Test
     public void testRetrievesAllGroups() {
         Iterable<ChannelGroup> groups = store.channelGroups();
         ImmutableList<Long> ids = ImmutableList.of(platformId, newPlatformId, region1Id, region2Id, region3Id);
         assertThat(Iterables.size(groups), is(5));
         assertThat(Iterables.get(groups, 0).getId(), isIn(ids));
         assertThat(Iterables.get(groups, 1).getId(), isIn(ids));
         assertThat(Iterables.get(groups, 2).getId(), isIn(ids));
         assertThat(Iterables.get(groups, 3).getId(), isIn(ids));
     }
     
     @Test
     public void testAddMultipleNumberings() {
         
         ChannelGroup platform = store.channelGroupFor(platformId).get();
         
         ChannelNumbering numbering = ChannelNumbering.builder()
                 .withStartDate(new LocalDate(2011, 1, 1))
                 .withChannel(1234L)
                 .withChannelGroup(platform)
                 .withChannelNumber("1")
                 .build();
         
         platform.addChannelNumbering(numbering);
         platform = store.createOrUpdate(platform);
         
         assertThat(platform.getChannelNumberings().size(), is(1));
         
         platform.addChannelNumbering(numbering);
         platform = store.createOrUpdate(platform);
         
         assertThat(platform.getChannelNumberings().size(), is(1));
         
         numbering = ChannelNumbering.builder()
                 .withStartDate(new LocalDate(2011, 1, 1))
                 .withChannel(1234L)
                 .withChannelGroup(platform)
                 .withChannelNumber("2")
                 .build();
         
         platform.addChannelNumbering(numbering);
         platform = store.createOrUpdate(platform);
         
         assertThat(platform.getChannelNumberings().size(), is(2));
     }
     
     @Test
     public void testChangeOfPlatform() {
         Region region = (Region)store.channelGroupFor(region1Id).get();
         region.setPlatform(platformId);
         region = (Region)store.createOrUpdate(region);
         
         Platform platform = (Platform)store.channelGroupFor(platformId).get();
         Platform newPlatform = (Platform)store.channelGroupFor(newPlatformId).get();
         
         assertEquals(platformId, region.getPlatform());
         assertEquals(ImmutableSet.of(region.getId()), platform.getRegions());
         assertEquals(ImmutableSet.of(), newPlatform.getRegions());
         
         region.setPlatform(newPlatformId);
         region = (Region)store.createOrUpdate(region);
         
         platform = (Platform)store.channelGroupFor(platformId).get();
         newPlatform = (Platform)store.channelGroupFor(newPlatformId).get();
         
         assertEquals(newPlatformId, region.getPlatform());
         assertEquals(ImmutableSet.of(), platform.getRegions());
         assertEquals(ImmutableSet.of(region.getId()), newPlatform.getRegions());
     }
 }
