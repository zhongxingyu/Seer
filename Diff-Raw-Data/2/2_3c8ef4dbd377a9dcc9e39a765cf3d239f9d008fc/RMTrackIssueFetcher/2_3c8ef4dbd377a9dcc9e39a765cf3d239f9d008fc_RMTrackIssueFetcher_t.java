 package com.paulsh.rmtrack;
 
 import com.intellij.openapi.util.io.StreamUtil;
 import jetbrains.buildServer.http.HttpUtil;
 import jetbrains.buildServer.util.FileUtil;
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import jetbrains.buildServer.issueTracker.AbstractIssueFetcher;
 import jetbrains.buildServer.issueTracker.IssueData;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.util.DateUtil;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import sun.misc.BASE64Encoder;
 
 import javax.crypto.Mac;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * RMTrack support
  * User: paul-sh
  * Date: 22.10.11
  */
 public class RMTrackIssueFetcher extends AbstractIssueFetcher {
     private final static Log LOG = LogFactory.getLog(RMTrackIssueFetcher.class);
 
     private Pattern myPattern;
     private String myAccessKey;
     private String mySecretKey;
 
     public RMTrackIssueFetcher(@NotNull jetbrains.buildServer.util.cache.EhCacheUtil cacheUtil) {
         super(cacheUtil);
     }
 
 
     public void setPattern(final Pattern _myPattern) {
         myPattern = _myPattern;
     }
 
     public void setKeys(String accessKey, String secretKey) {
         myAccessKey = accessKey;
         mySecretKey = secretKey;
     }
 
     public class RMTIssueFetchFunction implements AbstractIssueFetcher.FetchFunction {
 
         private String myRepository;
         private String myId;
         private Credentials myCredentials;
 
         public RMTIssueFetchFunction(final String repository, final String id, final Credentials credentials) {
             //this.myRepository = removeTrailingSlash(repository);
             if (repository.length() == 0) {
                 throw new IllegalArgumentException(String.format("Repository cannot be empty"));
             }
             myRepository = repository;
             this.myId = id;
             this.myCredentials = credentials;
         }
 
         @NotNull
         public IssueData fetch() throws Exception {
             String url = getApiUrl(myRepository, myId);
             LOG.debug(String.format("Fetching issue data from %s", url));
             try {
                 InputStream xml = fetchIssueDetailsXml(url, myCredentials);
                 IssueData result = parseIssue(xml);
                 LOG.debug("IssueData: " + result.toString());
                 return result;
             }   catch (Exception e) {
                 LOG.error(e);
                 throw new RuntimeException("Error fetching issue data", e);
             }
         }
 
         private IssueData parseIssue(InputStream xml) {
             try {
                 Element issue = FileUtil.parseDocument(xml, false);
                 Element fields = issue.getChild("Fields");
                 if (fields == null) {
                   throw new RuntimeException(String.format("Invalid XML for issue '%s' on '%s'.", myId, myRepository));
                 }
                 String summary = getChildContent(fields, "Summary");
                 String state = getChildContent(fields, "StatusCode");
                 String url = getUrl(myRepository, myId);
                 boolean resolved = state.equalsIgnoreCase("Closed");
                 IssueData result = new IssueData(myId, summary, state, url, resolved);
                 return result;
             } catch (JDOMException e) {
                 //LOGGER.error(e);
                 throw new RuntimeException(String.format("Error parsing XML for issue '%s' on '%s'.", myId, myRepository));
             } catch (IOException e) {
                 //LOGGER.error(e);
                 throw new RuntimeException(String.format("Error reading XML for issue '%s' on '%s'.", myId, myRepository));
             }
         }
         @Nullable
         protected InputStream fetchIssueDetailsXml(@NotNull String url, @Nullable Credentials credentials)
                 throws IOException, InvalidKeyException, NoSuchAlgorithmException {
             HttpClient httpClient = HttpUtil.createHttpClient(120, new URL(url), credentials);
             GetMethod get = new GetMethod(url);
             get.addRequestHeader("x-rmtrack-date", DateUtil.formatDate(new Date(), DateUtil.PATTERN_RFC1123));
 
             StringBuilder b = new StringBuilder();
             b.append("GET");
             b.append("\n");
             b.append(new URL(url).getPath());
             b.append("\n");
             b.append(get.getRequestHeader("x-rmtrack-date").getValue());
             b.append("\n");
 
             BASE64Encoder base64 = new BASE64Encoder();
             String signature = base64.encode(calculateHMAC(b.toString().getBytes(), mySecretKey.getBytes()));
             get.addRequestHeader("Authorization", "RMT " + myAccessKey + ":" + signature);
 
             int code = httpClient.executeMethod(get);
 
             if (code < 200 || code >= 300) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("HTTP Response body:\n" + StreamUtil.readText(get.getResponseBodyAsStream()));
                 }
                 throw new RuntimeException("Failed to fetch issue details for \"" + url + "\", HTTP response code: " + code);
             }
 
             LOG.debug("HTTP response: " + code + ", length: " + get.getResponseContentLength());
             return get.getResponseBodyAsStream();
         }
 
         byte[] calculateHMAC(byte[] data, byte[] secret) throws NoSuchAlgorithmException, InvalidKeyException {
             SecretKey key = new SecretKeySpec(secret, "HmacSHA1");
             Mac m = Mac.getInstance("HmacSHA1");
             m.init(key);
             return m.doFinal(data);
         }
     }
 
     @NotNull
     public IssueData getIssue(@NotNull String host, @NotNull String id, @Nullable Credentials credentials) throws Exception {
         String url = getUrl(host, id);
         RMTIssueFetchFunction fetchFunction = new RMTIssueFetchFunction(host, id, credentials);
         return getFromCacheOrFetch(url, fetchFunction);
     }
 
     @NotNull
     public String getUrl(@NotNull String repository, @NotNull String id) {
        return String.format("https://rmtrack.com/%s/NonAdmin/Issues/IssueDetails.aspx?IssueId=%s", repository, getRealId(id));
     }
 
     @NotNull
     private String getApiUrl(@NotNull String repository, @NotNull String id) {
         return String.format("https://rmtrack.com/%s/rmtrackapi/IssueDetails.ashx?Include=Fields&IssueId=%s", repository, getRealId(id));
     }
 
     private String getRealId(@NotNull String id) {
         Matcher matcher = myPattern.matcher(id);
         String realId = id;
         if (matcher.find()) {
             realId = matcher.group(1);
         }
         return realId;
     }
 }
