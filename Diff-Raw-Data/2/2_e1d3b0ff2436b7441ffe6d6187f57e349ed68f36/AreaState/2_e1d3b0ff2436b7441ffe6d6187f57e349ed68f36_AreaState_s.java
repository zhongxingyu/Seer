 package states;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import projectiles.Projectile;
 
 import weapons.Attack;
 import weapons.Bear;
 import weapons.Coin;
 import weapons.Sword;
 import weapons.Weapon;
 import core.MainGame;
 import dudes.Monster;
 import dudes.Player;
 
 public class AreaState extends BasicGameState {
     protected Player[]                      players;
     protected TiledMap                      bgImage;
     protected ArrayList<ArrayList<Monster>> monsters;
     protected ArrayList<Monster>            currBattle;
     private ArrayList<Weapon>               floorweapons;
     private ArrayList<Coin>					floorcoins;
     private boolean                         inBattle;
     private boolean                         completed;
     private int                             progression;
     protected int[]                         battleStops;
     protected int                           areaLength;
     private ArrayList<Player>               sPlayers;
     private final int                       PLAYER_STUN_LENGTH = 500;
     private ArrayList<Projectile> liveProjectiles;
     public AreaState(int stateID) {
         super();
     }
     
     @Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
         progression = 0;
         players = MainGame.players;
         monsters = new ArrayList<ArrayList<Monster>>();
         currBattle = new ArrayList<Monster>();
         //floorweapons = makeInitItems();
         floorweapons = new ArrayList<Weapon>();
         liveProjectiles = new ArrayList<Projectile>();
         floorcoins = new ArrayList<Coin>();
         inBattle = false;
         completed = false;
         areaLength = 0;
         
         sPlayers = new ArrayList<Player>();
         sPlayers.add(players[0]);
         sPlayers.add(players[1]);
     }
     
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
     	int top = 24-container.getHeight()*24/768;
        bgImage.render(-progression % 32, top, progression / 32, top, 32 + 1, 24);
     	
         
         Collections.sort(sPlayers);
         for (Player p : sPlayers) {
             p.render(g);
             g.drawString("PLAYER " + (p.playerID + 1), 25 + (MainGame.GAME_WIDTH - 200) * p.playerID, 50);
             g.drawString("MANLINESS: " + p.score, 25 + (MainGame.GAME_WIDTH - 200)  * p.playerID, 100);
         }
         // for (int i = 0; i < players.length; i++) {
         // players[i].render(g);
         // }
         
         if (inBattle) {
             g.drawString("FIGHT", container.getWidth()/2, 170);
             
             Collections.sort(currBattle);
             for (Monster m : currBattle) {
                 m.render(g);
             }
         }
         
         for (Weapon i : floorweapons) {
             i.Draw();
         }
         
         for (Coin c : floorcoins){
         	c.Draw();
         }
         
         for (Projectile p : liveProjectiles) {
         	p.render(g);
         }
     }
     
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
         
         if (container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
             container.exit();
         }
         // progression++;
         
         for (int i = 0; i < players.length; i++) {
             players[i].move(container.getInput(), delta);
             runOverCoins(players[i]);
         }
         
         for (Projectile p : liveProjectiles) {
         	p.move();
         }
         
         if (inBattle) {
             for (Monster m : currBattle) {
                 m.move(container.getInput(), delta);
             }
         }
         
         float backPlayerPos = Math.min(players[0].pos[0], players[1].pos[0]);
         
         if (progression > 32 * (areaLength - 32)) {// don't scroll if you're at the end of the screen
             completed = true;
         } else {
             if (backPlayerPos > MainGame.GAME_WIDTH / 10) {// don't scroll unless both players are far right enough
                 if (!inBattle) {
                     // float shift = backPlayerPos - MainGame.GAME_WIDTH/3;
                     float shift = 4;
                     for (int stop : battleStops) {
                         if (progression < stop && progression + shift >= stop) {
                             progression = stop;
                             inBattle = true;
                             currBattle = monsters.remove(0);
                         }
                     }
                     if (!inBattle) {
                         progression += shift;
                         players[0].pos[0] -= shift;
                         players[1].pos[0] -= shift;
                         
                         floorItemsMove(shift);
                         
                     }
                 }
             }
         }
         
         for (Player p : players) {
         	while (p.weapon.projectiles.size() > 0) {
         		liveProjectiles.add(p.weapon.projectiles.get(0));
         		p.weapon.projectiles.remove(0);
         	}
         }
         
         removeProjectiles();
         for (Projectile p: liveProjectiles) {
         	for (Monster monster : this.currBattle) {
         		if (p.getHitbox().intersects(monster.hitbox) && p.hasHit == false) {
         			monster.hurt(p.damage, 500);
         			monster.setLastHit(p.owner);
         			p.hasHit = true;
         		}
         	}
         	
         	 
         	for (Player player : players) {
         		if (p.getHitbox().intersects(player.hitbox) && p.hasHit == false) {
         			if (p.owner == player){
         				
         			} else {
         				if (player.isRespawning) {
         					
         				} else {
         					player.hurt(player.weapon.damage, PLAYER_STUN_LENGTH);
         					p.hasHit = true;
         				}
         			}
         		}
         	}
         }
         
         for (int i = 0; i < players.length; i++) {
             Player player = players[i];
             player.invincibleTimer += delta;
             player.weapon.updateAttacks();
             for (Attack attack : player.weapon.attacks) {
                 for (Monster monster : this.currBattle) {
                     if (attack.hitbox.intersects(monster.hitbox)) {
                         monster.hurt(player.weapon.damage, 500);
                         monster.setLastHit(player);
                     }
                 }
                 if (attack.hitbox.intersects(players[(i + 1) % 2].hitbox)) {
                 	if (!players[(i + 1) % 2].isRespawning) {
                 		players[(i + 1) % 2].hurt(player.weapon.damage, PLAYER_STUN_LENGTH);
                 	}
                 }
             }
         }
         
         for (Monster monster : this.currBattle) {
             monster.invincibleTimer += delta;
             monster.weapon.updateAttacks();
             monster.aiLoop(players, delta);
             for (Attack attack : monster.weapon.attacks) {
                 for (Player player : players) {
                     if (attack.hitbox.intersects(player.hitbox)) {
                     	if (!player.isRespawning) {
                     		player.hurt(monster.weapon.damage, 500);
                     	}
                     }
                 }
             }
             monster.hitbox.setX(monster.pos[0]);
             monster.hitbox.setY(monster.pos[1]);
         }
         for (Player player : players) {
             player.hitbox.setX(player.pos[0]);
             player.hitbox.setY(player.pos[1]);
         }
         
         ArrayList<Weapon> remove = new ArrayList<Weapon>();
         ArrayList<Weapon> add = new ArrayList<Weapon>();
         for (Player p : players) {
             if (container.getInput().isKeyPressed(p.buttons.get("pickup"))) {
                 for (Weapon w : floorweapons) {
                     if (p.hitbox.intersects(w.getHitBox())) {
                         p.weapon.drop();
                         if (p.weapon.groundSprite == null) {
                             
                         } else {
                             add.add(p.weapon);
                         }
                         p.weapon = w;
                         w.assignOwner(p);
                         w.init();
                         remove.add(w);
                         
                     }
                 }
                 //runOverCoins(p);
             }
         }
         
         checkIfMonsterDead();
         for (Player p : players) {
         	p.deathCheck(delta);
         }
         
         for (Weapon r : remove) {
             floorweapons.remove(r);
         }
         
         for (Weapon a : add) {
             floorweapons.add(a);
         }
         
         if (currBattle.size() == 0 && inBattle) {
             inBattle = false;
         }
     }
     
     public ArrayList<Weapon> makeInitItems() throws SlickException {
         return new ArrayList<Weapon>();
     }
     
     public void runOverCoins(Player p){
         ArrayList<Coin> out = new ArrayList<Coin>();
         for (Coin c : floorcoins){
             if (p.hitbox.intersects(c.getHitBox())) {
             	p.score += c.value;
             	out.add(c);
             }
         }
         for (Coin c : out){
         	floorcoins.remove(c);
         }
     }
     
     public void floorItemsMove(float shift){
         ArrayList<Weapon> remove = new ArrayList<Weapon>();
         for (Weapon i : floorweapons) {
             i.x -= shift;
             if (i.x < 0) {
                 remove.add(i);
             }
         }
 
         for (Weapon i : remove) {
             floorweapons.remove(i);
         }
         
         ArrayList<Coin> rid = new ArrayList<Coin>();
         for (Coin c : floorcoins) {
             c.pos[0] -= shift;
             if (c.pos[0] < 0) {
                 rid.add(c);
             }
         }
         
         for (Coin i : rid) {
             floorcoins.remove(i);
         }
     }
     
     public void removeProjectiles() {
     	ArrayList<Projectile> removeProjectiles = new ArrayList<Projectile>();
     	for (Projectile p : liveProjectiles) {
     		if (p.hasHit) {
     			removeProjectiles.add(p);
     		} else if (p.pos[0] > MainGame.GAME_WIDTH + 200) {
     			removeProjectiles.add(p);
     		}    		
     		
     	}
     	
     	for (Projectile p : removeProjectiles) {
     		liveProjectiles.remove(p);
     	}
     	
     }
 
     public void checkIfMonsterDead() throws SlickException{
         ArrayList<Monster> removeMonster = new ArrayList<Monster>();
         for (Monster m : this.currBattle) {
             if (m.health <= 0) {
         		removeMonster.add(m);
                 m.getLastHit().incrementScore(100);
             }
         }
 
         monsterDrop(removeMonster);
     }
     
     public void monsterDrop(ArrayList<Monster> removeMonster) throws SlickException{
         for (Monster m : removeMonster) {
         	m.renderDeath();
             this.currBattle.remove(m);
             double rand = Math.random();
             Weapon w = m.getDropWeapon();
             if (w == null) {
             	
             } else {
          		floorweapons.add(w);	
             }            
             
             Coin c = m.getDropCoin();
             if (c == null) {
             	
             } else {
                 floorcoins.add(c);	
             }
             
         }
     }
     
     @Override
     public int getID() {
         // TODO Auto-generated method stub
         return 1;
     }
 }
