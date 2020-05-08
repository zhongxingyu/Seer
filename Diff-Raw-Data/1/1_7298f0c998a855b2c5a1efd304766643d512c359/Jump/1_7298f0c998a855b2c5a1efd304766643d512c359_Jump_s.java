 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.codefuss.actions;
 
 import com.codefuss.entities.Entity;
 import com.codefuss.entities.Creature;
 import com.codefuss.entities.Sprite;
 import java.util.ArrayList;
 import java.util.Collection;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.util.Log;
 
 /**
  *
  * @author Martin Vium <martin.vium@gmail.com>
  */
 public class Jump extends BaseAction {
 
     public Jump(Creature creature) {
         super(creature);
     }
 
     @Override
     public Collection<Entity> invoke() {
         if(creature.getVelocityY() == 0f) {
            creature.setState(Sprite.State.NORMAL);
             creature.setVelocityY(-creature.getSpeedY());
         }
         
         return new ArrayList<Entity>();
     }
 }
