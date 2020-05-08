 //This class will contain a number of enemy waves and a particular map
 
 package com.teamBasics.CollegeTD;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import com.teamBasics.framework.Graphics;
 import com.teamBasics.CollegeTD.SampleGame;
 
 public class Level {
 	//GamePlay Level Info
 	private ArrayList<EnemyWave> waves = new ArrayList<EnemyWave>();;
 	//private ArrayList<BorderTile> tilearrayBorder = new ArrayList<BorderTile>();
 	private ArrayList<PathTile> tilearrayPath = new ArrayList<PathTile>();
 	private ArrayList<Tower> towers = new ArrayList<Tower>();
 	private ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
 	
 	//User Info
 	private int livesLeft = 20;
 	private int cash = 200;
 	private int score = 0;
 	
 	//Will later be used to tell which map to load
 	private int level_number;
 	private boolean levelComplete;
 	
 	//Later in the constructor this class can just be given the level number
 	//That way each level can have a different map
 	public Level(int level_number) {		
 		//loadMapBorder();
 		loadMapPath();
 		loadLevel(level_number);	
 		levelComplete = false;
 	}
 	
 	//Adds a wave to the level
 	public void addWave(EnemyWave wave) {
 		waves.add(wave);
 	}
 	
 	//Update to next level.
 	public void loadLevel(int level_number){
 		
 		if( level_number == 1 ){
 			EnemyWave wave1 = new EnemyWave();
 			EnemyGroup group1 = new EnemyGroup(400, 12);
 			EnemyGroup group2 = new EnemyGroup(800, 25);
 			EnemyGroup group3 = new EnemyGroup(1200, 25);
 
 			// Add enemies to group
 			for(int i=0; i<10; i++) {
 				AssignmentEnemy a = new AssignmentEnemy(tilearrayPath);
 				group1.addEnemy(a);
 			}
 			for(int i=0; i<10; i++) {
 				TeacherEnemy t = new TeacherEnemy(tilearrayPath);
 				group2.addEnemy(t);
 			}
 			for(int i=0; i<10; i++) {
 				GeorgiaBulldogEnemy g = new GeorgiaBulldogEnemy(tilearrayPath);
 				group3.addEnemy(g);
 			}
 		
 			// Add groups to wave
 			if(group1 != null && group2 != null && group3 != null) {
 				wave1.addEnemyGroup(group1);
 				wave1.addEnemyGroup(group2);
 				wave1.addEnemyGroup(group3);
 				waves.add(wave1);
 			}
 		} // End Level_1
 
 		
 		if( level_number == 2 ){
 			EnemyWave wave1 = new EnemyWave();
 			EnemyGroup group1 = new EnemyGroup(400, 12);
 			EnemyGroup group2 = new EnemyGroup(800, 25);
 			EnemyGroup group3 = new EnemyGroup(1200, 25);
 
 			// Add enemies to group
 			for(int i=0; i<10; i++) {
 				AssignmentEnemy a = new AssignmentEnemy(tilearrayPath);
 				group1.addEnemy(a);
 			}
 			for(int i=0; i<10; i++) {
 				TeacherEnemy t = new TeacherEnemy(tilearrayPath);
 				group2.addEnemy(t);
 			}
 			for(int i=0; i<10; i++) {
 				GeorgiaBulldogEnemy g = new GeorgiaBulldogEnemy(tilearrayPath);
 				group3.addEnemy(g);
 			}
 		
 			// Add groups to wave
 			if(group1 != null && group2 != null && group3 != null) {
 				wave1.addEnemyGroup(group1);
 				wave1.addEnemyGroup(group2);
 				wave1.addEnemyGroup(group3);
 				waves.add(wave1);
 			}
 		} // End Level_2
 
 	}
 	
 	//Update 1 wave of the level at a time. Update the first wave you
 	//come to that is not complete.
 	public void update() {
 		//update enemies
 		EnemyWave waveToUpdate = null;
 		for(int i=0; i<waves.size(); i++) {
 			// Enemy waves not complete
 			if(!(waves.get(i).isComplete())) {
 				waveToUpdate = waves.get(i);
 				//break;
 			}
 			// Enemy waves complete
 			else if( i == waves.size()-1 ){
 				SampleGame.setCurrentLevel(level_number+1);
 				SampleGame.loadMapPath();
 				setLevelComplete(true);
 			}
 		}
 		//update towers
 		for(int i=0; i<towers.size(); i++) {
 			Projectile projectile = towers.get(i).update(waveToUpdate);
 			if(projectile != null) {
 				projectiles.add(projectile);
 			}
 		}
 		//update projectiles
 		for(int i=0; i<projectiles.size(); i++) {
 			if(!(projectiles.get(i).isVisible())) {
 				projectiles.remove(i);
 			}
 			else {
 				projectiles.get(i).update();
 			}
 		}
 		if(waveToUpdate != null) {
 			waveToUpdate.update();
 		}
 		else {
 			//give user some kind of screen that they won the level
 		}
 	}
 	
 	public void draw(Graphics g) {
 		//drawing of map happens in gameScreen class
 		int lives_lost = 0;
 		//draw enemies
 		for(int i=0; i<waves.size(); i++) {
 			lives_lost += waves.get(i).draw(g);
 		}
 		//set up for when towers get animations
 		for(int i=0; i<towers.size(); i++) {
 			towers.get(i).draw(g);
 		}
 		//draw projectiles
 		for(int i=0; i<projectiles.size(); i++) {
 			projectiles.get(i).draw(g);
 		}
 		livesLeft -= lives_lost;
 		if(livesLeft < 0 ) {
 			livesLeft = 0;
 		}
 	}
 	
 	//Updates the cash when an update is called in GameScreen
 	
 	int num=0;
 	public void checkCash(){
 		ArrayList<Enemy> enemies;
		ArrayList<Enemy> totalenemies;
 		int size=0;
 		for(int i=0; i<waves.size(); i++) {
 			enemies = waves.get(i).killedEnemies();
			totalenemies = waves.get(i).allEnemies();
 			size=enemies.size();
			if(size!=num && isKama(totalenemies)!=true){
 				num=size;
 				cash+=5;
 				score+=20;
 			}
 		}
 	}
 	
 	public boolean isKama(ArrayList<Enemy> totalenemies){
 		for(int j=0; j<totalenemies.size();j++){
 			if(totalenemies.get(j).isKamakazi()){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	//Will be used later on to load the map for each level
 	// Load map border
 	/*public void loadMapBorder() {
 		ArrayList<String> lines = new ArrayList<String>();
 		int width = 0;
 		int height = 0;
 
 		Scanner scanner = new Scanner(SampleGame.mapBorder);
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 
 			// no more lines to read
 			if (line == null) {
 				break;
 			}
 
 			if (!line.startsWith("!")) {
 				lines.add(line);
 				width = Math.max(width, line.length());
 			}
 		}
 		height = lines.size();
 
 		// j = y value
 		for (int j = 0; j < height; j++) {
 			String line = (String) lines.get(j);
 			for (int i = 0; i < width; i++) {
 				if (i < line.length()) {
 					char ch = line.charAt(i);
 					BorderTile t = new BorderTile(i, j, Character.getNumericValue(ch));
 					tilearrayBorder.add(t);
 				}
 			}
 		}
 	}*/
 
 
 	// Load walkway-path
 	public void loadMapPath() {
 		ArrayList<String> lines = new ArrayList<String>();
 		int width = 0;
 		int height = 0;
 
 		Scanner scanner = new Scanner(SampleGame.mapPath);
 		while (scanner.hasNextLine()) {
 			String line = scanner.nextLine();
 			
 			// no more lines to read
 			if (line == null) {
 				break;
 			}
 
 			if (!line.startsWith("!")) {
 				lines.add(line);
 				width = Math.max(width, line.length());
 			}
 		}
 		height = lines.size();
 
 		// j = y value
 		for (int j = 0; j < height; j++) {
 			String line = (String) lines.get(j);
 			for (int i = 0; i < width; i++) {
 
 				if (i < line.length()) {
 					char ch = line.charAt(i);
 					PathTile t = new PathTile(i, j, ch);
 					tilearrayPath.add(t);
 				}
 			}
 		}
 
 	}
 	
 	
 	/*public void paintBorderTiles(Graphics g) {
 		for (int i = 0; i < tilearrayBorder.size(); i++) {
 			BorderTile t = (BorderTile) tilearrayBorder.get(i);
 			if (t.type != 0) {
 				g.drawImage(t.getTileImage(), t.getTileX(), t.getTileY());
 			}
 		}
 	}*/
 
 	public void paintPathTiles(Graphics g) {
 		for (int i = 0; i < tilearrayPath.size(); i++) {
 			PathTile t = (PathTile) tilearrayPath.get(i);
 			if (t.type2 != 'v') {
 				g.drawImage(t.getTileImage(), t.getTileX(), t.getTileY());
 			}
 		}
 	}
 
 
 	/*public void updateBorderTiles() {
 
 		for (int i = 0; i < tilearrayBorder.size(); i++) {
 			BorderTile t = (BorderTile) tilearrayBorder.get(i);
 			t.update();
 		}
 
 	}*/
 	
 	public void updatePathTiles() {
 		for (int i = 0; i < tilearrayPath.size(); i++) {
 			PathTile t = (PathTile) tilearrayPath.get(i);
 			t.update();
 		}
 
 	}
 
 	public ArrayList<EnemyWave> getLevel() {
 		return waves;
 	}
 
 	public void setLevel(ArrayList<EnemyWave> level) {
 		this.waves = level;
 	}
 
 	/*public ArrayList<BorderTile> getTilearrayBorder() {
 		return tilearrayBorder;
 	}
 
 	public void setTilearrayBorder(ArrayList<BorderTile> tilearrayBorder) {
 		this.tilearrayBorder = tilearrayBorder;
 	}*/
 
 	public ArrayList<PathTile> getTilearrayPath() {
 		return tilearrayPath;
 	}
 
 	public void setTilearrayPath(ArrayList<PathTile> tilearrayPath) {
 		this.tilearrayPath = tilearrayPath;
 	}
 
 	public int getLivesLeft() {
 		return livesLeft;
 	}
 
 	public void setLivesLeft(int livesLeft) {
 		this.livesLeft = livesLeft;
 	}
 
 	public ArrayList<Tower> getTowers() {
 		return towers;
 	}
 	
 	public void setTowers(ArrayList<Tower> towers) {
 		this.towers = towers;
 	}
 	
 	public void addTower(Tower addTower) {
 		this.towers.add(addTower);
 	}
 
 	public ArrayList<Projectile> getProjectiles() {
 		return projectiles;
 	}
 
 	public void setProjectiles(ArrayList<Projectile> projectiles) {
 		this.projectiles = projectiles;
 	}
 
 	public int getCash() {
 		return cash;
 	}
 
 	public void setCash(int cash) {
 		this.cash = cash;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void setScore(int score) {
 		this.score = score;
 	}
 
 	public int getLevel_number() {
 		return level_number;
 	}
 
 	public void setLevel_number(int level_number) {
 		this.level_number = level_number;
 	}
 
 	public boolean isLevelComplete() {
 		return levelComplete;
 	}
 
 	public void setLevelComplete(boolean levelComplete) {
 		this.levelComplete = levelComplete;
 	}
 	
 }
