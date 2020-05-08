 package client;
 
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 
 
 public class YoutubeLink {
 	private String title = "";
 	private String duration = "";
	private int durationMin = 0, durationSec = 0; 
 	
 	YoutubeLink(String url)
 	{
 		try {
 			
 			Document doc = Jsoup.connect(url).get();
 			title = doc.title();
 			duration = doc.select("meta[itemprop=duration]").attr("content");
			duration = duration.replace("PT", "");
			duration = duration.replace("S","");
			
			durationMin = Integer.parseInt(duration.split("M")[0]);
			durationSec = Integer.parseInt(duration.split("M")[1]);	
			
 			
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
		if(durationMin < 60)
			return "(" + durationMin + ":" + durationSec + ")";
		else
			return "(" + durationMin/60 + ":" + durationMin%60 + ":" + durationSec + ")";
 	}
 }
