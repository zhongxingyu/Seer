 package it.wargame.map;
 
 import it.wargame.ai.TargetAi;
 import it.wargame.creatures.Creature;
 import it.wargame.gamestates.GameWorld;
 
import java.util.ArrayList;
import java.util.Iterator;

 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.pathfinding.PathFindingContext;
 import org.newdawn.slick.util.pathfinding.TileBasedMap;
 
 public class GameMap implements TileBasedMap {
 
 	private int[][] map;
 	private int size;
 
 	public static final int GRASS = 0;
 	public static final int BLOCK = 1;
 
 	private GameMapRenderer mapRenderer;
 	private Creature selectedCreature;
 
 	private ArrayList<Creature> creatures = new ArrayList<Creature>();
 	private TargetAi ai;
 	
 	public GameMap(int size) {
 		this.size = size;
 		map = new int[size][size];
 		mapRenderer = new GameMapRenderer(this);
 	}
 
 	public GameMap init() {
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				map[i][j] = GRASS;
 			}
 		}
 		return this;
 	}
 	
 	public GameMap placeBlock(int x, int y){
 		map[x][y] = BLOCK;
 		return this;
 	}
 
 	public int getTile(int x, int y) {
 		if (x < 0 || x > size || y < 0 || y > size) {
 			return -1;
 		}
 		return map[x][y];
 	}
 	
 	public int getSize() {
 		return size;
 	}
 
 	public void render(GameContainer container, StateBasedGame state, Graphics g) throws SlickException {
 		mapRenderer.render(container, state, g, selectedCreature);
 	}
 
 	public void drawMoveable(Creature selectedCreature) {
 		this.selectedCreature = selectedCreature;
 	}
 
 	public boolean isGrass(int x, int y){
 		return getTile(x,y) == GRASS;
 	}
 	
 	public boolean isBlock(int x, int y) {
 		return getTile(x,y) == BLOCK;
 	}
 
 	@Override
 	public boolean blocked(PathFindingContext arg0, int x, int y) {
 		return isBlock(x,y) || isCreatureAt(x, y, Creature.GROUP_AI);
 	}
 
 	@Override
 	public float getCost(PathFindingContext arg0, int arg1, int arg2) {
 		return 0;
 	}
 
 	@Override
 	public int getHeightInTiles() {
 		return size;
 	}
 
 	@Override
 	public int getWidthInTiles() {
 		// TODO Auto-generated method stub
 		return size;
 	}
 
 	@Override
 	public void pathFinderVisited(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 	}
 	
 	public void placeUnits(GameWorld world) throws SlickException {
 		// add player units
 		Creature c = Creature.buildWarrior().setLocation(0, 1).setGroup(Creature.GROUP_PLAYER);
 		creatures.add(c);
 		c = Creature.buildArcher().setLocation(0, 2).setGroup(Creature.GROUP_PLAYER);
 		creatures.add(c);
 		c = Creature.buildArcher().setLocation(0, 3).setGroup(Creature.GROUP_PLAYER);
 		creatures.add(c);
 		c = Creature.buildWarrior().setLocation(0, 4).setGroup(Creature.GROUP_PLAYER);
 		creatures.add(c);
 
 		// add ai units
 		c = Creature.buildWarrior().setLocation(11, 1).setGroup(Creature.GROUP_AI);
 		creatures.add(c);
 		c = Creature.buildArcher().setLocation(11, 2).setGroup(Creature.GROUP_AI);
 		creatures.add(c);
 		c = Creature.buildArcher().setLocation(11, 3).setGroup(Creature.GROUP_AI);
 		creatures.add(c);
 		c = Creature.buildWarrior().setLocation(11, 4).setGroup(Creature.GROUP_AI);
 		creatures.add(c);
 
 		ai = new TargetAi(creatures, world);
 	}
 
 	public void renderCreatures(GameContainer container, StateBasedGame state,
 			Graphics g) throws SlickException {
 		for (Creature c : creatures) {
 			c.render(container, state, g);
 		}
 	}
 
 	public void removeDeadCreatures(GameContainer gc, StateBasedGame state,
 			int arg2) {
 		// check dead creatures
 		for (Iterator<Creature> iterator = creatures.iterator(); iterator.hasNext();) {
 			Creature t = iterator.next();
 			if (t.isDead()) {
 				iterator.remove();
 			}
 		}
 		
 	}
 	
 	public boolean isTargetable(int tx, int ty, int targetGroup) {
 		for (Creature c : creatures) {
 			if (c.getOwner() != targetGroup) {
 				if (c.getX() == tx && c.getY() == ty) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}	
 	
 	public Creature isCreature(int x, int y) {
 		for (Creature c : creatures) {
 			if (c.isLocation(x, y)) {
 				return c;
 			}
 		}
 		return null;
 	}
 	
 	public boolean isCreatureAt(int x, int y,int targetGroup) {
 		return isCreature(x,y) != null && isCreature(x, y).getOwner() == targetGroup ? true : false;
 	}	
 
 	public void resetMoveAttackCreatures() {
 		// reset moves&atacks
 		for (Creature c : creatures) {
 			c.setMoved(false);
 			c.setAttacked(false);
 		}
 		
 	}	
 
 }
