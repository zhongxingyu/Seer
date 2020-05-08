 /*
  *  Copyright 2010 Heroic Efforts, LLC
  *  
  *  This file is part of Viable.
  *
  *  Viable is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Viable is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Viable.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.heroicefforts.viable.android.rep.it.gdata;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 import net.heroicefforts.viable.android.Config;
 import net.heroicefforts.viable.android.dao.Comment;
 import net.heroicefforts.viable.android.dao.Issue;
 import net.heroicefforts.viable.android.rep.ServiceException;
 import net.heroicefforts.viable.android.rep.it.auth.Authenticate;
 import net.heroicefforts.viable.android.rep.it.auth.AuthenticationException;
 import net.heroicefforts.viable.android.rep.it.auth.GCLAccountAuthenticator;
 import net.heroicefforts.viable.android.rep.it.auth.NetworkException;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 /**
  * The service class for interacting with the Gdata Issues protocol.
  * 
  * @author jevans
  *
  */
 public class ProjectHostingService
 {
 	private static final String TAG = "ProjectHostingService";
 
 	private static final String ENTRY = "entry";
 	private static final String PUBLISHED_DATE = "published";
 
 	private static final int CONN_TIMEOUT = 5000;
 	
 	private static final String EOL = System.getProperty("line.separator");
 	private static final String SALT_STACKTRACE = "<p>Stacktrace:  ";
 
 	private String authToken;
 
 	private DefaultHttpClient httpclient;
 	private Pattern nfp = Pattern.compile("issue [0-9]+ .* not found");
 	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 
 
 	public ProjectHostingService()
 	{
 		httpclient = new DefaultHttpClient();
 		HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), CONN_TIMEOUT);		
 	}
 
 	/**
 	 * Sets the authentication token to be used for actions that require authentication.
 	 * 
 	 * @param token a Google Client Login "code" token.
 	 */
 	public void setAuthSubToken(String token)
 	{
 		authToken = token;
 	}
 	
 	public void setUserCredentials(String username, String password)
 		throws AuthenticationException, NetworkException
 	{
 		authToken = Authenticate.authenticate(username, password, GCLAccountAuthenticator.TOKEN_TYPE_ISSUE_TRACKER);
 	}
 
 	/**
 	 * Wraps the request in authentication and gzip headers before executing it.
 	 * 
 	 * @param request the request to be executed.
 	 * @return the client response
 	 * @throws IOException if an error occurs
 	 */
 	private HttpResponse execute(HttpUriRequest request)
 		throws IOException
 	{
 		if(Config.LOGV)
 			Log.v(TAG, "Requesting:  " + request.getURI().toASCIIString());
 		request.addHeader("Accept-Encoding", "gzip");
 		if(authToken != null)
 		{
 			request.addHeader("Authorization", "GoogleLogin auth=" + authToken);
 //			request.addHeader("Authorization", "AuthSub token=" + authToken);
 			if(Config.LOGV)
 				Log.v(TAG, "Added auth token '" + authToken + "' to request.");
 		}
 		return httpclient.execute(request);
 	}
 	
 	/**
 	 * Reads the supplied response body and converts it to a JSON object.
 	 * @param response the client response.
 	 * @return the JSON representation of the response
 	 * @throws IOException if an service error occurs
 	 * @throws JSONException if a parsing error occurs.
 	 */
 	private JSONObject readJSON(HttpResponse response) throws IOException, JSONException
 	{
 		String body = readResponse(response);
 		JSONObject obj = new JSONObject(body);
 		return obj;
 	}
 
 	/**
 	 * Reads the response body and returns a string.  This also handles gzip decompression, if necessary.
 	 * @param response the client response
 	 * @return a string of the response body.
 	 * @throws IOException if a service error occurs.
 	 */
 	private String readResponse(HttpResponse response) throws IOException
 	{
 		InputStream instream = response.getEntity().getContent();
 		Header contentEncoding = response.getFirstHeader("Content-Encoding");
 		if(Config.LOGV) //NOPMD
 			if(contentEncoding != null) //NOPMD
 				Log.v(TAG, "Response content encoding was '" + contentEncoding.getValue() + "'");
 		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
 			if(Config.LOGD)
 				Log.d(TAG, "Handling GZIP response.");
 		    instream = new GZIPInputStream(instream);
 		}		
 
 		BufferedInputStream bis = new BufferedInputStream(instream);
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		byte[] buf = new byte[1024];
 		int read = 0;
 		while((read = bis.read(buf)) > 0)
 			baos.write(buf, 0, read);
 		String body = baos.toString();
 		if(Config.LOGV)
 			Log.v(TAG, "Response:  " + body);
 		return body;
 	}
 	
 	/**
 	 * Execute the specified query.
 	 * @param myQuery a legitimate Gdata Issue query.
 	 * @return a feed of corresponding found issues.
 	 * @throws ServiceException if a service error occurs.
 	 */
 	public IssuesFeed query(IssuesQuery myQuery)
 		throws ServiceException
 	{
 		try
 		{
 			String url = myQuery.toUrl();
 			if(Config.LOGV)
 				Log.v(TAG, "Query url:  " + url);
 			HttpGet get = new HttpGet(new URI(url));
 			HttpResponse response = execute(get);
 			JSONObject obj = readJSON(response);
 			List<Issue> issues = loadIssueFeed(obj);
 			IssuesFeed feed = new IssuesFeed(issues);
 			return feed;
 		}
 		catch (Exception e)
 		{
 			throw new ServiceException("Error executing query.", e);
 		}
 	}
 
 	/**
 	 * Retrieves a single issue based upon an issue entity url.
 	 * @param feedUrl an issue entity url.
 	 * @return the issue entity or null if the entity was not found.
 	 * @throws ServiceException if a service error occurs.
 	 */
 	public Issue getEntry(URL feedUrl)
 		throws ServiceException
 	{
 		try
 		{
 			String url = feedUrl.toExternalForm();
 			if(url.indexOf('?') != -1)
 				url += "&";
 			else
 				url += "?";
 			url += "alt=json";
 			HttpGet get = new HttpGet(new URI(url));
 			HttpResponse response = execute(get);
 			String body = readResponse(response);
 			if(!notFound(body))
 			{
 				JSONObject obj = new JSONObject(body);
 				if(Config.LOGV)
 					Log.v(TAG, "Entry response:  " + obj.toString(4));
 				if(obj.has(ENTRY))
 					return loadIssue(obj.getJSONObject(ENTRY));
 			}
 
 			return null;
 		}
 		catch (Exception e)
 		{
 			throw new ServiceException("Error executing query.", e);
 		}
 	}
 
 	/**
 	 * Fetches a feed of issue comments.
 	 * @param feedUrl a valid Gdata issue URL to issue comments
 	 * @return a feed of issue comments
 	 * @throws ServiceException if a service error occurs.
 	 */
 	public IssueCommentsFeed getFeed(URL feedUrl)
 		throws ServiceException
 	{
 		try
 		{
 			String url = feedUrl.toExternalForm();
 			if(url.indexOf('?') != -1)
 				url += "&";
 			else
 				url += "?";
 			url += "alt=json";
 			HttpGet get = new HttpGet(new URI(url));
 			HttpResponse response = execute(get);
 			JSONObject obj = readJSON(response);
 			return loadCommentFeed(obj);
 		}
 		catch (Exception e)
 		{
 			throw new ServiceException("Error executing query.", e);
 		}
 	}
 
 	private IssueCommentsFeed loadCommentFeed(JSONObject obj)
 		throws JSONException
 	{
 		if(Config.LOGV)
 			Log.v(TAG, "CommentFeed query response:  " + obj.toString(4));
 		List<Comment> comments = new ArrayList<Comment>();
 		JSONObject feed = obj.getJSONObject("feed");
 		if(feed.has(ENTRY))
 		{
 			JSONArray entries = feed.getJSONArray(ENTRY);
 			for(int i = 0; i < entries.length(); i++)
 				comments.add(loadComment(entries.getJSONObject(i)));
 		}			
 		
 		int total = Integer.parseInt(feed.getJSONObject("openSearch$totalResults").getString("$t"));
 		
 		return new IssueCommentsFeed(comments, total);
 	}
 
 	private Comment loadComment(JSONObject obj)
 		throws JSONException
 	{
 		String idStr = obj.getJSONObject("id").getString("$t");
 		long id = Long.parseLong(idStr.substring(idStr.lastIndexOf('/') + 1));
 		StringBuilder body = new StringBuilder();
 		body.append(obj.getJSONObject("title").getString("$t")).append(EOL);
 		body.append(EOL);
 		body.append(obj.getJSONObject("content").getString("$t"));
 		JSONArray authors = obj.getJSONArray("author");
 		StringBuilder author = new StringBuilder();
 		for(int i = 0; i < authors.length(); i++)
 			author.append(authors.getJSONObject(i).getJSONObject("name").getString("$t")).append(", ");
 		author.setLength(author.length() - 2);
 
 		Date created = null;
 		try
 		{
 			created = sdf.parse(obj.getJSONObject(PUBLISHED_DATE).getString("$t"));
 		}
 		catch (ParseException e)
 		{
 			throw new JSONException("Error parsing JSON date:  " + obj.getJSONObject(PUBLISHED_DATE).getString("$t"));
 		}
 		
 		return new Comment(id, body.toString(), author.toString(), created);
 	}
 
 	/**
 	 * Inserts the supplied issue using the GData Issue protocol.
 	 * @param postUrl the valid issue post URL.
 	 * @param issue the issue to insert.
 	 * @return the issue populated with the returned id and values.
 	 * @throws AuthenticationException if authentication to the service fails
 	 * @throws ServiceException if a service error occurs
 	 */
 	public Issue insert(URL postUrl, Issue issue)
 		throws AuthenticationException, ServiceException
 	{
 		try
 		{
 			String appName = issue.getAppName();
 			HttpPost post = new HttpPost(postUrl.toString());
 			post.addHeader("Content-Type", "application/atom+xml");
			String entity = new AtomIssue(issue).toString();
			if(Config.LOGV)
				Log.v(TAG, "Posting issue:  '" + entity + "'");
			post.setEntity(new StringEntity(entity));
 			HttpResponse response = execute(post);
 			int responseCode = response.getStatusLine().getStatusCode();
 			String body = readResponse(response);
 			if(HttpStatus.SC_CREATED == responseCode)
 			{
 				Issue wireIssue = new AtomIssue(body);
 				issue.copy(wireIssue);
 			}
 			else if(HttpStatus.SC_FORBIDDEN == responseCode || HttpStatus.SC_UNAUTHORIZED == responseCode)
 				throw new AuthenticationException(responseCode, body);
 			else
 				throw new ServiceException("Exception creating issue:  " + body);
 			
 			issue.setAppName(appName);
 			
 			return issue;
 		}
 		catch (Exception e)
 		{
 			throw new ServiceException("Error creating issue.", e);
 		}
 	}
 
 	public Issue update(URL postUrl, Issue issue)
 	throws AuthenticationException, ServiceException
 	{
 		try
 		{
 			String appName = issue.getAppName();
 			HttpPut post = new HttpPut(postUrl.toString());
 			post.addHeader("Content-Type", "application/atom+xml");
 			post.setEntity(new StringEntity(new AtomIssue(issue).toString()));
 			HttpResponse response = execute(post);
 			int responseCode = response.getStatusLine().getStatusCode();
 			if(Config.LOGD)
 				Log.d(TAG, "Update response code:  " + responseCode);
 			String body = readResponse(response);
 			if(HttpStatus.SC_CREATED == responseCode)
 			{
 				Issue wireIssue = new AtomIssue(body);
 				issue.copy(wireIssue);
 			}
 			else if(HttpStatus.SC_FORBIDDEN == responseCode || HttpStatus.SC_UNAUTHORIZED == responseCode)
 				throw new AuthenticationException(responseCode, body);
 			else
 				throw new ServiceException("Exception creating issue:  " + body);
 			
 			issue.setAppName(appName);
 			
 			return issue;
 		}
 		catch (Exception e)
 		{
 			throw new ServiceException("Error creating issue.", e);
 		}
 	}
 	
 	/**
 	 * Inserts the supplied issue comment using the GData Issue protocol.
 	 * @param postUrl the valid issue comment post URL.
 	 * @param issue the issue comment to insert.
 	 * @return the issue comment populated with the returned id and values.
 	 * @throws AuthenticationException if authentication to the service fails
 	 * @throws ServiceException if a service error occurs
 	 */
 	public Comment insert(URL postUrl, Comment comment)
 		throws AuthenticationException, ServiceException
 	{
 		try
 		{
 			HttpPost post = new HttpPost(postUrl.toString());
 			post.addHeader("Content-Type", "application/atom+xml");
 			post.setEntity(new StringEntity(new AtomComment(comment).toString()));
 			HttpResponse response = execute(post);
 			int responseCode = response.getStatusLine().getStatusCode();
 			String body = readResponse(response);
 			if(HttpStatus.SC_CREATED == responseCode)
 			{
 				Comment wireIssue = new AtomComment(body);
 				comment.copy(wireIssue);
 			}
 			else if(HttpStatus.SC_FORBIDDEN == responseCode || HttpStatus.SC_UNAUTHORIZED == responseCode)
 				throw new AuthenticationException(responseCode, body);
 			else
 				throw new ServiceException("Exception creating issue:  " + body);
 						
 			return comment;
 		}
 		catch (Exception e)
 		{
 			throw new ServiceException("Error creating issue.", e);
 		}
 	}
 	
 	private boolean notFound(String body)
 	{
 		return nfp.matcher(body).matches();
 	}
 	
 	private List<Issue> loadIssueFeed(JSONObject obj)
 		throws JSONException
 	{
 		if(Config.LOGV)
 			Log.v(TAG, "IssueFeed query response:  " + obj.toString(4));
 		List<Issue> issues = new ArrayList<Issue>();
 		JSONObject feed = obj.getJSONObject("feed");
 		if(feed.has(ENTRY))
 		{
 			JSONArray entries = feed.getJSONArray(ENTRY);
 			for(int i = 0; i < entries.length(); i++)
 			{
 				issues.add(loadIssue(entries.getJSONObject(i)));
 			}
 		}
 		
 		return issues;
 	}
 
 	private Issue loadIssue(JSONObject obj)
 		throws JSONException
 	{
 		Issue issue = new Issue();
 		issue.setSummary(obj.getJSONObject("title").getString("$t"));
 		String bodyStr = obj.getJSONObject("content").getString("$t");
 		int idxStart = bodyStr.indexOf(SALT_STACKTRACE);
 		if (idxStart != -1)
 		{
 			int idxEnd = bodyStr.indexOf("</p>", idxStart);
 			issue.setStacktrace(bodyStr.substring(idxStart + SALT_STACKTRACE.length(), idxEnd));
 			issue.setDescription(bodyStr.substring(0, idxStart).replaceAll("<p>", "").replaceAll("</p>", EOL));
 		}
 		else
 			issue.setDescription(bodyStr.replaceAll("<p>", "").replaceAll("</p>", EOL));
 		
 		issue.setIssueId(obj.getJSONObject("issues$id").getString("$t"));
 		issue.setVotes(obj.getJSONObject("issues$stars").getLong("$t"));
 		JSONArray labels = obj.getJSONArray("issues$label");
 		ArrayList<String> affectedVersions = new ArrayList<String>();
 		for(int i = 0; i < labels.length(); i++)
 		{
 			String label = labels.getJSONObject(i).getString("$t");
 			if(Config.LOGV)
 				Log.v(TAG, "Label:  " + label);
 			if(label.startsWith("Type-"))
 				issue.setType(label.substring("Type-".length()));
 			else if(label.startsWith("Priority-"))
 				issue.setPriority(label.substring("Priority-".length()));
 			else if(label.startsWith("Hash-"))
 				issue.setHash(label.substring("Hash-".length()));
 			else if(label.startsWith("AffectedVersion-"))
 				affectedVersions.add(label.substring("AffectedVersion-".length()));
 		}
 		issue.setAffectedVersions(affectedVersions.toArray(new String[affectedVersions.size()]));
 		
 		issue.setState(obj.getJSONObject("issues$state").getString("$t"));
 		try
 		{
 			issue.setCreateDate(sdf.parse(obj.getJSONObject(PUBLISHED_DATE).getString("$t")));
 		}
 		catch (ParseException e)
 		{
 			throw new JSONException("Error parsing JSON date:  " + obj.getJSONObject(PUBLISHED_DATE).getString("$t"));
 		}
 
 		try
 		{
 			issue.setModifiedDate(sdf.parse(obj.getJSONObject("updated").getString("$t")));
 		}
 		catch (ParseException e)
 		{
 			throw new JSONException("Error parsing JSON date:  " + obj.getJSONObject("updated").getString("$t"));
 		}
 		
 		return issue;
 	}
 
 }
