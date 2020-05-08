 package org.easy.scrum.controller.sprint;
 
 import java.io.Serializable;
 import java.util.List;
 import org.easy.jsf.d3js.burndown.IterationBurndown;
 import org.easy.scrum.controller.day.GraphHelper;
 import org.easy.scrum.model.BurnDownType;
 import org.easy.scrum.model.SprintBE;
 import org.easy.scrum.model.SprintDayBE;
 import org.easy.scrum.model.TeamBE;
 
 public class SprintOverview implements Serializable {
     private static final GraphHelper GH = new GraphHelper();
     private final IterationBurndown burnDown;
     private final TeamBE team;
     private final SprintBE sprint;
     
     private final double neededBurndown;
     private final double avgBurndown;
     private final double plannedHoursByStoryPoint;
     private final double doneHoursByStoryPoint;
     
     public SprintOverview(TeamBE team, SprintBE sprint, List<SprintDayBE> days, BurnDownType burnDownType) {
         super();
         this.team = team;
         this.sprint = sprint;
         this.burnDown = GH.recalcualteBurndown(sprint, days, burnDownType);
         
        neededBurndown = burnDown.getWorkDays() > 0 ? burnDown.getHoursRemaining() / burnDown.getWorkDays() : 0;
        avgBurndown = days.size() > 0 ? burnDown.getHoursDone() / days.size() : 0;
        plannedHoursByStoryPoint = sprint.getStoryPoints() > 0 ? sprint.getPlannedHours() / sprint.getStoryPoints() : 0;
        doneHoursByStoryPoint = sprint.getStoryPoints() > 0 ? burnDown.getHoursDone() / sprint.getStoryPoints() : 0;
     }
 
     public IterationBurndown getBurnDown() {
         return burnDown;
     }
 
     public TeamBE getTeam() {
         return team;
     }
 
     public SprintBE getSprint() {
         return sprint;
     }
 
     public double getNeededBurndown() {
         return neededBurndown;
     }
 
     public double getAvgBurndown() {
         return avgBurndown;
     }
 
     public double getPlannedHoursByStoryPoint() {
         return plannedHoursByStoryPoint;
     }
 
     public double getDoneHoursByStoryPoint() {
         return doneHoursByStoryPoint;
     }
 }
