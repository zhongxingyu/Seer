 package lu.mastorak.luxinfowidget.managers;
 
 import lu.mastorak.luxinfowidget.content.RssData;
 import lu.mastorak.luxinfowidget.content.VdlData;
 import lu.mastorak.luxinfowidget.util.Constants;
 import lu.mastorak.luxinfowidget.util.LocalContent;
 import lu.mastorak.luxinfowidget.util.RemoteContent;
 import lu.mastorak.luxinfowidget.util.AppResources;
 import lu.mastorak.luxinfowidget.widget.R;
 import org.w3c.dom.Document;
 import android.util.Log;
 
 /**
  * A class to manage the vdl information rss feed
  * @author Konstantinos Mastorakis
  *
  */
 public class VdlManager {
 	
 	private LocalContent localContent;
 	
 	/**
 	 * Default constructor for VdlManager
 	 */
 	public VdlManager(){
 		
 		localContent=new LocalContent();
 		
 	}
 
 	
 	/**
 	 * A method that parses the page for a particular news item and retrieves the initial text	  
 	 * @param String url
 	 * @return String the headline of the vdl info
 	 */
 	private String getVdlItemText(String url){
 		
 		String text=null;
 		String page=null;
 		RemoteContent remoteContentManager=new RemoteContent();
 		
 		page=remoteContentManager.getRemoteTextContent(url);
 		
 		if(page!=null){
 			//the content is indicated on the page
 			int start =page.indexOf("<!-- ISI_LISTEN_START-->");
 			int end= page.indexOf("<!-- ISI_LISTEN_STOP-->");
 			if(end!=-1 && start!=-1 && end>start){
 				//get the content part of the page
 				String content=page.substring(start, end);
 				//get the first paragraph
 				int textStart =content.indexOf("<p");
 				int textEnd=content.indexOf("</p>");
 			    
 				if(textEnd!=-1 && textStart!=-1 && textEnd>textStart){
 					text=content.substring(textStart, textEnd);
 					//remove html tags
 					text=text.replaceAll("<[^>]+>", "");
 					//remove html entities
 					text=text.replaceAll("&.*?;","");
 					//format string for display
 					text=text.substring(0, 100);
 					text+="...";
 			    }
 			}
 			
 			
 		}
 		Log.i(Constants.UTIL_LOG, "Vdl text:"+text);
 		return text;
 	}
 	
 	/**
 	 * A method to retrieve vdl data object from the vdl rss feed object
 	 * @param Document doc
 	 * @return VdlData 
 	 */
 	public VdlData getVdlData(Document doc){
 		VdlData vdlData=null;
 		String title=null;
 		String url=null;
 		String text=null;
 		
 		RssManager rssManager=new RssManager();
 		
 		RssData rssData=rssManager.getLatestItemRssData(doc);
 		
 		//create the vdl data object
 		if(rssData!=null && !rssData.getTitle().equals("") && !rssData.getUrl().equals("")){
 			title=rssData.getTitle();
 			url=rssData.getUrl();
 		
			text=getVdlItemText(url);
 			
 			if(text==null){
 				text=new String("");
 			}
 			
 			vdlData=new VdlData(title, url, text);
 		}else{
 			vdlData=new VdlData(AppResources.getStringResource(R.string.not_available), "", "");
 		}
 		
 		//store the vdl data
 		localContent.storeVdlData(vdlData);
 		
 		return vdlData;
 	}
 	
 	
 	
 }
