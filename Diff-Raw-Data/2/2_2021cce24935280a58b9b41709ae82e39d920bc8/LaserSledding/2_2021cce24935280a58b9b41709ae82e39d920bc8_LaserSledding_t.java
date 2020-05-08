 /*
  *                This file is part of Laserinne.
  * 
  *  Laser projections in public space, inspiration and
  *  information, exploring the aesthetic and interactive possibilities of
  *  laser-based displays.
  * 
  *  http://www.laserinne.com/
  * 
  * Laserinne is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Laserinne is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Laserinne. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.laserinne.lasersledding;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import processing.core.PApplet;
 
 @SuppressWarnings("serial")
 public class LaserSledding extends com.laserinne.util.TwoPlayerCompetition {
 
 	final int COLLECTIBLE_NUMBER = 5;
 	
 	ArrayList<Collectible> p1Collectibles, p2Collectibles;
 	int pointsP1, pointsP2;
 	
 	int scanSpeed;
     
     public static void main(String args[]) {
         PApplet.main(new String[] { LaserSledding.class.getCanonicalName() });
     }
     
 	public void setup() {
 		super.setup();
 		
 		// Allocate memory skiers for collectibles
 		leftSkier = new LaserSleddingSkierContestant();
 		rightSkier = new LaserSleddingSkierContestant();
 		p1Collectibles = new ArrayList<Collectible>();
 		p2Collectibles = new ArrayList<Collectible>();
 		pointsP1 = 0;
 		pointsP2 = 0;
 		
 		// Create the collectibles and their positions.
 		for(int i = 0; i < COLLECTIBLE_NUMBER; i++) {
 			int x = (int)random(width/2);
 			int y = (int)random(height-50);
 			
 			p1Collectibles.add(new Collectible(this, x, y));
 			p2Collectibles.add(new Collectible(this, x + width / 2, y));
 		}
 		
 		// TODO: Implement interface for sorting
 		// Sort p1Collectibles
 		Collections.sort(p1Collectibles, new Comparator<Collectible>() {
 
 			@Override
 			public int compare(Collectible c1, Collectible c2) {
 				return (int)(c1.location.y - c2.location.y);
 			}
 		});
 		
 		// Sort p2Collectibles
 		Collections.sort(p2Collectibles, new Comparator<Collectible>() {
 			
 			@Override
 			public int compare(Collectible c1, Collectible c2) {
 				return (int)(c1.location.y - c2.location.y);
 			}
 		});
 	}
 	
 	public void draw() {
 	    super.draw();
 	    
         line(width / 2, 0, width / 2, height);
         
 		// Checks if leftSkier has crossed the finish line and calculates points
 		if(leftSkier.finished() && pointsP1 == 0) {
 			((LaserSleddingSkierContestant) leftSkier).setScore(COLLECTIBLE_NUMBER - p1Collectibles.size());
 		}
 		
 		// Checks if rightSkierhas crossed the finish line and calculates points
 		if(rightSkier.finished() && pointsP2 == 0) {
 			((LaserSleddingSkierContestant) rightSkier).setScore(COLLECTIBLE_NUMBER - p2Collectibles.size());
 		}
 		
 		if (laserOn) {
 	        // Display Skiers
 //	        leftSkier.setPosition(mouseX, mouseY);
 	        leftSkier.draw(g);
 //	        rightSkier.setPosition(mouseX+width/2, mouseY);
 	        rightSkier.draw(g);
 	        
 		    drawWithLaser();
 		}
 	}
 	
 	protected void drawGame() {
         leftSkier.update();
         rightSkier.update();
         
         // Check location and display collectibles
         for(int i = 0; i < p1Collectibles.size(); i++) {
             if(p1Collectibles.get(i).checkLocation((LaserSleddingSkierContestant) leftSkier)) {
                 p1Collectibles.remove(i);
             }
             else {
                 p1Collectibles.get(i).update(1);
                 p1Collectibles.get(i).display();
             }
         }
             
         for(int i = 0; i < p2Collectibles.size(); i++) {
             if(p2Collectibles.get(i).checkLocation((LaserSleddingSkierContestant) rightSkier)) {
                 p2Collectibles.remove(i);
             }
             else {
                 p2Collectibles.get(i).update(2);
                 p2Collectibles.get(i).display();
             }
         }
 	}
 	
 public void keyPressed() {
 		
 		if(key == CODED) {
 			if(keyCode == UP) {
 				scanSpeed += 1000;
 				System.out.println("Scanspeed: " + scanSpeed);
 			}
 			if(keyCode == DOWN) {
 				scanSpeed -= 1000;
 				System.out.println("Scanspeed: " + scanSpeed);
 			}
 		} else if(key == 'n') {
 			// Allocate memory for skier and collectible
 			p1Collectibles = new ArrayList<Collectible>();
 			p2Collectibles = new ArrayList<Collectible>();
 			pointsP1 = 0;
 			pointsP2 = 0;
 			
 			// Create the collectibles and their positions.
 			for(int i = 0; i < COLLECTIBLE_NUMBER; i++) {
 				int x = (int)random(width/2);
 				int y = (int)random(height-50);
 				
 				p1Collectibles.add(new Collectible(this, x, y));
				p2Collectibles.add(new Collectible(this, x + width / 2, y));
 			}
 			
 			// TODO: Implement interface for sorting
 			// Sort p1Collectibles
 			Collections.sort(p1Collectibles, new Comparator<Collectible>() {
 
 				@Override
 				public int compare(Collectible c1, Collectible c2) {
 					return (int)(c1.location.y - c2.location.y);
 				}
 			});
 			
 			// Sort p2Collectibles
 			Collections.sort(p2Collectibles, new Comparator<Collectible>() {
 				
 				@Override
 				public int compare(Collectible c1, Collectible c2) {
 					return (int)(c1.location.y - c2.location.y);
 				}
 			});
 			
 			// Reset the skiers
 			reset();
 		} else {
 		    super.keyPressed();
 		}
 	}
 }
