 /**
  * 
  */
 package org.qza.gft.crawler;
 
 import java.util.Date;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
 /**
  * @author qza
  * 
  *         Initiates crawling process
  * 
  */
 public class CrawlerRunner {
 
 	private ApplicationContext ac;
 
 	private CrawlerSpawner spawner;
 
 	private CrawlerResulter resulter;
 
 	final Logger log = LoggerFactory.getLogger(CrawlerRunner.class);
 
 	/**
 	 * Created new CrawlerRunner instance
 	 */
 
 	public CrawlerRunner() {
 		ac = new AnnotationConfigApplicationContext(CrawlerConfig.class);
 		spawner = ac.getBean(CrawlerSpawner.class);
 		resulter = ac.getBean(CrawlerResulter.class);
 	}
 
 	public void start() {
 		log.info("Crawling process started (" + new Date() + ")");
 		spawner.spawn();
 		log.info("Writing links to file ... (" + new Date() + ")");
 		resulter.writeLinksToFile();
 		log.info("Crawling process ended (" + new Date() + ")");
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		CrawlerRunner runner = new CrawlerRunner();
 		runner.start();
		System.exit(0);
 	}
 
 }
