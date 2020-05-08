 package de.objectcode.time4u.client.store.impl.common;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.objectcode.time4u.client.store.api.IStatisticRepository;
 import de.objectcode.time4u.client.store.api.RepositoryException;
 import de.objectcode.time4u.client.store.api.StatisticEntry;
 import de.objectcode.time4u.client.store.api.event.DayInfoRepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.IRepositoryListener;
 import de.objectcode.time4u.client.store.api.event.RepositoryEvent;
 import de.objectcode.time4u.server.api.data.CalendarDay;
 
 public abstract class BaseStatisticRepository implements IStatisticRepository, IRepositoryListener
 {
   private final Map<Integer, Map<String, MutableStatisticEntry>> m_yearCache;
   private final Map<CalendarMonth, Map<String, MutableStatisticEntry>> m_monthCache;
   private final Map<CalendarWeek, Map<String, MutableStatisticEntry>> m_weekCache;
 
   protected BaseStatisticRepository()
   {
     // Cache no more than 5 years
     m_yearCache = new LinkedHashMap<Integer, Map<String, MutableStatisticEntry>>() {
       private static final long serialVersionUID = 2978982921561881053L;
 
       @Override
       protected boolean removeEldestEntry(final Map.Entry<Integer, Map<String, MutableStatisticEntry>> eldest)
       {
         return size() > 5;
       }
     };
 
     // Cache no more than 24 month
     m_monthCache = new LinkedHashMap<CalendarMonth, Map<String, MutableStatisticEntry>>() {
       private static final long serialVersionUID = 2978982921561881053L;
 
       @Override
       protected boolean removeEldestEntry(final Map.Entry<CalendarMonth, Map<String, MutableStatisticEntry>> eldest)
       {
         return size() > 24;
       }
     };
 
     // Cache no more than 54 weeks
     m_weekCache = new LinkedHashMap<CalendarWeek, Map<String, MutableStatisticEntry>>() {
       private static final long serialVersionUID = 2978982921561881053L;
 
       @Override
       protected boolean removeEldestEntry(final Map.Entry<CalendarWeek, Map<String, MutableStatisticEntry>> eldest)
       {
         return size() > 54;
       }
     };
   }
 
   public Map<String, ? extends StatisticEntry> getDayStatistic(final int day, final int month, final int year)
       throws RepositoryException
   {
     final Calendar begin = Calendar.getInstance();
     begin.clear();
     begin.set(year, month - 1, day, 0, 0, 0);
 
     final Calendar end = Calendar.getInstance();
     end.clear();
     end.set(year, month - 1, day, 0, 0, 0);
     end.add(Calendar.DAY_OF_MONTH, 1);
 
     final Map<String, MutableStatisticEntry> statistic = calculateStatistic(new Date(begin.getTimeInMillis()),
         new Date(end.getTimeInMillis()));
 
     return statistic;
   }
 
   public synchronized Map<String, ? extends StatisticEntry> getWeekStatistic(final int week, final int year)
       throws RepositoryException
   {
     final Map<String, MutableStatisticEntry> cachedStatistic = m_weekCache.get(new CalendarWeek(week, year));
 
     if (cachedStatistic != null) {
       return cachedStatistic;
     }
 
     final Calendar begin = Calendar.getInstance();
     begin.clear();
     begin.set(Calendar.YEAR, year);
     begin.set(Calendar.WEEK_OF_YEAR, week);
 
     final Calendar end = Calendar.getInstance();
     end.clear();
     end.set(Calendar.YEAR, year);
     end.set(Calendar.WEEK_OF_YEAR, week);
     end.add(Calendar.WEEK_OF_YEAR, 1);
 
     final Map<String, MutableStatisticEntry> statistic = calculateStatistic(new Date(begin.getTimeInMillis()),
         new Date(end.getTimeInMillis()));
 
     m_weekCache.put(new CalendarWeek(week, year), statistic);
 
     return statistic;
 
   }
 
   public synchronized Map<String, ? extends StatisticEntry> getMonthStatistic(final int month, final int year)
       throws RepositoryException
   {
     final Map<String, MutableStatisticEntry> cachedStatistic = m_monthCache.get(month);
 
     if (cachedStatistic != null) {
       return cachedStatistic;
     }
 
     final Calendar begin = Calendar.getInstance();
     begin.clear();
     begin.set(year, month - 1, 1, 0, 0, 0);
     final Calendar end = Calendar.getInstance();
     end.clear();
     end.set(year, month - 1, 1, 0, 0, 0);
     end.add(Calendar.MONTH, 1);
 
     final Map<String, MutableStatisticEntry> statistic = calculateStatistic(new Date(begin.getTimeInMillis()),
         new Date(end.getTimeInMillis()));
 
     m_monthCache.put(new CalendarMonth(month, year), statistic);
 
     return statistic;
   }
 
   public synchronized Map<String, ? extends StatisticEntry> getYearStatistic(final int year) throws RepositoryException
   {
     final Map<String, MutableStatisticEntry> cachedStatistic = m_yearCache.get(year);
 
     if (cachedStatistic != null) {
       return cachedStatistic;
     }
 
     final Map<String, MutableStatisticEntry> statistic = new HashMap<String, MutableStatisticEntry>();
 
     for (int month = 1; month <= 12; month++) {
       final Map<String, ? extends StatisticEntry> monthStatistic = getMonthStatistic(month, year);
 
       for (final StatisticEntry monthEntry : monthStatistic.values()) {
         MutableStatisticEntry entry = statistic.get(monthEntry.getProjectId());
 
         if (entry == null) {
           entry = new MutableStatisticEntry(monthEntry.getProjectId(), monthEntry.getProjectPath());
 
           statistic.put(monthEntry.getProjectId(), entry);
         }
 
         entry.sum(monthEntry);
       }
     }
 
     m_yearCache.put(year, statistic);
 
     return statistic;
   }
 
   public void handleRepositoryEvent(final RepositoryEvent event)
   {
     switch (event.getEventType()) {
       case DAYINFO:
         final DayInfoRepositoryEvent dayInfoEvent = (DayInfoRepositoryEvent) event;
         final CalendarDay day = dayInfoEvent.getDayInfo().getDay();
         synchronized (this) {
           m_yearCache.remove(day.getYear());
           m_monthCache.remove(new CalendarMonth(day));
           m_weekCache.remove(new CalendarWeek(day));
         }
 
         break;
     }
   }
 
   protected Map<String, MutableStatisticEntry> calculateStatistic(final Date from, final Date until)
       throws RepositoryException
   {
     final Map<String, MutableStatisticEntry> statistic = new HashMap<String, MutableStatisticEntry>();
 
     iterateWorkItems(from, until, new IStatisticCollector() {
       public void collect(final Date day, final int begin, final int end, final String projectId,
           final String projectParentKey, final String taskId)
       {
         MutableStatisticEntry entry = statistic.get(projectId);
 
         if (entry == null) {
           entry = new MutableStatisticEntry(projectId, projectParentKey.split(":"));
           statistic.put(projectId, entry);
         }
         entry.incrWorkItem(end - begin);
       }
     });
     aggregateDown(statistic);
 
     return statistic;
   }
 
   protected void aggregateDown(final Map<String, MutableStatisticEntry> statistic)
   {
     final List<String> projectIds = new ArrayList<String>(statistic.keySet());
 
     final MutableStatisticEntry totals = new MutableStatisticEntry(null, null);
 
     statistic.put(null, totals);
 
     for (final String projectId : projectIds) {
       final MutableStatisticEntry entry = statistic.get(projectId);
       final String[] projectPath = entry.getProjectPath();
 
       for (int i = projectPath.length - 2; i >= 0; i--) {
         MutableStatisticEntry parentEntry = statistic.get(projectPath[i]);
 
         if (parentEntry == null) {
           final String[] parentPath = new String[i];
           System.arraycopy(projectPath, 0, parentPath, 0, i);
           parentEntry = new MutableStatisticEntry(projectPath[i], parentPath);
 
           statistic.put(projectPath[i], parentEntry);
         }
         parentEntry.aggregate(entry);
        totals.aggregate(entry);
       }
     }
   }
 
   protected abstract void iterateWorkItems(Date from, Date until, IStatisticCollector collector)
       throws RepositoryException;
 }
