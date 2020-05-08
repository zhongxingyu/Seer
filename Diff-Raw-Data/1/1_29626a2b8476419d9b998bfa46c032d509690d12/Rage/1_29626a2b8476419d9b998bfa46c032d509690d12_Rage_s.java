 package com.osc.game.benefits;
 
 import com.artemis.Entity;
 import com.soc.core.Constants;
 import com.soc.core.SoC;
 import com.soc.game.components.Stats;
 import com.soc.game.graphics.AnimatedRenderer;
 import com.soc.utils.GraphicsLoader;
 
 public class Rage implements Benefit{
 	public float timer;
 	public float gainStrength;
 	public int initialStrength;
 	boolean buffAdded;
 	public float lastBuff;
 	public AnimatedRenderer renderer; 
 	public Rage(){
 		timer=Constants.Buff.RAGE_DURATION;
 		initialStrength=0;
 		gainStrength=Constants.Buff.GAIN_STRENGTH;
 		buffAdded=false;
 		lastBuff=Constants.Buff.RAGE_DURATION;
 		renderer=GraphicsLoader.loadRageAura();
 	}
 	@Override
 	public void process(Entity e) {
 		timer -= SoC.game.world.delta;
 		Stats stats=SoC.game.statsmapper.get(e);
 		if(!buffAdded && timer>0){
 			initialStrength=stats.strength;
 			stats.strength+=Constants.Buff.GAIN_STRENGTH;
 			lastBuff=timer;
 		}else{
 			if(timer>0){
 				if((lastBuff-timer)>=1){
 					stats.strength+=Constants.Buff.GAIN_STRENGTH;
 					lastBuff=timer;
 				}
 			}else{
 				stats.strength=initialStrength;
 				SoC.game.buffmapper.get(e).removebuff(this);
 			}
 		}
 		
 	}
 
 }
