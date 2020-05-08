 package com.ml.bus.schedule;
 
 
 import java.util.List;
 import java.util.Map;
 
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.scheduling.quartz.QuartzJobBean;
 
 import com.ml.bus.model.CrawlPattern;
 import com.ml.bus.model.News;
 import com.ml.bus.service.MemoryService;
 import com.ml.db.LinkDB;
 import com.ml.db.ParserDB;
 import com.ml.nlp.parser.IParser;
 import com.ml.nlp.parser.SinaNewsParser;
 import com.ml.nlp.parser.SohuNewsParser;
 
 public class ParserJob extends QuartzJobBean {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(ParserJob.class);
     
 	private MemoryService memoryService;
 	
     @Override
 	protected void executeInternal(JobExecutionContext context)
 			throws JobExecutionException {
 		LOGGER.info("Starting to parsing ......");
 		
 		Map<?, ?> dataMap = context.getJobDetail().getJobDataMap();
 		ApplicationContext ctx = (ApplicationContext)dataMap.get("applicationContext");
 		
 		memoryService = (MemoryService)ctx.getBean("memoryService");
 
 		List<CrawlPattern> crawlPatterns = memoryService.getCrawlPatterns();
 		for(CrawlPattern cp: crawlPatterns) {
 			String queueName = cp.getCrawlUrl();
 			String type = cp.getType();
 			IParser<?> parser = getParser(type);
 			
 			doParser(parser, queueName, 0);
 		}
 
 		
 		LOGGER.info("End of parsing");
 	}
     
 	
 	private void doParser(IParser<?> parser, String queueName, int limitNumber) {
 		
 		int i = 0;
 		// 循环迭代出连接，然后提取该连接中的新闻。limitNumber为0时不限制解析数量
 		while(!LinkDB.unVisitedUrlsEmpty(queueName) 
 				&& (i < limitNumber || limitNumber == 0)) {
 			String visitUrl = LinkDB.unVisitedUrlDeQueue(queueName);
 			if (visitUrl == null)
 				continue;
 			// 解析连接
			News news = (News) parser.parse(visitUrl);	
			if(news.getContent().equals("")) {
 				continue;
 			}
 			//http://it.sohu.com -> it
 			news.setOriginalCategory(queueName.substring(queueName.indexOf("/") + 2, queueName.indexOf(".")));
 			// 放到待分析队列
 			ParserDB.addUnAnalysizedNews(news);
 			
 			// 该 url 放入到已访问的 URL 中
 			LinkDB.addVisitedUrl(visitUrl);
 			
 			i++;
 		}
 	}
 	
 	private IParser<?> getParser(String name) {
 		IParser<?> parser = null;
 		
 		if(name.equals("sohu")) {
 			parser = new SohuNewsParser();
 		}
 		else if(name.equals("sina")) {
 			parser = new SinaNewsParser();
 		}
 		
 		return parser;
 	}
 	
 
 }
