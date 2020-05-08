 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.nwu.bioinformatics.commons.ComparisonUtils;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.LinkedHashMap;
 
 /**
  * @author Rhett Sutphin
  */
 public class ManagePeriodEventsCommand {
     private Period period;
 
     /** Representation of the activity/day grid.
      * Map keys are activity IDs,
      * values are lists of counts of the number of times that activity occurs on that day
      * (list index is day number, zero-based).
      */
     private List<GridRow> grid;
 
     public ManagePeriodEventsCommand(Period period) {
         this.period = period;
         grid = createGrid(period);
     }
 
     /**
      * Set up the initial grid
      */
     private List<GridRow> createGrid(Period src) {
         Map<String, GridRow> bins = new LinkedHashMap<String, GridRow>();
         Integer dayCount = src.getDuration().getDays();
         for (PlannedEvent event : src.getPlannedEvents()) {
             String binKey = GridRow.key(event);
             if (!bins.containsKey(binKey)) {
                 bins.put(binKey, new GridRow(event.getActivity(), event.getDetails(), dayCount));
             }
             bins.get(binKey).incrementDay(event.getDay());
         }
         return new ExpandingList<GridRow>(
             new GridFiller(dayCount),
             new LinkedList<GridRow>(bins.values()));
     }
 
     /**
      * Apply any changes in the grid to the period in the command.
      */
     public void apply() {
         List<GridRow> existingGrid = createGrid(period);
         Map<String, GridRow> difference = new LinkedHashMap<String, GridRow>();
         // Initialize difference from existing events
        for (GridRow existing : existingGrid) {
            difference.put(existing.key(), existing);
         }
         // calculate difference from bound grid
         for (GridRow bound : getGrid()) {
             if (difference.containsKey(bound.key())) {
                 // if this is an update to an existing row calculate the difference
                 GridRow diffRow = difference.get(bound.key());
                 for (int i = 0; i < bound.getCounts().size(); i++) {
                     Integer newCount = bound.getCounts().get(i);
                    diffRow.getCounts().set(i, newCount - diffRow.getCounts().get(i));
                 }
             } else {
                 // otherwise, it is all new
                 difference.put(bound.key(), bound);
             }
         }
         // add/remove events according to difference
         for (GridRow diffRow : difference.values()) {
             for (int i = 0; i < diffRow.getCounts().size(); i++) {
                 int diff = diffRow.getCounts().get(i);
                 int day = i + 1;
                 while (diff < 0) {
                     removeEvent(diffRow, day);
                     diff++;
                 }
                 while (diff > 0) {
                     addEvent(diffRow, day);
                     diff--;
                 }
             }
         }
     }
 
     private void addEvent(GridRow row, int day) {
         PlannedEvent newEvent = new PlannedEvent();
         newEvent.setDay(day);
         newEvent.setActivity(row.getActivity());
         newEvent.setDetails(row.getDetails());
         period.addPlannedEvent(newEvent);
     }
 
     private void removeEvent(GridRow row, int day) {
         for (Iterator<PlannedEvent> it = period.getPlannedEvents().iterator(); it.hasNext();) {
             PlannedEvent event = it.next();
             if (row.matchesEvent(event) && day == event.getDay()) {
                 it.remove();
                 break;  // only remove one
             }
         }
     }
 
     public List<GridRow> getGrid() {
         return grid;
     }
 
     public Period getPeriod() {
         return period;
     }
 
     private static class GridFiller implements ExpandingList.Filler<GridRow> {
         private int length;
 
         public GridFiller(int length) {
             this.length = length;
         }
 
         public GridRow createNew(int index) {
             return new GridRow(length);
         }
     }
 
     public static class GridRow {
         private Activity activity;
         private List<Integer> counts;
         private String details;
 
         public GridRow(int length) {
             counts = new ArrayList<Integer>(length);
             while (counts.size() < length) {
                 counts.add(0);
             }
         }
         
         public GridRow(Activity activity, String details, int length) {
             this(length);
             setActivity(activity);
             setDetails(details);
         }
 
         ////// LOGIC
 
         public void incrementDay(Integer day) {
             int curVal = getCounts().get(day - 1);
             getCounts().set(day - 1, curVal + 1);
         }
 
         public void decrementDay(Integer day) {
             int curVal = getCounts().get(day - 1);
             getCounts().set(day - 1, curVal - 1);
         }
 
         public String key() {
             return key(getActivity(), getDetails());
         }
 
         private static String key(Activity activity, String details) {
             return activity.getId() + details;
         }
 
         public static String key(PlannedEvent event) {
             return key(event.getActivity(), event.getDetails());
         }
 
         public boolean matchesEvent(PlannedEvent event) {
             return event.getActivity().equals(getActivity())
                 && ComparisonUtils.nullSafeEquals(event.getDetails(), getDetails());
         }
 
         ////// BOUND PROPERTIES
 
         public Activity getActivity() {
             return activity;
         }
 
         public void setActivity(Activity activity) {
             this.activity = activity;
         }
 
         public List<Integer> getCounts() {
             return counts;
         }
 
         public void setCounts(List<Integer> counts) {
             this.counts = counts;
         }
 
         public String getDetails() {
             return details;
         }
 
         public void setDetails(String details) {
             this.details = details;
         }
     }
 }
