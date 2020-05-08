 package kniemkiewicz.jqblocks.ingame.content.bird;
 
 import kniemkiewicz.jqblocks.Configuration;
 import kniemkiewicz.jqblocks.ingame.PointOfView;
 import kniemkiewicz.jqblocks.ingame.Sizes;
 import kniemkiewicz.jqblocks.ingame.content.player.PlayerController;
 import kniemkiewicz.jqblocks.ingame.controller.UpdateQueue;
 import kniemkiewicz.jqblocks.ingame.hud.info.TimingInfo;
 import kniemkiewicz.jqblocks.ingame.inventory.item.Item;
 import kniemkiewicz.jqblocks.ingame.renderer.RenderQueue;
 import kniemkiewicz.jqblocks.ingame.util.WeightedPicker;
import kniemkiewicz.jqblocks.ingame.object.serialization.SerializableRef;
 import kniemkiewicz.jqblocks.ingame.util.Direction;
 import kniemkiewicz.jqblocks.util.Pair;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 /**
  * User: krzysiek
  * Date: 23.09.12
  */
 @Component
 public class SimpleBirdController {
 
   @Autowired
   PlayerController playerController;
 
   @Autowired
   PointOfView pointOfView;
 
   @Autowired
   RenderQueue renderQueue;
 
   @Autowired
   Configuration configuration;
 
   @Autowired
   TimingInfo timingInfo;
 
   List<Bird> birds = new ArrayList<Bird>();
 
   WeightedPicker<Pair<Bird.BirdColor, Direction>> picker;
 
   Random random = new Random();
 
   int MAX_BIRDS;
   float SPAWN_PROBABILITY;
 
   @PostConstruct
   void init() {
     MAX_BIRDS = configuration.getInt("SimpleBirdController.MAX_BIRDS", 2);
     SPAWN_PROBABILITY = configuration.getFloat("SimpleBirdController.SPAWN_PROBABILITY", 0.0005f);
     List<Pair<Bird.BirdColor, Direction>> choices = new ArrayList<Pair<Bird.BirdColor, Direction>>();
     for (Bird.BirdColor color : Bird.BirdColor.values()) {
       for (Direction d : Direction.values()) {
         choices.add(Pair.of(color, d));
       }
     }
     picker = WeightedPicker.createUniformPicker(choices, SPAWN_PROBABILITY);
   }
 
   public void update(int delta) {
     timingInfo.getCounter("birds count").record(birds.size());
     Iterator<Bird> it = birds.iterator();
     while (it.hasNext()) {
       Bird b = it.next();
       b.update(delta);
       if (Math.abs(b.getXYMovement().getX() - playerController.getPlayer().getXYMovement().getX()) >
           pointOfView.getWindowWidth() / 2 + 5 * Sizes.BLOCK) {
         it.remove();
         renderQueue.remove(b);
       }
     }
     if (birds.size() < MAX_BIRDS) {
       Pair<Bird.BirdColor, Direction> pick = picker.pick();
       if (pick == null) return;
       float EMPTY_Y = Sizes.BLOCK * 4;
       float y = - random.nextFloat() * (pointOfView.getWindowHeight() / 2  - EMPTY_Y) - EMPTY_Y + playerController.getPlayer().getXYMovement().getY();
       float x = playerController.getPlayer().getXYMovement().getX() + (pick.getSecond() == Direction.RIGHT ? - 1: 1) *
           (pointOfView.getWindowWidth() / 2 + Sizes.BLOCK * 2);
       Bird b = new Bird(pick.getFirst(), pick.getSecond(), x, y);
       renderQueue.add(b);
       birds.add(b);
     }
   }
 
   // methods below are only for serialization
 
   public Iterator<Bird> iterateAll() {
     return birds.iterator();
   }
 
   public void add(Bird bird) {
     birds.add(bird);
   }
 }
