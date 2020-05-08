 package com.maximgalushka.tvserver.model;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import net.jcip.annotations.Immutable;
 
 import java.io.File;
 import java.util.*;
 
 /**
  * <p></p>
  *
  * @author Maxim Galushka
  * @since 12.12.12
  */
 @Immutable
 public final class Content {
 
     private String parent;
     private List<ContentElement> paths;
     private Gson json;
 
     private MediaContentFilter matcher = new MediaContentFilter();
 
     public Content(String parent, File[] files) {
         List<ContentElement> inner = new ArrayList<ContentElement>(files.length);
        for (File file : files) {
            ContentType contentType;
            if (file.isDirectory()) contentType = ContentType.DIRECTORY;
            else {
                if (matcher.accept(null, file.getName())) contentType = ContentType.MEDIA;
                else contentType = ContentType.OTHER;
             }
            inner.add(new ContentElement(file.getPath().replaceAll("\\\\", "/"), contentType));
         }
         Collections.sort(inner);
         this.paths = Collections.unmodifiableList(inner);
        if (parent != null) {
             this.parent = parent.replaceAll("\\\\", "/");
        } else {
             this.parent = "";
         }
         this.json = new Gson();
     }
 
     public String getParent() {
         return parent;
     }
 
     public List<ContentElement> getPaths() {
         return paths;
     }
 
    public String toJson() {
         JsonObject root = new JsonObject();
         root.addProperty("parent", parent);
         root.add("paths", this.json.toJsonTree(paths));
         return this.json.toJson(root);
     }
 }
