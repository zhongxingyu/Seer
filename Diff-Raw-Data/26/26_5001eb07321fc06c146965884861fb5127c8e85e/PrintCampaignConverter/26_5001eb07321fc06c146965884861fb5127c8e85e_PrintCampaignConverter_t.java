 package org.netvogue.server.webmvc.converters;
 
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.Size;
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.webmvc.common.Constants;
 import org.netvogue.server.webmvc.domain.PrintCampaign;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class PrintCampaignConverter /*implements Converter<org.netvogue.server.neo4japi.domain.PrintCampaign, PrintCampaign>*/ {
 
 	@Autowired
 	private UploadManager uploadManager;
 	
 	public PrintCampaign convert(org.netvogue.server.neo4japi.domain.PrintCampaign source, String username) {
 		PrintCampaign newCampaign = new PrintCampaign();
 		newCampaign.setGalleryid(source.getPrintcampaignid());
 		newCampaign.setGalleryname(source.getPrintcampaignname());
 		String profilepic = source.getProfilePicLink();
 		if(null == profilepic || profilepic.isEmpty()) {
 			newCampaign.setGallerypic(Constants.GALLERY_DefaultPic);
 		} else {
 			String thumblink = uploadManager.getQueryString(profilepic, ImageType.PRINT_CAMPAIGN, 
					Size.PCAdd, username);
 			newCampaign.setGallerypic(thumblink);
 		}
 		newCampaign.setGallerydesc(source.getDescription());
 		return newCampaign;
 	}
 
 }
