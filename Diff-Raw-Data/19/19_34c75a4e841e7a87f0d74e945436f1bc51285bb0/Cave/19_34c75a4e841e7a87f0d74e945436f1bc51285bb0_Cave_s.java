 package com.first.tribes.core.being;
 
 import static playn.core.PlayN.random;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pythagoras.f.Rectangle;
 
 import com.first.tribes.core.Tile;
 import com.first.tribes.core.TribesWorld;
 import com.first.tribes.core.util.Updatee;
 
 public class Cave implements Updatee {
 
 	private static final int STARTING_MONSTERS=7;
 	private static final int MONSTER_MULTIPLIER=10;//how much stronger a monster is than an average human
 	
 	private ArrayList<Being> monsterList;
 	private int maxMonsters;
 	private TribesWorld world;
 	private int monsterColor;
 	
 	public Cave(TribesWorld world, int color){
 		this.world = world;
 		monsterColor=color;
 		maxMonsters=STARTING_MONSTERS;
 		monsterList=new ArrayList<Being>(STARTING_MONSTERS);
 		
 		while(monsterList.size()<maxMonsters){
 			monsterList.add(newMonster());
 		}
 	}
 	
 	private Monster newMonster() {
 		boolean isSafe = false;
         float x,y;
         do {
             x = random() * world.getAbsoluteSize().width;
             y = random() * world.getAbsoluteSize().height;
         } while (isUnsafe(x,y));
         Monster monster = new Monster(x,y,this,monsterColor);
 		monster.personality.setHardiness(MONSTER_MULTIPLIER*monster.personality.hardiness());
 		monster.personality.setStrength(MONSTER_MULTIPLIER*monster.personality.strength());
         return monster;
 	}
 	
     public List<Being> monstersInArea(Rectangle rectangle){
    	 if (rectangle.width < 0) {
             rectangle.x = rectangle.x + rectangle.width;
             rectangle.width *= -1;
         }
         if (rectangle.height < 0) {
             rectangle.y = rectangle.y + rectangle.height;
             rectangle.height *= -1;
         }
 
         List<Being> area = new ArrayList<Being>();
         for (Being being : monsterList) {
             if (rectangle.contains(being.xPos(), being.yPos())) {
                 area.add(being);
             }
         }
 
         return area;
    	
    }
 
 	boolean isUnsafe(float xPos, float yPos) {
         return world.unsafe(xPos, yPos, 0);
     }
 	
 	 public Tile tileAt(float xPos, float yPos) {
 	        return world.tileAt(xPos, yPos);
 	    }
 	 
 	 public List<Village> enemyVillages() {
 	        List<Village> enemies = world.villages();
 	        return enemies;
 	    }
 	
 	@Override
 	public void update(float delta) {
 		while(monsterList.size()<maxMonsters){
 			monsterList.add(newMonster());
 		}
 
 	}
 
 	public ArrayList<Being> monsters() {
 		return monsterList;
 	}
 	
 	public void spawnMonster(float x, float y) {
 		Monster monster = new Monster(x,y,this,monsterColor);
 		monster.personality.setHardiness(MONSTER_MULTIPLIER*monster.personality.hardiness());
 		monster.personality.setStrength(MONSTER_MULTIPLIER*monster.personality.strength());
 		monsterList.add(monster);
 	}
 
 }
