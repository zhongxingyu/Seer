 package niktgar.tod.level;
 
 import lombok.Data;
 import lombok.experimental.Accessors;
 import niktgar.tod.entity.PlayerEntity;
 import niktgar.tod.level.block.BlockLayer;
 import niktgar.tod.level.block.BlockMap;
 
 @Accessors(fluent = true)
 @Data
 public class Level {
 
     private final BlockMap blockMap;
     private final BlockLayer collisionBlocks;
     private final PlayerEntity player;
 
     private final Camera camera;
 
     public Level(final BlockMap map, final BlockLayer collisionBlocks, final PlayerEntity player) {
         this.blockMap = map;
         this.collisionBlocks = collisionBlocks;
         this.player = player;
         this.camera = new Camera(player);
        camera.rightBoundary(blockMap.rows() * BlockMap.BLOCK_SIZE - 800);
     }
 
     public void handleCollisions() {
         if (player.position().x() < 0) {
             player.position().x(0);
        } else if (player.position().x() > blockMap.rows() * BlockMap.BLOCK_SIZE - player.animation().width()) {
            player.position().x(blockMap.rows() * BlockMap.BLOCK_SIZE - player.animation().width());
         }
         collisionBlocks.checkForCollisions(player);
     }
 
     public void update(long delta) {
         player.update(delta);
         if (player.position().y > 20000) {
             player.position().y = 200;
             player.position().x = 150;
             player.velocity().y = 0;
         }
     }
 
     public void draw() {
         camera.pull();
         blockMap.draw(-camera.x(), 0);
         player.draw(-camera.x(), 0);
     }
 
 }
