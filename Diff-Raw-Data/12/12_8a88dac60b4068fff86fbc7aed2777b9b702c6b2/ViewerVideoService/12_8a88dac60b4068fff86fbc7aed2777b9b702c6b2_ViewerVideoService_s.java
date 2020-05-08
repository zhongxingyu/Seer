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

/*		return "<video id=\"vid1\" width=\"480\" height=\"267\" "
				+ " poster=\"http://cdn.kaltura.org/apis/html5lib/kplayer-examples/media/bbb480.jpg\""
				+ "durationHint=\"33\"> "
				+ "<source src=\"liwko.ogv\" type=\"video/ogg\" />" +

				"</video>";*/

		/*
		 * return "<a href=\"domain/" + fileName +
		 * "/fileContent\" rel=\"pixDisplay\">view</a>";
		 */
 	}
 }
