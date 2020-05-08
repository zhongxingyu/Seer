 package org.blink.game.model.spell.util;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.effect.ParticleMesh.Type;
 import com.jme3.scene.Node;
 import org.blink.net.message.Spell;
 import org.blink.util.LocalConfig;
 
 /**
  *
  * @author cmessel
  */
 public class SpellModel extends Node {
 
     protected AssetManager assetManager;
     protected static final int COUNT_FACTOR = 1;
     protected static final float COUNT_FACTOR_F = 1f;
     protected static final boolean POINT_SPRITE = true;
     protected static final Type EMITTER_TYPE = POINT_SPRITE ? Type.Point : Type.Triangle;
 
     public SpellModel(AssetManager assetManager, Spell spell) {
         super(spell.getSpellName());
         this.assetManager = assetManager;
     }
 }
