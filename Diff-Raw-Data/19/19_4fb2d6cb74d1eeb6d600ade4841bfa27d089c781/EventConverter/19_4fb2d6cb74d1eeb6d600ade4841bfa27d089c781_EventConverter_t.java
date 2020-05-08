 package org.ucam.srcf.assassins.storage.hibernate;
 
 import java.util.Set;
 
 import org.ucam.srcf.assassins.domain.JodaDateTimeConverter;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Sets;
 
final class EventConverter {
   private EventConverter() {
   }
 
   static Event convertProtoToStorage(org.ucam.srcf.assassins.proto.Event event) {
     Preconditions.checkNotNull(event);
     Event domainEvent = new Event();
     if (event.hasId()) {
       domainEvent.setId(event.getId());
     }
     domainEvent.setEventTime(JodaDateTimeConverter.toJodaDateTime(event.getEventTime()));
     Set<StateChange> stateChanges = Sets.newHashSet();
     for (org.ucam.srcf.assassins.proto.StateChange change : event.getStateChangeList()) {
       stateChanges.add(StateChangeConverter.convertProtoToStorage(change));
     }
     domainEvent.setStateChanges(stateChanges);
     return domainEvent;
   }
 
   static org.ucam.srcf.assassins.proto.Event convertStorageToProto(Event event) {
     Preconditions.checkNotNull(event);
 
     org.ucam.srcf.assassins.proto.Event.Builder eventBuilder = org.ucam.srcf.assassins.proto.Event.newBuilder();
     eventBuilder.setId(event.getId());
     eventBuilder.setCreationTime(JodaDateTimeConverter.toDomainDateTime(event.getCreationTime()));
     eventBuilder.setEventTime(JodaDateTimeConverter.toDomainDateTime(event.getEventTime()));
 
     for (StateChange stateChange : event.getStateChanges()) {
       eventBuilder.addStateChange(StateChangeConverter.convertStorageToProto(stateChange));
     }
 
     return eventBuilder.build();
   }
 }
