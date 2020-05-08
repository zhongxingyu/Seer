 package org.gescobar.wayra.service.impl;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.gescobar.wayra.service.StatisticsService;
 import org.gescobar.wayra.service.StatsDTO;
 import org.json.JSONArray;
 import org.json.JSONObject;
import org.springframework.expression.ParseException;
 
 public class GithubStatsService implements StatisticsService {
 
 	@Override
 	public StatsDTO buildStats(String data) {
 		
 		try {
 			
 			List<String> repos = getRepos(data);
 			Collection<Date> commitsDates = getCommitsDates(data, repos);
 			
 			int[] stats = new int[7];
 			Calendar cal = Calendar.getInstance();
 			for (int i=0; i < 7; i++) {
 				
 				int matches = matchesDate(commitsDates, cal);
 				stats[6-i] = matches;
 				
 				cal.add(Calendar.DAY_OF_MONTH, -1);
 			}
 			
 			return new StatsDTO(stats);
 			
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		}
 		
 		return new StatsDTO(0,0,0,0,0,0,0);
 	}
 	
 	private int matchesDate(Collection<Date> updateDates, Calendar cal) {
 		
 		int matches = 0;
 		for (Date updateDate : updateDates) {
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTime(updateDate);
 			
 			boolean sameDay = cal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
             	cal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
 			if (sameDay) {
 				matches++;
 			}
 		}
 		
 		return matches;
 		
 	}
 	
 	private List<String> getRepos(String username) throws Exception {
 		
 		List<String> repos = new ArrayList<String>();
 		
 		String jsonReposString = getHTML("https://api.github.com/users/" + username + "/repos");
 		JSONArray jsonProjects = new JSONArray(jsonReposString);
 		for (int i=0; i < jsonProjects.length(); i++) {
 			JSONObject jsonProject = jsonProjects.getJSONObject(i);
 			repos.add( jsonProject.getString("name") );
 		}
 		
 		return repos;
 		
 	}
 	
 	private Collection<Date> getCommitsDates(String username, Collection<String> repos) throws Exception {
 		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 		
 		Collection<Date> commitsDates = new ArrayList<Date>();
 		
 		for (String repo : repos) {
			
 			String jsonCommitsString = getHTML("https://api.github.com/repos/" + username + "/" + repo + "/commits");
 			
 			JSONArray jsonCommits = new JSONArray(jsonCommitsString);
 			for (int j=0; j < jsonCommits.length(); j++) {
 				JSONObject jsonCommit = jsonCommits.getJSONObject(j);
 				JSONObject jsonCommiter = jsonCommit.getJSONObject("commit").getJSONObject("committer");
 				try {
					commitsDates.add( sdf.parse(jsonCommiter.getString("date")) );
					
 				} catch (ParseException e) {
 					e.printStackTrace(System.err);
 				}
 			}
 		}
 		
 		return commitsDates;
 		
 	}
 	
 	private String getHTML(String urlToRead) throws IOException {
 		
 		HttpClient httpclient = new DefaultHttpClient();
 		
 		try {
 			HttpGet httpget = new HttpGet( urlToRead );
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			
 			return httpclient.execute(httpget, responseHandler);
 			
 		} finally {
 			httpclient.getConnectionManager().shutdown();
 		}
 	}
 
 }
