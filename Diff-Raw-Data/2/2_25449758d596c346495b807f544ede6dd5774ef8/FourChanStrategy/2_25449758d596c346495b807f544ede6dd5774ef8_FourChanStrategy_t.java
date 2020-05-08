 /*  Copyright (C) 2012  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package board;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import net.GetHtml;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.sun.istack.internal.logging.Logger;
 
 public class FourChanStrategy implements SiteStrategy {
 	GetHtml getHtml = new GetHtml();
 	static final Logger logger = Logger.getLogger(FourChanStrategy.class);
 	
 	@Override
 	public boolean validSiteStrategy(URL siteUrl) {
 		URL correctUrl;
 		
 		try {
 			correctUrl = new URL("http://www.4chan.org/");
 		} catch (MalformedURLException e) {
 			logger.severe("Strategy check URL is incorrect. Please fix this!");
 			return false;
 		}
 		
 		return (siteUrl.getHost().equals(correctUrl.getHost()));
 	}
 
 	@Override
 	public Map<String, URL> findBoards(URL siteUrl) {
 		HashMap<String, URL> boardMap = new HashMap<>();
 		Document mainDocument;
 
 		try {
 			mainDocument = Jsoup.connect(siteUrl.toString()).userAgent("Mozilla").get();
 		} catch (Exception e) {
 			logger.warning("Failed to process Mainpage " + siteUrl.toString()
 					+ " Cause: " + e.getMessage());
 			return boardMap;
 		}
 
 		Elements boards = mainDocument.select("a.boardlink");
 
 		for (Element boardEntry : boards){
 			String url = boardEntry.attr("href");
 			String name = boardEntry.attr("title");
 			
 			try {
 				String fullUrl = "http:" + url;
 				boardMap.put(name, new URL(fullUrl));
 			} catch (Exception e) {
 				logger.warning("Could not add Board " + name + " due to: " + e.getMessage());
 			}
 		}
 		
 		return boardMap;
 	}
 
 	@Override
 	public int getBoardPageCount(URL boardUrl) {
 		Document boardDocument;
 		
 		try {
 			boardDocument = Jsoup.connect(boardUrl.toString()).userAgent("Mozilla").get();
 		} catch (Exception e) {
 			logger.warning("Failed to process board " + boardUrl.toString()
 					+ " Cause: " + e.getMessage());
 			return 0;
 		}
 		
 		Elements pageLinks = boardDocument.select("div.pages > a");
 		int pageCount = pageLinks.size() + 1;
 
 		return pageCount;
 	}
 
 	@Override
 	public List<PageThread> parsePage(URL pageUrl) {
 		LinkedList<PageThread> pageThreads = new LinkedList<>();
 		Document pageDocument;
 
 		try {
 			pageDocument = Jsoup.connect(pageUrl.toString()).userAgent("Mozilla").get();
 		} catch (Exception e) {
 			logger.warning("Failed to process page " + pageUrl.toString()
 					+ " Cause: " + e.getMessage());
 			return pageThreads;
 		}
 
 		Elements threadLinks = pageDocument.select("a.replylink");
 
 		for (Element thread : threadLinks) {
 			String absoluteThreadUrl = thread.attr("abs:href");
 
 			try {
 				URL threadUrl = new URL(absoluteThreadUrl);
 				PageThread pageThread = new PageThread(threadUrl);
 				pageThreads.add(pageThread);
 			} catch (MalformedURLException e) {
 				logger.warning("Unable to process thread URL.\n "
 						+ absoluteThreadUrl + "\n" + e.getMessage());
 			}
 		}
 		return pageThreads;
 	}
 	
 	@Override
 	public List<Post> parseThread(PageThread pageThread) {
 		LinkedList<Post> postList = new LinkedList<>();
 		
 		String threadUrl = pageThread.getThreadUrl().toString();
 		
 		Document pageDocument;
 		try {
 			pageDocument = Jsoup.connect(threadUrl).userAgent("Mozilla").get();
 		} catch (IOException e) {
			logger.warning("Failed to parse " + threadUrl + " because: " + e.getMessage());
 			return postList;
 		}
 		
 		Element thread = pageDocument.select("#delform > div.board > div.thread").first();
 		Elements posts = thread.getElementsByClass("post");
 		
 		for(Element post : posts){
 			Post postObject = new Post();
 			
 			Elements fileElements = post.getElementsByClass("file");
 			
 			for(Element file : fileElements){
 				String imageUrl = "?";
 				try{
 					Element imageInfo = file.select("div.fileInfo > span.fileText").first();
 					if(imageInfo == null){
 						// the image was deleted
 						continue;
 					}
 					
 					postObject.setImageName(imageInfo.select("span").attr("title"));
 					imageUrl = imageInfo.select("a").attr("href");
 					
 					postObject.setImageUrl(new URL("https:" + imageUrl));
 				}catch(MalformedURLException mue){
 					logger.warning("Invalid image URL (" + imageUrl+ ") in thread " + threadUrl);
 					postObject.setImageName(null);
 					postObject.setImageUrl(null);
 				}
 			}
 			
 			postObject.setComment(post.getElementsByClass("postMessage").first().ownText());
 			
 			postList.add(postObject);
 		}
 		
 		return postList;
 	}
 }
