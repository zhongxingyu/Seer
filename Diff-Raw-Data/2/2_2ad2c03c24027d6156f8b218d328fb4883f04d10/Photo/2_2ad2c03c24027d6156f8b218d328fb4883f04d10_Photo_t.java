 package com.wedlum.styleprofile.domain.photo;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import com.wedlum.styleprofile.domain.DomainObject;
 import com.wedlum.styleprofile.util.web.ParseUtils;
 
 public class Photo implements DomainObject {
 
     private static final long serialVersionUID = 1L;
 
 	private String id;
 	private String metadata;
 
     public Photo() {}
 
 	public Photo(String id, String metadata) {
 		this.id = id;
 		this.metadata = metadata;
     }
 
     public String getId() {
 		return id;
 	}
 
 	public String getMetadata() {
         if (this.metadata == null || this.metadata.isEmpty()){
             return
                     "Photo:\n"+
                     "   Description:\n"+
                     "   Photographer:\n"+
                     "       Drue Carr\n"+
                     "   Tags:\n"+
                     "       Colors:\n"+
                     "           - code";
         }
 		return metadata;
 	}
 
     public List<String> getColors() {
     	return getValue("Colors");
     }
 
     public List<String> getFeaturedColors() {
     	return getValue("FeaturedColor");
     }
 
     @SuppressWarnings("unchecked")
 	private List<String> getValue(String tag) {
 		Map<String, Map<String, Map<String, Object>>> model =
                 (Map<String, Map<String, Map<String, Object>>>) ParseUtils.fromYaml(getMetadata()).get("Photo");
 
		if (model == null)
			throw new IllegalStateException("Invalid metadata for: " + this.id);
 		
 		Map<String, Map<String, Object>> tags = model.get("Tags");
 		if (tags == null)
 			throw new IllegalStateException("Invalid metadata for: " + this.id);
 			
 		List<String> values = (List<String>) tags.get(tag);
 		if (values == null)
         	return Collections.emptyList();
         
         return values;
 	}
 
 	@Override
 	public String toString() {
 		return id;
 	}
 
     @Override
     public boolean equals(Object obj) {
         return super.equals(obj);
     }
 
     @Override
     public int hashCode() {
         return super.hashCode();
     }
 
 
 }
