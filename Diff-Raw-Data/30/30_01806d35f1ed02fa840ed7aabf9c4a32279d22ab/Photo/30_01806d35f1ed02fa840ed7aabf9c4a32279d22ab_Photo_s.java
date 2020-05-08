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
 
    @SuppressWarnings("unchecked")
         public List<String> getColors() {
        Map<String, Map<String, Map<String, Object>>> model =
                (Map<String, Map<String, Map<String, Object>>>) ParseUtils.fromYaml(getMetadata()).get("Photo");

        return (List<String>) model
                .get("Tags")
                .get("Colors");
 
     }
 
     @SuppressWarnings("unchecked")
    public List<String> getFeaturedColors() {
        Map<String, Map<String, Map<String, Object>>> model =
                 (Map<String, Map<String, Map<String, Object>>>) ParseUtils.fromYaml(getMetadata()).get("Photo");
 
        List<String> featuredColors = (List<String>) model.get("Tags").get("FeaturedColor");
        if (featuredColors == null)
         	return Collections.emptyList();
         
        return featuredColors;
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
