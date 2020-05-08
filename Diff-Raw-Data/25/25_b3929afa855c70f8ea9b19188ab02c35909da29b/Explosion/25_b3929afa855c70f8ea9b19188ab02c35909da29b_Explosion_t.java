 package org.blink.game.model.spell.explosion;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.effect.ParticleEmitter;
 import org.blink.game.model.spell.util.SpellModel;
 import org.blink.game.model.spell.util.control.InstantDamageControl;
 import org.blink.game.model.spell.util.effects.DebrisEmitter;
 import org.blink.game.model.spell.util.effects.FlameEmitter;
 import org.blink.game.model.spell.util.effects.FlashEmitter;
 import org.blink.game.model.spell.util.effects.RoundSparkEmitter;
 import org.blink.game.model.spell.util.effects.ShockwaveEmitter;
 import org.blink.game.model.spell.util.effects.SmokeTrailEmitter;
 import org.blink.game.model.spell.util.effects.SparkEmitter;
 import org.blink.net.message.Spell;
 import org.blink.util.LocalConfig;
 
 /**
  * TODO: implement spells in a nice way
  * @author cmessel
  */
 public class Explosion extends SpellModel {
 
     private ParticleEmitter flame, flash, roundspark, spark, smoketrail, debris, shockwave;
 
     public Explosion(AssetManager assetManager, Spell spell) {
         super(assetManager, spell);
 
         flame = new FlameEmitter(assetManager);
         attachChild(flame);
 
         flash = new FlashEmitter(assetManager);
         attachChild(flash);
 
         roundspark = new RoundSparkEmitter(assetManager);
         attachChild(roundspark);
 
         spark = new SparkEmitter(assetManager);
         attachChild(spark);
 
         smoketrail = new SmokeTrailEmitter(assetManager);
         attachChild(smoketrail);
 
         debris = new DebrisEmitter(assetManager);
         attachChild(debris);
 
         shockwave = new ShockwaveEmitter(assetManager);
         attachChild(shockwave);
 
         addControl(new ExplosionControl(this));
 
        if (currentPlayerOwns(spell)) {
             addControl(new InstantDamageControl(this));
         }
 
         setLocalScale(0.5f);
         setLocalTranslation(spell.getVector3f());
     }
 
     public void emilFirstParticleWave() {
         flash.emitAllParticles();
         spark.emitAllParticles();
         smoketrail.emitAllParticles();
         debris.emitAllParticles();
         shockwave.emitAllParticles();
     }
 
     public void emitSecondParticleWave() {
         flame.emitAllParticles();
         roundspark.emitAllParticles();
     }
 
     public void killAllParticles() {
         flash.killAllParticles();
         spark.killAllParticles();
         smoketrail.killAllParticles();
         debris.killAllParticles();
         flame.killAllParticles();
         roundspark.killAllParticles();
         shockwave.killAllParticles();
     }
 }
