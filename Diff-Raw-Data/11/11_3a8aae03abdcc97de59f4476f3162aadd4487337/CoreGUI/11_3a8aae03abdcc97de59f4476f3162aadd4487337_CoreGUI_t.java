 package gui;
 
 import java.util.LinkedList;
 
 import core.DefenseCore;
 import creature.Creature;
 import creature.Enemy;
 import creature.SpeedCreature;
 import processing.core.PApplet;
 import projectile.Bullet;
 import projectile.Projectile;
 import tower.NormalTower;
 import tower.Tower;
 import world.Field;
 import world.World;
 
 public class CoreGUI extends PApplet{
 	
 	/*
 	 * 			Variables
 	 *************************************************************/
 	private DefenseCore core;
 	private String modus;
 	private int fieldSize;
 	private LinkedList<Field> path;
 	private String warning;
 	/*
 	 * 			Construcktors
 	 *************************************************************/
 	public void setup() {
 
 		this.fieldSize = 20;
 		path = new LinkedList<>();
 		core = new DefenseCore(20, 20);
 		this.modus = "-";
 		this.warning = "-";
 		
 		size(650, 425);
 		background(255);
 		frameRate(30);
 	}
 	
 	/*
 	 * 			Methods
 	 *************************************************************/
 	
 	/**
 	 * This method is used to draw the world as a 2D Matrix
 	 */
 	private void drawWorld() {
 		for (int y = 0; y < core.getWorld().getWorld()[0].length; y++) {
 			for (int x = 0; x < core.getWorld().getWorld().length; x++) {
 				if (core.getWorld().getField(x, y).getContain() == Field.CONTAIN_SPAWN) {
 					fill(255, 0, 0);
 				} else if (core.getWorld().getWorld()[x][y].getContain() == Field.CONTAIN_EXIT) {
 					fill(0, 255, 0);
 				} else if (core.getWorld().getWorld()[x][y].getContain() == Field.CONTAIN_CHECKPOINT) {
 					fill(0, 0, 255);
 				} else if (core.getWorld().getWorld()[x][y].getContain() == Field.CONTAIN_BLOCK) {
 					fill(0, 0, 0);
 				} else if (path != null && path.contains(core.getWorld().getField(x, y))) {
 					fill(200, 75, 0);
 				} else {
 					fill(255, 255, 255);
 				}
 				rect(x * this.fieldSize, y * this.fieldSize, this.fieldSize, this.fieldSize);
 			}
 		}
 	}
 	
 	private void addNormalTower(int x, int y) {
 		NormalTower c = new NormalTower(x, y);
 		core.getTowers().add(c);
 	}
 	
 	private void addCreature() {
 		Field spawn = core.getWorld().getSpawn();
 		Creature c = new Creature(spawn.getX(), spawn.getY(), (LinkedList<Field>) path.clone(), 0);
 		core.getEnemys().add(c);
 	}
 
 	private void addSpeedCreature() {
 		Field spawn = core.getWorld().getSpawn();
 		SpeedCreature c = new SpeedCreature(spawn.getX(), spawn.getY(), (LinkedList<Field>) path.clone(), 0);
 		core.getEnemys().add(c);
 	}
 	
 	public void mousePressed() {
 		int x = mouseX/this.fieldSize;
 		int y = mouseY/this.fieldSize;
 		
 		if(this.modus.equals("Spawn Placer")) {
 			//Remove the old spawn
 			if(core.getWorld().getSpawn() != null) core.getWorld().getSpawn().setContain(Field.CONTAIN_NOTHING);
 		
 			core.getWorld().setField(x, y, Field.CONTAIN_SPAWN);
 		}else if(this.modus.equals("Exit Placer")) {
 			//Remove the old spawn
 			if(core.getWorld().getExit() != null) core.getWorld().getExit().setContain(Field.CONTAIN_NOTHING);
 		
 			core.getWorld().setField(x, y, Field.CONTAIN_EXIT);
 		}else if(this.modus.equals("Block Placer")) {
 			if(core.getWorld().getField(x, y).getContain() == Field.CONTAIN_BLOCK) {
 				core.getWorld().setField(x, y, Field.CONTAIN_NOTHING);
 			}else{
 				core.getWorld().setField(x, y, Field.CONTAIN_BLOCK);
 				
 				if(aStar() == null) core.getWorld().setField(x, y, Field.CONTAIN_NOTHING);		
 			}
 		}else if(this.modus.equals("Tower Builder")) {
 			if(core.getWorld().getField(x, y).getContain() == Field.CONTAIN_PLAYER) {
 				core.removeTower(x, y);
 				core.getWorld().setField(x, y, Field.CONTAIN_NOTHING);
 			}else{
 				this.addNormalTower(x, y);
 				core.getWorld().setField(x, y, Field.CONTAIN_BLOCK);
 			}
 		}
 	}
 	
 	private LinkedList<Field> aStar() {
 		Field start = core.getWorld().getSpawn();
 		Field end = core.getWorld().getExit();
 		
 		return core.getWorld().aStar(new Field(Field.CONTAIN_SPAWN,start.getX(),start.getY()), new Field(Field.CONTAIN_EXIT,end.getX(),end.getY()));
 		
 	}
 	
 	/**
 	 * This Method is override from PApplet and is used for
 	 * key events. It is triggered every time a user pressed
 	 * a key.
 	 */
 	public void keyPressed() {
 		switch(keyCode) {
 			default:
 				this.modus = keyCode+"";
 				break;
 			case 83:	//Spawn
 				this.modus = "Spawn Placer";		
 				break;
 			case 69:	//Exit
 				this.modus = "Exit Placer";
 				break;
 			case 66:	//Block
 				this.modus = "Block Placer";
 				
 				break;
 			case 80:	//build Path
 				this.modus = "Build Path";
 				path = aStar();
 				break;
 			case 32:	//Mob Spawn
 				this.modus = "Mob Spawn";
 				this.addCreature();
 				break;
 			case 17:	//Speed Mob Spawn
 				this.modus = "Speed Mob Spawn";
 				this.addSpeedCreature();
 				break;
 			case 84:	//Build Tower
 				this.modus = "Tower Builder";
 				break;
 			case 71:	//Test Projectile
 				this.modus = "Test Projectile";
 				break;
 		}
 	}
 	
 	private void drawEnemy() {
 		try {
 			for(Enemy e : core.getEnemys()) {
 				fill(255,128,0);
 				rect(e.getAbsX()-5, e.getAbsY()-5,10,10);
 			}
 		}catch(Exception e){}
 	}
 	
 	private void drawTower() {
 		try {
 			for(Tower t : core.getTowers()) {
 				stroke(0);
 				fill(0,0,255);
 				rect(t.getAbsX()-5, t.getAbsY()-5,10,10);
 				for(Enemy e : t.getNearEnemys()) {
					if(t.getTarget() != null && e.equals(t.getTarget())) stroke(255,0,0);
 					else stroke(0,255,0);
 					line(t.getAbsX(), t.getAbsY(), e.getAbsX(), e.getAbsY());
 				}
 				fill(0,0,255,5);
 				ellipse(t.getAbsX(), t.getAbsY(), t.getRadius()*2, t.getRadius()*2);
 			}
 		}catch(Exception e){}
 	}
 	
 	private void drawProjectile() {
 		try {
 			for(Projectile p : core.getProjectiles()) {
 				ellipse(p.getAbsX(), p.getAbsY(), 3, 3);
 			}
 		}catch(Exception e) {}
 	}
 	
 	/**
 	 * This method draws every new frame.
 	 */
 	public void draw() {
 		background(255);
 		stroke(0);
 		
 		//Zeichne die Welt
 		drawWorld();
 		
 		//Zeichne die Gegner
 		drawEnemy();
 		
 		//Zeichne die Trme
 		drawTower();
 		
 		//Zeichne Kugeln
 		drawProjectile();
 		
 		//Gebe die Hilfe aus
 		textSize(14);
 		fill(0, 0, 0);
 		text("Hilfe", 410, 25);
 		text(" | Modus: "+this.modus, 475, 25);
 		
 		textSize(10);
 		text("Key s_____________Spawn Placer", 410, 40);
 		text("Key e_____________Exit Placer", 410, 55);
 		text("Key b_____________Block Placer", 410, 70);
 		text("Key t_____________Tower Build Mode", 410, 85);
 		text("Key g_____________Test Projectile",410,100);
 		text("Key whitespace___Spawn Mob", 410, 100+15);
 		text("Key STRG_________Spawn Speed Mob", 410, 100+30);
 		
 		//Info Text bottom
 		textSize(14);
 		fill(0, 0, 0);
 		text("Enemys: "+core.getEnemys().size(), 10, 415);
 		text("Warning: "+this.warning, 100, 415);
 	}
 }
