 /*
  * ------------------------------------------------------------------
  *             (C) Copyright 2013, EVAN GATES
  *                     ALL RIGHTS RESERVED
  *             THIS NOTICE DOES NOT IMPLY PUBLICATION
  * ------------------------------------------------------------------
  */
 package net.thoughtmerge.eventsourcing;
 
 import java.util.ArrayList;
 import java.util.List;
 import net.thoughtmerge.domain.Aggregate;
 import net.thoughtmerge.eventsourcing.impl.CachingReflectionCommandDispatcher;
 import net.thoughtmerge.eventsourcing.impl.CachingReflectionEventDispatcher;
 
 /**
  *
  * @author evan.gates
  */
 public abstract class EventSourcedAggregate extends Aggregate {
   
   private final List<Event> changes;
   
   private final static EventDispatcher EVENT_DISPATCHER = new CachingReflectionEventDispatcher();
   private final static CommandDispatcher COMMAND_DISPATCHER = new CachingReflectionCommandDispatcher();
   
   protected EventSourcedAggregate() {
    this(new ArrayList<Event>());
   }
   
   protected EventSourcedAggregate(Iterable<? extends Event> events) {
     
     this.changes = new ArrayList<>();
     
     this.handle(events);
   }
   
   protected final Iterable<Event> handle(Command command) {
     return COMMAND_DISPATCHER.dispatch(this, command);
   }
   
   protected final void handle(Iterable<? extends Event> events) {
     for (Event event : events) {
       EVENT_DISPATCHER.dispatch(this, event);
       this.changes.add(event);
     }
   }
 }
