 package me.hujin.rss.parser;
 
 import me.hujin.rss.util.RssDateTime;
 
 
 /**
  * present a single rss item.
  * 
  * @author Jin Hu <bixuehujin@gmail.com>
  */
 
 public class RSSItem {
 	
 	private String title;
 	
 	private String link;
 	private StringBuffer linkBuffer = new StringBuffer();
 	
 	private String description;
 	private StringBuffer descriptionBuffer = new StringBuffer();
 	
 	private String pubDate;
 	private StringBuffer pubDateBuffer = new StringBuffer();
 	
 	private String creator;
 	
 	private String content;
 	private StringBuffer contentBuffer = new StringBuffer();
 	
 	
 	public RSSItem(String title, String link, 
 			String description, String pubDate, String creator) {
 		this.title = title;
 		this.link = link;
 		this.description = description;
 		this.pubDate = pubDate;
 		this.creator = creator;
 	}
 	
 	public RSSItem(){
 		
 	}
 	
 	public String getTitle() {
		return this.title;
 	}
 	
 	public RSSItem setTitle(String title) {
 		this.title = title;
 		return this;
 	}
 	
 	public String getLink() {
 		if(link == null) {
 			link = linkBuffer.toString();
 		}
 		return link;
 	}
 	
 	public RSSItem setLink(String link) {
 		this.link = link;
 		return this;
 	}
 	
 	public void appendLink(String link) {
 		linkBuffer.append(link);
 	}
 
 	public String getDescription() {
 		if(description == null) {
 			description = descriptionBuffer.toString();
 		}
 		return description;
 	}
 
 	public RSSItem setDescription(String description) {
 		this.description = description;
 		return this;
 	}
 	
 	public void appendDescription(String desc) {
 		descriptionBuffer.append(desc);
 	}
 
 	public String getPubDate() {
 		if(pubDate == null) {
 			pubDate = pubDateBuffer.toString();
 		}
 		return pubDate;
 	}
 
 	public RSSItem setPubDate(String pubDate) {
 		this.pubDate = pubDate;
 		return this;
 	}
 
 	public void appendPubDate(String pubDate) {
 		pubDateBuffer.append(pubDate);
 	}
 	
 	public String getCreator() {
 		if(creator == null) {
 			return "";
 		}
 		return creator;
 	}
 
 	public String getContent() {
 		if(content == null) {
 			content = contentBuffer.toString();
 		}
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 	
 	public void appendContent(String content) {
 		contentBuffer.append(content);
 	}
 
 	public RSSItem setCreator(String creator) {
 		this.creator = creator;
 		return this;
 	}
 	
 	public long getTimestamp() {
 		if(pubDate == null) {
			return 0;
 		}
 		
 		return new RssDateTime(pubDate).getTimestamp();
 	}
 }
