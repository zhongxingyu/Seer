 package com.first.tribes.core.being;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.first.tribes.core.Tile;
 import com.first.tribes.core.being.Being.DeathReason;
 
 import playn.core.CanvasImage;
 import playn.core.Color;
 import playn.core.Font;
 import playn.core.Gradient;
 import playn.core.Surface;
 import pythagoras.f.Rectangle;
 
 import static playn.core.PlayN.*;
 import playn.core.TextFormat;
 import playn.core.TextLayout;
 
 public class Monster extends Being{
 
 	
 	private static final float MAX_ATTACK_RADIUS = 1000f;
 	private static final float ENEMY_PRIORITY = 15f;
 	private static final float MONSTER_SIZE = 30.0f;
     private static final float HEIGHT_PRIORITY = 5f;
 	
 	private static int monsterCount = 0;
     private Cave cave;
     
     private String name;
     private int number;
 
     private int color;
 	
 	public Monster(float xPos, float yPos, Cave cave, int color) {
 		super(xPos, yPos, MONSTER_SIZE, MONSTER_SIZE);
         this.cave = cave;
         this.color = color;
         this.xVel = (random() - 0.5f) * personality.mobility() * 4;
         this.yVel = (random() - 0.5f) * personality.mobility() * 4;
         name = genName();
         number = ++monsterCount;
 	}
 	
 	final static String firstSounds[] = {"'Ai", "Ali'", "Al", "'Au", "'Eh", "Ha'", "Ha", "Hi'", "Ho'", "'Io", "Ka'", "Ka", "Kai", "Ke'", "Ke", "Ki", "Ko", "Ku", "Ku'", "La'", "La", "Lei", "Li", "Lo", "Lu", "Ma", "Me", "Mi", "Mo", "Na'", "Nai'", "No", "Ona", "Pa", "Pi'", "Po'", "Pu", "U'", "Ulu", "Wa"};
     final static String laterSounds[] = {"la", "loa", "ka", "ne", "na", "kai", "hu", "wa", "ok", "ni", "pa", "ke", "leo", "le", "mi", "mue", "pe", "ma", "mo", "ki", "lo", "pau", "nu", "ke"};
 
     public static String genName() {
         String name = firstSounds[(int) (random() * firstSounds.length)];
 
         int additionalPieces = (int) ((random() * 4) + 1);
         for (int i = 0; i < additionalPieces; i++) {
             name += laterSounds[(int) (random() * laterSounds.length)];
         }
 
         return name;
     }
 
     private CanvasImage monsterImage;
     
     public void paintToRect(Rectangle rect, Surface surface) {
         if (monsterImage == null) {
             monsterImage = graphics().createImage(width, height);
             monsterImage.canvas().setFillColor(color);
             monsterImage.canvas().fillCircle(width / 2, height / 2, width / 2);
         }
         surface.drawImage(monsterImage, rect.x, rect.y, rect.width, rect.height);
     }
 
     public void update(float delta) {
         if (deathReason != null)
             return;
 
         Tile myTile = cave.tileAt(this.xPos, this.yPos);
         
         if(personality.aggression() > (float)Math.random()){
         	attack(findTarget());
         }
         
         if (newGoal(myTile)) {
                 
             Tile bestTile = pickTile(myTile);
             
             xVel = bestTile.bounds().center().x - this.xPos;
             yVel = bestTile.bounds().center().y - this.yPos;
             float speed = (float) Math.sqrt(xVel * xVel + yVel * yVel);
             float normalizer = speed / personality.mobility();
             xVel = xVel / normalizer;
             yVel = yVel / normalizer;
 
             xPos += xVel * delta;
             yPos += yVel * delta;
         }
 
         if (cave.isUnsafe(xPos, yPos)) {
             xVel *= -1;
             xPos += xVel * delta;
             yVel *= -1;
             yPos += yVel * delta;
         }
         if (cave.isUnsafe(xPos, yPos)) {
             //Drowned
             setDead(DeathReason.DROWNING);
             return;
         }
 
     }
 
     public void attack(Being v) {
         
     	if(v!=null){
     		float a = ((float) Math.random() * personality.strength());
     		float b = ((float) Math.random() * v.personality.hardiness());
         	if (a > b) {
         		v.setDead(DeathReason.KILLED_BY_MONSTER);
         	}
     	}
     }
     
 
     public Tile pickTile(Tile myTile) {
         Tile bestTile = myTile;
         float bestScore = calculateScore(myTile);
         
         float foundScore;
         for (Tile tile : myTile.neighbors()) {
             if (tile.isSafe(0)) {
             	foundScore = calculateScore(tile);
                 if (foundScore > bestScore) {
                     bestScore = foundScore;
                     bestTile = tile;
                 }
             }
         }
         return bestTile;
     }
     
     public float calculateScore(Tile tile){
     	float foundEnemy = 0;
     	List<Village> enemies = cave.enemyVillages();
     	for(int i=0; i<enemies.size(); i++){
     		foundEnemy += enemies.get(i).getDensityAt(tile)*personality.aggression()/((float)enemies.size()) ;
     	}
     	foundEnemy*=ENEMY_PRIORITY+random();
     	
     	float heightValue = tile.height()*personality.hardiness()*HEIGHT_PRIORITY+random();
     	
 //    	if(Math.random()<0.01)
 //    		System.out.println(foundFood+" "+foundEnemy+" "+foundFriend+" "+heightValue);
     	
     	return foundEnemy+heightValue;
     }
     
    public Villager findTarget(){
     	
    	List<Villager> e = new ArrayList<Villager>();
     	
     	List<Village> v = cave.enemyVillages();
     	
     	float radius = personality.aggression()*MAX_ATTACK_RADIUS;
     	
     	for(int i=0; i<v.size(); i++){
     		e.addAll(v.get(i).villagersInArea( new Rectangle(xPos-radius,yPos-radius,2*radius,2*radius) ));
     	}
     	float minDist = radius*radius;
     	int minLoc = -1;
     	for(int i=0; i<e.size(); i++){
     		if(Math.pow(e.get(i).xPos()-xPos(), 2)+Math.pow(e.get(i).yPos()-yPos(),2)<minDist){
     			minLoc=i;
     			minDist = (float) (Math.pow(e.get(i).xPos()-xPos(), 2)+Math.pow(e.get(i).yPos()-yPos(),2));
     		}
     	}
     	
     	
     	if(minLoc==-1){
     		return null;
     	}
     	return e.get(minLoc);
     }
     
     public boolean newGoal(Tile myTile) {
 
         if (Math.random()<.1) {
             return true;
         }
         
         return false;
     }
 
 
     public static final float STATS_BOX_HEIGHT = 96f;
 
     public void drawStatsBoxAt(Surface surface, float x, float y, float width, float height) {
         if (visualInfo == null) {
             visualInfo = graphics().createImage((int) width, (int) height);
             visualInfo.canvas().setFillColor(Color.rgb(0, 0, 0));
             visualInfo.canvas().fillRect(0, 0, width, height);
 
             visualInfo.canvas().setFillColor(this.color);
             visualInfo.canvas().fillRect(5, 5, width - 10, height - 10);
 
             Font titleFont = graphics().createFont("Sans serif", Font.Style.PLAIN, 16);
             TextLayout nameLayout = graphics().layoutText(name, new TextFormat().withFont(titleFont).withWrapWidth(200));
             visualInfo.canvas().setFillColor(Color.argb(200, 255, 255, 255));
             visualInfo.canvas().fillText(nameLayout, 8, 4);
 
             if (deathReason != null) {
                 Font deadFont = graphics().createFont("Sans serif", Font.Style.BOLD, 14);
                 TextLayout deadLayout = graphics().layoutText(" " + deathReason.description(), new TextFormat().withFont(deadFont).withWrapWidth(200));
                 visualInfo.canvas().setFillColor(Color.rgb(255, 100, 100));
                 visualInfo.canvas().fillText(deadLayout, 14 + nameLayout.width(), 4 + nameLayout.height() - deadLayout.height());
             }
 
 
             StringBuilder stats = new StringBuilder();
             stats.append("Rep. Appeal: " + (int) (personality.reproductiveAppeal() * 1000) / 1000.0);
             stats.append('\n');
             stats.append("Longevity: " + (int) (personality.longevity() * 1000) / 1000.0);
             stats.append('\n');
             stats.append("Intelligence: " + (int) (personality.intelligence() * 1000) / 1000.0);
             stats.append('\n');
             stats.append("Mobility: " + (int) (personality.mobility() * 1000) / 1000.0);
 
             Font textFont = graphics().createFont("Sans serif", Font.Style.PLAIN, 13);
             TextLayout firstColumnLayout = graphics().layoutText(stats.toString(), new TextFormat().withFont(textFont).withWrapWidth(width));
             visualInfo.canvas().setFillColor(Color.argb(200, 255, 255, 255));
             visualInfo.canvas().fillText(firstColumnLayout, 8, 3 + nameLayout.height());
 
             stats = new StringBuilder();
             stats.append("Hardiness: " + (int) (personality.hardiness() * 1000) / 1000.0);
             stats.append('\n');
             stats.append("Aggression: " + (int) (personality.aggression() * 1000) / 1000.0);
             stats.append('\n');
             stats.append("Loyalty: " + (int) (personality.loyalty() * 1000) / 1000.0);
             stats.append('\n');
             TextLayout secondColumnLayout = graphics().layoutText(stats.toString(), new TextFormat().withFont(textFont).withWrapWidth(width));
             visualInfo.canvas().setFillColor(Color.argb(200, 255, 255, 255));
             visualInfo.canvas().fillText(secondColumnLayout, firstColumnLayout.width() + 30, 3 + nameLayout.height());
 
 
 
         }
         surface.drawImage(visualInfo, x, y);
     }
 
 	
 	
 	
 }
