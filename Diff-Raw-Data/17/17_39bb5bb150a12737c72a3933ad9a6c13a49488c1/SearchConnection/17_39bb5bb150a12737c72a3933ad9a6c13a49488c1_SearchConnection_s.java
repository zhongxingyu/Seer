 package pidsearch;
 
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 
 /** Searches the connection.
  * 
  * This class provides the main application logic -- searching the connection. 
  *
  * @author jethro
  */
 public class SearchConnection {
 
     Vertex[] vertices;
     Connection[] connections;
     ConEdge[] edges;
     WalkEdge[] walks;
 
     /** Make a searching object.
      * 
      * This method gets as parametrs objects describing the timetable and a graph
      * of the connections and makes an object for searching connection. Referrences
      * between objects must be already defined when creating a search object.
      * 
      * @param vertices Array of vertices in graph
      * @param connections Array of connections
      * @param edges Array of ConEdges
      * @param walks Array of WalkEdges
      */
     public SearchConnection(Vertex[] vertices,
             Connection[] connections,
             ConEdge[] edges,
             WalkEdge[] walks) {
         this.vertices = vertices;
         this.connections = connections;
         this.edges = edges;
         this.walks = walks;
     }
 
     /**
      * Searches the connection.
      * 
      * This methods gets the search preferences and searched a connection. 
      *
      * @param prefs Search preferences.
      * @return Set&lt Arrival&gt with found unique connections, null if nothing was found.
      */
     public Set<Arrival> searchConnection(SearchPreferences prefs) {
         Vertex from = prefs.from;
         Vertex to = prefs.to;
         Calendar when = prefs.when;
         System.err.println("Searching connection from " + from.name + " to " + to.name);
 
         HashMap<Vertex, Boolean> usedVertex;
 
         usedVertex = new HashMap<Vertex, Boolean>();
         for (Vertex v : vertices) {
             usedVertex.put(v, Boolean.FALSE);
         }
 
         ArrivalComparator ac;
         ac = new ArrivalComparator();
 
         PriorityQueue<Arrival> stubs;
         stubs = new PriorityQueue<Arrival>(100, ac);
         int minute = when.get(Calendar.HOUR_OF_DAY) * 60 + when.get(Calendar.MINUTE);
 
         int wait = 100;
 
         List<ConEdge> departs;
         departs = findDepartures(from, minute, when, wait);
 
         for (ConEdge e : departs) {
             Arrival a;
             a = new Arrival(e);
             if (!stubs.contains(a)) {
                 System.err.println("At "+e.from.name+", adding "+e.connection.name);
                 stubs.add(a);
             }
         }
 
         for (WalkEdge v : from.walks) {
             departs = findDepartures(v.to, minute, when, wait);
             for (ConEdge e : departs) {
                 Arrival a;
                 a = new Arrival(e);
                 if (!stubs.contains(a)) {
                     stubs.add(a);
                 }
             }
         }
 
         usedVertex.put(from, Boolean.TRUE);
 
         Arrival first;
         boolean found;
         found = false;
 
         while (!found) {
             first = stubs.poll();
             if (first == null) {
                 return null;
             }
             
             if (usedVertex.get(first.edge.to))
                 continue;
             
             System.err.println(first.arrival+
                     " "+((ConEdge)first.edge).connection.name+
                     " from "+first.edge.from.name+
                     " to "+first.edge.to.name);
             if (first.edge.to.name.equals(to.name)){
                 found=true;
                 stubs.add(first);
             }
          
 
             departs = findDepartures(first.edge.to, first.arrival, when, wait);
 
             for (ConEdge e : departs) {
 
                 if (e.to.name.equals(to.name)) { 
                     found = true;
                 }
                 if (!usedVertex.get(e.to)) {
                     Arrival a;
                     a = new Arrival(first, e);
                     if (!stubs.contains(a)) {
                         stubs.add(a);
                     }
                 }   
 
             }
 
             for (WalkEdge e : first.edge.to.walks) {
                 if (e.to.name.equals(to.name)) {
                     found = true;
                 }
                 if (!usedVertex.get(e.to)) {
                     Arrival a;
                     a = new Arrival(first,e,prefs);
                     if (!stubs.contains(a)) {
                         stubs.add(a);
                     }
                 }
             }
             usedVertex.put(first.edge.to, Boolean.TRUE);
 
         }
 
         return toFound(stubs, to);
     }
 
     /**
      * Finds departures from given station in given interval.
      *
      * @param from Station to find departures from.
      * @param minute Minute from midnight, in which start to searching connections.
      * @param date Calendar object with date when search connection (if it goes at that date).
      * @param range Search departures until minute+range.
      * @return List of ConEdges starting at from Vertex. If there are no departures in specified range,
      * returns empty list.
      */
     public List<ConEdge> findDepartures(Vertex from, int minute, Calendar date, int range) {
         List<ConEdge> departs;
         departs = from.departs;
 
         ConEdge whenEdge;
         whenEdge = new ConEdge();
 
         DepartComparator dc;
         dc = new DepartComparator();
 
         int minIndex;
         whenEdge.departure = minute;
         minIndex = Collections.binarySearch(departs, whenEdge, dc);
        minIndex = (minIndex < 0) ? -(minIndex + 1) : minIndex;
 
         int maxIndex;
         whenEdge.departure = minute + range;
         maxIndex = Collections.binarySearch(departs, whenEdge, dc);
        maxIndex = (maxIndex < 0) ? -(maxIndex + 1) : maxIndex;
 
         List<ConEdge> departEdges;
         departEdges = new LinkedList<ConEdge>();
         if (minIndex > maxIndex) {
             return departEdges;
         }
         for (ConEdge e : departs.subList(minIndex, maxIndex)) {
             if (e.connection.goesAt(date)) {
                 departEdges.add(e);
             }
         }
         return departEdges;
     }
     
     /** Convert PriorityQueue from searching a connection to Set of Arrivals.
      * 
      * This method gets the PriorityQueue from searching the connection as it 
      * was while connection was found and then selects Arrival objects, whose
      * last edge goes to the station with same name as searches. Theese arrival
      * objects are inserted into the Set, which is returned, so every item in
      * the set represents one found connection.
      * 
      * @param stubs Priority queue from searching a connection
      * @param to Vertex with the name of the final station.
      * @return Set of Arrivals, whose ends in the final station.
      */
     private Set<Arrival> toFound(PriorityQueue<Arrival> stubs, Vertex to) {
         Set<Arrival> found;
         found = new HashSet<Arrival>();
         for (Arrival stub : stubs) {
             if (stub.edge.to.name.equals(to.name)) {
                     found.add(stub);
             }
         }
         return found;
     }
 }
