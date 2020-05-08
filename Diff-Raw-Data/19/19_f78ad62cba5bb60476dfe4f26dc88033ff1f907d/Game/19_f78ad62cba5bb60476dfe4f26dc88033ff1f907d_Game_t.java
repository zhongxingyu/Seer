 package it.univr.gameoflife;
 
 import java.awt.Point;
 import java.util.HashSet;
 import java.util.Set;
 
 public class Game {
 	
 	private Grid grid;
 	private final Set<Point> deadCellCoordinates;
 	private int width;
 	private int height;
 
 	public Game(int width, int height) {
 		this.grid = new Grid(width, height);
 		this.deadCellCoordinates = new HashSet<Point>();
 		this.width = width;
 		this.height = height;
 	}
 	
 	public Grid getGrid() {
 		return this.grid;
 	}
 	
 	private class NextGeneration {
 	
 		private int i;
 		private Grid next;
 			
		private NextGeneration(int numOfThreads) {
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
 			
			Game.this.grid = next;
 		}
 		
 		private synchronized int nextIndex() {
 			return i++;
 		}
 		
 		private class Slave extends Thread {
 			
 			@Override
 			public void run() {
				int slaveIndex, limit = height * width;
				while((slaveIndex = NextGeneration.this.nextIndex()) < limit) {
 					Point p = new Point(slaveIndex / width, slaveIndex % width);
 					if(!(deadCellCoordinates.contains(p))) {
 						int neighbours = countNeighbours(p);
						if (neighbours == 3 || (neighbours == 2 && grid.isAlive(p)))
 							next.changeState(p);
 					}	
 				}
 			}
 
 			private int countNeighbours(Point p) {
 				int count = 0;
 				for(int dx = -1; dx <= 1; dx++)
 					for(int dy = -1; dy <= 1; dy++) {
 						Point neighbour = new Point(p.x + dx, p.y + dy);
						if ((dx != 0 || dy != 0) && neighbour.x >= 0 && neighbour.x < height && neighbour.y >= 0 && neighbour.y < width && grid.isAlive(neighbour))
 							count++;
 					}
 				return count;
 			}
 		}
 		
 	}
 }
 
