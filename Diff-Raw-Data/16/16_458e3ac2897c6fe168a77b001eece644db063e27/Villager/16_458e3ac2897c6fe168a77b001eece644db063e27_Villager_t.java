 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
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
 
 /**
  *
  * @author taylor
  */
 public class Villager extends Being {
 
     public static final int MAX_AGE = 60000;
     private static final float VILLAGER_SIZE = 10.0f;
     private static final float MAX_ATTACK_RADIUS = 1000f;
     private static final float SOLDIER_APPEAL = .1f;
     private static final float SPOILS_CONSTANT = 10;
     //Movement calculation constants
     private static final float FOOD_PRIORITY = 0.5f;
     private static final float ENEMY_PRIORITY = 15f;
     private static final float FRIEND_PRIORITY = 0.1f;
     private static final float HEIGHT_PRIORITY = 5f;
     private static int villagerCount = 0;
     private Village village;
     private int color;
     private int age;
     private float hunger;
     private String name;
     private int number;
 
     public Villager(float xPos, float yPos, Village village, int color) {
         super(xPos, yPos, VILLAGER_SIZE, VILLAGER_SIZE);
         this.village = village;
         this.xVel = (random() - 0.5f) * personality.mobility() * 4;
         this.yVel = (random() - 0.5f) * personality.mobility() * 4;
         name = genName();
         this.color = color;
         number = ++villagerCount;
     }
 
     Villager copy() {
         Villager villager = new Villager(xPos, yPos, village, color);
         villager.name = this.name;
         villager.number = this.number;
         villager.personality = this.personality.copy();
         return villager;
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
     private CanvasImage villagerImage;
 
     public void paintToRect(Rectangle rect, Surface surface) {
         if (villagerImage == null) {
             villagerImage = graphics().createImage(width, height);
             villagerImage.canvas().setFillColor(color);
             villagerImage.canvas().fillCircle(width / 2, height / 2, width / 2);
         }
         surface.drawImage(villagerImage, rect.x, rect.y, rect.width, rect.height);
     }
 
     public void update(float delta) {
         if (deathReason != null)
             return;
 
         age += delta;
         if (age >= this.personality.longevity() * MAX_AGE) {
             setDead(DeathReason.AGE);
             return;
         }
 
         Tile myTile = myTile();
 
         if (personality.aggression() > (float) Math.random()) {
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
 
         if (village.isUnsafe(xPos, yPos)) {
             xVel *= -1;
             xPos += xVel * delta;
             yVel *= -1;
             yPos += yVel * delta;
         }
         if (village.isUnsafe(xPos, yPos)) {
             //Drowned
             setDead(DeathReason.DROWNING);
             return;
         }
 
 
         hunger += foodRequired();
         float food = village.gatherFood(this, Math.max(Math.min(hunger, 2 * foodRequired()), foodRequired()));
         hunger -= food;
         if (hunger > 20) {
             setDead(DeathReason.STARVING);
         }
     }
 
     public void attack(Being v) {
 
         if (v != null) {
             float a = ((float) Math.random() * personality.strength());
             float b = ((float) Math.random() * v.personality.hardiness());
             if (a > b) {
                 v.setDead(DeathReason.KILLED_BY_VILLAGER);
                 hunger -= v.personality.hardiness() * SPOILS_CONSTANT;
                 personality.setReproductiveAppeal(personality.reproductiveAppeal() + SOLDIER_APPEAL);
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
 
     public float calculateScore(Tile tile) {
         float foundFood = (tile.numFood) * FOOD_PRIORITY + random();
         float foundEnemy = 0;
         List<Village> enemies = village.enemyVillages();
         for (int i = 0; i < enemies.size(); i++) {
             foundEnemy += enemies.get(i).getDensityAt(tile) * personality.aggression() / ((float) enemies.size());
         }
         foundEnemy *= ENEMY_PRIORITY + random();
         float foundFriend = village.getDensityAt(tile) * personality.loyalty() * FRIEND_PRIORITY + random();
 
        if(tile.equals(village.tileAt(xPos(), yPos()))){
        	foundFriend = 0;
        }
        
         float heightValue = tile.height() * personality.hardiness() * HEIGHT_PRIORITY + random();
 
 //    	if(Math.random()<0.01)
 //    		System.out.println(foundFood+" "+foundEnemy+" "+foundFriend+" "+heightValue);
 
         return foundFood + foundEnemy + foundFriend - heightValue;
     }
 
     public Being findTarget() {
 
         List<Being> e = new ArrayList<Being>();
 
         List<Village> v = village.enemyVillages();
 
         List<Cave> c = village.enemyCaves();
 
         float radius = personality.aggression() * MAX_ATTACK_RADIUS;
 
         for (int i = 0; i < v.size(); i++) {
             e.addAll(v.get(i).villagersInArea(new Rectangle(xPos - radius, yPos - radius, 2 * radius, 2 * radius)));
         }
         for (int i = 0; i < c.size(); i++) {
             e.addAll(c.get(i).monstersInArea(new Rectangle(xPos - radius, yPos - radius, 2 * radius, 2 * radius)));
         }
         float minDist = radius * radius;
         int minLoc = -1;
         for (int i = 0; i < e.size(); i++) {
             if (Math.pow(e.get(i).xPos() - xPos(), 2) + Math.pow(e.get(i).yPos() - yPos(), 2) < minDist) {
                 minLoc = i;
                 minDist = (float) (Math.pow(e.get(i).xPos() - xPos(), 2) + Math.pow(e.get(i).yPos() - yPos(), 2));
             }
         }
 
 
         if (minLoc == -1) {
             return null;
         }
         return e.get(minLoc);
     }
 
     public boolean newGoal(Tile myTile) {
 
         if (foodRequired() > myTile.numFood) {
             return true;
         }
 
 
 
         return false;
     }
 
     public float foodRequired() {
 //        return ((1.0f - personality.mobility()) / 2 + (1.0f - personality.intelligence()) / 2) / 2;
         return ((1f - personality.intelligence()) / 2 - personality.hardiness() / 2) / 2;
 
     }
 
     public void convert() {
 
         float a = (float) Math.random() * personality.loyalty();
 
     }
 
     public String toString() {
         return personality.toString();
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
 
     Tile myTile() {
         return village.tileAt(this.xPos, this.yPos);
     }
 }
