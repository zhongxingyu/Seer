 package client;
 
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 
 
 public class YoutubeLink {
 	private String title = "";
 	private String duration = "";
 	
 	YoutubeLink(String url)
 	{
 		try {
 			
 			Document doc = Jsoup.connect(url).get();
 			title = doc.title();
 			duration = doc.select("meta[itemprop=duration]").attr("content");
			duration = duration.replace("PT", "(");
			duration = duration.replace('M',':');
			duration = duration.replace('S',')');
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	YoutubeLink()
 	{
 		
 	}
 	
 	public String getTitle()
 	{
 		return title;
 	}
 	
 	public String getDuration()
 	{
		return duration;
 	}
 }
