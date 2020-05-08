 package nz.co.searchwellington.controllers.admin;
 
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

 import nz.co.searchwellington.model.FeedNewsitem;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.SiteInformation;
 
 public class AdminUrlBuilder {
 
 	private SiteInformation siteInformation;
 
 		
 	public AdminUrlBuilder(SiteInformation siteInformation) {		
 		this.siteInformation = siteInformation;
 	}
 
 	public String getResourceEditUrl(Resource resource) {
 		return siteInformation.getUrl() + "/edit/edit?resource=" + resource.getId();
 	}
 	
 	public String getResourceDeleteUrl(Resource resource) {
 		return siteInformation.getUrl() + "/edit/delete?resource=" + resource.getId();
 	}
 	
 	public String getResourceCheckUrl(Resource resource) {
 		return siteInformation.getUrl() + "/admin/linkchecker/add?resource=" + resource.getId();
 	}
 	
 	public String getFeedNewsitemAcceptUrl(FeedNewsitem feednewsitem) {
 		return siteInformation.getUrl() + "/edit/accept?feed=" + feednewsitem.getFeed().getId() + "&item=" + feednewsitem.getItemNumber();
 	}
 	
	public String getFeedNewsitemSuppressUrl(FeedNewsitem feednewsitem) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/supress/supress?url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");
	}
	
	public String getFeedNewsitemUnsuppressUrl(FeedNewsitem feednewsitem) throws UnsupportedEncodingException {
		return siteInformation.getUrl() + "/supress/unsupress?url=" + URLEncoder.encode(feednewsitem.getUrl(), "UTF-8");
	}
 }
