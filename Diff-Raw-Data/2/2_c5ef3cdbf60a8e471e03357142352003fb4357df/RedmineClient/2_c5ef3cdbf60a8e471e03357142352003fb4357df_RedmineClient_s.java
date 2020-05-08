 /*
  * Copyright 2011 Zouhin Ro
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package magicware.scm.redmine.tools;
 
 import java.io.IOException;
 import java.util.UUID;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.AuthCache;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.BasicAuthCache;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RedmineClient {
 
     protected static Logger log = LoggerFactory.getLogger(RedmineClient.class);
 
     private HttpHost targetHost;
     private DefaultHttpClient httpclient;
     private String context = null;
 
     public RedmineClient(String host, int port, String context) {
         super();
         this.httpclient = new DefaultHttpClient();
         this.targetHost = new HttpHost(host, port, Constants.NORMAL_PROTOCOL);
         this.context = context;
     }
 
     public void fillBasicAuth(String userName, String base64Pwd) {
 
         // Basic認証
         httpclient.getCredentialsProvider().setCredentials(
                 new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                 new UsernamePasswordCredentials(userName, StringUtils
                         .isEmpty(base64Pwd) ? UUID.randomUUID().toString()
                         : new String(Base64.decodeBase64(base64Pwd))));
         AuthCache authCache = new BasicAuthCache();
         BasicScheme basicAuth = new BasicScheme();
         authCache.put(targetHost, basicAuth);
         BasicHttpContext localcontext = new BasicHttpContext();
         localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
     }
 
     public int queryIssue(String projectId, String fieldId, String keyNo)
             throws ClientProtocolException, IOException {
 
         HttpGet httpGet = null;
         try {
 
             int count = 0;
             StringBuilder uri = new StringBuilder();
 
             uri.append(this.context).append("/issues.json?")
                     .append("project_id=").append(projectId).append("&status_id=*&")
                     .append(fieldId).append("=").append(keyNo);
 
             httpGet = new HttpGet(uri.toString());
             log.debug("executing get request " + httpGet.getURI());
 
             // レスポンスの取得
             RdeminResponse rdeminResponse = getRdeminResponse(httpGet);
 
             if (rdeminResponse.isResponseOK()) {
                 Matcher m = Pattern.compile(Constants.ISSUE_COUNT_VALUE_EXP)
                         .matcher(rdeminResponse.getContent());
                 while (m.find()) {
                     MatchResult mr = m.toMatchResult();
                     count = Integer.valueOf(mr.group(1).trim());
                 }
                 log.debug("count of issue[" + keyNo + "] -> " + count);
             }
 
             return count;
 
         } finally {
             if (httpGet != null)
                 httpGet.abort();
         }
     }
 
     public String createNewIssue(String newIssue)
             throws ClientProtocolException, IOException {
         HttpPost httpPost = null;
         String newIssueId = null;
         try {
             log.trace(newIssue);
             httpPost = new HttpPost(this.context + "/issues.json");
             httpPost.setEntity(new StringEntity(newIssue, "application/json",
                     HTTP.UTF_8));
             log.debug("executing post request " + httpPost.getURI());
 
             // レスポンスの取得
             RdeminResponse rdeminResponse = getRdeminResponse(httpPost);
 
             if (rdeminResponse.isResponseOK()) {
                 Matcher m = Pattern.compile(Constants.ISSUE_ID_VALUE_EXP)
                         .matcher(rdeminResponse.getContent());
                 while (m.find()) {
                     MatchResult mr = m.toMatchResult();
                     newIssueId = mr.group(1).trim();
                 }
             }
             return newIssueId;
 
         } finally {
             if (httpPost != null)
                 httpPost.abort();
         }
     }
 
     public boolean deleteIssue(String issueId) throws ClientProtocolException,
             IOException {
         HttpDelete httpDelete = null;
         try {
             httpDelete = new HttpDelete(this.context + "/issues/" + issueId
                     + ".json");
             log.debug("executing request delete " + httpDelete.getURI());
             RdeminResponse rdeminResponse = getRdeminResponse(httpDelete);
             return rdeminResponse.isResponseOK();
         } finally {
             if (httpDelete != null)
                 httpDelete.abort();
         }
     }
 
     public int request(String uri) throws ClientProtocolException, IOException {
         HttpGet httpGet = null;
         try {
             httpGet = new HttpGet(this.context + uri);
             log.debug("executing request " + httpGet.getURI());
             return getRdeminResponse(httpGet).getStatus();
         } finally {
             if (httpGet != null)
                 httpGet.abort();
         }
     }
 
     private RdeminResponse getRdeminResponse(HttpRequest request)
             throws IOException, ClientProtocolException {
 
         RdeminResponse rdeminResponse = new RdeminResponse();
 
         log.debug("-------------------------------------");
 
         HttpResponse response = httpclient.execute(targetHost, request);
 
         int statusCode = response.getStatusLine().getStatusCode();
         log.debug("responseStatus:" + statusCode);
         log.trace("Response Header >>>");
         Header[] headers = response.getAllHeaders();
         for (Header header : headers) {
             log.trace(header.getName() + ": " + header.getValue());
         }
 
         log.trace("Response Body >>>");
 
         String responseString = EntityUtils.toString(response.getEntity());
         log.trace(responseString);
 
         rdeminResponse.setStatus(statusCode);
         rdeminResponse.setContent(responseString);
         if (!rdeminResponse.isResponseOK()) {
             log.error(responseString);
         }
 
         return rdeminResponse;
     }
 
     public void shutdown() {
         if (httpclient != null)
             httpclient.getConnectionManager().shutdown();
     }
 
     class RdeminResponse {
 
         private int status;
 
         private String content;
 
         public int getStatus() {
             return status;
         }
 
         public void setStatus(int status) {
             this.status = status;
         }
 
         public String getContent() {
             return content;
         }
 
         public void setContent(String content) {
             this.content = content;
         }
 
         public boolean isResponseOK() {
             return (status >= 200 && status < 300);
         }
     }
 
 }
