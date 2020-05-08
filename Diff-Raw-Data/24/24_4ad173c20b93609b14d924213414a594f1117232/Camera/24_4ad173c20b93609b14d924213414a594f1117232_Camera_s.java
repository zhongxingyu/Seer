 package niktgar.tod.level;
 
 import lombok.Data;
 import lombok.experimental.Accessors;
 import niktgar.tod.entity.Entity;
 
 @Accessors(fluent = true)
 @Data
 public class Camera {
 
     private int x;
     private int min;
     private int max;
 
     private Entity anchor;
 
     public Camera(Entity anchor) {
         this.anchor = anchor;
         x = 0;
        min = 200;
        max = 600;
     }
 
     public void pull() {
         int offset = anchor.position().snappedX() - x;
         if (offset < min) {
            x -= offset;
         } else if (offset > max) {
            x += offset;
         }
     }
 }
