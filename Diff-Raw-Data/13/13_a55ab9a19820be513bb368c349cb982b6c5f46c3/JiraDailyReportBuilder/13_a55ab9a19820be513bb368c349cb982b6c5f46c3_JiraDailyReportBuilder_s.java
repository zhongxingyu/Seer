 package com.nyver.jira.jiradailyreports;
 
 import com.atlassian.jira.rest.client.JiraRestClient;
 import com.atlassian.jira.rest.client.NullProgressMonitor;
 import com.atlassian.jira.rest.client.RestClientException;
 import com.atlassian.jira.rest.client.domain.BasicIssue;
 import com.atlassian.jira.rest.client.domain.Issue;
 import com.atlassian.jira.rest.client.domain.SearchResult;
 import com.nyver.jira.jiradailyreports.output.OutputInterface;
 
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
     private OutputInterface output;
     private String user;
 
     public JiraDailyReportBuilder(JiraRestClient client, OutputInterface output)
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
        cal.add(Calendar.DATE, -1);
         return cal.getTime();
     }
 
     /**
      * Build report
      */
     public void build() throws RestClientException
     {
         final NullProgressMonitor pm = new NullProgressMonitor();
 
         // What was done
         output.writeLn("What was done:");
 
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
         executeJql(
                 String.format(
                        "updatedDate < \"%s\" AND updatedDate > \"%s\" AND assignee CHANGED FROM \"%s\" AND assignee != \"%s\" ORDER BY priority DESC",
                         dateFormat.format(new Date()),
                         dateFormat.format(getYesterdayDate()),
                         user,
                         user
                 ),
                 pm
         );
 
         // What's next
         output.writeLn("What's next:");
 
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
                 output.writeLn(
                         String.format("[%s] %s (%s)", issue.getKey(), issue.getSummary(), issue.getStatus().getName())
                 );
             }
         }
     }
 
 }
