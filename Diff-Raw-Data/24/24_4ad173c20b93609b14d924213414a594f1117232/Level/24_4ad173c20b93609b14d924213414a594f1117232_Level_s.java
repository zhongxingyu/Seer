 package niktgar.tod.level;
 
 import lombok.Data;
 import lombok.experimental.Accessors;
 import niktgar.tod.block.BlockMap;
 import niktgar.tod.entity.Entity;
 
 @Accessors(fluent = true)
 @Data
 public class Level {
 
    private Entity anchor;
     private Camera camera;
 
     private BlockMap blockMap;
 
     public Level(Entity anchor, BlockMap map) {
        this.anchor = anchor;
         this.blockMap = map;
         this.camera = new Camera(anchor);
     }
 
     public void draw() {
         camera.pull();
         blockMap.draw(-camera.x(), 0);
     }
 }
