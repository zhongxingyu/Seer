 package org.easy.scrum.controller.day;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.easy.jsf.d3js.burndown.IterationBurndown;
 import org.easy.scrum.model.BurnDownType;
 import org.easy.scrum.model.SprintBE;
 import org.easy.scrum.model.SprintDayBE;
 import org.joda.time.LocalDate;
 
 public class GraphHelper {
     public IterationBurndown recalcualteBurndown(
             final SprintBE sprint, 
             final List<SprintDayBE> days,
             final BurnDownType burnDownType) {
         IterationBurndown burnDown = null;
         if (sprint != null && days != null) {
             burnDown = new IterationBurndown(
                 new LocalDate(sprint.getStart()), 
                 new LocalDate(sprint.getEnd()), 
                sprint.getPlannedHours(), false);
 
             List<SprintDayBE> elements = new ArrayList<SprintDayBE>(days);
             Collections.reverse(elements);
 
             for (SprintDayBE day : elements) {
                 if (burnDownType == BurnDownType.UP_SCALING_BEFORE_BURN_DOWN) {
                     addUpscaling(day, burnDown);
                 }
                 burnDown.addDay(
                             new LocalDate(day.getDay()), 
                             day.getBurnDown(), day.getComment());
                 if (burnDownType == BurnDownType.BURN_DOWN_BEFORE_UP_SCALING) {
                     addUpscaling(day, burnDown);
                 }
             }
         }
         return burnDown;
     }
 
     private void addUpscaling(SprintDayBE day, IterationBurndown burnDown) {
         if (day.getUpscaling() > 0) {
             burnDown.addDay(
                     new LocalDate(day.getDay()), 
                     day.getUpscaling() * (-1), day.getReasonForUpscaling());
         }
     }
 }
