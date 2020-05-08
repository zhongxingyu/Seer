 package jira;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Iterator;
 
 import com.atlassian.jira.rest.client.JiraRestClient;
 import com.atlassian.jira.rest.client.NullProgressMonitor;
 import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
 import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.Issue;
 import com.atlassian.jira.rest.client.domain.SearchResult;
 import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
 
 public class Jira
 {
 	JiraRestClient restClient;
 	public void initJira(String location) throws URISyntaxException
 	{
 		JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
		URI jiraServerUri = new URI("https://" + location);
 		restClient = factory.create(jiraServerUri, new AnonymousAuthenticationHandler());
 	}
 	
 	public void parseJira(String location) throws URISyntaxException
 	{
 		// initialize the Jira Client
 		initJira(location);
 		final NullProgressMonitor pm = new NullProgressMonitor();
 		
		//https://hibernate.onjira.com/rest/api/latest/search?jql=null&startAt=0&fields=*all
		
		SearchResult sr = restClient.getSearchClient().searchJql(null, pm);
 		Iterator<BasicIssue> iter = sr.getIssues().iterator();
 		while(iter.hasNext())
 		{
 			System.out.println(iter.next().getKey());
 		}
 	}
 }
