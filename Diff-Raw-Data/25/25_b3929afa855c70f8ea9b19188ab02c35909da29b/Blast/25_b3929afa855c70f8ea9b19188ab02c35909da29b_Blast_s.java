 package org.blink.game.model.spell.blast;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.math.ColorRGBA;
 import org.blink.game.model.spell.util.SpellModel;
 import org.blink.game.model.spell.util.control.ProgressiveDamageControl;
 import org.blink.game.model.spell.util.effects.DebrisEmitter;
 import org.blink.game.model.spell.util.effects.FlameEmitter;
 import org.blink.game.model.spell.util.effects.SmokeTrailEmitter;
 import org.blink.game.model.spell.util.effects.SparkEmitter;
 import org.blink.net.message.Spell;
 
 /**
  *
  * @author cmessel
  */
 public class Blast extends SpellModel {
 
     private final float SPEED = 5f;
 
     public Blast(AssetManager assetManager, Spell spell) {
         super(assetManager, spell);
 
         FlameEmitter fe = new FlameEmitter(assetManager);
         fe.setStartColor(ColorRGBA.Cyan);
         fe.setParticlesPerSec(20);
         fe.setGravity(0);
         attachChild(fe);
 
         SparkEmitter spark = new SparkEmitter(assetManager);
         spark.setParticlesPerSec(20);
         spark.setStartColor(ColorRGBA.DarkGray);
         spark.setGravity(0);
         attachChild(spark);
 
         SmokeTrailEmitter smoketrail = new SmokeTrailEmitter(assetManager);
         smoketrail.setStartColor(ColorRGBA.Black);
         smoketrail.setParticlesPerSec(20);
         smoketrail.setGravity(0);
         attachChild(smoketrail);
 
         DebrisEmitter debris = new DebrisEmitter(assetManager);
         debris.setStartColor(ColorRGBA.Blue);
         debris.setParticlesPerSec(20);
         debris.setGravity(0);
         attachChild(debris);
 
         setLocalTranslation(spell.getVector3f());
        
         addControl(new BlastControl(this, spell.getOwner()));
        
        addControl(new ProgressiveDamageControl(this));
     }
 
     public float getSpeed() {
         return SPEED;
     }
 }
