 package bnrc.weibo.crawler.crawl;
 
 import bnrc.weibo.crawler.util.AccessToken;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author songsiqi
  *
  */
 public class Crawler {
 	
 	private static PublicStatusCrawler publicStatusCrawler = new PublicStatusCrawler();
 	
 	// 刷新token相关
 	private static Date lastRefreshTokenTime = null;			// 上次刷新token的时间
 	private static final int intervalOfRefreshToken = 1000 * 60 * 60 * 23;	// 两次刷新token之间的时间间隔
 	
 	// 请求控制相关
	private static final int intervalOfRequest = 1000 * 5;		// 每次请求之间的间隔
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
		Date currentTime = Calendar.getInstance().getTime();
		
 		// 生成token
 		AccessToken.generate();
		lastRefreshTokenTime = currentTime;
 		
 		while (true) {
 			
 			AccessToken.setOneAccessToken();
 			publicStatusCrawler.crawl();
 			
 			try {
 				Thread.sleep(intervalOfRequest);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			// 检测是否需要刷新token
 			if (currentTime.getTime() - lastRefreshTokenTime.getTime() > intervalOfRefreshToken) {
 				AccessToken.generate();
 				lastRefreshTokenTime = currentTime;
 			}
 		}
 	}
 	
 }
