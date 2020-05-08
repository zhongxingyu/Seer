 package com.climber;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.math.Rectangle;
 
 public class Quadtree {
 	private Node root;
 	
 	public Quadtree(Rectangle r) {
 		root = new Node(r, null);
 		root.createChildrenQuads(r);
 		
 	}
 	
 	public void insert(Sprite s) {
 		root = insert(root, s);
 	}
 	
 	private Node insert(Node n, Sprite s) {
 		Rectangle temp = s.getRectangle();
 		
 		if (!n.childrenHaveBeenCreated()) {
 			n.createChildrenQuads(n.m_quadRect);
 		}
 		
 		//first check which node it can go into
 
 		if ( n.NW.m_quadRect.contains(temp)) {
 			n.NW = insert(n.NW, s);
 			//return n;
 		} else if ( n.NE.m_quadRect.contains(temp)) {
 			n.NE = insert(n.NE, s);
 			//return n;
 		} else if ( n.SW.m_quadRect.contains(temp)) {
 			n.SW = insert(n.SW, s);
 			//return n;
 		} else if ( n.SE.m_quadRect.contains(temp)) {
 			n.SE = insert(n.SE, s);
 			//return n;
 		} else {
 		
 			n.m_objects.add(s);
 		}
 		
 		return n;
 	}
 	
 	public ArrayList<Object> getListOfAll() {
 		return getListOfAll(root);
 	}
 	
 	private ArrayList<Object> getListOfAll(Node n) {
 		ArrayList<Object> list = new ArrayList<Object>();
 		list.addAll(n.m_objects);
 			
 		if ( !n.childrenHaveBeenCreated()) {
 			return list;
 		}
 
 		list.addAll(getListOfAll(n.NW));
 		list.addAll(getListOfAll(n.NE));
 		list.addAll(getListOfAll(n.SW));
 		list.addAll(getListOfAll(n.SE));
 		
 		return list;
 	}
 	
 	public ArrayList<Rectangle> getAllQuads() {
 		return getAllQuads(root);
 	}
 	
 	private ArrayList<Rectangle> getAllQuads(Node n) {
 		ArrayList<Rectangle> rects = new ArrayList<Rectangle>();
 		rects.add(n.m_quadRect);
 		
 		if (!n.childrenHaveBeenCreated())
 			return rects;
 
 		rects.addAll(getAllQuads(n.NW));
 		rects.addAll(getAllQuads(n.NE));
 		rects.addAll(getAllQuads(n.SW));
 		rects.addAll(getAllQuads(n.SE));
 		
 		return rects;
 	}
 	
 	public void simulate(Sprite ground) {
 		simulate(root, ground);
 	}
 	
 	private void simulate(Node n, Sprite ground) {
 		Rectangle groundRect = ground.getRectangle();
 		// My Plan: 
 		// Go through list of objects in current node
 		// for each one that is falling, simulate it
 		// check if it hit the ground, if so then stop
 		// check if it hit any other blocks
 		//    do this by gathering all of the blocks in the levels above it, and then all of the blocks in the layers below it that are in quads that it over laps
 		// stop if it hit any of them
 		// check to see if it is still in its quad
 		// if not, readd it to the list
 
 		for ( int i = 0; i < n.m_objects.size(); i++ ) {
 			Sprite current = (Sprite)n.m_objects.get(i);
 			
 			if (!current.isMb_isFalling())
 				continue;
 			
 			//simulate it
 			current.simulate();
 			
 			Rectangle currentRect = current.getRectangle();
 			
 			//check if it hit the ground
 			boolean hitGround = false;
 			if ( currentRect.y <= groundRect.y + groundRect.height) {
 				current.setFalling(false);
 				hitGround = true;
 			}
 			
 			//now check if it is outside of its out quad
 			if (!n.m_quadRect.contains(currentRect)) {
 				n.m_objects.remove(current);
 				insert(current);
 				continue;
 			}
 			
 			//it stopped moving, so it doesn't need to check against any other blocks
 			if ( hitGround )
 				continue;
 			
 			//now check it against all of the other blocks
 			//first get all of the blocks above it
 			ArrayList<Object> toCheck = getAllObjectsAbove(n);
 			//ArrayList<Object> toCheck = new ArrayList<Object>();
 			//now get all of the blocks below it
 			toCheck.addAll(getAllBlocksBelow(n, currentRect));
 			//getAllBlocksBelow also gets all of the blocks in the current node, so you're set there
 			//but don't forget to remove yourself from the list so you don't check against yourself!
 			toCheck.remove(current);
 			toCheck.remove(current);
 			
 			//now go and check them all!
 			for ( int j = 0; j < toCheck.size(); j++) {
 				Sprite temp = (Sprite)toCheck.get(j);
 				Rectangle tempRect = temp.getRectangle();
 				
 				if ( currentRect.overlaps(tempRect)) {
 					current.setFalling(false);
 					break;
 				}
 			}
 		}
 		
 		//now simulate all of the children nodes
 		if (!n.childrenHaveBeenCreated())
 			return;
 
 		simulate(n.NW, ground);
 		simulate(n.NE, ground);
 		simulate(n.SW, ground);
 		simulate(n.SE, ground);
 	}
 	
 	private ArrayList<Object> getAllBlocksBelow(Node n, Rectangle r) {
 		//first check to make sure that the current node has children - otherwise bail
 		if (!n.childrenHaveBeenCreated())
 			return new ArrayList<Object>();
 		
 		ArrayList<Object> list = new ArrayList<Object>();
 		//now check and see if the rectangle overlaps any children, if so then grab all of its items and search its kids
 		if (r.overlaps(n.NW.m_quadRect)) {
 			list.addAll(getAllBlocksBelow(n.NW, r));
 		}
 		if (r.overlaps(n.NE.m_quadRect)) {
 			list.addAll(getAllBlocksBelow(n.NE, r));
 		}
 		if (r.overlaps(n.SW.m_quadRect)) {
 			list.addAll(getAllBlocksBelow(n.SW, r));
 		}
 		if (r.overlaps(n.SE.m_quadRect)) {
 			list.addAll(getAllBlocksBelow(n.SE, r));
 		}
 		
 		list.addAll(n.m_objects);
 		return list;
 	}
 	
 	/**
 	 * Gets all objects above the current node, by traveling up the tree through the current nodes parents until it reaches the root,
 	 * grabbing all of the objects in all of the levels it passes
 	 * @param n
	 */
	/*private ArrayList<Object> getAllObjectsAbove(Node n) {
		if ( n == null )
			return new ArrayList<Object>(); //we need to return an arraylist, so return an empty one
		
		if ( n.m_parent == null) {
			return n.m_objects;
		}
		
		ArrayList<Object> list = new ArrayList<Object>();
		list.addAll(getAllObjectsAbove(n.m_parent));
		list.addAll(n.m_objects);
		return list;
	}*/
	
 	private ArrayList<Object> getAllObjectsAbove(Node n) {
 		if ( n == null )
 			return new ArrayList<Object>();
 		
 		ArrayList<Object> a = new ArrayList<Object>();
 		a.addAll(n.m_objects);
 		
 		if ( n.m_parent == null )
 			return a;
 		
 		a.addAll(getAllObjectsAbove(n.m_parent));
 		return a;
 	}
 	
 	public ArrayList<Object> query(Rectangle r) {
 		return getAllBlocksBelow(root, r);
 	}
 	
 	private class Node {
 		//the 4 quads, all nodes have 4 quads in them
 		public Node NW, NE, SW, SE;
 		public Node m_parent;
 		public ArrayList<Object> m_objects;
 		public Rectangle m_quadRect;
 		
 		public Node(Rectangle r, Node parent) {
 			m_quadRect = r;
 			m_objects = new ArrayList<Object>();
 			m_parent = parent;
 		}
 		
 		public void createChildrenQuads(Rectangle r) {
 			//create the quads beneath it as well
 			NW = new Node(new Rectangle(r.x, r.y + (r.height/2.0f), r.width/2.0f, r.height/2.0f), this);
 			NE = new Node(new Rectangle(r.x + (r.width/2.0f), r.y + (r.height/2.0f), r.width/2.0f, r.height/2.0f), this);
 			SW = new Node(new Rectangle(r.x, r.y, r.width/2.0f, r.height/2.0f), this);
 			SE = new Node(new Rectangle(r.x + (r.width/2.0f), r.y, r.width/2.0f, r.height/2.0f), this);
 		}
 		
 		/**
 		 * Checks if the children have already been created or not
 		 * @return
 		 */
 		public boolean childrenHaveBeenCreated() {
 			return !(NW == null);
 		}
 	}
 }
