 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.neuron.shell;
 
import com.google.common.collect.Sets;
 import de.weltraumschaf.commons.IO;
 import de.weltraumschaf.neuron.node.Event;
 import de.weltraumschaf.neuron.node.Event.Type;
 import de.weltraumschaf.neuron.node.Node;
 import java.util.Observable;
 import java.util.Observer;
import java.util.Set;
 
 /**
  * Handles events emitted by {@link Observable}.
  *
  * @author "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 public class EventHandler implements Observer {
 
     /**
      * Used to print event to output.
      */
     private final IO io;
 
     /**
      * Contains filtered node ids.
      */
    private final Set<Integer> filteredNodes = Sets.newHashSet();
 
     /**
      * Contains filtered event types.
      */
    private final Set<Event.Type> filteredTypes = Sets.newHashSet();
 
     /**
      * Dedicated constructor.
      *
      * @param io used for output
      */
     public EventHandler(final IO io) {
         super();
         this.io = io;
     }
 
     @Override
     public void update(final Observable o, final Object arg) {
         final Event event = (Event) arg;
         final Node source = (Node) o;
 
         if (shouldPrint(source, event)) {
             io.println(String .format("Event from %d: %s", source.getId(), event.toString()));
         }
     }
 
     /**
      * Determines if an event should be printed or not.
      *
      * Filters may be:
      * - by event source
      * - by event type
      *
      * @param source used to filter by
      * @param event event to check
      * @return true if filter does not match, else true
      */
     private boolean shouldPrint(final Node source, final Event event) {
         if (filterNode(source)) {
             return false;
         }
 
         if (filterType(event.getType())) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Checks if a node is filtered by its id.
      *
      * @param source node to check
      * @return true if node is filtered else false
      */
     private boolean filterNode(final Node source) {
         return filteredNodes.contains(Integer.valueOf(source.getId()));
     }
 
     /**
      * Checks if event type is filtered.
      *
      * @param type type to check
      * @return true if type is filtered else false
      */
     private boolean filterType(Type type) {
         return filteredTypes.contains(type);
     }
 
 }
