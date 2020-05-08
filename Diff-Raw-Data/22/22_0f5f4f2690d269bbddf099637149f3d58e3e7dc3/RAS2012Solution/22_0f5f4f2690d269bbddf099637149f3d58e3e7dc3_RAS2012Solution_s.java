 package org.drools.planner.examples.ras2012;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.TimeUnit;
 
 import org.drools.planner.api.domain.solution.PlanningEntityCollectionProperty;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.core.solution.Solution;
 import org.drools.planner.examples.ras2012.model.Arc;
 import org.drools.planner.examples.ras2012.model.ItineraryAssignment;
 import org.drools.planner.examples.ras2012.model.MaintenanceWindow;
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.util.model.Network;
 import org.drools.planner.examples.ras2012.util.visualizer.GraphVisualizer;
 
 public class RAS2012Solution extends Visualizable implements Solution<HardAndSoftScore> {
 
     private final String                          name;
 
     private final Network                         network;
     private final Collection<MaintenanceWindow>   maintenances;
     private final Map<Train, ItineraryAssignment> assignments = new HashMap<Train, ItineraryAssignment>();
     private final Collection<Train>               trains;
 
     private HardAndSoftScore                      score;
 
     public RAS2012Solution(final String name, final Network net,
             final Collection<MaintenanceWindow> maintenances, final Collection<Train> trains) {
         this.name = name;
         this.network = net;
         this.maintenances = maintenances;
         this.trains = trains;
         /*
          * generate assignments; always pick the best route for the particular train, nevermind if it's used by another train
          * already.
          */
         // TODO separate into a solution initializer, for the sake of clarity
         for (final Train t : this.getTrains()) {
             final ItineraryAssignment ia = new ItineraryAssignment(t, maintenances);
             ia.setRoute(this.getNetwork().getBestRoute(t));
             this.assignments.put(t, ia);
         }
     }
 
     private RAS2012Solution(final String name, final Network net,
             final Collection<MaintenanceWindow> maintenances, final Collection<Train> trains,
             final Collection<ItineraryAssignment> assignments) {
         this.name = name;
         this.network = net;
         this.maintenances = maintenances;
         this.trains = trains;
         // clone assignments
         for (final ItineraryAssignment a : assignments) {
             this.assignments.put(a.getTrain(), a.clone());
         }
     }
 
     @Override
     public Solution<HardAndSoftScore> cloneSolution() {
         final RAS2012Solution solution = new RAS2012Solution(this.name, this.network,
                 this.maintenances, this.trains, this.getAssignments());
         solution.setScore(this.getScore());
         return solution;
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof RAS2012Solution)) {
             return false;
         }
         final RAS2012Solution other = (RAS2012Solution) obj;
         if (this.assignments == null) {
             if (other.assignments != null) {
                 return false;
             }
         } else if (!this.assignments.equals(other.assignments)) {
             return false;
         }
         return true;
     }
 
     public ItineraryAssignment getAssignment(final Train t) {
         return this.assignments.get(t);
     }
 
     @PlanningEntityCollectionProperty
     public Collection<ItineraryAssignment> getAssignments() {
         return this.assignments.values();
     }
 
     public Collection<MaintenanceWindow> getMaintenances() {
         return this.maintenances;
     }
 
     public String getName() {
         return this.name;
     }
 
     public Network getNetwork() {
         return this.network;
     }
 
     public long getPlanningHorizon(final TimeUnit unit) {
        if (this.getName().endsWith("TOY")) {
            // FIXME ugly hack
            return unit.convert(150, TimeUnit.MINUTES);
        } else {
            return unit.convert(12, TimeUnit.HOURS);
         }
     }
 
     @Override
     public Collection<? extends Object> getProblemFacts() {
         final Collection<Object> allFacts = new LinkedList<Object>();
         return allFacts;
     }
 
     @Override
     public HardAndSoftScore getScore() {
         return this.score;
     }
 
     public SortedSet<Train> getTrains() {
         return new TreeSet<Train>(this.trains);
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + (this.assignments == null ? 0 : this.assignments.hashCode());
         return result;
     }
 
     @Override
     public void setScore(final HardAndSoftScore score) {
         this.score = score;
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("RAS2012Solution [name=");
         builder.append(this.name);
         builder.append(", network=");
         builder.append(this.network);
         builder.append(", maintenances=");
         builder.append(this.maintenances);
         builder.append(", assignments=");
         builder.append(this.assignments);
         builder.append(", score=");
         builder.append(this.score);
         builder.append("]");
         return builder.toString();
     }
 
     @Override
     public boolean visualize(final File target) {
         final Collection<Arc> arcs = new HashSet<Arc>();
         for (final ItineraryAssignment ia : this.getAssignments()) {
             arcs.addAll(ia.getRoute().getProgression().getArcs());
         }
         return this.visualize(new GraphVisualizer(arcs), target);
     }
 
 }
