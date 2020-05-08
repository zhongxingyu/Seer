 package org.dodgybits.shuffle.android.core.model;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.dodgybits.shuffle.android.core.util.StringUtils;
 import org.dodgybits.shuffle.android.persistence.provider.Shuffle;
 
 import android.text.format.DateUtils;
 import android.util.Log;
 
 public class TaskQuery {
     private static final String cTag = "TaskQuery";
 
     private PredefinedQuery mPredefined; 
     private List<Id> mProjects;
     private List<Id> mContexts;
     private DateRange mStartDateRange;
     private DateRange mDueDateRange;
     private DateRange mCreatedDateRange;
     private Boolean mComplete = null;
     private String mSortOrder;
     
     public final PredefinedQuery getPredefinedQuery() {
         return mPredefined;
     }
     
     public final List<Id> getProjects() {
         return mProjects;
     }
 
     public final List<Id> getContexts() {
         return mContexts;
     }
 
     public final DateRange getStartDateRange() {
         return mStartDateRange;
     }
 
     public final DateRange getDueDateRange() {
         return mDueDateRange;
     }
 
     public final DateRange getCreatedDateRange() {
         return mCreatedDateRange;
     }
 
     public final Boolean isComplete() {
         return mComplete;
     }
     
     public final String getSortOrder() {
         return mSortOrder;
     }
     
     public final boolean isInitialized() {
         return true;
     }
     
     public final String getSelection() {
         List<String> expressions = new ArrayList<String>();
         if (mPredefined != null) {
             expressions.add(predefinedSelection());
         }
         if (mProjects != null) {
             expressions.add(idListSelection(mProjects, Shuffle.Tasks.PROJECT_ID));
         }
         if (mContexts != null) {
             expressions.add(idListSelection(mContexts, Shuffle.Tasks.CONTEXT_ID));
         }
         // TODO add range values
         
         if (mComplete != null) {
             expressions.add(Shuffle.Tasks.COMPLETE + "=" + (mComplete ? "1" : "0"));
         }
         
         return StringUtils.join(expressions, " AND ");
     }
         
     private String predefinedSelection() {
         String result;
         switch (mPredefined) {
             case nextTasks:
                 result = "(complete = 0) AND (" +
                     "   (projectId is null) OR " +
                     "   (projectId IN (select p._id from project p where p.parallel = 1)) OR " +
                     "   (task._id = (select t2._id FROM task t2 WHERE " +
                     "      t2.projectId = task.projectId AND t2.complete = 0 " +
                     "      ORDER BY due ASC, displayOrder ASC limit 1))" +
                     ")";
                 break;
                 
             default:
                 long startMS = 0L;
                 long endOfToday = getEndDate();
                 long endOfTomorrow = endOfToday + DateUtils.DAY_IN_MILLIS;
                 result = "complete = 0" +
                     " AND (due > " + startMS + ")" +
                     " AND ( (due < " + endOfToday + ") OR" +
                     "( allDay = 1 AND due < " + endOfTomorrow + " ) )";
                 break;
         }
         
         return result;
     }
     
     private long getEndDate() {
         long endMS = 0L;
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         switch (mPredefined) {
         case dueToday:
             cal.add(Calendar.DAY_OF_YEAR, 1);
             endMS = cal.getTimeInMillis();
             break;
         case dueNextWeek:
             cal.add(Calendar.DAY_OF_YEAR, 7);
             endMS = cal.getTimeInMillis();
             break;
         case dueNextMonth:
             cal.add(Calendar.MONTH, 1);
             endMS = cal.getTimeInMillis();
             break;
         }
         if (Log.isLoggable(cTag, Log.INFO)) {
             Log.i(cTag, "Due date ends " + endMS);
         }
         return endMS;
     }
     
 
     private String idListSelection(List<Id> ids, String idName) {
         StringBuilder result = new StringBuilder();
         if (ids.size() > 0) {
             result.append(idName)
                 .append(" in (")
                 .append(StringUtils.repeat(ids.size(), "?", ","))
                 .append(')');
         } else {
             result.append(idName)
                 .append(" is null");
         }
         return result.toString();
     }
         
     private void addIdListArgs(List<String> args, List<Id> ids) {
         if (ids != null && ids.size() > 0) {
             for(Id id : ids) {
                 args.add(String.valueOf(id.getId()));
             }
         }
     }
     
     public final String[] getSelectionArgs() {
         List<String> args = new ArrayList<String>();
         addIdListArgs(args, mProjects);
         addIdListArgs(args, mContexts);
 
        return args.size() > 0 ? args.toArray(new String[0]): null;
     }
         
     @Override
     public final String toString() {
         return String.format(
                 "[TaskQuery predefined=%1$s projects=%2$s contexts='%3$s' " +
                 "startDateRange=%4$s dueDateRange=%5$s createdDateRange=%6$s " +
                 "complete=%7$s sortOrder=%8$s]",
                 mPredefined, mProjects, mContexts,
                 mStartDateRange, mDueDateRange, mCreatedDateRange, 
                 mComplete, mSortOrder);
     }
     
     public static Builder newBuilder() {
         return Builder.create();
     }
 
     
     public static class Builder {
 
         private Builder() {
         }
 
         private TaskQuery result;
 
         private static Builder create() {
             Builder builder = new Builder();
             builder.result = new TaskQuery();
             return builder;
         }
         
         public PredefinedQuery getPredefined() {
             return result.mPredefined;
         }
         
         public Builder setPredefined(PredefinedQuery value) {
             result.mPredefined = value;
             return this;
         }
 
         public List<Id> getProjects() {
             return result.mProjects;
         }
 
         public Builder setProjects(List<Id> value) {
             result.mProjects = value;
             return this;
         }
         
         public List<Id> getContexts() {
             return result.mContexts;
         }
 
         public Builder setContexts(List<Id> value) {
             result.mContexts = value;
             return this;
         }
         
         public DateRange getStartDateRange() {
             return result.mStartDateRange;
         }
         
         public Builder setStartDateRange(DateRange value) {
             result.mStartDateRange = value;
             return this;
         }
         
         public DateRange getDueDateRange() {
             return result.mDueDateRange;
         }
         
         public Builder setDueDateRange(DateRange value) {
             result.mDueDateRange = value;
             return this;
         }
 
         public DateRange getCreatedDateRange() {
             return result.mCreatedDateRange;
         }
         
         public Builder setCreatedDateRange(DateRange value) {
             result.mCreatedDateRange = value;
             return this;
         }
         
         public Boolean isComplete() {
             return result.mComplete;
         }
         
         public Builder setComplete(Boolean value) {
             result.mComplete = value;
             return this;
         }
         
         public String getSortOrder() {
             return result.mSortOrder;
         }
         
         public Builder setSortOrder(String value) {
             result.mSortOrder = value;
             return this;
         }
         
         public final boolean isInitialized() {
             return result.isInitialized();
         }
 
         public TaskQuery build() {
             if (result == null) {
                 throw new IllegalStateException(
                         "build() has already been called on this Builder.");
             }
             TaskQuery returnMe = result;
             result = null;
             return returnMe;
         }
         
         public Builder mergeFrom(TaskQuery query) {
             setPredefined(query.mPredefined);
             setProjects(query.mProjects);
             setContexts(query.mContexts);
             setStartDateRange(query.mStartDateRange);
             setDueDateRange(query.mDueDateRange);
             setCreatedDateRange(query.mCreatedDateRange);
             setComplete(query.mComplete);
             setSortOrder(query.mSortOrder);
             return this;
         }
 
     }
 
     public enum PredefinedQuery {
         nextTasks, dueToday, dueNextWeek, dueNextMonth
     }
 
     public final class DateRange {
         private final Date mBegin;
         private final Date mEnd;
         
         public DateRange(Date begin, Date end) {
             mBegin = begin;
             mEnd = end;
         }
         
         public Date getBegin() {
             return mBegin;
         }
         
         public Date getEnd() {
             return mEnd;
         }
         
         @Override
         public final String toString() {
             return String.format(
                     "[DateRange startDate=%1$s endDate='%2$s']",
                     mBegin, mEnd);
         }        
     }
     
     
 }
