 package com.chromabits.ugaacm.WarpDrive.UI;
 
import com.chromabits.ugaacm.WarpDrive.render.GlProgram;
 
 public class Text implements TextInterface{
 
     private int fontSize;
 
     public Text(int fontSize) {
         this.fontSize = fontSize;
     }
 
     // Font Size
     public int getFontSize() {
         return this.fontSize;
     }
     public void setFontSize(int fontSize) {
         this.fontSize = fontSize;
     }
 
    public void draw(GlProgram world) {
 
         // TODO Implement this method once Rendering is complete
 
     }
 
 }
