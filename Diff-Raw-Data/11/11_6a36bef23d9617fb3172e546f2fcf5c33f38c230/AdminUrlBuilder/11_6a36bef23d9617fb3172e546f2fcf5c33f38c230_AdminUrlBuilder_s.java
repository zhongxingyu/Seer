 package nz.co.searchwellington.controllers.admin;
 
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
 	
 }
