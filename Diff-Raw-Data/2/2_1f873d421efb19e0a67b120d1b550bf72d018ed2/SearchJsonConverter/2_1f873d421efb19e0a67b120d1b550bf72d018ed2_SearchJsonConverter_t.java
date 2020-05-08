 package org.triple_brain.module.search.json;
 
 import org.apache.solr.common.SolrDocument;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import static org.triple_brain.module.common_utils.Uris.decodeURL;
 import static org.triple_brain.module.model.json.graph.VertexJsonFields.*;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class SearchJsonConverter {
     public static String RELATIONS_NAME = "relations_name";
     public static String OWNER_USERNAME = "owner_username";
 
     public static JSONObject documentToJson(SolrDocument document){
         try{
             JSONObject documentAsJson = new JSONObject()
                    .put(URI, decodeURL((String) document.get("uri")))
                     .put(LABEL, document.get("label"))
                     .put(NOTE, document.get("note"))
                     .put(OWNER_USERNAME, document.get("owner_username"));
             documentAsJson.put(
                     RELATIONS_NAME,
                     buildRelationsName(document)
             );
             return documentAsJson;
         }catch(UnsupportedEncodingException | JSONException e){
             throw new RuntimeException(e);
         }
     }
 
     private static JSONArray buildRelationsName(SolrDocument document){
         return new JSONArray(
                 (ArrayList<String>) document.get("relation_name")
         );
     }
 }
