 
 package com.soc.game.systems;
 
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.VoidEntitySystem;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.soc.core.Constants;
 import com.soc.core.SoC;
 import com.soc.game.attacks.spells.Spell;
 import com.soc.game.components.Buff;
 import com.soc.game.components.Character;
 import com.soc.game.components.Delay;
 import com.soc.game.components.Player;
 import com.soc.game.components.Position;
 import com.soc.game.components.State;
 import com.soc.game.components.Stats;
 import com.soc.game.components.Velocity;
 import com.soc.game.states.benefits.LevelUp;
 import com.soc.game.states.benefits.Rage;
 import com.soc.game.states.benefits.Shield;
 import com.soc.game.states.benefits.ShieldBuff;
 import com.soc.game.states.benefits.Teleport;
 import com.soc.hud.HudSystem;
 import com.soc.utils.FloatingText;
 
 
 	public class PlayerInputSystem extends VoidEntitySystem implements InputProcessor{
 		 @Mapper ComponentMapper<Velocity> vm;
 		 @Mapper ComponentMapper<State> sm;
 		 @Mapper ComponentMapper<Position>pm;
 		 @Mapper ComponentMapper<Player> plm;
 		 @Mapper ComponentMapper<Stats> stm;
 		 @Mapper ComponentMapper<Character> cm;
 		 @Mapper ComponentMapper<Buff> bm;
 		 
 		 private float timer;
 		 private int lastKey;
 		 private boolean running;
 		 		 
 		 public PlayerInputSystem() {
 			 super();
 		 }
 		  
 		 @Override
 		 protected void initialize() {
 			 timer = 0;
 		 }
 		 
 		 @Override
 		 protected void processSystem() {
 		   
 			 Velocity vel = vm.get(SoC.game.player);			 
 			 State state = sm.get(SoC.game.player);
 			 Position pos=pm.get(SoC.game.player);
 			 Player player = plm.get(SoC.game.player);
 			  
 			 if((state.state < State.BLOCKED || state.state == State.SPINNING)){
 				 
 				boolean moving = false;
 				 
 			 	if(Gdx.input.isKeyPressed(player.move_up)){
 				 	vel.vy = vel.speed;
 				 	moving = true;
 			 	} else if(Gdx.input.isKeyPressed(player.move_down)){
 				 	vel.vy = -vel.speed;
 				 	moving = true;
 			 	} else {
 				 	vel.vy = 0;
 			 	}
 			 
 			 	if(Gdx.input.isKeyPressed(player.move_left)){
 				 	vel.vx = -vel.speed;
 				 	moving = true;
 			 	} else if(Gdx.input.isKeyPressed(player.move_right)){
 				 	vel.vx = vel.speed;
 				 	moving = true;
 			 	} else {
 				 	vel.vx = 0;
 			 	}
 			 	
 			 	if(moving){
 			 		if(running){
 			 			vel.vx*=2;
 			 			vel.vy*=2;
 			 		}
 			 		pos.direction.x = Math.signum(vel.vx);
 					pos.direction.y = Math.signum(vel.vy);
 			 	}
 			 	
 			 	timer -= world.delta;
 			 	if(timer <= 0){
 			 		lastKey = -1;
 			 	}
 			 	
 			 	if(state.state == State.SPINNING){
 					running = false;
 				 	lastKey = -1;
 			 		return;
 			 	}
 			 	if(moving && running){
 			 		state.state = State.RUN;
 			 	} else if(moving){
 				 	state.state = State.WALK;
 			 	} else {
 				 	state.state = State.IDLE;
 				 	running = false;
 			 	}
 			 }
 			 if(state.state >= State.BLOCKED){
 				 running = false;
 			 	 lastKey = -1;
 			 }
 			 			 
 		 }
 
 		@Override
 		public boolean keyDown(int keycode) {
 			Player controls = plm.get(SoC.game.player);
 			Entity player = SoC.game.player;
 			Position pos = pm.get(player);
 			State state = sm.get(player);
 			Stats stats = stm.get(player);
 			Velocity vel = vm.get(player);
 			
 			if(keycode==controls.inventory){
 				world.getSystem(HudSystem.class).toggleInventory();
 				return true;
 			}
 			if(keycode==controls.characterMenu){
 				world.getSystem(HudSystem.class).toogleCharacterMenu();
 				return true;
 			}
 			if(keycode==controls.gameMenu){
 				world.getSystem(HudSystem.class).toogleGameMenu();
 				return true;
 			}
 			if(controls.blocking || state.state >= State.BLOCKED) return false;
 
 			if(keycode == controls.attack){
 				int spellnum = stm.get(player).attack;
 				Spell spell = SoC.game.spells[spellnum];
 				state.state = spell.state;
 				player.addComponent(new Delay(Constants.Groups.PLAYER_ATTACKS, spell.cast, spell.blocking, spellnum));
 				player.changedInWorld();
 				vel.vx = 0;
 				vel.vy = 0;
 				return true;
 			}
 			
 			for(int i = 0; i < controls.spellkeys.length; i++){
 				if(keycode == controls.spellkeys[i]){
 					int spellnum = stm.get(player).spells[i];
 					if(spellnum == Constants.Spells.NO_SPELL) return false;
 					Spell spell = SoC.game.spells[spellnum];
 					if(stats.mana < spell.mana){
 						FloatingText text = new FloatingText("No mana!", 1f, pos.x, pos.y, 50);
 						text.r = 0.5f;
 						text.b = 1;
 						text.g = 0.5f;
 						SoC.game.renderSystem.texts.add(text);
 						return true;
 					}
 					stats.mana -= spell.mana;
 					state.state = spell.state;
 					player.addComponent(new Delay(Constants.Groups.PLAYER_ATTACKS, spell.cast, spell.blocking, spellnum));
 					player.changedInWorld();
 					vel.vx = 0;
 					vel.vy = 0;
 					return true;
 				}
 			}
 			if(keycode == controls.shield){
 				if(stats.mana <= 0){
 					FloatingText text = new FloatingText("No mana!", 1f, pos.x, pos.y, 50);
 					text.r = 0.5f;
 					text.b = 1;
 					text.g = 0.5f;
 					SoC.game.renderSystem.texts.add(text);
 					return true;
 				}
 				Buff.addbuff(SoC.game.player, new Shield());
 				vel.vx = 0;
 				vel.vy = 0;
 				state.state = State.IDLE;
 				controls.blocking = true;
 				return true;
 			}
 			if(keycode==controls.move_down || keycode==controls.move_right || keycode==controls.move_left || keycode==controls.move_up){
 				if(lastKey == keycode){
 					running = true;
 				} else {
 					lastKey = keycode;
 					timer = 0.2f;
 				}
 			}
 			if(keycode==controls.healthPotion){
 				SoC.game.hudSystem.inventory.useFirstHealthPotion();
 			}
 			if(keycode==controls.manaPotion){
 				SoC.game.hudSystem.inventory.useFirstManaPotion();
 			}
 			return false;
 		}
 
 		@Override
 		public boolean keyUp(int keycode) {
 			Player controls = SoC.game.playermapper.get(SoC.game.player);
 			if(keycode == controls.shield){
 				if(controls.blocking){
 					bm.get(SoC.game.player).removebuff(Shield.class,SoC.game.player);;
 					controls.blocking = false;
 				}
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public boolean keyTyped(char character) {
 
 			
 			return false;
 		}
 
 		@Override
 		public boolean touchDown(int screenX, int screenY, int pointer,
 				int button) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean touchDragged(int screenX, int screenY, int pointer) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean mouseMoved(int screenX, int screenY) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean scrolled(int amount) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 	}
