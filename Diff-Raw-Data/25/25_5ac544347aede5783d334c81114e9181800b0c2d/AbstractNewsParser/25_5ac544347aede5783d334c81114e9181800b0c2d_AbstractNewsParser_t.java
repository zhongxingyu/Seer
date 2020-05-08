 package com.ml.nlp.parser;
 
 import java.util.Date;
 
 import org.htmlparser.NodeFilter;
 import org.htmlparser.Parser;
 import org.htmlparser.util.ParserException;
 
 import com.ml.bus.model.News;
 import com.ml.util.TimeExtractUtil;
 
 public abstract class AbstractNewsParser implements IParser<News> {
 	
     protected abstract String getTitle(NodeFilter titleFilter, Parser parser) throws ParserException;
     protected abstract String getAuthor(NodeFilter authorFilter, Parser parser) throws ParserException;
     protected abstract String getDate(NodeFilter dateFilter, Parser parser) throws ParserException;
     protected abstract String getContent(NodeFilter contentFilter, Parser parser) throws ParserException;
     protected abstract String getImg(NodeFilter imgFilter, Parser parser) throws ParserException;
     protected abstract String getSource(NodeFilter sourceFilter, Parser parser) throws ParserException;
 	
     protected abstract NodeFilters getNodeFilters();
     
     /**
      * 对新闻URL进行解析提取新闻，同时将新闻插入到数据库中。
      */
 	public final News parse(String url) {
 		String title = null;
 		String content = null;
 		String date = null;
		String author = "";
		String img = "";
		String source = "";
		News news = null;
 		try{
 			Parser parser = new Parser(url);
 	        NodeFilters nodeFilters = getNodeFilters();
 	        
 	        title = getTitle(nodeFilters.getTitleFilter(), parser);
 	        parser.reset();   //记得每次用完parser后，要重置一次parser。要不然就得不到我们想要的内容了。
 	        content = getContent(nodeFilters.getContentFilter(), parser);
 	        parser.reset();
 	        date = getDate(nodeFilters.getDateFilter(), parser);
 	        parser.reset();
 	        author = getAuthor(nodeFilters.getAuthorFilter(), parser);
 	        parser.reset();
 	        img = getImg(nodeFilters.getImgFilter(), parser);
 	        parser.reset();
 	        source = getSource(nodeFilters.getSourceFilter(), parser);
	        
	        //先设置新闻对象，让新闻对象里有新闻内容。
	        news = this.setNews(title, author, content, date, url, img, source);
 		} catch (Exception e) {
 			System.out.println("error url: " + url);
 			e.printStackTrace();
 		}
        
             
         return news;
     }
     
     /**
      *  设置新闻对象，让新闻对象里有新闻数据
      * @param newsTitle 新闻标题
      * @param newsauthor  新闻作者
      * @param newsContent 新闻内容
      * @param newsDate  新闻日期
      * @param url  新闻链接
      */
     private News setNews(String title, String author, String content, String date, String url, String img, String source) {
     	News news = new News();
     	news.setTitle(title);
     	news.setAuthor(author);
     	news.setContent(content);	//compress content
     	Date d = null;
     	try {
     		d = TimeExtractUtil.extractDate(date, "");
     	} catch (Exception e) {
     		System.out.println(e.getMessage());
     	}
     	
     	news.setDate(d);
     	
     	news.setUrl(url);	//short url
     	news.setImg(img);
     	news.setSource(source);
     	
     	return news;
     }
     
 }
