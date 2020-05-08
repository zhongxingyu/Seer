 package com.scrye.badgertunes;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import java.util.ArrayList;
 
 public class Node {
     public String name;
     public ArrayList<Node> children;
     public Node parent;
     
     public String toString() {
     	return name;
     }
 
     static public Node read(JSONObject json) {
         Node node = new Node();
         JSONArray json_children;
         try {
            node.name = json.getString("Name");        
             json_children = json.getJSONArray("Children");
         } catch( JSONException ex ) {
            return null;
         }
         if( json_children != null && json_children.length() > 0 ) {
             node.children = new ArrayList<Node>(json_children.length());
             for( int i = 0; i < json_children.length(); i++ ) {
                 try {
                     JSONObject json_child = json_children.getJSONObject( i );
                     Node child = read( json_child );
                     if( child != null ) {
                         node.children.add( child );
                         child.parent = node;
                     }
                 } catch( JSONException ex ) {}
             }
         }
         return node;
     }
 }
