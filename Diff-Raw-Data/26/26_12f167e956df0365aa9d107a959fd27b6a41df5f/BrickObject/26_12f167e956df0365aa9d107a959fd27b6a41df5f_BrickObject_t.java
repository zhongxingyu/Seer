 package brickManager;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import objectManager.GameObject;
 import objectManager.ObjectChangeEvent;
 import objectManager.ObjectType;
 import objectManager.ObjectChangeEvent.ObjectChangeType;
 
 import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
 
 import physicsManager.PhysicalObject;
 import utilityManager.MathBox;
 
 public abstract class BrickObject extends PhysicalObject {
 	protected ArrayList<Brick> bricks;
 	protected ArrayList<ArrayList<Integer>> adjacencyList = new ArrayList<ArrayList<Integer>>();
 	
 	protected boolean bricksChange = false;
 	
 	public BrickObject(ObjectType objectType, GameObject source, Hashtable<String, Double> values, ArrayList<Brick> bricks, ArrayList<ArrayList<Integer>> adjacencyList) {
 		super(objectType, source, values);
 		this.bricks = bricks;
 		this.adjacencyList = adjacencyList;
 		
 		if (bricks != null) {
 			for (Brick brick : bricks) {
 				brick.setParent(this);
 			}
 		}
 	}
 	
 	public void updateBricks() {
 		for (Brick brick : bricks) {
 			if (brick.getHealth() <= 0) {
 				brick.setExploding(true);
 				brick.setExplosionTimer(brick.getExplosionTimer() - 1);
 				
				if (brick.getHealth() <= 0) {
 					brick.setAlive(false);
 				}
 			}
 		}
 		removeDeadBricks();
 	}
 	
 	public Brick getClosestBrick(Vector2D colliderPosition) {
 		if (bricks.isEmpty()) {
 			return null;
 		}
 		
 		Brick closest = bricks.get(0);
 		Vector2D absolutePosition = MathBox.rotatePoint(closest.getPosition(), orientation).add(position);
 		double min = absolutePosition.distance(colliderPosition);
 		for (Brick brick : bricks) {
 			absolutePosition = MathBox.rotatePoint(brick.getPosition(), orientation).add(position);
 			double distance = absolutePosition.distance(colliderPosition);
 			if (distance < min && !brick.isExploding() && brick.isAlive()) {
 				closest = brick;
 				min = distance;
 			}
 		}
 		return closest;
 	}
 	
 	private void removeDeadBricks() {
 		ArrayList<Brick> toRemove = new ArrayList<Brick>();
 		for (Brick brick : bricks) {
			if (!brick.isAlive() && brick.getExplosionTimer() <= 0) {
 				toRemove.add(brick);
 			}
 		}
 		
 		for (Brick brick : toRemove) {
 			//register change to bricks for updating the view of this BrickObject
 			bricksChange = true;
 			
 			//get the indices if the bricks connected to the brick being removed
 			ArrayList<Integer> indicesToRemove = new ArrayList<Integer>();
 			for (Integer connected : adjacencyList.get(brick.getIndex())) {
 				indicesToRemove.add(connected);
 			}
 			
 			//clear edges of brick being removed
 			adjacencyList.get(brick.getIndex()).clear();
 			
 			for (Integer indexToRemove : indicesToRemove) {
 				adjacencyList.get(indexToRemove).remove((Integer) brick.getIndex());
 			}
 			
 			bricks.remove(brick);
 			
 			updateObservers(new ObjectChangeEvent(this, ObjectChangeType.CREATION, getFragments()));
 		}
 	}
 	
 	private ArrayList<PhysicalObject> getFragments() {
 		ArrayList<PhysicalObject> fragments = new ArrayList<PhysicalObject>();
 		
 		ArrayList<Brick> connected = getBricks(getConnected(new ArrayList<Integer>(), 0));
 		if (connected.size() < bricks.size()) {
 			//get unconnected bricks
 			ArrayList<Brick> unconnected = new ArrayList<Brick>();
 			for (Brick brick : bricks) {
 				if (!connected.contains(brick)) {
 					unconnected.add(brick);
 				}
 			}
 			
 			while (!unconnected.isEmpty()) {
 				//get bricks connected to unconnected brick
 				ArrayList<Brick> newGroup = getBricks(getConnected(new ArrayList<Integer>(), unconnected.get(0).getIndex()));
 				
 				//get adjacency list for above bricks
 				ArrayList<ArrayList<Integer>> newAdjacencyList = new ArrayList<ArrayList<Integer>>();
 				
 				for (Brick brick : newGroup) {
 					newAdjacencyList.add(adjacencyList.get(brick.getIndex()));
 				}
 
 				//remove bricks in newGroup from unconnected
 				for (Brick brick : newGroup) {
 					unconnected.remove(brick);
 				}
 				
 				//remove fragment bricks from bricks
 				for (Brick brick : newGroup) {
 					bricks.remove(brick);
 				}
 				
 				//create new fragment
 				fragments.add(createFragment(newGroup, newAdjacencyList));
 			}
 		}
 		
 		return fragments;
 	}
 	
 	private ArrayList<Integer> getConnected(ArrayList<Integer> connected, int start) {
 		if (!connected.contains(start)) {
 			connected.add(start);
 		}
 		for (Integer i : adjacencyList.get(start)) {
 			if (!connected.contains(i)) {
 				connected.add(i);
 				getConnected(connected, i);
 			}
 		}
 		
 		return connected;
 	}
 	
 	public Brick getBrickFromIndex(int index) {
 		for (Brick brick : bricks) {
 			if (brick.getIndex() == index) {
 				return brick;
 			}
 		}
 		return null;
 	}
 	
 	private ArrayList<Brick> getBricks(ArrayList<Integer> indices) {
 		ArrayList<Brick> matches = new ArrayList<Brick>();
 		for (Brick brick : bricks) {
 			if (indices.contains(brick.getIndex())) {
 				matches.add(brick);
 			}
 		}
 		return matches;
 	}
 	
 	public ArrayList<Brick> getAdjacentBricks(int index) {
 		ArrayList<Brick> adjacentBricks = new ArrayList<Brick>();
 		ArrayList<Integer> indices = adjacencyList.get(index);
 		for (Brick brick : bricks) {
 			if (indices.contains(brick.getIndex())) {
 				adjacentBricks.add(brick);
 			}
 		}
 		return adjacentBricks;
 	}
 	
 	@Override
 	public ArrayList<double[]> getLines() {
 		ArrayList<double[]> lines = new ArrayList<double[]>();
 		
 		//position of corners of square brick
 		
 		for (Brick brick : bricks) {
 			//get edges of current brick, each edge is made up of 4 points
 			ArrayList<double[]> brickLines = brick.getLines();
 			
 			//rotate edges by object orientation
 			for (int i = 0; i < brickLines.size(); i++) {
 				double[] line = new double[] {
 						brickLines.get(i)[0],// + position.getX(), 
 						brickLines.get(i)[1],// + position.getY(), 
 						brickLines.get(i)[2],// + position.getX(), 
 						brickLines.get(i)[3],// + position.getY()
 				};
 				
 				double[] p1 = MathBox.rotatePoint(new double[] {line[0], line[1]}, orientation);
 				double[] p2 = MathBox.rotatePoint(new double[] {line[2], line[3]}, orientation);
 				
 				p1[0] += position.getX();
 				p1[1] += position.getY();
 				p2[0] += position.getX();
 				p2[1] += position.getY();
 				
 //				double[] p1 = MathBox.rotatePoint(new double[] {line[0], line[1]}, new double[] {position.getX(), position.getY()}, orientation);
 //				double[] p2 = MathBox.rotatePoint(new double[] {line[2], line[3]}, new double[] {position.getX(), position.getY()}, orientation);
 				brickLines.set(i, new double[] {p1[0], p1[1], p2[0], p2[1]});
 			}
 			
 			//add lines to line storage
 			lines.addAll(brickLines);
 		}
 
 		return lines;
 	}
 	
 	protected abstract PhysicalObject createFragment(ArrayList<Brick> fragmentBricks, ArrayList<ArrayList<Integer>> adjacencyList);
 }
