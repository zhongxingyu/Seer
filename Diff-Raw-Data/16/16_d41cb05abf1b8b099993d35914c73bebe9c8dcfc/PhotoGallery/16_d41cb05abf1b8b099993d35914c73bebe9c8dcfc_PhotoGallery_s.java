 package com.wedlum.styleprofile.domain.photo;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.wedlum.styleprofile.util.observer.Observer;
 import com.wedlum.styleprofile.util.web.ParseUtils;
 
 @Named(value = "photoGallery")
 public class PhotoGallery  {
 
 	private Set<String> untagged = new LinkedHashSet<String>();
 	private PhotoSource source;
 
 	@Inject
     public PhotoGallery(PhotoSource source) {
         setPhotoSource(source);
 	}
 
 	private void setPhotoSource(PhotoSource source) {
 		this.source = source;
         source.addObserver(new Observer<String>() {
             public void update(String photoId) {
                 untagged.add(photoId);
             }
 
             @Override
             public void remove(String photoId) {
                 untagged.remove(photoId);
             }
         });
 	}
 
 	public Set<String> untagged() {
 		return untagged;
 	}
 
 	public Photo photo(String id) {
 		for (String photoId : untagged)
 			if (photoId.equals(id))
 				return new Photo(id, "");
 		
 		throw new IllegalStateException("Photo not found: " + id);
 				
 	}
 
 	public String loadDetail(String id) {
         String stored = source.getMetadata(id);
         if (stored == "")
             return ParseUtils.toJson(new Photo(id, ""));
        return stored;
     }
 
 	public void storeDetail(String id, String $metadata) {
         ColorSwatchMetadata metadata = ColorSwatchMetadata.fromJson($metadata);
 		source.setMetadata(id, metadata);
     }
 
     public void delete(String id) {
         source.delete(id);
     }
 }
