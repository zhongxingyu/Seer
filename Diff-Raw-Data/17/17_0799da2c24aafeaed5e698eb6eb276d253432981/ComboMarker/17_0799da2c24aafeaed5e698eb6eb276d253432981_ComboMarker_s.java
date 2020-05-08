 package aigilas.entities;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.management.SpriteType;
 import sps.bridge.DrawDepth;
 import sps.bridge.EntityType;
 import sps.core.Point2;
 import sps.core.Settings;
 import sps.entities.Entity;
 import sps.particles.ParticleEngine;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class ComboMarker extends Entity {
     private final BaseCreature _parent;
     private final int _index;
 
     public ComboMarker(BaseCreature source, Elements elementId, int index) {
         initialize(source.getLocation(), SpriteType.Combo_Marker, EntityType.Combo_Marker, DrawDepth.ComboMarker);
         _graphic.setColor(elementId.Tint);
         _graphic.setAlpha(0);
        ParticleEngine.emit(sps.particles.behaviors.RotateBehavior.getInstance(), this, _graphic.getColor());
         _parent = source;
         _index = index;
     }
 
     private static final List<Point2> __dMults = Arrays.asList(new Point2(-1, 0), new Point2(0, -1), new Point2(1, 0));
 
     @Override
     public void update() {
         float dX = (Settings.get().spriteWidth / 16) * __dMults.get(_index).X;
         float dY = (Settings.get().spriteHeight / 16) * __dMults.get(_index).Y;
         setLocation(new Point2(_parent.getLocation().PosX + dX, _parent.getLocation().PosY + dY));
     }
 
     @Override
     public void draw() {
         _graphic.draw();
     }
 }
