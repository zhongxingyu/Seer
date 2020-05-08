 package org.apache.mahout.graph.components;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.mahout.graph.model.GenericGraphElement;
 import org.apache.mahout.graph.model.Membership;
 import org.apache.mahout.graph.model.RepresentativeEdge;
 import org.apache.mahout.graph.model.Zone;
 import org.apache.mahout.graph.model.ZoneAssignment;
 
 /**
  * Container for the {@link FindComponentsJob} {@link Mapper} and
  * {@link Reducer} classes.
  * 
  */
 public class FindComponents {
 
   /**
    * Scatters zone assignments and edges. This {@link Mapper} has two inputs:
    * 
    * <ul>
    * <li>edge file</li>
    * <li>zone file</li>
    * </ul>
    * 
    * Forward the zone assignments.<br />
    * Forward the edges to each of the vertices.
    */
   public static class ScatterEdgesToEachNodeAndForwardZoneAssignments extends
       Mapper<Membership, GenericGraphElement, Membership, GenericGraphElement> {
     @Override
     public void map(Membership key, GenericGraphElement generic, Context ctx)
         throws IOException, InterruptedException {
       if (generic.getValue() instanceof RepresentativeEdge) {
         RepresentativeEdge e = (RepresentativeEdge) generic.getValue();
         // write the edge to each vertex
         ctx.write(new Membership().addMember(e.getVertex0()), generic);
         ctx.write(new Membership().addMember(e.getVertex1()), generic);
       }
       if (generic.getValue() instanceof Zone) {
         // write the zone assignment to the reducer
         ctx.write(key, generic);
       }
     }
   }
 
   /**
    * Joins zones and edges. Input is a {@link ZoneAssignment} and a set of edges
    * the incident to the vertex that is assigned with the assignment.
    * <p>
    * This class joins {@link Zone} to all the edges of the input, outputting the
   * {@Zone} as value, binned under the membership set of the edge.
    */
   public static class AssignOneZoneToEdges extends
       Reducer<Membership, GenericGraphElement, Membership, GenericGraphElement> {
     @Override
     public void reduce(Membership key, Iterable<GenericGraphElement> generics,
         Context ctx) throws IOException, InterruptedException {
       ZoneAssignment z = null;
       // TODO implement the lineage flag to avoid memory consumption
       List<RepresentativeEdge> edges = new LinkedList<RepresentativeEdge>();
       for (GenericGraphElement generic : generics) {
         if (generic.getValue() instanceof ZoneAssignment) {
           z = (ZoneAssignment) generic.getValue();
         }
         if (generic.getValue() instanceof RepresentativeEdge) {
           edges.add(RepresentativeEdge.duplicate((RepresentativeEdge) generic
               .getValue()));
         }
       }
       for (RepresentativeEdge edge : edges) {
         ctx.write(Membership.factorize(edge),
             new GenericGraphElement(z.getZone()));
       }
     }
   }
 
   /**
    * Find the minimum zone for each edge.
    * <p>
    * For each other zone, output an interzone edge, which is a record with key
    * other zone and value minimum zone that is to be assigned.
    */
   public static class FindInterzoneEdges extends
       Reducer<Membership, GenericGraphElement, Zone, GenericGraphElement> {
     @Override
     public void reduce(Membership key, Iterable<GenericGraphElement> generics,
         Context ctx) throws IOException, InterruptedException {
       Set<Zone> zones = new TreeSet<Zone>();
       for (GenericGraphElement generic : generics) {
         zones.add((Zone) generic.getValue());
       }
       Zone min = zones.iterator().next();
       zones.remove(min);
       for (Zone other : zones) {
         ctx.write(other, new GenericGraphElement(min));
       }
     }
   }
 }
