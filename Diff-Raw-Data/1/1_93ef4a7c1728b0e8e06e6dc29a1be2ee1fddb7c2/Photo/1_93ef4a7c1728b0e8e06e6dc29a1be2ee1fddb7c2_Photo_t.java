 package com.wedlum.styleprofile.domain.photo;
 
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.wedlum.styleprofile.domain.DomainObject;
 
 public class Photo implements DomainObject {
 
 	private static final long serialVersionUID = 1L;
     private static Map<String, ColorSwatchMetadata> parseMetaCache =
             Collections.synchronizedMap(new LinkedHashMap<String, ColorSwatchMetadata>());
 
 
     private String id;
 	private String metadata;
     private ColorSwatchMetadata meta;
 
 	public Photo() {
 	}
 
 	public Photo(String id, String metadata) {
 		this.id = id;
 		this.metadata = metadata;
         if (!parseMetaCache.containsKey(this.getMetadata()))
             parseMetaCache.put(this.getMetadata(), ColorSwatchMetadata.fromJson(this.getMetadata()));
         this.meta = parseMetaCache.get(this.getMetadata());
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public String getMetadata() {
 		if (this.metadata == null || this.metadata.isEmpty()) {
 			return	"{" +
 						"\"id\":" +
 							"\"" + this.id + "\"," +
 						"\"metadata\":" +
 							"\"Photo:\\n" +
 							"   Description:\\n" +
 							"   Photographer:\\n" +
 							"       Drue Carr\\n" +
 							"   Tags:\\n" +
 							"       Colors:\\n" +
 							"           - code\\n\"" +
 					"}";
 		}
 
 		return metadata;
 	}
 
 	public List<String> getColors() {
 		return meta.getValue("Colors");
 	}
 
 	public List<String> getFeaturedColors() {
        if (meta == null) return Collections.EMPTY_LIST;
 		return meta.getValue("FeaturedColor");
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
