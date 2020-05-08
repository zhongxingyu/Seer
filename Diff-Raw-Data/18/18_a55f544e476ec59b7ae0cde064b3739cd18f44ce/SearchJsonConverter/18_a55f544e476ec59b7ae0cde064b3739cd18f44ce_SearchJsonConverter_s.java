 package org.triple_brain.module.search.json;
 
 import org.apache.solr.common.SolrDocument;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.triple_brain.module.common_utils.Uris.decodeURL;
 import static org.triple_brain.module.model.json.graph.VertexJson.*;
 
 /*
 * Copyright Mozilla Public License 1.1
 */
 public class SearchJsonConverter {
     public static String RELATIONS_NAME = "relations_name";
     public static String IDENTIFICATIONS = "identifications";
     public static String OWNER_USERNAME = "owner_username";
     public static String SOURCE_VERTEX_URI = "source_vertex_uri";
     public static String DESTINATION_VERTEX_URI = "destination_vertex_uri";
 
     public static JSONObject documentToJson(SolrDocument document){
         try{
             JSONObject documentAsJson = new JSONObject()
                     .put(URI, decodeURL((String) document.get("uri")))
                     .put(LABEL, document.get("label"))
                     .put(COMMENT, document.get("comment"))
                    .put(OWNER_USERNAME, document.get("owner_username"))
                    .put(SOURCE_VERTEX_URI, decodeURL((String)document.get("source_vertex_uri")))
                    .put(DESTINATION_VERTEX_URI, decodeURL((String)document.get("destination_vertex_uri")));

             documentAsJson.put(
                     RELATIONS_NAME,
                     buildRelationsName(document)
             );
             documentAsJson.put(
                     IDENTIFICATIONS,
                     buildIdentifications(document)
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
     private static JSONArray buildIdentifications(SolrDocument document){
         JSONArray identificationsAsJson = new JSONArray();
         if(!document.containsKey("identification")){
             return identificationsAsJson;
         }
         List<String> identifications = (ArrayList<String>) document.get(
                 "identification"
         );
         for(String identification : identifications){
             try{
                 identificationsAsJson.put(
                         decodeURL(identification)
                 );
             }catch(UnsupportedEncodingException e){
                 throw new RuntimeException(e);
             }
         }
         return identificationsAsJson;
     }
 }
