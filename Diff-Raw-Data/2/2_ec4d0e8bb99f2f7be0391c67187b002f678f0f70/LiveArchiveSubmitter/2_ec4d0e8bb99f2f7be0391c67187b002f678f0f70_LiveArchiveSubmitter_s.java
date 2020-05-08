 package com.dumbear.dumboj.submitter;
 
 import java.io.File;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.dumbear.dumboj.R;
 import com.dumbear.dumboj.util.Utility;
 
 public class LiveArchiveSubmitter extends Submitter {
     public static final String SITE = "LiveArchive";
     public static final String DEFAULT_CHARSET = "UTF-8";
 
     private static BlockingQueue<Integer> accountIds = new LinkedBlockingQueue<Integer>();
     private static Account[] accounts;
 
     static {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder builder = factory.newDocumentBuilder();
             Document document = builder.parse(new File(SITE + "Accounts.xml"));
             NodeList nodes = document.getElementsByTagName("Account");
             accounts = new Account[nodes.getLength()];
             for (int i = 0; i < nodes.getLength(); ++i) {
                 Element element = (Element)nodes.item(i);
                 accounts[i] = new Account(element.getAttribute("username"), element.getAttribute("password"));
             }
             for (int i = 0; i < accounts.length; ++i) {
                 accountIds.put(i);
             }
         } catch (Exception e) {
             R.logger.severe("Load " + SITE + " accounts error: " + e);
             System.exit(1);
         }
     }
 
     public static final String homeUrl = "http://livearchive.onlinejudge.org";
     public static final String loginUrl = "http://livearchive.onlinejudge.org/index.php?option=com_comprofiler&task=login";
     public static final String statusUrl = "http://livearchive.onlinejudge.org/index.php?option=com_onlinejudge&Itemid=9";
     public static final String submitUrl = "http://livearchive.onlinejudge.org/index.php?option=com_onlinejudge&Itemid=25&page=save_submission";
     public static final String additionalUrl = "http://livearchive.onlinejudge.org/index.php?option=com_onlinejudge&Itemid=9&page=show_compilationerror&submission=";
 
     private int accountId;
     private Account account;
     private StringBuffer cookie;
 
     private void login() throws Exception {
         try {
             for (int i = 3; i > 0; --i) {
                 StringBuffer buffer = new StringBuffer();
                 buffer.append("username=");
                 buffer.append(URLEncoder.encode(account.username, DEFAULT_CHARSET));
                 buffer.append("&passwd=");
                 buffer.append(URLEncoder.encode(account.password, DEFAULT_CHARSET));
                 buffer.append("&remember=");
                 buffer.append(URLEncoder.encode("yes", DEFAULT_CHARSET));
                 cookie = new StringBuffer();
                 String source = Utility.getHtmlSourceByGet(homeUrl, DEFAULT_CHARSET, cookie);
                 source = Utility.getMatcherString(source, "<form action=\"http://livearchive\\.onlinejudge\\.org/index\\.php\\?option=com_comprofiler&amp;task=login\"([\\s\\S]*?)</form>", 1);
                 Matcher matcher = Pattern.compile("<input type=\"hidden\" name=\"([\\s\\S]*?)\" value=\"([\\s\\S]*?)\" />").matcher(source);
                 while (matcher.find()) {
                     buffer.append("&" + URLEncoder.encode(matcher.group(1), DEFAULT_CHARSET));
                     buffer.append("=" + URLEncoder.encode(matcher.group(2), DEFAULT_CHARSET));
                 }
                 byte[] bytes = buffer.toString().getBytes(DEFAULT_CHARSET);
                 Utility.getHtmlSourceByPost(loginUrl, DEFAULT_CHARSET, bytes, cookie);
                 if (!checkLogin()) {
                     if (i == 1) {
                         throw new Exception("Cannot login");
                     }
                     continue;
                 }
                 break;
             }
         } catch (Exception e) {
             R.logger.warning("Login failed: " + e);
             throw new Exception("Login failed");
         }
     }
 
     private boolean checkLogin() throws Exception {
         String source = Utility.getHtmlSourceByGet(homeUrl, DEFAULT_CHARSET, cookie);
         return source.contains("<input type=\"hidden\" name=\"op2\" value=\"logout\" />");
     }
 
     private int fetchLastId() throws Exception {
         String id = "";
         try {
             for (int i = 3; i > 0; --i) {
                 String source = Utility.getHtmlSourceByGet(statusUrl, DEFAULT_CHARSET, cookie);
                 id = Utility.getMatcherString(source, "<td width=\"2%\" class=\"title\">#</td>[\\s\\S]*?<tr class=\"sectiontableentry1\">\\s*<td>(\\d+)</td>", 1);
                 if (id.isEmpty() && !source.contains("<td width=\"2%\" class=\"title\">#</td>")) {
                     if (i == 1) {
                         throw new Exception("Cannot fetch last id");
                     }
                     Thread.sleep(4096);
                     continue;
                 }
                 break;
             }
         } catch (Exception e) {
             R.logger.warning("Fetch last id failed: " + e);
             throw new Exception("Fetch last id failed");
         }
         return id.isEmpty() ? 0 : Integer.parseInt(id);
     }
 
     private void submit() throws Exception {
         StringBuffer buffer = new StringBuffer();
         buffer.append("localid=");
         buffer.append(URLEncoder.encode(submission.problemId, DEFAULT_CHARSET));
         buffer.append("&language=");
         buffer.append(URLEncoder.encode(submission.language, DEFAULT_CHARSET));
         buffer.append("&code=");
         buffer.append(URLEncoder.encode(submission.sourceCode, DEFAULT_CHARSET));
         try {
             byte[] bytes = buffer.toString().getBytes(DEFAULT_CHARSET);
             String source = Utility.getHtmlSourceByPost(submitUrl, DEFAULT_CHARSET, bytes, cookie);
             if (!source.trim().isEmpty()) {
                 throw new Exception("Submit rejected");
             }
         } catch (Exception e) {
             R.logger.warning("Submit failed: " + e);
             throw new Exception("Submit failed");
         }
     }
 
     private void fetchResult(int lastId) throws Exception {
         String regex = "<td>(\\d+)</td>\\s*" +
                        "<td align=\"right\">[\\s\\S]*?</td>\\s*" +
                        "<td>[\\s\\S]*?</td>\\s*" +
                        "<td>([\\s\\S]*?)</td>\\s*" +
                        "<td>[\\s\\S]*?</td>\\s*" +
                        "<td>([\\s\\S]*?)</td>";
         Pattern pattern = Pattern.compile(regex);
         try {
             long now = new Date().getTime();
            while (new Date().getTime() - now < 10 * 60 * 1000) {
                 String source = Utility.getHtmlSourceByGet(statusUrl, DEFAULT_CHARSET, cookie);
                 Matcher matcher = pattern.matcher(source);
                 if (matcher.find() && Integer.parseInt(matcher.group(1)) > lastId) {
                     submission.originalId = matcher.group(1);
                     submission.result = matcher.group(2).replaceAll("<[\\s\\S]*?>", "").trim();
                     if (!submission.result.contains("ing") && !submission.result.equals("In judge queue") && !submission.result.equals("Sent to judge") && !submission.result.equals("Received") && !submission.result.isEmpty()) {
                         if (submission.result.equals("Accepted")) {
                             submission.time = (int)(Double.parseDouble(matcher.group(3)) * 1000.0);
                         } else if (submission.result.equals("Compilation error")) {
                             fetchAdditionalInfo();
                         }
                         return;
                     }
                     R.updateSubmission(submission);
                 }
                 Thread.sleep(2048);
             }
             throw new Exception("Cannot fetch result");
         } catch (Exception e) {
             R.logger.warning("Fetch result failed: " + e);
             throw new Exception("Fetch result failed");
         }
     }
 
     private void fetchAdditionalInfo() throws Exception {
         try {
             String source = Utility.getHtmlSourceByGet(additionalUrl + submission.originalId, DEFAULT_CHARSET, cookie);
             submission.additionalInfo = Utility.getMatcherString(source, "<pre>([\\s\\S]*?)</pre>", 1);
         } catch (Exception e) {
             R.logger.warning("Fetch additional info failed: " + e);
             throw new Exception("Fetch additional info failed");
         }
     }
 
     @Override
     public void start() throws Exception {
         accountId = accountIds.take();
         try {
             account = accounts[accountId];
             R.updateSubmission(submission.id, "Sending to " + SITE);
             login();
             int lastId = fetchLastId();
             submit();
             fetchResult(lastId);
             Thread.sleep(4096);
             R.updateSubmission(submission);
         } catch (Exception e) {
             R.updateSubmission(submission.id, "DumbJudge Error");
             throw e;
         } finally {
             accountIds.put(accountId);
         }
     }
 }
