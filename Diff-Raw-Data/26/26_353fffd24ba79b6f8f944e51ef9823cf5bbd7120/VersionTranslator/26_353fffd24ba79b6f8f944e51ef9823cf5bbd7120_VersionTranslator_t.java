 package org.atlasapi.persistence.media.entity;
 
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Restriction;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.media.segment.SegmentEvent;
 import org.atlasapi.media.segment.SegmentEventTranslator;
 import org.atlasapi.persistence.ModelTranslator;
 import org.joda.time.Duration;
 
 import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.ids.NumberToShortStringCodec;
 import com.metabroadcast.common.persistence.translator.TranslatorUtils;
 import com.mongodb.BasicDBList;
 import com.mongodb.DBObject;
 
 public class VersionTranslator implements ModelTranslator<Version> {
     
 	private static final String THREE_D_KEY = "threeD";
     static final String ENCODINGS_KEY = "manifestedAs";
     static final String BROADCASTS_KEY = "broadcasts";
     private static final String PROVIDER_KEY = "provider";
     private static final String SEGMENT_EVENTS_KEY = "segmentEvents";
 	
 	private final IdentifiedTranslator descriptionTranslator = new IdentifiedTranslator();
     private final BroadcastTranslator broadcastTranslator = new BroadcastTranslator();
     private final EncodingTranslator encodingTranslator = new EncodingTranslator();
     private final RestrictionTranslator restrictionTranslator = new RestrictionTranslator();
     private final SegmentEventTranslator segmentEventTranslator;
 
     public VersionTranslator(NumberToShortStringCodec idCodec) {
         this.segmentEventTranslator = new SegmentEventTranslator(idCodec);
     }
     
     @Override
     public Version fromDBObject(DBObject dbObject, Version entity) {
         if (entity == null) {
             entity = new Version();
         }
         
         descriptionTranslator.fromDBObject(dbObject, entity);
         Integer durationInSeconds = (Integer) dbObject.get("duration");
 		if (durationInSeconds != null) {
 			entity.setDuration(Duration.standardSeconds(durationInSeconds));
 		}
 		
 		if (dbObject.containsField(PROVIDER_KEY)) {
 			entity.setProvider(Publisher.fromKey((String) dbObject.get(PROVIDER_KEY)).requireValue());
 		}
 		
         entity.setPublishedDuration((Integer) dbObject.get("publishedDuration"));
         entity.set3d(TranslatorUtils.toBoolean(dbObject, THREE_D_KEY));
 
         if(dbObject.get("restriction") != null) {
         	entity.setRestriction(restrictionTranslator.fromDBObject((DBObject) dbObject.get("restriction"), null));
         } else {
         	entity.setRestriction(new Restriction());
         }
         
         List<DBObject> list = TranslatorUtils.toDBObjectList(dbObject,BROADCASTS_KEY);
         if (list != null && ! list.isEmpty()) {
             Set<Broadcast> broadcasts = Sets.newLinkedHashSet();
             for (DBObject object: list) {
                 Broadcast broadcast = broadcastTranslator.fromDBObject(object);
                 broadcasts.add(broadcast);
             }
             entity.setBroadcasts(broadcasts);
         }
         
         list = TranslatorUtils.toDBObjectList(dbObject,ENCODINGS_KEY);
         if (list != null && ! list.isEmpty()) {
             Set<Encoding> encodings = Sets.newHashSet();
             for (DBObject object: list) {
                 Encoding encoding = encodingTranslator.fromDBObject(object, null);
                 encodings.add(encoding);
             }
             entity.setManifestedAs(encodings);
         }
         
         list = TranslatorUtils.toDBObjectList(dbObject, SEGMENT_EVENTS_KEY);
         if (list != null && !list.isEmpty()) {
             entity.setSegmentEvents(Lists.transform(list, new Function<DBObject, SegmentEvent>() {
                 @Override
                 public SegmentEvent apply(DBObject input) {
                     return segmentEventTranslator.fromDBObject(input, null);
                 }
             }));
         }
         
         return entity;
     }
 
     @Override
     public DBObject toDBObject(DBObject dbObject, Version entity) {
         dbObject = descriptionTranslator.toDBObject(dbObject, entity);
         
         TranslatorUtils.from(dbObject, "duration", entity.getDuration());
         TranslatorUtils.from(dbObject, "publishedDuration", entity.getPublishedDuration());
         TranslatorUtils.from(dbObject, THREE_D_KEY, entity.is3d());
         
         if(entity.getRestriction() != null && entity.getRestriction().hasRestrictionInformation()) {
         	dbObject.put("restriction", restrictionTranslator.toDBObject(null, entity.getRestriction()));
         }
         
         if (entity.getProvider() != null) {
         	dbObject.put(PROVIDER_KEY, entity.getProvider().key());
         }
         
         if (! entity.getBroadcasts().isEmpty()) {
             BasicDBList list = new BasicDBList();
             for (Broadcast broadcast: sortByBroadcastTime(entity.getBroadcasts())) {
                 if (broadcast != null) {
                     list.add(broadcastTranslator.toDBObject(broadcast));
                 }
             }
             if (! list.isEmpty()) {
                 dbObject.put(BROADCASTS_KEY, list);
             }
         }
         
         if (! entity.getManifestedAs().isEmpty()) {
             BasicDBList list = new BasicDBList();
             for (Encoding encoding: entity.getManifestedAs()) {
                 if (encoding != null) {
                     list.add(encodingTranslator.toDBObject(null, encoding));
                 }
             }
             if (! list.isEmpty()) {
                 dbObject.put(ENCODINGS_KEY, list);
             }
         }
         
         if (!entity.getSegmentEvents().isEmpty()) {
             BasicDBList list = new BasicDBList();
             for (SegmentEvent event : entity.getSegmentEvents()) {
                 if (event != null) {
                     list.add(segmentEventTranslator.toDBObject(null, event));
                 }
             }
             if (!list.isEmpty()) {
                 dbObject.put(SEGMENT_EVENTS_KEY, list);
             }
         }
         
         return dbObject;
     }
 
 	private List<Broadcast> sortByBroadcastTime(Set<Broadcast> broadcasts) {
 		return MOST_RECENT_FIRST.sortedCopy(broadcasts);
 	}
 	
 	private static final Ordering<Broadcast> MOST_RECENT_FIRST = new Ordering<Broadcast>() {
 
 		@Override
 		public int compare(Broadcast a, Broadcast b) {
		    
		    return ComparisonChain.start()
		        .compare(a.getTransmissionTime(), b.getTransmissionTime(), Ordering.natural().nullsLast())
		        .compare(a.getBroadcastOn(), b.getBroadcastOn(), Ordering.natural().nullsLast())
		        .compare(a.getSourceId(), b.getSourceId(), Ordering.natural().nullsLast())
		        .compare(a, b, Ordering.arbitrary())
		        .result();
 		}
 	};
 
 }
