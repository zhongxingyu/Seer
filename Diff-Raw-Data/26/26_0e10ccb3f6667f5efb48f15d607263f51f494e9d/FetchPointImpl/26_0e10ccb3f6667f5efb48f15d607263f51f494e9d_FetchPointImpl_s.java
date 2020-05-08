 package spiderman.plugin.impl;
 
 import org.eweb4j.spiderman.fetcher.FetchResult;
 import org.eweb4j.spiderman.plugin.FetchPoint;
 import org.eweb4j.spiderman.spider.SpiderListener;
 import org.eweb4j.spiderman.task.Task;
 
 import spiderman.plugin.util.PageFetcherImpl;
 import spiderman.plugin.util.SpiderConfig;
 
 /**
  * 一个Host一个FetchPointImpl对象
  * @author weiwei l.weiwei@163.com
  * @date 2013-1-7 下午06:40:05
  */
 public class FetchPointImpl implements FetchPoint{
 
 	private SpiderListener listener = null;
 	private Task task = null;
 	
 	public void init(Task task, SpiderListener listener) throws Exception {
 		this.task = task;
 		this.listener = listener;
 	}
 	
 	public FetchResult fetch(FetchResult result) throws Exception {
 		synchronized (this.task) {
 			if (this.task.site.fetcher == null){
 				PageFetcherImpl fetcher = new PageFetcherImpl();
 				SpiderConfig config = new SpiderConfig();
 				config.setCharset(task.site.getCharset());
 				fetcher.setConfig(config);
 				
 				fetcher.init(this.task.site);
 				this.task.site.fetcher = fetcher;
 			}
 			return this.task.site.fetcher.fetch(task.url);
 		}
 //		return fetch();
 	}
 	
 //	private FetchResult fetch(){
 //		FetchResult fetchResult = new FetchResult();
 //		CrawlerConfiguration config = new CrawlerConfiguration(task.url);
 //		
 //		listener.onInfo(Thread.currentThread(), "crawling url: " + task.url);
 //
 //		Url urlToCrawl = new Url(config.beginUrl(), 0);
 //        Page page = config.downloader().get(urlToCrawl.link());
 //        if (page.getStatusCode() != Status.OK) {
 //        	listener.onError(Thread.currentThread(), "errorUrl->" + urlToCrawl.link(), new Exception(page.getStatusCode().name() + " link->" + urlToCrawl.link()));
 //        } else {
 //        	org.eweb4j.spiderman.fetcher.Page _page = new org.eweb4j.spiderman.fetcher.Page();
 //			_page.setContent(page.getContent());
 //			_page.setContentType("text/html");
 //			_page.setContentData(page.getContent().getBytes());
 //			_page.setCharset(page.getCharset());
 //			_page.setUrl(page.getUrl());
 //			fetchResult.setPage(_page);
 //			fetchResult.setFetchedUrl(page.getUrl());
 //			fetchResult.setStatusCode(page.getStatusCode().ordinal());
 //        }
 //
 //        for (String l : page.getLinks()) {
 //            String link = config.normalizer().normalize(l);
 //            final Url url = new Url(link, urlToCrawl.depth() + 1);
 //            //是否进入递归抓取，如果进入递归就需要控制深度
 //        }
 //        
 //        return fetchResult;
 //	}
 
 }
