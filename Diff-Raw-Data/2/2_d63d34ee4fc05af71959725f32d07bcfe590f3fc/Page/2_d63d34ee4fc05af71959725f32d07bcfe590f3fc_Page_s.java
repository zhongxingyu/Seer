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
 import java.util.LinkedList;
 import java.util.logging.Logger;
 
 import net.FileLoader;
 import net.GetHtml;
 import net.PageLoadException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import filter.Filter;
 import filter.FilterItem;
 import filter.FilterState;
 
 /**
  * This class represents a Page on the Board.
  */
 public class Page implements Runnable, Parsable{
 	private LinkedList<PageThread> pageThreads = new LinkedList<PageThread>();
 	private static Logger logger = Logger.getLogger(Page.class.getName());
 	private URL pageUrl, boardUrl;
 	private Filter filter;
 	private FileLoader imageLoader;
 	private int pageNumber;
 	private LinkedList<URL> threadUrls = new LinkedList<>();
 
 	private boolean stop = false;
 
 	public Page(URL boardUrl,int pageNumber, Filter filter,FileLoader imageLoader){
 		this.boardUrl = boardUrl;
 		this.filter = filter;
 		this.imageLoader = imageLoader;
 		this.pageNumber = pageNumber;
 		makePageUrl();
 	}
 
 	private void makePageUrl(){
 		try {
 			String boardurl;
 			if(boardUrl.toString().endsWith("/"))
 				boardurl = this.boardUrl.toString();
 			else
 				boardurl = this.boardUrl.toString()+"/";
 			
 			
 			if(pageNumber == 0)
 				this.pageUrl = new URL(boardurl);
 			else{
 				this.pageUrl = new URL(boardurl+pageNumber);
 			}
 		} catch (MalformedURLException e) {
 			logger.severe("Could not generate page URL for\n"+boardUrl+pageNumber);
 		}
 	}
 
 	public void setStop(boolean stop){
 		this.stop = stop;
 	}
 
 	public URL getPageUrl() {
 		return pageUrl;
 	}
 
 	@Override
 	public void parseHtml(String html){
 		threadUrls.clear();
 		
 		if(html == null || html.equals("")){
 			logger.warning("No html data for " + pageUrl);
 			return;
 		}
 		
 		Document pageDocument = Jsoup.parse(html);
 		
 		Elements board = pageDocument.select("#delform > div.board");
 		Elements threads = board.first().getElementsByClass("thread");
 		
 		for(Element thread : threads){
 			String relativeThreadUrl = thread.getElementsByClass("replylink").first().attr("href");
 			
 			try {
				threadUrls.add(new URL(boardUrl.toString()+ "/" + relativeThreadUrl));
 			} catch (MalformedURLException e) {
 				logger.warning("unable to process thread URL.\n " + relativeThreadUrl + "\n" + e.getMessage());
 			}
 		}
 	}
 	
 	public LinkedList<URL> getThreadUrls() {
 		return threadUrls;
 	}
 
 	@Override
 	public void run(){
 		if(stop){return;}
 			
 		pageThreads.clear();
 
 		parseHtml(loadUrl(pageUrl));
 
 		boolean blocked = false;
 
 		for(URL url : threadUrls){
 			if(stop){break;}
 			blocked = false;
 			FilterState threadFilterState = filter.getFilterState(url);
 
 			if(threadFilterState == FilterState.DENY | threadFilterState == FilterState.PENDING){
 				continue;
 			}
 
 			PageThread pt = new PageThread(url);
 			pt.processThread(loadUrl(pt.getThreadUrl()));
 			String response;
 
 			for(Post p : pt.getPosts()){
 				response = filter.checkPost(p);
 				
 				if(response != null && threadFilterState != FilterState.ALLOW){
 					filter.reviewThread(new FilterItem(pt.getThreadUrl(), pt.getBoardDesignation(), response, FilterState.PENDING));
 					filter.downloadThumbs(pt.getThreadUrl().toString(), pt.getPosts());
 					blocked = true;
 					break;
 				}
 			}
 
 			if(! blocked)
 				pageThreads.add(pt);
 		}
 
 		for(PageThread pt : pageThreads){
 			if(stop){break;}
 
 			for(Post p : pt.getPosts()){
 				if(stop){break;}
 
 				if(p.hasImage()){
 						imageLoader.add(p.getImageUrl(), pt.getBoardDesignation()+"\\"+pt.getThreadNumber()+"\\"+p.getImageName());
 				}
 			}
 		}
 		
 		if(! stop)
 			try{Thread.sleep(10*1000);}catch(InterruptedException ie){}
 	}
 
 	private String loadUrl(URL url){
 		String html ="";
 
 		try {
 			html = new GetHtml().get(url);
 		
 		} catch (PageLoadException ple) {
 			if(ple.getResponseCode() == 404 || ple.getResponseCode() == 500)
 				logger.warning("unable to load "+pageUrl.toString()+" , ResponseCode: "+ple.getResponseCode());//TODO do something else?
 			else
 				logger.warning("unable to load "+pageUrl.toString()+" , ResponseCode: "+ple.getResponseCode());
 		} catch (IOException io) {
 			logger.warning("unable to load "+pageUrl.toString()+" , ResponseCode: "+io.getMessage());
 		}
 		return html;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(!(obj instanceof Page))
 			return false;
 		
 		Page p = (Page)obj;
 		
 		if(getPageUrl().toString().equals(p.getPageUrl().toString()))
 			return true;
 		
 		return false;
 	}
 }
