 package com.wedlum.styleprofile.domain.photo;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.wedlum.styleprofile.util.web.ParseUtils;
 
 import com.wedlum.styleprofile.util.observer.Observer;
 
 @Named(value = "tagAutocomplete")
 public class TagAutocompleteImpl implements TagAutocomplete {
 
     private LinkedHashMap<String,String> storage = new LinkedHashMap<String, String>();
 
     @Inject
     public TagAutocompleteImpl(final PhotoSource photoSource) {
         setPhotoSource(photoSource);
     }
 
 	private void setPhotoSource(final PhotoSource photoSource) {
 		photoSource.addObserver(new Observer<String>() {
             public void update(String id) {
                 storage.put(id, photoSource.getMetadata(id));
             }
 
             @Override
             public void remove(String info) {}
         });
 	}
 
     @Override
 	@SuppressWarnings("unchecked")
 	public Map<String, Set<String>> getSuggestions() {
         LinkedHashMap<String, Set<String>> result = new LinkedHashMap<String, Set<String>>();
 
         for(String source : storage.values()) {
             Map<String, Object> model = ParseUtils.fromYaml(source);
             if (model == null) continue;
            Object metadata = ParseUtils.fromYaml(model.get("metadata").toString());
            if (!(metadata instanceof Map))
            	continue;

            model = (Map<String, Object>) metadata;

             traverse("Root", model, result);
         }
 
         return result;
     }
 
     @SuppressWarnings("unchecked")
 	private void traverse(String parent, Map<String, Object> model, LinkedHashMap<String, Set<String>> result) {
         Set<String> children = new LinkedHashSet<String>();
 
         for(String key : model.keySet()) {
 
         	String child = parent + "/" + key;
 
             children.add(key);
             Object value = model.get(key);
 
             if (value instanceof LinkedHashMap){
                 traverse(child, (Map<String, Object>) value,result);
             } else if (value instanceof String) {
                 addChild(result, child, value);
             } else if (value instanceof ArrayList) {
                 ArrayList<String> list = (ArrayList<String>) value;
                 for(Object each : list)
 					addChild(result, child, each);
 
             } else if (value == null) {
             	
             } else {
             	throw new IllegalStateException("Unexpected value " + value + " of class " + value.getClass() );            	
             }
         }
 
         for(String child : children)
         	addChild(result, parent, child);
     }
 
 	private void addChild(LinkedHashMap<String, Set<String>> map, String key, Object child) {
 		if (!map.containsKey(key))
 			map.put(key, new LinkedHashSet<String>());
 
 		map.get(key).add((String) child);
 	}
 
 }
