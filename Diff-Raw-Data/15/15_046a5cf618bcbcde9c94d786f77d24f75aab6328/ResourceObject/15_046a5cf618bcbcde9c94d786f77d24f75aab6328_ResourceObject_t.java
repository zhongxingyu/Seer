 package kniemkiewicz.jqblocks.ingame.object.resource;
 
 import kniemkiewicz.jqblocks.ingame.MovingObjects;
 import kniemkiewicz.jqblocks.ingame.PointOfView;
 import kniemkiewicz.jqblocks.ingame.RenderQueue;
 import kniemkiewicz.jqblocks.ingame.Sizes;
 import kniemkiewicz.jqblocks.ingame.item.Item;
 import kniemkiewicz.jqblocks.ingame.object.MovingPhysicalObject;
 import kniemkiewicz.jqblocks.ingame.object.ObjectRenderer;
 import kniemkiewicz.jqblocks.ingame.object.PickableObject;
 import kniemkiewicz.jqblocks.ingame.object.RenderableObject;
 import kniemkiewicz.jqblocks.util.Assert;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 
 /**
  * User: knie
  * Date: 7/27/12
  */
 public class ResourceObject<T extends Resource> implements RenderableObject<ResourceObject>, PickableObject, MovingPhysicalObject {
 
   private static final long serialVersionUID = 1;
 
   public static final int SIZE = Sizes.BLOCK;
 
   Rectangle rectangle;
 
   T resource;
 
   public ResourceObject(T resource, int x, int y) {
     this.rectangle = new Rectangle(x, y, SIZE, SIZE);
     this.resource = resource;
   }
 
   public void addTo(RenderQueue renderQueue, MovingObjects movingObjects) {
     renderQueue.add(this);
    Assert.executeAndAssert(movingObjects.addPickable(this));
   }
 
   @Override
   public Class<? extends ObjectRenderer<ResourceObject>> getRenderer() {
     // TODO: something more fancy here, with dynamic images based on which resource we have inside.
     return null;
   }
 
   @Override
   public void renderObject(Graphics g, PointOfView pov) {
     g.setColor(Color.cyan);
     float percentage = (float) resource.getAmount() * 1.0f / resource.getMaxPileSize() * 1.0f;
     g.fill(new Rectangle(rectangle.getX(), rectangle.getY() + rectangle.getHeight() * (1.0f - percentage), rectangle.getWidth() * percentage, rectangle.getHeight() * percentage));
   }
 
   @Override
   public Layer getLayer() {
     return Layer.OBJECTS;
   }
 
   @Override
   public Shape getShape() {
     return rectangle;
   }
 
   @Override
   public Item getItem() {
     return new ResourceItem(resource);
   }
 
   @Override
   public void setY(int y) {
     rectangle.setY(y);
   }
 
   public int getResourceAmount() {
     return resource.getAmount();
   }
 
   public boolean isFull() {
     assert resource.getAmount() <= resource.getMaxPileSize();
     return resource.getAmount() == resource.getMaxPileSize();
   }
 
   public int getResourceMaxPileSize() {
     return resource.getMaxPileSize();
   }
 
   public T addResource(T resource) {
     if (!isFull()) {
       int roomLeft = this.resource.getMaxPileSize() - this.resource.getAmount();
       if (roomLeft < resource.getAmount()) {
         resource.transferTo(this.resource, roomLeft);
       } else {
         resource.transferTo(this.resource);
       }
     }
     return resource;
   }
 
   public boolean isSameType(Resource resource) {
     Assert.assertTrue(resource != null);
     return this.resource.getType().equals(resource.getType());
   }
 }
