 package com.me.Roguish.Controller;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import com.me.Roguish.Model.Entity;
 import com.me.Roguish.Model.HeroUnit;
 import com.me.Roguish.Model.MonsterUnit;
 import com.me.Roguish.Model.Level;
 
 public class LevelController {
 	private Level level;
 	private Entity hero;
 	public Random Dice = new Random();
 	public boolean gameOver = false;
 	public boolean gameWon = false;
 	private boolean ability1 = false;
 	private boolean ability2 = false;
 	private boolean ability3 = false;
 	private boolean ability4 = false;
 	private boolean ability5 = false;
 	// Index is the index in the level entity array of the current Unit whose turn it is 
 	private int index = 0;
 	
 	enum Keys {
 		LEFT, RIGHT, UP, DOWN, ONE, TWO, THREE, FOUR, FIVE
 	}
 	
 	static Map<Keys, Boolean> keys = new HashMap<LevelController.Keys, Boolean>();
 	static {
 		keys.put(Keys.LEFT, false);
 		keys.put(Keys.RIGHT, false);
 		keys.put(Keys.UP, false);
 		keys.put(Keys.DOWN, false);
 		keys.put(Keys.ONE, false);
 		keys.put(Keys.TWO, false);
 		keys.put(Keys.THREE, false);
 		keys.put(Keys.FOUR, false);
 		keys.put(Keys.FIVE, false);
 	};
 	
 	public LevelController(Level level){
 		this.level = level;
 		this.hero = level.getHero();
 		
 	}
 	
 	// Main update method
 	public void update(float delta){
 		// process
 		processInput();
 		// do something
 	}
 	
 	
 	private void processInput(){
 		if (keys.get(Keys.LEFT)){
 			
 		}
 		if (keys.get(Keys.RIGHT)){
 		}
 		if (keys.get(Keys.DOWN)){
 			
 		}
 		if (keys.get(Keys.UP)){
 		}
 	}
 	
 	// Turn Mechanics
 	
 	private void doHeroTurn(Keys direction) {
 		checkHeroTurn();
 		if(!doHeroAbility()){
 		switch(direction){
 		case UP:{
 			if(hero.getY() > 0 && tileOpen(hero.getX(), hero.getY() - 1)){
 				level.queue.getNext();
 				hero.movePosition(0, -1);
 				level.queue.add(hero);
 			}
 			break;
 		}
 		case DOWN:{
 			if(hero.getY() < 14 && tileOpen(hero.getX(), hero.getY() + 1)){
 				level.queue.getNext();
 				hero.movePosition(0, 1);
 				level.queue.add(hero);
 			}
 			break;
 		}
 		case LEFT:{
 			if(hero.getX() > 0 && tileOpen(hero.getX() - 1, hero.getY())){
 				level.queue.getNext();
 				hero.movePosition(-1, 0);
 				level.queue.add(hero);
 			}
 			break;
 		}
 		case RIGHT:{
 			if(hero.getX() < 9 && tileOpen(hero.getX() + 1, hero.getY())){
 				level.queue.getNext();
 				hero.movePosition(1 , 0);
 				level.queue.add(hero);
 			}
 			break;
 		}
 		}
 		}
 		
 	}
 	
 	private boolean doHeroAbility() {
 		if(!ability1 && !ability2 && !ability3 && !ability4 && !ability5) return false;
 		else{
 			// Check & call the abilities here.
 			return true;
 		}
 	}
	
 
 	public void doMonsterTurns(){
 		if (level.entities.get(index) instanceof MonsterUnit){
 			if(adjacentHero(level.entities.get(index).getX(), level.entities.get(index).getY())){
 				switch( ((MonsterUnit)level.entities.get(index)).getType()){
 					case MonsterUnit.RAT: doRatAttack(); break;
 					case MonsterUnit.BAT: doBatAttack(); break;
 				}
 				
 			}
 			else{
 				switch( ((MonsterUnit)level.entities.get(index)).getType()){
 					case MonsterUnit.RAT: doRatMovement(); break;
 					case MonsterUnit.BAT: doBatMovement(); break;
 				}	
 			}
 			if(level.entities.get(index).getAlive())
 				level.queue.add(level.entities.get(index));
 		}		
 	}
 	//Iterates over NPCs and performs their turns until it is the Hero's turn
 	public void checkHeroTurn(){
 		checkLoseConditions();
 		checkWinConditions();
 		
 		System.out.println(level.queue.turnCount);
 		while (!(level.queue.getEnt() instanceof HeroUnit)){
 			doMonsterTurns();
 			index = findId(level.queue.getNext());
 		}
 	}
 	
 	
 	//Location Utilities:
 	
 	public boolean heroOn(int x, int y){
 		return(hero.getX() == x && hero.getY() == y );
 	}
 	
 	// Returns true if the hero is adjacent to the inputted x,y coordinates
 	public boolean adjacentHero(int x, int y){
 		if ( (hero.getX() == x + 1 || hero.getX() == x -1)  && (hero.getY() == y))
 			return true;
 		if ( (hero.getY() == y + 1 || hero.getY() == y - 1) && (hero.getX() == x))
 			return true;
 		else return false;
 	}
 	
 	public void checkLoseConditions(){
 		if(level.getHero().getHP() <= 0) gameOver = true;
 	}
 	
 	public void checkWinConditions(){
 		for (Entity ent : level.getEntities()) {
 			if(ent.getId() == 1337 && heroOn(ent.getX(), ent.getY())) gameWon = true; 
 		}
 	}
 	
 	
 	public int findId(int x){
 		for(int i = 0; i < level.entities.size; i++ ){
 			if(x == level.entities.get(i).getId()) return i;
 		}
 		return -1;	
 	}
 	
 	
 	//Returns true if the tile at the x, y is open
 	public boolean tileOpen(int x, int y){
 		for (Entity ent : level.getEntities()) {
 			if (ent.getX() == x && ent.getY() == y && ent.getAlive()) return false;
 		}
 		if(level.tilePropCheck(x,y,"wall")) return false;
 		else return true;
 	}
 	
	public boolean inRange(Entity source, Entity target, int ability ){
		if( distance(source.getX(), source.getY(), target.getX(), target.getY())  <= level.ability.getRange(ability) ) return true;
		else return false;
	}
 	
	//Returns Manhattan distance between two points.
	private int distance(int x, int y, int x2, int y2) {
		return Math.abs(x-x2) + Math.abs(y-y2);
 	}

	//Unit AI
	
 	
 	public void doRatMovement(){
 		moveRandom(level.entities.get(index));
 	}
 	
 	public void doRatAttack() {
 		level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.BITE);	
 		System.out.println(level.getHero().getHP());
 	}
 	
 	public void doBatMovement(){
 		moveEntityTowardHero(level.entities.get(index));
 	}
 	
 	public void doBatAttack(){
 		level.ability.activate(level.entities.get(index), level.getHero(), AbilityController.STRONGBITE);
 	}
 	
 	public void moveEntityTowardHero(Entity mover){
 		int deltaX = mover.getX() - level.getHero().getX();
 		int deltaY = mover.getY() - level.getHero().getY();
 		if (deltaX > 0  && deltaY == 0){
 			if(tileOpen(mover.getX() - 1, mover.getY()))
 				mover.movePosition(-1, 0);
 			// Mover is to the right of the hero in the same y.
 		}
 		else if (deltaX < 0  && deltaY == 0){
 			if(tileOpen(mover.getX() + 1, mover.getY()))
 				mover.movePosition(1, 0);
 			// Mover is to the left of the hero in the same y.
 		}
 		else if (deltaY > 0 && deltaX == 0){
 			if(tileOpen(mover.getX(), mover.getY() - 1))
 				mover.movePosition(0, -1);
 			// Mover is below the hero.
 		}
 		else if (deltaY < 0 && deltaX == 0){
 			if(tileOpen(mover.getX(), mover.getY() + 1))
 				mover.movePosition(0, 1);
 			// Mover is above the hero.
 		}
 		else if (deltaX > 0 && deltaY > 0){
 			if(tileOpen(mover.getX(), mover.getY() + 1))
 				mover.movePosition(0, 1);
			else if(tileOpen(mover.getX() - 1, mover.getY()))
 				mover.movePosition(-1, 0);
 			//mover is Right && Below
 		}
 		else if (deltaX < 0 && deltaY > 0){
 			if(tileOpen(mover.getX(), mover.getY() - 1))
 				mover.movePosition(0, -1);
			else if(tileOpen(mover.getX() + 1, mover.getY()))
 				mover.movePosition(1, 0);
 			//mover is Left && below
 		}
 		else if (deltaX < 0 && deltaY < 0){
 			if(tileOpen(mover.getX(), mover.getY() + 1))
 				mover.movePosition(0, 1);
			else if(tileOpen(mover.getX() + 1, mover.getY()))
 				mover.movePosition(1, 0);
 			//mover is Left and Above
 		}
 		else if (deltaX > 0 && deltaY < 0){
 			if(tileOpen(mover.getX(), mover.getY() + 1))
 				mover.movePosition(0, 1);
			else if(tileOpen(mover.getX() -1, mover.getY()))
 				mover.movePosition(-1, 0);
 			//mover is Right and Above
 		}
 		
 	}
 	
 	public void moveRandom(Entity mover){
 		boolean moved = false;
 		int count = 0;
 		do{
 			switch(Dice.nextInt(4)){
 				case 0:{
 					if(tileOpen(mover.getX(), mover.getY() - 1)){
 						mover.movePosition(0, -1);
 						moved = true;
 					}
 					break;
 				}
 				case 1:{
 					if(tileOpen(mover.getX(), mover.getY() + 1)){
 						mover.movePosition(0, 1);
 						moved = true;
 					}
 					break;
 				}
 				case 2:{
 					if(tileOpen(mover.getX() - 1, mover.getY())){
 						mover.movePosition(-1, 0);
 						moved = true;
 					}
 					break;
 				}
 				case 3:{
 					if(tileOpen(mover.getX() + 1, mover.getY())){
 						mover.movePosition(1, 0);
 						moved = true;
 					}
 					break;
 				}
 				default: break;
 			}
 			count++;
 			
 		}while(!moved && count < 20);
 		
 	}
 	
 	// Keypresses
 	public void leftPressed() {
 		keys.get(keys.put(Keys.LEFT, true));
 	}
 	
 	public void leftReleased() {
 		keys.get(keys.put(Keys.LEFT, false));
 		doHeroTurn(Keys.LEFT);
 	}
 
 	public void rightPressed() {
 		keys.get(keys.put(Keys.RIGHT, true));
 	}
 	
 	public void rightReleased() {
 		keys.get(keys.put(Keys.RIGHT, false));
 		doHeroTurn(Keys.RIGHT);
 	}
 	
 	public void upPressed() {
 		keys.get(keys.put(Keys.UP, true));
 	}
 	
 	public void upReleased() {
 		keys.get(keys.put(Keys.UP, false));
 		doHeroTurn(Keys.UP);
 	}
 	
 	public void downPressed() {
 		keys.get(keys.put(Keys.DOWN, true));
 	}
 	
 	public void downReleased() {
 		keys.get(keys.put(Keys.DOWN, false));
 		doHeroTurn(Keys.DOWN);
 	}
 	
 	public void onePressed(){
 		keys.get(keys.put(Keys.ONE, true));
 	}
 	public void oneReleased() {
 		keys.get(keys.put(Keys.ONE, false));
 		ability1 = true;
 	}
 	public void twoPressed(){
 		keys.get(keys.put(Keys.TWO, true));
 	}
 	public void twoReleased() {
 		keys.get(keys.put(Keys.TWO, false));
 		ability2 = true;
 	}
 	public void threePressed(){
 		keys.get(keys.put(Keys.THREE, true));
 	}
 	public void threeReleased() {
 		keys.get(keys.put(Keys.THREE, false));
 		ability3 = true;
 	}
 	public void fourPressed(){
 		keys.get(keys.put(Keys.FOUR, true));
 	}
 	public void fourReleased() {
 		keys.get(keys.put(Keys.FOUR, false));
 		ability4 = true;
 	}
 	
 	public void fivePressed(){
 		keys.get(keys.put(Keys.FIVE, true));
 	}
 	public void fiveReleased() {
 		keys.get(keys.put(Keys.FIVE, false));
 		ability5 = true;
 	}
 	
 }
