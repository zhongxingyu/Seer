 package org.netvogue.server.webmvc.converters;
 
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.Size;
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.webmvc.common.Constants;
 import org.netvogue.server.webmvc.domain.Editorial;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class EditorialConverter /*implements Converter<org.netvogue.server.neo4japi.domain.Editorial, Editorial>*/ {
 
 	@Autowired
 	private UploadManager uploadManager;
 	
 	public Editorial convert(org.netvogue.server.neo4japi.domain.Editorial source, String username) {
 		Editorial newEditorial = new Editorial();
 		newEditorial.setGalleryid(source.getEditorialid());
 		newEditorial.setGalleryname(source.getEditorialname());
 		
 		String profilepic = source.getProfilePicLink();
 		if(null == profilepic || profilepic.isEmpty()) {
 			newEditorial.setGallerypic(Constants.GALLERY_DefaultPic);
 		} else {
 			String thumblink = uploadManager.getQueryString(profilepic, ImageType.EDITORIAL, 
					Size.EThumb, username);
 			newEditorial.setGallerypic(thumblink);
 		}
 		newEditorial.setGallerydesc(source.getDescription());
 		return newEditorial;
 	}
 
 }
