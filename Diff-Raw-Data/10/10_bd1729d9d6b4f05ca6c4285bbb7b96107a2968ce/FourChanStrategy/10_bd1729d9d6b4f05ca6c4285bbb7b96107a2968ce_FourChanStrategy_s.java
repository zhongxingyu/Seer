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
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.github.dozedoff.commonj.net.GetHtml;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FourChanStrategy implements SiteStrategy {
 	GetHtml getHtml = new GetHtml();
 	static final Logger logger = LoggerFactory.getLogger(FourChanStrategy.class);
 	
 	@Override
 	public boolean validSiteStrategy(URL siteUrl) {
 		URL correctUrl;
 		
 		try {
 			correctUrl = new URL("http://www.4chan.org/");
 		} catch (MalformedURLException e) {
 			logger.error("Strategy check URL is incorrect. Please fix this!");
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
 			String url = siteUrl.toString();
 			parseError(url, "frontpage", e);
 			return boardMap;
 		}
 
 		Elements boards = mainDocument.select("div.column a.boardlink");
 
 		for (Element boardEntry : boards){
 			String url = boardEntry.attr("href");
 			String name = boardEntry.attr("title");
 			
 			try {
 				String fullUrl = "http:" + url;
 				boardMap.put(name, new URL(fullUrl));
 			} catch (Exception e) {
 				logger.warn("Could not add Board " + name + " due to: " + e.getMessage());
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
 			String url = boardUrl.toString();
 			parseError(url, "board", e);
 			return 0;
 		}
 		
		Elements pageLinks = boardDocument.select("div.pages > a");
		int pageCount = pageLinks.size() + 1;
 
 		return pageCount;
 	}
 
 	@Override
 	public List<URL> parsePage(URL pageUrl) {
 		LinkedList<URL> threadUrls = new LinkedList<>();
 		Document pageDocument;
 
 		try {
 			pageDocument = Jsoup.connect(pageUrl.toString()).userAgent("Mozilla").get();
 		} catch (Exception e) {
 			String url = pageUrl.toString();
 			parseError(url, "page", e);
 			return threadUrls;
 		}
 
 		Elements threadLinks = pageDocument.select("a.replylink");
 
 		for (Element thread : threadLinks) {
 			String absoluteThreadUrl = thread.attr("abs:href");
 
 			try {
 				URL threadUrl = new URL(absoluteThreadUrl);
 				threadUrls.add(threadUrl);
 			} catch (MalformedURLException e) {
 				parseError(absoluteThreadUrl, "thread", e);
 			}
 		}
 		return threadUrls;
 	}
 	
 	@Override
 	public List<Post> parseThread(URL pageThread) {
 		LinkedList<Post> postList = new LinkedList<>();
 		
 		String threadUrl = pageThread.toString();
 		
 		Document pageDocument;
 		try {
 			pageDocument = Jsoup.connect(threadUrl).userAgent("Mozilla").get();
 		} catch (IOException e) {
 			parseError(threadUrl, "page thread", e);
 			return postList;
 		}
 		
 		Element thread = pageDocument.select("#delform > div.board > div.thread").first();
 		Elements posts = thread.getElementsByClass("post");
 		
 		for(Element post : posts){
 			Post postObject = parsePost(threadUrl, post);
 			postList.add(postObject);
 		}
 		
 		return postList;
 	}
 
 	private Post parsePost(String threadUrl, Element post) {
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
 				logger.warn("Invalid image URL (" + imageUrl+ ") in thread " + threadUrl);
 				postObject.setImageName(null);
 				postObject.setImageUrl(null);
 			}
 		}
 		
 		postObject.setComment(post.getElementsByClass("postMessage").first().ownText());
 		return postObject;
 	}
 
 	@Override
 	public int getThreadNumber(URL threadUrl) {
 		String urlFragments[] = threadUrl.toString().split("/");
 		int threadNumber = 0;
 		
 		try{
 			threadNumber = Integer.parseInt(urlFragments[urlFragments.length - 1]); 
 		}catch(NumberFormatException nfe){
 			logger.warn("Got an invalid thread number for " + threadUrl.toString());
 		}
 		
 		return threadNumber;
 	}
 
 	@Override
 	public String getBoardShortcut(URL threadUrl) {
 		String urlFragments[] = threadUrl.toString().split("/");
 		
 		// can the URL be considered valid?
 		if(urlFragments.length < 4) {
 			return "";
 		}
 		
 		return urlFragments[urlFragments.length-1];
 	}
 	
 	private void parseError(String failedUrl, String pageType, Exception exception) {
 		logger.warn("Failed to parse " + pageType + " " + failedUrl + " because: " + exception.getMessage());
 	}
 }
