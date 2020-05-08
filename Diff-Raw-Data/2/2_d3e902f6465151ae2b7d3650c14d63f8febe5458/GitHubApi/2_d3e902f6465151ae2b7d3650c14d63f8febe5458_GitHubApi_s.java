 package com.github.nyao.gwtgithub.client;
 
 import com.github.nyao.gwtgithub.client.api.AUser;
 import com.github.nyao.gwtgithub.client.api.Comments;
 import com.github.nyao.gwtgithub.client.api.Issues;
 import com.github.nyao.gwtgithub.client.api.Repositories;
 import com.github.nyao.gwtgithub.client.api.Users;
 import com.github.nyao.gwtgithub.client.models.Issue;
 import com.github.nyao.gwtgithub.client.models.Repository;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.jsonp.client.JsonpRequestBuilder;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class GitHubApi {
 
 	private String accessToken = null;
 	
 	public void setAuthorization(String accessToken) {
 		this.accessToken = accessToken;
 	}
 	
 	private static final String BASE_URL = "https://api.github.com/";
 
 	public void getUser(AsyncCallback<AUser> callback) {
 		String url = addAutorization(BASE_URL + "user");
 		GWT.log(url);
 		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
 		jsonp.requestObject(url, callback);
 	}
 
 	public void getMyRepository(AsyncCallback<Repositories> callback) {
 		String url = addAutorization(BASE_URL + "user/repos");
 		GWT.log(url);
 		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
 		jsonp.requestObject(url, callback);
 	}
 	
 	public void getRepositories(String user, AsyncCallback<Repositories> callback) {
 		String url = addAutorization(BASE_URL + "users/" + user + "/repos");
         GWT.log(url);
 		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
 		jsonp.requestObject(url, callback);
 	}
 
 	public void getOrganizations(String user, AsyncCallback<Users> callback) {
 		String url = addAutorization(BASE_URL + "users/" + user + "/orgs");
         GWT.log(url);
 		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
 		jsonp.requestObject(url, callback);
 	}
 	
 	public void getOrganizations(AsyncCallback<Users> callback) {
 		String url = addAutorization(BASE_URL + "user/orgs");
         GWT.log(url);
 		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
 		jsonp.requestObject(url, callback);
 	}
     
     public void getIssues(String user, String repository, AsyncCallback<Issues> callback) {
         String url = addAutorization(BASE_URL + "repos/" + user + "/" + repository + "/issues");
         getIssues(url, callback);
     }
     
     public void getIssues(Repository repository, AsyncCallback<Issues> callback) {
         getIssues(addAutorization(repository.getUrl() + "/issues"), callback);
     }
     
     protected void getIssues(String url, AsyncCallback<Issues> callback) {
         GWT.log(url);
         JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
         jsonp.requestObject(url, callback);
     }
     
    public void getComment(Repository r, Issue issue, AsyncCallback<Comments> callback) {
 		String url = addAutorization(r.getUrl() + "/issues/" + issue.getNumber() + "/comments");
         GWT.log(url);
         JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
         jsonp.requestObject(url, callback);
     }
 	
 	private String addAutorization(String url) {
 		String prefix = "?";
 		if(url.contains("?")) {
 			prefix = "&";
 		}
 		
 		if(accessToken != null) {
 			url += prefix + "access_token=" + accessToken;
 		}
 		return url;
 	}
 }
