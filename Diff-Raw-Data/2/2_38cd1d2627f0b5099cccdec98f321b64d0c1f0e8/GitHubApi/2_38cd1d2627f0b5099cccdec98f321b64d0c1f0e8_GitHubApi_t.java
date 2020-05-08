 package com.github.nyao.gwtgithub.client;
 
 import com.github.nyao.gwtgithub.client.models.AJSON;
 import com.github.nyao.gwtgithub.client.models.JSONs;
 import com.github.nyao.gwtgithub.client.models.repos.Content;
 import com.github.nyao.gwtgithub.client.models.repos.Repo;
 import com.github.nyao.gwtgithub.client.models.gitdata.Blob;
 import com.github.nyao.gwtgithub.client.models.gitdata.BlobCreated;
 import com.github.nyao.gwtgithub.client.models.gitdata.Commit;
 import com.github.nyao.gwtgithub.client.models.gitdata.Reference;
 import com.github.nyao.gwtgithub.client.models.gitdata.Tree;
 import com.github.nyao.gwtgithub.client.models.issues.Issue;
 import com.github.nyao.gwtgithub.client.models.issues.IssueComment;
 import com.github.nyao.gwtgithub.client.models.issues.Label;
 import com.github.nyao.gwtgithub.client.models.issues.Milestone;
 import com.github.nyao.gwtgithub.client.models.users.GHUser;
 import com.github.nyao.gwtgithub.client.values.GHValue;
 import com.github.nyao.gwtgithub.client.values.RepoValue;
 import com.github.nyao.gwtgithub.client.values.gitdata.BlobValue;
 import com.github.nyao.gwtgithub.client.values.gitdata.CommitValue;
 import com.github.nyao.gwtgithub.client.values.gitdata.ReferenceCreateValue;
 import com.github.nyao.gwtgithub.client.values.gitdata.ReferenceUpdateValue;
 import com.github.nyao.gwtgithub.client.values.gitdata.TreeValue;
 import com.github.nyao.gwtgithub.client.values.issues.IssueCommentValue;
 import com.github.nyao.gwtgithub.client.values.issues.IssueValue;
 import com.github.nyao.gwtgithub.client.values.issues.LabelValue;
 import com.github.nyao.gwtgithub.client.values.issues.MilestoneValue;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsonUtils;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.jsonp.client.JsonpRequestBuilder;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class GitHubApi {
 
     private String accessToken = null;
     private String baseUrl = "https://api.github.com/";
     private boolean authorized = false;
 
     public void setGitHubURL(String url) {
         this.baseUrl = url;
     }
 
     public void setAccessToken(String accessToken) {
         this.accessToken = accessToken;
     }
     
     public boolean isAuthorized() {
         return this.authorized;
     }
     
     // Users
 
     public void getUser(String login, final AsyncCallback<AJSON<GHUser>> callback) {
         get(baseUrl + "users/" + URL.encode(login), callback);
     }
 
     public void getUser(final AsyncCallback<AJSON<GHUser>> callback) {
         get(baseUrl + "user", callback);
     }
     
     // Repos
 
     public void getRepos(AsyncCallback<JSONs<Repo>> callback) {
         get(baseUrl + "user/repos", callback);
     }
 
     public void getRepos(String user, AsyncCallback<JSONs<Repo>> callback) {
         get(baseUrl + "users/" + URL.encode(user) + "/repos", callback);
     }
 
     public void getRepo(String login, String name, AsyncCallback<AJSON<Repo>> callback) {
         get(baseUrl + "repos/" + URL.encode(login) + "/" + URL.encode(name), callback);
     }
     
     public void saveRepo(Repo r, RepoValue prop, AsyncCallback<Repo> callback) {
         post(r.getUrl(), prop, callback);
     }
     
     // Contents
     
     public void getContent(Repo repo, String path, String ref, AsyncCallback<AJSON<Content>> callback) {
         get(repo.getUrl() + "/contents/" + path + "?ref=" + ref, callback);
     }
     
     // Orgs
 
     public void getOrgs(String user, AsyncCallback<JSONs<GHUser>> callback) {
         get(baseUrl + "users/" + user + "/orgs", callback);
     }
 
     public void getOrgs(AsyncCallback<JSONs<GHUser>> callback) {
         get(baseUrl + "user/orgs", callback);
     }
     
     // Issues
 
     public void getIssues(String user, String r, AsyncCallback<JSONs<Issue>> callback) {
         get(baseUrl + "repos/" + user + "/" + r + "/issues", callback);
     }
 
     public void getIssues(Repo r, AsyncCallback<JSONs<Issue>> callback) {
         get(r.getUrl() + "/issues", callback);
     }
 
     public void createIssue(Repo r, IssueValue prop, final AsyncCallback<Issue> callback) {
         post(r.getUrl() + "/issues", prop, callback);
     }
 
     public void editIssue(Repo r, Issue issue, IssueValue prop,
             final AsyncCallback<Issue> callback) {
         if (issue == null) {
             createIssue(r, prop, callback);
         } else {
             post(r.getUrl() + "/issues/" + issue.getNumber(), prop, callback);
         }
     }
     
     // Comments
 
     public void getIssueComments(Repo r, Issue issue, AsyncCallback<JSONs<IssueComment>> callback) {
         get(r.getUrl() + "/issues/" + issue.getNumber() + "/comments", callback);
     }
 
     public void createIssueComment(Repo r, Issue issue, IssueCommentValue prop,
             final AsyncCallback<IssueComment> callback) {
         post(r.getUrl() + "/issues/" + issue.getNumber() + "/comments", prop, callback);
     }
     
     // Milestones
 
     public void getMilestones(Repo r, AsyncCallback<JSONs<Milestone>> callback) {
         get(r.getUrl() + "/milestones", callback);
     }
 
     public void createMilestone(Repo r, MilestoneValue prop,
             final AsyncCallback<Milestone> callback) {
         post(r.getUrl() + "/milestones", prop, callback);
     }
 
     public void saveMilestone(Repo r, Milestone m, MilestoneValue prop,
             final AsyncCallback<Milestone> callback) {
         if (m ==null) {
             createMilestone(r, prop, callback);
         } else {
             post(r.getUrl() + "/milestones/" + m.getNumber(), prop, callback);
         }
     }
 
     public void saveMilestone(Repo r, String number, MilestoneValue prop,
             final AsyncCallback<Milestone> callback) {
         if (number ==null) {
             createMilestone(r, prop, callback);
         } else {
             post(r.getUrl() + "/milestones/" + URL.encode(number), prop, callback);
         }
     }
     
     // Labels
     
     public void getLabels(Repo r, AsyncCallback<JSONs<Label>> callback) {
         get(r.getUrl() + "/labels", callback);
     }
 
     public void createLabel(Repo r, LabelValue prop,
             final AsyncCallback<Label> callback) {
         post(r.getUrl() + "/labels", prop, callback);
     }
 
     public void saveLabel(Repo r, String name, LabelValue prop,
             final AsyncCallback<Label> callback) {
         if (name == null) {
             createLabel(r, prop, callback);
         } else {
             post(r.getUrl() + "/labels/" + URL.encode(name), prop, callback);
         }
     }
 
     public void saveLabel(Repo r, Label label, LabelValue prop,
             final AsyncCallback<Label> callback) {
         if (label == null) {
             createLabel(r, prop, callback);
         } else {
             post(r.getUrl() + "/labels/" + URL.encode(label.getName()), prop, callback);
         }
     }
     
 
     // Blobs
 
     public void getBlob(Repo r, String sha, AsyncCallback<AJSON<Blob>> callback) {
         get(r.getUrl() + "/git/blobs/" + URL.encode(sha), callback);
     }
     
     public void getBlob(Repo repo, Reference ref, final String filename, final AsyncCallback<AJSON<Blob>> callback) {
         new GitHubSimpleApi(this, repo).getBlob(ref, filename, callback);
     }
 
     public void createBlob(Repo r, BlobValue blob, AsyncCallback<BlobCreated> callback) {
         post(r.getUrl() + "/git/blobs", blob, callback);
     }
 
     // Trees
 
     public void getTree(Repo r, String sha, AsyncCallback<AJSON<Tree>> callback) {
         get(r.getUrl() + "/git/trees/" + URL.encode(sha), callback);
     }
 
     public void createTree(Repo r, TreeValue tree, AsyncCallback<Tree> callback) {
         post(r.getUrl() + "/git/trees", tree, callback);
     }
 
     // Commits
     
     public void getCommit(Repo r, String sha, AsyncCallback<AJSON<Commit>> callback) {
         get(r.getUrl() + "/commits/" + URL.encode(sha), callback);
     }
 
     public void getCommit(Reference ref, AsyncCallback<AJSON<Commit>> callback) {
         get(ref.getObject().getUrl(), callback);
     }
 
     public void createCommit(Repo r, CommitValue commit, AsyncCallback<Commit> callback) {
         post(r.getUrl() + "/git/commits", commit, callback);
     }
 
     public void createSimpleCommitAndPush(Repo r, Reference ref, String refName, String filename, String content, String message, 
                                    AsyncCallback<Reference> callback) {
         new GitHubSimpleApi(this, r).createSimpleCommitAndPush(ref, refName, filename, content, message, callback);
     }
     
     public void createSimpleCommit(Repo r, String ref, String filename, String content, String message, 
                                    AsyncCallback<Commit> callback) {
         new GitHubSimpleApi(this, r).createSimpleCommit(null, filename, content, message, callback);
     }
 
     // References
     
     public void getReference(Repo r, String ref, AsyncCallback<JSONs<Reference>> callback) {
         get(r.getUrl() + "/git/" + URL.encode(ref), callback);
     }
 
     public void getReferenceHead(Repo r, String ref, AsyncCallback<AJSON<Reference>> callback) {
         get(r.getUrl() + "/git/refs/heads/" + URL.encode(ref), callback);
     }
 
     public void createReference(Repo r, ReferenceCreateValue ref, AsyncCallback<Reference> callback) {
         post(r.getUrl() + "/git/refs", ref, callback);
     }
     
     public void updateReference(Repo r, Reference ref, ReferenceUpdateValue refValue, AsyncCallback<Reference> callback) {
         post(r.getUrl() + "/git/" + ref.getRef(), refValue, callback);
     }
     
     // private methods
     
     private <T extends JavaScriptObject> AsyncCallback<T> hookCallback(final AsyncCallback<T> callback) {
         return new AsyncCallback<T>() {
             @Override
             public void onSuccess(T result) {
                 if (accessToken != null) authorized = true;
                 callback.onSuccess(result);
             }
             
             @Override
             public void onFailure(Throwable caught) {
                 callback.onFailure(caught);
             }
         };
     }
 
     private <T extends JavaScriptObject> void get(String url, final AsyncCallback<T> callback) {
         String requestUrl = makeRequestUrl(url);
         GWT.log("[GET]" + requestUrl);
         JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
         jsonp.requestObject(requestUrl, hookCallback(callback));
     }
 
     private <T extends JavaScriptObject> void post(String url, GHValue<?> request,
             AsyncCallback<T> callback) {
         String requestUrl = makeRequestUrl(url);
         RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, requestUrl);
         String requestJson = request.toJson();
         final AsyncCallback<T> hookedCallback = hookCallback(callback);
         final StringBuilder log = new StringBuilder();
         log.append("[POST]" + requestUrl + "\n" + requestJson);
         try {
             builder.sendRequest(requestJson, new RequestCallback() {
                 @Override
                 public void onResponseReceived(Request request, Response response) {
                    T result = JsonUtils.<T>safeEval(response.getText());
                     log.append("\n\n--" + response.getStatusText() + ":" + response.getStatusCode() + "\n" + response.getText());
                     hookedCallback.onSuccess(result);
                     GWT.log(log.toString());
                 }
 
                 @Override
                 public void onError(Request request, Throwable e) {
                     log.append("\n\n--" + e.getStackTrace());
                     hookedCallback.onFailure(e);
                     GWT.log(log.toString());
                 }
             });
         } catch (RequestException e) {
             log.append("\n\n--" + e.getStackTrace());
             hookedCallback.onFailure(e);
             GWT.log(log.toString());
         }
     }
 
     private String makeRequestUrl(String url) {
         String prefix = "?";
         if (url.contains("?")) {
             prefix = "&";
         }
 
         if (accessToken != null) {
             url += prefix + "access_token=" + accessToken;
         }
         return url;
     }
 }
