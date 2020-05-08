 package de.kaffeeshare.server.plugins;
 
 import java.io.IOException;
 import java.net.URL;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import de.kaffeeshare.server.datastore.Item;
 import de.kaffeeshare.server.exception.InputErrorException;
 
 public class Garfield extends BasePlugin {
 
 	@Override
 	public boolean match(String url) {
 		return url.startsWith("http://feedproxy.google.com/~r/uclick/garfield/")
				|| url.contains("http://www.gocomics.com/garfield");
 	}
 
 	@Override
 	public void creatItem(URL url, Item item) throws IOException {
 		log.info("Running Garfield plugin!");
 		
 		Document doc;
 		doc = Jsoup.parse(url, 10000);
 		String caption = null;
 		
 		try {
 			caption = getProperty(doc, "og:title");
 		} catch (Exception e) {}
 		
 		if (caption == null) {
 			try {
 				caption = doc.select("title").first().text();
 			} catch (Exception e) {
 				caption = "";
 			}
 		}
 		
 		log.info("caption: " + caption);
 
 		String description = "";
 
 		String imageUrl = null;
 		try {
 			Elements elements = doc.getElementsByClass("strip");
 			description  = "<img src=\"";
 			description += elements.get(0).attr("src");
 			description += "\" />";
 		} catch (Exception e) {
 			throw new InputErrorException();
 		}
 		log.info("description: " + description);
 		
 		String urlString = null;
 		try {
 			urlString = getProperty(doc, "og:url");
 		} catch (Exception e) {
 			urlString = url.toString();
 		}
 		item.setCaption(caption);
 		item.setUrl(urlString);
 		item.setDescription(description);
 		item.setImageUrl(imageUrl);
 	}
 }
