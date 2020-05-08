 package eu.alertproject.iccs.mlsensor.run;
 
 import eu.alertproject.iccs.mlsensor.subscribers.kde.api.KdeDownloader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import java.net.URL;
 import java.util.List;
 
 /**
  * User: fotis
  * Date: 04/11/11
  * Time: 20:49
  */
 public class Init {
 
     private static Logger logger = LoggerFactory.getLogger(Init.class);
 
     public static void main(String[] args) {
 
         ApplicationContext context = new ClassPathXmlApplicationContext(
                "applicationContext.xml"
         );
 
 
         KdeDownloader kdeDownloader = (KdeDownloader) context.getBean("kdeDownloader");
 
         logger.info("init() Initializing ");
         String url = "http://mail.kde.org/pipermail/kde-hardware-devel";
         try {
             List<URL> urls = kdeDownloader.fetchUrls(url);
             kdeDownloader.loadMessages(urls);
 
         } catch (Exception e) {
             logger.error("Couldn't load url {}", url, e);
         }
 
     }
 
 }
