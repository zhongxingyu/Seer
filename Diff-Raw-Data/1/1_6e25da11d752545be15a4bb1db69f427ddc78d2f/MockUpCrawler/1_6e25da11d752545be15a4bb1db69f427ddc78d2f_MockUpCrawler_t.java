 package no.magott.nicolas.crawler;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.core.task.TaskExecutor;
 
 public class MockUpCrawler {
 
 	@Autowired
 	private TaskExecutor taskExecutor;
 
 	private List<String> urls = Arrays.asList("http://www.mysite.com",
 			"http://www.yoursite.com");
 
 	public void doCrawl() throws InterruptedException, ExecutionException {
 		List<FutureTask<Boolean>> tasks = new ArrayList<FutureTask<Boolean>>();
 
 		// Runs crawling tasks
 		for (String url : urls) {
 			tasks.add(doCrawl(url));
 		}
 
 		// Checks status of crawling
 		int count = 0;
 		for (FutureTask<Boolean> futureTask : tasks) {
 			
 			try {
 				Boolean status = futureTask.get(1, TimeUnit.SECONDS);
 				System.out.println("Crawling of "+ urls.get(count)+ " finished with status " +status);
 			} catch (TimeoutException e) {
 				System.err.println("Crawling of "+urls.get(count) +" timed out");
 			}
			count++;
 		}
 
 	}
 
 	private FutureTask<Boolean> doCrawl(String url) {
 		FutureTask<Boolean> crawlingTask = createFuture();
 		taskExecutor.execute(crawlingTask);
 		return crawlingTask;
 
 	}
 
 	private FutureTask<Boolean> createFuture() {
 		final FutureTask<Boolean> task = new FutureTask<Boolean>(
 				new Callable<Boolean>() {
 					public Boolean call() throws Exception {
 						// Your crawling magic goes here:
 						System.out.println("Crawling something");
 						return true;
 					}
 				});
 		return task;
 	}
 
 	public void crawl() throws InterruptedException, ExecutionException {
 		ConfigurableApplicationContext context = null;
 		context = new ClassPathXmlApplicationContext("crawler-context.xml");
 		context.getAutowireCapableBeanFactory().autowireBeanProperties(this,
 				AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
 
 		doCrawl();
 		context.close();
 	}
 
 	public static void main(String[] args) throws InterruptedException,
 			ExecutionException {
 		MockUpCrawler crawler = new MockUpCrawler();
 		crawler.crawl();
 	}
 
 }
