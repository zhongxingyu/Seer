 package com.gravitoids.bean;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import com.gravitoids.helper.WrappingHelper;
 import com.gravitoids.panel.GravitoidsPanel;
 
 /**
  * Copyright (c) 2008, Michael Cook
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the Michael Cook nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY Michael Cook ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL Michael Cook BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 public class IntelligentGravitoidsShip extends GravitoidsAutonomousObject {
 	// The chances of a mutation, change rate of a small mutation
 	
 	private static final double MUTATION_RATE = 0.2;
 	private static final double MINI_MUTATION = 0.2;
 	
 	// Whether we want to draw the insight into why this object is behaving like it does
 	
 	private static boolean drawMotiviation = true;
 	private static boolean drawThrust = true;
 	private static boolean drawGravitationalPull = true;
 	
 	// The object(s) that are motivating us
 	
 	private GravitoidsObject mainMotivation = null;
 	private GravitoidsObject secondMotivation = null;
 	
 	// How big our brain is
 	
 	private static final int BRAIN_SIZE = 15;
 	
 	// What each element in the brain represents
 	
 	private static final int THRUST_SPLIT = 0;
 	private static final int MINIMUM_FEAR = 1;
 	
 	private static final int OBJECT_GRAVITY_A_TERM = 2;
 	private static final int OBJECT_GRAVITY_B_TERM = 3;
 	private static final int OBJECT_GRAVITY_C_TERM = 4;
 	
 	private static final int OBJECT_SIZE_A_TERM = 5;
 	private static final int OBJECT_SIZE_B_TERM = 6;
 	private static final int OBJECT_SIZE_C_TERM = 7;
 
 	private static final int OBJECT_MOVEABLE_FACTOR = 8;
 	
 	private static final int CARE_TO_THRUST_A_TERM = 9;
 	private static final int CARE_TO_THRUST_B_TERM = 10;
 	private static final int CARE_TO_THRUST_C_TERM = 11;
 	
 	private static final int OBJECT_SPEED_A_TERM = 12;
 	private static final int OBJECT_SPEED_B_TERM = 13;
 	private static final int OBJECT_SPEED_C_TERM = 14;
 	/*
 	private static final int OBJECT_DIRECTION_A_TERM = 15;
 	private static final int OBJECT_DIRECTION_B_TERM = 16;
 	private static final int OBJECT_DIRECTION_C_TERM = 17;
 	*/
 	private double brain[] = null; 
 	private int evolutionDirection[] = null;	// Which way the brian term is moving
 	
 	private String name = null;
 
 	private long age = 0L;
 	
 	public void resetObject() {
 		super.resetObject();
 		
 		mainMotivation = null;
 		secondMotivation = null;
 	}
 	
 	public IntelligentGravitoidsShip() {
 		// Give us a random brain
 		
 		brain = new double[BRAIN_SIZE];
 		evolutionDirection = new int[BRAIN_SIZE];
 		
 		for (int i = 0; i < BRAIN_SIZE; i++) {
 			brain[i] = Math.random() * 2.0 - 1.0;
 			evolutionDirection[i] = 0;
 		}
 	}
 	
 	public IntelligentGravitoidsShip(IntelligentGravitoidsShip source) {
 		// Copy our brain
 		
 		brain = new double[BRAIN_SIZE];
 		evolutionDirection = new int[BRAIN_SIZE];
 		
 		for (int i = 0; i < BRAIN_SIZE; i++) {
 			brain[i] = source.brain[i];
 			evolutionDirection[i] = source.evolutionDirection[i];
 		}
 	}
 
 	public double[] getBrain() {
 		return brain;
 	}
 	
 	public int[] getEvolutionDirection() {
 		return evolutionDirection;
 	}
 	
 	public static boolean isDrawThrust() {
 		return drawThrust;
 	}
 	
 	public static void setDrawThrust(boolean drawThrust) {
 		IntelligentGravitoidsShip.drawThrust = drawThrust;
 	}
 	
 	public static boolean isDrawGravitationalPull() {
 		return drawGravitationalPull;
 	}
 	
 	public static void setDrawGravitationalPull(boolean drawGravity) {
 		IntelligentGravitoidsShip.drawGravitationalPull = drawGravity;
 	}
 	
 	public static boolean isDrawMotivation() {
 		return drawMotiviation;
 	}
 	
 	public static void setDrawMotivation(boolean drawMotivation) {
 		IntelligentGravitoidsShip.drawMotiviation = drawMotivation;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public void incrementAge() {
 		age++;
 	}
 	
 	public long getAge() {
 		return age;
 	}
 	
 	public void clearAge() {
 		age = 0L;
 	}
 	
 	public double calculateFromBrain(double input, int aTerm, int bTerm, int cTerm) {
 		return input * input * brain[aTerm] +
 				input * brain[bTerm] +
 				brain[cTerm];
 	}
 	
 	private double fixSpeedDelta(double speedDelta, double ourCoord, double theirCoord, double maxValue) {
 		double distance = ourCoord - theirCoord;
 		
 		if (distance > (maxValue / 2.0)) {
 			if (ourCoord < theirCoord) {
 				theirCoord -= maxValue;
 			} else {
 				theirCoord += maxValue;
 			}
 		}
 		
 		if (ourCoord < theirCoord) {
 			// We are to their "left"
 			
 			if (speedDelta > 0) {
 				// This will move us TO them. Wrong.
 				speedDelta *= -1.0;
 			}
 		} else {
 			// We are to their "right"
 			
 			if (speedDelta < 0) {
 				// This will move us TO them. Wrong
 				speedDelta *= -1.0;
 			}
 		}
 		
 		return speedDelta;
 	}
 	
 	private void asessObject(GravitoidsObject object, double[] weights, double[] headings, double[] distances, int index) {
 		// Figure out our distance from it, used in the gravity calculation
 		
 		double distance = WrappingHelper.calculateDistanceToObject(this, object); 
 		
 		// Now handle it's mass to get gravity
 		
 		double gravity = calculateFromBrain(distance / object.getMass(), OBJECT_GRAVITY_A_TERM, OBJECT_GRAVITY_B_TERM, OBJECT_GRAVITY_C_TERM);
 		
 		// Now the direction
 		
 		double direction = WrappingHelper.calculateDirectionToObject(this, object);
 		
 		//direction = calculateFromBrain(direction, OBJECT_DIRECTION_A_TERM, OBJECT_DIRECTION_B_TERM, OBJECT_DIRECTION_C_TERM);
 		
 		direction = direction + Math.PI;	// Make the direction point away from the object
 		
 		while (direction > Math.PI) {	// Clamp it
 			direction -= 2.0 * Math.PI;
 		}
 		
 		while (direction < Math.PI) {
 			direction += 2.0 * Math.PI;
 		} 
 		
 		// The object's speed relative to us
 		
 		double xSpeedDelta = getXSpeed() - object.getXSpeed();
 		double ySpeedDelta = getYSpeed() - object.getYSpeed();
 		
 		xSpeedDelta = fixSpeedDelta(xSpeedDelta, getXPosition(), object.getXPosition(), GravitoidsPanel.PANEL_WIDTH);
 		ySpeedDelta = fixSpeedDelta(ySpeedDelta, getYPosition(), object.getYPosition(), GravitoidsPanel.PANEL_HEIGHT);
 		/*
 		if ((xSpeedDelta < 0.0) && (getXPosition() > object.getXPosition())) {	// Copensate for relative positions
 			xSpeedDelta *= -1.0;
 		}
 		
 		if ((ySpeedDelta < 0.0) && (getYPosition() > object.getYPosition())) {
 			ySpeedDelta *= -1.0;
 		}
 		*/
 		xSpeedDelta = xSpeedDelta < 0.0 ? 0.0 : xSpeedDelta;	// Mark we don't care about negative speeds
 		ySpeedDelta = ySpeedDelta < 0.0 ? 0.0 : ySpeedDelta;
 		
 		double speed = Math.sqrt(Math.pow(xSpeedDelta, 2.0) + Math.pow(ySpeedDelta, 2.0));
 		
 		speed = calculateFromBrain(speed, OBJECT_SPEED_A_TERM, OBJECT_SPEED_B_TERM, OBJECT_SPEED_C_TERM);
 		
 		// Now the object's size
 		
 		double size = calculateFromBrain(object.getRadius(), OBJECT_SIZE_A_TERM, OBJECT_SIZE_B_TERM, OBJECT_SIZE_C_TERM);
 		
 		// Now moveability
 		
 		double moveability = object.isMoveable() ? brain[OBJECT_MOVEABLE_FACTOR] : 0.0;
 		
 		// Store things we need to for this object
 		
 		weights[index] = moveability + gravity + size + speed;
 		headings[index] = direction;
 		distances[index] = distance;
 	}
 	
 	public void prepareMove(GravitoidsObject[] theirObjects) {
 		// Prepare arrays to hold stuff we'll be messing with, and how much we care
 		
 		GravitoidsObject[] objects = new GravitoidsObject[theirObjects.length];
 		
 		double[] weights = new double[objects.length];	// How much we care
 		double[] headings = new double[objects.length];	// Which heading to go in
 		double[] distances = new double[objects.length];	// How far away it is
 		
 		// Assess each object in the universe we are told about
 		
 		for (int i = 0; i < objects.length; i++) {
 			// Get what we're working with
 			
 			GravitoidsObject object = theirObjects[i];
 			
 			objects[i] = object;
 			
 			asessObject(object, weights, headings, distances, i);
 		}
 		
 		// Now that we have all that, we need to find what we care about most
 		
 		mainMotivation = null;		// Just to make sure we don't have stuff yet
 		secondMotivation = null;
 		
 		int importantIndex = -1;			// Thing we are most scared of
 		int secondImportantIndex = -1;		// Thing we are second most scared of
 		
 		double mostImportant = Double.NEGATIVE_INFINITY;		// Known value
 		double secondMostImportant = Double.NEGATIVE_INFINITY;	// Known value
 		
 		double minimumFear = brain[MINIMUM_FEAR] + 1.0 * 10.0;	// 10.0 gives us a range
 
 		for (int i = 0; i < weights.length; i++) {
 			if ((weights[i] >= mostImportant) && (weights[i] > minimumFear)) {
 				// Must be either more freightening, or closer
 				
 				if ((weights[i] > mostImportant) ||
 						(distances[i] < (importantIndex == -1 ? Double.POSITIVE_INFINITY : distances[importantIndex]))) { 
 					secondMostImportant = mostImportant;
 					mostImportant = weights[i];
 					
 					secondMotivation = mainMotivation;
 					mainMotivation = objects[i];
 					
 					secondImportantIndex = importantIndex;
 					importantIndex = i;
 				}
 			} else if ((weights[i] >= secondMostImportant) && (weights[i] > minimumFear)) {
 				// Must be either more freightening, or closer
 				
 				if ((weights[i] > mostImportant) ||
 						(distances[i] < (secondImportantIndex == -1 ? Double.POSITIVE_INFINITY : distances[secondImportantIndex]))) {
 					secondMostImportant = weights[i];
 					secondMotivation = objects[i];
 					secondImportantIndex = i;
 				}
 			}
 		}
 		/*
 		if ((mostImportant >= 0) || (secondImportantIndex >= 0)) {
 			System.out.println("\n");
 			System.out.println("min: " + minimumFear);
 			System.out.println("main: " + (importantIndex >= 0 ? weights[importantIndex] : "n/a"));
 			System.out.println("second: " + (secondImportantIndex >= 0 ? weights[secondImportantIndex] : "n/a"));
 			for (int i = 0; i < weights.length; i++) {
 				System.out.println(i + ": " + weights[i] + (importantIndex == i ? "*" : "") + (secondImportantIndex == i ? "." : ""));
 			}
 		}
 		*/
 		// Now that we have that, we'll scale thrust to the normal of the weights
 
 		if (mainMotivation != null) {
 			if (secondMotivation != null) {
 				// OK, two things to handle
 				
 				double newThrustA = weights[importantIndex];
 				
 				newThrustA = calculateFromBrain(newThrustA, CARE_TO_THRUST_A_TERM, CARE_TO_THRUST_B_TERM, CARE_TO_THRUST_C_TERM);
 				
 				double newThrustB = weights[secondImportantIndex];
 				
 				newThrustB = calculateFromBrain(newThrustB, CARE_TO_THRUST_A_TERM, CARE_TO_THRUST_B_TERM, CARE_TO_THRUST_C_TERM);
 				
 				double splitFactor = (brain[THRUST_SPLIT] + 1.0) / 2.0;
 				
 				if (splitFactor < 0.5) {				// Make sure we base thrust more on
 					splitFactor = 1.0 - splitFactor;	//		the object we are more fearful of
 				}
 				
 				newThrustA = newThrustA * splitFactor;
 				newThrustB = newThrustB * (1.0 - splitFactor);	// These are supposed to be compliments
 				
 				// Now we have to add the two thrust vectors
 				
 				double theirXPosition = mainMotivation.getXPosition();
 				
 				if (Math.abs(getXPosition() - theirXPosition) > GravitoidsPanel.PANEL_WIDTH / 2.0) {
 					if (theirXPosition > getXPosition()) {
 						theirXPosition -= GravitoidsPanel.PANEL_WIDTH;
 					} else {
 						theirXPosition += GravitoidsPanel.PANEL_WIDTH;
 					}
 				}
 
 				double xThrust = Math.cos(headings[importantIndex]) * newThrustA * (getXPosition() > theirXPosition ? -1 : 1);
 				double yThrust = Math.sin(headings[importantIndex]) * newThrustA * (getXPosition() > theirXPosition ? -1 : 1);
 				
 				theirXPosition = secondMotivation.getXPosition();
 							
 				if (Math.abs(getXPosition() - theirXPosition) > GravitoidsPanel.PANEL_WIDTH / 2.0) {
 					if (theirXPosition > getXPosition()) {
 						theirXPosition -= GravitoidsPanel.PANEL_WIDTH;
 					} else {
 						theirXPosition += GravitoidsPanel.PANEL_WIDTH;
 					}
 				}
 				
 				double secondXThrust = Math.cos(headings[secondImportantIndex]) * newThrustB * (getXPosition() > theirXPosition ? -1 : 1);
 				double secondYThrust = Math.sin(headings[secondImportantIndex]) * newThrustB * (getXPosition() > theirXPosition ? -1 : 1);
 				
 				// Now we have to get that back into a thrust and thrust percentages
 				// Get the total thrust, then normalize the two vector parts
 				
 				double factor = Math.sqrt(Math.pow(xThrust + secondXThrust, 2.0) + Math.pow(yThrust + secondYThrust, 2.0));
 				
 				setThrust(factor);
 				
 				setXThrustPortion((xThrust + secondXThrust) / factor);
 				setYThrustPortion((yThrust + secondYThrust) / factor);
 			} else {	
 				// Just one, this is easy
 				
 				double newThrust = weights[importantIndex];
 				
 				newThrust = calculateFromBrain(newThrust, CARE_TO_THRUST_A_TERM, CARE_TO_THRUST_B_TERM, CARE_TO_THRUST_C_TERM);
 				
 				setThrust(newThrust);
 				
 				// Set our heading
 				
 				double theirXPosition = mainMotivation.getXPosition();
 				
 				if (Math.abs(getXPosition() - theirXPosition) > GravitoidsPanel.PANEL_WIDTH / 2.0) {
 					if (theirXPosition > getXPosition()) {
 						theirXPosition -= GravitoidsPanel.PANEL_WIDTH;
 					} else {
 						theirXPosition += GravitoidsPanel.PANEL_WIDTH;
 					}
 				}
 				
 				setXThrustPortion(Math.cos(headings[importantIndex]) * (getXPosition() > theirXPosition ? -1 : 1));
 				setYThrustPortion(Math.sin(headings[importantIndex]) * (getXPosition() > theirXPosition ? -1 : 1));
 			}
 		} else {
 			// We ain't scared of nothing
 			
 			setThrust(0.0);
 			setXThrustPortion(0.0);
 			setYThrustPortion(0.0);
 		}
 	}
 	
 	public void draw(Graphics g) {
 		// Figure out where we are
 		
 		int r = (int) getRadius();
 		
 		int ovalDrawX = (int) getXPosition() - r;
 		int ovalDrawY = (int) getYPosition() - r;
 		
 		// Draw us
 		
 		g.setColor(Color.BLUE);
 		g.fillOval(ovalDrawX, ovalDrawY, r * 2, r * 2);
 		
 		// Now, if requested and in existance, draw our motivation
 		
 		if (drawMotiviation) {
 			if (secondMotivation != null) {
 				g.setColor(Color.LIGHT_GRAY);
 				drawMotivation(g, secondMotivation);
 			}
 			if (mainMotivation != null) {
 				g.setColor(Color.BLUE);
 				drawMotivation(g, mainMotivation);
 			}
 		}
 		
 		if (drawThrust && getThrust() > 0.0) {
 			g.setColor(Color.RED);
 			g.drawLine((int) getXPosition(), (int) getYPosition(),
 						(int) (getXPosition() - getXThrustPortion() * getThrust()),
 						(int) (getYPosition() - getYThrustPortion() * getThrust()));
 		}
 		
 		if (drawGravitationalPull) {
 			g.setColor(Color.GREEN);
 			g.drawLine((int) getXPosition(), (int) getYPosition(),
 						(int) (getXPosition() + 100.0 * getXGravitationalForce()),
 						(int) (getYPosition() + 100.0 * getYGravitationalForce()));
 		}
 	}
 	
 	private void drawMotivation(Graphics g, GravitoidsObject motivation) {
 		// If we are doing things directly, this is easy.
 		// If we are doing a wrap around, we'll need to draw two lines.
 		
 		double xDistance = getXPosition() - motivation.getXPosition();
 		double yDistance = getYPosition() - motivation.getYPosition();
 		
 		if ((Math.abs(xDistance) < (GravitoidsPanel.PANEL_WIDTH / 2.0))
 				&& (Math.abs(yDistance) < (GravitoidsPanel.PANEL_HEIGHT / 2.0))) {
 			// The simple case. Just one line directly between the objects.
 			
 			g.drawLine((int) getXPosition(), (int) getYPosition(), (int) motivation.getXPosition(), (int) motivation.getYPosition());
 		} else {
 			// OK, we'll need two lines
 			// One from us to where the other object would be off screen
 			
 			double temporaryX = motivation.getXPosition();
 			double temporaryY = motivation.getYPosition();
 			
 			if (Math.abs(xDistance) > (GravitoidsPanel.PANEL_WIDTH / 2.0)) {
 				if (getXPosition() < motivation.getXPosition()) {
 					// We're to the left of them, move them to our left
 					
 					temporaryX = motivation.getXPosition() - GravitoidsPanel.PANEL_WIDTH;
 				} else {
 					// We're to the right of them, move them to our right
 					
 					temporaryX = motivation.getXPosition() + GravitoidsPanel.PANEL_WIDTH;
 				}
 			}
 			
 			if (Math.abs(yDistance) > (GravitoidsPanel.PANEL_HEIGHT / 2.0)) {
 				if (getYPosition() < motivation.getYPosition()) {
 					// We're above them, move them above us
 					
 					temporaryY = motivation.getYPosition() - GravitoidsPanel.PANEL_HEIGHT;
 				} else {
 					// We're below them, move them below us
 					
 					temporaryY = motivation.getYPosition() + GravitoidsPanel.PANEL_HEIGHT;
 				}
 			}
 			
 			g.drawLine((int) getXPosition(), (int) getYPosition(), (int) temporaryX, (int) temporaryY);
 			
 			// One from the object motivating us to where we would be off screen
 			
 			temporaryX = getXPosition();
 			temporaryY = getYPosition();
 			
 			if (Math.abs(xDistance) > (GravitoidsPanel.PANEL_WIDTH / 2.0)) {
 				if (motivation.getXPosition() < getXPosition()) {
 					// We're to the left of them, move them to our left
 					
 					temporaryX = getXPosition() - GravitoidsPanel.PANEL_WIDTH;
 				} else {
 					// We're to the right of them, move them to our right
 					
 					temporaryX = getXPosition() + GravitoidsPanel.PANEL_WIDTH;
 				}
 			}
 			
 			if (Math.abs(yDistance) > (GravitoidsPanel.PANEL_HEIGHT / 2.0)) {
 				if (motivation.getYPosition() < getYPosition()) {
 					// We're above them, move them above us
 					
 					temporaryY = getYPosition() - GravitoidsPanel.PANEL_HEIGHT;
 				} else {
 					// We're below them, move them below us
 					
 					temporaryY = getYPosition() + GravitoidsPanel.PANEL_HEIGHT;
 				}
 			}
 			
 			g.drawLine((int) motivation.getXPosition(), (int) motivation.getYPosition(), (int) temporaryX, (int) temporaryY);
 		}
 	}
 	
 	public static IntelligentGravitoidsShip mutate(IntelligentGravitoidsShip source) {
 		// Come up with the space to hold our new brain
 		
 		IntelligentGravitoidsShip ship = new IntelligentGravitoidsShip();
 		
 		double[] brain = ship.getBrain();
 		int[] evolutionDirection = ship.getEvolutionDirection();
 		
 		// Copy the characteristics of the source over
 		
 		for (int i = 0; i < BRAIN_SIZE; i++) {	// Randomly select someone's genes
 			brain[i] = source.getBrain()[i];
 		}
 		
 		// Now, mutation
 		
 		for (int i = 0; i < BRAIN_SIZE; i++) {
 			// First, we'll figure out what direction to evolve in
 			
 			if (Math.random() > 0.3) {
 				// Things go in the direction of our parrents
 				
 				evolutionDirection[i] = source.getEvolutionDirection()[i];
 			} else {
 				// New direction
 				
 				evolutionDirection[i] = Math.random() > 0.5 ? 1 : -1;
 			}
 			
 			// Now figure out if we are evolving
 			
 			double num = Math.random();
 			
 			if (num < MUTATION_RATE) {
 				// Total replacement
 				
 				double oldBrain = brain[i];
 				
 				brain[i] = Math.random() * 2.0 - 1.0;
 				
 				evolutionDirection[i] = brain[i] == oldBrain ? 0 : (brain[i] < oldBrain ? -1 : 1);
 			} else if (num < 2.0 * MUTATION_RATE) {
 				// Minor mutation
 				// See if we need a direction to mutate in
 				
 				if (evolutionDirection[i] == 0) {
 					evolutionDirection[i] = Math.random() > 0.5 ? 1 : -1;
 				}
 				
 				// Change value by up to MINI_MUTATION, plus or minus based on evolutionDirection
 				
 				brain[i] = brain[i] + (Math.random() * ((double) evolutionDirection[i])) * MINI_MUTATION;
 				
 				if (brain[i] > 1.0) {	// Clamp to the limits
 					brain[i] = 1.0;
 				} else if (brain[i] < -1.0) {
 					brain[i] = -1.0;
 				}
 			} else {
 				// Nothing changes
 			}
 		}
 		
 		// Return it
 		
 		return ship;
 	}
 	
 	public static IntelligentGravitoidsShip breed(IntelligentGravitoidsShip one, IntelligentGravitoidsShip two) {
 		// Come up with the space to hold our new brain
 		
 		IntelligentGravitoidsShip ship = new IntelligentGravitoidsShip();
 		
 		double[] brain = ship.getBrain();
 		int[] evolutionDirection = ship.getEvolutionDirection();
 		
 		// Do the simple breeding
 		
 		for (int i = 0; i < BRAIN_SIZE; i++) {	// Randomly select someone's genes
 			double coin = Math.random();
 			
 			brain[i] = coin > 0.5 ? one.brain[i] : two.brain[i];
 		}
 		
 		// Now, handle mutation
 		
 		for (int i = 0; i < BRAIN_SIZE; i++) {
 			// First, we'll figure out what direction to evolve in
 			
 			if (Math.random() > 0.1) {
 				// Things go in the direction of our parrents
 				
 				evolutionDirection[i] = Math.random() > 0.5 ? one.evolutionDirection[i] : two.evolutionDirection[i];
 			} else {
 				// New direction
 				
 				evolutionDirection[i] = Math.random() > 0.5 ? 1 : -1;
 			}
 			
 			// Now figure out if we are evolving
 			
 			double num = Math.random();
 			
 			if (num < MUTATION_RATE) {
 				// Total replacement
 				
 				double oldBrain = brain[i];
 				
 				brain[i] = Math.random() * 2.0 - 1.0;
 				
 				evolutionDirection[i] = brain[i] == oldBrain ? 0 : (brain[i] < oldBrain ? -1 : 1);
 			} else if (num < 2.0 * MUTATION_RATE) {
 				// Minor mutation
 				// See if we need a direction to mutate in
 				
 				if (evolutionDirection[i] == 0) {
 					evolutionDirection[i] = Math.random() > 0.5 ? 1 : -1;
 				}
 				
 				// Change value by up to MINI_MUTATION, plus or minus based on evolutionDirection
 				
 				brain[i] = brain[i] + (Math.random() * ((double) evolutionDirection[i])) * MINI_MUTATION;
 				
 				if (brain[i] > 1.0) {	// Clamp to the limits
 					brain[i] = 1.0;
 				} else if (brain[i] < -1.0) {
 					brain[i] = -1.0;
 				}
 			} else {
 				// Nothing changes
 			}
 		}
 		
 		// Return it
 		
 		return ship;
 	}
 }
