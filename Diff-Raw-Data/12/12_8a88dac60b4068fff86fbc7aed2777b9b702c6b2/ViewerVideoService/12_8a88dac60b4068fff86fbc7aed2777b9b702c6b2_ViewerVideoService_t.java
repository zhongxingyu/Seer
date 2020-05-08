 package net.luszczyk.mdbv.modules.video.service;
 
 import net.luszczyk.mdbv.common.service.ViewerService;
 
 import org.springframework.stereotype.Service;
 
 @Service
 public class ViewerVideoService extends ViewerService {
 
 	{
 		typeList.add("video/ogv");
 	}
 
 	@Override
 	public String getLink(final String fileName) {
 		
 		return "<a href=\"/web/res/domain/" + fileName +
 				 "/fileContent\" rel=\"pixDisplayVideo\">view</a>";
 	}
 }
