 package com.freeroom.projectci.beans;
 
 import com.freeroom.di.annotations.Bean;
 import com.freeroom.di.annotations.Inject;
 import com.freeroom.persistence.Athena;
 import com.freeroom.util.Pair;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.util.List;
 
 import static com.freeroom.projectci.beans.ReportType.*;
 import static java.lang.String.format;
 
 @Bean
 public class ReportService {
 
     @Inject
     private Athena athena;
 
     public Collection getCollection(ReportType type)
     {
         return new Collection(type, type.getEstimatedEffort(),
                 calculateUsedEffort(athena.from(TimeReport.class).find(format("type='%s'", type))));
     }
 
     public Pair<Integer, Integer> getTickBar() {
         final DateTime now = new DateTime();
         final DateTime begin = new DateTime(2014, 4, 15, 0, 0, 0);
         final DateTime end = new DateTime(2014, 10, 13, 0, 0, 0);
 
         return Pair.of(Days.daysBetween(begin, end).getDays(), Days.daysBetween(begin, now).getDays());
     }
 
     public void addReport(TimeReport report) {
         athena.persist(report);
     }
 
     private long calculateUsedEffort(List<Object> reports) {
         long usedEffort = 0;
         for (Object report : reports) {
             usedEffort += ((TimeReport) report).getHours();
         }
         return usedEffort;
     }
 
     public String utilityData() {
         final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
         final DateTime yesterday = new DateTime().minusDays(1);
         DateTime date = new DateTime(2014, 4, 15, 0, 0, 0);
 
         System.out.println(">>>>>>>>>>>>>> 1111111111111111");
 
         final StringBuilder sb = new StringBuilder();
         sb.append("date\tMust\tOthers\r\n");
 
         while(date.isBefore(yesterday)) {
             final List<Object> mustReports = athena.from(TimeReport.class).find(
                     format("date='%s' and (type='%s' or type='%s' or type='%s' or type='%s' or type='%s')",
                             date.toDate().getTime(), UserStory, FunctionalTesting, PerformanceTesting, IntegrationTesting, Document));
 
             final List<Object> othersReports = athena.from(TimeReport.class).find(
                     format("date='%s' and (type='%s' or type='%s' or type='%s' or type='%s')",
                             date.toDate().getTime(), OverTime, BugFixing, Leave, Others));
 
             sb.append(format("%s\t%d\t%d\r\n", formatter.print(date), calculateUsedEffort(mustReports), calculateUsedEffort(othersReports)));
             date = date.plusDays(1);
         }
 
        System.out.println(">>>>>>>>>>>>>> 22222222222222222: " + sb.toString());
 
         return sb.toString();
     }
 }
