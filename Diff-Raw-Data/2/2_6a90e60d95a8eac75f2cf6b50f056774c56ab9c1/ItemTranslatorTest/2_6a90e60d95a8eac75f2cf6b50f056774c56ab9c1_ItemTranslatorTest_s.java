 package org.atlasapi.persistence.media.entity;
 
 import java.util.List;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.Actor;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.Duration;
 import org.joda.time.LocalDate;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.time.Clock;
 import com.metabroadcast.common.time.SystemClock;
 import com.mongodb.BasicDBList;
 import com.mongodb.DBObject;
 
 public class ItemTranslatorTest extends TestCase {
 	
 	private final Clock clock = new SystemClock();
     
     @SuppressWarnings("unchecked")
     public void testConvertFromItem() throws Exception {
         Item item = new Item("canonicalUri", "curie", Publisher.BBC);
         item.setTitle("title");
         
         Location loc = new Location();
         loc.setAvailable(true);
         
         Encoding enc = new Encoding();
         enc.setAdvertisingDuration(1);
         enc.addAvailableAt(loc);
         
         
         Duration duration = Duration.standardSeconds(1);
         Broadcast br = new Broadcast("channel", clock.now(), duration);
         br.setScheduleDate(new LocalDate(2010, 3, 20));
         
         Version version = new Version();
         version.setDuration(duration);
         version.addManifestedAs(enc);
         version.addBroadcast(br);
         item.addVersion(version);
         
         Set<String> tags = Sets.newHashSet();
         tags.add("tag");
         item.setTags(tags);
         
         ItemTranslator it = new ItemTranslator();
         DBObject dbObject = it.toDBObject(null, item);
         
         assertEquals("canonicalUri", dbObject.get(DescriptionTranslator.CANONICAL_URI));
         assertEquals("title", dbObject.get("title"));
         
         List<String> t = (List<String>) dbObject.get("tags");
         assertFalse(t.isEmpty());
         for (String tag: t) {
             assertTrue(tags.contains(tag));
         }
         
         Iterable<DBObject> items = (Iterable<DBObject>) dbObject.get("contents");
         DBObject itemDdbo = Iterables.getOnlyElement(items);
         
         BasicDBList vs = (BasicDBList) itemDdbo.get("versions");
         assertEquals(1, vs.size());
         DBObject v = (DBObject) vs.get(0);
         assertEquals(version.getDuration(), v.get("duration"));
         
         BasicDBList bs = (BasicDBList) v.get("broadcasts");
         assertEquals(1, bs.size());
         DBObject b = (DBObject) bs.get(0);
         assertEquals(br.getScheduleDate().toString(), b.get("scheduleDate"));
         
         BasicDBList ma = (BasicDBList) v.get("manifestedAs");
         assertEquals(1, ma.size());
         DBObject e = (DBObject) ma.get(0);
         assertEquals(enc.getAdvertisingDuration(), e.get("advertisingDuration"));
         
         BasicDBList ls = (BasicDBList) e.get("availableAt");
         assertEquals(1, ls.size());
         DBObject l = (DBObject) ls.get(0);
         assertEquals(loc.getAvailable(), l.get("available"));
     }
     
     public void testConvertToItem() throws Exception {
         Item item = new Item("canonicalUri", "curie", Publisher.BBC);
         item.setTitle("title");
         
         Location loc = new Location();
         loc.setAvailable(true);
         
         Encoding enc = new Encoding();
         enc.setAdvertisingDuration(1);
         enc.addAvailableAt(loc);
         
         Duration duration = Duration.standardSeconds(1);
         
         Broadcast br = new Broadcast("channel", clock.now(), duration);
         br.setScheduleDate(new LocalDate(2010, 3, 20));
         
         Version version = new Version();
         version.setDuration(duration);
         version.addManifestedAs(enc);
         version.addBroadcast(br);
         item.addVersion(version);
         
         Actor actor = Actor.actor("blah", "some guy", Publisher.BBC);
         item.addPerson(actor);
         
         Set<String> tags = Sets.newHashSet();
         tags.add("tag");
         item.setTags(tags);
         
         ItemTranslator it = new ItemTranslator();
         DBObject dbObject = it.toDBObject(null, item);
         
         Item i = it.fromDBObject(dbObject, null);
         assertEquals(i.getCanonicalUri(), item.getCanonicalUri());
         assertEquals(i.getCurie(), item.getCurie());
         
         Set<String> t = i.getTags();
         for (String tag: t) {
             assertTrue(item.getTags().contains(tag));
         }
         
         Set<Version> vs = item.getVersions();
         assertEquals(1, vs.size());
         Version v = vs.iterator().next();
         assertEquals(version.getDuration(), v.getDuration());
         
         Set<Broadcast> bs = v.getBroadcasts();
         assertEquals(1, bs.size());
         Broadcast b = bs.iterator().next();
         assertEquals(br.getScheduleDate(), b.getScheduleDate());
         
         Set<Encoding> ma = v.getManifestedAs();
         assertEquals(1, ma.size());
         Encoding e = ma.iterator().next();
         assertEquals(enc.getAdvertisingDuration(), e.getAdvertisingDuration());
         
         Set<Location> ls = e.getAvailableAt();
         assertEquals(1, ls.size());
         assertEquals(loc.getAvailable(), ls.iterator().next().getAvailable());
         
        Set<CrewMember> people = i.people();
         assertEquals(1, people.size());
         assertEquals("some guy", ((Actor) Iterables.getFirst(people, null)).character());
     }
 }
