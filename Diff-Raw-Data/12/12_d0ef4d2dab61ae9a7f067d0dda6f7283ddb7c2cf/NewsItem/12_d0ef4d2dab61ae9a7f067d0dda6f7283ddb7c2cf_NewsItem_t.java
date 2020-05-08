 package com.markbuikema.juliana32.server.model;
 
 import java.util.ArrayList;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.safety.Whitelist;
 import org.jsoup.select.Elements;
 
 import com.markbuikema.juliana32.server.singletons.NewsItems;
 import com.markbuikema.juliana32.server.tools.Tools;
 
 @XmlRootElement
 public class NewsItem implements Comparable<NewsItem> {
	private static final String NEW_LINE = "NEWLINEREFERENCE1337";
 	@XmlElement
 	private int id;
 	@XmlElement
 	private String title;
 	@XmlElement
 	private String subTitle;
 	@XmlElement
 	private String content;
 	@XmlElement
 	private long createdAt;
 	@XmlElement
 	private String detailUrl;
 	@XmlElement
 	private ArrayList<String> photos;
 
 	public NewsItem() {
 		id = NewsItems.getLatestUnusedId();
 		photos = new ArrayList<>();
 	}
 
 	public NewsItem(String title, String subTitle, String detailUrl, long createdAt) {
 		id = NewsItems.getLatestUnusedId();
 		this.title = title;
 		this.subTitle = subTitle;
 		this.detailUrl = detailUrl;
 		this.createdAt = createdAt;
 		this.content = Tools.getContent(detailUrl);
 
 		
 		
 		photos = new ArrayList<>();
 
 		try {
 			content = content.split("<div id='content-text'>")[1];
 		} catch (ArrayIndexOutOfBoundsException e) {
 		}
 		try {
 			content = content.split("<div id='terug'>")[0];
 		} catch (ArrayIndexOutOfBoundsException e) {
 		}
 		try {
 			content = content.split("<div id='crumbtail'>")[0];
 		} catch (ArrayIndexOutOfBoundsException e) {
 		}
 		
 		content = Jsoup.clean(content, new Whitelist().addTags("img","br","p"));
 
 
 		content = content.replaceAll("<br>", "<br/>");
 		content = content.replaceAll("<BR>", "<br/>");
		content = content.replaceAll("<br/>",NEW_LINE);
		content = content.replaceAll("<br />", NEW_LINE);
		content = content.replaceAll("<p>", NEW_LINE);
		content = content.replaceAll("</p>", NEW_LINE);
		content = content.replaceAll("\n","");
 
 		System.out.println("<!---");
 		System.out.println(content);
 		System.out.println("--->");
 		
 //		content = content.replaceAll("<br>", "\n");
 //		content = content.replaceAll("<BR>", "\n");
 //
 //		content = content.replaceAll("&nbsp;", " ");
 //		content = content.replaceAll("&acirc;??", "'");
 //		content = content.replaceAll(";??", "");
 
 		generatePhotosFromHtml(content);
 
 //		content = Jsoup.parse(content).text();
 //		content = Jsoup.clean(content, Whitelist.basic());
 		
 	}
 
 	public String getDetailUrl() {
 		return detailUrl;
 	}
 
 	private void generatePhotosFromHtml(String html) {
 		Document doc = Jsoup.parse(html);
 		Elements elements = doc.getElementsByTag("img");
 		for (int i = 0; i < elements.size(); i++) {
 			photos.add(elements.get(i).absUrl("src"));
 		}
 		System.out.println("NewsItem " + id + " has " + photos.size() + " photos");
 	}
 
 	@Override
 	public String toString() {
 		return id + ": " + title + "," + subTitle + "," + content + "," + createdAt + "," + detailUrl;
 	}
 
 	public void replace(NewsItem i) {
 		id = i.id;
 		title = i.title;
 		subTitle = i.subTitle;
 		content = i.content;
 		createdAt = i.createdAt;
 		detailUrl = i.detailUrl;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	@Override
 	public int compareTo(NewsItem item) {
 		int difference = (int) (createdAt - item.createdAt);
 
 		return difference == 0 ? 0 : (difference / difference);
 
 	}
 
 }
