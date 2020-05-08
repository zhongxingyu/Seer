 package com.nyver.jira.jiradailyreports;
 
 import com.atlassian.jira.rest.client.JiraRestClient;
 import com.atlassian.jira.rest.client.NullProgressMonitor;
 import com.atlassian.jira.rest.client.RestClientException;
 import com.atlassian.jira.rest.client.domain.BasicIssue;
 import com.atlassian.jira.rest.client.domain.Issue;
 import com.atlassian.jira.rest.client.domain.SearchResult;
 import com.atlassian.jira.rest.client.domain.Worklog;
 import com.nyver.jira.jiradailyreports.output.Output;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Builder for Jira's daily report
  *
  * @author Yuri Novitsky
  */
 public class JiraDailyReportBuilder
 {
     private JiraRestClient client;
     private Output output;
     private String user;
 
     public JiraDailyReportBuilder(JiraRestClient client, Output output)
     {
         this.client = client;
         this.output = output;
     }
 
     /**
      * Set user
      * @param user
      */
     public void setUser(String user)
     {
         this.user = user;
     }
 
     /**
      * Get yesterday date
      * @return yesterday date
      */
     protected Date getYesterdayDate()
     {
         Calendar cal = Calendar.getInstance();
 
         cal.set(Calendar.HOUR, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
 
         if (Calendar.MONDAY == cal.get(Calendar.DAY_OF_WEEK)) {
             cal.add(Calendar.DATE, -3);
         } else {
             cal.add(Calendar.DATE, -1);
         }
         return cal.getTime();
     }
 
     /**
      * Get today date
      * @return
      */
     protected Date getTodayDate()
     {
         Calendar cal = Calendar.getInstance();
 
         cal.set(Calendar.HOUR, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
 
         return cal.getTime();
     }
 
 
     /**
      * Build report
      */
     public void build() throws RestClientException
     {
         final NullProgressMonitor pm = new NullProgressMonitor();
 
         output.writeString(String.format("Jira daily report for %s (%s)", user, new Date()));
         output.writeString("");
 
         // What was done
         output.writeHeader("What was done:");
 
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date yesterdayDate = getYesterdayDate();
        Date todayDate = getTodayDate();

         SearchResult result = client.getSearchClient().searchJql(
             String.format(
                    "updatedDate > \"%s\" AND updatedDate < \"%s\" AND (assignee WAS \"%s\" ON \"%s\" OR assignee = \"%s\") ORDER BY priority DESC",
                     dateFormat.format(yesterdayDate),
                     dateFormat.format(todayDate),
                     user,
                     dateFormat.format(getYesterdayDate()),
                     user
             ),
             pm
         );
 
         if (result.getTotal() > 0) {
             for(BasicIssue basicIssue: result.getIssues()) {
                 Issue issue = client.getIssueClient().getIssue(basicIssue.getKey(), pm);
                 for(Worklog worklog: issue.getWorklogs()) {
                    if (worklog.getCreationDate().isAfter(yesterdayDate.getTime())
                            && worklog.getCreationDate().isBefore(todayDate.getTime())
                             && worklog.getAuthor().getName().equals(user)
                             ) {
                         output.writeIssue(issue);
                         break;
                     }
                 }
             }
         }
 
         // What's next
         output.writeHeader("What's next:");
 
         executeJql(
                 String.format("assignee in (\"%s\") AND status in (Open, \"In Progress\", Reopened) ORDER BY priority DESC", user),
                 pm
         );
 
     }
 
     /**
      * Execute JQL and output results
      * @param jql
      * @param pm
      */
     protected void executeJql(String jql, NullProgressMonitor pm)
     {
         SearchResult result = client.getSearchClient().searchJql(
                 jql,
                 pm
         );
 
         if (result.getTotal() > 0) {
             for(BasicIssue basicIssue: result.getIssues()) {
                 Issue issue = client.getIssueClient().getIssue(basicIssue.getKey(), pm);
                 output.writeIssue(issue);
             }
         }
     }
 
 }
