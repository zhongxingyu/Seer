 package com.soc.game.attacks;
 
 import com.artemis.Entity;
 import com.artemis.utils.Bag;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.soc.core.Constants;
 import com.soc.core.SoC;
 import com.soc.game.components.Attack;
 import com.soc.game.components.Bounds;
 import com.soc.game.components.Damage;
 import com.soc.game.components.Position;
 import com.soc.game.components.State;
 import com.soc.game.graphics.AnimatedRenderer;
 import com.soc.game.graphics.DirectionalStaticRenderer;
 import com.soc.utils.GraphicsLoader;
 
 public class ArrowProcessor implements AttackProcessor {
 	public boolean hit;
 	public DirectionalStaticRenderer renderer;
 	
 	public ArrowProcessor() {
 		this.hit = false;
 		this.renderer = GraphicsLoader.loadArrow();
 		
 		
 	}
 
 	@Override
 	public void process(Entity attack) {
 		
 	}
 
 	@Override
 	public boolean collision(Entity attack, Entity victim) {
 		Position attackpos = SoC.game.positionmapper.get(attack);
 		Position victimpos = SoC.game.positionmapper.get(victim);
 		Bounds attackbounds = SoC.game.boundsmapper.get(attack);
 		Bounds victimbounds = SoC.game.boundsmapper.get(victim);
 		State state = SoC.game.statemapper.get(victim);
 		
 		return (!hit && state.state != State.DYING && attackpos.x < victimpos.x + victimbounds.width && attackpos.x + attackbounds.width > victimpos.x && attackpos.y < victimpos.y + victimbounds.height && attackpos.y + attackbounds.height > victimpos.y);
 	}
 
 	@Override
 	public void frame(Entity attack, SpriteBatch sprite) {
 		Position pos = SoC.game.positionmapper.get(attack);
 		Bounds bounds = SoC.game.boundsmapper.get(attack);
 		sprite.draw(renderer.frame(SoC.game.world.delta),pos.x,pos.y,bounds.width, bounds.height);
		System.out.println("LLAMO");
 	}
 
 	@Override
 	public void handle(Entity attack, Entity victim) {		
 		Attack a = SoC.game.attackmapper.get(attack);
 		hit=true;
 		if(SoC.game.damagemapper.has(victim)){
 			SoC.game.damagemapper.get(victim).damage+=a.damage;
 		}else{
 			victim.addComponent(new Damage(a.damage));
 			victim.changedInWorld();
 		}
 		attack.deleteFromWorld();
 		
 	}
 
 	@Override
 	public void delete() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
