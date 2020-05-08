 package com.fisherevans.lrk.states.adventure.combat.skills;
 
 import com.fisherevans.lrk.managers.SoundManager;
 import com.fisherevans.lrk.states.adventure.combat.Skill;
 import com.fisherevans.lrk.states.adventure.combat.effects.HealthCone;
 import com.fisherevans.lrk.states.adventure.entities.AdventureEntity;
 
 /**
  * Created with IntelliJ IDEA.
  * User: immortal
  * Date: 8/6/13
  * Time: 3:29 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Slash extends Skill
 {
    private AdventureEntity _owner;
     private AdventureEntity.Team[] _effects;
 
    public Slash(AdventureEntity owner, AdventureEntity.Team... effects)
     {
         super("Slash", "Slash with your weapon.", 1f);
         _effects = effects;
     }
 
     @Override
     public boolean execute(AdventureEntity owner)
     {
         SoundManager.play("hit");
 
         owner.getState().getEntityEffectQueue().addEntityEffect(
                 new HealthCone(
                         (float)Math.toRadians(owner.getDegrees()),
                         (float) Math.toRadians(90), 1f, owner.getBody().getPosition().clone(),
                         -5, _effects
                 )
         );
         return true;
     }
 }
