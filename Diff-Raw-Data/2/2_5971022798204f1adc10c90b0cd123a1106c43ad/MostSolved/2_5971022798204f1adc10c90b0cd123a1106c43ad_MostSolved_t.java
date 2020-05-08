 package com.github.rocketsurgery;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class MostSolved extends Gameplay {
 	
 	@Override
 	public int getID() {
 		return UncrossTheWires.MOST_SOLVED;
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		super.enter(gc, sbg);
 		
		Timer.reset();
		
 		// initialize variables
 		reset();
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		super.update(gc, sbg, delta);
 		
 		Timer.decrease(delta);
 		
 		if (Timer.isDone()) {
 			sbg.enterState(UncrossTheWires.SCORE_MENU);
 		}
 		
 		// if no wires intersect end Level
 		if (levelComplete) {
 				Score.update(1);
 				reset();
 		}
 	}
 	
 	private void reset() {
 		Level.generateLevel(Level.selectedDifficulty, Gameplay.gameContainer);
 		winDelay = 500;
 		levelComplete = false;
 	}
 }
