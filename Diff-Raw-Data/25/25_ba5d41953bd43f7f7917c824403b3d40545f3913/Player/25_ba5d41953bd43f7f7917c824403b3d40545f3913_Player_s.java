 package com.race.entity;
 
 import org.newdawn.slick.Graphics;
 
 import com.race.main.ButtonHandler;
 
 public class Player {
 	/** Starting positions of the car */
 	int x = 200;
 	int y = 200;
 	/** The car the player is currently using */
 	Car selectedCar = null;
 
 	public Player() {
 		selectedCar = new RedCar(x, y);
 	}
 
 	/**
 	 * Updates the car, and handles the keys.
 	 * 
 	 * @param deltaT
 	 *            The time that has passed
 	 */
 	public void update(float deltaT) {
 		if (selectedCar != null) {
 			selectedCar.updateAll(deltaT);
 			selectedCar
 					.setMoving(ButtonHandler.getButtonHandler().getForward());
 			if (ButtonHandler.getButtonHandler().getRight()) {
 				selectedCar.changeDirection(0.2f, deltaT);
 			}
 			if (ButtonHandler.getButtonHandler().getLeft()) {
 				selectedCar.changeDirection(-0.2f, deltaT);
 			}
 		}
 	}
 
 	/**
 	 * Sets the start position of all the cars
 	 * 
 	 * @param x
 	 *            The x position of the car.
 	 * @param y
 	 *            The y position of the car.
 	 */
 	public void setStartPosition(int x, int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Draws the car
 	 * 
 	 * @param g
 	 *            Just pass g here.
 	 */
 	public void draw(Graphics g) {
 		if (selectedCar != null) {
 			selectedCar.draw(g);
 		}
 	}
 
 }
