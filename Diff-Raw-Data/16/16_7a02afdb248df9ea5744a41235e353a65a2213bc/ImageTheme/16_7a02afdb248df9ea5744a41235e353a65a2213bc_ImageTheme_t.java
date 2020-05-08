 package org.atlasapi.media.entity;
 
 public enum ImageTheme {
     
     DARK_OPAQUE("dark_opaque"), 
     LIGHT_OPAQUE("light_opaque"), 
     DARK_TRANSPARENT("dark_transparent"),
    LIGHT_TRANSPARENT("light_transparent");
     
     private final String name;
     
     private ImageTheme(String name) {
         this.name = name;
     }
     
     public String getName() {
         return name;
     }
 }
