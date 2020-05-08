 package kniemkiewicz.jqblocks.ingame.object.player;
 
 import kniemkiewicz.jqblocks.ingame.*;
 import kniemkiewicz.jqblocks.ingame.block.SolidBlocks;
 import kniemkiewicz.jqblocks.ingame.controller.HitResolver;
 import kniemkiewicz.jqblocks.ingame.controller.KeyboardUtils;
 import kniemkiewicz.jqblocks.ingame.item.ItemInventory;
 import kniemkiewicz.jqblocks.ingame.item.Item;
 import kniemkiewicz.jqblocks.ingame.level.VillageGenerator;
 import kniemkiewicz.jqblocks.ingame.object.PickableObject;
 import kniemkiewicz.jqblocks.ingame.object.PickableObjectType;
 import kniemkiewicz.jqblocks.ingame.resource.inventory.ResourceInventory;
 import kniemkiewicz.jqblocks.ingame.resource.item.ResourceItem;
 import kniemkiewicz.jqblocks.util.Assert;
 import kniemkiewicz.jqblocks.util.GeometryUtils;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 /**
  * User: krzysiek
  * Date: 08.07.12
  */
 @Component
 public class PlayerController implements InputListener {
 
   Player player;
 
   @Autowired
   SolidBlocks blocks;
 
   @Autowired
   PointOfView pointOfView;
 
   @Autowired
   MovingObjects movingObjects;
 
   @Autowired
   World objectKiller;
 
   @Autowired
   ItemInventory inventory;
 
   @Autowired
   ResourceInventory resourceInventory;
 
   @Autowired
   RenderQueue renderQueue;
 
   @Autowired
   VillageGenerator villageGenerator;
 
   @Autowired
   CollisionController collisionController;
 
   /**
    * This is manually invoked by Game to make sure that level is created before.
    */
   public void initPlayer() {
     player = new Player();
     player.addTo(renderQueue, movingObjects);
     player.getXMovement().setPos(VillageGenerator.STARTING_X);
     player.getYMovement().setPos(villageGenerator.getStartingY() - Player.HEIGHT - 2);
     player.updateShape();
   }
 
   public void listen(Input input, int delta) {
     /*
     if (KeyboardUtils.isDownPressed(input)) {
       player.setY(player.getY() + delta);
     }
     */
     Rectangle belowPlayer = new Rectangle(player.getShape().getMinX() + 1, player.getShape().getMaxY() + 2,
         player.getShape().getWidth() - 4, 0);
     boolean flying = !blocks.getBlocks().collidesWithNonEmpty(belowPlayer);
     if (KeyboardUtils.isUpPressed(input) && !flying) {
       player.getYMovement().setSpeed(-Player.JUMP_SPEED);
     } else {
       player.getYMovement().setAcceleration(Sizes.G);
     }
     if (KeyboardUtils.isLeftPressed(input)) {
       player.getXMovement().setAcceleration(-Player.X_ACCELERATION);
     }
     if (KeyboardUtils.isRightPressed(input)) {
       player.getXMovement().setAcceleration(Player.X_ACCELERATION);
     }
 
     float x = player.getXMovement().getPos();
     float y = player.getYMovement().getPos();
     player.update(delta);
     //having a copy of players shape make debugging easier.
     Rectangle playerShape = GeometryUtils.getNewBoundingRectangle(player.getShape());
     List<Rectangle> collidingRectangles = blocks.getBlocks().getIntersectingRectangles(playerShape);
 
     for (Rectangle r : collidingRectangles) {
       if(!GeometryUtils.intersects(r, playerShape)) {
         blocks.getBlocks().getIntersectingRectangles(playerShape);
         assert false;
       }
     }
     for (Rectangle r : collidingRectangles) {
       HitResolver.resolve(player, player.getXMovement().getPos() - x, player.getYMovement().getPos() - y, r);
       player.updateShape();
     }
     for (PickableObject pickableObject : collisionController.<PickableObject>fullSearch(MovingObjects.PICKABLE, player.getShape())) {
       Item item = pickableObject.getItem();
       PickableObjectType poType = pickableObject.getType();
       if (PickableObjectType.ACTION.equals(poType)) {
         if (inventory.add(item)) {
           objectKiller.killMovingObject(pickableObject);
         }
       } else if (PickableObjectType.RESOURCE.equals(poType)) {
         if (KeyboardUtils.isDownPressed(input)) {
           if (resourceInventory.add((ResourceItem) item)) {
             objectKiller.killMovingObject(pickableObject);
           }
         }
       }
     }
     if (Assert.ASSERT_ENABLED) {
       for (Rectangle r : collidingRectangles) {
         assert !GeometryUtils.intersects(r, player.getShape());
       }
     }
     assert blocks.getBlocks().getIntersectingRectangles(player.getShape()).size() == 0;
     // Do not change this without a good reason. May lead to screen flickering in rare conditions.
     int centerX = (int)player.getXMovement().getPos() + Player.WIDTH / 2;
     int centerY = (int)player.getYMovement().getPos() + Player.HEIGHT / 2;
     pointOfView.setCenter(centerX, centerY);
   }
 
   public Player getPlayer() {
     return player;
   }
 
   public void setPlayer(Player player) {
     this.player = player;
   }
 }
