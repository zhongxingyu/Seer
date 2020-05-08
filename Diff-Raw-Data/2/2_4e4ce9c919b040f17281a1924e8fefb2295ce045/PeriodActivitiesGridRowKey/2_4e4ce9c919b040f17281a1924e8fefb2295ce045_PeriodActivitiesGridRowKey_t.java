 package edu.northwestern.bioinformatics.studycalendar.web.template.period;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.tools.BeanPropertyListComparator;
 import org.apache.commons.collections.comparators.NullComparator;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.TreeSet;
 
 /**
  * @author Rhett Sutphin
 */
 public class PeriodActivitiesGridRowKey implements Comparable<PeriodActivitiesGridRowKey> {
         private final Logger log = LoggerFactory.getLogger(getClass());
 
     private static final BeanPropertyListComparator<PeriodActivitiesGridRowKey> COMPARATOR
         = new BeanPropertyListComparator<PeriodActivitiesGridRowKey>().
             addProperty("details", new NullComparator(String.CASE_INSENSITIVE_ORDER)).
             addProperty("condition", new NullComparator(String.CASE_INSENSITIVE_ORDER)).
             addProperty("comparableLabels", new NullComparator(String.CASE_INSENSITIVE_ORDER));
 
     private Integer activityId;
     protected String details;
     protected String condition;
     protected Collection<String> labels;
     protected Integer weight;
 
     public static PeriodActivitiesGridRowKey create(Activity activity) {
         return new PeriodActivitiesGridRowKey(
            activity.getId(), null, null, Collections.<String>emptySet(), null
         );
     }
 
     public static PeriodActivitiesGridRowKey create(PlannedActivity plannedActivity) {
         if (plannedActivity.getActivity().getId() == null) {
             throw new StudyCalendarError("Cannot build a useful key if the activity has no ID");
         }
         return new PeriodActivitiesGridRowKey(
             plannedActivity.getActivity().getId(),
             plannedActivity.getDetails(),
             plannedActivity.getCondition(),
             plannedActivity.getLabels(),
             plannedActivity.getWeight()
         );
     }
 
     public PeriodActivitiesGridRowKey(Integer activityId, String details, String condition, Collection<String> labels, Integer weight) {
         this.activityId = activityId;
         this.details = details;
         this.condition = condition;
         this.labels = new TreeSet<String>(labels);
         this.weight = weight;
     }
 
     public Integer getActivityId() {
         return activityId;
     }
 
     public String getDetails() {
         return details;
     }
 
     public String getCondition() {
         return condition;
     }
 
     public Collection<String> getLabels() {
         return labels;
     }
 
     public Integer getWeight() {
         return weight;
     }
 
     public String getComparableLabels() {
         if (labels.size() == 0) {
             return null;
         } else {
             return StringUtils.join(getLabels().iterator(), "|");
         }
     }
 
     @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
     public int compareTo(PeriodActivitiesGridRowKey other) {
         return COMPARATOR.compare(this, other);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof PeriodActivitiesGridRowKey)) return false;
 
         PeriodActivitiesGridRowKey key = (PeriodActivitiesGridRowKey) o;
 
         if (activityId != null ? !activityId.equals(key.activityId) : key.activityId != null)
             return false;
         if (condition != null ? !condition.equals(key.condition) : key.condition != null)
             return false;
         if (details != null ? !details.equals(key.details) : key.details != null) return false;
         if (weight != null ? !weight.equals(key.weight) : key.weight != null) return false;
         String thisLabels = getComparableLabels();
         String otherLabels = getComparableLabels();
         return !(thisLabels != null ? !thisLabels.equals(otherLabels) : otherLabels != null);
     }
 
     @Override
     public int hashCode() {
         int result;
         result = (activityId != null ? activityId.hashCode() : 0);
         result = 31 * result + (details != null ? details.hashCode() : 0);
         result = 31 * result + (condition != null ? condition.hashCode() : 0);
         String thisLabels = getComparableLabels();
         result = 31 * result + (thisLabels != null ? thisLabels.hashCode() : 0);
         result = 31 * result + (weight != null ? weight.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return new StringBuilder().
             append(getActivityId()).
             append(getDetails()).
             append(getCondition()).
             append(getComparableLabels()).
             append(getWeight()).
             toString();
     }
 }
