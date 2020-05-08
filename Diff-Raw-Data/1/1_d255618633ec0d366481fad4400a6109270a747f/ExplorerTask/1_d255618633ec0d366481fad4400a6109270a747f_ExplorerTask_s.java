 package spp.pakpos.controllers;
 
 import java.util.ArrayList;
 
 import spp.pakpos.PakPos;
 import spp.pakpos.models.PakPosPath;
 import spp.pakpos.models.Path;
 import spp.pakpos.models.PosArea;
 
 public class ExplorerTask implements Runnable {
 	private ArrayList<Integer> visitedNodes;
 	private int node;
 	private int totalDistance;
 	
 	public ExplorerTask(ArrayList<Integer> visited, int currentNode, int distance){
 		visitedNodes = visited;
 		node = currentNode;
 		totalDistance = distance;
 	}
 	
 	@Override
 	public void run() {
 		int inc = PakPos.threadCount.incrementAndGet();
 		System.out.println(Thread.currentThread().getName() + " is starting: " + inc);
 		
 		if (!PakPos.isVisited(visitedNodes, node)){
 			visitedNodes.add(node);
 			System.out.println(Thread.currentThread().getName() + ": " +visitedNodes.toString());
 			System.out.println(Thread.currentThread().getName() + ": " + PakPos.isArrive(node) + ": " + node);
 			if (PakPos.isArrive(node)){
 				Path validOne = new Path(visitedNodes, totalDistance);
 				PakPosPath.savePath(validOne);
 				System.out.println(Thread.currentThread().getName() + " has saved its works, " + validOne.toString());
 			} else {
 				ArrayList<Integer> neighbours = PosArea.getNeighbours(node);
 				System.out.println(Thread.currentThread().getName() + " detects neighbour " + neighbours.toString());
 				for (int i=0; i<neighbours.size(); i++){
 					System.out.println(Thread.currentThread().getName() + " is checking ");
 					System.out.println(Thread.currentThread().getName() + ": " + neighbours.get(i) + ", " + PosArea.isNeighbour(neighbours.get(i)));
 					if (PosArea.isNeighbour(neighbours.get(i))){
 						System.out.println(Thread.currentThread().getName() + " is spawning new thread");
 						// Spawn new thread
 						PakPos.addTask(
 							new ExplorerTask(new ArrayList<Integer>(visitedNodes), i, totalDistance + neighbours.get(i))
 						);
 					} else {
 						System.out.println(Thread.currentThread().getName() + " detects invalid neighbour");
 					}
 				}
 			}
 		} else {
 			System.out.println(Thread.currentThread().getName() + " hit visited node");
 		}
 		System.out.println(Thread.currentThread().getName() + " has done: " + PakPos.threadCount.decrementAndGet());
 		
 		if (PakPos.threadCount.get() == 0){
 			System.out.println(Thread.currentThread().getName() + " is trying to shutdown the pool");
 			PakPos.threadPool.shutdownNow();
			Thread.currentThread().yield();
 		}
 	}
 
 }
