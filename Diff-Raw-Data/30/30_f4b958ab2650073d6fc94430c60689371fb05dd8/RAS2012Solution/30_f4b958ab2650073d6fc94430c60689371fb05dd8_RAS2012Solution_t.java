 package org.drools.planner.examples.ras2012;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.drools.planner.api.domain.solution.PlanningEntityCollectionProperty;
 import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
 import org.drools.planner.core.solution.Solution;
 import org.drools.planner.examples.ras2012.model.MaintenanceWindow;
 import org.drools.planner.examples.ras2012.model.Network;
 import org.drools.planner.examples.ras2012.model.Route;
 import org.drools.planner.examples.ras2012.model.Train;
 import org.drools.planner.examples.ras2012.model.planner.ItineraryAssignment;
 
 public class RAS2012Solution implements Solution<HardAndSoftScore> {
 
     public static final Integer                   PLANNING_HORIZON_MINUTES       = 12 * 60;
 
     public static final double                    PLANNING_TIME_DIVISION_MINUTES = 0.5;
 
     private final String                          name;
     private final Network                         network;
     private final Collection<MaintenanceWindow>   maintenances;
     private final Collection<ItineraryAssignment> assignments                    = new LinkedList<ItineraryAssignment>();
 
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
            this.assignments.add(ia);
         }
         this.setScore(new RAS2012ScoreCalculator().calculateScore(this));
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
             this.assignments.add(a.clone());
         }
         this.setScore(new RAS2012ScoreCalculator().calculateScore(this));
     }
 
     @Override
     public Solution<HardAndSoftScore> cloneSolution() {
         return new RAS2012Solution(this.name, this.network, this.maintenances, this.trains,
                 this.assignments);
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
 
     @PlanningEntityCollectionProperty
     public Collection<ItineraryAssignment> getAssignments() {
         return this.assignments;
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
 
     @Override
     public Collection<? extends Object> getProblemFacts() {
         final Collection<Object> allFacts = new LinkedList<Object>();
         return allFacts;
     }
 
     public Collection<Route> getRoutes() {
         final Collection<Route> r = new LinkedList<Route>();
         r.addAll(this.getNetwork().getAllEastboundRoutes());
         r.addAll(this.getNetwork().getAllWestboundRoutes());
         return r;
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
         builder.append("RAS2012Solution [name=").append(this.name).append(", net=")
                 .append(this.network).append(", maintenances=").append(this.maintenances)
                 .append(", trains=").append(this.trains).append(", score=").append(this.score)
                 .append("]");
         return builder.toString();
     }
 
 }
