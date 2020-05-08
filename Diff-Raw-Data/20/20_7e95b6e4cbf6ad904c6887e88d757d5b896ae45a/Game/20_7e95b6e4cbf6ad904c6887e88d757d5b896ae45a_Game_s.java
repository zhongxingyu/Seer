 package it.univr.gameoflife;
 
 import java.awt.Point;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Game {
 	
 	private Grid grid;
	private final Set<Point> deadCellsCoordinates;
 	private int width;
 	private int height;
 
 	public Game(int width, int height) {
 		this.grid = new Grid(width, height);
		this.deadCellsCoordinates = new HashSet<Point>();
 		this.width = width;
 		this.height = height;
 	}
 	
 	public Grid getGrid() {
 		return this.grid;
 	}
 	
 	private class NextGeneration {
 	
 		private int i;
 		private Grid current;
 		private Grid next;
 			
 		private NextGeneration (int numOfThreads) {
 			current = Game.this.grid;
 			next = new Grid(width, height);
 			
 			Slave[] slaves = new Slave[numOfThreads];
 			for(int pos = 0; pos < slaves.length; pos++) {
 				slaves[pos] = new Slave();
 				slaves[pos].start();
 			}	
 			
 			for(Slave slave: slaves)
 				try {
 					slave.join();
 				} catch(InterruptedException e) {}
 			
 			current = next;
 		}
 		
 		private synchronized int nextIndex() {
 			return i++;
 		}
 		
 		private class Slave extends Thread {
 			
 			@Override
 			public void run() {
 				int slaveIndex;
 				while((slaveIndex = NextGeneration.this.nextIndex()) < height * width) {
					Point p = new Point((int) slaveIndex / width, (int) slaveIndex % width);
					if(!(deadCellsCoordinates.contains(p))) {
						if(current.isAlive(p) && (countNeighbours(p) < 2 || countNeighbours(p) > 3))
							next.changeState(p);
						if((!(current.isAlive(p))) && countNeighbours(p) == 3)
 							next.changeState(p);
 					}	
 				}
 			}
 
 			private int countNeighbours(Point p) {
 				int count = 0;
 				for(int dx = -1; dx <= 1; dx++)
 					for(int dy = -1; dy <= 1; dy++) {
						Point pt = new Point(p.x + dx, p.y + dy);
						if(pt.x >= 0 && pt.x < height && pt.y >= 0 && pt.y < width && current.isAlive(pt))
 							count++;
 					}
 				return count;
 			}
 		}
 		
 	}
 }
 
