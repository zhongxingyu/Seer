 package org.bluemagic.config.service.utils;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 public final class TagUtils {
 
     // Private Constructor so that it can't be instantiated.
     private TagUtils() {
     }
 
     public static String reorder(String unorderedTags) {
         // Split the original tags, so we can order them.
         String[] unsortedTags = unorderedTags.split("&");
 
         // Order the single tags.
         List<String> tagsToSort = new ArrayList<String>();
         for (String part : unsortedTags) {
             // Look for the single tags.
             if (part.startsWith("tags=")) {
                 int index = part.indexOf("=");
                 String tagsString = part.substring(index + 1);                
                 String reorderedSingleTags = reorderSingleTags(tagsString);
 
		// "tags=" readded after parsing it out
                tagsToSort.add("tags=" + reorderedSingleTags);
                 // Not a single tag, so add it to the tags to sort.

		
             } else {
                 tagsToSort.add(part);
             }
         }
 
         // Order the tags.
         TreeSet<String> sortedTags = new TreeSet<String>(tagsToSort);
         StringBuilder normalizedTags = new StringBuilder();
 
         while (!sortedTags.isEmpty()) {
             normalizedTags.append(sortedTags.pollFirst());
 
             if (!sortedTags.isEmpty()) {
                 normalizedTags.append("&");
             }
         }
 
         return normalizedTags.toString();
     }
     
     public static Map<String, String> parseTags(URI key) {
     	
     	Map<String, String> results = new HashMap<String, String>();
     	
     	// GET ALL THE TAGS
     	String query = key.getQuery();
     	
     	if (query != null) {
     		
     		// SPLIT INTO KEY=VALUE PAIRS
     		String[] properties = query.split("&");
     		
     		for (String property : properties) {
     			
     			String[] keyValue = property.split("=");
     			
     			// RE-ORDER SINGLE TAGS
     			if (keyValue[0].equals("tags")) {
     				
     				String reorderedTags = reorderSingleTags(keyValue[1]);
     				
     				results.put(keyValue[0], reorderedTags);
     			} else {
     				
     				// ADD PROPERTY TO RESULTS MAPPING
     				results.put(keyValue[0], keyValue[1]);
     			}    		
     		}
     	}
     	
     	return results;
     }
     
 
     /**
      * For Example:
      * http://bluemaicsource.org/config/database/user?tags=@sean,security,production
      * By the time we reach this method the tags= have been parsed off of the URI
      * and is the input as "@sean,security,production". Now, this can be placed on by
      * the user or by the server when it read say a certificate.
      **/
     public static String parseUserFromSingleTags(String singleTags) {
 		
 		String result = null;
 		
 		String[] tags = singleTags.split(",");
 		
 		for (String tag : tags) {
 			
 			if (tag.startsWith("@") && tag.length() > 1) {
 				
 				result = tag.substring(1);
 				break;
 			}
 		}
 		
 		return result;
 	}
     
     public static String removeUserFromSingleTags(String singleTags) {
 		
 		String[] tags = singleTags.split(",");
 		int totalTags = tags.length;
 		
 		StringBuilder singleTagsWithoutUser = new StringBuilder();
 		
 		for (int i = 0; i < totalTags; i++) {
 			
 			String tag = tags[i];
 			
 			if (!tag.startsWith("@")) {
 				
 				singleTagsWithoutUser.append(tag);
 				
 				if (i < totalTags - 1) {
 					
 					singleTagsWithoutUser.append(",");
 				}
 			}
 		}
 		
 		return reorderSingleTags(singleTagsWithoutUser.toString());
 	}
 
 	public static String reorderSingleTags(String originalTags) {
 		// TREESET WILL BE USED TO ORDER TAGS
 		TreeSet<String> singleTags = new TreeSet<String>();
 		
 		// SPLIT ALL THE SINGLE TAGS
 		String[] individualTags = originalTags.split(",");
 		
 		// ADD SINGLE TAGS TO TREESET
 		for (String tag : individualTags) {
 			
 			singleTags.add(tag);
 		}
 		
 		// CREATE NEW STRING
 		StringBuilder reorderedSingleTags = new StringBuilder();
 		
 		// RE-ASSEMBLE SINGLE TAGS
 		Iterator<String> iter = singleTags.iterator();
 		
 		while (iter.hasNext()) {
 			
 			reorderedSingleTags.append(iter.next());
 			
 			if (iter.hasNext()) {
 				reorderedSingleTags.append(",");
 			}
 		}
 		
 		String reorderedTags = reorderedSingleTags.toString();
 		return reorderedTags;
 	}
 	
 	public static String reassemble(String base, Map<String, String> tags) {
 
 		String result = base;
 		
 		if (!tags.isEmpty()) {
 			
 			// TreeSet to order the tags.
 			TreeSet<String> orderedTags = new TreeSet<String>();
 			
 			// Put the key, value pairs together.
 			for (Map.Entry<String, String> property : tags.entrySet()) {
 				
 				// Assemble the property
 				String prop = property.getKey() + "=" + property.getValue();
 				
 				orderedTags.add(prop);
 			}
 			
 			// Assemble the query portion.
 			StringBuilder rearrangedTags = new StringBuilder();
 			Iterator<String> iter = orderedTags.iterator();
 			
 			while (iter.hasNext()) {
 				
 				rearrangedTags.append(iter.next());
 				
 				if (iter.hasNext()) {
 					
 					rearrangedTags.append("&");
 				}
 			}
 			
 			result =  base + "?" + rearrangedTags.toString();
 		}
 		
 		return result;
 	}
 	
 	public static String getPropertyWithoutTags(URI key, String baseUrl) {
 		
 		String keyAsString = key.toASCIIString();
 		String keyWithoutBaseUrl;
 		if (baseUrl != null) {
 			keyWithoutBaseUrl = keyAsString.replaceAll(baseUrl, "");
 		}
 		else {
 			keyWithoutBaseUrl = keyAsString;
 		}
 		
 		String result = keyWithoutBaseUrl;
 		
 		if (keyWithoutBaseUrl.contains("?")) {
 			
 			int queryBegins = keyWithoutBaseUrl.indexOf("?");
 			
 			result = keyWithoutBaseUrl.substring(0, queryBegins);
 		}
 		
 		return result;
 	}
 }
