 package niktgar.tod.level.block;
 
 import java.util.ArrayList;
 
 import niktgar.tod.block.Block;
 import niktgar.tod.collision.BoundingBox;
 import niktgar.tod.collision.BoundingBoxQuad;
 import niktgar.tod.entity.Entity;
 import niktgar.tod.geometry.Vector;
 
 public class BlockLayer extends ArrayList<Block> {
 
     private static final long serialVersionUID = 1L;
 
     public BlockLayer() {
         super();
     }
 
     public void checkForCollisions(final Entity entity) {
         System.out.println("CHECK");
         boolean floating = true;
         for (final Block block : this) {
             final BoundingBox blockBox = block.bound();
             final BoundingBox entityBox = entity.bound();
             if (blockBox.isColliding(entityBox)) {
                 final BoundingBoxQuad quad = new BoundingBoxQuad(entityBox);
                 boolean collideLeft = blockBox.isColliding(quad.left());
                 boolean collideRight = blockBox.isColliding(quad.right());
 
                 if (blockBox.isColliding(quad.bottom())) {
                     System.err.println("BOTTOM");
                     entity.collidedBottom(block);
                 } else if (blockBox.isColliding(quad.top())) {
                     System.err.println("TOP");
                     entity.collidedTop(block);
                 } else if (blockBox.isColliding(quad.left())) {
                     System.err.println("LEFT");
                     if (blockBox.isInside(new Vector(entity.bound().ulX(), entity.bound().ulY() + 1))) {
                        Vector oldPosition = new Vector(0, 0);
                        oldPosition.x = entity.position().x;
                        oldPosition.y = entity.position().y;
                         entity.collidedTop(block);
                         if (blockBox.isColliding((new BoundingBoxQuad(entity.bound())).left())) {
                             entity.collidedLeft(block);
                         } else {
                             entity.position(oldPosition);
                         }
                     } else {
                         entity.collidedLeft(block);
                     }
                 } else if (blockBox.isColliding(quad.right())) {
                     System.err.println("RIGHT");
                    if (blockBox.isInside(new Vector(entity.bound().lrX(), entity.bound().ulY() + 1))) {
                        Vector oldPosition = new Vector(0, 0);
                        oldPosition.x = entity.position().x;
                        oldPosition.y = entity.position().y;
                         entity.collidedTop(block);
                         if (blockBox.isColliding((new BoundingBoxQuad(entity.bound())).right())) {
                             entity.collidedRight(block);
                         } else {
                             entity.position(oldPosition);
                         }
                     } else {
                         entity.collidedRight(block);
                     }
                 }
             }
             if (blockBox.isInside(new Vector((entity.bound().ulX() + entity.bound().lrX()) / 2, entity.bound().lrY() + 1))) {
                 System.err.println("ON GROUND");
                 block.updateMovementState(entity);
                 floating = false;
             }
         }
         if (floating) {
             System.err.println("FLOATING");
             entity.alertFloating();
         }
     }
 }
