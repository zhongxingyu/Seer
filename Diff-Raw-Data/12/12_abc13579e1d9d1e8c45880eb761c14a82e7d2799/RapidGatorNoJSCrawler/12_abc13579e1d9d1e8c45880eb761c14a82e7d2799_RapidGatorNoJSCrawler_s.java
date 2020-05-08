 package net.pickapack.spider.noJs.crawler.example;
 
 import net.pickapack.dateTime.DateHelper;
 import net.pickapack.io.file.IterableBigTextFile;
 import net.pickapack.spider.noJs.crawler.NoJSCrawler;
 import net.pickapack.spider.noJs.spider.HttpMethod;
 import net.pickapack.spider.noJs.spider.NoJSSpider;
 import net.pickapack.spider.noJs.spider.Page;
 import org.apache.commons.io.FileUtils;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import javax.xml.transform.TransformerException;
 import javax.xml.xpath.XPathExpressionException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 public class RapidGatorNoJSCrawler extends NoJSCrawler {
     public RapidGatorNoJSCrawler(String userAgent) {
         this(userAgent, null, -1);
     }
 
     public RapidGatorNoJSCrawler(String userAgent, String proxyHost, int proxyPort) {
         super(userAgent, 600 * 60 * 1000, proxyHost, proxyPort);
     }
 
     @Override
     protected void onPageVisited(Page page, Map<String, Object> context) {
         System.out.printf("[%s] Visiting page: %s\n", DateHelper.toString(new Date()), page.getUrl());
     }
 
     public void downloadFiles(String userId, String password, List<String> urls, final String folderFiles) {
         try {
             List<NameValuePair> requestParameters = new ArrayList<org.apache.http.NameValuePair>();
 
             requestParameters.add(new BasicNameValuePair("LoginForm[email]", userId));
             requestParameters.add(new BasicNameValuePair("LoginForm[password]", password));
 
             Page page = this.getPage(new URL("http://rapidgator.net/auth/login"), HttpMethod.POST, requestParameters, null, null);
             if(page.getResponse().getStatusCode() != 302) {
                 return;
             }
 
             ExecutorService downloadDocumentsService = Executors.newFixedThreadPool(5);
 
             for(final String url : urls) {
                 downloadDocumentsService.submit(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             Page page = getPage(new URL(url));
 
                             page.setText(page.getText().replaceAll("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">", "<html lang=\"en\">"));
 
                             String href = page.getFirstByXPath("//div[@class='main-block wide']//div[@class='btm']/p/a/@href").getNodeValue();
                             String fileName = page.getFirstByXPath("//div[@class='main-block wide']//div[@class='btm']/p/a/text()").getNodeValue().trim();
                             downloadDocument(null, new URL(href), folderFiles + "/" + fileName);
                         } catch (MalformedURLException e) {
                             throw new RuntimeException(e);
                         } catch (TransformerException e) {
                             throw new RuntimeException(e);
                         } catch (XPathExpressionException e) {
                             throw new RuntimeException(e);
                         } catch (IOException e) {
                             throw new RuntimeException(e);
                         }
                     }
                 });
             }
 
             downloadDocumentsService.shutdown();
             while (!downloadDocumentsService.isTerminated()) {
                 try {
                     downloadDocumentsService.awaitTermination(1, TimeUnit.SECONDS);
                 } catch (InterruptedException e) {
                     throw new RuntimeException(e);
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
 
     public static void main(String[] args) throws FileNotFoundException {
         if (args.length == 1) {
             String fileNameUrls = args[0];
 
             List<String> urls = new ArrayList<String>();
 
             for(String url : new IterableBigTextFile(new FileReader(fileNameUrls))) {
                 url = url.trim();
                 if(!url.isEmpty()) {
                     urls.add(url);
                 }
             }
 
 //            RapidGatorNoJSCrawler crawler = new RapidGatorNoJSCrawler(NoJSSpider.FIREFOX_3_6);
             RapidGatorNoJSCrawler crawler = new RapidGatorNoJSCrawler(NoJSSpider.FIREFOX_3_6, "localhost", 8888);
            crawler.downloadFiles("joyxsn@hotmail.com", "557999", urls, FileUtils.getUserDirectoryPath() + File.separatorChar + "Videos" + File.separatorChar + "New3" + File.separatorChar + "Rape");
         }
     }
 }
