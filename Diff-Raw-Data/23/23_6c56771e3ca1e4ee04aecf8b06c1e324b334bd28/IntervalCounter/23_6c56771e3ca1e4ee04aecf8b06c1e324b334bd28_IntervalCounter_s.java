 package core.todo;
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import org.joda.time.*;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Class encapsulating count details of entities of type T, covering a specific interval length of certain interval type.
  * Note that for a MONTHLY IntervalCounter, the interval points are created at the beginning of the month and
  * all counts are assumed to be on the first day of the month.
  * <p/>
  * For example, assume a query which returns monthly counts of certain entity T covering last 12 months. This may return
  * the following results (assuming we are in Sep 2012):
  * <p/>
  * YEAR, MONTH, COUNT(*)
  * 2011	 9	    1,912
  * 2011	 10	    2,857
  * 2011	 11	    2,768
  * 2011	 12	    1,932
  * 2012	 1	    2,286
  * 2012	 2	    2,901
  * 2012	 3	    3,569
  * 2012	 4	    3,547
  * 2012	 5	    4,289
  * 2012	 6	    3,330
  * 2012	 7	    3,311
  * 2012	 8	    2,895
  * 2012	 9	    660
  * <p/>
  * A MONTHLY IntervalCounter holding these interval point results would assume the first day of the month for each
  * monthly interval point.  Note that this doesn't apply to DAILY IntervalCounters where the date of the count is
  * stored as is.
  */
 public class IntervalCounter<T> implements Serializable {
     public enum IntervalType {
         MONTHLY, DAILY;
     }
 
     public enum RenderType {
         SPLINE("spline"),
         COLUMN("column");
 
         private String type;
 
         private RenderType(String type) {
             this.type = type;
         }
 
         private String getType() {
             return type;
         }
     }
 
     /**
      * entities which this count object hold details of.
      */
     private List<T> countedEntities;
 
     /**
      * optional and if null then one will be calculated from the toString() representation of counted entities.
      */
     private String label;
 
     /**
      * the interval type of this count object
      */
     private IntervalType intervalType;
     /**
      * the interval length of this count object
      */
     private int intervalLength;
     /**
      * the interval points of this count object. This is automatically calculated based on
      * the intervalType and intervalLength variables.
      */
     private Map<LocalDate, Integer> intervalPoints;
 
     /**
      * Constructs a count object to hold count details about given countedEntities covering the given intervalLength of
      * type intervalType. For example,
      * 1) countedEntities=[T], intervalType='MONTHLY', intervalLength=12 -->
      * a count object holding details about entity T1 covering the last 12 month
      * 2) countedEntities=[R1, R2], intervalType='DAILY', intervalLength=30 -->
      * a count object holding details about entities of type R, covering the last 30 days
      * <p/>
      * A comma separated String is formed by concatenating the result of calling the toString() method on each
      * countedEntity to form the series label of the interval. If you wish to use a specific label, then call the
      * other constructor.
      *
      * @param countedEntities the entities which this object hold details about.
      * @param intervalType    The interval type of this count object
      * @param intervalLength  The interval length that this count object covers.
      */
     public IntervalCounter(List<T> countedEntities, IntervalType intervalType, int intervalLength) {
         this(null, countedEntities, intervalType, intervalLength);
     }
 
     public IntervalCounter(String label, List<T> countedEntities, IntervalType intervalType, int intervalLength) {
         this.label = label;
         this.countedEntities = countedEntities;
         this.intervalType = intervalType;
         this.intervalLength = intervalLength;
         prepareIntervalPointSlots();
     }
 
     /*
         work out the interval point slots.
         Note that for time series graphs, all plotted series data need to cover the same interval length and must
         define data for the same interval points. However, the database query is not guaranteed to return data for
         all interval points. For example, if there are no counts for an entity T on a given date, then the database query
         will not return a count for that date (remember query uses group by clause). However, in the graph, we need to
         still be able to plot that date with count=0. Hence, the solution is to pre-construct the interval point slots,
         which will subsequently be filled with count details for each point.
      */
     private void prepareIntervalPointSlots() {
         this.intervalPoints = new TreeMap<LocalDate, Integer>();
        LocalDate localDate = LocalDate.now();
         if (intervalType == IntervalType.MONTHLY) {
             for (int i = intervalLength; i > 0; i--) {
                 LocalDate prevPoint = localDate.minusMonths(i).withDayOfMonth(1);
                 intervalPoints.put(prevPoint, 0);
             }
         } else if (intervalType == IntervalType.DAILY) {
             for (int i = intervalLength; i > 0; i--) {
                 LocalDate prevPoint = localDate.minusDays(i);
                 intervalPoints.put(prevPoint, 0);
             }
         }
     }
 
     public void addIntervalPoint(Integer year, Integer month, Integer day, Integer count) {
         LocalDate point = makeIntervalPointDate(year, month, day);
         intervalPoints.put(point, count);
     }
 
     private LocalDate makeIntervalPointDate(Integer year, Integer month, Integer day) {
         //missing days default to beginning of month
        LocalDate point = new LocalDate(year, month, day == null ? 1 : day);
         return point;
     }
 
     public List<T> getCountedEntities() {
         return countedEntities;
     }
 
     private Map<LocalDate, Integer> getIntervalPoints() {
         return intervalPoints;
     }
 
     //TODO tighten
     private boolean isWeekEnd(LocalDate localDate, Locale locale) {
         if (locale.equals(Locale.UK)) {
             int dayNumber = localDate.dayOfWeek().get();
             //Joda-Time uses the ISO standard Monday to Sunday week. So weekend for uk is Sat/Sun (6/7)
             return dayNumber == 6 || dayNumber == 7;
         } else {
             //currently we do not support other locales so return false
             return false;
         }
     }
 
     /**
      * returns data in following form:
      * {
      * "name" : seriesName which is this counter's label and if missing then one is calculated by concatenating
      * toString() of individual countedEntities,
      * "type" : renderType,
      * "data" : depends on RenderType. For RenderType.SPLINE it is of the form '[[intervalPointDate formatted as
      * milliseconds since epoch, intervalPointCount], ...]'
      * }
      * <p/>
      * example returned result for RenderType.SPLINE:
      * {
      * "name":"ACCEPT",
      * "type":"spline",
      * "data":[[1314831600000,1912],[1317423600000,2857],[1320105600000,2768],[1322697600000,1932],[1325376000000,2286],
      * [1328054400000,2901],[1330560000000,3569],[1333234800000,3547],[1335826800000,4289],[1338505200000,3330],
      * [1341097200000,3311],[1343775600000,2895],[1346454000000,684]]
      * }
      *
      * @param locale       used to workout weekends
      * @param skipWeekEnds flag to exclude interval points falling on weekends
      * @return
      */
     //todo tighten - depends on the chosen graph library
     public Map toJSONStructure(Locale locale, boolean skipWeekEnds, RenderType renderType) {
         Map<String, Object> jsonObject = new HashMap<String, Object>();
         String seriesLabel = label;
         if (Strings.isNullOrEmpty(seriesLabel)) {
             List<String> stringRepOfCountedEntities = Lists.transform(getCountedEntities(), new Function<T, String>() {
                 public String apply(T entity) {
                     return entity.toString();
                 }
             });
             seriesLabel = Joiner.on(",").join(stringRepOfCountedEntities);
         }
 
         List<Object> seriesData = new ArrayList<Object>();
         for (LocalDate ld : getIntervalPoints().keySet()) {
             if (skipWeekEnds && isWeekEnd(ld, locale)) {
                 continue;
             }
 
             List<Object> arr = new ArrayList<Object>();
             //note joda has 1-based months but javascript and java use 0-based months, we correct this on client
 //            arr.put(DateUtil.makeCommaSeparatedDateString(ld.toDate()));
 
             //using millis avoids the need to translate months/dates on client side but note use of UTC which is important
             arr.add(ld.toDateMidnight(DateTimeZone.UTC).getMillis());
             arr.add(getIntervalPoints().get(ld));
             seriesData.add(arr);
         }
         //name, type, and data attributes are what is expected by HighCharts.js - do not change
         jsonObject.put("name", seriesLabel);
         jsonObject.put("type", renderType.getType());
         jsonObject.put("data", seriesData);
 
 //        System.out.println(jsonObject.toString());
         return jsonObject;
     }
 
     public IntervalCounter add(IntervalCounter other) {
         return add(other, this.label);
     }
 
     public IntervalCounter add(IntervalCounter other, String newLabel) {
         Preconditions.checkArgument(this.intervalType == other.intervalType, "IntervalCounters are not equivalent");
         Preconditions.checkArgument(this.intervalLength == other.intervalLength, "IntervalCounters are not equivalent");
 
         this.label = newLabel;
         Map<LocalDate, Integer> otherPoints = other.getIntervalPoints();
         Map<LocalDate, Integer> thisPoints = getIntervalPoints();
         for (LocalDate ld : otherPoints.keySet()) {
             Integer combinedCount = otherPoints.get(ld) + thisPoints.get(ld);
             thisPoints.put(ld, combinedCount);
         }
         return this;
     }
 }
