 package com.mpages.parsing.crawler;
 
 import java.net.URLDecoder;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CrawlerIntegrationTest {
 	private static Logger log = LoggerFactory.getLogger(Crawler.class);
 	
 	private static final String LINKS_REGEXP = "href=\"(.{0,50}ru/offer/ad/.{0,50}/kiev/page.{0,50})\">";
 	private static final String DATA_REGEXP = "кий</td>\\W+<td   nowrap>(.{0,400})</td>.{0,800}<a href=\"(.{0,100})\" target=\"_blank\">подробнее</a>";
 
 	@Test
 	public void test() throws Exception
 	{
 		log.info(URLDecoder.decode("addSearch%5Bdt_id%5D=10&addSearch%5Bd_id%5D=0&addSearch%5Bt_id%5D=0&addSearch%5Bprice_min%5D=&addSearch%5Bprice_max%5D=&addSearch%5Bcurrency%5D=usd&addSearch%5BpriceFor%5D=0&addSearch%5Bfields%5D%5B384%5D%5Bmin%5D=0&addSearch%5Bfields%5D%5B384%5D%5Bmax%5D=0&addSearch%5Bfields%5D%5B388%5D%5Bmin%5D=&addSearch%5Bfields%5D%5B388%5D%5Bmax%5D=&addSearch%5Bfields%5D%5B390%5D%5Bmin%5D=&addSearch%5Bfields%5D%5B390%5D%5Bmax%5D=&addSearch%5Bphone%5D=&addSearch%5Bdays%5D=&fsbmtfReg=%CF%EE%E8%F1%EA", "UTF-8"));
 		Crawler crawler = new Crawler(DATA_REGEXP, LINKS_REGEXP, "http://www.svdevelopment.com/ru/offer/ad/10/kiev/page/11/");
		crawler.start();
		
 	}
 }
 
 
