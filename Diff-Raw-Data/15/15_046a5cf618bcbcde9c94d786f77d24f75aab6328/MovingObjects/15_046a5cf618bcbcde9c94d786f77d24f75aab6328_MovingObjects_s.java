 package kniemkiewicz.jqblocks.ingame;
 
 import kniemkiewicz.jqblocks.ingame.object.PhysicalObject;
 import kniemkiewicz.jqblocks.ingame.object.player.Player;
 import kniemkiewicz.jqblocks.ingame.object.RenderableObject;
 import kniemkiewicz.jqblocks.ingame.util.LinearIntersectionIterator;
 import kniemkiewicz.jqblocks.util.Assert;
 import kniemkiewicz.jqblocks.util.IterableIterator;
 import org.newdawn.slick.geom.Shape;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * User: krzysiek
  * Date: 11.07.12
  */
 @Component
 public class MovingObjects {
 
   @Autowired
   RenderQueue queue;
 
   HashSet<PhysicalObject> objects = new HashSet<PhysicalObject>();
 
   // TODO: It should be known which object can collide with which.
   public boolean addCollidingWithPlayer(PhysicalObject object) {
     Iterator<PhysicalObject> it = this.intersects(object.getShape());
     if (it.hasNext()) {
       if (!(it.next() instanceof Player)) return false;
     }
     objects.add(object);
     return true;
   }
 
   public boolean add(PhysicalObject object) {
     assert Assert.validateSerializable(object);
     if (this.intersects(object.getShape()).hasNext()) return false;
     objects.add(object);
     return true;
   }
 
 
 
   public IterableIterator<PhysicalObject> intersects(Shape shape) {
     return new LinearIntersectionIterator<PhysicalObject>(objects.iterator(), shape);
   }
 
   public void remove(PhysicalObject po) {
     objects.remove(po);
   }
 
   public Iterator<PhysicalObject> iterateAll() {
     return objects.iterator();
   }
 }
