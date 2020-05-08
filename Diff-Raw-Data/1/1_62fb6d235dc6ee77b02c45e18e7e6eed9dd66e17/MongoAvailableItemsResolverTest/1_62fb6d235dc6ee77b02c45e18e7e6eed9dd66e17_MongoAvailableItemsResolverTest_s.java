 package org.atlasapi.persistence.output;
 
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 import java.util.Set;
 
 import org.atlasapi.application.v3.ApplicationConfiguration;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.LookupRef;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.mongo.MongoContentWriter;
 import org.atlasapi.persistence.lookup.entry.LookupEntry;
 import org.atlasapi.persistence.lookup.mongo.MongoLookupEntryStore;
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.TimeMachine;
 
 
 public class MongoAvailableItemsResolverTest {
     
     private final DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();
     private final TimeMachine clock = new TimeMachine();
     private final MongoLookupEntryStore lookupStore
         = new MongoLookupEntryStore(mongo.collection("lookup"));
     private final MongoAvailableItemsResolver resolver
         = new MongoAvailableItemsResolver(mongo, lookupStore, clock);
     private final ContentWriter writer = new MongoContentWriter(mongo, lookupStore, clock);
     
     @Test
     public void testResolvesChildrenOfEquivalentBrandsAsTheItemsOfThePrimaryBrand() {
         
         Brand primary = new Brand("primary", "primary", Publisher.BBC);
         Brand equivalent = new Brand("equivalent", "equivalent", Publisher.PA);
         
         Episode p1 = episode("p1", primary, location("p1l1", dateTime(0), dateTime(10)));
         Episode p2 = episode("p2", primary);
         
         Episode e1 = episode("e1", equivalent, location("e1l1", dateTime(0), dateTime(10)));
         Episode e2 = episode("e2", equivalent, location("e2l2", dateTime(0), dateTime(10)));
         
         writer.createOrUpdate(primary);
         writer.createOrUpdate(equivalent);
         writer.createOrUpdate(p1);
         writer.createOrUpdate(p2);
         writer.createOrUpdate(e1);
         writer.createOrUpdate(e2);
         
         writeEquivalences(p1, e1);
         writeEquivalences(p2, e2);
         
         primary.setEquivalentTo(ImmutableSet.of(LookupRef.from(equivalent)));
         
         clock.jumpTo(dateTime(5));
         ApplicationConfiguration noPaConfig = ApplicationConfiguration.defaultConfiguration();
         Set<ChildRef> childRefs = ImmutableSet.copyOf(resolver.availableItemsFor(primary, noPaConfig));
         ChildRef childRef = Iterables.getOnlyElement(childRefs);
         assertEquals(p1.getCanonicalUri(), childRef.getUri());
 
         ApplicationConfiguration withPaConfig = ApplicationConfiguration.defaultConfiguration()
                 .enable(Publisher.PA);
         childRefs = ImmutableSet.copyOf(resolver.availableItemsFor(primary, withPaConfig));
         assertThat(childRefs.size(), is(2));
         assertThat(Iterables.transform(childRefs, ChildRef.TO_URI), hasItems(p1.getCanonicalUri(), p2.getCanonicalUri()));
         
     }
 
     private void writeEquivalences(Episode a, Episode b) {
         LookupEntry aEntry = LookupEntry.lookupEntryFrom(a)
             .copyWithDirectEquivalents(ImmutableSet.of(LookupRef.from(b)))
             .copyWithEquivalents(ImmutableSet.of(LookupRef.from(b)));
         LookupEntry bEntry = LookupEntry.lookupEntryFrom(b)
             .copyWithDirectEquivalents(ImmutableSet.of(LookupRef.from(a)))
             .copyWithEquivalents(ImmutableSet.of(LookupRef.from(a)));
         
         lookupStore.store(aEntry);
         lookupStore.store(bEntry);
         
     }
 
     private Episode episode(String uri, Brand container, Location... locations) {
         Episode episode = new Episode();
         episode.setCanonicalUri(uri);
         episode.setContainer(container);
         episode.setPublisher(container.getPublisher());
         
         Version version = new Version();
         Encoding encoding = new Encoding();
         encoding.setAvailableAt(ImmutableSet.copyOf(locations));
         version.setManifestedAs(ImmutableSet.of(encoding));
         episode.setVersions(ImmutableSet.of(version));
         return episode;
     }
 
     private Location location(String uri, DateTime start, DateTime end) {
         Location location = new Location();
         location.setUri(uri);
         Policy policy = new Policy();
         policy.setAvailabilityStart(start);
         policy.setAvailabilityEnd(end);
         location.setPolicy(policy);
         return location;
     }
 
     private DateTime dateTime(int millis) {
         return new DateTime(millis,DateTimeZones.UTC);
     }
     
 }
