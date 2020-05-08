 package com.soc.game.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.managers.TagManager;
 import com.artemis.systems.EntityProcessingSystem;
 import com.soc.core.Constants;
 import com.soc.core.SoC;
 import com.soc.game.components.Damage;
 import com.soc.game.components.Delay;
 import com.soc.game.components.Enemy;
 import com.soc.game.components.Expires;
 import com.soc.game.components.Position;
 import com.soc.game.components.Character;
 import com.soc.game.components.State;
 import com.soc.game.components.Stats;
 import com.soc.game.components.Velocity;
 import com.soc.utils.EffectsPlayer;
 import com.soc.utils.FloatingText;
 
 public class DamageProcessingSystem extends EntityProcessingSystem {
 
 
 	@Mapper ComponentMapper<Stats> sm;
 	@Mapper ComponentMapper<Damage> dm;
 	@Mapper ComponentMapper<State> stm;
 	@Mapper ComponentMapper<Velocity> vm;
 	@Mapper ComponentMapper<Enemy> em;
 	@Mapper ComponentMapper<Position> pm;
 	@Mapper ComponentMapper<Character> cm;
 	@Mapper ComponentMapper<Delay> dlm;
 
 
 	
 	@SuppressWarnings("unchecked")
 	public DamageProcessingSystem() {
 		super(Aspect.getAspectForAll(Damage.class));
 	}
 
 	@Override
 	protected void process(Entity e) {	
 		Stats stats=sm.get(e);
 		Damage dr=dm.get(e);
 		State state=stm.get(e);
 		Position pos = pm.get(e);
 		Velocity velocity=vm.get(e);
 		int pureDamage=dr.pureDamage;
 		int dmg = dr.damage - stats.armor;
 		if(dmg < 0) dmg = 0;
 		dmg += pureDamage;
 		if(dmg < 0) dmg = 0; 
 		stats.health-=dmg;
 		e.removeComponent(dr);
 		e.changedInWorld();
 		FloatingText text = new FloatingText(""+dmg, Constants.Configuration.LABEL_DURATION, pos.x, pos.y, Constants.Configuration.LABEL_SPEED);
 		world.getSystem(RenderSystem.class).texts.add(text);
 		text.r = dr.r;
 		text.g = dr.g;
 		text.b = dr.b;
 		if(SoC.game.playermapper.has(e)&& pureDamage==0){
 			text.r=1;
 			text.g=0;
 			text.b=0;
 		}
 		
 		if(stats.health<=0){
			stats.health=0;
 			if(cm.has(e)) EffectsPlayer.play(cm.get(e).deathSound);
 			state.state=State.DYING;
 			if(cm.has(e)) e.addComponent(new Expires((cm.get(e).deathTime)));
 			else e.addComponent(new Expires(1));
 			if(dlm.has(e)) e.removeComponent(Delay.class);
 			
 			e.changedInWorld();
 			velocity.vx=0;
 			velocity.vy=0;
 			
 			if(em.has(e)){
 				Enemy enemy=em.get(e);
 				Entity player=SoC.game.player;
 				sm.get(player).addExperience(enemy.experience);
 			} 
 			
 		} else {
 			if(cm.has(e)) EffectsPlayer.play(cm.get(e).damageSound);
 		}
 	}
 }
