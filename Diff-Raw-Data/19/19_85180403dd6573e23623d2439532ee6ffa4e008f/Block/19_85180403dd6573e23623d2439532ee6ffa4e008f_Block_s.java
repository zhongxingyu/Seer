 package com.macbury.unamed.level;
 
 public class Block {
   public static int gid = 0;
   int id = 0;
   
   public boolean solid       = false;
   public boolean visited     = false;
   public boolean visible     = false;
   public int     lightPower  = 255;
   
   public Block() {
     this.id = Block.gid++;
   }
   
   public void markAsVisible() {
     this.visited = true;
     this.visible = true;
   }
 
   public boolean isVisible() {
     return this.visible;
   }
 
   public void markAsInvisible() {
     this.visible = false;
     this.lightPower = 0;
   }
 
   public boolean haveBeenVisited() {
     return this.visited;
   }
 }
