 package com.group7.dragonwars.engine;
 
 public abstract class DrawableMapObject {
 
     public String name;
     public String spriteLocation;
     public String spriteDir;
     public String spritePack;
 
     public DrawableMapObject(final String name, final String spriteLocation,
                              final String spriteDir, final String spritePack) {
         this.name = name;
         this.spriteLocation = spriteLocation;
         this.spriteDir = spriteDir;
         this.spritePack = spritePack;
     }
     public String getName() {
         return name;
     }
 
     public String getSpriteLocation() {
         return spriteLocation;
     }
 
    public String getSpriteDir() {
         return spriteDir;
     }
 
     public String getSpritePack() {
         return spritePack;
     }
 
 }
