 package net.pickapack.forumSprite.helper.crawler;
 
 import net.pickapack.dateTime.DateHelper;
 import net.pickapack.action.Action1;
 import net.pickapack.action.Function1;
 import net.pickapack.spider.noJs.crawler.NoJSCrawler;
 import net.pickapack.spider.noJs.crawler.media.MediaFileBeginDownloadingEvent;
 import net.pickapack.spider.noJs.spider.HttpMethod;
 import net.pickapack.spider.noJs.spider.NoJSSpider;
 import net.pickapack.spider.noJs.spider.Page;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.Node;
 
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPathExpressionException;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 public class JkForumNoJSCrawler extends NoJSCrawler {
     private String folderImages;
     private long timeLastReplied;
     private PrintWriter pwDownloadMediaUrls;
     private boolean downloadImages;
     private boolean downloadMediaFiles;
     private int numPages;
     private int maxNumPages;
 
     public JkForumNoJSCrawler(String userAgent, String folderImages, boolean downloadImages, boolean downloadMediaFiles, int maxNumPages) {
         this(userAgent, folderImages, downloadImages, downloadMediaFiles, maxNumPages, null, -1);
     }
 
     public JkForumNoJSCrawler(String userAgent, String folderImages, boolean downloadImages, boolean downloadMediaFiles, int maxNumPages, String proxyHost, int proxyPort) {
         super(userAgent, 60000, proxyHost, proxyPort);
         this.folderImages = folderImages;
         this.downloadImages = downloadImages;
         this.downloadMediaFiles = downloadMediaFiles;
         this.maxNumPages = maxNumPages;
         try {
             new File(FileUtils.getUserDirectoryPath() + this.folderImages).mkdirs();
             this.pwDownloadMediaUrls = new PrintWriter(new FileWriter(FileUtils.getUserDirectoryPath() + this.folderImages + "/media_urls.txt"));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void close() {
         this.pwDownloadMediaUrls.close();
         super.close();
     }
 
     @SuppressWarnings("unchecked")
     public void run(String url, String userName, String password) {
         try {
             System.out.printf("[%s] Logging in as: %s\n", DateHelper.toString(new Date()), userName);
 
             ArrayList<NameValuePair> requestParameters = new ArrayList<NameValuePair>();
             requestParameters.add(new BasicNameValuePair("fastloginfield", "username"));
             requestParameters.add(new BasicNameValuePair("username", userName));
             requestParameters.add(new BasicNameValuePair("password", password));
             requestParameters.add(new BasicNameValuePair("quickforward", "yes"));
             requestParameters.add(new BasicNameValuePair("handlekey", "ls"));
             requestParameters.add(new BasicNameValuePair("questionid", "0"));
             requestParameters.add(new BasicNameValuePair("answer", ""));
 
             Page page1 = this.getPage(new URL("http://www.jkforum.net/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&inajax=1"), HttpMethod.POST, requestParameters, null, null);
 
             if(page1.getText().contains("密碼錯誤次數過多，請 15 分鐘後重新登入")) {
                 System.out.printf("[%s] Failed to logged in as: %s (password is incorrect)\n", DateHelper.toString(new Date()), userName);
                 return;
             }
 
            if (!page1.getText().contains("歡迎你回來")) {
                 throw new IllegalArgumentException();
             }
 
             System.out.printf("[%s] Logged in as: %s\n", DateHelper.toString(new Date()), userName);
 
             this.visit(new URL(url));
         } catch (IOException e) {
             throw new RuntimeException(e);
         } catch (XPathExpressionException e) {
             throw new RuntimeException(e);
         } catch (TransformerException e) {
             throw new RuntimeException(e);
         }
     }
 
     private Random random = new Random();
 
     @SuppressWarnings("unchecked")
     @Override
     protected void onPageVisited(Page page, Map<String, Object> context) {
         System.out.printf("[%s] Visiting page: %s\n", DateHelper.toString(new Date()), page.getUrl());
 
         String pageUrl = page.getUrl().toString();
 
         if (pageUrl.startsWith("http://www.jkforum.net/forum-forumdisplay-fid")) {
             this.visitLinks(page, "//tbody[starts-with(@id,'normalthread_')]//tr//th//a[@class='xst'][@onclick='atarget(this)']", null, new Function1<Node, String>() {
                 @Override
                 public String apply(Node param) {
                     return param.getAttributes().getNamedItem("href").getNodeValue();
                 }
             });
 
             this.numPages++;
             if (this.numPages < this.maxNumPages) {
                 this.visitLinks(page, "//div[@class='pg']//a[@class='nxt']", null, new Function1<Node, String>() {
                     @Override
                     public String apply(Node param) {
                         return param.getAttributes().getNamedItem("href").getNodeValue();
                     }
                 });
             }
         } else if (pageUrl.startsWith("http://www.jkforum.net/thread") || pageUrl.startsWith("http://www.jkforum.net/forum-viewthread-tid")) {
             Node domTextSubject = page.getFirstByXPath("//a[@id='thread_subject']//text()");
             String title = domTextSubject != null ? domTextSubject.getNodeValue() : "";
             System.out.printf("%s%n", title);
 
             Node anchorReplyPost = (page).getFirstByXPath("//div[@class='like_locked']//div//a[text()='回覆']");
 
             if (anchorReplyPost == null) {
                 System.out.println("retrieved...");
 
                 if (this.downloadImages) {
                     List<Node> subjectContentUrlAttributes = page.getByXPath("//td[starts-with(@id,'postmessage_')]//img[@src]/@src");
 
                     List<URL> urls = new ArrayList<URL>();
                     for (Node node : subjectContentUrlAttributes) {
                         try {
                             String urlStr = node.getNodeValue();
                             if (urlStr.contains(":")) {
                                 urls.add(new URL(urlStr));
                             } else {
                                 System.out.printf("[%s] Invalid document url found: %s\n", DateHelper.toString(new Date()), urlStr);
                             }
                         } catch (MalformedURLException e) {
                             recordException(e);
                         }
                     }
 
                     this.downloadDocumentByUrls(null, FileUtils.getUserDirectoryPath() + this.folderImages + "/" + title, urls);
                 }
 
                 List<Node> downloadMediaUrlAttributes = page.getByXPath("//td[starts-with(@id,'postmessage_')]//div[@class='showhide']//a[not(starts-with(@href,'http://www.jkforum.net'))]/@href");
 
                 List<URL> downloadMediaUrls = new ArrayList<URL>();
                 for (Node node : downloadMediaUrlAttributes) {
                     try {
                         String urlStr = node.getNodeValue();
                         if (urlStr.contains(":")) {
                             downloadMediaUrls.add(new URL(urlStr));
                         } else {
                             System.out.printf("[%s] Invalid document url found: %s\n", DateHelper.toString(new Date()), urlStr);
                         }
                     } catch (MalformedURLException e) {
                         recordException(e);
                     }
                 }
 
                 pwDownloadMediaUrls.println(title + "(" + pageUrl + ")");
                 pwDownloadMediaUrls.flush();
                 for (URL downloadMediaUrl : downloadMediaUrls) {
                     System.out.println("Media download link found: " + downloadMediaUrl);
                     pwDownloadMediaUrls.println("  " + downloadMediaUrl);
                 }
                 pwDownloadMediaUrls.println();
                 pwDownloadMediaUrls.flush();
 
                 if (downloadMediaFiles) {
                     this.downloadMediaFiles(FileUtils.getUserDirectoryPath() + this.folderImages + "/" + title, downloadMediaUrls);
                 }
             } else {
                 String replyUrl = anchorReplyPost.getAttributes().getNamedItem("href").getNodeValue();
 
                 try {
                     Page pageReply = this.getPage(new URL("http://www.jkforum.net/" + replyUrl));
 
                     if (!pageReply.getText().contains("對不起，你無權在該版塊回文。")) {
                         System.out.printf("[%s] Visiting page: %s\n", DateHelper.toString(new Date()), pageReply.getUrl());
 
                         String formHash = pageReply.getFirstByXPath("//form[@id='postform']//input[@name='formhash']").getAttributes().getNamedItem("value").getNodeValue();
                         String postTime = pageReply.getFirstByXPath("//form[@id='postform']//input[@name='posttime']").getAttributes().getNamedItem("value").getNodeValue();
 
                         Node form = pageReply.getFirstByXPath("//form[@id='postform']");
 
                         ArrayList<NameValuePair> requestParameters = new ArrayList<NameValuePair>();
                         requestParameters.add(new BasicNameValuePair("formhash", formHash));
                         requestParameters.add(new BasicNameValuePair("posttime", postTime));
                         requestParameters.add(new BasicNameValuePair("wysiwyg", "0"));
                         requestParameters.add(new BasicNameValuePair("noticeauthor", ""));
                         requestParameters.add(new BasicNameValuePair("noticetrimstr", ""));
                         requestParameters.add(new BasicNameValuePair("noticeauthormsg", ""));
                         requestParameters.add(new BasicNameValuePair("subject", ""));
                         requestParameters.add(new BasicNameValuePair("checkbox", "0"));
                         requestParameters.add(new BasicNameValuePair("message", RandomStringUtils.randomAlphabetic(random.nextInt(100) + 100)));
                         requestParameters.add(new BasicNameValuePair("save", ""));
 
                         try {
                             long delta = System.currentTimeMillis() - timeLastReplied;
                             if (delta < 15000) {
                                 long timeToSleep = 15000 - delta;
                                 System.out.printf("delay replying after %d seconds...%n", timeToSleep / 1000);
                                 Thread.sleep(timeToSleep);
                             }
                         } catch (InterruptedException e) {
                             throw new RuntimeException(e);
                         }
 
                         Page pageReplyResult = this.getPage(new URL("http://www.jkforum.net/" + form.getAttributes().getNamedItem("action").getNodeValue()), HttpMethod.POST, requestParameters, null, null);
 
                         System.out.printf("[%s] Visiting page: %s\n", DateHelper.toString(new Date()), pageReplyResult.getUrl());
 
                         if (pageReplyResult.getText().contains("非常感謝")) {
                             this.onPageVisited(pageReply, null);
                             this.onPageVisited(pageReplyResult, null);
 
                             System.out.println("replied...");
 
                             timeLastReplied = System.currentTimeMillis();
 
                             this.removePageByUrl(page.getUrl().toString());
                             this.visit(page.getUrl());
                         } else if (pageReplyResult.getUrl().toString().equals(page.getUrl().toString()) && (pageReplyResult.getFirstByXPath("//div[@class='like_locked']//div//a[text()='回覆']") != null)) {
                             this.onPageVisited(pageReply, null);
                             this.onPageVisited(pageReplyResult, null);
 
                             System.out.println("replied...");
 
                             this.removePageByUrl(page.getUrl().toString());
                             this.visit(page.getUrl());
                         } else if (pageReplyResult.getText().contains("對不起，您兩次發表間隔少於 15 秒，請稍後再發表。")) {
                             this.onPageVisited(pageReply, null);
                             this.onPageVisited(pageReplyResult, null);
 
                             this.removePageByUrl(page.getUrl().toString());
 
                             try {
                                 long delta = System.currentTimeMillis() - timeLastReplied;
                                 if (delta < 15000) {
                                     long timeToSleep = 15000 - delta;
                                     System.out.printf("retry replying after %d seconds...%n", timeToSleep / 1000);
                                     Thread.sleep(timeToSleep);
                                 }
                             } catch (InterruptedException e) {
                                 throw new RuntimeException(e);
                             }
                             this.visit(page.getUrl());
                         } else {
                             this.onPageVisited(pageReply, null);
                             this.onPageVisited(pageReplyResult, null);
 
                             System.out.printf("failed to reply: %s (reply url: %s)%n", page.getUrl(), pageReplyResult.getUrl());
                             System.out.println(pageReplyResult.getText());
                         }
                     }
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 } catch (XPathExpressionException e) {
                     throw new RuntimeException(e);
                 } catch (TransformerException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
     }
 
     public String getFolderImages() {
         return folderImages;
     }
 
     public long getTimeLastReplied() {
         return timeLastReplied;
     }
 
     public boolean isDownloadImages() {
         return downloadImages;
     }
 
     public boolean isDownloadMediaFiles() {
         return downloadMediaFiles;
     }
 
     public int getNumPages() {
         return numPages;
     }
 
     public int getMaxNumPages() {
         return maxNumPages;
     }
 
     public static void main(String[] args) throws IOException {
         String folderImages = "/OOXXPicsSpider/JkForumNoJSCrawler";
 //        String url = "http://www.jkforum.net/forum-forumdisplay-fid-54-filter-author-orderby-dateline.html";
 //        String url = "http://www.jkforum.net/forum-forumdisplay-fid-1116-page-1.html";
 //        String url = "http://www.jkforum.net/forum-viewthread-tid-2975818-extra-page%3D1%26orderby%3Ddateline.html";
 //        String url = "http://www.jkforum.net/thread-3122913-1-2.html";
        String url = "http://www.jkforum.net/thread-3117859-1-1.html";
 
         if (args.length == 1) {
             url = args[0];
         }
 
         String fileNameSolvedUrls = FileUtils.getUserDirectory() + folderImages + "/" + "solvedUrls.txt";
         new File(fileNameSolvedUrls).getParentFile().mkdirs();
 
         final PrintWriter pwSolvedUrls = new PrintWriter(new FileWriter(fileNameSolvedUrls));
 
         JkForumNoJSCrawler crawler = new JkForumNoJSCrawler(NoJSSpider.FIREFOX_3_6, folderImages, false, true, 1, "localhost", 8888);
 //        JkForumNoJSCrawler crawler = new JkForumNoJSCrawler(NoJSSpider.FIREFOX_3_6, folderImages, false, true, 1);
 
         crawler.getEventDispatcher().addListener(MediaFileBeginDownloadingEvent.class, new Action1<MediaFileBeginDownloadingEvent>() {
             @Override
             public void apply(MediaFileBeginDownloadingEvent event) {
                 pwSolvedUrls.println(event.getUrl());
                 pwSolvedUrls.flush();
             }
         });
 
         crawler.run(url, "itecgo", "bywwnss");
 //        crawler.downloadMediaFiles(FileUtils.getUserDirectory() +  "/Desktop/", new URL("http://lumfile.com/kw6pubwjipim/2510.part1.rar.html"));
 //        crawler.downloadMediaFiles(FileUtils.getUserDirectory() +  "/Desktop/", new URL("http://www.filereactor.com/x95kztrx3e7v/5217a.rar.html"));
         crawler.close();
 
         pwSolvedUrls.close();
     }
 }
