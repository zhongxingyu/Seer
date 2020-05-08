 package org.netvogue.server.webmvc.converters;
 
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.Size;
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.webmvc.common.Constants;
 import org.netvogue.server.webmvc.domain.Gallery;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class GalleryConverter /*implements Converter<org.netvogue.server.neo4japi.domain.Gallery, Gallery>*/ {
 
 	@Autowired
 	private UploadManager uploadManager;
 	
 	//This is used while returning data from database. So source is database domain and destination is web domain
 	public Gallery convert(org.netvogue.server.neo4japi.domain.Gallery dbGallery, String username) {
 	    Gallery gallery = new Gallery();
 	    gallery.setGalleryid(dbGallery.getGalleryid());
 	    gallery.setGalleryname(dbGallery.getGalleryname());
 	    
 	    String profilepic = dbGallery.getProfilePicLink();
 	    if(null == profilepic || profilepic.isEmpty()) {
 			gallery.setGallerypic(Constants.GALLERY_DefaultPic);
 		} else {
 			//String thumblink = uploadManager.getQueryString(dbGallery.getProfilePicLink(), ImageType.GALLERY, Size.GThumb);
 			String thumblink = uploadManager.getQueryString(profilepic, ImageType.GALLERY, 
					Size.GAdd, username);
 			gallery.setGallerypic(thumblink);
 		}
 	    gallery.setGallerydesc(dbGallery.getDescription());
 		return gallery;
 	}
 
 }
